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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests that new fields are created properly.
 */
final class NewFieldCompletionTests extends CompletionTestSuite {

    @Test
    void testNewFieldSimple() {
        String contents = "class SomeClass {\nString str}"
        String expected = "class SomeClass {\nString string}"
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "str"), "String string")
    }

    @Test
    void testNewField1() {
        String contents =
                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent  }"

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent "),
            [
                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent frameHyperlinkEvent }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent hyperlinkEvent }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent event }"
            ] as String[],
            [
                "HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent",
                "HTMLFrameHyperlinkEvent frameHyperlinkEvent",
                "HTMLFrameHyperlinkEvent hyperlinkEvent",
                "HTMLFrameHyperlinkEvent event"
            ] as String[])
    }

    @Test
    void testNewField2() {
        String contents =
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent  }"

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent "),
            [
                "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent }",
                "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent frameHyperlinkEvent }",
                "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent hyperlinkEvent }",
                "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent event }"
            ] as String[],
            [
                "HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent",
                "HTMLFrameHyperlinkEvent frameHyperlinkEvent",
                "HTMLFrameHyperlinkEvent hyperlinkEvent",
                "HTMLFrameHyperlinkEvent event"
            ] as String[])
    }

    @Test
    void testNewField3() {
        String contents =
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent f }"

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent f"),
            [
                "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent frameHyperlinkEvent }",
            ] as String[],
            [
                "HTMLFrameHyperlinkEvent frameHyperlinkEvent",
            ] as String[])
    }

    @Test
    void testNewField4() {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent        }"

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent       "),
            [
                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent       htmlFrameHyperlinkEvent }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent       frameHyperlinkEvent }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent       hyperlinkEvent }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent       event }"
            ] as String[],
            [
                "HTMLFrameHyperlinkEvent       htmlFrameHyperlinkEvent",
                "HTMLFrameHyperlinkEvent       frameHyperlinkEvent",
                "HTMLFrameHyperlinkEvent       hyperlinkEvent",
                "HTMLFrameHyperlinkEvent       event"
            ] as String[])
    }

    @Test
    void testNewField5() {
        String contents =
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent     f }"

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent     f"),
            [
                "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent     frameHyperlinkEvent }",
            ] as String[],
            [
                "HTMLFrameHyperlinkEvent     frameHyperlinkEvent",
            ] as String[])
    }

    @Test
    void testNewField6() {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent[]        }"

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent[]       "),
            [
                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent[]       htmlFrameHyperlinkEvents }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent[]       frameHyperlinkEvents }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent[]       hyperlinkEvents }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent[]       events }"
            ] as String[],
            [
                "HTMLFrameHyperlinkEvent[]       htmlFrameHyperlinkEvents",
                "HTMLFrameHyperlinkEvent[]       frameHyperlinkEvents",
                "HTMLFrameHyperlinkEvent[]       hyperlinkEvents",
                "HTMLFrameHyperlinkEvent[]       events"
            ] as String[])
    }

    @Test
    void testNewField7() {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]        }"

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent  [][]       "),
            [
                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]       htmlFrameHyperlinkEvents }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]       frameHyperlinkEvents }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]       hyperlinkEvents }",

                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]       events }"
            ] as String[],
            [
                "HTMLFrameHyperlinkEvent  [][]       htmlFrameHyperlinkEvents",
                "HTMLFrameHyperlinkEvent  [][]       frameHyperlinkEvents",
                "HTMLFrameHyperlinkEvent  [][]       hyperlinkEvents",
                "HTMLFrameHyperlinkEvent  [][]       events"
            ] as String[])
    }

    @Test
    void testNoNewField1() {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent HTMLFrameHyperlinkEvent\nht  }"

        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(contents, "\nht"))
        proposalExists(proposals, "HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent", 0)
    }

    @Test
    void testNoNewField2() {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent HTMLFrameHyperlinkEvent\n      }"

        ICompilationUnit unit = addGroovySource(contents, "File", "")
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(contents, "\n    "))
        proposalExists(proposals, "HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent", 0)
    }
}
