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
package org.codehaus.groovy.eclipse.junit.test;

import org.codehaus.groovy.eclipse.test.EclipseTestSetup
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName

abstract class JUnitTestCase {

    @BeforeClass
    static final void setUpTestSuite() {
        new EclipseTestSetup(null).setUp()
        EclipseTestSetup.addJUnit4()
    }

    @AfterClass
    static final void tearDownTestSuite() {
        new EclipseTestSetup(null).tearDown()
    }

    @Rule
    public TestName test = new TestName()

    @Before
    final void setUpTestCase() {
        println '----------------------------------------'
        println 'Starting: ' + test.getMethodName()
    }

    @After
    final void tearDownTestCase() {
        EclipseTestSetup.removeSources()
    }

    protected GroovyCompilationUnit addGroovySource(CharSequence contents, String name = 'Hello', String pack = 'p2') {
        EclipseTestSetup.addGroovySource(contents, name, pack)
    }
}
