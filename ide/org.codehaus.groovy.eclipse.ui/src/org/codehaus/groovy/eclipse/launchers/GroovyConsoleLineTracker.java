/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
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
			    
//			    throw new RuntimeException("Not implemented yet");
			    
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
