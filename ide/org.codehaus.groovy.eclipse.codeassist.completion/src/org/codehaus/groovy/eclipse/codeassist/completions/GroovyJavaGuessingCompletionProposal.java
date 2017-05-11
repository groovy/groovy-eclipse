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

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.ImportContainer;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * An adaptation of {@link ParameterGuessingProposal} for Groovy code.
 */
public class GroovyJavaGuessingCompletionProposal extends JavaMethodCompletionProposal {

    /**
     * Creates a {@link ParameterGuessingProposal} or <code>null</code> if the
     * core context isn't available or extended.
     *
     * @param proposal the original completion proposal
     * @param context the current context
     * @param fillBestGuess if set, the best guess will be filled in
     *
     * @return a proposal or <code>null</code>
     */
    public static GroovyJavaGuessingCompletionProposal createProposal(CompletionProposal proposal,
            JavaContentAssistInvocationContext context, boolean fillBestGuess, String contributor, ProposalFormattingOptions options) {
        CompletionContext coreContext = context.getCoreContext();
        if (coreContext != null && coreContext.isExtended()) {
            return new GroovyJavaGuessingCompletionProposal(proposal, context, coreContext, fillBestGuess, contributor, options);
        }
        return null;
    }

    private ICompletionProposal[][] fChoices; // initialized by guessParameters()

    private Position[] fPositions; // initialized by guessParameters()

    private IRegion fSelectedRegion; // initialized by apply()

    private IPositionUpdater fUpdater;

    private final boolean fFillBestGuess;

    private final CompletionContext fCoreContext;

    private final ProposalFormattingOptions options;

    private final String contributor;

    private boolean methodPointer;

    private GroovyJavaGuessingCompletionProposal(CompletionProposal proposal, JavaContentAssistInvocationContext context,
            CompletionContext coreContext, boolean fillBestGuess, String contributor, ProposalFormattingOptions proposalOptions) {
        super(proposal, context);
        fCoreContext = coreContext;
        fFillBestGuess = fillBestGuess;
        this.contributor = contributor;
        this.options = proposalOptions;
    }

    @Override
    protected int computeRelevance() {
        // precomputed
        return fProposal.getRelevance();
    }

    private IJavaElement getEnclosingElement() {
        return fCoreContext.getEnclosingElement();
    }

    // includes the regular and the named parameters
    private String[] cachedVisibleParameterTypes = null;

    private String[] getParameterTypes() {
        if (cachedVisibleParameterTypes == null) {
            char[] signature = SignatureUtil.fix83600(fProposal.getSignature());
            char[][] types = Signature.getParameterTypes(signature);

            String[] ret = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                ret[i] = new String(Signature.toCharArray(types[i]));
            }
            cachedVisibleParameterTypes = ret;
        }
        return cachedVisibleParameterTypes;
    }

    private IJavaElement[][] getAssignableElements() {
        // get the visible parameters (ie- the regular and named params
        // together)
        String[] parameterTypes = getParameterTypes();
        IJavaElement[][] assignableElements = new IJavaElement[parameterTypes.length][];
        for (int i = 0; i < parameterTypes.length; i++) {
            // hmmmm...I don't like all this back and forth between type names
            // and signatures
            String typeName = new String(parameterTypes[i]);
            assignableElements[i] = fCoreContext.getVisibleElements(Signature.createTypeSignature(typeName, true));
        }
        return assignableElements;
    }

    /*
     * @see ICompletionProposalExtension#apply(IDocument, char)
     */
    @Override
    public void apply(IDocument document, char trigger, int offset) {
        methodPointer = ProposalUtils.isMethodPointerCompletion(document, getReplacementOffset());
        try {
            super.apply(document, trigger, offset);

            int baseOffset = getReplacementOffset();
            String replacement = getReplacementString();

            if (fPositions != null && getTextViewer() != null) {

                LinkedModeModel model = new LinkedModeModel();

                for (int i = 0; i < fPositions.length; i++) {
                    LinkedPositionGroup group = new LinkedPositionGroup();
                    int positionOffset = fPositions[i].getOffset();
                    int positionLength = fPositions[i].getLength();

                    if (fChoices[i].length < 2) {
                        group.addPosition(new LinkedPosition(document, positionOffset, positionLength, LinkedPositionGroup.NO_STOP));
                    } else {
                        ensurePositionCategoryInstalled(document, model);
                        document.addPosition(getCategory(), fPositions[i]);
                        group.addPosition(new ProposalPosition(document, positionOffset, positionLength,
                                LinkedPositionGroup.NO_STOP, fChoices[i]));
                    }
                    model.addGroup(group);
                }

                model.forceInstall();
                JavaEditor editor = getJavaEditor();
                if (editor != null) {
                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
                }

                LinkedModeUI ui = new EditorLinkedModeUI(model, getTextViewer());
                ui.setExitPosition(getTextViewer(), baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
                ui.setExitPolicy(new ExitPolicy(')', document));
                ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
                ui.setDoContextInfo(true);
                ui.enter();
                fSelectedRegion = ui.getSelectedRegion();

            } else {
                fSelectedRegion = new Region(baseOffset + replacement.length(), 0);
            }

        } catch (BadLocationException e) {
            ensurePositionCategoryRemoved(document);
            GroovyContentAssist.logError(e);
            openErrorDialog(e);
        } catch (BadPositionCategoryException e) {
            ensurePositionCategoryRemoved(document);
            GroovyContentAssist.logError(e);
            openErrorDialog(e);
        }
    }

    /**
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#needsLinkedMode()
     */
    @Override
    protected boolean needsLinkedMode() {
        return false; // we handle it ourselves
    }

    @Override
    protected StyledString computeDisplayString() {
        return super.computeDisplayString().append(getStyledGroovy());
    }

    private StyledString getStyledGroovy() {
        return new StyledString(" (" + contributor + ")", StyledString.DECORATIONS_STYLER);
    }

    /**
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#computeReplacementString()
     */
    @Override
    protected String computeReplacementString() {
        char[] proposalName = fProposal.getName();
        boolean hasWhitespace = ProposalUtils.hasWhitespace(proposalName);

        if (methodPointer) {
            // complete the name only for a method pointer expression
            return String.valueOf(!hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"'));
        }

        if (!hasParameters() || !hasArgumentList()) {
            if (options.noParens) {
                return String.valueOf(!hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"'));
            } else {
                String replacementString = super.computeReplacementString();
                if (replacementString.endsWith(");")) {
                    replacementString = replacementString.substring(0, replacementString.length() - 1);
                }
                return replacementString;
            }
        }

        try {
            return computeGuessingCompletion();
        } catch (JavaModelException e) {
            fPositions = null;
            fChoices = null;
            GroovyContentAssist.logError(e);
            openErrorDialog(e);
        }

        return super.computeReplacementString();
    }
    /**
     * Creates the completion string. Offsets and Lengths are set to the offsets
     * and lengths of the
     * parameters.
     *
     * @return the completion string
     * @throws JavaModelException if parameter guessing failed
     */
    private String computeGuessingCompletion() throws JavaModelException {
        StringBuffer buffer = new StringBuffer();
        char[] proposalName = fProposal.getName();
        boolean hasWhitespace = ProposalUtils.hasWhitespace(proposalName);
        char[] newProposalName = !hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"');

        fProposal.setName(newProposalName);
        appendMethodNameReplacement(buffer);
        fProposal.setName(proposalName);

        FormatterPrefs prefs = getFormatterPrefs();
        if (options.noParens) {
            // eat the opening paren replace with a space if there isn't one already
            buffer.replace(buffer.length() - 1, buffer.length(), prefs.beforeOpeningParen ? "" : " ");
        }

        setCursorPosition(buffer.length());

        // groovy doesn't require parens around closures if it is the last argument
        // If the option is set, then we follow that heuristic
        char[][] regularParameterTypes = ((GroovyCompletionProposal) fProposal).getRegularParameterTypeNames();

        // check if the last regular parameter is a closure. If so, it must be
        // moved to the end
        boolean lastArgIsClosure = lastArgIsClosure(regularParameterTypes);
        int indexOfLastClosure = lastArgIsClosure ? regularParameterTypes.length - 1 : -1;

        char[][] namedParameterNames = ((GroovyCompletionProposal) fProposal).getNamedParameterNames();
        char[][] regularParameterNames = ((GroovyCompletionProposal) fProposal).getRegularParameterNames();
        int namedCount = namedParameterNames.length;
        int argCount = regularParameterNames.length;
        int allCount = argCount + namedCount;

        if (options.noParensAroundClosures) {
            // remove the opening paren only if there is a single closure parameter
            if (indexOfLastClosure == 0 && namedCount == 0) {
                buffer.replace(buffer.length() - 1, buffer.length(), "");
                // add space if not already there
                // would be added by call to appendMethodNameReplacement
                if (!prefs.beforeOpeningParen) {
                    buffer.append(SPACE);
                }
            } else {
                if (prefs.afterOpeningParen)
                    buffer.append(SPACE);
            }
        } else {
            if (prefs.afterOpeningParen)
                buffer.append(SPACE);
        }

        // we don't want parameters for static import declarations
        if (fCoreContext.getEnclosingElement() != null
                && !(fCoreContext.getEnclosingElement().getParent() instanceof ImportContainer)) {
            // now add the parameters
            int replacementOffset = getReplacementOffset();
            fChoices = guessParameters(namedParameterNames, regularParameterNames);

            for (int i = 0; i < allCount; i++) {
                if (i == indexOfLastClosure) {
                    // handle the last closure separately
                    continue;
                }

                char[] nextName;
                if (i < argCount) {
                    nextName = regularParameterNames[i];
                } else {
                    nextName = namedParameterNames[i - argCount];
                }
                if (i >= argCount || options.useNamedArguments) {
                    buffer.append(nextName).append(":");
                }

                // handle the value
                ICompletionProposal proposal = fChoices[i][0];
                String argument = proposal.getDisplayString();

                Position position = fPositions[i];
                position.setOffset(replacementOffset + buffer.length());
                position.setLength(argument.length());

                // handle the "unknown" case where we only insert a proposal.
                if (proposal instanceof JavaCompletionProposal) {
                    ((JavaCompletionProposal) proposal).setReplacementOffset(replacementOffset + buffer.length());
                }
                buffer.append(argument);

                // check what to add after argument
                if (i == allCount - 1 || (i == allCount - 2 && i == indexOfLastClosure - 1)) {
                    if (prefs.beforeClosingParen || options.noParens) {
                        buffer.append(SPACE);
                    }
                    if (!options.noParens) {
                        buffer.append(RPAREN);
                    }
                } else if (i < allCount - 1) {
                    if (prefs.beforeComma)
                        buffer.append(SPACE);
                    buffer.append(COMMA);
                    if (prefs.afterComma)
                        buffer.append(SPACE);
                }
            }

            // closures at the end are added in an idiomatic groovy way
            if (indexOfLastClosure >= 0) {
                if (allCount > 1) {
                    if (!options.noParensAroundClosures) {
                        buffer.append(COMMA);
                    }
                    buffer.append(SPACE);
                }
                Position position = fPositions[indexOfLastClosure];
                position.setOffset(replacementOffset + buffer.length());
                position.setLength(1);
                buffer.append("{");

                if (!options.noParensAroundClosures) {
                    buffer.append(" }");
                    buffer.append(RPAREN);
                }
            }
        }
        return buffer.toString();
    }

    private boolean lastArgIsClosure(char[][] regularParameterTypes) {
        if (regularParameterTypes != null && regularParameterTypes.length > 0) {
            char[] lastArgType = regularParameterTypes[regularParameterTypes.length - 1];
            // we should be comparing against a fully qualified type name, but
            // it is not always available
            // so a simple name is close enough
            return CharOperation.equals("Closure".toCharArray(), lastArgType);
        } else {
            // no args
            return false;
        }
    }

    /**
     * Returns the currently active java editor, or <code>null</code> if it
     * cannot be determined.
     *
     * @return the currently active java editor, or <code>null</code>
     */
    private JavaEditor getJavaEditor() {
        IEditorPart part = JavaPlugin.getActivePage().getActiveEditor();
        if (part instanceof JavaEditor)
            return (JavaEditor) part;
        else
            return null;
    }

    private ICompletionProposal[][] guessParameters(char[][] firstParameterNames, char[][] secondParameterNames)
            throws JavaModelException {
        // find matches in reverse order. Do this because people tend to declare
        // the variable meant for the last
        // parameter last. That is, local variables for the last parameter in
        // the method completion are more
        // likely to be closer to the point of code completion. As an example
        // consider a "delegation" completion:
        //
        // public void myMethod(int param1, int param2, int param3) {
        // someOtherObject.yourMethod(param1, param2, param3);
        // }
        //
        // The other consideration is giving preference to variables that have
        // not previously been used in this
        // code completion (which avoids
        // "someOtherObject.yourMethod(param1, param1, param1)";

        char[][] parameterNames = new char[firstParameterNames.length + secondParameterNames.length][];
        System.arraycopy(firstParameterNames, 0, parameterNames, 0, firstParameterNames.length);
        System.arraycopy(secondParameterNames, 0, parameterNames, firstParameterNames.length, secondParameterNames.length);

        int count = parameterNames.length;
        fPositions = new Position[count];

        fChoices = new ICompletionProposal[count][];

        String[] parameterTypes = getParameterTypes();

        IJavaElement[][] assignableElements = getAssignableElements();

        for (int i = count - 1; i >= 0; i--) {
            String paramName = new String(parameterNames[i]);
            Position position = new Position(0, 0);

            ICompletionProposal[] argumentProposals = new ParameterGuesserDelegate(getEnclosingElement()).parameterProposals(
                    parameterTypes[i],
                    paramName, position, assignableElements[i], fFillBestGuess);

            if (argumentProposals.length == 0)
                argumentProposals = new ICompletionProposal[] { new JavaCompletionProposal(paramName, 0, paramName.length(), null,
                        paramName, 0) };

            fPositions[i] = position;
            fChoices[i] = argumentProposals;
        }

        return fChoices;
    }

    /**
     * @see ICompletionProposal#getSelection(IDocument)
     */
    @Override
    public Point getSelection(IDocument document) {
        if (fSelectedRegion == null)
            return new Point(getReplacementOffset(), 0);

        return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
    }

    private void openErrorDialog(Exception e) {
        Shell shell = getTextViewer().getTextWidget().getShell();
        MessageDialog.openError(shell, "Error guessing parameters for content assist proposal", e.getMessage());
    }

    private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
        if (!document.containsPositionCategory(getCategory())) {
            document.addPositionCategory(getCategory());
            fUpdater = new InclusivePositionUpdater(getCategory());
            document.addPositionUpdater(fUpdater);

            model.addLinkingListener(new ILinkedModeListener() {

                /*
                 * @see
                 * org.eclipse.jface.text.link.ILinkedModeListener#left(org.
                 * eclipse.jface.text.link.LinkedModeModel, int)
                 */
                public void left(LinkedModeModel environment, int flags) {
                    ensurePositionCategoryRemoved(document);
                }

                public void suspend(LinkedModeModel environment) {}

                public void resume(LinkedModeModel environment, int flags) {}
            });
        }
    }

    private void ensurePositionCategoryRemoved(IDocument document) {
        if (document.containsPositionCategory(getCategory())) {
            try {
                document.removePositionCategory(getCategory());
            } catch (BadPositionCategoryException e) {
                // ignore
            }
            document.removePositionUpdater(fUpdater);
        }
    }

    private String getCategory() {
        return "ParameterGuessingProposal_" + toString(); //$NON-NLS-1$
    }

    /**
     * Not API, for testing only.
     *
     * @return the guessed parameter proposals
     */
    public ICompletionProposal[][] getChoices() {
        return fChoices;
    }
}
