/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
import org.eclipse.jdt.internal.ui.search.IOccurrencesFinder.OccurrenceLocation;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal.DeleteBlockingExitPolicy;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
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

/**
 * @author andrew
 * @created Jan 7, 2011
 */
public class GroovyRenameLinkedMode extends RenameLinkedMode {

    // copy from super
    private class EditorSynchronizer implements ILinkedModeListener {
        public void left(LinkedModeModel model, int flags) {
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

        public void resume(LinkedModeModel model, int flags) {}

        public void suspend(LinkedModeModel model) {}
    }

    // copy from super
    private class ExitPolicy extends DeleteBlockingExitPolicy {
        public ExitPolicy(IDocument document) {
            super(document);
        }

        @Override
        public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
            setShowPreview((event.stateMask & SWT.CTRL) != 0 && (event.character == SWT.CR || event.character == SWT.LF));
            return super.doExit(model, event, offset, length);
        }
    }

    private final GroovyEditor editor;

    public GroovyRenameLinkedMode(IJavaElement element, GroovyEditor editor) {
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
        Point fOriginalSelection = viewer.getSelectedRange();
        setOriginalSelection(fOriginalSelection);
        int offset = fOriginalSelection.x;
        int length = fOriginalSelection.y;

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

            OccurrenceLocation[] occurrences = finder.getOccurrences();
            if (occurrences.length == 0) {
                return;
            }

            // now just in case some source locations are not correct (eg-
            // accessing a getter method as a non-getter property), remove all
            // that do not have the same source length as the original
            int nameLength = finder.getElementName().length();
            List<OccurrenceLocation> newOccurrences = new ArrayList<OccurrenceLocation>(occurrences.length);
            for (OccurrenceLocation occurrence : occurrences) {
                if (occurrence.getLength() == nameLength) {
                    newOccurrences.add(occurrence);
                }
            }
            occurrences = newOccurrences.toArray(new OccurrenceLocation[0]);

            // sort for iteration order, starting with the node @ offset
            Arrays.sort(occurrences, new Comparator<OccurrenceLocation>() {
                public int compare(OccurrenceLocation o1, OccurrenceLocation o2) {
                    return rank(o1) - rank(o2);
                }

                /**
                 * Returns the absolute rank of an <code>ASTNode</code>. Nodes
                 * preceding <code>pos</code> are ranked last.
                 *
                 * @param node the node to compute the rank for
                 * @return the rank of the node with respect to the invocation
                 *         offset
                 */
                private int rank(OccurrenceLocation o) {
                    int relativeRank = o.getOffset() + o.getLength() - pos;
                    if (relativeRank < 0)
                        return Integer.MAX_VALUE + relativeRank;
                    else
                        return relativeRank;
                }
            });
            for (int i = 0; i < occurrences.length; i++) {
                OccurrenceLocation location = occurrences[i];
                LinkedPosition linkedPosition = new LinkedPosition(document, location.getOffset(), location.getLength(), i);
                if (i == 0) {
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
            viewer.setSelectedRange(fOriginalSelection.x, fOriginalSelection.y);

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

    // method calls openSecondaryPopup

    // get fields fFocusEditingSupport, fOriginalSelection, fLinkedModeModel,
    // fLinkedPositionGroup

    // set fields fgActiveLinkedMode, fLinkedModeModel, fNamePosition,
    // fOriginalName, fStartingUndoOperation, fOriginalSelection, fShowPreview

    private void setOriginalSelection(Point p) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fOriginalSelection", this, p);
    }

    private void setLinkedPositionGroup(LinkedPositionGroup group) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fLinkedPositionGroup", this, group);
    }

    private IEditingSupport getFocusEditingSupport() {
        return (IEditingSupport) ReflectionUtils.getPrivateField(RenameLinkedMode.class, "fFocusEditingSupport", this);
    }

    private boolean getShowPreview() {
        return (Boolean) ReflectionUtils.getPrivateField(RenameLinkedMode.class, "fShowPreview", this);
    }

    private IJavaElement getJavaElement() {
        return (IJavaElement) ReflectionUtils.getPrivateField(RenameLinkedMode.class, "fJavaElement", this);
    }

    private static RenameLinkedMode getMyActiveLinkedMode() {
        return (RenameLinkedMode) ReflectionUtils.getPrivateField(RenameLinkedMode.class, "fgActiveLinkedMode", null);
    }

    private void setShowPreview(boolean show) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fShowPreview", this, show);
    }

    private void setNamePosition(LinkedPosition pos) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fNamePosition", this, pos);
    }

    private void setStartingUndoOperation(IUndoableOperation op) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fStartingUndoOperation", this, op);
    }

    private void setLinkedModeModel(LinkedModeModel model) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fLinkedModeModel", this, model);
    }

    private void setOriginalName(String name) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fOriginalName", this, name);
    }

    private void setActiveLinkedMode(RenameLinkedMode active) {
        ReflectionUtils.setPrivateField(RenameLinkedMode.class, "fgActiveLinkedMode", this, active);
    }

    private void doOpenSecondaryPopup() {
        ReflectionUtils.executeNoArgPrivateMethod(RenameLinkedMode.class, "openSecondaryPopup", this);
    }

    private void doLinkedModeLeft() {
        ReflectionUtils.executeNoArgPrivateMethod(RenameLinkedMode.class, "linkedModeLeft", this);
    }

    private void doDoRename(boolean showPreview) {
        ReflectionUtils.executePrivateMethod(RenameLinkedMode.class, "doRename", new Class[] { boolean.class }, this,
                new Object[] { showPreview });
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
}
