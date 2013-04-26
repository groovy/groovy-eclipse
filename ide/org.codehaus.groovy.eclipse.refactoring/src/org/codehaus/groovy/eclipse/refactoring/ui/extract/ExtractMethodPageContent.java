/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.ui.extract;

import groovyjarjarasm.asm.Opcodes;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyMethodRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyConventionsBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.utils.StatusHelper;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.Signature;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Composite for Userinput for ExtractMethod refactoring
 *
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class ExtractMethodPageContent extends Composite implements Observer {

    private final ExtractGroovyMethodRefactoring extractMethodRefactoring;
	private final ExtractMethodPage extractMethodPage;

	private Text txtNewMethodName;
	private Text txtPreviewCall;
	private Table tblParameters;

	private static final String MODIFIER_DEF = "def";
	private static final String MODIFIER_PROTECTED = "protected";
	private static final String MODIFIER_PRIVATE = "private";
	private static final String MODIFIER_NONE = "none";
	private final String[] possibleModifiers = { MODIFIER_DEF, MODIFIER_PROTECTED, MODIFIER_PRIVATE, MODIFIER_NONE};
	private static final int DEFAULT_MODIFIER = 2;

	private Composite accessModifierComposite;
	private Composite newMethodNameComposite;
	private Composite parameterComposite;
	private Composite previewComposite;

	private Button btnDown;
	private Button btnUp;

	private TableEditor editor;

	private final Map<String, String> renameVariablesMap = new HashMap<String, String>();
	private boolean firstPreviewEver = true;

    public ExtractMethodPageContent(Composite parent, ExtractGroovyMethodRefactoring refactoring, ExtractMethodPage extractMethodPage) {
		super(parent, SWT.NONE);
		this.extractMethodRefactoring = refactoring;
		this.extractMethodPage = extractMethodPage;
		if (refactoring != null) {
			refactoring.addObserver(this);
		}

		setLayout(new GridLayout());
		GridData compositeLData = new GridData();
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessHorizontalSpace = true;
		setLayoutData(compositeLData);

		createNewMethodNameComposite(this);
		createAccessModifierComposite(this);
		createParameterComposite(this);
		createPreviewComposite(this);
		initializeValues(-1);
		layout();
	}

	private void initializeValues(int selectionIndex) {
		if (extractMethodRefactoring != null) {

			setModifier();

			if(extractMethodRefactoring.getCallAndMethHeadParameters().length > 0){
				tblParameters.removeAll();
				for (Parameter param : extractMethodRefactoring.getCallAndMethHeadParameters()) {
					TableItem tblItem = new TableItem(tblParameters, SWT.NONE);
                    tblItem.setText(0, createSimpleTypeName(param.getType()));
					setVariableNameInTable(param, tblItem);
				}
				tblParameters.setSelection(selectionIndex);
				updateButtonsEnabled(tblParameters);
			}
			updateView();
		}
	}

    private String createSimpleTypeName(ClassNode node) {
        String name = node.getName();
        if (name.startsWith("[")) {
            int arrayCount = Signature.getArrayCount(name);
            String noArrayName = Signature.getElementType(name);
            String simpleName = Signature.getSignatureSimpleName(noArrayName);
            StringBuilder sb = new StringBuilder();
            sb.append(simpleName);
            for (int i = 0; i < arrayCount; i++) {
                sb.append("[]");
            }
            return sb.toString();
        } else {
            return node.getNameWithoutPackage();
        }
    }

	private void setVariableNameInTable(Parameter param, TableItem tblItem) {
		String variableName = param.getName();
		if(renameVariablesMap.containsKey(variableName))
			tblItem.setText(1, renameVariablesMap.get(variableName));
		else
			tblItem.setText(1, variableName);
	}

	/**
	 * Update View and check the given Values
	 */
	private void updateView() {
		String methodHead = extractMethodRefactoring.getMethodHead();
		if(firstPreviewEver){
			createDummyMethodHead(methodHead);
		}else{
			txtPreviewCall.setText(methodHead);
		}
		RefactoringStatus status = validateGroovyIdentifiers();
		checkForDuplicateVariableNames(status);
		extractMethodPage.setPageComplete(status);
	}

	private void checkForDuplicateVariableNames(RefactoringStatus status) {
		HashSet<String> uniquenessTestSet = new HashSet<String>();
		for (Parameter p : extractMethodRefactoring.getCallAndMethHeadParameters()){
			if(!uniquenessTestSet.add(p.getName())){
				String errorMsg = MessageFormat.format(GroovyRefactoringMessages.ExtractMethodWizard_DuplicateVariableName, p.getName());
				status.addFatalError(errorMsg);
			}
		}
	}

	private void createDummyMethodHead(String methodHead) {
		String dummyMethodName = GroovyRefactoringMessages.ExtractMethodWizard_DefaultMethodName;
		int paraStartPos = methodHead.indexOf('(');
		String partOne = methodHead.substring(0, paraStartPos);
		String partTwo = methodHead.substring(paraStartPos,methodHead.length());
		txtPreviewCall.setText(partOne + dummyMethodName + partTwo);
	}

	private RefactoringStatus validateGroovyIdentifiers() {
		List<String> variablesToCheck = new LinkedList<String>();
		variablesToCheck.add(txtNewMethodName.getText());

		for(Parameter p : extractMethodRefactoring.getCallAndMethHeadParameters()){
			variablesToCheck.add(p.getName());
		}

        IStatus statusOfUsedGroovyIdentifiers = new GroovyConventionsBuilder(variablesToCheck, GroovyConventionsBuilder.METHOD)
                .validateGroovyIdentifier().validateLowerCase(IStatus.WARNING).done();
		return StatusHelper.convertStatus(statusOfUsedGroovyIdentifiers);
	}

	private void setModifier() {
		extractMethodRefactoring.setModifier(DEFAULT_MODIFIER);
	}

	private void setModifier(MouseEvent e) {
		if(e.getSource() instanceof Button){
			Button selectedButton = (Button) e.getSource();
			if (selectedButton.getText().equals(MODIFIER_PRIVATE))
				extractMethodRefactoring.setModifier(Opcodes.ACC_PRIVATE);
			else if (selectedButton.getText().equals(MODIFIER_DEF))
				extractMethodRefactoring.setModifier(Opcodes.ACC_PUBLIC);
			else if (selectedButton.getText().equals(MODIFIER_PROTECTED))
				extractMethodRefactoring.setModifier(Opcodes.ACC_PROTECTED);
			else if (selectedButton.getText().equals(MODIFIER_NONE))
				extractMethodRefactoring.setModifier(0);
		}
	}

	public void update(Observable o, Object arg) {
		updateView();
	}

	private void createAccessModifierComposite(Composite parent) {
		accessModifierComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		gridLayout.marginHeight = 0;
		GridData compositeLData = new GridData();
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessHorizontalSpace = true;
		accessModifierComposite.setLayoutData(compositeLData);
		accessModifierComposite.setLayout(gridLayout);

		Label lbAccessModifier = new Label(accessModifierComposite, SWT.NONE);
		lbAccessModifier.setText(GroovyRefactoringMessages.ExtractMethodWizard_LB_AcessModifier);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		lbAccessModifier.setLayoutData(data);

		MouseAdapter btnClick = new MouseAdapter() {
			@Override
            public void mouseUp(MouseEvent e) {
				super.mouseUp(e);
				setModifier(e);
			}
		};

		createRadioButtons(btnClick);
	}

	private void createRadioButtons(MouseAdapter btnClick) {
		for(String buttonName : possibleModifiers){
			Button btnModifier = new Button(accessModifierComposite, SWT.RADIO);
			btnModifier.setText(buttonName);
			btnModifier.addMouseListener(btnClick);
			enableAndSelectButtons(buttonName, btnModifier);
		}
	}

	private void enableAndSelectButtons(String buttonName, Button btnModifier) {
		if(buttonName.equals(possibleModifiers[DEFAULT_MODIFIER])){
			btnModifier.setSelection(true);
		}
		if(buttonName.equals(MODIFIER_NONE)){
            btnModifier.setEnabled(extractMethodRefactoring.isStatic());
		}
	}

	private void createNewMethodNameComposite(Composite parent) {
		newMethodNameComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 10;
		GridData compositeLData = new GridData();
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessHorizontalSpace = true;
		newMethodNameComposite.setLayoutData(compositeLData);
		newMethodNameComposite.setLayout(gridLayout);

		Label lbMethodName = new Label(newMethodNameComposite, SWT.NONE);
		lbMethodName.setText(GroovyRefactoringMessages.ExtractMethodWizard_LB_NewMethodName);

		txtNewMethodName = new Text(newMethodNameComposite, SWT.BORDER);
		txtNewMethodName.addKeyListener(new KeyAdapter() {
			@Override
            public void keyReleased(KeyEvent e) {
				firstPreviewEver = false;
				extractMethodRefactoring.setNewMethodname(txtNewMethodName.getText());
			}
		});

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		txtNewMethodName.setLayoutData(data);
	}

	private void createParameterComposite(Composite parent) {
		if(extractMethodRefactoring.getCallAndMethHeadParameters().length > 0){
			GridLayout gridLayout;
			initParameterComposite(parent);

			Label lbParameters = new Label(parameterComposite, SWT.NONE);
			lbParameters.setText(GroovyRefactoringMessages.ExtractMethodWizard_LB_Parameters);

			Composite parameterTableAndButtons = createTableAndButtonComposite();

			Composite tableframe = createTableComposite(parameterTableAndButtons);

			createParameterTable(tableframe);

			Composite buttonframe = new Composite(parameterTableAndButtons, SWT.NONE);
			gridLayout = new GridLayout();
			gridLayout.numColumns = 1;
			buttonframe.setLayout(gridLayout);

			GridData buttonData = new GridData();
			buttonData.widthHint = 90;

			createUpButton(buttonframe, buttonData);

			createDownButton(buttonframe, buttonData);

			createTableSelectionListener();
		}
	}

	private void createTableSelectionListener() {

		createTableEditor();

		tblParameters.addSelectionListener(new SelectionAdapter(){

			@Override
            public void widgetSelected(SelectionEvent e) {

				final int EDITABLECOLUMN = 1;// editing the second column

				TableItem currentTableItem = ((TableItem)e.item);
				if (currentTableItem == null) return;

				updateButtonsEnabled(tblParameters);
				disposeTableEditor();

				Text newEditor = new Text(tblParameters, SWT.NONE);
				newEditor.setText(currentTableItem.getText(EDITABLECOLUMN));

				addListenerToEditor(EDITABLECOLUMN, newEditor);

				newEditor.selectAll();
		        newEditor.setFocus();
		        editor.setEditor(newEditor, currentTableItem, EDITABLECOLUMN);
			}

			private void addListenerToEditor(final int EDITABLECOLUMN, Text newEditor) {

				newEditor.addModifyListener(new ModifyListener(){

					public void modifyText(ModifyEvent e) {
						 saveRenamedVariable(EDITABLECOLUMN);
					}

					private void saveRenamedVariable(final int EDITABLECOLUMN) {
						Text text = (Text) editor.getEditor();
						 int selectionIndex = tblParameters.getSelectionIndex();
						 String before = extractMethodRefactoring.getOriginalParameterName(selectionIndex);
				         editor.getItem().setText(EDITABLECOLUMN, text.getText());
				         String after = editor.getItem().getText(EDITABLECOLUMN);
				         renameVariablesMap.put(before, after);
				         extractMethodRefactoring.setParameterRename(renameVariablesMap);
					}

				});
			}

		});
	}

	private void createTableEditor() {
		editor = new TableEditor(tblParameters);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 200;
	}

	private Button createPushButton(Composite buttonframe, GridData buttonData, String text, boolean enabled, boolean visible) {
		Button pushButton = new Button(buttonframe, SWT.PUSH);
		pushButton.setText(text);
		pushButton.setLayoutData(buttonData);
		pushButton.setEnabled(enabled);
		pushButton.setVisible(visible);
		return pushButton;
	}

	private void createDownButton(Composite buttonframe, GridData buttonData) {
		btnDown = createPushButton(buttonframe, buttonData, GroovyRefactoringMessages.ExtractMethodWizard_LB_BTN_Down, false, true);
		btnDown.addMouseListener(new ExtractMethodMouseAdapter(this,ExtractMethodMouseAdapter.DOWNEVENT));
	}

	private void createUpButton(Composite buttonframe, GridData buttonData) {
		btnUp = createPushButton(buttonframe, buttonData, GroovyRefactoringMessages.ExtractMethodWizard_LB_BTN_UP, false, true);
		btnUp.addMouseListener(new ExtractMethodMouseAdapter(this,ExtractMethodMouseAdapter.UPEVENT));
	}

	private void createParameterTable(Composite tableframe) {
		int tableParams = SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION;
		tblParameters = new Table(tableframe, tableParams);
		tblParameters.setLinesVisible(true);
		tblParameters.setHeaderVisible(true);

		createTableColumn(GroovyRefactoringMessages.ExtractMethodWizard_LB_Col_Type);
		createTableColumn(GroovyRefactoringMessages.ExtractMethodWizard_LB_Col_Name);
	}

	private Composite createTableComposite(Composite parameterTableAndButtons) {
		GridLayout gridLayout;
		Composite tableframe = new Composite(parameterTableAndButtons, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.horizontalSpacing = 0;
		tableframe.setLayout(gridLayout);
		return tableframe;
	}

	private Composite createTableAndButtonComposite() {
		GridLayout gridLayout;
		Composite parameterTableAndButtons = new Composite(parameterComposite, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parameterTableAndButtons.setLayout(gridLayout);
		return parameterTableAndButtons;
	}

	private void initParameterComposite(Composite parent) {
		parameterComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginTop = 10;
		gridLayout.verticalSpacing = 0;
		GridData compositeLData = new GridData();
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessHorizontalSpace = true;
		parameterComposite.setLayoutData(compositeLData);
		parameterComposite.setLayout(gridLayout);
	}

	private void createTableColumn(String title) {
		TableColumn column = new TableColumn(tblParameters, SWT.NONE);
		column.setText(title);
		column.setWidth(200);
		column.setMoveable(false);
		column.setResizable(false);
	}

	private void createPreviewComposite(Composite parent) {
		previewComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		GridData compositeLData = new GridData();
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessHorizontalSpace = true;
		previewComposite.setLayoutData(compositeLData);
		previewComposite.setLayout(gridLayout);

		Label lbPreview = new Label(previewComposite, SWT.NONE);
		lbPreview.setText(GroovyRefactoringMessages.ExtractMethodWizard_LB_MethodSignaturePreview);

		txtPreviewCall = new Text(previewComposite, SWT.MULTI | SWT.READ_ONLY);
		txtPreviewCall.setText(GroovyRefactoringMessages.ExtractMethodWizard_MethodCall);
		GridData data = new GridData();
		data.heightHint = 60;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		txtPreviewCall.setLayoutData(data);
	}

	protected void handleUpDownEvent(boolean upEvent) {
		String variName = "";
		if (tblParameters.getSelectionCount() > 0){
			variName = tblParameters.getSelection()[0].getText(1);
		}
		int indexOfSelectedParam = extractMethodRefactoring.setMoveParameter(variName, upEvent, 1);
		initializeValues(indexOfSelectedParam);
		disposeTableEditor();
	}

	private void disposeTableEditor() {
		Control oldEditor = editor.getEditor();
		if (oldEditor != null) oldEditor.dispose();
	}

	private void updateButtonsEnabled(Table tbl) {
		int lastElementIndex = extractMethodRefactoring.getCallAndMethHeadParameters().length - 1;
		int selectionIndex = tbl.getSelectionIndex();
		if(tbl.getItemCount() == 1){
			btnDown.setEnabled(false);
			btnUp.setEnabled(false);
		} else if(selectionIndex == 0){
			btnDown.setEnabled(true);
			btnUp.setEnabled(false);
		} else if(selectionIndex == lastElementIndex){
			btnDown.setEnabled(false);
			btnUp.setEnabled(true);
		} else {
			btnDown.setEnabled(true);
			btnUp.setEnabled(true);
		}
	}

}

class ExtractMethodMouseAdapter extends MouseAdapter{

	public static final int EDITEVENT = 1;
	public static final int DOWNEVENT = 2;
	public static final int UPEVENT = 3;

	private final ExtractMethodPageContent wizard;
	private final int eventType;

	public ExtractMethodMouseAdapter(ExtractMethodPageContent wizard, int eventtype){
		this.wizard = wizard;
		this.eventType = eventtype;
	}

	@Override
    public void mouseUp(MouseEvent e) {
		super.mouseUp(e);

		switch(eventType){
			case DOWNEVENT:
				wizard.handleUpDownEvent(false);
				break;
			case UPEVENT:
				wizard.handleUpDownEvent(true);
				break;
		}
	}

}
