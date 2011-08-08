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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class RadioSelectionDialogueControl extends AbstractControlManager {

    private IDialogueControlDescriptor[] radioValues;

    private IDialogueControlDescriptor defaultValue;

    protected RadioSelectionDialogueControl(IDialogueControlDescriptor[] radioValues, IDialogueControlDescriptor defaultValue) {
        this.radioValues = radioValues;
        this.defaultValue = defaultValue;
    }

    protected Map<Control, IDialogueControlDescriptor> createManagedControls(Composite parent) {
        Composite buttonArea = new Composite(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(buttonArea);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonArea);

        Map<Control, IDialogueControlDescriptor> controls = new HashMap<Control, IDialogueControlDescriptor>();

        for (IDialogueControlDescriptor descriptor : radioValues) {

            String buttonLabel = descriptor.getLabel();

            if (buttonLabel == null || buttonLabel.length() == 0) {
                continue;
            }

            final Button button = new Button(buttonArea, SWT.RADIO);
            button.setText(buttonLabel);

            GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(button);
            button.setSelection(false);

            if (button != null) {
                controls.put(button, descriptor);

                button.setData(descriptor);
                String toolTipText = descriptor.getToolTipText();

                if (toolTipText != null) {
                    button.setToolTipText(toolTipText);
                }
                if (buttonLabel.equals(defaultValue.getLabel())) {
                    button.setSelection(true);
                }
                button.addSelectionListener(new SelectionAdapter() {

                    public void widgetSelected(SelectionEvent e) {
                        if (button.getData() instanceof IDialogueControlDescriptor) {
                            notifyControlChange(button.getData(), button);
                        }
                    }

                });
            }
        }
        return controls;
    }

}
