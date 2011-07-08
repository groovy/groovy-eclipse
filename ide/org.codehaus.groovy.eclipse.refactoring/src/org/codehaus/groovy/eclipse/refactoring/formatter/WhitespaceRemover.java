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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Removes trailing whitespaces.
 */
public class WhitespaceRemover extends GroovyFormatter {

    private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("\\s+$");
    private final MultiTextEdit edits;

    public WhitespaceRemover(ITextSelection sel, IDocument doc) {
        this(sel, doc, new MultiTextEdit());
    }

    public WhitespaceRemover(ITextSelection sel, IDocument doc, MultiTextEdit edits) {
        super(sel, doc);
        this.edits = edits;
    }

    @Override
    public TextEdit format() {
        try {
            for (int i = 0; i < document.getNumberOfLines(); i++) {
                IRegion lineInfo = document.getLineInformation(i);
                String line = document.get(lineInfo.getOffset(), lineInfo.getLength());

                Matcher matcher = TRAILING_WHITESPACE_PATTERN.matcher(line);

                if (matcher.find()) {
                    int whitespaceOffset = lineInfo.getOffset() + matcher.start();
                    int whitespaceLength = matcher.end() - matcher.start();
                    addWhitespaceRemoval(whitespaceOffset, whitespaceLength);
                }
            }
        } catch (BadLocationException e) {
            GroovyCore.logException("Cannot perform whitespace removal.", e);
        }

        return edits;
    }

    private void addWhitespaceRemoval(int whitespaceOffset, int whitespaceLength) {
        TextEdit deleteWhitespace = new DeleteEdit(whitespaceOffset, whitespaceLength);
        try {
            edits.addChild(deleteWhitespace);
        } catch (MalformedTreeException e) {
            GroovyCore.logWarning("Ignoring conflicting edit: " + deleteWhitespace, e);
        }
    }
}
