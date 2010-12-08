/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

/**
 * An implementation of IFormatterPreferences that is backed by an
 * {@link IPreferenceStore}. Warning: I have not found a simple way to
 * get a "correct" preferences store that reflects project specific
 * preferences as well as falls back on global settings when they are
 * not set.
 * <p>
 * The correct way to create an IFormatterPreferences is to instantiate
 * the class FormatterPreferences with an IJavaProject or ICompilationUnit.
 * <p>
 * Directly instantiating this class is only advisable in a testing context
 * where you explicitly want to create a preferences store with testing
 * preferences.
 *
 * @author Mike Klenk mklenk@hsr.ch
 * @author Kris De Volder <kris.de.volder@gmail.com>
 */
public class FormatterPreferencesOnStore implements IFormatterPreferences {

    public static final int SAME_LINE = 0;
    public static final int NEXT_LINE = 1;

    ///////////////////////////////////////////////////////////////////////
    //// Default values used for preferences if there are problems getting
    //// proper values from a preferences store.

    private static final int DEFAULT_MAX_LINE_LEN = 80;
    private static final int DEFAULT_BRACES_START = SAME_LINE;
    private static final int DEFAULT_BRACES_END = NEXT_LINE;
    private static final boolean DEFAULT_USE_TABS = true;
    private static final int DEFAULT_TAB_SIZE = 4;

    private static final int DEFAULT_INDENT_MULTILINE = 2;

    private static final boolean DEFAULT_SMART_PASTE = true;

    private static final boolean DEFAULT_INDENT_EMPTY_LINES = false;

    // //// preferences cached in fields below ////////////

    private boolean useTabs;

    private int tabSize;

    private int indentationMultiline;

    private int bracesStart;

    private int bracesEnd;

    private int maxLineLength;

    private boolean smartPaste;

    private boolean indentEmptyLines;

	////////////////////////////////////////////////////

    /**
     * Create Formatter Preferences for a given GroovyCompilationUnit.
     * The formatter preferences object will cache preferences it fetches
     * from the preferences store. This cache is never updated so you must
     * create a new instance to update preferences.
     */
    public FormatterPreferencesOnStore(IPreferenceStore preferences) {
        if (preferences == null)
            preferences = new PreferenceStore();
        refresh(preferences);
	}

    /**
     * Refresh cached values from the preferences store.
     *
     * @param preferences
     */
    private void refresh(IPreferenceStore preferences) {

        indentEmptyLines = DEFAULT_INDENT_EMPTY_LINES;
        String pIndentEmpty = preferences.getString(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES);
        if (pIndentEmpty != null) {
            indentEmptyLines = pIndentEmpty.equals(DefaultCodeFormatterConstants.TRUE);
        }

        bracesStart = DEFAULT_BRACES_START;
        String pBracesStart = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_BRACES_START);
        if (pBracesStart != null && pBracesStart.equals("next"))
            bracesStart = NEXT_LINE;

        this.bracesEnd = DEFAULT_BRACES_END;
        String pBracesEnd = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_BRACES_END);
        if (pBracesEnd != null && pBracesEnd.equals("same"))
            bracesEnd = SAME_LINE;

        this.useTabs = DEFAULT_USE_TABS;
        String pTab = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_INDENTATION);
        if (pTab != null && pTab.equals(JavaCore.SPACE))
            useTabs = false;
        if (pTab != null && pTab.equals(JavaCore.TAB))
            useTabs = true;

        this.tabSize = DEFAULT_TAB_SIZE;
        int pTabSize = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE);
        if (pTabSize != 0)
            tabSize = pTabSize;

        this.indentationMultiline = DEFAULT_INDENT_MULTILINE;
        int pIndeMulti = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION);
        if (pIndeMulti != 0)
            indentationMultiline = pIndeMulti;

        int pMaxLine = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_MAX_LINELENGTH);
        this.maxLineLength = DEFAULT_MAX_LINE_LEN;
        if (pMaxLine != 0)
            maxLineLength = pMaxLine;

        String pSmartPaste = preferences.getString(org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_PASTE);
        this.smartPaste = DEFAULT_SMART_PASTE;
        if ("true".equals(pSmartPaste))
            smartPaste = true;
        else if ("false".equals(pSmartPaste))
            smartPaste = false;
    }

    public int getIndentationMultiline() {
        return indentationMultiline;
    }

    public int getBracesStart() {
        return bracesStart;
    }

    public boolean isUseTabs() {
        return useTabs;
    }

    public int getTabSize() {
        return tabSize;
    }

    public int getBracesEnd() {
        return bracesEnd;
    }

    public int getMaxLineLength() {
        return maxLineLength;
    }

    public boolean isSmartPaste() {
        return smartPaste;
    }

    public boolean isIndentEmptyLines() {
        return indentEmptyLines;
    }

}
