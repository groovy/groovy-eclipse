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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import static org.codehaus.groovy.eclipse.codeassist.processors.StatementAndExpressionCompletionProcessor.METHOD_POINTER_COMPLETION;

import java.util.Optional;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaMethodCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.codehaus.jdt.groovy.ast.MethodNodeWithNamedParams;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.ui.text.java.AnnotationAtttributeProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

public class GroovyMethodProposal extends AbstractGroovyProposal {

    private final MethodNode method;

    private final String contributor;

    private ProposalFormattingOptions options;

    public GroovyMethodProposal(MethodNode method) {
        this(method, null);
    }

    public GroovyMethodProposal(MethodNode method, String contributor) {
        super();
        this.method = method;
        this.contributor = contributor;
    }

    @Override
    public AnnotatedNode getAssociatedNode() {
        return method;
    }

    public MethodNode getMethod() {
        return method;
    }

    public ProposalFormattingOptions getProposalFormattingOptions() {
        if (options == null) {
            options = ProposalFormattingOptions.newFromOptions();
        }
        return options;
    }

    public void setProposalFormattingOptions(ProposalFormattingOptions options) {
        this.options = options;
    }

    @Override @SuppressWarnings("incomplete-switch")
    public IJavaCompletionProposal createJavaProposal(CompletionEngine engine, ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        int completionOffset = context.completionLocation, kind = CompletionProposal.METHOD_REF;
        switch (context.location) {
        case ANNOTATION_BODY:
            kind = CompletionProposal.ANNOTATION_ATTRIBUTE_REF;
            break;
        case EXPRESSION:
            if (METHOD_POINTER_COMPLETION.matcher(context.fullCompletionExpression).matches()) {
                kind = CompletionProposal.METHOD_NAME_REFERENCE;
            }
            break;
        case IMPORT:
            kind = CompletionProposal.METHOD_NAME_REFERENCE;
            break;
        case METHOD_CONTEXT:
            // if location is METHOD_CONTEXT, then the type must be MethodInfoContentAssistContext
            // ...but there are other times when the type is MethodInfoContentAssistContext as well
            MethodInfoContentAssistContext methodContext = (MethodInfoContentAssistContext) context;
            // only show context information and only for methods that exactly match the name
            // this happens when we are at the start of an argument or an open paren
            if (!methodContext.methodName.equals(method.getName())) {
                return null;
            }
            completionOffset = methodContext.methodNameEnd;
        }

        GroovyCompletionProposal proposal = new GroovyCompletionProposal(kind, completionOffset);

        if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
            proposal.setCompletion(CharOperation.NO_CHAR);
            proposal.setReplaceRange(context.completionLocation, context.completionLocation);
        } else {
            boolean parens;
            switch (kind) {
            case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
            case CompletionProposal.METHOD_NAME_REFERENCE:
                parens = false;
                break;
            default:
                parens = !context.isParenAfter(javaContext.getDocument());
            }
            proposal.setCompletion(Optional.ofNullable(getRequiredQualifier()).map(q ->
                CharOperation.concat(q.toCharArray(), completionName(parens), '.')
            ).orElseGet(() -> completionName(parens)));
            proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), context.completionEnd);
        }
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(method.getDeclaringClass()));
        proposal.setName(completionName(false));
        if (method instanceof MethodNodeWithNamedParams) {
            fillInExtraParameters((MethodNodeWithNamedParams) method, proposal);
        } else {
            char[][] parameterNames = getParameterNames(method.getParameters());
            if (parameterNames.length < 1 || (!CharOperation.equals(parameterNames[0], ProposalUtils.ARG0) && !CharOperation.equals(parameterNames[0], ProposalUtils.ARG1))) {
                proposal.setParameterNames(parameterNames);
            } else {
                proposal.setCompletionEngine(engine);
                proposal.setNameLookup(engine.nameEnvironment.nameLookup);
                proposal.setDeclarationTypeName(ProposalUtils.createSimpleTypeName(method.getDeclaringClass()));
                proposal.setDeclarationPackageName(Optional.ofNullable(method.getDeclaringClass().getPackageName()).map(String::toCharArray).orElse(CharOperation.NO_CHAR));
            }
            proposal.setParameterTypeNames(getParameterTypeNames(method.getParameters()));
        }
        proposal.setFlags(getModifiers());
        proposal.setAdditionalFlags(CompletionFlags.Default);
        proposal.setOriginalSignature(ProposalUtils.createMethodSignature(method));
        proposal.setSignature(createMethodSignature());
        proposal.setKey(proposal.getSignature());
        proposal.setRelevance(computeRelevance(context));

        if (getRequiredStaticImport() != null) {
            CompletionProposal importProposal;
            if (new AssistOptions(javaContext.getProject().getOptions(true)).suggestStaticImport) {
                importProposal = CompletionProposal.create(CompletionProposal.METHOD_IMPORT, context.completionLocation);
                importProposal.setAdditionalFlags(CompletionFlags.StaticImport);
                importProposal.setDeclarationSignature(proposal.getDeclarationSignature());
                importProposal.setName(proposal.getName());

                /*
                importProposal.setCompletion(("import static " + getRequiredStaticImport() + "\n").toCharArray());
                importProposal.setDeclarationPackageName(method.declaringClass.qualifiedPackageName());
                importProposal.setDeclarationTypeName(method.declaringClass.qualifiedSourceName());
                importProposal.setFlags(method.modifiers);
                if (original != method) proposal.setOriginalSignature(getSignature(original));
                if (parameterNames != null) importProposal.setParameterNames(parameterNames);
                importProposal.setParameterPackageNames(parameterPackageNames);
                importProposal.setParameterTypeNames(parameterTypeNames);
                importProposal.setPackageName(method.returnType.qualifiedPackageName());
                importProposal.setReplaceRange(importStart - this.offset, importEnd - this.offset);
                importProposal.setRelevance(relevance);
                importProposal.setSignature(getSignature(method));
                importProposal.setTokenRange(importStart - this.offset, importEnd - this.offset);
                importProposal.setTypeName(method.returnType.qualifiedSourceName());
                */
            } else {
                importProposal = CompletionProposal.create(CompletionProposal.TYPE_IMPORT, context.completionLocation);
                importProposal.setSignature(proposal.getDeclarationSignature());
            }

            proposal.setRequiredProposals(new CompletionProposal[] {importProposal});
        }

        LazyJavaCompletionProposal lazyProposal = null;
        if (kind == CompletionProposal.ANNOTATION_ATTRIBUTE_REF) {
            proposal.setSignature(ProposalUtils.createTypeSignature(getMethod().getReturnType()));
            proposal.setFlags(getModifiers() & ~Flags.AccDefaultMethod); // clear the "default method" flag

            lazyProposal = new LazyJavaCompletionProposal(proposal, javaContext);
            lazyProposal.setProposalInfo(new AnnotationAtttributeProposalInfo(javaContext.getProject(), proposal));
        } else {
            lazyProposal = new GroovyJavaMethodCompletionProposal(proposal, getProposalFormattingOptions(), javaContext, contributor);
        }
        return lazyProposal;
    }

    private void fillInExtraParameters(MethodNodeWithNamedParams methodNode, GroovyCompletionProposal proposal) {
        proposal.setNamedParameterNames(getParameterNames(methodNode.getNamedParams()));
        proposal.setOptionalParameterNames(getParameterNames(methodNode.getOptionalParams()));
        proposal.setRegularParameterNames(getParameterNames(methodNode.getPositionalParams()));
        proposal.setParameterNames(getParameterNames(((MethodNode) methodNode).getParameters()));

        proposal.setNamedParameterTypeNames(getParameterTypeNames(methodNode.getNamedParams()));
        proposal.setOptionalParameterTypeNames(getParameterTypeNames(methodNode.getOptionalParams()));
        proposal.setRegularParameterTypeNames(getParameterTypeNames(methodNode.getPositionalParams()));
        proposal.setParameterTypeNames(getParameterTypeNames(((MethodNode) methodNode).getParameters()));
    }

    protected char[] completionName(boolean includeParens) {
        StringBuilder name = new StringBuilder(method.getName());
        if (name.chars().anyMatch(Character::isWhitespace)) {
            name.insert(0, '"');
            name.append('"');
        }
        if (includeParens) {
            name.append("()");
        }
        return CharArrayUtils.extractChars(name);
    }

    protected int getModifiers() {
        return (method.getModifiers() | (GroovyUtils.isDeprecated(method) ? Flags.AccDeprecated : 0));
    }

    protected char[] createMethodSignature() {
        return ProposalUtils.createMethodSignature(method);
    }

    protected char[][] getParameterNames(Parameter[] parameters) {
        return ProposalUtils.getParameterNames(parameters);
    }

    protected char[][] getParameterTypeNames(Parameter[] parameters) {
        return ProposalUtils.getParameterTypeNames(parameters);
    }
}
