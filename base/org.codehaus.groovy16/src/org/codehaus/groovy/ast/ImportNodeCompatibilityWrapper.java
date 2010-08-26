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
package org.codehaus.groovy.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class provides a standard interface to access import nodes 
 * on a module node.
 * 
 * Since the API has changed between groovy versions 1.6 and 1.7,
 * we need to ensure there is a common API that Groovy-Eclipse can
 * access independent on the version.
 * 
 * @author Andrew Eisenberg
 * @created Apr 29, 2010
 */
public class ImportNodeCompatibilityWrapper {
    private class ImportNodeComparator implements Comparator<ImportNode> {
        public int compare(ImportNode i1, ImportNode i2) {
            return i1.getStart() - i2.getStart();
        }
    }
    
    private SortedSet<ImportNode> sortedImports;
    private ModuleNode module;
    
    public ImportNodeCompatibilityWrapper(ModuleNode module) {
        if (module == null) {
            throw new IllegalArgumentException("Module node should not be null");
        }
        this.module = module;
    }
    
    public SortedSet<ImportNode> getAllImportNodes() {
        if (sortedImports == null) {
            initialize();
        }
        return sortedImports;
    }

    private void initialize() {
        sortedImports = new TreeSet<ImportNode>(new ImportNodeComparator());
        sortedImports.addAll(module.getImports());
        // not available in 1.6 stream
//        sortedImports.addAll(module.getStarImports());
//        sortedImports.addAll(module.getStaticStarImports().values());
//        sortedImports.addAll(module.getStaticImports().values());
    }
    
    // not available in 1.6 stream
    public static String getFieldName(ImportNode node) {
    	return null;
    }
    // not available in 1.6 stream
    public static Map<String, ImportNode> getStaticImports(ModuleNode node) {
        return Collections.emptyMap();
    }
    // not available in 1.6 stream
    public static Map<String, ImportNode> getStaticStarImports(ModuleNode node) {
        return Collections.emptyMap();
    }
    // mock this up for the 1.6 stream
    public static List<ImportNode> getStarImports(ModuleNode node) {
        List<String> importPackages = node.getImportPackages();
        if (importPackages != null) {
            List<ImportNode> importPackageNodes = new ArrayList<ImportNode>(importPackages.size()); 
            for (String importPackage : importPackages) {
                ImportNode newImport = new ImportNode(null, null);
                newImport.setPackageName(importPackage);
                importPackageNodes.add(newImport);
            }
            return importPackageNodes;
        } else {
            return Collections.emptyList();
        }
    }
}
