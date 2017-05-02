/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.formatter

import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferencesOnStore
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSpec
import org.codehaus.groovy.eclipse.refactoring.test.internal.TestPrefInitializer
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.jface.preference.IPreferenceStore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized)
final class GroovyFormatterTests {

    @Parameters
    static Iterable<Object[]> params() {
        URL url = Platform.getBundle('org.codehaus.groovy.eclipse.refactoring.test').getEntry('/resources/Formatter')
        new File(FileLocator.toFileURL(url).getFile()).listFiles({ File dir, String item ->
            item ==~ /Formatter_Test_.*/
        } as FilenameFilter).collect {
            [it, it.name - ~/.txt/] as Object[]
        }
    }

    GroovyFormatterTests(File file, String name) {
        spec = new RefactoringTestSpec(file)

        println '----------------------------------------'
        println "Starting: $name"
    }

    private RefactoringTestSpec spec

    @Test
    void test() {
        boolean indentendOnly = false
        IPreferenceStore pref = null
        if (spec.properties['setPreferences'] == 'true') {
            indentendOnly = (spec.properties['indentendOnly'] == 'true')
            pref = TestPrefInitializer.initializePreferences(spec.properties as HashMap, null)
        }

        DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(
            spec.userSelection, spec.document, new FormatterPreferencesOnStore(pref), indentendOnly)
        formatter.format().apply(spec.document)

        String actual = spec.document.get()
        String expect = spec.expected.get()
        assert actual == expect
    }
}
