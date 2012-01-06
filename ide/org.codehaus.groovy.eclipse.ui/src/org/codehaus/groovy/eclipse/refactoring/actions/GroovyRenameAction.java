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

package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.actions.RenameAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author Andrew Eisenberg
 * @created Aug 26, 2009
 * Replaces JDT's rename action with Groovy's
 */
public class GroovyRenameAction extends RenameAction {

    private RenameDispatcherAction renameDelegate;
    private final GroovyEditor editor;

    public GroovyRenameAction(final JavaEditor editor) {
        super(editor);
        if (editor instanceof GroovyEditor) {
            this.editor = (GroovyEditor) editor;
        } else {
            this.editor = null;
        }
        renameDelegate = new RenameDispatcherAction();
    }

    @Override
    public void run(IStructuredSelection selection) {
        // do nothing...not applicable here
    }

    @Override
    public void run(ITextSelection selection) {
        if (editor != null) {
            renameDelegate.setActiveEditor(this, editor);
            renameDelegate.selectionChanged(this, selection);
            renameDelegate.run(this);
        }
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        renameDelegate.selectionChanged(this, selection);
    }

    @Override
    public void selectionChanged(ITextSelection selection) {
        renameDelegate.selectionChanged(this, selection);
    }
}
