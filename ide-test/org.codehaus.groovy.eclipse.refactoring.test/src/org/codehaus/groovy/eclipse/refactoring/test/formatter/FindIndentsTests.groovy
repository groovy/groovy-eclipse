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

import static org.junit.Assert.assertEquals

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferencesOnStore
import org.codehaus.groovy.eclipse.refactoring.test.internal.TestPrefInitializer
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.Document
import org.junit.Before
import org.junit.Test

@CompileStatic
final class FindIndentsTests {

    private DefaultGroovyFormatter formatter

    @Before
    void setUp() {
        Map<String, String> props = [tabsize: '3']
        IPreferenceStore pref = TestPrefInitializer.initializePreferences(props, null)
        formatter = new DefaultGroovyFormatter(new Document(), new FormatterPreferencesOnStore(pref), 0)
    }

    @Test
    void testIndent1() {
        String line = '         f'
        assertEquals("Incorrect indentation found for line '$line'", 3, formatter.computeIndentLevel(line))
    }

    @Test
    void testIndent2() {
        String line = '\t\t\tf'
        assertEquals("Incorrect indentation found for line '$line'", 3, formatter.computeIndentLevel(line))
    }

    @Test
    void testIndent3() {
        String line = '\t\t\t'
        assertEquals("Incorrect indentation found for line '$line'", 3, formatter.computeIndentLevel(line))
    }

    @Test
    void testIndent4() {
        String line = '\t\t\t\n'
        assertEquals("Incorrect indentation found for line '$line'", 3, formatter.computeIndentLevel(line))
    }

    @Test
    void testIndent5() {
        String line = ' \t \t \t f'
        assertEquals("Incorrect indentation found for line '$line'", 3, formatter.computeIndentLevel(line))
    }

    @Test
    void testIndent6() {
        String line = '   \t \t \tf'
        assertEquals("Incorrect indentation found for line '$line'", 4, formatter.computeIndentLevel(line))
    }

    @Test
    void testIndent7() {
        String line = 'f'
        assertEquals("Incorrect indentation found for line '$line'", 0, formatter.computeIndentLevel(line))
    }

    @Test
    void testIndent8() {
        String line = ''
        assertEquals("Incorrect indentation found for line '$line'", 0, formatter.computeIndentLevel(line))
    }

    @Test
    void testIndent9() {
        String line = '\n'
        assertEquals("Incorrect indentation found for line '$line'", 0, formatter.computeIndentLevel(line))
    }
}
