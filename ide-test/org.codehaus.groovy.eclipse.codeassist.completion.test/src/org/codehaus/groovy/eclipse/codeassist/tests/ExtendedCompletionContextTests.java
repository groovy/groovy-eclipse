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

import java.util.Arrays;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyExtendedCompletionContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.text.Document;
import org.eclipse.ui.preferences.WorkingCopyManager;

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

    public class SearchRequestor implements ITypeRequestor {

        public VariableScope currentScope;
        public ASTNode node;
        
        public SearchRequestor(ASTNode node) {
            this.node = node;
        }

        public VisitStatus acceptASTNode(ASTNode visitorNode, TypeLookupResult visitorResult,
                IJavaElement enclosingElement) {
            
            if (node == visitorNode) {
                this.currentScope = visitorResult.scope;
                return VisitStatus.STOP_VISIT;
            }
            return VisitStatus.CONTINUE;
        }
    }

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
        assertElements(context, INTEGER_SIG, "x", "a");
        assertElements(context, STRING_SIG, "y", "b");
        assertElements(context, LIST_SIG, "z", "c");
    }
    
    public void testExtendedContextInScript2() throws Exception {
        String contents = "int[] x\nString[] y\nList[] z\nint a\nString b\nList c\nz";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertElements(context, INTEGER_ARR_SIG, "x");
        assertElements(context, STRING_ARR_SIG, "y");
        assertElements(context, LIST_ARR_SIG, "z");
    }
    
    public void testExtendedContextInScript3() throws Exception {
        String contents = "class Sub extends Super{ }\nclass Super { }\ndef x = new Super()\ndef y = new Sub()\ndef z\nz";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertElements(context, "LSuper;", "x", "y");
        assertElements(context, "LSub;", "y");
    }
    
    public void testExtendedContextInScript4() throws Exception {
        String contents = "class Sub extends Super{ }\nclass Super { }\ndef x = new Super[0]\ndef y = new Sub[0]\ndef z\nz";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("run", enclosing.getElementName());
        assertElements(context, "[LSuper;", "x", "y");
        assertElements(context, "[LSub;", "y");
        assertElements(context, "LSuper;");
        assertElements(context, "LSub;");
    }
    
    public void testExtendedContextInClass1() throws Exception {
        String contents = "class Sub extends Super{ }\nclass Super {\n def foo() { \ndef x = new Super[0]\ndef y = new Sub[0]\ndef z\nz } }";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("foo", enclosing.getElementName());
        assertElements(context, "[LSuper;", "x", "y");
        assertElements(context, "[LSub;", "y");
        assertElements(context, "LSuper;");
        assertElements(context, "LSub;");
    }
    
    public void testExtendedContextInClass2() throws Exception {
        String contents = "class Sub extends Super{ }\nclass Super { \nSuper x\nSub y\ndef z\n def foo() { \nz } }";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("foo", enclosing.getElementName());
        assertElements(context, "LSuper;", "x", "y");
        assertElements(context, "LSub;", "y");
    }
    
    public void testExtendedContextInClass3() throws Exception {
        String contents = "class Super{ \nSuper a\nSub b\ndef c }\nclass Sub extends Super { \nSuper x\nSub y\ndef z\n def foo() { \nSuper z } }";
        GroovyExtendedCompletionContext context = getExtendedCoreContext(create(contents), contents.lastIndexOf('z')+1);
        IJavaElement enclosing = context.getEnclosingElement();
        assertEquals("foo", enclosing.getElementName());
        assertElements(context, "LSub;", "y", "b");
        assertElements(context, "LSuper;", "x", "y", "a", "b", "z");
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
        assertElements(context, "Ljava.util.Map;", "y", "x");
        assertElements(context, "Ljava.util.HashMap;", "y");
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
        assertElements(context, "Ljava.lang.Integer;", "y", "x");
        assertElements(context, "I", "y", "x");
        assertElements(context, "Ljava.lang.Boolean;", "a", "b");
        assertElements(context, "Z", "a", "b");
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
        assertElements(context, "Ljava.lang.Integer;", "y", "x");
        assertElements(context, "I", "y", "x");
        assertElements(context, "Ljava.lang.Boolean;", "a", "b");
        assertElements(context, "Z", "a", "b");
        assertElements(context, "[Ljava.lang.Integer;", "y1", "x1");
        assertElements(context, "[I", "y1", "x1");
        assertElements(context, "[Ljava.lang.Boolean;", "a1", "b1");
        assertElements(context, "[Z", "a1", "b1");
        assertElements(context, "[[Ljava.lang.Integer;", "y2", "x2");
        assertElements(context, "[[I", "y2", "x2");
        assertElements(context, "[[Ljava.lang.Boolean;", "a2", "b2");
        assertElements(context, "[[Z", "a2", "b2");
    }
    
    private void assertElements(GroovyExtendedCompletionContext context, String signature, String...expectedNames) {
        IJavaElement[] visibleElements = context.getVisibleElements(signature);
        assertEquals("Incorrect number of visible elements\nexpected: " + Arrays.toString(expectedNames) + 
                "\nfound: " + elementsToNames(visibleElements), expectedNames.length, visibleElements.length);
        
        for (String name : expectedNames) {
            boolean found = false;
            for (IJavaElement element : visibleElements) {
                if (element.getElementName().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (! found) {
                fail ("couldn't find element named " + name + " in " + elementsToNames(visibleElements));
            }
        }
    }

    private String elementsToNames(IJavaElement[] visibleElements) {
        String[] names = new String[visibleElements.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = visibleElements[i].getElementName();
        }
        return Arrays.toString(names);
    }
    
    private GroovyExtendedCompletionContext getExtendedCoreContext(ICompilationUnit unit, int invocationOffset) throws JavaModelException {
        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;
        gunit.becomeWorkingCopy(null);
        
        GroovyCompletionProposalComputer computer = new GroovyCompletionProposalComputer();
        ContentAssistContext context = computer.createContentAssistContext(gunit, invocationOffset, new Document(String.valueOf(gunit.getContents())));
        
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(gunit);
        SearchRequestor requestor = new SearchRequestor(context.completionNode);
        visitor.visitCompilationUnit(requestor);

        return new GroovyExtendedCompletionContext(context, requestor.currentScope);
    }
}
