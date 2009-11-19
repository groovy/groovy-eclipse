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
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.context.impl.ClassContext;
import org.codehaus.groovy.eclipse.core.context.impl.ClassScopeContext;
import org.codehaus.groovy.eclipse.core.context.impl.ClosureScopeContext;
import org.codehaus.groovy.eclipse.core.context.impl.ConstructorScopeContext;
import org.codehaus.groovy.eclipse.core.context.impl.MethodScopeContext;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;
import org.objectweb.asm.Opcodes;

/**
 * Groovy fields symbol table. It is only to be used in a {@link ClassContext}, {@link ClassScopeContext},
 * {@link ConstructorScopeContext}, {@link MethodScopeContext}, or {@link ClosureScopeContext}.
 * 
 * @author empovazan
 */
public class GroovyFieldsTable implements ISymbolTable, ISourceCodeContextAware {
	private ISourceCodeContext context;

	private ClassNode classNode;

	private boolean staticAccess;

	public GroovyDeclaration lookup(String name) {
		if (context == null || name.equals("this") || name.equals("super")) {
			return null;
		}

		FieldNode fieldNode = classNode.getField(name);
		if (fieldNode != null) {
			if (!staticAccess || (fieldNode.getModifiers() & Opcodes.ACC_STATIC) != 0) {
				return TypeUtil.newField(fieldNode);
			}
		}
		
		return null;
	}

	public void setSourceCodeContext(ISourceCodeContext context) {
		this.context = context;

		classNode = (ClassNode) context.getASTPath()[1]; // moduleNode -> classNode -> rest.

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
	}
}