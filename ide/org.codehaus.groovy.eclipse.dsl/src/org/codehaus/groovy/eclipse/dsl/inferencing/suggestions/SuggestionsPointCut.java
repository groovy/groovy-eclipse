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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import groovy.lang.Closure;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager.ProjectSuggestions;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

public class SuggestionsPointCut implements IPointcut {

    private IFile xdslFile;
    private IProject project;

    public SuggestionsPointCut(IFile xdslFile) {
        this.xdslFile = xdslFile;
        this.project = xdslFile.getProject();
    }

    @Override
    public void accept(Closure contributionGroupClosure) {
    }

    @Override
    public void addArgument(Object argument) {
    }

    @Override
    public void addArgument(String name, Object argument) {
    }

    @Override
    public boolean fastMatch(GroovyDSLDContext pattern) {
        return true;
    }

    @Override
    public String[] getArgumentNames() {
        return null;
    }

    @Override
    public Object[] getArgumentValues() {
        return null;
    }

    @Override
    public IStorage getContainerIdentifier() {
        return xdslFile;
    }

    @Override
    public Object getFirstArgument() {
        return null;
    }

    @Override
    public String getFirstArgumentName() {
        return null;
    }

    @Override
    public String getPointcutDebugName() {
        return null;
    }

    @Override
    public String getPointcutName() {
        return null;
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        // toMatch is the declaring type with the unresolved property or method
        ProjectSuggestions suggestions = InferencingSuggestionsManager.getInstance().getSuggestions(project);

        if (suggestions != null && !suggestions.getDeclaringTypes().isEmpty()) {

            // FIXNS: Not optimal. check supertypes. To improve performance per
            // inferencing run (i.e. AST walk)
            // cache any supertypes check into the GroovyDSLDContext, preferably
            // a map with current subtype obtained
            // from the pattern as a key, and the list of ClassNodes in the AST
            // as a value. In addition,
            // cache the list of exact declaring types in the ProjectSuggestion
            // that matches a specific current subtype
            // in a map, so that the calculation does not have to be recomputed.
            // It is safe to cache anything in the GroovyDSLDContext and avoid
            // memory leaks as it gets purged
            // at the end of the AST walk. An AST walk happens every time a
            // change is made to a Groovy file.

            List<GroovySuggestionDeclaringType> superTypes = new DeclaringTypeSuperTypeMatcher(project).getAllSuperTypes(pattern);
            if (superTypes != null && !superTypes.isEmpty()) {

                // Should return ClassNodes. Since toMatch is a ClassNode,
                // return that for now. Ideally it should return all ClassNodes
                // for the found supertypes above
                return Collections.singletonList(toMatch);
            }
        }
        return null;
    }

    @Override
    public IPointcut normalize() {
        return this;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

    @Override
    public void verify() throws PointcutVerificationException {
    }
}
