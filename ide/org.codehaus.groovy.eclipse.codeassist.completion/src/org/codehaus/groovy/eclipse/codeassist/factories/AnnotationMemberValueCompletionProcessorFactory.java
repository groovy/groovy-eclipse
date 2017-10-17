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
package org.codehaus.groovy.eclipse.codeassist.factories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.creators.MethodProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.processors.AbstractGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.processors.IGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class AnnotationMemberValueCompletionProcessorFactory implements IGroovyCompletionProcessorFactory {

    public IGroovyCompletionProcessor createProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {

        return new AbstractGroovyCompletionProcessor(context, javaContext, nameEnvironment) {

            public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
                if (monitor == null) {
                    monitor = new NullProgressMonitor();
                }
                monitor.beginTask("Assist in annotation", 2);
                try {
                    List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

                    generateAnnotationAttributeProposals(proposals);
                    monitor.worked(1);

                //VariableExpression

                    // annotation values can be found as static final fields

                    // if (value type is class or class[]) types

                    return proposals;
                } finally {
                    monitor.done();
                }
            }

            protected void generateAnnotationAttributeProposals(List<ICompletionProposal> proposals) {
                ContentAssistContext context = getContext();
                AnnotationNode annotation = (AnnotationNode) context.containingCodeBlock;

                Set<String> currentMembers = annotation.getMembers().keySet();
                if (isCompletionExpressionBeneathValueExpression(annotation)) {
                    currentMembers = new HashSet<String>(currentMembers);
                    currentMembers.remove("value");
                }

                // generate member proposals by looking for method proposals
                MethodProposalCreator mpc = new MethodProposalCreator();
                mpc.setCurrentScope(context.currentScope);
                List<IGroovyProposal> candidates = mpc.findAllProposals(
                    annotation.getClassNode(),
                    Collections.<ClassNode>emptySet(), // no categories
                    context.completionExpression,
                    false, // isStatic
                    true); // isPrimary

                for (IGroovyProposal candidate : candidates) {
                    if (candidate instanceof GroovyMethodProposal) {
                        GroovyMethodProposal gmp = (GroovyMethodProposal) candidate;
                        String src = gmp.getMethod().getDeclaringClass().getName();
                        // screen for members of Annotation, Object, or existing member-value pairs
                        if (!src.equals("java.lang.Object") && !src.equals("java.lang.annotation.Annotation") && !currentMembers.contains(gmp.getMethod().getName())) {
                            proposals.add(gmp.createJavaProposal(context, getJavaContext()));
                        }
                    }
                }
            }

            /**
             * If only one expression exists for annotation, "value" member can be implicit.
             */
            protected final boolean isCompletionExpressionBeneathValueExpression(AnnotationNode annotation) {
                final boolean[] result = new boolean[1];

                Expression valueExpression = annotation.getMember("value");
                if (valueExpression != null && annotation.getMembers().keySet().size() == 1) {
                    valueExpression.visit(new DepthFirstVisitor() {
                        @Override
                        protected void visitExpression(Expression expression) {
                            if (expression == getContext().completionNode) {
                                result[0] = true;
                            }
                            super.visitExpression(expression);
                        }
                    });
                }

                return result[0];
            }
        };
    }
}
