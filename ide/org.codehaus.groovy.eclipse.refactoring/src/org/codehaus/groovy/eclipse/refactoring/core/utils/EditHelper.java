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
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * HelperClass that provides various ReplaceEdits for the
 * Rename Refactorings
 */
public class EditHelper {

	private EditHelper() {
	}

	public static ReplaceEdit getDefaultReplaceEdit(ASTNode node, boolean execute, IDocument document,
			String oldClassName, String newClassName) {

		TextSelection sel = new TextSelection(document, node.getStart(), node.getEnd() - node.getStart());
		int offset = sel.getOffset();
		if (execute) {
			return new ReplaceEdit(offset, oldClassName.length(), newClassName);
		}
		return new ReplaceEdit(offset, oldClassName.length(), oldClassName);
	}

	public static ReplaceEdit getLookupReplaceEdit(ASTNode node, boolean execute, IDocument document,
			String oldName, String newName) {
		TextSelection sel = new TextSelection(document, node.getStart(), node.getEnd() - node.getStart());
		int offset = sel.getOffset();
		if (execute) {
		    // try the nameStart and nameEnd fields first
		    if (node instanceof AnnotatedNode && ((AnnotatedNode) node).getNameEnd() > 0) {
		        AnnotatedNode aNode = (AnnotatedNode) node;
		        return new ReplaceEdit(aNode.getNameStart(), aNode.getNameEnd()-aNode.getNameStart()+1, newName);
		    }
		    
		    
			// There is no node that starts exactly at the class's name position
			// (modifiers/annotation
			// are included) find the startposition by looking in the document
			try {
				FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
				IRegion foundRegion = finder.find(offset, oldName, true, true, true, false);
				IRegion lineInfoAtOffset = document.getLineInformationOfOffset(foundRegion.getOffset());

				// read the part of the line in which the replace will take
				// place
				String definitionLine = document.get(lineInfoAtOffset.getOffset(), lineInfoAtOffset.getLength());
				offset = lineInfoAtOffset.getOffset() + definitionLine.indexOf(oldName);
				// If there are multiple occurrences of the old name, relay on the node information.
				if(definitionLine.indexOf(oldName) != definitionLine.lastIndexOf(oldName)) {
					offset = lineInfoAtOffset.getOffset() + node.getColumnNumber() -1 ;
				}
				return new ReplaceEdit(offset, oldName.length(), newName);
			} catch (BadLocationException e) {
				throw new RuntimeException(e);
			}
		}
		return new ReplaceEdit(offset, oldName.length(), oldName);
	}

	public static ReplaceEdit getRenameMethodCallEdit(MethodCallExpression node, IDocument document,
			String newName) {
		TextSelection sel = new TextSelection(document, node.getStart(), node.getEnd() - node.getStart());
		int offset = sel.getOffset();
		offset += node.getMethod().getColumnNumber() - node.getColumnNumber();
		return new ReplaceEdit(offset, node.getMethod().getText().length(), newName);
	}

}