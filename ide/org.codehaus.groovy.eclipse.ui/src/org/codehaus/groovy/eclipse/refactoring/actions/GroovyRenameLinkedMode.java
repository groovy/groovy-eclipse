/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.search.GroovyOccurrencesFinder;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.refactoring.reorg.RenameLinkedMode;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal.DeleteBlockingExitPolicy;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

public class GroovyRenameLinkedMode extends RenameLinkedMode {

    private final GroovyEditor editor;

    public GroovyRenameLinkedMode(final IJavaElement element, final GroovyEditor editor) {
        super(element, editor);
        this.editor = editor;
    }

    @Override
    public void start() {
        if (getActiveLinkedMode() != null) {
            // for safety; should already be handled in RenameJavaElementAction
            getMyActiveLinkedMode().startFullDialog();
            return;
        }

        ISourceViewer viewer = editor.getViewer();
        IDocument document = viewer.getDocument();
        Point selection = viewer.getSelectedRange();
        setOriginalSelection(selection);
        int offset = selection.x;
        int length = selection.y;

        try {
            if (viewer instanceof ITextViewerExtension6) {
                IUndoManager undoManager = ((ITextViewerExtension6) viewer).getUndoManager();
                if (undoManager instanceof IUndoManagerExtension) {
                    IUndoManagerExtension undoManagerExtension = (IUndoManagerExtension) undoManager;
                    IUndoContext undoContext = undoManagerExtension.getUndoContext();
                    IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
                    setStartingUndoOperation(operationHistory.getUndoOperation(undoContext));
                }
            }

            LinkedPositionGroup fLinkedPositionGroup = new LinkedPositionGroup();
            setLinkedPositionGroup(fLinkedPositionGroup);
            GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder();
            finder.setGroovyCompilationUnit(editor.getGroovyCompilationUnit());
            finder.initialize(null, offset, length);
            ASTNode nodeToLookFor = finder.getNodeToLookFor();
            if (nodeToLookFor == null) {
                return;
            }
            setOriginalName(finder.getElementName());
            final int pos = nodeToLookFor.getStart();
            Object occurrences = finder.getOccurrences();
            if (occurrences == null || Array.getLength(occurrences) == 0) {
                return;
            }

            // convert from array of OccurrenceLocation to ordered collection of Position
            Set<Position> positions = new TreeSet<>(new Comparator<Position>() {
                @Override
                public int compare(final Position p1, final Position p2) {
                    return rank(p1) - rank(p2);
                }

                /**
                 * Returns the absolute rank of an <code>ASTNode</code>. Nodes preceding <code>pos</code> are ranked last.
                 *
                 * @return the rank of the position with respect to the invocation offset
                 */
                private int rank(final Position p) {
                    int relativeRank = p.getOffset() + p.getLength() - pos;
                    if (relativeRank < 0)
                        return Integer.MAX_VALUE + relativeRank;
                    return relativeRank;
                }
            });

            int nameLength = finder.getElementName().length();
            for (int i = 0, n = Array.getLength(occurrences); i < n; i += 1) {
                Object occurrence = Array.get(occurrences, i);
                int off = (Integer) ReflectionUtils.getPrivateField(occurrence.getClass(), "fOffset", occurrence),
                    len = (Integer) ReflectionUtils.getPrivateField(occurrence.getClass(), "fLength", occurrence);

                // just in case some source locations are not correct (eg-accessing a getter method as a non-getter property),
                // remove ones that do not have the same source length as the original
                if (len == nameLength)
                    positions.add(new Position(off, len));
            }

            int i = 0;
            for (Position position : positions) {
                LinkedPosition linkedPosition = new LinkedPosition(document, position.getOffset(), position.getLength(), i);
                if (i++ == 0) {
                    setNamePosition(linkedPosition);
                }
                fLinkedPositionGroup.addPosition(linkedPosition);
            }

            // can't do anything if no linked positions are found
            if (fLinkedPositionGroup.isEmpty()) {
                IStatusLineManager status = getStatusLineManager();
                if (status != null) {
                    status.setErrorMessage("No positions found.  Cannot do refactoring...");
                }
                return;
            }

            LinkedModeModel fLinkedModeModel = new LinkedModeModel();
            setLinkedModeModel(fLinkedModeModel);
            fLinkedModeModel.addGroup(fLinkedPositionGroup);
            fLinkedModeModel.forceInstall();
            fLinkedModeModel.addLinkingListener(new EditorHighlightingSynchronizer(editor));
            fLinkedModeModel.addLinkingListener(new EditorSynchronizer());

            LinkedModeUI ui = new EditorLinkedModeUI(fLinkedModeModel, viewer);
            ui.setExitPosition(viewer, offset, 0, Integer.MAX_VALUE);
            ui.setExitPolicy(new ExitPolicy(document));
            ui.enter();

            // by default, full word is selected; restore original selection
            viewer.setSelectedRange(selection.x, selection.y);

            if (viewer instanceof IEditingSupportRegistry) {
                IEditingSupportRegistry registry = (IEditingSupportRegistry) viewer;
                registry.register(getFocusEditingSupport());
            }

            doOpenSecondaryPopup();
            // startAnimation();
            setActiveLinkedMode(this);

        } catch (BadLocationException e) {
            JavaPlugin.log(e);
        }
    }

    private void setOriginalSelection(final Point p) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fOriginalSelection", this, p);
    }

    private void setLinkedPositionGroup(final LinkedPositionGroup group) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fLinkedPositionGroup", this, group);
    }

    private IEditingSupport getFocusEditingSupport() {
        return ReflectionUtils.getPrivateField(RenameLinkedMode.class, "fFocusEditingSupport", this);
    }

    private boolean getShowPreview() {
        return (Boolean) ReflectionUtils.getPrivateField(RenameLinkedMode.class, "fShowPreview", this);
    }

    private IJavaElement getJavaElement() {
        return ReflectionUtils.getPrivateField(RenameLinkedMode.class, "fJavaElement", this);
    }

    private static RenameLinkedMode getMyActiveLinkedMode() {
        return ReflectionUtils.getPrivateField(RenameLinkedMode.class, "fgActiveLinkedMode", null);
    }

    private void setShowPreview(final boolean show) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fShowPreview", this, show);
    }

    private void setNamePosition(final LinkedPosition pos) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fNamePosition", this, pos);
    }

    private void setStartingUndoOperation(final IUndoableOperation op) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fStartingUndoOperation", this, op);
    }

    private void setLinkedModeModel(final LinkedModeModel model) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fLinkedModeModel", this, model);
    }

    private void setOriginalName(final String name) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fOriginalName", this, name);
    }

    private void setActiveLinkedMode(final RenameLinkedMode active) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fgActiveLinkedMode", this, active);
    }

    private void doOpenSecondaryPopup() {
        ReflectionUtils.executePrivateMethod(RenameLinkedMode.class, "openSecondaryPopup", this);
    }

    private void doLinkedModeLeft() {
        ReflectionUtils.executePrivateMethod(RenameLinkedMode.class, "linkedModeLeft", this);
    }

    private void doDoRename(final boolean showPreview) {
        ReflectionUtils.executePrivateMethod(RenameLinkedMode.class, "doRename", new Class[] {boolean.class}, this, new Object[] {showPreview});
    }

    private IStatusLineManager getStatusLineManager() {
        if (editor != null) {
            try {
                return editor.getEditorSite().getActionBars().getStatusLineManager();
            } catch (NullPointerException e) {
                // can ignore
            }
        }
        return null;
    }

    // copy from super
    private class EditorSynchronizer implements ILinkedModeListener {

        @Override
        public void left(final LinkedModeModel model, final int flags) {
            doLinkedModeLeft();
            // don't actually do the refactorings for local variables
            // just let the change in text work itself
            // this is because doRename wants to use the JDT Rename Temp
            // Refactoring
            // which will not work for groovy locals
            if ((flags & ILinkedModeListener.UPDATE_CARET) != 0 && getJavaElement().getElementType() != IJavaElement.LOCAL_VARIABLE) {
                doDoRename(getShowPreview());
            }
        }

        @Override
        public void resume(final LinkedModeModel model, final int flags) {
        }

        @Override
        public void suspend(final LinkedModeModel model) {
        }
    }

    // copy from super
    private class ExitPolicy extends DeleteBlockingExitPolicy {

        ExitPolicy(final IDocument document) {
            super(document);
        }

        @Override
        public ExitFlags doExit(final LinkedModeModel model, final VerifyEvent event, final int offset, final int length) {
            setShowPreview((event.stateMask & SWT.CTRL) != 0 && (event.character == SWT.CR || event.character == SWT.LF));
            return super.doExit(model, event, offset, length);
        }
    }
}
