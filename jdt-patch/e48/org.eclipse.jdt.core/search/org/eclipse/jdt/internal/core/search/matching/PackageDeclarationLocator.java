/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

public class PackageDeclarationLocator extends PatternLocator {

protected PackageDeclarationPattern pattern;

public PackageDeclarationLocator(PackageDeclarationPattern pattern) {
	super(pattern);

	this.pattern = pattern;
}
@Override
protected int matchContainer() {
	return 0;
}
@Override
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
