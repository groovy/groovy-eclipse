/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.junit.test

import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.internal.junit.launcher.JUnit3TestFinder
import org.junit.Test

final class JUnit3TestFinderTests extends JUnitTestSuite {

    private void assertTypeIsTest(boolean expected, ICompilationUnit unit, String typeName, String reasonText = '') {
        IType type = unit.getType(typeName)
        assert type.exists() : "Groovy type $typeName should exist"
        assert new JUnit3TestFinder().isTest(type) == expected : "Groovy type $typeName should${expected ? '' : 'n\'t'} be a JUnit 3 test $reasonText"
    }

    @Test
    void testFinderWithSuite() {
        def unit = addGroovySource('''\
            import junit.framework.Test
            public class Hello {
              public static Test suite() throws Exception { }
            }
            '''.stripIndent()
        )

        assertTypeIsTest(true, unit, 'Hello')
    }

    @Test
    void testFinderOfSubclass() {
        def unit1 = addGroovySource('''\
            public class Hello extends Tester {
            }
            '''.stripIndent()
        )
        def unit2 = addGroovySource('''\
            import junit.framework.TestCase
            abstract class Tester extends TestCase {
            }
            '''.stripIndent(), 'Tester'
        )

        assertTypeIsTest(true, unit1, 'Hello')
        assertTypeIsTest(false, unit2, 'Tester', '(it is abstract)')
    }

    @Test
    void testFinderOfNonPublicSubclass() {
        def unit1 = addGroovySource('''\
            class Hello extends Tester {
            }
            '''.stripIndent()
        )
        def unit2 = addGroovySource('''\
            import junit.framework.TestCase
            abstract class Tester extends TestCase {
            }
            '''.stripIndent(), 'Tester'
        )

        assertTypeIsTest(true, unit1, 'Hello', '(even though it is non-public)')
        assertTypeIsTest(false, unit2, 'Tester', '(it is abstract)')
    }

    @Test
    void testFindAllTestSuites() {
        def unit = addGroovySource('''\
            public class Hello extends Tester {
            }
            '''.stripIndent()
        )
        addGroovySource('''\
            import junit.framework.TestCase
            public class Tester extends TestCase {
            }
            '''.stripIndent(), 'Tester'
        )
        addGroovySource('''\
            import junit.framework.Test
            public class Hello2 {
               public static Test suite() throws Exception { }
            }
            '''.stripIndent(), 'Hello2'
        )
        addGroovySource('''\
            import junit.framework.TestCase
            abstract class NotATest extends TestCase {
            }
            '''.stripIndent(), 'NotATest'
        )

        Set<IType> testTypes = [] as Set
        new JUnit3TestFinder().findTestsInContainer(unit.getJavaProject(), testTypes, null)

        assert testTypes.size() == 3 : 'Should have found 3 test classes'
        assert testTypes.any { it.elementName == 'Hello' } : 'Hello should be a test type'
        assert testTypes.any { it.elementName == 'Hello2' } : 'Hello2 should be a test type'
        assert testTypes.any { it.elementName == 'Tester' } : 'Tester should be a test type'
    }
}
