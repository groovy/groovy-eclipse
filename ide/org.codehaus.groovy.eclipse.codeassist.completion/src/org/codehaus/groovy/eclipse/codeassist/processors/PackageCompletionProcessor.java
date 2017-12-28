/*
 * Copyright 2009-2017 the original author or authors.
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
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class PackageCompletionProcessor extends AbstractGroovyCompletionProcessor implements ITypeResolver {

    protected JDTResolver resolver;

    public PackageCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    @Override
    public void setResolverInformation(ModuleNode module, JDTResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();

        char[] packageCompletionText = getPackageCompletionText(context.fullCompletionExpression);
        if (packageCompletionText == null || packageCompletionText.length == 0 || !mightBePackage(packageCompletionText)) {
            return Collections.emptyList();
        }

        if (context.location == ContentAssistLocation.PARAMETER) {
            AnnotatedNode completionNode = (AnnotatedNode) context.completionNode;
            if (completionNode.getStart() < completionNode.getNameStart() &&
                    context.completionLocation >= completionNode.getNameStart()) {
                return Collections.emptyList();
            }
        }

        int expressionStart = context.completionLocation - context.fullCompletionExpression.trim().length();
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
            context, getJavaContext(), expressionStart, context.completionEnd - expressionStart, getNameEnvironment().nameLookup, monitor);
        getNameEnvironment().findPackages(packageCompletionText, requestor);
        List<ICompletionProposal> proposals = requestor.processAcceptedPackages();

        boolean alsoLookForTypes = shouldLookForTypes(packageCompletionText);
        if (alsoLookForTypes) {
            getNameEnvironment().findTypes(packageCompletionText,
                true /* find all member types, should be false when in constructor*/,
                true /* camel case match */, getSearchFor(), requestor, monitor);
            proposals.addAll(requestor.processAcceptedTypes(resolver));
        }
        return proposals;
    }

    /**
     * Do not look for types if there is no '.'.  In this case,
     * type searching is handled by {@link TypeCompletionProcessor}.
     */
    private boolean shouldLookForTypes(char[] packageCompletionText) {
        return CharOperation.indexOf('.', packageCompletionText) > -1;
    }

    /**
     * more complete search to see if this is a valid package name
     */
    private boolean mightBePackage(char[] packageCompletionText) {
        String text = String.valueOf(packageCompletionText);
        String[] splits = text.split("\\.");
        for (String split : splits) {
            // use 1.7 because backwards compatibility ensures that nothing is missed.
            if (split.length() > 0) {
                IStatus status = JavaConventions.validateIdentifier(split, "1.7", "1.7");
                if (status.getSeverity() >= IStatus.ERROR) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * removes whitespace and does a fail-fast if a non-java identifier is found
     */
    private char[] getPackageCompletionText(String fullCompletionExpression) {
        List<Character> chars = new LinkedList<>();
        if (fullCompletionExpression == null) {
            return CharOperation.NO_CHAR;
        }
        char[] fullArray = fullCompletionExpression.toCharArray();
        for (int i = 0, n = fullArray.length; i < n; i += 1) {
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
            i += 1;
        }
        return res;
    }

    private int getSearchFor() {
        switch(getContext().location) {
        case EXTENDS:
            return IJavaSearchConstants.CLASS;
        case IMPLEMENTS:
            return IJavaSearchConstants.INTERFACE;
        case EXCEPTIONS:
            return IJavaSearchConstants.CLASS;
        default:
            return IJavaSearchConstants.TYPE;
        }
    }
}
