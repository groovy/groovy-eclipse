/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Adapted for Groovy-Eclipse
 *		Andrew McCullough - initial API and implementation
 *		IBM Corporation  - general improvement and bug fixes, partial reimplementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codeassist.completions;

import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
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
 * An adaptation of {@link ParameterGuessingProposal} for Groovy code
 *
 * @author andrew
 * @created May 4, 2011
 */
public class GroovyJavaGuessingCompletionProposal extends JavaMethodCompletionProposal {

    /**
     * Creates a {@link ParameterGuessingProposal} or <code>null</code> if the
     * core context isn't available or extended.
     *
     * @param proposal the original completion proposal
     * @param context the currrent context
     * @param fillBestGuess if set, the best guess will be filled in
     *
     * @return a proposal or <code>null</code>
     */
    public static GroovyJavaGuessingCompletionProposal createProposal(CompletionProposal proposal,
            JavaContentAssistInvocationContext context, boolean fillBestGuess, String contributor, ProposalFormattingOptions options) {
        CompletionContext coreContext = context.getCoreContext();
        if (coreContext != null && coreContext.isExtended()) {
            return new GroovyJavaGuessingCompletionProposal(proposal, context, coreContext, fillBestGuess, contributor,
                    options);
        }
        return null;
    }

    /** Tells whether this class is in debug mode. */
    private static final boolean DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jdt.ui/debug/ResultCollector")); //$NON-NLS-1$//$NON-NLS-2$

    private static final String LAST_CLOSURE_TEXT = "{";

    private ICompletionProposal[][] fChoices; // initialized by
                                              // guessParameters()

    private Position[] fPositions; // initialized by guessParameters()

    private IRegion fSelectedRegion; // initialized by apply()

    private IPositionUpdater fUpdater;

    private final boolean fFillBestGuess;

    private final CompletionContext fCoreContext;

    private final ProposalFormattingOptions proposalOptions;

    private final String contributor;

    private GroovyJavaGuessingCompletionProposal(CompletionProposal proposal, JavaContentAssistInvocationContext context,
            CompletionContext coreContext, boolean fillBestGuess, String contributor, ProposalFormattingOptions proposalOptions) {
        super(proposal, context);
        fCoreContext = coreContext;
        fFillBestGuess = fillBestGuess;
        this.contributor = contributor;
        this.proposalOptions = proposalOptions;
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
            JavaPlugin.log(e);
            openErrorDialog(e);
        } catch (BadPositionCategoryException e) {
            ensurePositionCategoryRemoved(document);
            JavaPlugin.log(e);
            openErrorDialog(e);
        }
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#
     * needsLinkedMode()
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

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#
     * computeReplacementString()
     */
    @Override
    protected String computeReplacementString() {

        if (!hasParameters() || !hasArgumentList()) {
            if (proposalOptions.noParens) {
                // command chain expression with no known arguments
                char[] proposalName = fProposal.getName();
                boolean hasWhitespace = false;
                for (int i = 0; i < proposalName.length; i++) {
                    if (CharOperation.isWhitespace(proposalName[i])) {
                        hasWhitespace = true;
                    }
                }
                String newProposalName;
                if (hasWhitespace) {
                    newProposalName = "\"" + String.valueOf(proposalName) + "\"";
                } else {
                    newProposalName = String.valueOf(proposalName);
                }
                return newProposalName;
            } else {
                return super.computeReplacementString();
            }
        }

        long millis = DEBUG ? System.currentTimeMillis() : 0;
        String replacement;
        try {
            replacement = computeGuessingCompletion();
        } catch (JavaModelException x) {
            fPositions = null;
            fChoices = null;
            JavaPlugin.log(x);
            openErrorDialog(x);
            return super.computeReplacementString();
        }
        if (DEBUG)
            System.err.println("Parameter Guessing: " + (System.currentTimeMillis() - millis)); //$NON-NLS-1$

        return replacement;
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
        boolean hasWhitespace = false;
        for (int i = 0; i < proposalName.length; i++) {
            if (CharOperation.isWhitespace(proposalName[i])) {
                hasWhitespace = true;
            }
        }
        char[] newProposalName;
        if (hasWhitespace) {
            newProposalName = CharOperation.concat(new char[] { '"' }, CharOperation.append(proposalName, '"'));
        } else {
            newProposalName = proposalName;
        }
        fProposal.setName(newProposalName);
        appendMethodNameReplacement(buffer);
        fProposal.setName(proposalName);

        FormatterPrefs prefs = getFormatterPrefs();
        if (proposalOptions.noParens) {
            // eat the opening paren replace with a space if there isn't one
            // already
            buffer.replace(buffer.length() - 1, buffer.length(), prefs.beforeOpeningParen ? "" : " ");
        }


        setCursorPosition(buffer.length());

        // groovy doesn't require parens around closures if it is the last
        // argument
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

        if (proposalOptions.noParensAroundClosures) {

            // remove the opening paren only if there is a single closure
            // parameter
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
            if (i >= argCount || proposalOptions.useNamedArguments) {
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
                if (prefs.beforeClosingParen || proposalOptions.noParens) {
                    buffer.append(SPACE);
                }
                if (!proposalOptions.noParens) {
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
                if (!proposalOptions.noParensAroundClosures) {
                    buffer.append(COMMA);
                }
                buffer.append(SPACE);
            }
            Position position = fPositions[indexOfLastClosure];
            position.setOffset(replacementOffset + buffer.length());
            position.setLength(LAST_CLOSURE_TEXT.length());
            buffer.append(LAST_CLOSURE_TEXT);

            if (!proposalOptions.noParensAroundClosures) {
                buffer.append(" }");
                buffer.append(RPAREN);
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

    /*
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
