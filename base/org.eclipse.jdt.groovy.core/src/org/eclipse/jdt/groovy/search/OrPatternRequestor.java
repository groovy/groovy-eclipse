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

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author Andrew Eisenberg
 * @created Nov 17, 2009
 * 
 */
public class OrPatternRequestor implements ITypeRequestor {
	private final ITypeRequestor[] requestors;

	public OrPatternRequestor(List<ITypeRequestor> requestors) {
		this.requestors = requestors.toArray(new ITypeRequestor[requestors.size()]);
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		VisitStatus status = VisitStatus.CONTINUE;
		for (ITypeRequestor requestor : requestors) {
			status = VisitStatus.merge(status, requestor.acceptASTNode(node, result, enclosingElement));
		}
		return status;
	}

}
