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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * Job used to verify the position of a breakpoint 
 * Somewhat based on org.eclipse.jdt.internal.debug.ui.actions.BreakpointLocationVerifierJob
 */
public class BreakpointLocationVerifierJob extends Job {

    public final static Object FAMILY = new Object();
	
	/**
	 * The temporary breakpoint that has been set. Can be <code>null</code> if the callee was not able
	 * to check if a breakpoint was already set at this position.
	 */	
	private IJavaLineBreakpoint fBreakpoint;
	
	/**
	 * The number of the line where the breakpoint has been requested.
	 */
	private int fLineNumber;
	
	/**
	 * The qualified type name of the class where the temporary breakpoint as been set.
	 * Can be <code>null</code> if fBreakpoint is null.
	 */	
	private String fTypeName;
	
	/**
	 * The type in which should be set the breakpoint.
	 */
	private IType fType;

	/**
	 * The resource in which should be set the breakpoint.
	 */
	private IResource fResource;
	
	
	/**
	 * The status line to use to display errors
	 */
	private IEditorStatusLine fStatusLine;

	public BreakpointLocationVerifierJob(IJavaLineBreakpoint breakpoint, int lineNumber, String typeName, IType type, IResource resource, IEditorPart editorPart) {
		super(ActionMessages.BreakpointLocationVerifierJob_breakpoint_location); 
		fBreakpoint= breakpoint;
		fLineNumber= lineNumber;
		fTypeName= typeName;
		fType= type;
		fResource= resource;
		fStatusLine= (IEditorStatusLine) editorPart.getAdapter(IEditorStatusLine.class);
	}
	
	public IStatus run(IProgressMonitor monitor) {
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) fResource);
		try {
		    ModuleNode node = null;
		    if (cu instanceof GroovyCompilationUnit) {
		        node = ((GroovyCompilationUnit) cu).getModuleNode();
		    } 
		    
		    if (node == null) {
		        return new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null);
		    }
		    
            if (fBreakpoint != null) {
                DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
            }
		    ValidBreakpointLocationFinder finder = new ValidBreakpointLocationFinder(fLineNumber);
		    ASTNode valid = finder.findValidBreakpointLocation(node);
		    if (valid != null) {
		        createNewBreakpoint(valid, fTypeName);
		        return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpoint_set, null); 
		    }		    
		} catch (JavaModelException e) {
		} catch (CoreException e) {
		}
		// Cannot find a valid location
		report(ActionMessages.BreakpointLocationVerifierJob_not_valid_location); 
		return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null); 
		
	}
	
	/**
	 * Best approximation for whether a line is empty or commented out
	 * @param line
	 * @return
	 */
	private boolean emptyOrComment(String line) {
		String trimmed = line.trim();
		return trimmed.equals("") //$NON-NLS-1$
			|| trimmed.startsWith("/*") //$NON-NLS-1$
			|| trimmed.startsWith("*") //$NON-NLS-1$
			|| trimmed.startsWith("//"); //$NON-NLS-1$
	}


	/**
	 * Create a new breakpoint at the right position.
	 */
	private void createNewBreakpoint(ASTNode node, String typeName) throws CoreException {
		Map newAttributes = new HashMap(10);
		int start= node.getStart();
		int end= node.getEnd();
		if (fType != null) {
			BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(newAttributes, fType, start, end);
		}
		JDIDebugModel.createLineBreakpoint(fResource, typeName, node.getLineNumber(), start, end, 0, true, newAttributes);
	}

	protected void report(final String message) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fStatusLine != null) {
					fStatusLine.setMessage(true, message, null);
				}
				if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
					Display.getCurrent().beep();
				}
			}
		});
	}
	
	@Override
	public boolean belongsTo(Object family) {
	    return family == FAMILY;
	}
}
