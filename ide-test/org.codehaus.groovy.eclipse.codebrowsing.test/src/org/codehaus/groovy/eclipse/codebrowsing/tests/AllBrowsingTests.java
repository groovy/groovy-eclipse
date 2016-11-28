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

import org.codehaus.groovy.eclipse.test.EclipseTestSetup;

public final class AllBrowsingTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("AST Visitation");
        suite.addTestSuite(ASTFragmentTests.class);
        suite.addTestSuite(FindSurroundingNodeTests.class);
        suite.addTestSuite(JDTAstPositionTests.class);
        suite.addTestSuite(PartialVisitTests.class);
        Test astVisitorTests = new EclipseTestSetup(suite);

        suite = new TestSuite("Code Selection");
        suite.addTestSuite(CodeSelectAttributesTests.class);
        suite.addTestSuite(CodeSelectCategoriesTests.class);
        suite.addTestSuite(CodeSelectFieldsTests.class);
        suite.addTestSuite(CodeSelectGenericsTests.class);
        suite.addTestSuite(CodeSelectImportsTests.class);
        suite.addTestSuite(CodeSelectLocalTests.class);
        suite.addTestSuite(CodeSelectMethodsTests.class);
        suite.addTestSuite(CodeSelectPackageTests.class);
        suite.addTestSuite(CodeSelectPropertiesTests.class);
        suite.addTestSuite(CodeSelectStaticImportsTests.class);
        suite.addTestSuite(CodeSelectTypesTests.class);
        Test codeSelectTests = new EclipseTestSetup(suite);

        suite = new TestSuite(AllBrowsingTests.class.getName());
        suite.addTest(astVisitorTests);
        suite.addTest(codeSelectTests);
        suite.addTestSuite(IsSameExpressionTests.class);
        suite.addTestSuite(FindAllOccurrencesVisitorTests.class);
        return suite;
    }
}
