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
package org.codehaus.groovy.eclipse.refactoring.core.inlineMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.SingleFileRefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.DocumentHelpers;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTNodeInfo;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTScanner;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.AssignementAndCall;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

public class InlineMethodProvider extends SingleFileRefactoringProvider {

	private boolean inlineAllInvocations, deleteMethod, methodDeclarationSelected;
	private final FindMethod finder;
	private final ASTScanner scanner;
	private final int selectedMethodBody = 0;
	private int selectedMethodCall = 0;

	public InlineMethodProvider(IGroovyDocumentProvider docProvider, UserSelection selecion) {
		super(docProvider, selecion);
		finder = new FindMethod(getSelection(), getDocument(), getRootNode());
		scanner = new ASTScanner(getRootNode(),new AssignementAndCall(),getDocument());
		scanner.startASTscan();


		if (finder.selectedMethodPattern != null) {
			if (finder.selectedMethodPattern.getNode() instanceof MethodNode) {
				methodDeclarationSelected = true;
			}
		}
		selectedMethodCall = finder.getMethodCalls().indexOf(
				finder.selectedMethodPattern);		
	}

	@Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
    public void addInitialConditionsCheckStatus(RefactoringStatus status) {
		if (finder.selectedMethodPattern == null)
			status.addFatalError(GroovyRefactoringMessages.InlineMethodInfo_No_Method_Call_Found);
		if (finder.getMethodDefinitions().size() == 0)
			status.addFatalError(GroovyRefactoringMessages.InlineMethodInfo_No_Methodbody_Found);
		if (finder.getMethodDefinitions().size() > 1)
			status.addError(GroovyRefactoringMessages.InlineMethodInfo_Multiple_Methodbodies);
		if (finder.getMethodCalls().size() == 0)
			status.addFatalError(GroovyRefactoringMessages.InlineMethodInfo_No_Methodcall_found);
		for(MethodPattern p : finder.getMethodDefinitions()) {
			if(ASTTools.hasMultipleReturnStatements(((MethodNode)p.getNode()).getCode())) {
				status.addFatalError(GroovyRefactoringMessages.InlineMethodInfo_Multiple_Returns_found);
			}
		}
	}

	@Override
    public GroovyChange createGroovyChange(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		MultiTextEdit edits = new MultiTextEdit();
		inlineMethodCalls(edits);
		if (deleteMethod){
			removeMethodBody(edits);
		}
		GroovyChange change = new GroovyChange(GroovyRefactoringMessages.InlineMethodRefactoring);
		change.addEdit(getDocumentProvider(), edits);
		return change;
	}

	private void removeMethodBody(MultiTextEdit edits) {
		MethodPattern method = finder.getMethodDefinitions().get(selectedMethodBody);
		UserSelection sel = ASTTools.includeLeedingGap(method.getNode(), getDocument());
		edits.addChild(new DeleteEdit(sel.getOffset(), sel.getLength()));
	}

	private void inlineMethodCalls(MultiTextEdit edits) {
		if (inlineAllInvocations) {
			for (MethodPattern methodCall : finder.getMethodCalls()) {
				addMecthodCallReplaceEdit(edits, methodCall);
			}
		} else {
			addMecthodCallReplaceEdit(edits, finder.getMethodCalls().get(selectedMethodCall));
		}
	}

	private void addMecthodCallReplaceEdit(MultiTextEdit edits, MethodPattern methodCall) {
		List<Variable> methParams = new ArrayList<Variable>();
		FindVariables varFinder = new FindVariables(methParams);
		finder.methodDefinitions.get(selectedMethodBody).getArguments().visit(varFinder);

		Map<String, String> renameVars = new HashMap<String, String>();
		if (methodCall.getArgSize() > 0) {
			List<Variable> callParams = new ArrayList<Variable>();
			varFinder.setContainer(callParams);
			methodCall.getArguments().visit(varFinder);

			for (int i = 0; i < callParams.size(); i++) {
				if (!callParams.get(i).getName().equals(methParams.get(i).getName())) {
					renameVars.put(methParams.get(i).getName(), callParams.get(i).getName());
				}
			}

		}
		
		ASTNode node = methodCall.getNode();
		ASTNodeInfo info = scanner.getInfo(node);
		ASTNode surroundingNode = null;
		while(info.getParent() != null) {
			if(AssignementAndCall.isAssignement(info.getParent()) || AssignementAndCall.isMethodCall(info.getParent()))
				surroundingNode = info.getParent();		
			info = scanner.getInfo(info.getParent());
		}

		IDocument methodText = getInlinedCode(renameVars);
		ASTNodeInfo nodeInfo = scanner.getInfo(methodCall.getNode());
		
		try {
			int line = methodCall.getNode().getLineNumber() - 1;
			String methodCallLine = getDocument().get(
					getDocument().getLineOffset(line),
					methodCall.getNode().getColumnNumber() - 1);	
			if (surroundingNode != null) {

				int lastLineNumber = methodText.getNumberOfLines() - 1;
				int offsetLastLine = methodText.getLineOffset(lastLineNumber);

				if(methodText.getNumberOfLines()> 1) {
				edits.addChild(new InsertEdit(
						getDocument().getLineOffset(surroundingNode.getLineNumber() - 1)
								+ ASTTools.getLeadingGap(methodCallLine).length(), methodText
								.get(0, offsetLastLine)
								+ ASTTools.getLeadingGap(methodCallLine)));
				}
				// Prevent from side effects add ()
				String lastLine = "(" + methodText.get(offsetLastLine, methodText
						.getLineLength(lastLineNumber)) + ")";

				edits.addChild(new ReplaceEdit(nodeInfo.getOffset(),
						nodeInfo.getLength(), lastLine));

			} else {
				edits.addChild(new ReplaceEdit(nodeInfo.getOffset(),
						nodeInfo.getLength(), methodText.get()));
			}

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	private IDocument getInlinedCode(Map<String, String> renameVars) {
		MethodNode methodNode = (MethodNode) finder.getMethodDefinitions().get(
				selectedMethodBody).getNode();

		try {
			BlockStatement block = (BlockStatement) methodNode.getCode();

			UserSelection methodStatements = ASTTools
					.getPositionOfBlockStatements(block, getDocument());

			String methodCode = getDocument().get(methodStatements.getOffset(),
					methodStatements.getLength());
			
			// Remove return
			methodCode = methodCode.replaceFirst("[\t ]*return\\s*", "");

			Document replace = DocumentHelpers.applyRenameEditsToDocument("script",
					renameVars, new Document(methodCode));

			return replace;

		} catch (BadLocationException e) {
			return new Document(""); 
		}
	}

	public boolean isInlineAllInvocations() {
		return inlineAllInvocations;
	}

	public void setInlineAllInvocations(boolean inlineAllInvocations) {
		this.inlineAllInvocations = inlineAllInvocations;
	}

	public boolean isDeleteMethod() {
		return deleteMethod;
	}

	public void setDeleteMethod(boolean deleteMethod) {
		this.deleteMethod = deleteMethod;
	}

	public boolean isMethodDeclarationSelected() {
		return methodDeclarationSelected;
	}

	public void setMethodDeclarationSelected(boolean methodDeclarationSelected) {
		this.methodDeclarationSelected = methodDeclarationSelected;
	}

}
