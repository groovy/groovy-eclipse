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

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope
import org.eclipse.jdt.internal.ui.util.MainMethodSearchEngine
import org.eclipse.jdt.ui.IJavaElementSearchConstants
import org.junit.Test

/**
 * Tests for {@link org.eclipse.jdt.internal.ui.util.MainMethodSearchEngine}.
 */
final class MainMethodFinderTests extends JUnitTestSuite {

    /**
     * @param expected fully-qualified type names
     */
    private expectTypesWithMain(String... expected) {
        MainMethodSearchEngine engine = new MainMethodSearchEngine()
        def types = engine.searchMainMethods(new NullProgressMonitor(),
            new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
        assert types.length == expected.length : "Wrong number of main methods found in: ${ -> types*.fullyQualifiedName }"
        Arrays.asList(types).eachWithIndex { type, i ->
            assert type.fullyQualifiedName == expected[i]
        }
    }

    @Test
    void testMainMethodFinder1() {
        addGroovySource '''
            class A {
                static def main(args) {}
            }
            '''

        expectTypesWithMain 'p.A'
    }

    @Test
    void testMainMethodFinder2() {
        addGroovySource '''
            class B {
                static def main(String... args) {}
            }
            '''

        expectTypesWithMain()
    }

    @Test
    void testMainMethodFinder3() {
        addGroovySource '''
            class C {
                static def main(String[] args) {}
            }
            '''

        expectTypesWithMain()
    }

    @Test
    void testMainMethodFinder4() {
        addGroovySource '''
            class D {
                private static def main(String[] args) {}
            }
            '''

        expectTypesWithMain()
    }

    @Test
    void testMainMethodFinder5() {
        addGroovySource '''
            class E {
                def main(String[] args) {}
            }
            '''

        expectTypesWithMain()
    }

    @Test
    void testMainMethodFinder6() {
        addGroovySource 'print "hello"', 'script1'

        expectTypesWithMain 'p.script1'
    }

    @Test
    void testMainMethodFinder7() {
        addGroovySource '''
            print 'Hello'

            class F {
                static def main(args) {}
            }
            ''', 'script2'

        expectTypesWithMain 'p.script2', 'p.F'
    }

    @Test
    void testMainMethodFinder8() {
        addGroovySource '''
            print 'Hello'

            class G {
                static def main(args) {}
            }
            class H {
                static def main(args) {}
            }
            ''', 'script3'

        expectTypesWithMain 'p.script3', 'p.G', 'p.H'
    }
}
