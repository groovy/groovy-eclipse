/*
 * Copyright 2009-2023 the original author or authors.
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

import static org.codehaus.groovy.eclipse.GroovyPlugin.getDefault as getGroovyPlugin

import org.codehaus.groovy.eclipse.editor.GroovyTagScanner
import org.codehaus.groovy.eclipse.preferences.PreferenceConstants
import org.eclipse.jface.preference.PreferenceConverter
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.TextAttribute
import org.eclipse.jface.text.rules.IToken
import org.eclipse.swt.graphics.RGB
import org.junit.Test

final class GroovyTagScannerTests {

    private final GroovyTagScanner scanner = new GroovyTagScanner(groovyPlugin.textTools.colorManager,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST)

    private void tryString(String string, String colorPreference) {
        IDocument doc = new Document(string)
        scanner.setRange(doc, 0, string.length())

        IToken token
        while (!(token = scanner.nextToken()).isEOF()) { if (token.isWhitespace()) continue
            assert token.data instanceof TextAttribute : 'Token data should be a TextAttribute, but instead is ' + token.data.class
            RGB actual = getActualColor(token), expect = getExpectedColor(colorPreference)
            assert actual == expect
        }
    }

    private static RGB getExpectedColor(String colorPreference) {
        PreferenceConverter.getColor(PreferenceConstants.getPreferenceStore(), colorPreference)
    }

    private static RGB getActualColor(IToken token) {
        token.data.foreground?.RGB ?: new RGB(0, 0, 0)
    }

    //--------------------------------------------------------------------------

    @Test
    void testNoColor1() {
        tryString('"foo bar" baz', PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR)
    }

    @Test
    void testNoColor2() {
        tryString('foo "bar baz"', PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1522
    void testNoColor3() {
        scanner.setRange(new Document('foo.@bar.isBaz()'), 0, 16)
        assert getActualColor(scanner.nextToken()) == getExpectedColor(PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR)
        assert getActualColor(scanner.nextToken()) == getExpectedColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR)
        assert getActualColor(scanner.nextToken()) == getExpectedColor(PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR)
        assert getActualColor(scanner.nextToken()) == getExpectedColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR)
        assert getActualColor(scanner.nextToken()) == getExpectedColor(PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR)
        assert getActualColor(scanner.nextToken()) == getExpectedColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR)
    }

    @Test
    void testNoColor4() {
        tryString('var foo', PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR)
    }

    @Test
    void testKeyword1() {
        tryString('def', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR)
    }

    @Test
    void testKeyword2() {
        tryString('int', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR)
    }

    @Test
    void testKeyword3() {
        tryString('void', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR)
    }

    @Test
    void testKeyword4() {
        tryString('as', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR)
    }

    @Test
    void testKeyword5() {
        tryString('if', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR)
    }

    @Test
    void testKeyword6() {
        tryString('in', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR)
    }

    @Test
    void testKeyword7() {
        tryString('interface', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR)
    }

    @Test
    void testKeyword8() {
        tryString('@interface', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR)
    }

    @Test
    void testKeyword9() {
        tryString('assert', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ASSERT_COLOR)
    }

    @Test
    void testKeyword10() {
        tryString('return', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR)
    }

    @Test
    void testAnnotation1() {
        tryString('@Deprecated', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR)
    }

    @Test
    void testAnnotation2() {
        tryString('@java.lang.Deprecated', PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR)
    }
}
