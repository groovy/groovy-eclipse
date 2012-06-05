/*
 * Copyright 2009-2010 the original author or authors.
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
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class CategoryProposalCreator extends AbstractProposalCreator {

    public List<IGroovyProposal> findAllProposals(ClassNode type, Set<ClassNode> categories, String prefix, boolean isStatic,
            boolean isPrimary) {
        ClassNode candidate = VariableScope.maybeConvertFromPrimitive(type);
        Set<String> set = new HashSet<String>();
        getAllSupersAsStrings(candidate, set);
        set.add("java.lang.Object");
        List<IGroovyProposal> groovyProposals = findAllProposals(set, categories, prefix, candidate);
        return groovyProposals;
    }

    private List<IGroovyProposal> findAllProposals(Set<String> set, Set<ClassNode> categories, String prefix,
            ClassNode declaringClass) {
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
                // need to check if the method is being accessed directly
                // or as a property (eg- getText() --> text)
                String methodName = method.getName();
                if (method.isStatic() && method.isPublic()) {
                    Parameter[] params = method.getParameters();
                    if (ProposalUtils.looselyMatches(prefix, methodName)) {
                        if (params != null && params.length > 0 && set.contains(params[0].getType().getName())
                                && !dupMethod(method, existingMethodProposals)) {
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
                    } else if (params.length == 1
                            && findLooselyMatchedAccessorKind(prefix, methodName, true).isAccessorKind(method, true)
                            && !existingFieldProposals.contains(methodName) && hasNoField(declaringClass, methodName)) {
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
     * Check thatthe new method hasn't already been added
     * We SHOULD be checking if this new method is more specific than the old
     * method
     * and replacing if it is, but we are not doing that.
     *
     * @param method
     * @param existingMethodProposals
     * @return
     */
    private boolean dupMethod(MethodNode newMethod, Map<String, List<MethodNode>> existingMethodProposals) {
        List<MethodNode> otherMethods = existingMethodProposals.get(newMethod.getName());
        if (otherMethods != null) {
            Parameter[] newParameters = newMethod.getParameters();
            for (MethodNode otherMethod : otherMethods) {
                Parameter[] otherParameters = otherMethod.getParameters();
                if (otherParameters.length == newParameters.length) {
                    // already know that the first parameter has a matching
                    // type, so
                    // start with remaining parameters
                    for (int i = 1; i < otherParameters.length; i++) {
                        if (!otherParameters[i].getType().getName().equals(newParameters[i].getType().getName())) {
                            // there is a mismatched parameter
                            continue;
                        }
                    }
                    // all parameters match
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param category
     * @return
     */
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