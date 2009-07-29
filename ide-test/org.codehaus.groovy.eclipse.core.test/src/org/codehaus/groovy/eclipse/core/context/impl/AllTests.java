/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.context.impl;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{
    public static Test suite()
    {
        final TestSuite suite = new TestSuite( "Test for " + AllTests.class.getPackage().getName() );
        //$JUnit-BEGIN$
        suite.addTestSuite( ScriptContextFactoryTests.class );
        suite.addTestSuite( ClassContextFactoryTests.class );
        //$JUnit-END$
        return suite;
    }
}
