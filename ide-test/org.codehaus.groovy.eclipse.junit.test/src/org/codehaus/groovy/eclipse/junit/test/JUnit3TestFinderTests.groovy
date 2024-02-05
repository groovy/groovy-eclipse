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
package org.codehaus.groovy.eclipse.junit.test

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.internal.junit.launcher.JUnit3TestFinder
import org.junit.Before
import org.junit.Test

final class JUnit3TestFinderTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        addJUnit(3)
    }

    private Set<IType> getAllTests() {
        Set<IType> testTypes = []
        SynchronizationUtils.waitForIndexingToComplete()
        new JUnit3TestFinder().findTestsInContainer(packageFragmentRoot, testTypes, null)
        return testTypes
    }

    private boolean isTest(ICompilationUnit unit, String typeName = unit.types[0].elementName) {
        def type = unit.getType(typeName)
        assert type.exists() : "Groovy type $typeName should exist"
        return new JUnit3TestFinder().isTest(type)
    }

    //--------------------------------------------------------------------------

    @Test
    void testIsTest0() {
        def unit = addGroovySource '''
            class C {
                void test() { }
            }
        ''', 'C', 'p'
        assert !isTest(unit)
    }

    @Test
    void testIsTest1() {
        def unit = addGroovySource '''
            class C extends junit.framework.TestCase {
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest2() {
        def unit = addGroovySource '''
            class C {
                static junit.framework.Test suite() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest3() {
        def unit = addGroovySource '''
            abstract class TestBase extends junit.framework.TestCase {
            }
        ''', 'TestBase', 'p'
        assert !isTest(unit)

        unit = addGroovySource '''
            class C extends TestBase {
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest4() {
        def unit = addGroovySource '''
            abstract class TestBase extends junit.framework.TestCase {
            }
        ''', 'TestBase', 'p'
        assert !isTest(unit)

        unit = addGroovySource '''
            @groovy.transform.PackageScope class C extends TestBase {
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    //

    @Test
    void testFindTests() {
        addGroovySource '''
            abstract class TestBase extends junit.framework.TestCase {
            }
        ''', 'TestBase', 'p'
        addGroovySource '''
            class X extends TestBase {
            }
        ''', 'X', 'p'
        addGroovySource '''
            class Y extends junit.framework.TestCase {
            }
        ''', 'Y', 'p'
        addGroovySource '''
            class Z {
                static junit.framework.Test suite() throws Exception {}
            }
        ''', 'Z', 'p'

        Set<IType> testTypes = allTests

        assert testTypes.any { it.elementName == 'X' } : 'X should be a test type'
        assert testTypes.any { it.elementName == 'Y' } : 'Y should be a test type'
        assert testTypes.any { it.elementName == 'Z' } : 'Z should be a test type'
        assert testTypes.size() == 3
    }
}
