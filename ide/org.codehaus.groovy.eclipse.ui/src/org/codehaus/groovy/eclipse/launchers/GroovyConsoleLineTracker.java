/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.launchers;

import org.eclipse.jdt.internal.debug.ui.console.JavaConsoleTracker;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Adds links to consoles for each occurrence of "pack.Type.meth(File.groovy)".
 * <p>
 * NOTE: "pack.Type.meth(File.groovy:line)" is handled by {@code JavaConsoleTracker}.
 */
public class GroovyConsoleLineTracker extends JavaConsoleTracker {

    @Override
    public void matchFound(final PatternMatchEvent event) {
        int offset = event.getOffset();
        int length = event.getLength() - 1;
        TextConsole console = getConsole();
        try {
            String consoleExcerpt = console.getDocument().get(offset, length);

            IHyperlink link = new JavaStackTraceHyperlink(console) {
                @Override
                protected String getLinkText() {
                    return consoleExcerpt + ":1)";
                }

                @Override
                protected String getTypeName(final String linkText) {
                    String typeName = linkText.substring(0, linkText.indexOf('('));
                    typeName = typeName.substring(0, typeName.lastIndexOf('.'));
                    return typeName;
                }
            };

            int index = consoleExcerpt.indexOf('(') + 1;
            console.addHyperlink(link, offset + index, length - index);
        } catch (BadLocationException ignore) {
        }
    }
}
