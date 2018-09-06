/*
 * Copyright 2009-2018 the original author or authors.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.search.GenericsMapper;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Completion processor that determines methods to be overridden or implemented.
 */
public class NewMethodCompletionProcessor extends AbstractGroovyCompletionProcessor {

    public NewMethodCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        List<ICompletionProposal> proposals = new ArrayList<>();

        ContentAssistContext context = getContext();
        IType enclosingType = context.getEnclosingType();
        if (enclosingType != null) {
            for (MethodNode method : getAllUnimplementedMethods(context.completionExpression, context.getEnclosingGroovyType())) {
                proposals.add(createProposal(method, enclosingType));
            }
        }
        // add proposals from relevant proposal providers
        try {
            for (IProposalProvider provider : ProposalProviderRegistry.getRegistry().getProvidersFor(context.unit)) {
                List<MethodNode> methods = provider.getNewMethodProposals(context);
                if (methods != null) {
                    for (MethodNode method : methods) {
                        proposals.add(createProposal(method, enclosingType));
                    }
                }
            }
        } catch (CoreException e) {
            GroovyContentAssist.logError("Exception looking for proposal providers in " + context.unit.getElementName(), e);
        }

        return proposals;
    }

    private ICompletionProposal createProposal(MethodNode method, IType enclosingType) {
        ContentAssistContext context = getContext();
        int length = context.completionExpression.length();
        int offset = context.completionLocation - length;
        IJavaProject project = getJavaContext().getProject();
        char[][] parameterNames = ProposalUtils.getParameterNames(method.getParameters());
        String[] parameterTypeNames = getParameterTypeNames(method); // resolves generic types

        GroovyCompletionProposal proposal = createProposal(CompletionProposal.METHOD_DECLARATION, context.completionLocation);
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(method.getDeclaringClass()));
        proposal.setSignature(ProposalUtils.createMethodSignature(method));
        proposal.setName(method.getName().toCharArray());
        proposal.setFlags(method.getModifiers()); // TODO: Mix in Flags.AccVarargs?

        if (parameterNames.length > 0 && CharOperation.equals(parameterNames[0], ProposalUtils.ARG0)) {
            for (int i = 0; i < parameterNames.length; i += 1) { parameterNames[i] = ProposalUtils.ARG_; }
            proposal.setDeclarationTypeName(ProposalUtils.createSimpleTypeName(method.getDeclaringClass()));
            proposal.setDeclarationPackageName(Optional.ofNullable(method.getDeclaringClass().getPackageName()).map(String::toCharArray).orElse(CharOperation.NO_CHAR));
            proposal.setCompletionEngine(new CompletionEngine(getNameEnvironment(), new CompletionRequestor() { @Override public void accept(CompletionProposal proposal) {} }, null, project, null, null));
        } else {
            proposal.setParameterNames(parameterNames);
        }

        proposal.setCompletion(createMethodCompletion(method, parameterNames));

        OverrideCompletionProposal override = new OverrideCompletionProposal(project, context.unit, method.getName(),
            parameterTypeNames, offset, length, ProposalUtils.createDisplayString(proposal), String.valueOf(proposal.getCompletion()));
        override.setImage(ProposalUtils.getImage(proposal));
        // TODO: override.setCursorPosition(offset within body of new method);
        override.setRelevance(Relevance.VERY_HIGH.getRelevance(VariableScope.OBJECT_CLASS_NODE.equals(method.getDeclaringClass()) ? 0.9f : 1.0f));
        return override;
    }

    private ClassNode findResolvedType(ClassNode target, ClassNode toResolve) {
        if (target != null) {
            if (target.equals(toResolve)) {
                return target;
            }
            ClassNode result = findResolvedType(target.getUnresolvedSuperClass(false), toResolve);
            if (result != null) {
                return result;
            }
            for (ClassNode inter : target.getUnresolvedInterfaces(false)) {
                result = findResolvedType(inter, toResolve);
                if (result != null) {
                    return result;
                }
            }

            ClassNode redirect = target.redirect();
            if (redirect != target) {
                return findResolvedType(redirect, toResolve);
            }
        }
        return null;
    }

    private String[] getParameterTypeNames(MethodNode method) {
        GenericsMapper mapper = null;
        ClassNode declaringClass = method.getDeclaringClass();
        if (declaringClass.getGenericsTypes() != null && declaringClass.getGenericsTypes().length > 0) {
            mapper = mappers.computeIfAbsent(declaringClass, x -> {
                ClassNode enclosingClass = findResolvedType(getContext().getEnclosingGroovyType(), declaringClass);
                return GenericsMapper.gatherGenerics(enclosingClass, declaringClass);
            });
        }
        Parameter[] parameters = method.getParameters();
        String[] paramTypeNames = new String[parameters.length];
        for (int i = 0; i < paramTypeNames.length; i += 1) {
            ClassNode paramType = parameters[i].getType();
            if (mapper != null && paramType.getGenericsTypes() != null && paramType.getGenericsTypes().length > 0) {
                paramType = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(paramType));
            }
            paramTypeNames[i] = Signature.toString(String.valueOf(ProposalUtils.createTypeSignature(paramType)));
        }
        return paramTypeNames;
    }

    private Map<ClassNode, GenericsMapper> mappers = new HashMap<>();

    //--------------------------------------------------------------------------

    private static List<MethodNode> getAllUnimplementedMethods(String completionExpression, ClassNode declaringType) {
        List<MethodNode> thisClassMethods = declaringType.getMethods();
        List<MethodNode> allMethods = declaringType.getAllDeclaredMethods();
        List<MethodNode> unimplementedMethods = new ArrayList<>(allMethods.size() - thisClassMethods.size());

        // uggh n^2 loop.  Can be made more efficient by doing declaring.getMethods(allMethodNode.getName())
        for (MethodNode allMethodNode : allMethods) {
            String name = allMethodNode.getName();
            if (name.startsWith(completionExpression)) {
                boolean canBeOverridden = (!name.contains("$") && !name.contains("<") &&
                    !allMethodNode.isPrivate() && !allMethodNode.isStatic() && !allMethodNode.isFinal());
                if (canBeOverridden) {
                    boolean found = false;
                    inner:
                    for (MethodNode thisClassMethod : thisClassMethods) {
                        if (allMethodNode.getParameters().length == thisClassMethod.getParameters().length &&
                            allMethodNode.getName().equals(thisClassMethod.getName())) {
                            // now check param types
                            Parameter[] allMethodParams = allMethodNode.getParameters();
                            Parameter[] thisClassParams = thisClassMethod.getParameters();
                            for (int i = 0; i < thisClassParams.length; i += 1) {
                                if (!allMethodParams[i].getType().getName().equals(thisClassParams[i].getType().getName())) {
                                    continue inner;
                                }
                            }
                            found = true;
                            break inner;
                        }
                    }
                    if (!found) {
                        unimplementedMethods.add(allMethodNode);
                    }
                    found = false;
                }
            }
        }
        return unimplementedMethods;
    }

    private static char[] createMethodCompletion(MethodNode method, char[][] parameterNames) {
        StringBuffer completion = new StringBuffer();

        //// Modifiers
        // flush uninteresting modifiers
        ASTNode.printModifiers(method.getModifiers() & ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC), completion);

        //// Return type
        completion.append(ProposalUtils.createSimpleTypeName(method.getReturnType()));
        completion.append(' ');

        //// Selector
        completion.append(method.getName());
        completion.append('(');

        ////Parameters
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i += 1) {
            if (i != 0) {
                completion.append(',');
                completion.append(' ');
            }
            completion.append(ProposalUtils.createSimpleTypeName(parameters[i].getType()));
            completion.append(' ').append(parameterNames[i]);
        }
        completion.append(')');

        //// Exceptions
        ClassNode[] exceptions = method.getExceptions();
        if (exceptions != null && exceptions.length > 0) {
            completion.append(' ');
            completion.append("throws");
            completion.append(' ');
            for (int i = 0; i < exceptions.length; i += 1) {
                if (i != 0) {
                    completion.append(' ');
                    completion.append(',');
                }
                completion.append(ProposalUtils.createSimpleTypeName(exceptions[i]));
            }
        }

        char[] chars = new char[completion.length()];
        completion.getChars(0, chars.length, chars, 0);
        return chars;
    }
}
