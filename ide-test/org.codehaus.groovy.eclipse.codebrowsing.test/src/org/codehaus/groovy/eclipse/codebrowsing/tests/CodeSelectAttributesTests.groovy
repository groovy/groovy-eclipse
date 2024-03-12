/*
 * Copyright 2009-2024 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser
import static org.eclipse.jdt.groovy.core.util.JavaConstants.AST_LEVEL
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.dom.*
import org.junit.Test

final class CodeSelectAttributesTests extends BrowsingTestSuite {

    @Test
    void testCodeSelectOnAttributeName1() {
        String source = '''\
            |@SuppressWarnings(value=['rawtypes','unchecked'])
            |class C {
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'value')
        assert elem.inferredElement instanceof MethodNode
    }

    @Test
    void testCodeSelectOnAttributeName2() {
        addJavaSource '''\
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |@interface A {
            |  String one();
            |  String two();
            |}
            |'''.stripMargin(), 'A'

        String source = '''\
            |@A(one='1', two=C.D)
            |class C {
            |  public static final String D = ""
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'one')
        assert elem.inferredElement instanceof MethodNode

        /**/elem = assertCodeSelect([source], 'two')
        assert elem.inferredElement instanceof MethodNode
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/959
    void testCodeSelectOnAttributeName3() {
        addGroovySource '''\
            |@groovy.transform.EqualsAndHashCode
            |@groovy.transform.AnnotationCollector
            |@interface A {
            |}
            |'''.stripMargin(), 'A'
        buildProject()

        String source = '''\
            |@A(excludes='temporary')
            |class C {
            |  def temporary
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'excludes')
        assert elem.inferredElement instanceof MethodNode
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1155
    void testCodeSelectOnAttributeName4() {
        String source = '''\
            |class C {
            |  void m() {
            |    @groovy.transform.ASTTest(phase=CONVERSION, value={
            |      assert node.text == 'def var = null'
            |    })
            |    def var = null
            |  }
            |}
            |'''.stripMargin()

        assertCodeSelect([source], 'phase').with {
            assert inferredElement.name == 'phase'
            assert inferredElement instanceof MethodNode
            assert inferredElement.declaringClass.name == 'groovy.transform.ASTTest'
        }
        assertCodeSelect([source], 'value').with {
            assert inferredElement.name == 'value'
            assert inferredElement instanceof MethodNode
            assert inferredElement.declaringClass.name == 'groovy.transform.ASTTest'
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1512
    void testCodeSelectOnAttributeName5() {
        assumeTrue(isParrotParser())

        addJavaSource '''\
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE_USE)
            |@interface A {
            |  String one();
            |  String two();
            |}
            |'''.stripMargin(), 'A'

        String source = '''\
            |@A(one=C.VALUE)
            |class C {
            |  public static final @A(two=C.VALUE) String VALUE = ""
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'one')
        assert elem.inferredElement instanceof MethodNode

        /**/elem = assertCodeSelect([source], 'two')
        assert elem.inferredElement instanceof MethodNode
    }

    @Test
    void testCodeSelectOnAttributeValue1() {
        String source = '''\
            |@SuppressWarnings(value=['rawtypes','unchecked'])
            |class C {
            |}
            |'''.stripMargin()

        assertCodeSelect([source], 'rawtypes', '')
    }

    @Test
    void testCodeSelectOnAttributeValue2() {
        addJUnit(4)

        String source = '''\
            |import org.junit.Test
            |class C {
            |  @Test(expected=Exception)
            |  void testSomething() {
            |  }
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'Exception')
        assert elem.inferredElement instanceof ClassNode
    }

    @Test
    void testCodeSelectOnAttributeValue3() {
        addJUnit(4)

        String source = '''\
            |import org.junit.Test
            |class C {
            |  @Test(expected=Exception.class)
            |  void testSomething() {
            |  }
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'Exception')
        assert elem.inferredElement instanceof ClassNode
    }

    @Test
    void testCodeSelectOnAttributeValue4() {
        addJUnit(4)

        String source = '''\
            |import org.junit.Test
            |class C {
            |  @Test(expected=java.lang.Exception)
            |  void testSomething() {
            |  }
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'Exception')
        assert elem.inferredElement instanceof ClassNode
    }

    @Test
    void testCodeSelectOnAttributeValue5() {
        String source = '''\
            |class C {
            |  public static final String VALUE = 'rawtypes'
            |  @SuppressWarnings(value=VALUE)
            |  def method() {
            |  }
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'VALUE')
        assert elem.inferredElement instanceof FieldNode
    }

    @Test
    void testCodeSelectOnAttributeValue6() {
        String source = '''\
            |class C {
            |  public static final String VALUE = 'rawtypes'
            |  @SuppressWarnings(value=C.VALUE)
            |  def method() {
            |  }
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'VALUE')
        assert elem.inferredElement instanceof FieldNode
    }

    @Test
    void testCodeSelectOnAttributeValue7() {
        addGroovySource '''\
            |class Bar {
            |  public static final String VALUE = 'nls'
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String source = '''\
            |import static foo.Bar.VALUE
            |class C {
            |  @SuppressWarnings(VALUE)
            |  def method() {
            |  }
            |}
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'VALUE')
        assert elem.inferredElement instanceof FieldNode
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1155
    void testCodeSelectOnAttributeValue8() {
        String source = '''\
            |class C {
            |  void m() {
            |    @groovy.transform.ASTTest(phase=CONVERSION, value={
            |      assert node.text
            |    })
            |    def var
            |  }
            |}
            |'''.stripMargin()

        assertCodeSelect([source], 'CONVERSION').with {
            assert inferredElement.name == 'CONVERSION'
            assert inferredElement instanceof FieldNode
            assert inferredElement.declaringClass.name == 'org.codehaus.groovy.control.CompilePhase'
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1155
    void testCodeSelectOnAttributeValue9() {
        String source = '''\
            |import org.codehaus.groovy.control.*
            |class C {
            |  void m() {
            |    @groovy.transform.ASTTest(phase=CompilePhase.CONVERSION, value={ -> })
            |    def var
            |  }
            |}
            |'''.stripMargin()

        assertCodeSelect([source], 'CompilePhase').with {
            assert inferredElement instanceof ClassNode
            assert inferredElement.name == 'org.codehaus.groovy.control.CompilePhase'
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1155
    void testCodeSelectOnAttributeValue10() {
        String source = '''\
            |class C {
            |  void m() {
            |    @groovy.transform.ASTTest({
            |      node.text
            |    })
            |    def var
            |  }
            |}
            |'''.stripMargin()

        assertCodeSelect([source], 'node').with {
            assert elementType == IJavaElement.LOCAL_VARIABLE
        }
        assertCodeSelect([source], 'text', 'getText').with {
            assert inferredElement instanceof MethodNode
            assert inferredElement.declaringClass.name == 'org.codehaus.groovy.ast.ASTNode'
        }

        def unit = addGroovySource(source, nextUnitName())
        unit = unit.reconcile(AST_LEVEL, true, null, null)
        def elem = NodeFinder.perform(unit, source.indexOf('node'), 4)
        assert elem instanceof Block
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1497
    void testCodeSelectOnAttributeValue11() {
        String source = '''\
            |class C {
            |  @groovy.transform.ASTTest({
            |    node
            |  })
            |  void m() {
            |  }
            |}
            |'''.stripMargin()

        assertCodeSelect([source], 'node').with {
            assert elementType == IJavaElement.LOCAL_VARIABLE
        }

        def unit = addGroovySource(source, nextUnitName())
        unit = unit.reconcile(AST_LEVEL, true, null, null)
        def elem = NodeFinder.perform(unit, source.indexOf('node'), 4)
        assert !(elem instanceof Annotation) : 'expect closure placeholder'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/15xx
    void testCodeSelectOnAttributeValue12() {
        String source = '''import groovy.transform.*
            |class C {
            |  @NamedVariant m(@NamedParam( type = String) p) {  }
            |}
            |'''.stripMargin()

        assertCodeSelect([source], 'type')
        def elem = assertCodeSelect([source], 'String')
        assert elem.inferredElement instanceof ClassNode
    }
}
