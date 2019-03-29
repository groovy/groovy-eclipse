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

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.ui.text.java.FieldProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuesser;
import org.eclipse.jdt.internal.ui.text.template.contentassist.PositionBasedCompletionProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

/**
 * A delegate to {@link ParameterGuesser}. Wraps calls to the other class and
 * does some extra work to make the parameter guesses slightly more groovy.
 */
public class ParameterGuesserDelegate {

    private final ParameterGuesser guesser;
    private final JavaContentAssistInvocationContext invocationContext;

    public ParameterGuesserDelegate(IJavaElement enclosingElement, JavaContentAssistInvocationContext invocationContext) {
        guesser = new ParameterGuesser(enclosingElement);
        this.invocationContext = invocationContext;
    }

    public ICompletionProposal[] parameterProposals(String parameterType, String paramName, Position position, IJavaElement[] suggestions, boolean fillBestGuess) {
        try {
            ICompletionProposal[] completions = guesser.parameterProposals(parameterType, paramName, position, suggestions, fillBestGuess, false);
            if (completions != null && completions.length > 0 && suggestions != null && suggestions.length > 0) {
                IType declaring = (IType) suggestions[suggestions.length - 1].getAncestor(IJavaElement.TYPE);
                if (declaring != null && declaring.isEnum()) {
                    boolean preferStaticImport = isStaticImportPreferred();

                    // NOTE: completions and suggestions are not parallel arrays
                    for (int i = 0; i < completions.length; i += 1) {
                        ICompletionProposal completion = completions[i];

                        IJavaElement suggestion = null;
                        for (int j = suggestions.length - 1; j >= 0; j -= 1) {
                            if (suggestions[j].getElementType() == IJavaElement.FIELD &&
                                    suggestions[j].getElementName().equals(completion.getDisplayString())) {
                                suggestion = suggestions[j];
                                break;
                            }
                        }

                        if (suggestion != null) {
                            CompletionProposal supporting;
                            String replacement = completion.getDisplayString();

                            if (preferStaticImport) {
                                supporting = CompletionProposal.create(CompletionProposal.FIELD_IMPORT, 0);
                                supporting.setAdditionalFlags(CompletionFlags.StaticImport);
                                supporting.setDeclarationSignature(Signature.createTypeSignature(declaring.getFullyQualifiedName(), true).toCharArray());
                                supporting.setName(replacement.toCharArray());
                            } else {
                                supporting = CompletionProposal.create(CompletionProposal.TYPE_IMPORT, 0);
                                supporting.setSignature(Signature.createTypeSignature(declaring.getFullyQualifiedName(), true).toCharArray());
                            }

                            completions[i] = newEnumProposal(position, replacement, supporting,
                                completion.getImage(), ((PositionBasedCompletionProposal) completion).getTriggerCharacters());
                        }
                    }
                }
            }
            return addExtras(completions, parameterType, position);

        } catch (Exception e) {
            GroovyContentAssist.logError(e);
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
                if (VariableScope.CLOSURE_CLASS_NODE.getName().equals(expectedType) &&
                        GroovyContentAssist.getDefault().getPreferenceStore().getBoolean(GroovyContentAssist.CLOSURE_BRACKETS)) {
                    for (int i = (parameterProposals.length - 1); i > 0; i -=1) {
                        parameterProposals[i] = parameterProposals[i - 1];
                    }
                    parameterProposals[0] = proposal;
                } else {
                    parameterProposals[parameterProposals.length - 1] = proposal;
                }
            } else {
                parameterProposals = (ICompletionProposal[]) ArrayUtils.add(parameterProposals, proposal);
            }
        }
        return parameterProposals;
    }

    private boolean isStaticImportPreferred() {
        if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEASSIST_ADDIMPORT)) {
            return new AssistOptions(invocationContext.getProject().getOptions(true)).suggestStaticImport;
        }
        return false;
    }

    private ICompletionProposal newEnumProposal(Position position, String replacement, CompletionProposal supporting, Image image, char[] triggers) {
        CompletionProposal groovyProposal = CompletionProposal.create(CompletionProposal.METHOD_REF, 0);
        groovyProposal.setRequiredProposals(new CompletionProposal[] {supporting});

        // replacement offset and length are not known at this time, so they must be supplied from Position upon request
        JavaCompletionProposal javaProposal = new JavaCompletionProposal(replacement, 0, 0, image, null, 1, false, invocationContext) {
            private int getInitialGuessLength() {
                return fInvocationContext.getViewer().getSelectedRange().y;
            }
            @Override
            public int getReplacementLength() {
                return position.getLength();
            }
            @Override
            public int getReplacementOffset() {
                return position.getOffset();
            }
            @Override
            public void setReplacementOffset(int offset) {
                if (offset > position.getOffset() && position.getLength() > getInitialGuessLength()) {
                    offset = position.getOffset() + (position.getLength() - getInitialGuessLength());
                    // advance replacement offset to account for inserted qualifier and reset length
                    position.setOffset(offset); position.setLength(getInitialGuessLength());
                }
            }
        };
        javaProposal.setProposalInfo(new FieldProposalInfo(invocationContext.getProject(), groovyProposal));
        javaProposal.setTriggerCharacters(triggers);
        return javaProposal;
    }
}
