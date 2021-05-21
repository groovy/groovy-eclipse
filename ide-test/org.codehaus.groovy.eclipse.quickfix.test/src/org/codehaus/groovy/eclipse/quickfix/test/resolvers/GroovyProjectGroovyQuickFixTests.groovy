/*
 * Copyright 2009-2021 the original author or authors.
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

import static org.codehaus.groovy.eclipse.core.model.GroovyRuntime.removeGroovyClasspathContainer

import groovy.transform.NotYetImplemented

import org.codehaus.groovy.eclipse.quickfix.test.QuickFixTestSuite
import org.eclipse.jdt.internal.corext.util.JavaModelUtil
import org.junit.Assert
import org.junit.Test

/**
 * Tests Groovy quick fixes for Groovy files in a Groovy project.
 */
final class GroovyProjectGroovyQuickFixTests extends QuickFixTestSuite {

    @Test
    void testAddImportField() {
        addGroovySource('class Foo {}', 'Foo', 'p')

        def unit = addGroovySource('''\
            |class Bar {
            |  private Foo foo
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals[0].displayString == 'Import \'Foo\' (p)'
    }

    /**
     * Tests that an add import quick fix is proposed if the unresolved type is in a return type declaration.
     */
    @Test
    void testAddImportReturnType() {
        addGroovySource('class Foo {}', 'Foo', 'p')

        def unit = addGroovySource('''\
            |class Bar {
            |  Foo test() {
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals[0].displayString == 'Import \'Foo\' (p)'
    }

    /**
     * Tests that an add import quick fix is proposed if the unresolved type is in a parameter declaration.
     */
    @Test
    void testAddImportMethodParameter() {
        addGroovySource('class Foo {}', 'Foo', 'p')

        def unit = addGroovySource('''\
            |class Bar {
            |  void test(Foo foo) {
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals[0].displayString == 'Import \'Foo\' (p)'
    }

    /**
     * Tests that an add import quick fix is proposed if the unresolved type is a generic.
     */
    @Test
    void testAddImportGeneric() {
        addGroovySource('class Foo {}', 'Foo', 'p')

        def unit = addGroovySource('''\
            |class Bar {
            |  List<Foo> list
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals[0].displayString == 'Import \'Foo\' (p)'
    }

    /**
     * Tests that an add import quick fix is proposed if a class is extending an unresolved type.
     */
    @Test
    void testAddImportSubclassing() {
        addGroovySource('class Foo {}', 'Foo', 'p')

        def unit = addGroovySource('''\
            |class Bar extends Foo {
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals[0].displayString == 'Import \'Foo\' (p)'
    }

    /**
     * Tests that an add import quick fix is proposed if the unresolved type is in a local variable declaration.
     */
    @Test
    void testAddImportLocalVariable() {
        addGroovySource('class Foo {}', 'Foo', 'p')

        def unit = addGroovySource('''\
            |class Bar {
            |  void test() {
            |    Foo foo
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals[0].displayString == 'Import \'Foo\' (p)'
    }

    /**
     * Tests that a Groovy add import quick fix resolver can be obtained when
     * the unresolved type is encountered in multiple places in the code.
     */
    @Test
    void testAddImportMultipleLocations() {
        addGroovySource('class Foo {}', 'Foo', 'p')

        def unit = addGroovySource('''\
            |class Bar extends Foo {
            |  List<Foo> test(Foo foo) {
            |    return [foo]
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert 'Import \'Foo\' (p)' in proposals*.displayString
    }

    /**
     * Tests if a Groovy add import quick fix can be obtained when other
     * unresolved types exist in the Groovy file.
     */
    @Test
    void testAddImportMultipleUnresolved() {
        addGroovySource('class Foo {}', 'Foo', 'p')

        def unit = addGroovySource('''\
            |class Bar extends Foo {
            |  void test() {
            |    CSS css; HTML val = new Entry()
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert 'Import \'Foo\' (p)' in proposals*.displayString
    }

    /**
     * Tests if a Groovy add import resolver has multiple suggestions for the
     * same unresolved simple name.
     */
    @Test
    void testAddImportMultipleProposalsForSameType() {
        addGroovySource('class Foo {}', 'Foo', 'p')
        addGroovySource('class Foo {}', 'Foo', 'q')

        def unit = addGroovySource('''\
            |class Bar {
            |  void test() {
            |    Foo foo
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals*.displayString == ['Import \'Foo\' (p)', 'Import \'Foo\' (q)']
    }

    /**
     * Tests that no Groovy add import quick fix resolvers are obtained for an
     * unresolved type that does not exist.
     */
    @Test
    void testNoAddImportProposals() {
        def unit = addGroovySource('''\
            |class Foo {
            |  void test() {
            |    DoesNotExistTopLevelType bar
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 0 : 'Expected no quick fixes for nonexistant type: DoesNotExistTopLevelType'
    }

    @Test
    void testAddImportAnnotation1() {
        def unit = addGroovySource('@Builder class Foo {}')

        def proposals = getGroovyQuickFixes(unit)

        assert proposals*.displayString == ['Import \'Builder\' (groovy.transform.builder)']
    }

    @Test
    void testAddImportAnnotation2() {
        def unit = addGroovySource('@Target @interface Foo {}')

        def proposals = getGroovyQuickFixes(unit)

        assert proposals*.displayString == ['Import \'Target\' (groovy.lang.DelegatesTo)', 'Import \'Target\' (java.lang.annotation)']
    }

    @Test
    void testAddImportAnnotation3() {
        def unit = addGroovySource('@CompileDynamic class Foo {}')

        def proposals = getGroovyQuickFixes(unit)

        assert proposals*.displayString == ['Import \'CompileDynamic\' (groovy.transform)']
    }

    @Test // GRECLIPSE-1612
    void testAddImportClassExpression() {
        addJavaSource('''\
            |public class J {
            |  public static String getProperty() {
            |    return "whatever";
            |  }
            |}'''.stripMargin(), 'J', 'p')

        def unit = addGroovySource('''\
            |@groovy.transform.TypeChecked
            |class G {
            |  static main(args) {
            |    J.getProperty()
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1
        assert proposals[0].displayString == 'Import \'J\' (p)'
    }

    @Test
    void testAddImportStaticInnerType1() {
        // When an inner type is referenced with its declaring type, for example,
        // Map.Entry, 'Map' is imported. When the InnerType is referenced by it's
        // simple name, there may be further suggestions as other top level types
        // might have inner types with the same name therefore 'Entry' is imported
        // and the actual fully qualified top level is shown within parenthesis

        def unit = addGroovySource('Entry entry = null')

        def proposals = getGroovyQuickFixes(unit)

        assert 'Import \'Entry\' (java.util.Map)' in proposals*.displayString
    }

    @Test
    void testAddImportStaticInnerType2() {
        // When an inner type is referenced with its declaring type, for example,
        // Map.Entry, 'Map' is imported. When the inner type is referenced by its
        // simple name, there may be further suggestions as other top level types
        // might have inner types with the same name.

        addGroovySource('''\
            |class Foo {
            |  static class Bar {
            |  }
            |}'''.stripMargin(), 'Foo', 'p')

        def unit = addGroovySource('Foo.Bar bar = null')

        def proposals = getGroovyQuickFixes(unit)

        assert proposals[0].displayString == 'Import \'Foo\' (p)'
    }

    @Test // GRECLIPSE-1777
    void testInsertTypecast() {
        def unit = addGroovySource('''\
            |@groovy.transform.CompileStatic
            |class C {
            |  Number foo() {
            |    new Integer(1)
            |  }
            |  Integer bar() {
            |    Integer result = foo()
            |    result
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1 : "Expected a quick fix proposal for ${ -> unit }"
        assert proposals[0].displayString ==  'Add cast to Integer'
    }

    @Test
    void testAddUnimplementedMethods1() {
        def unit = addGroovySource('''\
            |class Foo implements Map.Entry {
            |}
            |'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length > 0 : 'Expected quick fix for adding unimplemented methods'
        assert proposals[0].displayString == 'Add unimplemented methods'
    }

    @Test
    void testAddUnimplementedMethods2() {
        def unit = addGroovySource('''\
            |def list = new List() {
            |}
            |'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length > 0 : 'Expected quick fix for adding unimplemented methods'
        assert proposals[0].displayString == 'Add unimplemented methods'
    }

    @Test
    void testRemoveFinalModifier0() {
        def unit = addGroovySource('''\
            |class Foo {
            |  void wait() {} // attempts to override final method
            |}
            |'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 0 : 'Expected no quick fix for override of final method in binary type'
    }

    @Test
    void testRemoveFinalModifier1() {
        def unit = addGroovySource('''\
            |package foo
            |class Bar {
            |  final void meth() {}
            |}
            |class Baz extends Bar {
            |  void meth() {} // attempts to override final method
            |}
            |'''.stripMargin(), 'Baz', 'foo')

        def expected = unit.source.replaceFirst('final ', '')

        def proposals = getGroovyQuickFixes(unit)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
        Assert.assertEquals(expected, unit.source)
    }

    @Test
    void testRemoveFinalModifier2() {
        def unit1 = addGroovySource('''\
            |package foo
            |class Bar {
            |  final void meth() {}
            |}
            |'''.stripMargin(), 'Bar', 'foo')

        def unit2 = addGroovySource('''\
            |package foo
            |class Baz extends Bar {
            |  void meth() {} // attempts to override final method
            |}
            |'''.stripMargin(), 'Baz', 'foo')

        def expected = unit1.source.replaceFirst('final ', '')

        def proposals = getGroovyQuickFixes(unit2)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit1)
        Assert.assertEquals(expected, unit1.source)
    }

    @Test
    void testRemoveFinalModifier3() {
        def unit1 = addGroovySource('''\
            |package foo
            |class Bar {
            |  final void meth() {}
            |}
            |'''.stripMargin(), 'Bar', 'foo')

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

        def expected = unit1.source.replaceFirst('final ', '')

        def proposals = getGroovyQuickFixes(unit2)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit1)
        Assert.assertEquals(expected, unit1.source)
    }

    @Test
    void testRaiseVisibilityModifier1() {
        def unit = addGroovySource('''\
            |class Foo {
            |  public void meth() {}
            |}
            |class Bar extends Foo {
            |  private void meth() {} // attempts to lower visibility
            |}
            |'''.stripMargin())

        def expected = unit.source.replaceFirst('private ', 'public ') // TODO: Could replace with ' ' instead of 'public '.

        def proposals = getGroovyQuickFixes(unit)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
        Assert.assertEquals(expected, unit.source)
    }

    @Test
    void testRaiseVisibilityModifier2() {
        def unit = addGroovySource('''\
            |class Foo {
            |  protected void meth() {}
            |}
            |class Bar extends Foo {
            |  private void meth() {} // attempts to lower visibility
            |}
            |'''.stripMargin())

        def expected = unit.source.replaceFirst('private ', 'protected ')

        def proposals = getGroovyQuickFixes(unit)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
        Assert.assertEquals(expected, unit.source)
    }

    @Test @NotYetImplemented
    void testRaiseVisibilityModifier3() {
        def unit = addGroovySource('''\
            |import groovy.transform.PackageScope
            |class Foo {
            |  @PackageScope void meth() {}
            |}
            |class Bar extends Foo {
            |  private void meth() {} // attempts to lower visibility
            |}
            |'''.stripMargin())

        def expected = unit.source.replaceFirst('private ', '@PackageScope ')

        def proposals = getGroovyQuickFixes(unit)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
        Assert.assertEquals(expected, unit.source)
    }

    @Test @NotYetImplemented
    void testRaiseVisibilityModifier4() {
        def unit = addGroovySource('''\
            |class Foo {
            |  private MetaClass getMetaClass() {} // attempts to lower visibility
            |}
            |'''.stripMargin())

        def expected = unit.source.replaceFirst('private ', 'public ')

        def proposals = getGroovyQuickFixes(unit)

        proposals[0].apply(null)
        JavaModelUtil.reconcile(unit)
        Assert.assertEquals(expected, unit.source)
    }

    @Test
    void testAddGroovyRuntime() {
        removeGroovyClasspathContainer(packageFragmentRoot.javaProject)

        def unit = addGroovySource('println "hello world"')

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1
        assert proposals[0].displayString == 'Add Groovy Libraries to classpath'

        proposals[0].apply(null)

        def markers = getJavaProblemMarkers(packageFragmentRoot.javaProject.resource)

        assert markers.length == 0 : 'Should not have found problems in this project'
    }

    @Test
    void testNoProposalsForUnrecognizedError() {
        def unit = addGroovySource('''\
            |class BarUnrecognisedError {
            |  void doSomething() {
            |    return 222 // return from void method
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 0 : 'Encountered quick fix(es) for unknown compilation error; none expected'
    }
}
