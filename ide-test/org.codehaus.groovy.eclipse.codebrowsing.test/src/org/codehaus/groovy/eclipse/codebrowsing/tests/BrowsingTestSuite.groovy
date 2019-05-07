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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.junit.Assert.*

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.SourceRange

abstract class BrowsingTestSuite extends GroovyEclipseTestSuite {

    protected IJavaElement assertCodeSelect(Iterable<? extends CharSequence> sources, String target, String elementName = target) {
        def unit = null
        sources.each {
            unit = addGroovySource(it.toString(), nextUnitName())
        }
        prepareForCodeSelect(unit)

        int offset = unit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= unit.source.length()

        IJavaElement[] elems = unit.codeSelect(offset, length)
        if (!elementName) {
            assertEquals(0, elems.length)
        } else {
            assertEquals('Should have found a selection', 1, elems.length)
            assertEquals('Should have found reference to: ' + elementName, elementName, elems[0].elementName)
            assertTrue('Element should have existed in the model', elems[0].exists())
            return elems[0]
        }
    }

    protected IJavaElement assertCodeSelect(CharSequence source, SourceRange targetRange, String elementName) {
        GroovyCompilationUnit gunit = addGroovySource(source, nextUnitName())
        prepareForCodeSelect(gunit)

        IJavaElement[] elems = gunit.codeSelect(targetRange.offset, targetRange.length)
        if (!elementName) {
            assertEquals(0, elems.length)
        } else {
            assertEquals('Should have found a selection', 1, elems.length)
            assertEquals('Should have found reference to: ' + elementName, elementName, elems[0].elementName)
            assertTrue(elems[0].exists())
            return elems[0]
        }
    }

    protected void prepareForCodeSelect(ICompilationUnit unit) {
        openInEditor(unit)
        if (unit instanceof GroovyCompilationUnit) {
            def problems = unit.getModuleInfo(true).result.problems
            problems?.findAll { it.error }?.each { println it }
        }
    }
}
