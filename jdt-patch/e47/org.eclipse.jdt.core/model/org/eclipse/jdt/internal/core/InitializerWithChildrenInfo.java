/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;

public class InitializerWithChildrenInfo extends InitializerElementInfo {

	protected IJavaElement[] children;
	
	public InitializerWithChildrenInfo(IJavaElement[] children) {
		this.children = children;
	}

	public IJavaElement[] getChildren() {
		return this.children;
	}

}
