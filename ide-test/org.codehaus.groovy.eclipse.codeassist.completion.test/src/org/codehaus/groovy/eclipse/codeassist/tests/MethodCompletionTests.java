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

package org.codehaus.groovy.eclipse.codeassist.tests;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.groovy.eclipse.test.SynchronizationUtils;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Dec 8, 2009
 *
 * Tests that Method completions are working properly
 */
public class MethodCompletionTests extends CompletionTestCase {

    private class MockGroovyMethodProposal extends GroovyMethodProposal {
        public MockGroovyMethodProposal(MethodNode method) {
            super(method);
        }
        @Override
        protected char[][] createParameterNames(ICompilationUnit unit) {
            return super.createParameterNames(unit);
        }
    }

    public MethodCompletionTests(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        env.setAutoBuilding(true);
    }

    public void testAfterParens1() throws Exception {
        String contents = "HttpRetryException f() {\nnull\n}\nf().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }

    public void testAfterParens2() throws Exception {
        String contents = "HttpRetryException f() {\nnull\n}\nthis.f().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }

    public void testAfterParens3() throws Exception {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\nnew Super().f().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }

    public void testAfterParens4() throws Exception {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\nclass Sub extends Super { }\nnew Sub().f().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }

    public void testAfterParens5() throws Exception {
        String contents = "class Super {HttpRetryException f(arg) {\nnull\n}}\ndef s = new Super()\ns.f(null).";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f(null)."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }

    public void testAfterParens6() throws Exception {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\ndef s = new Super()\ns.f().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }


    public void testParameterNames1() throws Exception {
        String contents = "import org.codehaus.groovy.runtime.DefaultGroovyMethods\nnew DefaultGroovyMethods()";
        ICompilationUnit unit = create(contents);
        ClassNode clazz = extract((GroovyCompilationUnit) unit);
        List<MethodNode> methods = clazz.getMethods("is");
        for (MethodNode method : methods) {
            if (method.getParameters().length == 2) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method);
                char[][] names = proposal.createParameterNames(unit);
                checkNames(new char[][] {"self".toCharArray(), "other".toCharArray() }, names);
            }
        }
        if (methods.size() != 1) {
            fail("expecting to find 1 'is' method, but instead found " + methods.size() + ":\n" + methods);
        }
    }

    public void testParameterNames2() throws Exception {
        String contents = "MyClass\nclass MyClass { def m(int x) { }\ndef m(String x, int y) { }}";
        ICompilationUnit unit = create(contents);
        ClassNode clazz = extract((GroovyCompilationUnit) unit);
        List<MethodNode> methods = clazz.getMethods("m");
        for (MethodNode method : methods) {
            if (method.getParameters().length == 1) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method);
                char[][] names = proposal.createParameterNames(unit);
                checkNames(new char[][] {"x".toCharArray()}, names);
            }
            if (method.getParameters().length == 2) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method);
                char[][] names = proposal.createParameterNames(unit);
                checkNames(new char[][] {"x".toCharArray(), "y".toCharArray()}, names);
            }
        }
        if (methods.size() != 2) {
            fail("expecting to find 2 m methods, but instead found " + methods.size() + ":\n" + methods);
        }
    }

    public void testParameterNames3() throws Exception {
        // failing inermitewntly on build server, so run in a loop
        env.setAutoBuilding(false);
        String contents = "class MyClass { def m(int x) { }\ndef m(String x, int y) { }}";
        create(contents);
        GroovyCompilationUnit unit = (GroovyCompilationUnit) create("new MyClass()", "Other");
        env.fullBuild();
        expectingNoProblems();
        List<MethodNode> methods = null;
        for (int i = 0; i < 5; i++) {
            methods = delegateTestParameterNames(unit);
            if (methods.size() == 2) {
                // expected
                return;
            }
        }
        fail("expecting to find 2 'm' methods, but instead found " + methods.size() + ":\n" + methods);
    }

    public void testParameterNames4() throws Exception {
        // failing inermitewntly on build server, so run in a loop
        env.setAutoBuilding(false);
        ICompilationUnit unit = create("new MyJavaClass()", "Other");
        String contents = "public class MyJavaClass { void m(int x) { }\nvoid m(String x, int y) { }}";
        createJava(contents, "MyJavaClass");
        env.fullBuild();
        List<MethodNode> methods = null;
        for (int i = 0; i < 5; i++) {
            methods = delegateTestParameterNames((GroovyCompilationUnit) unit);
            if (methods.size() == 2) {
                // expected
                return;
            }
        }
        fail("expecting to find 2 'm' methods, but instead found " + methods.size() + ":\n" + methods);

    }

    private List<MethodNode> delegateTestParameterNames(GroovyCompilationUnit unit) throws Exception {
        // for some reason, need to wait for indices to be built before this can work
        SynchronizationUtils.waitForIndexingToComplete();
        ClassNode clazz = extract(unit);
        List<MethodNode> methods = clazz.getMethods("m");
        for (MethodNode method : methods) {
            if (method.getParameters().length == 1) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method);
                char[][] names = proposal.createParameterNames(unit);
                checkNames(new char[][] {"x".toCharArray()}, names);
            }
            if (method.getParameters().length == 2) {
                MockGroovyMethodProposal proposal = new MockGroovyMethodProposal(method);
                char[][] names = proposal.createParameterNames(unit);
                checkNames(new char[][] {"x".toCharArray(), "y".toCharArray()}, names);
            }
        }
        return methods;
    }

    private void checkNames(char[][] expected, char[][] names) {
        if (!CharOperation.equals(expected, names)) {
            fail("Wrong number of parameter names.  Expecting:\n" +
                    CharOperation.toString(expected) + "\n\nbut found:\n" + CharOperation.toString(names));
        }
    }
    private ClassNode extract(GroovyCompilationUnit unit) {
        Statement state = (Statement) unit.getModuleNode().getStatementBlock().getStatements().get(0);
        if (state instanceof ReturnStatement) {
            ReturnStatement ret = (ReturnStatement) state;
            return ((Expression) ret.getExpression()).getType();
        } else if (state instanceof ExpressionStatement) {
            ExpressionStatement expr = (ExpressionStatement) state;
            return ((Expression) expr.getExpression()).getType();
        } else {
            fail ("Invalid statement kind for " + state + "\nExpecting return statement or expression statement");
            return null;
        }
    }
}
