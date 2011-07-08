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

package org.codehaus.groovy.eclipse.preferences;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.builder.ConvertLegacyProject;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

/**
 * Brings up a dialogue that asks if old plugins should be converted
 * @author Andrew Eisenberg
 * @created Sep 28, 2009
 *
 */
public class AskToConvertLegacyProjects extends UIJob {

    public AskToConvertLegacyProjects() {
        super("Convert Legacy Projects");
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        ConvertLegacyProject legacy = new ConvertLegacyProject();

        if (legacy.getAllOldProjects().length == 0) {
            return Status.OK_STATUS;
        }

        Shell shell = this.getDisplay().getActiveShell();
        boolean shouldDispose = false;
        if (shell == null) {
            Shell[] shells = this.getDisplay().getShells();
            if (shells.length > 0) {
                shell = shells[0];
            } else {
                shell = new Shell(this.getDisplay());
                shouldDispose = true;
            }
        }
        IPreferenceStore prefs = GroovyPlugin.getDefault().getPreferenceStore();
        MessageDialogWithToggle d = MessageDialogWithToggle.openYesNoQuestion(
                shell, "Convert legacy Groovy Projects",
                "Some of your Groovy projects appear to be incompatible " +
                "with the new version of the Groovy plugin.  Should they be converted now?" +
                "\n\nThey can be converted later from the Groovy Preferences page",
                "Don't show this message again.", false, prefs,
                PreferenceConstants.GROOVY_ASK_TO_CONVERT_LEGACY_PROJECTS);
        try {
            if (IDialogConstants.YES_ID == d.getReturnCode()) {
                IProject[] projects = legacy.getAllOldProjects();
                monitor.beginTask("", projects.length);
                for (IProject project : projects) {
                    legacy.convertProject(project);
                    monitor.internalWorked(1);
                }
            }

            prefs.setValue(PreferenceConstants.GROOVY_ASK_TO_CONVERT_LEGACY_PROJECTS, !d.getToggleState());
            return Status.OK_STATUS;
        } catch (Exception e) {
            return new Status(IStatus.ERROR, GroovyPlugin.PLUGIN_ID, "Error converting legacy projects", e);
        } finally {
            if (shouldDispose) {
                shell.dispose();
            }
        }
    }
}
