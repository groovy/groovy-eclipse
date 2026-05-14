/*
 * Copyright 2009-2021 the original author or authors.
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

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.BindingKey
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.SourceRange

abstract class BrowsingTestSuite extends GroovyEclipseTestSuite {

    protected IJavaElement assertCodeSelect(final Iterable<? extends CharSequence> sources, final String target, final String elementName = target) {
        GroovyCompilationUnit unit = null
        sources.each {
            unit = addGroovySource(it.toString(), nextUnitName())
        }
        prepareForCodeSelect(unit)

        int offset = unit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= unit.source.length()

        IJavaElement[] elements = unit.codeSelect(offset, length)
        if (!elementName) {
            assert elements.length == 0
        } else {
            assert elements.length == 1 : 'Should have found a selection'

            assert elements[0].elementName == elementName : "Should have found reference to: $elementName"
            assert elements[0].exists() : 'Element should exist in the model'
            if (elements[0] instanceof org.eclipse.jdt.core.IMember)
                new BindingKey(elements[0].key).toSignature()
            return elements[0]
        }
    }

    protected IJavaElement assertCodeSelect(final CharSequence source, final SourceRange targetRange, final String elementName) {
        GroovyCompilationUnit unit = addGroovySource(source, nextUnitName())
        prepareForCodeSelect(unit)

        IJavaElement[] elements = unit.codeSelect(targetRange.offset, targetRange.length)
        if (!elementName) {
            assert elements.length == 0
        } else {
            assert elements.length == 1 : 'Should have found a selection'

            assert elements[0].elementName == elementName : "Should have found reference to: $elementName"
            assert elements[0].exists() : 'Element should exist in the model'
            return elements[0]
        }
    }

    protected void prepareForCodeSelect(final ICompilationUnit unit) {
        openInEditor(unit)
        if (unit instanceof GroovyCompilationUnit) {
            def problems = unit.getModuleInfo(true).result.problems
            problems?.each { if (it.error) println it }
        }
    }
}
