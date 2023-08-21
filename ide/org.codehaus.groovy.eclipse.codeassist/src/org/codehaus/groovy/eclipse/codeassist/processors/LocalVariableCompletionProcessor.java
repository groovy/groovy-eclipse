/*
 * Copyright 2009-2023 the original author or authors.
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

import static org.eclipse.jdt.groovy.search.VariableScope.CLASS_CLASS_NODE;
import static org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS_NODE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class LocalVariableCompletionProcessor extends AbstractGroovyCompletionProcessor {

    private final int offset;
    private final int replaceLength;

    public LocalVariableCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);

        this.offset = context.completionLocation;
        this.replaceLength = context.completionExpression.length();
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        if (replaceLength < 1 && getContext().location == ContentAssistLocation.METHOD_CONTEXT) {
            return Collections.emptyList();
        }
        Map<String, ClassNode> localNames = findLocalNames(extractVariableNameStart());
        List<ICompletionProposal> proposals = createProposals(localNames);
        // now add closure proposals if necessary
        proposals.addAll(createClosureProposals());
        return proposals;
    }

    private List<ICompletionProposal> createClosureProposals() {
        ContentAssistContext context = getContext();
        if (context.currentScope != null && context.currentScope.getEnclosingClosure() != null) {
            ClassNode thisType = context.currentScope.getThis();
            ClassNode ownerType = context.currentScope.getOwner();
            ClassNode delegateType = context.currentScope.getDelegate();

            List<ICompletionProposal> proposals = new ArrayList<>();
            maybeAddClosureProperty(proposals, "owner", CLOSURE_CLASS_NODE, ownerType, false);
            maybeAddClosureProperty(proposals, "getOwner", CLOSURE_CLASS_NODE, ownerType, true);
            maybeAddClosureProperty(proposals, "thisObject", CLOSURE_CLASS_NODE, thisType, false);
            maybeAddClosureProperty(proposals, "getThisObject", CLOSURE_CLASS_NODE, thisType, true);
            maybeAddClosureProperty(proposals, "delegate", CLOSURE_CLASS_NODE, delegateType, false);
            maybeAddClosureProperty(proposals, "getDelegate", CLOSURE_CLASS_NODE, delegateType, true);
            maybeAddClosureProperty(proposals, "directive", CLOSURE_CLASS_NODE, ClassHelper.int_TYPE, false);
            maybeAddClosureProperty(proposals, "getDirective", CLOSURE_CLASS_NODE, ClassHelper.int_TYPE, true);
            maybeAddClosureProperty(proposals, "resolveStrategy", CLOSURE_CLASS_NODE, ClassHelper.int_TYPE, false);
            maybeAddClosureProperty(proposals, "getResolveStrategy", CLOSURE_CLASS_NODE, ClassHelper.int_TYPE, true);
            maybeAddClosureProperty(proposals, "parameterTypes", CLOSURE_CLASS_NODE, CLASS_CLASS_NODE.makeArray(), false);
            maybeAddClosureProperty(proposals, "getParameterTypes", CLOSURE_CLASS_NODE, CLASS_CLASS_NODE.makeArray(), true);
            maybeAddClosureProperty(proposals, "maximumNumberOfParameters", CLOSURE_CLASS_NODE, ClassHelper.int_TYPE, false);
            maybeAddClosureProperty(proposals, "getMaximumNumberOfParameters", CLOSURE_CLASS_NODE, ClassHelper.int_TYPE, true);
            return proposals;
        } else {
            return Collections.emptyList();
        }
    }

    private void maybeAddClosureProperty(List<ICompletionProposal> proposals, String name, ClassNode declaringType, ClassNode type, boolean isMethod) {
        if (ProposalUtils.looselyMatches(getContext().completionExpression, name)) {
            IGroovyProposal proposal;
            if (isMethod) {
                proposal = createMethodProposal(name, declaringType, type);
            } else {
                proposal = createFieldProposal(name, declaringType, type);
            }
            proposals.add(proposal.createJavaProposal(getContext(), getJavaContext()));
        }
    }

    private GroovyFieldProposal createFieldProposal(String name, ClassNode declaring, ClassNode type) {
        FieldNode field = new FieldNode(name, Flags.AccPublic, type, declaring, null);
        field.setDeclaringClass(declaring);
        return new GroovyFieldProposal(field);
    }

    private GroovyMethodProposal createMethodProposal(String name, ClassNode declaring, ClassNode returnType) {
        MethodNode method = new MethodNode(name, Flags.AccPublic, returnType, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
        method.setDeclaringClass(declaring);
        return new GroovyMethodProposal(method);
    }

    // removes any leading whitespace or non-java identifier chars
    private String extractVariableNameStart() {
        String fullExpression = getContext().completionExpression;
        if (fullExpression.length() == 0) {
            return "";
        }
        int end = fullExpression.length() - 1;
        while (end >= 0 && Character.isJavaIdentifierPart(fullExpression.charAt(end))) {
            end -= 1;
        }
        if (end >= 0) {
            end += 1;
            return fullExpression.substring(end);
        } else {
            return fullExpression;
        }
    }

    private Map<String, ClassNode> findLocalNames(String prefix) {
        BlockStatement block = getContainingBlock(getContext().containingCodeBlock);
        if (block == null) {
            return Collections.emptyMap();
        }

        Map<String, ClassNode> nameTypeMap = new HashMap<>();

        VariableScope scope = block.getVariableScope();
        while (scope != null) {
            for (Iterator<Variable> it = scope.getDeclaredVariablesIterator(); it.hasNext();) {
                Variable var = it.next();
                int end = Integer.MIN_VALUE;
                if (var instanceof Parameter) {
                    end = ((Parameter) var).getEnd();
                } else if (var instanceof VariableExpression) {
                    end = ((VariableExpression) var).getEnd();
                }

                if (end < 1 && var.getName().charAt(0) == '$') {
                    continue;
                }
                if (end < offset && ProposalUtils.looselyMatches(prefix, var.getName())) {
                    nameTypeMap.put(var.getName(), var.getOriginType() != null ? var.getOriginType() : var.getType());
                }
            }
            scope = scope.getParent();
        }

        // if completion location is within declaration expression, exclude declared variable
        block.visit(new CodeVisitorSupport() {
            @Override
            public void visitDeclarationExpression(DeclarationExpression expression) {
                if (expression.getStart() <= offset && offset <= expression.getEnd()) {
                    nameTypeMap.remove(expression.getVariableExpression().getName());
                }
                super.visitDeclarationExpression(expression);
            }
        });

        return nameTypeMap;
    }

    private BlockStatement getContainingBlock(ASTNode node) {
        if (node instanceof BlockStatement) {
            return (BlockStatement) node;
        }
        if (node instanceof ReturnStatement) {
            // empty or simple methods may collapse to return statement
            if (getContext().containingDeclaration instanceof MethodNode) {
                return new BlockStatement(
                    Collections.singletonList((ReturnStatement) node),
                    ((MethodNode) getContext().containingDeclaration).getVariableScope());
            }
        }
        if (node instanceof ClassNode && GroovyUtils.isScript((ClassNode) node)) {
            for (MethodNode method : ((ClassNode) node).redirect().getMethods()) {
                if (method.isScriptBody()) return (BlockStatement) method.getCode();
            }
        }
        return null;
    }

    private List<ICompletionProposal> createProposals(Map<String, ClassNode> nameTypes) {
        List<ICompletionProposal> proposals = new ArrayList<>();
        for (Entry<String, ClassNode> nameType : nameTypes.entrySet()) {
            proposals.add(createProposal(nameType.getKey(), nameType.getValue()));
        }
        return proposals;
    }

    private ICompletionProposal createProposal(String replaceName, ClassNode type) {
        CompletionProposal proposal = CompletionProposal.create(CompletionProposal.LOCAL_VARIABLE_REF, offset);
        proposal.setCompletion(replaceName.toCharArray());
        proposal.setReplaceRange(offset - replaceLength, getContext().completionEnd);
        proposal.setSignature(ProposalUtils.createTypeSignature(type));
        proposal.setRelevance(Relevance.HIGH.getRelevance());

        LazyJavaCompletionProposal javaProposal = new LazyJavaCompletionProposal(proposal, getJavaContext());
        javaProposal.setTriggerCharacters(ProposalUtils.VAR_TRIGGER);
        javaProposal.setRelevance(proposal.getRelevance());
        return javaProposal;
    }
}
