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

import org.eclipse.core.resources.IProject;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-15
 */
public abstract class AbstractCreateOperation extends AbstractSuggestionOperation {

    protected static final String MISSING_DESCRIPTOR = "Failed to run operation. Descriptor required to create suggestion.";

    private SuggestionDescriptor descriptor;

    public AbstractCreateOperation(IProject project, IBaseGroovySuggestion suggestionContext) {
        super(project, suggestionContext);
    }

    // Not the most elegant way using setters add a descriptor for the
    // operation. However
    // descriptors may not be available when the operation is first created.
    public void setSuggestionDescriptor(SuggestionDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public SuggestionDescriptor getDescriptor() {
        return descriptor;
    }

    public ValueStatus run() {

        if (descriptor != null) {
            return run(descriptor);
        }

        return ValueStatus.getErrorStatus(null, MISSING_DESCRIPTOR);

    }

    /**
     * Descriptor is required to edit the
     * current suggestion
     * context. The descriptor contains the edited information. The suggestion
     * context is the
     * original, unedited suggestion.
     * */
    abstract protected ValueStatus run(SuggestionDescriptor descriptor);

}
