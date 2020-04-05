/*
 * Copyright 2009-2020 the original author or authors.
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

import org.junit.Test

final class ExtendedCompletionContextTests extends CompletionTestSuite {

    @Test
    void testExtendedContextInScript1() {
        String contents = '''\
            |def x = 9
            |def y = ""
            |def z = []
            |int a
            |String b
            |List c
            |z
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'run'
        assertExtendedContextElements(context, 'Ljava.lang.Integer;', 'x', 'a')
        assertExtendedContextElements(context, 'Ljava.lang.String;', 'y', 'b')
        assertExtendedContextElements(context, 'Ljava.util.List;', 'z', 'c')
    }

    @Test
    void testExtendedContextInScript2() {
        String contents = '''\
            |int[] x
            |String[] y
            |List[] z
            |z
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'run'
        assertExtendedContextElements(context, '[I', 'x')
        assertExtendedContextElements(context, '[Ljava.lang.String;', 'y')
        assertExtendedContextElements(context, '[Ljava.util.List;', 'z')
    }

    @Test
    void testExtendedContextInScript3() {
        String contents = '''\
            |class Sub extends Super{ }
            |class Super { }
            |def x = new Super()
            |def y = new Sub()
            |def z
            |z
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'run'
        assertExtendedContextElements(context, 'LSuper;', 'x', 'y')
        assertExtendedContextElements(context, 'LSub;', 'y')
    }

    @Test
    void testExtendedContextInScript4() {
        String contents = '''\
            |class Sub extends Super{ }
            |class Super { }
            |def x = new Super[0]
            |def y = new Sub[0]
            |def z
            |z
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'run'
        assertExtendedContextElements(context, '[LSuper;', 'x', 'y')
        assertExtendedContextElements(context, '[LSub;', 'y')
        assertExtendedContextElements(context, 'LSuper;')
        assertExtendedContextElements(context, 'LSub;')
    }

    @Test
    void testExtendedContextInClass1() {
        String contents = '''\
            |class Sub extends Super{ }
            |class Super {
            |  def foo() {
            |    def x = new Super[0]
            |    def y = new Sub[0]
            |    def z
            |    z
            |  }
            |}
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'foo'
        assertExtendedContextElements(context, '[LSuper;', 'x', 'y')
        assertExtendedContextElements(context, '[LSub;', 'y')
        assertExtendedContextElements(context, 'LSuper;')
        assertExtendedContextElements(context, 'LSub;')
    }

    @Test
    void testExtendedContextInClass2() {
        String contents = '''\
            |class Sub extends Super { }
            |class Super {
            |  Super x
            |  Sub y
            |  def z
            |  def foo() { z }
            |}
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'foo'
        assertExtendedContextElements(context, 'LSuper;', 'x', 'y')
        assertExtendedContextElements(context, 'LSub;', 'y')
    }

    @Test
    void testExtendedContextInClass3() {
        String contents = '''\
            |class Super {
            |  Super a
            |  Sub b
            |  def c
            |}
            |class Sub extends Super {
            |  Super x
            |  Sub y
            |  def z
            |  def foo() { Super z }
            |}
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'foo'
        assertExtendedContextElements(context, 'LSub;', 'b', 'y')
        assertExtendedContextElements(context, 'LSuper;', 'a', 'b', 'x', 'y', 'z')
    }

    @Test // should be using erasure types, so generics need not match
    void testExtendedContextWithGenerics() {
        String contents = '''\
            |Map<Integer, Class> x
            |HashMap<Class, Integer> y
            |z
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'run'
        assertExtendedContextElements(context, 'Ljava.util.Map;', 'x', 'y')
        assertExtendedContextElements(context, 'Ljava.util.HashMap;', 'y')
    }

    @Test // now look at boxing and unboxing
    void testExtendedContextWithBoxing() {
        String contents = '''\
            |int x
            |Integer y
            |boolean a
            |Boolean b
            |z
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'run'
        assertExtendedContextElements(context, 'I', 'x', 'y')
        assertExtendedContextElements(context, 'Ljava.lang.Integer;', 'x', 'y')
        assertExtendedContextElements(context, 'Z', 'a', 'b')
        assertExtendedContextElements(context, 'Ljava.lang.Boolean;', 'a', 'b')
    }

    @Test
    void testExtendedContextWithArrays() {
        String contents = '''\
            |int x
            |Integer y
            |boolean a
            |Boolean b
            |int[] x1
            |Integer[] y1
            |boolean[] a1
            |Boolean[] b1
            |int[][] x2
            |Integer[][] y2
            |boolean[][] a2
            |Boolean[][] b2
            |z
            |'''.stripMargin()
        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), contents.lastIndexOf('z') + 1)
        assert context.enclosingElement.elementName == 'run'
        assertExtendedContextElements(context, 'Ljava.lang.Integer;', 'x', 'y')
        assertExtendedContextElements(context, 'I', 'x', 'y')
        assertExtendedContextElements(context, 'Ljava.lang.Boolean;', 'a', 'b')
        assertExtendedContextElements(context, 'Z', 'a', 'b')

        // arrays do not follow the same autoboxing rules
        assertExtendedContextElements(context, '[I', 'x1')
        assertExtendedContextElements(context, '[Ljava.lang.Integer;', 'y1')
        assertExtendedContextElements(context, '[Z', 'a1')
        assertExtendedContextElements(context, '[Ljava.lang.Boolean;', 'b1')
        assertExtendedContextElements(context, '[[I', 'x2')
        assertExtendedContextElements(context, '[[Ljava.lang.Integer;', 'y2')
        assertExtendedContextElements(context, '[[Z', 'a2')
        assertExtendedContextElements(context, '[[Ljava.lang.Boolean;', 'b2')

        // this also matched binding and metaClass and a bunch of stuff, so skip this check
        //assertExtendedContextElements(context, 'Ljava.lang.Object;', 'a1', 'b1', 'a2', 'b2', 'x1', 'y1', 'x2', 'y2')
        assertExtendedContextElements(context, '[Ljava.lang.Object;', 'a1', 'b1', 'a2', 'b2', 'x1', 'y1', 'x2', 'y2')
        assertExtendedContextElements(context, '[[Ljava.lang.Object;', 'a2', 'b2', 'x2', 'y2')
    }
}
