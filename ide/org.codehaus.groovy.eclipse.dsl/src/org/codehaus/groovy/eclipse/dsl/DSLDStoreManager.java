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
