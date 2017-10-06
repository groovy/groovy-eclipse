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
package org.codehaus.groovy.eclipse.test.adapters

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.part.FileEditorInput
import org.junit.Assert
import org.junit.Test

/**
 * Tests the Groovy File Adapter Factory.
 */
final class GroovyIFileEditorInputAdapterFactoryTests extends GroovyEclipseTestSuite {

    @Test
    void testIFileEditorInputAdapter() {
        def unit = addGroovySource('class MainClass { static void main(String[] args', 'MainClass', 'pack1')
        buildProject()

        IFileEditorInput editor = new FileEditorInput(unit.getResource())
        ClassNode node = editor.getAdapter(ClassNode.class)
        Assert.assertEquals('pack1.MainClass', node.getName())
        Assert.assertFalse(node.isInterface())
        Assert.assertNotNull(node.getMethods('main'))
    }

    @Test
    void testIFileEditorInputAdapterCompileError() {
        def unit = addGroovySource('class OtherClass { static void main(String[] args', 'OtherClass', 'pack1')
        buildProject()

        IFileEditorInput editor = new FileEditorInput(unit.getResource())
        ClassNode node = editor.getAdapter(ClassNode.class)
        Assert.assertEquals('pack1.OtherClass', node.getName())
        Assert.assertFalse(node.isInterface())
        Assert.assertNotNull(node.getMethods('main'))
    }

    @Test
    void testIFileEditorInputAdapterCompileError2() {
        def unit = addGroovySource('class C { abstract def foo() {} }', 'C', 'pack1')
        buildProject()

        IFileEditorInput editor = new FileEditorInput(unit.getResource())
        Assert.assertNull(editor.getAdapter(ClassNode.class))
    }

    @Test
    void testIFileEditorInputAdapterNotGroovyFile() {
        def file = addPlainText('this is not a groovy file', 'NotGroovy.file')
        buildProject()

        IFileEditorInput editor = new FileEditorInput(file)
        Assert.assertNull(editor.getAdapter(ClassNode.class))
    }
}
