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

import static org.eclipse.jdt.internal.compiler.impl.CompilerOptions.OPTIONG_GroovyCompilerConfigScript

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder
import org.junit.Before
import org.junit.Test

final class JUnit4TestFinderTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        addJUnit(4)
    }

    private Set<IType> getAllTests() {
        Set<IType> testTypes = []
        SynchronizationUtils.waitForIndexingToComplete()
        new JUnit4TestFinder().findTestsInContainer(packageFragmentRoot, testTypes, null)
        return testTypes
    }

    private boolean isTest(ICompilationUnit unit, String typeName = unit.types[0].elementName) {
        def type = unit.getType(typeName)
        assert type.exists() : "Groovy type $typeName should exist"
        return new JUnit4TestFinder().isTest(type)
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
            class C {
                @org.junit.Test
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

    @Test
    void testIsTest5() {
        def unit = addGroovySource '''
            @org.junit.runner.RunWith(org.junit.runners.Suite)
            class C {
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test // GRECLIPSE-569
    void testIsTest6() {
        def unit = addGroovySource '''
            class C {
                @org.junit.Test(expected=RuntimeException)
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1542
    void testIsTest7() {
        addGroovySource '''import static java.lang.annotation.ElementType.*
            @java.lang.annotation.Target([METHOD,CONSTRUCTOR])
            @interface A { }
        ''', 'A', 'p'

        def unit = addGroovySource '''
            class C {
                @A m(x, y='foo', z='bar') { }
                @org.junit.Test
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)

        unit = addGroovySource '''
            class C {
                @A C(x, y='foo', z='bar') { }
            }
        ''', 'C', 'p'
        assert !isTest(unit)
    }

    //

    @Test
    void testFindTests1() {
        addGroovySource '''
            abstract class TestBase extends junit.framework.TestCase {
            }
        ''', 'C', 'p'
        addGroovySource '''
            class X1 extends TestBase {
            }
        ''', 'X1', 'p'
        addGroovySource '''
            class Y1 extends junit.framework.TestCase {
            }
        ''', 'Y1', 'p'
        addGroovySource '''
            class Z1 {
                static junit.framework.Test suite() throws Exception {}
            }
        ''', 'Z1', 'p'
        addGroovySource '''
            class X2 {
                @org.junit.Test
                void method() {}
            }
        ''', 'X2', 'p'
        addGroovySource '''
            class Y2 {
                @org.junit.Test(expected = IllegalStateException)
                void method() {}
            }
        ''', 'Y2', 'p'
        addGroovySource '''
            @org.junit.runner.RunWith(org.junit.runners.Suite)
            class Z2 {
                void method() {}
            }
        ''', 'Z2', 'p'

        Set<IType> testTypes = allTests

        assert testTypes.any { it.elementName == 'X1' } : 'X1 should be a test type'
        assert testTypes.any { it.elementName == 'Y1' } : 'Y1 should be a test type'
        assert testTypes.any { it.elementName == 'Z1' } : 'Z1 should be a test type'
        assert testTypes.any { it.elementName == 'X2' } : 'X2 should be a test type'
        assert testTypes.any { it.elementName == 'Y2' } : 'Y2 should be a test type'
        assert testTypes.any { it.elementName == 'Z2' } : 'Z2 should be a test type'
        assert testTypes.size() == 6
    }

    @Test
    void testFindTests2() {
        try {
            setJavaPreference(OPTIONG_GroovyCompilerConfigScript, 'config.groovy')
            addPlainText '''
                withConfig(configuration) {
                    // fails if HierarchyBuilder returns file name without path
                    source(unitValidator: { unit -> !!(unit.name =~ /.p./) }) {
                        imports {
                            normal 'junit.framework.TestCase'
                            normal 'org.junit.Test'
                        }
                    }
                }
            ''', '../config.groovy'

            addGroovySource '''
                abstract class TestBase extends TestCase {
                }
            ''', 'TestBase', 'p'
            addGroovySource '''
                class X3 extends TestBase {
                }
            ''', 'X3', 'p'
            addGroovySource '''
                class X4 {
                    @Test // unresolved if GroovyCompilationUnit skips config script
                    void m() {
                    }
                }
            ''', 'X4', 'p'

            Set<IType> testTypes = allTests

            assert testTypes.any { it.elementName == 'X3' } : 'X3 should be a test type'
            assert testTypes.any { it.elementName == 'X4' } : 'X4 should be a test type'
            assert testTypes.size() == 2
        } finally {
            setJavaPreference(OPTIONG_GroovyCompilerConfigScript, null)
        }
    }
}
