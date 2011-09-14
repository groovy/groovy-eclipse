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

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IValueCheckingRule;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.JavaValidParameterizedTypeRule;
import org.codehaus.groovy.eclipse.ui.browse.IBrowseTypeHandler;
import org.codehaus.groovy.eclipse.ui.browse.TypeBrowseSupport;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class JavaTypeBrowsingControl extends JavaTextControl {

    /**
     * 
     */
    private static final String BROWSE = "Browse...";

    private Button browse;

    private IJavaProject project;

    public JavaTypeBrowsingControl(IDialogueControlDescriptor labelDescriptor, Point offsetLabelLocation, String initialValue,
            IJavaProject project) {
        super(labelDescriptor, offsetLabelLocation, initialValue);
        this.project = project;
    }

    /**
     * Must return the control that is managed by the manager.
     */
    protected Control getManagedControl(Composite parent) {
        // First create a composite with 2 columns, one for the labeled text
        // control
        // and the other for the browse button
        Composite fieldComposite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(fieldComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(fieldComposite);

        // Create the text control first in the first column
        Text text = (Text) super.getManagedControl(fieldComposite);

        // create the browse button in the second column
        browse = new Button(fieldComposite, SWT.PUSH);

        browse.setEnabled(true);
        browse.setText(BROWSE);

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(browse);

        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.heightHint = getButtonHeight();

        browse.setLayoutData(data);

        addTypeBrowseSupport(text, browse, parent.getShell());
        return text;
    }

    protected void addTypeBrowseSupport(Text text, Button browse, Shell shell) {
        final Text finText = text;

        new TypeBrowseSupport(shell, project, new IBrowseTypeHandler() {

            public void handleTypeSelection(String qualifiedName) {
                finText.setText(qualifiedName);
                notifyControlChange(qualifiedName, finText);
            }

        }).applySupport(browse, text);
    }

    protected int getButtonHeight() {
        return 23;
    }

    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        browse.setEnabled(enable);
    }

    protected IValueCheckingRule getCachedValidationRule() {
        return new JavaValidParameterizedTypeRule(project);
    }

}
