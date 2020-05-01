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
package org.codehaus.groovy.eclipse.test.ui

import org.codehaus.groovy.eclipse.editor.GroovyPartitionScanner
import org.eclipse.jdt.ui.text.IJavaPartitions
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.rules.IToken
import org.junit.Test

final class GroovyPartitionScannerTests {

    private final GroovyPartitionScanner scanner = new GroovyPartitionScanner()

    private void tryString(String string, int start, String expectedContentType) {
        Document document = new Document(string)
        scanner.setRange(document, start, string.length())
        IToken token = scanner.nextToken()
        assert token.data == expectedContentType : "Incorrect content type for \'$string\'"
    }

    @Test
    void testSingleQuotes() {
        tryString('\'\'\'\'\'\'', 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS)
    }

    @Test
    void testSingleQuotes2() {
        tryString('\'\'\'\n\'\'\'', 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS)
    }

    @Test
    void testSingleQuotes3() {
        tryString('\'\'\'dsfasddsfds\n\'\'\'', 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS)
    }

    @Test
    void testDoubleQuotes() {
        tryString('\"\"\"\"\"\"', 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS)
    }

    @Test
    void testDoubleQuotes2() {
        tryString('\"\"\"\n\"\"\"', 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS)
    }

    @Test
    void testDoubleQuotes3() {
        tryString('\"\"\"dsafasdfasdds\n\"\"\"', 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS)
    }

    @Test
    void testNone() {
        tryString('\"\n\"\"\"', 0, IJavaPartitions.JAVA_STRING)
    }

    @Test
    void testNone2() {
        tryString('\'\'\n\'\'\'', 0, IJavaPartitions.JAVA_STRING)
    }

    @Test
    void testComment() {
        tryString('''\
            |/* blah
            | * blah
            | */'''.stripMargin(), 0, IJavaPartitions.JAVA_MULTI_LINE_COMMENT)
    }

    @Test
    void testJavaDoc() {
        tryString('''\
            |/** blah
            | * blah
            | */'''.stripMargin(), 0, IJavaPartitions.JAVA_DOC)
    }
}
