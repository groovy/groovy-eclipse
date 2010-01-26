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

/**
 * Strategy to deal with possible pairs in the character (ie. normal code)
 * partition.
 * 
 * @author emp
 */
public class CharacterPartitionPairStrategy extends
		AbstractPairInPartitionStrategy {
	public boolean isActive() {
		return true;
	}

	public void doInsert(IDocument document, DocumentCommand command) {
		if (!completeTripleQuotes(document, command)) {
			if (!completeSingle(document, command)) {
				adjustForRedundantPress(document, command);
			}
		} else {
			adjustForRedundantPress(document, command);
		}
	}

	private boolean completeTripleQuotes(IDocument document,
			DocumentCommand command) {
		// Complete triple quotes.
		if (command.offset > 2
				&& (command.text.equals("'") || command.text.equals("\""))) {
			IPreferenceStore preferenceStore = JavaPlugin.getDefault().getPreferenceStore();
			boolean autoClose = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS);
			
			String replace = null;
			String triple;
			try {
				triple = document.get(command.offset - 3, 3);
				if (triple.equals("'''"))
					replace = "'''";
				else if (triple.equals("\"\"\""))
					replace = "\"\"\"";
				if (autoClose && replace != null) {
					command.shiftsCaret = false;
					command.text = replace;
					command.caretOffset = command.offset;
					return true;
				}
			} catch (BadLocationException e) {
			}
		}

		return false;
	}

	private boolean completeSingle(IDocument document, DocumentCommand command) {
		String replace = null;
		
		IPreferenceStore preferenceStore = JavaPlugin.getDefault().getPreferenceStore();

		if (command.text.equals("'")) {
			if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS) == false) {
				return false;
			}
			replace = "''";
		} else if (command.text.equals("\"")) {
			if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS) == false) {
				return false;
			}
			replace = "\"\"";
		} else if (command.text.equals("[")) {
			if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACKETS) == false) {
				return false;
			}
			replace = "[]";
		} else if (command.text.equals("(")) {
			if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACKETS) == false) {
				return false;
			}
			replace = "()";
		}

		if (replace != null) {
			command.shiftsCaret = false;
			command.text = replace;
			command.caretOffset = command.offset + 1;
			return true;
		}
		return false;
	}

	/**
	 * When tab, ), etc. is pressed, and the press is redundant because the
	 * closing character has been adden automatically, the cursor should jump to
	 * the end of the completion.
	 * 
	 * @param document
	 * @param command
	 */
	private void adjustForRedundantPress(IDocument document,
			DocumentCommand command) {
		String replace = null;

		if (matchesOne(command.text, new String[] { "\t", "]", ")" })) {
			try {
				if (command.offset > 0) {
					String pair = document.get(command.offset, 1);
					if (command.text.equals(pair)) {
						// The comments below are too restrictive. I the long
						// run, this needs a good look at the Java editor does
						// it, it it really matters at all.
						// String pair = document.get(command.offset - 1, 2);
						// if (in(pair, new String[] { "[]", "()" })) {
						command.shiftsCaret = true;
						replace = "";
					}
				}
			} catch (BadLocationException e) {
			}
		}
		if (replace != null) {
			command.text = replace;
			command.caretOffset = command.offset + 1;
		}
	}

	public void doRemove(IDocument document, DocumentCommand command) {
		IPreferenceStore preferenceStore = JavaPlugin.getDefault().getPreferenceStore();
		if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS) == false) {
			return;
		}

		String pair;
		try {
			pair = document.get(command.offset, 2);
			if (pair.equals("\"\"") || pair.equals("''") || pair.equals("[]")
					|| pair.equals("()")) {
				command.length = 2;
				command.text = "";
				command.caretOffset = command.offset;
			}
		} catch (BadLocationException e) {
		}
	}
}
