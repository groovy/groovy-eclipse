/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Helper class to recursivley build the hierarchy of a given element
 * 
 * @author Stefan Reinhard
 */
public class HierarchyBuilder {
	
	private static ITypeHierarchy javaHierarchy;
	
	/**
	 * Adds the existing hierarchy of declaringType to to declaringClass
	 * 
	 * @param declaringType
	 * @param declaringClass
	 */
	public static void addHierarchyToType(IType declaringType, ClassNode declaringClass) {
		try {
			if (javaHierarchy==null) initHierarchy(declaringType);
			String superClassName = declaringType.getSuperclassName();
			ClassNode superClass = ClassHelper.makeWithoutCaching(superClassName);
			declaringClass.setSuperClass(superClass);
			
			if (javaHierarchy.getSuperclass(declaringType) != null) {
				IType parent = javaHierarchy.getSuperclass(declaringType);
				addHierarchyToType(parent, superClass);
			} else {
				javaHierarchy=null;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private static void initHierarchy(IType typeRoot) {
		IProject project = typeRoot.getResource().getProject();
		IJavaProject javaProject = JavaCore.create(project);
		try {
			javaHierarchy = typeRoot.newTypeHierarchy(javaProject, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
}
