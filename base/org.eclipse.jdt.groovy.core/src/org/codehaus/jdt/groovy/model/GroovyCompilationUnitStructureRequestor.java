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
package org.codehaus.jdt.groovy.model;

import static org.codehaus.groovy.tools.GrapeUtil.getIvyParts;

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

    protected GroovyCompilationUnitStructureRequestor(ICompilationUnit unit, CompilationUnitElementInfo unitInfo, @SuppressWarnings("rawtypes") Map elements) {
        super(unit, unitInfo, elements);
    }

    @Override
    protected IAnnotation acceptAnnotation(Annotation annotation, AnnotatableInfo parentInfo, JavaElement parentHandle) {
        IAnnotation result = super.acceptAnnotation(annotation, parentInfo, parentHandle);

        // check for groovy grapes
        if (result.getElementName().endsWith("Grab")) {
            String group = null, module = null, version = null;
            for (MemberValuePair mvp : annotation.memberValuePairs()) {
                switch (String.valueOf(mvp.name)) {
                case "value":
                    String value = mvp.value.toString();
                    if (value.contains(":") && !value.contains("#")) {
                        Map<String, Object> parts = getIvyParts(value.substring(1, value.length() - 1));
                        group = (String) parts.get("group");
                        module = (String) parts.get("module");
                        version = (String) parts.get("version");
                    }
                    break;
                case "group":
                    group = mvp.value.toString();
                    group = group.substring(1, group.length() - 1);
                    break;
                case "module":
                    module = mvp.value.toString();
                    module = module.substring(1, module.length() - 1);
                    break;
                case "version":
                    version = mvp.value.toString();
                    version = version.substring(1, version.length() - 1);
                    break;
                }
            }
            if (group != null && module != null && version != null) {
                if (grapesContainer == null) {
                    grapesContainer = new GrapesContainer(unit);
                    grapesContainerInfo = new GrapesContainerInfo();
                    newElements.put(grapesContainer, grapesContainerInfo);
                    addToChildren(unitInfo, grapesContainer); // link into unit
                }
                addToChildren(grapesContainerInfo, new GrabDeclaration(grapesContainer, annotation.sourceStart, annotation.sourceEnd, group, module, version));
            }
        }

        return result;
    }

    private   void addToChildren(Object parentInfo, JavaElement handle) {
        @SuppressWarnings("unchecked")
        List<JavaElement> list = (List<JavaElement>) children.computeIfAbsent(parentInfo, x -> new ArrayList<>());
        list.add(handle);
    }

    @Override
    public    void exitCompilationUnit(int unitDeclarationEnd) {
        super.exitCompilationUnit(unitDeclarationEnd);
        if (grapesContainerInfo != null) {
            @SuppressWarnings("unchecked")
            List<JavaElement> elements = (List<JavaElement>) children.get(grapesContainerInfo);
            grapesContainerInfo.children = elements.toArray(new IJavaElement[elements.size()]);
        }
    }

    protected void setParser(SourceElementParser parser) {
        this.parser = parser;
    }
}
