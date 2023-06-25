/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.grails.ide.eclipse.groovy.debug.core.evaluation;

import groovy.lang.Binding;
import groovy.util.Proxy;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * A binding that will properly update, set
 * fields and variables in the currently debugged application
 * @author Andrew Eisenberg
 * @since 2.5.1
 */
public class JDIBinding extends Binding {

    private final JDITargetDelegate delegate;
    private final IJavaStackFrame frame;
    
    // the metaclass for the 'this'object
    private JDIMetaClass thisMetaClass;
    private boolean initialized;
    
    public JDIBinding(JDITargetDelegate delegate, IJavaStackFrame frame) {
        this.delegate = delegate;
        this.frame = frame;
        this.initialized = false;
    }
    
    public void markAsInitialized() {
        this.initialized = true;
    }
    
    public void setThisMetaClass(JDIMetaClass thisMetaClass) {
        this.thisMetaClass = thisMetaClass;
    }
    
    @Override
    public void setVariable(String property, Object newValue) {
        if (!initialized) {
            // if not yet initialized, only add to binding
            // don't try to make any changes to running application
            super.setVariable(property, newValue);
            return;
        }
        try {
            IJavaValue newJDIObject = delegate.toJDIObject(newValue);
            if (getVariables().containsKey(property)) {
                // this is a local variable
                Proxy variable = (Proxy) getVariable(property);
                variable.setAdaptee(newJDIObject);
                variable.setMetaClass(new JDIMetaClass((IJavaObject) newJDIObject, delegate));
                IVariable[] vars = frame.getVariables();
                for (IVariable var : vars) {
                    if (var.getName().equals(property)) {
                        if (var.getValue() instanceof IJavaPrimitiveValue) {
                            newJDIObject = delegate.convertToUnboxedType(newJDIObject);
                        }
                        
                        var.setValue(newJDIObject);
                        break;
                    }
                }
            
            } else {
                // assume that this is a property on 'this'
                // need to call set property on 'this'
                getMetaClass().setProperty(delegate.getThis(), property, newJDIObject);
            }
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

}
