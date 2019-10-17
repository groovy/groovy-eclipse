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
package org.codehaus.groovy.eclipse.launchers;

import java.io.File;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.util.CompilerUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GroovyConsoleLaunchShortcut extends AbstractGroovyLaunchShortcut {

    @Override
    public ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType("org.codehaus.groovy.eclipse.groovyConsoleLaunchConfiguration");
    }

    @Override
    protected String applicationOrConsole() {
        return "Console";
    }

    @Override
    protected boolean canLaunchWithNoType() {
        return true;
    }

    @Override
    protected String mainArgs(IType runType, IJavaProject javaProject) {
        StringBuilder mainArgs = new StringBuilder("groovy.ui.Console");

        if (isAtLeastGroovy(2, 5, 0)) {
            CompilerOptions compilerOptions = new CompilerOptions(javaProject.getOptions(true));
            CompilerUtils.configureOptionsBasedOnNature(compilerOptions, javaProject);

            if (compilerOptions.groovyCompilerConfigScript != null && !compilerOptions.groovyCompilerConfigScript.isEmpty() &&
                    (runType == null || (javaProject.isOnClasspath(runType) && !matchesScriptFilter(runType.getResource())))) {
                mainArgs.append(" --configscript ").append('"').append(getProjectLocation(javaProject))
                    .append(File.separator).append(compilerOptions.groovyCompilerConfigScript).append('"');
            }
            if ((compilerOptions.groovyFlags & CompilerUtils.InvokeDynamic) != 0) {
                mainArgs.append(" --indy");
            }
            if (compilerOptions.produceMethodParameters) {
                mainArgs.append(" --parameters");
            }
        }

        if (runType != null) {
            try {
                mainArgs.append(" \"${workspace_loc:").append(runType.getResource().getFullPath().toOSString().substring(1)).append("}\"");
            } catch (NullPointerException e) {
                GroovyCore.logException("Error running Groovy", new IllegalArgumentException("Could not find file to run for " + runType));
            }
        }

        return mainArgs.toString();
    }
}
