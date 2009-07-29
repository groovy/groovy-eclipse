/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

/**
 * The SingleTypeRequestor is an IJavaElementRequestor that 
 * only accepts one result element and then cancels.
 */
/* package */ class SingleTypeRequestor implements IJavaElementRequestor {
	/**
	 * The single accepted element
	 */
	protected IType fElement= null;
/**
 * @see IJavaElementRequestor
 */
public void acceptField(IField field) {
	// implements interface method
}
/**
 * @see IJavaElementRequestor
 */
public void acceptInitializer(IInitializer initializer) {
	// implements interface method
}
/**
 * @see IJavaElementRequestor
 */
public void acceptMemberType(IType type) {
	fElement= type;
}
/**
 * @see IJavaElementRequestor
 */
public void acceptMethod(IMethod method) {
	// implements interface method
}
/**
 * @see IJavaElementRequestor
 */
public void acceptPackageFragment(IPackageFragment packageFragment) {
	// implements interface method
}
/**
 * @see IJavaElementRequestor
 */
public void acceptType(IType type) {
	fElement= type;
}
/**
 * Returns the type accepted by this requestor, or <code>null</code>
 * if no type has been accepted.
 */
public IType getType() {
	return fElement;
}
/**
 * @see IJavaElementRequestor
 */
public boolean isCanceled() {
	return fElement != null;
}
/**
 * Reset the state of this requestor
 */
public void reset() {
	fElement= null;
}
}
