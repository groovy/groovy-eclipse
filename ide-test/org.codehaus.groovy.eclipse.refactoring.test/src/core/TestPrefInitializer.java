/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package core;

import java.util.HashMap;

import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

/**
 *
 * Class to initialize Eclipse preferences in file based tests
 */
public class TestPrefInitializer {

	public static IPreferenceStore initializePreferences(HashMap<String, String> properties) {
	    IPreferenceStore pref = new PreferenceStore();

		String ind = properties.get("indentation");
        if (ind != null)
            pref.setValue(PreferenceConstants.GROOVY_FORMATTER_INDENTATION, ind);

		String tabsize = properties.get("tabsize");
		//Older tests will use tabsize assuming its the same as indentsize, so set both of these!
		if(tabsize != null) {
			pref.setValue(
					PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE,
					Integer.parseInt(tabsize));
			pref.setValue(
					PreferenceConstants.GROOVY_FORMATTER_TAB_SIZE,
					Integer.parseInt(tabsize));
		}
		String indentsize = properties.get("indentsize");
		if (indentsize!=null) {
			pref.setValue(
					PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE,
					Integer.parseInt(indentsize));
		}

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

		String indentEmptyLines = properties.get("indentEmptyLines");
		if (indentEmptyLines!=null) {
			pref.setValue(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, indentEmptyLines);
		}
		
		return pref;
	}

}
