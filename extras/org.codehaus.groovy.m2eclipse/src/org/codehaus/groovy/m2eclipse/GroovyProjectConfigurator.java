/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.groovy.m2eclipse;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.eclipse.m2e.jdt.internal.AbstractJavaProjectConfigurator;

public class GroovyProjectConfigurator extends AbstractJavaProjectConfigurator implements IJavaProjectConfigurator {

    // copy from org.codehaus.jdt.groovy.model.GroovyNature
    private static final String GROOVY_NATURE = "org.eclipse.jdt.groovy.core.groovyNature";

    // copy from org.codehaus.groovy.eclipse.core.model.GroovyRuntime
    private static final IPath DSLD_CONTAINER_ID = new Path("GROOVY_DSL_SUPPORT");

    @Override
    public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
        super.configure(request, monitor); // drives calls to configureClasspath and configureRawClasspath

        ProjectSourceType sourceType = ProjectSourceType.getSourceType(request.getMavenProjectFacade());
        if (sourceType != null) {
            addNature(request.getProject(), GROOVY_NATURE, monitor);
        } else {
            IProjectDescription description = request.getProject().getDescription();
            if (description.hasNature(GROOVY_NATURE)) {
                description.setNatureIds(Arrays.stream(description.getNatureIds())
                    .filter(n -> !n.equals(GROOVY_NATURE)).toArray(String[]::new));
                request.getProject().setDescription(description, IResource.KEEP_HISTORY, null);
            }
        }

        IEclipsePreferences preferences = new ProjectScope(request.getProject()).getNode("org.eclipse.jdt.groovy.core");
        if (sourceType != null) {
            String filters = getScriptFilters(preferences, request.getMavenProjectFacade());
            if (!filters.isEmpty()) {
                preferences.put("groovy.script.filters", filters);
                preferences.putBoolean("org.codehaus.groovy.eclipse.preferences.compiler.project", true);
                try {
                    preferences.flush();
                } catch (Exception ex) {
                  //org.slf4j.LoggerFactory.getLogger(getClass()).error(ex);
                }
            }
        } else {
            try {
                preferences.removeNode();
            } catch (Exception ex) {
              //org.slf4j.LoggerFactory.getLogger(getClass()).error(ex);
            }
        }
    }

    @Override
    public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor) {
        // nothing to add to the Maven Dependencies container
    }

    @Override
    public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath, IProgressMonitor monitor) {
        ProjectSourceType sourceType = ProjectSourceType.getSourceType(request.getMavenProjectFacade());
        if (sourceType != null) {
            if (isAbsent(classpath, DSLD_CONTAINER_ID) && isAddDslSupport()) {
                classpath.addEntry(JavaCore.newContainerEntry(
                    DSLD_CONTAINER_ID,
                    null, // access rules
                    new IClasspathAttribute[] {JavaCore.newClasspathAttribute("maven.pomderived", "true")},
                    false // exported
                ));
            }

            IProject project = request.getProject();
            IPath mainOutput = getFolder(project, request.getMavenProject().getBuild().getOutputDirectory()).getFullPath();
            IPath testOutput = getFolder(project, request.getMavenProject().getBuild().getTestOutputDirectory()).getFullPath();

            IFolder srcMainGroovy = project.getFolder("src/main/groovy");
            if (srcMainGroovy.exists() && (sourceType == ProjectSourceType.MAIN || sourceType == ProjectSourceType.BOTH)) {
                if (isAbsent(classpath, srcMainGroovy.getFullPath())) {
                    classpath.addSourceEntry(srcMainGroovy.getFullPath(), mainOutput, /*generated:*/ true);
                }
            }

            IFolder srcTestGroovy = project.getFolder("src/test/groovy");
            if (srcTestGroovy.exists() && (sourceType == ProjectSourceType.TEST || sourceType == ProjectSourceType.BOTH)) {
                if (isAbsent(classpath, srcTestGroovy.getFullPath())) {
                    classpath.addSourceEntry(srcTestGroovy.getFullPath(), testOutput, /*generated:*/ true).setClasspathAttribute("test", "true");
                }
            }

            classpath.removeEntry(project.getFullPath().append("target/generated-sources/groovy-stubs/main"));
        }
    }

    @Override
    protected void addJavaProjectOptions(Map<String, String> options, ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
        String configScript = null;

        for (MojoExecution me : getCompilerMojoExecutions(request, monitor)) {
            Map<String, String> m = maven.getMojoParameterValue(request.getMavenProject(), me, "compilerArguments", Map.class, monitor);
            if (m != null && m.get("configScript") != null) {
                configScript = m.get("configScript").trim();
            } else {
                String s = maven.getMojoParameterValue(request.getMavenProject(), me, "compilerArgument", String.class, monitor);
                if (s != null && s.contains("configScript")) {
                    String[] tokens = s.split("=");
                    if (tokens.length == 2 && tokens[0].trim().matches("-?configScript")) {
                        configScript = tokens[1].trim();
                    }
                }
            }
        }

        // see org.eclipse.jdt.internal.compiler.impl.CompilerOptions.OPTIONG_GroovyCompilerConfigScript
        options.put("org.eclipse.jdt.core.compiler.groovy.groovyCompilerConfigScript", configScript);

        super.addJavaProjectOptions(options, request, monitor);
    }

    //--------------------------------------------------------------------------

    private static String getScriptFilters(IEclipsePreferences preferences, IMavenProjectFacade facade) {
        Set<String> scriptFilters = new java.util.TreeSet<>();

        String[] tokens = StringUtils.split(preferences.get("groovy.script.filters", ""), ",");
        for (int i = 0, n = tokens.length; i < n; i += 1) {
            scriptFilters.add(tokens[i++] + "," + (i < n ? tokens[i] : "y"));
        }
        for (IPath path : facade.getResourceLocations()) {
            if (facade.getProject().exists(path))
                scriptFilters.add(path.toString() + "/**/*.groovy,y");
        }
        for (IPath path : facade.getTestResourceLocations()) {
            if (facade.getProject().exists(path))
                scriptFilters.add(path.toString() + "/**/*.groovy,y");
        }

        return StringUtils.join(scriptFilters.iterator(), ",");
    }

    protected static boolean isAbsent(IClasspathDescriptor classpath, IPath path) {
        if (classpath.containsPath(path)) {
            classpath.touchEntry(path);
            return false;
        }
        return true;
    }

    protected static boolean isAddDslSupport() {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("org.codehaus.groovy.eclipse.dsl");
        return prefs.getBoolean("org.codehaus.groovy.eclipse.dsl.auto.add.support", true);
    }
}
