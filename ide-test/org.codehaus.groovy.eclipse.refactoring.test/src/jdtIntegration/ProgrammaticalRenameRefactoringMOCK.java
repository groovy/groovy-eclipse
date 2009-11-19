/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IAmbiguousRenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * @author Stefan Sidler
 */
public class ProgrammaticalRenameRefactoringMOCK extends GroovyRefactoring{
	
	public static Map<String, List<Integer[]>> simulateUserInput;
	
	
	public ProgrammaticalRenameRefactoringMOCK(RenameInfo info, String s) {
		super(info);
		setName(s);
		moveAmbigousCandidates();
	}

	private void moveAmbigousCandidates() {
		if (refactoringInfo instanceof IAmbiguousRenameInfo) {
			IAmbiguousRenameInfo ambiguousInfo = (IAmbiguousRenameInfo) refactoringInfo;
			
			Map<IGroovyDocumentProvider, List<ASTNode>> changeList = getSpecifiedAmbiguousCandidates(simulateUserInput, ambiguousInfo);
			change(changeList, ambiguousInfo);
			ambiguousInfo.removeAllAmbiguousEntrys();

		}
	}

	private Map<IGroovyDocumentProvider, List<ASTNode>> getSpecifiedAmbiguousCandidates(Map<String, List<Integer[]>> userInput, IAmbiguousRenameInfo ambiguousInfo) {
		
		Map<IGroovyDocumentProvider, List<ASTNode>> changeList = new HashMap<IGroovyDocumentProvider, List<ASTNode>>();
		for (IGroovyDocumentProvider docProvider : ambiguousInfo.getAmbiguousCandidates().keySet()) {
			List<ASTNode> astNodes = ambiguousInfo.getAmbiguousCandidates().get(docProvider);
			
			for (ASTNode astNode : astNodes) {
				
				List<Integer[]> testLineNumbers = userInput.get(docProvider.getName());
				for (Integer testLineNumber[] : testLineNumbers) {
					if (testLineNumber[0] == astNode.getLineNumber()) {
						// Specified in Testfile
						if (changeList.containsKey(docProvider)){
							List<ASTNode> list = changeList.get(docProvider);
							list.add(astNode);
						}
						else {
							List<ASTNode> list = new ArrayList<ASTNode>(0);
							list.add(astNode);
							changeList.put(docProvider, list);
						}
						testLineNumber[1]++;
						System.out.println(" - Found ambiguous candidate at file '" +
											docProvider.getName() +
											"' on line '" + 
											astNode.getLineNumber() +
											"' - moved to definitive candidates");
					}
				}
			}
		}
		return changeList;
	}

	private void change(Map<IGroovyDocumentProvider, List<ASTNode>> changeList, IAmbiguousRenameInfo info) {
		Set<IGroovyDocumentProvider> keys = changeList.keySet();
		for (IGroovyDocumentProvider key : keys) {
			List<ASTNode> nodes = changeList.get(key);
			for (ASTNode node : nodes) {
				info.addDefinitiveEntry(key, node);
			}
		}
		
	}
	
	public Change createChange(IProgressMonitor pm) throws CoreException,
		OperationCanceledException {
		moveAmbigousCandidates();
		return super.createChange(pm);
	}
}
