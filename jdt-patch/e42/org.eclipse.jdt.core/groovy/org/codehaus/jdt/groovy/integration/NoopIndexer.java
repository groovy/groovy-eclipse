/*******************************************************************************
 * Copyright (c) 2013 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.integration;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;

/**
 * 
 * @author Andrew Eisenberg
 * @created 2013-04-30
 */
public class NoopIndexer implements ISupplementalIndexer {

	/**
	 * @return an empty list
	 */
	public List extractNamedReferences(byte[] contents, ClassFileReader reader) {
		return Collections.EMPTY_LIST;
	}

}
