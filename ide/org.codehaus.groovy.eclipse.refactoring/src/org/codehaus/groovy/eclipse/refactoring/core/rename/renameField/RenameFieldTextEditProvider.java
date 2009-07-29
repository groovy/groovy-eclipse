/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameField;

import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEdit;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * Provides all textedits of a single file
 * 
 * @author reto kleeb
 * 
 */
public class RenameFieldTextEditProvider extends RenameTextEditProvider {

	private final FieldPattern patternOfField;
	private final List<ASTNode> listOfNodesToRename;

	public RenameFieldTextEditProvider(String newFieldName, String oldName,
			IGroovyDocumentProvider docprovider, FieldPattern pattern,
			List<ASTNode> listOfNodesToRename) {
		super(oldName, docprovider);
		this.patternOfField = pattern;
		this.newName = newFieldName;
		this.listOfNodesToRename = listOfNodesToRename;
	}

	@Override
    public MultiTextEdit getMultiTextEdit() {
		RenameTextEdit textEdit = new RenameFieldTextEdit(docProvider, listOfNodesToRename,
				patternOfField.getName(), newName);
		textEdit.scanAST();
		return textEdit.getEdits();
	}

}
