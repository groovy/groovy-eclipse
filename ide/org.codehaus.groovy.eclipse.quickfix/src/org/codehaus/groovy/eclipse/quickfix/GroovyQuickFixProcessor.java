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
package org.codehaus.groovy.eclipse.quickfix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.codehaus.groovy.eclipse.quickfix.proposals.GroovyQuickFixResolverRegistry;
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemDescriptor;
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemType;
import org.codehaus.groovy.eclipse.quickfix.proposals.QuickFixProblemContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

/**
 * Integrates Groovy proposals into JDT quick fix framework.
 */
public class GroovyQuickFixProcessor implements IQuickFixProcessor {

    @Override
    public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
        return GroovyQuickFixPlugin.isGroovyProject(unit) && ProblemType.isRecognizedProblemId(problemId);
    }

    @Override
    public IJavaCompletionProposal[] getCorrections(final IInvocationContext context, final IProblemLocation[] locations) throws CoreException {
        if (!GroovyQuickFixPlugin.isGroovyProject(context) || locations == null || locations.length == 0) {
            return new IJavaCompletionProposal[0];
        }

        List<IJavaCompletionProposal> proposals = new ArrayList<>();
        for (IProblemLocation location : locations) {
            ProblemDescriptor descriptor = getProblemDescriptor(location.getProblemId(), location.getMarkerType(), location.getProblemArguments());
            if (descriptor != null) {
                List<IQuickFixResolver> resolvers = new GroovyQuickFixResolverRegistry(
                    new QuickFixProblemContext(descriptor, context, location)).getQuickFixResolvers();
                if (resolvers != null && !resolvers.isEmpty()) {
                    for (IQuickFixResolver resolver : resolvers) {
                        proposals.addAll(Optional.ofNullable(resolver.getQuickFixProposals()).orElseGet(Collections::emptyList));
                    }
                }
            }
        }
        return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
    }

    //@VisibleForTesting
    public ProblemDescriptor getProblemDescriptor(final int problemId, final String markerType, final String[] problemArgs) {
        ProblemType type = ProblemType.getProblemType(problemId, markerType, problemArgs);
        if (type != null) {
            return new ProblemDescriptor(type, problemArgs);
        }
        return null;
    }
}
