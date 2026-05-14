/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IValueCheckingRule;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.JavaValidIdentifierRule;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ValueStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Validation rules for Java are typically cached as it they may use name
 * lookups which may be expensive to create frequently.
 */
public class JavaTextControl extends AbstractLabeledDialogueControl {

    private String initialValue;

    private Text textControl;

    private IValueCheckingRule cachedValueCheckingRule;

    public JavaTextControl(IDialogueControlDescriptor labelDescriptor, Point offsetLabelLocation, String initialValue) {
        super(labelDescriptor, offsetLabelLocation);
        this.initialValue = initialValue;
    }

    @Override
    protected Control getManagedControl(Composite parent) {
        textControl = new Text(parent, SWT.BORDER);
        if (initialValue != null) {
            textControl.setText(initialValue);
        }
        textControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textControl.addModifyListener(e -> notifyControlChange(textControl.getText(), textControl));

        return textControl;
    }

    protected Text getTextControl() {
        return textControl;
    }

    @Override
    protected ValueStatus isControlValueValid(Control control) {
        if (control == textControl) {
            String stringVal = textControl.getText();
            return isControlValueValid(stringVal);
        }
        return null;
    }

    /**
     * If not explicitly checked, it will be default assume the value is valid
     */
    protected ValueStatus isControlValueValid(String value) {
        if (cachedValueCheckingRule == null) {
            cachedValueCheckingRule = getCachedValidationRule();
        }

        if (cachedValueCheckingRule != null) {
            return cachedValueCheckingRule.checkValidity(value);
        }
        return ValueStatus.getValidStatus(value);
    }

    /**
     * Instantiate a validation rule that is cached by the control. Override
     * this method if a validation rule may be expensive to create every time it
     * is needed (e.g., a Java validation rule that uses name lookup to validate
     * a Java type). If null, no validation will be performed on a control
     * value,
     * and the control value will be assumed to be valid by default.
     *
     * @return validation rule to cache, or null if no cached validation is
     *         required.
     */
    protected IValueCheckingRule getCachedValidationRule() {
        return new JavaValidIdentifierRule();
    }
}
