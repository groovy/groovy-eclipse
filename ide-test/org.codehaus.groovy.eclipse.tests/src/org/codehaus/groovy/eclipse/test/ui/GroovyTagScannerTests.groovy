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

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants
import org.codehaus.groovy.eclipse.editor.GroovyTagScanner
import org.eclipse.jface.preference.PreferenceConverter
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.TextAttribute
import org.eclipse.jface.text.rules.IToken
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.RGB
import org.junit.Test

final class GroovyTagScannerTests {

    private final GroovyTagScanner scanner = new GroovyTagScanner(GroovyPlugin.default.textTools.colorManager,
                Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST)

    @Test
    void testNoColor() {
        tryString('fddsaadsa "fdfdassdfafasd"', 'black')
    }

    @Test
    void testGJDKColor() {
        tryString('def', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR)
    }

    @Test
    void testJavaTypeColor() {
        tryString('int', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR)
    }

    @Test
    void testJavaKeywordColor() {
        tryString('for', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR)
    }

    @Test
    void testStringColor() {
        tryString('\'fafdsads adsfds\'', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR)
    }

    //

    private void tryString(String string, String foregroundColorPreference) {
        IDocument doc = new Document(string)
        scanner.setRange(doc, 0, string.length())
        IToken token = scanner.nextToken()

        assert token.data instanceof TextAttribute : 'Token data should be a TextAttribute, but instead is ' + token.data.class
        RGB actual = getActualColor(token)
        RGB expected = getExpectedColor(foregroundColorPreference)
        assert actual == expected
    }

    private RGB getActualColor(IToken token) {
        Color c = token.data.foreground
        (c != null ? c.getRGB() : new RGB(0, 0, 0))
    }

    private RGB getExpectedColor(String foregroundColorPreference) {
        if (foregroundColorPreference.equals('black')) {
            return new RGB(0, 0, 0)
        }
        return PreferenceConverter.getColor(GroovyPlugin.default.preferenceStore, foregroundColorPreference)
    }
}
