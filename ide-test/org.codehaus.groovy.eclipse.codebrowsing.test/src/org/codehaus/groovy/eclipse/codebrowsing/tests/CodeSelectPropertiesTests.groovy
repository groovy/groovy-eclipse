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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.junit.Test

final class CodeSelectPropertiesTests extends BrowsingTestSuite {

    @Test
    void testGetProperty1() {
        String contents = '''\
            |class C {
            |  String string = ""
            |  def meth() {
            |    def str = string
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'string')
    }

    @Test
    void testGetProperty1a() {
        String contents = '''\
            |@groovy.transform.TypeChecked
            |class C {
            |  String string = ""
            |  def meth() {
            |    def str = string
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'string')
    }

    @Test
    void testGetProperty1b() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |class C {
            |  String string = ""
            |  def meth() {
            |    def str = string
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'string')
    }

    @Test
    void testSetProperty1() {
        String contents = '''\
            |class C {
            |  String string
            |  def meth() {
            |    string = ""
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'string')
    }

    @Test
    void testSetProperty1a() {
        String contents = '''\
            |@groovy.transform.TypeChecked
            |class C {
            |  String string
            |  def meth() {
            |    string = ""
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'string')
    }

    @Test
    void testSetProperty1b() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |class C {
            |  String string
            |  def meth() {
            |    string = ""
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'string')
    }

    @Test
    void testGettersAndField1() {
        String contents = '''\
            |class C {
            |  String xxx
            |  def getXxx() { xxx }
            |}
            |new C().xxx
            |'''.stripMargin()
        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testGettersAndField2() {
        String contents = '''\
            |class C {
            |  String xxx
            |  def getXxx() { xxx }
            |}
            |new C().getXxx()
            |'''.stripMargin()
        assertCodeSelect([contents], 'getXxx')
    }

    @Test
    void testGettersAndField3() {
        String contents = '''\
            |class C {
            |  String xxx
            |}
            |new C().getXxx()
            |'''.stripMargin()
        assertCodeSelect([contents], 'getXxx', 'xxx')
    }

    @Test
    void testGettersAndField4() {
        String contents = '''\
            |class C {
            |  def getXxx() { xxx }
            |}
            |new C().xxx
            |'''.stripMargin()
        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testGettersAndField5() {
        String contents = '''\
            |class C {
            |  String xxx
            |  def getXxx() { xxx }
            |}
            |new C().xxx
            |'''.stripMargin()
        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testGettersAndField6() {
        String contents = '''\
            |class C {
            |  String xxx
            |  def getXxx() { xxx }
            |}
            |new C().getXxx
            |'''.stripMargin()
        assertCodeSelect([contents], 'getXxx', null)
    }

    @Test
    void testGettersAndField7() {
        String contents = '''\
            |class C {
            |  public getXxx() { xxx }
            |}
            |new C().xxx
            |'''.stripMargin()
        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testGettersAndField8() {
        String contents = '''\
            |class C {
            |  String xxx
            |}
            |new C().getXxx()
            |'''.stripMargin()
        assertCodeSelect([contents], 'getXxx', 'xxx')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/888
    void testGetter1() {
        addGroovySource('''\
            |class Pogo {
            |  String string
            |}
            |'''.stripMargin())

        def unit = addJavaSource('''\
            |class Main {
            |  void meth() {
            |    new Pogo().getString()
            |  }
            |}
            |'''.stripMargin())
        prepareForCodeSelect(unit)

        String target = 'getString'
        int offset = unit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= unit.source.length()

        //
        def elements = unit.codeSelect(offset, length)

        assert elements.length == 1
        assert elements[0].exists()
        assert elements[0].elementName == 'string'
    }

    @Test
    void testGetter1a() {
        addGroovySource('''\
            |class Pogo {
            |  private String string
            |}
            |'''.stripMargin())

        def unit = addJavaSource('''\
            |class Main {
            |  void meth() {
            |    new Pogo().getString()
            |  }
            |}
            |'''.stripMargin())
        prepareForCodeSelect(unit)

        String target = 'getString'
        int offset = unit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= unit.source.length()

        //
        def elements = unit.codeSelect(offset, length)

        assert elements.length == 0
    }

    @Test
    void testGetter1b() {
        addGroovySource('''\
            |class Pogo {
            |  String string
            |}
            |'''.stripMargin())

        def unit = addJavaSource('''\
            |class Main {
            |  void meth() {
            |    new Pogo().getValue()
            |  }
            |}
            |'''.stripMargin())
        prepareForCodeSelect(unit)

        String target = 'getValue'
        int offset = unit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= unit.source.length()

        //
        def elements = unit.codeSelect(offset, length)

        assert elements.length == 0
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/888
    void testSetter1() {
        addGroovySource('''\
            |class Pogo {
            |  String string
            |}
            |'''.stripMargin())

        def unit = addJavaSource('''\
            |class Main {
            |  void meth() {
            |    new Pogo().setString("")
            |  }
            |}
            |'''.stripMargin())
        prepareForCodeSelect(unit)

        String target = 'setString'
        int offset = unit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= unit.source.length()

        //
        def elements = unit.codeSelect(offset, length)

        assert elements.length == 1
        assert elements[0].exists()
        assert elements[0].elementName == 'string'
    }

    @Test // GRECLIPSE-1162
    void testIsGetter1() {
        String contents = '''\
            |class C {
            |  boolean xxx
            |}
            |new C().isXxx()
            |'''.stripMargin()
        assertCodeSelect([contents], 'isXxx', 'xxx')
    }

    @Test
    void testIsGetter2() {
        addGroovySource('''\
            |class Pogo {
            |  boolean value
            |}
            |'''.stripMargin())

        def unit = addJavaSource('''\
            |class Main {
            |  void meth() {
            |    new Pogo().isValue()
            |  }
            |}
            |'''.stripMargin())
        prepareForCodeSelect(unit)

        String target = 'isValue'
        int offset = unit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= unit.source.length()

        //
        def elements = unit.codeSelect(offset, length)

        assert elements.length == 1
        assert elements[0].exists()
        assert elements[0].elementName == 'value'
    }

    @Test
    void testCodeSelectNonStaticProperty1() {
        String contents = '''\
            |class Super {
            |  def getSql() { null }
            |}
            |
            |class Foo extends Super {
            |  def foo() {
            |    sql
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test
    void testCodeSelectStaticProperty1() {
        String contents = '''\
            |class Super {
            |  static def getSql() { null }
            |}
            |
            |class Foo extends Super {
            |  def static foo() {
            |    sql
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test
    void testCodeSelectStaticProperty2() {
        String contents = '''\
            |class Foo {
            |  static def getSql() { null }
            |  def foo() {
            |    sql
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test
    void testCodeSelectStaticProperty3() {
        String contents = '''\
            |import java.util.logging.*
            |class Foo {
            |  static Logger getLog() { null }
            |  def foo() {
            |    log.info 'message' // should not be confused with field created by @Log transform (see CodeSelectFieldsTests.testCodeSelectLoggerFieldInClass)
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'log', 'getLog')
    }

    // TODO: map properties, unknown properties
}
