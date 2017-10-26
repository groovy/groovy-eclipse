/*
 * Copyright 2009-2017 the original author or authors.
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

import java.lang.reflect.Array;

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.ProposalContextInformation;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

public class GroovyJavaMethodCompletionProposal extends JavaMethodCompletionProposal {

    private static final char[] CLOSURE_TYPE_NAME = "Closure".toCharArray();

    private final ProposalFormattingOptions options;

    private final String contributor;

    // if true, shows the context only and does not perform completion
    private boolean contextOnly;

    private boolean methodPointer;

    private int[] fArgumentOffsets;
    private int[] fArgumentLengths;
    private IRegion fSelectedRegion; // initialized by apply()

    public GroovyJavaMethodCompletionProposal(GroovyCompletionProposal proposal, JavaContentAssistInvocationContext context, ProposalFormattingOptions options) {
        this(proposal, context, options, null);
    }

    public GroovyJavaMethodCompletionProposal(GroovyCompletionProposal proposal, JavaContentAssistInvocationContext context, ProposalFormattingOptions options, String contributor) {
        super(proposal, context);

        this.options = options;
        this.contributor = contributor;
        this.setRelevance(proposal.getRelevance());
        this.setProposalInfo(new MethodProposalInfo(context.getProject(), proposal));
        this.setTriggerCharacters(!proposal.hasParameters() ? ProposalUtils.METHOD_TRIGGERS : ProposalUtils.METHOD_WITH_ARGUMENTS_TRIGGERS);
    }

    public void contextOnly() {
        contextOnly = true;
    }

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        methodPointer = ProposalUtils.isMethodPointerCompletion(document, getReplacementOffset());

        super.apply(document, trigger, offset);

        if (fArgumentOffsets != null && fArgumentOffsets.length > 0 && fArgumentLengths != null && fArgumentLengths.length > 0) {
            fSelectedRegion = new Region(getReplacementOffset() + fArgumentOffsets[0], fArgumentLengths[0]);
        } else {
            fSelectedRegion = new Region(getReplacementOffset() + getReplacementString().length(), 0);
        }
    }

    @Override
    public Point getSelection(IDocument document) {
        if (fSelectedRegion == null)
            return new Point(getReplacementOffset(), 0);

        return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
    }

    @Override
    protected StyledString computeDisplayString() {
        StyledString displayString = super.computeDisplayString();
        if (contributor != null && contributor.trim().length() > 0) {
            displayString.append(new StyledString(" (" + contributor.trim() + ")", StyledString.DECORATIONS_STYLER));
        }
        return displayString;
    }

    @Override
    protected IContextInformation computeContextInformation() {
        if (hasParameters() && (fProposal.getKind() == CompletionProposal.METHOD_REF ||
                                fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION)) {
            ProposalContextInformation contextInformation = new ProposalContextInformation(fProposal);
            if (fContextInformationPosition != 0 && fProposal.getCompletion().length == 0)
                contextInformation.setContextInformationPosition(fContextInformationPosition);
            return contextInformation;
        }
        return super.computeContextInformation();
    }

    @Override
    protected LazyJavaCompletionProposal createRequiredTypeCompletionProposal(CompletionProposal completionProposal, JavaContentAssistInvocationContext invocationContext) {
        LazyJavaCompletionProposal requiredProposal = super.createRequiredTypeCompletionProposal(completionProposal, invocationContext);
        // disable generics completion for constructors (i.e. complete expression as "new ArrayList()" instead of "new ArrayList<E>()"
        if (fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION && requiredProposal instanceof LazyGenericTypeProposal) {
            try {
                //requiredProposal.fTypeArgumentProposals = new LazyGenericTypeProposal.TypeArgumentProposal[0];
                ReflectionUtils.setPrivateField(LazyGenericTypeProposal.class, "fTypeArgumentProposals", requiredProposal,
                    Array.newInstance(Class.forName("org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal$TypeArgumentProposal"), 0));
            } catch (Exception e) {
                GroovyContentAssist.logError(e);
            }
        }
        return requiredProposal;
    }

    @Override
    protected String computeReplacementString() {
        if (contextOnly) {
            return "";
        }

        char[] proposalName = fProposal.getName();
        boolean hasWhitespace = ProposalUtils.hasWhitespace(proposalName);

        if (methodPointer) {
            // complete the name only for a method pointer expression
            return String.valueOf(!hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"'));
        }

        // with no arguments there is nothing groovy to do
        if ((!hasParameters() || !hasArgumentList()) && !hasWhitespace) {
            String replacementString = super.computeReplacementString();
            if (replacementString.endsWith(");")) {
                replacementString = replacementString.substring(0, replacementString.length() - 1);
            }
            return replacementString;
        }

        StringBuffer buffer = new StringBuffer();
        char[] newProposalName = !hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"');

        fProposal.setName(newProposalName);
        appendMethodNameReplacement(buffer);
        fProposal.setName(proposalName);

        if (!hasParameters()) {
            if (getFormatterPrefs().inEmptyList) {
                buffer.append(SPACE);
            }
            buffer.append(RPAREN);
        } else {
            int indexOfLastClosure = -1;
            char[][] regularParameterTypes = ((GroovyCompletionProposal) fProposal).getRegularParameterTypeNames();
            char[][] namedParameterTypes = ((GroovyCompletionProposal) fProposal).getNamedParameterTypeNames();
            if (options.noParensAroundClosures) {
                // need to check both regular and named parameters for closure
                if (lastArgIsClosure(regularParameterTypes, namedParameterTypes)) {
                    indexOfLastClosure = regularParameterTypes.length + namedParameterTypes.length - 1;
                }

                // remove the opening paren only if there is a single closure parameter
                if (indexOfLastClosure == 0) {
                    buffer.replace(buffer.length() - 1, buffer.length(), "");

                    // add space if not already there would be added by call to appendMethodNameReplacement
                    if (!getFormatterPrefs().beforeOpeningParen) {
                        buffer.append(SPACE);
                    }
                }
            } else if (getFormatterPrefs().afterOpeningParen) {
                buffer.append(SPACE);
            }

            // now add the parameters; named parameters go first
            char[][] namedParameterNames = ((GroovyCompletionProposal) fProposal).getNamedParameterNames();
            char[][] regularParameterNames = ((GroovyCompletionProposal) fProposal).getRegularParameterNames();
            int namedCount = namedParameterNames.length;
            int argCount = regularParameterNames.length;
            int allCount = argCount + namedCount;

            fArgumentOffsets = new int[allCount];
            fArgumentLengths = new int[allCount];

            for (int i = 0; i < allCount; i += 1) {
                // check for named args (either all of them, or the explicitly named ones)
                char[] nextName;
                char[] nextTypeName;
                if (i < namedCount) { // named arg
                    nextName = namedParameterNames[i];
                    nextTypeName = namedParameterNames[i];
                } else {
                    nextName = regularParameterNames[i - namedCount];
                    nextTypeName = regularParameterTypes[i - namedCount];
                }

                if (options.useNamedArguments || i < namedCount) {
                    buffer.append(nextName).append(":");
                }
                fArgumentOffsets[i] = buffer.length();
                if (i == 0) {
                    setCursorPosition(buffer.length());
                }
                // handle the argument name
                if (options.useBracketsForClosures && CharOperation.equals(nextTypeName, CLOSURE_TYPE_NAME)) {
                    fArgumentOffsets[i] = buffer.length() + 2;
                    fArgumentLengths[i] = 2; // select "it"
                    buffer.append("{ it }");
                } else {
                    fArgumentOffsets[i] = buffer.length();
                    fArgumentLengths[i] = nextName.length;
                    buffer.append(nextName);
                }

                if (i == indexOfLastClosure - 1 || (i != indexOfLastClosure && i == allCount - 1)) {
                    if (getFormatterPrefs().beforeClosingParen) {
                        buffer.append(SPACE);
                    }
                    buffer.append(RPAREN);
                    if (i == indexOfLastClosure - 1) {
                        buffer.append(SPACE);
                    }
                } else if (i < allCount - 1) {
                    if (getFormatterPrefs().beforeComma)
                        buffer.append(SPACE);
                    buffer.append(COMMA);
                    if (getFormatterPrefs().afterComma)
                        buffer.append(SPACE);
                }
            }
        }

        return buffer.toString();
    }

    /**
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#needsLinkedMode()
     */
    @Override
    protected boolean needsLinkedMode() {
        return super.needsLinkedMode();
    }

    @Override
    protected void setUpLinkedMode(IDocument document, char closingCharacter) {
        ITextViewer textViewer = getTextViewer();
        if (textViewer != null && fArgumentOffsets != null) {
            int baseOffset = getReplacementOffset();
            String replacement = getReplacementString();
            try {
                LinkedModeModel model = new LinkedModeModel();
                for (int i = 0, n = fArgumentOffsets.length; i < n; i += 1) {
                    LinkedPositionGroup group = new LinkedPositionGroup();
                    group.addPosition(new LinkedPosition(document, baseOffset + fArgumentOffsets[i], fArgumentLengths[i], LinkedPositionGroup.NO_STOP));
                    model.addGroup(group);
                }
                model.forceInstall();

                JavaEditor editor = getJavaEditor();
                if (editor != null) {
                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
                }

                LinkedModeUI ui = new EditorLinkedModeUI(model, textViewer);
                ui.setExitPosition(textViewer, baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
                ui.setExitPolicy(new ExitPolicy(closingCharacter, document));
                ui.setDoContextInfo(true);
                ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
                ui.enter();

                fSelectedRegion = ui.getSelectedRegion();

            } catch (BadLocationException e) {
                GroovyContentAssist.logError(e);
            }
        }
    }

    /**
     * Returns the currently active Java editor, or <code>null</code> if it
     * cannot be determined.
     */
    private static JavaEditor getJavaEditor() {
        IEditorPart part= JavaPlugin.getActivePage().getActiveEditor();
        if (part instanceof JavaEditor) {
            return (JavaEditor) part;
        }
        return null;
    }

    private static boolean lastArgIsClosure(char[][] regularParameterTypes, char[][] namedParameterTypes) {
        char[] lastArgType;
        if (namedParameterTypes != null && namedParameterTypes.length > 0) {
            lastArgType = namedParameterTypes[namedParameterTypes.length - 1];
        } else if (regularParameterTypes != null && regularParameterTypes.length > 0) {
            lastArgType = regularParameterTypes[regularParameterTypes.length - 1];
        } else { // no args
            return false;
        }
        // we should be comparing against a fully qualified type name, but it is not always available so a simple name is close enough
        return CharOperation.equals(lastArgType, CLOSURE_TYPE_NAME);
    }
}
