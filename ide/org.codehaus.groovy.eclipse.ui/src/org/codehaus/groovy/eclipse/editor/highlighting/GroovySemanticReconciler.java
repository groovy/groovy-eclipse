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


import greclipse.org.eclipse.jdt.internal.ui.javaeditor.HighlightedPosition;
import greclipse.org.eclipse.jdt.internal.ui.javaeditor.HighlightingStyle;
import greclipse.org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingPresenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.editor.GroovyColorManager;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.JavaPresentationReconciler;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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

    private final Object fReconcileLock= new Object();
    private GroovyEditor editor;

    // make these configurable
    private HighlightingStyle undefinedRefHighlighting;

    private HighlightingStyle regexRefHighlighting;

    private HighlightingStyle deprecatedRefHighlighting;

    private HighlightingStyle fieldRefHighlighting;

    private HighlightingStyle staticFieldRefHighlighting;

    private HighlightingStyle methodRefHighlighting;

    private HighlightingStyle staticMethodRefHighlighting;

    private HighlightingStyle numberRefHighlighting;
    private SemanticHighlightingPresenter presenter;

    /**
     * <code>true</code> if any thread is executing
     * <code>reconcile</code>, <code>false</code> otherwise.
     */
    private boolean fIsReconciling= false;


    public GroovySemanticReconciler() {
        RGB rgbString = PreferenceConverter.getColor(GroovyPlugin.getDefault().getPreferenceStore(),
                PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
        RGB rgbNumber = PreferenceConverter.getColor(GroovyPlugin.getDefault().getPreferenceStore(),
                PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR);
        RGB rgbField = findRGB("semanticHighlighting.field.color", new RGB(0, 0, 192));
        RGB rgbMethod = findRGB("semanticHighlighting.method.color", new RGB(0, 0, 0));
        GroovyColorManager colorManager = GroovyPlugin.getDefault().getTextTools().getColorManager();
        Color regexColor = colorManager.getColor(rgbString);
        Color fieldColor = colorManager.getColor(rgbField);
        Color methodColor = colorManager.getColor(rgbMethod);
        Color numberColor = colorManager.getColor(rgbNumber);
        undefinedRefHighlighting = new HighlightingStyle(new TextAttribute(null, null, TextAttribute.UNDERLINE), true);
        regexRefHighlighting = new HighlightingStyle(new TextAttribute(regexColor, null, SWT.ITALIC), true);
        numberRefHighlighting = new HighlightingStyle(new TextAttribute(numberColor), true);
        deprecatedRefHighlighting = new HighlightingStyle(new TextAttribute(null, null, TextAttribute.STRIKETHROUGH), true);
        fieldRefHighlighting = new HighlightingStyle(new TextAttribute(fieldColor), true);
        staticFieldRefHighlighting = new HighlightingStyle(new TextAttribute(fieldColor, null, SWT.ITALIC), true);
        methodRefHighlighting = new HighlightingStyle(new TextAttribute(methodColor), true);
        staticMethodRefHighlighting = new HighlightingStyle(new TextAttribute(methodColor, null, SWT.ITALIC), true);
    }


    private static RGB findRGB(String key, RGB defaultRGB) {
        return PreferenceConverter.getColor(JavaPlugin.getDefault().getPreferenceStore(), key);
    }

    public void install(GroovyEditor editor, JavaSourceViewer viewer) {
        this.editor = editor;
        this.presenter = new SemanticHighlightingPresenter();
        presenter.install(viewer, (JavaPresentationReconciler) editor.getGroovyConfiguration().getPresentationReconciler(viewer));
    }

    public void uninstall() {
        presenter.uninstall();
        presenter = null;
        editor = null;
    }

    public void aboutToBeReconciled() { }

    @SuppressWarnings("unchecked")
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

            GroovyCompilationUnit unit = editor.getGroovyCompilationUnit();
            if (unit != null) {
                presenter.setCanceled(progressMonitor.isCanceled());
                GatherSemanticReferences finder = new GatherSemanticReferences(unit);
                Collection<HighlightedTypedPosition> semanticReferences = finder.findSemanticHighlightingReferences();
                progressMonitor.worked(50);

                List<HighlightedPosition> newPositions = new LinkedList<HighlightedPosition>();
                List<HighlightedPosition> removedPositions = new LinkedList<HighlightedPosition>();

                for (HighlightedPosition oldPosition : (Iterable<HighlightedPosition>) presenter.fPositions) {
                    if (oldPosition != null) {
                        removedPositions.add(oldPosition);
                    }
                }
                progressMonitor.worked(20);
                List<HighlightedPosition> semanticReferencesHighlighted = new ArrayList<HighlightedPosition>(semanticReferences.size());
                for (HighlightedTypedPosition pos : semanticReferences) {
                    HighlightedPosition range = createHighlightedPosition(pos);
                    maybeAddPosition(newPositions, removedPositions, range);
                    semanticReferencesHighlighted.add(range);
                }
                progressMonitor.worked(20);

                TextPresentation textPresentation = null;
                if (!presenter.isCanceled()) {
                    textPresentation= presenter.createPresentation(newPositions, removedPositions);
                }

                if (!presenter.isCanceled()) {
                    updatePresentation(textPresentation, newPositions, removedPositions);
                }
                progressMonitor.worked(10);
            }
        } catch (NullPointerException e) {
            // do nothing...reconciler has been uninstalled
        } finally {
            synchronized (fReconcileLock) {
                fIsReconciling= false;
            }
        }
    }

    private HighlightedPosition createHighlightedPosition(HighlightedTypedPosition pos) {
        switch (pos.kind) {
            case UNKNOWN:
                return new HighlightedPosition(pos.offset, pos.length, undefinedRefHighlighting, this);
            case NUMBER:
                return new HighlightedPosition(pos.offset, pos.length, numberRefHighlighting, this);
            case REGEX:
                return new HighlightedPosition(pos.offset, pos.length, regexRefHighlighting, this);
            case DEPRECATED:
                return new HighlightedPosition(pos.offset, pos.length, deprecatedRefHighlighting, this);
            case FIELD:
                return new HighlightedPosition(pos.offset, pos.length, fieldRefHighlighting, this);
            case STATIC_FIELD:
                return new HighlightedPosition(pos.offset, pos.length, staticFieldRefHighlighting, this);
            case METHOD:
                return new HighlightedPosition(pos.offset, pos.length, methodRefHighlighting, this);
            case STATIC_METHOD:
                return new HighlightedPosition(pos.offset, pos.length, staticMethodRefHighlighting, this);
        }
        // won't get here
        return null;
    }

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
