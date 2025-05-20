/*
 * Copyright 2009-2025 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.tests

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Assert
import org.junit.Test

final class TriggerCharacterCompletionTests extends CompletionTestSuite {

    private static IPreferenceStore groovyPrefs = GroovyContentAssist.getDefault().preferenceStore

    private static CharSequence getExpected(String contents, ICompletionProposal proposal) {
        StringBuilder closure = new StringBuilder()
        closure << '{'
        if (isEnabled(FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER)) {
            closure << ' '
        }
        if (!proposal.toString().contains('Closure')) {
            closure << 'o1'
            if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER)) {
                closure << ' '
            }
            closure << ','
            if (isEnabled(FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER)) {
                closure << ' '
            }
            closure << 'o2 ->'
        }
        if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER)) {
            closure << ' '
        }
        closure << '}'

        StringBuilder expected = new StringBuilder(contents)
        if (!proposal.toString().contains('mutate')) {
            if (isEnabled(GroovyContentAssist.CLOSURE_NOPARENS)) {
                if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK)) {
                    expected << ' '
                }
                expected << closure
            } else {
                if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION)) {
                    expected << ' '
                }
                expected << '('

                if (isEnabled(FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION)) {
                    expected << ' '
                }
                if (isEnabled(GroovyContentAssist.NAMED_ARGUMENTS)) {
                    expected << (proposal.toString() =~ /(\w+)\)/)[0][1]
                    if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT)) {
                        expected << ' '
                    }
                    expected << ':'
                    if (isEnabled(FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT)) {
                        expected << ' '
                    }
                }
                expected << closure

                if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION)) {
                    expected << ' '
                }
                expected << ')'
            }
        } else /*proposal.toString().contains('mutate')*/ {
            if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION)) {
                expected << ' '
            }
            expected << '('

            if (isEnabled(FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION)) {
                expected << ' '
            }
            if (isEnabled(GroovyContentAssist.NAMED_ARGUMENTS)) {
                expected << 'mutate'
                if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT)) {
                    expected << ' '
                }
                expected << ':'
                if (isEnabled(FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT)) {
                    expected << ' '
                }
            }
            expected << 'mutate'

            if (!(proposal.toString() =~ /Closure|Comparator/)) {
                if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION)) {
                    expected << ' '
                }
                expected << ')'

                if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK)) {
                    expected << ' '
                }
                expected << '{'
            } else {
                if (!isEnabled(GroovyContentAssist.CLOSURE_NOPARENS)) {
                    if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS)) {
                        expected << ' '
                    }
                    expected << ','

                    if (isEnabled(FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS)) {
                        expected << ' '
                    }
                    if (isEnabled(GroovyContentAssist.NAMED_ARGUMENTS)) {
                        expected << (proposal.toString() =~ /(\w+)\)/)[0][1]
                        if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT)) {
                            expected << ' '
                        }
                        expected << ':'
                        if (isEnabled(FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT)) {
                            expected << ' '
                        }
                    }
                } else {
                    if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION)) {
                        expected << ' '
                    }
                    expected << ')'

                    if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK)) {
                        expected << ' '
                    }
                }
                expected << closure

                if (!isEnabled(GroovyContentAssist.CLOSURE_NOPARENS)) {
                    if (isEnabled(FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION)) {
                        expected << ' '
                    }
                    expected << ')'
                }
            }
        }
        expected
    }

    private static boolean isEnabled(String preference) {
        if (preference.startsWith(GroovyContentAssist.PLUGIN_ID)) {
            groovyPrefs.getBoolean(preference)
        } else {
            JavaCore.getOption(preference) ==~ (JavaCore.ENABLED + '|' + JavaCore.INSERT)
        }
    }

    //--------------------------------------------------------------------------

    @Test
    void testTriggerForMethodProposals0() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        groovyPrefs.setValue(GroovyContentAssist.NAMED_ARGUMENTS , false)

        String contents = '((Collection) []).sort'; int offset = contents.length()
        List<ICompletionProposal> proposals = orderByRelevance(createProposalsAtOffset(contents, offset)).findAll {
            it.toString().startsWith('sort') && !it.toString().contains('()') // '{' is not a valid trigger
        }
        /* expecting:
        sort(Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate) : List - DefaultGroovyMethods
        sort(boolean mutate, Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate, Comparator comparator) : List - DefaultGroovyMethods
        */
        for (proposal in proposals) {
            IDocument document = new Document(contents)
            proposal.apply(document, '{' as char, offset)
            def expected = getExpected(contents, proposal)
            Assert.assertEquals(proposal.toString(), expected.normalize(), document.get().normalize())
        }
    }

    @Test
    void testTriggerForMethodProposals1() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        groovyPrefs.setValue(GroovyContentAssist.NAMED_ARGUMENTS , false)

        String contents = '((Collection) []).sort'; int offset = contents.length()
        List<ICompletionProposal> proposals = orderByRelevance(createProposalsAtOffset(contents, offset)).findAll {
            it.toString().startsWith('sort') && !it.toString().contains('()') // '{' is not a valid trigger
        }
        /* expecting:
        sort(Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate) : List - DefaultGroovyMethods
        sort(boolean mutate, Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate, Comparator comparator) : List - DefaultGroovyMethods
        */

        for (proposal in proposals) {
            IDocument document = new Document(contents)
            proposal.apply(document, '{' as char, offset)
            def expected = getExpected(contents, proposal)
            Assert.assertEquals(proposal.toString(), expected.normalize(), document.get().normalize())
        }
    }

    @Test
    void testTriggerForMethodProposals2() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)
        groovyPrefs.setValue(GroovyContentAssist.NAMED_ARGUMENTS , false)

        String contents = '((Collection) []).sort'; int offset = contents.length()
        List<ICompletionProposal> proposals = orderByRelevance(createProposalsAtOffset(contents, offset)).findAll {
            it.toString().startsWith('sort') && !it.toString().contains('()') // '{' is not a valid trigger
        }
        /* expecting:
        sort(Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate) : List - DefaultGroovyMethods
        sort(boolean mutate, Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate, Comparator comparator) : List - DefaultGroovyMethods
        */

        for (proposal in proposals) {
            IDocument document = new Document(contents)
            proposal.apply(document, '{' as char, offset)
            def expected = getExpected(contents, proposal)
            Assert.assertEquals(proposal.toString(), expected.normalize(), document.get().normalize())
        }
    }

    @Test
    void testTriggerForMethodProposals3() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)
        groovyPrefs.setValue(GroovyContentAssist.NAMED_ARGUMENTS , true)

        String contents = '((Collection) []).sort'; int offset = contents.length()
        List<ICompletionProposal> proposals = orderByRelevance(createProposalsAtOffset(contents, offset)).findAll {
            it.toString().startsWith('sort') && !it.toString().contains('()') // '{' is not a valid trigger
        }
        /* expecting:
        sort(Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate) : List - DefaultGroovyMethods
        sort(boolean mutate, Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate, Comparator comparator) : List - DefaultGroovyMethods
        */

        for (proposal in proposals) {
            IDocument document = new Document(contents)
            proposal.apply(document, '{' as char, offset)
            def expected = getExpected(contents, proposal)
            Assert.assertEquals(proposal.toString(), expected.normalize(), document.get().normalize())
        }
    }
}
