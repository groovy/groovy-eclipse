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
package org.codehaus.groovy.eclipse.test;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.test.actions.GroovyNatureActionTestCase;
import org.codehaus.groovy.eclipse.test.adapters.GroovyFileAdapterFactoryTestCase;
import org.codehaus.groovy.eclipse.test.adapters.GroovyIFileEditorInputAdapterFactoryTestCase;
import org.codehaus.groovy.eclipse.test.adapters.IsMainTesterTests;
import org.codehaus.groovy.eclipse.test.core.types.TypeEvaluatorTestCase;
import org.codehaus.groovy.eclipse.test.core.util.ExpressionFinderTestCase;
import org.codehaus.groovy.eclipse.test.debug.DebugBreakpointsTests;
import org.codehaus.groovy.eclipse.test.ui.FoldingPositionProviderTest;
import org.codehaus.groovy.eclipse.test.ui.GroovyTagScannerTests;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

/**
 * 
 * Suite needs to be run as eclipse plugin test
 *
 */

public class AllUITests {
	public static final Bundle BUNDLE = Activator.bundle();
	public static String BUNDLE_ROOT = null;
	static {
		try {
			BUNDLE_ROOT = FileLocator.resolve(BUNDLE.getEntry("/")).getFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static final String PACKAGE_ROOT = BUNDLE_ROOT + "/src/"
			+ AllUITests.class.getPackage().getName().replaceAll("\\.", "/")
			+ "/";

	public static Test suite() throws Exception {
		final TestSuite suite = new TestSuite("Test for "
				+ AllUITests.class.getPackage().getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(GroovyLaunchShortuctTestCase.class);
		suite.addTestSuite(GroovyNatureActionTestCase.class);
		suite.addTestSuite(GroovyFileAdapterFactoryTestCase.class);
		suite.addTestSuite(GroovyIFileEditorInputAdapterFactoryTestCase.class);
		suite.addTestSuite(IsMainTesterTests.class);
		suite.addTestSuite(FoldingPositionProviderTest.class);
		suite.addTest(org.codehaus.groovy.eclipse.test.core.types.AllTests.suite());
		suite.addTest(org.codehaus.groovy.eclipse.test.core.util.AllTests.suite());
		suite.addTestSuite(TypeEvaluatorTestCase.class);
		suite.addTestSuite(ExpressionFinderTestCase.class);
		suite.addTestSuite(GroovyTagScannerTests.class);
		suite.addTestSuite(DebugBreakpointsTests.class);
		
		// $JUnit-END$
		
		return suite;
	}
}
