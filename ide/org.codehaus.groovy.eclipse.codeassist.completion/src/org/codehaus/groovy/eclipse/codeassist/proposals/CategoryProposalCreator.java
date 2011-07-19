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

package org.codehaus.groovy.eclipse.codeassist.proposals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class CategoryProposalCreator extends AbstractProposalCreator {

    public List<IGroovyProposal> findAllProposals(ClassNode type, Set<ClassNode> categories, String prefix, boolean isStatic) {
        ClassNode candidate = VariableScope.maybeConvertFromPrimitive(type);
        Set<String> set = new HashSet<String>();
        getAllSupersAsStrings(candidate, set);
        set.add("java.lang.Object");
        List<IGroovyProposal> groovyProposals = findAllProposals(set, categories, prefix, candidate);
        return groovyProposals;
    }

    private List<IGroovyProposal> findAllProposals(Set<String> set, Set<ClassNode> categories, String prefix,
            ClassNode declaringClass) {
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        Set<String> existingFieldProposals = new HashSet<String>();
        for (ClassNode category : categories) {
            List<MethodNode> allMethods = category.getAllDeclaredMethods();
            for (MethodNode method : allMethods) {

                // need to check if the method is being accessed directly
                // or as a property (eg- getText() --> text)
                String methodName = method.getName();
                if (method.isStatic() && method.isPublic()) {
                    Parameter[] params = method.getParameters();
                    if (ProposalUtils.looselyMatches(prefix, methodName)) {
                        if (params != null && params.length > 0 && set.contains(params[0].getType().getName())) {
                            GroovyCategoryMethodProposal methodProposal = new GroovyCategoryMethodProposal(method);
                            methodProposal.setRelevanceMultiplier(isInterestingType(method.getReturnType()) ? 101 : 1);
                            groovyProposals.add(methodProposal);
                        }
                    } else if (params.length == 1 && looselyMatchesGetterName(prefix, methodName)
                            && !existingFieldProposals.contains(methodName) && hasNoField(declaringClass, methodName)) {
                        // we are not handling setters...should we?
                        // add property
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
}
