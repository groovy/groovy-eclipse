/*
 * Copyright 2003-2011 the original author or authors.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.actions.ExpandSelectionAction;
import org.codehaus.groovy.eclipse.editor.actions.GroovyConvertLocalToFieldAction;
import org.codehaus.groovy.eclipse.editor.actions.GroovyExtractConstantAction;
import org.codehaus.groovy.eclipse.editor.actions.GroovyExtractLocalAction;
import org.codehaus.groovy.eclipse.editor.actions.GroovyExtractMethodAction;
import org.codehaus.groovy.eclipse.editor.actions.GroovyTabAction;
import org.codehaus.groovy.eclipse.editor.actions.IGroovyEditorActionDefinitionIds;
import org.codehaus.groovy.eclipse.editor.highlighting.GroovySemanticReconciler;
import org.codehaus.groovy.eclipse.editor.outline.GroovyOutlinePage;
import org.codehaus.groovy.eclipse.editor.outline.OutlineExtenderRegistry;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatAllGroovyAction;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatGroovyAction;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatKind;
import org.codehaus.groovy.eclipse.refactoring.actions.GroovyRenameAction;
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImportsAction;
import org.codehaus.groovy.eclipse.search.GroovyOccurrencesFinder;
import org.codehaus.groovy.eclipse.ui.decorators.GroovyImageDecorator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction;
import org.eclipse.jdt.internal.ui.actions.CleanUpAction;
import org.eclipse.jdt.internal.ui.actions.CompositeActionGroup;
import org.eclipse.jdt.internal.ui.actions.SurroundWithActionGroup;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.selectionactions.SelectionHistory;
import org.eclipse.jdt.internal.ui.javaeditor.selectionactions.StructureSelectionAction;
import org.eclipse.jdt.internal.ui.search.IOccurrencesFinder;
import org.eclipse.jdt.internal.ui.search.IOccurrencesFinder.OccurrenceLocation;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.JavaPartitionScanner;
import org.eclipse.jdt.internal.ui.text.JavaWordFinder;
import org.eclipse.jdt.internal.ui.text.Symbols;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.actions.AddGetterSetterAction;
import org.eclipse.jdt.ui.actions.GenerateActionGroup;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.RefactorActionGroup;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
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
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class GroovyEditor extends CompilationUnitEditor {
    private static final String INDENT_ON_TAB = "IndentOnTab";

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
    private class GroovyExitPolicy implements IExitPolicy {

        final char fExitCharacter;
        final char fEscapeCharacter;
        final Stack<GroovyBracketLevel> fStack;
        final int fSize;

        public GroovyExitPolicy(char exitCharacter, char escapeCharacter, Stack<GroovyBracketLevel> stack) {
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
                    GroovyBracketLevel level= fStack.peek();
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
    private class GroovyBracketInserter implements VerifyKeyListener, ILinkedModeListener {

        private boolean fCloseBrackets= true;

        private boolean fCloseBraces = true; // GROOVY change
        private boolean fCloseStrings= true;
        private boolean fCloseAngularBrackets= true;
        private final String CATEGORY= toString();
        private final IPositionUpdater fUpdater= new GroovyExclusivePositionUpdater(CATEGORY);
        private final Stack<GroovyBracketLevel> fBracketLevelStack= new Stack<GroovyBracketLevel>();

        public void setCloseBracketsEnabled(boolean enabled) {
            fCloseBrackets= enabled;
        }

        public void setCloseStringsEnabled(boolean enabled) {
            fCloseStrings= enabled;
        }

        /**
         * closing curly braces
         *
         * @param enabled
         */
        public void setCloseBracesEnabled(boolean enabled) {
            fCloseBraces = enabled;
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
                    // GROOVY change. Allow autoclosing of curly braces in
                    // GStrings
                case '{':
                    // GROOVy end change
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

                        // GROOVY change, allow curly braces closing in GStrings
                    case '{':
                        if (!fCloseBraces || nextToken == Symbols.TokenIDENT || next != null && next.length() > 1)
                            return;
                        break;
                        // GROOVY end change
                    default:
                        return;
                }

                ITypedRegion partition= TextUtilities.getPartition(document, IJavaPartitions.JAVA_PARTITIONING, offset, true);
                if (event.character != '{' && !IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType()) && // original
                        // GROOVY change autoclose triple quotes
                        !shouldCloseTripleQuotes(document, offset, partition, getPeerCharacter(event.character))) { // GROOVY
                    // change
                    return;
                }

                // GROOVY change check for autoclose curly braces
                if (event.character == '{' && !shouldCloseCurly(document, offset, partition, getPeerCharacter(event.character))) {
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
         * GROOVY change
         *
         * @param document
         * @param offset
         * @param partition
         * @param peer
         * @return true iff we should be closing a curly bracket. Only happens
         *         as part of a GString
         * @throws BadLocationException
         */
        private boolean shouldCloseCurly(IDocument document, int offset, ITypedRegion partition, char peer)
                throws BadLocationException {
            if (offset < 2
                    || !(JavaPartitionScanner.JAVA_STRING.equals(partition.getType()) || GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS
                            .equals(partition.getType()))) {
                return false;
            }

            char maybeOpen = document.getChar(offset - 1);
            if (maybeOpen != '$') {
                return false;
            }

            char maybeNext = document.getChar(offset);
            return Character.isWhitespace(maybeNext) || maybeNext == '\"' || maybeNext == '\'';
        }

        /**
         * GROOVY change
         *
         * @param document
         * @param offset
         * @param partition
         * @param quote
         * @return true if we are at a position of triple quotes
         * @throws BadLocationException
         */
        private boolean shouldCloseTripleQuotes(IDocument document, int offset, ITypedRegion partition, char quote) throws BadLocationException {
            if (offset < 3 || GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS.equals(partition.getType())) {
                return false;
            }
            String maybequotes = document.get(offset-3, 3);
            return maybequotes.equals(Character.toString(quote)+quote+quote);
        }

        /*
         * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
         */
        public void left(LinkedModeModel environment, int flags) {

            final GroovyBracketLevel level= fBracketLevelStack.pop();

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

    @Override
    protected void setPreferenceStore(IPreferenceStore store) {
        ChainedPreferenceStore newStore = new ChainedPreferenceStore(new IPreferenceStore[] { store,
                GroovyPlugin.getDefault().getPreferenceStore() });
        super.setPreferenceStore(newStore);

        // now create a new configuration to overwrite the Java-centric one
        setSourceViewerConfiguration(createJavaSourceViewerConfiguration());
    }


    public GroovyConfiguration getGroovyConfiguration() {
        return (GroovyConfiguration) getSourceViewerConfiguration();
    }

    private void installGroovySemanticHighlighting() {
        try {
            fSemanticManager.uninstall();
            semanticReconciler = new GroovySemanticReconciler();
            semanticReconciler.install(this, (JavaSourceViewer) this.getSourceViewer());
            ReflectionUtils.executePrivateMethod(CompilationUnitEditor.class, "addReconcileListener",
                    new Class[] { IJavaReconcilingListener.class }, this, new Object[] { semanticReconciler });

        } catch (SecurityException e) {
            GroovyCore.logException("Unable to install semantic reconciler for groovy editor", e);
        }
    }

    private void uninstallGroovySemanticHighlighting() {
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
        uninstallGroovySemanticHighlighting();

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
                    length= end - start + 1;  // Note, original JDT code has 8 here

                    // just in case...
                    int docLength = ((IImportDeclaration) reference).getOpenable().getBuffer().getLength();
                    if (docLength < offset+length) {
                        offset = docLength;
                    }
                } else {
                    // fallback
                    offset= range.getOffset()+1;
                    length= range.getLength()-2;
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
        organizeImports.setActionDefinitionId(IJavaEditorActionDefinitionIds.ORGANIZE_IMPORTS);
        setAction("OrganizeImports", organizeImports); //$NON-NLS-1$

        // use our FormatAll instead
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fFormatAll", group,
                new FormatAllGroovyAction(this.getEditorSite(), FormatKind.FORMAT));

        // use our Format instead
        IAction formatAction = new FormatGroovyAction(this.getEditorSite(), FormatKind.FORMAT);
        formatAction
        .setActionDefinitionId(IJavaEditorActionDefinitionIds.FORMAT);
        setAction("Format", formatAction); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(formatAction, IJavaHelpContextIds.FORMAT_ACTION);

        // use our Indent instead
        IAction indentAction = new FormatGroovyAction(this.getEditorSite(), FormatKind.INDENT_ONLY);
        indentAction
        .setActionDefinitionId(IJavaEditorActionDefinitionIds.INDENT);
        setAction("Indent", indentAction); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(indentAction, IJavaHelpContextIds.INDENT_ACTION);

        // Use our IndentOnTab instead
        IAction indentOnTabAction = new GroovyTabAction(this);
        setAction(INDENT_ON_TAB, indentOnTabAction);
        markAsStateDependentAction(INDENT_ON_TAB, true);
        markAsSelectionDependentAction(INDENT_ON_TAB, true);

        // now remove some actions:
        // GRECLIPSE-966 must dispose action to avoid memory leak
        AddGetterSetterAction agsa = (AddGetterSetterAction) ReflectionUtils.getPrivateField(GenerateActionGroup.class,
                "fAddGetterSetter", group);
        if (agsa != null) {
            ReflectionUtils.setPrivateField(AddGetterSetterAction.class, "fEditor", agsa, null);
        }
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fAddGetterSetter", group, null);

        AllCleanUpsAction acua = (AllCleanUpsAction) ReflectionUtils.getPrivateField(GenerateActionGroup.class, "fCleanUp", group);
        // GRECLIPSE-966 must dispose action to avoid memory leak
        if (acua != null) {
            acua.dispose();
            ReflectionUtils.setPrivateField(CleanUpAction.class, "fEditor", acua, null);
        }
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fCleanUp", group, new NoopCleanUpsAction(getEditorSite()));

        // remove most refactorings since they are not yet really supported
        removeRefactoringAction("fSelfEncapsulateField");
        removeRefactoringAction("fMoveAction");
        //        removeRefactoringAction("fRenameAction");
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
        removeRefactoringAction("fExtractTempAction");
        removeRefactoringAction("fExtractConstantAction");
        removeRefactoringAction("fChangeTypeAction");
        removeRefactoringAction("fConvertNestedToTopAction");
        removeRefactoringAction("fInferTypeArgumentsAction");
        removeRefactoringAction("fInlineAction");
        // fConvertLocalToFieldAction
        removeRefactoringAction("fConvertAnonymousToNestedAction");
        removeRefactoringAction("fIntroduceIndirectionAction");
        // fInlineAction
        removeRefactoringAction("fUseSupertypeAction");

        // use our Rename action instead
        GroovyRenameAction renameAction = new GroovyRenameAction(this);
        renameAction.setActionDefinitionId(IGroovyEditorActionDefinitionIds.GROOVY_RENAME_ACTION);
        setAction("RenameElement", renameAction); //$NON-NLS-1$
        replaceRefactoringAction("fRenameAction", renameAction);

        // use our Extract constant action instead
        GroovyExtractConstantAction extractConstantAction = new GroovyExtractConstantAction(this);
        extractConstantAction
        .setActionDefinitionId(IJavaEditorActionDefinitionIds.EXTRACT_CONSTANT);
        setAction("ExtractConstant", extractConstantAction); //$NON-NLS-1$
        replaceRefactoringAction("fExtractConstantAction", extractConstantAction);

        // use our Extract method action instead
        GroovyExtractMethodAction extractMethodAction = new GroovyExtractMethodAction(this);
        extractMethodAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.EXTRACT_METHOD);
        setAction("ExtractMethod", extractMethodAction); //$NON-NLS-1$
        replaceRefactoringAction("fExtractMethodAction", extractMethodAction);

        // use our Extract local instead
        GroovyExtractLocalAction extractLocalAction = new GroovyExtractLocalAction(this);
        extractLocalAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.EXTRACT_LOCAL_VARIABLE);
        setAction("ExtractLocalVariable", extractLocalAction); //$NON-NLS-1$
        replaceRefactoringAction("fExtractTempAction", extractLocalAction);

        // use our Convert local instead
        GroovyConvertLocalToFieldAction convertLocalAction = new GroovyConvertLocalToFieldAction(this);
        convertLocalAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.PROMOTE_LOCAL_VARIABLE);
        setAction("ConvertLocalToField", convertLocalAction);
        replaceRefactoringAction("fConvertLocalToFieldAction", convertLocalAction);

        // use our SurroundWith quick menu instead
        // for now set to null until we find a way to instantiate it
        //        setAction("org.eclipse.jdt.internal.ui.actions.SurroundWithTemplateMenuAction", null);

        // selections
        ExpandSelectionAction selectionAction= new ExpandSelectionAction(this,
                (SelectionHistory) ReflectionUtils.getPrivateField(JavaEditor.class, "fSelectionHistory", this));
        selectionAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.SELECT_ENCLOSING);
        selectionAction.getDescription();
        setAction(StructureSelectionAction.ENCLOSING, selectionAction);
        setAction(StructureSelectionAction.NEXT, null);
        setAction(StructureSelectionAction.PREVIOUS, null);

        // Now stick our own SurroundWith actions in the menu
        // TODO how can we avoid using fully qualified name in a string here???
        ISurroundWithFactory surroundWithFactory = (ISurroundWithFactory) Platform.getAdapterManager().loadAdapter(this, "org.codehaus.groovy.eclipse.quickfix.templates.SurroundWithAdapterFactory");
        if (surroundWithFactory != null) {
            CompositeActionGroup compositActions = (CompositeActionGroup) ReflectionUtils.getPrivateField(CompilationUnitEditor.class, "fContextMenuGroup", this);
            ActionGroup[] groups = (ActionGroup[]) ReflectionUtils.getPrivateField(CompositeActionGroup.class, "fGroups", compositActions);
            boolean found = false;
            ActionGroup surroundWithGroup = surroundWithFactory.createSurrundWithGroup(this, ITextEditorActionConstants.GROUP_EDIT);
            for (int i = 0; i < groups.length; i++) {
                if (groups[i] instanceof SurroundWithActionGroup) {
                    found = true;
                    groups[i] = surroundWithGroup;
                    break;
                }
            }
            if (!found) {
                GroovyCore.logTraceMessage("Oops...surroundWithActionGroup not found in context menus");
            }

            found = false;
            groups = (ActionGroup[]) ReflectionUtils.getPrivateField(CompositeActionGroup.class, "fGroups", fActionGroups);
            for (int i = 0; i < groups.length; i++) {
                if (groups[i] instanceof SurroundWithActionGroup) {
                    found = true;
                    groups[i] = surroundWithGroup;
                    break;
                }
            }
            if (!found) {
                GroovyCore.logTraceMessage("Oops...surroundWithActionGroup not found");
            }
        } else {
            GroovyCore.logTraceMessage("Oops...surroundWithFactory not initialized");
        }
    }

    private void removeRefactoringAction(String actionFieldName) {
        replaceRefactoringAction(actionFieldName, null);
    }

    private void replaceRefactoringAction(String actionFieldName, SelectionDispatchAction newAction) {
        RefactorActionGroup group = getRefactorActionGroup();
        ISelectionChangedListener action = (ISelectionChangedListener)
                ReflectionUtils.getPrivateField(RefactorActionGroup.class, actionFieldName, group);
        if (action != null) {
            getSite().getSelectionProvider().removeSelectionChangedListener(action);
        }
        ReflectionUtils.setPrivateField(RefactorActionGroup.class, actionFieldName, group, newAction);
    }

    private IFile getFile() {
        IEditorInput input = getEditorInput();
        if (input instanceof FileEditorInput) {
            return ((FileEditorInput) input).getFile();
        } else {
            return null;
        }
    }

    /**
     * @return the {@link GroovyCompilationUnit} associated with this editor,
     * or returns null if the input is not a {@link GroovyCompilationUnit}.
     */
    public GroovyCompilationUnit getGroovyCompilationUnit() {
        ITypeRoot root = super.getInputJavaElement();
        if (root instanceof GroovyCompilationUnit) {
            return (GroovyCompilationUnit) root;
        } else {
            return null;
        }
    }

    public ModuleNode getModuleNode() {
        GroovyCompilationUnit unit = getGroovyCompilationUnit();
        if (unit != null) {
            return unit.getModuleNode();
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class required) {
        if (IResource.class == required || IFile.class == required) {
            return this.getFile();
        }
        if (GroovyCompilationUnit.class == required || ICompilationUnit.class == required || CompilationUnit.class == required) {
            return super.getInputJavaElement();
        }

        if (ModuleNode.class == required) {
            return this.getModuleNode();
        }

        // The new variant test in e43 which addresses bug 391253 means groovy doesn't get an outline (it must fail the
        // isCalledByOutline() test but I haven't investigated deeply)
        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null && getSourceViewer() != null)
                fOutlinePage= createOutlinePage();
            return fOutlinePage;
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
        installGroovySemanticHighlighting();

        IPreferenceStore preferenceStore = getPreferenceStore();

        // ensure that the bracket inserter from the superclass is disabled

        boolean closeBrackets= preferenceStore.getBoolean(CLOSE_BRACKETS);
        boolean closeStrings= preferenceStore.getBoolean(CLOSE_STRINGS);
        boolean closeBraces = preferenceStore.getBoolean(CLOSE_BRACES);
        boolean closeAngularBrackets= JavaCore.VERSION_1_5.compareTo(preferenceStore.getString(JavaCore.COMPILER_SOURCE)) <= 0;

        groovyBracketInserter.setCloseBracketsEnabled(closeBrackets);
        groovyBracketInserter.setCloseStringsEnabled(closeStrings);
        groovyBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);
        groovyBracketInserter.setCloseBracesEnabled(closeBraces);


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
            uninstallGroovySemanticHighlighting();
        }
        internalInput = input;
        super.doSetInput(input);
        unsetJavaBreakpointUpdater();
        internalInput = null;
        if (wasInstalled) {
            installGroovySemanticHighlighting();
        }
    }

    /**
     * Finds and marks occurrence annotations.
     * Copied from {@link JavaEditor}
     *
     * @since 3.0
     */
    class OccurrencesFinderJob extends Job {

        private final IDocument fDocument;

        private final ISelection fSelection;

        private final ISelectionValidator fPostSelectionValidator;

        private boolean fCanceled = false;

        private final OccurrenceLocation[] fLocations;

        public OccurrencesFinderJob(IDocument document, OccurrenceLocation[] locations, ISelection selection) {
            super("Mark Occurrences");
            fDocument = document;
            fSelection = selection;
            fLocations = locations;

            if (getSelectionProvider() instanceof ISelectionValidator)
                fPostSelectionValidator = (ISelectionValidator) getSelectionProvider();
            else
                fPostSelectionValidator = null;
        }

        // cannot use cancel() because it is declared final
        void doCancel() {
            fCanceled = true;
            cancel();
        }

        private boolean isCanceled(IProgressMonitor progressMonitor) {
            return fCanceled || progressMonitor.isCanceled() || fPostSelectionValidator != null
                    && !(fPostSelectionValidator.isValid(fSelection) || getForcedMarkOccurrencesSelection() == fSelection)
                    || LinkedModeModel.hasInstalledModel(fDocument);
        }

        /*
         * @see Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus run(IProgressMonitor progressMonitor) {
            if (isCanceled(progressMonitor))
                return Status.CANCEL_STATUS;

            ITextViewer textViewer = getViewer();
            if (textViewer == null)
                return Status.CANCEL_STATUS;

            IDocument document = textViewer.getDocument();
            if (document == null)
                return Status.CANCEL_STATUS;

            IDocumentProvider documentProvider = getDocumentProvider();
            if (documentProvider == null)
                return Status.CANCEL_STATUS;

            IAnnotationModel annotationModel = documentProvider.getAnnotationModel(getEditorInput());
            if (annotationModel == null)
                return Status.CANCEL_STATUS;

            // Add occurrence annotations
            int length = fLocations.length;
            Map annotationMap = new HashMap(length);
            for (int i = 0; i < length; i++) {

                if (isCanceled(progressMonitor))
                    return Status.CANCEL_STATUS;

                OccurrenceLocation location = fLocations[i];
                Position position = new Position(location.getOffset(), location.getLength());

                String description = location.getDescription();
                String annotationType = (location.getFlags() == IOccurrencesFinder.F_WRITE_OCCURRENCE) ? "org.eclipse.jdt.ui.occurrences.write" : "org.eclipse.jdt.ui.occurrences"; //$NON-NLS-1$ //$NON-NLS-2$

                annotationMap.put(new Annotation(annotationType, false, description), position);
            }

            if (isCanceled(progressMonitor))
                return Status.CANCEL_STATUS;

            synchronized (myGetLockObject(annotationModel)) {
                if (annotationModel instanceof IAnnotationModelExtension) {
                    ((IAnnotationModelExtension) annotationModel).replaceAnnotations(getOccurrenceAnnotations(), annotationMap);
                } else {
                    myRemoveOccurrenceAnnotations();
                    Iterator iter = annotationMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry mapEntry = (Map.Entry) iter.next();
                        annotationModel.addAnnotation((Annotation) mapEntry.getKey(), (Position) mapEntry.getValue());
                    }
                }
                setOccurrenceAnnotations((Annotation[]) annotationMap.keySet().toArray(
                        new Annotation[annotationMap.keySet().size()]));
            }

            return Status.OK_STATUS;
        }
    }

    // create our own since the version in the super class is of a
    // non-accessible type
    private OccurrencesFinderJob groovyOccurrencesFinderJob = null;

    // the following methods are used to get, set, and call private members in
    // the super class that have to do with mark ocurrences.

    private Object myGetLockObject(IAnnotationModel model) {
        return ReflectionUtils.executePrivateMethod(JavaEditor.class, "getLockObject", new Class[] { IAnnotationModel.class },
                this, new Object[] { model });
    }

    private void myRemoveOccurrenceAnnotations() {
        ReflectionUtils.executePrivateMethod(JavaEditor.class, "removeOccurrenceAnnotations", new Class[0], this, new Object[0]);
    }

    private void setOccurrenceAnnotations(Annotation[] as) {
        ReflectionUtils.setPrivateField(JavaEditor.class, "fOccurrenceAnnotations", this, as);
    }

    private void setMarkOccurrenceTargetRegion(IRegion r) {
        ReflectionUtils.setPrivateField(JavaEditor.class, "fMarkOccurrenceTargetRegion", this, r);
    }

    private void setMarkOccurrenceModificationStamp(long l) {
        ReflectionUtils.setPrivateField(JavaEditor.class, "fMarkOccurrenceModificationStamp", this, l);
    }

    private Annotation[] getOccurrenceAnnotations() {
        return (Annotation[]) ReflectionUtils.getPrivateField(JavaEditor.class, "fOccurrenceAnnotations", this);
    }

    private IRegion getMarkOccurrenceTargetRegion() {
        return (IRegion) ReflectionUtils.getPrivateField(JavaEditor.class, "fMarkOccurrenceTargetRegion", this);
    }

    private long getMarkOccurrenceModificationStamp() {
        return (Long) ReflectionUtils.getPrivateField(JavaEditor.class, "fMarkOccurrenceModificationStamp", this);
    }

    private boolean getStickyOccurrenceAnnotations() {
        return (Boolean) ReflectionUtils.getPrivateField(JavaEditor.class, "fStickyOccurrenceAnnotations", this);
    }

    private boolean getMarkOccurrenceAnnotations() {
        return (Boolean) ReflectionUtils.getPrivateField(JavaEditor.class, "fMarkOccurrenceAnnotations", this);
    }

    private ISelection getForcedMarkOccurrencesSelection() {
        return (ISelection) ReflectionUtils.getPrivateField(JavaEditor.class, "fForcedMarkOccurrencesSelection", this);
    }


    /**
     * Override from super class since we need to handle Groovy code here.
     */
    @Override
    protected void updateOccurrenceAnnotations(ITextSelection selection, org.eclipse.jdt.core.dom.CompilationUnit astRoot) {
        if (groovyOccurrencesFinderJob != null)
            groovyOccurrencesFinderJob.cancel();

        if (!getMarkOccurrenceAnnotations())
            return;

        if (astRoot == null || selection == null)
            return;

        IDocument document = getSourceViewer().getDocument();
        if (document == null)
            return;

        boolean hasChanged = false;
        if (document instanceof IDocumentExtension4) {
            int offset = selection.getOffset();
            long currentModificationStamp = ((IDocumentExtension4) document).getModificationStamp();
            IRegion markOccurrenceTargetRegion = getMarkOccurrenceTargetRegion();
            hasChanged = currentModificationStamp != getMarkOccurrenceModificationStamp();
            if (markOccurrenceTargetRegion != null && !hasChanged) {
                if (markOccurrenceTargetRegion.getOffset() <= offset
                        && offset <= markOccurrenceTargetRegion.getOffset() + markOccurrenceTargetRegion.getLength())
                    return;
            }
            setMarkOccurrenceTargetRegion(findMarkOccurrencesRegion(document, offset));
            setMarkOccurrenceModificationStamp(currentModificationStamp);
        }

        OccurrenceLocation[] locations = null;

        GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder();
        finder.initialize(astRoot, selection.getOffset(), selection.getLength());
        locations = finder.getOccurrences();

        if (locations == null) {
            if (!getStickyOccurrenceAnnotations())
                myRemoveOccurrenceAnnotations();
            else if (hasChanged) // check consistency of current annotations
                myRemoveOccurrenceAnnotations();
            return;
        }

        groovyOccurrencesFinderJob = new OccurrencesFinderJob(document, locations, selection);
        groovyOccurrencesFinderJob.run(new NullProgressMonitor());

    }

    protected IRegion findMarkOccurrencesRegion(IDocument document, int offset) {
        IRegion word = JavaWordFinder.findWord(document, offset);
        try {
            if (word != null && word.getLength() > 1 && document.getChar(word.getOffset()) == '$') {
                // this is likely a GString expresion without {}, eg: "$var"
                word = new Region(word.getOffset()+1, word.getLength()-1);
            }
        } catch (BadLocationException e) {
            // if this ever gets thrown, then a more interesting exception will be thrown later
        }
        return word;
    }

    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { "org.codehaus.groovy.eclipse.editor.groovyEditorScope" }); //$NON-NLS-1$
    }

    /**
     * Make accessible
     */
    @Override
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

    /** Preference key for automatically closing curly braces */
    private final static String CLOSE_BRACES = PreferenceConstants.EDITOR_CLOSE_BRACES;

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
            if (CLOSE_BRACES.equals(p)) {
                groovyBracketInserter.setCloseBracesEnabled(getPreferenceStore().getBoolean(p));
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

                // GROOVY change
            case '{':
                return '}';
                // GROOVY change end

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

    /**
     * outline management
     */
    private GroovyOutlinePage page;

    /**
     * Gets the outline page for this editor only if the outline page is an
     * augmented {@link GroovyOutlinePage}.
     *
     * Otherwise returns null
     *
     * @return the {@link GroovyOutlinePage} or null
     */
    public GroovyOutlinePage getOutlinePage() {
        if (page == null) {
            IContentOutlinePage outlinePage = (IContentOutlinePage) getAdapter(IContentOutlinePage.class);
            if (outlinePage instanceof GroovyOutlinePage) {
                page = (GroovyOutlinePage) outlinePage;
            }
        }
        return page;
    }

    // @Override
    // protected ITypeRoot getInputJavaElement() {
    // return page != null ? page.getOutlineCompilationUnit() :
    // super.getInputJavaElement();
    // }

    @Override
    protected void synchronizeOutlinePage(ISourceReference element, boolean checkIfOutlinePageActive) {
        if (page != null) {
            page.refresh();
        }
        super.synchronizeOutlinePage(element, checkIfOutlinePageActive);
    }

    @Override
    protected ISourceReference computeHighlightRangeSourceReference() {
        return page != null ? page.getOutlineElmenetAt(getCaretOffset()) : super.computeHighlightRangeSourceReference();
    }

    @Override
    protected JavaOutlinePage createOutlinePage() {
        OutlineExtenderRegistry outlineExtenderRegistry = GroovyPlugin.getDefault().getOutlineTools().getOutlineExtenderRegistry();

        GroovyCompilationUnit unit = getGroovyCompilationUnit();
        if (unit != null) {
            try {
                page = outlineExtenderRegistry.getGroovyOutlinePageForEditor(unit.getJavaProject().getProject(),
                        fOutlinerContextMenuId, this);
            } catch (CoreException e) {
                GroovyCore.logException("Error creating Groovy Outline page", e);
            }
            if (page != null) {
                // don't call this since it will grab the GroovyCompilationUnit
                // instead of the OCompilationUnit
                // setOutlinePageInput(page, getEditorInput());
                // FIXADE do we need to call
                // page.getOutlineCompilationUnit().exists()?
                page.setInput(page.getOutlineCompilationUnit());
                return page;
            }
        }
        return super.createOutlinePage();
    }

}