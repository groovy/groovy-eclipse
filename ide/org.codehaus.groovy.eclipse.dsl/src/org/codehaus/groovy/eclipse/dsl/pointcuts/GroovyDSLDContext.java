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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IResource;
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

    /**
     * Patch from project to package root
     */
    public final String packageRootPath;

    /**
     * Path from package root to file name (exclusive)
     */
    public final String packageFolderPath;
    
    /** will be null if this object created from deprecated API */
    private ResolverCache resolverCache;

    private BindingSet currentBinding;

    private VariableScope currentScope;

    /** 
     * the type of the expression currently being analyzed
     * set by the type lookup, should not be set by the pointcuts 
     */
    private ClassNode targetType;

    public GroovyDSLDContext(GroovyCompilationUnit unit, ModuleNode module, JDTResolver jdtResolver) throws CoreException {
        this(getProjectNatures(unit), 
                getFullPathToFile(unit),
                getPathToPackage(unit));
        resolverCache = new ResolverCache(jdtResolver, module);
    }


    /**
     * Not API!!!
     * Should not use this constructor.  It is only for testing
     */
    @Deprecated
    public GroovyDSLDContext(String[] projectNatures, String fullPathName, String packageRootPath) {
        this.fullPathName = fullPathName;
        this.packageRootPath = packageRootPath;
        if (fullPathName != null) {
            int lastDot = fullPathName.lastIndexOf('/');
            this.simpleFileName = fullPathName.substring(lastDot+1);
        } else {
            this.simpleFileName = null;
        }
        
        // assumption is that packageRootPath is a prefix of fullPathName
        String candidate;
        if (packageRootPath != null && packageRootPath.length() < fullPathName.length()) {
            candidate = fullPathName.substring(packageRootPath.length());
            if (simpleFileName != null) {
                int indexOf = candidate.lastIndexOf("/" + simpleFileName);
                int start = candidate.startsWith("/") ? 1 : 0;
                if (indexOf > 0 && candidate.length() > 0) {
                    candidate = candidate.substring(start, indexOf);
                }
            }
        } else {
            candidate = "";
        }
        packageFolderPath = candidate;
        this.projectNatures = projectNatures;
    }
    
    private static String getPathToPackage(GroovyCompilationUnit unit) {
        IResource resource = unit.getPackageFragmentRoot().getResource();
        return resource == null ? null : resource.getFullPath().removeFirstSegments(1).toPortableString();
    }


    private static String getFullPathToFile(GroovyCompilationUnit unit) {
        IResource resource = unit.getResource();
        return resource == null ? null : resource.getFullPath().removeFirstSegments(1).toPortableString();
    }


    private static String[] getProjectNatures(GroovyCompilationUnit unit) throws CoreException {
        return unit.getJavaProject().getProject().getDescription().getNatureIds();
    }

    /** cached type hierarchy for checking type matches (consider caching more) */
    private Set<ClassNode> cachedHierarchy;

    private boolean isStatic;

    private boolean isPrimaryNode;
    
    /**
     * called by the type lookup, not by the pointcuts
     * @param targetType
     */
    public void setTargetType(ClassNode targetType) {
        cachedHierarchy = null;
        this.targetType = targetType;
    }
    
    /**
     * Only the type lookup should use this method
     * @param currentBinding
     */
    public void setCurrentBinding(BindingSet currentBinding) {
        this.currentBinding = currentBinding;
    }
    public void resetBinding() {
        this.currentBinding = new BindingSet();
    }

    
    /**
     * Only the type lookup and the proposl provider should use this method
     * @param currentBinding
     */
    public BindingSet getCurrentBinding() {
        return currentBinding;
    }
    
    /**
     * Adds the collection to the currnt binding.  At this point, currentBinding should never be null
     * Used by the pointcuts only
     * @param bindingName
     * @param toAdd
     */
    public void addToBinding(String bindingName, Collection<?> toAdd) {
        currentBinding.addToBinding(bindingName, toAdd);
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
        isPrimaryNode = currentScope.isPrimaryNode();
    }
    
    public ClassNode getCurrentType() {
        return targetType;
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


    public ResolverCache getResolverCache() {
        return resolverCache;
    }

    public boolean isPrimaryNode() {
        return isPrimaryNode;
    }
    
    public void setPrimaryNode(boolean isPrimaryNode) {
        this.isPrimaryNode = isPrimaryNode;
    }
    
    public void setStatic(boolean s) {
        isStatic = s;
    }
    public boolean isStatic() {
        return isStatic;
    }
}
