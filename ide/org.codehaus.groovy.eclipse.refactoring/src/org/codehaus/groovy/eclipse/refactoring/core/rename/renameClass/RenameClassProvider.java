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
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.MultiFileRefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.participation.GroovyParticipantManager;
import org.codehaus.groovy.eclipse.refactoring.core.participation.GroovySharableParticipants;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyConventionsBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ImportResolver;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.participants.JavaProcessors;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * Provider for the Refactoring Rename Class
 * 
 * @author martin kempf
 */
public class RenameClassProvider extends MultiFileRefactoringProvider implements
		IRenameProvider {

    private static final List<RefactoringParticipant> EMPTY_PARTICIPANTS= Collections.EMPTY_LIST;

	private final ClassNode selectedNode;
	private String alias;
	protected List<RenameTextEditProvider> textEditProviders = new ArrayList<RenameTextEditProvider>();
	private final ICompilationUnit compilationUnit;
	private String newclassName;
	
	private List<RefactoringParticipant> fParticipants = EMPTY_PARTICIPANTS;

	public RenameClassProvider(IGroovyFileProvider docProvider,
			ClassNode selectedNode, ICompilationUnit unit) {
		super(docProvider);
		this.selectedNode = selectedNode;
		this.selectedASTNode = selectedNode;
		this.compilationUnit = unit;
		try {
		    createProvidersConsideringAlias();
		} catch (NullPointerException e) {
		    GroovyCore.logException("Exception creating rename participant", e);
		}
	}

	public RenameClassProvider(IGroovyFileProvider docProvider,
			UserSelection selection, ClassNode selectedNode, ICompilationUnit unit) {
		this(docProvider, selectedNode, unit);
		setSelection(selection);
	}

	private void createProvidersConsideringAlias() {
		// If the selected ClassNode is written as Alias, the rename is local
		alias = ImportResolver.asAlias(getDocumentProvider().getRootNode(),
				selectedNode);
		if (!alias.equals("")) {
			textEditProviders.add(new RenameAliasTextEditProvider(alias,
					selectedNode, getDocumentProvider()));
		} else {
			// selected ClassNode can be used in every file in the project
			for (IGroovyDocumentProvider document : getUsedDocuments()) {
				textEditProviders.add(new RenameClassTextEditProvider(
						selectedNode, document));
			}
		}
	}

	@Override
    public boolean hasCandidates() {
		return !textEditProviders.isEmpty();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {

		RefactoringStatus state = new RefactoringStatus();
		List<String> alreadyUsedNames = new ArrayList<String>();
		for (RenameTextEditProvider textEdit : textEditProviders) {
			alreadyUsedNames.addAll(textEdit.getAlreadyUsedNames());
		}

		String newName = textEditProviders.get(0).getNewName();
		for (String usedName : alreadyUsedNames) {
			if (usedName.equals(newName)) {
				state
						.addFatalError(MessageFormat
								.format(
										GroovyRefactoringMessages.RenameClassInfo_ClassNameAlreadyExists,
										newName));
			}
		}
		
		IJavaElement element = codeResolve(compilationUnit);
		
		if (element != null && element.getElementType() == IJavaElement.TYPE) {    
		    processRenameParticipants(state, (IType) element);
		} else {
		    if (compilationUnit != null && selection != null) {
		        // only add error if there was a valid selection going in.
		    	state.addError("Cannot resolve selection to a Groovy program element");
		    }
		}
				
		return state;
	}
	
	private IJavaElement codeResolve(ICompilationUnit input) throws JavaModelException {
	    if (input == null || selectedASTNode == null) {
	        return null;
	    }
	    JavaModelUtil.reconcile((ICompilationUnit) input);
	    IJavaElement[] elements = input.getChildren();
	    if (elements != null && elements.length > 0) {
	        return elements[0];
	    } else {
	        return null;
	    }
	}
	
	private void processRenameParticipants(RefactoringStatus refactoringStatus, IJavaElement element) throws CoreException{
		
		GroovySharableParticipants sharableParticipants= new GroovySharableParticipants(); 
		RenameArguments arguments = new RenameArguments(newclassName, true);
		
		RenameTypeProcessor processor = new RenameTypeProcessor((IType)element);
        
		
		RefactoringParticipant[] loadedParticipants= GroovyParticipantManager.loadRenameParticipants(refactoringStatus, processor, element, arguments,
																					JavaProcessors.computeAffectedNatures(element), null, sharableParticipants);
		if (loadedParticipants == null || loadedParticipants.length == 0) {
			fParticipants= EMPTY_PARTICIPANTS;
		} else {
			fParticipants= new ArrayList<RefactoringParticipant>();
			for (int i= 0; i < loadedParticipants.length; i++) {
				fParticipants.add(loadedParticipants[i]);
			}
		}
		
		
	}

	@Override
	public void addInitialConditionsCheckStatus(RefactoringStatus status) {
		// check if the class can be renamed, only groovy and java classes, that
		// are defined in
		// the project can be renamed. Alias can always be renamed, since they
		// are like a local
		// class definition
		if (alias == "") {
			if (!isGroovySource() && !isJavaSource()) {
				status
						.addFatalError(GroovyRefactoringMessages.RenameClassInfo_RenameNotPossible);
			}
		}
	}

	private boolean isGroovySource() {
		List<String> groovyClassNames = new ArrayList<String>();
		for (RenameTextEditProvider textEdit : textEditProviders) {
			ModuleNode rootNode = textEdit.getDocProvider().getRootNode();
			GroovyClassDefinitionCollector collector = new GroovyClassDefinitionCollector(
					rootNode);
			collector.scanAST();
			groovyClassNames.addAll(collector.getGroovyClasses());
		}

		return selectedNode != null
				&& groovyClassNames.contains(selectedNode.getName());
	}

	private boolean isJavaSource() {
		// Search project for java sources
		if (selectedNode != null && fileProvider.getProject() != null) {
			try {
				IJavaProject javaProject = JavaCore.create(fileProvider
						.getProject());
				IType javaType = javaProject.findType(selectedNode.getName());
				return javaType != null && !javaType.isBinary();
			} catch (JavaModelException e) {
			}
		}
		return false;
	}

	@Override
	public GroovyChange createGroovyChange(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		GroovyChange change = new GroovyChange(
				GroovyRefactoringMessages.RenameClassRefactoring);

		for (RenameTextEditProvider textEditProvider : textEditProviders) {
			MultiTextEdit multi = removeDuplicatedTextedits(textEditProvider);
            IGroovyDocumentProvider docProvider = textEditProvider.getDocProvider();
			change.addEdit(docProvider, multi);
		}
		
		for (RefactoringParticipant participant : fParticipants) {
			Change participantChange= participant.createChange(new SubProgressMonitor(pm, 1));
			if (participantChange != null) {
				GroovyCore.logTraceMessage("--adding participant change " + participantChange);
				change.addChange(participantChange);
			}
		}
		
		return change;
	}

	public void checkUserInput(RefactoringStatus status, String userInput) {
		IStatus stateValidName = new GroovyConventionsBuilder(userInput,
				GroovyConventionsBuilder.CLASS).validateGroovyIdentifier()
				.validateUpperCase(IStatus.ERROR).done();
		addStatusEntries(status, stateValidName);
	}

	@Override
    public String getOldName() {
		if (alias.equals("")) {
			return selectedNode.getNameWithoutPackage();
		}
		return alias;
	}

	public void setNewName(String newName) {
		this.newclassName = newName;
		for (RenameTextEditProvider provider : textEditProviders) {
			provider.setNewName(newName);
		}
	}

	public String getNewName() {
		for (RenameTextEditProvider provider : textEditProviders) {
			return provider.getNewName();
		}
		return null;
	}

	public ClassNode getSelectedNode() {
		return selectedNode;
	}

	public static ClassNode giveClassNodeToRename(ASTNode node) {
		if (node instanceof ClassNode) {
			return (ClassNode) node;
		} else if (node instanceof ConstructorCallExpression) {
			return ((ConstructorCallExpression) node).getType();
		} else if (node instanceof ClassExpression) {
			return ((ClassExpression) node).getType();
		} else if (node instanceof ConstructorNode) {
			return ((ConstructorNode) node).getDeclaringClass();
		} else if (node instanceof ImportNode) {
			return ((ImportNode) node).getType();
		}
		return null;
	}

	@Override
	protected void prepareCandidateLists() {
	}

}
