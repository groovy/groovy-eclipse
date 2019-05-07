/*
 * Copyright 2009-2019 the original author or authors.
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
 * Ensures that the code select requestor properly avoids visiting ASTNodes that
 * are not in the same {@link IJavaElement} as the target node.
 */
final class PartialVisitTests extends BrowsingTestSuite {

    private final PartialCodeSelectHelper helper = new PartialCodeSelectHelper()

    // should not visit the class or the main method
    @Test
    void testSimple() {
        String contents = 'new Foo().x\nclass Foo {\n def x \n}\n'
        assertCodeSelectWithSkippedNames(contents, indexOf(contents, 'x'), 'x', 'Hello()', 'Hello(context)', 'main(args)')
    }

    // should not visit the Hello constructor, Foo class, or the main method
    @Test
    void testSimple2() {
        String contents = 'class Foo {\n def x \n}\nnew Foo().x'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x', 'Foo', 'Hello()', 'Hello(context)', 'main(args)')
    }

    // should not visit the x field
    @Test
    void testSimple3() {
        String contents = 'class Foo {\n def x\n def blah() { \nx } }'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'x'), 'x', 'x')
    }

    @Test
    void testFieldInitializer() {
        String contents = 'class Foo { Foo() { } \n def y \ndef x = y }'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'y'), 'y', 'Foo()', 'y')
    }

    // static initializers are now visited in place
    @Test
    void testStaticFieldInitializer() {
        String contents = 'class Foo { Foo() { } \n static y \n def z \nstatic x = y }'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'y'), 'y', 'Foo()', 'z', 'y')
    }

    @Test
    void testInnerClass() {
        String contents = 'class Foo { Foo() { } \n static y \n def z \nstatic x = y \n class Inner { \n def blog \n def blag = y } }'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'y'), 'y', 'Foo()', 'x', 'y', 'z', 'blog')
    }

    @Test
    void testInnerClass2() {
        String contents = 'class Foo { Foo() { } \n static y \n def z \nstatic x = y \n class Inner { \n def blog }\n def blag = y  }'
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, 'y'), 'y', 'Foo()', 'x', 'y', 'z', 'Inner')
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
