 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.types.impl;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

public class JavaAccessibleFieldsTable implements ISymbolTable, ISourceCodeContextAware, IGroovyProjectAware {
	private ISourceCodeContext context;

	private GroovyProjectFacade project;

	private boolean staticAccess;

	private ClassNode classNode;

	public GroovyDeclaration lookup(String name) {
		if (project == null || context == null) {
			return null;
		}
		
		ClassNode superClassNode = classNode.getSuperClass();
		if (superClassNode != null) {
			try {
				IType type = project.groovyClassToJavaType(superClassNode);
				return lookup(type, name);
			} catch (JavaModelException e) {
				GroovyCore.logException("Error retrieving Java element", e);
			}
		}

		return null;
	}

	private GroovyDeclaration lookup(IType type, String name) throws JavaModelException {
		// FUTURE: emp - this method is incredibly slow. Need to investigate how JDT does it so quickly.
		if (type == null || name.equals("this")) {
			return null;
		}
		
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
		IType[] superclasses = hierarchy.getAllSuperclasses(type);
		
		for (int i = 0; i < superclasses.length; i++) {
			IField field = type.getField(name);
			if (field.exists() && (!staticAccess || staticAccess && Flags.isStatic(field.getFlags()))) {
				return TypeUtil.newField(field);
			}
		}

		return null;
	}

	public void setSourceCodeContext(ISourceCodeContext context) {
		this.context = context;
		ASTNode[] path = context.getASTPath();
		for (int i = path.length - 1; i >= 0; --i) {
			if (path[i] instanceof MethodNode) {
				MethodNode methodNode = (MethodNode) path[i];
				if (methodNode.isStatic()) {
					staticAccess = true;
				}
				break;
			}
		}

		classNode = (ClassNode) context.getASTPath()[1]; // module -> class -> rest
	}

	public void setGroovyProject(GroovyProjectFacade project) {
		this.project = project;
	}
}
