/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ClassImport;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;

public class ImportedClassesAndAliasCollector extends RefactoringCodeVisitorSupport {

	private List<String> alreadyImportedClasses = new ArrayList<String>();
	private final String newAlias;

	public ImportedClassesAndAliasCollector(ModuleNode rootNode, String newAlias) {
		super(rootNode);
		this.newAlias = newAlias;
	}
	
	@Override
    public void visitClassImport(ClassImport classImport) {
		super.visitClassImport(classImport);
		//getAlias() is either a real alias or the name of the imported class without package
		//in both case, if it is equal to the newAlias, they will get overridden
		if (classImport.getAlias().equals(newAlias)){
			alreadyImportedClasses .add(classImport.getAlias());
		}
	}

	public List<String> getAlreadyImportedClasses() {
		return alreadyImportedClasses;
	}
	
}
