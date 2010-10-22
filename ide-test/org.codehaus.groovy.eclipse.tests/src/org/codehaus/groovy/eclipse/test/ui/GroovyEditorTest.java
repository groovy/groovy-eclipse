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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Event;

/**
 * A test class, meant to be subclasses. Provides utilities for initializing
 * an editor with some text contents, sending keystrokes and commands to
 * the editor and verifying the effect on the contents of the editor.
 *
 * @author Kris De Volder
 */
public abstract class GroovyEditorTest extends EclipseTestCase {

    /**
     * Special string pattern that indicates the position of the caret
     * in the editor's contents.
     */
    protected static final String CARET = "<***>";

    /**
     * The editor used for testing. Create this editor with
     * makeEditor.
     */
    protected GroovyEditor editor;

    @Override
    protected void tearDown() throws Exception {
        GroovyPlugin.getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
        editor = null;
        super.tearDown();
    }

    /**
     * Create an editor on a document initialized with the contents of a String.
     * <p>
     * If the String contains the 'special' character sequence "<***>" this
     * character sequence is removed from the string before initializing the
     * editor. The position of the caret in the Editor is placed at the
     * position of the "<***>" sequence.
     *
     * @throws CoreException
     */
    protected void makeEditor(String contents) throws CoreException {
        int cursor = 0;
        if (contents.contains(CARET)) {
            cursor = contents.indexOf(CARET);
            contents = contents.replace(CARET, "");
        }
        IFile file = testProject.createGroovyTypeAndPackage("", "Test.groovy", contents);
        editor = (GroovyEditor) EditorUtility.openInEditor(file);
        editor.setHighlightRange(cursor, 0, true);
        editor.setFocus();
    }

    /**
     * Pretend to type a string of characters into the editor.
     */
    protected void send(String text) {
        for (char c : text.toCharArray()) {
            send(c);
        }
    }

    /**
     * Pretend to type a single character into the editor.
     */
    protected void send(char c) {
        Event e = new Event();
        e.character = c;
        e.doit = true;
        e.widget = editor.getViewer().getTextWidget();
        e.widget.notifyListeners(SWT.KeyDown, e);
        // Note: I don't think it matters if we send a KeyDown event.
        // The editor/widget doesn't seem to care about it.
    }

    /**
     * Pretend to type a backwards Tab character (i.e. a tab character with the
     * shift
     * key pressed.
     */
    protected void sendBackTab() {
        Event e = new Event();
        e.character = '\t';
        e.stateMask = SWT.SHIFT;
        e.doit = true;
        e.widget = editor.getViewer().getTextWidget();
        e.widget.notifyListeners(SWT.KeyDown, e);
        // Note: I don't think it matters if we send a KeyDown event.
        // The editor/widget doesn't seem to care about it.
    }

    /**
     * Send a string of characters all at once, as if pasted into the editor by
     * a paste command.
     */
    protected void sendPaste(String pasted) {
        StyledText widget = editor.getViewer().getTextWidget();
        Clipboard clipboard = new Clipboard(editor.getViewer().getTextWidget().getDisplay());
        TextTransfer plainTextTransfer = TextTransfer.getInstance();
        clipboard.setContents(new Object[] { pasted }, new Transfer[] { plainTextTransfer });
        widget.paste();
        clipboard.dispose();
    }

    /**
     * Get the text of the editor (does not include cursor position marker).
     */
    protected String getText() {
        return getDocument().get();
    }

    /**
     * @return The document that the editor is working on.
     */
    protected IDocument getDocument() {
        return editor.getViewer().getDocument();
    }

    /**
     * Check the contents of the editor. If the text that is expected contains
     * the CURSOR marker, then the cursor position of the editor will be
     * verified to
     * be in that position as well.
     *
     * @param expected
     */
    protected void assertEditorContents(String expected) {
        String actual = getText();
        if (expected.contains(CARET)) {
            int cursor = getCaret();
            actual = actual.substring(0, cursor) + CARET + actual.substring(cursor);
        }
        assertEquals(expected, actual);
    }

    /**
     * @return the current position of the caret in the Editor
     */
    private int getCaret() {
        return editor.getCaretOffset();
    }

}

