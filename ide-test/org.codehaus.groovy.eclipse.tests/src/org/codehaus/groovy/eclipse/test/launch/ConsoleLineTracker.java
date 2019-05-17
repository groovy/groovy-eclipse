/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.test.launch;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * Simple console line tracker extension point that delegates messages
 */
public final class ConsoleLineTracker implements IConsoleLineTrackerExtension {

    /**
     * Forwards messages to the delegate when not <code>null</code>
     */
    private static IConsoleLineTrackerExtension fDelegate;
    private static IConsole fConsole;

    /**
     * Sets the delegate, possibly <code>null</code>
     */
    public static void setDelegate(IConsoleLineTrackerExtension tracker) {
        fDelegate = tracker;
        fConsole = null;
    }

    @Override
    public void dispose() {
        if (fDelegate != null) {
            fDelegate.dispose();
        }
        fConsole = null;
    }

    @Override
    public synchronized void init(IConsole console) {
        fConsole = console;
        if (fDelegate != null) {
            fDelegate.init(console);
        }
    }

    /**
     * Returns the document backing this console
     */
    public static IDocument getDocument() {
        return fConsole.getDocument();
    }

    @Override
    public void lineAppended(IRegion line) {
        if (fDelegate != null) {
            fDelegate.lineAppended(line);
        }
    }

    @Override
    public void consoleClosed() {
        if (fDelegate != null && fConsole != null) {
            fDelegate.consoleClosed();
        }
    }
}
