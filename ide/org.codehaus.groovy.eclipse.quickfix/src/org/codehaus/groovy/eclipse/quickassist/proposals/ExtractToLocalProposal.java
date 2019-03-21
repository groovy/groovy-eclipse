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
import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyLocalRefactoring;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;

/**
 * Extracts an expression to a local variable. Delegates to {@link ExtractGroovyLocalRefactoring}.
 */
public class ExtractToLocalProposal extends GroovyQuickAssistProposal2 {

    public ExtractToLocalProposal(boolean replaceAll) {
        this.replaceAll = replaceAll;
    }

    private final boolean replaceAll;
    private Refactoring delegate;

    protected Refactoring getDelegate() {
        if (delegate == null) {
            ExtractGroovyLocalRefactoring r = new ExtractGroovyLocalRefactoring(
                context.getCompilationUnit(), context.getSelectionOffset(), context.getSelectionLength());
            r.setLocalName(r.guessLocalNames()[0]);
            r.setReplaceAllOccurrences(replaceAll);
            delegate = r;
        }
        return delegate;
    }

    @Override
    public String getDisplayString() {
        return getDelegate().getName();
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
    }

    @Override
    public int getRelevance() {
        try {
            if (getDelegate().checkAllConditions(new NullProgressMonitor()).isOK()) {
                return (replaceAll ? 10 : 9);
            }
        } catch (CoreException e) {
            GroovyQuickFixPlugin.log(e);
        }
        return 0;
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) throws CoreException {
        return (TextChange) getDelegate().createChange(monitor);
    }
}
