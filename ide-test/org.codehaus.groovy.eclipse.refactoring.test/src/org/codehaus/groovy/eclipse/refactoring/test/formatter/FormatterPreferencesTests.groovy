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
package org.codehaus.groovy.eclipse.refactoring.test.formatter

import static org.codehaus.groovy.eclipse.refactoring.PreferenceConstants.*
import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*
import static org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_PASTE

import org.codehaus.groovy.eclipse.preferences.FormatterPreferenceInitializer
import org.codehaus.groovy.eclipse.preferences.FormatterPreferencesPage
import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences
import org.codehaus.groovy.eclipse.refactoring.formatter.IFormatterPreferences
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.resources.ProjectScope
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.ui.JavaPlugin
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.ui.preferences.ScopedPreferenceStore
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests if the FormatterPreferences are taken from the right preference stores.
 */
final class FormatterPreferencesTests extends GroovyEclipseTestSuite {

    private GroovyCompilationUnit gunit

    @Before
    void setUp() {
        gunit = addGroovySource('class Test { }', nextUnitName(), 'nice.pkg')
    }

    @After
    void tearDown() {
        new ScopedPreferenceStore(new ProjectScope(gunit.javaProject.project), JavaCore.PLUGIN_ID).with {
            storePreferences.@properties.keys().each { k ->
                if (!isDefault(k) && k =~ /^org.eclipse.jdt.core.formatter./) {
                    println "Resetting '$k' to its default"
                    setToDefault(k)
                }
            }
        }
        new FormatterPreferenceInitializer().initializeDefaultPreferences()
        JavaCore.setOptions(JavaCore.getDefaultOptions())
    }

    /**
     * Braces preferences should come from the preferences store used by the groovy preferences page
     */
    @Test
    void testBracesPrefs() {
        FormatterPreferencesPage preferencesPage = new FormatterPreferencesPage()
        IPreferenceStore groovyPrefs = preferencesPage.preferenceStore
        assert groovyPrefs.contains(GROOVY_FORMATTER_BRACES_START) : 'Using the wrong preferences store?'
        assert groovyPrefs.contains(GROOVY_FORMATTER_BRACES_END) : 'Using the wrong preferences store?'

        groovyPrefs.setValue(GROOVY_FORMATTER_BRACES_START, PreferenceConstants.NEXT)
        FormatterPreferences formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.bracesStart == PreferenceConstants.NEXT_LINE

        groovyPrefs.setValue(GROOVY_FORMATTER_BRACES_START, PreferenceConstants.SAME)
        formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.bracesStart == PreferenceConstants.SAME_LINE

        groovyPrefs.setValue(GROOVY_FORMATTER_BRACES_END, PreferenceConstants.NEXT)
        formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.bracesEnd == PreferenceConstants.NEXT_LINE

        groovyPrefs.setValue(GROOVY_FORMATTER_BRACES_END, PreferenceConstants.SAME)
        formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.bracesEnd == PreferenceConstants.SAME_LINE
    }

    /**
     * Tab related preferences should be inherited from the Java project.
     */
    @Test
    void testTabRelatedPrefs() {
        IPreferenceStore projectPrefs = new ScopedPreferenceStore(new ProjectScope(gunit.javaProject.project), JavaCore.PLUGIN_ID)
        assert projectPrefs.contains(FORMATTER_TAB_CHAR) : 'Using the wrong preferences store?'
        assert projectPrefs.contains(FORMATTER_TAB_SIZE) : 'Using the wrong preferences store?'

        projectPrefs.setValue(FORMATTER_TAB_CHAR, JavaCore.SPACE)
        IFormatterPreferences formatPrefs = new FormatterPreferences(gunit)
        assert !formatPrefs.useTabs()

        projectPrefs.setValue(FORMATTER_TAB_CHAR, JavaCore.TAB)
        formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.useTabs()

        projectPrefs.setValue(FORMATTER_TAB_SIZE, 13)
        formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.tabSize == 13

        projectPrefs.setValue(FORMATTER_TAB_CHAR, JavaCore.TAB)
        projectPrefs.setValue(FORMATTER_TAB_SIZE, 11)
        projectPrefs.setValue(FORMATTER_INDENTATION_SIZE, 5)
        formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.indentationSize == 11
        assert formatPrefs.tabSize == 11

        projectPrefs.setValue(FORMATTER_TAB_CHAR, MIXED)
        projectPrefs.setValue(FORMATTER_TAB_SIZE, 11)
        projectPrefs.setValue(FORMATTER_INDENTATION_SIZE, 5)
        formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.indentationSize == 5
        assert formatPrefs.tabSize == 11
    }

    /**
     * Indentation of empty lines preferences should be inherited from the Java project.
     */
    @Test
    void testIndentEmptyLinesPrefs() {
        IPreferenceStore projectPrefs = new ScopedPreferenceStore(new ProjectScope(gunit.javaProject.project), JavaCore.PLUGIN_ID)
        assert projectPrefs.contains(FORMATTER_INDENT_EMPTY_LINES) : 'Using the wrong preferences store?'

        projectPrefs.setValue(FORMATTER_INDENT_EMPTY_LINES, TRUE)
        IFormatterPreferences formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.isIndentEmptyLines()

        projectPrefs.setValue(FORMATTER_INDENT_EMPTY_LINES, FALSE)
        formatPrefs = new FormatterPreferences(gunit)
        assert !formatPrefs.isIndentEmptyLines()
    }

    /**
     * If not defined in the Java project explicitly indent empty lines prefs should be
     * be inherited from JavaCore preferences.
     */
    @Test
    void testIndentEmptyLinesFromCore() {
        setJavaPreference(FORMATTER_INDENT_EMPTY_LINES, TRUE)
        IFormatterPreferences formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.isIndentEmptyLines()
    }

    /**
     * If not defined in the Java project explicitly tab related preferences should
     * be inherited from JavaCore preferences.
     */
    @Test
    void testTabRelatedPrefsFromCore() {
        setJavaPreference(FORMATTER_TAB_SIZE, 13.toString())
        IFormatterPreferences formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.tabSize == 13
    }

    /**
     * We don't have a mechanism to automatically pick up changes to Java options
     * through a listener. Creating a new instance of FormatterPreferences should
     * pick up changed preferences however.
     */
    @Test
    void testRefreshPrefsFromCore() {
        setJavaPreference(FORMATTER_TAB_SIZE, 13.toString())
        FormatterPreferences formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.tabSize == 13

        setJavaPreference(FORMATTER_TAB_SIZE, 7.toString())
        formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.tabSize == 7
    }

    /**
     * Smart paste option should come from Java UI plugin preferences
     */
    @Test
    void testSmartPaste() {
        def uiprefs = JavaPlugin.default.preferenceStore
        boolean orig = uiprefs.getBoolean(EDITOR_SMART_PASTE)
        try {
            assert new FormatterPreferences(gunit).isSmartPaste() == orig

            uiprefs.setValue(EDITOR_SMART_PASTE, !orig)
            assert new FormatterPreferences(gunit).isSmartPaste() != orig

            uiprefs.setValue(EDITOR_SMART_PASTE, orig)
            assert new FormatterPreferences(gunit).isSmartPaste() == orig
        } finally {
            uiprefs.setValue(EDITOR_SMART_PASTE, orig)
        }
    }

    /**
     * Semicolon preferences should come from the preferences store used by the groovy preferences page
     */
    @Test
    void testSemicolonPrefs() {
        FormatterPreferencesPage preferencesPage = new FormatterPreferencesPage()
        IPreferenceStore groovyPrefs = preferencesPage.preferenceStore
        assert groovyPrefs.contains(GROOVY_FORMATTER_REMOVE_UNNECESSARY_SEMICOLONS) : 'Using the wrong preferences store?'

        groovyPrefs.setValue(GROOVY_FORMATTER_REMOVE_UNNECESSARY_SEMICOLONS, true)
        FormatterPreferences formatPrefs = new FormatterPreferences(gunit)
        assert formatPrefs.isRemoveUnnecessarySemicolons()

        groovyPrefs.setValue(GROOVY_FORMATTER_REMOVE_UNNECESSARY_SEMICOLONS, false)
        formatPrefs = new FormatterPreferences(gunit)
        assert !formatPrefs.isRemoveUnnecessarySemicolons()
    }
}
