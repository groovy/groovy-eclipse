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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import org.eclipse.jdt.internal.ui.text.java.GetterSetterCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class GetSetMethodCompletionProcessor extends AbstractGroovyCompletionProcessor {

    public GetSetMethodCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        List<ICompletionProposal> proposals = new LinkedList<>();
        ContentAssistContext context = getContext();
        IType enclosingType = context.getEnclosingType();
        if (enclosingType != null) {
            try {
                for (IField field : enclosingType.getFields()) {
                    proposals.addAll(createProposal(field, context));
                }
            } catch (JavaModelException e) {
                GroovyContentAssist.logError("Exception looking for proposal providers in " + context.unit.getElementName(), e);
            }
        }
        return proposals;
    }

    private List<ICompletionProposal> createProposal(IField field, ContentAssistContext context) throws JavaModelException {
        List<ICompletionProposal> proposals = new ArrayList<>(2);
        int relevance = Relevance.HIGH.getRelevance();
        IMethod getter = GetterSetterUtil.getGetter(field);
        if (getter == null || !getter.exists()) {
            proposals.add(new GetterSetterCompletionProposal(field, context.completionLocation -
                context.completionExpression.length(), context.completionExpression.length(), true, relevance));
        }
        IMethod setter = GetterSetterUtil.getSetter(field);
        if ((field.getFlags() & Flags.AccFinal) == 0 && (setter == null || !setter.exists())) {
            proposals.add(new GetterSetterCompletionProposal(field, context.completionLocation -
                context.completionExpression.length(), context.completionExpression.length(), false, relevance));
        }
        return proposals;
    }
}
