/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
/**
 * 
 */
package org.codehaus.groovy.eclipse.adapters;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IFileEditorInput;

/**
 * This class will take an FileEditorInput and adapt it to varios Groovy friendly 
 * classes / interfaces.
 * 
 * @author David Kerber
 */
public class GroovyIFileEditorInputAdapterFactory implements IAdapterFactory {
	
	private static final Class< ? >[] classes = new Class[] { ClassNode.class }  ;

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
		
		Object returnValue = null ; 
		
		if( (ClassNode.class.equals(adapterType) || ClassNode[].class.equals(adapterType) ) && adaptableObject instanceof IFileEditorInput) {
			try {
				IFileEditorInput fileEditor = (IFileEditorInput) adaptableObject ;
				returnValue = fileEditor.getFile().getAdapter(adapterType);
			} catch (Exception ex) {
				GroovyCore.logException("error adapting file to ClassNode", ex);
			}
		}
		
		
		return returnValue ;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
		return classes ; 
	}

}
