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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class DocumentDialogueControl extends AbstractLabeledDialogueControl {

    public DocumentDialogueControl(IDialogueControlDescriptor labelDescriptor, Point offsetLabelLocation) {
        super(labelDescriptor, offsetLabelLocation);
    }

    void setControlValue(Control control, Object value) {
        if (control instanceof Browser && value instanceof String) {
            ((Browser) control).setText((String) value);
        }
    }

    protected Control getLabeledControl(Composite parent) {
        final Browser browser = new Browser(parent, SWT.BORDER);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        browser.setVisible(true);

        browser.addKeyListener(new KeyListener() {

            public void keyReleased(KeyEvent e) {
                notifyLabeledControlChange(browser.getText());
            }

            public void keyPressed(KeyEvent e) {
                // nothing.
            }
        });
        return browser;
    }
}
