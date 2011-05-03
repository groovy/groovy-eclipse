/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codeassist.tests;

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;



/**
 * @author Andrew Eisenberg
 * @created May 2, 2011
 * 
 * Tests that new fields are created properly
 */
public class NewFieldCompletionTests extends CompletionTestCase {

    public NewFieldCompletionTests(String name) {
        super(name);
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
