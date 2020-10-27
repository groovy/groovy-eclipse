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
package org.codehaus.groovy.eclipse.test.ui

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*
import static org.eclipse.jdt.ui.PreferenceConstants.EDITOR_CLOSE_BRACES

import org.eclipse.jdt.core.JavaCore
import org.junit.Before
import org.junit.Test

final class GroovyAutoIndenterTests extends GroovyEditorTestSuite {

    @Before
    void setUp() {
        setJavaPreference(EDITOR_CLOSE_BRACES, true)
        setJavaPreference(FORMATTER_TAB_CHAR, JavaCore.TAB)
        setJavaPreference(FORMATTER_TAB_SIZE, 4)
    }

    /**
     * A simple test just to see whether our scaffolding for testing the editor works.
     */
    @Test
    void testScaffolding() {
        makeEditor("${CARET}")
        send('a')
        assert this.text == 'a'
        assertEditorContents("a${CARET}")
    }

    @Test
    void test1() {
        makeEditor("${CARET}")
        send('class Foo {\n')

        assertEditorContents """\
            class Foo {
            \t${CARET}
            }""".stripIndent()
    }

    @Test
    void test2() {
        makeEditor """\
            class Foo {
            \tdef foo() {${CARET}
            }
            """.stripIndent()

        send('\n')

        assertEditorContents """\
            class Foo {
            \tdef foo() {
            \t\t${CARET}
            \t}
            }
            """.stripIndent()
    }

    @Test
    void test3() {
        makeEditor """\
            class Foo {
            \tdef foo() {
            \t\tif (a<b) {
            \t\t\tblah()
            \t\t}${CARET}
            \t}
            }
            """.stripIndent()

        send('\n')

        assertEditorContents """\
            class Foo {
            \tdef foo() {
            \t\tif (a<b) {
            \t\t\tblah()
            \t\t}
            \t\t${CARET}
            \t}
            }
            """.stripIndent()
    }

    @Test
    void testGRE631() {
        makeEditor """\
            class Foo {
            \tdef foo () {
            \t\tdef foo = ['']${CARET}
            \t}
            }
            """.stripIndent()

        send('\n')

        assertEditorContents """\
            class Foo {
            \tdef foo () {
            \t\tdef foo = ['']
            \t\t${CARET}
            \t}
            }
            """.stripIndent()

        send('return []\n')

        assertEditorContents """\
            class Foo {
            \tdef foo () {
            \t\tdef foo = ['']
            \t\treturn []
            \t\t${CARET}
            \t}
            }
            """.stripIndent()
    }

    /**
     * Check whether we are picking up on changed tab/space preferences set
     * for the Java Editor.
     */
    @Test
    void testSpaces() {
        setJavaPreference(FORMATTER_TAB_CHAR, JavaCore.SPACE)

        makeEditor """\
            class Foo {
            \tdef foo() {${CARET}
            }
            """.stripIndent()

        send('\n')

        assertEditorContents """\
            class Foo {
            \tdef foo() {
                    ${CARET}
                }
            }
            """.stripIndent()
    }

    /**
     * Check whether we are also picking up on changed tab/space preferences
     * even if they are change happens after the editor was already opened.
     */
    @Test
    void testSpacesOptionSetAfterOpen() {
        makeEditor"""\
            class Foo {
            \tdef foo() {${CARET}
            }
            """.stripIndent()

        setJavaPreference(FORMATTER_TAB_CHAR, JavaCore.SPACE)
        send('\n')

        assertEditorContents """\
            class Foo {
            \tdef foo() {
                    ${CARET}
                }
            }
            """.stripIndent()
    }

    /**
     * Checks that auto-indenter works correct for mixed tab/spaces mode.
     */
    @Test
    void testMixedTabsAndSpaces() {
        makeEditor """\
            class Foo {
                def foo() {
                    def bar = {${CARET}
                    }
                }
            }
            """.stripIndent()

        setJavaPreference(FORMATTER_INDENTATION_SIZE, 4)
        setJavaPreference(FORMATTER_TAB_CHAR, MIXED)
        setJavaPreference(FORMATTER_TAB_SIZE, 8)
        send('\n')

        assertEditorContents """\
            class Foo {
                def foo() {
                    def bar = {
            \t    ${CARET}
                    }
                }
            }
            """.stripIndent()
    }

    /**
     * Similar to above, but also check whether it counts the tabs on previous lines correctly.
     */
    @Test
    void testMixedTabsAndSpaces2() {
        makeEditor """\
            class Foo {
                def foo() {
            \tdef bar {${CARET}
            \t}
                }
            }
            """.stripIndent()

        setJavaPreference(FORMATTER_INDENTATION_SIZE, 4)
        setJavaPreference(FORMATTER_TAB_CHAR, MIXED)
        setJavaPreference(FORMATTER_TAB_SIZE, 8)
        send('\n')

        assertEditorContents """\
            class Foo {
                def foo() {
            \tdef bar {
            \t    ${CARET}
            \t}
                }
            }
            """.stripIndent()
    }

    /**
     * GRE_751: Pasting text into a multiline string should not perform any transformations.
     */
    @Test
    void testPasteInMultiLineString() {
        String initial = '''\
            class Foo {
            \tdef command = """<***>"""
            }
            '''.stripIndent()
        makeEditor(initial)
        String pasteString = 'A bunch of \n \t\tmore here. Not\t\t\nand done!'
        sendPaste(pasteString)
        assertEditorContents(initial.replace(CARET, pasteString + CARET))
    }
}
