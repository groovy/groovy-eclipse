/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.testplugin.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.testplugin.JavaProjectHelper;

import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;


public class JavaTestCase extends TestCase {

	private IJavaProject fJavaProject;

	public JavaTestCase(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite= new TestSuite();
		suite.addTest(new JavaTestCase("doTest1"));
		return suite;
	}

	/**
	 * Creates a new test Java project.
	 * 
	 * @throws Exception in case of any problem
	 */
	protected void setUp() throws Exception {
		fJavaProject= JavaProjectHelper.createJavaProject("HelloWorldProject", "bin");

		IPackageFragmentRoot root= JavaProjectHelper.addSourceContainer(fJavaProject, "src");
		IPackageFragment pack= root.createPackageFragment("ibm.util", true, null);

		ICompilationUnit cu= pack.getCompilationUnit("A.java");
		IType type= cu.createType("public class A {\n}\n", null, true, null);
		type.createMethod("public void a() {}\n", null, true, null);
		type.createMethod("public void b(java.util.Vector v) {}\n", null, true, null);
	}

	/**
	 * Removes the test java project.
	 * 
	 * @throws Exception in case of any problem
	 */
	protected void tearDown () throws Exception {
		JavaProjectHelper.delete(fJavaProject);
	}

	/*
	 * Basic test: Checks for created methods.
	 */
	public void doTest1() throws Exception {

		String name= "ibm/util/A.java";
		ICompilationUnit cu= (ICompilationUnit) fJavaProject.findElement(new Path(name));
		assertTrue("A.java must exist", cu != null);
		IType type= cu.getType("A");
		assertTrue("Type A must exist", type != null);

		System.out.println("methods of A");
		IMethod[] methods= type.getMethods();
		for (int i= 0; i < methods.length; i++) {
			System.out.println(methods[i].getElementName());
		}
		assertTrue("Should contain 2 methods", methods.length == 2);
	}


}
