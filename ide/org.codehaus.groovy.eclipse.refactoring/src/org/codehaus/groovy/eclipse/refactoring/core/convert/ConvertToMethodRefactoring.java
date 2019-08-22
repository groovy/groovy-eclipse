/*
 * Copyright 2009-2019 the original author or authors.
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
 * Common class to process the convert to method refactoring. Used by both the
 * completion proposal and the refactor menu option.
 */
// FIXGWD: This class should be converted into a proper refactoring class which extends Refactoring.
public class ConvertToMethodRefactoring {

    private final FieldNode targetField;

    public ConvertToMethodRefactoring(GroovyCompilationUnit unit, int offset) {
        this.targetField = getTargetField(unit, offset);
    }

    private FieldNode getTargetField(GroovyCompilationUnit unit, int offset) {
        if (unit.exists() && unit.isOnBuildPath())
        try {
            IJavaElement maybeField = unit.getElementAt(offset);
            if (maybeField instanceof IField && maybeField.exists() && ((IField) maybeField).getNameRange().getOffset() > 0) {
                String fieldName = maybeField.getElementName();
                // check to see if the field is assigned to a closure
                for (ClassNode clazz : unit.getModuleNode().getClasses()) {
                    if (clazz.getNameWithoutPackage().equals(maybeField.getParent().getElementName())) {
                        FieldNode targetField = clazz.getDeclaredField(fieldName);
                        if (targetField != null && targetField.getInitialExpression() instanceof ClosureExpression) {
                            return targetField;
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Oops", e);
        }
        return null;
    }

    public boolean isApplicable() {
        return targetField != null;
    }

    public void applyRefactoring(IDocument document) {
        if (isApplicable()) {
            try {
                TextEdit thisEdit = createEdit(document);
                if (thisEdit != null) {
                    thisEdit.apply(document);
                }
            } catch (Exception e) {
                GroovyCore.logException("Oops.", e);
            }
        }
    }

    public TextEdit createEdit(IDocument doc) throws BadLocationException {
        // find the opening and closing parentheses
        int afterName = targetField.getNameEnd() + 1;
        int openingBracket = findOpenBracket(doc, targetField, afterName);
        int afterLastParam = findAfterLastParam(targetField); // -1 means that there are no params
        int afterArrow = findAfterArrow(doc, Math.max(openingBracket, afterLastParam)); // -1 means that there are no params

        if (!(openingBracket < doc.getLength() && (doc.getChar(openingBracket) == '{') || Character.isWhitespace(doc.getChar(openingBracket)))) {
            return null;
        }
        if (afterLastParam > -1 && afterArrow == -1) {
            // couldn't find the arrow even though there were parameters, something strange has happened
            return null;
        }
        TextEdit edit;
        if (afterLastParam > -1) {
            edit = new MultiTextEdit();
            edit.addChild(new ReplaceEdit(afterName, openingBracket - afterName + 1, "("));
            edit.addChild(new ReplaceEdit(afterLastParam, afterArrow - afterLastParam + 1, ") {"));
        } else {
            edit = new ReplaceEdit(afterName, Math.max(openingBracket, afterArrow) - afterName + 1, "() {");
        }
        return edit;
    }

    private static int findOpenBracket(IDocument doc, FieldNode targetField, int afterName) throws BadLocationException {
        int offset = afterName;
        while (offset < doc.getLength() && doc.getChar(offset) != '{') {
            offset += 1;
        }
        Parameter[] parameters = getClosureParameters(targetField);
        if (parameters != null && parameters.length > 0) {
            int firstParameterStart = parameters[0].getStart();
            offset += 1;
            // also eat up whitespace after the '{'
            while (offset < firstParameterStart && Character.isWhitespace(doc.getChar(offset))) {
                offset += 1;
            }
            offset -= 1;
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
     * @return the value after the end of the arrow or -1 if no arguments
     */
    private static int findAfterArrow(IDocument doc, int afterLastParam) throws BadLocationException {
        if (afterLastParam >= 0) {
            int offset = afterLastParam;
            while (offset < doc.getLength() - 1) {
                if (doc.getChar(offset) == '-' && doc.getChar(offset + 1) == '>') {
                    return offset + 1;
                }
                offset += 1;
            }
        }
        return -1;
    }
}
