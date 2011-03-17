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

import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.jdt.internal.debug.ui.variables.JavaDebugElementAdapterFactory 
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * An adapterfactory that overrides {@link JavaDebugElementAdapterFactory}.
 * Where requested, the uses the {@link GroovyJavaStackFrameLabelProvider} to label 
 * stack frames of groovy internals in a lighter shade.  
 * <p>
 * In all other cases, control is delegated to an internal {@link JavaDebugElementAdapterFactory}.
 * @author Andrew Eisenberg
 * @created Jan 27, 2010
 */
class GroovyJavaDebugElementAdapterFactoryGROOVY implements IAdapterFactory {
    
    JavaDebugElementAdapterFactory containedFactory = new JavaDebugElementAdapterFactory()
    
    static final GroovyJavaStackFrameLabelProvider fgLPFrame = new GroovyJavaStackFrameLabelProvider();
    
    Object getAdapter(Object adaptableObject, Class adapterType) {
        if (IElementLabelProvider.class.equals(adapterType)) {
            if (adaptableObject instanceof IJavaStackFrame) { 
                return fgLPFrame;
            }
        }
        return containedFactory.getAdapter(adaptableObject, adapterType)
    }
    
    Class[] getAdapterList() {
        return [ IElementLabelProvider.class, IElementContentProvider.class, IWatchExpressionFactoryAdapter.class ] as Class[]
    }
    
    static connect() {
        // first remove the JDI adapter if one exists.
        removeJDIAdapter()
        Platform.adapterManager.registerAdapters new GroovyJavaDebugElementAdapterFactoryGROOVY(), IJavaStackFrame
        fgLPFrame.connect()
    }
    
    static removeJDIAdapter() {
        // a little dicey, so wrap in try/catch
        try {
            List factories = Platform.adapterManager.factories["org.eclipse.jdt.debug.core.IJavaStackFrame"]
            int adapterIndex = factories.findIndexOf { it.class.name == "org.eclipse.core.internal.adapter.AdapterFactoryProxy" }
            if (adapterIndex >= 0) {
                factories.remove adapterIndex
            }
        } catch (e) {
            GroovyCore.logException "Exception removing JDI Adapter", e
        }
    }
    
    static disconnect() {
        fgLPFrame.disconnect()
    }
}