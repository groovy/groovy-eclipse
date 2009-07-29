/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.ImportReference;

/**
 * Represents a groovy aliased import, something like 'import a.b.c.D as Foo' where Foo will be the alias. JDT creates a map from
 * the simple name for the import to the full type and for a normal import the simple name is the final part of the import. An
 * aliased import can simply return a different simple name to JDT when it is building this map.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class AliasImportReference extends ImportReference {

	// eg. 'Foo' in 'import a.b.c.D as Foo'
	private char[] alias;

	public AliasImportReference(char[] alias, char[][] tokens, long[] sourcePositions, boolean onDemand, int modifiers) {
		super(tokens, sourcePositions, onDemand, modifiers);
		this.alias = alias;
	}

	@Override
	public char[] getSimpleName() {
		return alias;
	}

}
