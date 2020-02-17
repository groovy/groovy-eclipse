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

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.util.CompilerUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GroovyShellLaunchShortcut extends AbstractGroovyLaunchShortcut {

    @Override
    protected ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType("org.codehaus.groovy.eclipse.groovyShellLaunchConfiguration");
    }

    @Override
    protected String applicationOrConsole() {
        return "Shell";
    }

    @Override
    protected boolean canLaunchWithNoType() {
        return true;
    }

    @Override
    protected String mainArgs(IType runType, IJavaProject javaProject) {
        StringBuilder mainArgs = new StringBuilder("org.codehaus.groovy.tools.shell.Main");
        mainArgs.append(" --define jline.terminal=jline.UnsupportedTerminal");

        if (isAtLeastGroovy(2, 5, 0)) {
            CompilerOptions compilerOptions = new CompilerOptions(javaProject.getOptions(true));
            CompilerUtils.configureOptionsBasedOnNature(compilerOptions, javaProject);
            if (compilerOptions.produceMethodParameters) {
                mainArgs.append(" --parameters");
            }
        }

        return mainArgs.toString();
    }
}
