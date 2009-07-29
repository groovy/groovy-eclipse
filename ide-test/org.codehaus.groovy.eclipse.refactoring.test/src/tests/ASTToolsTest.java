/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package tests;

import core.TestDocumentProvider;
import java.util.List;
import junit.framework.TestCase;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/**
 * @author mklenk
 *
 */
public class ASTToolsTest extends TestCase {

	/**
	 * Test method for {@link org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools#getPositionOfBlockStatements(org.codehaus.groovy.ast.stmt.BlockStatement, org.eclipse.jface.text.IDocument)}.
	 */
	public void testGetPositionOfBlockStatements() {
		final String testCode="class Test {\n\tdef run() {\n\t\tprintln(5)\n\t}\n}";
		
		UserSelection sel = null;
		final TestDocumentProvider provider = new TestDocumentProvider(new Document(testCode),"");
		for (final ClassNode cl : (List<ClassNode>) provider.getRootNode().getClasses()) {
			for (final MethodNode method : (List<MethodNode>) cl.getMethods()) {
				if(method.getName().equals("run")) {
					sel = ASTTools.getPositionOfBlockStatements((BlockStatement)method.getCode(), provider.getDocument());					
				}
			}
		}
		assertNotNull( sel );
		assertEquals(sel.getOffset(), 28);
		assertEquals(sel.getLength(), 10);
	}

	/**
	 * Test method for {@link org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools#includeLeedingGap(org.codehaus.groovy.ast.ASTNode, org.eclipse.jface.text.IDocument)}.
	 */
	public void testIncludeLeedingGap() {
		final String testCode="class Test {\n\tdef run() {\n\t\tprintln(5)\n\t}\n}";
		
		UserSelection sel = null;
		final TestDocumentProvider provider = new TestDocumentProvider(new Document(testCode),"");
		for (final ClassNode cl : (List<ClassNode>) provider.getRootNode().getClasses()) {
			for (final MethodNode method : (List<MethodNode>) cl.getMethods()) {
				if(method.getName().equals("run")) {
					final UserSelection blockSel = ASTTools.getPositionOfBlockStatements((BlockStatement)method.getCode(), provider.getDocument());
					final BlockStatement block = (BlockStatement)method.getCode();
					block.setSourcePosition(blockSel.getASTNode(provider.getDocument()));
					sel = ASTTools.includeLeedingGap(block,provider.getDocument());					
				}
			}
		}
		assertEquals(26, sel.getOffset());
		assertEquals(12, sel.getLength());
	}

	/**
	 * Test method for {@link org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools#hasValidPosition(org.codehaus.groovy.ast.ASTNode)}.
	 */
	public void testHasValidPosition() {
		final ASTNode node = new ASTNode();
		assertFalse(ASTTools.hasValidPosition(node));
		node.setColumnNumber(1);
		assertFalse(ASTTools.hasValidPosition(node));
		node.setLineNumber(1);
		assertFalse(ASTTools.hasValidPosition(node));
		node.setLastColumnNumber(1);
		assertFalse(ASTTools.hasValidPosition(node));
		node.setLastLineNumber(1);
		assertTrue(ASTTools.hasValidPosition(node));
	}
	
	public void testTrimLeadingGap() {
		final String t1 = "\t\tTest";
		final String t2 = "   Test";
		final String t3 = " \t Test";
		
		assertEquals("Test", ASTTools.trimLeadingGap(t1));
		assertEquals("Test", ASTTools.trimLeadingGap(t2));
		assertEquals("Test", ASTTools.trimLeadingGap(t3));
	}
	
	public void testGetLeadingGap() {
		final String t1 = "\t\tTest";
		final String t2 = "   Test";
		final String t3 = " \t Test";
		
		assertEquals("\t\t", ASTTools.getLeadingGap(t1));
		assertEquals( "   ", ASTTools.getLeadingGap(t2));
		assertEquals( " \t ",ASTTools.getLeadingGap(t3));
	}
	
	public void testSetIntentationTo() {
		final String t1 = "Test";
		final String t2 = "\t\tTest";
		final String t3 = "        Test";
		final String t4 = " \t Test";
		final String t5 = "Test\n\tTest2";
		final String t6 = "\n";
		
		assertEquals("    Test",ASTTools.setIndentationTo(t1,1,ASTTools.SPACE));
		
		assertEquals("\tTest",ASTTools.setIndentationTo(t1,1,ASTTools.TAB));
		assertEquals("    Test",ASTTools.setIndentationTo(t2,-1,ASTTools.SPACE));
		
		assertEquals("Test",ASTTools.setIndentationTo(t3,-3,ASTTools.SPACE));
		assertEquals("\tTest",ASTTools.setIndentationTo(t3,-1,ASTTools.TAB));
		
		assertEquals("\tTest",ASTTools.setIndentationTo(t4,0,ASTTools.TAB));
		assertEquals("    Test",ASTTools.setIndentationTo(t4,0,ASTTools.SPACE));
		
		assertEquals("\tTest\n\t\tTest2",ASTTools.setIndentationTo(t5,1,ASTTools.TAB));
		assertEquals("Test\nTest2",ASTTools.setIndentationTo(t5,-1,ASTTools.TAB));

		assertEquals("    \n",ASTTools.setIndentationTo(t6,1,ASTTools.SPACE));
	}
	
	public void testLineFeed() {
		
		final Document defDoc = new Document();
		final String linebreak = defDoc.getDefaultLineDelimiter();
		
		final String t1 ="T\nT\r\nT\nT";
		final String t2 ="T\r\nT\r\nT\nT";
		final Document doc = new Document(t1);
		
		final Document doc2 = new Document(t2);
		
		assertEquals(4,doc.getNumberOfLines());
		assertEquals(4,doc2.getNumberOfLines());
		
		ASTTools.getDocumentWithSystemLineBreak(t1);
		
		try {
			assertEquals(linebreak, ASTTools.getDocumentWithSystemLineBreak(t1).getLineDelimiter(1));
			assertEquals(linebreak, ASTTools.getDocumentWithSystemLineBreak(t2).getLineDelimiter(1));
			
		} catch (final BadLocationException e) {
			e.printStackTrace();
		}
		
		
	}

}
