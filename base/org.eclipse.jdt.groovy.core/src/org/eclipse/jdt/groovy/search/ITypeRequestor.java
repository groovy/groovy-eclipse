/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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

@FunctionalInterface
public interface ITypeRequestor {

    /**
     * Accepts an ast node. This node may be stored by the requestor if it is deemed to be interesting.
     * A {@link VisitStatus} is returned that specifies if the AST Visit needs to be continued.
     */
    VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement);

    /**
     * Specifies whether the visit should continue, the branch should be canceled, or the entire visit should be stopped.
     */
    enum VisitStatus {
        /** continue to the next ASTNode */
        CONTINUE,
        /** Don't visit any of this ASTNode's children */
        CANCEL_BRANCH,
        /** Stop visiting the enclosing memebr declaration (ie- type, field or method) */
        CANCEL_MEMBER,
        /** Completely end the visit */
        STOP_VISIT;

        public static VisitStatus merge(VisitStatus status1, VisitStatus status2) {
            return status1.ordinal() > status2.ordinal() ? status1 : status2;
        }
    }
}
