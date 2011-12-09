/*
 * Copyright 2011 SpringSource, a division of VMware, Inc andrew - Initial API and implementation
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.codehaus.groovy.eclipse.quickassist;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.SemicolonRemover;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.TextEdit;

/**
 * Remove the unnecessary semicolons from the file or selection only
 */
public class RemoveUnnecessarySemicolonsCompletionProposal extends
        AbstractGroovyCompletionProposal {

    private final int length;

    private final int offset;

    public RemoveUnnecessarySemicolonsCompletionProposal(
            IInvocationContext context) {
        super(context);

        length = context.getSelectionLength();
        offset = context.getSelectionOffset();
    }

    public int getRelevance() {
        return 0;
    }

    public void apply(IDocument document) {

        ITextSelection selection = new TextSelection(offset, length);
        GroovyFormatter formatter = new SemicolonRemover(selection, document);

        TextEdit textEdit = formatter.format();
        try {
            if (textEdit.getChildrenSize() > 0) { // Higher than 0 means we have changes
                textEdit.apply(document);
            }
        } catch (Exception e) {
            GroovyCore.logException("Oops.", e);
        }
    }

    public Point getSelection(IDocument document) {
        return new Point(offset, 0);
    }

    public String getAdditionalProposalInfo() {
        return getDisplayString();
    }

    public String getDisplayString() {
        return "Remove unnecessary semicolons";
    }

    public IContextInformation getContextInformation() {
        return new ContextInformation(getImage(), getDisplayString(),
                getDisplayString());
    }

    @Override
    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_REMOVE;
    }

    @Override
    public boolean hasProposals() {
        return getContext().getCompilationUnit() instanceof GroovyCompilationUnit;
    }

}
