/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.test.ui;

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.editor.GroovyTagScanner;
import org.codehaus.groovy.eclipse.editor.GroovyTextTools;
import org.codehaus.groovy.eclipse.preferences.PreferenceInitializer;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
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
 *
 * Tests for the GroovyTagScanner
 */
public class GroovyTagScannerTests extends TestCase {
    
    private final static String BLACK = "black";

    GroovyTagScanner scanner;
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        new PreferenceInitializer().initializeDefaultPreferences();
        GroovyTextTools textTools= GroovyPlugin.getDefault().getTextTools();
        IColorManager colorManager = textTools.getColorManager();
        scanner = new GroovyTagScanner(colorManager);
    }
    
    
    public void testNoColor() throws Exception {
        tryString("fddsaadsa \"fdfdassdfafasd\"", BLACK);
    }
    public void testGJDKColor() throws Exception {
        tryString("def", PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR);
    }
    public void testJavaColor() throws Exception {
        tryString("boolean", PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR);
    }
    public void testStringColor() throws Exception {
        tryString("/fafdsads adsfds/", PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
    }

    private void tryString(String string, String foregroundColorPreference) {
        IDocument doc = new Document(string);
        scanner.setRange(doc, 0, string.length());
        IToken token = scanner.nextToken();
        
        assertTrue("Token data should be a TextAttribute, but instead is " + token.getData().getClass(), token.getData() instanceof TextAttribute);
        RGB actual = getActualColor(token);
        RGB expected = getExpectedColor(foregroundColorPreference);
        assertEquals(expected, actual);
        
    }

    private RGB getExpectedColor(String foregroundColorPreference) {
        if (foregroundColorPreference.equals(BLACK)) {
            return new RGB(0,0,0);
        }
        return PreferenceConverter.getColor(getPreferenceStore(), foregroundColorPreference);
    }

    private RGB getActualColor(IToken token) {
        Color c = ((TextAttribute) token.getData()).getForeground();
        RGB actual = c == null ? new RGB(0,0,0) : c.getRGB();
        return actual;
    }
    
    private IPreferenceStore getPreferenceStore() {
        return GroovyPlugin.getDefault().getPreferenceStore();
    }

}
