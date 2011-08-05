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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-08-04
 */
public class ParameterDialogue extends AbstractDialogue {

    public static final DialogueDescriptor DIALOGUE_DESCRIPTOR = new DialogueDescriptor("Add parameter", "Suggestion Parameter",
            "icons/GROOVY.png");

    enum ControlTypes implements IDialogueControlDescriptor {
        NAME("Name", "Enter a type name"),

        TYPE("Type", "Enter or browse for a type");

        private String label;

        private String toolTipText;

        private ControlTypes(String label, String toolTipText) {
            this.label = label;
            this.toolTipText = toolTipText;
        }

        public String getLabel() {

            return label;
        }

        public String getToolTipText() {

            return toolTipText;
        }

    }

    private Point labelOffset;

    private String type;

    private String name;

    private IJavaProject javaProject;

    public ParameterDialogue(Shell parentShell, IJavaProject javaProject, String name, String type) {
        super(parentShell, DIALOGUE_DESCRIPTOR);
        this.javaProject = javaProject;
        this.name = name != null ? name : "";
        this.type = type != null ? type : "";
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
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

    @Override
    protected void createCommandArea(Composite parent) {
        LabeledTextControl nameControl = new LabeledTextControl(ControlTypes.NAME, getOffsetLabelLocation(), name);
        nameControl.createControlArea(parent);
        nameControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    name = (String) selection;
                }
            }
        });

        JavaTextDialogueControl declaringTypeControl = new JavaTextDialogueControl(ControlTypes.TYPE, getOffsetLabelLocation(),
                type, javaProject);
        declaringTypeControl.createControlArea(parent);

        declaringTypeControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    type = (String) selection;
                }
            }
        });

    }

}
