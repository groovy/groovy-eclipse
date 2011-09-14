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

import java.lang.reflect.Method;

import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.codehaus.groovy.eclipse.core.GroovyCore;
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
import org.eclipse.jdt.internal.ui.text.java.ParameterGuesser;
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

    private static final String CLOSURE_TEXT = "{  }";

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

        if (!hasParameters() || !hasArgumentList())
            return super.computeReplacementString();

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

        setCursorPosition(buffer.length());

        // groovy doesn't require parens around closures if it is the last
        // argument
        // If the option is set, then we follow that heuristic
        int indexOfLastClosure = -1;
        char[][] regularParameterTypes = ((GroovyCompletionProposal) fProposal).getRegularParameterTypeNames();
        char[][] namedParameterTypes = ((GroovyCompletionProposal) fProposal).getNamedParameterTypeNames();
        if (proposalOptions.noParensAroundClosures) {
            if (lastArgIsClosure(regularParameterTypes, namedParameterTypes)) {
                indexOfLastClosure = regularParameterTypes.length + namedParameterTypes.length - 1;
            }

            // remove the opening paren only if there is a single closure
            // parameter
            if (indexOfLastClosure == 0) {
                buffer.replace(buffer.length() - 1, buffer.length(), "");

                // add space if not already there
                // would be added by call to appendMethodNameReplacement
                if (!prefs.beforeOpeningParen) {
                    buffer.append(SPACE);
                }
            }
        } else {
            if (prefs.afterOpeningParen)
                buffer.append(SPACE);
        }

        // now add the parameters
        char[][] regularParameterNames = ((GroovyCompletionProposal) fProposal).getRegularParameterNames();
        char[][] namedParameterNames = ((GroovyCompletionProposal) fProposal).getNamedParameterNames();
        int argCount = regularParameterNames.length;
        int namedCount = namedParameterNames.length;
        int allCount = argCount + namedCount;

        int replacementOffset = getReplacementOffset();
        fChoices = guessParameters(regularParameterNames, namedParameterNames);

        for (int i = 0; i < allCount; i++) {
            char[] nextName;
            char[] nextTypeName;
            if (i < argCount) {
                nextTypeName = regularParameterTypes[i];
                nextName = regularParameterNames[i];
            } else {
                // named arg
                nextName = namedParameterNames[i - argCount];
                nextTypeName = namedParameterNames[i - argCount];
            }

            if (proposalOptions.useNamedArguments || i >= argCount) {
                buffer.append(nextName).append(":");
            }

            // handle the argument name
            if (proposalOptions.useBracketsForClosures && Signature.getSimpleName(nextTypeName).equals("Closure")) {
                // closure
                Position position = fPositions[i];
                position.setOffset(replacementOffset + buffer.length() + 2);
                buffer.append(CLOSURE_TEXT);
                position.setLength(0);

            } else {
                // regular argument
                ICompletionProposal proposal = fChoices[i][0];
                String argument = proposal.getDisplayString();

                Position position = fPositions[i];
                position.setOffset(replacementOffset + buffer.length());
                position.setLength(argument.length());

                // handle the "unknown" case where we only insert a proposal.
                if (proposal instanceof JavaCompletionProposal)
                    ((JavaCompletionProposal) proposal).setReplacementOffset(replacementOffset + buffer.length());
                buffer.append(argument);
            }

            if (i == indexOfLastClosure - 1 || (i != indexOfLastClosure && i == allCount - 1)) {
                if (prefs.beforeClosingParen) {
                    buffer.append(SPACE);
                }
                buffer.append(RPAREN);
                if (i == indexOfLastClosure - 1) {
                    buffer.append(SPACE);
                }
            } else if (i < allCount - 1) {
                if (prefs.beforeComma)
                    buffer.append(SPACE);
                buffer.append(COMMA);
                if (prefs.afterComma)
                    buffer.append(SPACE);
            }

        }

        return buffer.toString();
    }

    private boolean lastArgIsClosure(char[][] regularparameterTypes, char[][] namedParameterTypes) {
        char[] lastArgType;
        if (namedParameterTypes != null && namedParameterTypes.length > 0) {
            lastArgType = namedParameterTypes[namedParameterTypes.length - 1];
        } else if (regularparameterTypes != null && regularparameterTypes.length > 0) {
            lastArgType = regularparameterTypes[regularparameterTypes.length - 1];
        } else {
            // no args
            return false;
        }

        // we should be comparing against a fully qualified type name, but it is not always available
        // so a simple name is close enough
        return CharOperation.equals("Closure".toCharArray(), lastArgType);
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

    private ICompletionProposal[][] guessParameters(char[][] regularParameterNames, char[][] namedParameterNames)
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

        char[][] parameterNames = new char[regularParameterNames.length + namedParameterNames.length][];
        System.arraycopy(regularParameterNames, 0, parameterNames, 0, regularParameterNames.length);
        System.arraycopy(namedParameterNames, 0, parameterNames, regularParameterNames.length, namedParameterNames.length);

        int count = parameterNames.length;
        fPositions = new Position[count];
        fChoices = new ICompletionProposal[count][];

        String[] parameterTypes = getParameterTypes();

        ParameterGuesser guesser = new ParameterGuesser(getEnclosingElement());
        IJavaElement[][] assignableElements = getAssignableElements();

        for (int i = count - 1; i >= 0; i--) {
            String paramName = new String(parameterNames[i]);
            Position position = new Position(0, 0);

            ICompletionProposal[] argumentProposals = parameterProposals(guesser, parameterTypes[i], paramName, position,
                    assignableElements[i]);
            if (argumentProposals.length == 0)
                argumentProposals = new ICompletionProposal[] { new JavaCompletionProposal(paramName, 0, paramName.length(), null,
                        paramName, 0) };

            fPositions[i] = position;
            fChoices[i] = argumentProposals;
        }

        return fChoices;
    }

    // unfortunately, the parameterProposals method has a different signature in
    // 3.6 and 3.7.
    // so must call using reflection
    private ICompletionProposal[] parameterProposals(ParameterGuesser guesser, String parameterType, String paramName,
            Position position, IJavaElement[] assignable) {
        parameterType = convertToPrimitive(parameterType);

        Method method = findParameterProposalsMethod();
        try {
            if (method.getParameterTypes().length == 5) {
                // 3.6
                return (ICompletionProposal[]) method.invoke(guesser, parameterType, paramName, position, assignable,
                        fFillBestGuess);
            } else {
                // 3.7
                return (ICompletionProposal[]) method.invoke(guesser, parameterType, paramName, position, assignable,
                        fFillBestGuess, false);
            }
        } catch (Exception e) {
            GroovyCore.logException("Exception trying to reflectively invoke 'parameterProposals' method.", e);
            return new ICompletionProposal[0];
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
