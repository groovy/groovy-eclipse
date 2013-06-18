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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
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
    public void buildStarting(BuildContext[] files, boolean isBatch) {
        if (files == null || files.length == 0) {
            return;
        }

        IProject project = files[0].getFile().getProject();
        SpecifiedVersion projectLevel = CompilerUtils.getCompilerLevel(project);
        try {
            boolean compilerMatch = CompilerUtils.projectVersionMatchesWorkspaceVersion(projectLevel);
            IMarker[] findMarkers = project.findMarkers(COMPILER_MISMATCH_PROBLEM, true, IResource.DEPTH_ZERO);
            if (compilerMatch) {
                for (IMarker marker : findMarkers) {
                    marker.delete();
                }
            } else if (findMarkers.length == 0) {
                CompilerUtils.addCompilerMismatchError(project, projectLevel);
            }
        } catch (CoreException e) {
            GroovyCore.logException("Error creating marker", e);
        }
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

    @Override
    public void buildFinished(IJavaProject javaProject) {
        // if the project does not already have a compiler level set, infer it
        // from the classpath
        // but only if there was a clean build.
        IProject project = javaProject.getProject();
        try {
            SpecifiedVersion projectLevel = CompilerUtils.getCompilerLevel(project);
            if (projectLevel == SpecifiedVersion.DONT_CARE) {
                return; // All checks related to compiler levels disabled.
            }
            // project is unspecified. Try to find the groovy version on the
            // classpath
            if (projectLevel == SpecifiedVersion.UNSPECIFIED) {
                IClasspathEntry[] classpath = javaProject.getResolvedClasspath(true);
                SpecifiedVersion found1 = null;
                SpecifiedVersion found2 = null;
                for (IClasspathEntry entry : classpath) {
                    if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                        String jarName = entry.getPath().lastSegment();
                        SpecifiedVersion inferredProjectLevel = SpecifiedVersion.parseVersion(jarName);
                        if (inferredProjectLevel != SpecifiedVersion.UNSPECIFIED) {
                        	if (found1 == null) {
                        		//first one found now. Just remember it
                        		found1 = inferredProjectLevel;
                            } else if (found2 == null) { // only found 1 version
                                                         // so far
                            	if (inferredProjectLevel==found1) {
                            		//Same, so nothing new.
                            	} else {
                            	    found2 = inferredProjectLevel;
                                    CompilerUtils.addMultipleCompilersOnClasspathError(project, found1, found2);
                            	}
                            }
                        }
                    }
                }
                if (found1!=null && found2==null) {
                	//Only set compiler level if there's no ambiguity about what to set it to.
                    CompilerUtils.setCompilerLevel(project, found1);
                }
            }

        } catch (CoreException e) {
            GroovyCore.logException("Exception thrown while inferring project " + project.getName() + "'s groovy compiler level.",
                    e);
            return;
        }

    }
}
