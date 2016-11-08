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

import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Ensures type completion is working and that the resulting document remains well-formed.
 *
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 */
public final class TypeCompletionTests2 extends CompletionTestCase {

    private static final String HTML_PROPOSAL = "HTML - javax.swing.text.html";
    private static final String HTMLTableCaptionElement_PROPOSAL = "HTMLTableCaptionElement - org.w3c.dom.html";

    public TypeCompletionTests2(String name) {
        super(name);
    }

    private void checkProposal(String source, String target, String proposalSite, String proposalName) {
        try {
            checkProposalApplicationType(source, target, getIndexOf(source, proposalSite), proposalName);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //

    public void testTypeCompletionInScript1() {
        String contents = "HTML";
        String expected = "import javax.swing.text.html.HTML\n\nHTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript2() {
        String contents = "import javax.swing.plaf.ButtonUI\n\nHTML\nButtonUI";
        String expected = "import javax.swing.plaf.ButtonUI\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript2_() {
        String contents = "/*header*/\nimport javax.swing.plaf.ButtonUI //note\n\nHTML\nButtonUI";
        String expected = "/*header*/\nimport javax.swing.plaf.ButtonUI //note\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript2a() {
        String contents = "import javax.swing.plaf.ButtonUI\n\nHTML\nButtonUI";
        String expected = "import javax.swing.plaf.ButtonUI\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript2b() {
        String contents = "import javax.swing.plaf.ButtonUI as Button\n\nHTML\nButton";
        String expected = "import javax.swing.plaf.ButtonUI as Button\nimport javax.swing.text.html.HTML\n\nHTML\nButton";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript2c() {
        String contents = "import javax.swing.plaf.ButtonUI as Button\n\nHTML\nButton";
        String expected = "import javax.swing.plaf.ButtonUI as Button\nimport javax.swing.text.html.HTML\n\nHTML\nButton";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript3() {
        String contents = "import javax.swing.plaf.*\n\nHTML\nButtonUI";
        String expected = "import javax.swing.plaf.*\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript3a() {
        String contents = "import javax.swing.plaf.*\n\nHTML\nButtonUI";
        String expected = "import javax.swing.plaf.*\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript4() {
        String contents = "import static java.util.Collections.emptyList\n\nHTML\ndef list = emptyList()";
        String expected = "import static java.util.Collections.emptyList\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = emptyList()";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript4a() {
        String contents = "import static java.util.Collections.emptyList\n\nHTML\ndef list = emptyList()";
        String expected = "import static java.util.Collections.emptyList\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = emptyList()";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript4b() {
        String contents = "import static java.util.Collections.EMPTY_LIST as EMPTY\n\nHTML\ndef list = EMPTY";
        String expected = "import static java.util.Collections.EMPTY_LIST as EMPTY\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = EMPTY";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript4c() {
        String contents = "import static java.util.Collections.EMPTY_LIST as EMPTY\n\nHTML\ndef list = EMPTY";
        String expected = "import static java.util.Collections.EMPTY_LIST as EMPTY\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = EMPTY";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript5() {
        String contents = "import static java.util.Collections.*\n\nHTML\ndef list = emptyList()";
        String expected = "import static java.util.Collections.*\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = emptyList()";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInScript5b() {
        String contents = "import static java.util.Collections.*\n\nHTML\ndef list = emptyList()";
        String expected = "import static java.util.Collections.*\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = emptyList()";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void _testTypeCompletionInScript6() {
        String contents = "/* header comment */\n" +
                "import javax.swing.plaf.ButtonUI /*tag*/ //note\n" +
                "  // some single-line comment\n" +
                "\n" +
                "HTML\n" +
                "ButtonUI";
        String expected = "/* header comment */\n" +
                "import javax.swing.plaf.ButtonUI /*tag*/ //note\n" +
                "  // some single-line comment\n" +
                "import javax.swing.text.html.HTML\n" +
                "\n" +
                "HTML\n" +
                "ButtonUI";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    // TODO: Add cases for default package and variations with a package def.

    public void testTypeCompletionInBrokenScript1() {
        String contents = "def x(HTML";
        String expected = "import javax.swing.text.html.HTML\n\ndef x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInBrokenScript2() {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = "package f\n\ndef x(HTML";
        String expected = "package f\n\nimport javax.swing.text.html.HTML\n\n\ndef x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInBrokenScript3() {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = "/**some stuff*/\npackage f\n\ndef x(HTML";
        String expected = "/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML\n\n\ndef x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    // Bug !!! See GRECLIPSE-1231  import statements placed on same line because ';' is not recognized as part of the import statement
    public void testTypeCompletionInBrokenScript4() {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = "/**some stuff*/\n\nimport javax.swing.plaf.ButtonUI\n\ndef x(HTML";
        String expected = "/**some stuff*/\n\nimport javax.swing.plaf.ButtonUI\nimport javax.swing.text.html.HTML\n\ndef x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    // Bug !!! See GRECLIPSE-1231  import statements placed on same line because ';' is not recognized as part of the import statement
    public void testTypeCompletionInBrokenScript5() {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = "/**some stuff*/\npackage f\n\nimport javax.swing.plaf.ButtonUI\n\ndef x(HTML";
        String expected = "/**some stuff*/\npackage f\n\nimport javax.swing.plaf.ButtonUI\nimport javax.swing.text.html.HTML\n\ndef x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInBrokenScript6() {
        String contents =
                "/**some stuff*/\n" +
                "package f\n" +
                "HTML";
        String expected =
                "/**some stuff*/\n" +
                "package f\n" +
                "\n" +
                "import javax.swing.text.html.HTML\n" +
                "\n" +
                "HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInBrokenScript7() {
        // disabled on 17 and earlier since parser recovery not implemented
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents =
                "/**some stuff*/\n" +
                "package f\n" +
                "\n" +
                "import javax.swing.plaf.ButtonUI\n" +
                "\n" +
                "def x(HTML\n" +
                "";
        String expected =
                "/**some stuff*/\n" +
                "package f\n" +
                "\n" +
                "import javax.swing.plaf.ButtonUI\n" +
                "import javax.swing.text.html.HTML\n" +
                "\n" +
                "def x(HTML\n" +
                "";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInBrokenScript8() {
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
                "import javax.swing.text.html.HTML\n" +
                "\n" +
                "def x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInBrokenScript9() {
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
                "import javax.swing.text.html.HTML\n" +
                "\n" +
                "import java.awt.dnd.DropTarget as Foo\n" +
                "\n" +
                "def x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInBrokenScript10() {
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
                "import javax.swing.text.html.HTML\n" +
                "\n" +
                "import java.awt.dnd.DropTarget as Foo\n" +
                "\n" +
                "def x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    // GRECLIPSE-926
    public void testTypeCompletionInBrokenScript11() {
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
                "import org.w3c.dom.html.HTMLTableCaptionElement\n" +
                "\n" +
                "\n" +
                "   void nuthin() {\n" +
                "         if (! (this instanceof HTMLTableCaptionElement/*_*/) {\n" +
                "            HTML\n" +
                "         }\n" +
                "    }";

        checkProposal(contents, expected, "HTMLT", HTMLTableCaptionElement_PROPOSAL);
    }

    public void testTypeCompletionInBrokenClass1() {
        String contents = "/**some stuff*/\npackage f\n\nclass Y {\ndef x(HTML";
        String expected = "/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML\n\nclass Y {\ndef x(HTML";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    public void testTypeCompletionInBrokenClass2() {
        String contents = "/**some stuff*/\npackage f\n\nclass Y extends HTML {\ndef x(H";
        String expected = "/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML\n\nclass Y extends HTML {\ndef x(H";

        checkProposal(contents, expected, "HTML", HTML_PROPOSAL);
    }

    // https://github.com/groovy/groovy-eclipse/issues/177
    public void testTypeCompletionForClassAnnotation() {
        IPreferenceStore prefs = JavaPlugin.getDefault().getPreferenceStore();
        String originalOrder = prefs.getString(PreferenceConstants.ORGIMPORTS_IMPORTORDER);
        prefs.setValue(PreferenceConstants.ORGIMPORTS_IMPORTORDER, "\\#;java;javax;groovy;groovyx;;");
        try {
        String contents = "import static org.mockito.Mockito.mock\n\n" +
                "import org.junit.Test\n\n" +
                "@TypeCh\n" +
                "final class WeakReferenceSetTests {\n" +
                "    @Test\n" +
                "    void testAddAndEmpty() {\n" +
                "        WeakReferenceSet set = new WeakReferenceSet()\n" +
                "        assert set.empty\n" +
                "        set << mock(ConcreteType)\n" +
                "        assert !set.empty\n" +
                "    }\n" +
                "}";
        String expected = "import static org.mockito.Mockito.mock\n\n" +
                "import groovy.transform.TypeChecked\n\n" +
                "import org.junit.Test\n\n" +
                "@TypeChecked\n" +
                "final class WeakReferenceSetTests {\n" +
                "    @Test\n" +
                "    void testAddAndEmpty() {\n" +
                "        WeakReferenceSet set = new WeakReferenceSet()\n" +
                "        assert set.empty\n" +
                "        set << mock(ConcreteType)\n" +
                "        assert !set.empty\n" +
                "    }\n" +
                "}";
        checkProposal(contents, expected, "@TypeCh", "TypeChecked - groovy.transform");
        } finally {
            prefs.setValue(PreferenceConstants.ORGIMPORTS_IMPORTORDER, originalOrder);
        }
    }
}
