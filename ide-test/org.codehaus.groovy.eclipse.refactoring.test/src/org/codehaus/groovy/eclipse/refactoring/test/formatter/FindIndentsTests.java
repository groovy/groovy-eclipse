/*
 * Copyright 2003-2009 the original author or authors.
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

import java.util.HashMap;

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferencesOnStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;

import core.TestPrefInitializer;

/**
 * @author Andrew Eisenberg
 * @created Oct 15, 2009
 *
 */
public class FindIndentsTests extends TestCase {

    DefaultGroovyFormatter formatter;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("tabsize", "3");
        IPreferenceStore pref = TestPrefInitializer.initializePreferences(props);
        formatter = new DefaultGroovyFormatter(new Document(), new FormatterPreferencesOnStore(pref), 0);
    }

    public void testIndent1() throws Exception {
        String line = "         f";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 3, formatter.computeIndentLevel(line));
    }

    public void testIndent2() throws Exception {
        String line = "\t\t\tf";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 3, formatter.computeIndentLevel(line));
    }

    public void testIndent3() throws Exception {
        String line = "\t\t\t";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 3, formatter.computeIndentLevel(line));
    }

    public void testIndent4() throws Exception {
        String line = "\t\t\t\n";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 3, formatter.computeIndentLevel(line));
    }

    public void testIndent5() throws Exception {
        String line = " \t \t \t f";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 3, formatter.computeIndentLevel(line));
    }

    public void testIndent6() throws Exception {
        String line = "   \t \t \tf";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 4, formatter.computeIndentLevel(line));
    }
    public void testIndent7() throws Exception {
        String line = "f";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 0, formatter.computeIndentLevel(line));
    }
    public void testIndent8() throws Exception {
        String line = "";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 0, formatter.computeIndentLevel(line));
    }
    public void testIndent9() throws Exception {
        String line = "\n";
        assertEquals("Incorrect indentation found for line \"" + line + "\"", 0, formatter.computeIndentLevel(line));
    }
}
