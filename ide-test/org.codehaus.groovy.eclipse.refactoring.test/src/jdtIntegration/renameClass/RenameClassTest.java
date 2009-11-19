/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration.renameClass;

import jdtIntegration.ProgrammaticalRenameTest;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;

import core.FilePathHelper;

/**
 * @author Stefan Reinhard
 */
public class RenameClassTest extends ProgrammaticalRenameTest {

	
	static String fileName = FilePathHelper.getPathToTestFiles()
	+ "jdtIntegration/startedFromJava/renameClass/ClassRename.txt";

	public RenameClassTest() {
		super(fileName);
	}
	
	public void testRenameClass() {	
		ClassNode n = ClassHelper.make("tests.TestCodeFiles.jdtIntegration.FieldClass");		
		RenameClassProvider provider = new RenameClassProvider(fileProvider, n);
		provider.setNewName("Test");
		checkRefactoring(provider);		
	}
	

	
}
