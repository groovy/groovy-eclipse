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

import java.util.Map;
import java.util.WeakHashMap;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A wrapper around the JDT resolver that caches
 * resolve requests.  Handles classes with type parameters.
 * @author andrew
 * @created Nov 21, 2010
 */
public class ResolverCache {

    private final Map<String, ClassNode> nameTypeCache;
    private final JDTResolver resolver;
    
    public ResolverCache(JDTResolver resolver, ModuleNode thisModule) {
        this.nameTypeCache = new WeakHashMap<String, ClassNode>();
        this.resolver = resolver;
    }
    
    /**
     * Resolves a class name to a ClassNode.  Using the fully qualified type name, or the array type signature for arrays
     * Can specify type parameters, also using fully qualified names.
     * @param qName
     * @return
     */
    public ClassNode resolve(String qName) {
        if (qName == null || qName.length() == 0) {
             return ClassHelper.DYNAMIC_TYPE;
        }
        qName = qName.trim();
        if (qName.equals("java.lang.Void") || qName.equals("void")) {
            return VariableScope.VOID_CLASS_NODE;
        }
        ClassNode clazz = nameTypeCache.get(qName);
        int arrayCnt = 0;
        if (clazz == null && resolver != null) {
        	int typeParamEnd = qName.lastIndexOf('>');
        	int arrayStart = qName.indexOf('[', typeParamEnd);
        	String componentName;
        	if (arrayStart > 0) {
        		componentName = qName.substring(0, arrayStart);
        		arrayCnt = calculateArrayCount(qName, arrayStart);
        		
        	} else {
        		componentName = qName;
        	}
        			
        	String erasureName = componentName;
        	int typeParamStart = -1;
        	if (typeParamEnd > 0) {
                typeParamStart = componentName.indexOf('<');
                if (typeParamStart > 0) {
                    erasureName = componentName.substring(0, typeParamStart);
                }
        	}
            clazz = resolver.resolve(erasureName);
            if (clazz == null) {
                clazz = VariableScope.OBJECT_CLASS_NODE;
            }
            nameTypeCache.put(erasureName, clazz);
            
            // now recur down through the type parameters
            if (typeParamStart > 0) {
                // only need to clone if generics are involved
                clazz = VariableScope.clone(clazz);
                
                String[] typeParameterNames = componentName.substring(typeParamStart+1, componentName.length()-1).split(",");
                ClassNode[] typeParameters = new ClassNode[typeParameterNames.length];
                for (int i = 0; i < typeParameterNames.length; i++) {
                    typeParameters[i] = resolve(typeParameterNames[i]);
                }
                clazz = VariableScope.clone(clazz);
                GenericsType[] genericsTypes = clazz.getGenericsTypes();
                if (genericsTypes != null) {
                    // need to be careful here...there may be too many or too few type parameters
                    for (int i = 0; i < genericsTypes.length && i < typeParameters.length; i++) {
                        genericsTypes[i].setType(typeParameters[i]);
                        genericsTypes[i].setName(typeParameters[i].getName());
                    }
                    nameTypeCache.put(componentName, clazz);
                }
            }
            while (arrayCnt > 0) {
            	clazz = new ClassNode(clazz);
            	componentName += "[]";
                nameTypeCache.put(componentName, clazz);
            	arrayCnt--;
            }
        }
        
        return clazz;
    }

    /**
     * @param qName
     * @param arrayStart
     * @return
     */
    private int calculateArrayCount(String qName, int arrayStart) {
    	if (arrayStart < 0) {
    		return 0;
    	}
    	int cnt = 1;
    	while ((arrayStart = qName.indexOf('[', arrayStart+1)) > 0) {
    		cnt ++;
    	}
        return cnt;
    }
    
}
