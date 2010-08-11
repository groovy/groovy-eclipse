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

import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.Relevance;
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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Adds a list of new field proposals.  All field proposals
 * are dynamically typed static fields with an initializer of a closure.
 * Contributors should extend the
 *
 *
 * @author Andrew Eisenberg
 * @created Nov 10, 2009
 */
public class NewFieldCompletionProcessor extends AbstractGroovyCompletionProcessor {
    static class NewGroovyFieldCompletionProposal extends JavaCompletionProposal {

        NewGroovyFieldCompletionProposal(String fieldName,
                int replacementOffset, int replacementLength, int relevance) {
            super(createReplacementString(fieldName), replacementOffset, replacementLength, createImage(),
                    createDisplayString(fieldName), relevance);
        }

        // can we do better with the initializer?
        static String createReplacementString(String fieldName) {
            return "static " + fieldName + " = null";
        }

        static Image createImage() {
            CompletionProposal dummy = CompletionProposal.create(CompletionProposal.FIELD_REF, -1);
            dummy.setFlags(Flags.AccStatic);
            return ProposalUtils.getImage(dummy);
        }
        static StyledString createDisplayString(String fieldName) {
            StyledString ss = new StyledString();
            ss.append(fieldName);
            ss.append(" - New static field", StyledString.QUALIFIER_STYLER);
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
        return new NewGroovyFieldCompletionProposal(fieldName, context.completionLocation, context.completionExpression.length(), relevance);
    }


}
