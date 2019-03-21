/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.debug.ui.actions.RunToLineHandler;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class RunToLineAdapter implements IRunToLineTarget {

    @Override
    public boolean canRunToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
        if (target instanceof IDebugElement && target.canResume()) {
            IDebugTarget debugTarget = ((IDebugElement) target).getDebugTarget();
            IJavaDebugTarget adapter = debugTarget.getAdapter(IJavaDebugTarget.class);
            return (adapter != null);
        }
        return false;
    }

    @Override
    public void runToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException {
        ModuleNode moduleNode = Adapters.adapt(getTextEditor(part), ModuleNode.class);
        int lineNumber = ((ITextSelection) selection).getStartLine() + 1;
        if (moduleNode != null && lineNumber > 0) {
            ASTNode found = new BreakpointLocationFinder(moduleNode).findBreakpointLocation(lineNumber);
            if (found != null && found.getLineNumber() == lineNumber) {
                Map<String, Object> attributes = new HashMap<>(4);
                BreakpointUtils.addRunToLineAttributes(attributes);
                String typeName = moduleNode.getClasses().get(0).getName(); // TODO: How precise does this need to be?
                IBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), typeName, lineNumber, -1, -1, 1, false, attributes);

                IDebugTarget debugTarget = ((IDebugElement) target).getDebugTarget();
                if (debugTarget != null) {
                    RunToLineHandler handler = new RunToLineHandler(debugTarget, target, breakpoint);
                    handler.run(new NullProgressMonitor());
                    return;
                }
            } else {
                String errorMessage;
                if (((ITextSelection) selection).getLength() > 0) {
                    errorMessage = "Selected line is not a valid location to run to";
                } else {
                    errorMessage = "Cursor position is not a valid location to run to";
                }
                throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, errorMessage, null));
            }
        }
    }

    protected ITextEditor getTextEditor(IWorkbenchPart part) {
        if (part instanceof ITextEditor) {
            return (ITextEditor) part;
        }
        return part.getAdapter(ITextEditor.class);
    }
}
