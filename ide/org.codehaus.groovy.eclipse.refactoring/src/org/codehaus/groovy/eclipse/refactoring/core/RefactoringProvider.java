/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.StatusHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;


/**
 * Info Object to provide the selection, editor and root node of the file to the Refactoring Class
 * The Refactoring wizard, can also access to this info object to exchange information with
 * the currently running Refactoring
 * @author Michael Klenk
 *
 */
abstract public class RefactoringProvider {

	protected UserSelection selection;
	protected Map<Object,TextChange> javaChanges;
	
	public RefactoringProvider() {}
	
	public RefactoringProvider(UserSelection selecion) {
		this.selection = selecion;
	}

	public UserSelection getSelection() {
		return selection;
	}

	public void setSelection(UserSelection selection) {
		this.selection = selection;
	}
	
	public abstract void addInitialConditionsCheckStatus(RefactoringStatus status);
	
	protected void addStatusEntries(RefactoringStatus status, IStatus stateDuplicateName) {
		for (RefactoringStatusEntry refState : StatusHelper.convertStatus(stateDuplicateName).getEntries()){
			status.addEntry(refState);
		}
	}
	
	public abstract RefactoringStatus checkFinalConditions(
			IProgressMonitor pm) throws CoreException,
			OperationCanceledException;

	public abstract RefactoringStatus checkInitialConditions(
			IProgressMonitor pm) throws CoreException,
			OperationCanceledException;

	public abstract GroovyChange createGroovyChange(IProgressMonitor pm)
			throws CoreException, OperationCanceledException;
	
	public abstract IGroovyDocumentProvider getDocumentProvider();

	public void setJavaChanges(Map<Object, TextChange> changes) {
		javaChanges = changes;
	}

	public Map<Object, TextChange> getJavaChanges() {
		if(javaChanges != null)
			return javaChanges;
		else
			return new HashMap<Object, TextChange>();
	}

	protected MultiTextEdit removeDuplicatedTextedits(RenameTextEditProvider textEditProvider) {
		ArrayList<TextEdit> doubleEdits = new ArrayList<TextEdit>();
		MultiTextEdit multi = textEditProvider.getMultiTextEdit();
		IFile thisFile = textEditProvider.getDocProvider().getFile();
	
		TextEdit javaChange = getJavaTextEdit(thisFile);
		for (TextEdit edit : multi.getChildren()) {
			for(TextEdit jedit : javaChange.getChildren()) {
				if(edit.getRegion().equals(jedit.getRegion())) {
					doubleEdits.add(edit);
				}
			}
		}
	
		for (TextEdit editToRemove : doubleEdits) {
			multi.removeChild(editToRemove);
		}
		return multi;
	}

	private TextEdit getJavaTextEdit(IFile thisFile) {
		TextEdit javaChange = new DeleteEdit(0, 0);
		if (javaChanges != null) {
			for (TextChange javaEdit : javaChanges.values()) {
				if (javaEdit instanceof TextFileChange) {
					TextFileChange tfc = (TextFileChange) javaEdit;
					IFile remoteRile = tfc.getFile();
					if (remoteRile.equals(thisFile)) {
						javaChange = tfc.getEdit();
					}
				}
			}
		}
		return javaChange;
	}
	
}
