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

package org.codehaus.groovy.eclipse.junit.test;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 */
public class AllJUnitTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("Run of all JUnit Tests");
        suite.addTestSuite(JUnit3TestFinderTests.class);
        suite.addTestSuite(JUnit4TestFinderTests.class);
        suite.addTestSuite(MainMethodFinderTests.class);
        return suite;
    }
}
