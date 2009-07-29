/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod;

import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEdit;
import org.codehaus.groovy.eclipse.refactoring.core.utils.EditHelper;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.StaticFieldImport;

/**
 * adds the proper rename edits for the relevant nodes
 * @author reto kleeb
 *
 */
public class RenameMethodTextEdit extends RenameTextEdit {

	private final List<ASTNode> listOfNodesToRename;

	public RenameMethodTextEdit(IGroovyDocumentProvider docProvider, List<ASTNode> listOfNodesToRename, String newName, String oldName) {
		super(docProvider, oldName, newName);
		this.listOfNodesToRename = listOfNodesToRename;
	}
	
	@Override
    public void scanAST() {
		for(ASTNode nodeToRename : listOfNodesToRename){
			if(nodeToRename instanceof MethodNode){
				visitMethod((MethodNode) nodeToRename);
			} else {
				nodeToRename.visit(this);
			}
		}
	}
	
	@Override
    public void visitStaticFieldImport(StaticFieldImport staticAliasImport) {
		staticAliasImport.setNewField(newName);
		edits.addChild(EditHelper.getExactReplaceEdit(staticAliasImport, document));
	}
	
	@Override
    public void visitMethod(MethodNode node) {
		addLookupEditForNode(node);
	}
	
	@Override
    public void visitMethodCallExpression(MethodCallExpression call) {
		addLookupEditForNode(call);
	}
	
	@Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
		addLookupEditForNode(call);
	}

	private void addLookupEditForNode(ASTNode node) {
		edits.addChild(EditHelper.getLookupReplaceEdit(node, true, document, oldName, newName));
	}
	
}
