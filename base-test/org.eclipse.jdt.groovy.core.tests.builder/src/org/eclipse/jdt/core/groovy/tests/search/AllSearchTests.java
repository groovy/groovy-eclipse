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
package org.eclipse.jdt.core.groovy.tests.search;

import junit.framework.Test;
import junit.framework.TestSuite;

public final class AllSearchTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AllSearchTests.class.getName());
        suite.addTestSuite(ArrayInferencingTests.class);
        suite.addTestSuite(BinarySearchTests.class);
        suite.addTestSuite(CategorySearchTests.class);
        suite.addTestSuite(DeclarationInferencingTests.class);
        suite.addTestSuite(DGMInferencingTests.class);
        suite.addTestSuite(FieldReferenceSearchTests.class);
        suite.addTestSuite(GenericInferencingTests.class);
        suite.addTestSuite(GenericsMappingTest.class);
        suite.addTestSuite(Groovy20InferencingTests.class);
        suite.addTestSuite(Groovy21InferencingTests.class);
        suite.addTestSuite(InferencingTests.class);
        suite.addTestSuite(JDTPropertyNodeInferencingTests.class);
        suite.addTestSuite(LocalVariableReferenceSearchTests.class);
        suite.addTestSuite(MethodReferenceSearchTests.class);
        suite.addTestSuite(OperatorOverloadingInferencingTests.class);
        suite.addTestSuite(StaticInferencingTests.class);
        suite.addTestSuite(SyntheticAccessorInferencingTests.class);
        suite.addTestSuite(TypeReferenceSearchTests.class);
        return suite;
    }
}
