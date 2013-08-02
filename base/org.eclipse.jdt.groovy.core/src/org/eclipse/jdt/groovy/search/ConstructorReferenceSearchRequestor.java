/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.matching.ConstructorPattern;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jun 27, 2013
 */
public class ConstructorReferenceSearchRequestor implements ITypeRequestor {

	private final SearchRequestor requestor;
	private final SearchParticipant participant;
	private final String declaringQualifiedName;
	private final String simpleName;

	public ConstructorReferenceSearchRequestor(ConstructorPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
		this.requestor = requestor;
		this.participant = participant;
		this.simpleName = String.valueOf(pattern.declaringSimpleName);
		if (pattern.declaringQualification != null && pattern.declaringQualification.length > 0) {
			this.declaringQualifiedName = String.valueOf(pattern.declaringQualification) + '.' + this.simpleName;
		} else {
			this.declaringQualifiedName = this.simpleName;
		}
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		if (!(node instanceof ConstructorCallExpression) || node.getEnd() <= 0) {
			return VisitStatus.CONTINUE;
		}
		ConstructorCallExpression ccexpr = (ConstructorCallExpression) node;
		ClassNode declaring = ccexpr.getType();

		// don't match on method parameters, only class name
		if (declaring.getName().equals(declaringQualifiedName)) {
			IJavaElement realElement = enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy ? ((GroovyClassFileWorkingCopy) enclosingElement
					.getOpenable()).convertToBinary(enclosingElement) : enclosingElement;
			SearchMatch match = null;
			match = new MethodReferenceMatch(realElement, SearchMatch.A_ACCURATE, declaring.getStart(), declaring.getLength(),
					true, false, false, false, participant, realElement.getResource());
			try {
				requestor.acceptSearchMatch(match);
			} catch (CoreException e) {
				Util.log(e, "Error reporting search match inside of " + realElement + " in resource " + realElement.getResource());
			}
		}

		return VisitStatus.CONTINUE;
	}
}
