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
package org.codehaus.groovy.eclipse.core.types;

/**
 * A context for type evaluation. The type evaluation context provides a type evaluator with symbol types, fields,
 * properties and methods for use in evaluating expressions.
 * 
 * @author empovazan
 */
public interface ITypeEvaluationContext {
	ClassLoader getClassLoader();
	
	String[] getImports();
	
	Type lookupSymbol(String name);

	Field lookupField(String type, String name, boolean accessible, boolean staticAccess);

	Property lookupProperty(String type, String name, boolean accessible, boolean staticAccess);

	Method lookupMethod(String type, String name, String[] paramTypes, boolean accessible,
			boolean staticAccess);
}