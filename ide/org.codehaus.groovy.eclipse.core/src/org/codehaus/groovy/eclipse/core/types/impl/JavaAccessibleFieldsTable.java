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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.Type;
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

	public Type lookup(String name) {
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

	private Type lookup(IType type, String name) throws JavaModelException {
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
