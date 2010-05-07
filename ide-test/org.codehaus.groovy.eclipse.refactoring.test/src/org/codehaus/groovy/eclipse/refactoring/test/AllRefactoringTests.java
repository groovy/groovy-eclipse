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
package org.codehaus.groovy.eclipse.refactoring.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameFieldTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameLocalTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameMethodTests;
import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameTypeTests;

import formatter.FormatterTestSuite;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 27, 2010
 */
public class AllRefactoringTests {
    public static Test suite() {
        final TestSuite suite = new TestSuite("Test for "
                + AllRefactoringTests.class.getPackage().getName());
        suite.addTest(RenameTypeTests.suite());
        suite.addTest(RenameMethodTests.suite());
        suite.addTest(RenameFieldTests.suite());
        suite.addTest(RenameLocalTests.suite());
        suite.addTest(FormatterTestSuite.suite());
        return suite;
    }

}
