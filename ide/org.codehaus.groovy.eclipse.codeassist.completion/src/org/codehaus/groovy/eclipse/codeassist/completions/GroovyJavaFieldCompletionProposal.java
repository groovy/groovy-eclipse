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
package org.codehaus.groovy.eclipse.codeassist.completions;

import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.FieldProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

public class GroovyJavaFieldCompletionProposal extends JavaCompletionProposal {

    public GroovyJavaFieldCompletionProposal(CompletionProposal proposal, StyledString displayString, JavaContentAssistInvocationContext javaContext) {
        super(String.valueOf(proposal.getCompletion()), // replacementString
            proposal.getReplaceStart(), // replacementOffset
            proposal.getReplaceEnd() - proposal.getReplaceStart(), // replacementLength
            ProposalUtils.getImage(proposal),
            displayString,
            proposal.getRelevance(),
            false, // inJavadoc
            javaContext);

        setProposalInfo(new FieldProposalInfo(javaContext.getProject(), proposal));
        setTriggerCharacters(ProposalUtils.VAR_TRIGGER);
    }
}
