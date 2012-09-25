/*
 * Copyright 2011 the original author or authors.
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

import java.lang.reflect.Method;

import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuesser;
import org.eclipse.jdt.internal.ui.text.template.contentassist.PositionBasedCompletionProposal;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * A delegate to {@link ParameterGuesser}. Used because e3.6 and e3.7+
 * use different variants of {@link ParameterGuesser}. This class wraps calls
 * to the other class and does some extra work to make the parameter guesses
 * slightly more groovy.
 *
 * @author andrew
 * @created Nov 24, 2011
 */
public class ParameterGuesserDelegate {
    private static final String CLOSURE_TEXT = "{  }";

    private static final String EMPTY_STRING = "\"\"";

    private static final String NULL_TEXT = "null";

    private ParameterGuesser guesser;

    public ParameterGuesserDelegate(IJavaElement enclosingElement) {
        guesser = new ParameterGuesser(enclosingElement);
    }

    // unfortunately, the parameterProposals method has a different signature in
    // 3.6 and 3.7.
    // so must call using reflection
    public ICompletionProposal[] parameterProposals(String parameterType, String paramName,
            Position position, IJavaElement[] assignable, boolean fillBestGuess) {
        parameterType = convertToPrimitive(parameterType);

        Method method = findParameterProposalsMethod();
        try {
            ICompletionProposal[] allCompletions;
            if (method.getParameterTypes().length == 5) {
                // 3.6
                allCompletions = (ICompletionProposal[]) method
                        .invoke(guesser, parameterType, paramName, position, assignable, fillBestGuess);
            } else {
                // 3.7 and later
                allCompletions = (ICompletionProposal[]) method.invoke(guesser, parameterType, paramName, position, assignable,
                        fillBestGuess, false);
            }

            // ensure enum proposals insert the declaring type as part of the
            // name.
            if (allCompletions != null && allCompletions.length > 0 && assignable != null && assignable.length > 0) {
                IType declaring = (IType) assignable[0].getAncestor(IJavaElement.TYPE);
                if (declaring != null && declaring.isEnum()) {
                    // each enum is proposed twice. The first time, use the
                    // qualified name and the second keep with the simple name
                    boolean useFull = true;
                    for (int i = 0; i < assignable.length && i < allCompletions.length; i++) {
                        if (assignable[i].getElementType() == IJavaElement.FIELD) {
                            if (useFull) {
                                String newReplacement = declaring.getElementName() + '.' + assignable[i].getElementName();
                                ReflectionUtils.setPrivateField(PositionBasedCompletionProposal.class, "fReplacementString",
                                        allCompletions[i], newReplacement);
                                ReflectionUtils.setPrivateField(PositionBasedCompletionProposal.class, "fDisplayString",
                                        allCompletions[i], newReplacement);
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
            GroovyCore.logException("Exception trying to reflectively invoke 'parameterProposals' method.", e);
            return ProposalUtils.NO_COMPLETIONS;
        }

    }

    private String convertToPrimitive(String parameterType) {
        if ("java.lang.Short".equals(parameterType)) { //$NON-NLS-1$
            return "short";
        }
        if ("java.lang.Integer".equals(parameterType)) { //$NON-NLS-1$
            return "int";
        }
        if ("java.lang.Long".equals(parameterType)) { //$NON-NLS-1$
            return "long";
        }
        if ("java.lang.Float".equals(parameterType)) { //$NON-NLS-1$
            return "float";
        }
        if ("java.lang.Double".equals(parameterType)) { //$NON-NLS-1$
            return "double";
        }
        if ("java.lang.Character".equals(parameterType)) { //$NON-NLS-1$
            return "char";
        }
        if ("java.lang.Byte".equals(parameterType)) { //$NON-NLS-1$
            return "byte";
        }
        if ("java.lang.Boolean".equals(parameterType)) { //$NON-NLS-1$
            return "boolean";
        }
        return parameterType;
    }

    private static Method parameterProposalsMethod;

    private static Method findParameterProposalsMethod() {
        if (parameterProposalsMethod == null) {
            try {
                // 3.6
                parameterProposalsMethod = ParameterGuesser.class.getMethod("parameterProposals", String.class, String.class,
                        Position.class, IJavaElement[].class, boolean.class);
            } catch (SecurityException e) {
                GroovyCore.logException("Exception trying to reflectively find 'parameterProposals' method.", e);
            } catch (NoSuchMethodException e) {
                // 3.7 RC4 or later
                try {
                    parameterProposalsMethod = ParameterGuesser.class.getMethod("parameterProposals", String.class, String.class,
                            Position.class, IJavaElement[].class, boolean.class, boolean.class);
                } catch (SecurityException e1) {
                    GroovyCore.logException("Exception trying to reflectively find 'parameterProposals' method.", e1);
                } catch (NoSuchMethodException e1) {
                    GroovyCore.logException("Exception trying to reflectively find 'parameterProposals' method.", e1);
                }
            }
        }
        return parameterProposalsMethod;
    }

    /**
     * Add extra parameter proposals that are groovy specific, eg- Empty strings
     * and empty closures
     *
     * @param parameterProposals
     * @return
     */
    private ICompletionProposal[] addExtras(ICompletionProposal[] parameterProposals, String expectedType, Position position) {
        ICompletionProposal proposal = null;
        if (expectedType.equals(VariableScope.STRING_CLASS_NODE.getName())) {
            proposal = new PositionBasedCompletionProposal(EMPTY_STRING, position, 1);
        } else if (expectedType.equals(VariableScope.CLOSURE_CLASS.getName())) {
            proposal = new PositionBasedCompletionProposal(CLOSURE_TEXT, position, 2);
        }

        if (proposal != null) {
            int origLen = parameterProposals.length;
            // make the extra proposal come before 'null'
            if (parameterProposals[origLen - 1].getDisplayString().equals(NULL_TEXT)) {
                parameterProposals[origLen - 1] = proposal;
            } else {
                ICompletionProposal[] newProps = new ICompletionProposal[origLen + 1];
                System.arraycopy(parameterProposals, 0, newProps, 0, origLen);
                parameterProposals = newProps;
                parameterProposals[origLen] = proposal;
            }
        }
        return parameterProposals;
    }

}
