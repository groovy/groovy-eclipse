/*******************************************************************************
 * Copyright (c) 2004, 2009  IBM Corporation, SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 *              Andrew Eisenberg - convert for use with Groovy
 ******************************************************************************/
package org.codehaus.groovy.eclipse.debug.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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
	 * The document which contains the code source.
	 */
	private IDocument fDocument;
	
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

	private int fOffset;
	
	public BreakpointLocationVerifierJob(IDocument document, IJavaLineBreakpoint breakpoint, int offset, int lineNumber, String typeName, IType type, IResource resource, IEditorPart editorPart) {
		super(ActionMessages.BreakpointLocationVerifierJob_breakpoint_location); 
		fDocument= document;
		fBreakpoint= breakpoint;
		fOffset = offset;
		fLineNumber= lineNumber;
		fTypeName= typeName;
		fType= type;
		fResource= resource;
		fStatusLine= (IEditorStatusLine) editorPart.getAdapter(IEditorStatusLine.class);
//		setSystem(true);
	}
	
	public IStatus run(IProgressMonitor monitor) {
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) fResource);
		try {
			IJavaElement element = cu.getElementAt(fOffset);
			if(element == null 
					|| element instanceof ICompilationUnit 
					|| element instanceof IType
					|| element instanceof IField
					|| emptyOrComment(fDocument.get(fOffset, fDocument.getLineInformation(fLineNumber - 1).getLength()))) {

				if (fBreakpoint != null) {
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
				}
				int lineNumber = fLineNumber + 1;
				while (lineNumber < fDocument.getNumberOfLines()) {
					IRegion line = fDocument.getLineInformation(lineNumber - 1);
					int offset = line.getOffset();
                    element = cu.getElementAt(offset);
                    if (!(element == null 
							|| element instanceof ICompilationUnit 
							|| element instanceof IType
							|| element instanceof IField
							|| emptyOrComment(fDocument.get(offset, line.getLength())))) {
						createNewBreakpoint(lineNumber, fTypeName);
						return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null); 
					}
					lineNumber++;
				}
				// Cannot find a valid location
				report(ActionMessages.BreakpointLocationVerifierJob_not_valid_location); 
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null); 
			}			
		} catch (JavaModelException e) {
		} catch (CoreException e) {
		} catch (BadLocationException e) {
		}
		return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpoint_set, null); 
		
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
	private void createNewBreakpoint(int lineNumber, String typeName) throws CoreException {
		Map newAttributes = new HashMap(10);
		if (fType != null) {
			try {
				IRegion line= fDocument.getLineInformation(lineNumber - 1);
				int start= line.getOffset();
				int end= start + line.getLength() - 1;
				BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(newAttributes, fType, start, end);
			} catch (BadLocationException ble) {
				JDIDebugUIPlugin.log(ble);
			}
		}
		JDIDebugModel.createLineBreakpoint(fResource, typeName, lineNumber, -1, -1, 0, true, newAttributes);
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
