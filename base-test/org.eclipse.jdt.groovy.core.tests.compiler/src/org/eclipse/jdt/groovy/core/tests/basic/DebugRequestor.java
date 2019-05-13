/*
 * Copyright 2009-2019 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.IGroovyDebugRequestor;

class DebugRequestor implements IGroovyDebugRequestor {

    Map<String, GroovyCompilationUnitDeclaration> declarations = new HashMap<>();

    @Override
    public void acceptCompilationUnitDeclaration(GroovyCompilationUnitDeclaration gcuDeclaration) {
        String filename = String.valueOf(gcuDeclaration.getFileName());
        filename = filename.substring(filename.lastIndexOf(File.separator) + 1); // Filename now being just Foo.groovy or Bar.java

        declarations.put(filename, gcuDeclaration);
    }
}
