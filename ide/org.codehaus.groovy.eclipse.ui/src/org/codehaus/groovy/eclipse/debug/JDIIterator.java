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
package org.codehaus.groovy.eclipse.debug;

import java.util.Iterator;

import groovy.util.Proxy;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * Wrapper for an iterator in the JDI.
 *
 * @author Andrew Eisenberg
 */
public class JDIIterator implements Iterator<Proxy> {

    private final IJavaObject iteratorObj;
    private final JDITargetDelegate delegate;

    public JDIIterator(IJavaObject iteratorObj, JDITargetDelegate delegate) {
        delegate.disableCollection(iteratorObj);
        this.iteratorObj = iteratorObj;
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        try {
            IJavaValue next = iteratorObj.sendMessage("hasNext", "()Z", new IJavaValue[0], delegate.getThread(), false);
            return delegate.convertToBoolean(next);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Proxy next() {
        try {
            IJavaValue next = iteratorObj.sendMessage("next", "()Ljava/lang/Object;", new IJavaValue[0], delegate.getThread(), false);
            return delegate.createProxyFor(next);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        try {
            iteratorObj.sendMessage("remove", "()V", new IJavaValue[0], delegate.getThread(), false);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }
}
