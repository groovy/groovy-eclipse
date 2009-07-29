/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.IASTNodePredicate;

public abstract class ASTScannerPredicate extends RefactoringCodeVisitorSupport {

	protected IASTNodePredicate predicate;

	public ASTScannerPredicate(ModuleNode rootNode, IASTNodePredicate predicate) {
		super(rootNode);
		this.predicate = predicate;
	}

	@Override
    protected void analyzeNode(ASTNode node) {
		ASTNode evaluatedNode;
		if((evaluatedNode = predicate.evaluate(node)) != null) {
			doOnPredicate(evaluatedNode);
		}
	}
	
	@Override
    public void visitStaticFieldImport(StaticFieldImport staticAliasImport) {
		analyzeNode(staticAliasImport);
	}
	
	@Override
    public void visitStaticClassImport(StaticClassImport staticClassImport) {
		analyzeNode(staticClassImport);
	}
	
	@Override
    public void visitClassImport(ClassImport classImport) {
		analyzeNode(classImport);
	}

	abstract void doOnPredicate(ASTNode node);
}