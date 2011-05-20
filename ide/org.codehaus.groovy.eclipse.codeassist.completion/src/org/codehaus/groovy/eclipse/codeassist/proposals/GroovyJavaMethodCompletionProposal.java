package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ProposalContextInformation;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * @author Andrew Eisenberg
 * @created Aug 11, 2009
 *
 */
public class GroovyJavaMethodCompletionProposal extends JavaMethodCompletionProposal {
    private static final String CLOSURE_TEXT = "{  }";

    private int[] fArgumentOffsets;
    private int[] fArgumentLengths;
    private IRegion fSelectedRegion; // initialized by apply()

    private final ProposalFormattingOptions proposalOptions;
    private String contributor;

    public GroovyJavaMethodCompletionProposal(CompletionProposal proposal,
            JavaContentAssistInvocationContext context, ProposalFormattingOptions groovyFormatterPrefs) {
        super(proposal, context);
        this.proposalOptions = groovyFormatterPrefs;
        this.contributor = "Groovy";
        this.setRelevance(proposal.getRelevance());
    }
    public GroovyJavaMethodCompletionProposal(CompletionProposal proposal,
            JavaContentAssistInvocationContext context, ProposalFormattingOptions groovyFormatterPrefs, String contributor) {
        this(proposal, context, groovyFormatterPrefs);
        this.contributor = contributor;
    }


    @Override
    protected StyledString computeDisplayString() {
        return super.computeDisplayString().append(getStyledGroovy());
    }

    @Override
    protected IContextInformation computeContextInformation() {
        if ((fProposal.getKind() == CompletionProposal.METHOD_REF ||
                fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION) && hasParameters()) {
            ProposalContextInformation contextInformation= new ProposalContextInformation(fProposal);
            if (fContextInformationPosition != 0 && fProposal.getCompletion().length == 0)
                contextInformation.setContextInformationPosition(fContextInformationPosition);
            return contextInformation;
        }
        return super.computeContextInformation();
    }


    private StyledString getStyledGroovy() {
        return new StyledString(" (" + contributor + ")", StyledString.DECORATIONS_STYLER);
    }

    /*
     * @see ICompletionProposalExtension#apply(IDocument, char)
     */
    @Override
    public void apply(IDocument document, char trigger, int offset) {
        super.apply(document, trigger, offset);
        int baseOffset= getReplacementOffset();
        String replacement= getReplacementString();
        fSelectedRegion = new Region(baseOffset + replacement.length(), 0);
    }


    @Override
    protected void setUpLinkedMode(IDocument document, char closingCharacter) {
        if (fArgumentOffsets != null && getTextViewer() != null) {
            int baseOffset = getReplacementOffset();
            String replacement = getReplacementString();
            try {
                LinkedModeModel model= new LinkedModeModel();
                for (int i= 0; i != fArgumentOffsets.length; i++) {
                    LinkedPositionGroup group= new LinkedPositionGroup();
                    group.addPosition(new LinkedPosition(document, baseOffset + fArgumentOffsets[i], fArgumentLengths[i], LinkedPositionGroup.NO_STOP));
                    model.addGroup(group);
                }

                model.forceInstall();
                JavaEditor editor= getJavaEditor();
                if (editor != null) {
                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
                }

                LinkedModeUI ui= new EditorLinkedModeUI(model, getTextViewer());
                ui.setExitPosition(getTextViewer(), baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
                ui.setExitPolicy(new ExitPolicy(')', document));
                ui.setDoContextInfo(true);
                ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
                ui.enter();

                fSelectedRegion= ui.getSelectedRegion();

            } catch (BadLocationException e) {
                JavaPlugin.log(e);
                openErrorDialog(e);
            }
        }
    }

    /**
     * Groovify this replacement string
     * Remove parens if this method has parameters
     * If the first parameter is a closure, then use { }
     */
    @Override
    protected String computeReplacementString() {
        // with no arguments, there is nothing groovy to do.
        if (!hasArgumentList() || !hasArgumentList()) {
            return super.computeReplacementString();
        }

        // we're inserting a method plus the argument list - respect formatter preferences
        StringBuffer buffer= new StringBuffer();
        char[] proposalName = fProposal.getName();
        boolean hasWhitespace = false;
        for (int i = 0; i < proposalName.length; i++) {
            if (CharOperation.isWhitespace(proposalName[i])) {
                hasWhitespace = true;
            }
        }
        char[] newProposalName;
        if (hasWhitespace) {
            newProposalName = CharOperation.concat(new char[] {'"'}, CharOperation.append(proposalName, '"'));
        } else {
            newProposalName = proposalName;
        }
        fProposal.setName(newProposalName);
        appendMethodNameReplacement(buffer);
        fProposal.setName(proposalName);
        FormatterPrefs prefs= getFormatterPrefs();

        if (hasParameters()) {

            int indexOfLastClosure = -1;
            char[][] parameterTypes = Signature.getParameterTypes(fProposal.getSignature());
            if (proposalOptions.noParensAroundClosures) {
                if (lastArgIsClosure(parameterTypes)) {
                    indexOfLastClosure = parameterTypes.length - 1;
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
            char[][] parameterNames= fProposal.findParameterNames(null);
            int count= parameterNames.length;
            fArgumentOffsets= new int[count];
            fArgumentLengths= new int[count];

            for (int i= 0; i != count; i++) {
                if (proposalOptions.useNamedArguments) {
                    buffer.append(parameterNames[i]).append(":");
                }
				fArgumentOffsets[i] = buffer.length();
                if (i == 0) {
                    setCursorPosition(buffer.length());
                }
                // handle the argument name
                if (proposalOptions.useBracketsForClosures
                        && CharOperation.equals("Closure".toCharArray(), Signature.getSignatureSimpleName(parameterTypes[i]))) {
                    // closure
                    fArgumentOffsets[i] = buffer.length() + 2;
                    fArgumentLengths[i] = 0;
                    buffer.append(CLOSURE_TEXT);

                } else {
                    // regular argument
                    fArgumentOffsets[i] = buffer.length();
                    buffer.append(parameterNames[i]);
                    fArgumentLengths[i] = parameterNames[i].length;
                }

                if (i == indexOfLastClosure - 1 || (i != indexOfLastClosure && i == count - 1)) {
                    if (prefs.beforeClosingParen) {
                        buffer.append(SPACE);
                    }
                    buffer.append(RPAREN);
                    if (i == indexOfLastClosure - 1) {
                        buffer.append(SPACE);
                    }
                } else if (i < count - 1) {
                    if (prefs.beforeComma)
                        buffer.append(SPACE);
                    buffer.append(COMMA);
                    if (prefs.afterComma)
                        buffer.append(SPACE);
                }

            }
        } else {
            if (prefs.inEmptyList) {
                buffer.append(SPACE);
            }
            buffer.append(RPAREN);
        }

        return buffer.toString();
    }

    private boolean lastArgIsClosure(char[][] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return false;
        }

        return CharOperation.equals("Closure".toCharArray(),
                Signature.getSignatureSimpleName(parameterTypes[parameterTypes.length - 1]));
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#needsLinkedMode()
     */
    @Override
    protected boolean needsLinkedMode() {
        return super.needsLinkedMode(); // we handle it ourselves
        // return false; // we handle it ourselves
    }



    /**
     * Returns the currently active java editor, or <code>null</code> if it
     * cannot be determined.
     *
     * @return  the currently active java editor, or <code>null</code>
     */
    private JavaEditor getJavaEditor() {
        IEditorPart part= JavaPlugin.getActivePage().getActiveEditor();
        if (part instanceof JavaEditor)
            return (JavaEditor) part;
        else
            return null;
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

    private void openErrorDialog(BadLocationException e) {
        Shell shell= getTextViewer().getTextWidget().getShell();
        MessageDialog.openError(shell, "Error inserting parameters", e.getMessage());
    }

}