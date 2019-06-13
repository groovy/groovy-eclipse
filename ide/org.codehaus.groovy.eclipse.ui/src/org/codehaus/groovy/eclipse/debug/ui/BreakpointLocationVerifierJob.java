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

import org.codehaus.groovy.antlr.LocationSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * Verifies the position of a breakpoint.
 * <p>
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

    private IEditorPart fEditorPart;

    public BreakpointLocationVerifierJob(IJavaLineBreakpoint breakpoint, int lineNumber, String typeName, IType type, IResource resource, IEditorPart editorPart) {
        super(ActionMessages.BreakpointLocationVerifierJob_breakpoint_location);
        fBreakpoint = breakpoint;
        fLineNumber = lineNumber;
        fTypeName = typeName;
        fType = type;
        fResource = resource;
        fEditorPart = editorPart;
    }

    @Override
    public boolean belongsTo(Object family) {
        return (family == FAMILY);
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        try {
            if (fBreakpoint != null) {
                DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
            }

            ModuleNode module = Adapters.adapt(fEditorPart, ModuleNode.class);
            if (module != null) {
                ASTNode found = new BreakpointLocationFinder(module).findBreakpointLocation(fLineNumber);
                if (found instanceof ClassNode && ((ClassNode) found).getNameEnd() > 0) {
                    createNewClassBreakpoint((ClassNode) found, module.getNodeMetaData(LocationSupport.class));
                } else if (found instanceof FieldNode && ((FieldNode) found).getNameEnd() > 0) {
                    createNewFieldBreakpoint((FieldNode) found, module.getNodeMetaData(LocationSupport.class));
                } else if (found instanceof MethodNode && ((MethodNode) found).getNameEnd() > 0) {
                    createNewMethodBreakpoint((MethodNode) found, module.getNodeMetaData(LocationSupport.class));
                } else if (found != null) {
                    createNewLineBreakpoint(found);
                }
                if (found != null) {
                    return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), ActionMessages.BreakpointLocationVerifierJob_breakpoint_set);
                }
            }
        } catch (CoreException e) {
            JDIDebugUIPlugin.log(new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), "Breakpoint location verification failed", e));
        }

        // cannot find a valid location
        JDIDebugUIPlugin.getStandardDisplay().asyncExec(() -> {
            IEditorStatusLine statusLine = Adapters.adapt(fEditorPart, IEditorStatusLine.class);
            if (statusLine != null) {
                statusLine.setMessage(true, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null);
            }
            if (JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
                Display.getCurrent().beep();
            }
        });
        return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), ActionMessages.BreakpointLocationVerifierJob_not_valid_location);
    }

    private void createNewLineBreakpoint(ASTNode node) throws CoreException {
        // make sure that breakpoint doesn't exist on this line; line may have moved by the validator
        if (JDIDebugModel.lineBreakpointExists(fTypeName, node.getLineNumber()) == null) {
            // TODO: Find surrounding declaration for more accurate attributes
            Map<String, Object> newAttributes = new HashMap<>();
            int start = node.getStart(), end = node.getEnd();
            if (fType != null) {
                BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(newAttributes, fType, start, end);
            }
            JDIDebugModel.createLineBreakpoint(fResource, fTypeName, node.getLineNumber(), start, end, 0, true, newAttributes);
        }
    }

    private void createNewClassBreakpoint(ClassNode node, LocationSupport locator) throws CoreException {
        if (fType != null) {
            int start = node.getNameStart(), end = node.getNameEnd();
            IJavaElement element = fType.getTypeRoot().getElementAt(start);
            if (element != null) {
                int memberType = (!node.isInterface()
                    ? IJavaClassPrepareBreakpoint.TYPE_CLASS
                    : IJavaClassPrepareBreakpoint.TYPE_INTERFACE);
                Map<String, Object> attributes = new HashMap<>();
                BreakpointUtils.addJavaBreakpointAttributes(attributes, element);
                BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, element, start, end);
                JDIDebugModel.createClassPrepareBreakpoint(fResource, node.getName(), memberType, start, end, true, attributes);
            }
        }
    }

    private void createNewFieldBreakpoint(FieldNode node, LocationSupport locator) throws CoreException {
        if (fType != null) {
            int start = node.getNameStart(), end = node.getNameEnd();
            IJavaElement element = fType.getTypeRoot().getElementAt(start);
            if (element != null) {
                int lineNumber = locator.getRowCol(start)[0];
                Map<String, Object> attributes = new HashMap<>();
                BreakpointUtils.addJavaBreakpointAttributes(attributes, element);
                BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, element, start, end);
                JDIDebugModel.createWatchpoint(fResource, node.getDeclaringClass().getName(), node.getName(), lineNumber, start, end, 0, true, attributes);
            }
        }
    }

    private void createNewMethodBreakpoint(MethodNode node, LocationSupport locator) throws CoreException {
        if (fType != null) {
            int start = node.getNameStart(), end = node.getNameEnd();
            IJavaElement element = fType.getTypeRoot().getElementAt(start);
            if (element != null) {
                int lineNumber = locator.getRowCol(start)[0];
                Map<String, Object> attributes = new HashMap<>();
                BreakpointUtils.addJavaBreakpointAttributes(attributes, element);
                BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, element, start, end);
                JDIDebugModel.createMethodBreakpoint(fResource, node.getDeclaringClass().getName(), node.getName(), createMethodSignature(node), true, false, false, lineNumber, start, end, 0, true, attributes);
            }
        }
    }

    //--------------------------------------------------------------------------

    private static String createMethodSignature(MethodNode node) {
        String[] parameterTypes = GroovyUtils.getParameterTypeSignatures(node, true);
        String returnType = GroovyUtils.getTypeSignature(node.getReturnType(), true, true);
        return Signature.createMethodSignature(parameterTypes, returnType).replace('.', '/');
    }
}
