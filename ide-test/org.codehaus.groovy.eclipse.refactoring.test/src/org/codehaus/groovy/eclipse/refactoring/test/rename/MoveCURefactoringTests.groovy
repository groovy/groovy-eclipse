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
package org.codehaus.groovy.eclipse.refactoring.test.rename

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS
import static org.eclipse.jdt.ui.PreferenceConstants.ORGIMPORTS_IMPORTORDER
import static org.eclipse.ltk.core.refactoring.RefactoringCore.getUndoManager

import groovy.transform.NotYetImplemented

import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameRefactoringTestSuite.TestSource
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory
import org.eclipse.ltk.core.refactoring.Refactoring
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.Before
import org.junit.Test

/**
 * Ensures that moving compilaiton units between packages correctly updates
 * import statements and fully-qualified names.
 */
final class MoveCURefactoringTests extends RenameRefactoringTestSuite {

    /**
     * Moves the first source unit to the specified new package.  References to
     * any types contained within should be updated in the other source units.
     */
    private void performRefactoringAndUndo(String newPackageName, TestSource... sources) {
        ICompilationUnit[] units = createUnits(sources)

        MoveDescriptor descriptor = RefactoringSignatureDescriptorFactory.createMoveDescriptor()
        descriptor.setMoveResources(new IFile[0], new IFolder[0], units[0])
        descriptor.destination = getPackageFragment(newPackageName)
        descriptor.updateQualifiedNames = true
        descriptor.updateReferences = true

        Refactoring refactoring = createRefactoring(descriptor)
        RefactoringStatus result = performRefactoring(refactoring, true)

        result = ignoreKnownErrors(result)

        assert result.isOK() : 'Refactoring produced an error: ' + result

        String typeName = sources[0].name.substring(0, sources[0].name.indexOf('.'))
        String qualName = newPackageName.length() > 0 ? newPackageName + '.' + typeName : typeName
        ICompilationUnit newUnit = packageFragmentRoot.javaProject.findType(qualName).compilationUnit
        ICompilationUnit oldUnit = units[0]

        units[0] = newUnit
        assertContents(units, sources*.finalContents)

        // undo
        assert  undoManager.anythingToUndo()
        assert !undoManager.anythingToRedo()
        undoManager.performUndo(null, null)

        units[0] = oldUnit
        assertContents(units, sources*.contents)

        // redo
        assert !undoManager.anythingToUndo()
        assert  undoManager.anythingToRedo()
        undoManager.performRedo(null, null)

        units[0] = newUnit
        assertContents(units, sources*.finalContents)
    }

    @Before
    void setUp() {
        setJavaPreference(ORGIMPORTS_IMPORTORDER, '\\#;;')
        setJavaPreference(FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, '0')
    }

    //--------------------------------------------------------------------------

    @Test
    void testSimpleMove1() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Java.java',
            contents: 'package p;\npublic class Java {}',
            finalContents: 'package NEW;\npublic class Java {}'
        ))
    }

    @Test
    void testSimpleMove2() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\npublic class Groovy {}',
            finalContents: 'package NEW;\npublic class Groovy {}'
        ))
    }

    @Test
    void testSimpleMove3() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Java.java',
            contents: 'package p;\npublic class Java {}',
            finalContents: 'package NEW;\npublic class Java {}'
        ), new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\npublic class Groovy extends Java {}',
            finalContents: 'package p;\n\nimport NEW.Java\n\npublic class Groovy extends Java {}'
        ))
    }

    @Test
    void testSimpleMove4() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p1', name: 'Groovy.groovy',
            contents: 'package p1\nimport p2.Groovy2\npublic class Groovy {\n Groovy2 g\n}',
            finalContents: 'package NEW\nimport p2.Groovy2\npublic class Groovy {\n Groovy2 g\n}'
        ), new TestSource(
            pack: 'p2', name: 'Groovy2.groovy',
            contents: 'package p2\nimport p1.Groovy\npublic class Groovy2 extends Groovy {}',
            finalContents: 'package p2\nimport NEW.Groovy\npublic class Groovy2 extends Groovy {}'
        ))
    }

    @Test
    void testQualifiedMove1() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Java.java',
            contents: 'package p;\npublic class Java {}',
            finalContents: 'package NEW;\npublic class Java {}'
        ), new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\npublic class Groovy extends p.Java {}',
            finalContents: 'package p;\npublic class Groovy extends NEW.Java {}'
        ))
    }

    @Test
    void testQualifiedMove2() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p1', name: 'Groovy.groovy',
            contents: 'package p1\npublic class Groovy {\n p2.Groovy2 g\n}',
            finalContents: 'package NEW\npublic class Groovy {\n p2.Groovy2 g\n}'
        ), new TestSource(
            pack: 'p2', name: 'Groovy2.groovy',
            contents: 'package p2\npublic class Groovy2 extends p1.Groovy {}',
            finalContents: 'package p2\npublic class Groovy2 extends NEW.Groovy {}'
        ))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/310
    void testQualifiedMove3() {
        // moves j1.Java to j2.Java; links in g1.Groovy should be updated
        performRefactoringAndUndo('j2', new TestSource(
            pack: 'j1', name: 'Java.java',
            contents: '''\
                |package j1
                |public class Java {
                |  public static final int N = 1;
                |}
                |'''.stripMargin(),
            finalContents: '''\
                |package j2
                |public class Java {
                |  public static final int N = 1;
                |}
                |'''.stripMargin()
        ), new TestSource(
            pack: 'g1', name: 'Groovy.groovy',
            contents: '''\
                |package g1
                |
                |import static j1.Java.*
                |import j1.Java
                |
                |class Groovy {
                |  def m1() {
                |    Java j = new j1.Java()
                |  }
                |  def m2() {
                |    def n = N
                |  }
                |}
                |'''.stripMargin(),
            finalContents: '''\
                |package g1
                |
                |import static j2.Java.*
                |import j2.Java
                |
                |class Groovy {
                |  def m1() {
                |    Java j = new j2.Java()
                |  }
                |  def m2() {
                |    def n = N
                |  }
                |}
                |'''.stripMargin(),
        ))
    }

    @Test
    void testNonPrimaryMove() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p1', name: 'GroovyFoo.groovy',
            contents: 'package p1\nimport p2.Groovy2\npublic class Groovy {\n Groovy2 g\n}',
            finalContents: 'package NEW\nimport p2.Groovy2\npublic class Groovy {\n Groovy2 g\n}'
        ), new TestSource(
            pack: 'p2', name: 'GroovyFoo2.groovy',
            contents: 'package p2\nimport p1.Groovy\npublic class Groovy2 extends Groovy {}',
            finalContents: 'package p2\nimport NEW.Groovy\npublic class Groovy2 extends Groovy {}'
        ))
    }

    @Test
    void testNonPrimaryQualifiedMove() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p1', name: 'GroovyFoo.groovy',
            contents: 'package p1\npublic class Groovy {\n p2.Groovy2 g\n}',
            finalContents: 'package NEW\npublic class Groovy {\n p2.Groovy2 g\n}'
        ), new TestSource(
            pack: 'p2', name: 'GroovyFoo2.groovy',
            contents: 'package p2\npublic class Groovy2 extends p1.Groovy {}',
            finalContents: 'package p2\npublic class Groovy2 extends NEW.Groovy {}'
        ))
    }

    @Test
    void testMoveBack() {
        performRefactoringAndUndo('p2', new TestSource(
            pack: 'p1', name: 'Groovy.groovy',
            contents: 'package p1\nimport p2.Groovy2\n\npublic class Groovy {\n Groovy2 g\n}',
            finalContents: 'package p2\n\npublic class Groovy {\n Groovy2 g\n}'
        ), new TestSource(
            pack: 'p2', name: 'Groovy2.groovy',
            contents: 'package p2\nimport p1.Groovy\n\npublic class Groovy2 extends Groovy {}',
            finalContents: 'package p2\n\npublic class Groovy2 extends Groovy {}'
        ))
    }

    @Test @NotYetImplemented // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205
    void testInnerMove1() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\npublic class Groovy {\n class Inner {}\n}',
            finalContents: 'package NEW;\npublic class Groovy {\n class Inner {}\n}'
        ), new TestSource(
            pack: 'p', name: 'Groovy2.groovy',
            contents: 'package p;\npublic class Groovy2 extends Groovy.Inner {}',
            finalContents: 'package p;\nimport NEW.Groovy;\npublic class Groovy2 extends Groovy.Inner {}'
        ))
    }

    @Test @NotYetImplemented // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205
    void testInnerMove2() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy2.groovy',
            contents: 'package p;\npublic class Groovy2 extends Groovy.Inner {}',
            finalContents: 'package NEW;\nimport p.Groovy;\npublic class Groovy2 extends Groovy.Inner {}'
        ), new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\npublic class Groovy {\n class Inner {}\n}',
            finalContents: 'package p;\npublic class Groovy {\n class Inner {}\n}'
        ))
    }

    @Test @NotYetImplemented // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205
    void testInnerMove3() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\npublic class Groovy {\n class Inner {}\n}',
            finalContents: 'package NEW;\npublic class Groovy {\n class Inner {}\n}'
        ), new TestSource(
            pack: 'p', name: 'Groovy2.groovy',
            contents: 'package p;\nimport Groovy.Inner\npublic class Groovy2 extends Inner {}',
            finalContents: 'package p;\nimport NEW.Groovy.Inner;\npublic class Groovy2 extends Inner {}'
        ))
    }

    @Test @NotYetImplemented // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205
    void testInnerMove4() {
        performRefactoringAndUndo('NEW', new TestSource(
            pack: 'p', name: 'Groovy2.groovy',
            contents: 'package p;\nimport Groovy.Inner\npublic class Groovy2 extends Inner {}',
            finalContents: 'package NEW;\nimport p.Groovy.Inner;\npublic class Groovy2 extends Inner {}'
        ), new TestSource(
            pack: 'p', name: 'Groovy.groovy',
            contents: 'package p;\npublic class Groovy {\n class Inner {}\n}',
            finalContents: 'package p;\npublic class Groovy {\n class Inner {}\n}'
        ))
    }
}
