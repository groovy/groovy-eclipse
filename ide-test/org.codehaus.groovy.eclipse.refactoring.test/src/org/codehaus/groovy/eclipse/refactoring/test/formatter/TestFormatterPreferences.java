/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.formatter;

import java.util.Hashtable;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.preferences.FormatterPreferenceInitializer;
import org.codehaus.groovy.eclipse.preferences.FormatterPreferencesPage;
import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences;
import org.codehaus.groovy.eclipse.refactoring.formatter.IFormatterPreferences;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


/**
 * This tets suite is to contain some tests for checking whether
 * the FormatterPreferences are properly taken from the right preferences
 * stores.
 *
 * @author kdvolder
 * @created 2010-05-18
 */
public class TestFormatterPreferences extends EclipseTestCase {

    private static final String TAB_SIZE = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
    private static final String INDENT_SIZE = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
    private static final String TAB_CHAR = DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR;
    private static final String INDENT_EMPTY_LINES = DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES;
    private static final String BRACES_START = PreferenceConstants.GROOVY_FORMATTER_BRACES_START;
    private static final String BRACES_END = PreferenceConstants.GROOVY_FORMATTER_BRACES_END;
    private static final String SMART_PASTE = org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_PASTE;

    private GroovyCompilationUnit gunit;
    private Hashtable saveJavaOptions;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        saveJavaOptions = JavaCore.getOptions();
        if (!hasGroovyNature())
            GroovyRuntime.addGroovyRuntime(testProject.getProject());
        pack = testProject.createPackage("nice.pkg");
        gunit = (GroovyCompilationUnit) pack.createCompilationUnit("Test.groovy", "public class Test { }", true,
                new NullProgressMonitor());
        //Ensure we start tests with default values
        new FormatterPreferenceInitializer().initializeDefaultPreferences();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        JavaCore.setOptions(saveJavaOptions);
        //Ensure we reset preferences changed during tests to default values
        new FormatterPreferenceInitializer().initializeDefaultPreferences();
    }

    /**
     * Braces preferences should come from the preferences store used by the groovy preferences page
     */
    public void testBracesPrefs() throws Exception {
        FormatterPreferencesPage preferencesPage = new FormatterPreferencesPage();
        IPreferenceStore groovyPrefs =preferencesPage.getPreferenceStore();
        assertTrue("Using the wrong preferences store?", groovyPrefs.contains(BRACES_START));
        assertTrue("Using the wrong preferences store?", groovyPrefs.contains(BRACES_END));

        groovyPrefs.setValue(BRACES_START, "next");
        FormatterPreferences formatPrefs = new FormatterPreferences(gunit);
        assertTrue(formatPrefs.getBracesStart() == FormatterPreferences.NEXT_LINE);

        groovyPrefs.setValue(BRACES_START, "same");
        formatPrefs = new FormatterPreferences(gunit);
        assertTrue(formatPrefs.getBracesStart() == FormatterPreferences.SAME_LINE);

        groovyPrefs.setValue(BRACES_END, "next");
        formatPrefs = new FormatterPreferences(gunit);
        assertTrue(formatPrefs.getBracesEnd() == FormatterPreferences.NEXT_LINE);

        groovyPrefs.setValue(BRACES_END, "same");
        formatPrefs = new FormatterPreferences(gunit);
        assertTrue(formatPrefs.getBracesEnd() == FormatterPreferences.SAME_LINE);

    }

    /**
     * Tab related preferences should be inherited from the Java project.
     */
    public void testTabRelatedPrefs() throws Exception {
        IPreferenceStore projectPrefs = new ScopedPreferenceStore(new ProjectScope(testProject.getProject()), JavaCore.PLUGIN_ID);
        assertTrue("Using the wrong preferences store?", projectPrefs.contains(TAB_CHAR));
        assertTrue("Using the wrong preferences store?", projectPrefs.contains(TAB_SIZE));

        projectPrefs.setValue(TAB_CHAR, JavaCore.SPACE);
        IFormatterPreferences formatPrefs =  new FormatterPreferences(gunit);
        assertTrue(formatPrefs.useTabs() == false);

        projectPrefs.setValue(TAB_CHAR, JavaCore.TAB);
        formatPrefs = new FormatterPreferences(gunit);
        assertTrue(formatPrefs.useTabs() == true);

        projectPrefs.setValue(TAB_SIZE, 13);
        formatPrefs = new FormatterPreferences(gunit);
        assertEquals(13, formatPrefs.getTabSize());
        
        projectPrefs.setValue(TAB_CHAR, JavaCore.TAB);
        projectPrefs.setValue(TAB_SIZE, 11);
        projectPrefs.setValue(INDENT_SIZE, 5);
        formatPrefs = new FormatterPreferences(gunit);
        assertEquals(11, formatPrefs.getIndentationSize());
        assertEquals(11, formatPrefs.getTabSize());
        
        projectPrefs.setValue(TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
        projectPrefs.setValue(TAB_SIZE, 11);
        projectPrefs.setValue(INDENT_SIZE, 5);
        formatPrefs = new FormatterPreferences(gunit);
        assertEquals(5, formatPrefs.getIndentationSize());
        assertEquals(11, formatPrefs.getTabSize());
    }
    
    /**
     * Indentation of empty lines preferences should be inherited from the Java project.
     */
    public void testIndentEmptyLinesPrefs() throws Exception {
        IPreferenceStore projectPrefs = new ScopedPreferenceStore(new ProjectScope(testProject.getProject()), JavaCore.PLUGIN_ID);
        assertTrue("Using the wrong preferences store?", projectPrefs.contains(INDENT_EMPTY_LINES));

        projectPrefs.setValue(INDENT_EMPTY_LINES, DefaultCodeFormatterConstants.TRUE);
        IFormatterPreferences formatPrefs =  new FormatterPreferences(gunit);
        assertTrue(formatPrefs.isIndentEmptyLines());

        projectPrefs.setValue(INDENT_EMPTY_LINES, DefaultCodeFormatterConstants.FALSE);
        formatPrefs = new FormatterPreferences(gunit);
        assertFalse(formatPrefs.isIndentEmptyLines());
    }
    

    /**
     * If not defined in the Java project explicitly indent empty lines prefs should be
     * be inherited from JavaCore preferences.
     */
    public void testIndentEmptyLinesFromCore() throws Exception {
        setJavaPreference(INDENT_EMPTY_LINES, DefaultCodeFormatterConstants.TRUE);
        IFormatterPreferences formatPrefs = new FormatterPreferences(gunit);
        assertTrue(formatPrefs.isIndentEmptyLines());
    }
    
    /**
     * If not defined in the Java project explicitly tab related preferences should
     * be inherited from JavaCore preferences.
     */
    public void testTabRelatedPrefsFromCore() throws Exception {
        setJavaPreference(TAB_SIZE, ""+13);
        IFormatterPreferences formatPrefs = new FormatterPreferences(gunit);
        assertEquals(13, formatPrefs.getTabSize());
    }

    /**
     * We don't have a mechanism to automatically pick up changes to Java options
     * through a listener. Creating a new instance of FormatterPreferences should
     * pick up changed preferences however.
     */
    public void testRefreshPrefsFromCore() throws Exception {
        setJavaPreference(TAB_SIZE, ""+13);
        FormatterPreferences formatPrefs = new FormatterPreferences(gunit);
        assertEquals(13, formatPrefs.getTabSize());
        setJavaPreference(TAB_SIZE, ""+7);
        formatPrefs = new FormatterPreferences(gunit);
        assertEquals(7, formatPrefs.getTabSize());
    }

    /**
     * Smart paste option should come from Java UI plugin preferences
     */
    public void testSmartPaste() throws Exception {
        IPreferenceStore uiprefs = JavaPlugin.getDefault().getPreferenceStore();

        boolean org = uiprefs.getBoolean(SMART_PASTE);
        try {
            assertEquals(org, new FormatterPreferences(gunit).isSmartPaste());

            uiprefs.setValue( SMART_PASTE, !org );
            assertEquals(!org, new FormatterPreferences(gunit).isSmartPaste());

            uiprefs.setValue( SMART_PASTE, org );
            assertEquals(org, new FormatterPreferences(gunit).isSmartPaste());
        }
        finally {
            uiprefs.setValue( SMART_PASTE, org );
        }
    }

    protected void setJavaPreference(String name, String value) {
        Hashtable options = JavaCore.getOptions();
        options.put(name, value);
        JavaCore.setOptions(options);
    }
}
