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

package org.codehaus.groovy.eclipse.core.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * @author Andrew Eisenberg
 * @created May 26, 2009
 *
 */
public interface IDocumentFacade {

    public abstract IDocument getDocument();

    public abstract int getLineOffset(int row) throws BadLocationException;

    public abstract int getOffset(int row, int col) throws BadLocationException;

//    public abstract Point getRowCol(int offset) throws BadLocationException;

    public abstract String getText(int offset, int len)
            throws BadLocationException;

    public abstract int getLineLength(int row) throws BadLocationException;

    public abstract String getLine(int row) throws BadLocationException;

    public abstract List<String> getLines(int startRow, int endRow)
            throws BadLocationException;

    public abstract IFile getFile();

    public <T> T adapt(Class<T> type);
    
    public GroovyProjectFacade getProjectFacade();
}