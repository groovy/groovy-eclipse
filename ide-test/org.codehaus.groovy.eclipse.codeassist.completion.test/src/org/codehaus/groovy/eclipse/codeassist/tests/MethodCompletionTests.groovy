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

import static org.junit.Assert.fail

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.compiler.CharOperation
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

final class MethodCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.PARAMETER_GUESSING, false)
    }

    private List<MethodNode> delegateTestParameterNames(GroovyCompilationUnit unit) {
        waitForIndex()
        List<MethodNode> methods = extract(unit).getMethods('m')
        for (MethodNode method : methods) {
            if (method.getParameters().length == 1) {
                GroovyMethodProposal proposal = new GroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(['x'.toCharArray()] as char[][], names)
            }
            if (method.getParameters().length == 2) {
                GroovyMethodProposal proposal = new GroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(['x'.toCharArray(), 'y'.toCharArray()] as char[][], names)
            }
        }
        return methods
    }

    private static ClassNode extract(GroovyCompilationUnit unit) {
        Statement state = unit.getModuleNode().getStatementBlock().getStatements().get(0)
        if (state instanceof ReturnStatement) {
            ReturnStatement ret = (ReturnStatement) state
            return ret.getExpression().getType()
        } else if (state instanceof ExpressionStatement) {
            ExpressionStatement expr = (ExpressionStatement) state
            return expr.getExpression().getType()
        } else {
            fail('Invalid statement kind for ' + state + '\nExpecting return statement or expression statement')
            return null
        }
    }

    private static void checkNames(char[][] expected, char[][] names) {
        if (!CharOperation.equals(expected, names)) {
            fail('Wrong number of parameter names.  Expecting:\n' + CharOperation.toString(expected) + '\n\nbut found:\n' + CharOperation.toString(names))
        }
    }

    //--------------------------------------------------------------------------

    @Test
    void testAfterParens1() {
        String contents = '''\
            HttpRetryException f() { null }
            f().
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens2() {
        String contents = '''\
            HttpRetryException f() { null }
            this.f().
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens3() {
        String contents = '''\
            class Super { HttpRetryException f() { null } }
            new Super().f().
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens4() {
        String contents = '''\
            class Super { HttpRetryException f() { null } }
            class Sub extends Super { }
            new Sub().f().
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens5() {
        String contents = '''\
            class Super { HttpRetryException f(arg) { null } }
            def s = new Super()
            s.f(null).
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f(null).'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens6() {
        String contents = '''\
            class Super { HttpRetryException f() { null } }
            def s = new Super()
            s.f().
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testParameterNames1() {
        String contents = '''\
            import org.codehaus.groovy.runtime.DefaultGroovyMethods
            new DefaultGroovyMethods()
            '''.stripIndent()
        GroovyCompilationUnit gunit = addGroovySource(contents, nextUnitName())
        ClassNode clazz = extract(gunit)
        List<MethodNode> methods = clazz.getMethods('is')
        for (MethodNode method : methods) {
            if (method.getParameters().length == 2) {
                GroovyMethodProposal proposal = new GroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(gunit)
                checkNames(['self'.toCharArray(), 'other'.toCharArray()] as char[][], names)
            }
        }
        if (methods.size() != 1) {
            fail('expecting to find 1 \"is\" method, but instead found ' + methods.size() + ':\n' + methods)
        }
    }

    @Test
    void testParameterNames2() {
        String contents = '''\
            MyClass
            class MyClass {
              def m(int x) { }
              def m(String x, int y) { }
            }
            '''.stripIndent()
        GroovyCompilationUnit gunit = addGroovySource(contents, nextUnitName())
        ClassNode clazz = extract(gunit)
        List<MethodNode> methods = clazz.getMethods('m')
        for (MethodNode method : methods) {
            if (method.getParameters().length == 1) {
                GroovyMethodProposal proposal = new GroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(gunit)
                checkNames(['x'.toCharArray()] as char[][], names)
            }
            if (method.getParameters().length == 2) {
                GroovyMethodProposal proposal = new GroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(gunit)
                checkNames(['x'.toCharArray(), 'y'.toCharArray()] as char[][], names)
            }
        }
        if (methods.size() != 2) {
            fail('expecting to find 2 "m" methods, but instead found ' + methods.size() + ':\n' + methods)
        }
    }

    @Test
    void testParameterNames3() {
        addGroovySource('class MyClass { def m(int x) { }\ndef m(String x, int y) { } }', 'MyClass')
        GroovyCompilationUnit gunit = addGroovySource('new MyClass()', nextUnitName())
        List<MethodNode> methods = null
        for (int i = 0; i < 5; i++) {
            methods = delegateTestParameterNames(gunit)
            if (methods.size() == 2) {
                return // as expected
            }
        }
        fail('expecting to find 2 "m" methods, but instead found ' + methods.size() + ':\n' + methods)
    }

    @Test
    void testParameterNames4() {
        addJavaSource('public class MyJavaClass { void m(int x) { } void m(String x, int y) { } }', 'MyJavaClass')
        GroovyCompilationUnit gunit = addGroovySource('new MyJavaClass()', nextUnitName())
        List<MethodNode> methods = null
        for (int i = 0; i < 5; i++) {
            methods = delegateTestParameterNames(gunit)
            if (methods.size() == 2) {
                return // as expected
            }
        }
        fail('expecting to find 2 "m" methods, but instead found ' + methods.size() + ':\n' + methods)
    }

    @Test // GRECLIPSE-1374
    void testParensExprs1() {
        String contents = '''\
            (1).
            def u
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '(1).'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test // GRECLIPSE-1374
    void testParensExprs2() {
        String contents = '''\
            (((1))).
            def u
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '(((1))).'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test // GRECLIPSE-1374
    void testParensExprs3() {
        String contents = '(((1))).abs()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '(((1))).a'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test // GRECLIPSE-1528
    void testGetterSetter1() {
        String contents = 'class A { private int value\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '\n'))
        proposalExists(proposals, 'getValue', 1)
        proposalExists(proposals, 'setValue', 1)
    }

    @Test
    void testGetterSetter2() {
        String contents = 'class A { private final int value\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '\n'))
        proposalExists(proposals, 'getValue', 1)
        proposalExists(proposals, 'setValue', 0)
    }

    @Test
    void testGetterSetter3() {
        String contents = 'class A { private boolean value\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '\n'))
        proposalExists(proposals, 'isValue', 1)
        proposalExists(proposals, 'setValue', 1)
    }

    @Test // GRECLIPSE-1752
    void testStatic1() {
        String contents = '''\
            class A {
              static void util() {}
              void foo() {
                A.
              }
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'A.'))
        proposalExists(proposals, 'util', 1)
    }

    @Test
    void testStatic2() {
        String contents = '''\
            @groovy.transform.CompileStatic
            class A {
                static void util() {}
                void foo() {
                    A.
                }
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'A.'))
        proposalExists(proposals, 'util', 1)
    }

    @Test
    void testClass1() {
        String contents = '''\
            class A {
              static void util() {}
              void foo() {
                A.class.
              }
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'A.class.'))
        proposalExists(proposals, 'util', 1)
    }

    @Test
    void testClass2() {
        String contents = '''\
            @groovy.transform.CompileStatic
            class A {
              static void util() {}
              void foo() {
                A.class.
              }
            }'''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'util', 1)
    }

    @Test
    void testClass3() {
        String contents = '''\
            import java.util.regex.Pattern
            Pattern.com
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.com'))
        proposalExists(proposals, 'componentType', 1) // from Class
        proposalExists(proposals, 'compile', 2) // from Pattern
    }

    @Test
    void testClass4() {
        String contents = '''\
            import java.util.regex.Pattern
            Pattern.class.com
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.com'))
        proposalExists(proposals, 'componentType', 1) // from Class
        proposalExists(proposals, 'compile', 2) // from Pattern
    }

    @Test
    void testClass5() {
        String contents = '''\
            import java.util.regex.Pattern
            def pat = Pattern.class
            pat.com
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.com'))
        proposalExists(proposals, 'componentType', 1) // from Class
        proposalExists(proposals, 'compile', 2) // from Pattern
    }

    @Test
    void testStaticMethods() {
        String contents = '''\
            import java.util.regex.Pattern
            Pattern.
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'compile', 2) // 2 static, 1 non-static
        proposalExists(proposals, 'flags', 0) // 1 non-static
    }

    @Test
    void testImportStaticMethod() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            comp
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)
    }

    @Test
    void testImportStaticStarMethod() {
        String contents = '''\
            import static java.util.regex.Pattern.*
            comp
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)
    }

    @Test
    void testFavoriteStaticMethod() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.compile')

        String contents = '''\
            comp
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)

        applyProposalAndCheck(new Document(contents), findFirstProposal(proposals, 'compile(String regex)'), '''\
            |import static java.util.regex.Pattern.compile
            |
            |compile(regex)
            |'''.stripMargin())
    }

    @Test
    void testFavoriteStaticStarMethod() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.*')

        String contents = '''\
            comp
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)

        applyProposalAndCheck(new Document(contents), findFirstProposal(proposals, 'compile(String regex)'), '''\
            |import static java.util.regex.Pattern.compile
            |
            |compile(regex)
            |'''.stripMargin())
    }

    @Test // these should not produce redundant proposals
    void testFavoriteStaticStarAndImportStaticStarMethod() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.*')

        String contents = '''\
            import static java.util.regex.Pattern.*
            comp
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)
    }

    @Test
    void testMethodPointer1() {
        String contents = 'String.&isE'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'isE'))
        proposalExists(proposals, 'isEmpty', 1)

        applyProposalAndCheck(new Document(contents), findFirstProposal(proposals, 'isEmpty'), 'String.&isEmpty')
    }

    @Test
    void testMethodPointer2() {
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.PARAMETER_GUESSING, true)

        String contents = 'String.&isE'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'isE'))
        proposalExists(proposals, 'isEmpty', 1)

        applyProposalAndCheck(new Document(contents), findFirstProposal(proposals, 'isEmpty'), 'String.&isEmpty')
    }

    @Test
    void testMethodPointer3() {
        String contents = 'String.&  isE'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'isE'))
        proposalExists(proposals, 'isEmpty', 1)

        applyProposalAndCheck(new Document(contents), findFirstProposal(proposals, 'isEmpty'), 'String.&  isEmpty')
    }

    @Test
    void testIncompleteMethodCall() {
        String contents = '''\
            class Foo {
              void bar(Object param) {
                baz(param.getC
              }
              void baz(Object param) {
              }
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'getC'))
        proposalExists(proposals, 'getClass', 1)
    }

    @Test
    void testSyntheticBridgeMethod() {
        String contents = '''\
            class Foo implements Comparable<Foo> {
              int compareTo(Foo that) { return 0 }
              void bar() {
                this.com
              }
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'com'))
        proposalExists(proposals, 'compareTo', 1)
    }
}
