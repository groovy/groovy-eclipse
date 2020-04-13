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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.junit.Assert.*

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.eclipse.codebrowsing.requestor.CodeSelectHelper
import org.codehaus.groovy.eclipse.codebrowsing.requestor.CodeSelectRequestor
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.JavaModelException
import org.eclipse.jdt.groovy.search.TypeLookupResult
import org.junit.Test

/**
 * Ensures that the {@link CodeSelectRequestor} avoids visiting {@link ASTNode}s
 * that are not in the same {@link IJavaElement} as the target node.
 */
final class PartialVisitTests extends BrowsingTestSuite {

    private final PartialCodeSelectHelper helper = new PartialCodeSelectHelper()

    @Test // should not visit the Foo class or the main method
    void testBasic1() {
        String contents = 'new Foo().x\nclass Foo {\n def x\n}\n'
        assertCodeSelectWithSkippedNames(contents, indexOf(contents, 'x'), 'x', 'main(args)')
    }

    @Test // should not visit the Foo class or the main method
    void testBasic2() {
        String contents = 'class Foo {\n def x\n}\nnew Foo().x'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x', 'Foo', 'main(args)')
    }

    @Test
    void testBasic3() {
        String contents = 'class Foo {\n def x\n def blah() {\n x\n}\n}'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x')
    }

    @Test
    void testFieldInitializer() {
        String contents = 'class Foo {\n Foo() {}\n def y\n def x = y\n}'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'y'), 'y')
    }

    @Test
    void testStaticFieldInitializer() {
        String contents = 'class Foo { Foo() {}\n static y\n def z\n static x = y }'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'y'), 'y')
    }

    @Test
    void testInnerClass1() {
        String contents = '''\
            |class Foo {
            |  Foo() {}
            |  static y
            |  def z
            |  static x = y
            |  def m() {}
            |  class Inner {
            |    def one
            |    def two = y
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'y'), 'y', 'm()')
    }

    @Test
    void testInnerClass2() {
        String contents = '''\
            |class Foo {
            |  Foo() {}
            |  static y
            |  def z
            |  static x = y
            |  def m() {}
            |  class Inner {
            |    def one
            |  }
            |  void test() {
            |    def two = y
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'y'), 'y', 'm()', 'Inner')
    }

    //--------------------------------------------------------------------------

    private static String getElementName(IJavaElement element) {
        if (element instanceof IMethod) {
            try {
                String[] params = ((IMethod) element).parameterNames
                return element.elementName + (params.length < 1 ? '()' : Arrays.toString(params).replace('[', '(').replace(']', ')'))
            } catch (JavaModelException e) {}
        }
        return element.elementName
    }

    private static Region indexOf(String contents, String string) {
        return new Region(contents.indexOf(string), string.length())
    }

    private static Region lastIndexOf(String contents, String string) {
        return new Region(contents.lastIndexOf(string), string.length())
    }

    private void assertCodeSelectWithSkippedNames(String contents, Region region, String expectedElementName, String... skippedElementNames) {
        GroovyCompilationUnit unit = addGroovySource(contents, 'Hello')

        IJavaElement[] elems = helper.select(unit, region.offset, region.length)
        assertEquals('Should have found a single selection: ' + Arrays.toString(elems), 1, elems.length)
        assertEquals('Wrong element selected', expectedElementName, getElementName(elems[0]))

        for (skipped in skippedElementNames) {
            assertTrue('Element ' + skipped + ' should have been skipped\nExpected: ' + Arrays.toString(skippedElementNames) + '\nWas: ' + helper.skippedElements, helper.skippedElements.contains(skipped))
        }

        assertEquals('Wrong number of elements skipped\nExpected: ' + Arrays.toString(skippedElementNames) + '\nWas: ' + helper.skippedElements, skippedElementNames.length, helper.skippedElements.size())
    }

    private static class PartialCodeSelectRequestor extends CodeSelectRequestor {
        private final Set<String> skippedElements = new HashSet<String>()

        PartialCodeSelectRequestor(ASTNode node, GroovyCompilationUnit unit) {
            super(node, unit)
        }

        @Override
        VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
            VisitStatus status = super.acceptASTNode(node, result, enclosingElement)
            if (status == VisitStatus.CANCEL_MEMBER) {
                assert !skippedElements.contains(getElementName(enclosingElement)) :
                    "Element has been skipped twice, but should only have been skipped once: $enclosingElement"
                skippedElements.add(getElementName(enclosingElement))
            }
            return status
        }
    }

    private static class PartialCodeSelectHelper extends CodeSelectHelper {
        private Set<String> skippedElements = new HashSet<String>()

        @Override
        protected CodeSelectRequestor createRequestor(ASTNode node, Region r1, Region r2, GroovyCompilationUnit unit) {
            PartialCodeSelectRequestor partialCodeSelectRequestor = new PartialCodeSelectRequestor(node, unit)
            skippedElements = partialCodeSelectRequestor.skippedElements
            return partialCodeSelectRequestor
        }

        @Override
        public IJavaElement[] select(GroovyCompilationUnit unit, int start, int length) {
            skippedElements.clear()
            return super.select(unit, start, length)
        }

        @Override
        ASTNode selectASTNode(GroovyCompilationUnit unit, int start, int length) {
            skippedElements.clear()
            return super.selectASTNode(unit, start, length)
        }
    }
}
