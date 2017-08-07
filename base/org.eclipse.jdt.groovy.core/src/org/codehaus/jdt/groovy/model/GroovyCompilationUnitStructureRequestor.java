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
package org.codehaus.jdt.groovy.model;

import java.util.Map;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.CompilationUnitStructureRequestor;
import org.eclipse.jdt.internal.core.JavaElement;

class GroovyCompilationUnitStructureRequestor extends CompilationUnitStructureRequestor {

    @SuppressWarnings("rawtypes")
    protected GroovyCompilationUnitStructureRequestor(ICompilationUnit unit, CompilationUnitElementInfo unitInfo, Map newElements) {
        super(unit, unitInfo, newElements);
    }

    @Override
    protected IAnnotation acceptAnnotation(Annotation annotation, AnnotatableInfo parentInfo, JavaElement parentHandle) {
        IAnnotation result = super.acceptAnnotation(annotation, parentInfo, parentHandle);
        return result;
    }

    @Override
    public void exitCompilationUnit(int unitDeclarationEnd) {
        super.exitCompilationUnit(unitDeclarationEnd);
    }

    protected void setParser(SourceElementParser parser) {
        this.parser = parser;
    }
}
