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
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyCategoryMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.VariableScope;

public class CategoryProposalCreator extends AbstractProposalCreator {

    public List<IGroovyProposal> findAllProposals(ClassNode selfType, Set<ClassNode> categories, String prefix, boolean isStatic, boolean isPrimary) {
        DGMProposalFilter filter = new DGMProposalFilter();
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        Set<String> existingFieldProposals = new HashSet<String>();
        Map<String, List<MethodNode>> existingMethodProposals = new HashMap<String, List<MethodNode>>();
        for (ClassNode category : categories) {
            List<MethodNode> allMethods = category.getAllDeclaredMethods();
            boolean isDGMCategory = isDGMCategory(category);
            for (MethodNode method : allMethods) {
                // Check for DGMs filtered from preferences
                if (isDGMCategory && filter.isFiltered(method)) {
                    continue;
                }
                String methodName = method.getName();
                if (method.isStatic() && method.isPublic()) {
                    Parameter[] params = method.getParameters();
                    // need to check if the method is being accessed directly or as a property (eg- getText() --> text)
                    if (ProposalUtils.looselyMatches(prefix, methodName)) {
                        if (params.length > 0 && GroovyUtils.isAssignable(selfType, params[0].getType()) && !isDuplicate(method, existingMethodProposals)) {
                            GroovyCategoryMethodProposal methodProposal = new GroovyCategoryMethodProposal(method);
                            methodProposal.setRelevanceMultiplier(isInterestingType(method.getReturnType()) ? 101 : 1);
                            groovyProposals.add(methodProposal);
                            List<MethodNode> methodList = existingMethodProposals.get(methodName);
                            if (methodList == null) {
                                methodList = new ArrayList<MethodNode>(2);
                                existingMethodProposals.put(methodName, methodList);
                            }
                            methodList.add(method);
                        }
                    } else if (params.length == 1 && findLooselyMatchedAccessorKind(prefix, methodName, true).isAccessorKind(method, true) && !existingFieldProposals.contains(methodName) && hasNoField(selfType, methodName)) {
                        // add property variant of accessor name
                        GroovyFieldProposal fieldProposal = new GroovyFieldProposal(createMockField(method));
                        fieldProposal.setRelevanceMultiplier(1);
                        groovyProposals.add(fieldProposal);
                        existingFieldProposals.add(methodName);
                    }
                }
            }
        }
        return groovyProposals;
    }

    /**
     * Checks that the new method hasn't already been added.
     */
    private boolean isDuplicate(MethodNode newMethod, Map<String, List<MethodNode>> existingMethodProposals) {
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

    private boolean isDGMCategory(ClassNode category) {
        String className = category.getName();
        for (ClassNode dgClass : VariableScope.ALL_DEFAULT_CATEGORIES) {
            if (dgClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean redoForLoopClosure() {
        return true;
    }
}
