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

    @Test // arrays do not follow the same autoboxing rules
    void testExtendedContextWithArrays() {
        String contents = '''\
            |int     i
            |Integer I
            |boolean b
            |Boolean B
            |
            |int[]     i1
            |Integer[] I1
            |boolean[] b1
            |Boolean[] B1
            |
            |int[][]     i2
            |Integer[][] I2
            |boolean[][] b2
            |Boolean[][] B2
            |
            |x
            |'''.stripMargin()

        def context = getExtendedCoreContext(addGroovySource(contents, nextUnitName()), getIndexOf(contents, 'x'))

        assertExtendedContextElements(context, 'I', 'i', 'I')
        assertExtendedContextElements(context, 'Ljava.lang.Integer;', 'i', 'I')
        assertExtendedContextElements(context, 'Z', 'b', 'B')
        assertExtendedContextElements(context, 'Ljava.lang.Boolean;', 'b', 'B')

        assertExtendedContextElements(context, '[I', 'i1')
        assertExtendedContextElements(context, '[Ljava.lang.Integer;', 'I1')
        assertExtendedContextElements(context, '[Z', 'b1')
        assertExtendedContextElements(context, '[Ljava.lang.Boolean;', 'B1')
        assertExtendedContextElements(context, '[[I', 'i2')
        assertExtendedContextElements(context, '[[Ljava.lang.Integer;', 'I2')
        assertExtendedContextElements(context, '[[Z', 'b2')
        assertExtendedContextElements(context, '[[Ljava.lang.Boolean;', 'B2')

        // Object[] cannot accept primitive arrays
        assertExtendedContextElements(context, '[Ljava.lang.Object;', 'I1', 'B1', 'i2', 'I2', 'b2', 'B2')
        assertExtendedContextElements(context, '[[Ljava.lang.Object;', 'I2', 'B2')

        // this also matches Binding, MetaClass and a bunch of stuff, so skip it
        //assertExtendedContextElements(context, 'Ljava.lang.Object;', 'a1', 'B1', 'a2', 'B2', 'i1', 'I1', 'i2', 'I2')
    }
}
