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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public abstract class AbstractLabeledDialogueControl extends AbstractControlManager {

    private Point offsetLabelLocation;

    private Label parameterNameLabel;

    private IDialogueControlDescriptor labelDescriptor;

    protected AbstractLabeledDialogueControl(IDialogueControlDescriptor labelDescriptor, Point offsetLabelLocation) {
        this.labelDescriptor = labelDescriptor;
        this.offsetLabelLocation = offsetLabelLocation;
    }

    protected IDialogueControlDescriptor getLabelDescriptor() {
        return labelDescriptor;
    }

    protected int numberofColumns() {
        return 2;
    }

    protected Map<Control, IDialogueControlDescriptor> createManagedControls(Composite parent) {

        Map<Control, IDialogueControlDescriptor> controls = new HashMap<Control, IDialogueControlDescriptor>();
        if (labelDescriptor != null) {
            Composite labelArea = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().numColumns(numberofColumns()).margins(0, 0).equalWidth(false).applyTo(labelArea);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(labelArea);

            parameterNameLabel = new Label(labelArea, SWT.READ_ONLY);

            parameterNameLabel.setText(labelDescriptor.getLabel() + ": ");
            parameterNameLabel.setToolTipText(labelDescriptor.getToolTipText());

            GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).applyTo(parameterNameLabel);

            if (offsetLabelLocation != null) {
                GridData data = (GridData) parameterNameLabel.getLayoutData();
                int heightHint = offsetLabelLocation.y;
                if (heightHint > 0) {
                    data.heightHint = heightHint;
                }
                int widthHint = offsetLabelLocation.x;
                if (widthHint > 0) {
                    data.widthHint = widthHint;
                }
            }
            Control labeledControl = getManagedControl(labelArea);
            if (labeledControl != null) {
                // Although the label descriptor is used to create the label
                // control, it actually
                // also defines the control rendered after the label itself.
                controls.put(labeledControl, getLabelDescriptor());

            }
        }
        return controls;
    }

    /**
     * Create the control that is rendered on the right side of the label
     * control in the same row. The parent composite layout already
     * takes into account the presence of the label in the first column of the
     * layout. Implementors of this method will automatically get
     * their control placed in the second column of the parent composite in the
     * same row.
     * 
     * @param parent
     * @return
     */
    abstract protected Control getManagedControl(Composite parent);

    public Label getLabel() {
        return parameterNameLabel != null && !parameterNameLabel.isDisposed() ? parameterNameLabel : null;
    }

}
