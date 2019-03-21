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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IGroovySuggestion;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;

public class EditInferencingSuggestionDialogue extends AddInferencingSuggestionDialogue {

    public static final DialogueDescriptor EDIT_DIALOGUE_DESCRIPTOR = new DialogueDescriptor(
        "Edit a Groovy inferencing suggestion", "Inferencing Suggestion", "platform:/plugin/org.codehaus.groovy.eclipse/$nl$/groovy.png");

    /**
     * This constructor is used to edit an existing suggestion. Editing a declaring type is not yet supported.
     */
    public EditInferencingSuggestionDialogue(Shell parentShell, IGroovySuggestion suggestion, IProject project) {
        super(parentShell, project);
        setSuggestion(suggestion);
    }

    protected DialogueDescriptor getDialogueDescriptor() {
        return EDIT_DIALOGUE_DESCRIPTOR;
    }
}
