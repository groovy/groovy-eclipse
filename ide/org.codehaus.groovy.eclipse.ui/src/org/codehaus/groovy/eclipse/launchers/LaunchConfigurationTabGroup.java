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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaDependenciesTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Composite;

public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfiguration configuration = DebugUITools.getLaunchConfiguration(dialog);
        boolean isModularConfiguration = configuration != null && JavaRuntime.isModularConfiguration(configuration);

        List<ILaunchConfigurationTab> tabs = new ArrayList<>();
        tabs.add(new GroovyMainTab());
        tabs.add(new JavaArgumentsTab());
        tabs.add(new JavaJRETab());
        tabs.add(isModularConfiguration ? new JavaDependenciesTab() : new JavaClasspathTab());
        if (ILaunchManager.DEBUG_MODE.equals(mode)) tabs.add(new SourceLookupTab());
        tabs.add(new EnvironmentTab());
        tabs.add(new CommonTab());

        try { // available in Eclipse IDE 4.8:
            tabs.add((ILaunchConfigurationTab) Class.forName("org.eclipse.debug.ui.PrototypeTab").newInstance());
        } catch (Exception ignore) {
        }

        setTabs(tabs.toArray(new ILaunchConfigurationTab[tabs.size()]));
    }

    /**
     * A launch configuration tab that displays and edits project and main type
     * launch configuration attributes.
     */
    public static class GroovyMainTab extends JavaMainTab {

        @Override
        protected void createMainTypeEditor(Composite parent, String text) {
            super.createMainTypeEditor(parent, text);
            // hide main type extensions like "stop in main"
            super.createMainTypeExtensions(new Composite(parent, 0));

            fMainText.setEnabled(false);
            fMainText.getParent().getChildren()[1].setEnabled(false);
        }

        @Override
        protected void createMainTypeExtensions(Composite parent) {
        }

        @Override
        protected void handleSearchButtonSelected() {
        }
    }
}
