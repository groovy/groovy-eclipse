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

import org.eclipse.jface.viewers.LabelProvider
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes ; 

class CSTLabelProvider extends LabelProvider {

	public String getText(Object object){
		if( object instanceof GroovySourceAST){
			return getCSTLabel( object )
		} else {
			return String.valueOf( object ) 
		}
	}
	
	private String getCSTLabel(GroovySourceAST ast){
		GroovySourceAST identifier = ast.childOfType( GroovyTokenTypes.IDENT )
		String label = ast.toString() ; 
		if( identifier ){
			label += " - $identifier"
		}
		return label 
	}

}