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
package org.codehaus.groovy.eclipse.test.ui;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public final class GroovyAutoIndenterTests extends GroovyEditorTestCase {

    @Before
    public void setUp() throws Exception {
        // tests are sensitive to tab/space settings so ensure they are set to predictable default values
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
    }

    /**
     * A simple test just to see whether our scaffolding for testing the editor works.
     */
    @Test
    public void testScaffolding() throws Exception {
        makeEditor("<***>");
        send('a');
        Assert.assertEquals("a", getText());
        assertEditorContents("a<***>");
    }

    @Test
    public void test1() throws Exception {
        makeEditor("<***>");
        send("class Foo {\n");
        assertEditorContents("class Foo {\n\t<***>\n}");
    }

    @Test
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

    @Test
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

    @Test
    public void testGRE631() throws Exception {
        makeEditor(
                "class Foo {\n" +
                "\tdef foo () {\n" +
                "\t\tdef foo = [\"\"]<***>\n" +
                "\t}\n" +
                "}");
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
                "}");
    }

    /**
     * Check whether we are picking up on changed tab/space preferences set
     * for the Java Editor.
     */
    @Test
    public void testSpaces() throws Exception {
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        makeEditor(
                "class Foo {\n" +
                "\tdef foo() {<***>\n" +
                "}");
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "\tdef foo() {\n" +
                "        <***>\n" +
                "    }\n" +
                "}");
    }

    /**
     * GRE_751: Pasting text into a multiline string should not perform any
     * transformations.
     */
    @Test
    public void testPasteInMultiLineString() throws Exception {
        String initial = "class Foo {\n" +
                "\tdef command = \"\"\"<***>\"\"\"\n" +
                "}";
        makeEditor(initial);
        String pasteString = "A bunch of \n \t\tmore here. Not\t\t\nand done!";
        sendPaste(pasteString);
        assertEditorContents(initial.replace(CARET, pasteString + CARET));
    }

    /**
     * Check whether we are also picking up on changed tab/space preferences
     * even if they are change happens after the editor was already opened.
     */
    @Test
    public void testSpacesOptionSetAfterOpen() throws Exception {
        makeEditor(
                "class Foo {\n" +
                "\tdef foo() {<***>\n" +
                "}");
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "\tdef foo() {\n" +
                "        <***>\n" +
                "    }\n" +
                "}");
    }

    /**
     * Check whether autoindentor works correct for mixed tab/spaces mode.
     */
    @Test
    public void testMixedTabsAndSpaces() throws Exception {
        makeEditor(
                "class Foo {\n" +
                "    def foo() {\n" +
                "        def bar {<***>\n" +
                "        }\n" +
                "    }\n"+
                "}");
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "8");
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "4");
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "    def foo() {\n" +
                "        def bar {\n" +
                "\t    <***>\n" +
                "        }\n" +
                "    }\n"+
                "}");
    }

    /**
     * Similar to above, but also check whether it counts the tabs on previous lines correctly.
     */
    @Test
    public void testMixedTabsAndSpaces2() throws Exception {
        makeEditor(
                "class Foo {\n" +
                "    def foo() {\n" +
                "\tdef bar {<***>\n" +
                "\t}\n" +
                "    }\n"+
                "}");
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "8");
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "4");
        send("\n");
        assertEditorContents(
                "class Foo {\n" +
                "    def foo() {\n" +
                "\tdef bar {\n" +
                "\t    <***>\n" +
                "\t}\n" +
                "    }\n"+
                "}");
    }
}
