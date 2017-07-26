/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.m2eclipse;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.eclipse.m2e.jdt.internal.AbstractJavaProjectConfigurator;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class GroovyProjectConfigurator extends AbstractJavaProjectConfigurator implements IJavaProjectConfigurator {

    public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor)
            throws CoreException {
        super.configure(request, monitor);
        IProject project = request.getProject();
        if (ProjectSourceType.getSourceType(request.getMavenProjectFacade()) != null) {
            if (!project.hasNature(GroovyNature.GROOVY_NATURE)) {
                GroovyRuntime.addGroovyNature(project);
            }
        } else {
            GroovyRuntime.removeGroovyNature(project);
        }
    }

    public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor)
            throws CoreException {
        ProjectSourceType sourceType = ProjectSourceType.getSourceType(facade);
        if (sourceType != null) {
            IJavaProject javaProject = JavaCore.create(facade.getProject());

            // add DSL support classpath container
            if (!GroovyRuntime.hasClasspathContainer(javaProject, GroovyRuntime.DSLD_CONTAINER_ID) &&
                    new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.codehaus.groovy.eclipse.dsl").getBoolean("org.codehaus.groovy.eclipse.dsl.auto.add.support")) {
                GroovyRuntime.addClassPathEntry(javaProject, newContainerEntry(GroovyRuntime.DSLD_CONTAINER_ID));
            }

            // add source folders
            IPath projectPath = facade.getFullPath();

            Set<IPath> toRemove = new HashSet<IPath>();

            IFolder testFolder = javaProject.getProject().getFolder("src/test/groovy");
            if (testFolder.exists() && (sourceType == ProjectSourceType.TEST || sourceType == ProjectSourceType.BOTH)) {
                if (!hasEntry(javaProject, testFolder.getFullPath())) {
                    GroovyRuntime.addClassPathEntryToFront(javaProject, newSourceFolderEntry(testFolder.getFullPath(), projectPath.append("target/test-classes")));
                }
            } else {
                toRemove.add(testFolder.getFullPath());
            }

            IFolder sourceFolder = javaProject.getProject().getFolder("src/main/groovy");
            if (sourceFolder.exists() && (sourceType == ProjectSourceType.MAIN || sourceType == ProjectSourceType.BOTH)) {
                if (!hasEntry(javaProject, sourceFolder.getFullPath())) {
                    GroovyRuntime.addClassPathEntryToFront(javaProject, newSourceFolderEntry(sourceFolder.getFullPath(), projectPath.append("target/classes")));
                }
            } else {
                toRemove.add(sourceFolder.getFullPath());
            }

            // We should remove the generated sources from the classpath if it exists
            toRemove.add(projectPath.append("target/generated-sources/groovy-stubs/main"));

            // Also remove stuff that was there already which we control, but that shouldn't be there according to the gmaven conf
            IClasspathEntry[] allEntries = javaProject.getRawClasspath();
            for (IClasspathEntry entry : allEntries) {
                if (toRemove.contains(entry.getPath())) {
                    GroovyRuntime.removeClassPathEntry(javaProject, entry);
                }
            }
        }
    }

    public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath, IProgressMonitor monitor)
            throws CoreException {
    }

    private static boolean hasEntry(IJavaProject javaProject, IPath path)
            throws JavaModelException {
        for (IClasspathEntry entry : javaProject.getRawClasspath()) {
            if (entry.getPath().equals(path)) {
                return true;
            }
        }
        return false;
    }

    private static IClasspathEntry newContainerEntry(IPath path) {
        return JavaCore.newContainerEntry(
            path,
            null, // access rules
            new IClasspathAttribute[] {
                JavaCore.newClasspathAttribute("maven.pomderived", "true")
            },
            false // exported
        );
    }

    private static IClasspathEntry newSourceFolderEntry(IPath sourcePath, IPath targetPath) {
        return JavaCore.newSourceEntry(
            sourcePath,
            null, // inclusions
            null, // exclusions
            targetPath,
            new IClasspathAttribute[] {
                JavaCore.newClasspathAttribute("optional", "true"),
                JavaCore.newClasspathAttribute("maven.pomderived", "true")
            }
        );
    }
}
