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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.transform.trait.Traits.TraitBridge;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.GenericsMapper;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
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
    public List<ICompletionProposal> generateProposals(IProgressMonitor progressMonitor) {
        List<ICompletionProposal> proposals = new ArrayList<>();

        ContentAssistContext context = getContext();
        if (context.getEnclosingType() != null) {
            for (MethodNode method : collectUnimplementedMethods(context.completionExpression, context.getEnclosingGroovyType())) {
                proposals.add(createProposal(method, progressMonitor));
            }
        }
        // add proposals from relevant proposal providers
        try {
            for (IProposalProvider provider : ProposalProviderRegistry.getRegistry().getProvidersFor(context.unit)) {
                List<MethodNode> methods = provider.getNewMethodProposals(context);
                if (methods != null) {
                    for (MethodNode method : methods) {
                        proposals.add(createProposal(method, progressMonitor));
                    }
                }
            }
        } catch (CoreException e) {
            GroovyContentAssist.logError("Exception looking for proposal providers in " + context.unit.getElementName(), e);
        }

        return proposals;
    }

    private ICompletionProposal createProposal(MethodNode method, IProgressMonitor progressMonitor) {
        ContentAssistContext context = getContext();
        IJavaProject project = getJavaContext().getProject();
        ClassNode declaringClass = method.getDeclaringClass();

        GroovyCompletionProposal proposal = createProposal(CompletionProposal.METHOD_DECLARATION, context.completionLocation);
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(declaringClass));
        proposal.setSignature(ProposalUtils.createMethodSignature(method));
        proposal.setName(method.getName().toCharArray());
        proposal.setFlags(method.getModifiers()); // TODO: Mix in Flags.AccVarargs?

        char[][] parameterNames = ProposalUtils.getParameterNames(method.getParameters());
        if (parameterNames.length > 0 && CharOperation.equals(parameterNames[0], ProposalUtils.ARG0)) {
            proposal.setCompletionEngine(new CompletionEngine(getNameEnvironment(), new CompletionRequestor() { @Override public void accept(CompletionProposal proposal) {} }, null, project, null, null));
            proposal.setDeclarationPackageName(Optional.ofNullable(declaringClass.getPackageName()).map(String::toCharArray).orElse(CharOperation.NO_CHAR));
            proposal.setDeclarationTypeName(ProposalUtils.createSimpleTypeName(declaringClass));
            for (int i = 0; i < parameterNames.length; i += 1) {
                parameterNames[i] = ProposalUtils.ARG_;
            }
            proposal.findParameterNames(progressMonitor);
        } else {
            proposal.setParameterNames(parameterNames);
        }

        if (declaringClass.isUsingGenerics() /*|| method.isUsingGenerics()*/) {
            GenericsMapper mapper = mappers.computeIfAbsent(declaringClass, x -> {
                ClassNode enclosingClass = findResolvedType(context.getEnclosingGroovyType(), declaringClass);
                return GenericsMapper.gatherGenerics(enclosingClass, declaringClass);
            });
            method = VariableScope.resolveTypeParameterization(mapper, method);
            proposal.setSignature(ProposalUtils.createMethodSignature(method));
        }
        proposal.setCompletion(createMethodCompletion(method, parameterNames));

        int length = context.completionExpression.length(), offset = context.completionLocation - length;
        String[] parameterTypeNames = CharOperation.toStrings(ProposalUtils.getParameterTypeNames(method.getParameters()));

        OverrideCompletionProposal override = new OverrideCompletionProposal(project, context.unit, method.getName(),
            parameterTypeNames, offset, length, ProposalUtils.createDisplayString(proposal), String.valueOf(proposal.getCompletion()));
        override.setImage(ProposalUtils.getImage(proposal));
        // TODO: override.setCursorPosition(offset within body of new method);
        override.setRelevance(Relevance.VERY_HIGH.getRelevance(VariableScope.OBJECT_CLASS_NODE.equals(declaringClass) ? 0.99f : 1.00f));
        return override;
    }

    private final Map<ClassNode, GenericsMapper> mappers = new HashMap<>();

    //--------------------------------------------------------------------------

    private static List<MethodNode> collectUnimplementedMethods(String completionExpression, ClassNode declaringType) {
        Map<String, MethodNode> methods = new LinkedHashMap<>();
        Queue<ClassNode> types = new LinkedList<>();
        types.add(declaringType);
        do {
            ClassNode type = types.remove();
            Collections.addAll(types, type.getInterfaces());
            if (type.getSuperClass() != null) types.add(type.getSuperClass());

            for (MethodNode meth : type.getMethods()) {
                String name = meth.getName();
                if (name.startsWith(completionExpression) && name.indexOf('$') < 0 && name.indexOf('<') < 0 && !isTraitBridge(meth)) {
                    methods.putIfAbsent(meth.getTypeDescriptor(), meth);
                }
            }
        } while (!types.isEmpty());

        // remove implemented or re-declared methods
        for (MethodNode meth : declaringType.getMethods()) {
            if (!isTraitBridge(meth))
                methods.remove(meth.getTypeDescriptor());
        }

        // restrict to methods that can be overridden
        return methods.values().stream().filter(meth ->
            !meth.isFinal() && !meth.isPrivate() && !meth.isStatic()
        ).collect(Collectors.toList());
    }

    private static char[] createMethodCompletion(MethodNode method, char[][] parameterNames) {
        StringBuilder completion = new StringBuilder();

        //// Modifiers
        completion.append(Flags.toString(method.getModifiers() & ~(Flags.AccAbstract | Flags.AccNative | Flags.AccPublic)));

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
            completion.append(" throws ");
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

    private static ClassNode findResolvedType(ClassNode target, ClassNode toResolve) {
        if (target != null) {
            if (target.equals(toResolve)) {
                return target;
            }
            ClassNode result = findResolvedType(target.getUnresolvedSuperClass(false), toResolve);
            if (result != null) {
                return result;
            }
            ClassNode[] interfaces = target.getUnresolvedInterfaces(false);
            if (interfaces != null) {
                for (ClassNode cn : interfaces) {
                    result = findResolvedType(cn, toResolve);
                    if (result != null) {
                        return result;
                    }
                }
            }
            if (target.isRedirectNode()) {
                return findResolvedType(target.redirect(), toResolve);
            }
        }
        return null;
    }

    private static boolean isTraitBridge(MethodNode method) {
        return GroovyUtils.getAnnotations(method, TraitBridge.class.getName()).anyMatch(x -> true);
    }
}
