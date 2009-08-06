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
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEdit;
import org.codehaus.groovy.eclipse.refactoring.core.utils.EditHelper;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ClassImport;

public class RenameAliasTextEdit extends RenameTextEdit{
	
	private final ClassNode oldClass;

	public RenameAliasTextEdit(IGroovyDocumentProvider docProvider, ClassNode oldClass, 
			String alias, String newAlias) {
		super(docProvider, alias, newAlias);
		this.oldClass = oldClass;
	}
	
	@Override
    public void analyzeType(ClassNode classNode) {
	    ClassNode node = classNode;
    	while (node.isArray()) {
    		node = node.getComponentType();
    	}
		if (isNodeToRename(node)) { 
			edits.addChild(EditHelper.getDefaultReplaceEdit(node, true, document, oldName, newName));
		}
	}
	
	@Override
    public void visitClassImport(ClassImport classImport) {
		super.visitClassImport(classImport);
		classImport.setNewAlias(newName);
		edits.addChild(EditHelper.getExactReplaceEdit(classImport, document));
	}
	
	@Override
    public void visitClassExpression(ClassExpression expression) {
		super.visitClassExpression(expression);
		ClassNode type = expression.getType();
		type.setSourcePosition(expression);
		if (isNodeToRename(expression.getType())) {
			edits.addChild(EditHelper.getDefaultReplaceEdit(type, true, document, oldName, newName ));
		}
	}
	
	private boolean isNodeToRename(ClassNode node) {
		//do not consider nodes with invalid start point e.g. (-1,-1), these nodes don't need to
		//be renamed
		if (new SourceCodePoint(node,SourceCodePoint.BEGIN).isValid()) {
				return node.getName().equals(oldClass.getName());
		}
		return false;
	}

}
