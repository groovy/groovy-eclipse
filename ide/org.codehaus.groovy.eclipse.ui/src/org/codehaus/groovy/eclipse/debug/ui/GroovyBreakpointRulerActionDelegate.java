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

import static org.codehaus.jdt.groovy.model.GroovyNature.hasGroovyNature;

import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class GroovyBreakpointRulerActionDelegate extends RulerToggleBreakpointActionDelegate {

    private GroovyBreakpointRulerAction delegate;

    @Override
    protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
        IProject project = getProject(editor.getEditorInput());
        if (project != null && hasGroovyNature(project) && hasGroovySource(editor)) {
            delegate = new GroovyBreakpointRulerAction(rulerInfo, editor, getEditorPart());
            return delegate;
        }
        return super.createAction(editor, rulerInfo);
    }

    @Override
    public void runWithEvent(IAction action, Event event) {
        if (delegate != null) {
            delegate.runWithEvent(event);
        } else {
            super.runWithEvent(action, event);
        }
    }

    @Override
    public void dispose() {
        if (delegate != null) {
            delegate.dispose();
            delegate = null;
        }
        super.dispose();
    }

    //--------------------------------------------------------------------------

    protected IEditorPart getEditorPart() {
        return ReflectionUtils.getPrivateField(RulerToggleBreakpointActionDelegate.class, "fEditor", this);
    }

    protected IProject getProject(IEditorInput editorInput) {
        IFile file = Adapters.adapt(editorInput, IFile.class);
        if (file != null) {
            return file.getProject();
        }

        IClassFile classFile = Adapters.adapt(editorInput, IClassFile.class);
        if (classFile != null) {
            return classFile.getJavaProject().getProject();
        }

        return null;
    }

    protected boolean hasGroovySource(ITextEditor editor) {
        return (Adapters.adapt(editor, ModuleNode.class) != null);
    }
}
