/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

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
 * @author Andrew Eisenberg
 * @created Jul 21, 2009
 *
 *          Converts a legacty Groovy 1.x project into a 2.x project <br>
 * <br>
 *          Steps:<br>
 *          1. remove old groovy nature<br>
 *          2. add new groovy nature<br>
 *          3. remove old groovy builder<br>
 *          4. ensure java builder exists<br>
 *
 *          As of Greclipse 2.7.0, This is no longer run on startup. But, this
 *          funcionality is
 *          available from the preferences page.
 */
public class ConvertLegacyProject {

    public static final String OLD_NATURE = "org.codehaus.groovy.eclipse.groovyNature";
    public static final String OLD_BUILDER = "org.codehaus.groovy.eclipse.groovyBuilder";
    public static final String GROOVY_NATURE = "org.eclipse.jdt.groovy.core.groovyNature"; //$NON-NLS-1$

    public void convertProjects(IProject[] projects) {
        for (IProject project : projects) {
            convertProject(project);
        }
    }


    public void convertProject(IProject project) {
        try {
            IProjectDescription desc = project.getDescription();

            String[] natures = desc.getNatureIds();
            List<String> newNatures = new LinkedList<String>();
            for (String nature : natures) {
                if (!nature.equals(OLD_NATURE) && !nature.equals(GROOVY_NATURE)) {
                    newNatures.add(nature);
                }
            }
            newNatures.add(0, GROOVY_NATURE);

            desc.setNatureIds(newNatures.toArray(new String[newNatures.size()]));

            List<ICommand> builders = Arrays.asList(desc.getBuildSpec());
            List<ICommand> newBuilders = new ArrayList<ICommand>(builders.size());
            boolean javaBuilderFound = false;
            for (ICommand builder : builders) {
                if (! builder.getBuilderName().equals(OLD_BUILDER)) {
                    newBuilders.add(builder);
                    if (builder.getBuilderName().equals(JavaCore.BUILDER_ID)) {
                        javaBuilderFound = true;
                    }
                }
            }
            if (! javaBuilderFound) {
                ICommand newCommand = new BuildCommand();
                newCommand.setBuilderName(JavaCore.BUILDER_ID);
                newBuilders.add(newCommand);
            }
            desc.setBuildSpec(newBuilders.toArray(new ICommand[newBuilders.size()]));

            project.setDescription(desc, null);
        } catch (CoreException e) {
            GroovyCore.logException("Exception thrown when converting for legacy project " + project.getName(), e);
        }
    }

    public IProject[] getAllOldProjects() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> legacyProjects = new ArrayList<IProject>();
        for (IProject project : projects) {
            try {
                if (project.isAccessible() && project.hasNature(OLD_NATURE)) {
                    legacyProjects.add(project);
                }
            } catch (CoreException e) {
                GroovyCore.logException("Exception thrown when checking for legacy projects", e);
            }
        }
        return legacyProjects.toArray(new IProject[0]);
    }
}