/*******************************************************************************
 * Copyright (c) 2000-2012  IBM Corporation, SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial version
 *              Andrew Eisenberg - convert for use with Groovy
 ******************************************************************************/
package org.codehaus.groovy.eclipse.debug.ui;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class GroovyBreakpointRulerActionDelegate extends RulerToggleBreakpointActionDelegate {

    private IEditorPart fEditorPart;
    private GroovyBreakpointRulerAction groovyDelegate;
    /**
     * @see IEditorActionDelegate#setActiveEditor(bIAction, IEditorPart)
     */
    @Override
    public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
        fEditorPart = targetEditor;
        super.setActiveEditor(callerAction, targetEditor);
    }


    /**
     * @see AbstractRulerActionDelegate#createAction()
     */
    @Override
    protected IAction createAction(ITextEditor editor,
            IVerticalRulerInfo rulerInfo) {
        IResource resource;
        IEditorInput editorInput = editor.getEditorInput();
        if (editorInput instanceof IFileEditorInput) {
            resource = ((IFileEditorInput) editorInput).getFile();
            if (GroovyNature.hasGroovyNature(resource.getProject())) {
                groovyDelegate = new GroovyBreakpointRulerAction(rulerInfo, editor,
                        fEditorPart);
                return groovyDelegate;
            }
        }
        //	else: use jdt's action
        return super.createAction(editor, rulerInfo);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(IAction action, Event event) {
        if(groovyDelegate != null) {
            groovyDelegate.runWithEvent(event);
        } else {
            super.runWithEvent(action, event);
        }
    }

    @Override
    public void dispose() {
        groovyDelegate = null;
        super.dispose();
    }
}
