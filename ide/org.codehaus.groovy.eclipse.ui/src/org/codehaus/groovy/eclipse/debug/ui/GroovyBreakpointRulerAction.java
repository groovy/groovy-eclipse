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
package org.codehaus.groovy.eclipse.debug.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

public class GroovyBreakpointRulerAction extends Action {

    private IVerticalRulerInfo fRuler;
    private ITextEditor fTextEditor;
    private final IEditorStatusLine fStatusLine;
    private final ToggleBreakpointAdapter fBreakpointAdapter;

    public GroovyBreakpointRulerAction(IVerticalRulerInfo ruler, ITextEditor editor, IEditorPart editorPart) {
        super("Toggle &Breakpoint");
        fRuler = ruler;
        fTextEditor = editor;
        fStatusLine = Adapters.adapt(editorPart, IEditorStatusLine.class);
        fBreakpointAdapter = new ToggleBreakpointAdapter();
    }

    public void dispose() {
        fTextEditor = null;
        fRuler = null;
    }

    @Override
    public void run() {
        try {
            List<IMarker> list = getMarkers();
            if (list.isEmpty()) {
                // create new markers
                IDocument document = fTextEditor.getDocumentProvider().getDocument(fTextEditor.getEditorInput());
                int lineNumber = fRuler.getLineOfLastMouseButtonActivity();
                if (lineNumber >= document.getNumberOfLines()) {
                    return;
                }
                try {
                    IRegion line = document.getLineInformation(lineNumber);
                    ITextSelection selection = new TextSelection(document, line.getOffset(), line.getLength());
                    fBreakpointAdapter.toggleLineBreakpoints(fTextEditor, selection);
                } catch (BadLocationException e) {
                    // likely document is folded so you cannot get the line information of the folded line
                }
            } else {
                // remove existing breakpoints of any type
                IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
                Iterator<IMarker> iterator = list.iterator();
                while (iterator.hasNext()) {
                    IMarker marker = iterator.next();
                    IBreakpoint breakpoint = manager.getBreakpoint(marker);
                    if (breakpoint != null) {
                        breakpoint.delete();
                    }
                }
            }
        } catch (CoreException e) {
            JDIDebugUIPlugin.statusDialog("Failed to add breakpoint", e.getStatus());
        } catch (RuntimeException e) {
            JDIDebugUIPlugin.errorDialog("Failed to add breakpoint", e);
        }
    }

    /**
     * Returns a list of markers that exist at the current ruler location.
     */
    protected List<IMarker> getMarkers() {
        List<IMarker> breakpoints = Collections.emptyList();
        try {
            IEditorInput editorInput = fTextEditor.getEditorInput();
            IDocumentProvider provider = fTextEditor.getDocumentProvider();
            IAnnotationModel model = provider.getAnnotationModel(editorInput);
            if (model instanceof AbstractMarkerAnnotationModel) {
                IMarker[] markers;
                if (editorInput instanceof IFileEditorInput) {
                    markers = ((IFileEditorInput) editorInput).getFile().findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
                } else {
                    markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
                }
                if (markers != null) {
                    IDocument document = provider.getDocument(editorInput);
                    IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
                    for (IMarker marker : markers) {
                        IBreakpoint breakpoint = breakpointManager.getBreakpoint(marker);
                        if (breakpoint != null && breakpointManager.isRegistered(breakpoint) &&
                                includesRulerLine(((AbstractMarkerAnnotationModel) model).getMarkerPosition(marker), document)) {
                            if (breakpoints.isEmpty())
                                breakpoints = new ArrayList<>();
                            breakpoints.add(marker);
                        }
                    }
                }
            }
        } catch (CoreException e) {
            JDIDebugUIPlugin.log(e.getStatus());
        } catch (RuntimeException e) {
            JDIDebugUIPlugin.log(e);
        }
        return breakpoints;
    }

    /**
     * Checks whether a position includes the ruler's line of activity.
     *
     * @param position the position to be checked
     * @param document the document the position refers to
     * @return <code>true</code> if the line is included by the given position
     */
    protected boolean includesRulerLine(Position position, IDocument document) {
        if (position != null) {
            try {
                int markerLine = document.getLineOfOffset(position.getOffset());
                int line = fRuler.getLineOfLastMouseButtonActivity();
                if (line == markerLine) {
                    return true;
                }
            } catch (BadLocationException ignore) {
            }
        }
        return false;
    }

    protected void report(final String message) {
        JDIDebugUIPlugin.getStandardDisplay().asyncExec(() -> {
            if (fStatusLine != null) {
                fStatusLine.setMessage(true, message, null);
            }
            if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
                Display.getCurrent().beep();
            }
        });
    }
}
