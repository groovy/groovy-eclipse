/*
 * Copyright 2009-2024 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
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
    public List<ICompletionProposal> generateProposals(final IProgressMonitor monitor) {
        List<ICompletionProposal> proposals = new ArrayList<>();

        ContentAssistContext context = getContext();
        IType enclosingType = context.getEnclosingType();
        if (enclosingType != null) {
            AssistOptions options = new AssistOptions(getJavaContext().getProject().getOptions(true));
            int length = context.completionExpression.length(), offset = context.completionLocation - length;
            try {
                for (IField field : enclosingType.getFields()) {
                    if (field.getSourceRange().getLength() > 0 && !field.isEnumConstant() && !isRecordComponent(field) && ProposalUtils.matches(
                            context.completionExpression, org.apache.groovy.util.BeanUtils.capitalize(field.getElementName()), options.camelCaseMatch, options.subwordMatch)) {
                        IMethod getter = GetterSetterUtil.getGetter(field);
                        if (getter == null || !getter.exists()) {
                            proposals.add(new GetterSetterCompletionProposal(field, offset, length, true, Relevance.HIGH.getRelevance()));
                        }

                        if (!Flags.isFinal(field.getFlags())) {
                            IMethod setter = GetterSetterUtil.getSetter(field);
                            if (setter == null || !setter.exists()) {
                                proposals.add(new GetterSetterCompletionProposal(field, offset, length, false, Relevance.HIGH.getRelevance()));
                            }
                        }
                    }
                }
            } catch (JavaModelException e) {
                GroovyContentAssist.logError("Exception looking for proposal providers in " + context.unit.getElementName(), e);
            }
        }

        return proposals;
    }

    // 3.26+ can use field.isRecordComponent()
    private static boolean isRecordComponent(final IField field) throws JavaModelException {
        return !Flags.isStatic(field.getFlags()) && (field.getDeclaringType().getFlags() & 0x1000000) != 0;
    }
}
