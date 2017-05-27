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

import org.codehaus.groovy.eclipse.refactoring.core.convert.AssignStatementToNewLocalRefactoring;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.TextEdit;

/**
 * Assigns a statement to a new local variable.
 * <p>
 * Ex: "new Point(2,3)" becomes "def temp = new Point(2,3)"
 */
public class AssignStatementToNewLocalProposal extends AbstractGroovyTextCompletionProposal {

    private final AssignStatementToNewLocalRefactoring delegate;

    public AssignStatementToNewLocalProposal(IInvocationContext context) {
        super(context);

        delegate = new AssignStatementToNewLocalRefactoring(getGroovyCompilationUnit(), context.getSelectionOffset());
    }

    public String getDisplayString() {
        return "Assign statement to new local variable";
    }

    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_LOCAL;
    }

    public boolean hasProposals() {
        return delegate.isApplicable();
    }

    @Override
    public int getRelevance() {
        return new ExtractToConstantProposal(context).getRelevance() - 1;
    }

    @Override
    public Point getSelection(IDocument document) {
        return delegate.getNewSelection();
    }

    @Override
    protected TextEdit getTextEdit(IDocument document) {
        return delegate.createEdit(document);
    }

    @Override
    public void apply(IDocument document) {
        delegate.applyRefactoring(document);
    }
}
