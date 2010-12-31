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

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentationService;
import org.codehaus.groovy.eclipse.refactoring.formatter.IFormatterPreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.text.java.JavaAutoIndentStrategy;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * This a wrapper around a JavaAutoIndentStrategy, to which it delegates most
 * requests. However, whenever the Java strategy doesn't
 * quite do what we want it do for Groovy code, we can intercept the request and
 * handle it ourselves.
 *
 * @author kdvolder
 * @created 2010-05-19
 */
public class GroovyAutoIndentStrategy extends AbstractAutoEditStrategy {

    private JavaAutoIndentStrategy javaStrategy;
    private boolean closeBraces;

    private GroovyIndentationService indentor;

    public GroovyAutoIndentStrategy(String contentType, JavaAutoIndentStrategy javaStrategy) {
        this.javaStrategy = javaStrategy;
        ReflectionUtils.executePrivateMethod(JavaAutoIndentStrategy.class, "clearCachedValues", new Class<?>[0], javaStrategy,
                new Object[0]);
        this.indentor = new GroovyIndentationService((IJavaProject) ReflectionUtils.getPrivateField(JavaAutoIndentStrategy.class,
                "fProject", javaStrategy));
        this.closeBraces = (Boolean) ReflectionUtils.getPrivateField(JavaAutoIndentStrategy.class, "fCloseBrace", javaStrategy);
    }

    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
        try {
            if (c.doit == false)
                return;

            if (c.length == 0 && c.text != null && isNewline(d, c.text)) {
                autoEditAfterNewline(d, c);
            } else {
                if (c.text.length() > 2) {
                    smartPaste(d, c);
                }
                if ("}".equals(c.text)) {
                    // We know that the javaStrategy works reasonably well for
                    // "}"
                    // so just use that disable smartness for anything else by
                    // not
                    // passing the command on to the javaStrategy.
                    // Note that this also disables "smart paste".
                    javaStrategy.customizeDocumentCommand(d, c);
                }
            }
        } finally {
            // This ensures that we will refresh prefs each time we need them,
            // also
            // it saves a little bit of memory.
            indentor.disposePrefs();
        }
    }
    /**
     * Get our formatter related preferences.
     */
    private IFormatterPreferences getPrefs() {
        return indentor.getPrefs();
    }

    /**
     * This method is called when pasting text into the editor. It can decide to
     * modify the command, for example to adjust indentation of the pasted text.
     *
     * @throws BadLocationException
     */
    private void smartPaste(IDocument d, DocumentCommand c) {
        try {
            if (getPrefs().isSmartPaste() && c.length == 0 && indentor.isInEmptyLine(d, c.offset)) {
                int pasteLine = d.getLineOfOffset(c.offset);
                IRegion pasteLineRegion = d.getLineInformation(pasteLine);

                Document workCopy = new Document(d.get(0, pasteLineRegion.getOffset()));
                workCopy.replace(pasteLineRegion.getOffset(), 0, c.text);

                int startLine = workCopy.getLineOfOffset(pasteLineRegion.getOffset());
                int endLine = workCopy.getLineOfOffset(pasteLineRegion.getOffset() + c.text.length());

                int indentDiff = 0;
                for (int line = startLine; line <= endLine; line++) {
                    if (line - startLine < 2) {
                        // For first two lines use indentation logic to move the
                        // lines
                        int oldIndentLevel = indentor.getLineIndentLevel(workCopy, line);
                        int newIndentLevel = indentor.computeIndentForLine(workCopy, line);
                        indentor.fixIndentation(workCopy, line, newIndentLevel);
                        indentDiff = newIndentLevel - oldIndentLevel;
                    } else {
                        int oldIndentLevel = indentor.getLineIndentLevel(workCopy, line);
                        int newIndentLevel = oldIndentLevel + indentDiff;
                        indentor.fixIndentation(workCopy, line, newIndentLevel);
                    }
                }

                // Put the "smart" adjusted paste into the command
                int workStart = workCopy.getLineOffset(startLine);
                int workEnd = workCopy.getLineOffset(endLine) + workCopy.getLineLength(endLine);

                c.text = workCopy.get(workStart, workEnd - workStart);
                c.offset = pasteLineRegion.getOffset();
                c.length = pasteLineRegion.getLength();
                c.caretOffset = c.offset + c.text.length();
                c.shiftsCaret = false;
            }
        } catch (Throwable e) {
            GroovyCore.logException("Something went wrong in smartPaste", e);
        }
    }

    /**
     * Apply autoedits upon newline presses.
     */
    private void autoEditAfterNewline(IDocument d, DocumentCommand c) {
        try {
            int orgIndentLevel = indentor.getIndentLevel(d, c.offset);

            // Add indentation
            int indentLevel = indentor.computeIndentAfterNewline(d, c.offset);
            String indentation = indentor.createIndentation(indentLevel);
            c.text = c.text + indentation;

            // Add closing brace
            if (closeBraces && shouldInsertBrace(d, c.offset)) {
                int newCaret = c.offset + c.text.length();
                c.text = c.text + indentor.newline(d) + indentor.createIndentation(orgIndentLevel) + "}";
                c.caretOffset = newCaret;
                c.shiftsCaret = false;
            }

        } catch (Throwable e) {
            // This is a fail safe, in case anything goes wrong. We should
            // return normally. This way the edit should still be able to
            // proceed, but without any "smart" auto edits being applied.
            GroovyCore.logException("Something went wrong in Groovy autoEditAfterNewline", e);
            return;
        }
    }

    /**
     * Determine whether an offset in a document would be a good one
     * to insert an automatic closing brace for on the next line.
     */
    private boolean shouldInsertBrace(IDocument d, int enterPos) {
        if (!indentor.isAfterOpeningBrace(d, enterPos))
            return false;
        if (!indentor.isEndOfLine(d, enterPos))
            return false;
        try {
            int lineNum = d.getLineOfOffset(enterPos);
            int indentLevel = indentor.getLineIndentLevel(d, lineNum);
            String line;
            do {
                line = GroovyIndentationService.getLine(d, ++lineNum);
                line = line.trim();
            } while (line.equals("") && lineNum < d.getNumberOfLines());
            int nextIndentLevel = indentor.getLineIndentLevel(d, lineNum);
            if (nextIndentLevel > indentLevel)
                return false;
            if (nextIndentLevel < indentLevel)
                return true;
            return !line.startsWith("}");
        } catch (BadLocationException e) {
            GroovyCore.logException("internal error", e);
            return false;
        }
    }

}