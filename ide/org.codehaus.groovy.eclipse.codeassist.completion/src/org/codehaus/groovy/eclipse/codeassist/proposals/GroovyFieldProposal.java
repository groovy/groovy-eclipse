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

package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class GroovyFieldProposal extends AbstractGroovyProposal {

    
    private final FieldNode field;
    private final int relevance;
    private final String contributor;
    public GroovyFieldProposal(FieldNode field) {
        this.field = field;
        this.relevance = -1;
        this.contributor = "Groovy";
    }
    public GroovyFieldProposal(FieldNode field, String contributor) {
        this.field = field;
        this.relevance = -1;
        this.contributor = contributor;
    }
    public GroovyFieldProposal(FieldNode field, int relevance) {
        this.field = field;
        this.relevance = relevance;
        this.contributor = "Groovy";
    }
    public GroovyFieldProposal(FieldNode field, int relevance, String contributor) {
        this.field = field;
        this.relevance = relevance;
        this.contributor = contributor;
    }

    public IJavaCompletionProposal createJavaProposal(
            ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext) {
        
        return new GroovyJavaFieldCompletionProposal(createProposal(context), getImageFor(field), createDisplayString(field));
    }

    @Override
    protected int getRelevance(char[] name) {
        if (relevance >= 0) return relevance;
        
        int rel = super.getRelevance(name);
        if (field.isStatic()) {
            rel *=5;
        }
        return rel;
    }
    
    protected StyledString createDisplayString(FieldNode field) {
        StyledString ss = new StyledString();
        
        ss.append(field.getName())
          .append(" : ")
          .append(ProposalUtils.createSimpleTypeName(field.getType()))
          .append(" - ")
          .append(ProposalUtils.createSimpleTypeName(field.getDeclaringClass()), StyledString.QUALIFIER_STYLER)
          .append(" (" + contributor + ")", StyledString.DECORATIONS_STYLER);
        return ss;
    }

    private CompletionProposal createProposal(ContentAssistContext context) {
        InternalCompletionProposal proposal = (InternalCompletionProposal) CompletionProposal.create(CompletionProposal.FIELD_REF, context.completionLocation);
        proposal.setFlags(field.getModifiers());
        proposal.setName(field.getName().toCharArray());
        proposal.setCompletion(proposal.getName());
        proposal.setSignature(ProposalUtils.createTypeSignature(field.getType()));
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(field.getDeclaringClass()));
        proposal.setRelevance(getRelevance(proposal.getName()));
        int startIndex = context.completionLocation-context.completionExpression.length();
        proposal.setReplaceRange(startIndex, context.completionLocation);
        return proposal;
    }
}
