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
import org.codehaus.groovy.eclipse.test.ui.GroovyPartitionScannerTests;
import org.codehaus.groovy.eclipse.test.ui.GroovyTagScannerTests;
import org.codehaus.groovy.eclipse.test.ui.HighlightingExtenderTests;
import org.codehaus.groovy.eclipse.test.ui.OutlineExtenderTests;
import org.codehaus.groovy.eclipse.test.ui.SemanticHighlightingTests;
import org.codehaus.groovy.eclipse.test.wizards.NewGroovyTestCaseWizardTest;
import org.codehaus.groovy.eclipse.test.wizards.NewGroovyTypeWizardTest;
import org.codehaus.groovy.eclipse.ui.search.FindOccurrencesTests;

public class AllUITests {
    public static Test suite() {
        TestSuite suite = new TestSuite(AllUITests.class.getName());

        suite.addTestSuite(ErrorLogTest.class); //This must be first or it will pick up garbage left in log by other tests.

        // actions
        suite.addTestSuite(AliasingOrganizeImportsTest.class);
        suite.addTestSuite(ConvertToJavaOrGroovyActionTest.class);
        suite.addTestSuite(GroovyNatureActionTestCase.class);
        suite.addTestSuite(OrganizeImportsTest.class);
        suite.addTestSuite(SaveParticipantRegistryTest.class);

        // adapters
        suite.addTestSuite(GroovyFileAdapterFactoryTestCase.class);
        suite.addTestSuite(GroovyIFileEditorInputAdapterFactoryTestCase.class);
        suite.addTestSuite(IsMainTesterTests.class);

        // core.util
        suite.addTestSuite(ExpressionFinderTestCase.class);

        // debug
        suite.addTestSuite(BreakpointLocationTests.class);
        suite.addTestSuite(ConsoleLineTrackerTests.class);
        suite.addTestSuite(DebugBreakpointsTests.class);
        suite.addTestSuite(GroovyLauncherShortcutTests.class);

        // ui (except ErrorLogTest)
        suite.addTestSuite(BracketInserterTests.class);
        suite.addTestSuite(GroovyAutoIndenterTests.class);
        suite.addTestSuite(GroovyAutoIndenterTests2.class);
        suite.addTestSuite(GroovyPartitionScannerTests.class);
        suite.addTestSuite(GroovyTagScannerTests.class);
        suite.addTestSuite(HighlightingExtenderTests.class);
        suite.addTestSuite(OutlineExtenderTests.class);
        suite.addTestSuite(SemanticHighlightingTests.class);

        // wizards
        suite.addTestSuite(NewGroovyTestCaseWizardTest.class);
        suite.addTestSuite(NewGroovyTypeWizardTest.class);

        // ..ui.search
        suite.addTest(FindOccurrencesTests.suite());

        return suite;
    }
}
