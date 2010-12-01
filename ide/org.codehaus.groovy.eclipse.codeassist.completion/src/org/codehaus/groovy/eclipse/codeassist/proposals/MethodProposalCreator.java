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

package org.codehaus.groovy.eclipse.codeassist.proposals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 * Generates all of the method proposals for a given location
 * Also will add the non-getter form of getter methods if appropriate
 *
 */
public class MethodProposalCreator extends AbstractProposalCreator implements IProposalCreator {

    public List<IGroovyProposal> findAllProposals(ClassNode type,
            Set<ClassNode> categories, String prefix, boolean isStatic) {
        List<MethodNode> allMethods = type.getAllDeclaredMethods();
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        Set<String> alreadySeenFields = new HashSet<String>();

        for (MethodNode method : allMethods) {
            String methodName = method.getName();
            if ((!isStatic || method.isStatic() || method.getDeclaringClass() == VariableScope.OBJECT_CLASS_NODE) &&
                    checkName(methodName)) {
                boolean isInterestingType = isInterestingType(method
                                .getReturnType());
                if (ProposalUtils.looselyMatches(prefix, methodName)) {
                    GroovyMethodProposal methodProposal = new GroovyMethodProposal(method);
                    float relevanceMultiplier = isInterestingType ? 101 : 1;
                    relevanceMultiplier *= method.isStatic() ? 0.1 : 1;
                    methodProposal
                            .setRelevanceMultiplier(relevanceMultiplier);
                    groovyProposals.add(methodProposal);
                }

                if (looselyMatchesGetterName(prefix, methodName)) {
                    // if there is a getter or setter, then add a field proposal
                    // with the name being gotten
                    String mockFieldName = createMockFieldName(methodName);
                    if (!alreadySeenFields.contains(mockFieldName)) {

                        // be careful not to add fields twice
                        alreadySeenFields.add(mockFieldName);
                        if (hasNoField(method)) {
                            GroovyFieldProposal fieldProposal = new GroovyFieldProposal(
                                    createMockField(method));
                            fieldProposal
                                    .setRelevanceMultiplier(isInterestingType ? 11
                                            : 1);
                            groovyProposals.add(fieldProposal);
                        }
                    }
                }
            }
        }

        // now do methods from static imports
        ClassNode enclosingTypeDeclaration = currentScope
                .getEnclosingTypeDeclaration();
        if (enclosingTypeDeclaration != null
                && type.getName().equals(enclosingTypeDeclaration.getName())) {
            groovyProposals.addAll(getStaticImportProposals(prefix,
                    type.getModule()));
        }

        return groovyProposals;
    }

    /**
     * Check to ensure that there is no field with that name before creating
     * the mock field
     */
    private boolean hasNoField(MethodNode method) {
        return method.getDeclaringClass().getField(
                createMockFieldName(method.getName())) == null
                && method.getDeclaringClass().getField(
                        createCapitalMockFieldName(method.getName())) == null;
    }

    private FieldNode createMockField(MethodNode method) {
        FieldNode field = new FieldNode(createMockFieldName(method.getName()), method.getModifiers(), method.getReturnType(), method.getDeclaringClass(), null);
        field.setDeclaringClass(method.getDeclaringClass());
        field.setSourcePosition(method);
        return field;
    }

    private boolean looselyMatchesGetterName(String prefix, String methodName) {
        // fail fast if name is < 4 chars, it doesn't start with get or set, or
        // its 4th char is lower case
        if (methodName.length() < 4) {
            return false;
        } else if (!(methodName.startsWith("get") || methodName
                .startsWith("set"))
                || Character.isLowerCase(methodName.charAt(3))) {
            return false;
        }

        String newName = createMockFieldName(methodName);
        return ProposalUtils.looselyMatches(prefix, newName);
    }

    private String createMockFieldName(String methodName) {
        return methodName.length() > 3 ? Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4) : "$$$$$";
    }

    private String createCapitalMockFieldName(String methodName) {
        return methodName.length() > 3 ? methodName.substring(3) : "$$$$$";
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
                        staticProposals.add(new GroovyMethodProposal(method));
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
                        staticProposals.add(new GroovyMethodProposal(method));
                    }
                }
            }
        }

        return staticProposals;
    }

}
