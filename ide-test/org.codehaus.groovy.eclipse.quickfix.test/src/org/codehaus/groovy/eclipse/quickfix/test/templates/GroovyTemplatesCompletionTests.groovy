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
package org.codehaus.groovy.eclipse.quickfix.test.templates

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

import org.codehaus.groovy.eclipse.quickfix.templates.TemplateProposalComputer
import org.codehaus.groovy.eclipse.test.EclipseTestSetup
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext
import org.eclipse.jface.text.contentassist.ICompletionProposal

/**
 * Tests the Groovy templates contributed by the quickfix plug-in.
 */
final class GroovyTemplatesCompletionTests extends TestCase {

    static Test suite() {
        new EclipseTestSetup(new TestSuite(GroovyTemplatesCompletionTests))
    }

    @Override
    protected void tearDown() throws Exception {
        EclipseTestSetup.removeSources()
    }

    @Override
    protected void setUp() throws Exception {
        EclipseTestSetup.setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE)
        EclipseTestSetup.setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, '2')

        println '----------------------------------------'
        println 'Starting: ' + getName()
    }

    /**
     * @param contents editor begin state
     * @param expected editor end state -- use '|' at the end of a line to preserve thw whitespace
     * @param target substring to find for code completion proposals
     * @param which name of completion proposal to select and apply
     */
    protected void runTest(CharSequence contents, CharSequence expected, String target, String which = target) {
        def unit = EclipseTestSetup.addGroovySource(contents.stripIndent().toString(), nextFileName())
        def editor = EclipseTestSetup.openInEditor(unit)
        int offset = contents.stripIndent().toString().indexOf(target) + target.length()
        def context = new JavaContentAssistInvocationContext(editor.viewer, offset, editor)

        // find proposal
        List<ICompletionProposal> proposals = new TemplateProposalComputer().computeCompletionProposals(context, null)
        assertTrue('Expected some proposals, but got none', proposals != null && !proposals.empty)
        def matches = proposals.findAll { it.displayString.startsWith(which + ' - ') }
        assertEquals('Expected a match, but got ' + matches.size(), 1, matches.size())

        // apply template
        matches[0].apply(editor.viewer, 'x' as char, 0, offset)
        SynchronizationUtils.runEventQueue() // allow the change to show in the editor

        String expect = expected.stripIndent().toString().replace('|', '').replace('\r\n', '\n')
        String actual = editor.viewer.document.get().replace('\r\n', '\n')
        assertEquals(expect, actual)
    }

    protected static String nextFileName() {
        "File${salt.nextInt(999999)}"
    }

    private static final Random salt = new Random(System.currentTimeMillis())

    //--------------------------------------------------------------------------

    void testBasicTemplate() {
        String input = '''\
            try
            '''
        String output = '''\
            try {
              line_selection
            } catch (Exception e) {
              e.printStackTrace()
            }
            '''
        runTest(input, output, 'try')
    }

    void testJUnitBefore() {
        String input = '''\
            class SomeTest {
              Bef
            }
            '''
        String output = '''\
            import org.junit.Before

            class SomeTest {
              @Before
              void before() {
                |
              }
            }
            '''
        runTest(input, output, 'Bef', 'Before')
    }

    void testJUnitAfter() {
        String input = '''\
            class SomeTest {
              Aft
            }
            '''
        String output = '''\
            import org.junit.After

            class SomeTest {
              @After
              void after() {
                |
              }
            }
            '''
        runTest(input, output, 'Aft', 'After')
    }

    void testGContractsEnsures() {
        String input = '''\
            class SomeTest {
              Ens
              def meth() {
              }
            }
            '''
        String output = '''\
            import org.gcontracts.annotations.Ensures

            class SomeTest {
              @Ensures({ predicate })
              def meth() {
              }
            }
            '''
        runTest(input, output, 'Ens', 'Ensures')
    }

    void testGContractsRequires() {
        String input = '''\
            class SomeTest {
              Req
              def meth() {
              }
            }
            '''
        String output = '''\
            import org.gcontracts.annotations.Requires

            class SomeTest {
              @Requires({ predicate })
              def meth() {
              }
            }
            '''
        runTest(input, output, 'Req', 'Requires')
    }
}
