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
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.internal.junit.launcher.JUnit5TestFinder
import org.junit.Before
import org.junit.Test

final class JUnit5TestFinderTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        addJUnit(5)
    }

    private boolean isTest(ICompilationUnit unit, String typeName = unit.types[0].elementName) {
        def type = unit.getType(typeName)
        assert type.exists() : "Groovy type $typeName should exist"
        return new JUnit5TestFinder().isTest(type)
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
                @org.junit.jupiter.api.Test
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest3() {
        def unit = addGroovySource '''
            class C {
                @org.junit.jupiter.api.RepeatedTest(1)
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest4() {
        def unit = addGroovySource '''
            class C {
                @org.junit.jupiter.params.ParameterizedTest
                @org.junit.jupiter.params.provider.ValueSource(strings=['fizz','buzz'])
                void test(String string) { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest5() {
        def unit = addGroovySource '''
            class C {
                @org.junit.jupiter.params.ParameterizedTest
                @org.junit.jupiter.params.provider.NullSource
                @org.junit.jupiter.params.provider.EmptySource
                void test(String string) { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest6() {
        def unit = addGroovySource '''import org.junit.jupiter.api.*
            class C {
                @TestFactory
                DynamicTest test() {
                    DynamicTest.dynamicTest('test name') { -> assert true }
                }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest7() {
        def unit = addGroovySource '''
            class C {
                @org.junit.jupiter.api.Nested
                class D {
                    @org.junit.jupiter.api.Test
                    void test() { }
                }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest8() {
        def unit = addGroovySource '''
            @org.junit.jupiter.api.Disabled
            class C {
                @org.junit.jupiter.api.Test
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }

    @Test
    void testIsTest9() {
        def unit = addGroovySource '''
            @org.junit.jupiter.api.Test
            def @interface A {
            }
            class C {
                @A
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit, 'C')
    }

    @Test
    void testIsTest10() {
        def unit = addGroovySource '''
            class C {
                def m(x='foo', y='bar') { }
                @org.junit.jupiter.api.Test
                void test() { }
            }
        ''', 'C', 'p'
        assert isTest(unit)
    }
}
