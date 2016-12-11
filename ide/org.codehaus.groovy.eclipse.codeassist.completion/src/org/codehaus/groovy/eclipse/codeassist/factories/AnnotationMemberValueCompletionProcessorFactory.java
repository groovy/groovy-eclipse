/*
 * Copyright 2009-2016 the original author or authors.
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
import java.util.List;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.codeassist.creators.MethodProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.processors.AbstractGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.processors.IGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
                MethodProposalCreator mpc = new MethodProposalCreator();
                mpc.setCurrentScope(getContext().currentScope);
                List<IGroovyProposal> methodProposals = mpc.findAllProposals(
                    ((AnnotationNode) getContext().containingCodeBlock).getClassNode(),
                    Collections.<ClassNode>emptySet(),
                    getContext().completionExpression,
                    false, // isStatic
                    true); // isPrimary

                for (IGroovyProposal methodProposal : methodProposals) {
                    if (((GroovyMethodProposal) methodProposal).getMethod()
                            .getDeclaringClass().getName().equals("java.lang.annotation.Annotation")) {
                        continue;
                    }
                    proposals.add(methodProposal.createJavaProposal(getContext(), getJavaContext()));
                }
            }
        };
    }
}
