/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration.renameField;

import jdtIntegration.ProgrammaticalRenameTest;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldCandidateCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;

import core.FilePathHelper;

/**
 * @author Stefan Reinhard
 */

// TODO: Should be generalized
public class RenameFieldTest extends ProgrammaticalRenameTest {

	static String fileName = FilePathHelper.getPathToTestFiles()
	+ "jdtIntegration/startedFromJava/renameField/FieldRename.txt";
	
	public RenameFieldTest() {
		super(fileName);
	}

	public void testASTCreated() {
		assertNotNull(node);
	}
	
	public void testCandidatesFound() {
		FieldPattern pattern = createFieldPattern("a");
		RenameFieldProvider r = new RenameFieldProvider(fileProvider,pattern);
		assertTrue(r.hasCandidates());	
	}

	public void testCandidatesNotFound() {
		FieldPattern pattern = createFieldPattern("z");
		RenameFieldProvider r = new RenameFieldProvider(fileProvider,pattern);
		assertFalse(r.hasCandidates());	
	}

	public void testFieldCandidateCollector() {
		FieldPattern pattern = createFieldPattern("a");
		RenameFieldCandidateCollector collector =
			new RenameFieldCandidateCollector(node, pattern);
		collector.scanAST();
		assertTrue(collector.getCandidates().size()>0);
	}
	
	public void testRefactoring() {
		FieldPattern pattern = createFieldPattern("a");
		RenameFieldProvider provider = new RenameFieldProvider(fileProvider,pattern);
		provider.setNewName("b");
		checkRefactoring(provider);
	}
	
	private FieldPattern createFieldPattern(String name) {
		ClassNode node = ClassHelper.makeWithoutCaching("tests.TestCodeFiles.jdtIntegration.FieldClass");
		return new FieldPattern(node, name);
	}
}
