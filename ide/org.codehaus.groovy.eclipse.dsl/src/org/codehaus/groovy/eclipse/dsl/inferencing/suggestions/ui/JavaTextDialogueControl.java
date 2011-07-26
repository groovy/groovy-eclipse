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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class JavaTextDialogueControl extends LabeledTextControl {

    private Button browse;

    public JavaTextDialogueControl(IDialogueControlDescriptor labelDescriptor, Point offsetLabelLocation, String initialValue) {
        super(labelDescriptor, offsetLabelLocation, initialValue);
    }

    @Override
    protected Control getLabeledControl(Composite parent) {
        // First create a composite with 2 columns, one for the labeled text
        // control
        // and the other for the browse button
        Composite fieldComposite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(fieldComposite);
        GridDataFactory.fillDefaults().grab(false, false).applyTo(fieldComposite);

        // Create the text control first in the first column
        Control text = super.getLabeledControl(fieldComposite);

        // create the browse button in the second column
        browse = new Button(fieldComposite, SWT.PUSH);

        browse.setEnabled(true);
        browse.setText("Browse...");
        
        browse.addSelectionListener(new SelectionAdapter(   ) {

   
            public void widgetSelected(SelectionEvent e) {
 
                
            }});
        
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(browse);

        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.heightHint = getButtonHeight();

        browse.setLayoutData(data);
        return text;
    }

    protected int getButtonHeight() {
        return 23;
    }

    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        browse.setEnabled(enable);
    }

}
