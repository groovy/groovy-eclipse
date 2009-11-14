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

package org.eclipse.jdt.core.groovy.tests.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
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
    	boolean added = false;
    	for (int i = 0; i < matches.size(); i++) {
    	    if (matches.get(i).getOffset() > match.getOffset()) {
    	    	matches.add(i, match);
    	    	added = true;
    	    	break;
    	    }
    	}
    	if (!added) {
    		matches.add(match);
    	}
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
