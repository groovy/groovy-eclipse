/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;


public class HelloWorld extends TestCase {

	private IJavaProject fJProject;


	public static Test suite() {
		TestSuite suite= new TestSuite();
		suite.addTest(new HelloWorld("test1"));
		return suite;
	}

	public HelloWorld(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
			fJProject= JavaProjectHelper.createJavaProject("TestProject1", "bin");
	}


	protected void tearDown() throws Exception {
		JavaProjectHelper.delete(fJProject);
	}

	public void test1() throws Exception {
		if (JavaProjectHelper.addRTJar(fJProject) == null) {
			assertTrue("jdk not found", false);
			return;
		}

		String name= "java/util/Vector.java";
		IClassFile classfile= (IClassFile) fJProject.findElement(new Path(name));
		assertTrue("classfile not found", classfile != null);

		IType type= classfile.getType();
		System.out.println("methods of Vector");
		IMethod[] methods= type.getMethods();
		for (int i= 0; i < methods.length; i++) {
			System.out.println(methods[i].getElementName());
		}
	}

}
