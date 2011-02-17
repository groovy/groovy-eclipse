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
package org.codehaus.groovy.eclipse.dsl.pointcuts;

import java.util.LinkedHashSet;
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
        this(unit.getJavaProject().getProject().getDescription().getNatureIds(), unit.getResource().getFullPath().toPortableString());
        resolver = new ResolverCache(unit.getResolver(), unit.getModuleNode());
    }
    
    
    /**
     * Not API!!!
     * Shoud not use this constructor.  It is only for testing
     */
    @Deprecated
    public GroovyDSLDContext(String[] projectNatures, String fileName) {
        this.fileName = fileName;
        this.projectNatures = projectNatures;
    }
    
    /** will be null if this object created from deprecated API */
    public ResolverCache resolver;
    
    public final String[] projectNatures;

    /**
     * This value does not change once it is set since inferencing happens on 
     * a per-file basis
     */
    public final String fileName;

    private VariableScope currentScope;
    
    /** the type of the expression currently being analyzed */
    private ClassNode targetType;
    
    /** */
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
        // when left unspecified, always return true
        if (typeName == null || targetType == null) {
            return true;
        }
        
        if (typeName.equals(targetType.getName())) {
            return true;
        }
        
        if (cachedHierarchy == null) {
            // use linked hash set because order is important
            cachedHierarchy = new LinkedHashSet<ClassNode>();
            getAllSupers(targetType, cachedHierarchy);
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
        builder.append(fileName);
        builder.append(", targetType=");
        builder.append(targetType);
        builder.append(", currentScope=");
        builder.append(currentScope);
        builder.append("]");
        return builder.toString();
    }

}
