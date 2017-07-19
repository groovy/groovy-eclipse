/*
 * Copyright 2009-2017 the original author or authors.
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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * Job used to verify the position of a breakpoint
 * Somewhat based on org.eclipse.jdt.internal.debug.ui.actions.BreakpointLocationVerifierJob
 */
public class BreakpointLocationVerifierJob extends Job {

    public static final Object FAMILY = new Object();

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

    @SuppressWarnings("cast")
    public BreakpointLocationVerifierJob(IJavaLineBreakpoint breakpoint, int lineNumber, String typeName, IType type, IResource resource, IEditorPart editorPart) {
        super(ActionMessages.BreakpointLocationVerifierJob_breakpoint_location);
        fBreakpoint = breakpoint;
        fLineNumber = lineNumber;
        fTypeName = typeName;
        fType = type;
        fResource = resource;
        fStatusLine = (IEditorStatusLine) editorPart.getAdapter(IEditorStatusLine.class);
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        try {
            @SuppressWarnings("cast")
            ModuleNode node = (ModuleNode) ((IFile) fResource).getAdapter(ModuleNode.class);
            if (node == null) {
                return new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), ActionMessages.BreakpointLocationVerifierJob_not_valid_location);
            }
            if (fBreakpoint != null) {
                DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
            }
            ValidBreakpointLocationFinder finder = new ValidBreakpointLocationFinder(fLineNumber);
            ASTNode valid = finder.findValidBreakpointLocation(node);
            if (valid instanceof MethodNode && ((MethodNode) valid).getNameEnd() > 0) {
                createNewMethodBreakpoint((MethodNode) valid, fTypeName);
                return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), ActionMessages.BreakpointLocationVerifierJob_breakpoint_set);
            } else if (valid != null) {
                createNewLineBreakpoint(valid, fTypeName);
                return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), ActionMessages.BreakpointLocationVerifierJob_breakpoint_set);
            }
        } catch (CoreException e) {
            JDIDebugUIPlugin.log(new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), "Breakpoint location verification failed", e));
        }

        // cannot find a valid location
        report(ActionMessages.BreakpointLocationVerifierJob_not_valid_location);
        return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), ActionMessages.BreakpointLocationVerifierJob_not_valid_location);
    }

    private void createNewMethodBreakpoint(MethodNode node, String typeName) throws CoreException {
        Map<String, Object> newAttributes = new HashMap<String, Object>(10);
        int start = node.getNameStart();
        int end = node.getNameEnd();
        if (fType != null) {
            IJavaElement elt = fType.getTypeRoot().getElementAt(start);
            if (elt != null) {
                IMethod method = (IMethod) elt;
                BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(newAttributes, fType, start, end);
                BreakpointUtils.addJavaBreakpointAttributes(newAttributes, method);
                JDIDebugModel.createMethodBreakpoint(fResource, typeName, node.getName(), createMethodSignature(node), true, false, false, node.getLineNumber(), start, end, 0, true, newAttributes);
            }
        }
    }

    private String createMethodSignature(MethodNode node) {
        String returnType = createTypeSignatureStr(node.getReturnType());
        String[] parameterTypes = new String[node.getParameters().length];
        for (int i = 0, n = parameterTypes.length; i < n; i += 1) {
            parameterTypes[i] = createTypeSignatureStr(node.getParameters()[i].getType());
        }
        return Signature.createMethodSignature(parameterTypes, returnType).replace('.', '/');
    }

    private String createTypeSignatureStr(ClassNode node) {
        if (node == null) {
            node = VariableScope.OBJECT_CLASS_NODE;
        }
        String name = node.getName();
        if (name.startsWith("[")) {
            return name;
        } else {
            return Signature.createTypeSignature(name, true);
        }
    }

    /**
     * Create a new breakpoint at the right position.
     */
    private void createNewLineBreakpoint(ASTNode node, String typeName) throws CoreException {
        // check to make sure that breakpoint doesn't exist on this line
        // line may have moved by the validator
        if (JDIDebugModel.lineBreakpointExists(typeName, node.getLineNumber()) != null) {
            return;
        }
        Map<String, Object> newAttributes = new HashMap<String, Object>(10);
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
