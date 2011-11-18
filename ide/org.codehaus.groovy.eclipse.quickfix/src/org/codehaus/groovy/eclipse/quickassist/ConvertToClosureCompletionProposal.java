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

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Converts a method declaration to a field assigned to a closure
 * 
 * @author Andrew Eisenberg
 * @created Oct 24, 2011
 */
public class ConvertToClosureCompletionProposal extends
        AbstractGroovyCompletionProposal {
    
    private final GroovyCompilationUnit unit;
    private final int length;
    private final int offset;
    
    private IMethod targetMethod;

    public ConvertToClosureCompletionProposal(IInvocationContext context) {
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
        return "Convert method declaration to closure";
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
            IJavaElement maybeMethod = unit.getElementAt(offset);
            // must be a method.  Also check that the name range length is equal to the length of the element name.
            // if not, this indicates a synthetic method (eg- the run() method).
            boolean result = isApplicableMethod(maybeMethod);
            if (result) {
                targetMethod = (IMethod) maybeMethod;
            }
            return result;
        } catch (JavaModelException e) {
            GroovyCore.logException("Problem checking for quick assist", e);
        }
        return false;
    }

    public boolean isApplicableMethod(IJavaElement maybeMethod)
            throws JavaModelException {
        if (! (maybeMethod instanceof IMethod )) {
            return false;
        }
        
        ISourceRange nameRange = ((IMethod) maybeMethod).getNameRange();
        if (nameRange.getLength() == maybeMethod.getElementName().length()) {
            return true;
        }
        
        // For quoted method names, the name range will include the quotes, but the name itself will not include them
        // check the text to see if the name start is at a quote
        char[] contents = unit.getContents(); 
        if (contents.length >= nameRange.getOffset() && contents[nameRange.getOffset()] == '"') {
            return true;
        }
        return false;
     }
    
    
    private TextEdit findReplacement(IDocument doc) {
        try {
            ISourceRange nameRange = targetMethod.getSourceRange();
            // find the opening parnn and the closing paren
            int openingParen = findOpenParen(doc, nameRange);
            int closingParen = findCloseParen(doc, openingParen);
            int openingBracket = findOpenBracket(doc, closingParen);
            return createEdit(doc, openingParen, closingParen, openingBracket);
        } catch (Exception e) {
            GroovyCore.logException("Exception during convert to closure.", e);
            return null;
        }
    }
    
    /**
     * @return finds the first open paren after the name ends
     * @throws BadLocationException 
     */
    private int findOpenParen(IDocument doc, ISourceRange nameRange) throws BadLocationException {
        int offset = nameRange.getOffset() + targetMethod.getElementName().length();
        while (offset < doc.getLength() && doc.getChar(offset) != '(') {
            offset++;
        }
        return offset;
    }

    
    private int findOpenBracket(IDocument doc, int closingParen) throws BadLocationException {
        int offset = closingParen;
        while (offset < doc.getLength() && doc.getChar(offset) != '{') {
            offset++;
        }
        return offset;
    }

    private int findCloseParen(IDocument doc, int open) throws BadLocationException {
        int offset = open;
        while (offset < doc.getLength() && doc.getChar(offset) != ')') {
            offset++;
        }
        return offset;
    }


    private TextEdit createEdit(IDocument doc, int openingParen, int closingParen,
            int openingBracket) throws BadLocationException {
        if (! (openingParen < doc.getLength() && doc.getChar(openingParen) == '(')) {
            return null;
        }
        if (! (closingParen < doc.getLength() && doc.getChar(closingParen) == ')')) {
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
