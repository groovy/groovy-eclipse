/*
 * Copyright 2009-2018 the original author or authors.
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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser
import static org.junit.Assert.fail
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.compiler.CharOperation
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

final class MethodCompletionTests extends CompletionTestSuite {

    private List<MethodNode> delegateTestParameterNames(GroovyCompilationUnit unit) {
        waitForIndex()
        List<MethodNode> methods = extract(unit).getMethods('m')
        for (method in methods) {
            if (method.parameters.length == 1) {
                GroovyMethodProposal proposal = new GroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(['x'.toCharArray()] as char[][], names)
            }
            if (method.parameters.length == 2) {
                GroovyMethodProposal proposal = new GroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(unit)
                checkNames(['x'.toCharArray(), 'y'.toCharArray()] as char[][], names)
            }
        }
        return methods
    }

    private static ClassNode extract(GroovyCompilationUnit unit) {
        Statement state = unit.moduleNode.statementBlock.statements.get(0)
        if (state instanceof ReturnStatement) {
            ReturnStatement ret = (ReturnStatement) state
            return ret.expression.type
        } else if (state instanceof ExpressionStatement) {
            ExpressionStatement expr = (ExpressionStatement) state
            return expr.expression.type
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

    @Before
    void setUp() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'true')
    }

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
        for (method in methods) {
            if (method.parameters.length == 2) {
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
        for (method in methods) {
            if (method.parameters.length == 1) {
                GroovyMethodProposal proposal = new GroovyMethodProposal(method)
                char[][] names = proposal.createAllParameterNames(gunit)
                checkNames(['x'.toCharArray()] as char[][], names)
            }
            if (method.parameters.length == 2) {
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
    void testFavoriteStaticStarMethod() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.*')

        String contents = '''\
            comp
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)

        applyProposalAndCheck(findFirstProposal(proposals, 'compile(String regex)'), '''\
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
    void testFavoriteStaticMethod() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.compile')

        String contents = '''\
            comp
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)

        applyProposalAndCheck(findFirstProposal(proposals, 'compile(String regex)'), '''\
            |import static java.util.regex.Pattern.compile
            |
            |compile(regex)
            |'''.stripMargin())
    }

    @Test
    void testFavoriteStaticMethod2() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.compile')
        setJavaPreference(AssistOptions.OPTION_SuggestStaticImports, AssistOptions.DISABLED)
        try {
            String contents = '''\
                comp
                '''.stripIndent()
            ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
            proposalExists(proposals, 'compile', 2)

            applyProposalAndCheck(findFirstProposal(proposals, 'compile(String regex)'), '''\
                |import java.util.regex.Pattern
                |
                |Pattern.compile(regex)
                |'''.stripMargin())
        } finally {
            setJavaPreference(AssistOptions.OPTION_SuggestStaticImports, AssistOptions.ENABLED)
        }
    }

    @Test
    void testFavoriteStaticMethod3() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.compile')
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')

        String contents = '''\
            comp
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)

        applyProposalAndCheck(findFirstProposal(proposals, 'compile(String regex)'), '''\
            java.util.regex.Pattern.compile(regex)
            '''.stripIndent())
    }

    @Test
    void testMethodPointer0() {
        String contents = 'class Foo { public static Foo instance }\nFoo.&in'
        proposalExists(createProposalsAtOffset(contents, getLastIndexOf(contents, 'in')), 'instance', 0)
    }

    @Test
    void testMethodPointer0a() {
        String contents = 'class Foo { public static Foo instance }\nFoo::in'
        proposalExists(createProposalsAtOffset(contents, getLastIndexOf(contents, 'in')), 'instance', 0)
    }

    @Test
    void testMethodPointer1() {
        String contents = 'String.&isE'
        applyProposalAndCheck(checkUniqueProposal(contents, 'isE', 'isEmpty'), contents + 'mpty')
    }

    @Test
    void testMethodPointer1a() {
        assumeTrue(isParrotParser())
        String contents = 'String::isE'
        applyProposalAndCheck(checkUniqueProposal(contents, 'isE', 'isEmpty'), contents + 'mpty')
    }

    @Test
    void testMethodPointer2() {
        String contents = 'String.&  isE'
        applyProposalAndCheck(checkUniqueProposal(contents, 'isE', 'isEmpty'), contents + 'mpty')
    }

    @Test
    void testMethodPointer2a() {
        assumeTrue(isParrotParser())
        String contents = 'String::  isE'
        applyProposalAndCheck(checkUniqueProposal(contents, 'isE', 'isEmpty'), contents + 'mpty')
    }

    @Test
    void testMethodPointer3() {
        String contents = 'String.&isEmpty.mem'
        applyProposalAndCheck(checkUniqueProposal(contents, 'mem', 'memoize()'), contents + 'oize()')
    }

    @Test
    void testMethodPointer3a() {
        assumeTrue(isParrotParser())
        String contents = 'String::isEmpty.mem'
        applyProposalAndCheck(checkUniqueProposal(contents, 'mem', 'memoize()'), contents + 'oize()')
    }

    @Test
    void testMethodPointer4() {
        String contents = '(String.&isEmpty).mem'
        applyProposalAndCheck(checkUniqueProposal(contents, 'mem', 'memoize()'), contents + 'oize()')
    }

    @Test
    void testMethodPointer4a() {
        assumeTrue(isParrotParser())
        String contents = '(String::isEmpty).mem'
        applyProposalAndCheck(checkUniqueProposal(contents, 'mem', 'memoize()'), contents + 'oize()')
    }

    @Test
    void testAnnotatedMethod1() {
        String contents = '''\
            class Foo {
              @SuppressWarnings(value=[])
              def bar(def baz) {
                baz.
              }
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'baz.'))
        proposalExists(proposals, 'equals', 1)
    }

    @Test
    void testAnnotatedMethod2() {
        String contents = '''\
            class Foo {
              @SuppressWarnings(value=[])
              def bar() {
                def baz = whatever()
                baz.
              }
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'baz.'))
        proposalExists(proposals, 'equals', 1)
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

    @Test
    void testTrailingClosure1() {
        String contents = 'def foo(Closure block) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Closure block)')

        // replacement should be "foo()" with initial cursor inside parens and exit position after parens
        applyProposalAndCheckCursor(proposal, contents + '()', contents.length() + 1, 0, contents.length() + 2)
    }

    @Test
    void testTrailingClosure1a() {
        String contents = 'def foo(Collection items, Closure block) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Collection items, Closure block)')

        // replacement should be "foo()" with initial cursor inside parens and exit position after parens
        applyProposalAndCheckCursor(proposal, contents + '()', contents.length() + 1, 0, contents.length() + 2)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/633
    void testTrailingClosure2() {
        String contents = 'def foo(Closure block) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Closure block)')

        String expected = contents + ' {  }'
        applyProposalAndCheck(proposal, expected, '{' as char)

        def selection = proposal.getSelection(proposal.@fInvocationContext.document)
        assert selection.x == getLastIndexOf(expected, '{ ') && selection.y == 0
        assert proposal.replacementOffset + proposal.cursorPosition == getLastIndexOf(expected, '{ ')
    }

    @Test
    void testTrailingClosure2a() {
        String contents = 'def foo(Collection items, Closure block) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Collection items, Closure block)')

        def expected = contents + '() {  }'
        applyProposalAndCheck(proposal, expected, '{' as char)

        def selection = proposal.getSelection(proposal.@fInvocationContext.document)
        assert selection.x == getLastIndexOf(expected, '(') && selection.y == 0
        assert proposal.replacementOffset + proposal.cursorPosition == getLastIndexOf(expected, '{ ')
    }

    @Test
    void testTrailingFunctionalInterface() {
        String contents = 'def foo(Comparator c) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Comparator c)')

        def expected = contents + ' { o1, o2 -> }'
        applyProposalAndCheck(proposal, expected, '{' as char)

        def selection = proposal.getSelection(proposal.@fInvocationContext.document)
        assert selection.x == expected.lastIndexOf('o1') && selection.y == 'o1'.length()
        assert proposal.replacementOffset + proposal.cursorPosition == getLastIndexOf(expected, '->')
    }
}
