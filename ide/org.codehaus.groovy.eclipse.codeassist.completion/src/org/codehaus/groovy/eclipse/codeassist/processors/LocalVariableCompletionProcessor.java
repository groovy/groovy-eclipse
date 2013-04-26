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

import groovyjarjarasm.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Nov 10, 2009
 *
 */
public class LocalVariableCompletionProcessor extends AbstractGroovyCompletionProcessor {

    private final int offset;
    private final int replaceLength;
    private final JavaContentAssistInvocationContext javaContext;
    public LocalVariableCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
        this.javaContext = javaContext;
        this.replaceLength = context.completionExpression.length();
        this.offset = context.completionLocation;
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        Map<String,ClassNode> localNames = findLocalNames(extractVariableNameStart());
        List<ICompletionProposal> proposals = createProposals(localNames);
        // now add closure proposals if necessary
        proposals.addAll(createClosureProposals());
        return proposals;
    }

    private List<ICompletionProposal> createClosureProposals() {
        if (getContext().currentScope.getEnclosingClosure() != null) {
            List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(1);
            VariableInfo ownerInfo = getContext().currentScope.lookupName("owner");
            VariableInfo delegateInfo = getContext().currentScope.lookupName("delegate");
            maybeAddClosureProperty(proposals, "owner", ownerInfo.declaringType, ownerInfo.type, false);
            maybeAddClosureProperty(proposals, "getOwner", ownerInfo.declaringType, ownerInfo.type, true);
            maybeAddClosureProperty(proposals, "delegate", delegateInfo.declaringType, delegateInfo.type, false);
            maybeAddClosureProperty(proposals, "getDelegate", delegateInfo.declaringType, delegateInfo.type, true);
            maybeAddClosureProperty(proposals, "thisObject", org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.OBJECT_CLASS_NODE, false);
            maybeAddClosureProperty(proposals, "getThisObject", org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.OBJECT_CLASS_NODE, true);
            maybeAddClosureProperty(proposals, "resolveStrategy", org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.INTEGER_CLASS_NODE, false);
            maybeAddClosureProperty(proposals, "getResolveStrategy", org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.OBJECT_CLASS_NODE, true);
            maybeAddClosureProperty(proposals, "directive", org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.INTEGER_CLASS_NODE, false);
            maybeAddClosureProperty(proposals, "getDirective", org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.OBJECT_CLASS_NODE, true);
            maybeAddClosureProperty(proposals, "maximumNumberOfParameters",
                    org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.INTEGER_CLASS_NODE, false);
            maybeAddClosureProperty(proposals, "getMaximumNumberOfParameters",
                    org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.OBJECT_CLASS_NODE, true);
            maybeAddClosureProperty(proposals, "parameterTypes", org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.CLASS_ARRAY_CLASS_NODE, false);
            maybeAddClosureProperty(proposals, "getParameterTypes", org.eclipse.jdt.groovy.search.VariableScope.CLOSURE_CLASS,
                    org.eclipse.jdt.groovy.search.VariableScope.CLASS_ARRAY_CLASS_NODE, true);
            return proposals;
        } else {
            return Collections.emptyList();
        }
    }

    private void maybeAddClosureProperty(List<ICompletionProposal> proposals, String name, ClassNode type, ClassNode declaringType,
            boolean isMethod) {
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
        FieldNode field = new FieldNode(name, Opcodes.ACC_PUBLIC, type, declaring, null);
        field.setDeclaringClass(declaring);
        return new GroovyFieldProposal(field);
    }

    private GroovyMethodProposal createMethodProposal(String name, ClassNode declaring, ClassNode returnType) {
        MethodNode method = new MethodNode(name, Opcodes.ACC_PUBLIC, returnType, new Parameter[0], new ClassNode[0], null);
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
            end--;
        }
        if (end >= 0) {
            return fullExpression.substring(++end);
        } else {
            return fullExpression;
        }
    }

    private Map<String,ClassNode> findLocalNames(String prefix) {
         Map<String,ClassNode> nameTypeMap = new HashMap<String,ClassNode>();

         VariableScope scope = getVariableScope(getContext().containingCodeBlock);
         while (scope != null) {
             for (Iterator<Variable> varIter = scope.getDeclaredVariablesIterator(); varIter.hasNext();) {
                 Variable var = (Variable) varIter.next();
                 boolean inBounds;
                 if (var instanceof Parameter) {
                     inBounds = ((Parameter) var).getEnd() < offset;
                 } else if (var instanceof VariableExpression) {
                     inBounds = ((VariableExpression) var).getEnd() < offset;
                 } else {
                     inBounds = true;
                 }

                if (inBounds && ProposalUtils.looselyMatches(prefix, var.getName())) {
                    nameTypeMap.put(var.getName(), var.getOriginType() != null ? var.getOriginType() : var.getType());
                }
             }
             scope = scope.getParent();
         }

         return nameTypeMap;
    }



    private VariableScope getVariableScope(ASTNode astNode) {
        if (astNode instanceof BlockStatement) {
            return ((BlockStatement) astNode).getVariableScope();
        } else if (astNode instanceof ClassNode && ((ClassNode) astNode).isScript()) {
            // use scope of the run method
            ClassNode clazz = (ClassNode) astNode;
            MethodNode method = clazz.getMethod("run", new Parameter[0]);
            if (method != null && (BlockStatement) method.getCode() instanceof BlockStatement) {
                return ((BlockStatement) method.getCode()).getVariableScope();
            }
        }
        return null;
    }

    private List<ICompletionProposal> createProposals(Map<String,ClassNode> nameTypes) {
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
        for (Entry<String,ClassNode> nameType : nameTypes.entrySet()) {
            proposals.add(createProposal(nameType.getKey(), nameType.getValue()));
        }
        return proposals;
    }

    /**
     * @param offset
     * @param replaceLength
     * @param context
     * @param proposals
     * @param nameType
     */
    private ICompletionProposal createProposal(String replaceName, ClassNode type) {
        CompletionProposal proposal = CompletionProposal.create(CompletionProposal.LOCAL_VARIABLE_REF, offset);
        proposal.setCompletion(replaceName.toCharArray());
        proposal.setReplaceRange(offset - replaceLength,
                getContext().completionEnd);
        proposal.setSignature(ProposalUtils.createTypeSignature(type));

        proposal.setRelevance(Relevance.HIGH.getRelavance());
        LazyJavaCompletionProposal completion = new LazyJavaCompletionProposal(proposal, javaContext);
        completion.setRelevance(proposal.getRelevance());
        return completion;
    }

}
