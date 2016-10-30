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
package org.codehaus.groovy.alltests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.groovy.core.tests.basic.GroovySimpleTest;

/**
 * Tycho refuses to run tests directly from another bundle, so we will just have to
 * create a class here that runs them.
 */
public class SmokeTests {
    public static Test suite() throws Exception {
        // ensure that the compiler chooser starts up
        GroovyTestSuiteSupport.initializeCompilerChooser();

        TestSuite suite = new TestSuite(SmokeTests.class.getName());
        suite.addTest(GroovySimpleTest.suite());
        suite.addTest(org.codehaus.groovy.eclipse.codeassist.tests.AllCompletionTests.suite());
        //Can add more tests here.

        return suite;
    }
}
