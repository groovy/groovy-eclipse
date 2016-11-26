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
package org.codehaus.groovy.eclipse.refactoring.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.test.extract.*;
import org.codehaus.groovy.eclipse.refactoring.test.extractMethod.*;
import org.codehaus.groovy.eclipse.refactoring.test.formatter.*;
import org.codehaus.groovy.eclipse.refactoring.test.rename.*;

/**
 * @author Andrew Eisenberg
 * @created Mar 27, 2010
 */
public class AllRefactoringTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AllRefactoringTests.class.getName());

        // extract
        suite.addTest(ConvertLocalToFieldTests.suite());
        suite.addTest(ExtractConstantTests.suite());
        suite.addTest(ExtractLocalTests.suite());
        suite.addTestSuite(StaticExpressionCheckerTests.class);
        suite.addTestSuite(StaticFragmentCheckerTests.class);

        // extractMethod
        suite.addTest(ExtractMethodTestSuite.suite());

        // formatter
        suite.addTestSuite(FindIndentsTests.class);
        suite.addTest(FormatterTestSuite.suite());
        suite.addTestSuite(GroovyDocumentScannerTests.class);
        suite.addTestSuite(SemicolonRemoverTests.class);
        suite.addTestSuite(WhitespaceRemoverTests.class);

        // rename
        suite.addTestSuite(MoveCURefactoringTests.class);
        suite.addTest(RenameFieldTests.suite());
        suite.addTest(RenameLocalTests.suite());
        suite.addTest(RenameMethodTests.suite());
        suite.addTest(RenameTypeTests.suite());
        suite.addTestSuite(SyntheticAccessorRenamingTests.class);

        return suite;
    }
}
