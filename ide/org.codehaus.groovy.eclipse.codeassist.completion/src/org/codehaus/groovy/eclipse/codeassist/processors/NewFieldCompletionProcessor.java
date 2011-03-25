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

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Adds a list of new field proposals. All field proposals are dynamically typed static or
 * non-static fields with an initializer of a closure. Contributors should extend the
 *
 *
 * @author Andrew Eisenberg
 * @created Nov 10, 2009
 */
public class NewFieldCompletionProcessor extends AbstractGroovyCompletionProcessor {
    static class NewGroovyFieldCompletionProposal extends JavaCompletionProposal {
        NewGroovyFieldCompletionProposal(String fieldName,
                int replacementOffset, int replacementLength, int relevance,
                boolean isStatic, boolean useKeywordBeforeReplacement) {
            super(createReplacementString(fieldName, isStatic),
                    replacementOffset, replacementLength,
                    createImage(isStatic), createDisplayString(fieldName,
                            isStatic, useKeywordBeforeReplacement), relevance);
        }

        // can we do better with the initializer?
        static String createReplacementString(String fieldName, boolean isStatic) {
            if (isStatic) {
                return "static " + fieldName + " = null";
            } else {
                return "def " + fieldName;
            }
        }

        static Image createImage(boolean isStatic) {
            CompletionProposal dummy = CompletionProposal.create(CompletionProposal.FIELD_REF, -1);
            if (isStatic) {
                dummy.setFlags(Flags.AccStatic);
            }
            return ProposalUtils.getImage(dummy);
        }

        static StyledString createDisplayString(String fieldName,
                boolean isStatic, boolean useKeywordBeforeReplacement) {
            StyledString ss = new StyledString();

            // use a different styled string depending on the completion context
            // if the context completion node is a field, then must include the field modifier
            // if not, then don't include it.
            // this is because the display string must match the replacement otherwise no
            // replacement occurs.
            if (useKeywordBeforeReplacement) {
                if (isStatic) {
                    ss.append("static ", StyledString
                            .createColorRegistryStyler(
                                    JFacePreferences.HYPERLINK_COLOR, null));
                } else {
                    ss.append("def ", StyledString.createColorRegistryStyler(
                            JFacePreferences.HYPERLINK_COLOR, null));
                }
            }

            if (isStatic) {
                ss.append(fieldName);
                ss.append(" - New static property",
                        StyledString.QUALIFIER_STYLER);
            } else {
                ss.append(fieldName);
                ss.append(" - New property", StyledString.QUALIFIER_STYLER);
            }

            return ss;
        }
    }

    public NewFieldCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        List<String> unimplementedFieldNames = getAllSuggestedFieldNames(getContext());
        List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
        IType enclosingType = getContext().getEnclosingType();
        if (enclosingType != null) {
            for (String fieldName : unimplementedFieldNames) {
                proposals.add(createProposal(fieldName, getContext(), enclosingType));
            }
        }
        return proposals;
    }

    /**
     * @param context
     * @return
     */
    private List<String> getAllSuggestedFieldNames(ContentAssistContext context) {
        List<String> allNewFieldNames = new LinkedList<String>();
        try {
            List<IProposalProvider> providers = ProposalProviderRegistry.getRegistry().getProvidersFor(context.unit);
            for (IProposalProvider provider : providers) {
                List<String> newFieldNames = provider.getNewFieldProposals(context);
                if (newFieldNames != null) {
                    allNewFieldNames.addAll(newFieldNames);
                }
            }
        } catch (CoreException e) {
            GroovyCore.logException("Exception looking for proposal providers in " + context.unit.getElementName(), e);
        }

        return allNewFieldNames;
    }

    private ICompletionProposal createProposal(String fieldName,
            ContentAssistContext context, IType enclosingType) {
        int relevance = Relevance.VERY_HIGH.getRelavance();
        boolean isStatic;
        if (fieldName.startsWith(IProposalProvider.NONSTATIC_FIELD)) {
            fieldName = fieldName.substring(IProposalProvider.NONSTATIC_FIELD
                    .length());
            isStatic = false;
        } else {
            isStatic = true;
        }

        // use keyword replacement if the def/static keyword is completely or partially present
        boolean useKeywordBeforeReplacement = context.completionExpression
                .length() > 0
                && ((context.completionNode instanceof FieldNode)
                        || "def".startsWith(context.completionExpression) || "static"
                        .startsWith(context.completionExpression));

        // replace start is either the start of the field node (if using keyword replacement),
        // or it is the completion location - the length of the existing part of the expression
        int replaceStart = context.completionNode instanceof FieldNode ? context.completionNode
                .getStart() : context.completionLocation
                - context.completionExpression.length();

        // the completion length is the length of the bit of text that will be replaced
        // this is either the completion expression length or the difference between the
        // start of the field node and the completion location
        int replaceLength = context.completionNode instanceof FieldNode ? context.completionLocation
                - replaceStart
                : context.completionExpression.length();

        return new NewGroovyFieldCompletionProposal(fieldName, replaceStart,
                replaceLength,
                relevance, isStatic, useKeywordBeforeReplacement);
    }


}
