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
package org.codehaus.groovy.eclipse.refactoring.core.convert;

import javax.swing.text.BadLocationException;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Common class to process the convert to closure refactoring.
 * Used by both the completion proposal and the refactor menu option.
 *
 * @author Geoff Denning
 * @created Nov 15, 2011
 */
// FIXGWD: This class should be converted into a proper refactoring class which
// extends Refactoring.
public class ConvertToClosureRefactoring {

    private final IMethod targetMethod;

    public ConvertToClosureRefactoring(GroovyCompilationUnit unit, int offset) {
        targetMethod = findMethod(unit, offset);
    }

    private IMethod findMethod(GroovyCompilationUnit unit, int offset) {
        if (unit.isOnBuildPath()) {
            return null;
        }
        try {
            IJavaElement maybeMethod = unit.getElementAt(offset);

            if (!(maybeMethod instanceof IMethod)) {
                return null;
            }

            ISourceRange nameRange = ((IMethod) maybeMethod).getNameRange();
            if (nameRange.getLength() == maybeMethod.getElementName().length()) {
                return ((IMethod) maybeMethod);
            }

            // For quoted method names, the name range will include the quotes,
            // but
            // the name itself will not include them
            // check the text to see if the name start is at a quote
            char[] contents = unit.getContents();
            if (contents.length > nameRange.getOffset() && contents[nameRange.getOffset()] == '"') {
                return ((IMethod) maybeMethod);
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Error finding enclosing method for refactoring", e);
        }
        return null;
    }

    public void applyRefactoring(IDocument document) {
        if (targetMethod != null) {
            TextEdit thisEdit = findReplacement(document, targetMethod);
            try {
                if (thisEdit != null) {
                    thisEdit.apply(document);
                }
            } catch (Exception e) {
                GroovyCore.logException("Oops.", e);
            }
        }
    }

    public boolean isApplicable() {
        return targetMethod != null && targetMethod.exists();
    }

    private TextEdit findReplacement(IDocument doc, IMethod targetMethod) {
        try {
            ISourceRange nameRange = targetMethod.getSourceRange();
            // find the opening parnn and the closing paren
            int openingParen = findOpenParen(doc, targetMethod, nameRange);
            int closingParen = findCloseParen(doc, openingParen);
            int openingBracket = findOpenBracket(doc, closingParen);
            return createEdit(doc, targetMethod, openingParen, closingParen, openingBracket);
        } catch (Exception e) {
            GroovyCore.logException("Exception during convert to closure.", e);
            return null;
        }
    }

    /**
     * @return finds the first open paren after the name ends
     * @throws BadLocationException
     * @throws org.eclipse.jface.text.BadLocationException
     */
    private int findOpenParen(IDocument doc, IMethod targetMethod, ISourceRange nameRange) throws BadLocationException, org.eclipse.jface.text.BadLocationException {
        int offset = nameRange.getOffset() + targetMethod.getElementName().length();
        while (offset < doc.getLength() && doc.getChar(offset) != '(') {
            offset++;
        }
        return offset;
    }

    private int findOpenBracket(IDocument doc, int closingParen) throws BadLocationException, org.eclipse.jface.text.BadLocationException {
        int offset = closingParen;
        while (offset < doc.getLength() && doc.getChar(offset) != '{') {
            offset++;
        }
        return offset;
    }

    private int findCloseParen(IDocument doc, int open) throws BadLocationException, org.eclipse.jface.text.BadLocationException {
        int offset = open;
        while (offset < doc.getLength() && doc.getChar(offset) != ')') {
            offset++;
        }
        return offset;
    }

    private TextEdit createEdit(IDocument doc, IMethod targetMethod, int openingParen, int closingParen, int openingBracket)
            throws BadLocationException, org.eclipse.jface.text.BadLocationException {
        if (!(openingParen < doc.getLength() && doc.getChar(openingParen) == '(')) {
            return null;
        }
        if (!(closingParen < doc.getLength() && doc.getChar(closingParen) == ')')) {
            return null;
        }
        if (!(openingBracket < doc.getLength() && doc.getChar(openingBracket) == '{')) {
            return null;
        }
        TextEdit edit = new MultiTextEdit();
        if (targetMethod.getParameterTypes().length > 0) {
            edit.addChild(new ReplaceEdit(openingParen, 1, " = { "));
            edit.addChild(new ReplaceEdit(closingParen, openingBracket - closingParen + 1, " ->"));
        } else {
            edit.addChild(new ReplaceEdit(openingParen, 1, " = {"));
            edit.addChild(new DeleteEdit(closingParen, openingBracket - closingParen + 1));
        }
        return edit;
    }
}
