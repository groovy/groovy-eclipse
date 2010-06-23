/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.ui;

import java.util.Hashtable;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

/**
 *
 * @author kdvolder
 * @created 2010-05-20
 */
public class GroovyAutoIndenterTests extends GroovyEditorTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //Our tests are sensitive to tab/space settings so ensure they are
        //set to predictable default values.
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");

        //Also ensure that project specific settings on the test project are turned off
        // (or they will override our test settings on the plugin instance scope level)
        ProjectScope projectPrefScope = new ProjectScope(testProject.getProject());
        projectPrefScope.getNode(JavaCore.PLUGIN_ID).clear();
    }

    /**
     * A simple test just to see whether our scaffolding for testing the
     * editor works.
     *
     * @throws Exception
     */
    public void testScaffolding() throws Exception {
        makeEditor("<***>");
        send('a');
        assertEquals("a", getText());
        assertEditorContents("a<***>");
    }

    public void test1() throws Exception {
        makeEditor("<***>");
        send("class Foo {\n");
        assertEditorContents("class Foo {\n\t<***>\n}");
    }

    public void test2() throws Exception {
        makeEditor(
                "class Foo {\n" +
                "\tdef foo() {<***>\n" +
                "}\n\n");
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "\tdef foo() {\n" +
                "\t\t<***>\n" +
                "\t}\n" +
                "}\n\n");
    }

    public void test3() throws Exception {
        makeEditor(
                "class Foo {\n" +
                "\tdef foo() {\n" +
                "\t\tif (a<b) { \n" +
                "\t\t\tblah()\n" +
                "\t\t}<***>\n" +
                "\t}\n" +
                "}\n\n");
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "\tdef foo() {\n" +
                "\t\tif (a<b) { \n" +
                "\t\t\tblah()\n" +
                "\t\t}\n" +
                "\t\t<***>\n" +
                "\t}\n" +
                "}\n\n");
    }

    public void testGRE631() throws Exception {
        makeEditor(
                "class Foo {\n" +
                "\tdef foo () {\n" +
                "\t\tdef foo = [\"\"]<***>\n" +
                "\t}\n" +
                "}"
        );
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "\tdef foo () {\n" +
                "\t\tdef foo = [\"\"]\n" +
                "\t\t<***>\n" +
                "\t}\n" + "}");
        send("return []\n");
        assertEditorContents(
                "class Foo {\n" +
                "\tdef foo () {\n" +
                "\t\tdef foo = [\"\"]\n" +
                "\t\treturn []\n" +
                "\t\t<***>\n" +
                "\t}\n" +
                "}"
        );
    }

    /**
     * Check whether we are picking up on changed tab/space preferences set
     * for the Java Editor.
     */
    public void testSpaces() throws Exception {
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        makeEditor(
                "class Foo {\n" +
                "\tdef foo() {<***>\n" +
                "}\n\n");
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "\tdef foo() {\n" +
                "        <***>\n" +
                "    }\n" +
                "}\n\n");
    }

    /**
     * GRE_751: Pasting text into a multiline string should not perform any
     * transformations.
     */
    public void testPasteInMultiLineString() throws Exception {
        String initial = "class Foo {\n" +
        "\tdef command = \"\"\"<***>\"\"\"\n" +
        "}\n\n";
        makeEditor(initial);
        String pasteString = "A bunch of \n \t\tmore here. Not\t\t\nand done!";
        sendPaste(pasteString);
        assertEditorContents(initial.replace(CARET, pasteString + CARET));
    }

    /**
     * Check whether we are also picking up on changed tab/space preferences \\
     * even if they are change happens after the editor was already opened.
     */
    public void testSpacesOptionSetAfterOpen() throws Exception {
        makeEditor(
                "class Foo {\n" +
                "\tdef foo() {<***>\n" +
                "}\n\n");
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "\tdef foo() {\n" +
                "        <***>\n" +
                "    }\n" +
                "}\n\n");
    }

    protected void setJavaPreference(String name, String value) {
        Hashtable options = JavaCore.getOptions();
        options.put(name, value);
        JavaCore.setOptions(options);
    }

}
