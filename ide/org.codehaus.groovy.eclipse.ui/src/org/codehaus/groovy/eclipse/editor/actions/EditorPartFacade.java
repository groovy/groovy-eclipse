/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.editor.actions;


import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Through IEditorPart, one can access the editor, document, selection and many
 * other editing related object. However this is not trivial when new to
 * Eclipse.
 * 
 * This facade contains many methods which are much simpler to deal with. Its
 * code also serves as a good place to see how to get the different objects
 * directly through Eclipse APIs.
 * 
 * <b>This is only intended to be used with GroovyEclipse where the IEditorPart
 * is related to a GroovyEditor.</b>
 */
public class EditorPartFacade implements IDocumentFacade {
	protected IEditorPart editor;

	public EditorPartFacade() {

	}

	public EditorPartFacade(IEditorPart editor) {
		this.editor = editor;
	}

	public void setEditor(IEditorPart editor) {
		this.editor = editor;
	}

	public IEditorPart getEditor() {
		return editor;
	}

	public ITextEditor getTextEditor() {
		// IllegalStateException c_tor is 1.5 only, leaving it as
		// ClassCastException
		// until I decide what is needed. It took until 1.5 to get this c_tor?
		// -emp
		// try {
		return (ITextEditor) editor.getAdapter(ITextEditor.class);
		// } catch (ClassCastException e) {
		// throw new IllegalStateException("Expecting a text editor, found "
		// + editor.getClass().getName(), e);
		// }
	}

	public Shell getShell() {
		return editor.getEditorSite().getShell();
	}

	/**
	 * Shows a message dialog. Handy while developing.
	 * 
	 * @param title
	 * @param message
	 */
	public void showMessage(String message) {
		showMessage("", message);
	}

	public void showMessage(String title, String message) {
		MessageDialog.openInformation(getShell(), title, message);
	}

	public IDocument getDocument() {
		IDocumentProvider provider = getTextEditor().getDocumentProvider();
		if (provider != null)
			return provider.getDocument(getTextEditor().getEditorInput());
		return null;
	}

	public String getDelimiter() {
		return TextUtilities.getDefaultLineDelimiter(getDocument());
	}

	public int getDelimiterLength() {
		return TextUtilities.getDefaultLineDelimiter(getDocument()).length();
	}

	public ITextSelection getTextSelection() {
		return (ITextSelection) getTextEditor().getSelectionProvider()
				.getSelection();
	}

	public int getLineOffset(int row) throws BadLocationException {
		return getDocument().getLineOffset(row);
	}

	public int getOffset(int row, int col) throws BadLocationException {
		return getDocument().getLineOffset(row) + col;
	}

//	public Point getRowCol(int offset) throws BadLocationException {
//		int row = getDocument().getLineOfOffset(offset);
//		int col = offset - getLineOffset(row);
//		return new Point(col, row);
//	}

	public String getText(int offset, int len) throws BadLocationException {
		return getDocument().get(offset, len);
	}

	public void replaceText(int offset, int len, String text)
			throws BadLocationException {
		getDocument().replace(offset, len, text);
	}

	public void removeText(int offset, int len) throws BadLocationException {
		replaceText(offset, len, "");
	}

	public void insertText(int offset, String text) throws BadLocationException {
		getDocument().replace(offset, 0, text);
	}

	public int getLineLength(int row) throws BadLocationException {
		return getDocument().getLineInformation(row).getLength();
	}

	public String getLine(int row) throws BadLocationException {
		IRegion region = getDocument().getLineInformation(row);
		return getDocument().get(region.getOffset(), region.getLength());
	}

	public List< String > getLines(int startRow, int endRow) throws BadLocationException {
		List<String> list = new ArrayList<String>();
		for (int i = startRow; i <= endRow; ++i) {
			list.add(getLine(i));
		}
		return list;
	}

	public String removeLine(int row) throws BadLocationException {
		IRegion region = getDocument().getLineInformation(row);
		String ret = getLine(row);
		removeText(region.getOffset(), region.getLength()
				+ getDelimiterLength());
		return ret;
	}

	public List< String > removeLines(int startRow, int endRow)
			throws BadLocationException {
	    List<String> list = new ArrayList<String>();
	    for (int i = startRow; i <= endRow; ++i) {
			list.add(removeLine(i));
		}
		return list;
	}

	public void insertLine(int row, String line) throws BadLocationException {
		int offset = getLineOffset(row);
		replaceText(offset, 0, line + getDelimiterLength());
	}

	public void insertLines(int row, List< String > lines) throws BadLocationException {
		for (int i = row; i < row + lines.size(); ++i)
			insertLine(i, lines.get(i));
	}

	/**
	 * If there is any partial selection, get the complete lines that the
	 * selection partially covers.
	 * 
	 * @return A list of String of selected lines, which is empty if there is no
	 *         selection.
	 * @throws BadLocationException
	 */
	public List< String > getSelectedLines() throws BadLocationException {
		ITextSelection sel = getTextSelection();
		return getLines(sel.getStartLine(), sel.getEndLine());
	}

	public String getCurrentLine() throws BadLocationException {
		ITextSelection sel = getTextSelection();
		return getLine(sel.getStartLine());
	}

	public void select(int offset, int length) {
		ISelection sel = new TextSelection(offset, length);
		getTextEditor().getSelectionProvider().setSelection(sel);
	}

	//FIXME remove IFile dependency
  public IFile getFile() {
    return (IFile) getEditor().getEditorInput().getAdapter(IFile.class);
  }

  public Object adaptEditorInput(Class< ? > adapter) {
    return getEditor().getEditorInput().getAdapter(adapter);
  }

    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        return (T) getEditor().getAdapter(type);
    }

    public GroovyProjectFacade getProjectFacade() {
        IFile file = getFile();
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        return new GroovyProjectFacade(unit);
    }
}
