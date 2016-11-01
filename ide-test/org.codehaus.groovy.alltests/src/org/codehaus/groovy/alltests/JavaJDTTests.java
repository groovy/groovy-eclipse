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

import org.eclipse.jdt.core.tests.builder.*;

/**
 * All JDT regression tests.
 *
 * @author Eric Milles
 * @created Sep 21, 2016
 */
public class JavaJDTTests {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JavaJDTTests.class.getName());

        // JDT Builder regression tests
        suite.addTest(AbstractMethodTests.suite());
        suite.addTest(BasicBuildTests.suite());
        suite.addTest(BuildpathTests.suite());
        suite.addTest(CopyResourceTests.suite());
        suite.addTest(DependencyTests.suite());
        suite.addTest(EfficiencyTests.suite());
        suite.addTest(ErrorsTests.suite());
        suite.addTest(ExecutionTests.suite());
        suite.addTest(GetResourcesTests.suite());
        suite.addTest(IncrementalTests.suite());
        suite.addTest(Java50Tests.suite());
        suite.addTest(MultiProjectTests.suite());
        suite.addTest(MultiSourceFolderAndOutputFolderTests.suite());
        suite.addTest(OutputFolderTests.suite());
        suite.addTest(PackageInfoTest.suite());
        suite.addTest(PackageTests.suite());
        suite.addTest(ParticipantBuildTests.suite());
        suite.addTest(StaticFinalTests.suite());

        // JDT Compiler regression tests
        suite.addTest(org.eclipse.jdt.core.tests.eval.TestAll.suite());
        suite.addTest(org.eclipse.jdt.core.tests.compiler.parser.TestAll.suite());
        suite.addTest(org.eclipse.jdt.core.tests.compiler.regression.TestAll.suite());

        return suite;
    }
}
