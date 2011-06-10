/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.integration.internal.ISearchScopeExpander;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.CollectingSearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;

/**
 * Expands the search scope of a refactoring search so that private declarations
 * still have a project-wide search.
 * 
 * @author andrew
 * @created Jun 9, 2011
 */
public class RefactoringSearchScopeExpander implements ISearchScopeExpander {

    public IJavaSearchScope expandSearchScope(IJavaSearchScope scope, SearchPattern pattern, SearchRequestor requestor) {
        // at this point, we already know this is a groovy project, so no need
        // to check again.
        if (pattern.focus.getOpenable() instanceof ICompilationUnit && requestor instanceof CollectingSearchRequestor) {
            try {
                return RefactoringScopeFactory.create(pattern.focus, false, true);
            } catch (JavaModelException e) {
                GroovyCore.logException(
                        "Exception thrown when trying to expand the search scope of " + pattern.focus.getElementName(), e);
            }
        }
        return scope;
    }

}
