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
package org.codehaus.groovy.eclipse.test.debug

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.eclipse.debug.ui.BreakpointLocationFinder
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jface.text.Document
import org.junit.Test

final class BreakpointLocationTests extends GroovyEclipseTestSuite {

    private ASTNode findBreakpointLocation(String target, CharSequence source, String sourcePackage = '') {
        GroovyCompilationUnit unit = addGroovySource(source, nextUnitName(), sourcePackage)
        Document document = new Document(String.valueOf(unit.contents))
        int offset = document.get().indexOf(target)
        int line = document.getLineOfOffset(offset)

        def finder = new BreakpointLocationFinder(unit.moduleNode)
        return finder.findBreakpointLocation(line + 1)
    }

    //--------------------------------------------------------------------------

    @Test
    void testBreakpointInScript1() {
        def node = findBreakpointLocation 'comment', '''\
            // some comment text
            def t = [ x:1, y:2 ]
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInScript2() {
        def node = findBreakpointLocation 'def t', '''\
            // some comment text
            def t = [ x:1, y:2 ]
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInScript3() {
        def node = findBreakpointLocation 't.getX', '''\
            def t = [ x:1, y:2 ]
            t.getX()
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInScript4() {
        def node = findBreakpointLocation 'it.x', '''\
            def shiftTriangle = { it ->
                it.x += 1
                it.y += 1
                it.getX()
            }
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInScript5() {
        def node = findBreakpointLocation 'it.y', '''\
            def shiftTriangle = { it ->
                it.x += 1
                it.y += 1
                it.getX()
            }
            '''.stripIndent()

        assert node?.lineNumber == 3
    }

    @Test
    void testBreakpointInScript6() {
        def node = findBreakpointLocation 'it.getX', '''\
            def shiftTriangle = { it ->
                it.x += 1
                it.y += 1
                it.getX()
            }
            '''.stripIndent()

        assert node?.lineNumber == 4
    }

    @Test
    void testBreakpointInScript7() {
        def node = findBreakpointLocation 'y:', '''\
            def t
            t = [ x: 1,
                  y: 2,
                  z: 4 ]
            '''.stripIndent()

        assert node?.lineNumber == 3
    }

    @Test
    void testBreakpointInScript8() {
        def node = findBreakpointLocation 'z:', '''\
            def t
            t = [ x: 1,
                  y: 2,
                  z: 4 ]
            '''.stripIndent()

        assert node?.lineNumber == 4
    }

    @Test
    void testBreakpointInScript9() {
        def node = findBreakpointLocation '1', '''\
            def t
            t = [ 1,
                  2,
                  3]
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInScript10() {
        def node = findBreakpointLocation '2', '''\
            def t
            t = [ 1,
                  2,
                  3]
            '''.stripIndent()

        assert node?.lineNumber == 3
    }

    @Test
    void testBreakpointInScript11() {
        def node = findBreakpointLocation '3', '''\
            def t
            t = [ 1,
                  2,
                  3]
            '''.stripIndent()

        assert node?.lineNumber == 4
    }

    @Test
    void testBreakpointInScript12() {
        def node = findBreakpointLocation '[]', '''\
            def t
            t = [];
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInScript13() {
        def node = findBreakpointLocation 'def x', '''\
            def x() {
                print "Hi"
            }
            '''.stripIndent()

        assert node?.lineNumber == 1
    }

    @Test
    void testBreakpointInScript14() {
        def node = findBreakpointLocation 'print', '''\
            def x() {
                print "Hi"
            }
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInScript15() {
        def node = findBreakpointLocation 'print', '''\
            def p = { g -> print g }
            '''.stripIndent()

        assert node?.lineNumber == 1
    }

    //

    @Test
    void testBreakpointInClass1() {
        def node = findBreakpointLocation 'Class', '''\
            class Class {
              Class() {
                super()
              }
            }
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInClass2() {
        def node = findBreakpointLocation 'Class()', '''\
            class Class {
              Class() {
                super()
              }
            }
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInClass3() {
        def node = findBreakpointLocation 'super()', '''\
            class Class {
              Class() {
                super()
              }
            }
            '''.stripIndent()

        assert node?.lineNumber == 3
    }

    @Test
    void testBreakpointInClass4() {
        def node = findBreakpointLocation '}', '''\
            class Class {
              Class() {
                super()
              }
            }
            '''.stripIndent()

        assert node == null
    }

    @Test
    void testBreakpointInClass5() {
        def node = findBreakpointLocation 'here()', '''\
            class Class {
              static {
                here()
              }
            }
            '''.stripIndent()

        assert node?.lineNumber == 3
    }

    @Test
    void testBreakpointInClass6() {
        def node = findBreakpointLocation 'm()', '''\
            class Class {
              def m() {
                here()
              }
            }
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInClass7() {
        def node = findBreakpointLocation 'here()', '''\
            class Class {
              def m() {
                here()
              }
            }
            '''.stripIndent()

        assert node?.lineNumber == 3
    }

    @Test
    void testBreakpointInClass8() {
        def node = findBreakpointLocation 'here()', '''\
            class Class {
              def t = { here() }
            }
            '''.stripIndent()

        assert node?.lineNumber == 2
    }

    @Test
    void testBreakpointInClass9() {
        def node = findBreakpointLocation 'property', '''\
            class Class {
              String property
            }
            '''.stripIndent()

        assert node == null
    }

    @Test
    void testBreakpointInClass10() {
        def node = findBreakpointLocation '*', '''\
            /**
             *
             */
            class Class {
              Class() {
                super()
              }
            }
            '''.stripIndent()

        assert node instanceof ConstructorNode
        assert node.lineNumber == 5
    }

    @Test
    void testBreakpointInClass11() {
        def node = findBreakpointLocation '*', '''\
            class Class {
              /**
               *
               */
              Class() {
                super()
              }
            }
            '''.stripIndent()

        assert node instanceof ConstructorNode
        assert node.lineNumber == 5
    }

    @Test
    void testBreakpointInClass12() {
        def node = findBreakpointLocation '*', '''\
            class Class {
              /**
               *
               */
              def m() {
                here()
              }
            }
            '''.stripIndent()

        assert node instanceof MethodNode
        assert node.lineNumber == 5
    }

    @Test
    void testBreakpointInClass13() {
        def node = findBreakpointLocation 'def m', '''\
            class Class {
              @Deprecated
              def m() {
                here()
              }
            }
            '''.stripIndent()

        assert node instanceof MethodNode
    }
}
