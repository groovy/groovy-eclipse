/*******************************************************************************
 * Copyright (c) 2010, 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg        - Initial API and implementation
 *     Justin Edelson          - patch from GRECLIPSE-857
 *     Benson Margulies        - patch from GRECLIPSE-1068 (update for compatibility with m2e 0.13)
 *******************************************************************************/
package org.codehaus.groovy.m2eclipse;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.eclipse.m2e.jdt.internal.AbstractJavaProjectConfigurator;

public class GroovyProjectConfigurator extends AbstractJavaProjectConfigurator
        implements IJavaProjectConfigurator {

    @Override
    public void configure(ProjectConfigurationRequest request,
            IProgressMonitor monitor) throws CoreException {
        super.configure(request, monitor);
        MavenProject mavenProject = request.getMavenProject();
        IProject project = request.getProject();
        if (getSourceType(mavenProject) != null) {
            if (!project.hasNature(GroovyNature.GROOVY_NATURE)) {
                if (!project.hasNature(JavaCore.NATURE_ID)) {
                    addJavaNature(project);
                }
                GroovyRuntime.addGroovyNature(project);
            }
            
            // add dsl support if not already there
            IJavaProject javaProject = JavaCore.create(project);
            if (!GroovyRuntime.hasClasspathContainer(javaProject, GroovyRuntime.DSLD_CONTAINER_ID)) {
                GroovyRuntime.addLibraryToClasspath(javaProject, GroovyRuntime.DSLD_CONTAINER_ID);
            }
        } else {
            GroovyRuntime.removeGroovyNature(project);
        }
    }

    public void configureClasspath(IMavenProjectFacade facade,
            IClasspathDescriptor classpath, IProgressMonitor monitor)
            throws CoreException {
    	SourceType sourceType = getSourceType(facade.getMavenProject());
        if (sourceType != null) {
            // add source folders
            IJavaProject javaProject = JavaCore.create(facade.getProject());
            IPath projectPath = facade.getFullPath();

            if (sourceType == SourceType.MAIN || sourceType == SourceType.BOTH) {
            	IPath sourcePath = projectPath.append("src/main/groovy"); //$NON-NLS-1$
            	IPath sourceOutPath = projectPath.append("target/classes"); //$NON-NLS-1$
            	if (!hasEntry(javaProject, sourcePath)) {
            		GroovyRuntime.addClassPathEntry(javaProject, JavaCore.newSourceEntry(sourcePath, new Path[0], sourceOutPath));
            	}
            }

            if (sourceType == SourceType.TEST || sourceType == SourceType.BOTH) {
	            IPath testPath = projectPath.append("src/test/groovy"); //$NON-NLS-1$
	            IPath testOutPath = projectPath.append("target/test-classes"); //$NON-NLS-1$
	            if (!hasEntry(javaProject, testPath)) {
	                GroovyRuntime.addClassPathEntry(javaProject, JavaCore.newSourceEntry(testPath, new Path[0], testOutPath));
	            }
            }
            
            // now remove the generated sources from the classpath if it exists
            IClasspathEntry[] allEntries = javaProject.getRawClasspath();
            for (IClasspathEntry entry : allEntries) {
                if (entry.getPath().equals(javaProject.getProject().getFolder("target/generated-sources/groovy-stubs/main").getFullPath())) {
                    GroovyRuntime.removeClassPathEntry(javaProject, entry);
                    break;
                }
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

    /**
     * Determine if this mavan project uses src/main/groovy and/or src/test/groovy.  Only 
     * applicable for gmaven.  With groovy-eclipse-compiler, this is configured by the build-helper-maven-plugin
     * @param mavenProject
     * @return
     */
    private SourceType getSourceType(MavenProject mavenProject) {
        Plugin plugin = getGMavenPlugin(mavenProject);
        SourceType result = null;
        if (plugin == null) {
            // look to see if there is the maven-compiler-plugin
            // with a compilerId of the groovy eclipse compiler
            if (compilerPluginUsesGroovyEclipseAdapter(mavenProject)) {
                return SourceType.NONE;
            }
        }
        
        if (plugin != null && plugin.getExecutions() != null
                && !plugin.getExecutions().isEmpty()) {
        	result = SourceType.NONE;
        	for (PluginExecution execution : plugin.getExecutions()) {
            	if (execution.getGoals().contains(COMPILE)) {
            		switch (result) {
            			case NONE:
            				result = SourceType.MAIN;
            				break;
            			case TEST:
            				result = SourceType.BOTH;
            				break;
            		}
               	}
            	if (execution.getGoals().contains(TEST_COMPILE)) {
            		switch (result) {
            			case NONE:
            				result = SourceType.TEST;
            				break;
            			case MAIN:
            				result = SourceType.BOTH;
            				break;
            		}
               	}
            }
        }
        
        return result;
    }

    private Plugin getGMavenPlugin(MavenProject mavenProject) {
        Plugin p = mavenProject.getPlugin("org.codehaus.gmaven:gmaven-plugin"); //$NON-NLS-1$
        if (p == null) {
            // try the old (pre-1.1) version of the plugin
            p = mavenProject.getPlugin("org.codehaus.groovy.maven:gmaven-plugin"); //$NON-NLS-1$
        }
        return p;
    }
    
    private boolean compilerPluginUsesGroovyEclipseAdapter(MavenProject mavenProject) {
        for (Plugin buildPlugin : mavenProject.getBuildPlugins()) {
            if ("maven-compiler-plugin".equals(buildPlugin.getArtifactId()) && "org.apache.maven.plugins".equals(buildPlugin.getGroupId())) {
                for (Dependency dependency : buildPlugin.getDependencies()) {
                    if ("groovy-eclipse-compiler".equals(dependency.getArtifactId()) && "org.codehaus.groovy".equals(dependency.getGroupId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static final String COMPILE = "compile";
    private static final String TEST_COMPILE = "testCompile";
    
    private enum SourceType {
    	MAIN, TEST, BOTH, NONE
    }
    
    public static void addJavaNature(final IProject project)
            throws CoreException {
        final IProjectDescription description = project.getDescription();
        final String[] ids = description.getNatureIds();

        // add groovy nature at the start so that its image will be shown
        final String[] newIds = new String[ids == null ? 1 : ids.length + 1];
        newIds[0] = JavaCore.NATURE_ID;
        if (ids != null) {
            for (int i = 1; i < newIds.length; i++) {
                newIds[i] = ids[i - 1];
            }
        }

        description.setNatureIds(newIds);
        project.setDescription(description, null);
    }

}
