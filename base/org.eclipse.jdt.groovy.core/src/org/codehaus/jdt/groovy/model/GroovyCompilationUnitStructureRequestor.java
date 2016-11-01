/*
 * Copyright 2009-2016 the original author or authors.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.CompilationUnitStructureRequestor;
import org.eclipse.jdt.internal.core.ImportContainer;
import org.eclipse.jdt.internal.core.ImportContainerInfo;

/**
 * Stub class that makes the constructor accessible to GroovyCompilationUnit
 *
 * @author Andrew Eisenberg
 * @created Jun 11, 2009
 */
class GroovyCompilationUnitStructureRequestor extends CompilationUnitStructureRequestor {

    protected GroovyCompilationUnitStructureRequestor(ICompilationUnit unit, CompilationUnitElementInfo unitInfo, Map<ImportContainer, ImportContainerInfo> newElements) {
        super(unit, unitInfo, newElements);
    }

    void setParser(SourceElementParser parser) {
        this.parser = parser;
    }
}
