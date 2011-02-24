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
package org.codehaus.groovy.eclipse.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.eclipse.core.resources.IFile;

/**
 * Stores the contexts for a single project
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLDStore {

    private final Map<IPointcut, List<IContributionGroup>> contextContributionMap;  // maps contexts to their contributors
    private final Map<String, Set<IPointcut>> keyContextMap;  // maps unique keys (such as script names) to all the contexts that they produce
    public DSLDStore() {
        contextContributionMap = new HashMap<IPointcut, List<IContributionGroup>>();
        keyContextMap = new HashMap<String, Set<IPointcut>>();
    }
    
    public void addContributionGroup(IPointcut context, IContributionGroup contribution) {
        List<IContributionGroup> contributions = contextContributionMap.get(context);
        if (contributions == null) {
            contributions = new ArrayList<IContributionGroup>();
            contextContributionMap.put(context, contributions);
        }
        contributions.add(contribution);
        
        String identifier = context.getContainerIdentifier();
        Set<IPointcut> contexts = keyContextMap.get(identifier);
        if (contexts == null) {
            contexts = new HashSet<IPointcut>();
            keyContextMap.put(identifier, contexts);
        }
        contexts.add(context);
    }
    
    
    public void purgeIdentifier(String identifier) {
        Set<IPointcut> contexts = keyContextMap.remove(identifier);
        if (contexts != null) {
            for (IPointcut context : contexts) {
                contextContributionMap.remove(context);
            }
        }
    }
    
    public void purgeAll() {
        keyContextMap.clear();
        contextContributionMap.clear();
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
        for (Entry<IPointcut, List<IContributionGroup>> entry : contextContributionMap.entrySet()) {
            if (entry.getKey().fastMatch(pattern)) {
                subStore.addAllContributions(entry.getKey(), entry.getValue());
            }
        }
        return subStore;
    }

    public void addAllContributions(IPointcut context, List<IContributionGroup> contributions) {
        List<IContributionGroup> existing = contextContributionMap.get(context);
        if (existing == null) {
            contextContributionMap.put(context, contributions);
        } else {
            existing.addAll(contributions);
        }
    }
    public void addAllContexts(List<IPointcut> contexts, IContributionGroup contribution) {
        for (IPointcut context : contexts) {
            addContributionGroup(context, contribution);
        }
    }
    
    
    public void purgeFileFromStore(IFile file) {
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Purging context for DSL file " + file);
        }
        Set<IPointcut> contexts = keyContextMap.remove(convertToIdentifier(file));
        if (contexts != null) {
            for (IPointcut context : contexts) {
                contextContributionMap.remove(context);
            }
        }
    }

    /**
     * Find all contributions for this pattern and this declaring type
     * @return
     */
    public List<IContributionElement> findContributions(GroovyDSLDContext pattern) {
        List<IContributionElement> elts = new ArrayList<IContributionElement>();
        for (Entry<IPointcut, List<IContributionGroup>> entry : contextContributionMap.entrySet()) {
            BindingSet matches = entry.getKey().matches(pattern);
            if (matches != null) {
                for (IContributionGroup group : entry.getValue()) {
                    elts.addAll(group.getContributions(pattern, matches));
                }
            }
        }
        return elts;
    }
    
    public static String convertToIdentifier(IFile file) {
        return file.getFullPath().toPortableString();
    }
    
    public String[] getAllContextKeys() {
        return keyContextMap.keySet().toArray(new String[0]);
    }
} 