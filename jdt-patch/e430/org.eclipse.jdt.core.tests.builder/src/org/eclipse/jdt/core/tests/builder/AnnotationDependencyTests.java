/*******************************************************************************
 * Copyright (c) 2009, 2014 Walter Harley and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Walter Harley (eclipse@cafewalter.com) - initial implementation
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 365992 - [builder] [null] Change of nullness for a parameter doesn't trigger a build for the files that call the method
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.File;
import junit.framework.Test;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.osgi.framework.Bundle;

/**
 * Tests to verify that annotation changes cause recompilation of dependent types.
 * See http://bugs.eclipse.org/149768
 */
public class AnnotationDependencyTests extends BuilderTests {
	private IPath srcRoot = null;
	private IPath projectPath = null;

	public AnnotationDependencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(AnnotationDependencyTests.class);
	}

	public void setUp() throws Exception {
		super.setUp();

		this.projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
		env.addExternalJars(this.projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(this.projectPath,""); //$NON-NLS-1$

		this.srcRoot = env.addPackageFragmentRoot(this.projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(this.projectPath, "bin"); //$NON-NLS-1$
	}

	protected void tearDown() throws Exception {
		this.projectPath = null;
		this.srcRoot = null;

		super.tearDown();
	}

	private void addAnnotationType() {
		String annoCode = "package p1;\n"
			+ "@interface Anno {\n"
			+ "String value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "Anno", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoInt {\n"
			+ "int value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoInt", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoBoolean {\n"
			+ "boolean value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoBoolean", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoByte {\n"
			+ "byte value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoByte", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoChar {\n"
			+ "char value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoChar", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoShort {\n"
			+ "short value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoShort", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoDouble {\n"
			+ "double value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoDouble", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoFloat {\n"
			+ "float value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoFloat", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoLong {\n"
			+ "long value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoLong", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoStringArray {\n"
			+ "String[] value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoStringArray", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoAnnotation {\n"
			+ "AnnoLong value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoAnnotation", annoCode);
		annoCode = "package p1;\n"
			+ "enum E {\n"
			+ "A, B, C\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "E", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoEnum {\n"
			+ "E value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoEnum", annoCode);
		annoCode = "package p1;\n"
			+ "@interface AnnoClass {\n"
			+ "Class<?> value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "AnnoClass", annoCode);
	}

	void setupProjectForNullAnnotations() throws JavaModelException {
		// add the org.eclipse.jdt.annotation library (bin/ folder or jar) to the project:
		Bundle[] bundles = Platform.getBundles("org.eclipse.jdt.annotation","[1.1.0,2.0.0)");
		File bundleFile = FileLocator.getBundleFileLocation(bundles[0]).get();
		String annotationsLib = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();
		IJavaProject javaProject = env.getJavaProject(this.projectPath);
		IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
		int len = rawClasspath.length;
		System.arraycopy(rawClasspath, 0, rawClasspath = new IClasspathEntry[len+1], 0, len);
		rawClasspath[len] = JavaCore.newLibraryEntry(new Path(annotationsLib), null, null);
		javaProject.setRawClasspath(rawClasspath, null);

		javaProject.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	}

	/**
	 * This test makes sure that changing an annotation on type A causes type B
	 * to be recompiled, if B references A.  See http://bugs.eclipse.org/149768
	 */
	public void testTypeAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@Anno(\"A1\")" + "\n"
			+ "public class A {}";
		String a2Code = "package p1; " + "\n"
			+ "@Anno(\"A2\")" + "\n"
			+ "public class A {}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}

	/**
	 * This test makes sure that changing an annotation on a field within type A
	 * causes type B to be recompiled, if B references A.
	 * See http://bugs.eclipse.org/149768
	 */
	public void testFieldAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A1\")" + "\n"
			+ "  protected int f;" + "\n"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A2\")" + "\n"
			+ "  protected int f;" + "\n"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}

	/**
	 * This test makes sure that changing an annotation on a method within type A
	 * causes type B to be recompiled, if B references A.
	 * See http://bugs.eclipse.org/149768
	 */
	public void testMethodAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A1\")" + "\n"
			+ "  protected int f() { return 0; }" + "\n"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A2\")" + "\n"
			+ "  protected int f() { return 0; }" + "\n"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}

	/**
	 * This test makes sure that changing an annotation on an inner type X within type A
	 * causes type B to be recompiled, if B references A.
	 * Note that B does not directly reference A.X, only A.
	 * See http://bugs.eclipse.org/149768
	 */
	public void testInnerTypeAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A1\")" + "\n"
			+ "  public class X { }" + "\n"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A2\")" + "\n"
			+ "  public class X { }" + "\n"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.A$X", "p1.B" });
	}

	/**
	 * This test makes sure that changing an annotation on a type A
	 * does not cause type B to be recompiled, if B does not reference A.
	 * See http://bugs.eclipse.org/149768
	 */
	public void testUnrelatedTypeAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@Anno(\"A1\")" + "\n"
			+ "public class A {}";
		String a2Code = "package p1; " + "\n"
			+ "@Anno(\"A2\")" + "\n"
			+ "public class A {}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was not recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214948
	public void testPackageInfoDependency() throws Exception {
		String notypes = "@question.SimpleAnnotation(\"foo\") package notypes;";
		String question = "package question;";
		String deprecatedQuestion = "@Deprecated package question;";
		String SimpleAnnotation = "package question; " + "\n"
			+ "public @interface SimpleAnnotation { String value(); }";

		IPath notypesPath = env.addClass( this.srcRoot, "notypes", "package-info", notypes );
		env.addClass( this.srcRoot, "question", "package-info", question );
		env.addClass( this.srcRoot, "question", "SimpleAnnotation", SimpleAnnotation );

		fullBuild( this.projectPath );
		expectingNoProblems();

		env.addClass( this.srcRoot, "question", "package-info", deprecatedQuestion );
		incrementalBuild( this.projectPath );
		expectingOnlySpecificProblemFor(notypesPath, new Problem("", "The type SimpleAnnotation is deprecated", notypesPath, 10, 26, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$

		env.addClass( this.srcRoot, "question", "package-info", question );
		incrementalBuild( this.projectPath );
		expectingNoProblems();
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency2() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@Anno(\"A1\")" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@Anno(\"A1\")" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency3() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoInt(24)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoInt(24)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency4() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoByte(3)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoByte(3)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency5() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoBoolean(true)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoBoolean(true)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency6() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoChar('c')" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoChar('c')" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency7() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoDouble(1.0)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoDouble(1.0)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency8() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoFloat(1.0f)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoFloat(1.0f)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency9() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoLong(1L)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoLong(1L)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency10() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoShort(3)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoShort(3)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency11() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoStringArray({\"A1\",\"A2\"})" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoStringArray({\"A1\",\"A2\"})" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency12() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoAnnotation(@AnnoLong(3))" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoAnnotation(@AnnoLong(3))" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency13() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoEnum(E.A)\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoEnum(E.A)\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency14() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoClass(Object.class)\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoClass(Object.class)\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was NOT recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency15() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@Anno(\"A1\")" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@Anno(\"A2\")" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		//  verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency16() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoInt(3)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoInt(4)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency17() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoByte(3)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoByte(4)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B"});
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency18() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoBoolean(true)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoBoolean(false)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency19() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoChar('c')" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoChar('d')" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency20() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoDouble(1.0)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoDouble(2.0)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency21() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoFloat(1.0f)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoFloat(2.0f)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency22() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoLong(1L)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoLong(2L)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency23() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoShort(3)" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoShort(5)" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency24() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoStringArray({\"A1\",\"A2\"})" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoStringArray({\"A2\",\"A1\"})" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency25() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoAnnotation(@AnnoLong(3))" + "\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoAnnotation(@AnnoLong(4))" + "\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency26() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoEnum(E.A)\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoEnum(E.C)\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317841
	 */
	public void testTypeAnnotationDependency27() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@AnnoClass(Object.class)\n"
			+ "public class A {\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "@AnnoClass(String.class)\n"
			+ "public class A {\n"
			+ "\n"
			+ "    public void foo() {\n"
			+ "        System.out.println(\"test\");"
			+ "    }"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}

	// Bug 365992 - [builder] [null] Change of nullness for a parameter doesn't trigger a build for the files that call the method
	public void testParameterAnnotationDependency01() throws JavaModelException {
		// prepare the project:
		setupProjectForNullAnnotations();

		String test1Code = "package p1;\n"	+
			"public class Test1 {\n" +
			"    public void foo() {\n" +
			"        new Test2().bar(null);\n" +
			"    }\n" +
			"}";
		String test2Code = "package p1;\n" +
			"public class Test2 {\n" +
			"    public void bar(String str) {}\n" +
			"}";

		IPath test1Path = env.addClass( this.srcRoot, "p1", "Test1", test1Code );
		env.addClass( this.srcRoot, "p1", "Test2", test2Code );

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit Test2 to add @NonNull annotation (changes number of annotations)
		String test2CodeB = "package p1;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"public class Test2 {\n" +
			"    public void bar(@NonNull String str) {}\n" +
			"}";
		env.addClass( this.srcRoot, "p1", "Test2", test2CodeB );
		incrementalBuild( this.projectPath );
		expectingProblemsFor(test1Path,
				"Problem : Null type mismatch: required \'@NonNull String\' but the provided value is null [ resource : </Project/src/p1/Test1.java> range : <81,85> category : <90> severity : <2>]");

		// verify that Test1 was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test2" });

		// fix error by changing to @Nullable (change is only in an annotation name)
		String test2CodeC = "package p1;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"public class Test2 {\n" +
			"    public void bar(@Nullable String str) {}\n" +
			"}";
		env.addClass( this.srcRoot, "p1", "Test2", test2CodeC );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that Test1 was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test2" });
	}

	// Bug 365992 - [builder] [null] Change of nullness for a parameter doesn't trigger a build for the files that call the method
	// Bug 366341 - Incremental compiler fails to detect right scope for annotation related code changes
	public void testReturnAnnotationDependency01() throws JavaModelException {
		// prepare the project:
		setupProjectForNullAnnotations();

		String test1Code = "package p1;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"public class Test1 {\n" +
			"    public @NonNull Object foo() {\n" +
			"        return new Test2().bar();\n" +
			"    }\n" +
			"}";
		String test2Code = "package p1;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"public class Test2 {\n" +
			"    public @NonNull Object bar() { return this; }\n" +
			"}";

		IPath test1Path = env.addClass( this.srcRoot, "p1", "Test1", test1Code );
		env.addClass( this.srcRoot, "p1", "Test2", test2Code );

		fullBuild( this.projectPath );
		expectingNoProblems();

		// edit Test2 to replace annotation
		String test2CodeB = "package p1;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"public class Test2 {\n" +
			"    public @Nullable Object bar() { return null; }\n" +
			"}";
		env.addClass( this.srcRoot, "p1", "Test2", test2CodeB );
		incrementalBuild( this.projectPath );
		expectingProblemsFor(test1Path,
			"Problem : Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable [ resource : </Project/src/p1/Test1.java> range : <126,143> category : <90> severity : <2>]");

		// verify that Test1 was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test2" });

		// remove annotation, error changes from can be null to unknown nullness
		String test2CodeC = "package p1;\n" +
			"public class Test2 {\n" +
			"    public Object bar() { return null; }\n" +
			"}";
		env.addClass( this.srcRoot, "p1", "Test2", test2CodeC );
		incrementalBuild( this.projectPath );
		expectingProblemsFor(test1Path,
			"Problem : Null type safety: The expression of type 'Object' needs unchecked conversion to conform to \'@NonNull Object\' [ resource : </Project/src/p1/Test1.java> range : <126,143> category : <90> severity : <1>]");

		// verify that Test1 was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test2" });

		// back to initial OK version (re-add @NonNull annotation)
		env.addClass( this.srcRoot, "p1", "Test2", test2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that Test1 was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test2" });
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=373571
	// incremental build that uses binary type for Test1 should not report spurious null errors.
	public void testReturnAnnotationDependency02() throws JavaModelException {
		// prepare the project:
		setupProjectForNullAnnotations();

		String test1Code = "package p1;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"@NonNullByDefault\n" +
			"public class Test1 {\n" +
			"    public void doStuff(int i) {\n" +
			"    }\n" +
			"}";
		env.addClass( this.srcRoot, "p1", "Test1", test1Code );
		fullBuild( this.projectPath );
		expectingNoProblems();

		// add Test2
		String test2Code = "package p1;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"@NonNullByDefault\n" +
			"public class Test2 extends Test1{\n" +
			"	@Override\n" +
			"    public void doStuff(int i) {\n" +
			"		 super.doStuff(i);\n" +
			"    }\n" +
			"}";
		env.addClass( this.srcRoot, "p1", "Test2", test2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that Test2 only was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.Test2" });

		// edit Test2 to delete annotation
		test2Code = "package p1;\n" +
			"public class Test2 extends Test1{\n" +
			"	@Override\n" +
			"    public void doStuff(int i) {\n" +
			"		 super.doStuff(i);\n" +
			"    }\n" +
			"}";
		env.addClass( this.srcRoot, "p1", "Test2", test2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();

		// verify that Test2 only was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.Test2" });
	}

	 //https://bugs.eclipse.org/bugs/show_bug.cgi?id=411771
	 //[compiler][null] Enum constants not recognized as being NonNull.
	 //This test case exposes the bug mentioned in the defect. The enum
	 //definition comes from a file different from where it is accessed.
	 public void test411771a() throws JavaModelException {
		 setupProjectForNullAnnotations();
		 String testEnumCode = "package p1;\n" +
				 "enum TestEnum {FOO };\n";
		 env.addClass( this.srcRoot, "p1", "TestEnum", testEnumCode );
		 fullBuild( this.projectPath );
		 expectingNoProblems();

		 String nullTestCode = "package p1;\n" +
				 "import org.eclipse.jdt.annotation.NonNull;\n" +
				 "public class NullTest {\n" +
				 "	public static TestEnum bla() {\n" +
				 "		@NonNull final TestEnum t = TestEnum.FOO;\n" +
				 "		return t;\n" +
				 "	}\n" +
				 "}";
		 env.addClass( this.srcRoot, "p1", "NullTest", nullTestCode );
		 incrementalBuild( this.projectPath );
		 expectingNoProblems();

		 expectingUniqueCompiledClasses(new String[] { "p1.NullTest" });
	 }

	 //https://bugs.eclipse.org/bugs/show_bug.cgi?id=411771
	 //[compiler][null] Enum constants not recognized as being NonNull.
	 //Distinguish between enum constant and enum type. The enum type should not
	 //be marked as NonNull.
	 public void test411771b() throws JavaModelException {
		 setupProjectForNullAnnotations();
		 String testEnumCode = "package p1;\n" +
				 "enum TestEnum { FOO };\n";
		 env.addClass( this.srcRoot, "p1", "TestEnum", testEnumCode );
		 fullBuild( this.projectPath );
		 expectingNoProblems();

		 String testClass = "package p1;\n" +
				 "public class X { TestEnum f; };\n";
		 env.addClass( this.srcRoot, "p1", "X", testClass );
		 incrementalBuild( this.projectPath );
		 expectingNoProblems();

		 String nullTestCode = "package p1;\n" +
				 "import org.eclipse.jdt.annotation.NonNull;\n" +
				 "public class NullTest {\n" +
				 "	public static TestEnum bla(X x) {\n" +
				 "		@NonNull final TestEnum t = x.f;\n" +
				 "		return t;\n" +
				 "	}\n" +
				 "}\n";
		 IPath test1Path = env.addClass( this.srcRoot, "p1", "NullTest", nullTestCode );
		 incrementalBuild( this.projectPath );

		 expectingProblemsFor(test1Path,
				 "Problem : Null type safety: The expression of type 'TestEnum' needs unchecked conversion to conform to " +
				 "'@NonNull TestEnum' [ resource : </Project/src/p1/NullTest.java> range : <144,147> category : <90> severity : <1>]");

		 expectingUniqueCompiledClasses(new String[] { "p1.NullTest" });
	 }

	 //https://bugs.eclipse.org/bugs/show_bug.cgi?id=411771
	 //[compiler][null] Enum constants not recognized as being NonNull.
	 //A enum may contain fields other than predefined constants. We
	 //should not tag them as NonNull.
	 public void test411771c() throws JavaModelException {
		 setupProjectForNullAnnotations();
		 String testClass = "package p1;\n" +
				 "public class A {}";
		 env.addClass( this.srcRoot, "p1", "A", testClass );
		 fullBuild( this.projectPath );
		 expectingNoProblems();

		 String testEnumCode = "package p1;\n" +
				 "enum TestEnum {\n" +
				 "	FOO;\n" +
				 "	public static A a;" +
				 "};\n";
		 env.addClass( this.srcRoot, "p1", "TestEnum", testEnumCode );
		 incrementalBuild( this.projectPath );
		 expectingNoProblems();

		 String nullTestCode = "package p1;\n" +
				 "import org.eclipse.jdt.annotation.NonNull;\n" +
				 "public class NullTest {\n" +
				 "	public static TestEnum bla() {\n" +
				 "		@NonNull final TestEnum t = TestEnum.FOO;\n" +
				 "		return t;\n" +
				 "	}\n" +
				 "	public A testint() {\n" +
				 "	@NonNull A a = TestEnum.a;\n" +
				 "		return a;\n" +
				 "	}\n" +
				 "}";
		 IPath test1Path = env.addClass( this.srcRoot, "p1", "NullTest", nullTestCode );
		 incrementalBuild( this.projectPath );
		 expectingProblemsFor(test1Path,
				 "Problem : Null type safety: The expression of type 'A' needs unchecked conversion to conform to " +
				 "'@NonNull A' [ resource : </Project/src/p1/NullTest.java> range : <208,218> category : <90> severity : <1>]");

		 expectingUniqueCompiledClasses(new String[] { "p1.NullTest" });
	 }
}
