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

import org.codehaus.groovy.eclipse.codeassist.tests.AllCompletionTests;
import org.codehaus.groovy.eclipse.codebrowsing.tests.AllBrowsingTests;
import org.codehaus.groovy.eclipse.core.AllCoreTests;
import org.codehaus.groovy.eclipse.dsl.tests.AllDSLTests;
import org.codehaus.groovy.eclipse.junit.test.AllJUnitTests;
import org.codehaus.groovy.eclipse.quickfix.test.AllQuickFixTests;
import org.codehaus.groovy.eclipse.refactoring.test.AllRefactoringTests;
import org.codehaus.groovy.eclipse.test.AllUITests;
import org.codehaus.groovy.frameworkadapter.util.ResolverActivator;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 * Groovy plugin tests
 */
public class AllGroovyTests {
    public static Test suite() throws Exception {
        // ensure that the compiler chooser starts up
        GroovyTestSuiteSupport.initializeCompilerChooser();

        //Must use sys err if you wanna see the messages in the build log. sysout seems to disapear without a trace on
        // the build server.
        System.err.println("=========== AllGroovyTests ===============");
        System.err.println("active Groovy version             = "+ResolverActivator.getDefault().getChooser().getActiveVersion());
        System.err.println("active Groovy version (specified) = "+ResolverActivator.getDefault().getChooser().getActiveSpecifiedVersion());
        System.err.println("------------------------------------------");

        TestSuite suite = new TestSuite(AllGroovyTests.class.getName());
        suite.addTestSuite(SanityTest.class);
        suite.addTest(AllUITests.suite()); //This must be first because of 'ErrorLogTest' inside of it.
        suite.addTest(AllCoreTests.suite());
        suite.addTest(AllJUnitTests.suite());
        suite.addTest(AllCompletionTests.suite());
        suite.addTest(AllBrowsingTests.suite());
        suite.addTest(AllRefactoringTests.suite());
        suite.addTest(AllQuickFixTests.suite());
        suite.addTest(AllDSLTests.suite());
        return suite;
    }
}
