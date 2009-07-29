/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.StaticFieldImport;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;

/**
 * collects possible candidates for a rename method refactoring
 * @author reto kleeb
 *
 */
public class RenameMethodCandidatesCollector extends RefactoringCodeVisitorSupport {
	
	private final List<ASTNode> candidates = new ArrayList<ASTNode>();
	private final MethodPattern selectedMethodPattern;
	private final boolean exactMatchTest;
	
	public RenameMethodCandidatesCollector(ModuleNode rootNode, MethodPattern selectedMethodPattern, boolean exactMatchTest) {
		super(rootNode);
		this.selectedMethodPattern = selectedMethodPattern;
		this.exactMatchTest = exactMatchTest;
	}
	
	@Override
    public void visitStaticFieldImport(StaticFieldImport staticAliasImport) {
		super.visitStaticFieldImport(staticAliasImport);
		if(selectedMethodPattern.getMethodName().equals(staticAliasImport.getField())){
			candidates.add(staticAliasImport);
		}
	}
	
	@Override
    public void visitMethod(MethodNode node) {
		super.visitMethod(node);
		MethodPattern nodePattern = new MethodPattern(node);
		compareNode(node, nodePattern);
	}
	
	@Override
    public void visitMethodCallExpression(MethodCallExpression call) {
		super.visitMethodCallExpression(call);
		MethodPattern callPattern = new MethodPattern(call);
		compareNode(call, callPattern);
	}
	
	@Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
		super.visitStaticMethodCallExpression(call);
		MethodPattern callPattern = new MethodPattern(call);
		compareNode(call, callPattern);
	}

	private void compareNode(ASTNode call, MethodPattern pattern) {
		if(exactMatchTest){
			if(pattern.equalSignature(selectedMethodPattern)){
				candidates.add(call);
			}
		} else{
			if(pattern.getMethodName().equals(selectedMethodPattern.getMethodName())){
				candidates.add(call);			
			}
		}
	}

	public List<ASTNode> getCandidates() {
		return candidates;
	}

}
