/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codebrowsing.tests;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ResolvedBinaryField;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 */
public class CodeSelectGenericsTest extends BrowsingTestCase {

    public CodeSelectGenericsTest() {
        super(CodeSelectGenericsTest.class.getName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCodeSelectGenericField1() throws Exception {
        String structureContents = "class Structure { java.util.List<String> field; }";
        String javaContents = "class Java { { new Structure().field = null;} }";
        String groovyContents = "new Structure().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericField2() throws Exception {
        String structureContents = "class Structure { java.util.Map<String, Integer> field; }";
        String javaContents = "class Java { { new Structure().field = null;} }";
        String groovyContents = "new Structure().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericField3() throws Exception {
        String structureContents = "class Structure { java.util.Map<String[], java.util.List<Integer>> field; }";
        String javaContents = "class Java { { new Structure().field = null;} }";
        String groovyContents = "new Structure().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod1() throws Exception {
        String structureContents = "class Structure { java.util.Map<String[], java.util.List<Integer>> field; }";
        String javaContents = "class Java { { new Structure().field.entrySet();} }";
        String groovyContents = "new Structure().field.entrySet()";
        String toFind = "entrySet";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod2() throws Exception {
        String structureContents = "class Structure { java.util.List<Integer> method() { return null; } }";
        String javaContents = "class Java { { new Structure().method();} }";
        String groovyContents = "new Structure().method()";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod3() throws Exception {
        String structureContents = "class Structure { java.util.List<Integer> method(java.util.List<Integer> a) { return null; } }";
        String javaContents = "class Java { { new Structure().method(null);} }";
        String groovyContents = "new Structure().method(null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod4() throws Exception {
        String structureContents = "class Structure { java.util.List<Integer> method(java.util.List<Integer> a, java.util.List<String> b) { return null; } }";
        String javaContents = "class Java { { new Structure().method(null, null);} }";
        String groovyContents = "new Structure().method(null, null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod5() throws Exception {
        String structureContents = "class Structure { java.util.List<Integer> method(int a, int b, char x) { return null; } }";
        String javaContents = "class Java { { new Structure().method(1, 2, 'c');} }";
        String groovyContents = "new Structure().method(1, 2, 'c')";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericType1() throws Exception {
        String structureContents = "class Structure<T> { }";
        String javaContents = "class Java { { Structure<Integer> x = null;  if (x instanceof Object) { x.toString(); } } }";
        String groovyContents = "Structure<Integer> obj = new Structure<Integer>()\nobj";
        String toFind = "Structure";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndField1() throws Exception {
        String structureContents = "class Structure<T> { java.util.List<T> field; }";
        String javaContents = "class Java { { new Structure<Integer>().field = null;} }";
        String groovyContents = "new Structure<Integer>().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndField2() throws Exception {
        String structureContents = "class Structure<T,U> { java.util.Map<T,U> field; }";
        String javaContents = "class Java { { new Structure<String, Integer>().field = null;} }";
        String groovyContents = "new Structure<String, Integer>().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndField3() throws Exception {
        String structureContents = "class Structure<T,U> { java.util.Map<T,U> field; }";
        String javaContents = "import java.util.List;\nclass Java { { new Structure<String[], List<Integer>> ().field = null;} }";
        String groovyContents = "new Structure<String[], List<Integer>>().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod1() throws Exception {
        String structureContents = "class Structure<T,U> { java.util.Map<T,U> field; }";
        String javaContents = "import java.util.List;\nclass Java { { new Structure<String[], List<Integer>> ().field.entrySet();} }";
        String groovyContents = "new Structure<String[], List<Integer>>().field.entrySet()";
        String toFind = "entrySet";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod2() throws Exception {
        String structureContents = "class Structure<T> { java.util.List<T> method() { return null; } }";
        String javaContents = "class Java { { new Structure<Integer>().method();} }";
        String groovyContents = "new Structure<Integer>().method()";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod3() throws Exception {
        String structureContents = "class Structure<T> { java.util.List<T> method(java.util.List<T> a) { return null; } }";
        String javaContents = "class Java { { new Structure<Integer>().method(null);} }";
        String groovyContents = "new Structure<Integer>().method(null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod4() throws Exception {
        String structureContents = "class Structure<T> { java.util.List<T> method(T a) { return null; } }";
        String javaContents = "import java.util.List;\nclass Java { { new Structure<List<Integer>>().method(null);} }";
        String groovyContents = "new Structure<List<Integer>>().method(null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod5() throws Exception {
        String structureContents = "import java.util.List;\nclass Structure<T,U> { T method(T a, List<U> b) { return null; } }";
        String javaContents = "import java.util.List;\nclass Java { { new Structure<List<String>, Integer>().method(null, null);} }";
        String groovyContents = "new Structure<List<String>, Integer>().method(null, null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    private static final String XX = "class XX { public XX[] getXx() { return null; }\npublic XX getYy() { return null; }\n }";

    public void testCodeSelectArray1() throws Exception {
        String groovyContents = "new XX().xx[0].xx";
        String toFind = "xx";
        String elementName = "getXx";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray2() throws Exception {
        String groovyContents = "new XX().xx[0].yy";
        String toFind = "yy";
        String elementName = "getYy";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray3() throws Exception {
        String groovyContents = "new XX().xx[0].getXx()";
        String toFind = "getXx";
        assertCodeSelect(XX, groovyContents, toFind);
    }

    public void testCodeSelectArray4() throws Exception {
        String groovyContents = "new XX().xx[0].getYy()";
        String toFind = "getYy";
        assertCodeSelect(XX, groovyContents, toFind);
    }

    public void testCodeSelectArray5() throws Exception {
        String groovyContents = "class YY { YY[] xx \nYY yy }\n"
                + "new YY().xx[0].setXx()";
        String toFind = "setXx";
        String elementName = "xx";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray6() throws Exception {
        String groovyContents = "class YY { YY[] xx \nYY yy }\n"
                + "new YY().xx[0].setYy()";
        String toFind = "setYy";
        String elementName = "yy";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray7() throws Exception {
        String groovyContents = "class YY { YY[] xx \nYY yy }\n"
                + "new YY().xx[0].getXx()";
        String toFind = "getXx";
        String elementName = "xx";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray8() throws Exception {
        String groovyContents = "class YY { YY[] xx \nYY yy }\n"
                + "new YY().xx[0].getYy()";
        String toFind = "getYy";
        String elementName = "yy";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    private void assertCodeSelect(String structureContents,
            String groovyContents,
            String toFind) throws Exception, JavaModelException {
        assertCodeSelect(structureContents, null, groovyContents, toFind);
    }
    private void assertCodeSelect(String structureContents,
            String javaContents, String groovyContents, String toFind)
            throws Exception, JavaModelException {
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind, toFind);
    }

    private void assertCodeSelect(String structureContents,
            String javaContents, String groovyContents, String toFind,
            String elementName) throws Exception, JavaModelException {

        if (javaContents != null) {
            createJavaUnit("Structure", structureContents);
        } else {
            // this is an array test, use a different file name
            createJavaUnit("XX", structureContents);
        }
        GroovyCompilationUnit groovyUnit = createUnit(groovyContents);
        ICompilationUnit javaUnit = null;
        if (javaContents != null) {
            javaUnit = createJavaUnit("Java", javaContents);
        }
        incrementalBuild();
        expectingNoProblems();

        // check the groovy code select
        IJavaElement[] eltFromGroovy = groovyUnit.codeSelect(
                groovyContents.lastIndexOf(toFind), toFind.length());
        assertEquals("Should have found a selection", 1, eltFromGroovy.length);
        assertEquals("Should have found reference to: " + elementName,
                elementName,
                eltFromGroovy[0].getElementName());

        // check the java code select
        if (javaUnit != null) {
            IJavaElement[] eltFromJava = javaUnit.codeSelect(
                    javaContents.lastIndexOf(toFind), toFind.length());
            assertEquals("Should have found a selection", 1, eltFromJava.length);
            assertEquals("Should have found reference to: " + elementName,
                    elementName,
                    eltFromJava[0].getElementName());

            // now check that the unique keys of each of them are the same
            String groovyUniqueKey = getUniqueKey(eltFromGroovy[0]);
            String javaUniqueKey = getUniqueKey(eltFromJava[0]);
            assertEquals("Invalid unique key from groovy", javaUniqueKey,
                    groovyUniqueKey);
        }

    }

    /**
     * @param iJavaElement
     * @return
     */
    private String getUniqueKey(IJavaElement elt) {
        if (elt instanceof ResolvedSourceField) {
            return ((ResolvedSourceField) elt).getKey();
        } else if (elt instanceof ResolvedSourceMethod) {
            return ((ResolvedSourceMethod) elt).getKey();
        } else if (elt instanceof ResolvedSourceType) {
            return ((ResolvedSourceType) elt).getKey();
        }
        if (elt instanceof ResolvedBinaryField) {
            return ((ResolvedBinaryField) elt).getKey();
        } else if (elt instanceof ResolvedBinaryMethod) {
            return ((ResolvedBinaryMethod) elt).getKey();
        } else if (elt instanceof ResolvedBinaryType) {
            return ((ResolvedBinaryType) elt).getKey();
        }
        fail("Element " + elt + " is not resolved");
        // won't get here
        return null;
    }

    /**
     * @param contents
     * @return
     * @throws Exception
     */
    private GroovyCompilationUnit createUnit(String contents) throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        return unit;
    }

    private ICompilationUnit createJavaUnit(String className, String contents)
            throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addClass(root, "", className, contents);
        ICompilationUnit unit = getJavaCompilationUnit(root, className
                + ".java");
        assertTrue("Hello groovy unit should exist", unit.exists());
        return unit;
    }

}