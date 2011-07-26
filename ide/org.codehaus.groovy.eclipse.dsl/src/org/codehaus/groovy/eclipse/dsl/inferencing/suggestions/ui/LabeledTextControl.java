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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class LabeledTextControl extends AbstractLabeledDialogueControl {

    private boolean firstFocus = true;

    private String initialValue;

    public LabeledTextControl(IDialogueControlDescriptor labelDescriptor, Point offsetLabelLocation, String initialValue) {
        super(labelDescriptor, offsetLabelLocation);
        this.initialValue = initialValue;
    }


    @Override
    void setControlValue(Control control, Object value) {
        if (control instanceof Text && value instanceof String) {
            ((Text) control).setText((String) value);
        }
    }

    @Override
    protected Control getLabeledControl(Composite parent) {
        final Text textControl = new Text(parent, SWT.BORDER);
        if (initialValue != null) {
            textControl.setText(initialValue);
        }

        textControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Set the default value BEFORE adding the modify listener
        // Object defaultValue = getParameter().getValue();
        // if (defaultValue instanceof String) {
        // commandValueText.setText((String) defaultValue);
        // }

        textControl.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                notifyLabeledControlChange(textControl.getText());
            }
        });

        textControl.addFocusListener(new FocusListener() {

            public void focusLost(FocusEvent e) {
                // Nothing
            }

            public void focusGained(FocusEvent e) {
                // Notify the wizard that focus has been set the first
                // time to allow the wizard to output any error messages
                if (firstFocus) {
                    notifyLabeledControlChange(textControl.getText());
                    firstFocus = false;
                }
            }
        });
        return textControl;
    }

}
