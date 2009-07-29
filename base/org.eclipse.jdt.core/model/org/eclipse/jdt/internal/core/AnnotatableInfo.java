/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IAnnotation;

public class AnnotatableInfo extends MemberElementInfo {

	/*
	 * The annotations of this annotatble. Empty if none.
	 */
	protected IAnnotation[] annotations = Annotation.NO_ANNOTATIONS;
	
}
