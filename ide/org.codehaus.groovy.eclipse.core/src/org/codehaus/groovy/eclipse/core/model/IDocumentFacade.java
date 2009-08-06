 /*
 * Copyright 2003-2009 the original author or authors.
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