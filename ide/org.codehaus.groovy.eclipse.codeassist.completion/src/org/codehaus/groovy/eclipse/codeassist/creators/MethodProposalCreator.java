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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Generates all of the method proposals for a given location.
 * Also will add the property form of accessor methods if appropriate.
 */
public class MethodProposalCreator extends AbstractProposalCreator {

    private Set<ClassNode> alreadySeen = new HashSet<ClassNode>();

    public List<IGroovyProposal> findAllProposals(ClassNode type, Set<ClassNode> categories, String prefix, boolean isStatic, boolean isPrimary) {
        List<IGroovyProposal> proposals = new LinkedList<IGroovyProposal>();

        boolean firstTime = alreadySeen.isEmpty();
        List<MethodNode> allMethods = getAllMethods(type, alreadySeen);
        Set<String> alreadySeenFields = new HashSet<String>();
        if (isStatic) {
            // "class" is added by FieldProposalCreator
            alreadySeenFields.add("class");
        }

        for (MethodNode method : allMethods) {
            String methodName = method.getName();
            if ((!isStatic || method.isStatic() || method.getDeclaringClass() == VariableScope.OBJECT_CLASS_NODE) && checkName(methodName)) {
                if (ProposalUtils.looselyMatches(prefix, methodName)) {
                    GroovyMethodProposal proposal = new GroovyMethodProposal(method);
                    setRelevanceMultiplier(proposal, firstTime, isStatic);
                    proposals.add(proposal);
                }

                AccessorSupport accessor = findLooselyMatchedAccessorKind(prefix, methodName, false);
                if (accessor.isAccessorKind(method, false)) {
                    // if there is a getter or setter, then add a field proposal as well
                    String mockFieldName = ProposalUtils.createMockFieldName(methodName);
                    if (!alreadySeenFields.contains(mockFieldName)) {
                        // be careful not to add fields twice
                        alreadySeenFields.add(mockFieldName);
                        if (hasNoField(method.getDeclaringClass(), methodName)) {
                            proposals.add(new GroovyFieldProposal(createMockField(method)));
                        }
                    }
                }
            }
        }

        if (currentScope != null) {
            ClassNode enclosingTypeDeclaration = currentScope.getEnclosingTypeDeclaration();
            if (enclosingTypeDeclaration != null && firstTime && isPrimary && type.getModule() != null) {
                findStaticImportProposals(proposals, prefix, type.getModule());
                findStaticFavoriteProposals(proposals, prefix, type.getModule());
            }
        }

        // remove proposals for synthetic members
        for (Iterator<IGroovyProposal> it = proposals.iterator(); it.hasNext();) {
            IGroovyProposal proposal = it.next();
            if (proposal instanceof GroovyMethodProposal) {
                if (GroovyUtils.isSynthetic(((GroovyMethodProposal) proposal).getMethod())) {
                    it.remove();
                }
            } else if (proposal instanceof GroovyFieldProposal) {
                if (GroovyUtils.isSynthetic(((GroovyFieldProposal) proposal).getField())) {
                    it.remove();
                }
            }
        }

        return proposals;
    }

    private void findStaticImportProposals(List<IGroovyProposal> proposals, String prefix, ModuleNode module) {
        for (Map.Entry<String, ImportNode> entry : module.getStaticImports().entrySet()) {
            String fieldName = entry.getValue().getFieldName();
            if (ProposalUtils.looselyMatches(prefix, fieldName)) {
                List<MethodNode> methods = entry.getValue().getType().getDeclaredMethods(fieldName);
                if (methods != null) {
                    for (MethodNode method : methods) {
                        if (method.isStatic()) {
                            addIfNotPresent(proposals, new GroovyMethodProposal(method));
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, ImportNode> entry : module.getStaticStarImports().entrySet()) {
            ClassNode type = entry.getValue().getType();
            if (type != null) {
                for (MethodNode method : (Iterable<MethodNode>) type.getMethods()) {
                    if (method.isStatic() && ProposalUtils.looselyMatches(prefix, method.getName())) {
                        addIfNotPresent(proposals, new GroovyMethodProposal(method));
                    }
                }
            }
        }
    }

    private void findStaticFavoriteProposals(List<IGroovyProposal> proposals, String prefix, ModuleNode module) {
        for (String favoriteStaticMember : favoriteStaticMembers) {
            int pos = favoriteStaticMember.lastIndexOf('.');
            String typeName = favoriteStaticMember.substring(0, pos);
            String fieldName = favoriteStaticMember.substring(pos + 1);
            ClassNode typeNode = tryResolveClassNode(typeName, module);

            if (typeNode == null) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST, "Cannot resolve favorite type " + typeName);
                }
                continue;
            }

            if ("*".equals(fieldName)) {
                for (MethodNode method : (Iterable<MethodNode>) typeNode.getMethods()) {
                    if (method.isStatic() && ProposalUtils.looselyMatches(prefix, method.getName())) {
                        GroovyMethodProposal proposal = new GroovyMethodProposal(method);
                        proposal.setRequiredStaticImport(typeName + '.' + method.getName());
                        addIfNotPresent(proposals, proposal);
                    }
                }
            } else {
                if (ProposalUtils.looselyMatches(prefix, fieldName)) {
                    List<MethodNode> methods = typeNode.getDeclaredMethods(fieldName);
                    for (MethodNode method : methods) {
                        if (method.isStatic()) {
                            GroovyMethodProposal proposal = new GroovyMethodProposal(method);
                            proposal.setRequiredStaticImport(favoriteStaticMember);
                            addIfNotPresent(proposals, proposal);
                        }
                    }
                }
            }
        }
    }

    private void setRelevanceMultiplier(GroovyMethodProposal proposal, boolean firstTime, boolean isStatic) {
        MethodNode method = proposal.getMethod();

        float relevanceMultiplier;
        if (isStatic && method.isStatic()) {
            relevanceMultiplier = 1.10f;
        } else if (!method.isStatic()) {
            relevanceMultiplier = 1.00f;
        } else {
            relevanceMultiplier = 0.77f;
        }

        // de-emphasize 'this' references inside closure
        if (!firstTime) {
            relevanceMultiplier *= 0.1f;
        }

        proposal.setRelevanceMultiplier(relevanceMultiplier);
    }

    private static void addIfNotPresent(List<IGroovyProposal> proposals, GroovyMethodProposal proposal) {
        if (!proposals.isEmpty()) {
            String sig = ProposalUtils.createMethodSignatureStr(proposal.getMethod());
            for (IGroovyProposal igp : proposals) {
                if (igp instanceof GroovyMethodProposal) {
                    GroovyMethodProposal gmp = (GroovyMethodProposal) igp;
                    if (gmp.getMethod().isStatic() && sig.equals(ProposalUtils.createMethodSignatureStr(gmp.getMethod()))) {
                        return;
                    }
                }
            }
        }
        proposals.add(proposal);
    }
}
