/*
 * Copyright 2009-2019 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ButtonDialogueControl extends AbstractControlManager {

    private Button boolButton;

    private IDialogueControlDescriptor descriptor;

    private boolean initialValue;

    private int buttonType;

    /**
     * @param buttonType SWT button type SWT.CHECK, SWT.PUSH, SWT.RADIO
     */
    public ButtonDialogueControl(IDialogueControlDescriptor descriptor, int buttonType, boolean initialValue) {
        this.descriptor = descriptor;
        this.buttonType = buttonType;
        this.initialValue = initialValue;
    }

    @Override
    protected Map<Control, IDialogueControlDescriptor> createManagedControls(Composite parent) {
        Composite baseCommandArea = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(baseCommandArea);
        GridDataFactory.fillDefaults().applyTo(baseCommandArea);

        boolButton = new Button(baseCommandArea, buttonType);
        boolButton.setText(descriptor.getLabel());
        boolButton.setData(descriptor);
        boolButton.setSelection(initialValue);
        boolButton.setToolTipText(descriptor.getToolTipText());

        GridDataFactory.fillDefaults().align(GridData.FILL, GridData.CENTER).applyTo(boolButton);

        boolButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                notifyControlChange(boolButton.getSelection(), boolButton);
            }
        });
        Map<Control, IDialogueControlDescriptor> controls = new HashMap<>();
        controls.put(boolButton, descriptor);
        return controls;
    }
}
