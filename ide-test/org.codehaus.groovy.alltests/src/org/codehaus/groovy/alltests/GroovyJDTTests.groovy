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

// From org.eclipse.jdt.groovy.core.tests.builder plug-in:
import org.eclipse.jdt.core.groovy.tests.builder.BasicGroovyBuildTests
import org.eclipse.jdt.core.groovy.tests.builder.BuildAccessRulesTests
import org.eclipse.jdt.core.groovy.tests.builder.FullProjectTests
import org.eclipse.jdt.core.groovy.tests.builder.STCScriptsTests
import org.eclipse.jdt.core.groovy.tests.builder.ScriptFolderTests
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
import org.eclipse.jdt.core.groovy.tests.search.ArrayInferencingTests
import org.eclipse.jdt.core.groovy.tests.search.BinarySearchTests
import org.eclipse.jdt.core.groovy.tests.search.CategorySearchTests
import org.eclipse.jdt.core.groovy.tests.search.DGMInferencingTests
import org.eclipse.jdt.core.groovy.tests.search.DeclarationInferencingTests
import org.eclipse.jdt.core.groovy.tests.search.FieldReferenceSearchTests
import org.eclipse.jdt.core.groovy.tests.search.GenericInferencingTests
import org.eclipse.jdt.core.groovy.tests.search.GenericsMappingTest
import org.eclipse.jdt.core.groovy.tests.search.Groovy20InferencingTests
import org.eclipse.jdt.core.groovy.tests.search.Groovy21InferencingTests
import org.eclipse.jdt.core.groovy.tests.search.InferencingTests
import org.eclipse.jdt.core.groovy.tests.search.JDTPropertyNodeInferencingTests
import org.eclipse.jdt.core.groovy.tests.search.LocalVariableReferenceSearchTests
import org.eclipse.jdt.core.groovy.tests.search.MethodReferenceSearchTests
import org.eclipse.jdt.core.groovy.tests.search.OperatorOverloadingInferencingTests
import org.eclipse.jdt.core.groovy.tests.search.StaticInferencingTests
import org.eclipse.jdt.core.groovy.tests.search.SyntheticAccessorInferencingTests
import org.eclipse.jdt.core.groovy.tests.search.TypeReferenceSearchTests
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
    static junit.framework.Test suite() {
        // ensure that the compiler chooser starts up
        GroovyTestSuiteSupport.initializeCompilerChooser()

        def suite = new junit.framework.TestSuite(GroovyJDTTests.name)

        suite.addTest(new junit.framework.JUnit4TestAdapter(SanityTests))

        // Basic tests
        suite.addTest(AnnotationsTests.suite())
        suite.addTest(ErrorRecoveryTests.suite())
        suite.addTest(GenericsTests.suite())
        suite.addTest(GroovySimpleTest.suite())
        suite.addTest(GroovySimpleTests_Compliance_1_8.suite())
        if (isAtLeastGroovy(23))
            suite.addTest(TraitsTests.suite())
            suite.addTest(TransformationsTests.suite())

        // Builder tests
        suite.addTest(new junit.framework.JUnit4TestAdapter(BasicGroovyBuildTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(BuildAccessRulesTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(FullProjectTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScriptFolderTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(STCScriptsTests))

        // Location tests
        suite.addTest(new junit.framework.JUnit4TestAdapter(ASTConverterTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(ASTNodeSourceLocationsTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationSupportTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(SourceLocationsTests))

        // Model tests
        suite.addTest(new junit.framework.JUnit4TestAdapter(ASTTransformsTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(GroovyClassFileTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(GroovyCompilationUnitTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(GroovyContentTypeTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(GroovyPartialModelTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(MoveRenameCopyTests))

        // Search tests
        suite.addTest(new junit.framework.JUnit4TestAdapter(ArrayInferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(BinarySearchTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(CategorySearchTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(DeclarationInferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(DGMInferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(FieldReferenceSearchTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(GenericInferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(GenericsMappingTest))
        suite.addTest(new junit.framework.JUnit4TestAdapter(Groovy20InferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(Groovy21InferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(InferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(JDTPropertyNodeInferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocalVariableReferenceSearchTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(MethodReferenceSearchTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperatorOverloadingInferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(StaticInferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(SyntheticAccessorInferencingTests))
        suite.addTest(new junit.framework.JUnit4TestAdapter(TypeReferenceSearchTests))

        return suite
    }
}
