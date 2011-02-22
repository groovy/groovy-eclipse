/*
 * Copyright 2010 the original author or authors.
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

package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.proposals.AbstractGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

/**
 * @author Romain Bioteau
 * Filters completion proposals displayed by the groovy editor content assistant.
 * Contributions to the <tt>org.codehaus.groovy.eclipse.codeassist.completion.ProposalFilter</tt> extension point
 * must implement this interface.
*/
public interface IProposalFilter {

    /**
     * Filter a list of IGroovyProposal <br>
     * - Remove undesired (duplicate) entries <br>
     * - Augment the relevance of certain proposals <br>
     * - Supplement existing entries with additional information<br>
     *
     * Note that if you want to augment the relevance of a given proposal, you
     * must cast the proposal to {@link AbstractGroovyProposal} and call the
     * method {@link AbstractGroovyProposal#setRelevanceMultiplier(float)}. Look
     * at the {@link Relevance} enumeration for how relevance is calculated.
     *
     * @param proposals
     *            The List of proposals
     * @return The filtered list of proposals as List
     */
    public List<IGroovyProposal> filterProposals(
            List<IGroovyProposal> proposals, ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext);
}
