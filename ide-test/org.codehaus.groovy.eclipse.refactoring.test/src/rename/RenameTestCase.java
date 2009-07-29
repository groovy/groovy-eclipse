/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package rename;

import core.RenameDispatcherMock;
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
import org.codehaus.groovy.eclipse.refactoring.core.rename.IAmbiguousRenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.NoRefactoringForASTNodeException;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameDispatcher;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import tests.MultiFileTestCase;

/**
 * Testcase for all of the rename refactorings
 * 
 * @author reto kleeb
 * 
 */
public class RenameTestCase extends MultiFileTestCase {

	private RenameDispatcher dispatcher;
	private RenameInfo renameInfo;

	private IAmbiguousRenameInfo renMethInfo;
	private RenameFieldInfo renFieldInfo;

	private Map<String, List<Integer>> manuallySelectedCandidates;

	public RenameTestCase(String testName, File file) {
		super(testName, file);
	}

	@Override
    public void preAction() {
		dispatcher = new RenameDispatcherMock(getDocumentProvider(), selection, getFileProvider());
		GroovyRefactoring refactoring;
		try {
			refactoring = dispatcher.dispatchRenameRefactoring();
			renameInfo = (RenameInfo) refactoring.getInfo();
		} catch (NoRefactoringForASTNodeException e1) {
			fail(e1.getMessage());
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
			selectSomeOfTheDoubtfulFieldCandidates();
		}
	}

	private void selectSomeOfTheDoubtfulFieldCandidates() {
		manuallySelectedCandidates = readAcceptedLineProperty();
		List<Integer> acceptedLines = new ArrayList<Integer>();
		for (Entry<String, List<Integer>> entry : manuallySelectedCandidates.entrySet()) {
				acceptedLines.addAll(entry.getValue());
		}
		for (Entry<IGroovyDocumentProvider, List<ASTNode>> entry : renFieldInfo.getAmbiguousCandidates().entrySet()) {
			for (ASTNode node : entry.getValue()) {
				int lineNumber = node.getLineNumber();
				if(node instanceof PropertyExpression){
					lineNumber = ((PropertyExpression)node).getProperty().getLineNumber();
				}
				if(acceptedLines.contains(lineNumber)){
					renFieldInfo.addDefinitiveEntry(entry.getKey(), node);
				}
			}
		}
	}

	private void checkForRenameMethod(RefactoringInfo info) {
		if (info instanceof RenameMethodInfo) {
			renMethInfo = (IAmbiguousRenameInfo) info;
			selectSomeOfTheDoubtfulMethodCandidates();
		}
	}

	private void selectSomeOfTheDoubtfulMethodCandidates() {
		manuallySelectedCandidates = readAcceptedLineProperty();
		for (Entry<IGroovyDocumentProvider, List<ASTNode>> entry : renMethInfo.getAmbiguousCandidates().entrySet()) {
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
