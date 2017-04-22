/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.testplugin.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.testplugin.JavaProjectHelper;


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
