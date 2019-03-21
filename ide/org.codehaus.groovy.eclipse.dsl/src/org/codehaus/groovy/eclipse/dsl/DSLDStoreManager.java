/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Singleton class that holds the {@link DSLDStore}s for all Groovy projects.
 */
public class DSLDStoreManager {

    private final Map<String, DSLDStore> projectDsldMap = new HashMap<>();

    private final Set<String> inProgress = new HashSet<>();

    public DSLDStore getDSLDStore(IJavaProject project) {
        return getDSLDStore(project.getElementName());
    }
    public DSLDStore getDSLDStore(IProject project) {
        return getDSLDStore(project.getName());
    }

    public DSLDStore getDSLDStore(String projectName) {
        DSLDStore contextStore = projectDsldMap.get(projectName);
        if (contextStore == null) {
            contextStore = new DSLDStore();
            projectDsldMap.put(projectName, contextStore);
        }
        return contextStore;
    }

    public void clearDSLDStore(IProject project) {
        projectDsldMap.remove(project.getName());
    }

    public void clearDSLDStore(IJavaProject project) {
        projectDsldMap.remove(project.getElementName());
    }

    public void reset() {
        projectDsldMap.clear();
    }

    public boolean hasDSLDStoreFor(IProject project) {
        return projectDsldMap.containsKey(project.getName());
    }

    public List<String> getAllStores() {
        return new ArrayList<>(projectDsldMap.keySet());
    }

    public void initializeAll(boolean synchronous) {
        if (GroovyDSLCoreActivator.getDefault().isDSLDDisabled()) {
            return;
        }
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        initialize(allProjects, synchronous);
    }

    public void initialize(IProject[] projects, boolean synchronous) {
        initialize(Arrays.asList(projects), synchronous);
    }

    public void initialize(IProject project, boolean synchronous) {
        initialize(Collections.singletonList(project), synchronous);
    }

    public void initialize(List<IProject> projects, boolean synchronous) {
        List<IProject> groovyProjects = new ArrayList<>(projects.size());
        for (IProject project : projects) {
            if (GroovyNature.hasGroovyNature(project)) {
                groovyProjects.add(project);
            }
        }
        @SuppressWarnings("deprecation")
        Job refreshJob = new RefreshDSLDJob(groovyProjects);
        refreshJob.setPriority(synchronous ? Job.INTERACTIVE : Job.LONG);
        refreshJob.schedule();
        if (synchronous) {
            waitForFinish();
        }
    }

    public void ensureInitialized(IProject project, boolean synchronous) {
        if (!hasDSLDStoreFor(project) && !isInProgress(project)) {
            initialize(project, synchronous);
        }
    }

    private final static int TIME_LIMIT = 30000;
    synchronized void waitForFinish() {
        long end = System.currentTimeMillis() + TIME_LIMIT;
        while (!inProgress.isEmpty()) {
            try {
                long timeLeft = end - System.currentTimeMillis();
                if (timeLeft > 0) {
                    wait(timeLeft);
                } else {
//                    GroovyDSLCoreActivator.logException("Avoiding potential deadlock", new RuntimeException());
                    break;
                }
            } catch (InterruptedException e) {
            }
        }
    }
    synchronized boolean isInProgress(IProject project) {
        return inProgress.contains(project.getName());
    }
    private synchronized boolean addInProgress(IProject project) {
        return inProgress.add(project.getName());
    }
    synchronized List<IProject> addInProgress(List<IProject> projects) {
        List<IProject> addedProjects = new ArrayList<>(projects.size());
        for (IProject project : projects) {
            if (addInProgress(project)) {
                addedProjects.add(project);
            }
        }
        return addedProjects;
    }
    synchronized void removeInProgress(IProject project) {
        inProgress.remove(project.getName());
        notifyAll();
    }
}
