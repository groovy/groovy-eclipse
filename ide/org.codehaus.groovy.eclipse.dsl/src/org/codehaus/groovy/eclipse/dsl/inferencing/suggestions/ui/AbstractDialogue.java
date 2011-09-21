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
import java.util.Map.Entry;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public abstract class AbstractDialogue extends TitleAreaDialog {

    private Map<IDialogueControlDescriptor, SetValue> invalidValues;

    protected static final String EMPTY_ERROR_MESSAGE = "  ";

    public AbstractDialogue(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Must not be null
     * 
     * @return
     */
    abstract protected DialogueDescriptor getDialogueDescriptor();

    protected boolean isResizable() {
        return true;
    }

    protected String iconLocation() {
        return null;
    }

    protected Control createDialogArea(Composite parent) {
        invalidValues = new HashMap<IDialogueControlDescriptor, SetValue>();
        DialogueDescriptor descriptor = getDialogueDescriptor();
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

    protected Control createContents(Composite parent) {

        Control control = super.createContents(parent);
        // Set "OK" button state after all dialogue contents are created to
        // ensure the
        // dialogue buttons have already been created.
        if (invalidValues != null) {
            for (SetValue setValue : invalidValues.values()) {
                if (setValue.getValue() == null) {
                    enableOKButton(false);
                }
            }
        }
        return control;
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

    /**
     * Control listener to be used if the dialogue has required values that
     * determine if
     * the user can click "OK" on the dialogue. Two options exist:
     * 1. The value is not initially required (i.e. value is empty or null), but
     * once a value is set, it needs
     * to be verified before enabling the OK button
     * 2. The value is required, and an empty or null value is not acceptable.
     * 
     * @author Nieraj Singh
     * @created 2011-08-06
     */
    abstract protected class ValidatedValueSelectionListener implements IControlSelectionListener {

        /**
         * User this only if values should not be initially marked as required
         * until a value is actually set
         */
        public ValidatedValueSelectionListener() {

        }

        public ValidatedValueSelectionListener(IDialogueControlDescriptor descriptor, Object initialValue) {
            // Add any instances of the listener to the list of descriptors that
            // need to
            // be observed for value validation
            if (descriptor != null) {
                invalidValues.put(descriptor, new SetValue(initialValue, null));
            }
        }

        public void handleSelection(ControlSelectionEvent event) {
            handleValidatedValue(event);
            notifyValidValueSet(event.getControlDescriptor(), event.getSelectionData());
        }

        public void handleInvalidSelection(ControlSelectionEvent event) {
            IDialogueControlDescriptor descriptor = event.getControlDescriptor();
            invalidValues.put(descriptor, new SetValue(event.getSelectionData(), event.getErrorMessage()));
            displayInvalidValueError(descriptor, event.getErrorMessage(), true);
        }

        abstract protected void handleValidatedValue(ControlSelectionEvent event);
    }

    protected void notifyValidValueSet(IDialogueControlDescriptor descriptor, Object value) {
        invalidValues.remove(descriptor);

        // Check if there are any remaining inValid values and display the next
        // error
        if (invalidValues != null) {

            for (Entry<IDialogueControlDescriptor, SetValue> entry : invalidValues.entrySet()) {
                if (entry.getValue().getValue() == null) {
                    displayInvalidValueError(entry.getKey(), entry.getValue().getErrorMessage(), true);
                    return;
                }
            }
        }
        setErrorMessage(null);
        enableOKButton(true);
    }

    /**
     * Checks if any required values are set with valid values before enabling
     * the "OK" button.
     */
    protected void displayInvalidValueError(IDialogueControlDescriptor descriptor, String errorMessage, boolean displayErrorMessage) {

        StringBuffer missingFields = new StringBuffer();

        missingFields.append("Value (");
        missingFields.append(descriptor.getLabel());
        missingFields.append(')');
        if (errorMessage != null && errorMessage.length() > 0) {
            missingFields.append(':');
            missingFields.append(' ');

            missingFields.append(errorMessage);
        } else {
            missingFields.append(" is missing");
        }

        if (displayErrorMessage) {
            setErrorMessage(missingFields.toString());
        }
        enableOKButton(false);
    }

    /**
     * Should only be called once the button bars have been created for the
     * dialogue
     * 
     * @param enable
     */
    protected void enableOKButton(boolean enable) {
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (okButton != null && !okButton.isDisposed()) {
            okButton.setEnabled(enable);
        }
    }

    protected class SetValue {

        private Object value;

        private String errorMessage;

        public SetValue(Object value, String errorMessage) {
            this.value = value;
            this.errorMessage = errorMessage;
        }

        public Object getValue() {
            return value;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

    }

}
