/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod;

import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEdit;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * Provides all textedits of a single file
 * @author reto kleeb
 * 
 */
public class RenameMethodTextEditProvider extends RenameTextEditProvider {

	private final MethodPattern selectedMethodPattern;
	private final List<ASTNode> listOfNodesToRename;

	public RenameMethodTextEditProvider(MethodPattern selectedMethodPattern, String newMethodName,
			IGroovyDocumentProvider docprovider, List<ASTNode> list) {

		super(selectedMethodPattern.getMethodName(), docprovider);
		this.newName = newMethodName;
		this.selectedMethodPattern = selectedMethodPattern;
		this.listOfNodesToRename = list;
	}

	@Override
    public MultiTextEdit getMultiTextEdit() {
		RenameTextEdit textEdit = new RenameMethodTextEdit(docProvider, listOfNodesToRename, newName,
				selectedMethodPattern.getMethodName());
		textEdit.scanAST();
		return textEdit.getEdits();
	}

}
