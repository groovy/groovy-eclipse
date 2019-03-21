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

public class ControlSelectionEvent {

    private Object data;

    private IDialogueControlDescriptor descriptor;

    private String errorMessage;

    /**
     * Use this constructor if an error event is generated due to invalid value.
     * The value is set to null
     */
    public ControlSelectionEvent(IDialogueControlDescriptor descriptor, String errorMessage) {
        this.data = null;
        this.descriptor = descriptor;
        this.errorMessage = errorMessage;
    }

    /**
     * Use this constructor if a value is successfully validated in the control.
     */
    public ControlSelectionEvent(Object data, IDialogueControlDescriptor descriptor) {
        this.data = data;
        this.descriptor = descriptor;
        this.errorMessage = null;
    }

    public Object getSelectionData() {
        return data;
    }

    public IDialogueControlDescriptor getControlDescriptor() {
        return descriptor;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
