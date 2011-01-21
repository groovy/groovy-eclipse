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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ActionDelegateHelper;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Toggles a line breakpoint in a Java editor.
 * Based on org.eclipse.jdt.internal.debug.ui.actions.ToggleBreakpointAdapter,
 * but uses
 * BreakpointLocationVerifierJob and ValidBreakpointLocationLocator from this
 * package and does not
 * support field watchpoints.
 *
 * Borrowed from same class in AJDT
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	public final static Object TOGGLE_BREAKPOINT_FAMILY = new Object();

	public ToggleBreakpointAdapter() {
		// init helper in UI thread
		ActionDelegateHelper.getDefault();
	}

    protected void report(final String message, final IWorkbenchPart part) {
        JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                IEditorStatusLine statusLine = (IEditorStatusLine) part.getAdapter(IEditorStatusLine.class);
                if (statusLine != null) {
                    if (message != null) {
                        statusLine.setMessage(true, message, null);
                    } else {
                        statusLine.setMessage(true, null, null);
                    }
                }
                if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
                    JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
                }
            }
        });
    }

    protected IType getType(ITextSelection selection) {
        IMember member = ActionDelegateHelper.getDefault().getCurrentMember(selection);
        IType type = null;
        if (member instanceof IType) {
            type = (IType) member;
        } else if (member != null) {
            type = member.getDeclaringType();
        }
        // bug 52385: we don't want local and anonymous types from compilation
        // unit,
        // we are getting 'not-always-correct' names for them.
        try {
            while (type != null && !type.isBinary() && type.isLocal()) {
                type = type.getDeclaringType();
            }
        } catch (JavaModelException e) {
            JDIDebugUIPlugin.log(e);
        }
        return type;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(IWorkbenchPart,
     *      ISelection)
     */
    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        toggleLineBreakpoints(part, selection, false);
    }

    public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection, final boolean bestMatch) {
        Job job = new Job("Toggle Line Breakpoint") { //$NON-NLS-1$
            @Override
            public boolean belongsTo(Object family) {
                return family == TOGGLE_BREAKPOINT_FAMILY;
            }
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                if (selection instanceof ITextSelection) {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    report(null, part);
                    IEditorPart editorPart = (IEditorPart) part;
                    ITextSelection textSelection = (ITextSelection) selection;
                    IType type = getType(textSelection);
                    IEditorInput editorInput = editorPart.getEditorInput();
                    IDocumentProvider documentProvider = ((ITextEditor) editorPart).getDocumentProvider();
                    if (documentProvider == null) {
                        return Status.CANCEL_STATUS;
                    }
                    IDocument document = documentProvider.getDocument(editorInput);
                    int lineNumber = textSelection.getStartLine() + 1;
                    int offset = textSelection.getOffset();
                    try {
                        if (type == null) {
                            IClassFile classFile = (IClassFile) editorInput.getAdapter(IClassFile.class);
                            if (classFile != null) {
                                type = classFile.getType();
                                // bug 34856 - if this is an inner type, ensure
                                // the breakpoint is not
                                // being added to the outer type
                                if (type.getDeclaringType() != null) {
                                    ISourceRange sourceRange = type.getSourceRange();
                                    int start = sourceRange.getOffset();
                                    int end = start + sourceRange.getLength();
                                    if (offset < start || offset > end) {
                                        // not in the inner type
                                        IStatusLineManager statusLine = editorPart.getEditorSite().getActionBars().getStatusLineManager();
                                        statusLine.setErrorMessage(NLS.bind("Breakpoints can only be created within the type associated with the editor: {0}.", new String[] { type.getTypeQualifiedName() }));
                                        Display.getCurrent().beep();
                                        return Status.OK_STATUS;
                                    }
                                }
                            }
                        }

                        String typeName = null;
                        IResource resource = null;
                        Map attributes = new HashMap(10);
                        if (type == null) {
                            resource = getResource(editorPart);
                            if (editorPart instanceof ITextEditor) {
                                ModuleNode node = getModuleNode((ITextEditor) editorPart);
                                if (node != null) {  // can be null if not on build path
                                    for (ClassNode clazz : (Iterable<ClassNode>) node.getClasses()) {
                                        int begin = clazz.getStart();
                                        int end = clazz.getEnd();
                                        if (offset >= begin && offset <= end && !clazz.isInterface()) {
                                            typeName = clazz.getName();
                                            break;
                                        }
                                    }
                                }
                            }
                            if(typeName == null) {
                            	ICompilationUnit unit = JavaCore.createCompilationUnitFrom((IFile) resource);
                            	if(unit != null) {
	                            	IType[] types = unit.getAllTypes();
	                            	for (int i = 0; i < types.length; i++) {
	                            		 int begin = types[i].getSourceRange().getOffset();
	                                     int end = begin + types[i].getSourceRange().getLength();
	                                     if (offset >= begin && offset <= end && !types[i].isInterface()) {
	                                         typeName = types[i].getPackageFragment().getElementName() + "." + types[i].getTypeQualifiedName(); //$NON-NLS-1$
	                                         break;
	                                     }
									}
                            	}
                            }
                        } else {
                            typeName = type.getFullyQualifiedName();
                            int index = typeName.indexOf('$');
                            if (index >= 0) {
                                typeName = typeName.substring(0, index);
                            }
                            resource = BreakpointUtils.getBreakpointResource(type);
                            try {
                                IRegion line = document.getLineInformation(lineNumber - 1);
                                int start = line.getOffset();
                                int end = start + line.getLength() - 1;
                                BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, type, start, end);
                            } catch (BadLocationException ble) {
                                JDIDebugUIPlugin.log(ble);
                            }
                        }

                        if (typeName != null && resource != null) {
                            IJavaLineBreakpoint existingBreakpoint = JDIDebugModel.lineBreakpointExists(resource, typeName, lineNumber);
                            if (existingBreakpoint != null) {
                                removeBreakpoint(existingBreakpoint, true);
                                return Status.OK_STATUS;
                            }
                            createLineBreakpoint(resource, typeName, offset, lineNumber, -1, -1, 0, true, attributes, document, bestMatch, type, editorPart);
                        }
                    } catch (CoreException ce) {
                        return ce.getStatus();
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private void createLineBreakpoint(IResource resource, String typeName, int offset, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes, IDocument document, boolean bestMatch, IType type, IEditorPart editorPart) throws CoreException {
        IJavaLineBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(resource, typeName, lineNumber, charStart, charEnd, hitCount, register, attributes);
        new BreakpointLocationVerifierJob(breakpoint, lineNumber, typeName, type, resource, editorPart).schedule();
    }

    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
        return selection instanceof ITextSelection;
    }

    public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection finalSelection) {
    }

    private void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException {
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, delete);
    }

    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
    	return false;
    }

    protected ModuleNode getModuleNode(ITextEditor editor) throws CoreException {
        IEditorInput editorInput = editor.getEditorInput();
        ICompilationUnit unit = (ICompilationUnit) editorInput.getAdapter(ICompilationUnit.class);
        if (unit == null) {
            IFile file = (IFile) editorInput.getAdapter(IFile.class);
            if (file != null) {
                unit = JavaCore.createCompilationUnitFrom(file);
            }
        }
        if (! (unit instanceof GroovyCompilationUnit)) {
            throw new CoreException(Status.CANCEL_STATUS);
        }
        return ((GroovyCompilationUnit) unit).getModuleNode();
    }

    public void toggleWatchpoints(final IWorkbenchPart part, final ISelection finalSelection) {
    }

    protected static IResource getResource(IEditorPart editor) {
        IEditorInput editorInput = editor.getEditorInput();
        IResource resource = (IResource) editorInput.getAdapter(IFile.class);
        if (resource == null) {
            resource = ResourcesPlugin.getWorkspace().getRoot();
        }
        return resource;
    }

    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
    	return false;
    }

    public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        toggleLineBreakpoints(part, selection, true);
    }

    public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
        return canToggleLineBreakpoints(part, selection);
    }
}
