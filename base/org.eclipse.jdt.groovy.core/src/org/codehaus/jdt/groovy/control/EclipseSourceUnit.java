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
package org.codehaus.jdt.groovy.control;

import static org.eclipse.core.filesystem.URIUtil.toURI;

import java.net.URI;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.resources.IFile;

public class EclipseSourceUnit extends SourceUnit {

    private final IFile file;
    public final JDTResolver resolver;

    public EclipseSourceUnit(/*@Nullable*/ IFile file, String filePath, char[] sourceCode,
        CompilerConfiguration compilerConfig, GroovyClassLoader classLoader, ErrorCollector errorCollector, JDTResolver resolver) {

        super(filePath, new CharArrayReaderSource(sourceCode) {
            @Override public URI getURI() {
                return toURI(filePath);
            }
        }, compilerConfig, classLoader, errorCollector);

        this.file = file;
        this.resolver = resolver;
    }

    /**
     * Will be {@code null} if workspace is closed (ie- batch compilation mode).
     */
    public IFile getEclipseFile() {
        return file;
    }

    @Override
    public void convert() throws CompilationFailedException {
        super.convert();
        cst = null;
    }

    @Override
    public String toString() {
        return "EclipseSourceUnit(" + getName() + ")";
    }
}
