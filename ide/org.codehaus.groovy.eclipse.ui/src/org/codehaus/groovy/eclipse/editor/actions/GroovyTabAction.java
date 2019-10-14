/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.actions;

import static org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentationService.getLineLeadingWhiteSpace;
import static org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentationService.getLineTextUpto;

import java.util.ResourceBundle;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentationService;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.eclipse.ui.texteditor.TextEditorAction;

public class GroovyTabAction extends TextEditorAction {

    public GroovyTabAction(ITextEditor editor) {
        super(fgBundleForConstructedKeys, "Indent.", editor);
    }

    @Override
    public void run() {
        // update has been called by the framework
        if (!isEnabled() || !validateEditorInputState())
            return;

        ITextSelection selection = getSelection();
        final IDocument d = getDocument();
        if (d != null) {
            try {
                int offset = selection.getOffset();

                // We are making a few assumptions about the circumstances under which we get to this point in the code:
                Assert.isTrue(selection.getLength() == 0);
                Assert.isTrue(isInSmartTabRegion(d, offset));

                int tabLine = d.getLineOfOffset(offset);
                String lineStartText = getLineTextUpto(d, offset);

                IFile file = Adapters.adapt(getTextEditor().getEditorInput(), IFile.class);
                if (file != null && file.getProject() != null) {

                    GroovyIndentationService indentation = GroovyIndentationService.get(JavaCore.create(file.getProject()));
                    int newIndentLevel = indentation.computeIndentForLine(d, tabLine);
                    int cursorIndent = indentation.indentLevel(lineStartText);

                    if (cursorIndent < newIndentLevel) {
                        // Smart tab should apply
                        String leadingWhiteSpace = getLineLeadingWhiteSpace(d, tabLine);
                        int editOffset = d.getLineOffset(tabLine);
                        int editLength = leadingWhiteSpace.length();
                        String editText = indentation.createIndentation(newIndentLevel);
                        d.replace(editOffset, editLength, editText);
                        selectAndReveal(editOffset + editText.length(), 0);
                    } else {
                        // Just indent one tab size using whatever style dictated by preferences (i.e. maybe use tabs or spaces)
                        String editText = indentation.getTabString();
                        d.replace(offset, 0, editText);
                        selectAndReveal(offset + editText.length(), 0);
                    }
                } else {
                    // Insert one tab without relying on GroovyIndentationService
                    String editText = "\t";
                    d.replace(offset, 0, editText);
                    selectAndReveal(offset + editText.length(), 0);
                }
            } catch (Exception e) {
                GroovyPlugin.getDefault().logError("something went wrong in smart tab", e);
            }
        }
    }

    /**
     * Determine whether a given offset is in a region of text containing
     * whitespace only at the beginning of line. (Only position for which this
     * method returns true
     * should be subjected to "smart tab" processing).
     */
    private boolean isInSmartTabRegion(IDocument d, int offset) throws BadLocationException {
        String lineStartText = GroovyIndentationService.getLineTextUpto(d, offset);
        return lineStartText.trim().equals("");
    }

    // //////////////////////////////////////////////////////////////////////
    // Code below this line copied from
    // org.eclipse.jdt.internal.ui.actions.IndentAction
    /*******************************************************************************
     * Copyright (c) 2000, 2009 IBM Corporation and others.
     * All rights reserved. This program and the accompanying materials
     * are made available under the terms of the Eclipse Public License v1.0
     * which accompanies this distribution, and is available at
     * http://www.eclipse.org/legal/epl-v10.html
     *
     * Contributors:
     * IBM Corporation - initial API and implementation
     * Tom Eicher (Avaloq Evolution AG) - block selection mode
     *******************************************************************************/

    @Override
    public void update() {
        super.update();

        if (isEnabled())
            setEnabled(canModifyEditor() && isSmartMode() && isValidSelection());
    }

    /**
     * Returns if the current selection is valid, i.e. whether it is empty and
     * the caret in the
     * whitespace at the start of a line, or covers multiple lines.
     *
     * @return <code>true</code> if the selection is valid for an indent operation
     */
    private boolean isValidSelection() {
        ITextSelection selection = getSelection();
        if (selection.isEmpty())
            return false;

        int offset = selection.getOffset();
        int length = selection.getLength();

        IDocument document = getDocument();
        if (document == null)
            return false;

        try {
            IRegion firstLine = document.getLineInformationOfOffset(offset);
            int lineOffset = firstLine.getOffset();

            // either the selection has to be empty and the caret in the WS at
            // the line start
            // or the selection has to extend over multiple lines
            if (length == 0) {
                return document.get(lineOffset, offset - lineOffset).trim().length() == 0;
            } else {
                // return lineOffset + firstLine.getLength() < offset + length;
                return false; // only enable for empty selections for now
            }
        } catch (BadLocationException ignore) {
        }
        return false;
    }

    /**
     * Returns the smart preference state.
     *
     * @return <code>true</code> if smart mode is on, <code>false</code> otherwise
     */
    private boolean isSmartMode() {
        ITextEditor editor = getTextEditor();

        if (editor instanceof ITextEditorExtension3)
            return ((ITextEditorExtension3) editor).getInsertMode() == ITextEditorExtension3.SMART_INSERT;

        return false;
    }

    /**
     * Returns the document currently displayed in the editor, or
     * <code>null</code> if none can be obtained.
     *
     * @return the current document or <code>null</code>
     */
    private IDocument getDocument() {

        ITextEditor editor = getTextEditor();
        if (editor != null) {

            IDocumentProvider provider = editor.getDocumentProvider();
            IEditorInput input = editor.getEditorInput();
            if (provider != null && input != null)
                return provider.getDocument(input);
        }
        return null;
    }

    /**
     * Returns the selection on the editor or an invalid selection if none can
     * be obtained. Returns
     * never <code>null</code>.
     *
     * @return the current selection, never <code>null</code>
     */
    private ITextSelection getSelection() {
        ISelectionProvider provider = getSelectionProvider();
        if (provider != null) {

            ISelection selection = provider.getSelection();
            if (selection instanceof ITextSelection)
                return (ITextSelection) selection;
        }

        // null object
        return TextSelection.emptySelection();
    }

    /**
     * Returns the editor's selection provider.
     *
     * @return the editor's selection provider or <code>null</code>
     */
    private ISelectionProvider getSelectionProvider() {
        ITextEditor editor = getTextEditor();
        if (editor != null) {
            return editor.getSelectionProvider();
        }
        return null;
    }

    /**
     * Selects the given range on the editor.
     *
     * @param newOffset the selection offset
     * @param newLength the selection range
     */
    private void selectAndReveal(int newOffset, int newLength) {
        Assert.isTrue(newOffset >= 0);
        Assert.isTrue(newLength >= 0);
        ITextEditor editor = getTextEditor();
        if (editor instanceof JavaEditor) {
            ISourceViewer viewer = ((JavaEditor) editor).getViewer();
            if (viewer != null) {
                viewer.setSelectedRange(newOffset, newLength);
            }
        } else {
            // this is too intrusive, but will never get called anyway
            getTextEditor().selectAndReveal(newOffset, newLength);
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    // Code below copied from
    // org.eclipse.jdt.internal.ui.javaeditor.JavaEditorMessages
    /*******************************************************************************
     * Copyright (c) 2000, 2009 IBM Corporation and others.
     * All rights reserved. This program and the accompanying materials
     * are made available under the terms of the Eclipse Public License v1.0
     * which accompanies this distribution, and is available at
     * http://www.eclipse.org/legal/epl-v10.html
     *
     * Contributors:
     * IBM Corporation - initial API and implementation
     * Andre Soereng <andreis@fast.no> - [syntax highlighting] highlight numbers
     * - https://bugs.eclipse.org/bugs/show_bug.cgi?id=63573
     *******************************************************************************/
    private static final String BUNDLE_FOR_CONSTRUCTED_KEYS = "org.eclipse.jdt.internal.ui.javaeditor.ConstructedJavaEditorMessages";
    private static ResourceBundle fgBundleForConstructedKeys = ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);
}
