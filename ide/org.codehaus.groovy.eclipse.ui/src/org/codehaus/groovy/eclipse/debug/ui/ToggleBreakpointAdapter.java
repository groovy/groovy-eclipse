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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
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
import org.eclipse.jdt.core.IOrdinaryClassFile;
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

    public static final Object TOGGLE_BREAKPOINT_FAMILY = new Object();

    protected static IResource getResource(IEditorPart editor) {
        IResource resource = Adapters.adapt(editor.getEditorInput(), IFile.class);
        if (resource == null) {
            resource = ResourcesPlugin.getWorkspace().getRoot();
        }
        return resource;
    }

    public ToggleBreakpointAdapter() {
        // init helper in UI thread
        ActionDelegateHelper.getDefault();
    }

    //--------------------------------------------------------------------------

    @Override
    public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
        return (selection instanceof ITextSelection);
    }

    @Override
    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
        return canToggleBreakpoints(part, selection);
    }

    @Override
    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
        return canToggleBreakpoints(part, selection);
    }

    @Override
    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
        return canToggleBreakpoints(part, selection);
    }

    @Override
    public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) {
        Job job = new Job("Toggle Breakpoint") {
            @Override
            public boolean belongsTo(Object family) {
                return (family == TOGGLE_BREAKPOINT_FAMILY);
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
                            IClassFile classFile = Adapters.adapt(editorInput, IClassFile.class);
                            if (classFile instanceof IOrdinaryClassFile) {
                                type = ((IOrdinaryClassFile) classFile).getType();
                                // bug 34856 - if this is an inner type, ensure
                                // the breakpoint is not being added to the outer type
                                if (type.getDeclaringType() != null) {
                                    ISourceRange sourceRange = type.getSourceRange();
                                    int start = sourceRange.getOffset();
                                    int end = start + sourceRange.getLength();
                                    if (offset < start || offset > end) {
                                        // not in the inner type
                                        IStatusLineManager statusLine = editorPart.getEditorSite().getActionBars().getStatusLineManager();
                                        statusLine.setErrorMessage(NLS.bind("Breakpoints can only be created within the type associated with the editor: {0}.", new Object[] {type.getTypeQualifiedName()}));
                                        Display.getCurrent().beep();
                                        return Status.OK_STATUS;
                                    }
                                }
                            }
                        }

                        String typeName = null;
                        IResource resource = null;
                        Map<String, Object> attributes = new HashMap<>(10);
                        if (type == null) {
                            resource = getResource(editorPart);
                            if (editorPart instanceof ITextEditor) {
                                ModuleNode node = Adapters.adapt(editorInput, ModuleNode.class);
                                if (node != null) { // can be null if not on build path
                                    for (ClassNode clazz : (Iterable<ClassNode>) node.getClasses()) {
                                        if (offset >= clazz.getStart() && offset <= clazz.getEnd() && !clazz.isInterface()) {
                                            typeName = clazz.getName();
                                            break;
                                        }
                                    }
                                }
                            }
                            if (typeName == null) {
                                ICompilationUnit unit = JavaCore.createCompilationUnitFrom((IFile) resource);
                                if (unit != null) {
                                    IType[] types = unit.getAllTypes();
                                    for (int i = 0; i < types.length; i += 1) {
                                        int begin = types[i].getSourceRange().getOffset();
                                        int end = begin + types[i].getSourceRange().getLength();
                                        if (offset >= begin && offset <= end && !types[i].isInterface()) {
                                            typeName = types[i].getPackageFragment().getElementName() + '.' + types[i].getTypeQualifiedName();
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
                                int start = line.getOffset(), end = start + line.getLength() - 1;
                                BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, type, start, end);
                            } catch (BadLocationException ble) {
                                JDIDebugUIPlugin.log(ble);
                            }
                        }

                        if (typeName != null) {
                            IJavaLineBreakpoint existingBreakpoint = JDIDebugModel.lineBreakpointExists(typeName, lineNumber);
                            if (existingBreakpoint != null) {
                                removeBreakpoint(existingBreakpoint, true);
                                return Status.OK_STATUS;
                            }
                            if (resource != null) {
                                boolean register = true;
                                int charStart = -1, charEnd = -1, hitCount = 0;
                                IJavaLineBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(
                                    resource, typeName, lineNumber, charStart, charEnd, hitCount, register, attributes);
                                new BreakpointLocationVerifierJob(breakpoint, lineNumber, typeName, type, resource, editorPart).schedule();
                            }
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

    @Override
    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
        toggleBreakpoints(part, selection);
    }

    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection, boolean bestMatch) {
        toggleBreakpoints(part, selection);
    }

    @Override
    public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
        toggleBreakpoints(part, selection);
    }

    @Override
    public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) {
        toggleBreakpoints(part, selection);
    }

    //--------------------------------------------------------------------------

    protected IType getType(ITextSelection selection) {
        IMember member = ActionDelegateHelper.getDefault().getCurrentMember(selection);
        IType type = null;
        if (member instanceof IType) {
            type = (IType) member;
        } else if (member != null) {
            type = member.getDeclaringType();
        }
        // bug 52385: don't want local and anonymous types from compilation unit
        // we are getting 'not-always-correct' names for them
        try {
            while (type != null && !type.isBinary() && type.isLocal()) {
                type = type.getDeclaringType();
            }
        } catch (JavaModelException e) {
            JDIDebugUIPlugin.log(e);
        }
        return type;
    }

    private void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException {
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, delete);
    }

    protected void report(String message, IWorkbenchPart part) {
        JDIDebugUIPlugin.getStandardDisplay().asyncExec(() -> {
            IEditorStatusLine statusLine = Adapters.adapt(part, IEditorStatusLine.class);
            if (statusLine != null) {
                statusLine.setMessage(true, message, null);
            }
            if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
                JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
            }
        });
    }
}
