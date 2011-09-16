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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;

/**
 * Must be run in the UI thread. Performs add, edit and remove of suggestions in
 * a given project.
 * Adding and edit may optionally also include a context. For adding, the
 * context may be an existing
 * suggestion or declaring type. In either case, the declaring type of the
 * existing suggestion will
 * be used for the new suggestion (i.e. the suggestion may be added to that
 * declaring type). For editing,
 * the context values are edited and a new suggestion instance containing the
 * edited values is returned.
 * Note that even in the edit case, the original context suggestion is NOT
 * modified. A new instance of
 * a suggestion is returned even during an edit.
 * 
 * @author Nieraj Singh
 * @created 2011-09-15
 */
public class OperationManager {

    public IGroovySuggestion addGroovySuggestion(IProject project, IBaseGroovySuggestion context, Shell shell) {
        return performGroovyOperation(new AddSuggestionsOperation(project, context), shell);
    }

    public IGroovySuggestion addGroovySuggestion(IProject project, SuggestionDescriptor descriptor, Shell shell) {
        AddSuggestionsOperation operation = new AddSuggestionsOperation(project, null);
        operation.setSuggestionDescriptor(descriptor);
        return performGroovyOperation(operation, shell);
    }

    public IGroovySuggestion editGroovySuggestion(IProject project, IBaseGroovySuggestion context, Shell shell) {
        return performGroovyOperation(new EditSuggestionOperation(project, context), shell);
    }

    protected IGroovySuggestion performGroovyOperation(AbstractCreateOperation operation, Shell shell) {

        SuggestionsUIOperation uiOperation = new SuggestionsUIOperation(operation, shell);
        ValueStatus status = uiOperation.run();
        if (!status.isError()) {
            Object valObj = status.getValue();
            if (valObj instanceof IGroovySuggestion) {
                return (IGroovySuggestion) valObj;
            }
        }
        return null;
    }

    public void removeGroovySuggestion(IProject project, List<IBaseGroovySuggestion> selection) {
        new RemoveSuggestionOperation(project, selection).run();
    }
}
