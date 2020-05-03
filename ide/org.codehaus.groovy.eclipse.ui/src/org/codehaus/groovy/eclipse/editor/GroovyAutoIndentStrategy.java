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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentationService;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.text.java.JavaAutoIndentStrategy;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * This a wrapper around a JavaAutoIndentStrategy, to which it delegates most
 * requests. However, whenever the Java strategy doesn't quite do what we want
 * it do for Groovy code, we can intercept the request and handle it ourselves.
 */
class GroovyAutoIndentStrategy implements IAutoEditStrategy {

    private final GroovyIndentationService indentService;
    private final JavaAutoIndentStrategy javaStrategy;
    private final boolean closeBraces;

    GroovyAutoIndentStrategy(final JavaAutoIndentStrategy javaStrategy) {
        this.javaStrategy = javaStrategy;
        ReflectionUtils.executePrivateMethod(JavaAutoIndentStrategy.class, "clearCachedValues", javaStrategy);
        this.closeBraces = ReflectionUtils.getPrivateField(JavaAutoIndentStrategy.class, "fCloseBrace", javaStrategy);
        this.indentService = GroovyIndentationService.get(ReflectionUtils.getPrivateField(JavaAutoIndentStrategy.class, "fProject", javaStrategy));
    }

    @Override
    public void customizeDocumentCommand(final IDocument d, final DocumentCommand c) {
        if (c.doit) {
            try {
                if (c.length == 0 && isNewline(d, c.text)) {
                    autoEditAfterNewline(d, c);
                } else if (c.text.length() > 2) {
                    smartPaste(d, c);
                } else if ("{".equals(c.text) || "}".equals(c.text)) {
                    // delegate for some simple cases like braces
                    javaStrategy.customizeDocumentCommand(d, c);
                }
            } finally {
                // ensures that prefs will refresh each time they are needed; also saves a little bit of memory
                indentService.disposePrefs();
            }
        }
    }

    /**
     * This method is called when pasting text into the editor. It can decide to
     * modify the command, for example to adjust indentation of the pasted text.
     */
    private void smartPaste(final IDocument d, final DocumentCommand c) {
        try {
            if (indentService.getPrefs().isSmartPaste() && indentService.isInEmptyLine(d, c.offset)) {
                int pasteLine = d.getLineOfOffset(c.offset);
                IRegion pasteLineRegion = d.getLineInformation(pasteLine);

                Document workCopy = new Document(d.get(0, pasteLineRegion.getOffset()));
                workCopy.replace(pasteLineRegion.getOffset(), 0, c.text);

                int startLine = workCopy.getLineOfOffset(pasteLineRegion.getOffset());
                int endLine = workCopy.getLineOfOffset(pasteLineRegion.getOffset() + c.text.length());

                int indentDiff = 0;

                boolean isMultiLineComment = false, isMultiLineString = false;
                for (int line = startLine; line <= endLine; line += 1) {
                    IRegion lineRegion = workCopy.getLineInformation(line);
                    String text = workCopy.get(lineRegion.getOffset(), lineRegion.getLength());

                    if (line - startLine < 2) {
                        // For first two lines use indentation logic to move the
                        // lines
                        int oldIndentLevel = indentService.getLineIndentLevel(workCopy, line);
                        int newIndentLevel = indentService.computeIndentForLine(workCopy, line);

                        if (isMultiLineComment) {
                            newIndentLevel++;
                            indentService.fixIndentation(workCopy, line, newIndentLevel);
                        } else if (!isMultiLineString) {
                            indentService.fixIndentation(workCopy, line, newIndentLevel);
                        }
                        indentDiff = newIndentLevel - oldIndentLevel;
                    } else {
                        int oldIndentLevel = indentService.getLineIndentLevel(workCopy, line);
                        int newIndentLevel = oldIndentLevel + indentDiff;
                        if (isMultiLineComment) {
                            indentService.fixIndentation(workCopy, line, newIndentLevel);
                        } else if (!isMultiLineString) {
                            indentService.fixIndentation(workCopy, line, newIndentLevel);
                        }
                    }

                    if (text.indexOf("/*") != -1) {
                        isMultiLineComment = true;
                    }
                    if ((text.indexOf("*/") != -1) && isMultiLineComment) {
                        isMultiLineComment = false;
                    } else if (((text.indexOf("\"\"\"") != -1) || (text.indexOf("'''") != -1)) && !isMultiLineComment) {
                        isMultiLineString = !isMultiLineString;
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
        } catch (Exception e) {
            GroovyPlugin.getDefault().logError("Something went wrong in GroovyAutoIndentStrategy.smartPaste", e);
        }
    }

    private boolean isNewline(final IDocument d, final String s) {
        String[] delimiters = d.getLegalLineDelimiters();
        if (delimiters != null) {
            for (String nl : delimiters) {
                if (nl.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Applies edits upon newline presses.
     */
    private void autoEditAfterNewline(final IDocument d, final DocumentCommand c) {
        try {
            int orgIndentLevel = indentService.getIndentLevel(d, c.offset);

            // Add indentation
            int indentLevel = indentService.computeIndentAfterNewline(d, c.offset);
            String indentation = indentService.createIndentation(indentLevel);
            c.text += indentation;

            // Add closing brace
            if (closeBraces) {
                int lengthToCurly = indentService.lengthToNextCurly(d, c.offset);
                if (shouldInsertBrace(d, c.offset, lengthToCurly > 0)) {
                    // munch all chars from the insertion point to the curly brace (if one already exists)
                    c.length = lengthToCurly;
                    int newCaret = c.offset + c.text.length();
                    c.text += indentService.newline(d) + indentService.createIndentation(orgIndentLevel) + "}";
                    c.caretOffset = newCaret;
                    c.shiftsCaret = false;
                }
            }
        } catch (Exception e) {
            // This is a fail safe, in case anything goes wrong. We should
            // return normally. This way the edit should still be able to
            // proceed, but without any "smart" auto edits being applied.
            GroovyPlugin.getDefault().logError("Something went wrong in GroovyAutoIndentStrategy.autoEditAfterNewline", e);
        }
    }

    /**
     * Determines whether an offset in a document would be a good one
     * to insert an automatic closing brace for on the next line.
     */
    private boolean shouldInsertBrace(final IDocument d, final int enterPos, final boolean nextTokenIsCloseBrace) throws BadLocationException {
        if (indentService.moreOpenThanCloseBefore(d, enterPos) &&
                (nextTokenIsCloseBrace || indentService.isEndOfLine(d, enterPos))) {
            int lineNum = d.getLineOfOffset(enterPos);
            int indentLevel = indentService.getLineIndentLevel(d, lineNum);
            String line;
            do {
                line = GroovyIndentationService.getLine(d, ++lineNum);
                line = line.trim();
            } while (line.isEmpty() && lineNum < d.getNumberOfLines());
            int nextIndentLevel = indentService.getLineIndentLevel(d, lineNum);
            if (nextIndentLevel > indentLevel)
                return false;
            if (nextIndentLevel < indentLevel)
                return true;
            return !line.startsWith("}");
        }
        return false;
    }
}
