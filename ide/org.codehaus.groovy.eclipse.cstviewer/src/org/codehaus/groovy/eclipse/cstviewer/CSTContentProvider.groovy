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
package org.codehaus.groovy.eclipse.cstviewer;
import org.codehaus.groovy.eclipse.core.cst.CSTUtil
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.core.resources.IFile;
import org.codehaus.groovy.antlr.GroovySourceAST;

class CSTContentProvider implements ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}

		public void dispose() {}

		public Object[] getElements(Object inputElement) {
			IFile file = (IFile) inputElement 
			CSTUtil cstUtil = new CSTUtil()  

			try{
				GroovySourceAST cst = cstUtil.getCST(file)
				def elements = [ cst ]
	
				if( cst ){
					while(cst.nextSibling != null ){
	     				cst = cst.nextSibling
	     				elements.add( cst ) ; 
	     			}
					return elements ;
				} else {
					return ["No CST is currently available for ${file.name}"]
				}
			} catch (Exception e){
				println "caught" + e 
				e.printStackTrace()
				return [ e ]
			}
		}

		public Object getParent(Object child) {
			return null ;
		}

		public Object[] getChildren(Object parent) {
			if( parent instanceof Exception ){
				return parent.stackTrace 				
			} else {
				def children = getProperties( parent ) + getChildren( parent )
				children = children.findAll{ it != null } 
				return children 				
			}
		}

		public boolean hasChildren(Object parent) {
			return parent instanceof GroovySourceAST || parent instanceof Exception ;
		}

		private List getProperties(Object parent){
			def children =  parent?.class.methods.collect{ method -> 
				if( method.name.startsWith('get') && method.parameterTypes.length == 0 ){
					def name = method.name[3..-1]
					name = name[0].toLowerCase() + name[1..-1]
   					def value = method.invoke(parent, new Object[0] )
					return "$name : $value"
				}
			}
			
			return children.findAll{ it != null }
		}
		
		private List getChildren(GroovySourceAST ast){
			def children = [] 
			for( i in 0..ast.numberOfChildren - 1 ){
				children << ast.childAt( i )
			}
			return children 
		}
}