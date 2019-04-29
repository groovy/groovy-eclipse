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
package org.codehaus.groovy.eclipse.core.compiler;

import static org.eclipse.jdt.groovy.core.Activator.PLUGIN_ID;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.groovy.core.Activator;

public class CompilerCheckerParticipant extends CompilationParticipant {

    public static final String COMPILER_MISMATCH_PROBLEM = "org.codehaus.groovy.eclipse.core.compilerMismatch";

    @Override
    public boolean isActive(IJavaProject javaProject) {
        if (GroovyNature.hasGroovyNature(javaProject.getProject())) {
            IEclipsePreferences workspacePreferences = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
            return workspacePreferences.getBoolean(Activator.GROOVY_CHECK_FOR_COMPILER_MISMATCH, true);
        }
        return false;
    }

    @Override
    public void buildStarting(BuildContext[] files, boolean isBatch) {
        if (files == null || files.length == 0) return;
        IProject project = files[0].getFile().getProject();
        SpecifiedVersion projectLevel = CompilerUtils.getCompilerLevel(project);
        try {
            boolean compilerMatch = CompilerUtils.projectVersionMatchesWorkspaceVersion(projectLevel);
            IMarker[] markers = project.findMarkers(COMPILER_MISMATCH_PROBLEM, true, IResource.DEPTH_ZERO);
            if (compilerMatch) {
                for (IMarker marker : markers) {
                    marker.delete();
                }
            } else if (markers.length == 0) {
                CompilerUtils.addCompilerMismatchError(project, projectLevel);
            }
        } catch (CoreException e) {
            GroovyCore.logException("Error creating/deleting marker(s)", e);
        }
    }

    @Override
    public void cleanStarting(IJavaProject javaProject) {
        IProject project = javaProject.getProject();
        try {
            IMarker[] markers = project.findMarkers(COMPILER_MISMATCH_PROBLEM, true, IResource.DEPTH_ZERO);
            for (IMarker marker : markers) {
                marker.delete();
            }
        } catch (CoreException e) {
            GroovyCore.logException("Error deleting markers", e);
        }
    }

    @Override
    public void buildFinished(IJavaProject javaProject) {
        // if the project does not already have a compiler level set, infer it
        // from the classpath but only if there was a clean build
        IProject project = javaProject.getProject();
        try {
            SpecifiedVersion projectLevel = CompilerUtils.getCompilerLevel(project);
            if (projectLevel == SpecifiedVersion.DONT_CARE) {
                return; // all checks related to compiler levels disabled
            }
            // project is unspecified; try to find the groovy version on the classpath
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
                                // first one found now; just remember it
                                found1 = inferredProjectLevel;
                            } else if (found2 == null) { // only found 1 version so far
                                if (inferredProjectLevel == found1) {
                                    // same, so nothing new
                                } else {
                                    found2 = inferredProjectLevel;
                                    CompilerUtils.addMultipleCompilersOnClasspathError(project, found1, found2);
                                }
                            }
                        }
                    }
                }
                if (found1 != null && found2 == null) {
                    // only set compiler level if there's no ambiguity about what to set it to
                    CompilerUtils.setCompilerLevel(project, found1);
                }
            }
        } catch (CoreException e) {
            GroovyCore.logException("Exception thrown while inferring project " + project.getName() + "'s groovy compiler level.", e);
        }
    }
}
