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
package org.codehaus.groovy.eclipse.launchers;

import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Helper methods for Launch Shortcuts to keep things dry.
 * 
 * @author David Kerber
 */
public class LaunchShortcutHelper {
	/**
	 * The dialog title when selecting a class to run
	 */
	public static final String SELECT_CLASS_DIALOG_TITLE = "Select Groovy Class" ;
	/**
	 * The dialog text when selecting a class to run
	 */	
	public static final String SELECT_CLASS_DIALOG_TEXT = "More than one Groovy class in this file can be run.  Please select the class to run." ;
	
	/**
	 * Prompts the user to select a class from the Lists.
	 * 
	 * @param types A List of IType in a given file for the user to pick from.
	 * @return Returns the IType that the user selected.
	 * @throws OperationCanceledException If the user selects cancel.
	 */
	public static IType chooseClassNode(List<IType> types) {
		return chooseFromList(types, new JavaUILabelProvider(), SELECT_CLASS_DIALOG_TITLE, SELECT_CLASS_DIALOG_TEXT);
	}
	
	/**
	 * Prompts user to select from a list of items.
	 * 
	 * @param options The options to pick from.
	 * @param labelProvider The label provider for the objects
	 * @param title The title for the dialog
	 * @param message The message for the dialog
	 * @return Returns the object the user selected.
	 * @throws OperationCanceledException If the user selects cancel
	 */
	@SuppressWarnings("unchecked")
    public static <T> T chooseFromList(List<T> options, ILabelProvider labelProvider, String title, String message) {
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(GroovyPlugin.getActiveWorkbenchShell(), labelProvider);
		dialog.setElements(options.toArray());
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose() ;		
		if (result == Window.OK) {
			return (T) dialog.getFirstResult();
		}
		/*If the user hits cancel this will stop the whole thing silently*/
		throw new OperationCanceledException();
	}
}
