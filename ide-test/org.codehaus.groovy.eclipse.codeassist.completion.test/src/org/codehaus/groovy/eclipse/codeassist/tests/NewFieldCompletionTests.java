/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.tests;

import junit.framework.Test;

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests that new fields are created properly
 *
 * @author Andrew Eisenberg
 * @created May 2, 2011
 */
public final class NewFieldCompletionTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(NewFieldCompletionTests.class);
    }

    public void testNewFieldSimple() throws Exception {
        String contents = "class SomeClass {\nString str}";
        String expected = "class SomeClass {\nString string}";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "str"), "String string");
    }

    public void testNewField1() throws Exception {
        String contents =
                "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                "class SomeClass {\nHTMLFrameHyperlinkEvent  }";

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent "),
                new String[] {
                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent frameHyperlinkEvent }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent hyperlinkEvent }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent event }"
                },
                new String[] {
                    "HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent",
                    "HTMLFrameHyperlinkEvent frameHyperlinkEvent",
                    "HTMLFrameHyperlinkEvent hyperlinkEvent",
                    "HTMLFrameHyperlinkEvent event"
                });
    }

    public void testNewField2() throws Exception {
        String contents =
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent  }";

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent "),
                new String[] {
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent }",
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent frameHyperlinkEvent }",
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent hyperlinkEvent }",
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent event }"
        },
        new String[] {
            "HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent",
            "HTMLFrameHyperlinkEvent frameHyperlinkEvent",
            "HTMLFrameHyperlinkEvent hyperlinkEvent",
            "HTMLFrameHyperlinkEvent event"
        });
    }

    public void testNewField3() throws Exception {
        String contents =
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent f }";

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent f"),
                new String[] {
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent frameHyperlinkEvent }",
        },
        new String[] {
            "HTMLFrameHyperlinkEvent frameHyperlinkEvent",
        });
    }

    public void testNewField4() throws Exception {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent        }";

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent       "),
                new String[] {
                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent       htmlFrameHyperlinkEvent }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent       frameHyperlinkEvent }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent       hyperlinkEvent }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent       event }"
                },
                new String[] {
                    "HTMLFrameHyperlinkEvent       htmlFrameHyperlinkEvent",
                    "HTMLFrameHyperlinkEvent       frameHyperlinkEvent",
                    "HTMLFrameHyperlinkEvent       hyperlinkEvent",
                    "HTMLFrameHyperlinkEvent       event"
                });
    }

    public void testNewField5() throws Exception {
        String contents =
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent     f }";

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent     f"),
                new String[] {
            "class SomeClass {\njavax.swing.text.html.HTMLFrameHyperlinkEvent     frameHyperlinkEvent }",
        },
        new String[] {
            "HTMLFrameHyperlinkEvent     frameHyperlinkEvent",
        });
    }

    public void testNewField6() throws Exception {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent[]        }";

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent[]       "),
                new String[] {
                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent[]       htmlFrameHyperlinkEvents }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent[]       frameHyperlinkEvents }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent[]       hyperlinkEvents }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent[]       events }"
                },
                new String[] {
                    "HTMLFrameHyperlinkEvent[]       htmlFrameHyperlinkEvents",
                    "HTMLFrameHyperlinkEvent[]       frameHyperlinkEvents",
                    "HTMLFrameHyperlinkEvent[]       hyperlinkEvents",
                    "HTMLFrameHyperlinkEvent[]       events"
                });
    }
    public void testNewField7() throws Exception {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]        }";

        checkProposalApplication(contents, getIndexOf(contents, "HTMLFrameHyperlinkEvent  [][]       "),
                new String[] {
                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]       htmlFrameHyperlinkEvents }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]       frameHyperlinkEvents }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]       hyperlinkEvents }",

                    "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
                    "class SomeClass {\nHTMLFrameHyperlinkEvent  [][]       events }"
                },
                new String[] {
                    "HTMLFrameHyperlinkEvent  [][]       htmlFrameHyperlinkEvents",
                    "HTMLFrameHyperlinkEvent  [][]       frameHyperlinkEvents",
                    "HTMLFrameHyperlinkEvent  [][]       hyperlinkEvents",
                    "HTMLFrameHyperlinkEvent  [][]       events"
                });
    }


    public void testNoNewField1() throws Exception {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent HTMLFrameHyperlinkEvent\nht  }";

        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "\nht"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent", 0);
    }

    public void testNoNewField2() throws Exception {
        String contents =
            "import javax.swing.text.html.HTMLFrameHyperlinkEvent;\n" +
            "class SomeClass {\nHTMLFrameHyperlinkEvent HTMLFrameHyperlinkEvent\n      }";

        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "\n    "), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "HTMLFrameHyperlinkEvent htmlFrameHyperlinkEvent", 0);
    }

}
