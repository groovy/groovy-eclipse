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
            int start1 = i1.getStart();
            if (start1 <= 0 && i1.getType() != null) {
                start1 = i1.getType().getStart();
            }
            int start2 = i2.getStart();
            if (start2 <= 0 && i2.getType() != null) {
                start2 = i2.getType().getStart();
            }
            return start1 - start2;
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
        sortedImports.addAll(module.getStarImports());
        sortedImports.addAll(module.getStaticStarImports().values());
        sortedImports.addAll(module.getStaticImports().values());
    }

    // not available in 1.6 stream
    public static String getFieldName(ImportNode node) {
        return node.getFieldName();
    }
    // not available in 1.6 stream
    public static Map<String, ImportNode> getStaticImports(ModuleNode node) {
        return node.getStaticImports();
    }
    // not available in 1.6 stream
    public static Map<String, ImportNode> getStaticStarImports(ModuleNode node) {
        return node.getStaticStarImports();
    }
    // not available in 1.6 stream
    public static List<ImportNode> getStarImports(ModuleNode node) {
        return node.getStarImports();
    }
}
