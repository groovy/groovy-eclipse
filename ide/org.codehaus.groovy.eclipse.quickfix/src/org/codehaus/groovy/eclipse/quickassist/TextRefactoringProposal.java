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
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextChange;

/**
 * Quick Assist for a refactoring. Delegates to a {@link Refactoring} that generates a {@link TextChange}.
 */
public abstract class TextRefactoringProposal extends AbstractGroovyTextCompletionProposal {

    protected final Refactoring delegate;

    public TextRefactoringProposal(IInvocationContext context, Refactoring delegate) {
        super(context);
        this.delegate = delegate;
    }

    public String getDisplayString() {
        return delegate.getName();
    }

    public boolean hasProposals() {
        try {
            return delegate.checkAllConditions(new NullProgressMonitor()).isOK();
        } catch (CoreException e) {
            GroovyQuickFixPlugin.log(e);
        }
        return false;
    }

    protected TextChange getTextChange(IProgressMonitor monitor) throws CoreException {
        return (TextChange) delegate.createChange(monitor);
    }
}
