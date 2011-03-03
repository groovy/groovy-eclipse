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

import org.codehaus.groovy.ast.ASTNode;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 */
public interface ITypeRequestor {

	/**
	 * Specifies whether the visit should continue, the branch should be canceled, or the entire visit should be stopped
	 */
	public static enum VisitStatus {
		/** continue to the next ASTNode */
		CONTINUE(0),
		/** Don't visit any of this ASTNode's children */
		CANCEL_BRANCH(1),
		/** Stop visiting the enclosing memebr declaration (ie- type, field or method) */
		CANCEL_MEMBER(2),
		/** Completely end the visit */
		STOP_VISIT(3);

		int val;

		private VisitStatus(int val) {
			this.val = val;
		}

		/**
		 * @param status
		 * @param acceptASTNode
		 * @return
		 */
		public static VisitStatus merge(VisitStatus status1, VisitStatus status2) {
			if (status1.val > status2.val) {
				return status1;
			}
			return status2;
		}
	}

	/**
	 * Accepts an ast node. This node may be stored by the requestor if it is deemed to be interesting. A {@link VisitStatus} is
	 * returned that specifies if the AST Visit needs to be continued
	 */
	VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement);
}
