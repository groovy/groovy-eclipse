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
package org.codehaus.groovy.eclipse.dsl.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andrew Eisenberg
 * @created Mar 27, 2010
 */
public class AllDSLTests {
    public static Test suite() {
        TestSuite suite = new TestSuite(AllDSLTests.class.getName());
        suite.addTest(BuiltInDSLInferencingTests.suite());
        suite.addTest(DSLContentAssistTests.suite());
        suite.addTest(DSLInferencingTests.suite());
        suite.addTest(DSLNamedArgContentAssistTests.suite());
        suite.addTest(DSLStoreTests.suite());
        suite.addTest(MetaDSLInferencingTests.suite());
        suite.addTest(PointcutCreationTests.suite());
        suite.addTest(PointcutEvaluationTests.suite());
        suite.addTestSuite(StringObjectVectorTests.class);
        return suite;
    }
}
