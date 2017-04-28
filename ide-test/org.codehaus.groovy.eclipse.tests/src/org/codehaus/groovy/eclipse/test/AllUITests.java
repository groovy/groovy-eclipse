/*
 * Copyright 2009-2017 the original author or authors.
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

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.groovy.eclipse.test.actions.AddImportOnSelectionTests;
import org.codehaus.groovy.eclipse.test.actions.AliasingOrganizeImportsTest;
import org.codehaus.groovy.eclipse.test.actions.ConvertToJavaOrGroovyActionTests;
import org.codehaus.groovy.eclipse.test.actions.GroovyNatureActionTests;
import org.codehaus.groovy.eclipse.test.actions.OrganizeImportsTest;
import org.codehaus.groovy.eclipse.test.actions.SaveParticipantRegistryTests;
import org.codehaus.groovy.eclipse.test.adapters.GroovyFileAdapterFactoryTests;
import org.codehaus.groovy.eclipse.test.adapters.GroovyIFileEditorInputAdapterFactoryTests;
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
import org.codehaus.groovy.eclipse.test.wizards.NewGroovyTestCaseWizardTests;
import org.codehaus.groovy.eclipse.test.wizards.NewGroovyTypeWizardTests;
import org.codehaus.groovy.eclipse.ui.search.FindOccurrencesTests;

public final class AllUITests {
    public static Test suite() {
        TestSuite suite = new TestSuite(AllUITests.class.getName());

        suite.addTestSuite(ErrorLogTest.class); //This must be first or it will pick up garbage left in log by other tests.

        // actions
        suite.addTest(new EclipseTestSetup(newTestSuite("Organize Imports",
                OrganizeImportsTest.class, AliasingOrganizeImportsTest.class)));
        suite.addTest(new JUnit4TestAdapter(AddImportOnSelectionTests.class));
        suite.addTest(new JUnit4TestAdapter(ConvertToJavaOrGroovyActionTests.class));
        suite.addTest(new JUnit4TestAdapter(GroovyNatureActionTests.class));
        suite.addTest(new JUnit4TestAdapter(SaveParticipantRegistryTests.class));

        // adapters
        suite.addTest(new JUnit4TestAdapter(GroovyFileAdapterFactoryTests.class));
        suite.addTest(new JUnit4TestAdapter(GroovyIFileEditorInputAdapterFactoryTests.class));
        suite.addTest(new JUnit4TestAdapter(IsMainTesterTests.class));

        // core.util
        suite.addTestSuite(ExpressionFinderTestCase.class);

        // debug
        suite.addTest(new EclipseTestSetup(newTestSuite("Debug Breakpoints",
                BreakpointLocationTests.class, DebugBreakpointsTests.class)));
        suite.addTest(new JUnit4TestAdapter(ConsoleLineTrackerTests.class));
        suite.addTest(new JUnit4TestAdapter(GroovyLauncherShortcutTests.class));

        // ui (except ErrorLogTest)
        suite.addTest(new EclipseTestSetup(newTestSuite("Editor Enhancements",
                BracketInserterTests.class, SemanticHighlightingTests.class)));
        suite.addTest(new JUnit4TestAdapter(GroovyAutoIndenterTests.class));
        suite.addTest(new JUnit4TestAdapter(GroovyAutoIndenterTests2.class));
        suite.addTestSuite(GroovyPartitionScannerTests.class);
        suite.addTestSuite(GroovyTagScannerTests.class);
        suite.addTest(new JUnit4TestAdapter(HighlightingExtenderTests.class));
        suite.addTest(new JUnit4TestAdapter(OutlineExtenderTests.class));

        // wizards
        suite.addTest(new JUnit4TestAdapter(NewGroovyTestCaseWizardTests.class));
        suite.addTest(new JUnit4TestAdapter(NewGroovyTypeWizardTests.class));

        // ..ui.search
        suite.addTest(FindOccurrencesTests.suite());

        return suite;
    }

    private static Test newTestSuite(String name, Class<?>... tests) {
        TestSuite suite = new TestSuite(tests);
        suite.setName(name);
        return suite;
    }
}
