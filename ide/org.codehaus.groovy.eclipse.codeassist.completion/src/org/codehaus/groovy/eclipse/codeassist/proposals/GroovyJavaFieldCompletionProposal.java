/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 *
 * @author Andrew Eisenberg
 * @created Dec 8, 2009
 */
public class GroovyJavaFieldCompletionProposal extends JavaCompletionProposal {

    private final CompletionProposal proposal;
    public GroovyJavaFieldCompletionProposal(CompletionProposal proposal, Image image, StyledString displayString) {
        super(String.valueOf(proposal.getName()), proposal.getReplaceStart(),
                proposal.getReplaceEnd()-proposal.getReplaceStart(),
                image, displayString, proposal.getRelevance());
        this.proposal = proposal;
        this.setRelevance(proposal.getRelevance());
        this.setTriggerCharacters(ProposalUtils.VAR_TRIGGER);
    }

    public CompletionProposal getProposal() {
        return proposal;
    }
}
