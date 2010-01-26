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
package org.codehaus.groovy.eclipse.editor;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.highlighting.GroovySemanticReconciler;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatAllGroovyAction;
import org.codehaus.groovy.eclipse.refactoring.actions.GroovyRenameAction;
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImportsAction;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatAllGroovyAction.FormatKind;
import org.codehaus.groovy.eclipse.ui.decorators.GroovyImageDecorator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.debug.ui.BreakpointMarkerUpdater;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.Symbols;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.actions.GenerateActionGroup;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.RefactorActionGroup;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

public class GroovyEditor extends CompilationUnitEditor {
    public static final String EDITOR_ID = "org.codehaus.groovy.eclipse.editor.GroovyEditor";

    /**
     * Borrowed from {@link CompilationUnitEditor.ExclusivePositionUpdater} 
     * Position updater that takes any changes at the borders of a position to not belong to the position.
     *
     * @since 3.0
     */
    private static class GroovyExclusivePositionUpdater implements IPositionUpdater {

        /** The position category. */
        private final String fCategory;

        /**
         * Creates a new updater for the given <code>category</code>.
         *
         * @param category the new category.
         */
        public GroovyExclusivePositionUpdater(String category) {
            fCategory= category;
        }

        /*
         * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
         */
        public void update(DocumentEvent event) {

            int eventOffset= event.getOffset();
            int eventOldLength= event.getLength();
            int eventNewLength= event.getText() == null ? 0 : event.getText().length();
            int deltaLength= eventNewLength - eventOldLength;

            try {
                Position[] positions= event.getDocument().getPositions(fCategory);

                for (int i= 0; i != positions.length; i++) {

                    Position position= positions[i];

                    if (position.isDeleted())
                        continue;

                    int offset= position.getOffset();
                    int length= position.getLength();
                    int end= offset + length;

                    if (offset >= eventOffset + eventOldLength)
                        // position comes
                        // after change - shift
                        position.setOffset(offset + deltaLength);
                    else if (end <= eventOffset) {
                        // position comes way before change -
                        // leave alone
                    } else if (offset <= eventOffset && end >= eventOffset + eventOldLength) {
                        // event completely internal to the position - adjust length
                        position.setLength(length + deltaLength);
                    } else if (offset < eventOffset) {
                        // event extends over end of position - adjust length
                        int newEnd= eventOffset;
                        position.setLength(newEnd - offset);
                    } else if (end > eventOffset + eventOldLength) {
                        // event extends from before position into it - adjust offset
                        // and length
                        // offset becomes end of event, length adjusted accordingly
                        int newOffset= eventOffset + eventNewLength;
                        position.setOffset(newOffset);
                        position.setLength(end - newOffset);
                    } else {
                        // event consumes the position - delete it
                        position.delete();
                    }
                }
            } catch (BadPositionCategoryException e) {
                // ignore and return
            }
        }

    }

    /**
     * Borrowed from {@link CompilationUnitEditor.ExitPolicy} 
     */
    @SuppressWarnings("unchecked")
    private class GroovyExitPolicy implements IExitPolicy {

        final char fExitCharacter;
        final char fEscapeCharacter;
        final Stack fStack;
        final int fSize;

        public GroovyExitPolicy(char exitCharacter, char escapeCharacter, Stack stack) {
            fExitCharacter= exitCharacter;
            fEscapeCharacter= escapeCharacter;
            fStack= stack;
            fSize= fStack.size();
        }

        /*
         * @see org.eclipse.jdt.internal.ui.text.link.LinkedPositionUI.ExitPolicy#doExit(org.eclipse.jdt.internal.ui.text.link.LinkedPositionManager, org.eclipse.swt.events.VerifyEvent, int, int)
         */
        public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {

            if (fSize == fStack.size() && !isMasked(offset)) {
                if (event.character == fExitCharacter) {
                    GroovyBracketLevel level= (GroovyBracketLevel) fStack.peek();
                    if (level.fFirstPosition.offset > offset || level.fSecondPosition.offset < offset)
                        return null;
                    if (level.fSecondPosition.offset == offset && length == 0)
                        // don't enter the character if if its the closing peer
                        return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
                }
                // when entering an anonymous class between the parenthesis', we don't want
                // to jump after the closing parenthesis when return is pressed
                if (event.character == SWT.CR && offset > 0) {
                    IDocument document= getSourceViewer().getDocument();
                    try {
                        if (document.getChar(offset - 1) == '{')
                            return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
                    } catch (BadLocationException e) {
                    }
                }
            }
            return null;
        }

        private boolean isMasked(int offset) {
            IDocument document= getSourceViewer().getDocument();
            try {
                return fEscapeCharacter == document.getChar(offset - 1);
            } catch (BadLocationException e) {
            }
            return false;
        }
    }

    /**
     * Borrowed from {@link CompilationUnitEditor.BracketLevel}
     */
    private static class GroovyBracketLevel {
        LinkedModeUI fUI;
        Position fFirstPosition;
        Position fSecondPosition;
    }

    /**
     * Borrowed from {@link CompilationUnitEditor.BracketInserter}
     * 
     * Changes marked with // GROOVY
     */
    @SuppressWarnings("unchecked")
    private class GroovyBracketInserter implements VerifyKeyListener, ILinkedModeListener { 

        private boolean fCloseBrackets= true;
        private boolean fCloseStrings= true;
        private boolean fCloseAngularBrackets= true;
        private final String CATEGORY= toString();
        private final IPositionUpdater fUpdater= new GroovyExclusivePositionUpdater(CATEGORY);
        private final Stack fBracketLevelStack= new Stack();

        public void setCloseBracketsEnabled(boolean enabled) {
            fCloseBrackets= enabled;
        }

        public void setCloseStringsEnabled(boolean enabled) {
            fCloseStrings= enabled;
        }

        public void setCloseAngularBracketsEnabled(boolean enabled) {
            fCloseAngularBrackets= enabled;
        }

        private boolean isAngularIntroducer(String identifier) {
            return identifier.length() > 0
                    && (Character.isUpperCase(identifier.charAt(0))
                            || identifier.startsWith("final") //$NON-NLS-1$
                            || identifier.startsWith("public") //$NON-NLS-1$
                            || identifier.startsWith("public") //$NON-NLS-1$
                            || identifier.startsWith("protected") //$NON-NLS-1$
                            || identifier.startsWith("private")); //$NON-NLS-1$
        }
        
        private boolean isMultilineSelection() {
            ISelection selection= getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection) {
                ITextSelection ts= (ITextSelection) selection;
                return  ts.getStartLine() != ts.getEndLine();
            }
            return false;
        }

        /*
         * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
         */
        public void verifyKey(VerifyEvent event) {

            // early pruning to slow down normal typing as little as possible
            if (!event.doit || getInsertMode() != SMART_INSERT || isBlockSelectionModeEnabled() && isMultilineSelection())
                return;
            switch (event.character) {
                case '(':
                case '<':
                case '[':
                case '\'':
                case '\"':
                    break;
                default:
                    return;
            }

            final ISourceViewer sourceViewer= getSourceViewer();
            IDocument document= sourceViewer.getDocument();

            final Point selection= sourceViewer.getSelectedRange();
            int offset= selection.x;
            final int length= selection.y;

            try {
                IRegion startLine= document.getLineInformationOfOffset(offset);
                IRegion endLine= document.getLineInformationOfOffset(offset + length);

                JavaHeuristicScanner scanner= new JavaHeuristicScanner(document);
                int nextToken= scanner.nextToken(offset + length, endLine.getOffset() + endLine.getLength());
                String next= nextToken == Symbols.TokenEOF ? null : document.get(offset, scanner.getPosition() - offset).trim();
                int prevToken= scanner.previousToken(offset - 1, startLine.getOffset());
                int prevTokenOffset= scanner.getPosition() + 1;
                String previous= prevToken == Symbols.TokenEOF ? null : document.get(prevTokenOffset, offset - prevTokenOffset).trim();

                switch (event.character) {
                    case '(':
                        if (!fCloseBrackets
                                || nextToken == Symbols.TokenLPAREN
                                || nextToken == Symbols.TokenIDENT
                                || next != null && next.length() > 1)
                            return;
                        break;

                    case '<':
                        if (!(fCloseAngularBrackets && fCloseBrackets)
                                || nextToken == Symbols.TokenLESSTHAN
                                ||         prevToken != Symbols.TokenLBRACE
                                        && prevToken != Symbols.TokenRBRACE
                                        && prevToken != Symbols.TokenSEMICOLON
                                        && prevToken != Symbols.TokenSYNCHRONIZED
                                        && prevToken != Symbols.TokenSTATIC
                                        && (prevToken != Symbols.TokenIDENT || !isAngularIntroducer(previous))
                                        && prevToken != Symbols.TokenEOF)
                            return;
                        break;

                    case '[':
                        if (!fCloseBrackets
                                || nextToken == Symbols.TokenIDENT
                                || next != null && next.length() > 1)
                            return;
                        break;

                    case '\'':
                    case '"':
                        // GROOVY change, allow quote closing when there are no parens
                        if (!fCloseStrings
                                || nextToken == Symbols.TokenIDENT
//                                || prevToken == Symbols.TokenIDENT
                                || next != null && next.length() > 1
//                                || previous != null && previous.length() > 1
                                )
                            // GROOVY end change
                            return;
                        break;

                    default:
                        return;
                }

                ITypedRegion partition= TextUtilities.getPartition(document, IJavaPartitions.JAVA_PARTITIONING, offset, true);
                if (!IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType()) && 
                    !GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS.equals(partition.getType()) &&  // GROOVY change 
                    !shouldCloseTripleQuotes(document, offset, partition, getPeerCharacter(event.character))) { // GROOVY change
                    return;
                }

                if (!validateEditorInputState())
                    return;

                final char character= event.character;
                final char closingCharacter= getPeerCharacter(character);
                final StringBuffer buffer= new StringBuffer();
                buffer.append(character);
                buffer.append(closingCharacter);
                
                // GROOVY special case: multiline strings
                // Insert the closing of a triple quoted string whenever
                // there is a "" or a """ before 
                int insertedLength = 1;
                if (fCloseStrings && offset > 1) {
                    String start = document.get(offset-2, 2);
                    boolean doit = false;
                    if (event.character == closingCharacter) {
                        doit = start.equals(Character.toString(closingCharacter) + closingCharacter);
                    }
                    if (doit) {
                        buffer.append(closingCharacter);
                        insertedLength ++;
                        // now check for a preexisting third quote
                        insertedLength ++;
                        if (offset > 2 && document.getChar(offset-3) == closingCharacter) {
                            offset--;
                        } else {
                            // if no third quote already, must add another
                            buffer.append(closingCharacter);
                        }
                    }
                }
                // GROOVY end

                document.replace(offset, length, buffer.toString());


                GroovyBracketLevel level= new GroovyBracketLevel();
                fBracketLevelStack.push(level);

                LinkedPositionGroup group= new LinkedPositionGroup();
//                group.addPosition(new LinkedPosition(document, offset + 1, 0, LinkedPositionGroup.NO_STOP));
                group.addPosition(new LinkedPosition(document, offset + insertedLength, 0, LinkedPositionGroup.NO_STOP)); // GROOVY change

                LinkedModeModel model= new LinkedModeModel();
                model.addLinkingListener(this);
                model.addGroup(group);
                model.forceInstall();

                // set up position tracking for our magic peers
                if (fBracketLevelStack.size() == 1) {
                    document.addPositionCategory(CATEGORY);
                    document.addPositionUpdater(fUpdater);
                }
                level.fFirstPosition= new Position(offset, 1);
//                level.fSecondPosition= new Position(offset + 1, 1);
                level.fSecondPosition= new Position(offset + insertedLength, 1); // GROOVY change 
                document.addPosition(CATEGORY, level.fFirstPosition);
                document.addPosition(CATEGORY, level.fSecondPosition);

                level.fUI= new EditorLinkedModeUI(model, sourceViewer);
                level.fUI.setSimpleMode(true);
                level.fUI.setExitPolicy(new GroovyExitPolicy(closingCharacter, getEscapeCharacter(closingCharacter), fBracketLevelStack));
//                level.fUI.setExitPosition(sourceViewer, offset + 2, 0, Integer.MAX_VALUE);
                level.fUI.setExitPosition(sourceViewer, offset + 1 + insertedLength, 0, Integer.MAX_VALUE); // GROOVY change
                level.fUI.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
                level.fUI.enter();


                IRegion newSelection= level.fUI.getSelectedRegion();
//                sourceViewer.setSelectedRange(newSelection.getOffset(), newSelection.getLength());
                sourceViewer.setSelectedRange(newSelection.getOffset() - insertedLength + 1, newSelection.getLength()); // GROOVY change

                event.doit= false;

            } catch (BadLocationException e) {
                JavaPlugin.log(e);
            } catch (BadPositionCategoryException e) {
                JavaPlugin.log(e);
            }
        }
        /**
         * @param document
         * @param offset
         * @param partition 
         * @return
         * @throws BadLocationException 
         */
        private boolean shouldCloseTripleQuotes(IDocument document, int offset, ITypedRegion partition, char quote) throws BadLocationException {
            if (offset < 3 || !IJavaPartitions.JAVA_STRING.equals(partition.getType())) {
                return false;
            }
            String maybequotes = document.get(offset-3, 3);
            return maybequotes.equals(Character.toString(quote)+quote+quote);
        }

        /*
         * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
         */
        public void left(LinkedModeModel environment, int flags) {

            final GroovyBracketLevel level= (GroovyBracketLevel) fBracketLevelStack.pop();

            if (flags != ILinkedModeListener.EXTERNAL_MODIFICATION)
                return;

            // remove brackets
            final ISourceViewer sourceViewer= getSourceViewer();
            final IDocument document= sourceViewer.getDocument();
            if (document instanceof IDocumentExtension) {
                IDocumentExtension extension= (IDocumentExtension) document;
                extension.registerPostNotificationReplace(null, new IDocumentExtension.IReplace() {

                    public void perform(IDocument d, IDocumentListener owner) {
                        if ((level.fFirstPosition.isDeleted || level.fFirstPosition.length == 0)
                                && !level.fSecondPosition.isDeleted
                                && level.fSecondPosition.offset == level.fFirstPosition.offset)
                        {
                            try {
                                document.replace(level.fSecondPosition.offset,
                                                 level.fSecondPosition.length,
                                                 ""); //$NON-NLS-1$
                            } catch (BadLocationException e) {
                                JavaPlugin.log(e);
                            }
                        }

                        if (fBracketLevelStack.size() == 0) {
                            document.removePositionUpdater(fUpdater);
                            try {
                                document.removePositionCategory(CATEGORY);
                            } catch (BadPositionCategoryException e) {
                                JavaPlugin.log(e);
                            }
                        }
                    }
                });
            }


        }

        /*
         * @see org.eclipse.jface.text.link.ILinkedModeListener#suspend(org.eclipse.jface.text.link.LinkedModeModel)
         */
        public void suspend(LinkedModeModel environment) {
        }

        /*
         * @see org.eclipse.jface.text.link.ILinkedModeListener#resume(org.eclipse.jface.text.link.LinkedModeModel, int)
         */
        public void resume(LinkedModeModel environment, int flags) {
        }
    }
    
    private GroovyImageDecorator decorator = new GroovyImageDecorator();
    
    private GroovySemanticReconciler semanticReconciler;
    
    private final GroovyBracketInserter groovyBracketInserter = new GroovyBracketInserter();
    
    public GroovyEditor() {
		super();
        setRulerContextMenuId("#GroovyCompilationUnitRulerContext"); //$NON-NLS-1$  
        setEditorContextMenuId("#GroovyCompilationUnitEditorContext"); //$NON-NLS-1$  
	}

    protected void setPreferenceStore(IPreferenceStore store) {
        super.setPreferenceStore(store);
        setSourceViewerConfiguration(createJavaSourceViewerConfiguration());    
    }

    
    private void installSemanticHighlighting() {
        try {
            semanticReconciler = new GroovySemanticReconciler();
            semanticReconciler.install(this, (JavaSourceViewer) this.getSourceViewer());
            ReflectionUtils.executePrivateMethod(CompilationUnitEditor.class, "addReconcileListener", 
                    new Class[] { IJavaReconcilingListener.class }, this, new Object[] { semanticReconciler });
        } catch (SecurityException e) {
            GroovyCore.logException("Unable to install semantic reconciler for groovy editor", e);
        }
    }
    
    private void uninstallSemanticHighlighting() {
        if (semanticHighlightingInstalled()) {
            try {
                semanticReconciler.uninstall();
                ReflectionUtils.executePrivateMethod(CompilationUnitEditor.class, "removeReconcileListener", 
                        new Class[] { IJavaReconcilingListener.class }, this, new Object[] { semanticReconciler });
                semanticReconciler = null;
            } catch (SecurityException e) {
                GroovyCore.logException("Unable to uninstall semantic reconciler for groovy editor", e);
            }
        }
    }
    
    private boolean semanticHighlightingInstalled() {
        return semanticReconciler != null;
    }

    
    @Override
    public void dispose() {
        super.dispose();
        uninstallSemanticHighlighting();
        
        ISourceViewer sourceViewer= getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension) {
            ((ITextViewerExtension) sourceViewer).removeVerifyKeyListener(groovyBracketInserter);
        }
    }
    
	@Override
	public IEditorInput getEditorInput() {
	    return super.getEditorInput();
	}

    public int getCaretOffset() {
        ISourceViewer viewer = getSourceViewer();
        return viewer.getTextWidget().getCaretOffset();
    }
    
    @Override
    public Image getTitleImage() {
        Object element = getEditorInput().getAdapter(IFile.class);
        if (element == null) {
            // will be null if coming from a code repository such as svn or cvs
            element = getEditorInput().getName();
        }
        Image image = decorator.decorateImage(null, element);
        // cannot return null GRECLIPSE-257
        return image != null? image : super.getTitleImage();
    }
    
    @Override
    protected void setSelection(ISourceReference reference, boolean moveCursor) {
    	super.setSelection(reference, moveCursor);

    	// must override functionality because JavaEditor expects that there is a ';' at end of declaration
    	// also, offsets are wrong for import declarations, they start 7 characters too early and
    	// end 7 characters too early.
    	try {
			if (reference instanceof IImportDeclaration && moveCursor) {
				int offset;
				int length;
				ISourceRange range = ((ISourceReference) reference).getSourceRange();
				String content= reference.getSource();
				if (content != null) {
					int start = Math.max(content.indexOf("import") + 6, 7); //$NON-NLS-1$
					while (start < content.length() && content.charAt(start) == ' ')
						start++;
					
					int end= content.trim().length();
					do {
						end--;
					} while (end >= 0 && (content.charAt(end) == ' ' || content.charAt(end) == ';'));
					
					offset= range.getOffset() + start;
					length= end - start + 8;
					
					// just in case...
					int docLength = ((IImportDeclaration) reference).getOpenable().getBuffer().getLength();
					if (docLength < offset+length) {
					    offset = docLength;
					}
				} else {
					// fallback
					offset= range.getOffset();
					length= range.getLength();
				}
				
				if (offset > -1 && length > 0) {

					try  {
						getSourceViewer().getTextWidget().setRedraw(false);
						getSourceViewer().revealRange(offset, length);
						getSourceViewer().setSelectedRange(offset, length);
					} finally {
						getSourceViewer().getTextWidget().setRedraw(true);
					}

					markInNavigationHistory();
				}

			}
		} catch (JavaModelException e) {
			GroovyCore.logException("Error selecting import statement", e);
		}
    }
    
    
    @Override
    protected void createActions() {
        super.createActions();
        
        GenerateActionGroup group = getGenerateActionGroup();
        
        // use our Organize Imports instead
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fOrganizeImports", group, new OrganizeGroovyImportsAction(this));
        IAction organizeImports = new OrganizeGroovyImportsAction(this);
        organizeImports
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.ORGANIZE_IMPORTS);
        setAction("OrganizeImports", organizeImports); //$NON-NLS-1$
        
        // use our Format instead
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fFormatAll", group, new FormatAllGroovyAction(this.getEditorSite(), FormatKind.FORMAT));
        IAction formatAction = new FormatAllGroovyAction(this.getEditorSite(), FormatKind.FORMAT);
        formatAction
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.FORMAT);
        setAction("Format", formatAction); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(formatAction, IJavaHelpContextIds.FORMAT_ACTION);
        
        // use our Indent instead
        IAction indentAction = new FormatAllGroovyAction(this.getEditorSite(), FormatKind.INDENT_ONLY);
        indentAction
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.INDENT);
        setAction("Indent", indentAction); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(indentAction, IJavaHelpContextIds.INDENT_ACTION);
        
        // now remove some actions:
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fAddGetterSetter", group, null);
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fCleanUp", group, new NoopCleanUpsAction(getEditorSite()));
        
        // remove most refactorings since they are not yet really supported
        removeRefactoringAction("fSelfEncapsulateField");
        removeRefactoringAction("fMoveAction");
        removeRefactoringAction("fRenameAction");
        removeRefactoringAction("fModifyParametersAction");
        // fPullUpAction
        // fPushDownAction
        removeRefactoringAction("fIntroduceParameterAction");
        removeRefactoringAction("fIntroduceParameterObjectAction");
        removeRefactoringAction("fIntroduceFactoryAction");
        removeRefactoringAction("fExtractMethodAction");
        removeRefactoringAction("fExtractInterfaceAction");
        removeRefactoringAction("fExtractClassAction");
        removeRefactoringAction("fExtractSupertypeAction");
        removeRefactoringAction("fChangeTypeAction");
        removeRefactoringAction("fConvertNestedToTopAction");
        removeRefactoringAction("fInferTypeArgumentsAction");
        removeRefactoringAction("fConvertLocalToFieldAction");
        removeRefactoringAction("fConvertAnonymousToNestedAction");
        removeRefactoringAction("fIntroduceIndirectionAction");
        // fInlineAction
        removeRefactoringAction("fUseSupertypeAction");
        
        // use our Rename action instead
        IAction renameAction = new GroovyRenameAction(this);
        renameAction
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.RENAME_ELEMENT);
        setAction("RenameElement", renameAction); //$NON-NLS-1$
    }
    
    private void removeRefactoringAction(String actionFieldName) {
        RefactorActionGroup group = getRefactorActionGroup();
        ISelectionChangedListener action = (ISelectionChangedListener) 
                ReflectionUtils.getPrivateField(RefactorActionGroup.class, actionFieldName, group);
        getSite().getSelectionProvider().removeSelectionChangedListener(action);
        ReflectionUtils.setPrivateField(RefactorActionGroup.class, actionFieldName, group, null);
    }
    
    /*
     * Make accessible to source viewer
     */
    @Override
    protected void setStatusLineErrorMessage(String message) {
        super.setStatusLineErrorMessage(message);
    }
    
    /*
     * Make accessible to source viewer
     */
    @Override
    protected ITypeRoot getInputJavaElement() {
        return super.getInputJavaElement();
    }

    private IFile getFile() {
        IEditorInput input = getEditorInput();
        if (input instanceof FileEditorInput) {
            return ((FileEditorInput) input).getFile();
        } else {
            return null;
        }
    }
    
    public GroovyCompilationUnit getGroovyCompilationUnit() {
        return (GroovyCompilationUnit) getInputJavaElement();
    }
    
    public ModuleNode getModuleNode() {
        GroovyCompilationUnit unit = getGroovyCompilationUnit();
        if (unit != null) {
            return unit.getModuleNode();
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {
        if (IResource.class == required || IFile.class == required) {
            return this.getFile();
        }
        if (GroovyCompilationUnit.class == required || ICompilationUnit.class == required || CompilationUnit.class == required) {
            return this.getGroovyCompilationUnit();
        }
        
        if (ModuleNode.class == required) {
            return this.getModuleNode();
        }
        return super.getAdapter(required);
    }

    /**
     * Override this method so that we can get access to the newly initialized
     * annotation model
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        unsetJavaBreakpointUpdater();
        installSemanticHighlighting();
        
        IPreferenceStore preferenceStore = getPreferenceStore();

        // ensure that the bracket inserter from the superclass is disabled
        
        boolean closeBrackets= preferenceStore.getBoolean(CLOSE_BRACKETS);
        boolean closeStrings= preferenceStore.getBoolean(CLOSE_STRINGS);
        boolean closeAngularBrackets= JavaCore.VERSION_1_5.compareTo(preferenceStore.getString(JavaCore.COMPILER_SOURCE)) <= 0;

        groovyBracketInserter.setCloseBracketsEnabled(closeBrackets);
        groovyBracketInserter.setCloseStringsEnabled(closeStrings);
        groovyBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);
        

        ISourceViewer sourceViewer= getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension) {
            ((ITextViewerExtension) sourceViewer).prependVerifyKeyListener(groovyBracketInserter);
        }
        
        disableBracketInserter();
    }
    
    private void disableBracketInserter() {
        Object fBracketInserterField = ReflectionUtils.getPrivateField(CompilationUnitEditor.class, "fBracketInserter", this);
        Class<?> fBracketInserterClass = fBracketInserterField.getClass();
        ReflectionUtils.executePrivateMethod(fBracketInserterClass, "setCloseBracketsEnabled", new Class[] { boolean.class }, fBracketInserterField, new Object[] { false });
        ReflectionUtils.executePrivateMethod(fBracketInserterClass, "setCloseStringsEnabled", new Class[] { boolean.class }, fBracketInserterField, new Object[] { false });
        ReflectionUtils.executePrivateMethod(fBracketInserterClass, "setCloseStringsEnabled", new Class[] { boolean.class }, fBracketInserterField, new Object[] { false });
        ReflectionUtils.executePrivateMethod(fBracketInserterClass, "setCloseAngularBracketsEnabled", new Class[] { boolean.class }, fBracketInserterField, new Object[] { false });
    }
    
    // temporary storage for editor input
    // so that GroovyConiguration can use it
    IEditorInput internalInput;
    /**
     * Override this method so that we can get access to the newly initialized
     * annotation model
     */
    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        boolean wasInstalled = semanticHighlightingInstalled();
        if (wasInstalled) {
            uninstallSemanticHighlighting();
        }
        internalInput = input;
        super.doSetInput(input);
        unsetJavaBreakpointUpdater();
        internalInput = null;
        if (wasInstalled) {
            installSemanticHighlighting();
        }
    }
    
    
    /**
     * Make accessible
     */
    public JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
        GroovyTextTools textTools= GroovyPlugin.getDefault().getTextTools();
        return new GroovyConfiguration(textTools.getColorManager(), getPreferenceStore(), this);
    }
    
    /**
     * Ensure that the Java breakpoint updater is removed because we need to use
     * Groovy's breakpoint updater instead
     */
    @SuppressWarnings("unchecked")
    private void unsetJavaBreakpointUpdater() {
        ISourceViewer viewer = getSourceViewer();
        if (viewer != null) {
            IAnnotationModel model = viewer.getAnnotationModel();
            if (model instanceof AbstractMarkerAnnotationModel) {
                // force instantiation of the extension points 
                ReflectionUtils.executePrivateMethod(AbstractMarkerAnnotationModel.class, "installMarkerUpdaters", 
                        new Class<?>[0], model, new Object[0]);
                // remove the marker updater for Java breakpoints, the groovy one will be used instead
                List<IConfigurationElement> updaterSpecs = (List<IConfigurationElement>) 
                        ReflectionUtils.getPrivateField(AbstractMarkerAnnotationModel.class, 
                        "fMarkerUpdaterSpecifications", model);
                for (Iterator<IConfigurationElement> specIter = updaterSpecs.iterator(); specIter
                        .hasNext();) {
                    IConfigurationElement spec = specIter.next();
                    if (spec.getAttribute("class").equals(BreakpointMarkerUpdater.class.getCanonicalName())) {
                        specIter.remove();
                        break;
                    }
                }
            }
        }
    }
    
    /** Preference key for automatically closing strings */
    private final static String CLOSE_STRINGS = PreferenceConstants.EDITOR_CLOSE_STRINGS;
    /** Preference key for automatically closing brackets and parenthesis */
    private final static String CLOSE_BRACKETS= PreferenceConstants.EDITOR_CLOSE_BRACKETS;

    @Override
    protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
        super.handlePreferenceStoreChanged(event);
        ISourceViewer sv= getSourceViewer();
        if (sv != null) {

            String p= event.getProperty();

            if (CLOSE_BRACKETS.equals(p)) {
                groovyBracketInserter.setCloseBracketsEnabled(getPreferenceStore().getBoolean(p));
                disableBracketInserter();
                return;
            }

            if (CLOSE_STRINGS.equals(p)) {
                groovyBracketInserter.setCloseStringsEnabled(getPreferenceStore().getBoolean(p));
                disableBracketInserter();
                return;
            }
        }
    }
 
    
    // copied to make accessible from super-class
    private static char getEscapeCharacter(char character) {
        switch (character) {
            case '"':
            case '\'':
                return '\\';
            default:
                return 0;
        }
    }

    // copied to make accessible from super-class
    private static char getPeerCharacter(char character) {
        switch (character) {
            case '(':
                return ')';

            case ')':
                return '(';

            case '<':
                return '>';

            case '>':
                return '<';

            case '[':
                return ']';

            case ']':
                return '[';

            case '"':
                return character;

            case '\'':
                return character;

            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * exposed for testing purposes
     */
    public VerifyKeyListener getGroovyBracketInserter() {
        return groovyBracketInserter;
    }
}