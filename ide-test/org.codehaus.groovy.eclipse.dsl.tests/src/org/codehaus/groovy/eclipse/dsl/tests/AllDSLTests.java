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
package org.codehaus.groovy.eclipse.dsl.tests;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

public final class AllDSLTests {
    public static Test suite() {
        TestSuite suite = new TestSuite(AllDSLTests.class.getName());
        if (!Boolean.getBoolean("greclipse.dsld.disabled")) {
            suite.addTest(BuiltInDSLInferencingTests.suite());
            suite.addTest(new JUnit4TestAdapter(DSLContentAssistTests.class));
            suite.addTest(DSLInferencingTests.suite());
            suite.addTest(new JUnit4TestAdapter(DSLNamedArgContentAssistTests.class));
            suite.addTest(DSLStoreTests.suite());
            suite.addTest(MetaDSLInferencingTests.suite());
            suite.addTest(PointcutCreationTests.suite());
            suite.addTest(PointcutEvaluationTests.suite());
            suite.addTest(new JUnit4TestAdapter(StringObjectVectorTests.class));
        }
        return suite;
    }
}
