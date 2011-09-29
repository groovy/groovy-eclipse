 /*
 * Copyright 2003-2009 the original author or authors.
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

/**
 * @author Andrew Eisenberg
 * @created Oct 22, 2009
 *
 */
public class AllSearchTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("Search Tests"); //$NON-NLS-1$
        suite.addTestSuite(TypeReferenceSearchTests.class);
        suite.addTestSuite(FieldReferenceSearchTests.class);
        suite.addTestSuite(MethodReferenceSearchTests.class);
        suite.addTestSuite(CategorySearchTests.class);
        suite.addTestSuite(InferencingTests.class);
        suite.addTestSuite(StaticInferencingTests.class);
        suite.addTestSuite(GenericInferencingTests.class);
        suite.addTestSuite(DGMInferencingTests.class);
        suite.addTestSuite(LocalVariableReferenceSearchTests.class);
        suite.addTestSuite(JDTPropertyNodeInferencingTests.class);
        suite.addTestSuite(DeclarationInferencingTests.class);
        return suite;
    }
}
