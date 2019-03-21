/*
 * Copyright 2009-2017 the original author or authors.
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

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuesser;
import org.eclipse.jdt.internal.ui.text.template.contentassist.PositionBasedCompletionProposal;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * A delegate to {@link ParameterGuesser}. Wraps calls to the other class and
 * does some extra work to make the parameter guesses slightly more groovy.
 */
public class ParameterGuesserDelegate {

    private ParameterGuesser guesser;

    public ParameterGuesserDelegate(IJavaElement enclosingElement) {
        guesser = new ParameterGuesser(enclosingElement);
    }

    public ICompletionProposal[] parameterProposals(String parameterType, String paramName, Position position, IJavaElement[] assignable, boolean fillBestGuess) {
        try {
            ICompletionProposal[] allCompletions = guesser.parameterProposals(parameterType, paramName, position, assignable, fillBestGuess, false);

            // ensure enum proposals insert the declaring type as part of the name
            if (allCompletions != null && allCompletions.length > 0 && assignable != null && assignable.length > 0) {
                IType declaring = (IType) assignable[0].getAncestor(IJavaElement.TYPE);
                if (declaring != null && declaring.isEnum()) {
                    // each enum is proposed twice; the first with the qualified name and the second with the simple name
                    boolean useFull = true;
                    for (int i = 0; i < assignable.length && i < allCompletions.length; i += 1) {
                        if (assignable[i].getElementType() == IJavaElement.FIELD) {
                            if (useFull) {
                                String newReplacement = declaring.getElementName() + '.' + assignable[i].getElementName();
                                ReflectionUtils.setPrivateField(PositionBasedCompletionProposal.class, "fDisplayString", allCompletions[i], newReplacement);
                                ReflectionUtils.setPrivateField(PositionBasedCompletionProposal.class, "fReplacementString", allCompletions[i], newReplacement);
                                useFull = false;
                            } else {
                                useFull = true;
                            }
                        }
                    }
                }
            }
            return addExtras(allCompletions, parameterType, position);

        } catch (Exception e) {
            GroovyContentAssist.logError("Exception trying to reflectively invoke 'parameterProposals' method.", e);

            return ProposalUtils.NO_COMPLETIONS;
        }
    }

    /**
     * Adds extra parameter proposals that are groovy specific, eg- Empty strings
     * and empty closures.
     */
    private ICompletionProposal[] addExtras(ICompletionProposal[] parameterProposals, String expectedType, Position position) {
        ICompletionProposal proposal = null;
        if (VariableScope.BYTE_CLASS_NODE     .getName().equals(expectedType) ||
            VariableScope.CHARACTER_CLASS_NODE.getName().equals(expectedType) ||
            VariableScope.DOUBLE_CLASS_NODE   .getName().equals(expectedType) ||
            VariableScope.FLOAT_CLASS_NODE    .getName().equals(expectedType) ||
            VariableScope.INTEGER_CLASS_NODE  .getName().equals(expectedType) ||
            VariableScope.LONG_CLASS_NODE     .getName().equals(expectedType) ||
            VariableScope.SHORT_CLASS_NODE    .getName().equals(expectedType)) {

            proposal = new PositionBasedCompletionProposal("0", position, 1);

        } else if (VariableScope.BOOLEAN_CLASS_NODE.getName().equals(expectedType)) {

            proposal = new PositionBasedCompletionProposal("false", position, 5);
            parameterProposals = (ICompletionProposal[]) ArrayUtils.add(parameterProposals, parameterProposals.length - 1, proposal);
            proposal = new PositionBasedCompletionProposal("true", position, 4);

        } else if (VariableScope.STRING_CLASS_NODE.getName().equals(expectedType)) {

            proposal = new PositionBasedCompletionProposal("\"\"", position, 1);

        } else if (VariableScope.CLOSURE_CLASS_NODE.getName().equals(expectedType)) {

            proposal = new PositionBasedCompletionProposal("{  }", position, 2);
        }

        if (proposal != null) {
            if (parameterProposals[parameterProposals.length - 1].getDisplayString().equals("null")) {
                // replace 'null' proposal with simple value proposal
                parameterProposals[parameterProposals.length - 1] = proposal;
            } else {
                parameterProposals = (ICompletionProposal[]) ArrayUtils.add(parameterProposals, proposal);
            }
        }
        return parameterProposals;
    }
}
