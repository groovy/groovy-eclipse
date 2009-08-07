/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.jdt.groovy.model;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.CompilationUnitStructureRequestor;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jun 11, 2009 Stub class that makes the constructor accessible to GroovyCompilationUnit
 */
@SuppressWarnings("restriction")
class GroovyCompilationUnitStructureRequestor extends CompilationUnitStructureRequestor {

	@SuppressWarnings("unchecked")
	protected GroovyCompilationUnitStructureRequestor(ICompilationUnit unit, CompilationUnitElementInfo unitInfo, Map newElements) {
		super(unit, unitInfo, newElements);
	}

	void setParser(SourceElementParser parser) {
		this.parser = parser;
	}

}