/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

//import java.util.HashSet;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

public class DeclarationOfReferencedMethodsPattern extends MethodPattern {

protected IJavaElement enclosingElement;
protected SimpleSet knownMethods;

public DeclarationOfReferencedMethodsPattern(IJavaElement enclosingElement) {
	super(null, null, null, null, null, null, null, null, IJavaSearchConstants.REFERENCES, R_PATTERN_MATCH);

	this.enclosingElement = enclosingElement;
	this.knownMethods = new SimpleSet();
	this.mustResolve = true;
}
}
