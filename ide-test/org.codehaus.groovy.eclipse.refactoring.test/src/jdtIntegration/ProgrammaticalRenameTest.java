/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration;

import static tests.BaseTestCase.getContents;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.FilePartReader;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import core.ASTProvider;
import core.TestFileProvider;

/**
 * @author Stefan Reinhard
 */
public class ProgrammaticalRenameTest extends TestCase {

	protected ModuleNode node;
	protected Document doc;
	protected String content;
	protected IGroovyFileProvider fileProvider;
	protected File file;
	protected final Pattern origin, expected;
	
	public ProgrammaticalRenameTest(String fileName) {
		file = new File(fileName);
		String newLine = FilePartReader.getLineDelimiter(file);
		origin = Pattern.compile("###src" + newLine + "(.*)" + newLine + "###exp",Pattern.DOTALL);
		expected = Pattern.compile("###exp" + newLine + "(.*)" + newLine + "###end",Pattern.DOTALL);
		node = ASTProvider.getAST(getArea(origin),fileName);
		content = getArea(origin);
		doc = new Document(content);
		fileProvider = new TestFileProvider(content, "source");
	}
	
	protected String getArea(Pattern regExpression){
		String filecontent = getContents(file);
		Matcher match = regExpression.matcher(filecontent);
		if(match.find()) {
			return match.group(1);
		}
        return null;
	}
	
	protected void checkRefactoring(RefactoringProvider provider) {
		try {
			NullProgressMonitor npm = new NullProgressMonitor();
			RefactoringStatus s = provider.checkInitialConditions(npm);
			assertTrue(s.isOK());
			GroovyChange gc = provider.createGroovyChange(npm);
			gc.performChanges();
		} catch (Exception e) {
		    GroovyCore.logException(e.getMessage(), e);
		}
		IGroovyDocumentProvider gdoc = fileProvider.getAllSourceFiles().get(0);
		String code = gdoc.getDocumentContent();
		assertEquals(getArea(expected), code);
	}
}
