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

package org.codehaus.groovy.eclipse.debug.ui;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointMarkerUpdater;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Largely borrowed from {@link BreakpointMarkerUpdater}
 * 
 * @author Andrew Eisenberg
 * @created Oct 6, 2009
 *
 */
public class BreakpointUpdater implements IMarkerUpdater {

    public String[] getAttribute() {
        return new String[] {IMarker.LINE_NUMBER};
    }

    public String getMarkerType() {
        return "org.eclipse.debug.core.breakpointMarker"; //$NON-NLS-1$
    }

    public boolean updateMarker(IMarker marker, IDocument document,
            Position position) {
        GroovyCompilationUnit unit = getCompilationUnit(marker);
        if (unit == null) {
            // ignore non-GroovyCompilationUnits
            return true;
        }

        if(position.isDeleted()) {
            return false;
        }
        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint breakpoint = manager.getBreakpoint(marker);
        if(breakpoint == null) {
            return false;
        }
        try {
            Object attribute = marker.getAttribute(IMarker.LINE_NUMBER);
            if (attribute != null) {
                ValidBreakpointLocationFinder finder = new ValidBreakpointLocationFinder(
                        ((Integer) attribute).intValue());
                ASTNode validNode = finder.findValidBreakpointLocation(unit.getModuleNode());
                if (validNode == null) {
                    return false;
                }
                int line = validNode.getLineNumber();
                MarkerUtilities.setLineNumber(marker, line);
                if(isLineBreakpoint(marker)) {
                    ensureRanges(document, marker, line);
                    return lineBreakpointExists(marker.getResource(), ((IJavaLineBreakpoint)breakpoint).getTypeName(), line, marker) == null;
                }
            }

            return true;
        } catch (CoreException e) {
            GroovyCore.logException("Error updating breakpoint", e);
            return false;
        } catch (BadLocationException e) {
            GroovyCore.logException("Error updating breakpoint", e);
            return false;
        }
    }

    /**
     * Finds the groovy compilation unit associated with this marker's resource.
     * Tries to get a working copy first, or otherwise creates a new compilation unit
     * @param marker
     * @return {@link GroovyCompilationUnit} associated with this marker's resource or
     * null if none exists.
     */
    private GroovyCompilationUnit getCompilationUnit(IMarker marker) {
        IResource resource = marker.getResource();
        if (!Util.isJavaLikeFileName(resource.getName())) {
            return null;
        }
        ICompilationUnit unit = JavaPlugin.getDefault().getCompilationUnitDocumentProvider().getWorkingCopy(resource);
        if (unit == null && resource.getType() == IResource.FILE) {
            // nope...must create from new
            unit = JavaCore.createCompilationUnitFrom((IFile) resource);
        }
        if (unit != null && unit instanceof GroovyCompilationUnit) {
            return (GroovyCompilationUnit) unit;
        } else {
            return null;
        }
    }

    /**
     * Updates the charstart and charend ranges if necessary for the given line.
     * Returns immediately if the line is not valid (< 0 or greater than the total line number count)
     * @param document
     * @param marker
     * @param line
     * @throws BadLocationException
     */
    private void ensureRanges(IDocument document, IMarker marker, int line) throws BadLocationException {
        if(line < 0 || line > document.getNumberOfLines()) {
            return;
        }
        IRegion region = document.getLineInformation(line - 1);
        int charstart = region.getOffset();
        int charend = charstart + region.getLength();
        MarkerUtilities.setCharStart(marker, charstart);
        MarkerUtilities.setCharEnd(marker, charend);
    }

    /**
     * Returns if the specified marker is for an <code>IJavaLineBreakpoint</code>
     * @param marker
     * @return true if the marker is for an <code>IJavalineBreakpoint</code>, false otherwise
     * 
     * @since 3.4
     */
    private boolean isLineBreakpoint(IMarker marker) {
        return MarkerUtilities.isMarkerType(marker, "org.eclipse.jdt.debug.javaLineBreakpointMarker"); //$NON-NLS-1$
    }

    /**
     * Searches for an existing line breakpoint on the specified line in the current type that does not match the id of the specified marker
     * @param resource the resource to care about
     * @param typeName the name of the type the breakpoint is in
     * @param lineNumber the number of the line the breakpoint is on
     * @param currentmarker the current marker we are comparing to see if it will be moved onto an existing one
     * @return an existing line breakpoint on the current line of the given resource and type if there is one
     * @throws CoreException
     * 
     * @since 3.4
     */
    private IJavaLineBreakpoint lineBreakpointExists(IResource resource, String typeName, int lineNumber, IMarker currentmarker) throws CoreException {
        String modelId = JDIDebugPlugin.getUniqueIdentifier();
        String markerType= JavaLineBreakpoint.getMarkerType();
        IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints= manager.getBreakpoints(modelId);
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof IJavaLineBreakpoint)) {
                continue;
            }
            IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) breakpoints[i];
            IMarker marker = breakpoint.getMarker();
            if (marker != null && marker.exists() && marker.getType().equals(markerType) && currentmarker.getId() != marker.getId()) {
                String breakpointTypeName = breakpoint.getTypeName();
                if ((breakpointTypeName.equals(typeName) || breakpointTypeName.startsWith(typeName + '$')) &&
                        breakpoint.getLineNumber() == lineNumber &&
                        resource.equals(marker.getResource())) {
                    return breakpoint;
                }
            }
        }
        return null;
    }


}
