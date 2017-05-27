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

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionCompilationUnitHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;

/**
 * Adds DSL type inferencing suggestion.
 */
public class AddInferencingSuggestionQuickAssistProposal extends AbstractGroovyCompletionProposal {

    private final SuggestionCompilationUnitHelper delegate;

    public AddInferencingSuggestionQuickAssistProposal(IInvocationContext context) {
        super(context);

        IProject project = getProject();
        if (project == null || GroovyDSLCoreActivator.getDefault().isDSLDDisabled()) {
            delegate = null;
        } else {
            delegate = new SuggestionCompilationUnitHelper(context.getSelectionLength(), context.getSelectionOffset(), getGroovyCompilationUnit(), project);
        }
    }

    public String getDisplayString() {
        return "Add inferencing suggestion";
    }

    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_OBJS_IMPDECL;
    }

    public int getRelevance() {
        return 0;
    }

    public boolean hasProposals() {
        return delegate == null ? false : delegate.canAddSuggestion();
    }

    public void apply(IDocument document) {
        if (delegate != null)
            delegate.addSuggestion();
    }
}
