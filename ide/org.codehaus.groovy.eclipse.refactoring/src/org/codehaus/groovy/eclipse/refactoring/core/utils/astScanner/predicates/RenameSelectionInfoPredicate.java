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
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.StatementFinder;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.eclipse.jface.text.IDocument;

/**
 * 
 * @author martin
 *
 */
public class RenameSelectionInfoPredicate implements IASTNodePredicate {

	private final UserSelection selection;
	private final IDocument doc;
	
	public RenameSelectionInfoPredicate(UserSelection selection, IDocument document){
		this.selection = selection;
		this.doc = document;
	}
	
	public ASTNode evaluate(ASTNode input) {
		SourceCodePoint scp = new SourceCodePoint(input, SourceCodePoint.END);
		if (scp.isInvalid()) {
			input.setLastLineNumber(input.getLineNumber());
			input.setLastColumnNumber(input.getColumnNumber() + input.getText().length());
		}
		if (StatementFinder.testSelection(selection, input, doc, false)) {
			return input;
		}
        return null;
	}
}
