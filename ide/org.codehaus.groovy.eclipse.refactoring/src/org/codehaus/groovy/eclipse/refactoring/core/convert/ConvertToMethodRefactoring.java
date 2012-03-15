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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Common class to process the convert to method refactoring.
 * Used by both the completion proposal and the refactor menu option.
 *
 * @author Geoff Denning
 * @created Nov 15, 2011
 */
// FIXGWD: This class should be converted into a proper refactoring class which
// extends Refactoring.
public class ConvertToMethodRefactoring {

    private final FieldNode targetField;

    private IField field;

    public ConvertToMethodRefactoring(GroovyCompilationUnit unit, int offset) {
        this.targetField = getTargetField(unit, offset);
    }

    private FieldNode getTargetField(GroovyCompilationUnit unit, int offset) {
        if (unit.isOnBuildPath()) {
            return null;
        }
        try {
            FieldNode targetField = null;
            IJavaElement maybeField = unit.getElementAt(offset);
            boolean result = maybeField instanceof IField && ((IField) maybeField).getNameRange().getOffset() > 0;
            if (result) {
                field = (IField) maybeField;

                // now check to see if the field is assigned to a closure
                for (ClassNode clazz : unit.getModuleNode().getClasses()) {
                    if (clazz.getNameWithoutPackage().equals(maybeField.getParent().getElementName())) {
                        targetField = clazz.getDeclaredField(field.getElementName());
                        if (targetField != null && targetField.getInitialExpression() instanceof ClosureExpression) {
                            result = true;
                            break;
                        }
                    }
                }
            }

            if (result) {
                return targetField;
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Oops", e);
        }
        return null;
    }

    public boolean isApplicable() {
        return targetField != null && field != null && field.exists();
    }

    public void applyRefactoring(IDocument document) {
        if (targetField != null) {
            TextEdit thisEdit = findReplacement(document);
            try {
                if (thisEdit != null) {
                    thisEdit.apply(document);
                }
            } catch (Exception e) {
                GroovyCore.logException("Oops.", e);
            }
        }
    }

    private TextEdit findReplacement(IDocument doc) {
        try {
            // find the opening parnn and the closing paren
            int afterName = targetField.getNameEnd() + 1;
            int openingBracket = findOpenBracket(doc, targetField, afterName);
            int afterLastParam = findAfterLastParam(targetField);  // -1 means that there are no params
            int afterArrow = findAfterArrow(doc, afterLastParam);  // -1 means that there are no params
            return createEdit(doc, afterName, openingBracket, afterLastParam, afterArrow);
        } catch (Exception e) {
            GroovyCore.logException("Exception during convert to closure.", e);
            return null;
        }
    }

    private static int findOpenBracket(IDocument doc, FieldNode targetField, int afterName) throws BadLocationException {
        int offset = afterName;
        while (offset < doc.getLength() && doc.getChar(offset) != '{') {
            offset++;
        }

        Parameter[] parameters = getClosureParameters(targetField);
        if (parameters != null && parameters.length > 0) {
            int firstParameterStart = parameters[0].getStart();
            offset++;
            // also eat up whitespace after the '{'
            while (offset < firstParameterStart && Character.isWhitespace(doc.getChar(offset))) {
                offset++;
            }
            offset--;
        }
        return offset;
    }

    /**
     * @return the index after the last parameter, or -1 if no parameters
     */
    private static int findAfterLastParam(FieldNode targetField) {
        Parameter[] parameters = getClosureParameters(targetField);
        if (parameters == null || parameters.length == 0) {
            return -1;
        } else {
            Parameter lastParam = parameters[parameters.length - 1];
            if (lastParam.getInitialExpression() != null) {
                // hmmm...this is always null even when there are default values.   Maybe doesn't work?
                return lastParam.getInitialExpression().getEnd();
            } else {
                return lastParam.getNameEnd();
            }
        }
    }

    private static Parameter[] getClosureParameters(FieldNode targetField) {
        ClosureExpression closure = (ClosureExpression) targetField.getInitialExpression();
        Parameter[] parameters = closure.getParameters();
        return parameters;
    }

    /**
     * @param doc
     * @param afterLastParam
     * @return the value after the end of the arrow or -1 if no arguments
     * @throws BadLocationException
     */
    private static int findAfterArrow(IDocument doc, int afterLastParam) throws BadLocationException {
        if (afterLastParam == -1) {
            return -1;
        }
        int offset = afterLastParam;
        while (offset < doc.getLength() - 1) {
            if (doc.getChar(offset) == '-' && doc.getChar(offset+1) == '>') {
                return ++offset;
            }
            offset++;
        }
        return -1;
    }

    private static TextEdit createEdit(IDocument doc, int afterName,
            int openingBracket, int afterLastParam, int afterArrow) throws BadLocationException {
        if (!(openingBracket < doc.getLength() && (doc.getChar(openingBracket) == '{') || Character.isWhitespace(doc.getChar(openingBracket)))) {
            return null;
        }
        if (afterLastParam > -1 && afterArrow == -1) {
            // couldn't find the arrow even though there were parameters, somnething
            // strange has happened
            return null;
        }
        TextEdit edit;
        if (afterLastParam > -1) {
            edit = new MultiTextEdit();
            edit.addChild(new ReplaceEdit(afterName, openingBracket-afterName+1, "("));
            edit.addChild(new ReplaceEdit(afterLastParam, afterArrow-afterLastParam+1, ") {"));
        } else {
            edit = new ReplaceEdit(afterName, openingBracket-afterName+1, "() {");
        }
        return edit;
    }
}
