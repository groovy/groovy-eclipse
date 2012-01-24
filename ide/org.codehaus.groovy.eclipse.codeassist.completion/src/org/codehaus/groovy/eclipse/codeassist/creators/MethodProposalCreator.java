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

package org.codehaus.groovy.eclipse.codeassist.creators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 * Generates all of the method proposals for a given location
 * Also will add the non-getter form of getter methods if appropriate
 *
 */
public class MethodProposalCreator extends AbstractProposalCreator implements IProposalCreator {

    ProposalFormattingOptions options = ProposalFormattingOptions.newFromOptions();

    private Set<ClassNode> alreadySeen = Collections.emptySet();

    public List<IGroovyProposal> findAllProposals(ClassNode type, Set<ClassNode> categories, String prefix, boolean isStatic,
            boolean isPrimary) {
        boolean firstTime = alreadySeen.isEmpty();
        List<MethodNode> allMethods = getAllMethods(type);
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        Set<String> alreadySeenFields = new HashSet<String>();
        if (isStatic) {
            // "class" is added by FieldProposalCreator
            alreadySeenFields.add("class");
        }

        for (MethodNode method : allMethods) {
            String methodName = method.getName();
            if ((!isStatic || method.isStatic() || method.getDeclaringClass() == VariableScope.OBJECT_CLASS_NODE) &&
                    checkName(methodName)) {
                boolean isInterestingType = isInterestingType(method
                                .getReturnType());
                if (ProposalUtils.looselyMatches(prefix, methodName)) {
                    GroovyMethodProposal methodProposal = new GroovyMethodProposal(method, "Groovy", options);
                    float relevanceMultiplier = isInterestingType ? 101f : 1f;
                    relevanceMultiplier *= method.isStatic() ? 0.1f : 1f;
                    // de-emphasize 'this' references inside closure
                    relevanceMultiplier *= !alreadySeen.isEmpty() ? 0.1f : 1f;
                    methodProposal.setRelevanceMultiplier(relevanceMultiplier);
                    groovyProposals.add(methodProposal);
                }

                AccessorSupport accessor = findLooselyMatchedAccessorKind(prefix, methodName, false);
                if (accessor.isAccessorKind(method, false)) {
                    // if there is a getter or setter, then add a field proposal
                    // with the name being gotten
                    String mockFieldName = ProposalUtils.createMockFieldName(methodName);
                    if (!alreadySeenFields.contains(mockFieldName)) {
                        // be careful not to add fields twice
                        alreadySeenFields.add(mockFieldName);
                        if (hasNoField(method.getDeclaringClass(), methodName)) {
                            GroovyFieldProposal fieldProposal = new GroovyFieldProposal(createMockField(method));
                            fieldProposal.setRelevanceMultiplier(isInterestingType ? 11 : 1);
                            groovyProposals.add(fieldProposal);
                        }
                    }
                }
            }
        }

        // now do methods from static imports
        ClassNode enclosingTypeDeclaration = currentScope
                .getEnclosingTypeDeclaration();
        if (enclosingTypeDeclaration != null && firstTime && isPrimary && type.getModule() != null) {
            groovyProposals.addAll(getStaticImportProposals(prefix,
                    type.getModule()));
        }

        return groovyProposals;
    }

    protected List<MethodNode> getAllMethods(ClassNode thisType) {
        Set<ClassNode> types = new HashSet<ClassNode>();

        List<MethodNode> allMethods = thisType.getAllDeclaredMethods();
        if (!alreadySeen.isEmpty()) {
            // remove all methods from classes that we have already visited
            for (Iterator<MethodNode> methodIter = allMethods.iterator(); methodIter.hasNext();) {
                if (alreadySeen.contains(methodIter.next().getDeclaringClass())) {
                    methodIter.remove();
                }
            }
        }


        // keep track of the already seen types so that next time, we won't include them
        getAllSupers(thisType, types, alreadySeen);
        if (alreadySeen.isEmpty()) {
            alreadySeen = types;
        } else {
            alreadySeen.addAll(types);
        }


        return allMethods;
    }

    private List<IGroovyProposal> getStaticImportProposals(String prefix,
            ModuleNode module) {
        List<IGroovyProposal> staticProposals = new ArrayList<IGroovyProposal>();
        Map<String, ImportNode> staticImports = ImportNodeCompatibilityWrapper
                .getStaticImports(module);
        for (Entry<String, ImportNode> entry : staticImports.entrySet()) {
            String fieldName = entry.getValue().getFieldName();
            if (fieldName != null
                    && ProposalUtils.looselyMatches(prefix, fieldName)) {
                List<MethodNode> methods = entry.getValue().getType()
                        .getDeclaredMethods(fieldName);
                if (methods != null) {
                    for (MethodNode method : methods) {
                        staticProposals.add(new GroovyMethodProposal(method, "Groovy", options));
                    }
                }
            }
        }
        Map<String, ImportNode> staticStarImports = ImportNodeCompatibilityWrapper
                .getStaticStarImports(module);
        for (Entry<String, ImportNode> entry : staticStarImports.entrySet()) {
            ClassNode type = entry.getValue().getType();
            if (type != null) {
                for (MethodNode method : (Iterable<MethodNode>) type
                        .getMethods()) {
                    if (method.isStatic()
                            && ProposalUtils.looselyMatches(prefix,
                                    method.getName())) {
                        staticProposals.add(new GroovyMethodProposal(method, "Groovy", options));
                    }
                }
            }
        }

        return staticProposals;
    }

}
