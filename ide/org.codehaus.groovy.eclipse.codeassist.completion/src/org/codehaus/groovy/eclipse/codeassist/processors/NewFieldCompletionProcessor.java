 /*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.codeassist.CharArraySourceBuffer;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder.NameAndLocation;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.internal.core.InternalNamingConventions;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Adds a list of new field proposals. All field proposals are dynamically typed static or
 * non-static fields with an initializer of a closure. Contributors should extend the
 *
 *
 * @author Andrew Eisenberg
 * @created Nov 10, 2009
 */
public class NewFieldCompletionProcessor extends AbstractGroovyCompletionProcessor {
    public static class NewGroovyFieldCompletionProposal extends JavaCompletionProposal {
        NewGroovyFieldCompletionProposal(String fieldName,
                int replacementOffset, int replacementLength, int relevance,
                boolean isStatic, boolean useKeywordBeforeReplacement, String typeName) {
            super(createReplacementString(fieldName, typeName, isStatic),
                    replacementOffset, replacementLength,
                    createImage(isStatic), createDisplayString(fieldName, typeName,
                            isStatic, useKeywordBeforeReplacement), relevance);
        }

        // can we do better with the initializer?
        static String createReplacementString(String fieldName, String typeName, boolean isStatic) {
            if (isStatic) {
                if (typeName != null) {
                    return "static " + typeName + fieldName + " = null";
                } else {
                    return "static " + fieldName + " = null";
                }
            } else if (typeName != null) {
                return typeName + fieldName;
            } else {
                return "def " + fieldName;
            }
        }

        static Image createImage(boolean isStatic) {
            CompletionProposal dummy = CompletionProposal.create(CompletionProposal.FIELD_REF, -1);
            if (isStatic) {
                dummy.setFlags(Flags.AccStatic);
            }
            return ProposalUtils.getImage(dummy);
        }

        static StyledString createDisplayString(String fieldName, String typeName,
                boolean isStatic, boolean useKeywordBeforeReplacement) {
            StyledString ss = new StyledString();

            // use a different styled string depending on the completion context
            // if the context completion node is a field, then must include the field modifier
            // if not, then don't include it.
            // this is because the display string must match the replacement otherwise no
            // replacement occurs.
            if (useKeywordBeforeReplacement) {
                if (isStatic) {
                    ss.append("static ", StyledString
                            .createColorRegistryStyler(
                                    JFacePreferences.HYPERLINK_COLOR, null));
                    if (typeName != null) {
                        ss.append(typeName);
                    }
                } else {
                    if (typeName == null) {
                        ss.append("def ", StyledString.createColorRegistryStyler(JFacePreferences.HYPERLINK_COLOR, null));
                    } else {
                        ss.append(typeName);
                    }
                }
            }

            if (isStatic) {
                ss.append(fieldName);
                ss.append(" - New static property",
                        StyledString.QUALIFIER_STYLER);
            } else {
                ss.append(fieldName);
                ss.append(" - New property", StyledString.QUALIFIER_STYLER);
            }
            return ss;
        }
    }

    public NewFieldCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();
        List<String> unimplementedFieldNames = getAllSuggestedFieldNames(context);
        List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
        IType enclosingType = context.getEnclosingType();
        if (enclosingType != null) {
            for (String fieldName : unimplementedFieldNames) {
                proposals.add(createProposal(fieldName, context, enclosingType));
            }
            // next check to see if we are at a partially completed field
            // declaration,
            // ie- is the type specified, but the name is not (or is only
            // partially specified)?
            NameAndLocation nameAndLocation = findCompletionTypeName(context.unit, context.completionLocation);
            if (nameAndLocation != null) {
                String typeName = nameAndLocation.toTypeName();
                if (typeName.equals("def")) {
                    typeName = "value";
                }
                String[] suggestedNames = NamingConventions.suggestVariableNames(NamingConventions.VK_INSTANCE_FIELD,
                        InternalNamingConventions.BK_SIMPLE_TYPE_NAME, typeName, context.unit.getJavaProject(),
                        nameAndLocation.dims(), null, true);
                if (suggestedNames != null) {
                    for (String suggestedName : suggestedNames) {
                        if (suggestedName.startsWith(context.completionExpression)) {
                            proposals.add(createProposal(suggestedName, nameAndLocation.name, context, enclosingType, false, true,
                                    nameAndLocation.location, context.completionLocation - nameAndLocation.location));
                        }
                    }
                }
            }
        }

        return proposals;
    }

    /**
     * works backward from the current location to see if there is something
     * that looks like a type name as the previous token
     *
     * @param unit
     * @param completionLocation
     * @return
     */
    private NameAndLocation findCompletionTypeName(GroovyCompilationUnit unit, int completionLocation) {
        return new ExpressionFinder().findPreviousTypeNameToken(new CharArraySourceBuffer(unit.getContents()), completionLocation);
    }

    /**
     * @param context
     * @return
     */
    private List<String> getAllSuggestedFieldNames(ContentAssistContext context) {
        List<String> allNewFieldNames = new LinkedList<String>();
        try {
            List<IProposalProvider> providers = ProposalProviderRegistry.getRegistry().getProvidersFor(context.unit);
            for (IProposalProvider provider : providers) {
                List<String> newFieldNames = provider.getNewFieldProposals(context);
                if (newFieldNames != null) {
                    allNewFieldNames.addAll(newFieldNames);
                }
            }
        } catch (CoreException e) {
            GroovyCore.logException("Exception looking for proposal providers in " + context.unit.getElementName(), e);
        }

        return allNewFieldNames;
    }

    private ICompletionProposal createProposal(String fieldName,
            ContentAssistContext context, IType enclosingType) {
        boolean isStatic;
        if (fieldName.startsWith(IProposalProvider.NONSTATIC_FIELD)) {
            fieldName = fieldName.substring(IProposalProvider.NONSTATIC_FIELD
                    .length());
            isStatic = false;
        } else {
            isStatic = true;
        }

        // use keyword replacement if the def/static keyword is completely or partially present
        boolean useKeywordBeforeReplacement = context.completionExpression
                .length() > 0
                && ((context.completionNode instanceof FieldNode)
                        || "def".startsWith(context.completionExpression) || "static"
                        .startsWith(context.completionExpression));
        // replace start is either the start of the field node (if using keyword
        // replacement),
        // or it is the completion location - the length of the existing part of
        // the expression
        int replaceStart = context.completionNode instanceof FieldNode ? context.completionNode.getStart()
                : context.completionLocation - context.completionExpression.length();
        // the completion length is the length of the bit of text that will be
        // replaced
        // this is either the completion expression length or the difference
        // between the
        // start of the field node and the completion location
        int replaceLength = context.completionNode instanceof FieldNode ? context.completionLocation - replaceStart
                : context.completionExpression.length();

        return createProposal(fieldName, null, context, enclosingType, isStatic, useKeywordBeforeReplacement, replaceStart,
                replaceLength);
    }


    private ICompletionProposal createProposal(String fieldName, String typeName, ContentAssistContext context,
            IType enclosingType, boolean isStatic, boolean useKeywordBeforeReplacement, int replaceStart, int replaceLength) {

        int relevance = Relevance.VERY_HIGH.getRelavance();



        return new NewGroovyFieldCompletionProposal(fieldName, replaceStart,
                replaceLength,
 relevance, isStatic,
                useKeywordBeforeReplacement, typeName);
    }

}
