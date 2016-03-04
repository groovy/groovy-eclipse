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
package org.eclipse.jdt.core;


/**
 * Represents a package declaration in Java compilation unit.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPackageDeclaration extends IJavaElement, ISourceReference, IAnnotatable {
/**
 * Returns the name of the package the statement refers to.
 * This is a handle-only method.
 *
 * @return the name of the package the statement
 */
String getElementName();
}
