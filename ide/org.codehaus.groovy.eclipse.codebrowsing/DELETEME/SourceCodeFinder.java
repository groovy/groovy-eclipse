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
