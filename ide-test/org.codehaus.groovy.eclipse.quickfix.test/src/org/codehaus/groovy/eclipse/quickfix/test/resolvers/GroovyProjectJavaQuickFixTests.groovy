/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.test.resolvers

import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.ConvertToGroovyFileResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemType
import org.eclipse.core.resources.IMarker
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.junit.Test

/**
 * Tests Groovy quick fixes in a Java file contained in a Groovy project.
 */
final class GroovyProjectJavaQuickFixTests extends QuickFixHarness {

    @Test
    void testNoGroovyAddImportQuickFix() {
        def unit = addJavaSource('''\
            class TestJavaC {
              public void doSomething() {
                ImageBuilder imageBuilder = null;
              }
            }'''.stripIndent(), 'TestJavaC', 'test1')

        AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver('ImageBuilder', unit)
        assert resolver == null : "Expected no Groovy add import quick fix resolver for unresolved type: ImageBuilder in $unit.resource.name, as it is a Java file"
    }

    @Test
    void testConvertToGroovyQuickFixMissingSemiColon1() {
        def unit = addJavaSource('''\
            class TestJavaC {
              public void doSomething() {
                ImageBuilder imageBuilder = null
              }
            }'''.stripIndent(), 'TestJavaC', 'test2')

        ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit)
        assert resolver != null : 'Expected a quick fix resolver for converting to Groovy; none found'

        List<IJavaCompletionProposal> proposals = resolver.getQuickFixProposals()
        assert proposals?.size() > 0 : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixMissingSemiColon2() {
        def unit = addJavaSource('''\
            abstract class TestJavaC {
              public abstract void doSomething()
            }'''.stripIndent(), 'TestJavaC', 'test3')

        ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit)
        assert resolver != null : 'Expected a quick fix resolver for converting to Groovy; none found'

        List<IJavaCompletionProposal> proposals = resolver.getQuickFixProposals()
        assert proposals?.size() > 0 : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    //

    protected ConvertToGroovyFileResolver getConvertToGroovyQuickFixResolver(ICompilationUnit unit) {
        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit)
        List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, ProblemType.MISSING_SEMI_COLON_TYPE, unit)
        assert resolvers?.size() > 0
        for (resolver in resolvers) {
            if (resolver instanceof ConvertToGroovyFileResolver) {
                return (ConvertToGroovyFileResolver) resolver
            }
        }
        return null
    }
}
