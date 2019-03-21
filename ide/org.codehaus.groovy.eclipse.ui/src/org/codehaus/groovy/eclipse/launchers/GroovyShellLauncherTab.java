/*
 * Copyright 2009-2018 the original author or authors.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Dialog for creating a groovy shell launch configuration.
 */
public class GroovyShellLauncherTab extends JavaMainTab {

    @Override
    public Image getImage() {
        return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
    }

    @Override
    public String getName() {
        return "Groovy Shell";
    }

    @Override
    protected void createMainTypeEditor(Composite parent, String text) {
        super.createMainTypeEditor(parent, text);
        fMainText.getParent().setVisible(false);
        fMainText.setText("org.codehaus.groovy.tools.shell.Main");
        Button fSearchButton = ReflectionUtils.getPrivateField(SharedJavaMainTab.class, "fSearchButton", this);
        fSearchButton.setVisible(false);
        Button fSearchExternalJarsCheckButton = ReflectionUtils.getPrivateField(JavaMainTab.class, "fSearchExternalJarsCheckButton", this);
        fSearchExternalJarsCheckButton.setVisible(false);
        Button fConsiderInheritedMainButton = ReflectionUtils.getPrivateField(JavaMainTab.class, "fConsiderInheritedMainButton", this);
        fConsiderInheritedMainButton.setVisible(false);
        Button fStopInMainCheckButton = ReflectionUtils.getPrivateField(JavaMainTab.class, "fStopInMainCheckButton", this);
        fStopInMainCheckButton.setVisible(false);
    }

    @Override
    protected void handleSearchButtonSelected() {
    }

    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        String projectName = EMPTY_STRING;
        try {
            projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
        } catch (CoreException ce) {
            setErrorMessage(ce.getStatus().getMessage());
        }
        fProjText.setText(projectName);

        ReflectionUtils.executePrivateMethod(JavaLaunchTab.class, "setCurrentLaunchConfiguration", new Class[] {ILaunchConfiguration.class}, this, new Object[] {config});
    }

    @Override
    protected void updateMainTypeFromConfig(ILaunchConfiguration config) {
    }
}
