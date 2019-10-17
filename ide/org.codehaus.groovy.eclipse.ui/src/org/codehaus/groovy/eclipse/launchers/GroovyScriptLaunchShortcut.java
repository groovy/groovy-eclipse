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

public class GroovyScriptLaunchShortcut extends AbstractGroovyLaunchShortcut {

    @Override
    public ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType("org.codehaus.groovy.eclipse.groovyScriptLaunchConfiguration");
    }

    @Override
    protected String applicationOrConsole() {
        return "script";
    }

    @Override
    protected boolean canLaunchWithNoType() {
        return false;
    }

    @Override
    protected String mainArgs(IType runType, IJavaProject javaProject) {
        CompilerOptions compilerOptions = new CompilerOptions(javaProject.getOptions(true));
        CompilerUtils.configureOptionsBasedOnNature(compilerOptions, javaProject);

        StringBuilder mainArgs = new StringBuilder("groovy.ui.GroovyMain");

        if (compilerOptions.groovyCompilerConfigScript != null && !compilerOptions.groovyCompilerConfigScript.isEmpty() &&
                (runType == null || (javaProject.isOnClasspath(runType) && !matchesScriptFilter(runType.getResource())))) {
            mainArgs.append(" --configscript ").append('"').append(getProjectLocation(javaProject))
                .append(File.separator).append(compilerOptions.groovyCompilerConfigScript).append('"');
        }
        if (compilerOptions.defaultEncoding != null && !compilerOptions.defaultEncoding.isEmpty()) {
            mainArgs.append(" --encoding ").append(compilerOptions.defaultEncoding);
        }
        if (compilerOptions.produceMethodParameters && isAtLeastGroovy(2, 5, 0)) {
            mainArgs.append(" --parameters");
        }
        if (compilerOptions.enablePreviewFeatures && isAtLeastGroovy(2, 5, 7)) {
            mainArgs.append(" --enable-preview");
        }
        if ((compilerOptions.groovyFlags & CompilerUtils.InvokeDynamic) != 0) {
            //mainArgs.append(" --indy"); // GROOVY-9121
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
