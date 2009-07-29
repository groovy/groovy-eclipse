/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package ASTWriter;

import java.io.File;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.rewriter.ASTWriter;

import tests.BaseTestCase;
import core.ASTProvider;

public class ASTWriterTestCase extends BaseTestCase {
		
	public ASTWriterTestCase(String name, File fileToTest) {
		super(name, fileToTest);
		// Set Method to call for JUnit
		setName("testASTWriter");
	}
	
	public void testASTWriter() {
			
		String modifier = properties.get("modifier");
		String startOffsetProperty = properties.get("startOffset");
		int startOffset;
		if (startOffsetProperty != null) {
			startOffset = Integer.parseInt(startOffsetProperty);
		} else {
			startOffset = 0;
		}
		ModuleNode root = ASTProvider.getAST(getDocumentProvider().getDocumentContent(),file.getAbsolutePath());
		ASTWriter rewriter = new ASTWriter(root,getDocumentProvider().getDocument());
		if (modifier != null) {
			rewriter.setModifierToUse(modifier);
		}
		rewriter.setStartOffset(startOffset);
		rewriter.visitRoot();
		String result = rewriter.getGroovyCode(); 
		String expected = getExpected().get();
		
		assertEquals(expected, result);
	}
}
