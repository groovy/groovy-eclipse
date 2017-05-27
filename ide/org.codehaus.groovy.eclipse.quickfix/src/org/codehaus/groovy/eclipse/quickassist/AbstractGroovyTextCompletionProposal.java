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

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.ui.text.correction.proposals.EditAnnotator;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public abstract class AbstractGroovyTextCompletionProposal extends AbstractGroovyCompletionProposal implements ICompletionProposalExtension5 {

    public AbstractGroovyTextCompletionProposal(IInvocationContext context) {
        super(context);
    }

    public void apply(IDocument document) {
        try {
            TextChange change = getTextChange(new NullProgressMonitor());
            change.getEdit().apply(document);
        } catch (CoreException e) {
            GroovyQuickFixPlugin.log(e);
        } catch (BadLocationException e) {
            GroovyQuickFixPlugin.log(e);
        } catch (MalformedTreeException e) {
            GroovyQuickFixPlugin.log(e);
        }
    }

    /**
     * Previews the completion within the context of the compilation unit.
     */
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        StringBuffer buf = new StringBuffer();
        try {
            TextChange change = getTextChange(monitor);
            change.setKeepPreviewEdits(true);
            IDocument previewDocument = change.getPreviewDocument(monitor);
            TextEdit rootEdit = change.getPreviewEdit(change.getEdit());
            EditAnnotator ea = new EditAnnotator(buf, previewDocument);
            rootEdit.accept(ea);
            ea.unchangedUntil(previewDocument.getLength());
        } catch (CoreException e) {
            GroovyQuickFixPlugin.log(e);
        } catch (BadLocationException e) {
            GroovyQuickFixPlugin.log(e);
        }
        return buf.toString();
    }

    /**
     * Override to provide a {@link TextChange} directly. Default implementation
     * delegates to {@link #getTextEdit}. Leveraged by {@link #apply(IDocument)}
     * and {@link #getAdditionalProposalInfo(IProgressMonitor)}.
     */
    protected TextChange getTextChange(IProgressMonitor monitor) throws CoreException, BadLocationException {
        TextChange change = new CompilationUnitChange(getDisplayString(), getGroovyCompilationUnit());
        change.setEdit(getTextEdit(change.getCurrentDocument(monitor)));
        return change;
    }

    /**
     * Override to provide a {@link TextEdit} that implements the proposal.
     */
    protected TextEdit getTextEdit(IDocument document) throws CoreException, BadLocationException {
        return null;
    }
}
