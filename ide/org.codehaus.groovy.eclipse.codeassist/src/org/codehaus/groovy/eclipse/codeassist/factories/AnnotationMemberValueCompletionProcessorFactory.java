/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.factories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.creators.AbstractProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.FieldProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.MethodProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.processors.AbstractGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.processors.IGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.processors.PackageCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.processors.TypeCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.TypeProposalInfo;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class AnnotationMemberValueCompletionProcessorFactory implements IGroovyCompletionProcessorFactory {

    @Override
    public IGroovyCompletionProcessor createProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {

        return new AbstractGroovyCompletionProcessor(context, javaContext, nameEnvironment) {

            @Override
            public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
                if (monitor == null) {
                    monitor = new NullProgressMonitor();
                }
                monitor.beginTask("Content assist in annotation", 3);

                List<ICompletionProposal> proposals = new ArrayList<>();
                String memberName = getPerceivedCompletionMember();
                boolean memberFound = !getAnnotation().getClassNode().getMethods(memberName).isEmpty();

                if (!memberFound || isImplicitValueExpression()) {
                    generateAnnotationMemberProposals(proposals);
                }
                monitor.worked(1);

                if (memberFound || (isImplicitValueSupported() && getAnnotation().getMembers().isEmpty())) {
                    generateAnnotationMemberValueProposals(proposals, memberName != null ? memberName : "value", monitor);
                }
                monitor.worked(1);

                if (AssistOptions.ENABLED.equals(javaContext.getProject().getOption(AssistOptions.OPTION_PerformDeprecationCheck, true))) {
                    for (Iterator<ICompletionProposal> it = proposals.iterator(); it.hasNext();) {
                        CompletionProposal proposal = extractProposal(it.next()); // for flags
                        if (proposal != null && Flags.isDeprecated(proposal.getFlags())) {
                            it.remove();
                        }
                    }
                }
                monitor.worked(1);

                return proposals;
            }

            private void generateAnnotationMemberProposals(List<ICompletionProposal> proposals) {
                Set<String> currentMembers = getAnnotation().getMembers().keySet();
                if (isImplicitValueExpression()) {
                    currentMembers = new HashSet<>(currentMembers);
                    currentMembers.remove("value");
                }

                // generate member proposals by looking for method proposals
                MethodProposalCreator methodProposalCreator = initProposalCreator(new MethodProposalCreator());
                List<IGroovyProposal> candidates = methodProposalCreator.findAllProposals(
                    getAnnotation().getClassNode(), // completion type
                    Collections.emptySet(), // categories
                    context.completionExpression,
                    false, // isStatic
                    true); // isPrimary
                // generate proposals for EqualsAndHashCode, ToString, etc. for collector types like Canonical
                getMetaAnnotations().forEach(meta -> candidates.addAll(methodProposalCreator.findAllProposals(
                    meta.getClassNode(),
                    Collections.emptySet(),
                    context.completionExpression,
                    false,
                    true))
                );

                for (IGroovyProposal candidate : candidates) {
                    if (candidate instanceof GroovyMethodProposal) {
                        GroovyMethodProposal gmp = (GroovyMethodProposal) candidate;
                        String src = gmp.getMethod().getDeclaringClass().getName();
                        // screen for members of Annotation, Object, or existing member-value pairs
                        if (!"java.lang.Object".equals(src) &&
                            !"java.lang.annotation.Annotation".equals(src) &&
                            !currentMembers.contains(gmp.getMethod().getName())) {

                            proposals.add(gmp.createJavaProposal(context, javaContext));
                        }
                    }
                }
            }

            private void generateAnnotationMemberValueProposals(List<ICompletionProposal> proposals, String memberName, IProgressMonitor monitor) {
                ModuleNodeInfo moduleInfo = context.unit.getModuleInfo(true);
                MethodNode member = getAnnotation().getClassNode().getMethod(memberName, Parameter.EMPTY_ARRAY);

                // generate type and package proposals which may lead to suitable constants
                IGroovyCompletionProcessor[] processors = {
                    new TypeCompletionProcessor(context, javaContext, nameEnvironment),
                    new PackageCompletionProcessor(context, javaContext, nameEnvironment),
                };
                SubMonitor submon = SubMonitor.convert(monitor, processors.length + 1);
                for (IGroovyCompletionProcessor processor : processors) {
                    if (processor instanceof ITypeResolver) {
                        ((ITypeResolver) processor).setResolverInformation(moduleInfo.module, moduleInfo.resolver);
                    }
                    proposals.addAll(processor.generateProposals(submon.split(1)));
                }

                // generate field proposals from the current scope and (if applicable) enum constants
                FieldProposalCreator fieldProposalCreator = initProposalCreator(new FieldProposalCreator());
                fieldProposalCreator.setFavoriteStaticMembers(context.getFavoriteStaticMembers());
                fieldProposalCreator.setCurrentScope(context.getPerceivedCompletionScope());

                List<ClassNode> completionTypes = new ArrayList<>(2);
                if (context.containingDeclaration instanceof ClassNode) {
                    completionTypes.add((ClassNode) context.containingDeclaration);
                } else if (context.containingDeclaration.getDeclaringClass() != null) {
                    completionTypes.add(context.containingDeclaration.getDeclaringClass());
                }
                ClassNode memberType = (member != null ? member.getReturnType() : ClassHelper.VOID_TYPE);
                if (memberType.isArray()) memberType = memberType.getComponentType();
                if (memberType.isEnum()) {
                    completionTypes.add(memberType);
                    if (context.fullCompletionExpression.isEmpty()) {
                        proposals.add(newEnumTypeProposal(memberType));
                    }
                }

                List<IGroovyProposal> groovyProposals = new ArrayList<>();
                for (ClassNode completionType : completionTypes) {
                    groovyProposals.addAll(fieldProposalCreator.findAllProposals(
                        completionType, Collections.emptySet(), context.completionExpression, true, true));
                }
                for (IGroovyProposal groovyProposal : groovyProposals) {
                    if (groovyProposal instanceof GroovyFieldProposal) {
                        FieldNode fieldNode = ((GroovyFieldProposal) groovyProposal).getField();
                        if (fieldNode.isStatic() && fieldNode.isFinal() && memberType.equals(fieldNode.getType())) { String declTypeName;
                            // add static import reference for enum fields or references within type annotations (a.k.a. outside the declaring scope)
                            if ((fieldNode.isEnum() || (fieldNode.getDeclaringClass().equals(context.containingDeclaration) && isTypeAnnotation())) &&
                                    isNotStaticImported(fieldNode.getName(), declTypeName = fieldNode.getDeclaringClass().getName().replace('$', '.'))) {
                                String staticImport = (declTypeName + '.' + fieldNode.getName());
                                ((GroovyFieldProposal) groovyProposal).setRequiredStaticImport(staticImport);
                            }
                            proposals.add(groovyProposal.createJavaProposal(context, javaContext));
                        }
                    }
                }
            }

            private AnnotationNode getAnnotation() {
                return (AnnotationNode) context.containingCodeBlock;
            }

            private List<AnnotationNode> getMetaAnnotations() {
                List<AnnotationNode> act = getAnnotation().getNodeMetaData("AnnotationCollectorTransform");
                return act != null ? act : Collections.emptyList();
            }

            private String getPerceivedCompletionMember() {
                String maybe = null;

                for (Map.Entry<String, Expression> member : getAnnotation().getMembers().entrySet()) {
                    Expression value = member.getValue();
                    if (value.getStart() < context.completionLocation && context.completionLocation <= value.getEnd()) {
                        return member.getKey();
                    }
                    if (value.getStart() > context.completionLocation) {
                        break;
                    }
                    if (value.getLineNumber() == -1) {
                        maybe = member.getKey();
                    }
                }

                return maybe;
            }

            private <T extends AbstractProposalCreator> T initProposalCreator(T proposalCreator) {
                AssistOptions options = new AssistOptions(javaContext.getProject().getOptions(true));
                proposalCreator.setNameMatchingStrategy((String pattern, String candidate) -> {
                    return ProposalUtils.matches(pattern, candidate, options.camelCaseMatch, options.substringMatch);
                });
                return proposalCreator;
            }

            /**
             * If only one expression exists for annotation, "value" member can be implicit.
             */
            private boolean isImplicitValueExpression() {
                final boolean[] result = new boolean[1];

                AnnotationNode annotation = getAnnotation();
                Expression valueExpression = annotation.getMember("value");
                if (valueExpression != null && annotation.getMembers().keySet().size() == 1) {
                    valueExpression.visit(new DepthFirstVisitor() {
                        @Override
                        protected void visitExpression(Expression expression) {
                            if (expression == context.completionNode) {
                                result[0] = true;
                            }
                            super.visitExpression(expression);
                        }
                    });

                    if (result[0] && (valueExpression.getStart() - annotation.getClassNode().getEnd()) >= 7/*"(value=".length()*/) {
                        // try to discriminate between "@A(_)" and "@A(value=_)"
                        int offset = annotation.getClassNode().getEnd(), length = valueExpression.getStart() - offset;
                        String source = String.valueOf(context.unit.getContents(), offset, length);
                        result[0] = !Pattern.compile("\\bvalue\\s*=").matcher(source).find();
                    }
                }

                return result[0];
            }

            private boolean isImplicitValueSupported() {
                MethodNode valueMember = getAnnotation().getClassNode().getMethod("value", Parameter.EMPTY_ARRAY);
                return (valueMember != null);
            }

            private boolean isNotStaticImported(String memberName, String declaringTypeName) {
                ModuleNode moduleNode = context.unit.getModuleNode();
                if (moduleNode.getStaticImports().containsKey(memberName)) {
                    ImportNode importNode = moduleNode.getStaticImports().get(memberName);
                    return !importNode.getClassName().equals(declaringTypeName);
                }
                if (moduleNode.getStaticStarImports().containsKey(declaringTypeName)) {
                    return false;
                }
                return true;
            }

            private boolean isTypeAnnotation() {
                if (context.containingDeclaration instanceof ClassNode) {
                    return (getAnnotation().getEnd() < ((ClassNode) context.containingDeclaration).getNameStart());
                }
                return false;
            }

            private ICompletionProposal newEnumTypeProposal(ClassNode enumType) {
                String signature = GroovyUtils.getTypeSignatureWithoutGenerics(enumType, true, true);

                CompletionProposal proposal = CompletionProposal.create(CompletionProposal.TYPE_REF, 0);
                proposal.setSignature(signature.toCharArray());
                proposal.setFlags(enumType.getModifiers());

                JavaTypeCompletionProposal javaProposal = new JavaTypeCompletionProposal(
                    enumType.getName(),
                    context.unit,
                    context.completionLocation, 0,
                    ProposalUtils.getImage(proposal),
                    ProposalUtils.createDisplayString(proposal),
                    Relevance.MEDIUM.getRelevance(),
                    enumType.getName(),
                    javaContext
                );

                javaProposal.setProposalInfo(new TypeProposalInfo(javaContext.getProject(), proposal));

                return javaProposal;
            }
        };
    }
}
