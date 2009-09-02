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

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.core.search.JavaSearchDocument;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * @author Andrew Eisenberg
 * @created Sep 1, 2009
 *
 */
public class MockPossibleMatch extends PossibleMatch {

    public MockPossibleMatch(GroovyCompilationUnit unit) {
        super(null, unit.getResource(), unit, new JavaSearchDocument(unit.getResource().getFullPath().toPortableString(), new JavaSearchParticipant()), false);
    }

    
    static String printMatch(SearchMatch match) {
        return "Match at: (" + match.getOffset() + ", " + match.getLength() + "), accuracy: " + accuracy(match) +
                "\n Matched object: " + match.getElement() + "\n";
    }
    
    static String accuracy(SearchMatch match) {
        return match.getAccuracy() == SearchMatch.A_ACCURATE ? "ACCURATE" : "INACCURATE";
    }

}
