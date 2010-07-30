/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.alltests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.groovy.tests.builder.BasicGroovyBuildTests;
import org.eclipse.jdt.core.groovy.tests.locations.ASTConverterTests;
import org.eclipse.jdt.core.groovy.tests.locations.ASTNodeSourceLocationsTests;
import org.eclipse.jdt.core.groovy.tests.locations.LocationSupportTests;
import org.eclipse.jdt.core.groovy.tests.locations.SourceLocationsTests;
import org.eclipse.jdt.core.groovy.tests.model.AnnotationsTests;
import org.eclipse.jdt.core.groovy.tests.model.GroovyCompilationUnitTests;
import org.eclipse.jdt.core.groovy.tests.model.GroovyContentTypeTests;
import org.eclipse.jdt.core.groovy.tests.model.MoveRenameCopyTests;
import org.eclipse.jdt.core.groovy.tests.search.AllSearchTests;
import org.eclipse.jdt.groovy.core.tests.basic.GroovySimpleTest;

/**
 * @author Andrew Eisenberg
 * @created Jul 8, 2009
 *
 * All Groovy-JDT integration tests.
 *
 * TODO Determine if we should include the entire builder and compiler test suites
 * here, or only the Groovy-oriented tests.
 */
public class GroovyJDTTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("Groovy JDT Tests"); //$NON-NLS-1$

        // Model tests
        suite.addTest(AnnotationsTests.suite());
        suite.addTest(GroovyCompilationUnitTests.suite());
        suite.addTest(GroovyContentTypeTests.suite());
        suite.addTest(MoveRenameCopyTests.suite());

        // Builder tests
        suite.addTest(BasicGroovyBuildTests.suite());

		// Location tests
        suite.addTestSuite(LocationSupportTests.class);
		suite.addTestSuite(SourceLocationsTests.class);
		suite.addTestSuite(ASTNodeSourceLocationsTests.class);
		suite.addTestSuite(ASTConverterTests.class);

        // Compiler tests
        suite.addTest(GroovySimpleTest.suite());

        // Search tests
        suite.addTest(AllSearchTests.suite());

        return suite;
    }
}
