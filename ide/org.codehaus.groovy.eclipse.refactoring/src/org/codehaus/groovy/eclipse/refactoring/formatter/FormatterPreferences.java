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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import org.codehaus.groovy.eclipse.refactoring.preferences.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Mike Klenk mklenk@hsr.ch
 *
 */
public class FormatterPreferences {
	
	public static final int SAME_LINE = 0;
	public static final int NEXT_LINE = 1;
	
	
	public boolean useTabs = true;
	public int tabSize = 4;
	public int indentationMultiline = 2;
	
	public int bracesStart = SAME_LINE;
	public int bracesEnd = NEXT_LINE;
	
	public int maxLineLength = 80;

	
	public FormatterPreferences(IPreferenceStore preferences) {
		
		if(preferences != null) {
		String pTab = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_INDENTATION);
		int pTabSize = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE);
		int pIndeMulti = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION);
		
		String pBracesStart = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_BRACES_START);
		String pBracesEnd = preferences.getString(PreferenceConstants.GROOVY_FORMATTER_BRACES_END);		

		int pMaxLine = preferences.getInt(PreferenceConstants.GROOVY_FORMATTER_MAX_LINELENGTH);

		if(pBracesStart != null && pBracesStart.equals("next"))
			bracesStart = NEXT_LINE;
		if(pBracesEnd != null && pBracesEnd.equals("same"))
			bracesEnd = SAME_LINE;
		
		if (pTab != null && pTab.equals("space"))
			useTabs = false;
		if(pTabSize != 0)
			tabSize = pTabSize;
		if(pIndeMulti != 0)
			indentationMultiline = pIndeMulti;
		
		if(pMaxLine != 0)
			maxLineLength = pMaxLine;
		}
	}

}
