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
package org.codehaus.groovy.eclipse.test.adapters

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.ui.GroovyResourcePropertyTester
import org.junit.Test

/**
 * Tests to see if a groovy file has a runnable main method.
 */
final class IsMainTesterTests extends GroovyEclipseTestSuite {

    private void doTest(String text, boolean expected) {
        def unit = addGroovySource(text, 'MainClass', 'pack1').getResource()
        boolean result = new GroovyResourcePropertyTester().test(unit, 'hasMain', null, null)
        assert result == expected : 'Should have ' + (expected ? '' : '*not*') + ' found a main method in class:\n' + text
    }

    @Test
    void testHasMain1() {
        doTest('class MainClass { static void main(String[] args){}}', true)
    }

    @Test
    void testHasMain2() {
        doTest('class MainClass { static main(args){}}', true)
    }

    @Test
    void testHasMain2a() {
        doTest('class MainClass { static def main(args){}}', true)
    }

    @Test
    void testHasMain3() {
        // not static
        doTest('class MainClass { void main(String[] args){}}', false)
    }

    @Test
    void testHasMain3a() {
        // no args
        doTest('class MainClass { static void main(){}}', false)
    }

    @Test
    void testHasMain4() {
        // no script defined in this file
        doTest('class OtherClass { def s() { } }', false)
    }

    @Test
    void testHasMain5() {
        // has a script
        doTest('thisIsPartOfAScript()\nclass OtherClass { def s() { } }', true)
    }

    @Test
    void testHasMain5a() {
        // has a script
        doTest('class OtherClass { def s() { } }\nthisIsPartOfAScript()', true)
    }

    @Test
    void testHasMain6() {
        doTest('thisIsPartOfAScript()', true)
    }

    @Test
    void testHasMain7() {
        doTest('def x() { } \nx()', true)
    }
}
