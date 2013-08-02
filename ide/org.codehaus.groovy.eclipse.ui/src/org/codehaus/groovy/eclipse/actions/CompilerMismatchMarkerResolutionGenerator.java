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
    private static final IMarkerResolution[] NO_RESOLUTIONS = new IMarkerResolution[0];
    private static abstract class AbstractCompilerConfigurator extends WorkbenchMarkerResolution implements IMarkerResolution2 {
        private final IMarker thisMarker;

        public AbstractCompilerConfigurator(IMarker thisMarker) {
            this.thisMarker = thisMarker;
        }

        @Override
        public IMarker[] findOtherMarkers(IMarker[] markers) {
            List<IMarker> markerList = new ArrayList<IMarker>(markers.length);
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
        public String getLabel() {
            return "2. Configure Groovy compiler level for project";
        }

        public void run(IMarker marker) {
            IProject project = marker.getResource().getProject();
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

    private static class ConfigureWorksaceCompilerLevelResolution extends AbstractCompilerConfigurator {
        public ConfigureWorksaceCompilerLevelResolution(IMarker thisMarker) {
            super(thisMarker);
        }

        public String getLabel() {
            return "3. Configure the Groovy compiler level for the entire workspace";
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

    private static class SetToWorkspaceCompilerLevelResolution extends AbstractCompilerConfigurator {

        public SetToWorkspaceCompilerLevelResolution(IMarker thisMarker) {
            super(thisMarker);
        }
        public String getLabel() {
            return "1. Set the Groovy compiler level for project to match the workspace level";
        }

        public void run(IMarker marker) {
            IProject project = marker.getResource().getProject();
            CompilerUtils.setCompilerLevel(project, CompilerUtils.getActiveGroovyVersion(), true);
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
            return new IMarkerResolution[] { new ConfigureCompilerLevelResolution(marker),
                    new SetToWorkspaceCompilerLevelResolution(marker), new ConfigureWorksaceCompilerLevelResolution(marker) };
        }
        return NO_RESOLUTIONS;
    }

}
