/*
 * Copyright 2009-2020 the original author or authors.
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.processors.IGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.processors.TypeCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Groovy annotation declarations that are annotated with {@literal @}AnnotationCollector
 * are transformed into classes.  So binary types that have this annotation need some help
 * to show up as completion proposals.
 */
public class AnnotationCollectorTypeCompletionProcessorFactory implements IGroovyCompletionProcessorFactory {

    @Override
    public IGroovyCompletionProcessor createProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {

        return new TypeCompletionProcessor(context, javaContext, nameEnvironment) {
            @Override
            public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
                List<ICompletionProposal> proposals = super.generateProposals(monitor);
                if ((monitor != null && monitor.isCanceled()) || proposals.isEmpty()) {
                    return Collections.emptyList();
                }

                // remove types that are not annotated with the AnnotationCollector transform
                for (Iterator<ICompletionProposal> it = proposals.iterator(); it.hasNext();) {
                    ICompletionProposal proposal = it.next();
                    if (!isAnnotationCollectorType(proposal)) {
                        it.remove();
                    } else {
                        // change the displayed icon from 'C' to '@'
                        GroovyCompletionProposal gcp = ReflectionUtils.getPrivateField(LazyJavaCompletionProposal.class, "fProposal", proposal);
                        gcp.setFlags(gcp.getFlags() | Flags.AccAnnotation);
                    }
                }

                return proposals;
            }

            @Override
            protected boolean doTypeCompletion(ContentAssistContext context, String expression) {
                return !expression.isEmpty() && super.doTypeCompletion(context, expression);
            }

            @Override
            protected int getSearchFor() {
                return IJavaSearchConstants.CLASS;
            }

            protected boolean isAnnotationCollectorType(ICompletionProposal proposal) {
                if (proposal instanceof LazyJavaTypeCompletionProposal) {
                    LazyJavaTypeCompletionProposal p = (LazyJavaTypeCompletionProposal) proposal;
                    try {
                        IType t = javaContext.getCompilationUnit().getJavaProject().findType(p.getQualifiedTypeName());
                        if (t instanceof BinaryType) {
                            for (IAnnotation a : ((BinaryType) t).getAnnotations()) {
                                if (a.getElementName().equals("groovy.transform.AnnotationCollector")) {
                                    return true;
                                }
                            }
                        }
                    } catch (JavaModelException e) {
                        GroovyContentAssist.logError("Failed to check for @AnnotationCollector in: " + p.getQualifiedTypeName(), e);
                    }
                }
                return false;
            }
        };
    }
}
