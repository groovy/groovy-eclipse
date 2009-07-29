/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameField;

import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEdit;
import org.codehaus.groovy.eclipse.refactoring.core.utils.EditHelper;

/**
 * returns the proper edits for all the relevant nodes of the rename
 * field refactoring
 * @author reto kleeb
 *
 */
public class RenameFieldTextEdit extends RenameTextEdit {

	private final List<ASTNode> listOfNodesToRename;

	public RenameFieldTextEdit(IGroovyDocumentProvider docProvider,
			List<ASTNode> listOfNodesToRename, String oldName, String newName) {
		super(docProvider, oldName, newName);
		this.listOfNodesToRename = listOfNodesToRename;
	}
	
	@Override
    public void scanAST() {
		for(ASTNode nodeToVisit : listOfNodesToRename){
			if(nodeToVisit instanceof FieldNode){
				visitField((FieldNode) nodeToVisit);
			} else {
				nodeToVisit.visit(this);
			}
		}
	}

	@Override
    public void visitField(FieldNode node) {
		checkAndAddEdit(node);
	}

	@Override
    public void visitVariableExpression(VariableExpression expression) {
		checkAndAddEdit(expression);
	}

	@Override
    public void visitExpressionStatement(ExpressionStatement statement) {
		checkAndAddEdit(statement);
	}

	@Override
    public void visitFieldExpression(FieldExpression expression) {
		checkAndAddEdit(expression);
	}

	@Override
    public void visitAttributeExpression(AttributeExpression expression) {
		checkAndAddEdit(expression);
	}

	@Override
    public void visitPropertyExpression(PropertyExpression expression) {
		checkAndAddEdit(expression);
	}
	
	@Override
    public void visitConstantExpression(ConstantExpression expression) {
		checkAndAddEdit(expression);
	}

	private void checkAndAddEdit(ASTNode expression) {
		edits.addChild(EditHelper.getLookupReplaceEdit(expression, true, document, oldName, newName));
	}

}
