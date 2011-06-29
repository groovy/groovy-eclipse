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
package org.codehaus.groovy.eclipse.refactoring.test.formatter;

import java.io.File;

import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferencesOnStore;
import org.codehaus.groovy.eclipse.refactoring.test.BaseTestCase;
import org.codehaus.groovy.eclipse.refactoring.test.TestPrefInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;


/**
 * Test Case to test the Groovy Formatter
 *
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class FormatterTestCase extends BaseTestCase {

    public FormatterTestCase(String arg0, File arg1) {
        super(arg0, arg1);
        // Set Method to call for JUnit
        setName("testFormatter");
    }

    public void testFormatter() {
        doTest();
    }

    private void doTest() {
        boolean indentendOnly = false;
        IPreferenceStore pref = null;

        if (properties.get("setPreferences") != null && properties.get("setPreferences").equals("true")) {
            try {
                pref = TestPrefInitializer.initializePreferences(properties);
                String indOnly = properties.get("indentendOnly");
                if (indOnly != null && indOnly.equals("true"))
                    indentendOnly = true;

            } catch (Exception e) {
                e.printStackTrace();
                fail("Initialisation of testproperties failed! " + e.getMessage());
            }
        }

        DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(selection, getDocument(), new FormatterPreferencesOnStore(
                pref), indentendOnly);
        try {
            formatter.format().apply(getDocument());
        } catch (MalformedTreeException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        finalAssert();
    }

    @Override
    public void finalAssert() {
        String expected = getExpected().get();
        String content = getDocument().get();
        assertEquals("Error in File: " + file + " ", expected,content);
    }
}
