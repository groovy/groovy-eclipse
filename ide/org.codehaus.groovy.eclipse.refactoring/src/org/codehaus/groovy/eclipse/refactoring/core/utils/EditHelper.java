/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.RenameLocalHelper;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.VariableProxy;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringImportNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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

		UserSelection sel = new UserSelection(node, document);
		int offset = sel.getOffset();
		if (execute) {
			return new ReplaceEdit(offset, oldClassName.length(), newClassName);
		}
		return new ReplaceEdit(offset, oldClassName.length(), oldClassName);
	}

	public static ReplaceEdit getExactReplaceEdit(RefactoringImportNode importNode, IDocument document) {
		UserSelection sel = new UserSelection(importNode, document);
		int offset = sel.getOffset();
		int length = sel.getLength();
		return new ReplaceEdit(offset, length, importNode.getText());
	}

	public static ReplaceEdit getLookupReplaceEdit(ASTNode node, boolean execute, IDocument document,
			String oldName, String newName) {
		UserSelection sel = new UserSelection(node, document);
		int offset = sel.getOffset();
		if (execute) {
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

	public static ReplaceEdit getRenameMethodCallEdit(MethodCallExpression methodCall, IDocument document,
			String newName) {
		UserSelection sel = new UserSelection(methodCall, document);
		int offset = sel.getOffset();
		offset += methodCall.getMethod().getColumnNumber() - methodCall.getColumnNumber();
		return new ReplaceEdit(offset, methodCall.getMethod().getText().length(), newName);
	}

	public static ReplaceEdit getVariableProxyReplaceEdit(VariableProxy variable, IDocument document, String newName) {
		int offset = RenameLocalHelper.getVariableProxySpecificOffset(variable, document);
		return new ReplaceEdit(offset, variable.getName().length(), newName);
	}
	
}