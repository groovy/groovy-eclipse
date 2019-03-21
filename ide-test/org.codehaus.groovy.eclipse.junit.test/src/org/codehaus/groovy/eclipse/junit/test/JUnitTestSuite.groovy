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
package org.codehaus.groovy.eclipse.junit.test;

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.junit.Before

abstract class JUnitTestSuite extends GroovyEclipseTestSuite {

    @Before
    final void setUpJUnitTestCase() {
        addJUnit4()
    }

    protected GroovyCompilationUnit addGroovySource(CharSequence contents, String name = 'Hello', String pack = 'p2') {
        GroovyCompilationUnit unit = super.addGroovySource(contents, name, pack)
        waitForIndex()
        return unit
    }
}
