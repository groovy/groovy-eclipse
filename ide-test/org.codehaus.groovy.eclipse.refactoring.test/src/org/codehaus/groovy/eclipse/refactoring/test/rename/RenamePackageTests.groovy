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
            pack: 'p', name: 'C.groovy',
            contents: 'package p; class C {}',
            finalContents: 'package q; class C {}'
        ))
    }

    @Test // qualified reference
    void testRenamePackage2() {
        renamePackage(new TestSource(
            pack: 'p', name: 'A.java',
            contents: 'package p; interface A { String B = ""; }',
            finalContents: 'package q; interface A { String B = ""; }'
        ), new TestSource(
            pack: 'x', name: 'C.groovy',
            contents: 'package x; class C { def foo = p.A.B }',
            finalContents: 'package x; class C { def foo = q.A.B }'
        ))
    }

    @Test // more qualified references
    void testRenamePackage3() {
        renamePackage(new TestSource(
            pack: 'p', name: 'A.java',
            contents: 'package p; class A { }',
            finalContents: 'package q; class A { }'
        ), new TestSource(
            pack: 'p', name: 'C.groovy',
            contents: '''\
                package p
                class C {
                  p.A foo
                  private p.A bar
                  p.A baz() { new p.A() }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                class C {
                  q.A foo
                  private q.A bar
                  q.A baz() { new q.A() }
                }
                '''.stripIndent()
        ))
    }

    @Test // import reference
    void testRenamePackage4() {
        renamePackage(new TestSource(
            pack: 'p', name: 'A.java',
            contents: 'package p; interface A { String B = ""; }',
            finalContents: 'package q; interface A { String B = ""; }'
        ), new TestSource(
            pack: 'x', name: 'C.groovy',
            contents: '''\
                package x
                import p.A
                class C {
                  def foo = A.B
                }
                '''.stripIndent(),
            finalContents: '''\
                package x
                import q.A
                class C {
                  def foo = A.B
                }
                '''.stripIndent()
        ))
    }

    @Test // static import reference
    void testRenamePackage5() {
        renamePackage(new TestSource(
            pack: 'p', name: 'A.java',
            contents: 'package p; interface A { String B = ""; }',
            finalContents: 'package q; interface A { String B = ""; }'
        ), new TestSource(
            pack: 'x', name: 'C.groovy',
            contents: '''\
                package x
                import static p.A.*
                class C {
                  def foo = B
                }
                '''.stripIndent(),
            finalContents: '''\
                package x
                import static q.A.*
                class C {
                  def foo = B
                }
                '''.stripIndent()
        ))
    }
}
