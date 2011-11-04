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

import org.eclipse.jdt.core.tests.util.GroovyUtils;



/**
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 *
 * Tests that type completions are working properly.  Ensures that the
 * resulting document has the correct text in it.
 */
public class TypeCompletionTests2 extends CompletionTestCase {


    private static final String HTML = "HTML";
    private static final String HTMLT = "HTMLT";
    private static final String HTML_PROPOSAL = "HTML - javax.swing.text.html";
    private static final String HTMLTableCaptionElement_PROPOSAL = "HTMLTableCaptionElement - org.w3c.dom.html";
    
    public TypeCompletionTests2(String name) {
        super(name);
    }

    public void testSimpleCompletionTypesInScript1() throws Exception {
    	String contents = HTML;
        String expected = "import javax.swing.text.html.HTML;\n\nHTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }

    public void testSimpleCompletionTypesInScript2() throws Exception {
        String contents = "import javax.swing.plaf.ButtonUI;\n\nHTML\nButtonUI";
        String expected = "import javax.swing.plaf.ButtonUI;\nimport javax.swing.text.html.HTML;\n\nHTML\nButtonUI";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }

    public void testBrokenScript1() throws Exception {
        String contents = "def x(HTML";
        String expected = "import javax.swing.text.html.HTML;\n\ndef x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    
    public void testBrokenScript2() throws Exception {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = "package f\n\ndef x(HTML";
        String expected = "package f\n\nimport javax.swing.text.html.HTML;\n\n\ndef x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    
    public void testBrokenScript3() throws Exception {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = "/**some stuff*/\npackage f\n\ndef x(HTML";
        String expected = "/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML;\n\n\ndef x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    
    // Bug !!! See GRECLIPSE-1231  import statements placed on same line because ';' is not recognized as part of the import statement
    public void testBrokenScript4() throws Exception {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = "/**some stuff*/\n\nimport javax.swing.plaf.ButtonUI;\n\ndef x(HTML";
        String expected = "/**some stuff*/\n\nimport javax.swing.plaf.ButtonUI;import javax.swing.text.html.HTML;\n\n\ndef x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    
    // Bug !!! See GRECLIPSE-1231  import statements placed on same line because ';' is not recognized as part of the import statement
    public void testBrokenScript5() throws Exception {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = "/**some stuff*/\npackage f\n\nimport javax.swing.plaf.ButtonUI;\n\ndef x(HTML";
        String expected = "/**some stuff*/\npackage f\n\nimport javax.swing.plaf.ButtonUI;import javax.swing.text.html.HTML;\n\n\ndef x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    

    public void testBrokenScript6() throws Exception {
        String contents = 
                "/**some stuff*/\n" + 
        		"package f\n" + 
        		"HTML";
        String expected = 
                "/**some stuff*/\n" + 
        		"package f\n" +
        		"\n" +
        		"import javax.swing.text.html.HTML;\n" +
        		"\n" +
        		"HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    public void testBrokenScript7() throws Exception {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = 
                "/**some stuff*/\n" + 
                "package f\n" + 
                "\n" + 
                "import javax.swing.plaf.ButtonUI;\n" + 
                "\n" + 
                "def x(HTML\n" + 
                "";
        String expected = 
                "/**some stuff*/\n" + 
                "package f\n" + 
                "\n" + 
                "import javax.swing.plaf.ButtonUI;import javax.swing.text.html.HTML;\n" + 
                "\n" + 
                "\n" + 
                "def x(HTML\n" + 
                "";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    public void testBrokenScript8() throws Exception {
        String contents = 
                "/**some stuff*/\n" + 
                "package f\n" + 
                "\n" + 
                "import javax.swing.plaf.ButtonUI\n" + 
                "\n" + 
                "def x(HTML";
        String expected = 
                "/**some stuff*/\n" + 
                "package f\n" + 
                "\n" + 
                "import javax.swing.plaf.ButtonUI\n" + 
                "import javax.swing.text.html.HTML;\n" + 
                "\n" + 
                "def x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }

    
    public void testBrokenScript9() throws Exception {
        String contents = 
                "/**some stuff*/\n" + 
                "package f\n" + 
                "\n" + 
                "import javax.swing.plaf.ColorChooserUI\n" + 
                "import java.awt.dnd.DropTarget as Foo\n" + 
                "\n" + 
                "def x(HTML";
        String expected = 
                "/**some stuff*/\n" + 
                "package f\n" + 
                "\n" + 
                "import javax.swing.plaf.ColorChooserUI\n" + 
                "import javax.swing.text.html.HTML;\n" + 
                "\n" + 
                "import java.awt.dnd.DropTarget as Foo\n" + 
                "\n" + 
                "def x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }

    public void testBrokenScript10() throws Exception {
        String contents = 
                "/**some stuff*/\n" + 
                "\n" + 
                "import javax.swing.plaf.ColorChooserUI\n" + 
                "import java.awt.dnd.DropTarget as Foo\n" + 
                "\n" + 
                "def x(HTML";
        String expected = 
                "/**some stuff*/\n" + 
                "\n" + 
                "import javax.swing.plaf.ColorChooserUI\n" + 
                "import javax.swing.text.html.HTML;\n" + 
                "\n" + 
                "import java.awt.dnd.DropTarget as Foo\n" + 
                "\n" + 
                "def x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    
    // GRECLIPSE-926
    public void testBrokenScript11() throws Exception {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = 
                "package f\n" + 
                "\n" + 
                "import javax.swing.text.html.HTML\n" + 
                "\n" + 
                "\n" + 
                "   void nuthin() {\n" + 
                "         if (! (this instanceof HTMLT/*_*/) {\n" + 
                "            HTML\n" + 
                "         }\n" + 
                "    }";
        String expected = 
                "package f\n" + 
                "\n" + 
                "import javax.swing.text.html.HTML\n" + 
                "\n" + 
                "import org.w3c.dom.html.HTMLTableCaptionElement;\n" + 
                "\n" + 
                "\n" + 
                "   void nuthin() {\n" + 
                "         if (! (this instanceof HTMLTableCaptionElement/*_*/) {\n" + 
                "            HTML\n" + 
                "         }\n" + 
                "    }";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTMLT),
                HTMLTableCaptionElement_PROPOSAL);
    }

    public void testBrokenClass1() throws Exception {
        String contents = "/**some stuff*/\npackage f\n\nclass Y {\ndef x(HTML";
        String expected = "/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML;\n\nclass Y {\ndef x(HTML";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
    
    public void testBrokenClass2() throws Exception {
        String contents = "/**some stuff*/\npackage f\n\nclass Y extends HTML {\ndef x(H";
        String expected = "/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML;\n\nclass Y extends HTML {\ndef x(H";
        checkProposalApplicationType(contents, expected, getIndexOf(contents, HTML),
                HTML_PROPOSAL);
    }
}