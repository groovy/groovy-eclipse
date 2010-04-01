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
package org.codehaus.groovy.eclipse.refactoring.ui.pages.inlineMethod;

import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.ui.eclipse.TextInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class InlineMethodPage extends TextInputWizardPage{
	
	private InlineMethodInfo info;

	public InlineMethodPage(String name,InlineMethodInfo info) {
		super(name,true);
		this.info = info;
		setTitle(name);
		setMessage("Inline a Method");
	}

	public void createControl(Composite parent) {

		Composite control = new Composite(parent, SWT.NONE);
		GridLayout baseLayout = new GridLayout();
		baseLayout.numColumns = 1;
		
		new InlineMethodWizard(control,info);
		
		control.setLayout(baseLayout);
		setControl(control);
	}

}
