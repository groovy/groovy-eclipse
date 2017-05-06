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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * GRECLIPSE-512: most fields have properties created for them, so we want to avoid duplicate proposals.
 * Constants are an exception, so return them here. I may have missed something, so be prepared to add more kinds of fields here.
 */
public class FieldProposalCreator extends AbstractProposalCreator implements IProposalCreator {

    private static final GroovyFieldProposal CLASS_PROPOSAL = createClassProposal();

    private Set<ClassNode> alreadySeen = new HashSet<ClassNode>();

    private static GroovyFieldProposal createClassProposal() {
        FieldNode field = new FieldNode("class", Opcodes.ACC_PUBLIC & Opcodes.ACC_STATIC & Opcodes.ACC_FINAL, VariableScope.CLASS_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE, null);
        field.setDeclaringClass(VariableScope.OBJECT_CLASS_NODE);
        return new GroovyFieldProposal(field);
    }

    public List<IGroovyProposal> findAllProposals(ClassNode type, Set<ClassNode> categories, String prefix, boolean isStatic, boolean isPrimary) {
        List<IGroovyProposal> proposals = new ArrayList<IGroovyProposal>();

        boolean isFirstTime = alreadySeen.isEmpty();
        Collection<FieldNode> allFields = getAllFields(type, alreadySeen);

        for (FieldNode field : allFields) {
            // in static context, only allow static fields
            if ((!isStatic || field.isStatic()) && ProposalUtils.looselyMatches(prefix, field.getName())) {
                float relevanceMultiplier = isInterestingType(field.getType()) ? 101 : 1;
                if (field.isStatic()) relevanceMultiplier *= 0.1f;
                // de-emphasize 'this' references inside closure
                if (!isFirstTime) relevanceMultiplier *= 0.1f;

                GroovyFieldProposal proposal = new GroovyFieldProposal(field);
                proposal.setRelevanceMultiplier(relevanceMultiplier);
                proposals.add(proposal);

                if (field.getInitialExpression() instanceof ClosureExpression) {
                    // also add a method-like proposal
                    proposals.add(new GroovyMethodProposal(convertToMethodProposal(field)));
                }
            }
        }

        if (isStatic && "class".startsWith(prefix)) {
            proposals.add(CLASS_PROPOSAL);
        }

        if (currentScope != null) {
            ClassNode enclosingTypeDeclaration = currentScope.getEnclosingTypeDeclaration();
            if (enclosingTypeDeclaration != null && isFirstTime && isPrimary && type.getModule() != null) {
                findStaticImportProposals(proposals, prefix, type.getModule());
                findStaticFavoriteProposals(proposals, prefix, type.getModule());
            }
        }

        return proposals;
    }

    @Override
    protected boolean isInterestingType(ClassNode type) {
        return type.isEnum() || super.isInterestingType(type);
    }

    private MethodNode convertToMethodProposal(FieldNode field) {
        MethodNode method = new MethodNode(field.getName(), field.getModifiers(), field.getType(),
            extractParameters(field.getInitialExpression()), ClassNode.EMPTY_ARRAY, new BlockStatement());
        method.setDeclaringClass(field.getDeclaringClass());
        return method;
    }

    private Parameter[] extractParameters(Expression expr) {
        if (expr instanceof ClosureExpression) {
            return ((ClosureExpression) expr).getParameters();
        }
        return Parameter.EMPTY_ARRAY;
    }

    private void findStaticImportProposals(List<IGroovyProposal> proposals, String prefix, ModuleNode module) {
        for (Map.Entry<String, ImportNode> entry : ImportNodeCompatibilityWrapper.getStaticImports(module).entrySet()) {
            String fieldName = entry.getValue().getFieldName();
            if (fieldName != null && ProposalUtils.looselyMatches(prefix, fieldName)) {
                FieldNode field = entry.getValue().getType().getField(fieldName);
                if (field != null && field.isStatic()) {
                    proposals.add(new GroovyFieldProposal(field));
                }
            }
        }
        for (Map.Entry<String, ImportNode> entry : ImportNodeCompatibilityWrapper.getStaticStarImports(module).entrySet()) {
            ClassNode type = entry.getValue().getType();
            if (type != null) {
                for (FieldNode field : (Iterable<FieldNode>) type.getFields()) {
                    if (field.isStatic() && ProposalUtils.looselyMatches(prefix, field.getName())) {
                        proposals.add(new GroovyFieldProposal(field));
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
                    GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST, "FieldProposalCreator: Cannot resolve favorite type " + typeName);
                }
                continue;
            }

            if ("*".equals(fieldName)) {
                for (FieldNode field : (Iterable<FieldNode>) typeNode.getFields()) {
                    if (field.isStatic() && ProposalUtils.looselyMatches(prefix, field.getName())) {
                        proposals.add(newFavoriteFieldProposal(field, typeName + '.' + field.getName()));
                    }
                }
            } else {
                if (ProposalUtils.looselyMatches(prefix, fieldName)) {
                    FieldNode field = typeNode.getField(fieldName);
                    if (field != null && field.isStatic()) {
                        proposals.add(newFavoriteFieldProposal(field, favoriteStaticMember));
                    }
                }
            }
        }
    }

    private IGroovyProposal newFavoriteFieldProposal(FieldNode field, String favorite) {
        GroovyFieldProposal proposal = new GroovyFieldProposal(field);
        proposal.setRequiredStaticImport(favorite);
        return proposal;
    }
}
