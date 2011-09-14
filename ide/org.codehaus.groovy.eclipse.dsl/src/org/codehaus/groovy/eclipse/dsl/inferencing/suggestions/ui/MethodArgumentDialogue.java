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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.DuplicateParameterRule;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.MethodParameter;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ValueStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-08-04
 */
public class MethodArgumentDialogue extends AbstractDialogue {

    public static final DialogueDescriptor DIALOGUE_DESCRIPTOR = new DialogueDescriptor("Add parameter", "Suggestion Parameter",
            "icons/GROOVY.png");

    private Point labelOffset;

    private String type;

    private String name;

    private IJavaProject javaProject;

    private List<MethodParameter> existingParameters;

    public MethodArgumentDialogue(Shell parentShell, IJavaProject javaProject, MethodParameter parameterToEdit,
            List<MethodParameter> existingParameters) {
        super(parentShell, DIALOGUE_DESCRIPTOR);
        this.javaProject = javaProject;
        if (parameterToEdit != null) {
            this.name = parameterToEdit.getName();
            this.type = parameterToEdit.getType();
        }
        this.existingParameters = existingParameters;
    }

    public MethodParameter getMethodParameter() {
        return new MethodParameter(name, type);
    }

    protected Point getOffsetLabelLocation() {
        if (labelOffset == null) {
            IDialogueControlDescriptor[] descriptors = new IDialogueControlDescriptor[] { ControlTypes.TYPE, ControlTypes.NAME

            };
            String[] labelNames = new String[descriptors.length];
            for (int i = 0; i < descriptors.length; ++i) {
                labelNames[i] = descriptors[i].getLabel();
            }
            labelOffset = getOffsetLabelLocation(labelNames);
        }
        return labelOffset;
    }

    protected void createCommandArea(Composite parent) {
        JavaTextControl nameControl = new JavaTextControl(ControlTypes.NAME, getOffsetLabelLocation(), name) {

            protected ValueStatus isControlValueValid(Control control) {
                ValueStatus status = super.isControlValueValid(control);
                // If status is OK, do a further check to see if there are any
                // duplicate parameters
                if (!status.isError()) {
                    status = new DuplicateParameterRule(existingParameters).checkValidity(status.getValue());
                }
                return status;
            }

        };
        nameControl.createControlArea(parent);
        nameControl.addSelectionListener(new RequiredValueControlSelectionListener(ControlTypes.NAME, name) {

            protected void handleRequiredValue(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    name = (String) selection;
                }
            }
        });

        JavaTypeBrowsingControl declaringTypeControl = new JavaTypeBrowsingControl(ControlTypes.TYPE, getOffsetLabelLocation(),
                type, javaProject);
        declaringTypeControl.createControlArea(parent);

        declaringTypeControl.addSelectionListener(new RequiredValueControlSelectionListener(ControlTypes.TYPE, type) {

            protected void handleRequiredValue(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    type = (String) selection;
                }
            }
        });

    }
}
