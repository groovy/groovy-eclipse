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

package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Dec 10, 2009
 */
public class ConstructorCompletionProcessor extends AbstractGroovyCompletionProcessor implements ITypeResolver {

    private JDTResolver resolver;

    public ConstructorCompletionProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();
        char[] constructorCompletionText = getCompletionText(context.fullCompletionExpression);
        if (constructorCompletionText == null) {
            return Collections.emptyList();
        }
        int completionExprStart;
        if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
            completionExprStart = ((MethodInfoContentAssistContext) context).methodNameEnd
                    - ((MethodInfoContentAssistContext) context).methodName.length();
        } else {
            completionExprStart = context.completionLocation - constructorCompletionText.length;
        }

        if (completionExprStart < 0) {
            // will get here for some kinds of bad syntax
            return Collections.emptyList();
        }

        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
                context, getJavaContext(), completionExprStart,
                context.completionEnd - completionExprStart,
                getNameEnvironment().nameLookup, monitor);
        getNameEnvironment().findConstructorDeclarations(
                constructorCompletionText, true, requestor, monitor);

        List<ICompletionProposal> constructorProposals = requestor.processAcceptedConstructors(findUsedParameters(context),
                resolver);

        return constructorProposals;
    }

    private Set<String> findUsedParameters(ContentAssistContext context) {
        if (context.location != ContentAssistLocation.METHOD_CONTEXT) {
            return Collections.emptySet();
        }
        Set<String> usedParams = new HashSet<String>();
        ASTNode completionNode = context.completionNode;
        if (completionNode instanceof ConstructorCallExpression) {
            // next find out if there are any existing named args
            ConstructorCallExpression call = (ConstructorCallExpression) completionNode;
            Expression arguments = call.getArguments();
            if (arguments instanceof TupleExpression) {
                for (Expression maybeArg : ((TupleExpression) arguments).getExpressions()) {
                    if (maybeArg instanceof MapExpression) {
                        arguments = maybeArg;
                        break;
                    }
                }
            }

            // now remove the arguments that are already written
            if (arguments instanceof MapExpression) {
                // Do extra filtering to determine what parameters are still
                // available
                MapExpression enclosingCallArgs = (MapExpression) arguments;
                for (MapEntryExpression entry : enclosingCallArgs.getMapEntryExpressions()) {
                    usedParams.add(entry.getKeyExpression().getText());
                }
            }
        }
        return usedParams;
    }

    /**
     * removes whitespace and the 'new ' prefix and does a fail-fast if a
     * non-java identifier is found
     *
     * @param fullCompletionExpression
     * @return
     */
    private char[] getCompletionText(String fullCompletionExpression) {
        List<Character> chars = new LinkedList<Character>();
        if (fullCompletionExpression == null) {
            return new char[0];
        }
        char[] fullArray = fullCompletionExpression.toCharArray();
        int newIndex = CharOperation.indexOf("new ".toCharArray(), fullArray,
                true) + 4;
        if (newIndex == -1) {
            return null;
        }
        for (int i = newIndex; i < fullArray.length; i++) {
            if (Character.isWhitespace(fullArray[i])) {
                continue;
            } else if (Character.isJavaIdentifierPart(fullArray[i]) || fullArray[i] == '.') {
                chars.add(fullArray[i]);
            } else {
                // fail fast if something odd is found like parens or brackets
                return null;
            }
        }
        char[] res = new char[chars.size()];
        int i = 0;
        for (Character c : chars) {
            res[i] = c.charValue();
            i++;
        }
        return res;
    }

    public void setResolverInformation(ModuleNode module, JDTResolver resolver) {
        this.resolver = resolver;
    }
}
