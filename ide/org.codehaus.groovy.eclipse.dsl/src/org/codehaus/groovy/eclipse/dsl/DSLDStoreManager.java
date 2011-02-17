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
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Singleton class that holds the {@link DSLDStore}s for all Groovy projects
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLDStoreManager {
    
    private final Map<String, DSLDStore> projectDSLDMap;
    
    public DSLDStoreManager() {
        projectDSLDMap = new HashMap<String, DSLDStore>();
    }
    
    public DSLDStore getDSLDStore(IJavaProject project) {
        return getDSLDStore(project.getElementName());
    }
    public DSLDStore getDSLDStore(IProject project) {
        return getDSLDStore(project.getName());
    }
    
    public DSLDStore getDSLDStore(String projectName) {
        DSLDStore contextStore = projectDSLDMap.get(projectName);
        if (contextStore == null) {
            contextStore = new DSLDStore();
            projectDSLDMap.put(projectName, contextStore);
        }
        return contextStore;
    }
    
    public void clearDSLDStore(IProject project) {
        projectDSLDMap.remove(project.getName());
    }
    
    public void clearDSLDStore(IJavaProject project) {
        projectDSLDMap.remove(project.getElementName());
    }
    
    public void reset() {
        projectDSLDMap.clear();
    }

    public boolean hasDSLDStoreFor(IProject project) {
        return projectDSLDMap.containsKey(project.getName());
    }

    public List<String> getAllStores() {
        return new ArrayList<String>(projectDSLDMap.keySet());
    }

}
