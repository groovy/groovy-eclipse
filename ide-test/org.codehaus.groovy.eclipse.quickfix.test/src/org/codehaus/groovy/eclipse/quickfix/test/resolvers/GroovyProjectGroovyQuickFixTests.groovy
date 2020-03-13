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
package org.codehaus.groovy.eclipse.quickfix.test.resolvers

import static org.junit.Assert.assertEquals
import static org.junit.Assume.assumeTrue

import groovy.transform.NotYetImplemented

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.quickfix.proposals.AddClassCastResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.AddGroovyRuntimeResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemType
import org.codehaus.groovy.eclipse.quickfix.proposals.AddClassCastResolver.AddClassCastProposal
import org.eclipse.core.resources.IMarker
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.internal.corext.util.JavaModelUtil
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

/**
 * Tests Groovy quick fixes in a Groovy file contained in a Groovy project.
 */
final class GroovyProjectGroovyQuickFixTests extends QuickFixHarness {

    private static final String SUBTEST = 'com.test.subtest'
    private static final String SUBSUBTEST = 'com.test.subtest.subtest'
    private static final String TOP_LEVEL_TYPE = 'class TopLevelType { class InnerType { class InnerInnerType { } } }'

    private ICompilationUnit topLevelUnit

    @Before
    void setUp() {
        topLevelUnit = addGroovySource(TOP_LEVEL_TYPE, 'TopLevelType', SUBTEST)
    }

    @Test
    void testAddImportField() {
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarField'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String fullQualifiedTypeToImport = 'com.test.subtest.TopLevelType'
        String typeToAddImportContent = 'class BarField { TopLevelType typeVar }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    @Test
    void testAddImportInnerType() {
        // When an InnerType is referenced with its declaring type, for example,
        // Map.Entry,
        // 'Map' is imported. When the InnerType is referenced by it's simple
        // name, there may
        // be further suggestions as other top level types might have inner
        // types with the same name
        // therefore 'Inner' is imported and the actual fully qualified top
        // level is shown within parenthesis

        // This tests the inner type reference by itself: InnerType
        String typeToImport = 'InnerType'
        String typeToAddImport = 'BarUsingInner'
        String innerFullyQualified = 'com.test.subtest.TopLevelType.InnerType'
        String expectedQuickFixDisplay = 'Import \'InnerType\' (com.test.subtest.TopLevelType)'
        String typeToAddImportContent = 'class BarUsingInner { InnerType innerTypeVar }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, innerFullyQualified, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    @Test
    void testAddImportInnerType2() {
        // When an InnerType is referenced with its declaring type, for example,
        // Map.Entry,
        // 'Map' is imported. When the InnerType is referenced by it's simple
        // name, there may
        // be further suggestions as other top level types might have inner
        // types with the same name
        // therefore 'Inner' is imported and the actual fully qualified top
        // level is shown within parenthesis

        // This tests the inner type when it also contains the top level type:
        // TopLevelType.InnerType
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarUsingInnerB'
        String typeToImportFullyQualified = 'com.test.subtest.TopLevelType'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String typeToAddImportContent = 'class BarUsingInnerB { TopLevelType.InnerType innerTypeVar }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, typeToImportFullyQualified, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    @Test
    void testAddImportInnerInnerType() {
        String typeToImport = 'InnerInnerType'
        String typeToAddImport = 'BarUsingInnerInner'
        String typeToImportFullyQualified = 'com.test.subtest.TopLevelType.InnerType.InnerInnerType'
        String expectedQuickFixDisplay = 'Import \'InnerInnerType\' (com.test.subtest.TopLevelType.InnerType)'
        String typeToAddImportContent = 'class BarUsingInnerInner { InnerInnerType innerTypeVar }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, typeToImportFullyQualified, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    @Test
    void testAddImportReturnType() {
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarReturnType'
        String fullQualifiedTypeToImport = 'com.test.subtest.TopLevelType'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String typeToAddImportContent = 'class BarReturnType { TopLevelType doSomething() { return null } }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    /**
     * Tests if an add import resolver can be found if the unresolved type is in a local variable declaration
     */
    @Test
    void testAddImportMethodParameter() {
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarMethodParameter'
        String fullQualifiedTypeToImport = 'com.test.subtest.TopLevelType'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String typeToAddImportContent = 'class BarMethodParameter { void doSomething(TopLevelType ttI) {} }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    /**
     * Tests if an add import resolver can be found if the unresolved type is a generic
     */
    @Test
    void testAddImportGeneric() {
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarGeneric'
        String fullQualifiedTypeToImport = 'com.test.subtest.TopLevelType'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String typeToAddImportContent = 'class BarGeneric { List<TopLevelType> aList }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    /**
     * Tests if an add import resolver can be found if a class is extending an unresolved type
     */
    @Test
    void testAddImportSubclassing() {
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarSubclassing'
        String fullQualifiedTypeToImport = 'com.test.subtest.TopLevelType'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String typeToAddImportContent = 'class BarSubclassing extends TopLevelType {}'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    /**
     * Tests if an add import resolver can be found if the unresolved type is in a local variable declaration
     */
    @Test
    void testAddImportLocalVariable() {
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarLocalVariable'
        String fullQualifiedTypeToImport = 'com.test.subtest.TopLevelType'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String typeToAddImportContent = 'class BarLocalVariable  { void doSomething () { TopLevelType localVar } }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    /**
     * Tests that a Groovy add import quick fix resolver can be obtained when
     * the unresolved type is encountered in multiple places in the code.
     */
    @Test
    void testAddImportMultipleLocations() {
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarMultipleLocations'
        String fullQualifiedTypeToImport = 'com.test.subtest.TopLevelType'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String typeToAddImportContent = 'class BarMultipleLocations extends TopLevelType { List<TopLevelType> doSomething () { TopLevelType localVar; return null } }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    /**
     * Tests if a Groovy add import quick fix can be obtained when other
     * unresolved types exist in the Groovy file
     */
    @Test
    void testAddImportMultipleUnresolved() {
        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarMultipleUnresolved'
        String fullQualifiedTypeToImport = 'com.test.subtest.TopLevelType'
        String expectedQuickFixDisplay = 'Import \'TopLevelType\' (com.test.subtest)'
        String typeToAddImportContent = 'class BarMultipleUnresolved extends TopLevelType { CSS css; HTML val = new Entry() }'

        testSelectImportGroovyTypeFromNewPackage(typeToImport, fullQualifiedTypeToImport, expectedQuickFixDisplay, typeToAddImport, typeToAddImportContent)
    }

    /**
     * Tests if a Groovy add import resolver has multiple suggestions for the
     * same unresolved simple name.
     */
    @Test
    void testAddImportMultipleProposalsForSameType() {
        addGroovySource(TOP_LEVEL_TYPE, 'TopLevelType', SUBSUBTEST)

        String typeToImport = 'TopLevelType'
        String typeToAddImport = 'BarLocalMultipleSameType'
        String typeToAddImportContent = 'class BarLocalMultipleSameType { void doSomething () { TopLevelType localVar } }'

        Map<String, String> expectedQuickFixes = [:]
        expectedQuickFixes.put("Import 'TopLevelType' ($SUBTEST)", SUBTEST + '.TopLevelType')
        expectedQuickFixes.put("Import 'TopLevelType' ($SUBSUBTEST)", SUBSUBTEST + '.TopLevelType')
        testMultipleProposalsSameTypeName(typeToImport, expectedQuickFixes, typeToAddImport, typeToAddImportContent)
    }

    /**
     * Tests that no Groovy add import quick fix resolvers are obtained for an
     * unresolved type that does not exist.
     */
    @Test
    void testAddImportNoProposals() {
        String typeToAddImport = 'BarAddImportNoProposal'
        String nonExistantType = 'DoesNotExistTopLevelType'
        String typeToAddImportContent = 'class BarAddImportNoProposal  { void doSomething () { DoesNotExistTopLevelType localVar } }'
        def unit = addGroovySource(typeToAddImportContent, typeToAddImport, 'com.test')

        AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver(nonExistantType, unit)
        assert resolver == null : 'Expected no resolver for nonexistant type: ' + nonExistantType
    }

    @Test
    void testAddImportAnnotation1() {
        Map<String, String> expectedProposals = [
            'Import \'Builder\' (groovy.transform.builder)': 'groovy.transform.builder.Builder'
        ]
        testMultipleProposalsSameTypeName('Builder', expectedProposals, 'Test', '@Builder class Test {}')
    }

    @Test
    void testAddImportAnnotation2() {
        Map<String, String> expectedProposals = [
            'Import \'Target\' (java.lang.annotation)': 'java.lang.annotation.Target',
            'Import \'Target\' (groovy.lang.DelegatesTo)': 'groovy.lang.DelegatesTo.Target'
        ]
        testMultipleProposalsSameTypeName('Target', expectedProposals, 'Test', '@Target() public @interface Test {}')
    }

    @Test
    void testAddImportAnnotation3() {
        Map<String, String> expectedProposals = [
            'Import \'CompileDynamic\' (groovy.transform)': 'groovy.transform.CompileDynamic'
        ]
        testMultipleProposalsSameTypeName('CompileDynamic', expectedProposals, 'Test', '@CompileDynamic class Test {}')
    }

    @Test // GRECLIPSE-1612
    void testAddImportClassExpression() {
        addJavaSource('''\
            |public class FooJava {
            |  public static String getProperty() {
            |    return "sad";
            |  }
            |}'''.stripMargin(), 'FooJava', 'other')

        String typeToAddImport = 'FooGroovy'
        String typeToAddImportContent = '@groovy.transform.TypeChecked\nclass FooGroovy {\n def main() { FooJava.getProperty() } }'
        def unit = addGroovySource(typeToAddImportContent, typeToAddImport, 'com.test')

        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit)
        List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, ProblemType.MISSING_IMPORTS_TYPE, unit)

        assert resolvers.size() == 1 : 'Should have found exactly one resolver'
        assert resolvers.get(0) instanceof AddMissingGroovyImportsResolver : 'Wrong type of resolver'
        def proposal = resolvers[0].quickFixProposals[0]
        assert proposal.displayString == 'Import \'FooJava\' (other)'
    }

    @Test // GRECLIPSE-1777
    void testAddTypecast() {
        def unit = addGroovySource('''\
            |@groovy.transform.CompileStatic
            |class D {
            |  Number foo() {
            |    new Integer(1)
            |  }
            |  Integer bar() {
            |    Integer result = foo()
            |    result
            |  }
            |}'''.stripMargin(), 'D', 'com.test')

        String expectedQuickFixDisplay = 'Add cast to Integer'
        AddClassCastResolver resolver = getAddClassCastResolver(unit)
        assert resolver != null : "Expected a resolver for ${ -> unit }"
        AddClassCastProposal proposal = getAddClassCastProposal(expectedQuickFixDisplay, resolver)
        assert proposal != null : "Expected a quick fix proposal for ${ -> unit }"
        assert proposal.displayString ==  expectedQuickFixDisplay: "Actual quick fix display expression should be: ${ -> expectedQuickFixDisplay }"
    }

    @Test
    void testAddUnimplementedMethods() {
        String contents = '''\
            |package foo
            |class Bar implements Map.Entry {
            |}
            |'''.stripMargin()
        def unit = addGroovySource(contents, 'Bar', 'foo')

        def proposals = findQuickFixes(unit, ProblemType.UNIMPLEMENTED_METHODS_TYPE)

        assert !proposals.isEmpty() : 'Expected quick fix for adding unimplemented methods'
    }

    @Test
    void testRemoveFinalModifier0() {
        String contents = '''\
            |package foo
            |class Bar {
            |  void wait() {} // attempts to override final method
            |}
            |'''.stripMargin()
        def unit = addGroovySource(contents, 'Bar', 'foo')

        def proposals = findQuickFixes(unit, ProblemType.FINAL_METHOD_OVERRIDE)

        assert proposals.isEmpty() : 'Expected no quick fix for override of final method in binary type'
    }

    @Test
    void testRemoveFinalModifier1() {
        String contents = '''\
            |package foo
            |class Bar {
            |  final void meth() {}
            |}
            |class Baz extends Bar {
            |  void meth() {} // attempts to override final method
            |}
            |'''.stripMargin()
        def unit = addGroovySource(contents, 'Baz', 'foo')

        def proposals = findQuickFixes(unit, ProblemType.FINAL_METHOD_OVERRIDE)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
        assertEquals(contents.replaceFirst('final ', ''), String.valueOf(unit.contents))
    }

    @Test
    void testRemoveFinalModifier2() {
        String contents = '''\
            |package foo
            |class Bar {
            |  final void meth() {}
            |}
            |'''.stripMargin()
        def unit1 = addGroovySource(contents, 'Bar', 'foo')

        def unit2 = addGroovySource('''\
            |package foo
            |class Baz extends Bar {
            |  void meth() {} // attempts to override final method
            |}
            |'''.stripMargin(), 'Baz', 'foo')

        def proposals = findQuickFixes(unit2, ProblemType.FINAL_METHOD_OVERRIDE)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit1)
        assertEquals(contents.replaceFirst('final ', ''), String.valueOf(unit1.contents))
    }

    @Test
    void testRemoveFinalModifier3() {
        String contents = '''\
            |package foo
            |class Bar {
            |  final void meth() {}
            |}
            |'''.stripMargin()
        def unit1 = addGroovySource(contents, 'Bar', 'foo')

        addGroovySource('''\
            |package foo
            |class Baz extends Bar {
            |}
            |'''.stripMargin(), 'Baz', 'foo')

        def unit2 = addGroovySource('''\
            |package whatever
            |class Something extends foo.Baz {
            |  void meth() {} // attempts to override final method
            |}
            |'''.stripMargin(), 'Something', 'whatever')

        def proposals = findQuickFixes(unit2, ProblemType.FINAL_METHOD_OVERRIDE)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit1)
        assertEquals(contents.replaceFirst('final ', ''), String.valueOf(unit1.contents))
    }

    @Test
    void testRaiseVisibilityModifier1() {
        String contents = '''\
            |package foo
            |class Bar {
            |  public void meth() {}
            |}
            |class Baz extends Bar {
            |  private void meth() {} // attempts to lower visibility
            |}
            |'''.stripMargin()
        def unit = addGroovySource(contents, 'Baz', 'foo')

        def proposals = findQuickFixes(unit, ProblemType.WEAKER_ACCESS_OVERRIDE)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
         // TODO: Could replace with ' ' instead of 'public '.
        assertEquals(contents.replaceFirst('private ', 'public '), String.valueOf(unit.contents))
    }

    @Test
    void testRaiseVisibilityModifier2() {
        String contents = '''\
            |package foo
            |class Bar {
            |  protected void meth() {}
            |}
            |class Baz extends Bar {
            |  private void meth() {} // attempts to lower visibility
            |}
            |'''.stripMargin()
        def unit = addGroovySource(contents, 'Baz', 'foo')

        def proposals = findQuickFixes(unit, ProblemType.WEAKER_ACCESS_OVERRIDE)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
        assertEquals(contents.replaceFirst('private ', 'protected '), String.valueOf(unit.contents))
    }

    @Test @NotYetImplemented
    void testRaiseVisibilityModifier3() {
        String contents = '''\
            |package foo
            |import groovy.transform.PackageScope
            |class Bar {
            |  @PackageScope void meth() {}
            |}
            |class Baz extends Bar {
            |  private void meth() {} // attempts to lower visibility
            |}
            |'''.stripMargin()
        def unit = addGroovySource(contents, 'Baz', 'foo')

        def proposals = findQuickFixes(unit, ProblemType.WEAKER_ACCESS_OVERRIDE)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
        assertEquals(contents.replaceFirst('private ', '@PackageScope '), String.valueOf(unit.contents))
    }

    @Test
    void testAddGroovyRuntime() {
        GroovyRuntime.removeGroovyClasspathContainer(topLevelUnit.javaProject)
        def testProject = topLevelUnit.javaProject.project
        buildProject()

        IMarker[] markers = getJDTFailureMarkers(testProject)
        assumeTrue(markers != null && markers.length > 0)

        List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, ProblemType.MISSING_CLASSPATH_CONTAINER_TYPE, topLevelUnit)
        assert resolvers.size() == 1 : 'Should have found exactly one resolver'
        assert resolvers.get(0) instanceof AddGroovyRuntimeResolver : 'Wrong type of resolver'

        resolvers[0].quickFixProposals[0].apply(null)
        buildProject()

        markers = getJDTFailureMarkers(testProject)
        assert !markers : 'Should not have found problems in this project'
    }

    @Test // no Groovy quick fix resolvers should be encountered for unrecognized errors
    void testUnrecognisedErrorNoProposals() {
        String typeToAddImport = 'BarUnrecognisedError'
        String typeToAddImportContent = 'class BarUnrecognisedError  { public void doSomething () { 222  }  }'
        def unit = addGroovySource(typeToAddImportContent, typeToAddImport, 'com.test')

        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit)
        for (type in ProblemType.values()) {
            List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, type, unit)
            assert resolvers == null || resolvers.isEmpty() : 'Encountered resolvers for unknown compilation error; none expected'
        }
    }

    //--------------------------------------------------------------------------

    private List<? extends ICompletionProposal> findQuickFixes(ICompilationUnit unit, ProblemType type) {
        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit)
        getAllQuickFixResolversForType(markers, type, unit).collectMany {
            it.quickFixProposals
        }
    }
}
