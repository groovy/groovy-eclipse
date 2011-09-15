/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui.InferencingContributionDialogue;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * Performs a given suggestions operation using the suggestions
 * dialogue.
 * Must be run in the UI thread.
 * 
 * @author Nieraj Singh
 * @created 2011-09-15
 */
public class SuggestionsUIOperation {

    private Shell shell;

    private AbstractCreateOperation operation;

    protected static final String UNABLE_TO_OPEN_DIALOGUE = "Unable to open suggestions dialogue";

    /**
     * The shell and operation must not be null
     */
    public SuggestionsUIOperation(AbstractCreateOperation operation, Shell shell) {
        this.shell = shell;
        this.operation = operation;
    }

    public ValueStatus run() {
        IBaseGroovySuggestion context = operation.getContext();
        InferencingContributionDialogue dialogue = null;
        IProject project = operation.getProject();
        if (context == null) {
            dialogue = new InferencingContributionDialogue(shell, project);
        } else {
            if (context instanceof GroovySuggestionDeclaringType) {
                dialogue = new InferencingContributionDialogue(shell, (GroovySuggestionDeclaringType) context, project);
            } else if (context instanceof IGroovySuggestion) {
                // edits the selected suggestion context. A new suggestion is
                // created with the edited values
                if (operation instanceof EditSuggestionOperation) {
                    dialogue = new InferencingContributionDialogue(shell, (IGroovySuggestion) context, project);
                } else if (operation instanceof AddSuggestionsOperation) {
                    // If adding, only the declaring type is passed, as the new
                    // suggestion will be added to that declaring type
                    dialogue = new InferencingContributionDialogue(shell, ((IGroovySuggestion) context).getDeclaringType(), project);
                }
            }
        }

        if (dialogue != null && dialogue.open() == Window.OK) {
            SuggestionDescriptor descriptor = dialogue.getSuggestionChange();
            operation.setSuggestionDescriptor(descriptor);
            return operation.run();

        }

        return ValueStatus.getErrorStatus(null, UNABLE_TO_OPEN_DIALOGUE);
    }

}
