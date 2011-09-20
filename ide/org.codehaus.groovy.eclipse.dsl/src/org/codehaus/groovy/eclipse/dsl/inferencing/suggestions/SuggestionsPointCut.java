/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import groovy.lang.Closure;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager.ProjectSuggestions;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-17
 */
public class SuggestionsPointCut implements IPointcut {

    private IFile xdslFile;

    private IProject project;

    public SuggestionsPointCut(IFile xdslFile) {
        this.xdslFile = xdslFile;
        this.project = xdslFile.getProject();
    }

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

    public IStorage getContainerIdentifier() {
        return xdslFile;
    }

    public IPointcut normalize() {
        return this;
    }

    public void addArgument(String name, Object argument) {
        // TODO Auto-generated method stub

    }

    public void addArgument(Object argument) {
        // TODO Auto-generated method stub

    }

    public void verify() throws PointcutVerificationException {
        // TODO Auto-generated method stub

    }

    public Object getFirstArgument() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getFirstArgumentName() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object[] getArgumentValues() {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getArgumentNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public void accept(Closure contributionGroupClosure) {
        // TODO Auto-generated method stub

    }

    public boolean fastMatch(GroovyDSLDContext pattern) {

        return true;
    }

    // implement both of these
    public String getPointcutName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPointcutDebugName() {
        // TODO Auto-generated method stub
        return null;
    }

}
