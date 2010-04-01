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
package rename;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringInfo;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.CandidateCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.GroovyRefactoringDispatcher;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IAmbiguousRenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.NoRefactoringForASTNodeException;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tests.MultiFileTestCase;
import core.CandidateCollectorMock;
import core.GroovyRefactoringDispatcherMock;

/**
 * Testcase for all of the rename refactorings
 * 
 * @author reto kleeb
 * 
 */
public class RenameTestCase extends MultiFileTestCase {

	private CandidateCollector collector;
	private RenameInfo renameInfo;

	private IAmbiguousRenameInfo renMethInfo;
	private IAmbiguousRenameInfo renFieldInfo;

	private Map<String, List<Integer>> manuallySelectedCandidates;

	public RenameTestCase(String testName, File file) {
		super(testName, file);
	}

	@Override
    public void preAction() throws NoRefactoringForASTNodeException {
		collector = new CandidateCollectorMock(getDocumentProvider(), selection, getFileProvider());
		GroovyRefactoring refactoring;
		try {
			ASTNode candidates[] = collector.getGroovyCandidates();
			String candidate = properties.get("candidateNr");
			int selected = 0;
			if (candidate != null) {
				selected = Integer.parseInt(candidate)-1;
			}
			if (candidates.length > 0) {
    			GroovyRefactoringDispatcher dispatcher = 
    				new GroovyRefactoringDispatcherMock(candidates[selected], 
    						getDocumentProvider(), selection, getFileProvider());
    			refactoring = dispatcher.dispatchGroovyRenameRefactoring();
    			renameInfo = (RenameInfo) refactoring.getInfo();
			} else {
				throw new NoRefactoringForASTNodeException(collector.getSelectedNode());
			}
		} catch (NoRefactoringForASTNodeException e1) {
            // Maybe this is supposed to fail
		    if(shouldFail) {
                //Test the errorMessage
                if (properties.get("failMessage")!= null) {
                    assertEquals(properties.get("failMessage"),e1.getMessage());
                }
                throw e1;
            } else {
                fail(e1.getMessage());
            }
		}
	}

	@Override
    public RefactoringStatus checkInitialCondition() throws OperationCanceledException, CoreException {
		return renameInfo.checkInitialConditions(new NullProgressMonitor());
	}

	@Override
    public void simulateUserInput() {
		renameInfo.setNewName(readPropertiesFromFile());
		checkForRenameMethod(renameInfo);
		checkForRenameField(renameInfo);
	}

	@Override
    public RefactoringStatus checkFinalCondition() throws OperationCanceledException, CoreException {
		return renameInfo.checkFinalConditions(new NullProgressMonitor());
	}

	@Override
    public GroovyChange createChange() throws OperationCanceledException, CoreException {
		return renameInfo.createGroovyChange(new NullProgressMonitor());
	}
	
	private void checkForRenameField(RefactoringInfo info) {
		if (info instanceof RenameFieldInfo) {
			renFieldInfo = (RenameFieldInfo) info;
			selectSomeOfTheDoubtfulFieldCandidates(renFieldInfo);
		}
	}

	private void selectSomeOfTheDoubtfulFieldCandidates(IAmbiguousRenameInfo info) {
		manuallySelectedCandidates = readAcceptedLineProperty();
		List<Integer> acceptedLines = new ArrayList<Integer>();
		for (Entry<String, List<Integer>> entry : manuallySelectedCandidates.entrySet()) {
				acceptedLines.addAll(entry.getValue());
		}
		for (Entry<IGroovyDocumentProvider, List<ASTNode>> entry : info.getAmbiguousCandidates().entrySet()) {
			for (ASTNode node : entry.getValue()) {
				int lineNumber = node.getLineNumber();
				if(node instanceof PropertyExpression){
					lineNumber = ((PropertyExpression)node).getProperty().getLineNumber();
				}
				if(acceptedLines.contains(lineNumber)){
				    info.addDefinitiveEntry(entry.getKey(), node);
				}
			}
		}
	}

	private void checkForRenameMethod(RefactoringInfo info) {
		if (info instanceof RenameMethodInfo) {
			renMethInfo = (IAmbiguousRenameInfo) info;
			selectSomeOfTheDoubtfulMethodCandidates(renMethInfo);
		}
	}

	private void selectSomeOfTheDoubtfulMethodCandidates(IAmbiguousRenameInfo info) {
		manuallySelectedCandidates = readAcceptedLineProperty();
		for (Entry<IGroovyDocumentProvider, List<ASTNode>> entry : info.getAmbiguousCandidates().entrySet()) {
			for (ASTNode node : entry.getValue()) {
				if (node instanceof MethodNode) {
					handleMethodCallNode(entry, node);
				} else if (node instanceof MethodCallExpression) {
					handleMethodCallExpressionNode(entry, node);
				}
			}
		}
	}

	private void handleMethodCallExpressionNode(
			Entry<IGroovyDocumentProvider, List<ASTNode>> entry, ASTNode node) {

		MethodCallExpression methCall = (MethodCallExpression) node;
		for (Entry<String, List<Integer>> manualEntry : manuallySelectedCandidates.entrySet()) {
			for (Integer lineNumber : manualEntry.getValue()) {
				if (lineNumber == methCall.getLineNumber()) {
					renMethInfo.addDefinitiveEntry(entry.getKey(), node);
				}
			}
		}
	}

	private void handleMethodCallNode(
			Entry<IGroovyDocumentProvider, List<ASTNode>> entry, ASTNode node) {

		MethodNode methNode = (MethodNode) node;
		String className = methNode.getDeclaringClass().getName();
		List<Integer> listofLineNumbers = manuallySelectedCandidates.get(className);
		if (listofLineNumbers != null && listofLineNumbers.contains(methNode.getLineNumber())) {
			renMethInfo.addDefinitiveEntry(entry.getKey(), node);
		}
	}

	private Map<String, List<Integer>> readAcceptedLineProperty() {
		Map<String, List<Integer>> selectedCandidates = new HashMap<String, List<Integer>>();
		String acceptedLinesString = properties.get("acceptLines");
		if (acceptedLinesString != null) {
			for (String currentCandidate : acceptedLinesString.split(",")) {
				if (currentCandidate.split(":").length != 2) {
					throw new RuntimeException("Wrong Params in testfile");
				}
                String className = currentCandidate.split(":")[0];
                String lineNumber = currentCandidate.split(":")[1];
                if (selectedCandidates.get(className) != null) {
                	selectedCandidates.get(className).add(Integer.valueOf(lineNumber));
                } else {
                	List<Integer> listOfLineNumbers = new LinkedList<Integer>();
                	listOfLineNumbers.add(Integer.valueOf(lineNumber));
                	selectedCandidates.put(className, listOfLineNumbers);
                }
			}
		}
		return selectedCandidates;
	}

	private String readPropertiesFromFile() {
		String newName = "";
		if (properties.get("newClassName") != null) {
			newName = properties.get("newClassName");
		} else if (properties.get("newVarName") != null) {
			newName = properties.get("newVarName");
		} else if (properties.get("newMethodName") != null) {
			newName = properties.get("newMethodName");
		} else {
			newName = properties.get(("newFieldName"));
		}
		return newName;
	}

}
