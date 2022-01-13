/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug.ui;

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
 */
public class GroovyJavaDebugElementAdapterFactory implements IAdapterFactory {

    private final JavaDebugElementAdapterFactory delegateFactory = new JavaDebugElementAdapterFactory();

    private final GroovyJavaStackFrameLabelProvider stackFrameLabelProvider = new GroovyJavaStackFrameLabelProvider();

    public GroovyJavaDebugElementAdapterFactory() {
        // first remove the JDI adapter if one exists
        try {
            List<IAdapterFactory> factories = ((AdapterManager) Platform.getAdapterManager()).getFactories().get("org.eclipse.jdt.debug.core.IJavaStackFrame");
            for (IAdapterFactory factory : factories) {
                if (factory.getClass().getName().equals("org.eclipse.core.internal.adapter.AdapterFactoryProxy")) {
                    factories.remove(factory);
                    break;
                }
            }
        } catch (Exception | LinkageError e) {
            GroovyCore.logException("Exception removing JDI Adapter", e);
        }

        stackFrameLabelProvider.connect();
    }

    // TODO: stackFrameLabelProvider.disconnect();

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] {IElementLabelProvider.class, IElementContentProvider.class, IWatchExpressionFactoryAdapter.class};
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (IElementLabelProvider.class.equals(adapterType)) {
            if (adaptableObject instanceof IJavaStackFrame) {
                return (T) stackFrameLabelProvider;
            }
        }
        return delegateFactory.getAdapter(adaptableObject, adapterType);
    }
}
