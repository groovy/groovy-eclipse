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

import java.util.ArrayList;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

/**
 * @see IJavaElementRequestor
 */

public class JavaElementRequestor implements IJavaElementRequestor {
	/**
	 * True if this requestor no longer wants to receive
	 * results from its <code>IRequestorNameLookup</code>.
	 */
	protected boolean fCanceled= false;
	
	/**
	 * A collection of the resulting fields, or <code>null</code>
	 * if no field results have been received.
	 */
	protected ArrayList fFields= null;

	/**
	 * A collection of the resulting initializers, or <code>null</code>
	 * if no initializer results have been received.
	 */
	protected ArrayList fInitializers= null;

	/**
	 * A collection of the resulting member types, or <code>null</code>
	 * if no member type results have been received.
	 */
	protected ArrayList fMemberTypes= null;

	/**
	 * A collection of the resulting methods, or <code>null</code>
	 * if no method results have been received.
	 */
	protected ArrayList fMethods= null;

	/**
	 * A collection of the resulting package fragments, or <code>null</code>
	 * if no package fragment results have been received.
	 */
	protected ArrayList fPackageFragments= null;

	/**
	 * A collection of the resulting types, or <code>null</code>
	 * if no type results have been received.
	 */
	protected ArrayList fTypes= null;

	/**
	 * Empty arrays used for efficiency
	 */
	protected static IField[] fgEmptyFieldArray= new IField[0];
	protected static IInitializer[] fgEmptyInitializerArray= new IInitializer[0];
	protected static IType[] fgEmptyTypeArray= new IType[0];
	protected static IPackageFragment[] fgEmptyPackageFragmentArray= new IPackageFragment[0];
	protected static IMethod[] fgEmptyMethodArray= new IMethod[0];
/**
 * @see IJavaElementRequestor
 */
public void acceptField(IField field) {
	if (fFields == null) {
		fFields= new ArrayList();
	}
	fFields.add(field);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptInitializer(IInitializer initializer) {
	if (fInitializers == null) {
		fInitializers= new ArrayList();
	}
	fInitializers.add(initializer);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptMemberType(IType type) {
	if (fMemberTypes == null) {
		fMemberTypes= new ArrayList();
	}
	fMemberTypes.add(type);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptMethod(IMethod method) {
	if (fMethods == null) {
		fMethods = new ArrayList();
	}
	fMethods.add(method);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptPackageFragment(IPackageFragment packageFragment) {
	if (fPackageFragments== null) {
		fPackageFragments= new ArrayList();
	}
	fPackageFragments.add(packageFragment);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptType(IType type) {
	if (fTypes == null) {
		fTypes= new ArrayList();
	}
	fTypes.add(type);
}
/**
 * @see IJavaElementRequestor
 */
public IField[] getFields() {
	if (fFields == null) {
		return fgEmptyFieldArray;
	}
	int size = fFields.size();
	IField[] results = new IField[size];
	fFields.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IInitializer[] getInitializers() {
	if (fInitializers == null) {
		return fgEmptyInitializerArray;
	}
	int size = fInitializers.size();
	IInitializer[] results = new IInitializer[size];
	fInitializers.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IType[] getMemberTypes() {
	if (fMemberTypes == null) {
		return fgEmptyTypeArray;
	}
	int size = fMemberTypes.size();
	IType[] results = new IType[size];
	fMemberTypes.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IMethod[] getMethods() {
	if (fMethods == null) {
		return fgEmptyMethodArray;
	}
	int size = fMethods.size();
	IMethod[] results = new IMethod[size];
	fMethods.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IPackageFragment[] getPackageFragments() {
	if (fPackageFragments== null) {
		return fgEmptyPackageFragmentArray;
	}
	int size = fPackageFragments.size();
	IPackageFragment[] results = new IPackageFragment[size];
	fPackageFragments.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IType[] getTypes() {
	if (fTypes== null) {
		return fgEmptyTypeArray;
	}
	int size = fTypes.size();
	IType[] results = new IType[size];
	fTypes.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public boolean isCanceled() {
	return fCanceled;
}
/**
 * Reset the state of this requestor.
 */
public void reset() {
	fCanceled = false;
	fFields = null;
	fInitializers = null;
	fMemberTypes = null;
	fMethods = null;
	fPackageFragments = null;
	fTypes = null;
}
/**
 * Sets the #isCanceled state of this requestor to true or false.
 */
public void setCanceled(boolean b) {
	fCanceled= b;
}
}
