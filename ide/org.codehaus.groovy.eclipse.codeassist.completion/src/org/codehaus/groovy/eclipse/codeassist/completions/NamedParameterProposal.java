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

import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * A content assist proposal for named parameters.
 *
 * @author andrew
 * @created Sep 9, 2011
 */
public class NamedParameterProposal extends JavaCompletionProposal {

    private static final ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];

    private IRegion selectedRegion;

    private final CompletionContext coreContext;

    private final boolean tryParamGuessing;

    private ICompletionProposal[] choices;

    private Position paramNamePosition;

    private IPositionUpdater updater;

    private String paramSignature;

    private final String paramName;

    public NamedParameterProposal(String paramName, String paramSignature, int replacementOffset, int replacementLength,
            Image image,
            StyledString displayString, int relevance, boolean inJavadoc, JavaContentAssistInvocationContext invocationContext,
            boolean tryParamGuessing) {
        super(computeReplacementString(paramName), replacementOffset, replacementLength, image, displayString,
                relevance, inJavadoc,
                invocationContext);
        this.tryParamGuessing = tryParamGuessing;
        coreContext = invocationContext.getCoreContext();
        this.paramName = paramName;
        this.paramSignature = paramSignature;
        this.setTriggerCharacters(ProposalUtils.VAR_TRIGGER);
    }

    private String computeReplacementChoices(String name) {
        if (shouldDoGuessing()) {
            try {
                choices = guessParameters(paramName.toCharArray());
            } catch (JavaModelException e) {
                paramNamePosition = null;
                choices = null;
                JavaPlugin.log(e);
                openErrorDialog(e);
            }
        }
        return computeReplacementString(name);
    }

    private static String computeReplacementString(String name) {
        return name + ": __, ";
    }

    private IRegion calculateArgumentRegion() {
        String repl = getReplacementString();
        int replOffset = getReplacementOffset();
        int start = replOffset + repl.indexOf(": ") + 2;
        int end = replOffset + repl.indexOf(",");
        if (start > 0 && end > 0) {
            return new Region(start, end - start);
        } else {
            // couldn't find region
            return null;
        }
    }

    /**
     * @return true iff parameter guessing should be performed.
     */
    private boolean shouldDoGuessing() {
        return tryParamGuessing && coreContext.isExtended();
    }

    private ICompletionProposal[] guessParameters(char[] parameterName) throws JavaModelException {
        if (paramSignature == null) {
            return NO_COMPLETIONS;
        }
        String type = Signature.toString(paramSignature);
        IJavaElement[] assignableElements = getAssignableElements();
        Position position = new Position(selectedRegion.getOffset(), selectedRegion.getLength());
        ICompletionProposal[] argumentProposals = new ParameterGuesserDelegate(getEnclosingElement()).parameterProposals(type,
                paramName, position, assignableElements, tryParamGuessing);
        if (argumentProposals.length == 0) {
            argumentProposals = new ICompletionProposal[] { new JavaCompletionProposal(paramName, 0, paramName.length(), null,
                    paramName, 0) };
        }
        paramNamePosition = position;
        return choices = argumentProposals;
    }


    private IJavaElement getEnclosingElement() {
        return coreContext.getEnclosingElement();
    }

    private IJavaElement[] getAssignableElements() {
        return coreContext.getVisibleElements(paramSignature);
    }

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        super.apply(document, trigger, offset);
        if (selectedRegion == null) {
            selectedRegion = calculateArgumentRegion();
        }
        setUpLinkedMode(document, ',');
    }

    @Override
    protected void setUpLinkedMode(IDocument document, char closingCharacter) {
        ITextViewer textViewer = getTextViewer();
        if (textViewer != null) {
            int baseOffset = getReplacementOffset();
            String replacement = computeReplacementChoices(paramName);
            try {
                LinkedModeModel model = new LinkedModeModel();
                IRegion argRegion = calculateArgumentRegion();
                LinkedPositionGroup group = new LinkedPositionGroup();
                if (shouldDoGuessing()) {
                    ensurePositionCategoryInstalled(document, model);
                    document.addPosition(getCategory(), paramNamePosition);
                    group.addPosition(new ProposalPosition(document, paramNamePosition.getOffset(), paramNamePosition
                            .getLength(), LinkedPositionGroup.NO_STOP, choices));
                } else {
                    group.addPosition(new LinkedPosition(document, argRegion.getOffset(), argRegion.getLength(),
                            LinkedPositionGroup.NO_STOP));
                }
                model.addGroup(group);

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

                selectedRegion = ui.getSelectedRegion();

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
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    @Override
    public Point getSelection(IDocument document) {
        if (selectedRegion == null) {
            return new Point(getReplacementOffset(), 0);
        } else {
            return new Point(selectedRegion.getOffset(), selectedRegion.getLength());
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
        if (part instanceof JavaEditor) {
            return (JavaEditor) part;
        } else {
            return null;
        }
    }

    private void openErrorDialog(Exception e) {
        Shell shell = getTextViewer().getTextWidget().getShell();
        MessageDialog.openError(shell, "Error inserting parameters", e.getMessage());
    }

    private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
        if (!document.containsPositionCategory(getCategory())) {
            document.addPositionCategory(getCategory());
            updater = new InclusivePositionUpdater(getCategory());
            document.addPositionUpdater(updater);

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
            document.removePositionUpdater(updater);
        }
    }

    private String getCategory() {
        return "ParameterGuessingProposal_" + toString(); //$NON-NLS-1$
    }

    /**
     * Not API, for testing only.
     *
     * @return
     * @throws JavaModelException
     */
    public ICompletionProposal[] getChoices() throws JavaModelException {
        selectedRegion = calculateArgumentRegion();
        return guessParameters(paramName.toCharArray());
    }
}