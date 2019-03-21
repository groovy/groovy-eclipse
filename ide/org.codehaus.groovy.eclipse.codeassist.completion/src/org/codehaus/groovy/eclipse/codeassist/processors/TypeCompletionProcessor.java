/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.CharArraySourceBuffer;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder.NameAndLocation;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class TypeCompletionProcessor extends AbstractGroovyCompletionProcessor implements ITypeResolver {

    private static final Set<String> FIELD_MODIFIERS = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList("private", "protected", "public", "static", "final")));

    protected JDTResolver resolver;

    public TypeCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public void setResolverInformation(ModuleNode module, JDTResolver resolver) {
        this.resolver = resolver;
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();
        String prefix = context.completionExpression.replaceFirst("^new\\s+", "");
        if (!canProposeTypes(context, prefix)) {
            return Collections.emptyList();
        }

        int replacementLength = prefix.length();
        int replacementOffset = context.completionLocation - replacementLength;
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
            context, getJavaContext(), replacementOffset, replacementLength, getNameEnvironment().nameLookup, monitor);

        boolean findMembers = true /*should be false when in constructor*/, camelCaseMatch = true;
        getNameEnvironment().findTypes(prefix.toCharArray(), findMembers, camelCaseMatch, getSearchFor(), requestor, monitor);

        return requestor.processAcceptedTypes(resolver);
    }

    protected int getSearchFor() {
        switch(getContext().location) {
        case EXTENDS:
            return IJavaSearchConstants.CLASS;
        case IMPLEMENTS:
            return IJavaSearchConstants.INTERFACE;
        case EXCEPTIONS:
            return IJavaSearchConstants.CLASS;
        case ANNOTATION:
            return IJavaSearchConstants.ANNOTATION_TYPE;
        default:
            return IJavaSearchConstants.TYPE;
        }
    }

    /**
     * Don't show types...
     * <ul>
     * <li>if there is no previous text (except for imports or annotations)
     * <li>if completing a method or constructor parameter name
     * <li>if there is a '.'
     * <li>when in a class body and there is a type declaration immediately before
     * </ul>
     */
    private static boolean canProposeTypes(ContentAssistContext context, String prefix) {
        if (prefix.length() == 0) {
            return (context.location == ContentAssistLocation.ANNOTATION || context.location == ContentAssistLocation.IMPORT);
        }
        if (context.location == ContentAssistLocation.PARAMETER) {
            AnnotatedNode completionNode = (AnnotatedNode) context.completionNode;
            if (completionNode.getStart() < completionNode.getNameStart() &&
                    context.completionLocation >= completionNode.getNameStart()) {
                return false;
            }
        }
        if (context.fullCompletionExpression.contains(".")) {
            return false;
        }
        return !isBeforeTypeName(context);
    }

    private static boolean isBeforeTypeName(ContentAssistContext context) {
        if (context.location != ContentAssistLocation.CLASS_BODY) {
            return false;
        }
        NameAndLocation nameAndLocation = new ExpressionFinder().findPreviousTypeNameToken(
            new CharArraySourceBuffer(context.unit.getContents()), context.completionLocation);
        if (nameAndLocation == null) {
            return false;
        }
        if (!(context.completionNode instanceof FieldNode)) {
            return false;
        }
        return !FIELD_MODIFIERS.contains(nameAndLocation.name.trim());
    }
}
