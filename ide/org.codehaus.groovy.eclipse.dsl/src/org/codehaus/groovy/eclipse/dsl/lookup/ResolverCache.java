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
package org.codehaus.groovy.eclipse.dsl.lookup;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A wrapper around the JDT resolver that caches
 * resolve requests...consider pushing this back into 
 * GroovyCompilationUnit
 * @author andrew
 * @created Nov 21, 2010
 */
public class ResolverCache {

    private final Map<String, ClassNode> nameTypeCache;
    private final JDTResolver resolver;
    
    public ResolverCache(JDTResolver resolver, ModuleNode thisModule) {
        this.nameTypeCache = new HashMap<String, ClassNode>();
        this.resolver = resolver;
    }
    
    public ClassNode resolve(String qName) {
        ClassNode clazz = nameTypeCache.get(qName);
        if (clazz == null && resolver != null) {
            clazz = resolver.resolve(qName);
            if (clazz == null) {
                clazz = VariableScope.OBJECT_CLASS_NODE;
            }
            nameTypeCache.put(qName, clazz);
        }
        
        return clazz;
    }
    
}
