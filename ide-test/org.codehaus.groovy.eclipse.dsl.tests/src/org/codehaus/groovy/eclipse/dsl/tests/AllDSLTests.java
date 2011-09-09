/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 * @author Andrew Eisenberg
 * @created Mar 27, 2010
 */
public class AllDSLTests {
    public static Test suite() {
        final TestSuite suite = new TestSuite(AllDSLTests.class.getPackage().getName());

        suite.addTest(PointcutCreationTests.suite());
        suite.addTest(PointcutEvaluationTests.suite());
        suite.addTest(MetaDSLInferencingTests.suite());
        suite.addTest(DSLInferencingTests.suite());
        suite.addTest(BuiltInDSLInferencingTests.suite());
        suite.addTest(DSLStoreTests.suite());
        suite.addTestSuite(StringObjectVectorTests.class);
        suite.addTestSuite(DSLContentAssistTests.class);
        suite.addTestSuite(DSLNamedArgContentAssistTests.class);
        return suite;
    }

}
