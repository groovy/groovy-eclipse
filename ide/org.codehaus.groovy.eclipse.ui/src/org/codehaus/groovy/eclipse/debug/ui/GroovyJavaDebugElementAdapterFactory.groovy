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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.jdt.internal.debug.ui.variables.JavaDebugElementAdapterFactory 
import org.eclipse.jdt.internal.debug.ui.variables.JavaStackFrameLabelProvider;
import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * An adapterfactory that overrides {@link JavaDebugElementAdapterFactory}.
 * Where requested, the uses the {@link GroovyJavaStackFrameLabelProvider} to label 
 * stack frames of groovy internals in a lighter shade.  
 * <p>
 * In all other cases, control is delegated to an internal {@link JavaDebugElementAdapterFactory}.
 * @author Andrew Eisenberg
 * @created Jan 27, 2010
 */
class GroovyJavaDebugElementAdapterFactory implements IAdapterFactory {
	
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
		// race condition...need to ensure that this is called before jdt.debug.ui is loaded
		Platform.adapterManager.registerAdapters new GroovyJavaDebugElementAdapterFactory(), IJavaStackFrame
		fgLPFrame.connect()
	}
	
	static disconnect() {
		fgLPFrame.disconnect()
	}
}