
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
package org.codehaus.groovy.eclipse.cstviewer

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetParser
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.Viewer
import org.eclipse.core.resources.IFile
import org.eclipse.core.runtime.CoreException
import org.codehaus.groovy.antlr.GroovySourceAST

class CSTContentProvider implements ITreeContentProvider {

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {}

    public void dispose() {}

    public Object[] getElements(Object inputElement) {
        IFile file = (IFile) inputElement 
        GroovySnippetParser parser = new GroovySnippetParser()
        try {
            GroovySourceAST cst = parser.parseForCST(getDocumentContent(file))
            def elements = [cst]
            if (cst) {
                while(cst.nextSibling != null) {
                    cst = cst.nextSibling
                    elements.add( cst )
                }
                return elements
            } else {
                return ["No CST is currently available for ${file.name}"]
            }
        } catch (Exception e){
            println "caught" + e 
            e.printStackTrace()
            return [ e ]
        }
    }

		
    public String getDocumentContent(IFile file) {
        StringBuilder out = new StringBuilder()
        try {
            InputStream ins = file.getContents()
            byte[] b = new byte[4096]
            for (int n; (n = ins.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
        } catch (IOException e) {
            e.printStackTrace()
        } catch (CoreException e) {
            e.printStackTrace()
        }
        return out.toString()
    }

		 
		public Object getParent(Object child) {
			return null
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof Exception) {
				return parent.stackTrace			
			} else {
				def children = getProperties(parent) + getChildren(parent)
				children = children.findAll { it != null } 
				return children 				
			}
		}

		public boolean hasChildren(Object parent) {
			return parent instanceof GroovySourceAST || parent instanceof Exception
		}

		private List<String> getProperties(Object parent){
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
		
		private List<GroovySourceAST> getChildren(GroovySourceAST ast){
			def children = [] 
			for( i in 0..ast.numberOfChildren - 1 ){
				children << ast.childAt( i )
			}
			return children 
		}
}