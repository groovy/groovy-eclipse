/*
 * Copyright 2011 the original author or authors.
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

import java.util.List;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Removes trailing semicolons as they are optional in Groovy.
 */
public class SemicolonRemover extends GroovyFormatter {

    private final GroovyDocumentScanner scanner;
    private final MultiTextEdit edits;

    public SemicolonRemover(ITextSelection sel, IDocument doc) {
        this(sel, doc, new MultiTextEdit());
    }

    public SemicolonRemover(ITextSelection sel, IDocument doc, MultiTextEdit edits) {
        super(sel, doc);
        this.edits = edits;
        this.scanner = new GroovyDocumentScanner(doc);
    }

    @Override
    public TextEdit format() {
        try {
            List<Token> tokens = scanner.getTokens(selection);
            for (Token token : tokens) {
                Token nextToken = scanner.getNextToken(token);

                if (isSemicolon(token) && (nextToken == null || isDelimiter(nextToken)))
                    addSemicolonRemoval(token);
            }
        } catch (BadLocationException e) {
            GroovyCore.logException("Cannot perform semicolon removal.", e);
        } finally {
            scanner.dispose();
        }

        return edits;
    }

    private boolean isSemicolon(Token token) {
        return token != null && token.getType() == GroovyTokenTypeBridge.SEMI;
    }

    private boolean isDelimiter(Token token) {
        List<Integer> delimiterTypes = ListUtil.list(GroovyTokenTypeBridge.RCURLY, GroovyTokenTypeBridge.NLS, GroovyTokenTypeBridge.EOF);
        return token != null && delimiterTypes.contains(token.getType());
    }

    private void addSemicolonRemoval(Token semicolonToken) throws BadLocationException {
        int semicolonOffset = scanner.getOffset(semicolonToken);
        TextEdit deleteSemicolon = new DeleteEdit(semicolonOffset, 1);
        try {
            edits.addChild(deleteSemicolon);
        } catch (MalformedTreeException e) {
            GroovyCore.logWarning("Ignoring conflicting edit: " + deleteSemicolon, e);
        }
    }
}
