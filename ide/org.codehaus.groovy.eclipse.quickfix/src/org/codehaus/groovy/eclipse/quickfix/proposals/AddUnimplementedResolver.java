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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.ui.text.correction.LocalCorrectionsSubProcessor;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;

public class AddUnimplementedResolver extends AbstractQuickFixResolver {

    protected AddUnimplementedResolver(QuickFixProblemContext problem) {
        super(problem);
    }

    @Override
    protected ProblemType[] getTypes() {
        return new ProblemType[] {ProblemType.UNIMPLEMENTED_METHODS_TYPE};
    }

    @Override
    public List<IJavaCompletionProposal> getQuickFixProposals() {
        List<ICommandAccess> commands = new ArrayList<>(2);
        LocalCorrectionsSubProcessor.addUnimplementedMethodsProposals(
            getQuickFixProblem().getContext(), getQuickFixProblem().getLocation(), commands);

        List<IJavaCompletionProposal> proposals = new ArrayList<>(2);
        for (ICommandAccess command : commands) {
            if (command instanceof IJavaCompletionProposal) {
                proposals.add((IJavaCompletionProposal) command);
            }
        }
        return proposals;
    }
}
