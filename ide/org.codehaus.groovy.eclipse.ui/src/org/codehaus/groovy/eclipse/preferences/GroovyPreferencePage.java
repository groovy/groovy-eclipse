/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GroovyPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    public GroovyPreferencePage() {
    }
    


    public void init(IWorkbench workbench) {}

    protected String getPageId() {
        return this.getClass().getPackage().getName();
    }



    @Override
    protected Control createContents(Composite parent) {
        Label messageLabel = new Label(parent, SWT.WRAP);
        messageLabel.setText("Select a preference page below to configure Groovy preferences");
        return messageLabel;
    }

}