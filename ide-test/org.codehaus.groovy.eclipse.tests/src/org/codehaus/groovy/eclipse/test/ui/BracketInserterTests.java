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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Event;

/**
 * Tests {@link GroovyEditor.BracketInserter}
 * @author Andrew Eisenberg
 * @created Jan 25, 2010
 */
public class BracketInserterTests extends EclipseTestCase {
    
    public void testInsertDQuote1() throws Exception {
        assertClosing("", "\"\"", '\"', 0);
    }
    public void testInsertDQuote2() throws Exception {
        assertClosing("\"", "\"\"", '\"', 1);
    }
    public void testInsertDQuote3() throws Exception {
        assertClosing("\"\"", "\"\"\"\"\"\"", '\"', 2);
    }
    public void testInsertDQuote4() throws Exception {
        assertClosing("\"\"\"", "\"\"\"\"\"\"", '\"', 3);
    }
    public void testInsertDQuote5() throws Exception {
        assertClosing("assert ", "assert \"\"", '\"', 7);
    }
    public void testInsertDQuote6() throws Exception {
        assertClosing("assert \"", "assert \"\"", '\"', 8);
    }
    public void testInsertDQuote7() throws Exception {
        assertClosing("''", "'\"'", '\"', 1);
    }

    
    
    public void testInsertSQuote1() throws Exception {
        assertClosing("", "\'\'", '\'', 0);
    }
    public void testInsertSQuote2() throws Exception {
        assertClosing("\'", "\'\'", '\'', 1);
    }
    public void testInsertSQuote3() throws Exception {
        assertClosing("\'\'", "\'\'\'\'\'\'", '\'', 2);
    }
    public void testInsertSQuote4() throws Exception {
        assertClosing("\'\'\'", "\'\'\'\'\'\'", '\'', 3);
    }
    
    public void testInsertParen() throws Exception {
        assertClosing("", "()", '(', 0);
    }
    public void testInsertSquare() throws Exception {
        assertClosing("", "[]", '[', 0);
    }
    public void testInsertAngle() throws Exception {
        assertClosing("", "<>", '<', 0);
    }
    
    
    
    private void assertClosing(String initialDoc, String expectedDoc, char inserted, int location) throws Exception {
        // add extra spaces since the String rule fails for end of file.
        initialDoc += "\n";
        expectedDoc += "\n ";
        IFile file = testProject.createGroovyTypeAndPackage("", "BracketTesting.groovy", initialDoc + " ");
        GroovyEditor editor = (GroovyEditor) EditorUtility.openInEditor(file);
        try {
            Event e = new Event();
            e.character = inserted;
            e.doit = true;
            e.widget = editor.getViewer().getTextWidget();
            VerifyEvent ve = new VerifyEvent(e);
            editor.getViewer().setSelectedRange(location, 0);
            editor.getGroovyBracketInserter().verifyKey(ve);
            if (ve.doit) {
                editor.getViewer().getDocument().replace(location, 0, Character.toString(inserted));
            }
            String actual = editor.getViewer().getDocument().get();
            assertEquals("Invalid bracket insertion.\nInserted char: \'" + inserted + "\' at location " + location,
                    expectedDoc, actual);
        } finally {
            GroovyPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
        }
    }
}

