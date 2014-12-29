/*
 * Copyright (C) 2014, 2015 Hidetoshi Ayabe
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import groovyjarjarantlr.Token;

import org.codehaus.groovy.eclipse.refactoring.core.utils.FormatterOffUtils;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * @author Hidetoshi Ayabe
 */
public class FomatterOffHandler {

    private DefaultGroovyFormatter formatter;

    public FomatterOffHandler(DefaultGroovyFormatter formatter) {
        this.formatter = formatter;
    }

    public void protectIgnoredLines(MultiTextEdit edits) {
        GroovyDocumentScanner tokens = formatter.getTokens();
        try {
            Token last = tokens.getLastToken();
            for (Token token = tokens.getLastTokenBefore(1); token != null && token != last; token = tokens.getNextToken(token)) {
                int start = 0, end = 0;
                if (FormatterOffUtils.matchFormatterOff(token.getText())) {
                    start = formatter.getOffsetOfToken(token);
                    for (token = tokens.getNextToken(token); token != null && token != last; token = tokens.getNextToken(token)) {
                        token.setType(Token.SKIP);
                        if (FormatterOffUtils.matchFormatterOn(token.getText())) {
                            break;
                        }
                    }
                    if (token == null) {
                        token = tokens.getLastToken();
                    }
                    end = formatter.getOffsetOfToken(token) + formatter.getTokenLength(token);
                    ignoreFormatting(start, end, edits);
                }
            }
        } catch (BadLocationException e) {
            Util.log(e);
        }
    }

    private void ignoreFormatting(final int start, final int end, MultiTextEdit edits) throws BadLocationException {
        String text = formatter.getTokens().getDocument().get(start, end - start);
        edits.addChild(new ReplaceEdit(start, end - start, text));
    }
}
