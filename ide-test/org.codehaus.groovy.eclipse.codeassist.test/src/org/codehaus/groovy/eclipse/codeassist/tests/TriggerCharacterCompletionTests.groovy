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
package org.codehaus.groovy.eclipse.codeassist.tests

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized)
final class TriggerCharacterCompletionTests extends CompletionTestSuite {

    @Parameter(0)
    public List options

    @Parameters(name='{0}')
    static Iterable parameters() {
        def prefs = [
            GroovyContentAssist.NAMED_ARGUMENTS,
            GroovyContentAssist.CLOSURE_BRACKETS,
            GroovyContentAssist.CLOSURE_NOPARENS,
        ]

        // produce every combination of all prefs, each paired with a boolean
        prefs.collect { [[it], [true, false]].combinations() }.combinations()
    }

    private static final IPreferenceStore groovyPrefs = GroovyContentAssist.default.preferenceStore

    private static boolean isEnabled(String pref) {
        if (pref.startsWith(GroovyContentAssist.PLUGIN_ID)) {
            return groovyPrefs.getBoolean(pref)
        } else {
            return (JavaCore.getOption(pref) ==~ (JavaCore.ENABLED + '|' + JavaCore.INSERT))
        }
    }

    @Before
    void setUp() {
        options.each { key, val -> groovyPrefs.setValue(key, val) }
    }

    @After
    void tearDown() {
        System.clearProperty(AssistOptions.PROPERTY_SubstringMatch)
    }

    /**
     * Tries each completion proposal for <code>sort</code> using the trigger character <code>'{'</code>.
     */
    @Test
    void testTriggerForMethodProposals() {
        String contents = '[].sort'; int offset = contents.length()
        try {
            setJavaPreference(AssistOptions.OPTION_SubstringMatch, 'disabled')
        } catch (MissingPropertyException e) {
            System.setProperty(AssistOptions.PROPERTY_SubstringMatch, 'false')
        }
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, offset))
        /* expecting:
        sort(Comparator c) : void - List
        sort() : List - DefaultGroovyMethods
        sort(Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate) : List - DefaultGroovyMethods
        sort(boolean mutate, Closure closure) : List - DefaultGroovyMethods
        sort(boolean mutate, Comparator comparator) : List - DefaultGroovyMethods
        */

        for (proposal in proposals) {
            if (proposal.toString().contains('()')) {
                continue // '{' is not a valid trigger
            }

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

            //
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

            IDocument document = new Document(contents)
            proposal.apply(document, '{' as char, offset)

            Assert.assertEquals(proposal.toString(), expected.normalize(), document.get().normalize())
        }
    }
}
