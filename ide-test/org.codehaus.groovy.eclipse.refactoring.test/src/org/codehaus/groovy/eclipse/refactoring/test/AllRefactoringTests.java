/*
 * Copyright 2010-2011 the original author or authors.
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.codebrowsing.tests.FindAllOccurrencesVisitorTests;
import org.codehaus.groovy.eclipse.codebrowsing.tests.IsSameExpressionTests;
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractConstantTests;
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractLocalTests;
import org.codehaus.groovy.eclipse.refactoring.test.extract.StaticExpressionCheckerTests;
import org.codehaus.groovy.eclipse.refactoring.test.extract.StaticFragmentCheckerTests;
import org.codehaus.groovy.eclipse.refactoring.test.extractMethod.ExtractMethodTestSuite;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.FormatterTestSuite;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.SemicolonRemoverTests;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.WhitespaceRemoverTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.MoveCURefactoringTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameFieldTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameLocalTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameMethodTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameTypeTests;

/**
 * @author Andrew Eisenberg
 * @created Mar 27, 2010
 */
public class AllRefactoringTests {

    public static Test suite() {
        final TestSuite suite = new TestSuite("Test for " + AllRefactoringTests.class.getPackage().getName());

        // rename
        suite.addTest(RenameTypeTests.suite());
        suite.addTest(RenameMethodTests.suite());
        suite.addTest(RenameFieldTests.suite());
        suite.addTest(RenameLocalTests.suite());
        suite.addTest(new TestSuite(MoveCURefactoringTests.class));

        // extract various
        suite.addTest(new TestSuite(StaticExpressionCheckerTests.class));
        suite.addTest(new TestSuite(StaticFragmentCheckerTests.class));
        suite.addTest(new TestSuite(IsSameExpressionTests.class));
        suite.addTest(new TestSuite(FindAllOccurrencesVisitorTests.class));
        suite.addTest(ExtractConstantTests.suite());
        suite.addTest(ExtractLocalTests.suite());
        suite.addTest(ExtractMethodTestSuite.suite());

        // formatting and indenting
        suite.addTest(FormatterTestSuite.suite());
        suite.addTest(new TestSuite(SemicolonRemoverTests.class));
        suite.addTest(new TestSuite(WhitespaceRemoverTests.class));

        return suite;
    }
}
