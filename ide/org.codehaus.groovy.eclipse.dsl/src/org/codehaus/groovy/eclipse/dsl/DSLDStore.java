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
package org.codehaus.groovy.eclipse.dsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.internal.core.NonJavaResource;

/**
 * Stores the pointcuts for a single project.
 */
public class DSLDStore {

    /** Maps pointcuts to their contributors. */
    private final Map<IPointcut, List<IContributionGroup>> pointcutContributionMap = new LinkedHashMap<>();
    /** Maps keys (such as script names) to the pointcuts they produce. */
    private final Map<IStorage, Set<IPointcut>> keyContextMap = new HashMap<>();

    public void addContributionGroup(IPointcut pointcut, IContributionGroup contribution) {
        synchronized (pointcutContributionMap) {
            List<IContributionGroup> contributions = pointcutContributionMap.get(pointcut);
            if (contributions == null) {
                contributions = new ArrayList<>();
                pointcutContributionMap.put(pointcut, contributions);
            }
            contributions.add(contribution);
        }

        IStorage identifier = pointcut.getContainerIdentifier();
        synchronized (keyContextMap) {
            Set<IPointcut> pointcuts = keyContextMap.get(identifier);
            if (pointcuts == null) {
                pointcuts = new HashSet<>();
                keyContextMap.put(identifier, pointcuts);
            }
            pointcuts.add(pointcut);
        }
    }

    public void purgeIdentifier(IStorage identifier) {
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Purging pointcut for DSL file " + identifier);
        }
        Set<IPointcut> pointcuts;
        synchronized (keyContextMap) {
            pointcuts = keyContextMap.remove(identifier);
        }
        if (pointcuts != null) {
            synchronized (pointcutContributionMap) {
                for (IPointcut pointcut : pointcuts) {
                    pointcutContributionMap.remove(pointcut);
                }
            }
        }
    }

    public void purgeAll() {
        synchronized (keyContextMap) {
            keyContextMap.clear();
        }
        synchronized (pointcutContributionMap) {
            pointcutContributionMap.clear();
        }
    }

    /**
     * Creates a new {@link DSLDStore} based on the pattern passed in
     * only includes {@link IPointcut}s that match the pattern.
     * Sub-stores are meant to be short-lived and are not purged when a
     * script changes.
     *
     * @param patern the pattern to match against
     * @return a new {@link DSLDStore} containing only matches against the pattern
     */
    public DSLDStore createSubStore(GroovyDSLDContext pattern) {
        DSLDStore subStore = new DSLDStore();
        synchronized (pointcutContributionMap) {
            for (Map.Entry<IPointcut, List<IContributionGroup>> entry : pointcutContributionMap.entrySet()) {
                if (entry.getKey().fastMatch(pattern)) {
                    subStore.addAllContributions(entry.getKey(), entry.getValue());
                }
            }
        }
        return subStore;
    }

    public void addAllContributions(IPointcut pointcut, List<IContributionGroup> contributions) {
        synchronized (pointcutContributionMap) {
            List<IContributionGroup> existing = pointcutContributionMap.get(pointcut);
            if (existing == null) {
                pointcutContributionMap.put(pointcut, contributions);
            } else {
                existing.addAll(contributions);
            }
        }
    }

    public void addAllContexts(List<IPointcut> pointcuts, IContributionGroup contribution) {
        for (IPointcut pointcut : pointcuts) {
            addContributionGroup(pointcut, contribution);
        }
    }

    /**
     * Find all contributions for this pattern and this declaring type.
     *
     * @param pattern The pattern to match against
     * @param disabledScripts The set of scripts that are disabled and should be ignored
     * @return The set of contributions applicable for the pattern
     */
    public List<IContributionElement> findContributions(GroovyDSLDContext pattern, Set<String> disabledScripts) {
        List<IContributionElement> elts = new ArrayList<>();
        synchronized (pointcutContributionMap) {
            for (Map.Entry<IPointcut, List<IContributionGroup>> entry : pointcutContributionMap.entrySet()) {
                IPointcut pointcut = entry.getKey();
                if (!disabledScripts.contains(DSLDStore.toUniqueString(pointcut.getContainerIdentifier()))) {
                    pattern.resetBinding();
                    Collection<?> results = pointcut.matches(pattern, pattern.getCurrentType());
                    if (results != null) {
                        for (IContributionGroup group : entry.getValue()) {
                            elts.addAll(group.getContributions(pattern, pattern.getCurrentBinding()));
                        }
                    }
                }
            }
        }
        return elts;
    }

    public IStorage[] getAllContextKeys() {
        synchronized (keyContextMap) {
            return keyContextMap.keySet().toArray(new IStorage[0]);
        }
    }

    public static String toUniqueString(IStorage storage) {
        if (storage instanceof IFile) {
            return storage.getFullPath().toPortableString();
        } else if (storage instanceof NonJavaResource) {
            return ((NonJavaResource) storage).getPackageFragmentRoot().getJavaProject().getElementName() + " (binary) " + storage.getName();
        } else {
            return storage.getName();
        }
    }
}
