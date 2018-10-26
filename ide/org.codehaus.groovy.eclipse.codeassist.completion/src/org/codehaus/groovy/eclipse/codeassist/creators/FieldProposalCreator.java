/*
 * Copyright 2009-2018 the original author or authors.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * GRECLIPSE-512: most fields have properties created for them, so we want to avoid duplicate proposals.
 * Constants are an exception, so return them here. I may have missed something, so be prepared to add more kinds of fields here.
 */
public class FieldProposalCreator extends AbstractProposalCreator {

    private final Set<ClassNode> alreadySeen = new HashSet<>();

    @Override
    public List<IGroovyProposal> findAllProposals(ClassNode type, Set<ClassNode> categories, String prefix, boolean isStatic, boolean isPrimary) {
        List<IGroovyProposal> proposals = new ArrayList<>();

        boolean firstTime = alreadySeen.isEmpty();
        Collection<FieldNode> allFields = getAllFields(type, alreadySeen);

        for (FieldNode field : allFields) {
            // in static context, only allow static fields
            if ((!isStatic || field.isStatic()) && matcher.test(prefix, field.getName())) {
                // de-emphasize 'this' references inside closure
                float relevanceMultiplier = !firstTime ? 0.1f : 1.0f;
                if (field.isEnum()) relevanceMultiplier *= 5.0f;

                GroovyFieldProposal proposal = new GroovyFieldProposal(field);
                proposal.setRelevanceMultiplier(relevanceMultiplier);
                proposals.add(proposal);

                if (field.getInitialExpression() instanceof ClosureExpression) {
                    // also add a method-like proposal
                    proposals.add(new GroovyMethodProposal(convertToMethodProposal(field)));
                }
            }
        }

        if (!isPrimary && "class".startsWith(prefix) && VariableScope.CLASS_CLASS_NODE.equals(type)) {
            FieldNode field = new FieldNode("class",
                Flags.AccPublic | Flags.AccStatic | Flags.AccFinal,
                VariableScope.CLASS_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE, null);
            field.setDeclaringClass(VariableScope.OBJECT_CLASS_NODE);
            proposals.add(new GroovyFieldProposal(field));
        }

        if (currentScope != null) {
            ClassNode enclosingTypeDeclaration = currentScope.getEnclosingTypeDeclaration();
            if (enclosingTypeDeclaration != null && firstTime && isPrimary && type.getModule() != null) {
                findStaticImportProposals(proposals, prefix, type.getModule());
                findStaticFavoriteProposals(proposals, prefix, type.getModule());
                demoteLowVisibilityProposals(proposals, type); // de-emphasize other's secrets
            }
        }

        // remove proposals for synthetic members
        for (Iterator<IGroovyProposal> it = proposals.iterator(); it.hasNext();) {
            IGroovyProposal proposal = it.next();
            if (proposal instanceof GroovyFieldProposal) {
                if (GroovyUtils.isSynthetic(((GroovyFieldProposal) proposal).getField())) {
                    it.remove();
                }
            } else if (proposal instanceof GroovyMethodProposal) {
                if (GroovyUtils.isSynthetic(((GroovyMethodProposal) proposal).getMethod())) {
                    it.remove();
                }
            }
        }

        return proposals;
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
        for (Map.Entry<String, ImportNode> entry : module.getStaticImports().entrySet()) {
            String fieldName = entry.getValue().getFieldName();
            if (fieldName != null && matcher.test(prefix, fieldName)) {
                FieldNode field = entry.getValue().getType().getField(fieldName);
                if (field != null && field.isStatic()) {
                    proposals.add(new GroovyFieldProposal(field));
                }
            }
        }
        for (Map.Entry<String, ImportNode> entry : module.getStaticStarImports().entrySet()) {
            ClassNode type = entry.getValue().getType();
            if (type != null) {
                for (FieldNode field : type.getFields()) {
                    if (field.isStatic() && matcher.test(prefix, field.getName())) {
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
                for (FieldNode field : typeNode.getFields()) {
                    if (field.isStatic() && matcher.test(prefix, field.getName())) {
                        GroovyFieldProposal proposal = new GroovyFieldProposal(field);
                        proposal.setRequiredStaticImport(typeName + '.' + field.getName());
                        proposals.add(proposal);
                    }
                }
            } else {
                if (matcher.test(prefix, fieldName)) {
                    FieldNode field = typeNode.getField(fieldName);
                    if (field != null && field.isStatic()) {
                        GroovyFieldProposal proposal = new GroovyFieldProposal(field);
                        proposal.setRequiredStaticImport(favoriteStaticMember);
                        proposals.add(proposal);
                    }
                }
            }
        }
    }
}
