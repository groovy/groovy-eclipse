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
