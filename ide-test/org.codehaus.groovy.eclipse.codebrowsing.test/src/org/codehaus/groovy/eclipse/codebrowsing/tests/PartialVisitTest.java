/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.CodeSelectHelper;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.CodeSelectRequestor;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.TypeLookupResult;

/**
 * Ensures that the code select requestor properly avoids visiting ASTNodes that are not in the same
 * {@link IJavaElement} as the target node
 * @author andrew
 * @created Mar 2, 2011
 */
public class PartialVisitTest extends BrowsingTestCase {

    private class PartialCodeSelectRequestor extends CodeSelectRequestor {
        Set<String> skippedElements = new HashSet<String>();

        public PartialCodeSelectRequestor(ASTNode nodeToLookFor,
                GroovyCompilationUnit unit) {
            super(nodeToLookFor, unit);
        }

        @Override
        public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
                IJavaElement enclosingElement) {
            VisitStatus status = super.acceptASTNode(node, result,
                    enclosingElement);
            if (status == VisitStatus.CANCEL_MEMBER) {
                if (skippedElements.contains(enclosingElement)) {
                    fail("Element has been skipped twice, but should only have been skipped once: "
                            + enclosingElement);
                }
                skippedElements.add(enclosingElement.getElementName());
            }
            return status;
        }
    }

    class PartialCodeSelectHelper extends CodeSelectHelper {
        Set<String> skippedElements = new HashSet<String>();

        @Override
        protected CodeSelectRequestor createRequestor(
                GroovyCompilationUnit unit, ASTNode nodeToLookFor) {
            PartialCodeSelectRequestor partialCodeSelectRequestor = new PartialCodeSelectRequestor(
                    nodeToLookFor, unit);
            skippedElements = partialCodeSelectRequestor.skippedElements;
            return partialCodeSelectRequestor;
        }

        @Override
        public IJavaElement[] select(GroovyCompilationUnit unit, int start,
                int length) {
            helper.skippedElements.clear();
            return super.select(unit, start, length);
        }

        @Override
        public ASTNode selectASTNode(GroovyCompilationUnit unit, int start,
                int length) {
            helper.skippedElements.clear();
            return super.selectASTNode(unit, start, length);
        }

    }

    PartialCodeSelectHelper helper = new PartialCodeSelectHelper();

    public PartialVisitTest(String name) {
        super(name);
    }

    // should not visit the Foo class, or the main method
    public void testSimple() throws Exception {
        String contents = "new Foo().x\nclass Foo { \ndef x }\n";
        assertCodeSelectWithSkippedNames(contents, indexOf(contents, "x"), "x",
                "Hello", "main");
    }

    // should not visit the Hello constructor, Foo class, or the main method
    public void testSimple2() throws Exception {
        String contents = "class Foo { \ndef x }\nnew Foo().x";
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, "x"),
                "x", "Hello", "main", "Foo");
    }

    // should not visit the x field
    public void testSimple3() throws Exception {
        String contents = "class Foo { \ndef x\n def blah() { \nx } }";
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, "x"),
                "x", "x");
    }

    public void testFieldInitializer() throws Exception {
        String contents = "class Foo { Foo() { } \n def y \ndef x = y }";
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, "y"),
                "y", "Foo", "y");
    }

    // static initializers are now visited in place
    public void testStaticFieldInitializer() throws Exception {
        String contents = "class Foo { Foo() { } \n static y \n def z \nstatic x = y }";
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, "y"),
 "y", "Foo", "z", "y");
    }

    public void testInnerClass() throws Exception {
        String contents = "class Foo { Foo() { } \n static y \n def z \nstatic x = y \n class Inner { \n def blog \n def blag = y } }";
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, "y"),
                "y", "Foo", "x", "y", "z", "blog");
    }

    public void testInnerClass2() throws Exception {
        String contents = "class Foo { Foo() { } \n static y \n def z \nstatic x = y \n class Inner { \n def blog }\n def blag = y  }";
        assertCodeSelectWithSkippedNames(contents, lastIndexOf(contents, "y"),
                "y", "Foo", "x", "y", "z", "Inner");
    }

    private Region indexOf(String contents, String string) {
        return new Region(contents.indexOf(string), string.length());
    }

    private Region lastIndexOf(String contents, String string) {
        return new Region(contents.lastIndexOf(string), string.length());
    }

    private void assertCodeSelectWithSkippedNames(String contents,
            Region region, String expectedElementName,
            String... skippedElementNames) throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = helper.select(unit, region.getOffset(),
                region.getLength());
        assertEquals(
                "Should have found a single selection: " + Arrays.toString(elt),
                1, elt.length);
        assertEquals("Wrong element selected", expectedElementName,
                elt[0].getElementName());

        for (String skipped : skippedElementNames) {
            assertTrue(
                    "Element " + skipped
                            + " should have been skipped\nExpected: "
                            + Arrays.toString(skippedElementNames) + "\nWas: "
                            + helper.skippedElements,
                    helper.skippedElements.contains(skipped));
        }

        assertEquals(
                "Wrong number of elements skipped\nExpected: "
                        + Arrays.toString(skippedElementNames) + "\nWas: "
                        + helper.skippedElements,
                skippedElementNames.length, helper.skippedElements.size());
    }

}
