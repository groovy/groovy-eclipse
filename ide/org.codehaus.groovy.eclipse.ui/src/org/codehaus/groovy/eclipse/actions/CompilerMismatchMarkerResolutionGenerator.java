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
package org.codehaus.groovy.eclipse.actions;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.preferences.CompilerPreferencesPage;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

public class CompilerMismatchMarkerResolutionGenerator implements IMarkerResolutionGenerator {

    @Override
    public IMarkerResolution[] getResolutions(IMarker marker) {
        if (marker.getResource().getType() == IResource.PROJECT) {
            return new IMarkerResolution[] {
                new ConfigureCompilerLevelResolution(marker),
                new SetToWorkspaceCompilerLevelResolution(marker),
                new ConfigureWorksaceCompilerLevelResolution(marker),
            };
        }
        return new IMarkerResolution[0];
    }

    private abstract static class AbstractCompilerConfigurator extends WorkbenchMarkerResolution implements IMarkerResolution2 {
        private final IMarker thisMarker;

        AbstractCompilerConfigurator(IMarker thisMarker) {
            this.thisMarker = thisMarker;
        }

        @Override
        public IMarker[] findOtherMarkers(IMarker[] markers) {
            List<IMarker> markerList = new ArrayList<>(markers.length);
            for (IMarker marker : markers) {
                try {
                    if (marker != thisMarker && marker.getType().equals(GroovyPlugin.COMPILER_MISMATCH_MARKER)) {
                        markerList.add(marker);
                    }
                } catch (CoreException e) {
                    GroovyCore.logException("Error accessing marker", e);
                }
            }
            return markerList.toArray(new IMarker[0]);
        }
    }

    private static class ConfigureCompilerLevelResolution extends AbstractCompilerConfigurator {

        ConfigureCompilerLevelResolution(IMarker thisMarker) {
            super(thisMarker);
        }

        @Override
        public String getLabel() {
            return "2. Configure Groovy compiler level for project";
        }

        @Override
        public void run(IMarker marker) {
            IProject project = marker.getResource().getProject();
            PreferenceDialog propertyDialog = PreferencesUtil.createPropertyDialogOn(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), project,
                CompilerPreferencesPage.PROPERTY_ID, new String[] {CompilerPreferencesPage.PROPERTY_ID}, null);
            propertyDialog.open();
        }

        @Override
        public String getDescription() {
            return "Opens the Groovy Compiler preferences for the project so that the compiler level can be changed.";
        }

        @Override
        public Image getImage() {
            return JavaPluginImages.DESC_ELCL_CONFIGURE_BUILDPATH.createImage();
        }
    }

    private static class ConfigureWorksaceCompilerLevelResolution extends AbstractCompilerConfigurator {

        ConfigureWorksaceCompilerLevelResolution(IMarker thisMarker) {
            super(thisMarker);
        }

        @Override
        public String getLabel() {
            return "3. Configure the Groovy compiler level for the entire workspace";
        }

        @Override
        public void run(IMarker marker) {
            PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CompilerPreferencesPage.PREFERENCES_ID,
                new String[] {CompilerPreferencesPage.PREFERENCES_ID}, null);
            preferenceDialog.open();
        }

        @Override
        public String getDescription() {
            return "Opens the Groovy Compiler preferences for the workspace preferences." +
                "  From here, you can choose the Groovy compiler level for the workspace (restart required).";
        }

        @Override
        public Image getImage() {
            return JavaPluginImages.DESC_ELCL_CONFIGURE_BUILDPATH.createImage();
        }
    }

    private static class SetToWorkspaceCompilerLevelResolution extends AbstractCompilerConfigurator {

        SetToWorkspaceCompilerLevelResolution(IMarker thisMarker) {
            super(thisMarker);
        }

        @Override
        public String getLabel() {
            return "1. Set the Groovy compiler level for project to match the workspace level";
        }

        @Override
        public void run(IMarker marker) {
            IProject project = marker.getResource().getProject();
            CompilerUtils.setCompilerLevel(project, CompilerUtils.getActiveGroovyVersion(), true);
        }

        @Override
        public String getDescription() {
            return "Forces the workspace Groovy compiler level onto this project.";
        }

        @Override
        public Image getImage() {
            return JavaPluginImages.DESC_ELCL_CONFIGURE_BUILDPATH.createImage();
        }
    }
}
