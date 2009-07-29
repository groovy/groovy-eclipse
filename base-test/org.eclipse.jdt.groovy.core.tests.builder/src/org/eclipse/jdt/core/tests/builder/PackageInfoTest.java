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
package org.eclipse.jdt.core.tests.builder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import junit.framework.Test;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;

public class PackageInfoTest extends BuilderTests {
	
public PackageInfoTest(String name) {
	super(name);
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 3 };
//	TESTS_RANGE = new int[] { 21, 50 };
}
public static Test suite() {
    return buildTestSuite(PackageInfoTest.class);
}
public void test001() throws JavaModelException {
    IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
    env.addExternalJars(projectPath, Util.getJavaClassLibs());
    fullBuild(projectPath);
    
    // remove old package fragment root so that names don't collide
    env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
    
    IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
    env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
    
    env.addClass(root, "pack", "Annot", //$NON-NLS-1$ //$NON-NLS-2$
        "package pack;\n"+ //$NON-NLS-1$
        "public @interface Annot {}" //$NON-NLS-1$
    );

    incrementalBuild(projectPath);
   
    IPath packageInfoPath = env.addFile(root, "pack/package-info.java", //$NON-NLS-1$ //$NON-NLS-2$
        "@Annot package p1" //$NON-NLS-1$
    );
        
    incrementalBuild(projectPath);
    expectingOnlyProblemsFor(packageInfoPath);
    final Problem[] problems = env.getProblems();
	Arrays.sort(problems);
    assertNotNull(problems);
    final StringWriter stringWriter = new StringWriter();
    final PrintWriter writer = new PrintWriter(stringWriter);
    final int problemsLength = problems.length;
    if (problemsLength == 1) {
        writer.print(problems[0].getMessage());    
    } else {
        for (int i = 0; i < problemsLength - 1; i++) {
            writer.println(problems[i].getMessage());
        }
        writer.print(problems[problemsLength - 1].getMessage());
    }
    writer.close();
    final String expectedOutput =
        "Syntax error on token \"p1\", ; expected after this token\n" + 
    	"The declared package \"p1\" does not match the expected package \"pack\""; 
    assertSourceEquals("Different messages", expectedOutput, stringWriter.toString());
}
public void test002() throws JavaModelException {
    IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
    env.addExternalJars(projectPath, Util.getJavaClassLibs());
    fullBuild(projectPath);
    
    // remove old package fragment root so that names don't collide
    env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
    
    IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
    env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
    
    env.addClass(root, "testcase", "Main", //$NON-NLS-1$ //$NON-NLS-2$
		"package testcase;\n" +
		"\n" +
		"public class Main {\n" +
		"    public static void main(String[] argv) throws Exception {\n" +
        "		Package pkg = Package.getPackage(\"testcase\");\n" +
        "		System.out.print(pkg.getAnnotation(TestAnnotation.class));\n" +
		"		pkg = Class.forName(\"testcase.package-info\").getPackage();\n" +
		"		System.out.print(pkg.getAnnotation(TestAnnotation.class));\n" +
		"    }\n" +
		"}"
    );

    env.addClass(root, "testcase", "TestAnnotation", //$NON-NLS-1$ //$NON-NLS-2$
		"package testcase;\n" +
		"\n" +
		"import static java.lang.annotation.ElementType.PACKAGE;\n" +
		"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
		"\n" +
		"import java.lang.annotation.Retention;\n" +
		"import java.lang.annotation.Target;\n" +
		"\n" +
		"@Target(PACKAGE)\n" +
		"@Retention(RUNTIME)\n" +
		"public @interface TestAnnotation {\n" +
		"}"
        );
   
    env.addFile(root, "testcase/package-info.java", //$NON-NLS-1$ //$NON-NLS-2$
        "@TestAnnotation package testcase;" //$NON-NLS-1$
    );
        
    incrementalBuild(projectPath);
	expectingNoProblems();
	executeClass(projectPath, "testcase.Main", "@testcase.TestAnnotation()@testcase.TestAnnotation()", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
}
public void test003() throws JavaModelException {
    IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
    env.addExternalJars(projectPath, Util.getJavaClassLibs());
    fullBuild(projectPath);
    
    // remove old package fragment root so that names don't collide
    env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
    
    IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
    env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

    env.addPackage(root, "testcase");
    IPath packageInfoPath = env.addFile(root, "testcase/package-info.java", //$NON-NLS-1$ //$NON-NLS-2$
        "" //$NON-NLS-1$
    );
        
    incrementalBuild(projectPath);
//    expectingOnlyProblemsFor(packageInfoPath);
	expectingOnlySpecificProblemFor(packageInfoPath, new Problem("testcase/package-info.java", "The declared package \"\" does not match the expected package \"testcase\"", packageInfoPath, 0, 0, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
}

//test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=252555 : NPE on duplicate package-info
public void test004() throws JavaModelException {
    IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
    env.addExternalJars(projectPath, Util.getJavaClassLibs());
    fullBuild(projectPath);

    // remove old package fragment root so that names don't collide
    env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

    IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
    env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
    
    IPath otherRoot = env.addPackageFragmentRoot(projectPath, "test"); //$NON-NLS-1$
    env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

    env.addPackage(root, "my.foo");
    env.addPackage(otherRoot, "my.foo");
    
    env.addFile(root, "my/foo/package-info.java", //$NON-NLS-1$ //$NON-NLS-2$
    		"/**\n" +
            "* A demo package for foo.\n" +
            "*/\n" +
            "package my.foo;\n"
        );
    
    IPath otherPackageInfoPath = env.addFile(otherRoot, "my/foo/package-info.java", //$NON-NLS-1$ //$NON-NLS-2$
            "/**\n" +
            "* A demo package for foo.\n" +
            "*/\n" +
            "package my.foo;\n"
            );

    incrementalBuild(projectPath);
	expectingOnlySpecificProblemFor(otherPackageInfoPath, new Problem("my/foo/package-info.java", "The type package-info is already defined", otherPackageInfoPath, 0, 0, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
}


protected void assertSourceEquals(String message, String expected, String actual) {
    if (actual == null) {
        assertEquals(message, expected, null);
        return;
    }
    actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(actual);
    if (!actual.equals(expected)) {
        System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(actual.toString(), 0));
    }
    assertEquals(message, expected, actual);
}
public static Class testClass() {
	return PackageInfoTest.class;
}
}
