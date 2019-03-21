/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.internal;

import java.util.Map;

import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

/**
 * Class to initialize Eclipse preferences in file based tests.
 */
public final class TestPrefInitializer {

    public static IPreferenceStore initializePreferences(Map<String, String> properties, IJavaProject javaProject) {
        IPreferenceStore pref = new PreferenceStore();

        String indentation = properties.get("indentation");
        if (indentation != null) {
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_INDENTATION, indentation);
            if (javaProject != null) {
                javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, indentation);
            }
        }

        String tabsize = properties.get("tabsize");
        // older tests will use tabsize assuming its the same as indentsize, so set both of these!
        if (tabsize != null) {
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE, Integer.parseInt(tabsize));
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_TAB_SIZE, Integer.parseInt(tabsize));

            if (javaProject != null) {
                javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, tabsize);
                javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, tabsize);
            }
        } else {
            if (javaProject != null) {
                javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, null);
                javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, null);
            }
        }

        String indentsize = properties.get("indentsize");
        if (indentsize != null) {
            // GRECLIPSE-1137  This is strange, but it looks like the JDT preferences are switched for spaces mode
            if ("space".equals(indentation)) {
                pref.setValue(PreferenceConstants.GROOVY_FORMATTER_TAB_SIZE, Integer.parseInt(indentsize));
                if (javaProject != null) {
                    javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, indentsize);
                }
            } else {
                pref.setValue(PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE, Integer.parseInt(indentsize));
                if (javaProject != null) {
                    javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, indentsize);
                }
            }
        }

        String multiInd = properties.get("multilineIndentation");
        if (multiInd != null) {
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION, Integer.parseInt(multiInd));
        }

        String bracesStart = properties.get("bracesStart");
        if (bracesStart != null) {
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_BRACES_START, bracesStart);
        }

        String bracesEnd = properties.get("bracesEnd");
        if (bracesEnd != null) {
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_BRACES_END, bracesEnd);
        }

        String maxLineLength = properties.get("maxLineLegth");
        if (maxLineLength != null) {
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_MAX_LINELENGTH, Integer.parseInt(maxLineLength));
        }

        String longListLength = properties.get("longListLength");
        if (longListLength != null) {
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_LONG_LIST_LENGTH, Integer.parseInt(longListLength));
        }

        String indentEmptyLines = properties.get("indentEmptyLines");
        if (indentEmptyLines != null) {
            pref.setValue(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, indentEmptyLines);
        }

        String removeUnnecessarySemicolons = properties.get("removeUnnecessarySemicolons");
        if (removeUnnecessarySemicolons != null) {
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_REMOVE_UNNECESSARY_SEMICOLONS, removeUnnecessarySemicolons);
        }

        return pref;
    }
}
