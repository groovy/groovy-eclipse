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
package org.codehaus.groovy.eclipse.codeassist.tests;

import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyExtendedCompletionContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * 
 * @author Andrew Eisenberg
 * @created May 4, 2011
 */
public class ExtendedCompletionContextTests extends CompletionTestCase {
    
    private static final String STRING_SIG = "Ljava.lang.String;";
    private static final String STRING_ARR_SIG = "[Ljava.lang.String;";
    private static final String STRING_2ARR_SIG = "[[Ljava.lang.String;";
    private static final String INTEGER_SIG = "Ljava.lang.Integer;";
    private static final String INTEGER_ARR_SIG = "[Ljava.lang.Integer;";
    private static final String INT_SIG = "I";
    private static final String LIST_SIG = "Ljava.util.List;";
    private static final String LIST_ARR_SIG = "[Ljava.util.List;";

    public ExtendedCompletionContextTests(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(null, true);
        if (workingCopies != null) {
            for (ICompilationUnit unit : workingCopies) {
                try {
                    unit.discardWorkingCopy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void testExtendedContextInScript1() throws Exception {
        String contents = "def x = 9\ndef y = ''\ndef z = []\nint a\nString b\nList c\nz";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertExtendedContextElements(context, INTEGER_SIG, "x", "a");
        assertExtendedContextElements(context, STRING_SIG, "y", "b");
        assertExtendedContextElements(context, LIST_SIG, "z", "c");
    }
    
    public void testExtendedContextInScript2() throws Exception {
        String contents = "int[] x\nString[] y\nList[] z\nint a\nString b\nList c\nz";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertExtendedContextElements(context, INTEGER_ARR_SIG, "x");
        assertExtendedContextElements(context, STRING_ARR_SIG, "y");
        assertExtendedContextElements(context, LIST_ARR_SIG, "z");
    }
    
    public void testExtendedContextInScript3() throws Exception {
        String contents = "class Sub extends Super{ }\nclass Super { }\ndef x = new Super()\ndef y = new Sub()\ndef z\nz";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertExtendedContextElements(context, "LSuper;", "x", "y");
        assertExtendedContextElements(context, "LSub;", "y");
    }
    
    public void testExtendedContextInScript4() throws Exception {
        String contents = "class Sub extends Super{ }\nclass Super { }\ndef x = new Super[0]\ndef y = new Sub[0]\ndef z\nz";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertExtendedContextElements(context, "[LSuper;", "x", "y");
        assertExtendedContextElements(context, "[LSub;", "y");
        assertExtendedContextElements(context, "LSuper;");
        assertExtendedContextElements(context, "LSub;");
    }
    
    public void testExtendedContextInClass1() throws Exception {
        String contents = "class Sub extends Super{ }\nclass Super {\n def foo() { \ndef x = new Super[0]\ndef y = new Sub[0]\ndef z\nz } }";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("foo", enclosing.getElementName());
        assertExtendedContextElements(context, "[LSuper;", "x", "y");
        assertExtendedContextElements(context, "[LSub;", "y");
        assertExtendedContextElements(context, "LSuper;");
        assertExtendedContextElements(context, "LSub;");
    }
    
    public void testExtendedContextInClass2() throws Exception {
        String contents = "class Sub extends Super{ }\nclass Super { \nSuper x\nSub y\ndef z\n def foo() { \nz } }";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("foo", enclosing.getElementName());
        assertExtendedContextElements(context, "LSuper;", "x", "y");
        assertExtendedContextElements(context, "LSub;", "y");
    }
    
    public void testExtendedContextInClass3() throws Exception {
        String contents = "class Super{ \nSuper a\nSub b\ndef c }\nclass Sub extends Super { \nSuper x\nSub y\ndef z\n def foo() { \nSuper z } }";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("foo", enclosing.getElementName());
        assertExtendedContextElements(context, "LSub;", "y", "b");
        assertExtendedContextElements(context, "LSuper;", "x", "y", "a", "b", "z");
    }
    
    // We should be using erasure types, so generics need not match
    public void testExtendedContextWithGenerics() throws Exception {
        String contents = 
            "Map<Integer, Class> x\n" +
            "HashMap<Class, Integer> y\n" +
            "z";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertExtendedContextElements(context, "Ljava.util.Map;", "y", "x");
        assertExtendedContextElements(context, "Ljava.util.HashMap;", "y");
    }
    
    // now look at boxing and unboxing
    public void testExtendedContextWithBoxing() throws Exception {
        String contents = 
            "int x\n" +
            "Integer y\n" +
            "boolean a\n" +
            "Boolean b\n" +
            "z";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertExtendedContextElements(context, "Ljava.lang.Integer;", "y", "x");
        assertExtendedContextElements(context, "I", "y", "x");
        assertExtendedContextElements(context, "Ljava.lang.Boolean;", "a", "b");
        assertExtendedContextElements(context, "Z", "a", "b");
    }
    // now look at arrayed boxing and unboxing
    public void testExtendedContextWithBoxingAndArrays() throws Exception {
        String contents = 
            "int x\n" +
            "Integer y\n" +
            "boolean a\n" +
            "Boolean b\n" +
            "int[] x1\n" +
            "Integer[] y1\n" +
            "boolean[] a1\n" +
            "Boolean[] b1\n" +
            "int[][] x2\n" +
            "Integer[][] y2\n" +
            "boolean[][] a2\n" +
            "Boolean[][] b2\n" +
            "z";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertExtendedContextElements(context, "Ljava.lang.Integer;", "y", "x");
        assertExtendedContextElements(context, "I", "y", "x");
        assertExtendedContextElements(context, "Ljava.lang.Boolean;", "a", "b");
        assertExtendedContextElements(context, "Z", "a", "b");
        assertExtendedContextElements(context, "[Ljava.lang.Integer;", "y1", "x1");
        assertExtendedContextElements(context, "[I", "y1", "x1");
        assertExtendedContextElements(context, "[Ljava.lang.Boolean;", "a1", "b1");
        assertExtendedContextElements(context, "[Z", "a1", "b1");
        assertExtendedContextElements(context, "[[Ljava.lang.Integer;", "y2", "x2");
        assertExtendedContextElements(context, "[[I", "y2", "x2");
        assertExtendedContextElements(context, "[[Ljava.lang.Boolean;", "a2", "b2");
        assertExtendedContextElements(context, "[[Z", "a2", "b2");
    }
}
