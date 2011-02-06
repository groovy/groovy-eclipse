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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 *
 * @author andrew
 * @created May 18, 2010
 */
public class InferParameterAndReturnTypesRequestor implements ITypeRequestor {


    private Map<Variable, ClassNode> inferredTypes;

    public InferParameterAndReturnTypesRequestor(List<Variable> actualParameters, Set<Variable> returnParameters) {
        inferredTypes = new HashMap<Variable, ClassNode>();
        for (Variable variable : actualParameters) {
            inferredTypes.put(variable, null);
        }
        for (Variable variable : returnParameters) {
            inferredTypes.put(variable, null);
        }
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
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
}
