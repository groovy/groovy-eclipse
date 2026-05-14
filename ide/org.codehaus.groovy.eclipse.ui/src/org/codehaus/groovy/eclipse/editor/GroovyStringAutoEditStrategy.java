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
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

/**
 * Handles auto edits inside of Groovy strings, and Groovy multi-line strings.
 */
class GroovyStringAutoEditStrategy implements IAutoEditStrategy {

    //private static final boolean DEBUG = false; // TODO: Read value using Platform.getDebugOption

    //private IAutoEditStrategy wrappee = new DefaultIndentLineAutoEditStrategy();

    GroovyStringAutoEditStrategy(final String contentType) {
    }

    @Override
    public void customizeDocumentCommand(final IDocument d, final DocumentCommand c) {
        /*if (c.text.length() > 2) {
            if (DEBUG) {
                System.out.println("Paste into a String");
            }
            return;
        } else if ("{".equals(c.text)) {
            char before;
            try {
                before = d.getChar(c.offset - 1);
                if (before == '$' && !findCloseBrace(d, c.offset)) {
                    c.text = "{}";
                    c.shiftsCaret = false;
                    c.caretOffset = c.offset + 1;
                }
            } catch (BadLocationException e) {
                // swallow
            }
        }
        wrappee.customizeDocumentCommand(d, c);*/
    }

    /**
     * Tries to find a closing brace, starting from given offset in document.
     * Stop searching when either:
     * <ul>
     * <li> reached end of document
     * <li> reached end of line
     * <li> reached an opening brace
     * <li> reached a ' or "
     * </ul>
     */
    /*private boolean findCloseBrace(final IDocument d, int i) throws BadLocationException {
        int line = d.getLineOfOffset(i);
        int endOfLine = d.getLineOffset(line) + d.getLineLength(line);
        while (i < endOfLine) {
            switch (d.getChar(i)) {
            case '}':
                return true;
            case '{':
            case '"':
            case '\'':
                return false;
            case '\\':
                i += 1; // skip next char
                break;
            default:
                break;
            }
            i += 1;
        }
        return false;
    }*/
}
