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
package org.codehaus.groovy.alltests

import static org.eclipse.jdt.core.tests.util.GroovyUtils.isAtLeastGroovy

import junit.framework.Test
import junit.framework.TestSuite
// From org.eclipse.jdt.groovy.core.tests.builder plug-in:
import org.eclipse.jdt.core.groovy.tests.builder.BasicGroovyBuildTests
import org.eclipse.jdt.core.groovy.tests.builder.BuildAccessRulesTests
import org.eclipse.jdt.core.groovy.tests.builder.FullProjectTests
import org.eclipse.jdt.core.groovy.tests.compiler.STCScriptsTests
import org.eclipse.jdt.core.groovy.tests.compiler.ScriptFolderTests
import org.eclipse.jdt.core.groovy.tests.locations.ASTConverterTests
import org.eclipse.jdt.core.groovy.tests.locations.ASTNodeSourceLocationsTests
import org.eclipse.jdt.core.groovy.tests.locations.LocationSupportTests
import org.eclipse.jdt.core.groovy.tests.locations.SourceLocationsTests
import org.eclipse.jdt.core.groovy.tests.model.ASTTransformsTests
import org.eclipse.jdt.core.groovy.tests.model.GroovyClassFileTests
import org.eclipse.jdt.core.groovy.tests.model.GroovyCompilationUnitTests
import org.eclipse.jdt.core.groovy.tests.model.GroovyContentTypeTests
import org.eclipse.jdt.core.groovy.tests.model.GroovyPartialModelTests
import org.eclipse.jdt.core.groovy.tests.model.MoveRenameCopyTests
import org.eclipse.jdt.core.groovy.tests.search.AllSearchTests
// From org.eclipse.jdt.groovy.core.tests.compiler plug-in:
import org.eclipse.jdt.groovy.core.tests.basic.AnnotationsTests
import org.eclipse.jdt.groovy.core.tests.basic.ErrorRecoveryTests
import org.eclipse.jdt.groovy.core.tests.basic.GenericsTests
import org.eclipse.jdt.groovy.core.tests.basic.GroovySimpleTest
import org.eclipse.jdt.groovy.core.tests.basic.GroovySimpleTests_Compliance_1_8
import org.eclipse.jdt.groovy.core.tests.basic.TraitsTests
import org.eclipse.jdt.groovy.core.tests.basic.TransformationsTests

/**
 * All Groovy-JDT integration tests.
 */
final class GroovyJDTTests {
    static Test suite() {
        // ensure that the compiler chooser starts up
        GroovyTestSuiteSupport.initializeCompilerChooser()

        TestSuite suite = new TestSuite(GroovyJDTTests.class.name)

        suite.addTest(new junit.framework.JUnit4TestAdapter(SanityTests))

        // Builder tests
        suite.addTest(BasicGroovyBuildTests.suite())
        suite.addTest(BuildAccessRulesTests.suite())
        suite.addTest(FullProjectTests.suite())

        // Compiler tests
        suite.addTest(AnnotationsTests.suite())
        suite.addTest(ErrorRecoveryTests.suite())
        suite.addTest(GenericsTests.suite())
        suite.addTest(GroovySimpleTest.suite())
        suite.addTest(GroovySimpleTests_Compliance_1_8.suite())
        suite.addTest(ScriptFolderTests.suite())
        suite.addTest(STCScriptsTests.suite())
        if (isAtLeastGroovy(23))
            suite.addTest(TraitsTests.suite())
        suite.addTest(TransformationsTests.suite())

        // Location tests
        suite.addTest(ASTConverterTests.suite())
        suite.addTest(ASTNodeSourceLocationsTests.suite())
        suite.addTestSuite(LocationSupportTests.class)
        suite.addTest(SourceLocationsTests.suite())

        // Model tests
        suite.addTest(ASTTransformsTests.suite())
        suite.addTest(GroovyClassFileTests.suite())
        suite.addTest(GroovyCompilationUnitTests.suite())
        suite.addTest(GroovyContentTypeTests.suite())
        suite.addTest(GroovyPartialModelTests.suite())
        suite.addTest(MoveRenameCopyTests.suite())

        // Search tests
        suite.addTest(AllSearchTests.suite())

        return suite
    }
}
