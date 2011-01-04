 /*
 * Copyright 2003-2010 the original author or authors.
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.test.actions.AliasingOrganizeImportsTest;
import org.codehaus.groovy.eclipse.test.actions.ConvertToJavaOrGroovyActionTest;
import org.codehaus.groovy.eclipse.test.actions.GroovyNatureActionTestCase;
import org.codehaus.groovy.eclipse.test.actions.OrganizeImportsTest;
import org.codehaus.groovy.eclipse.test.actions.SaveParticipantRegistryTest;
import org.codehaus.groovy.eclipse.test.adapters.GroovyFileAdapterFactoryTestCase;
import org.codehaus.groovy.eclipse.test.adapters.GroovyIFileEditorInputAdapterFactoryTestCase;
import org.codehaus.groovy.eclipse.test.adapters.IsMainTesterTests;
import org.codehaus.groovy.eclipse.test.core.util.ExpressionFinderTestCase;
import org.codehaus.groovy.eclipse.test.debug.BreakpointLocationTests;
import org.codehaus.groovy.eclipse.test.debug.ConsoleLineTrackerTests;
import org.codehaus.groovy.eclipse.test.debug.DebugBreakpointsTests;
import org.codehaus.groovy.eclipse.test.debug.GroovyLauncherShortcutTests;
import org.codehaus.groovy.eclipse.test.ui.BracketInserterTests;
import org.codehaus.groovy.eclipse.test.ui.ErrorLogTest;
import org.codehaus.groovy.eclipse.test.ui.GroovyAutoIndenterTests;
import org.codehaus.groovy.eclipse.test.ui.GroovyAutoIndenterTests2;
import org.codehaus.groovy.eclipse.test.ui.GroovyTagScannerTests;
import org.codehaus.groovy.eclipse.test.ui.HighlightingExtenderTests;
import org.codehaus.groovy.eclipse.test.wizards.NewGroovyTestCaseWizardTest;
import org.codehaus.groovy.eclipse.test.wizards.NewGroovyTypeWizardTest;
import org.codehaus.groovy.eclipse.ui.search.FindOccurrencesTests;

/**
 * Suite needs to be run as eclipse plugin test
 */
public class AllUITests {
	public static Test suite() throws Exception {
		final TestSuite suite = new TestSuite(AllUITests.class.getName());
        suite.addTestSuite(GroovyAutoIndenterTests.class);
        suite.addTestSuite(GroovyAutoIndenterTests2.class);
		suite.addTestSuite(ErrorLogTest.class);
		suite.addTestSuite(GroovyLauncherShortcutTests.class);
		suite.addTestSuite(GroovyNatureActionTestCase.class);
		suite.addTestSuite(GroovyFileAdapterFactoryTestCase.class);
		suite.addTestSuite(GroovyIFileEditorInputAdapterFactoryTestCase.class);
		suite.addTestSuite(IsMainTesterTests.class);
		suite.addTestSuite(ExpressionFinderTestCase.class);
		suite.addTestSuite(GroovyTagScannerTests.class);
		suite.addTestSuite(DebugBreakpointsTests.class);
		suite.addTestSuite(BreakpointLocationTests.class);
		suite.addTestSuite(OrganizeImportsTest.class);
		suite.addTestSuite(AliasingOrganizeImportsTest.class);
		suite.addTestSuite(SaveParticipantRegistryTest.class);
		suite.addTestSuite(ConvertToJavaOrGroovyActionTest.class);
		suite.addTestSuite(ConsoleLineTrackerTests.class);
		suite.addTestSuite(HighlightingExtenderTests.class);
		suite.addTestSuite(BracketInserterTests.class);
		suite.addTestSuite(NewGroovyTypeWizardTest.class);
        suite.addTestSuite(NewGroovyTestCaseWizardTest.class);
        suite.addTest(FindOccurrencesTests.suite());
		return suite;
	}
}
