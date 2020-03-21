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
package org.codehaus.groovy.eclipse.dsl.classpath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class is called on startup as well as whenever there is a project added to the workspace.
 * If the option is set, then the DSL support will be secretly added to all groovy projects
 */
public class AutoAddContainerSupport implements IResourceChangeListener {

    private final IPreferenceStore store = GroovyDSLCoreActivator.getDefault().getPreferenceStore();

    private final Set<String> alreadyAddedProjects;

    public AutoAddContainerSupport() {
        alreadyAddedProjects = Arrays.stream(store.getString(DSLPreferencesInitializer.PROJECTS_TO_IGNORE).split(","))
            .filter(projName -> !projName.isEmpty() && ResourcesPlugin.getWorkspace().getRoot().getProject(projName).exists())
            .collect(Collectors.toSet());
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        // look for projects that are becoming groovy projects, or projects that are being created
        // we can approximate this by looking for changes in .project files
        IResourceDelta delta = event.getDelta();
        if (delta != null && delta.getAffectedChildren().length > 0) {
            List<IProject> projects = new ArrayList<>();
            for (IResourceDelta child : delta.getAffectedChildren()) {
                if (child.getResource() instanceof IProject) {
                    if (child.getAffectedChildren().length == 0) {
                        projects.add((IProject) child.getResource());
                    } else {
                        for (IResourceDelta grandchild : child.getAffectedChildren()) {
                            IResource r = grandchild.getResource();
                            if (r instanceof IFile && r.getName().equals(".project")) {
                                projects.add((IProject) child.getResource());
                            }
                        }
                    }
                }
            }
            addContainer(projects.toArray(new IProject[projects.size()]));
        }
    }

    public void addContainer(final IProject[] projects) {
        if (!store.getBoolean(DSLPreferences.AUTO_ADD_DSL_SUPPORT) || store.getBoolean(DSLPreferences.DISABLED_SCRIPTS)) {
            return;
        }
        for (IProject project : projects) {
            if (alreadyAddedProjects.contains(project.getName()) || !GroovyNature.hasGroovyNature(project)) {
                continue;
            }

            IJavaProject javaProject = JavaCore.create(project);

            AddDSLSupportJob job = new AddDSLSupportJob(javaProject);
            // prevent race condition with GrailsProjectVersionFixer
            job.setRule(getSetClassPathSchedulingRule(javaProject));
            job.setPriority(Job.BUILD);
            job.setSystem(true);
            job.schedule();
        }
    }

    /**
     * Same scheduling rule as {@link SetClasspathOperation}
     */
    private ISchedulingRule getSetClassPathSchedulingRule(final IJavaProject javaProject) {
        // copied from SetClassPathOperation. Rules must match (or be wider than this rule or the setClassPathOperation will fail)
        IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
        return new MultiRule(new ISchedulingRule[] {
            // use project modification rule as this is needed to create the .classpath file if it doesn't exist yet, or to update project references
            ruleFactory.modifyRule(javaProject.getProject()),

            // and external project modification rule in case the external folders are modified
            ruleFactory.modifyRule(JavaModelManager.getExternalManager().getExternalFoldersProject()),
        });
    }

    public void dispose() {
        store.setValue(DSLPreferencesInitializer.PROJECTS_TO_IGNORE, alreadyAddedProjects.stream().collect(Collectors.joining(",")));
        if (store instanceof IPersistentPreferenceStore) {
            try {
                ((IPersistentPreferenceStore) store).save();
            } catch (IOException e) {
                GroovyDSLCoreActivator.logException(e);
            }
        }
    }

    public void ignoreProject(final IProject project) {
        alreadyAddedProjects.add(project.getName());
    }

    public void unignoreProject(final IProject project) {
        alreadyAddedProjects.remove(project.getName());
    }

    public void unignoreAllProjects() {
        alreadyAddedProjects.clear();
    }

    private class AddDSLSupportJob extends Job {

        private final IJavaProject javaProject;

        private AddDSLSupportJob(final IJavaProject javaProject) {
            super("Add DSL Support");
            this.javaProject = javaProject;
        }

        @Override
        public IStatus run(final IProgressMonitor monitor) {
            try {
                if (!GroovyRuntime.findClasspathEntry(javaProject, cpe -> cpe.getPath().equals(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID)).isPresent()) {
                    GroovyRuntime.appendClasspathEntry(javaProject, JavaCore.newContainerEntry(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID));
                }
                // here, remember that we have added this project
                alreadyAddedProjects.add(javaProject.getElementName());
                return Status.OK_STATUS;
            } catch (JavaModelException e) {
                GroovyDSLCoreActivator.logException("Problem auto-adding DSL support to " + javaProject.getElementName(), e);
                return e.getStatus();
            }
        }
    }
}
