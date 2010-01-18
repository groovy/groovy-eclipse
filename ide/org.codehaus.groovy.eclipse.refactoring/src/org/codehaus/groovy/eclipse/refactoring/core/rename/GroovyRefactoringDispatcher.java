/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.CompilationUnitFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.GroovyCompilationUnitDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.RenameLocalProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.VariableProxy;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTNodeInfo;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTScanner;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.NodeNotFoundException;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.RenameSelectionInfoPredicate;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * @author Stefan Reinhard
 */
public class GroovyRefactoringDispatcher {
	
	private ASTNode selectedNode;
	private UserSelection selection;
	protected IGroovyDocumentProvider docProvider;
	private final ICompilationUnit unit;
	
	public GroovyRefactoringDispatcher(ASTNode selectedNode, UserSelection selection, IGroovyDocumentProvider docProv, ICompilationUnit unit) {
		this.selectedNode = selectedNode;
		this.selection = selection;
		docProvider = docProv;
		this.unit = unit;
	}
	
	public GroovyRefactoring dispatchGroovyRenameRefactoring() throws NoRefactoringForASTNodeException {
		
		GroovyRefactoring refactoring = null;
		
		if (selectedNode instanceof ClassNode){
			return initRenameClassRefactoring((ClassNode) selectedNode);
		} else if (selectedNode instanceof FieldNode) {
			FieldPattern fieldPattern = new FieldPattern((FieldNode) selectedNode);
			refactoring = initRenameFieldRefactoring(fieldPattern);
		} else if (selectedNode instanceof MethodNode) {
			MethodNode method = (MethodNode) selectedNode;
			MethodPattern methodPattern = new MethodPattern(method, method.getDeclaringClass());
			refactoring = initRenameMethodRefactoring(methodPattern);
		} else if (selectedNode instanceof VariableProxy){
			refactoring = initRenameLocalRefactoring((VariableProxy) selectedNode);
		} else if (refactoring == null) {
			throw new NoRefactoringForASTNodeException(selectedNode);
		}
		
		return refactoring;
	}
	
	protected GroovyRefactoring initRenameFieldRefactoring(FieldPattern renameFieldPattern) {
		RefactoringProvider provider = new RenameFieldProvider(getWSFileProvider(), renameFieldPattern);
		RenameFieldInfo info = new RenameFieldInfo(provider);
		return new AmbiguousRenameRefactoring(info,GroovyRefactoringMessages.RenameFieldRefactoring);
	}

	protected GroovyRefactoring initRenameLocalRefactoring(VariableProxy variableProxy) {
		RenameSelectionInfoPredicate renameSelectionInfoPredicate = new RenameSelectionInfoPredicate(selection,docProvider.getDocument());
		ASTScanner infoBuilder = new ASTScanner(docProvider.getRootNode(), renameSelectionInfoPredicate,docProvider.getDocument());
		infoBuilder.startASTscan();
		ASTNode currentCandidate = getLocalMethodASTNode(infoBuilder);
		MethodNode localMethod = findRenameLocalMethod(infoBuilder, currentCandidate);
		RefactoringProvider provider = new RenameLocalProvider(docProvider, selection, variableProxy, localMethod);
		RenameInfo info = new RenameInfo(provider);
		return new RenameRefactoring(info,GroovyRefactoringMessages.RenameLocalRefactoring);
	}

	protected GroovyRefactoring initRenameClassRefactoring(ClassNode selectedNode) {
		RefactoringProvider provider = new RenameClassProvider(getWSFileProvider(), selectedNode, unit);
		RenameInfo info = new RenameInfo(provider);
		return new RenameClassRefactoring(info);
	}
	
	protected GroovyRefactoring initRenameMethodRefactoring(MethodPattern selectedPattern) {
		RefactoringProvider provider = new RenameMethodProvider(getWSFileProvider(), selectedPattern, unit);
		RenameMethodInfo info = new RenameMethodInfo(provider);
		return new AmbiguousRenameRefactoring(info, GroovyRefactoringMessages.RenameMethodRefactoring);
	}

	protected IGroovyFileProvider getWSFileProvider() {
        return new CompilationUnitFileProvider(new GroovyCompilationUnitDocumentProvider(docProvider.getUnit()));
	}
	
	private ASTNode getLocalMethodASTNode(
			ASTScanner infoBuilder) {
		//get the tightest selection, inner most node 
		ASTNode currentCandidate = null;
		for (Entry<ASTNode, ASTNodeInfo> entry : infoBuilder.getMatchedNodes().entrySet()){
			ASTNode key = entry.getKey();
			if(currentCandidate == null){
				currentCandidate = key;
			} else { 
				//test if node's startpoint is after selectedNode's startpoint
				SourceCodePoint nodeStartPoint = new SourceCodePoint(key,SourceCodePoint.BEGIN);
				SourceCodePoint selectedNodeStartPoint = new SourceCodePoint(currentCandidate,SourceCodePoint.BEGIN);
				if (nodeStartPoint.isAfter(selectedNodeStartPoint) || key instanceof FieldExpression) {
						currentCandidate = key;
				}
			}
		}
		return currentCandidate;
	}
	
	private MethodNode findRenameLocalMethod(ASTScanner infoBuilder, ASTNode startNode) {
	    ASTNode start = startNode;
		try {
			ASTNodeInfo infoTmp = infoBuilder.getInfo(start);
			while(!(infoTmp.getParent() instanceof MethodNode)){
				start = infoTmp.getParent();
				infoTmp = infoBuilder.getInfo(start);
			}
			return  (MethodNode)infoTmp.getParent();
		} catch (NodeNotFoundException e) {
			return null;
		}
	}

}
