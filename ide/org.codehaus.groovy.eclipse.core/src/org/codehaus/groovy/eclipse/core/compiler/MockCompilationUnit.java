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
package org.codehaus.groovy.eclipse.core.compiler;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

class MockCompilationUnit implements ICompilationUnit {

    private char[] contents;
    private char[] fileName;

    MockCompilationUnit(char[] contents, char[] fileName) {
        this.contents = contents;
        this.fileName = fileName;
    }

    @Override
    public char[] getContents() {
        return contents;
    }

    @Override
    public char[] getMainTypeName() {
        return CharOperation.NO_CHAR;
    }

    @Override
    public char[][] getPackageName() {
        return CharOperation.NO_CHAR_CHAR;
    }

    @Override
    public char[] getFileName() {
        return fileName;
    }

    @Override
    public boolean ignoreOptionalProblems() {
        return false;
    }
}
