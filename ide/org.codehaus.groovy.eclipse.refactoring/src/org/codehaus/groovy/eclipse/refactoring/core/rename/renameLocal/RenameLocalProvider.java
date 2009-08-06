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
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import java.text.MessageFormat;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.SingleFileRefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyConventionsBuilder;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author reto kleeb
 * Contains all the information for the Rename Local refactoring
 */
public class RenameLocalProvider extends SingleFileRefactoringProvider implements IRenameProvider {
	
	private final VariableProxy selectedNode;
	private final RenameTextEditProvider textEditProvider;

	public RenameLocalProvider(IGroovyDocumentProvider docProvider,
			UserSelection selecion, VariableProxy selectedNode, MethodNode method) {
		super(docProvider, selecion);
		this.selectedNode = selectedNode;
		this.textEditProvider = new RenameLocalTextEditProvider(docProvider, selectedNode,method);
	}
	
	public void setNewName(String newVarName) {
		textEditProvider.setNewName(newVarName);
	}
	
	@Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus state = new RefactoringStatus();
		String newName = textEditProvider.getNewName();
		List<String> usedNames = textEditProvider.getAlreadyUsedNames();
		for (String name : usedNames) {
			if (name.equals(newName)) {
				state.addWarning(MessageFormat.format(
					GroovyRefactoringMessages.RenameLocalInfo_VariableAlreadyExists,newName));
			}
		}
		return state;
	}

	@Override
    public void addInitialConditionsCheckStatus(RefactoringStatus status) {
		//Do nothing
		//checkNrOfCandidates(status);
	}
	
	@Override
    public GroovyChange createGroovyChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		GroovyChange change = new GroovyChange(GroovyRefactoringMessages.RenameLocalRefactoring);
		change.addEdit(textEditProvider);
		return change;
	}

	public void checkUserInput(RefactoringStatus status, String input) {
		IStatus stateValidName = new GroovyConventionsBuilder(input, GroovyConventionsBuilder.VARIABLE)
				.validateGroovyIdentifier().validateLowerCase(IStatus.WARNING)
				.done();
		addStatusEntries(status, stateValidName);
	}

	public String getOldName() {
		return selectedNode.getName();
	}
	
	public static VariableProxy giveVariableExpressionToRename(ASTNode node){
		VariableProxy vp = new VariableProxy();
		vp.setSourcePosition(node);
		if(node instanceof Parameter){
			vp.setVariable((Parameter)node);
			return vp;
		} else if(node instanceof VariableExpression){
			Variable accessedVariable = ((VariableExpression)node).getAccessedVariable();
			if(!((accessedVariable instanceof FieldNode || accessedVariable instanceof PropertyNode))) {
				vp.setVariable(accessedVariable);
				return vp;
			}
		}
		return null;
	}
	
}
