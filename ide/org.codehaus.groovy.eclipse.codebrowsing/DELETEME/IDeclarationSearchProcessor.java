 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing;

import org.eclipse.jdt.core.IJavaElement;


/**
 * Processes a search for a declaration. There are many such processors, some
 * specialized for searching for methods, other for variables, other which try
 * to infer declaration matches for dynamic types and so on.
 * 
 * @author emp
 */
public interface IDeclarationSearchProcessor {
	/**
	 * Get declaration match proposals, if any.
	 * @param info
	 * @return An array of proposals, or an empty array if there weren't any.
	 */
	public IJavaElement[] getProposals(IDeclarationSearchInfo info);
	
    public static final IJavaElement[] NONE = new IJavaElement[0];
}
