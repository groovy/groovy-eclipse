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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;

public class ChangeOverriddenModifierResolver extends AbstractQuickFixResolver {

    protected ChangeOverriddenModifierResolver(QuickFixProblemContext problem) {
        super(problem);
    }

    @Override
    protected ProblemType[] getTypes() {
        return new ProblemType[] {ProblemType.FINAL_METHOD_OVERRIDE, ProblemType.WEAKER_ACCESS_OVERRIDE};
    }

    @Override
    public List<IJavaCompletionProposal> getQuickFixProposals() {
        /* TODO: Other 'kind' options:
        case IProblem.InheritedMethodReducesVisibility:
        case IProblem.MethodReducesVisibility:
        case IProblem.OverridingNonVisibleMethod:
            ModifierCorrectionSubProcessor.TO_VISIBLE
        case IProblem.FinalMethodCannotBeOverridden:
            ModifierCorrectionSubProcessor.TO_NON_FINAL
        case IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod:
            ModifierCorrectionSubProcessor.TO_NON_STATIC
         */
        QuickFixProblemContext context = getQuickFixProblem();
        try {
            int kind;
            switch (context.getProblemDescriptor().getType()) {
            case FINAL_METHOD_OVERRIDE:
                kind = ModifierCorrectionSubProcessor.TO_NON_FINAL;
                break;
            case WEAKER_ACCESS_OVERRIDE:
                kind = ModifierCorrectionSubProcessor.TO_VISIBLE;
                break;
            default:
                throw new IllegalStateException("Unsupported problem type: " + context.getProblemDescriptor().getType().name());
            }

            List<ICommandAccess> commands = new ArrayList<>();
            ModifierCorrectionSubProcessor.addChangeOverriddenModifierProposal(context.getContext(), context.getLocation(), commands, kind);
            return List.class.cast(commands.stream().filter(command -> command instanceof IJavaCompletionProposal).collect(Collectors.toList()));
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }
    }
}
