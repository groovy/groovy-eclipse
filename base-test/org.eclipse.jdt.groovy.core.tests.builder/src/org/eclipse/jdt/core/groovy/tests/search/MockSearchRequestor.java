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
package org.eclipse.jdt.core.groovy.tests.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * @author Andrew Eisenberg
 * @created Sep 1, 2009
 *
 */
public class MockSearchRequestor extends SearchRequestor {

    List<SearchMatch> matches = new ArrayList<SearchMatch>();

    @Override
    public void acceptSearchMatch(SearchMatch match) throws CoreException {
        matches.add(match);
        Collections.sort(matches, new Comparator<SearchMatch>() {
            public int compare(SearchMatch l, SearchMatch r) {
                ITypeRoot lTypeRoot = (ITypeRoot) ((IJavaElement) l.getElement()).getAncestor(IJavaElement.COMPILATION_UNIT);
                if (lTypeRoot == null) {
                    lTypeRoot = (ITypeRoot) ((IJavaElement) l.getElement()).getAncestor(IJavaElement.CLASS_FILE);
                }
                ITypeRoot rTypeRoot = (ITypeRoot) ((IJavaElement) r.getElement()).getAncestor(IJavaElement.COMPILATION_UNIT);
                if (rTypeRoot == null) {
                    rTypeRoot = (ITypeRoot) ((IJavaElement) r.getElement()).getAncestor(IJavaElement.CLASS_FILE);
                }
                if (!lTypeRoot.equals(rTypeRoot)) {
                    return lTypeRoot.getElementName().compareTo(rTypeRoot.getElementName());
                }
                 return l.getOffset() - r.getOffset();
            }
        });
    }

    String printMatches() {
        StringBuffer sb = new StringBuffer();
        for (SearchMatch match : matches) {
            sb.append(MockPossibleMatch.printMatch(match));
        }
        return sb.toString();
    }

    IJavaElement getElementNumber(int num) {
        return (IJavaElement) getMatch(num).getElement();
    }

    SearchMatch getMatch(int num) {
        return matches.get(num);
    }

    List<SearchMatch> getMatches() {
        return matches;
    }
}
