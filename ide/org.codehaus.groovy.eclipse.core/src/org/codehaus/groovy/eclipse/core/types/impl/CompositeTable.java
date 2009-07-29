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
package org.codehaus.groovy.eclipse.core.types.impl;

import java.util.List;

import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.Type;

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
	
	public Type lookup(String name) {
		for (int i = 0; i < tables.length; i++) {
			Type type = tables[i].lookup(name);
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
