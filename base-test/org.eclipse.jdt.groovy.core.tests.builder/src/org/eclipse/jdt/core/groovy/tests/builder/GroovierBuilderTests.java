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
package org.eclipse.jdt.core.groovy.tests.builder;

import org.eclipse.jdt.core.tests.builder.BuilderTests;

/**
 * Extension of the Builder Tests that can use generics.  Adds helpers suitable for groovy projects too.
 *
 * @author Andy Clement
 *
 */
public class GroovierBuilderTests extends BuilderTests {

    public GroovierBuilderTests(String name) {
        super(name);
    }

    // varargs
    protected void expectingCompiledClassesV(String... compiledClasses) {
        expectingCompiledClasses(compiledClasses);
    }
}
