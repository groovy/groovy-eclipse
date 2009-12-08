/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceDocumentProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * 
 * @author martin
 *
 */

public class GroovyChange {
	
	private Map<IGroovyDocumentProvider, MultiTextEdit> edits = new HashMap<IGroovyDocumentProvider, MultiTextEdit>();
	private String name;
	
	public GroovyChange(String name) {
		this.name = name;
	}
	
	/**
	 * Creates the whole change from the map. This method can only be called from the Eclipse and not from
	 * the testenvironment
	 * @return Changeobject that contains the edits
	 */
	public CompositeChange createChange() {
		CompositeChange change = new CompositeChange(name);
		for (IGroovyDocumentProvider docProvider : edits.keySet()) {
			MultiTextEdit multiEdit = edits.get(docProvider);
			if (multiEdit.hasChildren()) {
				TextFileChange textFileChange = new TextFileChange(docProvider.getName(),docProvider.getFile());
				textFileChange.setEdit(multiEdit);
				change.add(textFileChange);
			}
		}
		return change;
	}
	
	public void addEdit(IGroovyDocumentProvider docProvider, MultiTextEdit edit) {
		edits.put(docProvider, edit);
	}
	
	public void performChanges() throws MalformedTreeException, BadLocationException {
		for (IGroovyDocumentProvider docProvider : edits.keySet()) {
			MultiTextEdit multiEdit = edits.get(docProvider);
			multiEdit.apply(docProvider.getDocument());
		}
	}

	public void addEdit(IMultiEditProvider textEdit) {
		edits.put(textEdit.getDocProvider(),textEdit.getMultiTextEdit());
	}

}
