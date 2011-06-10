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
package org.codehaus.jdt.groovy.integration.internal;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * This class is a hook for the refactoring support to install an object that can expand the search scope of a refactoring if the
 * target is a private element of a {@link GroovyCompilationUnit}. We could use an adapter here, but that seems heavyweight for this
 * use.
 * 
 * @author Andrew Eisenberg
 * @created Jun 9, 2011
 */
public interface ISearchScopeExpander {
	IJavaSearchScope expandSearchScope(IJavaSearchScope scope, SearchPattern pattern, SearchRequestor requestor);
}
