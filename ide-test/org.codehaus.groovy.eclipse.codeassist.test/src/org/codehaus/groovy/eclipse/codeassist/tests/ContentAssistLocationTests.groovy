/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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

    @Test
    void testStatement1() {
        assertLocation('#', ContentAssistLocation.SCRIPT)
    }

    @Test
    void testStatement2() {
        // This is technically a bug, but I actually want this to be
        // the expected behaviour since having the extra completions
        // available from SCRIPT can be annoying
        assertLocation('a#', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement3() {
        assertLocation('a\n#', ContentAssistLocation.SCRIPT)
    }

    @Test
    void testStatement4() {
        // This is technically a bug, but I actually want this to be
        // the expected behaviour since having the extra completions
        // available from SCRIPT can be annoying
        assertLocation('a\na#', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement5() {
        assertLocation('a.g()#', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement6() {
        assertLocation('def x = #', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement7() {
        assertLocation('def x = #;', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement8() {
        assertLocation('def x = #\n', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement9() {
        assertLocation('def x = {# }', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement10() {
        assertLocation('def x = { a.g()# }', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement11a() {
        assertLocation('def x() {# }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof ReturnStatement
        }
    }

    @Test
    void testStatement11b() {
        assertLocation('def x() {# null }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement11c() {
        assertLocation('def x(int y) {# null }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement12a() {
        assertLocation('class C { def x() {#\n } }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof ReturnStatement
        }
    }

    @Test
    void testStatement12b() {
        assertLocation('class C { def x() {\n#}\n }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof MethodNode
            assert completionNode instanceof ReturnStatement
        }
    }

    @Test
    void testStatement12c() {
        assertLocation('class C { C() {#\n }\n }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof ConstructorNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement12d() {
        assertLocation('class C { C() {\n #}\n }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof ConstructorNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement12e() {
        assertLocation('class C { C(int x) {#\n }\n }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof ConstructorNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement13a() {
        assertLocation('class C { def x = {#\n } }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof FieldNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement13b() {
        assertLocation('class C { def x = {\n #} }', ContentAssistLocation.STATEMENT) {
            assert containingDeclaration instanceof FieldNode
            assert completionNode instanceof BlockStatement
        }
    }

    @Test
    void testStatement14() {
        assertLocation('def x = { a.g(    c#,b) }', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement15() {
        assertLocation('def x = { a.g a, b# }', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement16() {
        assertLocation('a.g a, b#', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement17() {
        assertLocation('a#()', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement18() {
        assertLocation('b a#()', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement19() {
        assertLocation('def x = { new ArrayList(a,b)# }', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement20() {
        assertLocation('new ArrayList(a#,b)', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement21() {
        assertLocation('new ArrayList(a,b#)', ContentAssistLocation.STATEMENT)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/409
    void testStatement22() {
        String contents = '''\
            class Bean {
              private String foo
              String getFoo() {}
            }
            def bean1 = new Bean()
            def bean2 = new Bean(foo#: bea)
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/409
    void testStatement23() {
        String contents = '''\
            class Bean {
              private String foo
              String getFoo() {}
            }
            def bean1 = new Bean()
            def bean2 = new Bean(foo: bea#)
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT)
    }

    @Test @NotYetImplemented
    void testStatement24() {
        String contents = '''\
            def a, b
            def x = true ? #
            if (x) println(x)
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement25() {
        String contents = '''\
            def a, b
            def x = true ? y#
            if (x) println(x)
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof VariableExpression
        }
    }

    @Test
    void testStatement26() {
        String contents = '''\
            0..#
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof RangeExpression
        }
    }

    @Test
    void testStatement27() {
        String contents = '''\
            0..<#
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof RangeExpression
        }
    }

    @Test
    void testStatement28() {
        String contents = '''\
            0.#.
            '''.stripIndent()
        assertLocation(contents, null)
    }

    @Test
    void testStatement29() {
        String contents = '''\
            0.#.<
            '''.stripIndent()
        assertLocation(contents, null)
    }

    @Test
    void testStatement30() {
        String contents = '''\
            0..#<
            '''.stripIndent()
        assertLocation(contents, null)
    }

    @Test
    void testStatement31() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E#:
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement31a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E#:
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement32() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E#
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement32a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E#
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement33() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E#
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement33a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E#
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement34() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E#
              case E.THREE:
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement34a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E#
              case E.THREE:
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement35() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E#
              case E.THREE:
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement35a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E#
              case E.THREE:
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testStatement36() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E#
              default:
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.STATEMENT) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E'
        }
    }

    @Test
    void testExpression1() {
        assertLocation('a.a#', ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression2() {
        assertLocation('a.#', ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression3() {
        assertLocation('a.\n#', ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression4() {
        assertLocation('a.// \n#', ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression5() {
        assertLocation('a.g(b.#)// \n', ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression6() {
        assertLocation('a.g a, a.b#', ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression7() {
        assertLocation('def x = { a.g(    z.c#,\nb) }', ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression8() {
        assertLocation('def x = { a.g(    c,\nz.b#) }', ContentAssistLocation.EXPRESSION)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/359
    void testExpression9() {
        String contents = '''\
            def a, b
            def x = true ? String.val#
            def y, z
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
        }
    }

    @Test
    void testExpression10() {
        String contents = '''\
            def a, b
            def x = false ? a : String.val#
            def y, z
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
        }
    }

    @Test
    void testExpression11() {
        String contents = '''\
            def a, b
            def x = a ?: String.val#
            def y, z
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
        }
    }

    @Test
    void testExpression12() {
        String contents = '''\
            0.#
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof PropertyExpression
        }
    }

    @Test
    void testExpression13() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.T#:
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression13a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.T#:
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression14() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.T#
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression14a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.T#
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression15() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E.T#
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression15a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E.T#
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression16() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.T#
              case E.THREE:
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression16a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.T#
              case E.THREE:
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression17() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E.T#
              case E.THREE:
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression17a() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E.T#
              case E.THREE:
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testExpression18() {
        addGroovySource 'enum E { ONE, TWO, THREE }'
        String contents = '''\
            void meth(E e) {
              switch (e) {
              case E.ONE:
              case E.T#
              default:
                println 'stmt'
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXPRESSION) {
            assert completionNode instanceof ClassExpression
            assert fullCompletionExpression == 'E.T'
        }
    }

    @Test
    void testMethodContext1() {
        assertLocation('a.g(#)', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext2() {
        assertLocation('def x = { a.g(#) }', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext3() {
        assertLocation('def x = { a.g(#a,b) }', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext5() {
        assertLocation('def x = { a.g(    c,#b) }', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext6() {
        assertLocation('def x = { a.g(    c,\n#b) }', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext7() {
        assertLocation('a.g #a, a.b', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext8() {
        assertLocation('a.g a,# a.b', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext9() {
        assertLocation('a.g a, #a.b', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext10() {
        assertLocation('new ArrayList(#)', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext11() {
        assertLocation('new ArrayList(#a)', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext12() {
        assertLocation('new ArrayList(a,#b)', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext13() {
        assertLocation('new ArrayList<String>(#)', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext14() {
        assertLocation('new ArrayList<String>(#a)', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext15() {
        assertLocation('new ArrayList<String>(a,#b)', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext16() {
        assertLocation('foo #\nh', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext17() {
        assertLocation('foo a,# \nh', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext18() {
        assertLocation('foo a, b #\nh', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext19() {
        assertLocation('foo (a, b #)\nh', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext20() {
        assertLocation('foo (a,# )\nh', ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext21() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            compile(#)
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext22() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            compile(#/[a-z0-9]/)
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext23() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            def regexp = /[a-z0-9]/
            compile(#regexp)
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext24() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            def regexp = /[a-z0-9]/
            compile(regexp,# )
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext25() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            def regexp = /[a-z0-9]/
            compile(regexp,# 0)
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testPackage0() {
        assertLocation('package #', ContentAssistLocation.PACKAGE)
    }

    @Test
    void testPackage1() {
        assertLocation('package p#', ContentAssistLocation.PACKAGE)
    }

    @Test
    void testImport0() {
        assertLocation('import #', ContentAssistLocation.IMPORT)
    }

    @Test
    void testImport1() {
        assertLocation('import T#', ContentAssistLocation.IMPORT)
    }

    @Test
    void testImportStatic0() {
        assertLocation('import static #', ContentAssistLocation.IMPORT)
    }

    @Test
    void testImportStatic1() {
        assertLocation('import static T#', ContentAssistLocation.IMPORT)
    }

    @Test
    void testClassBody1() {
        assertLocation('class X {# }', ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody2() {
        assertLocation('class X { t# }', ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody3() {
        assertLocation('class X { void t# }', ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody4() {
        assertLocation('class X { void t \n# }', ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody5() {
        String contents = '''\
            def list = new List() {
              #
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody6() {
        String contents = '''\
            def list = new List() {
              #
              @Override
              boolean isEmpty() { true }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody7() {
        String contents = '''\
            def list = new List() {
              @Override
              boolean isEmpty() { true }
              #
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody8() {
        String contents = '''\
            def list = new ArrayList(42) {
              #
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody9() {
        String contents = '''\
            class A {
              @Lazy def list = new ArrayList(42) {
                #
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.CLASS_BODY) {
            assert enclosingGroovyType.name == 'A$1'
        }
    }

    @Test
    void testExtends1() {
        assertLocation('class X extends T# { void t }', ContentAssistLocation.EXTENDS)
    }

    @Test
    void testExtends2() {
        assertLocation('class X extends Arr#ayList { void t }', ContentAssistLocation.EXTENDS)
    }

    @Test
    void testImplements1() {
        assertLocation('class X extends ArrayList implements T# { void t }', ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testImplements2() {
        assertLocation('class X extends ArrayList implements Li# { void t }', ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testImplements3() {
        assertLocation('class X extends ArrayList implements Foo, Li# { void t }', ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testParameters1() {
        assertLocation('class X { void t(#) {} }', ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters2() {
        assertLocation('class X { void t(v#) {} }', ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters3() {
        assertLocation('class X { void t(v w#) {} }', ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters4() {
        assertLocation('class X { void t(v w =# hh) {} }', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testParameters5() {
        assertLocation('class X { void t(v w = h#h) {} }', ContentAssistLocation.STATEMENT)
    }

    @Test
    void testParameters6() {
        assertLocation('class X { def t = {v# -> hh } }', ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters7() {
        assertLocation('class X { def t = {v# w -> hh } }', ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters8() {
        assertLocation('class X { def t = {v w# -> hh } }', ContentAssistLocation.PARAMETER)
    }

    @Test
    void testExceptions1() {
        assertLocation('class X { void t(v w = hh) throws Ex# {} }', ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions2() {
        assertLocation('class X { void t(v w = hh) throws T# {} }', ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions3() {
        assertLocation('class X { void t(v w = hh) throws Ex, T# {} }', ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions4() {
        assertLocation('class X { void t(v w = hh) throws Ex, Th# {} }', ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions5() {
        String contents = '''\
            class X {
              void m() {
                try {
                  ;
                } catch (Th#) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions6() {
        String contents = '''\
            class X {
              void m() {
                try {
                  ;
                } catch (Th# any) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions7() {
        String contents = '''\
            class X {
              void m() {
                try {
                  ;
                } catch (Th any#) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testExceptions8() {
        String contents = '''\
            class X {
              void m() {
                try {
                  ;
                } catch (Ex# | Th) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions9() {
        String contents = '''\
            class X {
              void m() {
                try {
                  ;
                } catch (Ex | Th#) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions10() {
        String contents = '''\
            class X {
              void m() {
                try {
                  ;
                } catch (Ex# | Th any) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions11() {
        String contents = '''\
            class X {
              void m() {
                try {
                  ;
                } catch (Ex | Th# any) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions12() {
        String contents = '''\
            class X {
              void m() {
                try {
                  ;
                } catch (Ex | Th any#) {
                }
              }
            }
            '''.stripIndent()
        assertLocation(contents, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testAnnotation1() {
        assertLocation('@# class X { void t(v y = hh) {} }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2() {
        assertLocation('@A# class X { void t(v y = hh) {} }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation3() {
        assertLocation('@A @B# class X { void t(v y = hh) {} }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation4() {
        assertLocation('@A @B# @C class X { void t(v y = hh) {} }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation5() {
        assertLocation('@A @# @C class X { void t(v y = hh) {} }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation6() {
        assertLocation('class X { @# void t(v y = hh) {} }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation7() {
        assertLocation('class X { @A# void t(v y = hh) {} }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation8() {
        assertLocation('class X { @# def t }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation9() {
        assertLocation('class X { @A# void t }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation10() {
        assertLocation('@A# import java.util.List\n class X { }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation11() {
        assertLocation('@A# package p\n class X { }', ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotationBody1() {
        assertLocation('@A(#) class X { }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody2() {
        assertLocation('class X { @A(#) def m() { } }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody3() {
        assertLocation('@A(v#) class X { }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody4() {
        assertLocation('class X { @A(v#) def m() { } }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody5() {
        assertLocation('@A(value=#) class X { }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody6() {
        assertLocation('class X { @A(value=#) def m() { } }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody7() {
        assertLocation('@A(value=v#) class X { }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody8() {
        assertLocation('class X { @A(value=v#) def m() { } }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody9() {
        assertLocation('@A(value=[v,w],#) class X { }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody10() {
        assertLocation('class X { @A(value=[v,w],#) def m() { } }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody11() {
        assertLocation('@A(value=v,w#) class X { }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody12() {
        assertLocation('class X { @A(value=v,w#) def m() { } }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody13() {
        assertLocation('@A(one = null, two = #) class X { }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody14() {
        assertLocation('class X { @A(one = null, two = #) def m() { } }', ContentAssistLocation.ANNOTATION_BODY)
    }

    @Test
    void testAnnotationBody15() {
        assertLocation('@A(value = Object.#) class X { }', ContentAssistLocation.EXPRESSION) {
            assert containingCodeBlock instanceof AnnotationNode
        }
    }

    @Test
    void testAnnotationBody16() {
        assertLocation('class X { @A(value = Object.#) def m() { } }', ContentAssistLocation.EXPRESSION) {
            assert containingCodeBlock instanceof AnnotationNode
        }
    }

    @Test
    void testAnnotationBody17() {
        assertLocation('@A(value = java.lang.Object.#) class X { }', ContentAssistLocation.EXPRESSION) {
            assert containingCodeBlock instanceof AnnotationNode
        }
    }

    @Test
    void testAnnotationBody18() {
        assertLocation('class X { @A(value = java.lang.Object.#) def m() { } }', ContentAssistLocation.EXPRESSION) {
            assert containingCodeBlock instanceof AnnotationNode
        }
    }

    //--------------------------------------------------------------------------

    private void assertLocation(String contents, ContentAssistLocation expected,
        @ClosureParams(value=SimpleType, options=['org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext']) @DelegatesTo(value=ContentAssistContext, strategy=Closure.DELEGATE_FIRST) Closure withContext = null) {

        def unit = addGroovySource(contents.replace('#', ''), nextUnitName()), offset = contents.indexOf('#')
        ContentAssistContext context = new GroovyCompletionProposalComputer().createContentAssistContext(unit, offset, new Document(unit.buffer.contents))

        assert context?.location == expected
        if (withContext != null) {
            DefaultGroovyMethods.with(context, withContext)
        }
    }
}
