/*
 * Copyright 2009-2019 the original author or authors.
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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.creators.CategoryProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.FieldProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.IProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.MethodProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MemberProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public abstract class AbstractGroovyCompletionProcessor implements IGroovyCompletionProcessor {

    private final ContentAssistContext context;
    private final SearchableEnvironment nameEnvironment;
    private final JavaContentAssistInvocationContext javaContext;

    public AbstractGroovyCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        this.context = context;
        this.javaContext = javaContext;
        this.nameEnvironment = nameEnvironment;
    }

    public final ContentAssistContext getContext() {
        return context;
    }

    public final SearchableEnvironment getNameEnvironment() {
        return nameEnvironment;
    }

    public final JavaContentAssistInvocationContext getJavaContext() {
        return javaContext;
    }

    protected IProposalCreator[] getProposalCreators() {
        return new IProposalCreator[] {
            new FieldProposalCreator(),
            new MethodProposalCreator(),
            new CategoryProposalCreator(),
        };
    }

    protected int getReplacementStartOffset() {
        int replacementStart;
        switch (context.location) {
        case ANNOTATION:
        case CONSTRUCTOR:
            replacementStart = context.completionNode.getStart();
            if (context.completionNode instanceof ClassNode && ((ClassNode) context.completionNode).getNameEnd() > 0) {
                replacementStart = ((ClassNode) context.completionNode).getNameStart();
            }
            break;
        case METHOD_CONTEXT:
            replacementStart = ((Expression) context.completionNode).getNameStart();
            break;
        default:
            replacementStart = (context.completionLocation - context.fullCompletionExpression.replaceFirst("^\\s+", "").length());
        }
        return replacementStart;
    }

    protected final GroovyCompletionProposal createProposal(int kind, int completionOffset) {
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(kind, completionOffset);
        proposal.setNameLookup(nameEnvironment.nameLookup);
        return proposal;
    }

    protected static CompletionProposal extractProposal(ICompletionProposal javaProposal) {
        if (javaProposal instanceof AbstractJavaCompletionProposal) {
            //ProposalInfo proposalInfo = ((AbstractJavaCompletionProposal) javaProposal).getProposalInfo();
            ProposalInfo proposalInfo = ReflectionUtils.executePrivateMethod(AbstractJavaCompletionProposal.class, "getProposalInfo", javaProposal);
            if (proposalInfo instanceof MemberProposalInfo) {
                //CompletionProposal completionProposal = ((MemberProposalInfo) proposalInfo).getProposal();
                CompletionProposal completionProposal = ReflectionUtils.getPrivateField(MemberProposalInfo.class, "fProposal", proposalInfo);
                return completionProposal;
            }
        }
        return null;
    }
}
