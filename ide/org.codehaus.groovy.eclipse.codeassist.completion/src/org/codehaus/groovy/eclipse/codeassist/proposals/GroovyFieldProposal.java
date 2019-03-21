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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaFieldCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

public class GroovyFieldProposal extends AbstractGroovyProposal {

    private final FieldNode field;

    public GroovyFieldProposal(FieldNode field) {
        this.field = field;
    }

    @Override
    public AnnotatedNode getAssociatedNode() {
        return field;
    }

    public FieldNode getField() {
        return field;
    }

    public IJavaCompletionProposal createJavaProposal(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
            return null;
        }

        GroovyCompletionProposal proposal = new GroovyCompletionProposal(CompletionProposal.FIELD_REF, context.completionLocation);
        proposal.setCompletion(proposal.getName());
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(field.getDeclaringClass()));
        proposal.setFlags(field.getModifiers());
        proposal.setName(field.getName().toCharArray());
        proposal.setRelevance(computeRelevance(context));
        proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), context.completionEnd);
        proposal.setSignature(ProposalUtils.createTypeSignature(field.getType()));

        if (getRequiredStaticImport() != null) {
            GroovyCompletionProposal fieldImportProposal = new GroovyCompletionProposal(CompletionProposal.FIELD_IMPORT, context.completionLocation);
            fieldImportProposal.setAdditionalFlags(CompletionFlags.StaticImport);
            fieldImportProposal.setCompletion(("import static " + getRequiredStaticImport() + "\n").toCharArray());
            fieldImportProposal.setDeclarationSignature(proposal.getDeclarationSignature());
            fieldImportProposal.setName(proposal.getName());

            /*
            fieldImportProposal.setDeclarationPackageName(field.getDeclaringClass().getPackageName().toCharArray());
            fieldImportProposal.setDeclarationTypeName(field.getDeclaringClass().getName().toCharArray());
            fieldImportProposal.setFlags(proposal.getFlags());
            fieldImportProposal.setPackageName(field.getType().getPackageName().toCharArray());
            fieldImportProposal.setRelevance(proposal.getRelevance());
            fieldImportProposal.setReplaceRange(importStart - this.offset, importEnd - this.offset);
            fieldImportProposal.setSignature(proposal.getSignature());
            fieldImportProposal.setTokenRange(importStart - this.offset, importEnd - this.offset);
            fieldImportProposal.setTypeName(field.getType().getName().toCharArray());
            */

            proposal.setRequiredProposals(new CompletionProposal[] {fieldImportProposal});
        }

        return new GroovyJavaFieldCompletionProposal(proposal, createDisplayString(field), javaContext);
    }

    private StyledString createDisplayString(FieldNode field) {
        return new StyledString().append(field.getName())
            .append(" : ")
            .append(ProposalUtils.createSimpleTypeName(field.getType()))
            .append(" - ")
            .append(ProposalUtils.createSimpleTypeName(field.getDeclaringClass()), StyledString.QUALIFIER_STYLER);
    }
}
