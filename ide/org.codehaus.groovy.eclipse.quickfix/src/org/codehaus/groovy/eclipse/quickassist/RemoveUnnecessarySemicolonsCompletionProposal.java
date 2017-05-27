/*
 * Copyright 2009-2017 the original author or authors.
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

import org.codehaus.groovy.eclipse.refactoring.formatter.SemicolonRemover;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.TextEdit;

/**
 * Removes the unnecessary semicolons from the selection or compilation unit.
 */
public class RemoveUnnecessarySemicolonsCompletionProposal extends AbstractGroovyTextCompletionProposal {

    private final boolean containsSemicolon;

    public RemoveUnnecessarySemicolonsCompletionProposal(IInvocationContext context) {
        super(context);

        char[] contents = getGroovyCompilationUnit().getContents();
        if (context.getSelectionLength() > 0) {
            int start = context.getSelectionOffset(),
                until = start + context.getSelectionLength();
            contents = CharOperation.subarray(contents, start, until);
        }

        containsSemicolon = CharOperation.contains(';', contents);
    }

    public String getDisplayString() {
        return "Remove unnecessary semicolons";
    }

    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_REMOVE;
    }

    public boolean hasProposals() {
        return containsSemicolon;
    }

    public int getRelevance() {
        return 1;
    }

    @Override
    protected TextEdit getTextEdit(IDocument document) {
        ITextSelection selection = new TextSelection(context.getSelectionOffset(), context.getSelectionLength());
        SemicolonRemover remover = new SemicolonRemover(selection, document);
        //if (textEdit.getChildrenSize() > 0)
        return remover.format();
    }
}
