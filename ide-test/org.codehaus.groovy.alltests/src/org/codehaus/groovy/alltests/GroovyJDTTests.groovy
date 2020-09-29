/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.alltests

import org.codehaus.groovy.eclipse.chooser.CompilerChooser
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite) @Suite.SuiteClasses([
    org.codehaus.groovy.alltests.SanityTests,

    // Basic tests
    org.eclipse.jdt.groovy.core.tests.basic.AnnotationsTests,
    org.eclipse.jdt.groovy.core.tests.basic.ConfigurationTests,
    org.eclipse.jdt.groovy.core.tests.basic.EnumerationTests,
    org.eclipse.jdt.groovy.core.tests.basic.ErrorRecoveryTests,
    org.eclipse.jdt.groovy.core.tests.basic.GenericsTests,
    org.eclipse.jdt.groovy.core.tests.basic.GroovySimpleTests,
    org.eclipse.jdt.groovy.core.tests.basic.ImportsTests,
    org.eclipse.jdt.groovy.core.tests.basic.InnerClassTests,
    org.eclipse.jdt.groovy.core.tests.basic.Java8Tests,
    org.eclipse.jdt.groovy.core.tests.basic.TraitsTests,

    // Xform tests
    org.eclipse.jdt.groovy.core.tests.xform.AnnotationCollectorTests,
    org.eclipse.jdt.groovy.core.tests.xform.AutoCloneTests,
    org.eclipse.jdt.groovy.core.tests.xform.AutoFinalTests,
    org.eclipse.jdt.groovy.core.tests.xform.CanonicalTests,
    org.eclipse.jdt.groovy.core.tests.xform.CategoryTests,
    org.eclipse.jdt.groovy.core.tests.xform.DelegateTests,
    org.eclipse.jdt.groovy.core.tests.xform.GrabTests,
    org.eclipse.jdt.groovy.core.tests.xform.ImmutableTests,
    org.eclipse.jdt.groovy.core.tests.xform.InheritConstructorsTests,
    org.eclipse.jdt.groovy.core.tests.xform.LoggingTests,
    org.eclipse.jdt.groovy.core.tests.xform.NamedVariantTests,
    org.eclipse.jdt.groovy.core.tests.xform.NullCheckTests,
    org.eclipse.jdt.groovy.core.tests.xform.PackageScopeTests,
    org.eclipse.jdt.groovy.core.tests.xform.SingletonTests,
    org.eclipse.jdt.groovy.core.tests.xform.SortableTests,
    org.eclipse.jdt.groovy.core.tests.xform.StaticCompilationTests,
    org.eclipse.jdt.groovy.core.tests.xform.TypeCheckedTests,
    org.eclipse.jdt.groovy.core.tests.xform.UserDefinedTests,

    // Builder tests
    org.eclipse.jdt.core.groovy.tests.builder.BasicGroovyBuildTests,
    org.eclipse.jdt.core.groovy.tests.builder.BuildAccessRulesTests,
    org.eclipse.jdt.core.groovy.tests.builder.FullProjectTests,
    org.eclipse.jdt.core.groovy.tests.builder.STCScriptsTests,
    org.eclipse.jdt.core.groovy.tests.builder.ScriptFolderTests,

    // Location tests
    org.eclipse.jdt.core.groovy.tests.locations.ASTNodeSourceLocationsTests,
    org.eclipse.jdt.core.groovy.tests.locations.LocationSupportTests,
    org.eclipse.jdt.core.groovy.tests.locations.SourceLocationsTests,

    // Model tests
    org.eclipse.jdt.core.groovy.tests.model.ASTTransformsTests,
    org.eclipse.jdt.core.groovy.tests.model.GroovyClassFileTests,
    org.eclipse.jdt.core.groovy.tests.model.GroovyCompilationUnitTests,
    org.eclipse.jdt.core.groovy.tests.model.GroovyContentTypeTests,
    org.eclipse.jdt.core.groovy.tests.model.GroovyPartialModelTests,
    org.eclipse.jdt.core.groovy.tests.model.MoveRenameCopyTests,

    // Search tests
    org.eclipse.jdt.core.groovy.tests.search.ArrayInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.BinarySearchTests,
    org.eclipse.jdt.core.groovy.tests.search.CategorySearchTests,
    org.eclipse.jdt.core.groovy.tests.search.ClosureInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.ConstructorReferenceSearchTests,
    org.eclipse.jdt.core.groovy.tests.search.DGMInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.DeclarationInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.FieldReferenceSearchTests,
    org.eclipse.jdt.core.groovy.tests.search.GenericInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.GenericsMappingTests,
    org.eclipse.jdt.core.groovy.tests.search.Groovy20InferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.Groovy21InferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.Groovy25InferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.InferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.JDTPropertyNodeInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.LocalVariableReferenceSearchTests,
    org.eclipse.jdt.core.groovy.tests.search.MethodReferenceSearchTests,
    org.eclipse.jdt.core.groovy.tests.search.OperatorOverloadingInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.SpockInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.StaticInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.SyntheticAccessorInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.TraitInferencingTests,
    org.eclipse.jdt.core.groovy.tests.search.TypeReferenceSearchTests,
])
final class GroovyJDTTests {
    @BeforeClass
    static void setUp() {
        CompilerChooser.instance.initialize()
    }
}
