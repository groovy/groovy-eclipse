/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.adapters

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.core.runtime.Adapters
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.part.FileEditorInput
import org.junit.Test

/**
 * Tests the Groovy File Adapter Factory.
 */
final class GroovyIFileEditorInputAdapterFactoryTests extends GroovyEclipseTestSuite {

    @Test
    void testIFileEditorInputAdapter() {
        def unit = addGroovySource('class MainClass { static void main(String[] args) {} }', 'MainClass', 'pack1')
        buildProject()

        IFileEditorInput editor = new FileEditorInput(unit.resource)
        ClassNode node = Adapters.adapt(editor, ClassNode)
        assert node.name == 'pack1.MainClass'
        assert !node.getMethods('main').empty
        assert !node.isInterface()
    }

    @Test
    void testIFileEditorInputAdapterCompileError() {
        def unit = addGroovySource('class OtherClass { static void main(String[] args', 'OtherClass', 'pack1')
        buildProject()

        IFileEditorInput editor = new FileEditorInput(unit.resource)
        ClassNode node = Adapters.adapt(editor, ClassNode)
        assert node.name == 'pack1.OtherClass'
        assert node.getMethods('main').empty
        assert !node.isInterface()
    }

    @Test
    void testIFileEditorInputAdapterCompileError2() {
        def unit = addGroovySource('class C { abstract def foo() {} }', 'C', 'pack1')
        buildProject()

        IFileEditorInput editor = new FileEditorInput(unit.resource)
        ClassNode node = Adapters.adapt(editor, ClassNode)
        assert node == null
    }

    @Test
    void testIFileEditorInputAdapterNotGroovyFile() {
        def file = addPlainText('this is not a groovy file', 'NotGroovy.file')
        buildProject()

        IFileEditorInput editor = new FileEditorInput(file)
        ClassNode node = Adapters.adapt(editor, ClassNode)
        assert node == null
    }
}
