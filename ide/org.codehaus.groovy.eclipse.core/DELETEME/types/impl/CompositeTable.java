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
package org.codehaus.groovy.eclipse.core.types.impl;

import java.util.List;

import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;

/**
 * Composite Symbol Table.
 * 
 * @author empovazan
 */
public class CompositeTable implements ISymbolTable, ISourceCodeContextAware, IGroovyProjectAware {
	ISymbolTable[] tables;
	
	public CompositeTable(ISymbolTable[] tables) {
		this.tables = tables;
	}
	
	public CompositeTable(List< ISymbolTable > tables) {
	    this( tables.toArray( new ISymbolTable[ 0 ] ) );
	}
	
	public GroovyDeclaration lookup(String name) {
		for (int i = 0; i < tables.length; i++) {
			GroovyDeclaration type = tables[i].lookup(name);
			if (type != null) {
				return type;
			}
		}
		return null;
	}
	
	public void setSourceCodeContext(ISourceCodeContext context) {
		for (int i = 0; i < tables.length; i++) {
			if (tables[i] instanceof ISourceCodeContextAware) {
				((ISourceCodeContextAware)tables[i]).setSourceCodeContext(context);
			}
		}
	}

	public void setGroovyProject(GroovyProjectFacade project) {
		for (int i = 0; i < tables.length; i++) {
			if (tables[i] instanceof IGroovyProjectAware) {
				((IGroovyProjectAware) tables[i]).setGroovyProject(project);
			}
		}
	}
}
