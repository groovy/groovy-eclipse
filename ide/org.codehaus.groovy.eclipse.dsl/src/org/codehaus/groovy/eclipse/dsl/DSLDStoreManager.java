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
package org.codehaus.groovy.eclipse.dsl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Singleton that manages the {@link DSLDStore}s for all Groovy projects.
 *
 * @see RefreshDSLDJob
 */
public class DSLDStoreManager {

    private final Map<String, DSLDStore> dsldStores = new ConcurrentHashMap<>();

    public String[] getProjectNames() {
        return dsldStores.keySet().toArray(new String[dsldStores.size()]);
    }

    public DSLDStore getDSLDStore(final IProject project) {
        return dsldStores.computeIfAbsent(project.getName(), projectName -> new DSLDStore());
    }

    public boolean hasDSLDStoreFor(final IProject project) {
        return dsldStores.containsKey(project.getName());
    }

    public void removeDSLDStore(final IProject project) {
        dsldStores.remove(project.getName());
    }

    public void reset() {
        dsldStores.clear();
    }

    //--------------------------------------------------------------------------

    private final Set<String> inProgress = new HashSet<>();

    public void initialize(final IProject project, final boolean synchronous) {
        initialize(Collections.singletonList(project), synchronous);
    }

    public void initialize(final IProject[] projects, final boolean synchronous) {
        initialize(Arrays.asList(projects), synchronous);
    }

    public void initialize(final Collection<IProject> projects, final boolean synchronous) {
        List<IProject> groovyProjects = projects.stream()
            .filter(GroovyNature::hasGroovyNature).collect(Collectors.toList());

        if (!groovyProjects.isEmpty()) {
            @SuppressWarnings("deprecation")
            Job refreshJob = new RefreshDSLDJob(groovyProjects);
            refreshJob.setPriority(synchronous ? Job.INTERACTIVE : Job.LONG);
            refreshJob.schedule();
            if (synchronous) {
                waitForFinish();
            }
        }
    }

    public void ensureInitialized(final IProject project, final boolean synchronous) {
        boolean isInProgress = isInProgress(project);
        if (!isInProgress && !hasDSLDStoreFor(project)) {
            initialize(project, synchronous);
        } else if (isInProgress && synchronous) {
            waitForFinish();
        }
    }

    private synchronized void waitForFinish() {
        long end = System.currentTimeMillis() + 30000;
        while (!inProgress.isEmpty()) {
            try {
                long timeLeft = end - System.currentTimeMillis();
                if (timeLeft > 0) {
                    wait(timeLeft);
                } else {
                    break;
                }
            } catch (InterruptedException ignore) {
            }
        }
    }

    private synchronized boolean isInProgress(final IProject project) {
        return inProgress.contains(project.getName());
    }

    private synchronized boolean addInProgress(final IProject project) {
        return inProgress.add(project.getName());
    }

    synchronized List<IProject> addInProgress(final Collection<IProject> projects) {
        return projects.stream().filter(this::addInProgress).collect(Collectors.toList());
    }

    synchronized void removeInProgress(final IProject project) {
        inProgress.remove(project.getName());
        notifyAll();
    }
}
