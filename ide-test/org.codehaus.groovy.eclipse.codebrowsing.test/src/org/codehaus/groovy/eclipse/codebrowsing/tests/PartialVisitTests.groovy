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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import groovy.transform.CompileStatic

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

    @Test // should not visit the class body or the main method
    void testBasic1() {
        String contents = 'new C().x\nclass C {\n int x\n}'
        assertCodeSelectWithSkippedNames(contents, indexOf(contents, 'x'), 'x', 'main(args)')
    }

    @Test // should not visit the class body or the main method
    void testBasic2() {
        String contents = 'class C {\n int x\n}\nnew C().x'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x', 'C', 'main(args)')
    }

    @Test // should not visit the field
    void testBasic3() {
        String contents = 'class C {\n int x\n def m() {\n  x\n }\n}'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x', 'x')
    }

    @Test // should not visit the field
    void testBasic4() {
        String contents = 'class C {\n Object x\n def m() {\n  x\n }\n}'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x', 'x')
    }

    @Test // should not visit the other field
    void testFieldInitializer() {
        String contents = 'class C {\n C() {}\n Number x\n private y = x\n}'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x', 'x')
    }

    @Test // should not visit the other fields
    void testStaticFieldInitializer() {
        String contents = 'class C {\n C() {}\n static Number x\n Number y\n static z = x }'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x', 'x', 'y')
    }

    @Test
    void testInnerClass1() {
        String contents = '''\
            |class C {
            |  C() {}
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
            |class C {
            |  C() {}
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
                String[] params = element.parameterNames
                return element.elementName + (params.length < 1 ? '()' : Arrays.toString(params).replace('[', '(').replace(']', ')'))
            } catch (JavaModelException ignore) {
            }
        }
        return element.elementName
    }

    private static Region indexOf(String contents, String string) {
        return new Region(contents.indexOf(string), string.length())
    }

    private static Region lastIndexOf(String contents, String string) {
        return new Region(contents.lastIndexOf(string), string.length())
    }

    private void assertCodeSelectWithSkippedNames(String contents, Region region, String expectedElementName, String... expectedSkippedElements) {
        def unit = addGroovySource(contents)

        def selected = helper.select(unit, region.offset, region.length)

        assert selected.length == 1
        assert getElementName(selected[0]) == expectedElementName

        for (shouldSkip in expectedSkippedElements) {
            assert shouldSkip in helper.skippedElements
        }
        assert helper.skippedElements.size() == expectedSkippedElements.length
    }

    @CompileStatic
    private static class PartialCodeSelectRequestor extends CodeSelectRequestor {
        private final Set<String> skippedElements = []

        PartialCodeSelectRequestor(ASTNode node, GroovyCompilationUnit unit) {
            super(node, unit)
        }

        @Override
        VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
            VisitStatus status = super.acceptASTNode(node, result, enclosingElement)
            if (status == VisitStatus.CANCEL_MEMBER) {
                assert skippedElements.add(getElementName(enclosingElement)) :
                    "Element '$enclosingElement.elementName' has been skipped more than once"
            }
            return status
        }
    }

    @CompileStatic
    private static class PartialCodeSelectHelper extends CodeSelectHelper {
        private Set<String> skippedElements = []

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
