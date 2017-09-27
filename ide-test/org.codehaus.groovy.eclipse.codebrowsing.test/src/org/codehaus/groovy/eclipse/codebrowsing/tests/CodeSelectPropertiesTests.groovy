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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.junit.Assume.assumeTrue

import org.junit.Test

final class CodeSelectPropertiesTests extends BrowsingTestSuite {

    @Test
    void testGetProperty1() {
        String contents = '''\
            class C {
              String string = ""
              def meth() {
                def str = string
              }
            }
            '''.stripIndent()

        assertCodeSelect([contents], 'string')
    }

    @Test
    void testGetProperty1a() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            @groovy.transform.TypeChecked
            class C {
              String string = ""
              def meth() {
                def str = string
              }
            }
            '''.stripIndent()

        assertCodeSelect([contents], 'string')
    }

    @Test
    void testGetProperty1b() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            @groovy.transform.CompileStatic
            class C {
              String string = ""
              def meth() {
                def str = string
              }
            }
            '''.stripIndent()

        assertCodeSelect([contents], 'string')
    }

    @Test
    void testSetProperty1() {
        String contents = '''\
            class C {
              String string
              def meth() {
                string = ""
              }
            }
            '''.stripIndent()

        assertCodeSelect([contents], 'string')
    }

    @Test
    void testSetProperty1a() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            @groovy.transform.TypeChecked
            class C {
              String string
              def meth() {
                string = ""
              }
            }
            '''.stripIndent()

        assertCodeSelect([contents], 'string')
    }

    @Test
    void testSetProperty1b() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            @groovy.transform.CompileStatic
            class C {
              String string
              def meth() {
                string = ""
              }
            }
            '''.stripIndent()

        assertCodeSelect([contents], 'string')
    }

    @Test
    void testGettersAndField1() {
        String contents = '''\
            class C {
              String xxx
              def getXxx() { xxx }
            }
            new C().xxx
            '''.stripIndent()

        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testGettersAndField2() {
        String contents = '''\
            class C {
              String xxx
              def getXxx() { xxx }
            }
            new C().getXxx()
            '''.stripIndent()

        assertCodeSelect([contents], 'getXxx')
    }

    @Test
    void testGettersAndField3() {
        String contents = '''\
            class C {
              String xxx
            }
            new C().getXxx()
            '''.stripIndent()

        assertCodeSelect([contents], 'getXxx', 'xxx')
    }

    @Test
    void testGettersAndField4() {
        String contents = '''\
            class C {
              def getXxx() { xxx }
            }
            new C().xxx
            '''.stripIndent()

        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testGettersAndField5() {
        String contents = '''\
            class C {
              String xxx
              def getXxx() { xxx }
            }
            new C().xxx
            '''.stripIndent()

        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testGettersAndField6() {
        String contents = '''\
            class C {
              String xxx
              def getXxx() { xxx }
            }
            new C().getXxx
            '''.stripIndent()

        assertCodeSelect([contents], 'getXxx')
    }

    @Test
    void testGettersAndField7() {
        String contents = '''\
            class C {
              public getXxx() { xxx }
            }
            new C().xxx
            '''.stripIndent()

        assertCodeSelect([contents], 'xxx', 'getXxx')
    }

    @Test
    void testGettersAndField8() {
        String contents = '''\
            class C {
              String xxx
            }
            new C().getXxx
            '''.stripIndent()

        assertCodeSelect([contents], 'getXxx', 'xxx')
    }

    @Test // GRECLIPSE-1162
    void testIsGetter1() {
        String contents = '''\
            class C {
              boolean xxx
            }
            new C().isXxx
            '''.stripIndent()

        assertCodeSelect([contents], 'isXxx', 'xxx')
    }

    @Test
    void testCodeSelectNonStaticProperty1() {
        String contents = '''\
            class Super {
              def getSql() { null }
            }

            class Foo extends Super {
              def foo() {
                sql
              }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test
    void testCodeSelectStaticProperty1() {
        String contents = '''\
            class Super {
              static def getSql() { null }
            }

            class Foo extends Super {
              def static foo() {
                sql
              }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test
    void testCodeSelectStaticProperty2() {
        String contents = '''\
            class Foo {
              static def getSql() { null }
              def foo() {
                sql
              }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test
    void testCodeSelectStaticProperty3() {
        String contents = '''\
            import java.util.logging.*
            class Foo {
              static Logger getLog() { null }
              def foo() {
                log.info 'message' // should not be confused with field created by @Log transform (see CodeSelectFieldsTests.testCodeSelectLoggerFieldInClass)
              }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'log', 'getLog')
    }

    // TODO: map properties, unknown properties
}
