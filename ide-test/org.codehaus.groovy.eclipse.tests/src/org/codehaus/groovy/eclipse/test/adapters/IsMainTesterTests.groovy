/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.adapters

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.jdt.groovy.core.GroovyPropertyTester
import org.junit.Test

/**
 * Tests to see if a groovy file has a runnable main method.
 */
final class IsMainTesterTests extends GroovyEclipseTestSuite {

    private void doTest(String name = 'MainType', String text, boolean expected) {
        def unit = addGroovySource(text, name, 'a_package')
        boolean hasMain = new GroovyPropertyTester().test(unit, 'hasMain', null, null)
        assert hasMain == expected : 'Should have ' + (expected ? '' : '*not*') + ' found a main method in class:\n' + text
    }

    @Test
    void testHasMain1() {
        doTest('class MainType { static void main(String[] args){}}', true)
    }

    @Test
    void testHasMain2() {
        doTest('class MainType { static main(args){}}', true)
    }

    @Test
    void testHasMain3() {
        doTest('class MainType { static def main(args){}}', true)
    }

    @Test
    void testHasMain4() {
        // not static
        doTest('class MainType { void main(String[] args){}}', false)
    }

    @Test
    void testHasMain5() {
        // no args
        doTest('class MainType { static void main(){}}', false)
    }

    @Test
    void testHasMain6() {
        // no script defined in this file
        doTest('class OtherClass { def s() { } }', false)
    }

    @Test
    void testHasMain7() {
        // has a script
        doTest('thisIsPartOfAScript()\nclass OtherClass { def s() { } }', true)
    }

    @Test
    void testHasMain8() {
        // has a script
        doTest('class OtherClass { def s() { } }\nthisIsPartOfAScript()', true)
    }

    @Test
    void testHasMain9() {
        doTest('thisIsPartOfAScript()', true)
    }

    @Test
    void testHasMain10() {
        doTest('def x() { } \nx()', true)
    }

    @Test
    void testHasMain11() {
        doTest('main_type', 'print "works"', true)
    }

    // TODO: GROOVY-4020, GROOVY-5760
}
