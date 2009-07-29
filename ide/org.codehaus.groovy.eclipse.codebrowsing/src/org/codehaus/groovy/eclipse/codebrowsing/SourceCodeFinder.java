/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codebrowsing;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * Utility class to find source code for different objects.
 * 
 * @author emp
 */
public class SourceCodeFinder {

	public static IJavaElement find(ClassNode type, IFile file) {
		return find(type, null, file);
	}

	public static IJavaElement find(ClassNode type, ASTNode member, IFile file) {
		IType javaType = getType(type, file);
		if (javaType != null) {
			IJavaElement javaElement = javaType;
			if (member instanceof FieldNode) {
				FieldNode fNode = (FieldNode) member;
				javaElement = javaType.getField(fNode.getName());
			} else if (member instanceof MethodNode) {
				MethodNode mNode = (MethodNode) member;
				Parameter[] params = mNode.getParameters();
				String[] paramTypes = new String[params.length];
				for (int i = 0; i < params.length; i++) {
					paramTypes[i] = Signature.createTypeSignature(params[i].getType().getName(), true);
				}
				javaElement = javaType.getMethod(mNode.getName(),
						paramTypes);
			}
			return javaElement;
		}
		return null;
	}

	private static IType getType(ClassNode type, IFile file) {
	    ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
	    try {
	        // pass in a null progress monitor to force looking for secondary types
            return unit.getJavaProject().findType(type.getName(), new NullProgressMonitor());
        } catch (JavaModelException e) {
            return null;
        }
    }
}
