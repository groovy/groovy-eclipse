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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class DocumentDialogueControl extends AbstractControlManager {

    private String initialValue;

    private IDialogueControlDescriptor descriptor;

    public DocumentDialogueControl(IDialogueControlDescriptor descriptor, Point offsetLabelLocation, String initialValue) {
        this.descriptor = descriptor;
        this.initialValue = initialValue;
    }

    protected void setControlValue(Control control, Object value) {
        if (control instanceof Browser && value instanceof String) {
            ((Browser) control).setText((String) value);
        }
    }

    // One column, to have the label and browser on separate rows
    protected int numberofColumns() {
        return 1;
    }

    protected int getDocumentControlHeight() {
        return 100;
    }

    protected Map<Control, IDialogueControlDescriptor> createManagedControls(Composite parent) {

        Map<Control, IDialogueControlDescriptor> controls = new HashMap<Control, IDialogueControlDescriptor>();
        if (descriptor != null) {
            Composite labelArea = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().numColumns(numberofColumns()).margins(0, 0).equalWidth(false).applyTo(labelArea);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(labelArea);

            Label parameterNameLabel = new Label(labelArea, SWT.READ_ONLY);

            parameterNameLabel.setText(descriptor.getLabel() + ": ");
            parameterNameLabel.setToolTipText(descriptor.getToolTipText());

            GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).applyTo(parameterNameLabel);

            final StyledText styledText = new StyledText(labelArea, SWT.BORDER | SWT.MULTI);

            GridDataFactory.fillDefaults().grab(true, true).minSize(SWT.DEFAULT, getDocumentControlHeight()).applyTo(styledText);
            styledText.setVisible(true);

            styledText.addKeyListener(new KeyListener() {

                public void keyReleased(KeyEvent e) {
                    notifyControlChange(styledText.getText(), styledText);
                }

                public void keyPressed(KeyEvent e) {
                    // nothing.
                }
            });

            if (initialValue != null) {
                styledText.setText(initialValue);
            }
            controls.put(styledText, descriptor);
        }
        return controls;
    }

}
