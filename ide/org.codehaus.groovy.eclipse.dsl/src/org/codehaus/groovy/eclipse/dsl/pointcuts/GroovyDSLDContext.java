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
package org.codehaus.groovy.eclipse.dsl.pointcuts;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.groovy.search.VariableScope;


/**
 * The current context used to match against when evaluating pointcuts,
 * Class is a bit messy
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class GroovyDSLDContext {

    public GroovyDSLDContext(GroovyCompilationUnit unit) throws CoreException {
        this(unit.getJavaProject().getProject().getDescription().getNatureIds(), unit.getResource().getFullPath().removeFirstSegments(1).toPortableString());
        resolver = new ResolverCache(unit.getResolver(), unit.getModuleNode());
    }
    
    
    /**
     * Not API!!!
     * Shoud not use this constructor.  It is only for testing
     */
    @Deprecated
    public GroovyDSLDContext(String[] projectNatures, String fullPathName) {
        this.fullPathName = fullPathName;
        if (fullPathName != null) {
            int lastDot = fullPathName.lastIndexOf('/');
            this.simpleFileName = fullPathName.substring(lastDot+1);
        } else {
            this.simpleFileName = null;
        }
        this.projectNatures = projectNatures;
    }
    
    /** will be null if this object created from deprecated API */
    public ResolverCache resolver;
    
    public final String[] projectNatures;

    /**
     * This value does not change once it is set since inferencing happens on 
     * a per-file basis
     */
    public final String fullPathName;

    /**
     * This value does not change once it is set since inferencing happens on 
     * a per-file basis
     */
    public final String simpleFileName;
    
    private VariableScope currentScope;
    
    /** the type of the expression currently being analyzed */
    private ClassNode targetType;
    
    /** cached type hierarchy for checking type matches (consider caching more) */
    private Set<ClassNode> cachedHierarchy;
    
    /** used for passing state from an outer pointcut to an inner pointcut*/
    private Object outerPointcutBinding;
    
    public void setTargetType(ClassNode targetType) {
        cachedHierarchy = null;
        this.targetType = targetType;
    }
    
    public boolean matchesNature(String natureId) {
        if (natureId == null) {
            return false;
        }
        for (String nature : projectNatures) {
            if (natureId.equals(nature)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param typeName
     * @return true iff typeName equals the targetType name or any tyoe in the hierarchy
     */
    public boolean matchesType(String typeName) {
        return matchesType(typeName, targetType);
    }
    public boolean matchesType(String typeName, ClassNode toCheck) {
        // when left unspecified, always return true
        if (typeName == null || toCheck == null) {
            return true;
        }
        
        if (typeName.equals(toCheck.getName())) {
            return true;
        }
        
        if (cachedHierarchy == null) {
            // use linked hash set because order is important
            cachedHierarchy = new LinkedHashSet<ClassNode>();
            getAllSupers(toCheck, cachedHierarchy);
        }
        
        for (ClassNode node : cachedHierarchy) {
            if (typeName.equals(node.getName())) {
                return true;
            }
        }
        return false;
    }
    
    
    
    public VariableScope getCurrentScope() {
        return currentScope;
    }
    
    public void setCurrentScope(VariableScope currentScope) {
        this.currentScope = currentScope;
    }
    
    public ClassNode getCurrentType() {
        return targetType;
    }
    
    public Object getOuterPointcutBinding() {
        return outerPointcutBinding;
    }
    public void setOuterPointcutBinding(Object outerPointcutBinding) {
        this.outerPointcutBinding = outerPointcutBinding;
    }

    
    @SuppressWarnings("cast") // keep cast to make 1.6 compile
    private void getAllSupers(ClassNode type, Set<ClassNode> set) {
        if (type == null) {
            return;
        }
        set.add(type);
        getAllSupers(type.getSuperClass(), set);
        for (ClassNode inter : (Iterable<ClassNode>) type.getAllInterfaces()) {
            if (! inter.getName().equals(type.getName())) {
                getAllSupers(inter, set);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ContextPattern [fileName=");
        builder.append(fullPathName);
        builder.append(", targetType=");
        builder.append(targetType);
        builder.append(", currentScope=");
        builder.append(currentScope);
        builder.append("]");
        return builder.toString();
    }

}
