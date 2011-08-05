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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public abstract class AbstractControl implements IDialogueControl {

    private IControlSelectionListener listener;

    private Map<IDialogueControlDescriptor, Control> controls;

    private Shell shell;

    protected void notifyControlChange(Object data, IDialogueControlDescriptor descriptor) {

        if (listener != null && descriptor != null) {
            listener.handleSelection(new ControlSelectionEvent(data, descriptor));
        }
    }

    public Composite createControlArea(Composite parent) {

        controls = new HashMap<IDialogueControlDescriptor, Control>();
        Map<IDialogueControlDescriptor, Control> createdControls = createManagedControls(parent);
        if (createdControls != null) {
            controls.putAll(createdControls);
        }
        shell = parent.getShell();
        return parent;
    }
    
    /**
     * Shell where controls are shown.
     * @return
     */
    protected Shell getShell() {
        return shell;
    }

    public void setEnabled(boolean enable) {
        if (controls != null) {
            for (Control control : controls.values()) {
                if (!control.isDisposed()) {
                    control.setEnabled(enable);
                }
            }
        }
    }

    public void addSelectionListener(IControlSelectionListener listener) {
        this.listener = listener;
    }

    abstract protected Map<IDialogueControlDescriptor, Control> createManagedControls(Composite parent);

    /**
     * Control can be assumed to be enabled and not disposed.
     * 
     * @param control
     * @param value
     */
    abstract protected void setControlValue(Control control, Object value);

    public void changeControlValue(ControlSelectionEvent event) {
        IDialogueControlDescriptor descriptor = event.getControlDescriptor();

        if (descriptor != null) {
            Control control = controls.get(descriptor);
            if (control != null && !control.isDisposed() && control.isEnabled()) {
                setControlValue(control, event.getSelectionData());
            }

        }

    }

}
