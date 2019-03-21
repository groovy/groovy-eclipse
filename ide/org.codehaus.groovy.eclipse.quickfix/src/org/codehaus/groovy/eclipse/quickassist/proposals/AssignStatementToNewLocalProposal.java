/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist.proposals;

import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal2;
import org.codehaus.groovy.eclipse.refactoring.core.convert.AssignStatementToNewLocalRefactoring;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Assigns a statement to a new local variable.
 * <p>
 * Ex: "new Point(2,3)" becomes "def temp = new Point(2,3)"
 */
public class AssignStatementToNewLocalProposal extends GroovyQuickAssistProposal2 {

    @Override
    public String getDisplayString() {
        return "Assign statement to new local variable";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
    }

    private AssignStatementToNewLocalRefactoring delegate;

    @Override
    public int getRelevance() {
        if (delegate == null) {
            delegate = new AssignStatementToNewLocalRefactoring(context.getCompilationUnit(), context.getSelectionOffset());
        }
        return delegate.isApplicable() ? 7 : 0;
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) throws CoreException, BadLocationException {
        return toTextChange(delegate.createEdit(context.newTempDocument()));
    }

    @Override
    public void apply(IDocument document) {
        delegate.applyRefactoring(document);
    }

    @Override
    public Point getSelection(IDocument document) {
        return delegate.getNewSelection();
    }
}
