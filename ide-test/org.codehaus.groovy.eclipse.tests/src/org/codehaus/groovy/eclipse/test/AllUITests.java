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
package org.codehaus.groovy.eclipse.test;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.test.actions.GroovyNatureActionTestCase;
import org.codehaus.groovy.eclipse.test.actions.OrganizeImportsTest;
import org.codehaus.groovy.eclipse.test.actions.SaveParticipantRegistryTest;
import org.codehaus.groovy.eclipse.test.adapters.GroovyFileAdapterFactoryTestCase;
import org.codehaus.groovy.eclipse.test.adapters.GroovyIFileEditorInputAdapterFactoryTestCase;
import org.codehaus.groovy.eclipse.test.adapters.IsMainTesterTests;
import org.codehaus.groovy.eclipse.test.core.types.TypeEvaluatorTestCase;
import org.codehaus.groovy.eclipse.test.core.util.ExpressionFinderTestCase;
import org.codehaus.groovy.eclipse.test.debug.BreakpointLocationTests;
import org.codehaus.groovy.eclipse.test.debug.DebugBreakpointsTests;
import org.codehaus.groovy.eclipse.test.ui.ErrorLogTest;
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
		suite.addTestSuite(ErrorLogTest.class);
		suite.addTestSuite(GroovyLaunchShortuctTestCase.class);
		suite.addTestSuite(GroovyNatureActionTestCase.class);
		suite.addTestSuite(GroovyFileAdapterFactoryTestCase.class);
		suite.addTestSuite(GroovyIFileEditorInputAdapterFactoryTestCase.class);
		suite.addTestSuite(IsMainTesterTests.class);
		suite.addTest(org.codehaus.groovy.eclipse.test.core.types.AllTests.suite());
		suite.addTest(org.codehaus.groovy.eclipse.test.core.util.AllTests.suite());
		suite.addTestSuite(TypeEvaluatorTestCase.class);
		suite.addTestSuite(ExpressionFinderTestCase.class);
		suite.addTestSuite(GroovyTagScannerTests.class);
		suite.addTestSuite(DebugBreakpointsTests.class);
		suite.addTestSuite(BreakpointLocationTests.class);
		suite.addTestSuite(OrganizeImportsTest.class);
		suite.addTestSuite(SaveParticipantRegistryTest.class);
		
		// $JUnit-END$
		
		return suite;
	}
}
