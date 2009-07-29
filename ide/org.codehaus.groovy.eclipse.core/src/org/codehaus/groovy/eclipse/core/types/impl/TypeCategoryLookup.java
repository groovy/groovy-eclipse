/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.core.types.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.AbstractMemberLookup;
import org.codehaus.groovy.eclipse.core.types.ClassType;
import org.codehaus.groovy.eclipse.core.types.JavaMethod;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.Property;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * Lookup for categories of a given type
 */
public class TypeCategoryLookup extends AbstractMemberLookup implements IGroovyProjectAware {
    static final String[] NO_PARAMETERS = new String[0];
    
    private GroovyProjectFacade project;
    
    // maps categories (class names) to methods mapped by the category type
    private Map<String, List<Method>> mapClassNameToMethods = new HashMap<String,List<Method>>();
    
    public TypeCategoryLookup(IType categoryType) {
        try {
            mapCategory(categoryType);
        } catch (JavaModelException e) {
            GroovyCore.logException("Error creating Cateory map for type: " + categoryType, e);
        }
    }
    
    public void setGroovyProject(GroovyProjectFacade project) {
        this.project = project;
    }
    
    protected List<Property> collectAllProperties(String type) {
        return Collections.emptyList();
    }
     
    protected List<Method> collectAllMethods(String type) {
        try {
            Collection<String> hierarchy = createTypeHierarchy(type);
    
            List<Method> results = new ArrayList<Method>();
            
            
            for (String typeName : hierarchy) {
                List<Method> l = mapClassNameToMethods.get(typeName);
                if (l != null) { 
                    results.addAll(l); 
                }
            }
            
            // for GStrings, also add all String methods
            if (type.equals("groovy.lang.GString")) {
            	List<Method> l = mapClassNameToMethods.get("java.lang.String");
                if (l != null) { 
                    results.addAll(l); 
                }
            }
            
            return results;
        } catch (JavaModelException jme) {
            GroovyCore.logException("Error calculating hierarchy of " + type, jme);
            return Collections.emptyList();
        }
    }
    
    private Collection<String> createTypeHierarchy(String typeName) throws JavaModelException {
        if (project == null) {
            return Collections.emptyList();
        }
        
        IType type = project.getProject().findType(typeName, new NullProgressMonitor());
        if (type != null) {
            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
            IType[] classes = hierarchy.getAllClasses();
            IType[] interfaces = hierarchy.getAllInterfaces();
            List<String> types = new ArrayList<String>(classes.length + interfaces.length);
            for (int i = 0; i < classes.length; i++) {
                types.add(classes[i].getFullyQualifiedName());
            }
            for (int i = 0; i < interfaces.length; i++) {
                types.add(interfaces[i].getFullyQualifiedName());
            }
            
            return types;
        }
        
        return Collections.emptyList();
    }
    
    
    /**
     * Category method has at least one argument, and that is the
     * Type that the category applies to.
     * 
     * Map class names of the first arg to a categorized method.
     * @throws JavaModelException 
     */
    private void mapCategory(IType type) throws JavaModelException {
        if (type == null) {
            return;
        }
        IMethod[] methods = type.getMethods();
        
        for (IMethod method : methods) {
            
            String[] origParameterTypes = method.getParameterTypes();
            String[] origParameterNames = method.getParameterNames();
            if (origParameterTypes.length > 0 &&
                    // categories must be static and public
                    Flags.isStatic(method.getFlags()) && 
                    Flags.isPublic(method.getFlags())) {
                // key is the type of the target of the category (ie- first argument's type)
                String key = Signature.toString(Signature.getTypeErasure(origParameterTypes[0]));
                
                String[] parameterNames = origParameterNames.length > 1 ? new String[origParameterNames.length-1] : NO_PARAMETERS;
                for (int i = 0; i < parameterNames.length; i++) {
                    parameterNames[i] = origParameterNames[i+1];
                }
                
                String[] parameterTypes = origParameterTypes.length > 1 ? new String[origParameterTypes.length-1] : NO_PARAMETERS;
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameterTypes[i] = origParameterTypes[i+1];
                }
                IType declaringType = method.getDeclaringType();
                if (!method.isBinary()) {
                    // must convert to fully qualified types for source methods
                    try {
                        key = join(declaringType.resolveType(key)[0], ".");
                    } catch (NullPointerException e) {
                        // ignore
                    }
                    if (key != null) {
                        for (int i = 0; i < parameterTypes.length; i++) {
                            String resolvedType = Signature.toString(Signature.getTypeErasure(parameterTypes[i]));
                            try {
                                resolvedType = join(declaringType.resolveType(resolvedType)[0], ".");
                            } catch (NullPointerException e) {
                                // ignore
                            }
                        }
                    }
                }
                Parameter[] parameters = TypeUtil.createParameterList(parameterTypes, parameterNames);
                int modifiers = TypeUtil.convertFromJavaCoreModifiers(method.getFlags());
                String returnType = Signature.toString(Signature.getTypeErasure(method.getReturnType()));
                ClassType declaringClass = TypeUtil.newClassType(method.getDeclaringType());
                
                JavaMethod javaMethod = new JavaMethod(modifiers, method.getElementName(), parameters, returnType, declaringClass);
                List<Method> list = mapClassNameToMethods.get(key);
                if (list == null) { 
                    list = new LinkedList<Method>(); 
                    mapClassNameToMethods.put(key,list); 
                }
                
                list.add(javaMethod);
            }
        }
    }
    
    private String join(final String[] strings, final String delim) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]);
            if (i < strings.length-1) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

}