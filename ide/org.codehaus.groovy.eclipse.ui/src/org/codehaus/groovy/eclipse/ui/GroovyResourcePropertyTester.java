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
package org.codehaus.groovy.eclipse.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Property tester for testing to see if a groovy file has a main 
 * or is a test case.
 * 
 * @author David Kerber
 */
public class GroovyResourcePropertyTester extends PropertyTester {

	/**
	 * Property name to determine if a class has a main method
	 */
	public static final String hasMain = "hasMain" ;  
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(Object receiver, String property, Object[] args, Object expectedValue)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		boolean returnValue = false;

		if (hasMain.equals(property)) {
			if(receiver instanceof IAdaptable) {
				try {
				    ICompilationUnit unit = (ICompilationUnit) ((IAdaptable) receiver).getAdapter(ICompilationUnit.class);
				    if (unit == null) {
	                    IFile file = (IFile) ((IAdaptable) receiver).getAdapter(IFile.class);
				        if (file != null && Util.isJavaLikeFileName(file.getName())) {
				            unit = JavaCore.createCompilationUnitFrom(file);
				        }
				    }
				    if (unit != null) {
				        List<IType> results = GroovyProjectFacade.findAllRunnableTypes(unit);
				        returnValue = results.size() > 0;
				    }
				} catch (IllegalArgumentException e) {
					// can ignore
					// passed in non-JavaLike file name
                } catch (JavaModelException e) {
                    GroovyCore.logException("Exception when testing for main methods " + receiver, e);
                }
				
			}
		}
		return returnValue;
	}

}
