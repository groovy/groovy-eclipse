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
package org.codehaus.groovy.eclipse.refactoring.core.extract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.util.Util;

/**
 *
 * @author andrew
 * @created May 18, 2010
 */
public class InferParameterAndReturnTypesRequestor implements ITypeRequestor {


    private Map<Variable, ClassNode> inferredTypes;

    private final Region selectedText;

    public InferParameterAndReturnTypesRequestor(List<Variable> actualParameters, Set<Variable> returnParameters,
            Region selectedText) {
        inferredTypes = new HashMap<Variable, ClassNode>();
        for (Variable variable : actualParameters) {
            inferredTypes.put(variable, null);
        }
        for (Variable variable : returnParameters) {
            inferredTypes.put(variable, null);
        }
        this.selectedText = selectedText;
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        // check to see if the enclosing element does not enclose the
        // nodeToLookFor
        if (!interestingElement(enclosingElement)) {
            return VisitStatus.CANCEL_MEMBER;
        }

        if (node instanceof Variable) {
            if (inferredTypes.containsKey(node)) {
                inferredTypes.put((Variable) node, extractType(result));
            } else if (node instanceof VariableExpression) {
                Variable accessedVar = ((VariableExpression) node).getAccessedVariable();
                if (inferredTypes.containsKey(accessedVar)) {
                    inferredTypes.put((Variable) accessedVar, extractType(result));
                }
            }
        }
        return VisitStatus.CONTINUE;
    }

    private ClassNode extractType(TypeLookupResult result) {
        ClassNode type = result.type.getPlainNodeReference();
        if (type.getName().equals(VariableScope.VOID_WRAPPER_CLASS_NODE.getName())) {
            type = VariableScope.OBJECT_CLASS_NODE;
        }
        return type;
    }

    public Map<Variable, ClassNode> getInferredTypes() {
        return inferredTypes;
    }

    /**
     * @param enclosingElement
     * @return true iff enclosingElement's source location contains the source
     *         location of {@link #selectedText}
     */
    private boolean interestingElement(IJavaElement enclosingElement) {
        // the clinit is always interesting since the clinit contains static
        // initializers
        if (enclosingElement.getElementName().equals("<clinit>")) {
            return true;
        }

        if (enclosingElement instanceof ISourceReference) {
            try {
                ISourceRange range = ((ISourceReference) enclosingElement).getSourceRange();
                return range.getOffset() <= selectedText.getOffset()
                        && range.getOffset() + range.getLength() >= selectedText.getEnd();
            } catch (JavaModelException e) {
                Util.log(e);
            }
        }
        return false;
    }

}
