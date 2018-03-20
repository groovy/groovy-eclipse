/*
 * Copyright 2009-2018 the original author or authors.
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

import org.eclipse.debug.core.ILaunchConfigurationType;

public class GroovyConsoleLaunchShortcut extends AbstractGroovyLaunchShortcut {

    public static final String GROOVY_CONSOLE_LAUNCH_CONFIG_ID = "org.codehaus.groovy.eclipse.groovyConsoleLaunchConfiguration";

    @Override
    public ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(GROOVY_CONSOLE_LAUNCH_CONFIG_ID);
    }

    @Override
    protected String applicationOrConsole() {
        return "console";
    }

    @Override
    protected String classToRun() {
        return "groovy.ui.Console";
    }

    @Override
    protected boolean canLaunchWithNoType() {
        return true;
    }
}
