package org.codehaus.groovy.eclipse.core.compiler;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.groovy.core.Activator;

public class CompilerCheckerParticipant extends CompilationParticipant {

    public static final String COMPILER_MISMATCH_PROBLEM = "org.codehaus.groovy.eclipse.core.compilerMismatch";

    private static IEclipsePreferences store;

    private static IEclipsePreferences getPreferences() {
        // workspace settings
        IScopeContext scope = InstanceScope.INSTANCE;
        return scope.getNode(Activator.PLUGIN_ID);
    }

    @Override
    public boolean isActive(IJavaProject javaProject) {
        if (store == null) {
            store = getPreferences();
        }
        return store.getBoolean(Activator.GROOVY_CHECK_FOR_COMPILER_MISMATCH, true)
                && GroovyNature.hasGroovyNature(javaProject.getProject());
    }

    @Override
    public int aboutToBuild(IJavaProject javaProject) {
        IProject project = javaProject.getProject();
        SpecifiedVersion projectLevel = CompilerUtils.getCompilerLevel(project);
        if (projectLevel == SpecifiedVersion.UNSPECIFIED) {
            // project is unspecified. just grab the current level and assume it
            // is correct
            SpecifiedVersion workspaceLevel = projectLevel = CompilerUtils.getWorkspaceCompilerLevel();
            CompilerUtils.setCompilerLevel(project, workspaceLevel);
        }
        try {
            boolean compilerMatch = CompilerUtils.projectVersionMatchesWorkspaceVersion(projectLevel);
            IMarker[] findMarkers = project.findMarkers(COMPILER_MISMATCH_PROBLEM, true, IResource.DEPTH_ZERO);
            if (compilerMatch) {
                for (IMarker marker : findMarkers) {
                    marker.delete();
                }
            } else if (findMarkers.length == 0) {
                SpecifiedVersion workspaceLevel = CompilerUtils.getWorkspaceCompilerLevel();
                IMarker marker = project.getProject().createMarker(COMPILER_MISMATCH_PROBLEM);
                marker.setAttribute(IMarker.MESSAGE,
                        "Groovy compiler level expected by the project does not match workspace compiler level. "
                                + "\nProject compiler level is: " + projectLevel.toReadableVersionString()
                                + "\nWorkspace compiler level is " + workspaceLevel.toReadableVersionString()
                                + "\nGo to Project properties -> Groovy compiler to set the Groovy compiler level for this project");
                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                marker.setAttribute(IMarker.LOCATION, project.getName());
            }
        } catch (CoreException e) {
            GroovyCore.logException("Error creating marker", e);
        }
        return super.aboutToBuild(javaProject);
    }

    @Override
    public void cleanStarting(IJavaProject javaProject) {
        IProject project = javaProject.getProject();
        try {
            IMarker[] findMarkers = project.findMarkers(COMPILER_MISMATCH_PROBLEM, true, IResource.DEPTH_ZERO);
            for (IMarker marker : findMarkers) {
                marker.delete();
            }
        } catch (CoreException e) {
            GroovyCore.logException("Error finding markers", e);
        }
    }
}
