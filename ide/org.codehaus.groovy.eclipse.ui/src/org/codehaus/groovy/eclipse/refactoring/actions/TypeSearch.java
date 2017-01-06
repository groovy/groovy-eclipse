/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector;

/**
 * Use a SearchEngine to look for the Java types
 * This will not find inner types, however
 *
 * @author Andrew Eisenberg
 * @author Nieraj Singh
 */
public class TypeSearch {
    /**
     * From {@link OrganizeImportsOperation.TypeReferenceProcessor.UnresolvedTypeData}
     */
    public static class UnresolvedTypeData {
        final String ref;
        final boolean isAnnotation;
        final ISourceRange range;
        final List<TypeNameMatch> foundInfos = new LinkedList<TypeNameMatch>();

        public UnresolvedTypeData(String ref, boolean annotation, ISourceRange range) {
            this.ref = ref;
            this.isAnnotation = annotation;
            this.range = range;
        }

        public void addInfo(TypeNameMatch info) {
            for (int i = foundInfos.size() - 1; i >= 0; i -= 1) {
                TypeNameMatch curr= foundInfos.get(i);
                if (curr.getTypeContainerName().equals(info.getTypeContainerName())) {
                    return; // not added; already contains type with same name
                }
            }
            foundInfos.add(info);
        }

        public List<TypeNameMatch> getFoundInfos() {
            return foundInfos;
        }
    }

    /**
     * Use a SearchEngine to look for the types.
     * <p>
     * NOTE: This will not find inner types.
     *
     * @see OrganizeImportsOperation.TypeReferenceProcessor#process(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void searchForTypes(GroovyCompilationUnit unit, Map<String, UnresolvedTypeData> missingTypes, IProgressMonitor monitor) throws JavaModelException {
        char[][] allTypes = new char[missingTypes.size()][];
        int i = 0;
        for (String simpleName : missingTypes.keySet()) {
            allTypes[i++] = simpleName.toCharArray();
        }
        final List<TypeNameMatch> typesFound = new ArrayList<TypeNameMatch>();
        TypeNameMatchCollector collector = new TypeNameMatchCollector(typesFound);
        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {unit.getJavaProject()});
        int policy = (monitor == null ? IJavaSearchConstants.FORCE_IMMEDIATE_SEARCH : IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH);
        new SearchEngine().searchAllTypeNames(null, allTypes, scope, collector, policy, monitor);

        for (TypeNameMatch match : typesFound) {
            UnresolvedTypeData data = missingTypes.get(match.getSimpleTypeName());
            if (data == null) {
                GroovyCore.logException("GRECLIPSE-735: Match not found in missing types: " + match.getFullyQualifiedName(), new Exception());
                continue;
            }
            if (isOfKind(match, data.isAnnotation)) {
                data.addInfo(match);
            }
        }
    }

    /**
     * If looking for an annotation, then filter out non-annoations,
     * otherwise everything is acceptable.
     */
    protected boolean isOfKind(TypeNameMatch match, boolean isAnnotation) {
        boolean isRegularAnnotation = isAnnotation ? Flags.isAnnotation(match.getModifiers()) : true;

        //Annotations that are annotated with {@link AnnotationCollector} are not treated as annotations, so additional check is required
        boolean isCollectedByAnnotationCollector = (match.getType().getAnnotation("AnnotationCollector") != null);

        return isRegularAnnotation || isCollectedByAnnotationCollector;
    }
}
