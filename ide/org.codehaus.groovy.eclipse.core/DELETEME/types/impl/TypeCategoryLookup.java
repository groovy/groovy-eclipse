 /*
 * Copyright 2003-2009 the original author or authors.
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
    
    /**
     * Ignore first parameter when doing content assist, but do not do so when doing code select
     */
    public TypeCategoryLookup(IType categoryType, boolean ignoreFirstParameter) {
        try {
            mapCategory(categoryType, ignoreFirstParameter);
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
        
        return Collections.singletonList("java.lang.Object");
    }
    
    
    /**
     * Category method has at least one argument, and that is the
     * Type that the category applies to.
     * 
     * Map class names of the first arg to a categorized method.
     * @param ignoreFirstParameter 
     * @throws JavaModelException 
     */
    private void mapCategory(IType type, boolean ignoreFirstParameter) throws JavaModelException {
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
                
                String[] parameterNames;
                String[] parameterTypes;
                if (ignoreFirstParameter) {
                    parameterNames = origParameterNames.length > 1 ? new String[origParameterNames.length-1] : NO_PARAMETERS;
                    for (int i = 0; i < parameterNames.length; i++) {
                        parameterNames[i] = origParameterNames[i+1];
                    }
                    
                    parameterTypes = origParameterTypes.length > 1 ? new String[origParameterTypes.length-1] : NO_PARAMETERS;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        parameterTypes[i] = origParameterTypes[i+1];
                    }
                } else {
                    parameterNames = new String[origParameterNames.length];
                    for (int i = 0; i < parameterNames.length; i++) {
                        parameterNames[i] = origParameterNames[i];
                    }
                    
                    parameterTypes = new String[origParameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        parameterTypes[i] = origParameterTypes[i];
                    }
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
                
                // now must convert to type names from type signatures
                parameterTypes = TypeUtil.convertFromTypeSignaturesToQualifiedNames(parameterTypes, type);
                Parameter[] parameters = TypeUtil.createParameterList(parameterTypes, parameterNames);
                int modifiers = TypeUtil.convertFromJavaCoreModifiers(method.getFlags());
                String returnType = Signature.toString(Signature.getTypeErasure(method.getReturnType()));
                ClassType declaringClass = TypeUtil.newClassType(method.getDeclaringType());
                
                Method javaMethod = new Method(modifiers, method.getElementName(), parameters, returnType, declaringClass, true);
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
            if (strings[i].length() > 0) {
                sb.append(strings[i]);
                if (i < strings.length-1) {
                    sb.append(delim);
                }
            }
        }
        return sb.toString();
    }

}