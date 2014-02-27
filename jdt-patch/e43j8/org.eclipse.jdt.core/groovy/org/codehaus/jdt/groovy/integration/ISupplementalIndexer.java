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

import java.util.List;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;

/**
 * 
 * @author Andrew Eisenberg
 * @created 2013-04-30
 */
public interface ISupplementalIndexer {

	/**
	 * Provides supplemental indexing for a class file
	 * @param contents The byte contents of the classfile
	 * @param reader a reader for the class file
	 * @return a char[] list of extra things to add to the index
	 */
	List extractNamedReferences(byte[] contents, ClassFileReader reader);

}
