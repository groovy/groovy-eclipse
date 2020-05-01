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

import org.codehaus.groovy.eclipse.editor.GroovyEditor
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.swt.events.VerifyEvent
import org.eclipse.swt.widgets.Event
import org.junit.Assert
import org.junit.Test

/**
 * Tests {@link GroovyEditor.GroovyBracketInserter}.
 */
final class BracketInserterTests extends GroovyEclipseTestSuite {

    private void assertClosing(String initialDoc, String expectedDoc, String inserted, int location) {
        // add extra spaces since the String rule fails for end of file
        initialDoc += '\n'
        expectedDoc += '\n'

        def unit = addGroovySource(initialDoc, nextUnitName())
        def editor = (GroovyEditor) openInEditor(unit)

        assert inserted.length() == 1
        def ve = new VerifyEvent(new Event(
            widget: editor.viewer.textWidget,
            character: inserted as char,
            doit: true
        ))
        editor.viewer.setSelectedRange(location, 0)
        editor.groovyBracketInserter.verifyKey(ve)
        if (ve.doit) {
            editor.viewer.document.replace(location, 0, inserted)
        }
        String actual = editor.viewer.document.get()
        Assert.assertEquals("Invalid bracket insertion.\nInserted char: '$inserted' at location $location", expectedDoc, actual)
    }

    @Test
    void testInsertDQuote1() {
        assertClosing('', '""', '"', 0)
    }

    @Test
    void testInsertDQuote2() {
        assertClosing('"', '""', '"', 1)
    }

    @Test
    void testInsertDQuote3() {
        assertClosing('""', '""""""', '"', 2)
    }

    @Test
    void testInsertDQuote4() {
        assertClosing('"""', '""""""', '"', 3)
    }

    @Test
    void testInsertDQuote5() {
        assertClosing('assert ', 'assert ""', '"', 7)
    }

    @Test
    void testInsertDQuote6() {
        assertClosing('assert "', 'assert ""', '"', 8)
    }

    @Test
    void testInsertDQuote7() {
        assertClosing(/''/, /'"'/, /"/, 1)
    }

    @Test
    void testInsertSQuote1() {
        assertClosing('', '\'\'', '\'', 0)
    }

    @Test
    void testInsertSQuote2() {
        assertClosing('\'', '\'\'', '\'', 1)
    }

    @Test
    void testInsertSQuote3() {
        assertClosing('\'\'', '\'\'\'\'\'\'', '\'', 2)
    }

    @Test
    void testInsertSQuote4() {
        assertClosing('\'\'\'', '\'\'\'\'\'\'', '\'', 3)
    }

    @Test
    void testInsertParen() {
        assertClosing('', '()', '(', 0)
    }

    @Test
    void testInsertSquare() {
        assertClosing('', '[]', '[', 0)
    }

    @Test
    void testInsertAngle() {
        assertClosing('', '<' + '>', '<', 0)
    }

    @Test
    void testInsertBraces1() {
        assertClosing('"$"', '"${}"', '{', 2)
    }

    @Test
    void testInsertBraces2() {
        assertClosing('"""$"""', '"""${}"""', '{', 4)
    }

    @Test
    void testInsertBraces3() {
        assertClosing('$', '${', '{', 1)
    }

    @Test
    void testInsertBraces4() {
        assertClosing('"""\n$"""', '"""\n${}"""', '{', 5)
    }
}
