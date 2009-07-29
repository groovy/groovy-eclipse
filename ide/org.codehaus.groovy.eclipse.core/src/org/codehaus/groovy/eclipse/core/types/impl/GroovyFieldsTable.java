/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
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
import org.codehaus.groovy.eclipse.core.types.Type;
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

	public Type lookup(String name) {
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