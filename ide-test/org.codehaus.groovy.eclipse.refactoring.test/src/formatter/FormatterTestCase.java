/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package formatter;

import core.TestPrefInitializer;
import java.io.File;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MalformedTreeException;
import tests.BaseTestCase;

/**
 * Test Case to test the Groovy Formatter
 * 
 * @author Michael Klenk mklenk@hsr.ch
 * 
 */
public class FormatterTestCase extends BaseTestCase {

	public FormatterTestCase(String arg0, File arg1) {
		super(arg0, arg1);
		// Set Method to call for JUnit
		setName("testFormatter");
	}

	public void testFormatter() {

		boolean indentendOnly = false;
		Preferences pref = null;

		if (properties.get("setPreferences") != null && properties.get("setPreferences").equals("true")) {
			try {
				pref = TestPrefInitializer.initializePreferences(properties);
				String indOnly = properties.get("indentendOnly");
				if(indOnly != null && indOnly.equals("true"))
					indentendOnly = true;

			} catch (Exception e) {
				e.printStackTrace();
				fail("Initialisation of testproperties failed! " + e.getMessage());
			}
		}

		ITextSelection sel = new TextSelection(selection.getOffset(), selection.getLength());
		DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(sel,
				getDocumentProvider().getDocument(), pref, indentendOnly);
		try {
			formatter.format().apply(getDocumentProvider().getDocument());
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		finalAssert();
	}
	
	@Override
    public void finalAssert() {
		String expected = getExpected().get();
		String content = getDocumentProvider().getDocumentContent();
		assertEquals("Error in File: " + file + " ", expected,content);
	}
}
