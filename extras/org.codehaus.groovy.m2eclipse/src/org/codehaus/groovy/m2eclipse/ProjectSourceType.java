/*
 * Copyright 2009-2023 the original author or authors.
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

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

enum ProjectSourceType {
    MAIN, TEST, BOTH, NONE;

    /**
     * Determines if maven project uses src/main/groovy and/or src/test/groovy.
     */
    static ProjectSourceType getSourceType(IMavenProjectFacade facade) {
        MavenProject mavenProject = facade.getMavenProject();
        // look to see if there is the maven-compiler-plugin
        // with a compilerId of the groovy eclipse compiler
        if (compilerPluginUsesGroovyEclipseAdapter(mavenProject, "org.apache.maven.plugins", "maven-compiler-plugin")) {
            return getSourceTypeInGECProject(facade.getProject());
        }

        // For eclipse plugins written in groovy :
        // look to see if there is the tycho-compiler-plugin
        // with a compilerId of the groovy eclipse compiler
        // /!\ Requires m2e-tycho >= 0.6.0.201210231015
        if (compilerPluginUsesGroovyEclipseAdapter(mavenProject, "org.eclipse.tycho", "tycho-compiler-plugin")) {
            //Assume configuration is controlled in the MANIFEST.MF, hence returning SourceType.NONE here
            return NONE;
        }

        // check for GMaven or GMavenPlus
        Plugin plugin = mavenProject.getPlugin("org.codehaus.gmaven:gmaven-plugin");
        if (plugin == null) {
            plugin = mavenProject.getPlugin("org.codehaus.gmavenplus:gmavenplus-plugin");
        }
        if (plugin != null) {
            return getSourceTypeInGMavenProject(plugin);
        }

        // not a groovy project
        return null;
    }

    private static ProjectSourceType getSourceTypeInGECProject(IProject project) {
        boolean srcMainGroovy = project.getFolder("src/main/groovy").exists();
        boolean srcTestGroovy = project.getFolder("src/test/groovy").exists();
        if (srcMainGroovy) {
            if (srcTestGroovy) {
                return BOTH;
            } else {
                return MAIN;
            }
        } else if (srcTestGroovy) {
            return TEST;
        } else {
            return NONE;
        }
    }

    private static ProjectSourceType getSourceTypeInGMavenProject(Plugin plugin) {
        ProjectSourceType result = NONE;
        if (plugin != null && plugin.getExecutions() != null && !plugin.getExecutions().isEmpty()) {
            for (PluginExecution execution : plugin.getExecutions()) {
                List<String> goals = execution.getGoals();
                if (goals.contains("compile")) {
                    switch (result) {
                    case NONE:
                        result = MAIN;
                        break;
                    case TEST:
                        result = BOTH;
                        break;
                    }
                }
                if (goals.contains("compileTests") || goals.contains("testCompile")) {
                    switch (result) {
                    case NONE:
                        result = TEST;
                        break;
                    case MAIN:
                        result = BOTH;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static boolean compilerPluginUsesGroovyEclipseAdapter(MavenProject mavenProject, String pluginGroupId, String pluginArtifactId) {
        for (Plugin buildPlugin : mavenProject.getBuildPlugins()) {
            if (pluginArtifactId.equals(buildPlugin.getArtifactId()) && pluginGroupId.equals(buildPlugin.getGroupId())) {
                for (Dependency dependency : buildPlugin.getDependencies()) {
                    if ("groovy-eclipse-compiler".equals(dependency.getArtifactId()) && "org.codehaus.groovy".equals(dependency.getGroupId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
