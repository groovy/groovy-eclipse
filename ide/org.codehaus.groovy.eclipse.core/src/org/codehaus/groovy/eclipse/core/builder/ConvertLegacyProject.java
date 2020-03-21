/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

/**
 * Converts a legacty Groovy 1.x project into a 2.x project.
 * <br>
 * Steps:<br>
 * 1. remove old groovy nature<br>
 * 2. add new groovy nature<br>
 * 3. remove old groovy builder<br>
 * 4. ensure java builder exists<br>
 *
 * This is no longer run on startup. But it is available from the preferences page.
 */
public class ConvertLegacyProject {

    public static final String OLD_NATURE = "org.codehaus.groovy.eclipse.groovyNature";
    public static final String OLD_BUILDER = "org.codehaus.groovy.eclipse.groovyBuilder";
    public static final String GROOVY_NATURE = "org.eclipse.jdt.groovy.core.groovyNature"; //$NON-NLS-1$

    public void convertProjects(final IProject[] projects) {
        for (IProject project : projects) {
            convertProject(project);
        }
    }

    public void convertProject(final IProject project) {
        try {
            IProjectDescription desc = project.getDescription();

            String[] natures = desc.getNatureIds();
            List<String> newNatures = new LinkedList<>();
            for (String nature : natures) {
                if (!nature.equals(OLD_NATURE) && !nature.equals(GROOVY_NATURE)) {
                    newNatures.add(nature);
                }
            }
            newNatures.add(0, GROOVY_NATURE);

            desc.setNatureIds(newNatures.toArray(new String[newNatures.size()]));

            List<ICommand> builders = Arrays.asList(desc.getBuildSpec());
            List<ICommand> newBuilders = new ArrayList<>(builders.size());
            boolean javaBuilderFound = false;
            for (ICommand builder : builders) {
                if (!builder.getBuilderName().equals(OLD_BUILDER)) {
                    newBuilders.add(builder);
                    if (builder.getBuilderName().equals(JavaCore.BUILDER_ID)) {
                        javaBuilderFound = true;
                    }
                }
            }
            if (!javaBuilderFound) {
                ICommand newCommand = new BuildCommand();
                newCommand.setBuilderName(JavaCore.BUILDER_ID);
                newBuilders.add(newCommand);
            }
            desc.setBuildSpec(newBuilders.toArray(new ICommand[newBuilders.size()]));

            project.setDescription(desc, null);
        } catch (CoreException e) {
            GroovyCore.logException("Failed to convert legacy project " + project.getName(), e);
        }
    }

    public IProject[] getAllOldProjects() {
        return Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
            .filter(project -> {
                try {
                    return (project.isAccessible() && project.hasNature(OLD_NATURE));
                } catch (CoreException e) {
                    GroovyCore.logException("Failed to check legacy status for project " + project.getName(), e);
                    return false;
                }
            })
            .toArray(IProject[]::new);
    }
}
