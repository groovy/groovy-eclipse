/*******************************************************************************
 * Copyright (c) 2007, 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg    - Initial API and Implementation
 *******************************************************************************/
 package org.codehaus.jdt.groovy.internal;

import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;

/**
 * The constructor API for {@link InternalExtendedCompletionContext} has changed
 * between 3.7 and 4.2.  Use this class as a way to abstract away from the API changes.
 * @author Andrew Eisenberg
 * @created Feb 2, 2012
 */
public class SimplifiedExtendedCompletionContext extends InternalExtendedCompletionContext {

	public SimplifiedExtendedCompletionContext() {
		// we don't use any fields from super
		super(null, null, null, null, null, null, null, null, null);
	}

}
