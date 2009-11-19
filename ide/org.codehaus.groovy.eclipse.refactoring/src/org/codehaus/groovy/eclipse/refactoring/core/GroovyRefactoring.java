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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.eclipse.refactoring.core.participation.ParticipantDispatcher;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiEditorInput;

/**
 * Base Class of all Refactorings of Groovy-Eclipse
 * @author Michael Klenk mklenk@hsr.ch
 */
public abstract class GroovyRefactoring extends Refactoring {

	protected RefactoringInfo refactoringInfo;
	protected List<IWizardPage> pages;
	private ParticipantDispatcher dispatcher;
	private String name;

	public GroovyRefactoring(RefactoringInfo info) {
		this();
		this.refactoringInfo = info;
		dispatcher = new ParticipantDispatcher(info.getProvider());
	}

	public GroovyRefactoring() {
		pages = new ArrayList<IWizardPage>();
	}
	
	public RefactoringInfo getInfo() {
		return this.refactoringInfo;
	}

	@Override
    public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<IWizardPage> getPages() {
		return this.pages;
	}

	@Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = refactoringInfo.checkFinalConditions(pm);	
		status.merge(dispatcher.checkFinalConditions(pm));
		return status;
	}

	@Override
    public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		CompositeChange groovyChange = refactoringInfo.createGroovyChange(pm).createChange();
		groovyChange.addAll(dispatcher.createChanges(pm));
		return groovyChange;
	}

	@Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) {
		try {
			RefactoringStatus status = refactoringInfo.checkInitialConditions(pm);
			status.merge(dispatcher.checkInitialConditions(pm));
			return status;
			
		} catch (OperationCanceledException e) {
			return RefactoringStatus.createErrorStatus(e.getMessage());
		} catch (CoreException e) {
			return RefactoringStatus.createErrorStatus(e.getMessage());
		}
	}
	
	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is 
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * @param skipNonResourceEditors if <code>true</code>, editors whose inputs do not adapt to {@link IResource}
	 * are not saved
	 * 
	 * @return an array of dirty editor parts
	 * @since 3.4
	 */
	public static IEditorPart[] getDirtyEditors(boolean skipNonResourceEditors) {
		Set<IEditorInput> inputs= new HashSet<IEditorInput>();
		List<IEditorPart> result= new ArrayList<IEditorPart>(0);
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorPart[] editors= pages[x].getDirtyEditors();
				for (int z= 0; z < editors.length; z++) {
					IEditorPart ep= editors[z];
					IEditorInput input= ep.getEditorInput();
					if (inputs.add(input)) {
						if (!skipNonResourceEditors || isResourceEditorInput(input)) {
							result.add(ep);
						}
					}
				}
			}
		}
		return result.toArray(new IEditorPart[result.size()]);
	}
	private static boolean isResourceEditorInput(IEditorInput input) {
		if (input instanceof MultiEditorInput) {
			IEditorInput[] inputs= ((MultiEditorInput) input).getInput();
			for (int i= 0; i < inputs.length; i++) {
				if (inputs[i].getAdapter(IResource.class) != null) {
					return true;
				}
			}
		} else if (input.getAdapter(IResource.class) != null) {
			return true;
		}
		return false;
	}
}
