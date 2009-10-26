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
package org.codehaus.groovy.eclipse.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
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
	public static final String hasMain = "hasMain";
	public static final String isScript = "isScript";
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(Object receiver, String property, Object[] args, Object expectedValue)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		boolean returnValue = false;

		if (hasMain.equals(property) || isScript.equals(property)) {
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
				        if (hasMain.equals(property) || isScript.equals(property)) {
    				        List<IType> results = GroovyProjectFacade.findAllRunnableTypes(unit);
    				        returnValue = results.size() > 0;
				        }
				    }
				} catch (IllegalArgumentException e) {
					// can ignore
					// passed in non-JavaLike file name
                } catch (JavaModelException e) {
                    // can ignore situations when trying to find types that are not on the classpath
                    if (e.getStatus() != null && 
                            e.getStatus().getCode() != IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH) {
                        GroovyCore.logException("Exception when testing for main methods " + receiver, e);
                    }
                }
				
			}
		}
		return returnValue;
	}

}
