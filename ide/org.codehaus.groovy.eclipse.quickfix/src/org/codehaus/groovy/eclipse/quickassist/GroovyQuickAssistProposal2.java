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

import static org.eclipse.jdt.groovy.core.util.ReflectionUtils.invokeConstructor;

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public abstract class GroovyQuickAssistProposal2 extends GroovyQuickAssistProposal implements ICompletionProposalExtension5 {

    protected abstract TextChange getTextChange(IProgressMonitor monitor) throws CoreException, BadLocationException;

    // helper in case it's easier to produce a TextEdit
    protected final TextChange toTextChange(TextEdit edit) {
        if (edit != null) {
            TextChange change = new CompilationUnitChange(getDisplayString(), context.getCompilationUnit());
            change.setEdit(edit);
            return change;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        StringBuffer buf = new StringBuffer();
        try {
            TextChange change = getTextChange(monitor);

            // for Eclipse Indigo (3.7) backwards compatibility
            Class<? extends ICompletionProposalExtension5> cup;
            try {
                cup = (Class<? extends ICompletionProposalExtension5>)
                    Class.forName("org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal");
            } catch (ClassNotFoundException e) {
                try {
                    cup = (Class<? extends ICompletionProposalExtension5>)
                        Class.forName("org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal");
                } catch (ClassNotFoundException e1) {
                    throw new IllegalStateException(e1);
                }
            }
            return invokeConstructor(cup,
                    new Class[]  {String.class, ICompilationUnit.class,       TextChange.class, int.class, Image.class},
                    new Object[] {"",           context.getCompilationUnit(), change,           0,         null       })
                .getAdditionalProposalInfo(monitor);

            /*
            change.setKeepPreviewEdits(true);
            IDocument previewDocument = change.getPreviewDocument(monitor);
            TextEdit rootEdit = change.getPreviewEdit(change.getEdit());
            org.eclipse.jdt.internal.ui.text.correction.proposals.EditAnnotator ea =
                new org.eclipse.jdt.internal.ui.text.correction.proposals.EditAnnotator(buf, previewDocument);
            rootEdit.accept(ea);
            ea.unchangedUntil(previewDocument.getLength());
            */
        } catch (CoreException e) {
            GroovyQuickFixPlugin.log(e);
        } catch (BadLocationException e) {
            GroovyQuickFixPlugin.log(e);
        }
        return buf.toString();
    }

    @Override
    public void apply(IDocument document) {
        try {
            TextChange change = getTextChange(new NullProgressMonitor());
            if (change != null) {
                change.getEdit().apply(document);
            }
        } catch (CoreException e) {
            GroovyQuickFixPlugin.log(e);
        } catch (BadLocationException e) {
            GroovyQuickFixPlugin.log(e);
        } catch (MalformedTreeException e) {
            GroovyQuickFixPlugin.log(e);
        }
    }
}
