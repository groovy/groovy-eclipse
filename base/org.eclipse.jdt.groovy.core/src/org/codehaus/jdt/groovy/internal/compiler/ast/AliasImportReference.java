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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.ImportReference;

/**
 * Represents a groovy aliased import, something like 'import a.b.c.D as Foo'
 * where Foo will be the alias.  The JDT creates a map from the simple name for
 * the import to the full type and for a normal import the simple name is the
 * final part of the import.  An aliased import can simply return a different
 * simple name to JDT when it is building this map.
 *
 * @see org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope#checkAndSetImports()
 * @see org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope#faultInImports()
 */
public class AliasImportReference extends ImportReference {

    // eg. 'Foo' in 'import a.b.c.D as Foo'
    private final char[] alias;

    public AliasImportReference(char[] alias, char[][] tokens, long[] sourcePositions, boolean onDemand, int modifiers) {
        super(tokens, sourcePositions, onDemand, modifiers);
        this.alias = alias;
    }

    @Override
    public char[][] getImportName() {
        return super.getImportName();
    }

    @Override
    public char[] getSimpleName() {
        return alias;
    }

    @Override
    public String toString() {
        return print(0, new StringBuffer(42)).append(" as ").append(alias).toString();
    }
}
