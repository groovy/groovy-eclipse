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
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

public class SingleLineStringPartitionPairStrategy extends
		AbstractPairInPartitionStrategy {
	public boolean isActive() {
		return true;
	}

	public void doInsert(IDocument document, DocumentCommand command) {
		IPreferenceStore preferenceStore = JavaPlugin.getDefault().getPreferenceStore();
		if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS) == false) {
			return;
		}
		
		// Complete multi line comment pair.
		String replace = null;
		if (command.offset > 0) {
			try {
				String text = document.get(command.offset - 1, 2);
				if (text.equals("\"\"") == false && text.equals("''") == false)
					return;
			} catch (BadLocationException e) {
			}
		}

		if (command.text.equals("'")) {
			replace = "''''";
		} else if (command.text.equals("\"")) {
			replace = "\"\"\"\"";
		}
		if (replace != null) {
			command.caretOffset = command.offset + 2;
			command.shiftsCaret = false;
		}
	}

	public void doRemove(IDocument document, DocumentCommand command) {

	}
}
