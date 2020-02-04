/*
 * Copyright 2009-2020 the original author or authors.
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
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
    public List<IGroovyProposal> findAllProposals(ClassNode selfType, Set<ClassNode> categories, String prefix, boolean isStatic, boolean isPrimary) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }

        DGMProposalFilter filter = new DGMProposalFilter();
        List<IGroovyProposal> proposals = new ArrayList<>();
        for (ClassNode category : categories) {
            boolean isDefaultCategory = isDefaultCategory(category);
            for (MethodNode method : category.getAllDeclaredMethods()) {
                // check for DGMs filtered by deprecation or user preference
                if (isDefaultCategory && (GroovyUtils.isDeprecated(method) || filter.isFiltered(method))) {
                    continue;
                }
                String methodName = method.getName();
                if (method.isStatic() && method.isPublic()) {
                    Parameter[] params = method.getParameters();

                    if (matcher.test(prefix, methodName)) {
                        if (params.length > 0 && GroovyUtils.isAssignable(selfType, params[0].getType())) {
                            proposals.add(new CategoryMethodProposal(method));
                        }
                    }

                    if (params.length == 1 && findLooselyMatchedAccessorKind(prefix, methodName, true).isAccessorKind(method, true) &&
                            hasNoField(selfType, methodName) && GroovyUtils.isAssignable(selfType, params[0].getType()) &&
                            (isDefaultCategory || !methodName.startsWith("is"))) { // GROOVY-5245
                        // add property variant of accessor method
                        proposals.add(new CategoryPropertyProposal(method));
                    }
                }
            }
        }
        return proposals;
    }

    protected boolean isDefaultCategory(ClassNode category) {
        return (VariableScope.DGM_CLASS_NODE.equals(category) || (currentScope != null && currentScope.isDefaultCategory(category)));
    }

    protected boolean isDefaultStaticCategory(ClassNode category) {
        return (VariableScope.DGSM_CLASS_NODE.equals(category) || (currentScope != null && currentScope.isDefaultStaticCategory(category)));
    }

    //--------------------------------------------------------------------------

    protected class CategoryMethodProposal extends GroovyMethodProposal {

        protected CategoryMethodProposal(MethodNode method) {
            super(method, GROOVY_METHOD_CONTRIBUTOR);

            if (isDefaultCategory(method.getDeclaringClass())) {
                setRelevanceMultiplier(0.1f);
            } else {
                setRelevanceMultiplier(5);
            }
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
        protected char[][] getParameterNames(Parameter[] parameters) {
            return (char[][]) ArrayUtils.remove(super.getParameterNames(parameters), 0);
        }

        @Override
        protected char[][] getParameterTypeNames(Parameter[] parameters) {
            return (char[][]) ArrayUtils.remove(super.getParameterTypeNames(parameters), 0);
        }

        @Override
        public IJavaCompletionProposal createJavaProposal(CompletionEngine engine, ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
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

        protected CategoryPropertyProposal(MethodNode method) {
            super(createMockField(method));

            if (!isDefaultStaticCategory(method.getDeclaringClass())) {
                getField().setModifiers(getField().getModifiers() & ~Flags.AccStatic);
            }

            if (isDefaultCategory(method.getDeclaringClass())) {
                setRelevanceMultiplier(0.1f);
            } else {
                setRelevanceMultiplier(5);
            }
        }

        @Override
        protected StyledString createDisplayString(FieldNode field) {
            return super.createDisplayString(field).append(new StyledString(
                " (" + GROOVY_METHOD_CONTRIBUTOR + ")", StyledString.DECORATIONS_STYLER));
        }
    }
}
