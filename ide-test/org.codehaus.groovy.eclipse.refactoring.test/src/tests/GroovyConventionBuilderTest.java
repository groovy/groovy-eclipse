/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package tests;

import java.lang.reflect.Field;
import junit.framework.TestCase;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyConventionsBuilder;
import org.eclipse.core.runtime.IStatus;

/**
 * @author Klenk Michael mklenk@hsr.ch
 * @author Kempf Martin martin.kempf@gmail.com
 * 
 * Test for class used to verify user input (making sure that no keywords etc are being used)
 */
public class GroovyConventionBuilderTest extends TestCase {
	

	public void testValidateEmptyName() {
		assertTrue("Empty name returns not an Error",new GroovyConventionsBuilder("",null).done().matches(IStatus.ERROR));
	}
	
	public void testValidateLowerCaseName() {
		assertTrue("Uppercase name returns no Warning",new GroovyConventionsBuilder("Build",null).validateLowerCase(IStatus.WARNING).done().matches(IStatus.WARNING));
	}

	public void testValidateUpperCaseName() {
		assertTrue("Lowercase name returns no Warning",new GroovyConventionsBuilder("build",null).validateUpperCase(IStatus.WARNING).done().matches(IStatus.WARNING));
	}
	
	public void testGroovyIdentifier() {
		String literal = null;
		for(Field f :GroovyTokenTypes.class.getDeclaredFields()) {
			if(f.getName().startsWith("LITERAL_")) {
				literal = f.getName().substring(8);
				assertTrue("Literal '" + literal + "' is not a valid identifier",new GroovyConventionsBuilder(literal,null).validateGroovyIdentifier().done().matches(IStatus.ERROR));
			}
		}
		
	}
	
}
