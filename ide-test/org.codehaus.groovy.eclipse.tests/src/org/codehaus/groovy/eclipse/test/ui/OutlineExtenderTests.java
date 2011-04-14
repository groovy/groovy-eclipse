/*
 * Copyright 2003-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.codehaus.groovy.eclipse.test.ui;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.editor.outline.GroovyOutlinePage;
import org.codehaus.groovy.eclipse.editor.outline.GroovyScriptOutlineExtender;
import org.codehaus.groovy.eclipse.editor.outline.OCompilationUnit;
import org.codehaus.groovy.eclipse.editor.outline.OField;
import org.codehaus.groovy.eclipse.editor.outline.OMethod;
import org.codehaus.groovy.eclipse.editor.outline.OType;
import org.codehaus.groovy.eclipse.editor.outline.OutlineExtenderRegistry;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TCompilationUnit;
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TGroovyOutlinePage;
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TType;
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender2.TCompilationUnit2;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.Workbench;

/**
 * @author Maxime Hamm
 * @created April 11, 2011
 */
public class OutlineExtenderTests extends EclipseTestCase {

    private OutlineExtenderRegistry registry;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyRuntime.addGroovyNature(testProject.getProject());
        registry = GroovyPlugin.getDefault().getOutlineTools()
                .getOutlineExtenderRegistry();
        registry.initialize();
    }

    @Override
    protected void tearDown() throws Exception {
        // must close all opened editors
        Workbench.getInstance().getActiveWorkbenchWindow().getActivePage()
                .closeAllEditors(false);
        super.tearDown();
    }

    public void testStandardOutlineIfNoExtender() throws Exception {
        String contents = "class X { }";
        GroovyOutlinePage outline = openFile("X", contents);

        // check no outline view
        assertNull(outline);
    }

    public void testStandardOutlineIfUnitNotApplyToExtender() throws Exception {
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files

        String contents = "class Z { }";
        GroovyOutlinePage outline = openFile("Z", contents);

        // check no outline view
        assertNull(outline);
    }

    public void testOutlineActivated() throws Exception {
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files

        String contents = "class X { }";
        GroovyOutlinePage outline = openFile("X", contents);

        // check outline view exists
        assertNotNull(outline);
    }

    public void testFirstDeclaredWins1() throws Exception {
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files

        String contents = "class XY { }";
        GroovyOutlinePage outline = openFile("XY", contents);

        // check outline view exists
        TCompilationUnit tu = (TCompilationUnit) outline
                .getOutlineCompilationUnit();
        assertTrue(tu.outlineExtender.getClass() == OutlineExtender1.class);
    }

    public void testFirstDeclaredWins2() throws Exception {
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files

        String contents = "class XY { }";
        GroovyOutlinePage outline = openFile("XY", contents);

        // check outline view exists
        TCompilationUnit tu = (TCompilationUnit) outline
                .getOutlineCompilationUnit();
        assertTrue(tu.outlineExtender.getClass() == OutlineExtender2.class);
    }

    public void testOutlineTreeConsistency1() throws Exception {
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files

        String contents = "Integer field1 = 0 \n" + "String field2 = 'S' \n"
                + "Long method1() {} \n" + "Integer method2() { \n"
                + "  return 0 \n" + "}";
        TGroovyOutlinePage outline = (TGroovyOutlinePage) openFile("YTest", contents);
        TCompilationUnit2 tu = (TCompilationUnit2) outline
                .getOutlineCompilationUnit();
        // check consistency
        TType tx = (TType) tu.getChildren()[0];

        assertEquals(4, tx.getChildren().length);
        assertIsField(tx.getChildren()[0], "field1", "Integer");
        assertIsField(tx.getChildren()[1], "field2", "String");
        assertIsMethod(tx.getChildren()[2], "method1", "Long");
        assertIsMethod(tx.getChildren()[3], "method2", "Integer");
    }

    public void testOutlineTreeConsistency2() throws Exception {
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files

        String contents = "inline1 { \n" + "  inline2 { \n"
                + "    Integer fieldA = 12 \n" + "  }\n" + "}";
        GroovyOutlinePage outline = openFile("YTest", contents);
        TCompilationUnit2 tu = (TCompilationUnit2) outline
                .getOutlineCompilationUnit();

        // check consistency
        TType yTest = (TType) tu.getChildren()[0];

        TType inline1 = (TType) yTest.getChildren()[0];
        assertEquals(1, inline1.getChildren().length);
        assertIsType(inline1.getChildren()[0], "inline2");

        TType inline2 = (TType) inline1.getChildren()[0];
        assertEquals(1, inline2.getChildren().length);
        assertIsField(inline2.getChildren()[0], "fieldA", "Integer");
    }

    public void testOutlineTreeSynchronized() throws Exception {
        testProject.addNature(OutlineExtender2.NATURE); // applies to *Y*.groovy files

        String contents = "Integer field1 = 0 \n" + "String field2 = 'S'";

        GroovyOutlinePage outline = openFile("YTest", contents);

        TCompilationUnit2 tu = (TCompilationUnit2) outline
                .getOutlineCompilationUnit();

        // check consistency
        TType tx = (TType) tu.getChildren()[0];
        assertEquals(2, tx.getChildren().length);
        assertIsField(tx.getChildren()[0], "field1", "Integer");
        assertIsField(tx.getChildren()[1], "field2", "String");

        // update content
        GroovyEditor editor = getGroovyEditor(tu);
        JavaSourceViewer viewer = (JavaSourceViewer) editor.getViewer();
        viewer.getTextWidget().setSelection(0);
        viewer.getTextWidget().insert("Long field3 = 100 \n");
        fullProjectBuild();
        waitForIndexes();
        tu.refresh();

        // check consistency
        tx = (TType) tu.getChildren()[0];
        assertEquals(3, tx.getChildren().length);
        assertIsField(tx.getChildren()[0], "field3", "Long");
        assertIsField(tx.getChildren()[1], "field1", "Integer");
        assertIsField(tx.getChildren()[2], "field2", "String");

        editor.close(false);

    }
    
    
    public void testUseGroovyScriptOutline() throws Exception {
        testProject.addNature(OutlineExtender1.NATURE); // applies to *X*.groovy files

        String contents = "int yyy";
        GroovyOutlinePage outline = openFile("Z", contents);

        // should use script outline extender
        assertEquals("Wrong outline extender chosen", "org.codehaus.groovy.eclipse.editor.outline.GroovyScriptOCompilationUnit", 
                outline.getOutlineCompilationUnit().getClass().getName());
    }

    
    public void testGroovyScriptOutline1() throws Exception {
        String contents = 
            "import java.util.Map\n" +
        	"int[] xxx \n" +
        	"def ttt = 8\n" +
        	"Object hhh = 8\n" +
        	"class Y { }\n" +
        	"String blah() {  }";
        GroovyOutlinePage outline = openFile("Script", contents);

        OCompilationUnit unit = outline
                .getOutlineCompilationUnit();
        IJavaElement[] children = unit.getChildren();
        
        assertEquals("Wrong number of children", 6, children.length);
        assertEquals("", children[0].getElementName());  // import container has no name
        assertEquals("xxx", children[1].getElementName());
        assertEquals("ttt", children[2].getElementName());
        assertEquals("hhh", children[3].getElementName());
        assertEquals("Y", children[4].getElementName());
        assertEquals("blah", children[5].getElementName());

        assertEquals(IJavaElement.IMPORT_CONTAINER, children[0].getElementType());
        assertEquals(IJavaElement.FIELD, children[1].getElementType());
        assertEquals(IJavaElement.FIELD, children[2].getElementType());
        assertEquals(IJavaElement.FIELD, children[3].getElementType());
        assertEquals(IJavaElement.TYPE, children[4].getElementType());
        assertEquals(IJavaElement.METHOD, children[5].getElementType());
        
        assertEquals("[I", ((IField) children[1]).getTypeSignature());
        assertEquals("Qdef;", ((IField) children[2]).getTypeSignature());
        assertEquals("QObject;", ((IField) children[3]).getTypeSignature());
        
        assertEquals(contents.indexOf("xxx"), ((IField) children[1]).getNameRange().getOffset());
        assertEquals(contents.indexOf("ttt"), ((IField) children[2]).getNameRange().getOffset());
        assertEquals(contents.indexOf("hhh"), ((IField) children[3]).getNameRange().getOffset());

        assertEquals(3, ((IField) children[1]).getNameRange().getLength());
        assertEquals(3, ((IField) children[2]).getNameRange().getLength());
        assertEquals(3, ((IField) children[3]).getNameRange().getLength());
    }
    
    public void testStructureUnknown() throws Exception {
        String contents = 
            "class X {  }\n int o( \n}";
        GroovyOutlinePage outline = openFile("Problem", contents);
        OCompilationUnit unit = outline.getOutlineCompilationUnit();
        IJavaElement[] children = unit.getChildren();
        assertEquals(1, children.length);
        assertEquals("Problem" + GroovyScriptOutlineExtender.NO_STRUCTURE_FOUND, children[0].getElementName());
    }

    private GroovyOutlinePage openFile(String className, String contents)
            throws Exception {
        IFile file = testProject.createGroovyTypeAndPackage("", className
                + ".groovy", contents);

        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore
                .createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(null);
        unit.reconcile(true, null);
        GroovyEditor editor = getGroovyEditor(unit);

        GroovyOutlinePage outline = editor.getOutlinePage();
        if (!unit.isWorkingCopy()) {
            fail("not working copy");
        }
        if (unit.getModuleNode() == null) {
            fail("Module node is null");
        }
        if (outline != null) {
            outline.refresh();
            return outline;
        }
        return null;
    }

    /**
     * get the groovy editor
     * @return
     * @throws PartInitException
     */
    private GroovyEditor getGroovyEditor(GroovyCompilationUnit unit)
            throws PartInitException {
        return (GroovyEditor) EditorUtility.openInEditor(unit);
    }

    /**
     * check that the element is the appropriate field
     * @param element
     * @param name
     * @param signature
     * @throws JavaModelException
     */
    private void assertIsField(IJavaElement element, String name,
            String typeSignature) throws JavaModelException {
        assertTrue("Element" + element.getElementName()
                + " in not an instanceof OMethod", element instanceof OField);

        OField field = (OField) element;
        assertTrue("Field name is '" + field.getElementName()
                + "' instead of '" + name + "'",
                name.equals(field.getElementName()));
        assertTrue("Field type signature is '" + field.getTypeSignature()
                + "' instead of '" + name + "'",
                typeSignature.equals(field.getTypeSignature()));
    }

    /**
     * check that the element is the appropriate method
     * @param element
     * @param name
     * @param returnType
     * @throws JavaModelException
     */
    private void assertIsMethod(IJavaElement element, String name,
            String returnType) throws JavaModelException {
        assertTrue("Element " + element.getElementName()
                + " in not an instanceof OMethod", element instanceof OMethod);

        OMethod method = (OMethod) element;
        assertTrue("Method name is '" + method.getElementName()
                + "' instead of '" + name + "'",
                name.equals(method.getElementName()));
        if (returnType != null) {
            assertTrue("Method return type is '" + method.getReturnTypeName()
                    + "' instead of '" + returnType + "'",
                    returnType.equals(method.getReturnTypeName()));
        }
    }

    /**
     * check that the element is the appropriate type
     * @param element
     * @param name
     * @param returnType
     * @throws JavaModelException
     */
    private void assertIsType(IJavaElement element, String name)
            throws JavaModelException {
        assertTrue("Element " + element.getElementName()
                + " in not an instanceof OType", element instanceof OType);

        OType type = (OType) element;
        assertTrue("Type name is '" + type.getElementName() + "' instead of '"
                + name + "'", name.equals(type.getElementName()));
    }
}
