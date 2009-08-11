 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.launchers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * 
 * @author Scott Hickey
 */
public class GroovyConsoleLineTracker implements IConsoleLineTracker {

	private IConsole console; 
	static Pattern linePattern = Pattern.compile("(.*)\\.groovy:(.*)");

	
	public void init(IConsole console) {
		this.console = console;
	}

	/**
	 * Hyperlink error lines to the editor.
	 */
	// not implemented yet
	public void lineAppended(IRegion line) {
		int lineOffset = line.getOffset();
		int lineLength = line.getLength();
		try {
			String consoleLine = console.getDocument().get(lineOffset, lineLength);
			GroovyPlugin.trace(consoleLine);
			Matcher m = linePattern.matcher(consoleLine);
//			String groovyFileName = null;
//			int lineNumber = 0;
//			int openParenIndexAt = -1;
//			int closeParenIndexAt = -1;
			// match
			if (m.matches()) {
			    System.out.println("match: " + m);
			    
//				consoleLine = m.group(0);
//				openParenIndexAt = consoleLine.indexOf("(");
//				if (openParenIndexAt >= 0) {
//				    int end = consoleLine.indexOf(".groovy");
//				    if(end == -1 || (openParenIndexAt + 1) >= end) {
//				        return;
//				    }
//					String groovyClassName = consoleLine.substring(openParenIndexAt + 1, end);
//					int classIndex = consoleLine.indexOf(groovyClassName);
//					int start = 3;
//					if(classIndex < start || classIndex >= consoleLine.length()) {
//					    return;
//					}
//					String groovyFilePath = consoleLine.substring(start, classIndex).trim().replace('.','/');
//					groovyFileName = groovyFilePath + groovyClassName + ".groovy";
//					int colonIndex = consoleLine.indexOf(":");
//					// get the line number in groovy class
//					if (colonIndex > 0) { 
//						closeParenIndexAt = consoleLine.indexOf(")");  
//						lineNumber = Integer.parseInt(consoleLine.substring(colonIndex + 1, closeParenIndexAt));
//					}
//					GroovyPlugin.trace("groovyFile=" + groovyFileName + " lineNumber:" + lineNumber);
//				}
//				// hyperlink if we found something
//				if (groovyFileName != null) {
//					GroovyModel model = GroovyModel.getModel();
//					IFile f = model.getIFileForSrcFile(groovyFileName);
//	                IHyperlink link = null;
//	                if (f !=null) link = new FileLink(f, null, -1, -1, lineNumber);
//					if (link != null)
//						console.addLink(link, lineOffset + openParenIndexAt + 1, closeParenIndexAt - openParenIndexAt -1);
//				}
			}
		} catch (BadLocationException e) {
			GroovyPlugin.trace("unexpected error:" +  e.getMessage());
		}
	}

	/**
	 * NOOP.
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
	}

}
