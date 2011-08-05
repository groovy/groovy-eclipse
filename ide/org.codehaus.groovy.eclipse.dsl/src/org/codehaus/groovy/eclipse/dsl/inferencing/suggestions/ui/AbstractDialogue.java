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

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public abstract class AbstractDialogue extends TitleAreaDialog {

    private DialogueDescriptor descriptor;

    public AbstractDialogue(Shell parentShell, DialogueDescriptor descriptor) {
        super(parentShell);
        this.descriptor = descriptor;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    protected String iconLocation() {
        return null;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        setTitle(descriptor.getTitle());
        setMessage(descriptor.getMessage());
        String iconLocation = descriptor.getIconLocation();
        if (iconLocation != null && iconLocation.length() > 0) {
            setTitleImage(GroovyDSLCoreActivator.getImageDescriptor(iconLocation).createImage());
        }
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayoutFactory
                .fillDefaults()
                .margins(getDefaultCompositeHMargin(), getDefaultCompositeVMargin())
                .spacing(convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING),
                        convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING)).applyTo(composite);

        Dialog.applyDialogFont(composite);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        createCommandArea(composite);

        return composite;
    }

    protected Point getOffsetLabelLocation(String[] labels) {

        int length = SWT.DEFAULT;
        int charLength = 0;

        for (String label : labels) {
            int nameLength = label.length();
            if (nameLength > charLength) {
                charLength = nameLength;
            }
        }
        if (charLength > 0) {
            Control control = getShell();
            GC gc = new GC(control);
            Font requiredLabelFont = getRequiredParameterFont();
            gc.setFont(requiredLabelFont != null ? requiredLabelFont : control.getFont());
            FontMetrics fontMetrics = gc.getFontMetrics();
            length = Dialog.convertWidthInCharsToPixels(fontMetrics, charLength);
            gc.dispose();
        }
        Point longestLabelWidth = new Point(length, -1);
        longestLabelWidth.x += getLabelNameSeparatorOffset();
        return longestLabelWidth;
    }

    protected int getLabelNameSeparatorOffset() {
        return 5;
    }

    protected Font getRequiredParameterFont() {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
    }

    protected int getDefaultCompositeVMargin() {
        return convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    }

    protected int getDefaultCompositeHMargin() {
        return convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    }

    abstract protected void createCommandArea(Composite parent);

}
