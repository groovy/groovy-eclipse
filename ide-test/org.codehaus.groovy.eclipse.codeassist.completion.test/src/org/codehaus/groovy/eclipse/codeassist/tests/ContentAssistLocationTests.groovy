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

import groovy.transform.NotYetImplemented
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.eclipse.jface.text.Document
import org.junit.Test

final class ContentAssistLocationTests extends CompletionTestSuite {

    private void assertLocation(String contents, int offset, ContentAssistLocation expectedLocation,
        @ClosureParams(value=SimpleType, options=['org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext']) @DelegatesTo(value=ContentAssistContext, strategy=Closure.DELEGATE_FIRST) Closure withContext = null) {

        def unit = addGroovySource(contents, nextUnitName())
        ContentAssistContext context = new GroovyCompletionProposalComputer().createContentAssistContext(unit, offset, new Document(unit.buffer.contents))

        assert context?.location == expectedLocation
        if (withContext != null) {
            DefaultGroovyMethods.with(context, withContext)
        }
    }

    @Test
    void testStatement1() {
        assertLocation('', 0, ContentAssistLocation.SCRIPT)
    }

    @Test
    void testStatement2() {
        // This is technically a bug, but I actually want this to be
        // the expected behaviour since having the extra completions
        // available from script can be annoying
        assertLocation('a', 1, ContentAssistLocation.STATEMENT/*SCRIPT*/)
    }

    @Test
    void testStatement3() {
        assertLocation('a\n', 2, ContentAssistLocation.SCRIPT)
    }

    @Test
    void testStatement4() {
        // This is technically a bug, but I actually want this to be
        // the expected behaviour since having the extra completions
        // available from script can be annoying
        assertLocation('a\na', 3, ContentAssistLocation.STATEMENT/*SCRIPT*/)
    }

    @Test
    void testStatement5() {
        String contents = 'a.g()'
        assertLocation(contents, contents.length(), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement6() {
        String contents = 'def x = '
        assertLocation(contents, contents.length(), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement7() {
        String contents = 'def x = ;'
        assertLocation(contents, contents.length() - 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement8() {
        String contents = 'def x = \n'
        assertLocation(contents, contents.length() - 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement9() {
        String contents = 'def x = { }'
        assertLocation(contents, contents.indexOf('{') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement10() {
        String contents = 'def x = { a.g() }'
        assertLocation(contents, contents.indexOf(')') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement11a() {
        String contents = 'def x() { }'
        assertLocation(contents, contents.indexOf('{') + 1, ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof ReturnStatement
        }
    }

    @Test
    void testStatement11b() {
        String contents = 'def x() { null }'
        assertLocation(contents, contents.indexOf('{') + 1, ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement11c() {
        String contents = 'def x(int y) { null }'
        assertLocation(contents, contents.indexOf('{') + 1, ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement12a() {
        String contents = 'class C { def x() {\n } }'
        assertLocation(contents, contents.lastIndexOf('{') + 1, ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof ReturnStatement
        }
    }

    @Test
    void testStatement12b() {
        String contents = 'class C { def x() {\n }\n }'
        assertLocation(contents, contents.indexOf('}'), ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof ReturnStatement
        }
    }

    @Test
    void testStatement12c() {
        String contents = 'class C { C() {\n }\n }'
        assertLocation(contents, contents.lastIndexOf('{') + 1, ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof ConstructorNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement12d() {
        String contents = 'class C { C() {\n }\n }'
        assertLocation(contents, contents.indexOf('}'), ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof ConstructorNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement12e() {
        String contents = 'class C { C(int x) {\n }\n }'
        assertLocation(contents, contents.lastIndexOf('{') + 1, ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof ConstructorNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement13a() {
        String contents = 'class C { def x = {\n } }'
        assertLocation(contents, contents.lastIndexOf('{') + 1, ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof PropertyNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement13b() {
        String contents = 'class C { def x = {\n } }'
        assertLocation(contents, contents.indexOf('}'), ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof PropertyNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement14() {
        String contents = 'def x = { a.g(    c,b) }'
        assertLocation(contents, getLastIndexOf(contents, 'c'), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement15() {
        String contents = 'def x = { a.g a, b }'
        assertLocation(contents, getLastIndexOf(contents, 'b'), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement16() {
        String contents = 'a.g a, b'
        assertLocation(contents, getLastIndexOf(contents, 'b'), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement17() {
        String contents = 'a()'
        assertLocation(contents, getLastIndexOf(contents, 'a'), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement18() {
        String contents = 'b a()'
        assertLocation(contents, getLastIndexOf(contents, 'a'), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement19() {
        String contents = 'new ArrayList(a,b)'
        assertLocation(contents, getLastIndexOf(contents, ')'), ContentAssistLocation.STATEMENT)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/409
    void testStatement20() {
        String contents = '''\
            class Bean {
              private String foo
              String getFoo() {}
            }
            def bean1 = new Bean()
            def bean2 = new Bean(foo: bea)
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, 'bea'), ContentAssistLocation.STATEMENT)
    }

    @Test @NotYetImplemented
    void testStatement21() {
        String contents = '''\
            def a, b
            def x = true ? _
            if (x) println(x)
            '''.stripIndent()
        assertLocation(contents.replace('_', ''), contents.indexOf('_'), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement22() {
        String contents = '''\
            def a, b
            def x = true ? y
            if (x) println(x)
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, 'y'), ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof VariableExpression
        }
    }

    @Test
    void testStatement23() {
        String contents = '''\
            0..
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, '..'), ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof RangeExpression
        }
    }

    @Test
    void testStatement23a() {
        String contents = '''\
            0..<
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, '..<'), ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof RangeExpression
        }
    }

    @Test
    void testStatement23b() {
        String contents = '''\
            0..
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, '.'), null)
    }

    @Test
    void testStatement23c() {
        String contents = '''\
            0..<
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, '.'), null)
    }

    @Test
    void testStatement23d() {
        String contents = '''\
            0..<
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, '..'), null)
    }

    @Test
    void testExpression1() {
        String contents = 'a.a'
        assertLocation(contents, contents.length(), ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression2() {
        String contents ='a.'
        assertLocation(contents, contents.length(), ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression3() {
        String contents = 'a.\n'
        assertLocation(contents, contents.length(), ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression4() {
        String contents = 'a.// \n'
        assertLocation(contents, contents.length(), ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression5() {
        String contents = 'a.g(b.)// \n'
        assertLocation(contents, getIndexOf(contents, 'b.'), ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression6() {
        String contents = 'a.g a, a.b'
        assertLocation(contents, getIndexOf(contents, 'b'), ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression7() {
        String contents = 'def x = { a.g(    z.c,\nb) }'
        assertLocation(contents, getIndexOf(contents, 'c'), ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression8() {
        String contents = 'def x = { a.g(    c,\nz.b) }'
        assertLocation(contents, getIndexOf(contents, 'b'), ContentAssistLocation.EXPRESSION)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/359
    void testExpression9() {
        String contents = '''\
            def a, b
            def x = true ? String.val
            def y, z
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, 'val'), ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
        }
    }

    @Test
    void testExpression10() {
        String contents = '''\
            def a, b
            def x = false ? a : String.val
            def y, z
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, 'val'), ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
        }
    }

    @Test
    void testExpression11() {
        String contents = '''\
            def a, b
            def x = a ?: String.val
            def y, z
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, 'val'), ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
        }
    }

    @Test
    void testExpression12() {
        String contents = '''\
            0.
            '''.stripIndent()
        assertLocation(contents, getIndexOf(contents, '.'), ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof PropertyExpression
        }
    }

    @Test
    void testMethodContext1() {
        String contents = 'a.g()'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext2() {
        String contents = 'def x = { a.g() }'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext3() {
        String contents = 'def x = { a.g(a,b) }'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext5() {
        String contents = 'def x = { a.g(    c,b) }'
        assertLocation(contents, contents.indexOf(',') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext6() {
        String contents = 'def x = { a.g(    c,\nb) }'
        assertLocation(contents, contents.indexOf(',') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext7() {
        String contents = 'a.g a, a.b'
        assertLocation(contents, contents.indexOf('g') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext8() {
        String contents = 'a.g a, a.b'
        assertLocation(contents, contents.indexOf(',') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext9() {
        String contents = 'a.g a, a.b'
        assertLocation(contents, contents.indexOf(',') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext10() {
        String contents = 'new ArrayList()'
        assertLocation(contents, getLastIndexOf(contents, '('), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext11() {
        String contents = 'new ArrayList(a)'
        assertLocation(contents, getLastIndexOf(contents, '('), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext12() {
        String contents = 'new ArrayList(a,b)'
        assertLocation(contents, getLastIndexOf(contents, ','), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test // see https://github.com/groovy/groovy-eclipse/issues/331
    void testMethodContext13() {
        String contents = 'new ArrayList(a,b)'
        assertLocation(contents, getLastIndexOf(contents, 'b'), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext14() {
        String contents = 'new ArrayList<String>()'
        assertLocation(contents, getLastIndexOf(contents, '('), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext15() {
        String contents = 'new ArrayList<String>(a)'
        assertLocation(contents, getLastIndexOf(contents, '('), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext16() {
        String contents = 'new ArrayList<String>(a,b)'
        assertLocation(contents, getLastIndexOf(contents, ','), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext17() {
        String contents = 'foo \nh'
        assertLocation(contents, getLastIndexOf(contents, 'foo '), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext18() {
        String contents = 'foo a, \nh'
        assertLocation(contents, getLastIndexOf(contents, ', '), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext19() {
        String contents = 'foo a, b \nh'
        assertLocation(contents, getLastIndexOf(contents, 'b '), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext20() {
        String contents = 'foo (a, b )\nh'
        assertLocation(contents, getLastIndexOf(contents, 'b '), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext21() {
        String contents = 'foo (a, )\nh'
        assertLocation(contents, getLastIndexOf(contents, ','), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext22() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            compile()
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, '('), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext23() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            compile(/[a-z0-9]/)
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, '('), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext24() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            def regexp = /[a-z0-9]/
            compile(regexp)
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, '('), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext25() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            def regexp = /[a-z0-9]/
            compile(regexp, )
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, ','), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext26() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            def regexp = /[a-z0-9]/
            compile(regexp, 0)
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, ','), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/409
    void testMethodContext27() {
        String contents = '''\
            class Bean {
              private String foo
              String getFoo() {}
            }
            def bean1 = new Bean()
            def bean2 = new Bean(foo: bea)
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, 'foo'), ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testPackage0() {
        String contents = 'package '
        assertLocation(contents, contents.length(), ContentAssistLocation.PACKAGE)
    }

    @Test
    void testPackage1() {
        String contents = 'package p'
        assertLocation(contents, contents.length(), ContentAssistLocation.PACKAGE)
    }

    @Test
    void testImport0() {
        String contents = 'import '
        assertLocation(contents, contents.length(), ContentAssistLocation.IMPORT)
    }

    @Test
    void testImport1() {
        String contents = 'import T'
        assertLocation(contents, contents.length(), ContentAssistLocation.IMPORT)
    }

    @Test
    void testImportStatic0() {
        String contents = 'import static '
        assertLocation(contents, contents.length(), ContentAssistLocation.IMPORT)
    }

    @Test
    void testImportStatic1() {
        String contents = 'import static T'
        assertLocation(contents, contents.length(), ContentAssistLocation.IMPORT)
    }

    @Test
    void testClassBody1() {
        String contents = 'class A { }'
        int loc = contents.indexOf('{') + 1
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody2() {
        String contents = 'class A { t }'
        int loc = contents.indexOf('t') + 1
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody3() {
        String contents = 'class A { void t }'
        int loc = contents.indexOf('t') + 1
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody4() {
        String contents = 'class A { void t \n }'
        int loc = contents.indexOf('\n') + 1
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testExtends1() {
        String contents = 'class A extends T { void t }'
        int loc = contents.indexOf('ds') + 4
        assertLocation(contents, loc, ContentAssistLocation.EXTENDS)
    }

    @Test
    void testExtends2() {
        String contents = 'class A extends ArrayList { void t }'
        int loc = contents.indexOf('Arr') + 3
        assertLocation(contents, loc, ContentAssistLocation.EXTENDS)
    }

    @Test
    void testImplements1() {
        String contents = 'class A extends ArrayList implements T { void t }'
        int loc = contents.indexOf('ents ') + 6
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testImplements2() {
        String contents = 'class A extends ArrayList implements Li { void t }'
        int loc = contents.indexOf(' Li') + 3
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testImplements3() {
        String contents = 'class A extends ArrayList implements Foo, Li { void t }'
        int loc = contents.indexOf(' Li') + 3
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testParameters1() {
        String contents = 'class A { void t() {} }'
        int loc = contents.indexOf('(') + 1
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters2() {
        String contents = 'class A { void t(v) {} }'
        int loc = contents.indexOf('(v') + 2
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test // this one should not propose anything
    void testParameters3() {
        String contents = 'class A { void t(v y) {} }'
        int loc = contents.indexOf('(v y') + 4
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters4() {
        String contents = 'class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('=') + 1
        assertLocation(contents, loc, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testParameters5() {
        String contents = 'class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('hh') + 1
        assertLocation(contents, loc, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testParameters6() {
        String contents = 'class A { def t = {v -> hh } }'
        int loc = contents.indexOf('v') + 1
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters7() {
        String contents = 'class A { def t = {v y -> hh } }'
        int loc = contents.indexOf('v') + 1
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters8() {
        String contents = 'class A { def t = {v y -> hh } }'
        int loc = contents.indexOf('y') + 1
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testExceptions1() {
        String contents = 'class A { void t(v y = hh) throws Ex {} }'
        assertLocation(contents, getLastIndexOf(contents, 'Ex'), ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions2() {
        String contents = 'class A { void t(v y = hh) throws T {} }'
        assertLocation(contents, getLastIndexOf(contents, 'T'), ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions3() {
        String contents = 'class A { void t(v y = hh) throws Ex, T {} }'
        assertLocation(contents, getLastIndexOf(contents, 'T'), ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions4() {
        String contents = 'class A { void t(v y = hh) throws Ex, Th {} }'
        assertLocation(contents, getLastIndexOf(contents, 'Th'), ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions5() {
        String contents = '''\
            class A {
              void m() {
                try {
                  ;
                } catch (Th) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, 'Th'), ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions5a() {
        String contents = '''\
            class A {
              void m() {
                try {
                  ;
                } catch (Th any) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, 'Th'), ContentAssistLocation.EXCEPTIONS)
        assertLocation(contents, getLastIndexOf(contents, 'any'), ContentAssistLocation.PARAMETER)
    }

    @Test
    void testExceptions6() {
        String contents = '''\
            class A {
              void m() {
                try {
                  ;
                } catch (Ex | Th) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, 'Ex'), ContentAssistLocation.EXCEPTIONS)
        assertLocation(contents, getLastIndexOf(contents, 'Th'), ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions7() {
        String contents = '''\
            class A {
              void m() {
                try {
                  ;
                } catch (Ex | Th any) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, getLastIndexOf(contents, 'Ex'), ContentAssistLocation.EXCEPTIONS)
        assertLocation(contents, getLastIndexOf(contents, 'Th'), ContentAssistLocation.EXCEPTIONS)
        assertLocation(contents, getLastIndexOf(contents, 'any'), ContentAssistLocation.PARAMETER)
    }

    @Test
    void testAnnotation1() {
        String contents = '@ class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@') + 1
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2() {
        String contents = '@A class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2a() {
        String contents = '@B @A class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2b() {
        String contents = '@B @A @C class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2d() {
        String contents = '@B @ @C class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@ ') + 1
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation3() {
        String contents = ' class A { @ void t(v y = hh) {} }'
        int loc = contents.indexOf('@') + 1
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation4() {
        String contents = ' class A { @A void t(v y = hh) {} }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation5() {
        String contents = ' class A { @ def t }'
        int loc = contents.indexOf('@') + 1
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation6() {
        String contents = ' class A { @A void t }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation7() {
        String contents = '@A import java.util.List\n class A { }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotationBody1() {
        String contents = '@A() class A { }'
        int loc = contents.indexOf('@A') + 3
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody2() {
        String contents = '@A(v) class A { }'
        int loc = contents.indexOf('@A') + 4
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody3() {
        String contents = '@A(value=) class A { }'
        int loc = contents.indexOf('value=') + 6
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody4() {
        String contents = '@A(value=x) class A { }'
        int loc = contents.indexOf('=x') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody5() {
        String contents = '@A(value=[x,y]) class A { }'
        int loc = contents.indexOf(',y') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody6() {
        String contents = '@A(value=[x,y],) class A { }'
        int loc = contents.indexOf('],') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody7() {
        String contents = '@A(one = null, two = ) class A { }'
        int loc = contents.indexOf('= )') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody8() {
        String contents = '@A(value = Object.) class A { }'
        assertLocation(contents, getLastIndexOf(contents, '.'), ContentAssistLocation.EXPRESSION) {
            assert containingCodeBlock instanceof AnnotationNode
        }
    }

    @Test
    void testAnnotationBody9() {
        String contents = '@A(value = java.lang.Object.) class A { }'
        assertLocation(contents, getLastIndexOf(contents, '.'), ContentAssistLocation.EXPRESSION) {
            assert containingCodeBlock instanceof AnnotationNode
        }
    }
}
