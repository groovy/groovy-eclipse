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
package org.codehaus.groovy.eclipse.dsl.contexts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLActivator;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Singleton class that holds the {@link ContextStore}s for all Groovy projects
 * @author andrew
 * @created Nov 17, 2010
 */
public class ContextStoreManager {
    
    private final Map<String, ContextStore> projectContextMap;
    
    public ContextStoreManager() {
        projectContextMap = new HashMap<String, ContextStore>();
    }
    
    public ContextStore getContextStore(IJavaProject project) {
        return getContextStore(project.getElementName());
    }
    public ContextStore getContextStore(IProject project) {
        return getContextStore(project.getName());
    }
    
    
    
    /**
     * Returns a context store if this is a groovy project that
     * includes the context store for all transitive project dependencies
     * @param project a groovy project
     * @return a combined context store for this project and all transitive dependencies
     * or null if no context store exists
     */
    public ContextStore getTransitiveContextStore(IJavaProject project) {
        GroovyLogManager.manager.log(TraceCategory.DSL, "Trawling through project dependencies of '" + 
                project.getElementName() + "' to build context store");
        ContextStore store = new ContextStore();
        Set<String> alreadySeenProjects = new HashSet<String>();
        internalGetTransitive(project, store, alreadySeenProjects);
        return store;
    }

    /**
     * Returns a context store if this is a groovy project that
     * includes the context store for all transitive project dependencies
     * @param project a groovy project
     * @return a combined context store for this project and all transitive dependencies
     * or an empty store if no context store exists
     */
    public ContextStore getTransitiveContextStore(IProject project) {
        return getTransitiveContextStore(JavaCore.create(project));
    }
    
    
    private void internalGetTransitive(IJavaProject project, ContextStore store, Set<String> alreadySeenProjects) {
        if (! GroovyNature.hasGroovyNature(project.getProject())) {
            return;
        }
        alreadySeenProjects.add(project.getElementName());
        
        GroovyLogManager.manager.log(TraceCategory.DSL, "   Adding project " + project.getElementName() + " to store");
        store.addContextStore(projectContextMap.get(project.getElementName()));
        try {
            // really should be checking to see if the project is exported on the classpath
            String[] required = project.getRequiredProjectNames();
            for (String otherProjectName : required) {
                if (! alreadySeenProjects.contains(otherProjectName)) {
                    internalGetTransitive((IJavaProject) JavaCore.create("=" + otherProjectName), store, alreadySeenProjects);
                }
            }
        } catch (CoreException e) {
            GroovyDSLActivator.logException(e);
        }
    }
    
    
    public ContextStore getContextStore(String projectName) {
        ContextStore contextStore = projectContextMap.get(projectName);
        if (contextStore == null) {
            contextStore = new ContextStore();
            projectContextMap.put(projectName, contextStore);
        }
        return contextStore;
    }
    
    public void clearContextStore(IProject project) {
        projectContextMap.remove(project.getName());
    }
    
    public void clearContextStore(IJavaProject project) {
        projectContextMap.remove(project.getElementName());
    }
    
    public void reset() {
        projectContextMap.clear();
    }

    public boolean hasContextStoreFor(IProject project) {
        return projectContextMap.containsKey(project.getName());
    }

    public List<String> getAllStores() {
        return new ArrayList<String>(projectContextMap.keySet());
    }

}
