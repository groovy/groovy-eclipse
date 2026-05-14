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
package org.codehaus.groovy.eclipse.codeassist.mock;

import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.processors.IProposalFilter;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

public class MockProposalFilter2 implements IProposalFilter {

    private static boolean filterCalled;

    @Override
    public List<IGroovyProposal> filterProposals(final List<IGroovyProposal> proposals,
            final ContentAssistContext context, final JavaContentAssistInvocationContext javaContext) {
        filterCalled = true;
        return proposals;
    }

    public static boolean wasFilterCalled() {
        return filterCalled;
    }

    public static void reset() {
        filterCalled = false;
    }
}
