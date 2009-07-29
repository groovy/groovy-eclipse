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


import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.AbstractASTBasedMemberLookup;

/**
 * Looks up members in Groovy classes throughout the current project.
 * 
 * @author empovazan
 */
public class GroovyProjectMemberLookup extends AbstractASTBasedMemberLookup {
  protected GroovyProjectFacade model;

	public GroovyProjectMemberLookup(GroovyProjectFacade model) {
		this.model = model;
	}
	
	protected ClassNode getClassNodeForName(String type) {
	    return model.getClassNodeForName(type);
	}
}