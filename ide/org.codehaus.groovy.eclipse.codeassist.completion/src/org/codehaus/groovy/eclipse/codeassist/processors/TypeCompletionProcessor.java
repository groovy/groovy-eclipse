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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.CharArraySourceBuffer;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder.NameAndLocation;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
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
        String toSearch = context.completionExpression.startsWith("new ") ? context.completionExpression.substring(4) : context.completionExpression;
        if (shouldShowTypes(context, toSearch)) {
            return Collections.emptyList();
        }

        int expressionStart = findExpressionStart(context);
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
                context, getJavaContext(), expressionStart,
                context.completionEnd - expressionStart,
                getNameEnvironment().nameLookup, monitor);

        getNameEnvironment().findTypes(toSearch.toCharArray(),
                true, // all member types, should be false when in constructor
                true, // camel case match
                getSearchFor(), requestor, monitor);

        List<ICompletionProposal> typeProposals = requestor.processAcceptedTypes(resolver);

        return typeProposals;
    }

    /**
     * <ul>
     * <li>Don't show types if there is no previous text (except for imports or annotations)
     * <li>Don't show types if there is a '.'
     * <li>Don't show types when in a class body and there is a type declaration immediately before
     * </ul>
     */
    private boolean shouldShowTypes(ContentAssistContext context, String toSearch) {
        if (toSearch == null || toSearch.length() == 0) {
            switch (context.location) {
            case ANNOTATION:
            case IMPORT:
                break;
            default:
                return false;
            }
        }
        return context.fullCompletionExpression.contains(".") ||
            isBeforeTypeName(context.location, context.unit, context.completionLocation);
    }

    private int findExpressionStart(ContentAssistContext context) {
        // remove "new"
        int completionLength;
        if (context.completionExpression.startsWith("new ")) {
            completionLength = context.completionExpression.substring("new ".length()).trim().length();
        } else {
            completionLength = context.completionExpression.length();
        }

        int expressionStart = context.completionLocation-completionLength;
        return expressionStart;
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

    private boolean isBeforeTypeName(ContentAssistLocation location, GroovyCompilationUnit unit, int completionLocation) {
        if (location != ContentAssistLocation.CLASS_BODY) {
            return false;
        }
        NameAndLocation nameAndLocation = new ExpressionFinder()
            .findPreviousTypeNameToken(new CharArraySourceBuffer(unit.getContents()), completionLocation);
        if (nameAndLocation == null) {
            return false;
        }
        if (!(getContext().completionNode instanceof FieldNode)) {
            return false;
        }
        return !FIELD_MODIFIERS.contains(nameAndLocation.name.trim());
    }
}
