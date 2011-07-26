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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public abstract class AbstractDialogue extends TitleAreaDialog {

	public AbstractDialogue(Shell parentShell) {
		super(parentShell);

	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getTitle());
	}

	protected String iconLocation() {
		return null;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle(getTitle());
		setMessage(getMessage());
		String iconLocation = iconLocation();

		if (iconLocation != null && iconLocation.length() > 0) {
//			setTitleImage(GrailsUiActivator.getImageDescriptor(iconLocation)
//					.createImage());
		}

		Composite composite = new Composite(parent, SWT.NONE);

		GridLayoutFactory
				.fillDefaults()
				.margins(getDefaultCompositeHMargin(),
						getDefaultCompositeVMargin())
				.spacing(
						convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING),
						convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING))
				.applyTo(composite);

		Dialog.applyDialogFont(composite);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		createCommandArea(composite);

		return composite;
	}

	protected int getDefaultCompositeVMargin() {
		return convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	}

	protected int getDefaultCompositeHMargin() {
		return convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	}

	public String getMessage() {
		return "";
	}

	public String getTitle() {
		return "";
	}

	abstract protected void createCommandArea(Composite parent);

}
