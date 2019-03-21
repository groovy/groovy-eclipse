/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.eclipse.codeassist.creators.CategoryProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.FieldProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.IProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.MethodProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

public abstract class AbstractGroovyCompletionProcessor implements IGroovyCompletionProcessor {

    private final ContentAssistContext context;
    private final SearchableEnvironment nameEnvironment;
    private final JavaContentAssistInvocationContext javaContext;

    public AbstractGroovyCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        this.context = context;
        this.javaContext = javaContext;
        this.nameEnvironment = nameEnvironment;
    }

    public final ContentAssistContext getContext() {
        return context;
    }

    public final SearchableEnvironment getNameEnvironment() {
        return nameEnvironment;
    }

    public final JavaContentAssistInvocationContext getJavaContext() {
        return javaContext;
    }

    protected IProposalCreator[] getAllProposalCreators() {
        return new IProposalCreator[] {
            new FieldProposalCreator(),
            new MethodProposalCreator(),
            new CategoryProposalCreator(),
        };
    }

    protected final GroovyCompletionProposal createProposal(int kind, int completionOffset) {
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(kind, completionOffset);
        proposal.setNameLookup(nameEnvironment.nameLookup);
        return proposal;
    }

    protected static boolean matches(String prefix, String candidate, boolean camelCaseMatch) {
        if (!camelCaseMatch) {
            return candidate.startsWith(prefix);
        }
        return SearchPattern.camelCaseMatch(prefix, candidate);
    }
}
