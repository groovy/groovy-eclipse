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
package org.codehaus.groovy.eclipse.test.adapters

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.jdt.groovy.core.GroovyPropertyTester
import org.junit.Test

final class IsScriptTesterTests extends GroovyEclipseTestSuite {

    private void doTest(String text, boolean expected) {
        boolean isScript = new GroovyPropertyTester().test(addGroovySource(text, nextUnitName()), 'isScript', null, null)
        assert (isScript == expected) : "Should have ${expected ? '' : '*not*'} found a script class in:\n$text"
    }

    @Test
    void testIsScript1() {
        doTest('class Main {}', false)
    }

    @Test
    void testIsScript2() {
        doTest('def method() {}', false)
    }

    @Test
    void testIsScript3() {
        doTest('def ', false) // error
        doTest('def variable = 0', true)
    }

    @Test
    void testIsScript4() {
        doTest('print "hello world"', true)
    }

    @Test
    void testIsScript5() {
        doTest('class C {}\nprint "hello"', true)
    }

    @Test
    void testIsScript6() {
        doTest('void main() {}', isAtLeastGroovy(50))
    }

    @Test
    void testIsScript7() {
        doTest('void main(o) {}', isAtLeastGroovy(50))
    }

    @Test
    void testIsScript8() {
        doTest('void main(Object o) {}', isAtLeastGroovy(50))
    }

    @Test
    void testIsScript9() {
        doTest('void main(String[] a) {}', isAtLeastGroovy(50))
    }

    @Test
    void testIsScript10() {
        doTest('Object main(String[] a) {}', isAtLeastGroovy(50))
    }

    @Test
    void testIsScript11() {
        doTest('protected main(String[] a) {}', isAtLeastGroovy(50))
    }

    @Test
    void testIsScript12() {
        doTest('class Main { static void main(String[] args){} }', false)
    }

    @Test
    void testIsScript13() {
        doTest('class Main { void main(String[] args) {} }', false)
    }

    @Test
    void testIsScript14() {
        doTest('private void main(String[] args) {}', false)
    }

    @Test
    void testIsScript15() {
        doTest('void main(Object[] args) {}', false)
    }

    @Test
    void testIsScript16() {
        doTest('def <T> void main(T t) {}', false)
    }
}
