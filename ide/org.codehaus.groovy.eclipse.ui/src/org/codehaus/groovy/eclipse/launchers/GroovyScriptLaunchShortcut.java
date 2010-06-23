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


import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.ILaunchShortcut;

/**
 * This class is reponsible for creating a launching Groovy script files.  If an
 * existing launch configuration exists it will use that, if not it will
 * create a new launch configuration and launch it.
 *
 * @see ILaunchShortcut
 */
public class GroovyScriptLaunchShortcut extends AbstractGroovyLaunchShortcut {

	public static final String GROOVY_SCRIPT_LAUNCH_CONFIG_ID = "org.codehaus.groovy.eclipse.groovyScriptLaunchConfiguration" ;

    @Override
    public ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(GROOVY_SCRIPT_LAUNCH_CONFIG_ID) ;
    }

    @Override
    protected String applicationOrConsole() {
        return "script";
    }


    @Override
    protected String classToRun() {
        return "groovy.ui.GroovyMain";
    }


    @Override
    protected boolean canLaunchWithNoType() {
        return false;
    }
}
