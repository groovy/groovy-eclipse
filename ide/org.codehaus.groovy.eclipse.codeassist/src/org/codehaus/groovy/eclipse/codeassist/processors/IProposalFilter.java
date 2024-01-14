/*
 * Copyright 2009-2024 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

/**
 * Filters completion proposals displayed by the groovy editor content assistant.
 * Contributions to the <tt>org.codehaus.groovy.eclipse.codeassist.completionProposalFilter</tt>
 * extension point must implement this interface.
 */
public interface IProposalFilter {

    /**
     * Filters a list of IGroovyProposal to:
     * <ul>
     * <li> Remove undesired (duplicate) entries
     * <li> Augment the relevance of certain proposals
     * <li> Supplement existing entries with additional information
     * </ul>
     * Note that if you want to augment the relevance of a given proposal, you
     * must cast the proposal to {@link org.codehaus.groovy.eclipse.codeassist.proposals.AbstractGroovyProposal AbstractGroovyProposal}
     * and call the method {@link org.codehaus.groovy.eclipse.codeassist.proposals.AbstractGroovyProposal#setRelevanceMultiplier(float) setRelevanceMultiplier}.
     * Look at the {@link org.codehaus.groovy.eclipse.codeassist.relevance.Relevance Relevance} enumeration for how relevance is calculated.
     *
     * @return List of filtered proposals or {@code null} if no change
     */
    List<IGroovyProposal> filterProposals(List<IGroovyProposal> proposals, ContentAssistContext context, JavaContentAssistInvocationContext javaContext);
}
