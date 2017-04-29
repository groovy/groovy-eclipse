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
package org.codehaus.groovy.eclipse.test.ui

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.editor.GroovyEditor
import org.codehaus.groovy.eclipse.editor.outline.GroovyOutlinePage
import org.codehaus.groovy.eclipse.editor.outline.OCompilationUnit
import org.codehaus.groovy.eclipse.editor.outline.OField
import org.codehaus.groovy.eclipse.editor.outline.OMethod
import org.codehaus.groovy.eclipse.editor.outline.OType
import org.codehaus.groovy.eclipse.editor.outline.OutlineExtenderRegistry
import org.codehaus.groovy.eclipse.test.EclipseTestCase
import org.codehaus.groovy.eclipse.test.EclipseTestSetup
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TCompilationUnit
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TGroovyOutlinePage
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TType
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender2.TCompilationUnit2
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.IField
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer
import org.eclipse.ui.internal.Workbench
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

final class OutlineExtenderTests extends EclipseTestCase {

    private final OutlineExtenderRegistry registry = GroovyPlugin.default.outlineTools.outlineExtenderRegistry

    @Before
    void setUp() {
        registry.initialize()
    }

    @After
    void tearDown() {
        // must close all opened editors
        Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false)
    }

    @Test
    void testStandardOutlineIfNoExtender() {
        String contents = "class X { }"
        GroovyOutlinePage outline = openFile("X", contents)

        // check no outline view
        Assert.assertNull(outline)
    }

    @Test
    void testStandardOutlineIfUnitNotApplyToExtender() {
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files

        String contents = "class Z { }"
        GroovyOutlinePage outline = openFile("Z", contents)

        // check no outline view
        Assert.assertNull(outline)
    }

    @Test
    void testOutlineActivated() {
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files

        String contents = "class X { }"
        GroovyOutlinePage outline = openFile("X", contents)

        // check outline view exists
        Assert.assertNotNull(outline)
    }

    @Test
    void testFirstDeclaredWins1() {
        // NOTE: addNature() appends to the head of the array
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files

        String contents = "class XY { }"
        GroovyOutlinePage outline = openFile("XY", contents)

        // check outline view exists
        TCompilationUnit tu = (TCompilationUnit) outline.getOutlineCompilationUnit()
        Assert.assertTrue(tu.outlineExtender.getClass() == OutlineExtender1.class)
    }

    @Test
    void testFirstDeclaredWins2() {
        // NOTE: addNature() appends to the head of the array
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files

        String contents = "class XY { }"
        GroovyOutlinePage outline = openFile("XY", contents)

        // check outline view exists
        TCompilationUnit tu = (TCompilationUnit) outline.getOutlineCompilationUnit()
        Assert.assertTrue(tu.outlineExtender.getClass() == OutlineExtender2.class)
    }

    @Test
    void testOutlineTreeConsistency1() {
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files

        String contents =
            "Integer field1 = 0 \n" +
            "String field2 = 'S' \n" +
            "Long method1() {} \n" +
            "Integer method2() { \n" +
            "  return 0 \n" + "}"
        TGroovyOutlinePage outline = (TGroovyOutlinePage) openFile("YTest", contents)
        TCompilationUnit2 tu = (TCompilationUnit2) outline.getOutlineCompilationUnit()
        // check consistency
        TType tx = (TType) tu.getChildren()[0]

        Assert.assertEquals(4, tx.getChildren().length)
        assertIsField(tx.getChildren()[0], "field1", "QInteger;")
        assertIsField(tx.getChildren()[1], "field2", "QString;")
        assertIsMethod(tx.getChildren()[2], "method1", "Long")
        assertIsMethod(tx.getChildren()[3], "method2", "Integer")
    }

    @Test
    void testOutlineTreeConsistency2() {
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files

        String contents =
            "inline1 { \n" +
            "  inline2 { \n" +
            "    Integer fieldA = 12 \n" +
            "  }\n" + "}"
        GroovyOutlinePage outline = openFile("YTest", contents)
        TCompilationUnit2 tu = (TCompilationUnit2) outline.getOutlineCompilationUnit()

        // check consistency
        TType yTest = (TType) tu.getChildren()[0]

        TType inline1 = (TType) yTest.getChildren()[0]
        Assert.assertEquals(1, inline1.getChildren().length)
        assertIsType(inline1.getChildren()[0], "inline2")

        TType inline2 = (TType) inline1.getChildren()[0]
        Assert.assertEquals(1, inline2.getChildren().length)
        assertIsField(inline2.getChildren()[0], "fieldA", "QInteger;")
    }

    @Test
    void testOutlineTreeSynchronized() {
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files

        String contents =
            "Integer field1 = 0 \n" +
            "String field2 = 'S'"
        GroovyOutlinePage outline = openFile("YTest", contents)

        TCompilationUnit2 tu = (TCompilationUnit2) outline.getOutlineCompilationUnit()

        // check consistency
        TType tx = (TType) tu.getChildren()[0]
        Assert.assertEquals(2, tx.getChildren().length)
        assertIsField(tx.getChildren()[0], "field1", "QInteger;")
        assertIsField(tx.getChildren()[1], "field2", "QString;")

        // update content
        GroovyEditor editor = (GroovyEditor) EclipseTestSetup.openInEditor(tu)
        JavaSourceViewer viewer = (JavaSourceViewer) editor.getViewer()
        viewer.getTextWidget().setSelection(0)
        viewer.getTextWidget().insert("Long field3 = 100 \n")
        buildAll()
        waitForIndex()
        tu.refresh()

        // check consistency
        tx = (TType) tu.getChildren()[0]
        Assert.assertEquals(3, tx.getChildren().length)
        assertIsField(tx.getChildren()[0], "field3", "QLong;")
        assertIsField(tx.getChildren()[1], "field1", "QInteger;")
        assertIsField(tx.getChildren()[2], "field2", "QString;")

        editor.close(false)
    }

    @Test
    void testUseGroovyScriptOutline() {
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files

        String contents = "int yyy"
        GroovyOutlinePage outline = openFile("Z", contents)

        // should use script outline extender
        Assert.assertEquals("Wrong outline extender chosen",
            "org.codehaus.groovy.eclipse.editor.outline.GroovyScriptOCompilationUnit",
            outline.getOutlineCompilationUnit().getClass().getName())
    }

    @Test
    void testGroovyScriptOutline1() {
        String contents =
            "import java.util.Map\n" +
            "int[] xxx \n" +
            "def ttt = 8\n" +
            "Object hhh = 8\n" +
            "class Y { }\n" +
            "String blah() {  }"
        GroovyOutlinePage outline = openFile("Script", contents)

        OCompilationUnit unit = outline.getOutlineCompilationUnit()
        IJavaElement[] children = unit.getChildren()

        Assert.assertEquals("Wrong number of children", 6, children.length)
        Assert.assertEquals("", children[0].getElementName()); // import container has no name
        Assert.assertEquals("xxx", children[1].getElementName())
        Assert.assertEquals("ttt", children[2].getElementName())
        Assert.assertEquals("hhh", children[3].getElementName())
        Assert.assertEquals("Y", children[4].getElementName())
        Assert.assertEquals("blah", children[5].getElementName())

        Assert.assertEquals(IJavaElement.IMPORT_CONTAINER, children[0].getElementType())
        Assert.assertEquals(IJavaElement.FIELD, children[1].getElementType())
        Assert.assertEquals(IJavaElement.FIELD, children[2].getElementType())
        Assert.assertEquals(IJavaElement.FIELD, children[3].getElementType())
        Assert.assertEquals(IJavaElement.TYPE, children[4].getElementType())
        Assert.assertEquals(IJavaElement.METHOD, children[5].getElementType())

        Assert.assertEquals("[I", ((IField) children[1]).getTypeSignature())
        Assert.assertEquals("Qdef;", ((IField) children[2]).getTypeSignature())
        Assert.assertEquals("QObject;", ((IField) children[3]).getTypeSignature())

        Assert.assertEquals(contents.indexOf("xxx"), ((IField) children[1]).getNameRange().getOffset())
        Assert.assertEquals(contents.indexOf("ttt"), ((IField) children[2]).getNameRange().getOffset())
        Assert.assertEquals(contents.indexOf("hhh"), ((IField) children[3]).getNameRange().getOffset())

        Assert.assertEquals(3, ((IField) children[1]).getNameRange().getLength())
        Assert.assertEquals(3, ((IField) children[2]).getNameRange().getLength())
        Assert.assertEquals(3, ((IField) children[3]).getNameRange().getLength())
    }

    @Test
    void testStructureUnknown() {
        String contents = "class X {  }\n int o( \n}"
        GroovyOutlinePage outline = openFile("Problem", contents)
        Assert.assertNull("X is not a script, so no Groovy outline should be available", outline)
        //OCompilationUnit unit = outline.getOutlineCompilationUnit()
        //IJavaElement[] children = unit.getChildren()
        //assertEquals(1, children.length)
        //assertEquals("Problem" + GroovyScriptOutlineExtender.NO_STRUCTURE_FOUND, children[0].getElementName())
    }

    private GroovyOutlinePage openFile(String className, String contents) {
        GroovyCompilationUnit unit = (GroovyCompilationUnit) testProject.createGroovyTypeAndPackage("", className + ".groovy", contents)
        unit.reconcile(true, null)
        GroovyEditor editor = (GroovyEditor) EclipseTestSetup.openInEditor(unit)

        GroovyOutlinePage outline = editor.getOutlinePage()
        if (!unit.isWorkingCopy()) {
            Assert.fail("not working copy")
        }
        if (unit.getModuleNode() == null) {
            Assert.fail("Module node is null")
        }
        if (outline != null) {
            outline.refresh()
            return outline
        }
        return null
    }

    /**
     * check that the element is the appropriate field
     */
    private void assertIsField(IJavaElement element, String name, String typeSignature) {
        Assert.assertTrue("Element" + element.getElementName() + " in not an instanceof OMethod", element instanceof OField)
        OField field = (OField) element
        Assert.assertTrue("Field name is '" + field.getElementName() + "' instead of '" + name + "'", name.equals(field.getElementName()))
        Assert.assertTrue("Field type signature is '" + field.getTypeSignature() + "' instead of '" + name + "'", typeSignature.equals(field.getTypeSignature()))
    }

    /**
     * check that the element is the appropriate method
     */
    private void assertIsMethod(IJavaElement element, String name, String returnType) {
        Assert.assertTrue("Element " + element.getElementName() + " in not an instanceof OMethod", element instanceof OMethod)
        OMethod method = (OMethod) element
        Assert.assertTrue("Method name is '" + method.getElementName() + "' instead of '" + name + "'", name.equals(method.getElementName()))
        if (returnType != null) {
            Assert.assertTrue("Method return type is '" + method.getReturnTypeName() + "' instead of '" + returnType + "'", returnType.equals(method.getReturnTypeName()))
        }
    }

    /**
     * check that the element is the appropriate type
     */
    private void assertIsType(IJavaElement element, String name) {
        Assert.assertTrue("Element " + element.getElementName() + " in not an instanceof OType", element instanceof OType)
        OType type = (OType) element
        Assert.assertTrue("Type name is '" + type.getElementName() + "' instead of '" + name + "'", name.equals(type.getElementName()))
    }
}
