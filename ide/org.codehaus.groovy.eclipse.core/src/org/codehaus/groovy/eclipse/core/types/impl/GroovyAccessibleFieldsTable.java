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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.context.impl.ClassContext;
import org.codehaus.groovy.eclipse.core.context.impl.ClassScopeContext;
import org.codehaus.groovy.eclipse.core.context.impl.ClosureScopeContext;
import org.codehaus.groovy.eclipse.core.context.impl.ConstructorScopeContext;
import org.codehaus.groovy.eclipse.core.context.impl.MethodScopeContext;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;
import org.eclipse.jdt.core.IType;
import org.objectweb.asm.Opcodes;

/**
 * Accessible Groovy class fields symbol table. This table can access fields of super types. It is only to be used in a
 * {@link ClassContext}, {@link ClassScopeContext}, {@link ConstructorScopeContext}, {@link MethodScopeContext}, or
 * {@link ClosureScopeContext}.
 * 
 * @author empovazan
 */
public class GroovyAccessibleFieldsTable implements ISymbolTable, ISourceCodeContextAware, IGroovyProjectAware {
	private ClassNode classNode;

	private ISourceCodeContext context;

	private boolean staticAccess;

	private GroovyProjectFacade project;

	public GroovyDeclaration lookup(String name) {
		if (project == null || context == null || name.equals("this")) {
			return null;
		}
		
		ClassNode superClass = classNode.getSuperClass();
		return lookup(superClass, name);
	}

	private GroovyDeclaration lookup(ClassNode classNode, String name) {
		if (classNode == null) {
			return null;
		}
		
		// Make sure this is a Groovy class.
		IType type = project.groovyClassToJavaType(classNode);
		if (type != null) {
			return null;
		}
		
		FieldNode fieldNode = classNode.getField(name);
		if (fieldNode != null) {
			int modifiers = fieldNode.getModifiers();
			if (test(modifiers, Opcodes.ACC_PUBLIC) || test(modifiers, Opcodes.ACC_PROTECTED)) {
				if (!staticAccess || test(modifiers, Opcodes.ACC_STATIC)) {
					return TypeUtil.newField(TypeUtil.newClassType(fieldNode.getDeclaringClass()), fieldNode);
				}
			}
		}

		return lookup(classNode.getSuperClass(), name);
	}

	private boolean test(int modifiers, int mask) {
		return (modifiers & mask) != 0;
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
