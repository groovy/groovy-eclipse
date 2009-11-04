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

package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * @author Andrew Eisenberg
 * @created Oct 29, 2009
 *
 */
public class StaticlyUnknownReferenceRequestor implements ITypeRequestor {

    List<ASTNode> unknownNodes = new LinkedList<ASTNode>();
    
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
            IJavaElement enclosingElement) {
        if (result.confidence == TypeConfidence.UNKNOWN && node.getEnd() > 0) {
        	unknownNodes.add(node);
            return VisitStatus.CANCEL_BRANCH;
        }
        return VisitStatus.CONTINUE;
    }

    
}
