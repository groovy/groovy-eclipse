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

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;

/**
 * @author Andrew Eisenberg
 * @created 2013-04-30
 * @since 3.11
 */
public class NoopIndexer implements ISupplementalIndexer {

	/**
	 * @return an empty list
	 */
	@Override
	public List<char[]> extractNamedReferences(byte[] contents, ClassFileReader reader) {
		return Collections.EMPTY_LIST;
	}
}
