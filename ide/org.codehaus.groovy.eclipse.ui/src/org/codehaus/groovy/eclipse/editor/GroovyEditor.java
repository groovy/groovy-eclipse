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
package org.codehaus.groovy.eclipse.editor;

import static java.lang.reflect.Array.getLength;
import static java.util.regex.Pattern.compile;

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR;
import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
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
import org.codehaus.groovy.eclipse.refactoring.actions.AddImportOnSelectionAction;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatAllGroovyAction;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatGroovyAction;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatKind;
import org.codehaus.groovy.eclipse.refactoring.actions.GroovyRenameAction;
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImportsAction;
import org.codehaus.groovy.eclipse.refactoring.core.utils.StringUtils;
import org.codehaus.groovy.eclipse.search.GroovyOccurrencesFinder;
import org.codehaus.groovy.eclipse.ui.decorators.GroovyImageDecorator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction;
import org.eclipse.jdt.internal.ui.actions.CompositeActionGroup;
import org.eclipse.jdt.internal.ui.actions.SurroundWithActionGroup;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.selectionactions.SelectionHistory;
import org.eclipse.jdt.internal.ui.javaeditor.selectionactions.StructureSelectionAction;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.JavaWordFinder;
import org.eclipse.jdt.internal.ui.text.Symbols;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.actions.GenerateActionGroup;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.RefactorActionGroup;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
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
import org.eclipse.jface.text.ITextSelection;
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
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

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
                        // position comes way before change - leave alone
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

        public void setCloseBracesEnabled(boolean enabled) {
            fCloseBraces = enabled;
        }

        public void setCloseAngularBracketsEnabled(boolean enabled) {
            fCloseAngularBrackets= enabled;
        }

        private boolean isAngularIntroducer(String identifier) {
            return identifier.length() > 0 &&
                (Character.isUpperCase(identifier.charAt(0)) ||
                    identifier.startsWith("final") ||
                    identifier.startsWith("public") ||
                    identifier.startsWith("public") ||
                    identifier.startsWith("protected") ||
                    identifier.startsWith("private"));
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
                                || prevToken != Symbols.TokenLBRACE
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
            if (offset < 2 || !(IJavaPartitions.JAVA_STRING.equals(partition.getType()) ||
                    GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS.equals(partition.getType()))) {
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
                                        "");
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

        public void suspend(LinkedModeModel environment) {
        }

        public void resume(LinkedModeModel environment, int flags) {
        }
    }

    private GroovyImageDecorator decorator = new GroovyImageDecorator();

    private GroovySemanticReconciler semanticReconciler;

    private final GroovyBracketInserter groovyBracketInserter = new GroovyBracketInserter();
    /* visible only for testing! */ public VerifyKeyListener getGroovyBracketInserter() {
        return groovyBracketInserter;
    }

    public GroovyEditor() {
        super();
        setRulerContextMenuId("#GroovyCompilationUnitRulerContext");
        setEditorContextMenuId("#GroovyCompilationUnitEditorContext");
    }

    @Override
    protected void setPreferenceStore(IPreferenceStore store) {
        super.setPreferenceStore(new ChainedPreferenceStore(
            new IPreferenceStore[] {store, GroovyPlugin.getDefault().getPreferenceStore()}));

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
            semanticReconciler.install(this, (JavaSourceViewer) getSourceViewer());
            ReflectionUtils.executePrivateMethod(CompilationUnitEditor.class, "addReconcileListener",
                new Class[] {IJavaReconcilingListener.class}, this, new Object[] {semanticReconciler});
        } catch (Throwable t) {
            GroovyPlugin.getDefault().logError("GroovyEditor: failed to install semantic reconciler", t);
        }
    }

    private void uninstallGroovySemanticHighlighting() {
        if (semanticHighlightingInstalled()) {
            try {
                semanticReconciler.uninstall();
                ReflectionUtils.executePrivateMethod(CompilationUnitEditor.class, "removeReconcileListener",
                    new Class[] {IJavaReconcilingListener.class}, this, new Object[] {semanticReconciler});
                semanticReconciler = null;
            } catch (Throwable t) {
                GroovyPlugin.getDefault().logError("GroovyEditor: failed to uninstall semantic reconciler", t);
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
        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension) {
            ((ITextViewerExtension) sourceViewer).removeVerifyKeyListener(groovyBracketInserter);
        }
    }

    public int getCaretOffset() {
        ISourceViewer viewer = getSourceViewer();
        return viewer.getTextWidget().getCaretOffset();
    }

    @Override
    public Image getTitleImage() {
        Object element = getEditorInput().getAdapter(IFile.class);
        if (element == null) {
            // null if coming from a code repository such as cvs, git, or svn
            element = getEditorInput().getName();
        }
        Image image = decorator.decorateImage(null, element);
        // cannot return null GRECLIPSE-257
        return image != null ? image : super.getTitleImage();
    }

    @Override
    protected void setSelection(ISourceReference reference, boolean moveCursor) {
        super.setSelection(reference, moveCursor);
        // must override functionality because JavaEditor expects that there is a ';' at end of declaration
        try {
            if (reference instanceof IImportDeclaration && moveCursor) {
                int offset;
                int length;
                ISourceRange range = reference.getSourceRange();
                String content = reference.getSource();
                if (content != null) {
                    int start = Math.max(content.indexOf("import") + 6, 7);
                    while (start < content.length() && content.charAt(start) == ' ')
                        start++;

                    int end = content.trim().length();
                    do {
                        end--;
                    } while (end >= 0 && (content.charAt(end) == ' ' || content.charAt(end) == ';'));

                    offset = range.getOffset() + start;
                    length = end - start + 1; // Note, original JDT code has 8 here

                    // just in case...
                    int docLength = ((IImportDeclaration) reference).getOpenable().getBuffer().getLength();
                    if (docLength < offset + length) {
                        offset = docLength;
                    }
                } else {
                    // fallback
                    offset = range.getOffset() + 1;
                    length = range.getLength() - 2;
                }

                if (offset > -1 && length > 0) {
                    try {
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
            GroovyPlugin.getDefault().logError("Error selecting import statement", e);
        }
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

    @Override @SuppressWarnings({"rawtypes", "unchecked"})
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
        // new variant test in e43 which addresses bug 391253 means groovy doesn't get an outline
        // (it must fail the isCalledByOutline() test but I haven't investigated deeply)
        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null && getSourceViewer() != null)
                fOutlinePage= createOutlinePage();
            return fOutlinePage;
        }
        return super.getAdapter(required);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        unsetJavaBreakpointUpdater();
        installGroovySemanticHighlighting();

        IPreferenceStore preferenceStore = getPreferenceStore();
        groovyBracketInserter.setCloseBracesEnabled(preferenceStore.getBoolean(CLOSE_BRACES));
        groovyBracketInserter.setCloseBracketsEnabled(preferenceStore.getBoolean(CLOSE_BRACKETS));
        groovyBracketInserter.setCloseStringsEnabled(preferenceStore.getBoolean(CLOSE_STRINGS));
        groovyBracketInserter.setCloseAngularBracketsEnabled(preferenceStore.getString(JavaCore.COMPILER_SOURCE).compareTo(JavaCore.VERSION_1_5) >= 0);

        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension) {
            ((ITextViewerExtension) sourceViewer).prependVerifyKeyListener(groovyBracketInserter);
        }

        // ensure the bracket inserter from the superclass is disabled
        disableBracketInserter();
    }

    private void disableBracketInserter() {
        Object fBracketInserterField = ReflectionUtils.getPrivateField(CompilationUnitEditor.class, "fBracketInserter", this);
        Class<?> fBracketInserterClass = fBracketInserterField.getClass();
        Class<?>[] bool = {boolean.class};
        Object[] disabled = {Boolean.FALSE};
        ReflectionUtils.executePrivateMethod(fBracketInserterClass, "setCloseBracketsEnabled", bool, fBracketInserterField, disabled);
        ReflectionUtils.executePrivateMethod(fBracketInserterClass, "setCloseStringsEnabled", bool, fBracketInserterField, disabled);
        ReflectionUtils.executePrivateMethod(fBracketInserterClass, "setCloseStringsEnabled", bool, fBracketInserterField, disabled);
        ReflectionUtils.executePrivateMethod(fBracketInserterClass, "setCloseAngularBracketsEnabled", bool, fBracketInserterField, disabled);
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        final boolean installed = semanticHighlightingInstalled();
        if (installed) {
            uninstallGroovySemanticHighlighting();
        }

        super.doSetInput(input);

        unsetJavaBreakpointUpdater();

        if (installed) {
            installGroovySemanticHighlighting();
        }
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        try {
            super.doSave(progressMonitor);
        } catch (RuntimeException e) {
            GroovyPlugin.getDefault().logError("GroovyEditor: error saving document", e);
            throw e;
        } catch (Error e) {
            GroovyPlugin.getDefault().logError("GroovyEditor: error saving document", e);
            throw e;
        }
    }

    @Override
    protected void handleExceptionOnSave(CoreException exception, IProgressMonitor progressMonitor) {
        GroovyPlugin.getDefault().logError("GroovyEditor: error saving document", exception);
        super.handleExceptionOnSave(exception, progressMonitor);
    }

    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] {"org.codehaus.groovy.eclipse.editor.groovyEditorScope"});
    }

    @Override
    public JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
        GroovyTextTools textTools = GroovyPlugin.getDefault().getTextTools();
        return new GroovyConfiguration(textTools.getColorManager(), getPreferenceStore(), this);
    }

    /**
     * Ensures that the Java breakpoint updater is removed because we need to use
     * Groovy's breakpoint updater instead.
     */
    private void unsetJavaBreakpointUpdater() {
        try {
            ISourceViewer viewer = getSourceViewer();
            if (viewer != null) {
                IAnnotationModel model = viewer.getAnnotationModel();
                if (model instanceof AbstractMarkerAnnotationModel) {
                    if (ReflectionUtils.getPrivateField(AbstractMarkerAnnotationModel.class, "fMarkerUpdaterSpecifications", model) == null) {
                        // force instantiation of the extension points
                        ReflectionUtils.executeNoArgPrivateMethod(AbstractMarkerAnnotationModel.class, "installMarkerUpdaters", model);
                    }
                    @SuppressWarnings("unchecked")
                    List<IConfigurationElement> updaterSpecs = (List<IConfigurationElement>)
                        ReflectionUtils.getPrivateField(AbstractMarkerAnnotationModel.class, "fMarkerUpdaterSpecifications", model);
                    // remove the marker updater for Java breakpoints; the Groovy one will be used instead
                    for (Iterator<IConfigurationElement> specIter = updaterSpecs.iterator(); specIter.hasNext();) {
                        IConfigurationElement spec = specIter.next();
                        if (spec.getAttribute("class").equals(BreakpointMarkerUpdater.class.getCanonicalName())) {
                            specIter.remove();
                            break;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            GroovyPlugin.getDefault().logError("GroovyEditor: failed to remove Java breakpoint updater", t);
        }
    }

    /** Preference key for automatically closing strings */
    private static final String CLOSE_STRINGS = PreferenceConstants.EDITOR_CLOSE_STRINGS;
    /** Preference key for automatically closing brackets and parenthesis */
    private static final String CLOSE_BRACKETS = PreferenceConstants.EDITOR_CLOSE_BRACKETS;
    /** Preference key for automatically closing curly braces */
    private static final String CLOSE_BRACES = PreferenceConstants.EDITOR_CLOSE_BRACES;

    @Override
    protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
        super.handlePreferenceStoreChanged(event);
        ISourceViewer sv = getSourceViewer();
        if (sv != null) {
            String p = event.getProperty();

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
                page = outlineExtenderRegistry.getGroovyOutlinePageForEditor(unit.getJavaProject().getProject(), fOutlinerContextMenuId, this);
            } catch (CoreException e) {
                GroovyPlugin.getDefault().logError("GroovyEditor: failed to create Outline page", e);
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

    //--------------------------------------------------------------------------

    private Action toPropertyAction;

    @Override
    protected void createActions() {
        super.createActions();
        updateSourceActions();
        updateRefactorActions();

        // indent on tab action
        setAction("IndentOnTab", new GroovyTabAction(this));
        markAsSelectionDependentAction("IndentOnTab", true);
        markAsStateDependentAction("IndentOnTab", true);

        // selection history actions
        ExpandSelectionAction selectionAction = new ExpandSelectionAction(this,
            (SelectionHistory) ReflectionUtils.getPrivateField(JavaEditor.class, "fSelectionHistory", this));
        selectionAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.SELECT_ENCLOSING);
        setAction(StructureSelectionAction.ENCLOSING, selectionAction);
        setAction(StructureSelectionAction.PREVIOUS, null);
        setAction(StructureSelectionAction.NEXT, null);

        // surround with actions
        // TODO: How can we avoid using full-qualified name in a string here?
        ISurroundWithFactory surroundWithFactory = (ISurroundWithFactory) Platform.getAdapterManager()
            .loadAdapter(this, "org.codehaus.groovy.eclipse.quickfix.templates.SurroundWithAdapterFactory");
        if (surroundWithFactory != null) {
            CompositeActionGroup compositActions = (CompositeActionGroup) ReflectionUtils.getPrivateField(CompilationUnitEditor.class, "fContextMenuGroup", this);
            ActionGroup[] groups = (ActionGroup[]) ReflectionUtils.getPrivateField(CompositeActionGroup.class, "fGroups", compositActions);
            boolean found = false;
            ActionGroup surroundWithGroup = surroundWithFactory.createSurrundWithGroup(this, ITextEditorActionConstants.GROUP_EDIT);
            for (int i = 0, n = groups.length; i < n; i += 1) {
                if (groups[i] instanceof SurroundWithActionGroup) {
                    found = true;
                    groups[i] = surroundWithGroup;
                    break;
                }
            }
            if (!found) {
                GroovyPlugin.trace("Oops...surroundWithActionGroup not found in context menus");
            }

            found = false;
            groups = (ActionGroup[]) ReflectionUtils.getPrivateField(CompositeActionGroup.class, "fGroups", fActionGroups);
            for (int i = 0, n = groups.length; i < n; i += 1) {
                if (groups[i] instanceof SurroundWithActionGroup) {
                    found = true;
                    groups[i] = surroundWithGroup;
                    break;
                }
            }
            if (!found) {
                GroovyPlugin.trace("Oops...surroundWithActionGroup not found");
            }
        } else {
            GroovyPlugin.trace("Oops...surroundWithFactory not initialized");
        }

        // to property action
        toPropertyAction = new Action("Replace Accessor call with Property read/write") {{
                setActionDefinitionId("org.codehaus.groovy.eclipse.ui.convertToProperty");
            }
            @Override
            public void run() {
                if (!ActionUtil.isEditable(GroovyEditor.this))
                    return;
                ISelection selection = getSelectionProvider().getSelection();
                if (!(selection instanceof ITextSelection))
                    return;
                GroovyCompilationUnit gcu = getGroovyCompilationUnit();
                if (!ElementValidator.checkValidateEdit(gcu, getSite().getShell(), "Convert to Property"))
                    return;
                try {
                    ModuleNodeInfo info = gcu.getModuleInfo(true);
                    if (info.isEmpty())
                        return;

                    org.codehaus.groovy.eclipse.codebrowsing.requestor.Region selectRegion =
                        new org.codehaus.groovy.eclipse.codebrowsing.requestor.Region(((ITextSelection) selection).getOffset(), ((ITextSelection) selection).getLength());
                    ASTNodeFinder nodeFinder = new ASTNodeFinder(selectRegion);
                    ASTNode node = nodeFinder.doVisit(info.module);
                    if (node instanceof ConstantExpression) {
                        IASTFragment fragment = new FindSurroundingNode(new org.codehaus.groovy.eclipse.codebrowsing.requestor.Region(node)).doVisitSurroundingNode(info.module);
                        if (fragment.kind() == ASTFragmentKind.METHOD_CALL) {
                            MethodCallExpression call = (MethodCallExpression) fragment.getAssociatedNode();
                            if (call != null && !call.isUsingGenerics() && call.getArguments() instanceof ArgumentListExpression) {
                                ArgumentListExpression args = (ArgumentListExpression) call.getArguments();

                                Matcher match; // check for accessor or mutator
                                if (args.getExpressions().isEmpty() && (match = compile("(?:get|is)(\\w+)").matcher(call.getMethodAsString())).matches()) {
                                    int offset = node.getStart(),
                                        length = (args.getEnd() + 1) - offset;
                                    String propertyName = match.group(1);

                                    // replace "getPropertyName()" with "propertyName"
                                    gcu.applyTextEdit(new ReplaceEdit(offset, length, StringUtils.uncapitalize(propertyName)), null);

                                } else if (args.getExpressions().size() == 1 && (match = compile("set(\\w+)").matcher(call.getMethodAsString())).matches()) {
                                    int offset = node.getStart(),
                                        length = args.getStart() - offset;
                                    String propertyName = match.group(1);

                                    // replace "setPropertyName(value_expression)" or "setPropertyName value_expression"
                                    // with "propertyName = value_expression" (check prefs for spaces around assignment)
                                    MultiTextEdit edits = new MultiTextEdit();
                                    Map<String, String> options = gcu.getJavaProject().getOptions(true);
                                    StringBuilder replacement = new StringBuilder(StringUtils.uncapitalize(propertyName));
                                    if (JavaCore.INSERT.equals(options.get(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR)))
                                        replacement.append(' ');
                                    replacement.append('=');
                                    if (JavaCore.INSERT.equals(options.get(FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR)))
                                        replacement.append(' ');

                                    edits.addChild(new ReplaceEdit(offset, length, replacement.toString()));
                                    if (gcu.getContents()[args.getEnd()] == ')') edits.addChild(new DeleteEdit(args.getEnd(), 1));

                                    gcu.applyTextEdit(edits, null);
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    GroovyPlugin.getDefault().logError("Failure in convert to property", t);
                }
            }
        };
        setAction(toPropertyAction.getActionDefinitionId(), toPropertyAction);
    }

    /** Modifies, replaces, or disables actions managed by the {@link GenerateActionGroup}. */
    protected void updateSourceActions() {
        GenerateActionGroup group = getGenerateActionGroup();
        // see also GenerateActionGroup.setGlobalActionHandlers

        //editor context menu > 'Source' submenu
        // GROUP_COMMENT
        //  "ToggleComment"                                                          -- works
        //  "AddBlockComment"                                                        -- works
        //  "RemoveBlockComment"                                                     -- works
        //  fAddJavaDocStub: AddJavaDocStubAction                                    -- works
        // GROUP_EDIT
        //  "Indent"                                                                 -- replace
        //  "Format"                                                                 -- replace
        //  "QuickFormat"                                                            -- TODO
        // GROUP_IMPORT
        //  fAddImport: AddImportOnSelectionAction                                   -- replace
        //  fOrganizeImports: OrganizeImportsAction                                  -- replace
        //  fSortMembers: MultiSortMembersAction                                     -- works
        //  fCleanUp: AllCleanUpsAction                                              -- disable
        // GROUP_GENERATE
        //  fOverrideMethods: OverrideMethodsAction                                  -- works
        //  fAddGetterSetter: AddGetterSetterAction                                  -- works
        //  fAddDelegateMethods: AddDelegateMethodsAction                            -- works
        //  fHashCodeEquals GenerateHashCodeEqualsAction                             -- works
        //  fToString: GenerateToStringAction                                        -- works
        //  fGenerateConstructorUsingFields: GenerateNewConstructorUsingFieldsAction -- works
        //  fAddUnimplementedConstructors: AddUnimplementedConstructorsAction        -- works
        // GROUP_CODE
        // GROUP_EXTERNALIZE
        //  fExternalizeStrings: ExternalizeStringsAction                            -- TODO

        //view context menu > 'Source' submenu
        // GROUP_COMMENT
        //  fAddJavaDocStub: AddJavaDocStubAction                                    -- works
        // GROUP_EDIT
        //  fFormatAll: FormatAllAction                                              -- replace
        // GROUP_IMPORT
        //  fAddImport: AddImportOnSelectionAction                                   -- replace
        //  fOrganizeImports: OrganizeImportsAction                                  -- replace
        //  fSortMembers: MultiSortMembersAction                                     -- works
        //  fCleanUp: AllCleanUpsAction                                              -- disable
        // GROUP_GENERATE
        //  fOverrideMethods: OverrideMethodsAction                                  -- works
        //  fAddGetterSetter: AddGetterSetterAction                                  -- works
        //  fAddDelegateMethods: AddDelegateMethodsAction                            -- works
        //  fHashCodeEquals GenerateHashCodeEqualsAction                             -- works
        //  fToString: GenerateToStringAction                                        -- works
        //  fGenerateConstructorUsingFields: GenerateNewConstructorUsingFieldsAction -- works
        //  fAddUnimplementedConstructors: AddUnimplementedConstructorsAction        -- works
        // GROUP_CODE
        // GROUP_EXTERNALIZE
        //  fExternalizeStrings: ExternalizeStringsAction                            -- TODO
        //  fFindNLSProblems: FindBrokenNLSKeysAction                                -- TODO


        // replace Indent
        IAction indentAction = new FormatGroovyAction(getEditorSite(), FormatKind.INDENT_ONLY);
        indentAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.INDENT);
        setAction("Indent", indentAction);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(indentAction, IJavaHelpContextIds.INDENT_ACTION);

        // replace Format
        IAction formatAction = new FormatGroovyAction(getEditorSite(), FormatKind.FORMAT);
        formatAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.FORMAT);
        setAction("Format", formatAction);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(formatAction, IJavaHelpContextIds.FORMAT_ACTION);

        // replace Format All
        IAction formatAllAction = new FormatAllGroovyAction(getEditorSite(), FormatKind.FORMAT);
        // setActionDefinitionId?
        // setAction?
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fFormatAll", group, formatAllAction);

        // replace Add Import
        IAction addImportOnSelectionAction = new AddImportOnSelectionAction(this);
        addImportOnSelectionAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.ADD_IMPORT);
        setAction("AddImport", addImportOnSelectionAction);
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fAddImport", group, addImportOnSelectionAction);

        // replace Organize Imports
        IAction organizeGroovyImportsAction = new OrganizeGroovyImportsAction(this);
        organizeGroovyImportsAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.ORGANIZE_IMPORTS);
        setAction("OrganizeImports", organizeGroovyImportsAction);
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fOrganizeImports", group, organizeGroovyImportsAction);

        // disable Clean Ups...
        AllCleanUpsAction acua = (AllCleanUpsAction) ReflectionUtils.getPrivateField(GenerateActionGroup.class, "fCleanUp", group);
        acua.setEnabled(false);
    }

    /** Modifies, replaces, or disables actions managed by the {@link RefactorActionGroup}. */
    protected void updateRefactorActions() {
        // remove most refactorings since they are not yet really supported
        removeRefactoringAction("fSelfEncapsulateField");
        removeRefactoringAction("fMoveAction");
        //removeRefactoringAction("fRenameAction");
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
        setAction("RenameElement", renameAction);
        replaceRefactoringAction("fRenameAction", renameAction);

        // use our Extract constant action instead
        GroovyExtractConstantAction extractConstantAction = new GroovyExtractConstantAction(this);
        extractConstantAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.EXTRACT_CONSTANT);
        setAction("ExtractConstant", extractConstantAction);
        replaceRefactoringAction("fExtractConstantAction", extractConstantAction);

        // use our Extract method action instead
        GroovyExtractMethodAction extractMethodAction = new GroovyExtractMethodAction(this);
        extractMethodAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.EXTRACT_METHOD);
        setAction("ExtractMethod", extractMethodAction);
        replaceRefactoringAction("fExtractMethodAction", extractMethodAction);

        // use our Extract local instead
        GroovyExtractLocalAction extractLocalAction = new GroovyExtractLocalAction(this);
        extractLocalAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.EXTRACT_LOCAL_VARIABLE);
        setAction("ExtractLocalVariable", extractLocalAction);
        replaceRefactoringAction("fExtractTempAction", extractLocalAction);

        // use our Convert local instead
        GroovyConvertLocalToFieldAction convertLocalAction = new GroovyConvertLocalToFieldAction(this);
        convertLocalAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.PROMOTE_LOCAL_VARIABLE);
        setAction("ConvertLocalToField", convertLocalAction);
        replaceRefactoringAction("fConvertLocalToFieldAction", convertLocalAction);
    }

    protected final void removeRefactoringAction(String actionFieldName) {
        replaceRefactoringAction(actionFieldName, null);
    }

    protected final void replaceRefactoringAction(String actionFieldName, SelectionDispatchAction newAction) {
        RefactorActionGroup group = getRefactorActionGroup();
        SelectionDispatchAction action = (SelectionDispatchAction) ReflectionUtils.getPrivateField(RefactorActionGroup.class, actionFieldName, group);
        if (action != null) {
            getSite().getSelectionProvider().removeSelectionChangedListener(action);
        }
        ReflectionUtils.setPrivateField(RefactorActionGroup.class, actionFieldName, group, newAction);
    }

    @Override
    public void editorContextMenuAboutToShow(IMenuManager contextMenu) {
        super.editorContextMenuAboutToShow(contextMenu);

        // inject to property action into the source submenu
        IMenuManager sourceMenu = contextMenu.findMenuUsingPath(GenerateActionGroup.MENU_ID);
        if (sourceMenu != null) {
            sourceMenu.appendToGroup(GenerateActionGroup.GROUP_CODE, toPropertyAction);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Updates the occurrences annotations based on the current selection.
     *
     * @see GroovyOccurrencesFinder
     */
    @Override
    protected void updateOccurrenceAnnotations(ITextSelection selection, org.eclipse.jdt.core.dom.CompilationUnit astRoot) {
        try {

        if (fOccurrencesFinderJob_get() != null)
            fOccurrencesFinderJob_get().cancel();

        if (!fMarkOccurrenceAnnotations_get())
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
            IRegion markOccurrenceTargetRegion = fMarkOccurrenceTargetRegion_get();
            hasChanged = currentModificationStamp != fMarkOccurrenceModificationStamp_get();
            if (markOccurrenceTargetRegion != null && !hasChanged) {
                if (markOccurrenceTargetRegion.getOffset() <= offset &&
                        offset <= markOccurrenceTargetRegion.getOffset() + markOccurrenceTargetRegion.getLength())
                    return;
            }
            fMarkOccurrenceTargetRegion_set(findMarkOccurrencesRegion(document, offset));
            fMarkOccurrenceModificationStamp_set(currentModificationStamp);
        }

        Object locations = GroovyOccurrencesFinder.findOccurrences(
                astRoot, selection.getOffset(), selection.getLength());

        if (locations == null || getLength(locations) == 0) {
            if (!fStickyOccurrenceAnnotations_get())
                removeOccurrenceAnnotations_call();
            else if (hasChanged) // check consistency of current annotations
                removeOccurrenceAnnotations_call();
            return;
        }

        fOccurrencesFinderJob_new(document, locations, selection);

        } catch (Throwable t) {
            GroovyPlugin.getDefault().logError("Failure in GroovyEditor.updateOccurrenceAnnotations", t);
        }
    }

    protected IRegion findMarkOccurrencesRegion(IDocument document, int offset) {
        IRegion word = JavaWordFinder.findWord(document, offset);
        try {
            if (word != null && word.getLength() > 1 && document.getChar(word.getOffset()) == '$') {
                // this is likely a GString expresion without {}, eg: "$var"
                word = new Region(word.getOffset() + 1, word.getLength() - 1);
            }
        } catch (BadLocationException e) {
            // if this ever gets thrown, then a more interesting exception will be thrown later
        }
        return word;
    }

    protected Job fOccurrencesFinderJob_get() throws Throwable {
        return (Job) ReflectionUtils.throwableGetPrivateField(JavaEditor.class, "fOccurrencesFinderJob", this);
    }

    protected boolean fMarkOccurrenceAnnotations_get() throws Throwable {
        return (Boolean) ReflectionUtils.throwableGetPrivateField(JavaEditor.class, "fMarkOccurrenceAnnotations", this);
    }

    protected IRegion fMarkOccurrenceTargetRegion_get() throws Throwable {
        return (IRegion) ReflectionUtils.throwableGetPrivateField(JavaEditor.class, "fMarkOccurrenceTargetRegion", this);
    }

    protected void fMarkOccurrenceTargetRegion_set(IRegion r) throws Throwable {
        ReflectionUtils.setPrivateField(JavaEditor.class, "fMarkOccurrenceTargetRegion", this, r);
    }

    protected long fMarkOccurrenceModificationStamp_get() throws Throwable {
        return (Long) ReflectionUtils.throwableGetPrivateField(JavaEditor.class, "fMarkOccurrenceModificationStamp", this);
    }

    protected void fMarkOccurrenceModificationStamp_set(long s) throws Throwable {
        ReflectionUtils.setPrivateField(JavaEditor.class, "fMarkOccurrenceModificationStamp", this, s);
    }

    protected boolean fStickyOccurrenceAnnotations_get() throws Throwable {
        return (Boolean) ReflectionUtils.throwableGetPrivateField(JavaEditor.class, "fStickyOccurrenceAnnotations", this);
    }

    protected void removeOccurrenceAnnotations_call() throws Throwable {
        ReflectionUtils.throwableExecutePrivateMethod(JavaEditor.class, "removeOccurrenceAnnotations", new Class[0], this, new Object[0]);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void fOccurrencesFinderJob_new(IDocument document, Object locations, ISelection selection) throws Throwable {
        //OccurrencesFinderJob ofj = new OccurrencesFinderJob(document, locations, selection);
        java.lang.reflect.Constructor ctor = ReflectionUtils.getConstructor(
            Class.forName("org.eclipse.jdt.internal.ui.javaeditor.JavaEditor$OccurrencesFinderJob"),
            new Class[] {JavaEditor.class, IDocument.class, locations.getClass(), ISelection.class});
        Job ofj = ReflectionUtils.invokeConstructor(ctor, new Object[] {this, document, locations, selection});
        //fOccurrencesFinderJob = ofj;
        ReflectionUtils.setPrivateField(JavaEditor.class, "fOccurrencesFinderJob", this, ofj);
        //ofj.run(new NullProgressMonitor());
        ReflectionUtils.throwableExecutePrivateMethod(ofj.getClass(), "run", new Class[] {IProgressMonitor.class}, ofj, new Object[] {new NullProgressMonitor()});
    }
}
