/*******************************************************************************
 * Copyright (c) 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg        - Initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.m2eclipse;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.maven.ide.eclipse.jdt.IClasspathDescriptor;
import org.maven.ide.eclipse.jdt.IJavaProjectConfigurator;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;

public class GroovyProjectConfigurator extends AbstractProjectConfigurator
        implements IJavaProjectConfigurator {

    @Override
    public void configure(ProjectConfigurationRequest request,
            IProgressMonitor monitor) throws CoreException {
        MavenProject mavenProject = request.getMavenProject();
        IProject project = request.getProject();
        if (isGroovyProject(mavenProject)) {
            if (!project.hasNature(GroovyNature.GROOVY_NATURE)) {
                GroovyRuntime.addGroovyNature(project);
            }
        } else {
            GroovyRuntime.removeGroovyNature(project);
        }

    }

    public void configureClasspath(IMavenProjectFacade facade,
            IClasspathDescriptor classpath, IProgressMonitor monitor)
            throws CoreException {
        if (isGroovyProject(facade.getMavenProject())) {
            // add source folders
            IJavaProject javaProject = JavaCore.create(facade.getProject());
            IPath projectPath = facade.getFullPath();

            IPath sourcePath = projectPath.append("src/main/groovy"); //$NON-NLS-1$
            IPath sourceOutPath = projectPath.append("target/classes"); //$NON-NLS-1$
            if (!hasEntry(javaProject, sourcePath)) {
                GroovyRuntime.addClassPathEntry(javaProject, JavaCore.newSourceEntry(sourcePath, new Path[0], sourceOutPath));
            }

            IPath testPath = projectPath.append("src/test/groovy"); //$NON-NLS-1$
            IPath testOutPath = projectPath.append("target/test-classes"); //$NON-NLS-1$
            if (!hasEntry(javaProject, testPath)) {
                GroovyRuntime.addClassPathEntry(javaProject, JavaCore.newSourceEntry(testPath, new Path[0], testOutPath));
            }
        }
    }

    private boolean hasEntry(IJavaProject javaProject, IPath path) throws JavaModelException {
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (IClasspathEntry entry : entries) {
            if (entry.getPath().equals(path)) {
                return true;
            }
        }
        return false;
    }
    
    public void configureRawClasspath(ProjectConfigurationRequest request,
            IClasspathDescriptor classpath, IProgressMonitor monitor)
            throws CoreException {
    }

    private boolean isGroovyProject(MavenProject mavenProject) {
        Plugin plugin = getGMavenPlugin(mavenProject);

        if (plugin != null && plugin.getExecutions() != null
                && !plugin.getExecutions().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private Plugin getGMavenPlugin(MavenProject mavenProject) {
        return mavenProject.getPlugin("org.codehaus.groovy.maven:gmaven-plugin"); //$NON-NLS-1$
    }

}
