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
package org.codehaus.groovy.eclipse.test.ui

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*
import static org.eclipse.jdt.ui.PreferenceConstants.EDITOR_CLOSE_BRACES

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.preference.IPreferenceStore
import org.junit.Assert
import org.junit.Before
import org.junit.Test

final class GroovyAutoIndenterTests extends GroovyEditorTestCase {

    @Before
    void setUp() {
        setJavaPreference(EDITOR_CLOSE_BRACES, IPreferenceStore.TRUE)
        setJavaPreference(FORMATTER_TAB_CHAR, JavaCore.TAB)
        setJavaPreference(FORMATTER_TAB_SIZE, '4')
    }

    /**
     * A simple test just to see whether our scaffolding for testing the editor works.
     */
    @Test
    void testScaffolding() {
        makeEditor('<***>')
        send('a')
        Assert.assertEquals('a', getText())
        assertEditorContents('a<***>')
    }

    @Test
    void test1() {
        makeEditor('<***>')
        send('class Foo {\n')
        assertEditorContents('class Foo {\n\t<***>\n}')
    }

    @Test
    void test2() {
        makeEditor(
                'class Foo {\n' +
                '\tdef foo() {<***>\n' +
                '}\n\n')
        send('\n')
        assertEditorContents(
                'class Foo {\n' +
                '\tdef foo() {\n' +
                '\t\t<***>\n' +
                '\t}\n' +
                '}\n\n')
    }

    @Test
    void test3() {
        makeEditor(
                'class Foo {\n' +
                '\tdef foo() {\n' +
                '\t\tif (a<b) { \n' +
                '\t\t\tblah()\n' +
                '\t\t}<***>\n' +
                '\t}\n' +
                '}\n\n')
        send('\n')
        assertEditorContents(
                'class Foo {\n' +
                '\tdef foo() {\n' +
                '\t\tif (a<b) { \n' +
                '\t\t\tblah()\n' +
                '\t\t}\n' +
                '\t\t<***>\n' +
                '\t}\n' +
                '}\n\n')
    }

    @Test
    void testGRE631() {
        makeEditor(
                'class Foo {\n' +
                '\tdef foo () {\n' +
                '\t\tdef foo = [\"\"]<***>\n' +
                '\t}\n' +
                '}')
        send('\n')
        assertEditorContents(
                'class Foo {\n' +
                '\tdef foo () {\n' +
                '\t\tdef foo = [\"\"]\n' +
                '\t\t<***>\n' +
                '\t}\n' +
                '}')
        send('return []\n')
        assertEditorContents(
                'class Foo {\n' +
                '\tdef foo () {\n' +
                '\t\tdef foo = [\"\"]\n' +
                '\t\treturn []\n' +
                '\t\t<***>\n' +
                '\t}\n' +
                '}')
    }

    /**
     * Check whether we are picking up on changed tab/space preferences set
     * for the Java Editor.
     */
    @Test
    void testSpaces() {
        setJavaPreference(FORMATTER_TAB_CHAR, JavaCore.SPACE)
        makeEditor(
                'class Foo {\n' +
                '\tdef foo() {<***>\n' +
                '}')
        send('\n')
        assertEditorContents(
                'class Foo {\n' +
                '\tdef foo() {\n' +
                '        <***>\n' +
                '    }\n' +
                '}')
    }

    /**
     * GRE_751: Pasting text into a multiline string should not perform any
     * transformations.
     */
    @Test
    void testPasteInMultiLineString() {
        String initial = 'class Foo {\n' +
                '\tdef command = \"\"\"<***>\"\"\"\n' +
                '}'
        makeEditor(initial)
        String pasteString = 'A bunch of \n \t\tmore here. Not\t\t\nand done!'
        sendPaste(pasteString)
        assertEditorContents(initial.replace(CARET, pasteString + CARET))
    }

    /**
     * Check whether we are also picking up on changed tab/space preferences
     * even if they are change happens after the editor was already opened.
     */
    @Test
    void testSpacesOptionSetAfterOpen() {
        makeEditor(
                'class Foo {\n' +
                '\tdef foo() {<***>\n' +
                '}')
        setJavaPreference(FORMATTER_TAB_CHAR, JavaCore.SPACE)
        send('\n')
        assertEditorContents(
                'class Foo {\n' +
                '\tdef foo() {\n' +
                '        <***>\n' +
                '    }\n' +
                '}')
    }

    /**
     * Check whether autoindentor works correct for mixed tab/spaces mode.
     */
    @Test
    void testMixedTabsAndSpaces() {
        makeEditor(
                'class Foo {\n' +
                '    def foo() {\n' +
                '        def bar {<***>\n' +
                '        }\n' +
                '    }\n'+
                '}')
        setJavaPreference(FORMATTER_INDENTATION_SIZE, '4')
        setJavaPreference(FORMATTER_TAB_CHAR, MIXED)
        setJavaPreference(FORMATTER_TAB_SIZE, '8')
        send('\n')
        assertEditorContents(
                'class Foo {\n' +
                '    def foo() {\n' +
                '        def bar {\n' +
                '\t    <***>\n' +
                '        }\n' +
                '    }\n'+
                '}')
    }

    /**
     * Similar to above, but also check whether it counts the tabs on previous lines correctly.
     */
    @Test
    void testMixedTabsAndSpaces2() {
        makeEditor(
                'class Foo {\n' +
                '    def foo() {\n' +
                '\tdef bar {<***>\n' +
                '\t}\n' +
                '    }\n'+
                '}')
        setJavaPreference(FORMATTER_INDENTATION_SIZE, '4')
        setJavaPreference(FORMATTER_TAB_CHAR, MIXED)
        setJavaPreference(FORMATTER_TAB_SIZE, '8')
        send('\n')
        assertEditorContents(
                'class Foo {\n' +
                '    def foo() {\n' +
                '\tdef bar {\n' +
                '\t    <***>\n' +
                '\t}\n' +
                '    }\n'+
                '}')
    }
}
