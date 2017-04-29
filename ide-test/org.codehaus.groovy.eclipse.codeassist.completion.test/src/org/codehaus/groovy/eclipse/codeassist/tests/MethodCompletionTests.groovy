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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.compiler.CharOperation
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests that Method completions are working properly.
 */
final class MethodCompletionTests extends CompletionTestSuite {

    private static class MockGroovyMethodProposal extends GroovyMethodProposal {
        public MockGroovyMethodProposal(MethodNode method) {
            super(method)
        }
        @Override
        protected char[][] createAllParameterNames(ICompilationUnit unit) {
            super.createAllParameterNames(unit)
        }
    }

    @Test
    void testAfterParens1() {
        String contents = "HttpRetryException f() {\nnull\n}\nf()."
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "cause", 1)
    }

    @Test
    void testAfterParens2() {
        String contents = "HttpRetryException f() {\nnull\n}\nthis.f()."
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "cause", 1)
    }

    @Test
    void testAfterParens3() {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\nnew Super().f()."
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "cause", 1)
    }

    @Test
    void testAfterParens4() {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\nclass Sub extends Super { }\nnew Sub().f()."
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "cause", 1)
    }

    @Test
    void testAfterParens5() {
        String contents = "class Super {HttpRetryException f(arg) {\nnull\n}}\ndef s = new Super()\ns.f(null)."
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f(null)."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "cause", 1)
    }

    @Test
    void testAfterParens6() {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\ndef s = new Super()\ns.f()."
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "cause", 1)
    }

    @Test
    void testParameterNames1() {
        String contents = "import org.codehaus.groovy.runtime.DefaultGroovyMethods\nnew DefaultGroovyMethods()"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ClassNode clazz = extract((GroovyCompilationUnit) unit)
        List<MethodNode> methods = clazz.getMethods("is")
        for (MethodNode method : methods) {
            if (method.getParameters().length == 2) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(["self".toCharArray(), "other".toCharArray()] as char[][], names)
            }
        }
        if (methods.size() != 1) {
            fail("expecting to find 1 'is' method, but instead found " + methods.size() + ":\n" + methods)
        }
    }

    @Test
    void testParameterNames2() {
        String contents = "MyClass\nclass MyClass { def m(int x) { }\ndef m(String x, int y) { }}"
        GroovyCompilationUnit unit = addGroovySource(contents, "File", "")
        ClassNode clazz = extract(unit)
        List<MethodNode> methods = clazz.getMethods("m")
        for (MethodNode method : methods) {
            if (method.getParameters().length == 1) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(["x".toCharArray()] as char[][], names)
            }
            if (method.getParameters().length == 2) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(["x".toCharArray(), "y".toCharArray()] as char[][], names)
            }
        }
        if (methods.size() != 2) {
            fail("expecting to find 2 m methods, but instead found " + methods.size() + ":\n" + methods)
        }
    }

    @Test
    void testParameterNames3() {
        String contents = "class MyClass { def m(int x) { }\ndef m(String x, int y) { }}"
        addGroovySource(contents, "File", "")
        GroovyCompilationUnit unit = addGroovySource("new MyClass()", "Other", "")
        List<MethodNode> methods = null
        for (int i = 0; i < 5; i++) {
            methods = delegateTestParameterNames(unit)
            if (methods.size() == 2) {
                // expected
                return
            }
        }
        fail("expecting to find 2 'm' methods, but instead found " + methods.size() + ":\n" + methods)
    }

    @Test
    void testParameterNames4() {
        addJavaSource("public class MyJavaClass { void m(int x) { }\nvoid m(String x, int y) { }}", "MyJavaClass", "")
        GroovyCompilationUnit unit = addGroovySource("new MyJavaClass()", "Other", "")
        List<MethodNode> methods = null
        for (int i = 0; i < 5; i++) {
            methods = delegateTestParameterNames(unit)
            if (methods.size() == 2) {
                // expected
                return
            }
        }
        fail("expecting to find 2 'm' methods, but instead found " + methods.size() + ":\n" + methods)
    }

    @Test // GRECLIPSE-1374
    void testParensExprs1() {
        String contents = "(1).\ndef u"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "(1)."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "abs", 1)
    }

    @Test // GRECLIPSE-1374
    void testParensExprs2() {
        String contents = "(((1))).\ndef u"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "(((1)))."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "abs", 1)
    }

    @Test // GRECLIPSE-1374
    void testParensExprs3() {
        String contents = "(((1))).abs()"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "(((1))).a"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "abs", 1)
    }

    @Test // GRECLIPSE-1528
    void testGetterSetter1() {
        String contents = "class A {\n private int value\n }"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "\n"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "getValue", 1)
        proposalExists(proposals, "setValue", 1)
    }

    @Test
    void testGetterSetter2() {
        String contents = "class A {\n private final int value\n }"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "\n"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "getValue", 1)
        proposalExists(proposals, "setValue", 0)
    }

    @Test
    void testGetterSetter3() {
        String contents = "class A {\n private boolean value\n }"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "\n"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "isValue", 1)
        proposalExists(proposals, "setValue", 1)
    }

    @Test // GRECLIPSE-1752
    void testStatic1() {
        String contents =
                "class A {\n" +
                "    public static void util() {}\n" +
                "    void foo() {\n" +
                "        A.\n" +
                "    }\n" +
                "}"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "A."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "util", 1)
    }

    @Test
    void testStatic2() {
        String contents =
                "@groovy.transform.CompileStatic\n" +
                "class A {\n" +
                "    public static void util() {}\n" +
                "    void foo() {\n" +
                "        A.\n" +
                "    }\n" +
                "}"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "A."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "util", 1)
    }

    @Test
    void testClass1() {
        String contents =
                "class A {\n" +
                "    public static void util() {}\n" +
                "    void foo() {\n" +
                "        A.class.\n" +
                "    }\n" +
                "}"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "A.class."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "util", 1)
    }

    @Test
    void testClass2() {
        String contents =
                "@groovy.transform.CompileStatic\n" +
                "class A {\n" +
                "    public static void util() {}\n" +
                "    void foo() {\n" +
                "        A.class.\n" +
                "    }\n" +
                "}"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "A.class."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "util", 1)
    }

    //

    private List<MethodNode> delegateTestParameterNames(GroovyCompilationUnit unit) {
        // for some reason, need to wait for indices to be built before this can work
        SynchronizationUtils.waitForIndexingToComplete(unit)
        ClassNode clazz = extract(unit)
        List<MethodNode> methods = clazz.getMethods("m")
        for (MethodNode method : methods) {
            if (method.getParameters().length == 1) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(["x".toCharArray()] as char[][], names)
            }
            if (method.getParameters().length == 2) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(["x".toCharArray(), "y".toCharArray()] as char[][], names)
            }
        }
        return methods
    }

    private void checkNames(char[][] expected, char[][] names) {
        if (!CharOperation.equals(expected, names)) {
            fail("Wrong number of parameter names.  Expecting:\n" + CharOperation.toString(expected) + "\n\nbut found:\n" + CharOperation.toString(names))
        }
    }

    private ClassNode extract(GroovyCompilationUnit unit) {
        Statement state = unit.getModuleNode().getStatementBlock().getStatements().get(0)
        if (state instanceof ReturnStatement) {
            ReturnStatement ret = (ReturnStatement) state
            return ret.getExpression().getType()
        } else if (state instanceof ExpressionStatement) {
            ExpressionStatement expr = (ExpressionStatement) state
            return expr.getExpression().getType()
        } else {
            fail("Invalid statement kind for " + state + "\nExpecting return statement or expression statement")
            return null
        }
    }
}
