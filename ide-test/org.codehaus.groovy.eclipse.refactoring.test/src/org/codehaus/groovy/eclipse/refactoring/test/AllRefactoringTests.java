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
package org.codehaus.groovy.eclipse.refactoring.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.groovy.eclipse.refactoring.test.extract.ConvertLocalToFieldTests;
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractConstantTests;
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractLocalTests;
import org.codehaus.groovy.eclipse.refactoring.test.extract.StaticExpressionCheckerTests;
import org.codehaus.groovy.eclipse.refactoring.test.extract.StaticFragmentCheckerTests;
import org.codehaus.groovy.eclipse.refactoring.test.extractMethod.ExtractMethodTestSuite;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.FindIndentsTests;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.FormatterPreferencesTests;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.FormatterTestSuite;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.GroovyDocumentScannerTests;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.SemicolonRemoverTests;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.WhitespaceRemoverTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.MoveCURefactoringTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameFieldTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameLocalTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameMethodTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameTypeTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.SyntheticAccessorRenamingTests;

public final class AllRefactoringTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AllRefactoringTests.class.getName());

        // extract
        suite.addTest(ConvertLocalToFieldTests.suite());
        suite.addTest(ExtractConstantTests.suite());
        suite.addTest(ExtractLocalTests.suite());
        suite.addTest(new JUnit4TestAdapter(StaticExpressionCheckerTests.class));
        suite.addTest(new JUnit4TestAdapter(StaticFragmentCheckerTests.class));

        // extractMethod
        suite.addTest(ExtractMethodTestSuite.suite());

        // formatter
        suite.addTest(FormatterTestSuite.suite());
        suite.addTest(new JUnit4TestAdapter(FindIndentsTests.class));
        suite.addTest(new JUnit4TestAdapter(FormatterPreferencesTests.class));
        suite.addTest(new JUnit4TestAdapter(GroovyDocumentScannerTests.class));
        suite.addTest(new JUnit4TestAdapter(SemicolonRemoverTests.class));
        suite.addTest(new JUnit4TestAdapter(WhitespaceRemoverTests.class));

        // rename
        suite.addTest(RenameFieldTests.suite());
        suite.addTest(RenameLocalTests.suite());
        suite.addTest(RenameMethodTests.suite());
        suite.addTest(RenameTypeTests.suite());
        suite.addTest(new JUnit4TestAdapter(MoveCURefactoringTests.class));
        suite.addTest(new JUnit4TestAdapter(SyntheticAccessorRenamingTests.class));

        return suite;
    }
}
