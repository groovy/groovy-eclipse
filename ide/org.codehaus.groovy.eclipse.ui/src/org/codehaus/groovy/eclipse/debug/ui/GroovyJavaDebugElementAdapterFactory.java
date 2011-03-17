/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug.ui;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.variables.JavaDebugElementAdapterFactory;

/**
 * An adapterfactory that overrides {@link JavaDebugElementAdapterFactory}.
 * Where requested, the uses the {@link GroovyJavaStackFrameLabelProvider} to label
 * stack frames of groovy internals in a lighter shade.
 * <p>
 * In all other cases, control is delegated to an internal {@link JavaDebugElementAdapterFactory}.
 * @author Andrew Eisenberg
 * @created Jan 27, 2010
 */
public class GroovyJavaDebugElementAdapterFactory implements IAdapterFactory {

    JavaDebugElementAdapterFactory containedFactory = new JavaDebugElementAdapterFactory();

    static final GroovyJavaStackFrameLabelProvider fgLPFrame = new GroovyJavaStackFrameLabelProvider();

    public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
        if (IElementLabelProvider.class.equals(adapterType)) {
            if (adaptableObject instanceof IJavaStackFrame) {
                return fgLPFrame;
            }
        }
        return containedFactory.getAdapter(adaptableObject, adapterType);
    }

    @SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
        return new Class[] { IElementLabelProvider.class, IElementContentProvider.class, IWatchExpressionFactoryAdapter.class };
    }

    public static void connect() {
        // first remove the JDI adapter if one exists.
        removeJDIAdapter();
        Platform.getAdapterManager().registerAdapters(new GroovyJavaDebugElementAdapterFactory(), IJavaStackFrame.class);
        fgLPFrame.connect();
    }

    public static void removeJDIAdapter() {
        // a little dicey, so wrap in try/catch
        try {
            @SuppressWarnings("unchecked")
            List<IAdapterFactory> factories = (List<IAdapterFactory>) ((AdapterManager) Platform.getAdapterManager())
                .getFactories().get("org.eclipse.jdt.debug.core.IJavaStackFrame");

            for (Iterator<IAdapterFactory> iterator = factories.iterator(); iterator.hasNext();) {
                IAdapterFactory factory = iterator.next();
                if (factory.getClass().getName().equals("org.eclipse.core.internal.adapter.AdapterFactoryProxy")) {
                    iterator.remove();
                    break;
                }
            }

        } catch (Exception e) {
            GroovyCore.logException("Exception removing JDI Adapter", e);
        }
    }

    public static void disconnect() {
        fgLPFrame.disconnect();
    }
}