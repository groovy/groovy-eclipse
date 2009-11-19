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
