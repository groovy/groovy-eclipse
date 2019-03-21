/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

public class GroovyConsoleFactory implements IConsoleFactory {

    private IConsoleManager fConsoleManager;
    private GroovyConsole fConsole;

    public GroovyConsoleFactory() {
        fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
        fConsoleManager.addConsoleListener(new IConsoleListener() {
            @Override
            public void consolesAdded(IConsole[] consoles) {
            }

            @Override
            public void consolesRemoved(IConsole[] consoles) {
                for (int i = 0; i < consoles.length; i++) {
                    if (consoles[i] == fConsole) {
                        fConsole = null;
                    }
                }
            }

        });
    }

    @Override
    public void openConsole() {
        if (fConsole == null) {
            fConsole = new GroovyConsole();
            fConsoleManager.addConsoles(new IConsole[] { fConsole });
        }
        fConsoleManager.showConsoleView(fConsole);
    }
}
