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
package org.codehaus.groovy.eclipse.refactoring.test.rename

import org.codehaus.groovy.eclipse.refactoring.test.rename.RenameRefactoringTestSuite.TestSource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.refactoring.IJavaRefactorings
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory
import org.eclipse.ltk.core.refactoring.Refactoring
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.Test

final class RenamePackageTests extends RenameRefactoringTestSuite {

    /**
     * Renames "package p" to "package q" for supplied sources.
     */
    private RefactoringStatus renamePackage(TestSource... sources) {
        ICompilationUnit[] units = createUnits(sources)

        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory.
            createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_PACKAGE)
        descriptor.updateTextualOccurrences = true
        descriptor.updateReferences = true
        descriptor.javaElement = packageFragmentRoot.getPackageFragment('p')
        descriptor.newName = 'q'

        Refactoring refactoring = createRefactoring(descriptor)
        RefactoringStatus result = performRefactoring(refactoring, true)
        result = ignoreKnownErrors(result)
        assert result.isOK()

        for (i in 0..<sources.length) {
            if (sources[i].pack == 'p') {
                units[i] = packageFragmentRoot.getPackageFragment('q').getCompilationUnit(sources[i].name)
            }
        }
        assertContents(units, sources*.finalContents)

        // undo
        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '!anythingToRedo'
        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())

        for (i in 0..<sources.length) {
            if (sources[i].pack == 'p') {
                units[i] = packageFragmentRoot.getPackageFragment('p').getCompilationUnit(sources[i].name)
            }
        }
        assertContents(units, sources*.contents)

        // redo
        assert !RefactoringCore.getUndoManager().anythingToUndo() : '!anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'
        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())

        for (i in 0..<sources.length) {
            if (sources[i].pack == 'p') {
                units[i] = packageFragmentRoot.getPackageFragment('q').getCompilationUnit(sources[i].name)
            }
        }
        assertContents(units, sources*.finalContents)
    }

    @Test
    void testRenamePackage1() {
        renamePackage(new TestSource(
            pack: 'p', name: 'A.groovy',
            contents: 'package p; class A {}',
            finalContents: 'package q; class A {}'
        ))
    }

    @Test // qualified reference
    void testRenamePackage2() {
        renamePackage(new TestSource(
            pack: 'p', name: 'B.java',
            contents: 'package p; interface B { String CONST = ""; }',
            finalContents: 'package q; interface B { String CONST = ""; }'
        ), new TestSource(
            pack: 'x', name: 'C.groovy',
            contents: 'package x; class C { def foo = p.B.CONST }',
            finalContents: 'package x; class C { def foo = q.B.CONST }'
        ))
    }

    @Test // more qualified references
    void testRenamePackage3() {
        renamePackage(new TestSource(
            pack: 'p', name: 'D.java',
            contents: 'package p; class D { }',
            finalContents: 'package q; class D { }'
        ), new TestSource(
            pack: 'p', name: 'E.groovy',
            contents: '''\
                package p
                class E {
                  p.D foo
                  private p.D bar
                  p.D baz() { new p.D() }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                class E {
                  q.D foo
                  private q.D bar
                  q.D baz() { new q.D() }
                }
                '''.stripIndent()
        ))
    }

    @Test // import reference
    void testRenamePackage4() {
        renamePackage(new TestSource(
            pack: 'p', name: 'F.java',
            contents: 'package p; interface F { String CONST = ""; }',
            finalContents: 'package q; interface F { String CONST = ""; }'
        ), new TestSource(
            pack: 'x', name: 'G.groovy',
            contents: '''\
                package x
                import p.F
                class C {
                  def foo = F.CONST
                }
                '''.stripIndent(),
            finalContents: '''\
                package x
                import q.F
                class C {
                  def foo = F.CONST
                }
                '''.stripIndent()
        ))
    }

    @Test // static import reference
    void testRenamePackage5() {
        renamePackage(new TestSource(
            pack: 'p', name: 'H.java',
            contents: 'package p; interface H { String CONST = ""; }',
            finalContents: 'package q; interface H { String CONST = ""; }'
        ), new TestSource(
            pack: 'x', name: 'I.groovy',
            contents: '''\
                package x
                import static p.H.*
                class C {
                  def foo = CONST
                }
                '''.stripIndent(),
            finalContents: '''\
                package x
                import static q.H.*
                class C {
                  def foo = CONST
                }
                '''.stripIndent()
        ))
    }
}
