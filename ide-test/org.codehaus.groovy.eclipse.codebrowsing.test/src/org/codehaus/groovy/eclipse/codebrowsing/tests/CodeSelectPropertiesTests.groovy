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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.junit.Test

final class CodeSelectPropertiesTests extends BrowsingTestSuite {

    private static final String XX = '''\
        |class XX {
        |  XX[] getXx() { null }
        |  XX   getYy() { null }
        |}
        |'''.stripMargin()
    private static final String YY = '''\
        |class YY {
        |  YY[] xx
        |  YY   yy
        |}
        |'''.stripMargin()

    @Test
    void testCodeSelectArray1() {
        String contents = 'new XX().xx[0].xx'
        String toFind = 'xx'
        String elementName = 'getXx'
        assertCodeSelect([XX, contents], toFind, elementName)
    }

    @Test
    void testCodeSelectArray2() {
        String contents = 'new XX().xx[0].yy'
        String toFind = 'yy'
        String elementName = 'getYy'
        assertCodeSelect([XX, contents], toFind, elementName)
    }

    @Test
    void testCodeSelectArray3() {
        String contents = 'new XX().xx[0].getXx()'
        String toFind = 'getXx'
        assertCodeSelect([XX, contents], toFind)
    }

    @Test
    void testCodeSelectArray4() {
        String contents = 'new XX().xx[0].getYy()'
        String toFind = 'getYy'
        assertCodeSelect([XX, contents], toFind)
    }

    @Test
    void testCodeSelectArray5() {
        String contents = 'new YY().xx[0].setXx()'
        String toFind = 'setXx'
        String elementName = 'xx'
        assertCodeSelect([XX, YY, contents], toFind, elementName)
    }

    @Test
    void testCodeSelectArray6() {
        String contents = 'new YY().xx[0].setYy(null)'
        String toFind = 'setYy'
        String elementName = 'yy'
        assertCodeSelect([XX, YY, contents], toFind, elementName)
    }

    @Test
    void testCodeSelectArray7() {
        String contents = 'new YY().xx[0].getXx()'
        String toFind = 'getXx'
        String elementName = 'xx'
        assertCodeSelect([XX, YY, contents], toFind, elementName)
    }

    @Test
    void testCodeSelectArray8() {
        String contents = 'new YY().xx[0].getYy()'
        String toFind = 'getYy'
        String elementName = 'yy'
        assertCodeSelect([XX, YY, contents], toFind, elementName)
    }

    @Test // GRECLIPSE-1050
    void testCodeSelectArray9() {
        String contents = 'org.codehaus.groovy.ast.ClassHelper.make(List)'
        String toFind = 'make'
        assertCodeSelect([XX, contents], toFind)
    }

    @Test // GRECLIPSE-1050
    void testCodeSelectArray10() {
        String contents = 'org.codehaus.groovy.ast.ClassHelper.make(new Class[0])[0].nameWithoutPackage'
        String toFind = 'nameWithoutPackage'
        String elementName = 'getNameWithoutPackage'
        assertCodeSelect([XX, contents], toFind, elementName)
    }

    @Test
    void testCodeSelectGetProperty1() {
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
    void testCodeSelectGetProperty1a() {
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
    void testCodeSelectGetProperty1b() {
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
    void testCodeSelectSetProperty1() {
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
    void testCodeSelectSetProperty1a() {
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
    void testCodeSelectSetProperty1b() {
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
    void testCodeSelectGettersAndField1() {
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
    void testCodeSelectGettersAndField2() {
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
    void testCodeSelectGettersAndField3() {
        String contents = '''\
            |class C {
            |  String xxx
            |}
            |new C().getXxx()
            |'''.stripMargin()
        assertCodeSelect([contents], 'getXxx', 'xxx')
    }

    @Test
    void testCodeSelectGettersAndField4() {
        String contents = '''\
            |class C {
            |  def getXxx() { xxx }
            |}
            |new C().xxx
            |'''.stripMargin()
        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testCodeSelectGettersAndField5() {
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
    void testCodeSelectGettersAndField6() {
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
    void testCodeSelectGettersAndField7() {
        String contents = '''\
            |class C {
            |  public getXxx() { xxx }
            |}
            |new C().xxx
            |'''.stripMargin()
        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testCodeSelectGettersAndField8() {
        String contents = '''\
            |class C {
            |  String xxx
            |}
            |new C().getXxx()
            |'''.stripMargin()
        assertCodeSelect([contents], 'getXxx', 'xxx')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/888
    void testCodeSelectGetter1() {
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
    void testCodeSelectGetter1a() {
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
    void testCodeSelectGetter1b() {
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
    void testCodeSelectSetter1() {
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
    void testCodeSelectIsGetter1() {
        String contents = '''\
            |class C {
            |  boolean xxx
            |}
            |new C().isXxx()
            |'''.stripMargin()
        assertCodeSelect([contents], 'isXxx', 'xxx')
    }

    @Test
    void testCodeSelectIsGetter2() {
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
