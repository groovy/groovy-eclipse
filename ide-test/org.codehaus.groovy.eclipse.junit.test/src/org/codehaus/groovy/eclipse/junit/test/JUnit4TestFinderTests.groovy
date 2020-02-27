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
import org.eclipse.jdt.internal.junit.JUnitPropertyTester
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder
import org.junit.Test

final class JUnit4TestFinderTests extends JUnitTestSuite {

    private void assertTypeIsTest(boolean expected, ICompilationUnit unit, String typeName, String reasonText = '') {
        def type = unit.getType(typeName)
        assert type.exists() : "Groovy type $typeName should exist"
        assert new JUnit4TestFinder().isTest(type) == expected : "Groovy type $typeName should${expected ? '' : 'n\'t'} be a JUnit 4 test $reasonText"
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
    void testUsingTestAnnotation() {
        def test = addGroovySource '''
            class D {
                @org.junit.Test
                void method() {}
            }
            '''

        assertTypeIsTest(true, test, 'D')
    }

    @Test
    void testUsingRunWithAnnotation() {
        def test = addGroovySource '''
            @org.junit.runner.RunWith(org.junit.runners.Suite)
            class E {
                void method() {}
            }
            '''

        assertTypeIsTest(true, test, 'E')
    }

    @Test // GRECLIPSE-569: @Test(expected=RuntimeException) not being found
    void testFindTestWithExpectedException() {
        def test = addGroovySource '''
            class F {
                @org.junit.Test(expected=RuntimeException)
                void method() {}
            }
            '''

        boolean found = new JUnitPropertyTester().test(test, 'canLaunchAsJUnit', new Object[0], null)
        assert found : "F should be a test type for $test.elementName"
    }

    @Test
    void testFindAllTestSuites() {
        addGroovySource '''
            abstract class TestBase extends junit.framework.TestCase {
            }
            '''

        addGroovySource '''
            class X3 extends TestBase {
            }
            '''

        addGroovySource '''
            class Y3 extends junit.framework.TestCase {
            }
            '''

        addGroovySource '''
            class Z3 {
                static junit.framework.Test suite() throws Exception {}
            }
            '''

        addGroovySource '''
            class X4 {
                @org.junit.Test
                void method() {}
            }
            '''

        addGroovySource '''
            class Y4 {
                @org.junit.Test(expected = IllegalStateException)
                void method() {}
            }
            '''

        addGroovySource '''
            @org.junit.runner.RunWith(org.junit.runners.Suite)
            class Z4 {
                void method() {}
            }
            '''

        Set<IType> testTypes = []
        new JUnit4TestFinder().findTestsInContainer(packageFragmentRoot, testTypes, null)

        assert testTypes.any { it.elementName == 'X3' } : 'X3 should be a test type'
        assert testTypes.any { it.elementName == 'Y3' } : 'Y3 should be a test type'
        assert testTypes.any { it.elementName == 'Z3' } : 'Z3 should be a test type'
        assert testTypes.any { it.elementName == 'X4' } : 'X4 should be a test type'
        assert testTypes.any { it.elementName == 'Y4' } : 'Y4 should be a test type'
        assert testTypes.any { it.elementName == 'Z4' } : 'Z4 should be a test type'
        assert testTypes.size() == 6
    }
}
