/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.creators;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.preferences.DGMProposalFilter;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MemberProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.MethodProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

public class CategoryProposalCreator extends AbstractProposalCreator {

    protected static final String GROOVY_METHOD_CONTRIBUTOR = "Groovy";

    @Override
    public List<IGroovyProposal> findAllProposals(final ClassNode selfType, final Set<ClassNode> categories, final String prefix, final boolean isStatic, final boolean isPrimary) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }

        DGMProposalFilter filter = new DGMProposalFilter();
        List<IGroovyProposal> proposals = new ArrayList<>();
        for (ClassNode category : categories) {
            boolean isDefaultCategory = isDefaultCategory(category);
            for (MethodNode method : getAllMethods(category, null)) {
                // check for DGMs filtered by deprecation or user preference
                if (isDefaultCategory && (GroovyUtils.isDeprecated(method) || filter.isFiltered(method))) {
                    continue;
                }
                String methodName = method.getName();
                if (method.isStatic() && method.isPublic()) {
                    Parameter[] params = method.getParameters();

                    if (params.length > 0 && matcher.test(prefix, methodName) &&
                            GroovyUtils.isAssignable(selfType, params[0].getType())) {
                        CategoryMethodProposal proposal = new CategoryMethodProposal(method);
                        proposal.setRelevanceMultiplier(tweakRelevance(method, selfType));
                        proposals.add(proposal);
                    }

                    if (params.length == 1 && findLooselyMatchedAccessorKind(prefix, methodName, true).isAccessorKind(method, true) &&
                            hasNoField(selfType, methodName) && GroovyUtils.isAssignable(selfType, params[0].getType()) &&
                            (isStatic || !GeneralUtils.isOrImplements(selfType, VariableScope.MAP_CLASS_NODE)) &&
                            (isDefaultCategory || !methodName.startsWith("is"))) { // GROOVY-5245
                        // add property variant of accessor category method
                        CategoryPropertyProposal proposal = new CategoryPropertyProposal(method);
                        proposal.setRelevanceMultiplier(tweakRelevance(method, selfType));
                        proposals.add(proposal);
                    }
                }
            }
        }
        return proposals;
    }

    protected boolean isDefaultCategory(final ClassNode category) {
        return (VariableScope.DGM_CLASS_NODE.equals(category) || (currentScope != null && currentScope.isDefaultCategory(category)));
    }

    protected boolean isDefaultStaticCategory(final ClassNode category) {
        return (VariableScope.DGSM_CLASS_NODE.equals(category) || (currentScope != null && currentScope.isDefaultStaticCategory(category)));
    }

    protected static boolean isObjectOrPrimitiveArray(final ClassNode type) {
        return (VariableScope.OBJECT_CLASS_NODE.equals(type.getComponentType()) || ClassHelper.isPrimitiveType(type.getComponentType()));
    }

    protected float tweakRelevance(final MethodNode method, final ClassNode selfType) {
        float relevanceMultiplier = isDefaultCategory(method.getDeclaringClass()) ? 0.1f : 5.0f;

        ClassNode firstParamType = method.getParameters()[0].getType();
        if (!selfType.equals(firstParamType)) {
            int distance = 0;

            if (!firstParamType.isInterface()) {
                ClassNode next = selfType;
                do {
                    distance += 1;
                    next = next.isArray() && !isObjectOrPrimitiveArray(next)
                        ? VariableScope.OBJECT_CLASS_NODE.makeArray() : next.getSuperClass();
                } while (next != null && !next.equals(firstParamType));
                if (next == null) distance = 5; // arbitrary for implicit Object
            } else if (firstParamType.equals(VariableScope.GROOVY_OBJECT_CLASS_NODE)) {
                distance = 5; // arbitrary for explicit or implicit GroovyObject
            } else {
                Queue<ClassDepth> todo = new LinkedList<>();
                Set<ClassNode> visited = new HashSet<>();
                todo.add(new ClassDepth(selfType, 0));

                out: while (!todo.isEmpty()) {
                    ClassDepth next = todo.remove();
                    visited.add(next.clazz.redirect());
                    for (ClassNode face : next.clazz.getInterfaces()) {
                        if (firstParamType.equals(face)) {
                            distance = next.depth + 1;
                            break out;
                        }
                        if (!visited.contains(face.redirect())) {
                            todo.add(new ClassDepth(face, next.depth + 1));
                        }
                    }

                    Optional.ofNullable(next.clazz.getSuperClass())
                        .filter(sc -> !sc.equals(VariableScope.OBJECT_CLASS_NODE))
                        .map(sc -> new ClassDepth(sc, next.depth + 1)).ifPresent(todo::add);
                }
            }

            while (distance-- > 0) {
                relevanceMultiplier *= 0.88f;
            }
        }

        return relevanceMultiplier;
    }

    private static class ClassDepth { // TODO: class->record

        ClassDepth(final ClassNode t, final int n) {
            clazz = t;
            depth = n;
        }

        final ClassNode clazz;
        final int       depth;
    }

    //--------------------------------------------------------------------------

    protected class CategoryMethodProposal extends GroovyMethodProposal {

        protected CategoryMethodProposal(final MethodNode method) {
            super(method, GROOVY_METHOD_CONTRIBUTOR);
        }

        @Override
        protected int getModifiers() {
            int modifiers = super.getModifiers();
            if (!isDefaultStaticCategory(getMethod().getDeclaringClass())) {
                modifiers &= ~Flags.AccStatic; // category methods are defined as static, but should not appear as such
            }
            return modifiers;
        }

        @Override
        protected char[] createMethodSignature() {
            return ProposalUtils.createMethodSignature(getMethod(), 1);
        }

        @Override
        protected char[][] getParameterNames(final Parameter[] parameters) {
            return (char[][]) ArrayUtils.remove(super.getParameterNames(parameters), 0);
        }

        @Override
        protected char[][] getParameterTypeNames(final Parameter[] parameters) {
            return (char[][]) ArrayUtils.remove(super.getParameterTypeNames(parameters), 0);
        }

        @Override
        public IJavaCompletionProposal createJavaProposal(final CompletionEngine engine, final ContentAssistContext context, final JavaContentAssistInvocationContext javaContext) {
            IJavaCompletionProposal javaProposal = super.createJavaProposal(engine, context, javaContext);
            if (javaProposal instanceof LazyJavaCompletionProposal) {
                //ProposalInfo proposalInfo = ((LazyJavaCompletionProposal) javaProposal).getProposalInfo();
                ProposalInfo proposalInfo = ReflectionUtils.executePrivateMethod(LazyJavaCompletionProposal.class, "getProposalInfo", javaProposal);
                //CompletionProposal proposal = ((LazyJavaCompletionProposal) javaProposal).getProposal();
                CompletionProposal proposal = ReflectionUtils.executePrivateMethod(LazyJavaCompletionProposal.class, "getProposal", javaProposal);
                // reuse existing or create one to call some private methods
                final MethodProposalInfo methodProposalInfo = (proposalInfo instanceof MethodProposalInfo ? (MethodProposalInfo) proposalInfo : new MethodProposalInfo(javaContext.getProject(), proposal));

                // replace default resolveMember, which fails due to self type being removed from parameter arrays
                ((LazyJavaCompletionProposal) javaProposal).setProposalInfo(new MemberProposalInfo(javaContext.getProject(), proposal) {
                    @Override
                    protected IMember resolveMember() throws JavaModelException {
                        IType type = fJavaProject.findType(getMethod().getDeclaringClass().getName());
                        if (type != null) {
                            try {
                                String methName = getMethod().getName();
                                String[] paramTypes = GroovyUtils.getParameterTypeSignatures(getMethod(), false);
                                //Map<String, char[]> typeVariables = methodProposalInfo.computeTypeVariables(type);
                                Map<String, char[]> typeVariables = ReflectionUtils.throwableExecutePrivateMethod(MethodProposalInfo.class, "computeTypeVariables",
                                    new Class[] {IType.class}, methodProposalInfo, new Object[] {type});

                                IMethod[] methods = type.getMethods();
                                for (int i = methods.length - 1; i >= 0; i -= 1) {
                                    if (!methName.equals(methods[i].getElementName())) continue;
                                    //boolean match = isSameMethodSignature(methName, paramTypes, false, methods[i], typeVariables, type);
                                    Boolean match = ReflectionUtils.throwableExecutePrivateMethod(MethodProposalInfo.class, "isSameMethodSignature",
                                        new Class [] {String.class, String[].class, boolean.class, IMethod.class, Map.class,     IType.class}, methodProposalInfo,
                                        new Object[] {methName,     paramTypes,     Boolean.FALSE, methods[i],    typeVariables, type       });
                                    if (Boolean.TRUE.equals(match)) {
                                        return methods[i];
                                    }
                                }
                            } catch (JavaModelException e) {
                                throw e;
                            } catch (InvocationTargetException e) {
                                if (e.getCause() instanceof JavaModelException) {
                                    throw (JavaModelException) e.getCause();
                                }
                                throw new RuntimeException(e.getCause());
                            } catch (Exception e) {
                                if (e instanceof RuntimeException) {
                                    throw (RuntimeException) e;
                                }
                                throw new RuntimeException(e);
                            }
                        }
                        return null;
                    }
                });
            }
            return javaProposal;
        }
    }

    protected class CategoryPropertyProposal extends GroovyFieldProposal {

        protected CategoryPropertyProposal(final MethodNode method) {
            super(createMockField(method));

            if (!isDefaultStaticCategory(method.getDeclaringClass())) {
                getField().setModifiers(getField().getModifiers() & ~Flags.AccStatic);
            }
        }

        @Override
        protected StyledString createDisplayString(final FieldNode field) {
            return super.createDisplayString(field).append(new StyledString(
                " (" + GROOVY_METHOD_CONTRIBUTOR + ")", StyledString.DECORATIONS_STYLER));
        }
    }
}
