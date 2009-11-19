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

package org.eclipse.jdt.groovy.search;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Aug 31, 2009
 * 
 */
public class TypeInferencingVisitorFactory {

	/**
	 * Create a new {@link TypeInferencingVisitorWithRequestor}
	 * 
	 * @param typeRequestor
	 * @param possibleMatch
	 * @param pattern
	 * @param requestor
	 * @return a fully configured {@link TypeInferencingVisitorWithRequestor}
	 */
	public TypeInferencingVisitorWithRequestor createVisitor(PossibleMatch possibleMatch) {

		try {
			IOpenable openable = possibleMatch.openable;
			if (openable instanceof GroovyCompilationUnit) {
				TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorWithRequestor(
						(GroovyCompilationUnit) openable, createLookups());
				return visitor;
			} else {
				Util.log(new RuntimeException(), "Attempted to do a groovy visit on a non-groovy file: "
						+ new String(possibleMatch.getFileName()));
			}
		} catch (Exception e) {
			Util.log(e, "Exception when creating TypeInferencingVisitorWithRequestor for " + possibleMatch.document.getPath()); //$NON-NLS-1$
		}
		return null;
	}

	public TypeInferencingVisitorWithRequestor createVisitor(GroovyCompilationUnit unit) {
		return new TypeInferencingVisitorWithRequestor(unit, createLookups());
	}

	private ITypeLookup[] createLookups() {
		ITypeLookup[] lookups = new ITypeLookup[] { new InferenceByAssignmentStatement(), new CategoryTypeLookup(),
				new SimpleTypeLookup() };
		return lookups;
	}

}
