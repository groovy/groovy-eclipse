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
package org.codehaus.groovy.eclipse.core.adapters;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;

/**
 * This class will take an IFile and adapt it to varios Groovy friends 
 * classes / interfaces.
 * 
 * @author David Kerber
 */
public class GroovyFileAdapterFactory implements IAdapterFactory {

	private static final Class[] classes = new Class[] { ClassNode.class, ClassNode[].class }  ;

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
    public Object getAdapter(Object adaptableObject, Class adapterType) {

		Object returnValue = null ; 

		if(adaptableObject instanceof IFile) {
			IFile file = (IFile) adaptableObject ;
			if(ContentTypeUtils.isGroovyLikeFileName(file.getName())) {
				if(ClassNode.class.equals(adapterType) || ClassNode[].class.equals(adapterType)) {
					try {				
					    
					    // we know this will be a GCU because of the file extension
					    GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
						List<ClassNode> classNodeList = unit.getModuleNode().getClasses();

						if(classNodeList != null && !classNodeList.isEmpty() ) {
							if (ClassNode.class.equals(adapterType)) {
								returnValue = classNodeList.get(0) ;
							} else if (ClassNode[].class.equals(adapterType)) {
								returnValue = classNodeList.toArray(new ClassNode[0] ); 
							}
						}		
					} catch (Exception ex) {
						GroovyCore.logException("error adapting file to ClassNode", ex);
					}
				}
			}
		}
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return classes ;
	}

}
