/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

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

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 * Groovy plugin tests
 */
public class AllGroovyTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("All Groovy Tests"); //$NON-NLS-1$
        suite.addTest(AllUITests.suite());
        suite.addTest(AllCoreTests.suite());
        suite.addTest(AllJUnitTests.suite());
        suite.addTest(AllCompletionTests.suite());
        suite.addTest(AllBrowsingTests.suite());
        suite.addTest(AllRefactoringTests.suite());
        suite.addTest(AllQuickFixTests.suite());
//        suite.addTest(AllDSLTests.suite());
        return suite;
    }
}
