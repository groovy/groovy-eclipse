/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codebrowsing.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 */
public class AllBrowsingTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("Run of all Browsing Tests");
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
        return suite;
    }
}
