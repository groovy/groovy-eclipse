/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.extract

import static org.eclipse.jdt.core.compiler.CharOperation.indexOf

import org.codehaus.groovy.eclipse.refactoring.core.convert.ConvertToMethodRefactoring
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.internal.core.util.SimpleDocument
import org.junit.Assert
import org.junit.Test

final class ConvertFieldToMethodTests extends RefactoringTestSuite {

    final String refactoringPath = null

    private void doContentsCompareTest(CharSequence originalContents, CharSequence expectedContents = originalContents) {
        def unit = (GroovyCompilationUnit) createCU(packageP, 'C.groovy', originalContents.stripIndent())
        def todo = new ConvertToMethodRefactoring(unit, indexOf('x' as char, unit.contents))
        def document = new SimpleDocument(unit.contents as String)
        todo.applyRefactoring(document)

        def expect = expectedContents.stripIndent()
        Assert.assertEquals(expect, document.get())
    }

    //--------------------------------------------------------------------------

    @Test
    void testPropertyToMethod0() {
        String originalContents = '''\
            class C {
                String x
                String y
                String z
            }
            '''
        doContentsCompareTest(originalContents)
    }

    @Test
    void testPropertyToMethod1() {
        String originalContents = '''\
            class C {
                String x = { }
            }
            '''
        String expectedContents = '''\
            class C {
                String x() { }
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testPropertyToMethod2() {
        String originalContents = '''\
            class C {
                String x = { -> }
            }
            '''
        String expectedContents = '''\
            class C {
                String x() { }
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testPropertyToMethod3() {
        String originalContents = '''\
            class C {
                String x = { a -> print '' }
            }
            '''
        String expectedContents = '''\
            class C {
                String x(a) { print '' }
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testPropertyToMethod4() {
        String originalContents = '''\
            class C {
                String x = { a, ... b -> print '' }
            }
            '''
        String expectedContents = '''\
            class C {
                String x(a, ... b) { print '' }
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }
}
