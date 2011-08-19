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
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

/**
 * This class is responsible for handling auto edits inside of Groovy
 * strings, and Groovy Multiline Strings.
 * <p>
 * The present implementation simply delegates all requests to a
 * DefaultIndentLineAutoEditStrategy. This should be reasonable since that
 * strategy is meant for editing text, which is mostly what should be inside a
 * String.
 *
 * @author kdvolder
 * @created 2010-05-19
 */
public class GroovyMultilineStringAutoEditStrategy extends AbstractAutoEditStrategy {
    // FIXKDV: This class should be renamed. It now handles both multiline and
    // single line strings (not doing that now, since SVN seems to get confused
    // by rename refactorings and creates messed-up patch files.)

    private static final boolean DEBUG = false;

    private IAutoEditStrategy wrappee = new DefaultIndentLineAutoEditStrategy();

    public GroovyMultilineStringAutoEditStrategy(String contentType) {
    }

    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
        if (true)
            return;

        if (c.text.length() > 2) {
            if (DEBUG) {
                System.out.println("Paste into a String");
            }
            return;
        }
        else if ("{".equals(c.text)) {
            char before;
            try {
                before = d.getChar(c.offset - 1);
                if (before == '$' && !findCloseBrace(d, c.offset)) {
                    c.text = "{}";
                    c.shiftsCaret = false;
                    c.caretOffset = c.offset + 1;
                }
            } catch (BadLocationException e) {
                /* swallow */
            }
        }
        wrappee.customizeDocumentCommand(d, c);
    }

    /**
     * Try to find a closing brace, starting from given offset in document. Stop
     * searching
     * when either:
     * - reached end of document
     * - reached end of line
     * - reached an opening brace
     * - reached a ' or "
     *
     * @param offset
     * @throws BadLocationException
     */
    private boolean findCloseBrace(IDocument d, int offset) throws BadLocationException {
        int line = d.getLineOfOffset(offset);
        int endOfLine = d.getLineOffset(line) + d.getLineLength(line);
        while (offset < endOfLine) {
            switch (d.getChar(offset)) {
                case '}':
                    return true;
                case '{':
                case '"':
                case '\'':
                    return false;
                case '\\':
                    offset++; // skip next char
                    break;
                default:
                    break;
            }

            offset++;
        }
        return false;
    }

}
