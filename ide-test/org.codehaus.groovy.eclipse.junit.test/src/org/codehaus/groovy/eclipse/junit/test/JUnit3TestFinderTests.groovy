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
package org.codehaus.groovy.eclipse.junit.test

import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.internal.junit.launcher.JUnit3TestFinder
import org.junit.Test

final class JUnit3TestFinderTests extends JUnitTestSuite {

    private void assertTypeIsTest(boolean expected, ICompilationUnit unit, String typeName, String reasonText = '') {
        def type = unit.getType(typeName)
        assert type.exists() : "Groovy type $typeName should exist"
        assert new JUnit3TestFinder().isTest(type) == expected : "Groovy type $typeName should${expected ? '' : 'n\'t'} be a JUnit 3 test $reasonText"
    }

    @Test
    void testFinderWithSuite() {
        def test = addGroovySource '''
            class A {
                static junit.framework.Test suite() throws Exception {}
            }
            '''

        assertTypeIsTest(true, test, 'A')
    }

    @Test
    void testFinderOfSubclass() {
        def base = addGroovySource '''
            abstract class TestBase extends junit.framework.TestCase {
            }
            '''

        def test = addGroovySource '''
            class B extends TestBase {
            }
            '''

        assertTypeIsTest(false, base, 'TestBase', '(it is abstract)')
        assertTypeIsTest(true, test, 'B')
    }

    @Test
    void testFinderOfNonPublicSubclass() {
        def base = addGroovySource '''
            abstract class TestBase extends junit.framework.TestCase {
            }
            '''

        def test = addGroovySource '''
            @groovy.transform.PackageScope class C extends TestBase {
            }
            '''

        assertTypeIsTest(false, base, 'TestBase', '(it is abstract)')
        assertTypeIsTest(true, test, 'C')
    }

    @Test
    void testFindAllTestSuites() {
        def base = addGroovySource '''
            abstract class TestBase extends junit.framework.TestCase {
            }
            '''

        addGroovySource '''
            class X extends TestBase {
            }
            '''

        addGroovySource '''
            class Y extends junit.framework.TestCase {
            }
            '''

        addGroovySource '''
            class Z {
                static junit.framework.Test suite() throws Exception {}
            }
            '''

        Set<IType> testTypes = []
        new JUnit3TestFinder().findTestsInContainer(base.javaProject, testTypes, null)

        assert testTypes.any { it.elementName == 'X' } : 'X should be a test type'
        assert testTypes.any { it.elementName == 'Y' } : 'Y should be a test type'
        assert testTypes.any { it.elementName == 'Z' } : 'Z should be a test type'
        assert testTypes.size() == 3
    }
}
