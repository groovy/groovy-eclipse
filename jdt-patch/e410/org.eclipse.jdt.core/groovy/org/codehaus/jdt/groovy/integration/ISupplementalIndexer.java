/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jdt.groovy.integration;

import java.util.List;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;

/**
 * @author Andrew Eisenberg
 * @created 2013-04-30
 * @since 3.11
 */
public interface ISupplementalIndexer {

	/**
	 * Provides supplemental indexing for a class file
	 * @param contents The byte contents of the classfile
	 * @param reader a reader for the class file
	 * @return a char[] list of extra things to add to the index
	 */
	List<char[]> extractNamedReferences(byte[] contents, ClassFileReader reader);
}
