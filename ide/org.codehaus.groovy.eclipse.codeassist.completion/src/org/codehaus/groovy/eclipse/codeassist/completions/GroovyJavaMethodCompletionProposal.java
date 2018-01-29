/*
 * Copyright 2009-2018 the original author or authors.
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
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

    protected final String contributor;

    protected final ReplacementPreferences preferences;

    public GroovyJavaMethodCompletionProposal(GroovyCompletionProposal proposal, JavaContentAssistInvocationContext context, ProposalFormattingOptions options) {
        this(proposal, context, options, null);
    }

    public GroovyJavaMethodCompletionProposal(GroovyCompletionProposal proposal, JavaContentAssistInvocationContext context, ProposalFormattingOptions options, String contributor) {
        super(proposal, context);

        this.contributor = contributor;
        this.preferences = new ReplacementPreferences(context.getProject(), getFormatterPrefs(), options);

        this.setRelevance(proposal.getRelevance());
        this.setProposalInfo(new MethodProposalInfo(context.getProject(), proposal));
        this.setTriggerCharacters(!proposal.hasParameters() ? ProposalUtils.METHOD_TRIGGERS : ProposalUtils.METHOD_WITH_ARGUMENTS_TRIGGERS);
    }

    protected ImportRewrite fImportRewite;

    public void setImportRewite(ImportRewrite importRewite) {
        fImportRewite = importRewite;
    }

    /** If {@code true}, shows the context only and does not perform completion. */
    protected boolean fContextOnly;

    public void contextOnly() {
        fContextOnly = true;
    }

    // initialized during application
    protected boolean fMethodPointer;
    protected int[] fArgumentOffsets;
    protected int[] fArgumentLengths;
    protected IRegion fSelectedRegion;

    //--------------------------------------------------------------------------

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        fMethodPointer = ProposalUtils.isMethodPointerCompletion(document, getReplacementOffset());
        ReflectionUtils.setPrivateField(LazyJavaCompletionProposal.class, "fReplacementStringComputed", this, Boolean.FALSE);

        super.apply(document, trigger, offset);

        if (fArgumentOffsets != null && fArgumentOffsets.length > 0 && fArgumentLengths != null && fArgumentLengths.length > 0) {
            fSelectedRegion = new Region(getReplacementOffset() + fArgumentOffsets[0], fArgumentLengths[0]);
        } else {
            fSelectedRegion = new Region(getReplacementOffset() + getReplacementString().length(), 0);
        }
    }

    @Override
    public Point getSelection(IDocument document) {
        return (fSelectedRegion == null ? null : new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength()));
    }

    @Override
    protected StyledString computeDisplayString() {
        StyledString displayString = super.computeDisplayString();
        if (contributor != null && !contributor.trim().isEmpty()) {
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
        if (fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION && requiredProposal instanceof LazyJavaTypeCompletionProposal) {
            if (fImportRewite != null) {
                ReflectionUtils.setPrivateField(LazyJavaTypeCompletionProposal.class, "fImportRewrite", requiredProposal, fImportRewite);
            }
            if (requiredProposal instanceof LazyGenericTypeProposal) {
                // disable generics completion for constructors (i.e. complete expression as "new ArrayList()" instead of "new ArrayList<E>()"
                try {
                    //requiredProposal.fTypeArgumentProposals = new LazyGenericTypeProposal.TypeArgumentProposal[0];
                    ReflectionUtils.setPrivateField(LazyGenericTypeProposal.class, "fTypeArgumentProposals", requiredProposal,
                        Array.newInstance(Class.forName("org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal$TypeArgumentProposal"), 0));
                } catch (Exception e) {
                    GroovyContentAssist.logError(e);
                }
            }
        }
        return requiredProposal;
    }

    @Override
    protected String computeReplacementString() {
        if (fContextOnly) {
            return "";
        }

        char[] proposalName = fProposal.getName();
        boolean hasWhitespace = ProposalUtils.hasWhitespace(proposalName);

        if (fMethodPointer) {
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
        fProposal.setName(!hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"'));
        appendMethodNameReplacement(buffer);
        fProposal.setName(proposalName);

        if (!hasParameters()) {
            if (preferences.insertSpaceBetweenEmptyParensInMethodCall) {
                buffer.append(SPACE);
            }
            buffer.append(RPAREN);
        } else {
            int indexOfLastClosure = -1;
            char[][] regularParameterTypes = ((GroovyCompletionProposal) fProposal).getRegularParameterTypeNames();
            char[][] namedParameterTypes = ((GroovyCompletionProposal) fProposal).getNamedParameterTypeNames();
            if (preferences.insertClosureAfterClosingParenInMethodCall) {
                // need to check both regular and named parameters for closure
                if (lastArgIsClosure(regularParameterTypes, namedParameterTypes)) {
                    indexOfLastClosure = regularParameterTypes.length + namedParameterTypes.length - 1;
                }

                // remove the opening paren only if there is a single closure parameter
                if (indexOfLastClosure == 0) {
                    buffer.deleteCharAt(buffer.length() - 1);

                    // add space if not already there would be added by call to appendMethodNameReplacement
                    if (!preferences.insertSpaceBeforeOpeningParenInMethodCall) {
                        buffer.append(SPACE);
                    }
                }
            } else if (preferences.insertSpaceAfterOpeningParenInMethodCall) {
                buffer.append(SPACE);
            }

            // now add the parameters; named parameters go first
            char[][] namedParameterNames = ((GroovyCompletionProposal) fProposal).getNamedParameterNames();
            char[][] regularParameterNames = ((GroovyCompletionProposal) fProposal).getRegularParameterNames();
            int namedCount = namedParameterNames.length, totalCount = regularParameterNames.length + namedCount;

            fArgumentOffsets = new int[totalCount];
            fArgumentLengths = new int[totalCount];

            for (int i = 0; i < totalCount; i += 1) {
                char[] nextName, nextType;
                // check for named args (either all of them, or the explicitly named ones)
                if (i < namedCount) {
                    nextName = namedParameterNames[i];
                    nextType = namedParameterTypes[i];
                } else {
                    nextName = regularParameterNames[i - namedCount];
                    nextType = regularParameterTypes[i - namedCount];
                }

                if ((preferences.useNamedArguments || i < namedCount) && i != indexOfLastClosure) {
                    buffer.append(nextName);
                    if (preferences.insertSpaceBeforeColonInNamedArgument) {
                        buffer.append(SPACE);
                    }
                    buffer.append(":");
                    if (preferences.insertSpaceAfterColonInNamedArgument) {
                        buffer.append(SPACE);
                    }
                }

                if (preferences.useClosureLiteral && CharOperation.equals(nextType, CLOSURE_TYPE_NAME)) {
                    buffer.append("{");
                    if (preferences.insertSpaceAfterOpeningBraceInClosure) {
                        buffer.append(SPACE);
                    }

                    fArgumentOffsets[i] = buffer.length();
                    fArgumentLengths[i] = 2; // select "it"
                    buffer.append("it");

                    if (preferences.insertSpaceBeforeClosingBraceInClosure) {
                        buffer.append(SPACE);
                    }
                    buffer.append("}");
                } else {
                    fArgumentOffsets[i] = buffer.length();
                    fArgumentLengths[i] = nextName.length;
                    buffer.append(nextName);
                }

                if (i == (indexOfLastClosure - 1) || (i != indexOfLastClosure && i == (totalCount - 1))) {
                    if (preferences.insertSpaceBeforeClosingParenInMethodCall) {
                        buffer.append(SPACE);
                    }
                    buffer.append(RPAREN);
                    if (i == indexOfLastClosure - 1) {
                        buffer.append(SPACE);
                    }
                } else if (i < (totalCount - 1)) {
                    if (preferences.insertSpaceBeforeCommaInMethodCallArgs) {
                        buffer.append(SPACE);
                    }
                    buffer.append(COMMA);
                    if (preferences.insertSpaceAfterCommaInMethodCallArgs) {
                        buffer.append(SPACE);
                    }
                }
            }
        }

        return buffer.toString();
    }

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

    //--------------------------------------------------------------------------

    /**
     * Returns the currently active Java editor, or <code>null</code> if it
     * cannot be determined.
     */
    protected static JavaEditor getJavaEditor() {
        IEditorPart part = JavaPlugin.getActivePage().getActiveEditor();
        if (part instanceof JavaEditor) {
            return (JavaEditor) part;
        }
        return null;
    }

    protected static boolean lastArgIsClosure(char[][] regularParameterTypes, char[][] namedParameterTypes) {
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

    protected static final char[] CLOSURE_TYPE_NAME = "Closure".toCharArray();

    //--------------------------------------------------------------------------

    /**
     * @see org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions
     * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal.FormatterPrefs
     */
    protected static class ReplacementPreferences {

        public ReplacementPreferences(IJavaProject project, FormatterPrefs prefs, ProposalFormattingOptions opts) {
            insertSpaceBetweenEmptyParensInMethodCall = prefs.inEmptyList;
            insertSpaceBeforeOpeningParenInMethodCall = prefs.beforeOpeningParen;
            insertSpaceAfterOpeningParenInMethodCall = prefs.afterOpeningParen;
            insertSpaceBeforeClosingParenInMethodCall = prefs.beforeClosingParen;
            insertSpaceBeforeCommaInMethodCallArgs = prefs.beforeComma;
            insertSpaceAfterCommaInMethodCallArgs = prefs.afterComma;

            useClosureLiteral = opts.useBracketsForClosures;
            insertSpaceAfterOpeningBraceInClosure = true; // TODO: Add formatter preference or read org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER
            insertSpaceBeforeClosingBraceInClosure = true; // TODO: Add formatter preference or read org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER
            insertClosureAfterClosingParenInMethodCall = opts.noParensAroundClosures;

            useNamedArguments = opts.useNamedArguments;
            insertSpaceBeforeColonInNamedArgument = false; // TODO: Add formatter preference or read org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT
            insertSpaceAfterColonInNamedArgument = true; // TODO: Add formatter preference or read org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT
        }

        public final boolean insertSpaceBetweenEmptyParensInMethodCall;
        public final boolean insertSpaceBeforeOpeningParenInMethodCall;
        public final boolean insertSpaceAfterOpeningParenInMethodCall ;
        public final boolean insertSpaceBeforeClosingParenInMethodCall;
        public final boolean insertSpaceBeforeCommaInMethodCallArgs;
        public final boolean insertSpaceAfterCommaInMethodCallArgs ;

        public final boolean useClosureLiteral;
        public final boolean insertSpaceAfterOpeningBraceInClosure;
        public final boolean insertSpaceBeforeClosingBraceInClosure;
        public final boolean insertClosureAfterClosingParenInMethodCall;

        public final boolean useNamedArguments;
        public final boolean insertSpaceBeforeColonInNamedArgument;
        public final boolean insertSpaceAfterColonInNamedArgument;
    }
}
