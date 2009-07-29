/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package core;

import java.util.HashMap;

import org.codehaus.groovy.eclipse.refactoring.preferences.PreferenceConstants;
import org.eclipse.core.runtime.Preferences;

/**
 * 
 * Class to initialize Eclipse preferences in file based tests
 */
public class TestPrefInitializer {
	
	public static Preferences initializePreferences(HashMap<String, String> properties) {
		Preferences pref = new Preferences();
		
		String ind = properties.get("indentation");
		if(ind != null)
		pref.setValue(PreferenceConstants.GROOVY_FORMATTER_INDENTATION,
				ind);

		String tabsize = properties.get("tabsize");
		if(tabsize != null)
		pref.setValue(
				PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE,
				Integer.parseInt(tabsize));
		
		String multiInd = properties.get("multilineIndentation");
		if(multiInd != null)
		pref.setValue(
				PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION,
				Integer.parseInt(multiInd));
		
		String bracesStart = properties.get("bracesStart");
		if(bracesStart != null)
		pref.setValue(PreferenceConstants.GROOVY_FORMATTER_BRACES_START, bracesStart);

		String bracesEnd = properties.get("bracesEnd");
		if(bracesEnd != null)
		pref.setValue(PreferenceConstants.GROOVY_FORMATTER_BRACES_END, bracesEnd);



		String maxLineLength = properties.get("maxLineLegth");
		if(maxLineLength != null)
		pref.setValue(
				PreferenceConstants.GROOVY_FORMATTER_MAX_LINELENGTH,
				Integer.parseInt(maxLineLength));
		
		return pref;
	}

}
