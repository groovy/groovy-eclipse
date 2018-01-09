/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.creators;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MemberProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.MethodProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

public class CategoryProposalCreator extends AbstractProposalCreator {

    @Override
    public List<IGroovyProposal> findAllProposals(ClassNode selfType, Set<ClassNode> categories, String prefix, boolean isStatic, boolean isPrimary) {
        DGMProposalFilter filter = new DGMProposalFilter();
        Set<String> existingPropertyProposals = new HashSet<>();
        Map<String, List<MethodNode>> existingMethodProposals = new HashMap<>();

        List<IGroovyProposal> groovyProposals = new LinkedList<>();
        for (ClassNode category : categories) {
            boolean isDGMCategory = isDGM(category);
            for (MethodNode method : category.getAllDeclaredMethods()) {
                // check for DGMs filtered by deprecation or user preference
                if (isDGMCategory && (GroovyUtils.isDeprecated(method) || filter.isFiltered(method))) {
                    continue;
                }
                String methodName = method.getName();
                if (method.isStatic() && method.isPublic()) {
                    Parameter[] params = method.getParameters();
                    // need to check if the method is being accessed directly or as a property (eg- getText() --> text)
                    if (ProposalUtils.looselyMatches(prefix, methodName)) {
                        if (params.length > 0 && GroovyUtils.isAssignable(selfType, params[0].getType()) && !isDuplicate(method, existingMethodProposals)) {
                            groovyProposals.add(new CategoryMethodProposal(method));

                            List<MethodNode> methodList = existingMethodProposals.get(methodName);
                            if (methodList == null) {
                                methodList = new ArrayList<>(2);
                                existingMethodProposals.put(methodName, methodList);
                            }
                            methodList.add(method);
                        }
                    } else if (params.length == 1 && findLooselyMatchedAccessorKind(prefix, methodName, true).isAccessorKind(method, true) && !existingPropertyProposals.contains(methodName) && hasNoField(selfType, methodName) && GroovyUtils.isAssignable(selfType, params[0].getType())) {
                        // add property variant of accessor name
                        groovyProposals.add(new CategoryPropertyProposal(method));

                        existingPropertyProposals.add(methodName);
                    }
                }
            }
        }
        return groovyProposals;
    }

    /**
     * Checks that the new method hasn't already been added.
     */
    protected boolean isDuplicate(MethodNode newMethod, Map<String, List<MethodNode>> existingMethodProposals) {
        List<MethodNode> otherMethods = existingMethodProposals.get(newMethod.getName());
        if (otherMethods != null) {
            Parameter[] newParameters = newMethod.getParameters();
            iterator: for (Iterator<MethodNode> it = otherMethods.iterator(); it.hasNext();) {
                MethodNode otherMethod = it.next();
                Parameter[] otherParameters = otherMethod.getParameters();
                if (otherParameters.length == newParameters.length) {
                    for (int i = 1, n = otherParameters.length; i < n; i += 1) {
                        if (!otherParameters[i].getType().getName().equals(newParameters[i].getType().getName())) {
                            // there is a mismatched parameter
                            continue iterator;
                        }
                    }

                    // all parameters match
                    /*if (GroovyUtils.isAssignable(otherParameters[0].getType(), newParameters[0].getType())) {
                        it.remove();
                        break;
                    } else {*/
                        return true;
                    /*}*/
                }
            }
        }
        return false;
    }

    protected boolean isDGM(ClassNode category) {
        return VariableScope.ALL_DEFAULT_CATEGORIES.contains(category);
    }

    protected boolean isDGSM(ClassNode category) {
        // TODO: check the runtime DGM configuration
        return VariableScope.DGSM_CLASS_NODE.equals(category);
    }

    @Override
    public boolean redoForLoopClosure() {
        return true;
    }

    //--------------------------------------------------------------------------

    protected class CategoryMethodProposal extends GroovyMethodProposal {

        protected CategoryMethodProposal(MethodNode method) {
            super(method, "Groovy");

            if (isDGM(method.getDeclaringClass())) {
                setRelevanceMultiplier(0.1f);
            } else {
                setRelevanceMultiplier(5);
            }
        }

        @Override
        protected int getModifiers() {
            int modifiers = super.getModifiers();
            if (!isDGSM(getMethod().getDeclaringClass())) {
                modifiers &= ~Flags.AccStatic; // category methods are defined as static, but should not appear as such
            }
            return modifiers;
        }

        @Override
        protected char[] createMethodSignature() {
            return ProposalUtils.createMethodSignature(getMethod(), 1);
        }

        @Override
        protected char[][] createAllParameterNames(ICompilationUnit unit) {
            return (char[][]) ArrayUtils.remove(super.createAllParameterNames(unit), 0);
        }

        @Override
        protected char[][] getParameterTypeNames(Parameter[] parameters) {
            return (char[][]) ArrayUtils.remove(super.getParameterTypeNames(parameters), 0);
        }

        @Override
        public IJavaCompletionProposal createJavaProposal(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
            IJavaCompletionProposal javaProposal = super.createJavaProposal(context, javaContext);
            if (javaProposal instanceof LazyJavaCompletionProposal) {
                //ProposalInfo proposalInfo = ((LazyJavaCompletionProposal) javaProposal).getProposalInfo();
                ProposalInfo proposalInfo = (ProposalInfo) ReflectionUtils.executeNoArgPrivateMethod(LazyJavaCompletionProposal.class, "getProposalInfo", javaProposal);
                //CompletionProposal proposal = ((LazyJavaCompletionProposal) javaProposal).getProposal();
                CompletionProposal proposal = (CompletionProposal) ReflectionUtils.executeNoArgPrivateMethod(LazyJavaCompletionProposal.class, "getProposal", javaProposal);
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
                                @SuppressWarnings("unchecked") Map<String, char[]> typeVariables = (Map<String, char[]>) ReflectionUtils.
                                    throwableExecutePrivateMethod(MethodProposalInfo.class, "computeTypeVariables", new Class[] {IType.class}, methodProposalInfo, new Object[] {type});

                                IMethod[] methods = type.getMethods();
                                for (int i = methods.length - 1; i >= 0; i -= 1) {
                                    if (!methName.equals(methods[i].getElementName())) continue;
                                    //boolean match = isSameMethodSignature(methName, paramTypes, false, methods[i], typeVariables, type);
                                    Boolean match = (Boolean) ReflectionUtils.throwableExecutePrivateMethod(MethodProposalInfo.class, "isSameMethodSignature",
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

            if (!isDGSM(method.getDeclaringClass())) {
                getField().setModifiers(getField().getModifiers() & ~Flags.AccStatic);
            }

            if (isDGM(method.getDeclaringClass())) {
                setRelevanceMultiplier(0.1f);
            } else {
                setRelevanceMultiplier(5);
            }
        }
    }
}
