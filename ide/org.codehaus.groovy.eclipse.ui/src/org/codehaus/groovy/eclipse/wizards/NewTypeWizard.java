/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyTypeBuilder;
import org.codehaus.groovy.eclipse.ui.decorators.GroovyPluginImages;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.bidi.StructuredTextTypeHandlerFactory;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.corext.refactoring.StubTypeContext;
import org.eclipse.jdt.internal.corext.refactoring.TypeContextChecker;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.dialogs.TableTextCellEditor;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.preferences.CodeTemplatePreferencePage;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.CompletionContextRequestor;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaTypeCompletionProcessor;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.SuperInterfaceSelectionDialog;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewAnnotationWizardPage;
import org.eclipse.jdt.ui.wizards.NewContainerWizardPage;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class NewTypeWizard extends NewElementWizard {

    public NewTypeWizard() {
        setDefaultPageImageDescriptor(GroovyPluginImages.DESC_NEW_GROOVY_ELEMENT);
        setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
        setWindowTitle(WizardMessages.NewTypeWizard_title);
    }

    @Override
    public void addPages() {
        addPage(new PageOne(getSelection()));
        addPage(new PageTwo());
    }

    @Override
    protected boolean canRunForked() {
        return false; // reads control data
    }

    @Override
    public boolean performFinish() {
        warnAboutTypeCommentDeprecation();
        boolean result = super.performFinish();
        if (result) {
            IResource resource = fCreatedType.getResource();
            if (resource != null) {
                selectAndReveal(resource);
                openResource((IFile) resource);
            }
        }
        return result;
    }

    @Override
    protected void finishPage(final IProgressMonitor monitor) throws CoreException, InterruptedException {
        PageOne pageOne = (PageOne) getPage(PageOne.PAGE_NAME);
        PageTwo pageTwo = (PageTwo) getPage(PageTwo.PAGE_NAME);

        GroovyTypeBuilder builder = new GroovyTypeBuilder();

        builder.setPackageFragmentRoot(pageOne.getPackageFragmentRoot());
        builder.setPackageFragment(pageOne.getPackageFragment());
        builder.setEnclosingType(pageOne.getEnclosingType());
        builder.setAddComments(pageTwo.isAddComments());
        builder.setTypeFlags(pageTwo.getModifiers());
        builder.setTypeKind(pageTwo.getTypeKind());
        builder.setTypeName(pageOne.getTypeName());
        builder.setSuper(pageTwo.getSuperClass());
        builder.setFaces(pageTwo.getInterfaces());

        SelectionButtonDialogFieldGroup stubs = (SelectionButtonDialogFieldGroup) pageTwo.getActiveControl().getData("StubControls");
        if (stubs != null) {
            IDialogSettings dialogSettings = getDialogSettings();
            if (dialogSettings != null) {
                final String sectionName = "NewClassWizardPage"; //$NON-NLS-1$
                IDialogSettings sectionSettings = dialogSettings.getSection(sectionName);
                if (sectionSettings == null) sectionSettings = dialogSettings.addNewSection(sectionName);

                sectionSettings.put("create_constructor", stubs.isSelected(0)); //$NON-NLS-1$
                sectionSettings.put("create_unimplemented", stubs.isSelected(1)); //$NON-NLS-1$
                // main method checkbox is not persistent as per https://bugs.eclipse.org/388342
            }

            builder.setStubs(stubs.isSelected(0), stubs.isSelected(1), stubs.isSelected(2));
        }

        fCreatedType = builder.build(monitor, this::finishType);
    }

    protected void finishType(final IType createdType, final IProgressMonitor subMonitor) throws OperationCanceledException {
        NewAnnotationWizardPage delegate = (NewAnnotationWizardPage) ((PageTwo) getPage(PageTwo.PAGE_NAME)).getActiveControl().getData("AnnotationDelegate");
        if (delegate != null) {
            subMonitor.beginTask(NewWizardMessages.NewAnnotationWizardPage_description, 3);

            //CompilationUnit astRoot = delegate.createASTForImports(fCreatedType.getCompilationUnit());
            CompilationUnit astRoot = ReflectionUtils.executePrivateMethod(
                NewTypeWizardPage.class, "createASTForImports", new Class[] {ICompilationUnit.class},
                delegate,                                       new Object[] {createdType.getCompilationUnit()});

            //ImportsManager imports = new ImportsManager(astRoot);
            ImportsManager imports = ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(ImportsManager.class, CompilationUnit.class), astRoot);

            subMonitor.worked(1);
            if (subMonitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            //delegate.createTypeMembers(createdType, imports, ((SubMonitor) subMonitor).split(1));
            ReflectionUtils.executePrivateMethod(
                NewAnnotationWizardPage.class, "createTypeMembers", new Class[] {IType.class, ImportsManager.class, IProgressMonitor.class},
                delegate,                                           new Object[] {createdType, imports, ((SubMonitor) subMonitor).split(1)});

            //imports.create(false, ((SubMonitor) subMonitor).split(1));
            ReflectionUtils.executePrivateMethod(
                ImportsManager.class, "create", new Class[] {boolean.class, IProgressMonitor.class},
                imports,                        new Object[] {Boolean.FALSE, ((SubMonitor) subMonitor).split(1)});
        }
    }

    @Override
    public IJavaElement getCreatedElement() {
        return fCreatedType;
    }

    private IType fCreatedType;

    //--------------------------------------------------------------------------

    public static class PageOne extends NewTypeWizardPage {

        public static final String PAGE_NAME = "NewTypeWizard.PageOne"; //$NON-NLS-1$

        public PageOne(final IStructuredSelection selection) {
            super(true, PAGE_NAME);
            setTitle(WizardMessages.NewTypeWizard_page1_title);
            setDescription(WizardMessages.NewTypeWizard_page1_message);

            IJavaElement javaElement = getInitialJavaElement(selection);
            initContainerPage(javaElement);
            initTypePage(javaElement);
            updateStatus();
        }

        @Override
        public void createControl(final Composite parent) {
            initializeDialogUnits(parent);

            final int nColumns = 4;

            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(nColumns, false);
            composite.setLayout(layout);

            createContainerControls(composite, nColumns);
            createPackageControls(composite, nColumns);
            createEnclosingTypeControls(composite, nColumns);

            createSeparator(composite, nColumns);

            createTypeKindControls(composite, nColumns);
            createTypeNameControls(composite, nColumns);

            setControl(composite);
            Dialog.applyDialogFont(composite);
            // TODO: PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ?);
        }

        protected void createTypeKindControls(final Composite parent, final int nColumns) {
            SelectionButtonDialogFieldGroup typeKindGroup = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] {
                WizardMessages.NewTypeWizard_page1_typeKind1,
                WizardMessages.NewTypeWizard_page1_typeKind2,
                WizardMessages.NewTypeWizard_page1_typeKind3,
                WizardMessages.NewTypeWizard_page1_typeKind4,
                WizardMessages.NewTypeWizard_page1_typeKind5,
                WizardMessages.NewTypeWizard_page1_typeKind6,
            }, 3);
            typeKindGroup.setDialogFieldListener(field -> {
                for (int i = 0; i < 6; i += 1) {
                    if (typeKindGroup.isSelected(i)) {
                        Composite nextPage = (Composite) getNextPage().getControl();
                        ((StackLayout) nextPage.getLayout()).topControl = nextPage.getChildren()[i];
                        nextPage.layout();
                    }
                }
                if (!typeKindGroup.isSelected(2)) {
                    fTypeKindStatus.setOK();
                    updateStatus();
                }
            });
            typeKindGroup.setLabelText(WizardMessages.NewTypeWizard_page1_typeKind);

            Control label = typeKindGroup.getLabelControl(parent);
            GridDataFactory.fillDefaults().span(1, 1).applyTo(label);

            Control group = typeKindGroup.getSelectionButtonsGroup(parent);
            GridDataFactory.fillDefaults().span(nColumns - 2, 1).applyTo(group);

            // fill last column
            DialogField.createEmptySpace(parent);

            // create callback that enables/disables Script type kind
            enclosingSelectionHandler = () -> {
                typeKindGroup.enableSelectionButton(2, !isEnclosingTypeSelected());
                if (!typeKindGroup.isEnabled(2) && typeKindGroup.isSelected(2)) {
                    fTypeKindStatus.setError(WizardMessages.NewTypeWizard_page1_error_script);
                } else {
                    fTypeKindStatus.setOK();
                }
            };
        }

        private Runnable enclosingSelectionHandler; // see usage below
        private final StatusInfo fTypeKindStatus = new StatusInfo();

        @Override
        protected void handleFieldChanged(final String fieldName) {
            if (getEnclosingType() != null && !ContentTypeUtils.isGroovyLikeFileName(getEnclosingType().getResource().getName())) {
                ((StatusInfo) fEnclosingTypeStatus).setError(WizardMessages.NewTypeWizard_page1_error_java);
            }
            if (ENCLOSINGSELECTION.equals(fieldName)) {
                enclosingSelectionHandler.run();
            }
            super.handleFieldChanged(fieldName);
            updateStatus();
        }

        @Override
        protected IStatus typeNameChanged() {
            StatusInfo status = (StatusInfo) super.typeNameChanged();

            if (status.getSeverity() < IStatus.WARNING) {
                IPackageFragmentRoot root = getPackageFragmentRoot();
                if (root != null) {
                    try {
                        if (!root.getJavaProject().getProject().hasNature(GroovyNature.GROOVY_NATURE)) {
                            status.setInfo(WizardMessages.bind(WizardMessages.NewTypeWizard_page1_info_groovyNature, root.getJavaProject().getElementName()));
                        }

                        String name = getTypeNameText();
                        if (ContentTypeUtils.isGroovyLikeFileName(name)) {
                            status.setInfo(WizardMessages.bind(WizardMessages.NewTypeWizard_page1_info_fileExtension, name.substring(name.lastIndexOf('.'))));
                        }

                        if (!isEnclosingTypeSelected()) {
                            ClasspathEntry entry = (ClasspathEntry) root.getRawClasspathEntry();
                            if (entry != null) {
                                IPath path = getPackageFragment().getResource().getFullPath().append(getCompilationUnitName(getTypeNameWithoutParameters()));
                                if (Util.isExcluded(path, entry.fullInclusionPatternChars(), entry.fullExclusionPatternChars(), false)) {
                                    status.setWarning(WizardMessages.NewTypeWizard_page1_warning_typeExcluded);
                                }
                            }

                            IType type = root.getJavaProject().findType(getPackageFragment().getElementName(), getTypeNameWithoutParameters());
                            if (type != null && type.exists()) {
                                status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExists);
                            }
                        }
                    } catch (Exception e) {
                        GroovyPlugin.getDefault().logError("Error in New Groovy Type wizard", e); //$NON-NLS-1$
                    }
                }
            }

            return status;
        }

        protected void updateStatus() {
            // the more severe status will be displayed and the Finish button enabled/disabled
            updateStatus(new IStatus[] {
                fContainerStatus,
                isEnclosingTypeSelected() ? fEnclosingTypeStatus : fPackageStatus,
                fTypeKindStatus,
                fTypeNameStatus,
            });
        }

        //

        @Override
        protected String getCompilationUnitName(final String typeName) {
            assert typeName.indexOf('<') == -1; // no parameters
            return typeName + ".groovy"; //$NON-NLS-1$
        }

        public String getTypeNameWithoutParameters() {
            return ReflectionUtils.executePrivateMethod(NewTypeWizardPage.class, "getTypeNameWithoutParameters", this); //$NON-NLS-1$
        }

        public String getTypeNameText() {
            StringDialogField fTypeNameDialogField = ReflectionUtils.getPrivateField(NewTypeWizardPage.class, "fTypeNameDialogField", this); //$NON-NLS-1$
            return fTypeNameDialogField.getText();
        }

        @Override
        public String getTypeName() {
            String typeName = getTypeNameText();
            if (ContentTypeUtils.isGroovyLikeFileName(typeName)) {
                return typeName.substring(0, typeName.lastIndexOf('.'));
            }
            return typeName;
        }

        @Override
        public String getSuperClass() {
            return Optional.ofNullable((PageTwo) getNextPage()).map(PageTwo::getSuperClass).orElse(""); //$NON-NLS-1$
        }

        @Override
        public List<String> getSuperInterfaces() {
            return Optional.ofNullable((PageTwo) getNextPage()).map(PageTwo::getInterfaces).map(Arrays::asList).orElse(Collections.emptyList());
        }

        @Override
        public boolean addSuperInterface(final String name) {
            PageTwo pageTwo = (PageTwo) getNextPage();

            @SuppressWarnings("unchecked")
            Function<String, Boolean> addInterface = (Function<String, Boolean>) pageTwo.getActiveControl().getData("AddInterface");

            return addInterface.apply(name);
        }

        @Override
        public void setSuperInterfaces(final List<String> names, final boolean mutable) {
            PageTwo pageTwo = (PageTwo) getNextPage();
            if (pageTwo != null) {
                @SuppressWarnings("unchecked")
                Consumer<List<String>> setInterfaces = (Consumer<List<String>>) pageTwo.getActiveControl().getData("SetInterfaces");

                setInterfaces.accept(names);
            }
        }

        @Override
        public void setVisible(final boolean visible) {
            super.setVisible(visible);
            if (visible) setFocus();
        }
    }

    public static class PageTwo extends WizardPage {

        public static final String PAGE_NAME = "NewTypeWizard.PageTwo"; //$NON-NLS-1$

        public PageTwo() {
            super(PAGE_NAME);
        }

        @Override
        public void createControl(final Composite parent) {
            initializeDialogUnits(parent);

            onVisible = dontCare -> {
                switch (getTypeKind()) {
                case "class": //$NON-NLS-1$
                    setTitle(WizardMessages.NewTypeWizard_page2_class_title);
                    setDescription(WizardMessages.NewTypeWizard_page2_class_message);
                    break;
                case "trait": //$NON-NLS-1$
                    setTitle(WizardMessages.NewTypeWizard_page2_trait_title);
                    setDescription(WizardMessages.NewTypeWizard_page2_trait_message);
                    break;
                case "script": //$NON-NLS-1$
                    setTitle(WizardMessages.NewTypeWizard_page2_script_title);
                    setDescription(WizardMessages.NewTypeWizard_page2_script_message);
                    break;
                case "interface": //$NON-NLS-1$
                    setTitle(WizardMessages.NewTypeWizard_page2_interface_title);
                    setDescription(WizardMessages.NewTypeWizard_page2_interface_message);
                    break;
                case "@interface": //$NON-NLS-1$
                    setTitle(WizardMessages.NewTypeWizard_page2_annotation_title);
                    setDescription(WizardMessages.NewTypeWizard_page2_annotation_message);
                    break;
                case "enum": //$NON-NLS-1$
                    setTitle(WizardMessages.NewTypeWizard_page2_enumeration_title);
                    setDescription(WizardMessages.NewTypeWizard_page2_enumeration_message);
                    break;
                }
            };

            Composite composite = new Composite(parent, SWT.NONE);
            StackLayout layout = new StackLayout();
            composite.setLayout(layout);

            createClassControls(composite);
            createTraitControls(composite);
            createScriptControls(composite);
            createInterfaceControls(composite);
            createAnnotationControls(composite);
            createEnumerationControls(composite);

            setControl(composite);
            Dialog.applyDialogFont(composite);
            layout.topControl = composite.getChildren()[0];
        }

        private void createClassControls(final Composite parent) {
            final int nColumns = 4;

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setData("TypeKind", "class"); //$NON-NLS-1$
            GridLayout layout = new GridLayout(nColumns, false);
            composite.setLayout(layout);

            Map<String, Integer> modifiers = new LinkedHashMap<>();
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_public,    Flags.AccPublic);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_default,   Flags.AccDefault);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_private,   Flags.AccPrivate);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_protected, Flags.AccProtected);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_abstract,  Flags.AccAbstract);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_final,     Flags.AccFinal);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_static,    Flags.AccStatic);

            createModifierControls(composite, nColumns, modifiers);
            createSuperClassControls(composite, nColumns, NewWizardMessages.NewTypeWizardPage_superclass_label, "java.lang.Object"); //$NON-NLS-1$
            createSuperInterfacesControls(composite, nColumns);
            createMethodStubSelectionControls(composite, nColumns);
            createCommentControls(composite, nColumns);
        }

        private void createTraitControls(final Composite parent) {
            final int nColumns = 4;

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setData("TypeKind", "trait"); //$NON-NLS-1$
            GridLayout layout = new GridLayout();
            layout.numColumns = nColumns;
            composite.setLayout(layout);

            Map<String, Integer> modifiers = new LinkedHashMap<>();
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_public,    Flags.AccPublic);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_default,   Flags.AccDefault);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_private,   Flags.AccPrivate);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_protected, Flags.AccProtected);
          //modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_static,    Flags.AccStatic);

            createModifierControls(composite, nColumns, modifiers);
            createSuperClassControls(composite, nColumns, WizardMessages.NewTypeWizard_page2_selftype_label, "java.lang.Object"); //$NON-NLS-1$
            createSuperInterfacesControls(composite, nColumns);
            createCommentControls(composite, nColumns);
        }

        private void createScriptControls(final Composite parent) {
            final int nColumns = 4;

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setData("TypeKind", "script"); //$NON-NLS-1$
            GridLayout layout = new GridLayout();
            layout.numColumns = nColumns;
            composite.setLayout(layout);

            createSuperClassControls(composite, nColumns, WizardMessages.NewTypeWizard_page2_basescript_label, "groovy.lang.Script"); //$NON-NLS-1$
            createCommentControls(composite, nColumns);
        }

        private void createInterfaceControls(final Composite parent) {
            final int nColumns = 4;

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setData("TypeKind", "interface"); //$NON-NLS-1$
            GridLayout layout = new GridLayout();
            layout.numColumns = nColumns;
            composite.setLayout(layout);

            Map<String, Integer> modifiers = new LinkedHashMap<>();
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_public,    Flags.AccPublic);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_default,   Flags.AccDefault);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_private,   Flags.AccPrivate);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_protected, Flags.AccProtected);

            createModifierControls(composite, nColumns, modifiers);
            createSuperInterfacesControls(composite, nColumns);
            createCommentControls(composite, nColumns);
        }

        private void createAnnotationControls(final Composite parent) {
            final int nColumns = 4;

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setData("TypeKind", "@interface"); //$NON-NLS-1$
            GridLayout layout = new GridLayout();
            layout.numColumns = nColumns;
            composite.setLayout(layout);

            Map<String, Integer> modifiers = new LinkedHashMap<>();
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_public,    Flags.AccPublic);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_default,   Flags.AccDefault);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_private,   Flags.AccPrivate);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_protected, Flags.AccProtected);

            createModifierControls(composite, nColumns, modifiers);

            createSeparator(composite, nColumns);

            NewAnnotationWizardPage delegate = new NewAnnotationWizardPage();
            //delegate.createAddAnnotationControls(composite, nColumns);
            ReflectionUtils.executePrivateMethod(NewAnnotationWizardPage.class, "createAddAnnotationControls",
                        new Class[] {Composite.class, int.class}, delegate, new Object[] {composite, nColumns});
            //delegate.initAnnotationPage();
            ReflectionUtils.executePrivateMethod(NewAnnotationWizardPage.class, "initAnnotationPage", delegate);

            createSeparator(composite, nColumns);

            createCommentControls(composite, nColumns);

            composite.setData("AnnotationDelegate", delegate);
        }

        private void createEnumerationControls(final Composite parent) {
            final int nColumns = 4;

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setData("TypeKind", "enum"); //$NON-NLS-1$
            GridLayout layout = new GridLayout();
            layout.numColumns = nColumns;
            composite.setLayout(layout);

            Map<String, Integer> modifiers = new LinkedHashMap<>();
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_public,    Flags.AccPublic);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_default,   Flags.AccDefault);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_private,   Flags.AccPrivate);
            modifiers.put(NewWizardMessages.NewTypeWizardPage_modifiers_protected, Flags.AccProtected);

            createModifierControls(composite, nColumns, modifiers);
            createSuperInterfacesControls(composite, nColumns);
            createCommentControls(composite, nColumns);
        }

        //

        private void createModifierControls(final Composite composite, final int nColumns, final Map<String, Integer> modifiers) {
            List<String> accessFlags = new ArrayList<>(), otherFlags = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : modifiers.entrySet()) {
                switch (entry.getValue()) {
                case Flags.AccPublic:
                case Flags.AccDefault:
                case Flags.AccPrivate:
                case Flags.AccProtected:
                    accessFlags.add(entry.getKey());
                    break;
                default:
                    otherFlags.add(entry.getKey());
                }
            }

            //
            SelectionButtonDialogFieldGroup accessFlagsGroup =
                new SelectionButtonDialogFieldGroup(SWT.RADIO, accessFlags.toArray(new String[accessFlags.size()]), 4);
            accessFlagsGroup.setLabelText(NewWizardMessages.NewTypeWizardPage_modifiers_acc_label);

            Control label = accessFlagsGroup.getLabelControl(composite);
            GridDataFactory.fillDefaults().span(1, 1).applyTo(label);

            Control group = accessFlagsGroup.getSelectionButtonsGroup(composite);
            GridDataFactory.fillDefaults().span(nColumns - 2, 1).applyTo(group);

            DialogField.createEmptySpace(composite);

            //
            SelectionButtonDialogFieldGroup otherFlagsGroup =
                new SelectionButtonDialogFieldGroup(SWT.CHECK, otherFlags.toArray(new String[otherFlags.size()]), 4);
            if (!otherFlags.isEmpty()) {
                DialogField.createEmptySpace(composite);

                group = otherFlagsGroup.getSelectionButtonsGroup(composite);
                GridDataFactory.fillDefaults().span(nColumns - 2, 1).applyTo(group);

                DialogField.createEmptySpace(composite);
            }

            //
            IDialogFieldListener listener = field -> {
                int flags = Flags.AccDefault;

                for (int i = 0, n = accessFlags.size(); i < n; i += 1) {
                    if (accessFlagsGroup.isEnabled(i) && accessFlagsGroup.isSelected(i)) {
                        flags |= modifiers.get(accessFlags.get(i));
                        break;
                    }
                }

                for (int i = 0, n = otherFlags.size(); i < n; i += 1) {
                    if (otherFlagsGroup.isEnabled(i) && otherFlagsGroup.isSelected(i)) {
                        flags |= modifiers.get(otherFlags.get(i));
                    }
                }

                composite.setData("TypeFlags", flags);

                StatusInfo status = new StatusInfo();
                if (Flags.isFinal(flags) && Flags.isAbstract(flags)) {
                    status.setError(NewWizardMessages.NewTypeWizardPage_error_ModifiersFinalAndAbstract);
                }
                mergeStatus("TypeFlags", status);
            };
            accessFlagsGroup.setDialogFieldListener(listener);
            otherFlagsGroup.setDialogFieldListener(listener);

            // protected, private and static are only available for inner types
            onVisible = onVisible.andThen(isInner -> {
                for (Map.Entry<String, Integer> entry : modifiers.entrySet()) {
                    switch (entry.getValue()) {
                    case Flags.AccProtected:
                    case Flags.AccPrivate:
                        accessFlagsGroup.enableSelectionButton(accessFlags.indexOf(entry.getKey()), isInner);
                        break;
                    case Flags.AccStatic:
                        otherFlagsGroup.enableSelectionButton(otherFlags.indexOf(entry.getKey()), isInner);
                    }
                }
            });
        }

        private void createSuperClassControls(final Composite composite, final int nColumns, final String label, final String value) {
            StringButtonDialogField superClassDialogField = new StringButtonDialogField(field -> {
                IType type = ReflectionUtils.executePrivateMethod(NewTypeWizardPage.class, "chooseSuperClass", getPreviousPage());
                if (type != null) {
                    ((StringButtonDialogField) field).setText(SuperInterfaceSelectionDialog.getNameWithTypeParameters(type));
                }
            });

            superClassDialogField.setDialogFieldListener(field -> {
                StatusInfo status = new StatusInfo();

                String typeName = ((StringButtonDialogField) field).getText();
                if (!typeName.isEmpty()) {
                    Type type = TypeContextChecker.parseSuperClass(typeName);
                    if (type == null) {
                        status.setError(NewWizardMessages.NewTypeWizardPage_error_InvalidSuperClassName);
                    } else if (type instanceof ParameterizedType && !JavaModelUtil.is50OrHigher(getJavaProject())) {
                        status.setError(NewWizardMessages.NewTypeWizardPage_error_SuperClassNotParameterized);
                    }
                }
                composite.setData("SuperClass", typeName);

                mergeStatus("SuperClass", status);
            });

            superClassDialogField.setText(value);
            superClassDialogField.setLabelText(label);
            superClassDialogField.setButtonLabel(NewWizardMessages.NewTypeWizardPage_superclass_button);
            superClassDialogField.doFillIntoGrid(composite, nColumns);
            Text text = superClassDialogField.getTextControl(null);
            LayoutUtil.setHorizontalGrabbing(text);
            LayoutUtil.setWidthHint(text, getMaxFieldWidth());
            TextFieldNavigationHandler.install(text); // enable sub-word navigation
            BidiUtils.applyBidiProcessing(text, StructuredTextTypeHandlerFactory.JAVA);

            JavaTypeCompletionProcessor superClassCompletionProcessor = new JavaTypeCompletionProcessor(false, false, true);
            superClassCompletionProcessor.setCompletionContextRequestor(new CompletionContextRequestor() {
                private StubTypeContext fSuperClassStubTypeContext;
                @Override
                public StubTypeContext getStubTypeContext() {
                    if (fSuperClassStubTypeContext == null) {
                        PageOne pageOne = (PageOne) getPreviousPage();
                        fSuperClassStubTypeContext = TypeContextChecker.createSuperClassStubTypeContext(
                            pageOne.getTypeName(), pageOne.getEnclosingType(), pageOne.getPackageFragment());
                    }
                    return fSuperClassStubTypeContext;
                }
            });
            ControlContentAssistHelper.createTextContentAssistant(text, superClassCompletionProcessor);
        }

        private void createSuperInterfacesControls(final Composite composite, final int nColumns) {
            ListDialogField<Wrapper> superInterfacesDialogField = newListField(composite, nColumns);

            superInterfacesDialogField.setDialogFieldListener(field -> {
                StatusInfo status = new StatusInfo();

                @SuppressWarnings("unchecked")
                ListDialogField<Wrapper> listField = (ListDialogField<Wrapper>) field;

                for (Wrapper element : listField.getElements()) {
                    String interfaceName = element.string;
                    Type type = TypeContextChecker.parseSuperInterface(interfaceName);
                    if (type == null) {
                        status.setError(WizardMessages.bind(
                            NewWizardMessages.NewTypeWizardPage_error_InvalidSuperInterfaceName, BasicElementLabels.getJavaElementName(interfaceName)));
                        //
                    } else if (type instanceof ParameterizedType && !JavaModelUtil.is50OrHigher(getJavaProject())) {
                        status.setError(WizardMessages.bind(
                            NewWizardMessages.NewTypeWizardPage_error_SuperInterfaceNotParameterized, BasicElementLabels.getJavaElementName(interfaceName)));
                    }
                }

                mergeStatus("Interfaces", status);
            });

            Function<String, Boolean> addInterface = name -> {
                return Boolean.valueOf(superInterfacesDialogField.addElement(new Wrapper(name)));
            };
            Consumer<List<String>> setInterfaces = names -> {
                superInterfacesDialogField.setElements(names.stream().map(Wrapper::new).collect(Collectors.toList()));
            };
            Supplier<List<String>> getInterfaces = () -> {
                return superInterfacesDialogField.getElements().stream().map(it -> it.string).collect(Collectors.toList());
            };

            composite.setData("AddInterface", addInterface);
            composite.setData("SetInterfaces", setInterfaces);
            composite.setData("GetInterfaces", getInterfaces);
        }

        private ListDialogField<Wrapper> newListField(final Composite composite, final int nColumns) {
            String[] buttons = new String[] {
                NewWizardMessages.NewTypeWizardPage_interfaces_add,
                null, // spacer
                NewWizardMessages.NewTypeWizardPage_interfaces_remove,
            };

            IListAdapter<Wrapper> addButtonHandler = new IListAdapter<Wrapper>() {
                @Override
                public void customButtonPressed(final ListDialogField<Wrapper> listField, final int index) {
                    ReflectionUtils.executePrivateMethod(NewTypeWizardPage.class, "chooseSuperInterfaces", getPreviousPage());
                    /*List<Wrapper> elements = listField.getElements();
                    if (!elements.isEmpty()) {
                        Wrapper element = elements.get(elements.size() - 1);
                        listField.editElement(element);
                    }*/
                }

                @Override
                public void selectionChanged(final ListDialogField<Wrapper> field) {
                }

                @Override
                public void doubleClicked(final ListDialogField<Wrapper> field) {
                }
            };

            LabelProvider labelProvider = new LabelProvider() {
                @Override
                public Image getImage(final Object element) {
                    return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_INTERFACE);
                }

                @Override
                public String getText(final Object element) {
                    return BasicElementLabels.getJavaElementName(((Wrapper) element).string);
                }
            };

            ListDialogField<Wrapper> superInterfacesDialogField = new ListDialogField<>(addButtonHandler, buttons, labelProvider);
            superInterfacesDialogField.setLabelText(NewWizardMessages.NewTypeWizardPage_interfaces_class_label);
            superInterfacesDialogField.setTableColumns(new ListDialogField.ColumnsDescription(1, false));
            superInterfacesDialogField.setRemoveButtonIndex(2);

            // returns: label, listbox, and buttons
            Control[] controls = superInterfacesDialogField.doFillIntoGrid(composite, nColumns);

            GridData gd = (GridData) controls[1].getLayoutData();
            gd.grabExcessHorizontalSpace = true;
            gd.grabExcessVerticalSpace = false;
            gd.widthHint = getMaxFieldWidth();
            gd.heightHint = convertHeightInCharsToPixels(3);

            gd = (GridData) controls[2].getLayoutData();
            gd.grabExcessVerticalSpace = false;

            //
            TableViewer tableViewer = superInterfacesDialogField.getTableViewer();
            tableViewer.setColumnProperties(new String[] {"interface"}); //$NON-NLS-1$

            setCellEditor(superInterfacesDialogField);
            setCellModifier(superInterfacesDialogField);
            setKeyListener(superInterfacesDialogField);

            return superInterfacesDialogField;
        }

        @SuppressWarnings("deprecation")
        private void setCellEditor(final ListDialogField<Wrapper> superInterfacesDialogField) {
            TableViewer tableViewer = superInterfacesDialogField.getTableViewer();

            TableTextCellEditor cellEditor = new TableTextCellEditor(tableViewer, 0) {
                @Override
                protected Control createControl(final Composite parent) {
                    Control control = super.createControl(parent);
                    BidiUtils.applyBidiProcessing(text, StructuredTextTypeHandlerFactory.JAVA);
                    return control;
                }

                @Override
                protected void doSetFocus() {
                    if (text != null) {
                        text.setFocus();
                        text.setSelection(text.getText().length());
                        checkSelection();
                        checkDeleteable();
                        checkSelectable();
                    }
                }
            };
            Text cellEditorText = cellEditor.getText();
            TextFieldNavigationHandler.install(cellEditorText);

            JavaTypeCompletionProcessor superInterfaceCompletionProcessor = new JavaTypeCompletionProcessor(false, false, true);
            superInterfaceCompletionProcessor.setCompletionContextRequestor(new CompletionContextRequestor() {
                private StubTypeContext fSuperInterfaceStubTypeContext;
                @Override
                public StubTypeContext getStubTypeContext() {
                    if (fSuperInterfaceStubTypeContext == null) {
                        PageOne pageOne = (PageOne) getPreviousPage();
                        fSuperInterfaceStubTypeContext = TypeContextChecker.createSuperInterfaceStubTypeContext(
                            pageOne.getTypeName(), pageOne.getEnclosingType(), pageOne.getPackageFragment());
                    }
                    return fSuperInterfaceStubTypeContext;
                }
            });
            org.eclipse.jface.contentassist.SubjectControlContentAssistant contentAssistant =
                ControlContentAssistHelper.createJavaContentAssistant(superInterfaceCompletionProcessor);
            org.eclipse.ui.contentassist.ContentAssistHandler.createHandlerForText(cellEditorText, contentAssistant);
            cellEditor.setContentAssistant(contentAssistant);

            tableViewer.setCellEditors(new CellEditor[] {cellEditor});
        }

        private void setCellModifier(final ListDialogField<Wrapper> superInterfacesDialogField) {
            TableViewer tableViewer = superInterfacesDialogField.getTableViewer();

            tableViewer.setCellModifier(new ICellModifier() {
                @Override
                public void modify(Object element, final String property, final Object value) {
                    if (element instanceof Item)
                        element = ((Item) element).getData();

                    ((Wrapper) element).string = (String) value;
                    superInterfacesDialogField.elementChanged((Wrapper) element);
                }

                @Override
                public Object getValue(final Object element, final String property) {
                    return ((Wrapper) element).string;
                }

                @Override
                public boolean canModify(final Object element, final String property) {
                    return true;
                }
            });
        }

        private void setKeyListener(final ListDialogField<Wrapper> superInterfacesDialogField) {
            TableViewer tableViewer = superInterfacesDialogField.getTableViewer();

            tableViewer.getTable().addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent event) {
                    if (event.keyCode == SWT.F2 && event.stateMask == 0) {
                        ISelection selection = tableViewer.getSelection();
                        if (selection instanceof IStructuredSelection) {
                            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                            tableViewer.editElement(structuredSelection.getFirstElement(), 0);
                        }
                    }
                }
            });
        }

        private void createMethodStubSelectionControls(final Composite composite, final int nColumns) {
            SelectionButtonDialogFieldGroup methodStubSelector = new SelectionButtonDialogFieldGroup(SWT.CHECK, new String[] {
                NewWizardMessages.NewClassWizardPage_methods_constructors,
                NewWizardMessages.NewClassWizardPage_methods_inherited,
                WizardMessages.NewTypeWizard_page2_methods_main,
            }, 1);
            methodStubSelector.setLabelText(NewWizardMessages.NewClassWizardPage_methods_label);

            IDialogSettings dialogSettings = getDialogSettings();
            if (dialogSettings != null) {
                dialogSettings = dialogSettings.getSection("NewClassWizardPage"); //$NON-NLS-1$
                if (dialogSettings != null) {
                    methodStubSelector.setSelection(0, dialogSettings.getBoolean("create_constructor")); //$NON-NLS-1$
                    methodStubSelector.setSelection(1, dialogSettings.getBoolean("create_unimplemented")); //$NON-NLS-1$
                }
            }

            composite.setData("StubControls", methodStubSelector);

            //
            Control label = methodStubSelector.getLabelControl(composite);
            LayoutUtil.setHorizontalSpan(label, nColumns);

            DialogField.createEmptySpace(composite);

            Control checks = methodStubSelector.getSelectionButtonsGroup(composite);
            LayoutUtil.setHorizontalSpan(checks, nColumns - 1);
        }

        private void createCommentControls(final Composite composite, final int nColumns) {
            Link link = new Link(composite, SWT.NONE);
            link.setText(NewWizardMessages.NewTypeWizardPage_addcomment_description);
            link.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, nColumns, 1));
            link.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(final SelectionEvent event) {
                    widgetSelected(event);
                }

                @Override
                public void widgetSelected(final SelectionEvent event) {
                    IJavaProject javaProject = getJavaProject();
                    if (javaProject != null) {
                        PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(
                            getShell(), javaProject.getProject(), CodeTemplatePreferencePage.PROP_ID, null, null);
                        dialog.open();
                    } else {
                        MessageDialog.openInformation(getShell(),
                            NewWizardMessages.NewTypeWizardPage_configure_templates_title,
                            NewWizardMessages.NewTypeWizardPage_configure_templates_message);
                    }
                }
            });

            DialogField.createEmptySpace(composite);

            SelectionButtonDialogField addCommentCheckbox = new SelectionButtonDialogField(SWT.CHECK);
            addCommentCheckbox.setLabelText(NewWizardMessages.NewTypeWizardPage_addcomment_label);
            addCommentCheckbox.setSelection(Boolean.valueOf(PreferenceConstants.getPreference(
                PreferenceConstants.CODEGEN_ADD_COMMENTS, getJavaProject())));
            addCommentCheckbox.doFillIntoGrid(composite, nColumns - 1);

            composite.setData("AddComments", (Supplier<Boolean>) addCommentCheckbox::isSelected);
        }

        /**
         * Creates a separator line. Expects a <code>GridLayout</code> with at least 1 column.
         *
         * @param composite the parent composite
         * @param nColumns number of columns to span
         */
        private void createSeparator(final Composite composite, final int nColumns) {
            LayoutUtil.setHorizontalGrabbing(
                (new Separator(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(composite, nColumns, convertHeightInCharsToPixels(1))[0]
            );
        }

        private void mergeStatus(final String key, final IStatus value) {
            Control panel = getControl();
            if (panel != null) {
                @SuppressWarnings("unchecked")
                Map<String, IStatus> map = (Map<String, IStatus>) panel.getData("StatusMap");
                if (map == null) {
                    map = new HashMap<>();
                    panel.setData("StatusMap", map);
                }

                map.put(key, value);

                IStatus status = StatusUtil.getMostSevere(map.values().toArray(new IStatus[0]));

                StatusUtil.applyToStatusLine(this, status);
                setPageComplete(status.getSeverity() < IStatus.ERROR);
            }
        }

        //

        private Control getActiveControl() {
            StackLayout stack = (StackLayout) ((Composite) getControl()).getLayout();
            return stack.topControl;
        }

        private IJavaProject getJavaProject() {
            return ((PageOne) getPreviousPage()).getJavaProject();
        }

        private int getMaxFieldWidth() {
            return (Integer) ReflectionUtils.executePrivateMethod(NewContainerWizardPage.class, "getMaxFieldWidth", getPreviousPage());
        }

        public String getTypeKind() {
            return (String) getActiveControl().getData("TypeKind");
        }

        public int getModifiers() {
            Integer modifiers = (Integer) getActiveControl().getData("TypeFlags");
            return (modifiers != null ? modifiers.intValue() : Flags.AccPublic);
        }

        public String getSuperClass() {
            return (String) getActiveControl().getData("SuperClass");
        }

        @SuppressWarnings("unchecked")
        public String[] getInterfaces() {
            Object getter = getActiveControl().getData("GetInterfaces");
            return getter == null ? null : ((Supplier<List<String>>) getter).get().toArray(new String[0]);
        }

        @SuppressWarnings("unchecked")
        public boolean isAddComments() {
            return ((Supplier<Boolean>) getActiveControl().getData("AddComments")).get();
        }

        private Consumer<Boolean> onVisible;

        @Override
        public void setVisible(final boolean visible) {
            if (visible) {
                PageOne pageOne = (PageOne) getPreviousPage();
                onVisible.accept(pageOne.isEnclosingTypeSelected());
            }
            super.setVisible(visible);
        }

        //

        public static final class Wrapper {
            public String string;

            public Wrapper(final String string) {
                this.string = Objects.requireNonNull(string);
            }

            @Override
            public int hashCode() {
                return string.hashCode();
            }

            @Override
            public boolean equals(final Object that) {
                if (that == this) {
                    return true;
                }
                if (!(that instanceof Wrapper)) {
                    return false;
                }
                return ((Wrapper) that).string.equals(this.string);
            }
        }
    }
}
