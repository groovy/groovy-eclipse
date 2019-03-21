/*
 * Copyright 2009-2018 the original author or authors.
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

import groovy.transform.CompileStatic

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
@CompileStatic
final class GroovyProjectJavaQuickFixTests extends QuickFixHarness {

    @Test
    void testNoGroovyAddImportQuickFix() {
        def unit = addJavaSource('''\
            class C {
              void doSomething() {
                ImageBuilder imageBuilder = null;
              }
            }'''.stripIndent())

        AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver('ImageBuilder', unit)
        assert resolver == null : "Expected no Groovy add import quick fix resolver for unresolved type: ImageBuilder in $unit.resource.name, as it is a Java file"
    }

    @Test
    void testConvertToGroovyQuickFixGroovyKeyword1() {
        def unit = addJavaSource('''\
            class C {
              def getSomething() {
              }
            }'''.stripIndent())

        ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit, ProblemType.GROOVY_KEYWORD_TYPE1)
        assert resolver != null : 'Expected a quick fix resolver for converting to Groovy; none found'

        List<IJavaCompletionProposal> proposals = resolver.quickFixProposals
        assert !proposals.isEmpty() : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixGroovyKeyword2() {
        def unit = addJavaSource('''\
            class C {
              void doSomething(Object x) {
                List l = x as List;
              }
            }'''.stripIndent())

        ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit, ProblemType.GROOVY_KEYWORD_TYPE1)
        assert resolver != null : 'Expected a quick fix resolver for converting to Groovy; none found'

        List<IJavaCompletionProposal> proposals = resolver.quickFixProposals
        assert !proposals.isEmpty() : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixGroovyKeyword3() {
        def unit = addJavaSource('''\
            class C {
              void doSomething(Object x) {
                for (Object i in x) {
                }
              }
            }'''.stripIndent())

        ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit, ProblemType.GROOVY_KEYWORD_TYPE2)
        assert resolver != null : 'Expected a quick fix resolver for converting to Groovy; none found'

        List<IJavaCompletionProposal> proposals = resolver.quickFixProposals
        assert !proposals.isEmpty() : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixGroovyKeyword4() {
        def unit = addJavaSource('''\
            trait T {
              void doSomething() {
              }
            }'''.stripIndent())

        ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit, ProblemType.GROOVY_KEYWORD_TYPE2)
        assert resolver != null : 'Expected a quick fix resolver for converting to Groovy; none found'

        List<IJavaCompletionProposal> proposals = resolver.quickFixProposals
        assert !proposals.isEmpty() : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixMissingSemiColon1() {
        def unit = addJavaSource('''\
            class C {
              void doSomething() {
                ImageBuilder imageBuilder = null
              }
            }'''.stripIndent())

        ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit, ProblemType.MISSING_SEMI_COLON_TYPE)
        assert resolver != null : 'Expected a quick fix resolver for converting to Groovy; none found'

        List<IJavaCompletionProposal> proposals = resolver.quickFixProposals
        assert !proposals.isEmpty() : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixMissingSemiColon2() {
        def unit = addJavaSource('''\
            abstract class C {
              abstract void doSomething()
            }'''.stripIndent())

        ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit, ProblemType.MISSING_SEMI_COLON_TYPE)
        assert resolver != null : 'Expected a quick fix resolver for converting to Groovy; none found'

        List<IJavaCompletionProposal> proposals = resolver.quickFixProposals
        assert !proposals.isEmpty() : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    //--------------------------------------------------------------------------

    protected ConvertToGroovyFileResolver getConvertToGroovyQuickFixResolver(ICompilationUnit unit, ProblemType type) {
        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit)
        List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, type, unit)
        assert !resolvers.isEmpty()
        for (resolver in resolvers) {
            if (resolver instanceof ConvertToGroovyFileResolver) {
                return resolver
            }
        }
    }
}
