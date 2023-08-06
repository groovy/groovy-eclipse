/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.dsl.pointcuts;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * The current context used to match against when evaluating pointcuts.
 *
 * Class is a bit messy
 */
public class GroovyDSLDContext {

    public final String[] projectNatures;

    /**
     * This value does not change once it is set since inferencing happens on
     * a per-file basis.
     */
    public final String fullPathName;

    /**
     * This value does not change once it is set since inferencing happens on
     * a per-file basis.
     */
    public final String simpleFileName;

    /**
     * Path from project to package root.
     */
    public final String packageRootPath;

    /**
     * Path from package root to file name (exclusive).
     */
    public final String packageFolderPath;

    /** will be null if this object created from deprecated API */
    private ResolverCache resolverCache;

    private BindingSet currentBinding;

    private VariableScope currentScope;

    private IJavaProject currentProject;

    private Map<String, String> currentOptions;

    /**
     * the type of the expression currently being analyzed
     * set by the type lookup, should not be set by the pointcuts
     */
    private ClassNode targetType;

    /** cached type hierarchy for checking type matches (consider caching more) */
    private Set<ClassNode> cachedHierarchy;

    private boolean isStatic;

    private boolean isPrimaryNode;

    public GroovyDSLDContext(GroovyCompilationUnit unit, ModuleNode module, JDTResolver jdtResolver) throws CoreException {
        this(getProjectNatures(unit), getFullPathToFile(unit), getPathToPackage(unit));
        resolverCache = new ResolverCache(jdtResolver, module);
        currentProject = unit.getJavaProject();
    }

    /**
     * Not API!!! Should not use this constructor. It is only for testing.
     */
    @Deprecated
    public GroovyDSLDContext(String[] projectNatures, String fullPathName, String packageRootPath) {
        this.fullPathName = fullPathName;
        this.packageRootPath = packageRootPath;
        if (fullPathName != null) {
            int lastDot = fullPathName.lastIndexOf('/');
            this.simpleFileName = fullPathName.substring(lastDot + 1);
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

    private static String[] getProjectNatures(GroovyCompilationUnit unit) throws CoreException {
        return unit.getJavaProject().getProject().getDescription().getNatureIds();
    }

    private static String getFullPathToFile(GroovyCompilationUnit unit) {
        IResource resource = unit.getResource();
        return resource == null ? null : resource.getFullPath().removeFirstSegments(1).toPortableString();
    }

    private static String getPathToPackage(GroovyCompilationUnit unit) {
        IResource resource = unit.getPackageFragmentRoot().getResource();
        return resource == null ? null : resource.getFullPath().removeFirstSegments(1).toPortableString();
    }

    //--------------------------------------------------------------------------

    /**
     * Adds the collection to the currnt binding.  At this point, currentBinding should never be null.
     * Used by the pointcuts only.
     */
    public void addToBinding(String bindingName, Collection<?> toAdd) {
        currentBinding.addToBinding(bindingName, toAdd);
    }

    /**
     * Only type lookup and the proposl provider should use this method.
     */
    public BindingSet getCurrentBinding() {
        return currentBinding;
    }

    public Map<String, String> getCurrentOptions() {
        if (currentOptions == null) {
            currentOptions = currentProject.getOptions(true);
        }
        return currentOptions;
    }

    public IJavaProject getCurrentProject() {
        return currentProject;
    }

    public VariableScope getCurrentScope() {
        return currentScope;
    }

    public ClassNode getCurrentType() {
        return targetType;
    }

    public ResolverCache getResolverCache() {
        return resolverCache;
    }

    public boolean isPrimaryNode() {
        return isPrimaryNode;
    }

    public boolean isStatic() {
        return isStatic;
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
     * @return {@code true} iff {@code typeName} matches the current type or any type in the hierarchy.
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
            Set<ClassNode> superTypes = new LinkedHashSet<>();
            VariableScope.createTypeHierarchy(toCheck, superTypes, false);
            cachedHierarchy = superTypes;
        }

        for (ClassNode node : cachedHierarchy) {
            if (typeName.equals(node.getName())) {
                return true;
            }
        }
        return false;
    }

    public void resetBinding() {
        setCurrentBinding(new BindingSet());
    }

    /**
     * Only type lookup should use this method.
     */
    public void setCurrentBinding(BindingSet currentBinding) {
        this.currentBinding = currentBinding;
    }

    public void setCurrentScope(VariableScope currentScope) {
        this.currentScope = currentScope;
        setPrimaryNode(currentScope.isPrimaryNode());
    }

    public void setPrimaryNode(boolean isPrimaryNode) {
        this.isPrimaryNode = isPrimaryNode;
    }

    public void setStatic(boolean s) {
        isStatic = s;
    }

    /**
     * Called by type lookup, not by the pointcuts.
     */
    public void setTargetType(ClassNode targetType) {
        if (currentScope.isPrimaryNode() && VariableScope.CLASS_CLASS_NODE.equals(targetType) && targetType.isUsingGenerics()) {
            targetType = targetType.getGenericsTypes()[0].getType();
        }
        this.targetType = targetType;
        cachedHierarchy = null;
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
