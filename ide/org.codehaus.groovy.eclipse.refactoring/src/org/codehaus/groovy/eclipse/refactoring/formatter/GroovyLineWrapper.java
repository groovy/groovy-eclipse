/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import java.util.List;

import groovyjarjarantlr.Token;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class GroovyLineWrapper {

    private final DefaultGroovyFormatter formatter;
    private final IFormatterPreferences preferences;
    private final LineIndentations lineIndentations;

    public GroovyLineWrapper(DefaultGroovyFormatter formatter, IFormatterPreferences preferences, LineIndentations lineIndentations) {
        this.formatter = formatter;
        this.preferences = preferences;
        this.lineIndentations = lineIndentations;
    }

    public TextEdit getLineWrapEdits() throws BadLocationException {
        MultiTextEdit lineWraps = new MultiTextEdit();
        int maxLineLength = preferences.getMaxLineLength();

        for (List<Token> tokenLine : formatter.getLineTokens()) {
            Token lastTokenOnLine = tokenLine.get(tokenLine.size() - 1);
            if (lastTokenOnLine.getColumn() >= maxLineLength) {
                int offsetInLine = 0;
                while (true) {
                    int lastToken = getLastTokenPositionUnderMaximum(tokenLine, offsetInLine);
                    if (lastToken > 0 && lastToken != tokenLine.size() - 2) {
                        int replOffset = formatter.getOffsetOfTokenEnd(tokenLine.get(lastToken));
                        int replLength = formatter.getOffsetOfToken(tokenLine.get(lastToken + 1)) - replOffset;
                        String insert = formatter.getNewLine();
                        int indentationLevel = lineIndentations.getLineIndentation(lastTokenOnLine.getLine());
                        if (!lineIndentations.isMultilineIndentation(lastTokenOnLine.getLine())) {
                            indentationLevel += preferences.getIndentationMultiline();
                        }
                        String leadingGap = formatter.getLeadingGap(indentationLevel);

                        if (replOffset >= formatter.formatOffset && replOffset + replLength <= formatter.formatOffset + formatter.formatLength) {
                            lineWraps.addChild(new ReplaceEdit(replOffset, replLength, insert + leadingGap));
                        }

                        offsetInLine = tokenLine.get(lastToken + 1).getColumn() - leadingGap.length();
                    } else {
                        break;
                    }
                }
            }
        }

        return lineWraps;
    }

    private int getLastTokenPositionUnderMaximum(List<Token> tokens, int offsetInLine) throws BadLocationException {
        for (int i = tokens.size() - 2; i >= 0; i -= 1) {
            Token token = tokens.get(i);
            if (token.getColumn() - offsetInLine + formatter.getTokenLength(token) <= preferences.getMaxLineLength()) {
                return i;
            }
        }
        return 0;
    }
}
