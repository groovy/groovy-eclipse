/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.jdt.groovy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.CompilationUnitStructureRequestor;
import org.eclipse.jdt.internal.core.JavaElement;

class GroovyCompilationUnitStructureRequestor extends CompilationUnitStructureRequestor {

    protected GrapesContainer grapesContainer;
    protected GrapesContainerInfo grapesContainerInfo;

    @SuppressWarnings("rawtypes")
    protected GroovyCompilationUnitStructureRequestor(ICompilationUnit unit, CompilationUnitElementInfo unitInfo, Map newElements) {
        super(unit, unitInfo, newElements);
    }

    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    protected IAnnotation acceptAnnotation(Annotation annotation, AnnotatableInfo parentInfo, JavaElement parentHandle) {
        IAnnotation result = super.acceptAnnotation(annotation, parentInfo, parentHandle);

        // check for groovy grapes
        if (result.getElementName().endsWith("Grab")) {
            String group = null, module = null, version = null;
            for (MemberValuePair mvp : annotation.memberValuePairs()) {
                String key = String.valueOf(mvp.name);
                if (key.equals("group")) {
                    group = mvp.value.toString();
                    group = group.substring(1, group.length() - 1);
                } else if (key.equals("module")) {
                    module = mvp.value.toString();
                    module = module.substring(1, module.length() - 1);
                } else if (key.equals("version")) {
                    version = mvp.value.toString();
                    version = version.substring(1, version.length() - 1);
                }
            }
            if (group != null && module != null && version != null) {
                if (grapesContainer == null) {
                    grapesContainer = new GrapesContainer(unit);
                    grapesContainerInfo = new GrapesContainerInfo();
                    children.put(grapesContainerInfo, new ArrayList());
                    ((List) children.get(unitInfo)).add(grapesContainer);
                    newElements.put(grapesContainer, grapesContainerInfo);
                }
                ((List) children.get(grapesContainerInfo)).add(
                    new GrabDeclaration(grapesContainer, annotation.sourceStart, annotation.sourceEnd, group, module, version));
            }
        }

        return result;
    }

    @Override
    public void exitCompilationUnit(int unitDeclarationEnd) {
        super.exitCompilationUnit(unitDeclarationEnd);
        if (grapesContainerInfo != null) {
            @SuppressWarnings("unchecked")
            List<IJavaElement> grapes = (List<IJavaElement>) children.get(grapesContainerInfo);
            grapesContainerInfo.children = grapes.toArray(new IJavaElement[grapes.size()]);
        }
    }

    protected void setParser(SourceElementParser parser) {
        this.parser = parser;
    }
}
