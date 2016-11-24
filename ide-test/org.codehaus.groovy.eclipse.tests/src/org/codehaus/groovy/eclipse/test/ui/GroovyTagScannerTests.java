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
package org.codehaus.groovy.eclipse.test.ui;

import java.util.Collections;

import junit.framework.TestCase;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.editor.GroovyTagScanner;
import org.codehaus.groovy.eclipse.preferences.PreferenceInitializer;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 */
public class GroovyTagScannerTests extends TestCase {

    GroovyTagScanner scanner;

    @Override
    protected void setUp() throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + getName());

        super.setUp();
        new PreferenceInitializer().initializeDefaultPreferences();
        IColorManager colorManager = GroovyPlugin.getDefault().getTextTools().getColorManager();

        scanner = new GroovyTagScanner(colorManager, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    public void testNoColor() throws Exception {
        tryString("fddsaadsa \"fdfdassdfafasd\"", "black");
    }

    public void testGJDKColor() throws Exception {
        tryString("def", PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR);
    }

    public void testJavaTypeColor() throws Exception {
        tryString("int", PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR);
    }

    public void testJavaKeywordColor() throws Exception {
        tryString("for", PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR);
    }

    public void testStringColor() throws Exception {
        tryString("'fafdsads adsfds'", PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
    }

    //

    private void tryString(String string, String foregroundColorPreference) {
        IDocument doc = new Document(string);
        scanner.setRange(doc, 0, string.length());
        IToken token = scanner.nextToken();

        assertTrue("Token data should be a TextAttribute, but instead is " + token.getData().getClass(), token.getData() instanceof TextAttribute);
        RGB actual = getActualColor(token);
        RGB expected = getExpectedColor(foregroundColorPreference);
        assertEquals(expected, actual);
    }

    private RGB getActualColor(IToken token) {
        Color c = ((TextAttribute) token.getData()).getForeground();
        RGB actual = c == null ? new RGB(0, 0, 0) : c.getRGB();
        return actual;
    }

    private RGB getExpectedColor(String foregroundColorPreference) {
        if (foregroundColorPreference.equals("black")) {
            return new RGB(0, 0, 0);
        }
        return PreferenceConverter.getColor(GroovyPlugin.getDefault().getPreferenceStore(), foregroundColorPreference);
    }
}
