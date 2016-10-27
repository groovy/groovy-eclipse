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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 */
public class AllBrowsingTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AllBrowsingTests.class.getName());
        suite.addTestSuite(CodeSelectMethodsTest.class);
        suite.addTestSuite(CodeSelectFieldsTest.class);
        suite.addTestSuite(CodeSelectTypesTest.class);
        suite.addTestSuite(CodeSelectCategoriesTest.class);
        suite.addTestSuite(CodeSelectGenericsTest.class);
        suite.addTestSuite(FindSurroundingNodeTests.class);
        suite.addTestSuite(FindAllOccurrencesVisitorTests.class);
        suite.addTestSuite(ASTFragmentTests.class);
        suite.addTestSuite(IsSameExpressionTests.class);
        suite.addTestSuite(PartialVisitTest.class);
        suite.addTestSuite(CodeSelectFieldsPropertiesTest.class);
        suite.addTestSuite(CodeSelectLocalTest.class);
        suite.addTestSuite(CodeSelectStaticImportsTest.class);
        suite.addTestSuite(JDTAstPositionTest.class);
        return suite;
    }
}
