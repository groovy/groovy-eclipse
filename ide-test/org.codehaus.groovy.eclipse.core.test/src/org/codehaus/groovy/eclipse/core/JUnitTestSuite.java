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
package org.codehaus.groovy.eclipse.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.core.context.impl.ClassContextFactoryTests;
import org.codehaus.groovy.eclipse.core.context.impl.ScriptContextFactoryTests;
import org.codehaus.groovy.eclipse.core.impl.StringSourceBufferTests;
import org.codehaus.groovy.eclipse.core.type.TypeEvaluatorTests;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinderTests;
import org.codehaus.groovy.eclipse.core.util.TokenStreamTests;

/**
 * This suite contains all the tests that can be run as pure Junit tests
 */
public class JUnitTestSuite {
	
	public static Test suite() {
		
		TestSuite suite = new TestSuite("All tests for org.codehaus.groovy.eclipse.core not requiring Eclipse.");
		
		//$JUnit-BEGIN$
		suite.addTestSuite(ClassContextFactoryTests.class);
		suite.addTestSuite(ScriptContextFactoryTests.class);
		suite.addTestSuite(StringSourceBufferTests.class);
		suite.addTestSuite(TokenStreamTests.class);
		suite.addTestSuite(ExpressionFinderTests.class);
		suite.addTestSuite(TypeEvaluatorTests.class);
		//This test needs an eclipse instance to run (should be deleted)
		//suite.addTestSuite(ErrorRecoveryTests.class);
		//$JUnit-END$
		
		return suite;
	}
}
