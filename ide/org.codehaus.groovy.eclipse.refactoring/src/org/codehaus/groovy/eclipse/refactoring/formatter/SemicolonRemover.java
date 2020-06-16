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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import groovyjarjarantlr.Token;
import org.codehaus.groovy.antlr.GroovyTokenTypeBridge;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Removes trailing semicolons as they are mostly optional in Groovy.
 */
public class SemicolonRemover extends GroovyFormatter {

    private final MultiTextEdit edits;
    private final GroovyDocumentScanner scanner;

    public SemicolonRemover(final ITextSelection sel, final IDocument doc) {
        this(sel, doc, new MultiTextEdit());
    }

    public SemicolonRemover(final ITextSelection sel, final IDocument doc, final MultiTextEdit edits) {
        super(sel, doc);
        this.edits = edits;
        this.scanner = new GroovyDocumentScanner(doc);
    }

    @Override
    public TextEdit format() {
        try {
            for (Token token : scanner.getTokens(selection)) {
                if (isOptionalSemicolon(token)) {
                    TextEdit removeSemicolon = new DeleteEdit(scanner.getOffset(token), 1);
                    try {
                        edits.addChild(removeSemicolon);
                    } catch (MalformedTreeException ignore) {
                    }
                }
            }
        } catch (BadLocationException e) {
            GroovyCore.logException("Cannot perform semicolon removal.", e);
        } finally {
            scanner.dispose();
        }

        return edits;
    }

    private boolean isOptionalSemicolon(Token token) throws BadLocationException {
        if (token != null && token.getType() == GroovyTokenTypeBridge.SEMI) {
            token = scanner.getNextToken(token);
            if (token != null) {
                int type = token.getType();
                if (type == GroovyTokenTypeBridge.NLS) {
                    while ((token = scanner.getNextToken(token)) != null &&
                        (type = token.getType()) == GroovyTokenTypeBridge.NLS) {
                    }
                    // semicolon may prevent treating next expression as method call argument
                    if (type != GroovyTokenTypeBridge.LPAREN && type != GroovyTokenTypeBridge.LCURLY && type != GroovyTokenTypeBridge.LBRACK) {
                        type = GroovyTokenTypeBridge.NLS;
                    }
                }
                return type == GroovyTokenTypeBridge.RCURLY || type == GroovyTokenTypeBridge.SEMI || type == GroovyTokenTypeBridge.NLS || type == GroovyTokenTypeBridge.EOF;
            }
        }
        return false;
    }
}
