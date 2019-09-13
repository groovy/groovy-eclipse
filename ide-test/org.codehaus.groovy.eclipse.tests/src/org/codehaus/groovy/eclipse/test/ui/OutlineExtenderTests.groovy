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
package org.codehaus.groovy.eclipse.test.ui

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.editor.GroovyEditor
import org.codehaus.groovy.eclipse.editor.outline.GroovyOutlinePage
import org.codehaus.groovy.eclipse.editor.outline.OField
import org.codehaus.groovy.eclipse.editor.outline.OMethod
import org.codehaus.groovy.eclipse.editor.outline.OType
import org.codehaus.groovy.eclipse.editor.outline.OutlineExtenderRegistry
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TCompilationUnit
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TGroovyOutlinePage
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender1.TType
import org.codehaus.groovy.eclipse.test.ui.OutlineExtender2.TCompilationUnit2
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.Adapters
import org.eclipse.jdt.core.IField
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer
import org.eclipse.ui.views.contentoutline.IContentOutlinePage
import org.junit.After
import org.junit.Before
import org.junit.Test

final class OutlineExtenderTests extends GroovyEclipseTestSuite {

    private final OutlineExtenderRegistry registry = GroovyPlugin.default.outlineTools.outlineExtenderRegistry

    @Before
    void setUp() {
        registry.initialize()
    }

    @After
    void tearDown() {
        removeNature(OutlineExtender1.NATURE, OutlineExtender2.NATURE)
    }

    @Test
    void testStandardOutlineIfNoExtender() {
        GroovyOutlinePage outline = openFile('X', 'class X { }')

        // check no outline view
        assert outline == null
    }

    @Test
    void testStandardOutlineIfUnitNotApplyToExtender() {
        addNature(OutlineExtender1.NATURE) // applies to *X*.groovy files

        GroovyOutlinePage outline = openFile('Z', 'class Z { }')

        // check no outline view
        assert outline == null
    }

    @Test
    void testOutlineActivated() {
        addNature(OutlineExtender1.NATURE) // applies to *X*.groovy files

        GroovyOutlinePage outline = openFile('X', 'class X { }')

        // check outline view exists
        assert outline != null
    }

    @Test
    void testFirstDeclaredWins1() {
        // NOTE: addNature() appends to the head of the array
        addNature(OutlineExtender2.NATURE) // applies to *Y*.groovy files
        addNature(OutlineExtender1.NATURE) // applies to *X*.groovy files

        GroovyOutlinePage outline = openFile('XY', 'class XY { }')

        // check outline view exists
        TCompilationUnit tu = (TCompilationUnit) outline.outlineCompilationUnit
        assert tu.outlineExtender.getClass() == OutlineExtender1
    }

    @Test
    void testFirstDeclaredWins2() {
        // NOTE: addNature() appends to the head of the array
        addNature(OutlineExtender1.NATURE) // applies to *X*.groovy files
        addNature(OutlineExtender2.NATURE) // applies to *Y*.groovy files

        GroovyOutlinePage outline = openFile('XY', 'class XY { }')

        // check outline view exists
        TCompilationUnit tu = (TCompilationUnit) outline.outlineCompilationUnit
        assert tu.outlineExtender.getClass() == OutlineExtender2
    }

    @Test
    void testOutlineTreeConsistency1() {
        addNature(OutlineExtender2.NATURE) // applies to *Y*.groovy files

        TGroovyOutlinePage outline = (TGroovyOutlinePage) openFile('YTest', '''\
            Integer field1 = 0
            String field2 = 'S'
            Long method1() {}
            Integer method2() {
              return 0
            }'''.stripIndent())
        TCompilationUnit2 tu = (TCompilationUnit2) outline.outlineCompilationUnit
        def children = (tu.children[0] as TType).children

        assert children.length == 4
        assertIsField(children[0], 'field1', 'QInteger;')
        assertIsField(children[1], 'field2', 'QString;')
        assertIsMethod(children[2], 'method1', 'Long')
        assertIsMethod(children[3], 'method2', 'Integer')
    }

    @Test
    void testOutlineTreeConsistency2() {
        addNature(OutlineExtender2.NATURE) // applies to *Y*.groovy files

        GroovyOutlinePage outline = openFile('YTest', '''\
            inline1 {
              inline2 {
                Integer fieldA = 12
              }
            }'''.stripIndent())
        TCompilationUnit2 tu = (TCompilationUnit2) outline.outlineCompilationUnit

        // check consistency
        TType yTest = (TType) tu.children[0]

        TType inline1 = (TType) yTest.children[0]
        assert inline1.children.length == 1
        assertIsType(inline1.children[0], 'inline2')

        TType inline2 = (TType) inline1.children[0]
        assert inline2.children.length == 1
        assertIsField(inline2.children[0], 'fieldA', 'QInteger;')
    }

    @Test
    void testOutlineTreeSynchronized() {
        addNature(OutlineExtender2.NATURE) // applies to *Y*.groovy files

        GroovyOutlinePage outline = openFile('YTest', '''\
            Integer field1 = 0
            String field2 = "S"
            '''.stripIndent())
        TCompilationUnit2 tu = (TCompilationUnit2) outline.outlineCompilationUnit

        // check consistency
        TType tx = (TType) tu.children[0]
        assert tx.children.length == 2
        assertIsField(tx.children[0], 'field1', 'QInteger;')
        assertIsField(tx.children[1], 'field2', 'QString;')

        // update content
        GroovyEditor editor = (GroovyEditor) openInEditor(tu)
        JavaSourceViewer viewer = (JavaSourceViewer) editor.viewer
        viewer.textWidget.selection = 0
        viewer.textWidget.insert('Long field3 = 100 \n')
        buildProject()
        waitForIndex()
        tu.refresh()

        // check consistency
        tx = (TType) tu.children[0]
        assert tx.children.length == 3
        assertIsField(tx.children[0], 'field3', 'QLong;')
        assertIsField(tx.children[1], 'field1', 'QInteger;')
        assertIsField(tx.children[2], 'field2', 'QString;')
    }

    @Test
    void testGroovyClassOutline1() {
        def unit = addGroovySource '''\
            class Pogo {
              String value
            }
            '''.stripIndent()
        def editor = openInEditor(unit)
        def viewer = Adapters.adapt(editor, IContentOutlinePage).outlineViewer
        IJavaElement[] children = viewer.getRawChildren(viewer.getRoot())

        assert children.size() == 1
        assert children[0].elementName == 'Pogo'
        assert children[0].elementType == IJavaElement.TYPE

        children = viewer.getRawChildren(children[0])

        assert children.size() == 1
        assert children[0].elementName == 'value'
        assert children[0].elementType == IJavaElement.FIELD
    }

    @Test
    void testGroovyClassOutline2() {
        def unit = addGroovySource '''\
            @groovy.transform.Sortable
            class Pogo {
              String value
            }
            '''.stripIndent()
        def editor = openInEditor(unit)
        def viewer = Adapters.adapt(editor, IContentOutlinePage).outlineViewer
        IJavaElement[] children = viewer.getRawChildren(viewer.getRoot())

        assert children.size() == 1
        assert children[0].elementName == 'Pogo'
        assert children[0].elementType == IJavaElement.TYPE

        children = viewer.getRawChildren(children[0])

        assert children.size() == 2
        assert children[0].elementName == 'value'
        assert children[0].elementType == IJavaElement.FIELD
        assert children[1].elementName == 'compareTo'
        assert children[1].elementType == IJavaElement.METHOD
    }

    @Test
    void testGroovyScriptOutline0() {
        GroovyOutlinePage outline = openFile('Z', 'int yyy')

        // should use script outline extender
        String oCompUnitName = outline.outlineCompilationUnit.class.simpleName
        assert oCompUnitName == 'GroovyScriptOCompilationUnit' : 'Wrong outline extender chosen'
    }

    @Test
    void testGroovyScriptOutline1() {
        String contents = '''\
            import java.util.Map
            int[] xxx
            def ttt = 8
            Object hhh = 8
            class Y { }
            String blah() {  }
            '''.stripIndent()
        GroovyOutlinePage outline = openFile('Script1', contents)
        IJavaElement[] children = outline.outlineCompilationUnit.children

        assert children.length == 6
        assert children[0].elementName == '' // import container has no name
        assert children[1].elementName == 'xxx'
        assert children[2].elementName == 'ttt'
        assert children[3].elementName == 'hhh'
        assert children[4].elementName == 'Y'
        assert children[5].elementName == 'blah'

        assert children[0].elementType == IJavaElement.IMPORT_CONTAINER
        assert children[1].elementType == IJavaElement.FIELD
        assert children[2].elementType == IJavaElement.FIELD
        assert children[3].elementType == IJavaElement.FIELD
        assert children[4].elementType == IJavaElement.TYPE
        assert children[5].elementType == IJavaElement.METHOD

        assert ((IField) children[1]).typeSignature == '[I'
        assert ((IField) children[2]).typeSignature == 'Qdef;'
        assert ((IField) children[3]).typeSignature == 'Qjava.lang.Object;'

        assert ((IField) children[1]).nameRange.offset == contents.indexOf('xxx')
        assert ((IField) children[2]).nameRange.offset == contents.indexOf('ttt')
        assert ((IField) children[3]).nameRange.offset == contents.indexOf('hhh')

        assert ((IField) children[1]).nameRange.length == 3
        assert ((IField) children[2]).nameRange.length == 3
        assert ((IField) children[3]).nameRange.length == 3
    }

    @Test
    void testGroovyScriptOutline2() {
        String contents = '''\
            Map<String, List<java.lang.String>> map
            '''.stripIndent()
        GroovyOutlinePage outline = openFile('Script1', contents)
        IJavaElement[] children = outline.outlineCompilationUnit.children

        assert children.length == 1
        assert children[0].elementName == 'map'
        assert children[0].elementType == IJavaElement.FIELD
        assert children[0].typeSignature == 'Qjava.util.Map<Qjava.lang.String;Qjava.util.List<Qjava.lang.String;>;>;'
    }

    @Test
    void testGroovyScriptOutline3() {
        String contents = '''\
            import groovy.transform.Field
            @Field Map<String,Object> map = [:]
            @Newify Object obj = Object.new()
            '''.stripIndent()
        GroovyOutlinePage outline = openFile('Script2', contents)
        IJavaElement[] children = outline.outlineCompilationUnit.children

        assert children.tail()*.elementName == ['map', 'obj']
        assert children.tail()*.typeSignature == ['Qjava.util.Map<QString;QObject;>;', 'Qjava.lang.Object;']
        assert children*.elementType == [IJavaElement.IMPORT_CONTAINER, IJavaElement.FIELD, IJavaElement.FIELD]
    }

    @Test
    void testStructureUnknown() {
        GroovyOutlinePage outline = openFile('Problem', '''\
            class X {  }
             int o(
            }
            '''.stripIndent())

        assert outline == null : 'X is not a script, so no Groovy outline should be available'
        //IJavaElement[] children = outline.outlineCompilationUnit.children
        //assertEquals(1, children.length)
        //assertEquals('Problem -- No structure found', children[0].elementName)
    }

    //--------------------------------------------------------------------------

    private GroovyOutlinePage openFile(String className, String contents) {
        GroovyCompilationUnit unit = addGroovySource(contents, className)
        GroovyEditor editor = (GroovyEditor) openInEditor(unit)
        assert unit.isWorkingCopy() : 'not working copy'
        assert unit.getModuleNode() != null : 'Module node is null'
        GroovyOutlinePage outline = editor.outlinePage
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
        assert element instanceof OField : "Element $element.elementName is not an instanceof OField"
        assert element.elementName == name : "Field name is '$element.elementName' instead of '$name'"
        assert element.typeSignature == typeSignature : "Field type signature is '$element.typeSignature' instead of '$typeSignature'"
    }

    /**
     * check that the element is the appropriate method
     */
    private void assertIsMethod(IJavaElement element, String name, String returnType) {
        assert element instanceof OMethod : "Element $element.elementName is not an instanceof OMethod"
        assert element.elementName == name : "Method name is '$element.elementName' instead of '$name'"
        if (returnType != null) {
            assert element.returnTypeName == returnType : "Method return type is '$element.returnTypeName' instead of '$returnType'"
        }
    }

    /**
     * check that the element is the appropriate type
     */
    private void assertIsType(IJavaElement element, String name) {
        assert element instanceof OType : "Element $element.elementName is not an instanceof OType"
        assert element.elementName == name : "Type name is '$element.elementName' instead of '$name'"
    }
}
