/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.internal.ui.text.correction.ICommandAccess;
import org.eclipse.jdt.internal.ui.text.correction.LocalCorrectionsSubProcessor;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 * 
 * @author Andrew Eisenberg
 * @created Oct 14, 2011
 */
public class AddUnimplementedResolver extends AbstractQuickFixResolver {

    protected AddUnimplementedResolver(QuickFixProblemContext problem) {
        super(problem);
    }

    public List<IJavaCompletionProposal> getQuickFixProposals() {
        
        Collection<ICommandAccess> proposals = new ArrayList<ICommandAccess>(2);
        LocalCorrectionsSubProcessor.addUnimplementedMethodsProposals(
                getQuickFixProblem().getContext(), getQuickFixProblem().getLocation(), proposals);
        List<IJavaCompletionProposal> newProposals = new ArrayList<IJavaCompletionProposal>();
        for (ICommandAccess command : proposals) {
            if (command instanceof IJavaCompletionProposal) {
                newProposals.add((IJavaCompletionProposal) command);
            }
        }
        return newProposals;
    }

    @Override
    protected ProblemType[] getTypes() {
        return new ProblemType[] { ProblemType.UNIMPLEMENTED_METHODS_TYPE };
    }

}
