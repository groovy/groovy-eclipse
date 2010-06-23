/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.ui.extract;

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyMethodRefactoring;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Michael Klenk mklenk@hsr.ch
 */
public class ExtractMethodPage extends UserInputWizardPage {

    private ExtractGroovyMethodRefactoring extractMethodRefactoring;

    public ExtractMethodPage(String name, ExtractGroovyMethodRefactoring refactoring) {
        super(name);
        this.extractMethodRefactoring = refactoring;
        setTitle(name);
        setMessage("Extract Method");
    }

    public void createControl(Composite parent) {

        Composite control = new Composite(parent, SWT.NONE);
        GridLayout baseLayout = new GridLayout();
        baseLayout.numColumns = 1;

        new ExtractMethodPageContent(control, extractMethodRefactoring, this);

        control.setLayout(baseLayout);
        setControl(control);
    }

}
