/*
 * Copyright 2009-2022 the original author or authors.
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

import java.util.Iterator;

import groovyjarjarantlr.Token;
import org.codehaus.groovy.antlr.GroovyTokenTypeBridge;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.DeleteEdit;
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
            for (Iterator<Token> it = scanner.getTokens(selection).iterator(); it.hasNext();) {
                Token token = it.next();
                if (token.getType() == GroovyTokenTypeBridge.LITERAL_enum) {
                    int block = 0;
                    while (it.hasNext()) {
                        token = it.next();
                        if (token.getType() == GroovyTokenTypeBridge.SEMI) {
                            // first semicolon in body terminates enum constants
                            if (block != 1) {
                                checkOptionalSemicolon(token);
                            } else {
                                Token t = token;
                                while ((t = scanner.getNextToken(t)) != null &&
                                    (t.getType() == GroovyTokenTypeBridge.NLS ||
                                     t.getType() == GroovyTokenTypeBridge.SL_COMMENT ||
                                     t.getType() == GroovyTokenTypeBridge.ML_COMMENT)){
                                }
                                if (t.getType() != GroovyTokenTypeBridge.IDENT) break;
                            }
                        } else if (token.getType() == GroovyTokenTypeBridge.LCURLY) {
                            ++block;
                        } else if (token.getType() == GroovyTokenTypeBridge.RCURLY) {
                            if (--block < 1) break;
                        }
                    }
                }
                checkOptionalSemicolon(token);
            }
        } catch (BadLocationException e) {
            GroovyCore.logException("Cannot perform semicolon removal.", e);
        } finally {
            scanner.dispose();
        }

        return edits;
    }

    private void checkOptionalSemicolon(Token token) throws BadLocationException {
        if (isOptionalSemicolon(token)) {
            edits.addChild(new DeleteEdit(scanner.getOffset(token), 1));
        }
    }

    private boolean isOptionalSemicolon(Token token) throws BadLocationException {
        if (token.getType() == GroovyTokenTypeBridge.SEMI) {
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
