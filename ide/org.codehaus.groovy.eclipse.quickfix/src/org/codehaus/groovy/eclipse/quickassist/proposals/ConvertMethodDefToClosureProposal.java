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
package org.codehaus.groovy.eclipse.quickassist.proposals;

import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal2;
import org.codehaus.groovy.eclipse.refactoring.core.convert.ConvertToClosureRefactoring;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;

/**
 * Converts a method declaration to a closure assigned to a field.
 */
public class ConvertMethodDefToClosureProposal extends GroovyQuickAssistProposal2 {

    @Override
    public String getDisplayString() {
        return "Convert method declaration to closure";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    }

    private ConvertToClosureRefactoring delegate;

    @Override
    public int getRelevance() {
        if (delegate == null) {
            delegate = new ConvertToClosureRefactoring(context.getCompilationUnit(), context.getSelectionOffset());
        }
        return (delegate.isApplicable() ? 5 : 0);
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) throws BadLocationException, JavaModelException {
        return toTextChange(delegate.createEdit(context.newTempDocument()));
    }

    @Override
    public void apply(IDocument document) {
        delegate.applyRefactoring(document);
    }
}
