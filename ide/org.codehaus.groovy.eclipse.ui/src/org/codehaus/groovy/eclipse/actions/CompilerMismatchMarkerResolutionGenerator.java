package org.codehaus.groovy.eclipse.actions;

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.preferences.CompilerPreferencesPage;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class CompilerMismatchMarkerResolutionGenerator implements IMarkerResolutionGenerator {
    private static final IMarkerResolution[] NO_RESOLUTIONS = new IMarkerResolution[0];

    private static class ConfigureCompilerLevelResolution implements IMarkerResolution2 {

        private IProject project;

        ConfigureCompilerLevelResolution(IProject project) {
            this.project = project;
        }
        public String getLabel() {
            return "Configure Groovy compiler level for " + project.getName();
        }

        public void run(IMarker marker) {
            PreferenceDialog propertyDialog = PreferencesUtil.createPropertyDialogOn(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell(), project, CompilerPreferencesPage.PROPERTY_ID,
                    new String[] { CompilerPreferencesPage.PROPERTY_ID }, null);
            propertyDialog.open();
        }

        public String getDescription() {
            return "Opens the Groovy Compiler preferences for the project so that the compiler level can be changed.";
        }

        public Image getImage() {
            return JavaPluginImages.DESC_ELCL_CONFIGURE_BUILDPATH.createImage();
        }
    }

    private static class ConfigureWorksaceCompilerLevelResolution implements IMarkerResolution2 {

        public String getLabel() {
            return "Configure the Groovy compiler level for the entire workspace";
        }

        public void run(IMarker marker) {
            PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell(), CompilerPreferencesPage.PREFERENCES_ID, new String[] { CompilerPreferencesPage.PREFERENCES_ID }, null);
            preferenceDialog.open();
        }

        public String getDescription() {
            return "Opens the Groovy Compiler preferences for the workspace preferences.  From here, you can choose the Groovy compiler level for the workspace (restart required).";
        }

        public Image getImage() {
            return JavaPluginImages.DESC_ELCL_CONFIGURE_BUILDPATH.createImage();
        }
    }

    private static class SetToWorkspaceCompilerLevelResolution implements IMarkerResolution2 {

        private IProject project;

        SetToWorkspaceCompilerLevelResolution(IProject project) {
            this.project = project;
        }
        public String getLabel() {
            return "Set the Groovy compiler level " + project.getName() + " to match the workspace level";
        }

        public void run(IMarker marker) {
            CompilerUtils.setCompilerLevel(project, CompilerUtils.getActiveGroovyVersion());
        }

        public String getDescription() {
            return "Forces the workspace Groovy compiler level onto this project.";
        }

        public Image getImage() {
            return JavaPluginImages.DESC_ELCL_CONFIGURE_BUILDPATH.createImage();
        }
    }

    public IMarkerResolution[] getResolutions(IMarker marker) {
        if (marker.getResource().getType() == IResource.PROJECT) {
            IProject project = (IProject) marker.getResource();
            return new IMarkerResolution[] { new ConfigureCompilerLevelResolution(project),
                    new SetToWorkspaceCompilerLevelResolution(project), new ConfigureWorksaceCompilerLevelResolution() };
        }
        return NO_RESOLUTIONS;
    }

}
