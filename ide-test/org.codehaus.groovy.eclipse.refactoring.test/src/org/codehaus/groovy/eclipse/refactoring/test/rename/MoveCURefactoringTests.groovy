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
package org.codehaus.groovy.eclipse.refactoring.test.rename

import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameRefactoringTestSuite.TestSource
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory
import org.eclipse.ltk.core.refactoring.Refactoring
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.Ignore
import org.junit.Test

/**
 * Ensures that moving compilaiton units and scripts between packages updates import statements appropriately.
 */
final class MoveCURefactoringTests extends RenameRefactoringTestSuite {

    // assume we are moving the first CU to the new specified package
    private void performRefactoringAndUndo(String newPackageName, TestSource... sources) {
        ICompilationUnit[] units = createUnits(sources)

        MoveDescriptor descriptor = RefactoringSignatureDescriptorFactory.createMoveDescriptor()
        descriptor.setMoveResources(new IFile[0], new IFolder[0], [units[0]] as ICompilationUnit[])
        descriptor.setDestination(getPackageFragment(newPackageName))
        descriptor.setUpdateQualifiedNames(true)
        descriptor.setUpdateReferences(true)

        Refactoring refactoring = createRefactoring(descriptor)
        RefactoringStatus result = performRefactoring(refactoring, true)

        result = ignoreKnownErrors(result)

        assert result.isOK() : 'Refactoring produced an error: ' + result

        ICompilationUnit newUnit = getNewUnit(newPackageName, sources[0].name)
        ICompilationUnit origUnit = units[0]
        units[0] = newUnit
        assertContents(units, sources*.finalContents)

        // undo
        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '! anythingToRedo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())

        units[0] = origUnit
        assertContents(units, sources*.contents)

        // redo
        assert !RefactoringCore.getUndoManager().anythingToUndo() : '! anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'
        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())
        units[0] = newUnit
        assertContents(units, sources*.finalContents)
    }

    private ICompilationUnit getNewUnit(String newPackName, String name) {
        String typeName = name.substring(0, name.indexOf('.'))
        String qualName = newPackName.length() > 0 ? newPackName + '.' + typeName : typeName

        packageFragmentRoot.javaProject.findType(qualName).compilationUnit
    }

    @Test
    void testSimpleMove1() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Java.java',
            contents: 'package p;\n\npublic class Java { }',
            finalContents: 'package NEW;\n\npublic class Java { }'
        ))
    }

    @Test
    void testSimpleMove2() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\n\npublic class Groovy { }',
            finalContents: 'package NEW;\n\npublic class Groovy { }'
        ))
    }

    @Test
    void testSimpleMove3() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Java.java',
            contents: 'package p;\n\npublic class Java { }',
            finalContents: 'package NEW;\n\npublic class Java { }'
        ), new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\n\npublic class Groovy extends Java { }',
            finalContents: 'package p;\n\nimport NEW.Java\n\npublic class Groovy extends Java { }'
        ))
    }

    @Test
    void testQualifiedMove1() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Java.java',
            contents: 'package p;\n\npublic class Java { }',
            finalContents: 'package NEW;\n\npublic class Java { }'
        ), new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\n\npublic class Groovy extends p.Java { }',
            finalContents: 'package p;\n\npublic class Groovy extends NEW.Java { }'
        ))
    }

    @Test
    void testSimpleMove4() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p1', name: 'Groovy.groovy',
            contents: 'package p1\n\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }',
            finalContents: 'package NEW\n\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }'
        ), new TestSource(
            pack: 'p2', name: 'Groovy2.groovy',
            contents: 'package p2\n\nimport p1.Groovy\npublic class Groovy2 extends Groovy { }',
            finalContents: 'package p2\n\nimport NEW.Groovy\npublic class Groovy2 extends Groovy { }'
        ))
    }

    @Test
    void testQualifiedMove2() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p1', name: 'Groovy.groovy',
            contents: 'package p1\n\npublic class Groovy {\np2.Groovy2 g }',
            finalContents: 'package NEW\n\npublic class Groovy {\np2.Groovy2 g }'
        ), new TestSource(
            pack: 'p2', name: 'Groovy2.groovy',
            contents: 'package p2\n\npublic class Groovy2 extends p1.Groovy { }',
            finalContents: 'package p2\n\npublic class Groovy2 extends NEW.Groovy { }'
        ))
    }

    @Test
    void testNonPrimaryMove1() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p1', name: 'GroovyFoo.groovy',
            contents: 'package p1\n\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }',
            finalContents: 'package NEW\n\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }'
        ), new TestSource(
            pack: 'p2', name: 'GroovyFoo2.groovy',
            contents: 'package p2\n\nimport p1.Groovy\npublic class Groovy2 extends Groovy { }',
            finalContents: 'package p2\n\nimport NEW.Groovy\npublic class Groovy2 extends Groovy { }'
        ))
    }

    @Test
    void testNonPrimaryQualifiedMove1() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p1', name: 'GroovyFoo.groovy',
            contents: 'package p1\n\npublic class Groovy {\np2.Groovy2 g }',
            finalContents: 'package NEW\n\npublic class Groovy {\np2.Groovy2 g }'
        ), new TestSource(
            pack: 'p2', name: 'GroovyFoo2.groovy',
            contents: 'package p2\n\npublic class Groovy2 extends p1.Groovy { }',
            finalContents: 'package p2\n\npublic class Groovy2 extends NEW.Groovy { }'
        ))
    }

    @Test
    void testMoveBack1() {
        performRefactoringAndUndo('p2', new TestSource(
            pack: 'p1', name: 'Groovy.groovy',
            contents: 'package p1\n\nimport p2.Groovy2\n\npublic class Groovy { Groovy2 g }',
            finalContents: 'package p2\n\npublic class Groovy { Groovy2 g }'
        ), new TestSource(
            pack: 'p2', name: 'Groovy2.groovy',
            contents: 'package p2\n\nimport p1.Groovy\n\npublic class Groovy2 extends Groovy { }',
            finalContents: 'package p2\n\npublic class Groovy2 extends Groovy { }'
        ))
    }

    @Test @Ignore('see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205')
    void testInnerMove1() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\n\npublic class Groovy { \n class Inner { } }',
            finalContents: 'package NEW;\n\npublic class Groovy { \n class Inner { } }'
        ), new TestSource(
            pack: 'p', name: 'Groovy2.groovy',
            contents: 'package p;\n\npublic class Groovy2 extends Groovy.Inner { }',
            finalContents: 'package p;\n\nimport NEW.Groovy;\npublic class Groovy2 extends Groovy.Inner { }'
        ))
    }

    @Test @Ignore('see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205')
    void testInnerMove2() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy2.groovy',
            contents: 'package p;\n\npublic class Groovy2 extends Groovy.Inner { }',
            finalContents: 'package NEW;\n\nimport p.Groovy;\npublic class Groovy2 extends Groovy.Inner { }'
        ), new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\n\npublic class Groovy { \n class Inner { } }',
            finalContents: 'package p;\n\npublic class Groovy { \n class Inner { } }'
        ))
    }

    @Test @Ignore('see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205')
    void testInnerMove3() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\n\npublic class Groovy { \n class Inner { } }',
            finalContents: 'package NEW;\n\npublic class Groovy { \n class Inner { } }'
        ), new TestSource(
            pack: 'p', name: 'Groovy2.groovy',
            contents: 'package p;\n\nimport Groovy.Inner\npublic class Groovy2 extends Inner { }',
            finalContents: 'package p;\n\nimport NEW.Groovy.Inner;\npublic class Groovy2 extends Inner { }'
        ))
    }

    @Test @Ignore('see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205')
    void testInnerMove4() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy2.groovy',
            contents: 'package p;\n\nimport Groovy.Inner\npublic class Groovy2 extends Inner { }',
            finalContents: 'package NEW;\n\nimport p.Groovy.Inner;\npublic class Groovy2 extends Inner { }'
        ), new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\n\npublic class Groovy { \n class Inner { } }',
            finalContents: 'package p;\n\npublic class Groovy { \n class Inner { } }'
        ))
    }
}
