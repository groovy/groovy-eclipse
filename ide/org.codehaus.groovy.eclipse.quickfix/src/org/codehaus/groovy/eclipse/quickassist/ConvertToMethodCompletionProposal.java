/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.eclipse.quickassist;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Converts a field declaration assigned to a closure to a method
 * 
 * @author Andrew Eisenberg
 * @created Oct 24, 2011
 */
public class ConvertToMethodCompletionProposal extends
        AbstractGroovyCompletionProposal {
    
    private final GroovyCompilationUnit unit;
    private final int length;
    private final int offset;
    
    private FieldNode targetField;
    public ConvertToMethodCompletionProposal(IInvocationContext context) {
        super(context);
        ICompilationUnit compUnit = context.getCompilationUnit();
        if (compUnit instanceof GroovyCompilationUnit) {
            this.unit = (GroovyCompilationUnit) compUnit;
        } else {
            this.unit = null;
        }
        length = context.getSelectionLength();
        offset = context.getSelectionOffset();
    }

    public int getRelevance() {
        return 0;
    }

    public void apply(IDocument document) {
        TextEdit thisEdit = findReplacement(document);
        try {
            if (thisEdit != null) {
                thisEdit.apply(document);
            }
        } catch (Exception e) {
            GroovyCore.logException("Oops.", e);
        }
    }

    public Point getSelection(IDocument document) {
        // this is not right.  We should be updating the position based on the text changes
        return new Point(offset, length+offset);
    }

    public String getAdditionalProposalInfo() {
        return getDisplayString();
    }

    public String getDisplayString() {
        return "Convert closure declaration to method";
    }

    public IContextInformation getContextInformation() {
        return new ContextInformation(getImage(), getDisplayString(), getDisplayString());
    }

    @Override
    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_CHANGE;
    }

    @Override
    public boolean hasProposals() {
        try {
            IJavaElement maybeField = unit.getElementAt(offset);
            boolean result = maybeField instanceof IField && ((IField) maybeField).getNameRange().getOffset() > 0;
            if (result) {
                IField field = (IField) maybeField;
                
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
            
            return result;
        } catch (JavaModelException e) {
            GroovyCore.logException("Oops", e);
        }
        return false;
    }
    
    
    private TextEdit findReplacement(IDocument doc) {
        try {
            // find the opening parnn and the closing paren
            int afterName = targetField.getNameEnd() + 1;
            int openingBracket = findOpenBracket(doc, afterName);
            int afterLastParam = findAfterLastParam();  // -1 means that there are no params
            int afterArrow = findAfterArrow(doc, afterLastParam);  // -1 means that there are no params
            return createEdit(doc, afterName, openingBracket, afterLastParam, afterArrow);
        } catch (Exception e) {
            GroovyCore.logException("Exception during convert to closure.", e);
            return null;
        }
    }

    private int findOpenBracket(IDocument doc, int afterName) throws BadLocationException {
        int offset = afterName;
        while (offset < doc.getLength() && doc.getChar(offset) != '{') {
            offset++;
        }
        
        Parameter[] parameters = getClosureParameters();
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
    private int findAfterLastParam() {
        Parameter[] parameters = getClosureParameters();
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

    private Parameter[] getClosureParameters() {
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
    private int findAfterArrow(IDocument doc, int afterLastParam) throws BadLocationException {
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

    
    private TextEdit createEdit(IDocument doc, int afterName,
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
