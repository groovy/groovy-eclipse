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
package org.codehaus.jdt.groovy.internal;

import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;

/**
 * The constructor API for {@link InternalExtendedCompletionContext} has changed
 * between 3.7 and 4.2.  Use this class as a way to abstract away from the API changes.
 * @author Andrew Eisenberg
 * @created Feb 2, 2012
 * @since 3.11
 */
public class SimplifiedExtendedCompletionContext extends InternalExtendedCompletionContext {

	public SimplifiedExtendedCompletionContext() {
		// we don't use any fields from super
		super(null, null, null, null, null, null, null, null, null);
	}
}
