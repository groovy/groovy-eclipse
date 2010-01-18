/*******************************************************************************
 *  Copyright (c) 2000, 2007 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.test.debug;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * Simple console line tracker extension point that delegates messages 
 */
public class ConsoleLineTracker implements IConsoleLineTrackerExtension {
	
	/**
	 * Forwards messages to the delegate when not <code>null</code> 
	 */
	private static IConsoleLineTrackerExtension fDelegate;
	private static IConsole fConsole;
	
	/**
	 * Sets the delegate, possibly <code>null</code>
	 *  
	 * @param tracker
	 */
	public static void setDelegate(IConsoleLineTrackerExtension tracker) {
		fDelegate = tracker;
		fConsole = null;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		if (fDelegate != null) {
			fDelegate.dispose();
		}
		fConsole = null;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public synchronized void init(IConsole console) {
		fConsole= console;
		if (fDelegate != null) {
			fDelegate.init(console);
		}
	}
	
	/**
	 * Returns the document backing this console
	 * @return the document backingthis console
	 */
	public static IDocument getDocument() {
		return fConsole.getDocument();
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		if (fDelegate != null) {
			fDelegate.lineAppended(line);
		}
	}
	
	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTrackerExtension#consoleClosed()
	 */
	public void consoleClosed() {
		if (fDelegate != null && fConsole != null) {
			fDelegate.consoleClosed();
		}		
	}
	
}
