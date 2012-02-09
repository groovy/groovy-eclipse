/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.classpath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.dsl.DSLPreferences;
import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.SetClasspathOperation;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class is called on startup as well as whenever there is a project added to the workspace.
 * If the option is set, then the DSL support will be secretly added to all groovy projects
 * @author andrew
 * @created May 27, 2011
 */
public class AutoAddContainerSupport implements IResourceChangeListener {
    
    /**
     * 
     * @author andrew
     * @created May 27, 2011
     */
    private final class AddDSLSupportJob extends Job {
        private final String projectName;

        private final IJavaProject project;

        private AddDSLSupportJob(String name, String projectName, IJavaProject project) {
            super(name);
            this.projectName = projectName;
            this.project = project;
        }

        @Override
        public IStatus run(IProgressMonitor monitor) {
            try {
                GroovyRuntime.addLibraryToClasspath(project, GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, false);
                // here, remember that we have added this project
                alreadyAddedProjects.add(projectName);
                return Status.OK_STATUS;
            } catch (JavaModelException e) {
                GroovyDSLCoreActivator.logException("Problem auto-adding DSL support to " + projectName, e);
                return e.getStatus();
            }
        }
    }


    private final IPreferenceStore store = GroovyDSLCoreActivator.getDefault().getPreferenceStore();
    
    private final Set<String> alreadyAddedProjects;
   
    public AutoAddContainerSupport() {
        alreadyAddedProjects = new HashSet<String>();
        String toIgnore = store.getString(DSLPreferencesInitializer.PROJECTS_TO_IGNORE);
        if (toIgnore != null) {
            String[] split = toIgnore.split(",");
            for (String projName : split) {
                projName = projName.trim();
                if (projName.length() > 0 && ResourcesPlugin.getWorkspace().getRoot().getProject(projName).exists()) {
                    alreadyAddedProjects.add(projName);
                }
            }
        }
        
    }
    
    private boolean shouldAddSupport() {
        return store.getBoolean(DSLPreferences.AUTO_ADD_DSL_SUPPORT) || store.getBoolean(DSLPreferences.DISABLED_SCRIPTS);
    }
    
    // will add container if it doesn't already exist
    private void addContainer(final IJavaProject project) {
        final String projectName = project.getElementName();
        AddDSLSupportJob runnable = new AddDSLSupportJob("Add DSL Support", projectName, project);
        runnable.setPriority(Job.BUILD);
        runnable.setSystem(true);
        //Next line is very important! Otherwise => race condition with GrailsProjectVersionFixer!
        runnable.setRule(getSetClassPathSchedulingRule(project));
        runnable.schedule();
    }

    /**
     * Same scheduling rule as {@link SetClasspathOperation}
     */
    private ISchedulingRule getSetClassPathSchedulingRule(IJavaProject project) {
        //copied from SetClassPathOperation. Rules must match (or be wider than this rule or the setClassPathOperation will fail)
        IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
        return new MultiRule(new ISchedulingRule[] {
                // use project modification rule as this is needed to create the .classpath file if it doesn't exist yet, or to update project references
                ruleFactory.modifyRule(project.getProject()),
                
                // and external project modification rule in case the external folders are modified
                ruleFactory.modifyRule(JavaModelManager.getExternalManager().getExternalFoldersProject()),
            });
    }

    public void addContainerToAll() {
        if (!shouldAddSupport()) {
            return;
        }
        
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : allProjects) {
            if (!alreadyAddedProjects.contains(project.getName()) && GroovyNature.hasGroovyNature(project)) {
                addContainer(JavaCore.create(project));
            }
        }
    }
    

    public void resourceChanged(IResourceChangeEvent event) {
        if (!shouldAddSupport()) {
            return;
        }
        
        // look for projects that are becoming groovy projects, or projects that are being created
        // we can approximate this by looking for changes in .project files
        IResourceDelta delta = event.getDelta();
        if (delta != null) {
            List<IProject> projects = new ArrayList<IProject>();
            if (delta.getAffectedChildren().length > 0) {
                IResourceDelta[] children = delta.getAffectedChildren();
                for (IResourceDelta child : children) {
                    if (child.getResource() instanceof IProject) {
                        if (child.getAffectedChildren().length == 0) {
                            projects.add((IProject) child.getResource());
                        } else {
                            for (IResourceDelta childDelta : child.getAffectedChildren()) {
                                IResource r = childDelta.getResource();
                                if (r instanceof IFile && r.getName().equals(".project")) {
                                    projects.add((IProject) child.getResource());
                                }
                            }
                        }
                    }
                }
            }
            
            for (IProject project : projects) {
                if (!alreadyAddedProjects.contains(project.getName()) && GroovyNature.hasGroovyNature(project)) {
                    addContainer(JavaCore.create(project));
                }
            }
        }
    }
    
    
    public void dispose() {
        StringBuilder sb = new StringBuilder();
        for (String projName : alreadyAddedProjects) {
            sb.append(projName);
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.replace(sb.length()-1, sb.length(), "");
        }
        store.setValue(DSLPreferencesInitializer.PROJECTS_TO_IGNORE, sb.toString());
        if (store instanceof IPersistentPreferenceStore) {
            try {
                ((IPersistentPreferenceStore) store).save();
            } catch (IOException e) {
                GroovyDSLCoreActivator.logException(e);
            }
        }
    }
    
    public void ignoreProject(IProject project) {
        alreadyAddedProjects.add(project.getName());
    }
}
