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

import groovy.util.Proxy;

import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * Wrapper for an iterator in the JDI
 * @author Andrew Eisenberg
 * @since 2.5.1
 */
public class JDIIterator implements Iterator<Proxy> {
    
    final private IJavaObject iteratorObj;
    final private JDITargetDelegate delegate;
    public JDIIterator(IJavaObject iteratorObj, JDITargetDelegate delegate) {
        this.iteratorObj = iteratorObj;
        try {
            iteratorObj.disableCollection();
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
        this.delegate = delegate;
    }

    public boolean hasNext() {
        try {
            IJavaValue obj = iteratorObj.sendMessage("hasNext", "()Z", new IJavaValue[0], delegate.getThread(), false);
            return delegate.convertToBoolean(obj);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    public Proxy next() {
        try {
            IJavaValue value = iteratorObj.sendMessage("next", "()Ljava/lang/Object;", new IJavaValue[0], delegate.getThread(), false);
            return delegate.createProxyFor(value);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    public void remove() {
        try {
            iteratorObj.sendMessage("remove", "()V", new IJavaValue[0], delegate.getThread(), false);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }    
    }
    
}
