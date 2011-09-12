/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * Stores the pointcuts for a single project
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLDStore {

    private final Map<IPointcut, List<IContributionGroup>> pointcutContributionMap;  // maps pointcuts to their contributors
    private final Map<IStorage, Set<IPointcut>> keyContextMap;  // maps unique keys (such as script names) to all the pointcuts that they produce
    public DSLDStore() {
        // use linked hash map because order matters
        pointcutContributionMap = new LinkedHashMap<IPointcut, List<IContributionGroup>>();
        keyContextMap = new HashMap<IStorage, Set<IPointcut>>();
    }
    
    public void addContributionGroup(IPointcut pointcut, IContributionGroup contribution) {
        List<IContributionGroup> contributions = pointcutContributionMap.get(pointcut);
        if (contributions == null) {
            contributions = new ArrayList<IContributionGroup>();
            pointcutContributionMap.put(pointcut, contributions);
        }
        contributions.add(contribution);
        
        IStorage identifier = pointcut.getContainerIdentifier();
        Set<IPointcut> pointcuts = keyContextMap.get(identifier);
        if (pointcuts == null) {
            pointcuts = new HashSet<IPointcut>();
            keyContextMap.put(identifier, pointcuts);
        }
        pointcuts.add(pointcut);
    }
    
    
    public void purgeIdentifier(IStorage identifier) {
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Purging pointcut for DSL file " + identifier);
        }
        Set<IPointcut> pointcuts = keyContextMap.remove(identifier);
        if (pointcuts != null) {
            for (IPointcut pointcut : pointcuts) {
                pointcutContributionMap.remove(pointcut);
            }
        }
    }
    
    public void purgeAll() {
        keyContextMap.clear();
        pointcutContributionMap.clear();
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
        for (Entry<IPointcut, List<IContributionGroup>> entry : pointcutContributionMap.entrySet()) {
            if (entry.getKey().fastMatch(pattern)) {
                subStore.addAllContributions(entry.getKey(), entry.getValue());
            }
        }
        return subStore;
    }

    public void addAllContributions(IPointcut pointcut, List<IContributionGroup> contributions) {
        List<IContributionGroup> existing = pointcutContributionMap.get(pointcut);
        if (existing == null) {
            pointcutContributionMap.put(pointcut, contributions);
        } else {
            existing.addAll(contributions);
        }
    }
    public void addAllContexts(List<IPointcut> pointcuts, IContributionGroup contribution) {
        for (IPointcut pointcut : pointcuts) {
            addContributionGroup(pointcut, contribution);
        }
    }
    
   
    /**
     * Find all contributions for this pattern and this declaring type
     * @param pattern The pattern to match against
     * @param disabledScripts The set of scripts that are disabled and should be ignored
     * @return The set of contributions applicable for the pattern
     */
    public List<IContributionElement> findContributions(GroovyDSLDContext pattern, Set<String> disabledScripts) {
        List<IContributionElement> elts = new ArrayList<IContributionElement>();
        for (Entry<IPointcut, List<IContributionGroup>> entry : pointcutContributionMap.entrySet()) {
            IPointcut pointcut = entry.getKey();
            if (! disabledScripts.contains(DSLDStore.toUniqueString(pointcut.getContainerIdentifier()))) {
                pattern.resetBinding();
                Collection<?> results = pointcut.matches(pattern, pattern.getCurrentType());
                if (results != null) {
                    for (IContributionGroup group : entry.getValue()) {
                        elts.addAll(group.getContributions(pattern, pattern.getCurrentBinding()));
                    }
                }
            }
        }
        return elts;
    }
    
    public IStorage[] getAllContextKeys() {
        return keyContextMap.keySet().toArray(new IStorage[0]);
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