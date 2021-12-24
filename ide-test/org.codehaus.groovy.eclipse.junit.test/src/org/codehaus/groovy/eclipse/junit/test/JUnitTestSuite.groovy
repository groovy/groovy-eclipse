/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.eclipse.junit.test

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.junit.Before

@AutoFinal @CompileStatic
abstract class JUnitTestSuite extends GroovyEclipseTestSuite {

    @Before
    final void setUpJUnitTestCase() {
        addJUnit(4)
    }

    @Override
    protected GroovyCompilationUnit addGroovySource(CharSequence contents, String name = nextUnitName(), String pack = 'p') {
        GroovyCompilationUnit unit = super.addGroovySource(contents, name, pack)
        SynchronizationUtils.waitForIndexingToComplete()
        return unit
    }
}
