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
package org.codehaus.groovy.eclipse.dsl.checker;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Shell;

/**
 * Adds resource markers for all unknown and type assertion failures
 * 
 * @author andrew
 * @created Aug 29, 2011
 */
public class ResourceMarkerHandler implements IStaticCheckerHandler {

    private IFile resource;
    
    private int numFound = 0;

    public void setResource(IFile resource) {
        this.resource = resource;
    }
    public void handleUnknownReference(ASTNode node, Position position, int line) {
        numFound++;
        createMarker(position, line, createUnknownMessage(node));
    }

    public void handleTypeAssertionFailed(ASTNode node, String expectedType, String actualType, Position position, int line) {
        numFound++;
        createMarker(position, line, createInvalidTypeMessage(node, expectedType, actualType));
    }

    private String createUnknownMessage(ASTNode node) {
        return "Type of expression is statically unknown: " + node.getText();
    }
    
    private String createInvalidTypeMessage(ASTNode node, String expectedType, String actualType) {
        return "Invalid inferred type.  Expected: " + expectedType + " Actual: " + actualType;
    }
    
    private void createMarker(Position position, int line, String message) {
        try {
            IMarker marker = resource.createMarker(GroovyDSLCoreActivator.MARKER_ID);
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
            marker.setAttribute(IMarker.CHAR_START, position.offset);
            marker.setAttribute(IMarker.CHAR_END, position.offset + position.length);
            marker.setAttribute(IMarker.LINE_NUMBER, line);
            marker.setAttribute(IMarker.LOCATION, "Type checking");
            marker.setAttribute(IMarker.SOURCE_ID, "Groovy");
            marker.setAttribute(IMarker.MESSAGE, message);
        } catch (CoreException e) {
            GroovyCore.logException("Unable to create marker on " + resource.getFullPath(), e);
        }
    }
    public int numProblemsFound() {
        return numFound;
    }
    public void handleResourceStart(IResource resource) throws CoreException {
        resource.deleteMarkers(GroovyDSLCoreActivator.MARKER_ID, true, IResource.DEPTH_ZERO);
    }
    
    public boolean finish(Shell shell) {
        if (shell != null) {
            if (numProblemsFound() == 0) {
                MessageDialog.openInformation(shell, "Static type checking complete", "Static type checking complete. Found no problems.");
            } else if (numProblemsFound() == 1) {
                MessageDialog.openInformation(shell, "Static type checking complete", "Static type checking complete. Found one problem.  See Problems view.");
            } else {
                MessageDialog.openInformation(shell, "Static type checking complete", "Static type checking complete. Found " + numProblemsFound() + " problems.  See Problems view.");
            }
        }
        return numFound == 0;
    }

}
