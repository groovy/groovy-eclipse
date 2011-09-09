/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 * This is perhaps a confusing name. The method context refers to displaying a
 * hover that contains method arguments instead of
 * actually inserting a proposal. The assist context refers to the information
 * required to complete this content assist request.
 *
 * When there is a method context completion, there are some values that are
 * changed. But, we also need to remember the original values so tha we can
 * propose named arguments as part of the completion.
 *
 * @author andrew
 * @created Sep 2, 2011
 */
public class MethodInfoContentAssistContext extends ContentAssistContext {

    /**
     * the end of the method name for this context
     * use instead of completionEnd for getting the method context proposal
     */
    public final int methodNameEnd;

    /**
     * The expression corresponding to call.getExpression()
     * use instead of completionExpression for getting the method context
     * proposal. In the case of {@link ConstructorCallExpression}s, this
     * value is the {@link ClassNode} for the type of the call expression.
     */
    public final AnnotatedNode methodExpression;

    /**
     * The name of the method (use instead of completionExpression for getting
     * method context proposal)
     */
    public final String methodName;

    public MethodInfoContentAssistContext(int completionLocation, String completionExpression, String fullCompletionExpression,
            ASTNode completionNode, ASTNode containingCodeBlock, Expression lhsNode,
            GroovyCompilationUnit unit, AnnotatedNode containingDeclaration, int completionEnd,
 AnnotatedNode methodExpression, String methodName,
            int methodNameEnd) {
        super(completionLocation, completionExpression, fullCompletionExpression, completionNode, containingCodeBlock, lhsNode,
                ContentAssistLocation.METHOD_CONTEXT, unit, containingDeclaration, completionEnd);
        this.methodNameEnd = methodNameEnd;
        this.methodExpression = methodExpression;
        this.methodName = methodName;
    }

    @Override
    public ASTNode getPerceivedCompletionNode() {
        return methodExpression;
    }

    @Override
    public String getPerceivedCompletionExpression() {
        return methodName;
    }
}
