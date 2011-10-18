/*******************************************************************************
 * Copyright (c) 2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;

/**
 * Used by the inferencing engine and type lookups that require the ability to resolve types from strings
 * 
 * @author Andrew Eisenberg
 * @created Oct 18, 2011
 */
public interface ITypeResolver {
	void setResolverInformation(ModuleNode module, JDTResolver resolver);
}
