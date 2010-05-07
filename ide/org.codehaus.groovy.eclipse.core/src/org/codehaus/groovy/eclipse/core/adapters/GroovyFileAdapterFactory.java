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
package org.codehaus.groovy.eclipse.core.adapters;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
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
					    ModuleNode module = unit.getModuleNode();
					    if (module != null) {
        					List<ClassNode> classNodeList = module.getClasses();
        
        					if(classNodeList != null && !classNodeList.isEmpty() ) {
        						if (ClassNode.class.equals(adapterType)) {
        							returnValue = classNodeList.get(0) ;
        						} else if (ClassNode[].class.equals(adapterType)) {
        							returnValue = classNodeList.toArray(new ClassNode[0] ); 
        						}
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
