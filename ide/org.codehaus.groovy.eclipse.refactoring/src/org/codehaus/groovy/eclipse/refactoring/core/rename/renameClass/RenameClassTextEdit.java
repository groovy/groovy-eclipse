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
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEdit;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.EditHelper;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ImportResolver;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ClassImport;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.StaticClassImport;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.StaticFieldImport;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class RenameClassTextEdit extends RenameTextEdit {
	
	private boolean renameAll = true;
	protected ClassNode oldClass;
	//set the start of the ClassNode representing a type to an invalid position
	private SourceCodePoint startOfDeclarationClassNode = new SourceCodePoint(0,0);

	public RenameClassTextEdit(IGroovyDocumentProvider docProvider,
			ClassNode oldClass, String newClassName) {
		super(docProvider, oldClass.getName(), newClassName);
		this.oldClass = oldClass;
		ModuleNode rootNode = docProvider.getRootNode();
		setOldAndNewName(rootNode, oldClass, newClassName);
		if (!ImportResolver.asAlias(rootNode, oldClass).equals("")) {
			renameAll = false;
		}
	}

	private void setOldAndNewName(ModuleNode rootNode, ClassNode oldClass,
			String newClassName) {
		//get full qualified name if it was explicit written in the source
		this.oldName = ImportResolver.getResolvedClassName(rootNode, oldClass,true);
		//get package of old class with a dot at the end. Because rootNode.getPackageName() 
		//delivers it like that
		String oldClassPackage = oldClass.getPackageName();
		if (oldClassPackage != null) {
			oldClassPackage += ".";
		}
		
		if (rootNode.getPackageName() == null && oldClassPackage == null) {
			//if the classes are not in a package
			this.newName = newClassName;
		} else if (rootNode.getPackageName() != null && rootNode.getPackageName().equals(oldClassPackage)) {
			//if the class is used in the same package like the definition
			this.newName = newClassName;
		} else if (!this.oldName.equals(oldClass.getName())) {
			//if package are not the same and the name has been resolved, there are import -> no package 
			//needed
			this.newName = newClassName;
		} else {
			//if package are not the same but resolve did nothing, there are no imports -> package needed
			this.newName = oldClass.getPackageName() + "." + newClassName;
		}
	}
	
	@Override
    public void analyzeType(ClassNode classNode) {
	    ClassNode node = classNode;
		super.analyzeType(node);
    	while (node.isArray()) {
    		node = node.getComponentType();
    	}
		SourceCodePoint typeStart = new SourceCodePoint(node,SourceCodePoint.BEGIN);
		if (isNodeToRename(node) && !typeStart.equals(startOfDeclarationClassNode) ) {
			ReplaceEdit edit = EditHelper.getDefaultReplaceEdit(node, renameAll, document, oldName, newName);
			for (TextEdit existing : edits.getChildren()) {
				if (existing.covers(edit)) {
					edit = null;
					break;
				}
			}
			if (edit != null) {
			    edits.addChild(edit);
			}
		}
	}

	@Override
    public void visitClassImport(ClassImport classImport) {
		super.visitClassImport(classImport);
		if (isNodeToRename(classImport.getType())) { 
			classImport.setNewClassName(newName);
			edits.addChild(EditHelper.getExactReplaceEdit(classImport, document));
		}
	}

	@Override
    public void visitStaticClassImport(StaticClassImport staticClassImport) {
		super.visitStaticClassImport(staticClassImport);
		if (isNodeToRename(staticClassImport.getType())) {
			staticClassImport.setNewClassName(newName);
			edits.addChild(EditHelper.getExactReplaceEdit(staticClassImport, document));
		}
	}

	@Override
    public void visitStaticFieldImport(StaticFieldImport staticAliasImport) {
		super.visitStaticFieldImport(staticAliasImport);
		if (isNodeToRename(staticAliasImport.getType())) {
			staticAliasImport.setNewClassName(newName);
			edits.addChild(EditHelper.getExactReplaceEdit(staticAliasImport, document));
		}
	}
	
	@Override
    public void visitClass(ClassNode node) {
		super.visitClass(node);
		if (isNodeToRename(node)) {
			edits.addChild(EditHelper.getLookupReplaceEdit(node,renameAll, document, oldName, newName));
		}
	}
	
	@Override
    public void visitClassExpression(ClassExpression expression) {
		super.visitClassExpression(expression);
		ClassNode type = expression.getType();
		//the ClassExpression has the correct source information but the compare must be based on the 
		//referenced ClassNode inside it. Therefore set the correct sourceposition to the ClassNode
		if (ASTTools.hasValidPosition(expression)) {
			type.setSourcePosition(expression);
			if (isNodeToRename(expression.getType())) {
				edits.addChild(EditHelper.getDefaultReplaceEdit(type, renameAll, document, oldName, newName ));
			}
		}
	}
	
	@Override
    public void visitConstructor(ConstructorNode node) {
		super.visitConstructor(node);
		if (ASTTools.hasValidPosition(node)) {
			if (isNodeToRename(node.getDeclaringClass())) {
				edits.addChild(EditHelper.getLookupReplaceEdit(node, renameAll, document, oldName, newName));
			}
		}
	}
	
	
	@Override
    public void visitFieldExpression(FieldExpression expression) {
		if (!expression.getType().getNameWithoutPackage().equals("MetaClass")) {
			super.visitFieldExpression(expression);
			if (isNodeToRename(expression.getField().getDeclaringClass()) && ASTTools.hasValidPosition(expression)) {
				edits.addChild(EditHelper.getDefaultReplaceEdit(expression, renameAll, document, oldName, newName));
			}
		}
	}
	
	//Just overridden to save the ClassNode which specifies the type. This is needed to avoid overlapping
	//text edits in multi declaration like String s1, s2. These are two declaration with the same ClassNode
	//representing the type.
	@Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
		super.visitDeclarationExpression(expression);
		ClassNode type = expression.getLeftExpression().getType();
		//if the type was renamed, save the start of the ClassNode
		if (isNodeToRename(type)) {
			startOfDeclarationClassNode = new SourceCodePoint(type,SourceCodePoint.BEGIN);
		}
	}
	
	private boolean isNodeToRename(ClassNode node) {
		//do not consider nodes with invalid source position e.g. (-1,-1)(-1,-1), these nodes don't need to
		//be renamed
		if (ASTTools.hasValidPosition(node)) {
				return oldClass.equals(node);
		}
		return false;
	}

}
