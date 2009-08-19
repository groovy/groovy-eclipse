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
package org.codehaus.groovy.eclipse.editor.actions;


import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * The base of all GroovyEclipse editing actions.
 * 
 * The term <i>editing action</i> is used broadly - any task to do with a
 * programmer editing source code is included. So changes to the document,
 * navigation, etc.
 * 
 * For convenience is has a doRun() method that catches and logs any
 * BadLocationException. it is simply called by IEditorActionDelegate#run which
 * can be implemented as normal if so desired. One of these methods must be
 * implemented to implement the editing action.
 * 
 * @author emp
 */
public abstract class EditingAction implements IEditorActionDelegate {
	protected ISelection selection;
	protected IEditorPart editor;
	public EditingAction() {
	}
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editor = targetEditor;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/**
	 * Override this if you want to implement the IEditorActionDelegate#run
	 * method yourself.
	 */
	public void run(IAction action) {
		try {
			doAction(action);
		} catch (BadLocationException e) {
			GroovyPlugin.getDefault().logException("Editing action error.", e);
		}
	}

	/**
	 * Override this if you don't want to deal with BadLocationException
	 * logging.
	 * 
	 * @param action
	 * @throws BadLocationException
	 */
	public void doAction(IAction action) throws BadLocationException {
	}
}