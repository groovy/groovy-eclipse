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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Andrew Eisenberg
 * @created Jul 21, 2009
 * 
 * Converts a legacty Groovy 1.x project into a 2.x project
 * <br><br>
 * Steps:<br>
 * 1. remove old groovy nature<br>
 * 2. add new groovy nature<br>
 * 3. remove old groovy builder<br>
 * 4. ensure java builder exists<br>
 */
public class ConvertLegacyProject {

    public static final String OLD_NATURE = "org.codehaus.groovy.eclipse.groovyNature"
    public static final String OLD_BUILDER = "org.codehaus.groovy.eclipse.groovyBuilder"
    public static final String GROOVY_NATURE = "org.eclipse.jdt.groovy.core.groovyNature"; //$NON-NLS-1$

    def convertProjects(projects) {
        projects.each( { it -> if (it.isAccessible()) {
            convertProject(it) 
        }} )
    }
    
    
    def convertProject(IProject project) {
        IProjectDescription desc = project.getDescription()
        
        def natures = desc.getNatureIds()
        def newNatures = natures.findAll( { it -> ! it.equals(OLD_NATURE) } ).asList()
        ((List) newNatures).add(0, GROOVY_NATURE)
        newNatures = newNatures.unique()
        desc.setNatureIds(newNatures.toArray(new String[newNatures.size()]))
        
        def builders = desc.getBuildSpec()
        def newBuilders = builders.findAll({ ICommand it -> ! it.getBuilderName().equals(OLD_BUILDER) } ).asList()
        
        if (! builders.find( { it.getBuilderName().equals(JavaCore.BUILDER_ID) } )) {
            def newCommand = new BuildCommand()
            newCommand.setBuilderName(JavaCore.BUILDER_ID)
            newBuilders.add(newCommand)
        }
        desc.setBuildSpec(newBuilders.toArray(new ICommand[newBuilders.size()]))

        project.setDescription(desc, null)
    }
    
    public IProject[] getAllOldProjects() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        def legacyProjects = projects.findAll { IProject it -> it.isAccessible() && it.hasNature(OLD_NATURE) }
        legacyProjects.toArray new IProject[0]
    }
}