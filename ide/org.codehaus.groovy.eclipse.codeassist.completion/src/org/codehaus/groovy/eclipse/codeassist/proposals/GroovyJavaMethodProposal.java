package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
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
 * Largely copied from FilledArgumentNamesMethodProposal
 * @author Andrew Eisenberg
 * @created Aug 11, 2009
 *
 */
public class GroovyJavaMethodProposal extends JavaMethodCompletionProposal {

    private int[] fArgumentOffsets;
    private int[] fArgumentLengths;
    private IRegion fSelectedRegion; // initialized by apply()

    public GroovyJavaMethodProposal(CompletionProposal proposal,
            JavaContentAssistInvocationContext context) {
        super(proposal, context);
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
        return new StyledString(" (Groovy)", StyledString.DECORATIONS_STYLER);
    }
    
    /*
     * @see ICompletionProposalExtension#apply(IDocument, char)
     */
    public void apply(IDocument document, char trigger, int offset) {
        super.apply(document, trigger, offset);
        int baseOffset= getReplacementOffset();
        String replacement= getReplacementString();

        if (fArgumentOffsets != null && getTextViewer() != null) {
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
        } else {
            fSelectedRegion= new Region(baseOffset + replacement.length(), 0);
        }
    }


    /**
     * Groovify this replacement string
     * Remove parens if this method has parameters
     * If the first parameter is a closure, then use { }
     */
    @Override
    protected String computeReplacementString() {
        if (!hasArgumentList()) {
            return super.computeReplacementString();
        }
        
        // we're inserting a method plus the argument list - respect formatter preferences
        StringBuffer buffer= new StringBuffer();
        appendMethodNameReplacement(buffer);

        FormatterPrefs prefs= getFormatterPrefs();

        if (hasParameters()) {
            // remove the openning paren
            buffer.replace(buffer.length()-1, buffer.length(), "");
            // add space if not already there
            if (!prefs.beforeOpeningParen) {
                buffer.append(SPACE);
            }
            
            // now add the parameters
            char[][] parameterNames= fProposal.findParameterNames(null);
            char[][] parameterTypes = Signature.getParameterTypes(fProposal.getSignature());
            int count= parameterNames.length;
            fArgumentOffsets= new int[count];
            fArgumentLengths= new int[count];

            for (int i= 0; i != count; i++) {
                if (i != 0) {
                    if (prefs.beforeComma) {
                        buffer.append(SPACE);
                    }
                    buffer.append(COMMA);
                    if (prefs.afterComma) {
                        buffer.append(SPACE);
                    }
                }

                fArgumentOffsets[i]= buffer.length();
                
                if (new String(Signature.getSignatureSimpleName(parameterTypes[i])).equals("Closure")) {
                    buffer.append("{ }");
                    fArgumentLengths[i] = 3;
                    if (i == 0) {
                        setCursorPosition(buffer.length()-2);
                    }
                    
                } else {
                    if (i == 0) {
                        setCursorPosition(buffer.length());
                    }
                    buffer.append(parameterNames[i]);
                    fArgumentLengths[i] = parameterNames[i].length;
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
    
    /*
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#needsLinkedMode()
     */
    protected boolean needsLinkedMode() {
        return false; // we handle it ourselves
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