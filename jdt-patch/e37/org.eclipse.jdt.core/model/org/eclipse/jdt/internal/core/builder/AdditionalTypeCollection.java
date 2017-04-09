/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

public class AdditionalTypeCollection extends ReferenceCollection {

char[][] definedTypeNames;

protected AdditionalTypeCollection(char[][] definedTypeNames, char[][][] qualifiedReferences, char[][] simpleNameReferences, char[][] rootReferences) {
	super(qualifiedReferences, simpleNameReferences, rootReferences);
	this.definedTypeNames = definedTypeNames; // do not bother interning member type names (i.e. 'A$M')
}
}

