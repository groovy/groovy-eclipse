/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

public class MultiLineStringPartitionPairStrategy extends
		AbstractPairInPartitionStrategy {

	public boolean isActive() {
		return true;
	}

	public void doInsert(IDocument document, DocumentCommand command) {
	}

	public void doRemove(IDocument document, DocumentCommand command) {
		IPreferenceStore preferenceStore = JavaPlugin.getDefault().getPreferenceStore();
		if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS) == false) {
			return;
		}

		String pair;
		try {
			pair = document.get(command.offset - 2, 6);
			if (pair.equals("\"\"\"\"\"\"") || pair.equals("''''''")) {
				command.offset = command.offset - 2;
				command.length = 6;
				command.text = "";
			}
		} catch (BadLocationException e) {
		}
	}
}
