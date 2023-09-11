/*
 * Copyright 2009-2023 the original author or authors.
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
import org.eclipse.jdt.groovy.core.GroovyPropertyTester
import org.junit.Test

/**
 * Tests to see if a groovy file has a runnable main method.
 */
final class HasMainTesterTests extends GroovyEclipseTestSuite {

    private void doTest(String name = 'Main', String text, boolean expected) {
        boolean hasMain = new GroovyPropertyTester().test(addGroovySource(text, name), 'hasMain', null, null)
        assert (hasMain == expected) : "Should have ${expected ? '' : '*not*'} found a main method in:\n$text"
    }

    @Test
    void testHasMain1() {
        doTest('class Main { public\n static void main(String[] args){} }', true)
    }

    @Test
    void testHasMain2() {
        doTest('class Main { static void main(String[] args){} }', true)
    }

    @Test
    void testHasMain3() {
        doTest('class Main { static void main(args){} }', true)
    }

    @Test
    void testHasMain4() {
        doTest('class Main { static def main(args){} }', true)
    }

    @Test
    void testHasMain5() {
        doTest('class Main { static main(args){} }', true)
    }

    @Test
    void testHasMain6() {
        doTest('static main(args){}', true)
    }

    @Test
    void testHasMain7() {
        doTest('thisIsPartOfAScript()', true)
    }

    @Test
    void testHasMain8() {
        doTest('thisIsPartOfAScript()\nclass Other { def m(){} }', true)
    }

    @Test
    void testHasMain9() {
        doTest('class Other { def m(){} }\nthisIsPartOfAScript()', true)
    }

    @Test
    void testHasMain10() {
        doTest('main_type', 'print "works"', true)
    }

    @Test // GROOVY-4020, GROOVY-5760
    void testHasMain11() {
        doTest('main-type', 'print "works"', true)
    }

    //

    @Test // not public
    void testHasMain12() {
        doTest('class Main { private static void main(String[] args){} }', false)
    }

    @Test // not static
    void testHasMain13() {
        doTest('class Main { public/**/void main(String[] args){} }', false)
    }

    @Test // wrong type 1
    void testHasMain14() {
        doTest('class Main { static String main(String[] args){} }', false)
    }

    @Test // wrong type 2
    void testHasMain15() {
        doTest('class Main { static void main(Object[] args){} }', false)
    }

    @Test // wrong type 3
    void testHasMain16() {
        doTest('class Main { static void main(String args){} }', false)
    }

    @Test // wrong type 4
    void testHasMain17() {
        doTest('class Main { static <T> void main(T t){} }', false)
    }

    @Test // no args
    void testHasMain18() {
        doTest('class Main { static void main(){} }', false)
    }

    @Test // no main
    void testHasMain19() {
        doTest('class Main { def m(){} }', false)
    }
}
