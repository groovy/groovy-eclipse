/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.junit.test

import groovy.transform.InheritConstructors
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope
import org.eclipse.jdt.internal.ui.util.MainMethodSearchEngine
import org.eclipse.jdt.ui.IJavaElementSearchConstants

/**
 * Tests for {@link org.eclipse.jdt.internal.ui.util.MainMethodSearchEngine}
 */
@InheritConstructors @TypeChecked
final class MainMethodFinderTests extends JUnitTestCase {

    private void createGroovyType(CharSequence contents, String file = 'Hello', String pack = 'p2') {
        IPath proj = createGenericProject()
        IPath root = proj.append('src')

        env.addGroovyClass(root, pack, file, contents.toString())
        incrementalBuild(proj)
        expectingNoProblems()
    }

    /**
     * @param expected fully-qualified type names
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    private expectTypesWithMain(String... expected) {
        MainMethodSearchEngine engine = new MainMethodSearchEngine()
        IType[] types = engine.searchMainMethods(null as IProgressMonitor,
            new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
        assertEquals("Wrong number of main methods found in: ${ -> types.collect { it.fullyQualifiedName }}", expected.length, types.length)
        for (i in 0..<types.length) {
            assertEquals(expected[i], types[i].fullyQualifiedName)
        }
    }

    void testMainMethodFinder1() {
        createGroovyType '''
            class Foo {
              static def main(args) { }
            }
            '''

        expectTypesWithMain 'p2.Foo'
    }

    void testMainMethodFinder2() {
        createGroovyType '''
            class Foo {
              static def main(String... args) { }
            }
            '''

        expectTypesWithMain ()
    }

    void testMainMethodFinder3() {
        createGroovyType '''
            class Foo {
              static def main(String[] args) { }
            }
            '''

        expectTypesWithMain ()
    }

    void testMainMethodFinder4() {
        createGroovyType '''
            class Foo {
              private static def main(String[] args) { }
            }
            '''

        expectTypesWithMain ()
    }

    void testMainMethodFinder5() {
        createGroovyType '''
            class Foo {
              def main(String[] args) { }
            }
            '''

        expectTypesWithMain ()
    }

    void testMainMethodFinder6() {
        createGroovyType '''
            print 'Nothing'
            '''

        expectTypesWithMain 'p2.Hello'
    }

    void testMainMethodFinder7() {
        createGroovyType '''
            print 'Hello'

            class Foo {
              static def main(args) { }
            }
            '''

        expectTypesWithMain 'p2.Hello', 'p2.Foo'
    }

    void testMainMethodFinder8() {
        createGroovyType '''
            print 'Hello'

            class Foo {
              static def main(args) { }
            }
            class Bar {
              static def main(args) { }
            }
            '''

        expectTypesWithMain 'p2.Hello', 'p2.Foo', 'p2.Bar'
    }
}
