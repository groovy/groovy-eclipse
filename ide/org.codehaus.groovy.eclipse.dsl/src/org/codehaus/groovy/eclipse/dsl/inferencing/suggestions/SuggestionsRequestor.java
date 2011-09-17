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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * 
 * 
 * @author Nieraj Singh
 * @created 2011-09-13
 */
public class SuggestionsRequestor implements ITypeRequestor {

    private final ASTNode nodeToLookFor;

    private SuggestionDescriptor descriptor;

    public SuggestionsRequestor(ASTNode nodeToLookFor) {
        this.nodeToLookFor = nodeToLookFor;
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {

        // check to see if the enclosing element does not enclose the
        // nodeToLookFor
        if (!interestingElement(enclosingElement)) {
            return VisitStatus.CANCEL_MEMBER;
        }

        if (node instanceof ImportNode) {
            node = ((ImportNode) node).getType();
            if (node == null) {
                return VisitStatus.CONTINUE;
            }

        }

        if (isValidNode(node) && doTest(node)) {
            Expression expression = (Expression) node;

            descriptor = createDescriptor(expression, result);
            return VisitStatus.STOP_VISIT;

        }

        return VisitStatus.CONTINUE;
    }

    public SuggestionDescriptor getSuggestionDescriptor() {
        return descriptor;
    }

    protected SuggestionDescriptor createDescriptor(Expression suggestionNode, TypeLookupResult result) {

        // get the declaring type and type of the member
        ClassNode declaringTypeNode = result.declaringType;
        ClassNode suggestionTypeNode = result.type;
        VariableScope scope = result.scope;

        String declaringTypeName = declaringTypeNode.getName();
        String suggestionType = suggestionTypeNode.getName();
        Object suggestionName = suggestionNode instanceof ConstantExpression ? ((ConstantExpression) suggestionNode).getValue()
                : suggestionNode.getText();
        String name = suggestionName instanceof String ? (String) suggestionName : null;
        // TODO: must figure out a way to determine if this is static. For now,
        // user has to remember
        // to set this correctly in the UI
        boolean isStatic = false;
        String javaDoc = null;
        boolean useNamedArguments = false;
        List<MethodParameter> parameters = null;
        boolean isMethod = isMethod(scope);

        boolean isActive = true;

        return isMethod ? new SuggestionDescriptor(declaringTypeName, isStatic, name, javaDoc, suggestionType, useNamedArguments,
                parameters, isActive) : new SuggestionDescriptor(declaringTypeName, isStatic, name, javaDoc, suggestionType,
                isActive);
    }

    protected boolean isMethod(VariableScope scope) {
        if (scope != null) {
            return scope.isMethodCall();
        }
        return false;
    }

    protected boolean interestingElement(IJavaElement enclosingElement) {
        // the clinit is always interesting since the clinit contains static
        // initializers
        if (enclosingElement.getElementName().equals("<clinit>")) {
            return true;
        }

        if (enclosingElement instanceof NamedMember) {
            try {
                ISourceRange range = ((ISourceReference) enclosingElement).getSourceRange();
                return range.getOffset() <= nodeToLookFor.getStart()
                        && range.getOffset() + range.getLength() >= nodeToLookFor.getEnd();
            } catch (JavaModelException e) {
                Util.log(e);
            }
        }
        return false;
    }

    private boolean doTest(ASTNode node) {
        return node.getClass() == nodeToLookFor.getClass() && nodeToLookFor.getStart() == node.getStart()
                && nodeToLookFor.getEnd() == node.getEnd();
    }

    public static boolean isValidNode(ASTNode node) {
        return node instanceof VariableExpression || node instanceof StaticMethodCallExpression || node instanceof FieldExpression
                || node instanceof ConstantExpression;
    }
}
