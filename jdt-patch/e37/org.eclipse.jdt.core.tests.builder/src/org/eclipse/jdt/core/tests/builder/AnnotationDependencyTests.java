/*******************************************************************************
 * Copyright (c) 2009, 2010 Walter Harley and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Walter Harley (eclipse@cafewalter.com) - initial implementation
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;

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
}
