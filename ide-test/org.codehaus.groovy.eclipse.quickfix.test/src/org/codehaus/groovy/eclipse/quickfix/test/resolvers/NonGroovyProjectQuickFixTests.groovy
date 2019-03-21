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
package org.codehaus.groovy.eclipse.quickfix.test.resolvers

import static org.codehaus.jdt.groovy.model.GroovyNature.GROOVY_NATURE

import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemType
import org.eclipse.core.resources.IMarker
import org.junit.Before
import org.junit.Test

/**
 * Tests that no Groovy quick fixes are present in either a Java or Groovy file in a non-Groovy project.
 */
final class NonGroovyProjectQuickFixTests extends QuickFixHarness {

    @Before
    void setUp() {
        removeNature(GROOVY_NATURE)
    }

    @Test
    void testNoGroovyAddImportQuickFix() {
        def unit = addJavaSource('''\
            class TestJavaC {
              public void doSomething() {
                ImageBuilder imageBuilder = null;
              }
            }
            '''.stripIndent(), 'TestJavaC', 'com.test')

        AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver('ImageBuilder', unit)
        assert resolver == null : "Expected no Groovy add import quick fix resolver for unresolved type: ImageBuilder in $unit.resource.name, as it is a Java project"
    }

    @Test
    void testNoGroovyQuickFixNonGroovyProject1() {
        def unit = addGroovySource('''\
            class TestGroovyC {
              public void doSomething() {
                ImageBuilder imageBuilder = null
              }
            }
            '''.stripIndent(), nextUnitName(), 'com.test')

        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit)
        for (type in ProblemType.values()) {
            List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, type, unit)
            assert resolvers == null || resolvers.isEmpty() : 'Encountered Groovy quick fix resolvers in a non-Groovy project; none expected'
        }
    }

    @Test
    void testNoGroovyQuickFixNonGroovyProject2() {
        def unit = addGroovySource('''\
            class TestGroovyC {
              public void doSomething() {
                ImageBuilder imageBuilder = null
              }
            }
            '''.stripIndent(), nextUnitName(), 'com.test')

        AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver('ImageBuilder', unit)
        assert resolver == null : 'Expected no Groovy add import resolvers as this is a Java project'
    }
}
