/*
 * Copyright 2009-2026 the original author or authors.
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
package org.codehaus.groovy.alltests

import org.codehaus.groovy.eclipse.chooser.CompilerChooser
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Only the formatter tests, for a quick run from the command line:
 * <pre>
 * mvn -Pe4.41 -pl ide-test/org.codehaus.groovy.alltests -am verify -Dtest=FormatterTests
 * </pre>
 */
@RunWith(Suite) @Suite.SuiteClasses([
    org.codehaus.groovy.eclipse.refactoring.test.formatter.FindIndentsTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.FormatterPreferencesTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.GroovyDocumentScannerTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.GroovyFormatterTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.SemicolonRemoverTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.WhitespaceRemoverTests,
])
final class FormatterTests {
    @BeforeClass
    static void setUp() {
        // ensure that the compiler chooser is started, as AllGroovyTests does
        CompilerChooser.instance.initialize()
    }
}
