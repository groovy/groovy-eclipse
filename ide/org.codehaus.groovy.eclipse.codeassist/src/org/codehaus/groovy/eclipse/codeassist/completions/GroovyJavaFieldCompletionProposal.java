/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.completions;

import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.compiler.CharOperation;
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
        fPrefix = getPrefix(javaContext.getDocument(), javaContext.getInvocationOffset());
    }

    @Override
    public char[] getTriggerCharacters() {
        char[] triggerCharacters;
        // in case of auto-activation, remove '.' trigger to allow typing range
        if (fPrefix.isEmpty() && ProposalUtils.isContentAssistAutoActiavted()) {
            triggerCharacters = ProposalUtils.getContentAssistContext(fInvocationContext).map(context -> {
                // check for completion like "0." or "foo." as candidate for range
                String q = context.getQualifiedCompletionExpression();
                if (q.endsWith(".") && q.indexOf('.') == q.lastIndexOf('.')) {
                    return CharOperation.remove(ProposalUtils.VAR_TRIGGER, '.');
                }
                return (char[]) null;
            }).orElse(ProposalUtils.VAR_TRIGGER);
        } else {
            triggerCharacters = ProposalUtils.VAR_TRIGGER;
        }
        setTriggerCharacters(triggerCharacters);
        return super.getTriggerCharacters();
    }

    @Override
    protected boolean isPrefix(String prefix, String string) {
        fPrefix = prefix; return super.isPrefix(prefix, string);
    }

    protected String fPrefix;
}
