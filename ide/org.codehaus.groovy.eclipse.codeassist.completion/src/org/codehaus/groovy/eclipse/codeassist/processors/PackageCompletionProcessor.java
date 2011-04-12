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
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Dec 10, 2009
 */
public class PackageCompletionProcessor extends AbstractGroovyCompletionProcessor {

    public PackageCompletionProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();
        char[] packageCompletionText = getPackageCompletionText(context.fullCompletionExpression);
        if(mightBePackage(packageCompletionText)) {

            int expressionStart = context.completionLocation
                    - context.fullCompletionExpression.trim().length();
            GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
                    context, getJavaContext(), expressionStart,
                    context.completionEnd - expressionStart,
                    getNameEnvironment().nameLookup, monitor);
            getNameEnvironment().findPackages(packageCompletionText, requestor);
            List<ICompletionProposal> typeProposals = requestor.processAcceptedPackages();

            boolean alsoLookForTypes = shouldLookForTypes(packageCompletionText);
            if (alsoLookForTypes) {
                getNameEnvironment().findTypes(packageCompletionText, true
                        /* find all member types, should be false when
                           in constructor*/,
                        true /* camel case match */,
                        getSearchFor(), requestor, monitor);
                typeProposals.addAll(requestor.processAcceptedTypes());
            }
            return typeProposals;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Do not look for types if there is no '.'.  In this case,
     * type searching is handled by {@link TypeCompletionProcessor}.
     * @param packageCompletionText
     * @return
     */
    private boolean shouldLookForTypes(char[] packageCompletionText) {
        return CharOperation.indexOf('.', packageCompletionText) > -1;
    }

    /**
     * more complete search to see if this is a valid package name
     * @param packageCompletionText
     * @return
     */
    private boolean mightBePackage(char[] packageCompletionText) {
        if (packageCompletionText == null || packageCompletionText.length == 0) {
            return false;
        }
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
     * @param fullCompletionExpression
     * @return
     */
    private char[] getPackageCompletionText(String fullCompletionExpression) {
        List<Character> chars = new LinkedList<Character>();
        if (fullCompletionExpression == null) {
            return new char[0];
        }
        char[] fullArray = fullCompletionExpression.toCharArray();
        for (int i = 0; i < fullArray.length; i++) {
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

    /**
     * @return
     */
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
