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

import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_BRACES_END;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_BRACES_START;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_INDENT_EMPTY_LINES;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_INDENT_MULTILINE;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_INDENT_SIZE;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_LONG_LIST_LENGTH;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_MAX_LINE_LEN;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_REMOVE_UNNECESSARY_SEMICOLONS;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_SMART_PASTE;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_TAB_SIZE;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.DEFAULT_USE_TABS;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.NEXT_LINE;
import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.SAME_LINE;

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



    ///////////////////////////////////////////////////////////////////////
    //// Default values used for preferences if there are problems getting
    //// proper values from a preferences store.



    // //// preferences cached in fields below //////////

    private boolean useTabs;
    private int tabSize;
    private int indentSize;
    private int indentationMultiline;
    private int bracesStart;
    private int bracesEnd;
    private int maxLineLength;
    private boolean smartPaste;
    private boolean indentEmptyLines;
    private boolean removeUnnecessarySemicolons;
    private int longListLength;

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
    protected void refresh(IPreferenceStore preferences) {
        indentEmptyLines = DEFAULT_INDENT_EMPTY_LINES;
        String pIndentEmpty = preferences.getString(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES);
        if (pIndentEmpty != null) {
            indentEmptyLines = pIndentEmpty.equals(DefaultCodeFormatterConstants.TRUE);
        }

        bracesStart = DEFAULT_BRACES_START;
        String pBracesStart = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_BRACES_START);
        if (pBracesStart != null && pBracesStart.equals(PreferenceConstants.NEXT))
            bracesStart = NEXT_LINE;

        bracesEnd = DEFAULT_BRACES_END;
        String pBracesEnd = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_BRACES_END);
        if (pBracesEnd != null && pBracesEnd.equals("same"))
            bracesEnd = SAME_LINE;

        tabSize = DEFAULT_TAB_SIZE;
        int pTabSize = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_TAB_SIZE);
        if (pTabSize != 0)
            tabSize = pTabSize;

        indentSize = DEFAULT_INDENT_SIZE;
        int pIndentSize = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE);
        if (pIndentSize != 0) {
            indentSize = pIndentSize;
        }

        useTabs = DEFAULT_USE_TABS;
        String pTab = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_INDENTATION);
        if (pTab != null) {
            if (pTab.equals(JavaCore.SPACE)) {
                useTabs = false;
                // GRECLIPSE-1137 strange, but editor appears to use the tab
                // size here for indenting.
                indentSize = tabSize;
            } else if (pTab.equals(JavaCore.TAB)) {
                useTabs = true;
                indentSize = tabSize; // If only tabs are allowed indentSize
                                      // must be tabSize!
            } else if (pTab.equals(DefaultCodeFormatterConstants.MIXED)) {
                useTabs = true;
            }
        }

        indentationMultiline = DEFAULT_INDENT_MULTILINE;
        int pIndeMulti = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION);
        if (pIndeMulti != 0)
            indentationMultiline = pIndeMulti;

        int pMaxLine = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_MAX_LINELENGTH);
        maxLineLength = DEFAULT_MAX_LINE_LEN;
        if (pMaxLine != 0)
            maxLineLength = pMaxLine;

        String pSmartPaste = preferences.getString(org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_PASTE);
        smartPaste = DEFAULT_SMART_PASTE;
        if ("true".equals(pSmartPaste))
            smartPaste = true;
        else if ("false".equals(pSmartPaste))
            smartPaste = false;

        removeUnnecessarySemicolons = DEFAULT_REMOVE_UNNECESSARY_SEMICOLONS;
        String pRemoveUnnecessarySemicolons = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_REMOVE_UNNECESSARY_SEMICOLONS);
        if (pRemoveUnnecessarySemicolons != null) {
            removeUnnecessarySemicolons = pRemoveUnnecessarySemicolons.equals("true");
        }
        longListLength = DEFAULT_LONG_LIST_LENGTH;
        int pLongListLength = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_LONG_LIST_LENGTH);
        if (pLongListLength > 0) {
            longListLength = pLongListLength;
        }
    }

    public int getIndentationMultiline() {
        return indentationMultiline;
    }

    public int getBracesStart() {
        return bracesStart;
    }

    public boolean useTabs() {
        return useTabs;
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

    public int getIndentationSize() {
        return indentSize;
    }

    public int getTabSize() {
        return tabSize;
    }

    public boolean isRemoveUnnecessarySemicolons() {
        return removeUnnecessarySemicolons;
    }

    public int getLongListLength() {
        return longListLength;
    }
}
