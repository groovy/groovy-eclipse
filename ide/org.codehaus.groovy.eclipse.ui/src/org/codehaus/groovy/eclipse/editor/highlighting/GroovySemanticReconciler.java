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

package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.editor.GroovyColorManager;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingManager;
import org.eclipse.jdt.internal.ui.text.JavaPresentationReconciler;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Simplest reconciling that we can do
 * 
 * @author Andrew Eisenberg
 * @created Oct 13, 2009
 *
 */
public class GroovySemanticReconciler implements IJavaReconcilingListener {
    
    /**
     * Borrowed from {@link SemanticHighlightingManager#Highlighting}
     * Highlighting.
     */
    static class HighlightingStyle {

        /** Text attribute */
        private TextAttribute fTextAttribute;
        /** Enabled state */
        private boolean fIsEnabled;

        /**
         * Initialize with the given text attribute.
         * @param textAttribute The text attribute
         * @param isEnabled the enabled state
         */
        public HighlightingStyle(TextAttribute textAttribute, boolean isEnabled) {
            setTextAttribute(textAttribute);
            setEnabled(isEnabled);
        }

        /**
         * @return Returns the text attribute.
         */
        public TextAttribute getTextAttribute() {
            return fTextAttribute;
        }

        /**
         * @param textAttribute The background to set.
         */
        public void setTextAttribute(TextAttribute textAttribute) {
            fTextAttribute= textAttribute;
        }

        /**
         * @return the enabled state
         */
        public boolean isEnabled() {
            return fIsEnabled;
        }

        /**
         * @param isEnabled the new enabled state
         */
        public void setEnabled(boolean isEnabled) {
            fIsEnabled= isEnabled;
        }
    }

    /**
     * Borrowed from {@link SemanticHighlightingManager#HighlightedPosition}
     * Highlighted Positions.
     */
    static class HighlightedPosition extends Position {

        /** Highlighting of the position */
        private HighlightingStyle fStyle;

        /** Lock object */
        private Object fLock;

        /**
         * Initialize the styled positions with the given offset, length and foreground color.
         *
         * @param offset The position offset
         * @param length The position length
         * @param highlighting The position's highlighting
         * @param lock The lock object
         */
        public HighlightedPosition(int offset, int length, HighlightingStyle highlighting, Object lock) {
            super(offset, length);
            fStyle= highlighting;
            fLock= lock;
        }

        /**
         * @return Returns a corresponding style range.
         */
        public StyleRange createStyleRange() {
            int len= 0;
            if (fStyle.isEnabled())
                len= getLength();

            TextAttribute textAttribute= fStyle.getTextAttribute();
            int style= textAttribute.getStyle();
            int fontStyle= style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
            StyleRange styleRange= new StyleRange(getOffset(), len, textAttribute.getForeground(), textAttribute.getBackground(), fontStyle);
            styleRange.strikeout= (style & TextAttribute.STRIKETHROUGH) != 0;
            styleRange.underline= (style & TextAttribute.UNDERLINE) != 0;

            return styleRange;
        }

        /**
         * Uses reference equality for the highlighting.
         *
         * @param off The offset
         * @param len The length
         * @param highlighting The highlighting
         * @return <code>true</code> iff the given offset, length and highlighting are equal to the internal ones.
         */
        public boolean isEqual(int off, int len, HighlightingStyle highlighting) {
            synchronized (fLock) {
                return !isDeleted() && getOffset() == off && getLength() == len && fStyle == highlighting;
            }
        }

        /**
         * Is this position contained in the given range (inclusive)? Synchronizes on position updater.
         *
         * @param off The range offset
         * @param len The range length
         * @return <code>true</code> iff this position is not delete and contained in the given range.
         */
        public boolean isContained(int off, int len) {
            synchronized (fLock) {
                return !isDeleted() && off <= getOffset() && off + len >= getOffset() + getLength();
            }
        }

        public void update(int off, int len) {
            synchronized (fLock) {
                super.setOffset(off);
                super.setLength(len);
            }
        }

        /*
         * @see org.eclipse.jface.text.Position#setLength(int)
         */
        public void setLength(int length) {
            synchronized (fLock) {
                super.setLength(length);
            }
        }

        /*
         * @see org.eclipse.jface.text.Position#setOffset(int)
         */
        public void setOffset(int offset) {
            synchronized (fLock) {
                super.setOffset(offset);
            }
        }

        /*
         * @see org.eclipse.jface.text.Position#delete()
         */
        public void delete() {
            synchronized (fLock) {
                super.delete();
            }
        }

        /*
         * @see org.eclipse.jface.text.Position#undelete()
         */
        public void undelete() {
            synchronized (fLock) {
                super.undelete();
            }
        }

        /**
         * @return Returns the highlighting.
         */
        public HighlightingStyle getHighlighting() {
            return fStyle;
        }
    }


    private final Object fReconcileLock= new Object();
    private GroovyEditor editor;
    
    // make these configurable
    private HighlightingStyle staticHighlighting;
    private HighlightingStyle undefinedRefHighlighting;
    
    private SemanticHighlightingPresenter presenter;
    
    /**
     * Contains the highlighted positions from the last reconcile
     */
    private List<HighlightedPosition> oldPositions = new ArrayList<HighlightedPosition>();
    /**
     * <code>true</code> if any thread is executing
     * <code>reconcile</code>, <code>false</code> otherwise.
     */
    private boolean fIsReconciling= false;

    
    public GroovySemanticReconciler() { 
        GroovyColorManager colorManager = GroovyPlugin.getDefault().getTextTools().getColorManager();
        Color color = colorManager.getColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR);
        staticHighlighting = new HighlightingStyle(new TextAttribute(color, null, SWT.ITALIC), true);
        undefinedRefHighlighting = new HighlightingStyle(new TextAttribute(color, null, TextAttribute.UNDERLINE), true);
    }
    
    public void install(GroovyEditor editor, JavaSourceViewer viewer) {
        this.editor = editor;
        this.presenter = new SemanticHighlightingPresenter();
        presenter.install(viewer, (JavaPresentationReconciler) editor.createJavaSourceViewerConfiguration().getPresentationReconciler(viewer));
    }
    
    public void uninstall() {
        presenter.uninstall();
        presenter = null;
        editor = null;
    }

    public void aboutToBeReconciled() { }

    public void reconciled(CompilationUnit ast, boolean forced,
            IProgressMonitor progressMonitor) {
        
        // ensure that only one thread can enter here at a time
        synchronized (fReconcileLock) {
            if (fIsReconciling)
                return;
            else
                fIsReconciling= true;
        }

        try {
            progressMonitor.beginTask("Groovy semantic highlighting", 100);
            
            ModuleNode module = editor.getModuleNode();
            if (module != null) {
                presenter.setCanceled(progressMonitor.isCanceled());
                GatherSemanticReferences visitor = new GatherSemanticReferences();
                List<HighlightedPosition> newPositions = new LinkedList<HighlightedPosition>();
                visitor.visitModule(module);
                progressMonitor.worked(50);

                List<HighlightedPosition> oldPositionsCopy = new LinkedList<HighlightedPosition>();
                oldPositionsCopy.addAll(oldPositions);
                for (Position pos : visitor.staticReferences) {
                    HighlightedPosition range = createHighlightedStaticPosition(pos);
                    maybeAddPosition(newPositions, oldPositionsCopy, range);
                }
                progressMonitor.worked(20);

                for (Position pos : visitor.unknownReferences) {
                    HighlightedPosition range = createHighlightedUnknownPosition(pos);
                    maybeAddPosition(newPositions, oldPositionsCopy, range);
                }
                progressMonitor.worked(20);
                
                TextPresentation textPresentation = null;
                if (!presenter.isCanceled()) {
                    textPresentation= presenter.createPresentation(newPositions, oldPositionsCopy);
                }
                
                if (!presenter.isCanceled()) {
                    updatePresentation(textPresentation, newPositions, oldPositionsCopy);
                    oldPositions = newPositions;
                }
                progressMonitor.worked(10);
            }
        } finally {
            synchronized (fReconcileLock) {
                fIsReconciling= false;
            }
        }
    }

    /**
     * @param unkownNode
     * @return
     */
    private HighlightedPosition createHighlightedUnknownPosition(Position pos) {
        return new HighlightedPosition(pos.offset, pos.length, undefinedRefHighlighting, this);
    }

    /**
     * @param staticNode
     * @return
     */
    private HighlightedPosition createHighlightedStaticPosition(Position pos) {
        return new HighlightedPosition(pos.offset, pos.length, staticHighlighting, this);
    }

    /**
     * @param newPositions
     * @param range
     */
    private void maybeAddPosition(List<HighlightedPosition> newPositions, List<HighlightedPosition> oldPositionsCopy, 
            HighlightedPosition maybePosition) {
        boolean found = false;
        for (Iterator<HighlightedPosition> positionIter = oldPositionsCopy.iterator(); positionIter.hasNext();) {
            HighlightedPosition oldPosition = positionIter.next();
            if (oldPosition.isEqual(maybePosition.offset, maybePosition.length, maybePosition.getHighlighting())) {
                positionIter.remove();
                found = true;
                break;
            }
        }
        if (!found) {
            newPositions.add(maybePosition);
        }
    }
    
    
    /**
     * Update the presentation.
     *
     * @param textPresentation the text presentation
     * @param addedPositions the added positions
     * @param removedPositions the removed positions
     */
    private void updatePresentation(TextPresentation textPresentation, List<HighlightedPosition> addedPositions, List<HighlightedPosition> removedPositions) {
        Runnable runnable= presenter.createUpdateRunnable(textPresentation, addedPositions, removedPositions);
        if (runnable == null)
            return;

        JavaEditor thisEditor= editor;
        if (thisEditor == null)
            return;

        IWorkbenchPartSite site= thisEditor.getSite();
        if (site == null)
            return;

        Shell shell= site.getShell();
        if (shell == null || shell.isDisposed())
            return;

        Display display= shell.getDisplay();
        if (display == null || display.isDisposed())
            return;

        display.asyncExec(runnable);
    }

}
