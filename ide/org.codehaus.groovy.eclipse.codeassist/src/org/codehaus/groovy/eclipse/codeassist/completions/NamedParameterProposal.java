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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.template.contentassist.PositionBasedCompletionProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * A content assist proposal for named parameters.
 */
public class NamedParameterProposal extends JavaCompletionProposal {

    private ICompletionProposal[] choices;
    private IPositionUpdater updater;
    private IRegion selectedRegion;

    private final String paramName;
    private final String paramSignature;

    public NamedParameterProposal(
        String paramName,
        String paramSignature,
        int replacementOffset,
        int replacementLength,
        Image image,
        StyledString displayString,
        int relevance,
        boolean inJavadoc,
        JavaContentAssistInvocationContext javaContext) {

        super(computeReplacementString(paramName), replacementOffset, replacementLength, image, displayString, relevance, inJavadoc, javaContext);

        this.paramName = paramName;
        this.paramSignature = paramSignature;
        this.setTriggerCharacters(ProposalUtils.VAR_TRIGGER);
    }

    private static String computeReplacementString(String name) {
        return (name + ": __");
    }

    private IRegion calculateArgumentRegion() {
        String repl = getReplacementString();
        int replOffset = getReplacementOffset();
        int start = replOffset + repl.indexOf(": ") + 2;
        int end = replOffset + repl.lastIndexOf("_") + 1;
        if (start > 0 && end > 0) {
            return new Region(start, end - start);
        }
        // couldn't find region
        return null;
    }

    /**
     * @return {@code true} iff parameter guessing should be performed
     */
    private boolean shouldDoGuessing() {
        if (fInvocationContext.getCoreContext().isExtended()) {
            IPreferenceStore prefs = JavaPlugin.getDefault().getPreferenceStore();
            return (prefs.getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES) &&
                    prefs.getBoolean(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS));
        }
        return false;
    }

    private ICompletionProposal[] doGuessing(Position position) {
        if (paramName == null || paramSignature == null) {
            return ProposalUtils.NO_COMPLETIONS;
        }

        IJavaElement[] visibleElements = fInvocationContext.getCoreContext().getVisibleElements(paramSignature);
        ParameterGuesserDelegate guesser = new ParameterGuesserDelegate(fInvocationContext.getCoreContext().getEnclosingElement(), fInvocationContext);
        ICompletionProposal[] guesses = guesser.parameterProposals(Signature.toString(paramSignature), paramName, position, visibleElements, true);
        if (guesses != null && guesses.length > 0) {
            for (int i = 0; i < guesses.length; i += 1) {
                guesses[i] = new PositionBasedCompletionProposal(guesses[i].getDisplayString(), position, guesses[i].getSelection(null).x - position.getOffset(), guesses[i].getImage(), guesses[i].getDisplayString(), guesses[i].getContextInformation(), null, ((PositionBasedCompletionProposal) guesses[i]).getTriggerCharacters()) {
                    @Override
                    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
                        super.apply(viewer, trigger, stateMask, offset);
                        if (trigger > 0)
                        try {
                            offset += getDisplayString().length();
                            viewer.getDocument().replace(offset, 0, String.valueOf(trigger));
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                        // TODO: exit linked mode
                    }
                };
            }
        } else {
            guesses = new ICompletionProposal[] {
                new JavaCompletionProposal(paramName, 0, paramName.length(), null, paramName, 0)
            };
        }
        return guesses;
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
            try {
                LinkedModeModel model = new LinkedModeModel();
                LinkedPositionGroup group = new LinkedPositionGroup();
                if (shouldDoGuessing()) {
                    Position position = new Position(selectedRegion.getOffset(), selectedRegion.getLength());
                    ensurePositionCategoryInstalled(document, model);
                    document.addPosition(getCategory(), position);
                    choices = doGuessing(position);

                    group.addPosition(new ProposalPosition(document, position.getOffset(), position.getLength(), LinkedPositionGroup.NO_STOP, choices));
                } else {
                    IRegion argRegion = calculateArgumentRegion();
                    group.addPosition(new LinkedPosition(document, argRegion.getOffset(), argRegion.getLength(), LinkedPositionGroup.NO_STOP));
                }
                model.addGroup(group);

                JavaEditor editor = getJavaEditor();
                if (editor != null) {
                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
                }
                model.forceInstall();

                LinkedModeUI ui = new EditorLinkedModeUI(model, textViewer);
                ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
                ui.setDoContextInfo(true);
                ui.setExitPolicy((LinkedModeModel m, VerifyEvent e, int off, int len) -> {
                    if (e.character == closingCharacter) {
                        return new ExitFlags(ILinkedModeListener.UPDATE_CARET, true);
                    } else if (e.character == ';' || e.character == '\r') {
                        return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
                    }
                    return null;
                });
                ui.setExitPosition(textViewer, getReplacementOffset() + getReplacementString().length(), 0, Integer.MAX_VALUE);
                ui.setSimpleMode(true);
                ui.enter();

                selectedRegion = ui.getSelectedRegion();

            } catch (Exception e) {
                ensurePositionCategoryRemoved(document);
                GroovyContentAssist.logError(e);
                openErrorDialog(e);
                choices = null;
            }
        }
    }

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
                @Override
                public void left(LinkedModeModel environment, int flags) {
                    ensurePositionCategoryRemoved(document);
                }

                @Override
                public void suspend(LinkedModeModel environment) {
                }

                @Override
                public void resume(LinkedModeModel environment, int flags) {
                }
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
            updater = null;
        }
    }

    private String getCategory() {
        return "ParameterGuessingProposal_" + toString();
    }

    /**
     * Not API, for testing only.
     */
    public ICompletionProposal[] getChoices() throws JavaModelException {
        selectedRegion = calculateArgumentRegion();
        return doGuessing(new Position(selectedRegion.getOffset(), selectedRegion.getLength()));
    }
}
