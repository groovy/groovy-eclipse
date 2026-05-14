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

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.editor.GroovyEditor
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentationService
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.jdt.groovy.core.util.ReflectionUtils
import org.eclipse.jface.text.IDocument
import org.eclipse.swt.SWT
import org.eclipse.swt.dnd.Clipboard
import org.eclipse.swt.dnd.TextTransfer
import org.eclipse.swt.dnd.Transfer
import org.eclipse.swt.widgets.Event
import org.eclipse.ui.texteditor.AbstractTextEditor
import org.junit.After
import org.junit.Assert

/**
 * A test class, meant to be subclasses. Provides utilities for initializing
 * an editor with some text contents, sending keystrokes and commands to
 * the editor and verifying the effect on the contents of the editor.
 */
abstract class GroovyEditorTestSuite extends GroovyEclipseTestSuite {

    /**
     * Special string pattern that indicates the position of the caret
     * in the editor's contents.
     */
    protected static final String CARET = '<***>'

    /**
     * The editor used for testing. Create this editor with makeEditor().
     */
    protected GroovyEditor editor

    @After
    final void tearDownEditors() {
        GroovyIndentationService.disposeLast() // clear the cached reference
        GroovyPlugin.activeWorkbenchWindow.activePage.closeAllEditors(false)
    }

    /**
     * Create an editor on a document initialized with the contents of a String.
     * <p>
     * If the String contains the 'special' character sequence '<***>' this
     * character sequence is removed from the string before initializing the
     * editor. The position of the caret in the Editor is placed at the
     * position of the '<***>' sequence.
     */
    protected void makeEditor(String contents) {
        int cursor = 0
        if (contents.contains(CARET)) {
            cursor = contents.indexOf(CARET)
            contents = contents.replace(CARET, '')
        }
        def unit = addGroovySource(contents, nextUnitName())
        editor = (GroovyEditor) openInEditor(unit)
        editor.setHighlightRange(cursor, 0, true)
        editor.setFocus()
    }

    /**
     * Simulates typing characters into the editor.
     */
    protected void send(CharSequence text) {
        text.chars().forEach {
            send((char) it)
        }
    }

    /**
     * Pretend to type a single character into the editor.
     */
    protected void send(char c) {
        editor.viewer.textWidget.with {
            notifyListeners(SWT.KeyDown, new Event(
                character: c,
                widget: it,
                doit: true
            ))
        }
        // Note: I don't think it matters if we send a KeyDown event.
        // The editor/widget doesn't seem to care about it.
    }

    /**
     * Pretend to type a backwards tab character (i.e. Shift+Tab).
     */
    protected void sendBackTab() {
        editor.viewer.textWidget.with {
            notifyListeners(SWT.KeyDown, new Event(
                stateMask: SWT.SHIFT,
                character: '\t',
                widget: it,
                doit: true
            ))
        }
        // Note: I don't think it matters if we send a KeyDown event.
        // The editor/widget doesn't seem to care about it.
    }

    /**
     * Send a string of characters all at once, as if pasted into the editor by a paste command.
     */
    protected void sendPaste(CharSequence pasted) {
        editor.viewer.textWidget.with {
            Object[] chars = [pasted.toString()]
            Transfer[] types = [TextTransfer.instance]

            // transfer the characters to the system clipboard
            Clipboard clipboard = new Clipboard(it.display)
            clipboard.setContents(chars, types)
            clipboard.dispose()

            paste()
        }
    }

    /**
     * @return the current position of the caret in the editor
     */
    private int getCaret() {
        return editor.caretOffset
    }

    /**
     * @return the document that the editor is working on
     */
    protected IDocument getDocument() {
        return editor.viewer.document
    }

    /**
     * @return the text of the editor (does not include cursor position marker)
     */
    protected String getText() {
        return editor.viewer.document.get()
    }

    /**
     * Checks the contents of the editor. If the text that is expected contains
     * the CARET marker, then the cursor position of the editor will be verified
     * to be in that position as well.
     */
    protected void assertEditorContents(String expected) {
        String actual = getText()
        if (expected.contains(CARET)) {
            actual = actual.substring(0, getCaret()) + CARET + actual.substring(getCaret())
        }
        Assert.assertEquals(expected, actual.normalize())
    }

    protected void assertStatusLineText(String expected) {
        Object manager = ReflectionUtils.throwableExecutePrivateMethod(AbstractTextEditor, 'getStatusLineManager', new Class[0], editor, new Object[0])
        String actual = ReflectionUtils.throwableGetPrivateField(manager.class, 'message', manager)
        Assert.assertEquals(expected, actual)
    }
}
