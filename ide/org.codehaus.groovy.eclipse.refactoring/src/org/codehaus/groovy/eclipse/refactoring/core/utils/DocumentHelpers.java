/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import java.util.Map;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.RenameLocalTextEdit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;


/** 
 * Various Helper Methods for the handling of Document Object
 * @author reto kleeb
 */
public class DocumentHelpers {
	
	public static Document applyRenameEditsToDocument(String methodName, Map<String,String> renameMap, Document doc) {
		
		ModuleNode root = ASTTools.getASTNodeFromSource(doc.get());
		
		//get the edits with the scanner from rename local. Variable to rename are in the renameMap
		RenameLocalTextEdit scanner = new RenameLocalTextEdit(root, doc, methodName, renameMap);
		scanner.scanAST();
		MultiTextEdit renameEdits = scanner.getEdits();
		
		try {
			renameEdits.apply(doc);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return doc;
	}
}
