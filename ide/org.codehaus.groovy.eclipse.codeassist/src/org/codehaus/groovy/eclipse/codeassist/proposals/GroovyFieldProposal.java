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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaFieldCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MemberProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
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

    @Override
    public IJavaCompletionProposal createJavaProposal(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
            return null;
        }

        GroovyCompletionProposal proposal = new GroovyCompletionProposal(CompletionProposal.FIELD_REF, context.completionLocation);
        proposal.setName(field.getName().toCharArray());

        char[] completion = proposal.getName();
        if (context.location == ContentAssistLocation.STATEMENT &&
            field.getDeclaringClass().equals(VariableScope.CLASS_CLASS_NODE)) {
            // qualifier is required for references to members of java.lang.Class
            completion = CharOperation.concat("this.".toCharArray(), completion);
        } else if (getRequiredQualifier() != null) {
            completion = CharOperation.concat(getRequiredQualifier().toCharArray(), completion, '.');
        }

        proposal.setCompletion(completion);
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(field.getDeclaringClass()));
        proposal.setFlags(field.getModifiers() | (GroovyUtils.isDeprecated(field) ? Flags.AccDeprecated : 0));
        proposal.setRelevance(computeRelevance(context));
        proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), context.completionEnd);
        proposal.setSignature(ProposalUtils.createTypeSignature(field.getType()));

        if (getRequiredStaticImport() != null) {
            CompletionProposal importProposal;
            if (new AssistOptions(javaContext.getProject().getOptions(true)).suggestStaticImport) {
                importProposal = CompletionProposal.create(CompletionProposal.FIELD_IMPORT, context.completionLocation);
                importProposal.setAdditionalFlags(CompletionFlags.StaticImport);
                importProposal.setDeclarationSignature(proposal.getDeclarationSignature());
                importProposal.setName(proposal.getName());
                /*
                importProposal.setCompletion(("import static " + getRequiredStaticImport() + "\n").toCharArray());
                importProposal.setDeclarationPackageName(field.getDeclaringClass().getPackageName().toCharArray());
                importProposal.setDeclarationTypeName(field.getDeclaringClass().getName().toCharArray());
                importProposal.setFlags(proposal.getFlags());
                importProposal.setPackageName(field.getType().getPackageName().toCharArray());
                importProposal.setRelevance(proposal.getRelevance());
                importProposal.setReplaceRange(importStart - this.offset, importEnd - this.offset);
                importProposal.setSignature(proposal.getSignature());
                importProposal.setTokenRange(importStart - this.offset, importEnd - this.offset);
                importProposal.setTypeName(field.getType().getName().toCharArray());
                */
            } else {
                // a method reference adds type qualifier with base completion characters (ImportCompletionProposal#computeReplacementString)
                ReflectionUtils.setPrivateField(InternalCompletionProposal.class, "completionKind", proposal, CompletionProposal.METHOD_REF);
                importProposal = CompletionProposal.create(CompletionProposal.TYPE_IMPORT, context.completionLocation);
                importProposal.setSignature(proposal.getDeclarationSignature());
            }
            proposal.setRequiredProposals(new CompletionProposal[] {importProposal});
        }

        var javaProposal = new GroovyJavaFieldCompletionProposal(proposal, createDisplayString(field), javaContext);
        var groovyMethod = field.<MethodNode>getNodeMetaData("groovy.method");
        if (groovyMethod != null) {
            try {
                var type = javaContext.getProject().findType(groovyMethod.getDeclaringClass().getName());
                if (type != null) {
                    var method = type.getMethod(groovyMethod.getName(), GroovyUtils.getParameterTypeSignatures(groovyMethod, true));
                    if (method != null && method.exists()) {
                        ProposalInfo proposalInfo = ReflectionUtils.executePrivateMethod(AbstractJavaCompletionProposal.class, "getProposalInfo", javaProposal);
                        ReflectionUtils.throwableSetPrivateField(MemberProposalInfo.class, "fJavaElementResolved", proposalInfo, Boolean.TRUE);
                        ReflectionUtils.throwableSetPrivateField(ProposalInfo.class, "fElement", proposalInfo, method);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return javaProposal;
    }

    protected StyledString createDisplayString(FieldNode field) {
        return new StyledString().append(field.getName())
            .append(" : ")
            .append(ProposalUtils.createSimpleTypeName(field.getType()))
            .append(" - ")
            .append(ProposalUtils.createSimpleTypeName(field.getDeclaringClass()), StyledString.QUALIFIER_STYLER);
    }
}
