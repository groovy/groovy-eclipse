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

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IGroovySuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.OperationManager;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionDescriptor;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionsRequestor;
import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

/**
 * Adds a DSL type inferencing suggestion to the current project.
 */
public class AddInferencingSuggestionProposal extends GroovyQuickAssistProposal {

    @Override
    public String getDisplayString() {
        return "Add inferencing suggestion";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_SEARCH_READACCESS);
    }

    @Override
    public int getRelevance() {
        if (!GroovyDSLCoreActivator.getDefault().isDSLDDisabled() && SuggestionsRequestor.isValidNode(context.getCoveredNode())) {
            return 1;
        }
        return 0;
    }

    @Override
    public void apply(IDocument document) {
        SuggestionsRequestor requestor = new SuggestionsRequestor(context.getCoveredNode());
        SuggestionDescriptor descriptor = context.visitCompilationUnit(requestor).getSuggestionDescriptor();
        if (descriptor != null) {
            IGroovySuggestion suggestion = new OperationManager().addGroovySuggestion(context.getProject(), descriptor, context.getShell());
            if (suggestion != null) {
                InferencingSuggestionsManager.getInstance().commitChanges(context.getProject());
            }
        }
    }
}
