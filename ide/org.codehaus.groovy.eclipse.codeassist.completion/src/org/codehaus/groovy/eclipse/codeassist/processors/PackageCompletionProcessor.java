/*
 * Copyright 2009-2018 the original author or authors.
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
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class PackageCompletionProcessor extends AbstractGroovyCompletionProcessor implements ITypeResolver {

    protected ModuleNode module;
    protected JDTResolver resolver;

    public PackageCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    @Override
    public void setResolverInformation(ModuleNode module, JDTResolver resolver) {
        this.module = module;
        this.resolver = resolver;
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();

        char[] completionChars = getPackageCompletion(context.fullCompletionExpression);
        if (completionChars == null || completionChars.length == 0 || !mightBePackage(completionChars)) {
            return Collections.emptyList();
        }

        if (context.location == ContentAssistLocation.PARAMETER) {
            AnnotatedNode completionNode = (AnnotatedNode) context.completionNode;
            if (completionNode.getStart() < completionNode.getNameStart() &&
                    context.completionLocation >= completionNode.getNameStart()) {
                return Collections.emptyList();
            }
        }

        int expressionStart = context.completionLocation - context.fullCompletionExpression.trim().length(),
            replacementLength = context.completionEnd - expressionStart;
        SearchableEnvironment environment = getNameEnvironment();
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
            context, getJavaContext(), expressionStart, replacementLength, environment.nameLookup, monitor);
        environment.findPackages(completionChars, requestor);

        //
        List<ICompletionProposal> proposals = requestor.processAcceptedPackages();

        if (lookForTypes(completionChars)) {
            // not sure about findMembers; javadoc says method does not find member types
            boolean findMembers = true;  boolean camelCaseMatch = true;  int searchFor = getSearchFor();
            environment.findTypes(completionChars, findMembers, camelCaseMatch, searchFor, requestor, monitor);

            // check for member types
            char[] qualifier = CharOperation.subarray(completionChars, 0, CharOperation.lastIndexOf('.', completionChars));
            if (!environment.nameLookup.isPackage(CharOperation.toStrings(CharOperation.splitOn('.', qualifier)))) {
                String fullyQualifiedName = resolver.resolve(String.valueOf(qualifier)).getName();
                IType outer = environment.nameLookup.findType(fullyQualifiedName, false, 0);
                String prefix = String.valueOf(CharOperation.lastSegment(completionChars, '.'));
                try {
                    for (IType inner : outer.getTypes()) {
                        if (ProposalUtils.looselyMatches(prefix, inner.getElementName()) && isAcceptable(inner, searchFor)) {
                            requestor.acceptType(inner.getPackageFragment().getElementName().toCharArray(), inner.getElementName().toCharArray(),
                                CharOperation.splitOn('$', outer.getTypeQualifiedName().toCharArray()), inner.getFlags(), ProposalUtils.getTypeAccessibility(inner));
                        }
                    }
                } catch (JavaModelException e) {
                    GroovyContentAssist.logError(e);
                }
            }

            //
            proposals.addAll(requestor.processAcceptedTypes(resolver));
        }
        return proposals;
    }

    /**
     * Removes whitespace and does a fail-fast if a non-java identifier is found.
     */
    protected char[] getPackageCompletion(String fullCompletionExpression) {
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

    /**
     * Do not look for types if there is no '.'.  In this case,
     * type searching is handled by {@link TypeCompletionProcessor}.
     */
    protected boolean lookForTypes(char[] packageCompletion) {
        return CharOperation.contains('.', packageCompletion);
    }

    /**
     * More complete search to see if this is a valid package name.
     */
    protected boolean mightBePackage(char[] packageCompletion) {
        for (char[] segment : CharOperation.splitOn('.', packageCompletion)) {
            if (segment.length > 0) {
                // use 1.7 because backwards compatibility ensures that nothing is missed
                IStatus status = JavaConventions.validateIdentifier(String.valueOf(segment), "1.7", "1.7");
                if (status.getSeverity() >= IStatus.ERROR) {
                    return false;
                }
            }
        }
        return true;
    }

    protected int getSearchFor() {
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

    protected boolean isAcceptable(IType type, int searchFor) throws JavaModelException {
        if (searchFor == IJavaSearchConstants.TYPE) {
            return true;
        }
        int kind = TypeDeclaration.kind(type.getFlags());
        switch (kind) {
        case TypeDeclaration.ENUM_DECL:
        case TypeDeclaration.CLASS_DECL:
        case TypeDeclaration.ANNOTATION_TYPE_DECL:
            return (searchFor == IJavaSearchConstants.CLASS);
        case TypeDeclaration.INTERFACE_DECL:
            return (searchFor == IJavaSearchConstants.INTERFACE);
        }
        return false;
    }
}
