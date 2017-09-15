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
import org.eclipse.jdt.core.IField
import org.eclipse.jdt.core.refactoring.IJavaRefactorings
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring
import org.junit.Test

/**
 * Testing the {@link SyntheticAccessorsRenameParticipant}.
 */
final class SyntheticAccessorRenamingTests extends RenameRefactoringTestSuite {

    // assume we are renaming the first memebr of the first type to the new name
    private void performRefactoringAndUndo(String newName, TestSource... sources) {
        def units = createUnits(sources)
        def toRename = units[0].types[0].children[0]
        String id = toRename instanceof IField ? IJavaRefactorings.RENAME_FIELD : IJavaRefactorings.RENAME_METHOD

        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(id)
        descriptor.setJavaElement(toRename)
        descriptor.setNewName(newName)
        descriptor.setRenameGetters(false)
        descriptor.setRenameSetters(false)
        descriptor.setUpdateReferences(true)

        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor)
        RefactoringStatus result = performRefactoring(refactoring, true)
        assertContents(units, sources*.finalContents)

        // undo
        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '! anythingToRedo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())
        assertContents(units, sources*.contents)

        // redo
        assert !RefactoringCore.getUndoManager().anythingToUndo() : '! anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'
        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())
        assertContents(units, sources*.finalContents)
    }

    @Test
    void testSingleFileRename1() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  boolean foo
                  def run() {
                    foo
                    getFoo()
                    setFoo()
                    isFoo()
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  boolean flar
                  def run() {
                    flar
                    getFlar()
                    setFlar()
                    isFlar()
                  }
                }
                '''.stripIndent(),
        ))
    }

    @Test // don't automatically rename if the accessors are explicitly named
    void testSingleFileRename2() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  def foo
                  def getFoo() { }
                  def setFoo() { }
                  def isFoo() { }
                  def run() {
                    foo
                    getFoo()
                    setFoo()
                    isFoo()
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def flar
                  def getFoo() { }
                  def setFoo() { }
                  def isFoo() { }
                  def run() {
                    flar
                    getFoo()
                    setFoo()
                    isFoo()
                  }
                }
                '''.stripIndent()
        ))
    }

    @Test
    void testMultiFileRename1() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  boolean foo
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  boolean flar
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def f = new p.First()
                f.foo
                f.getFoo()
                f.setFoo()
                f.isFoo()
                '''.stripIndent(),
            finalContents: '''\
                package q
                def f = new p.First()
                f.flar
                f.getFlar()
                f.setFlar()
                f.isFlar()
                '''.stripIndent()
        ))
    }

    @Test // don't automatically rename if the accessors are explicitly named
    void testMultiFileRename2() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  def foo
                  def getFoo() { }
                  def setFoo() { }
                  def isFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def flar
                  def getFoo() { }
                  def setFoo() { }
                  def isFoo() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def f = new p.First()
                f.foo
                f.getFoo()
                f.setFoo()
                f.isFoo()
                '''.stripIndent(),
            finalContents: '''\
                package q
                def f = new p.First()
                f.flar
                f.getFoo()
                f.setFoo()
                f.isFoo()
                '''.stripIndent()
        ))
    }

    @Test // this will have compile errors, but it should still work
    void testJavaRename1() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  boolean foo
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  boolean flar
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    f.foo = null;
                    f.getFoo();
                    f.setFoo(null);
                    f.isFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    f.flar = null;
                    f.getFlar();
                    f.setFlar(null);
                    f.isFlar();
                  }
                }
                '''.stripIndent()
        ))
    }

    @Test // this will have compile errors, but it should still work; don't automatically rename if the accessors are explicitly named
    void testJavaRename2() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  def foo
                  def getFoo() { }
                  def setFoo(arg) { }
                  def isFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def flar
                  def getFoo() { }
                  def setFoo(arg) { }
                  def isFoo() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    f.foo = null;
                    f.getFoo();
                    f.setFoo(null);
                    f.isFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    f.flar = null;
                    f.getFoo();
                    f.setFoo(null);
                    f.isFoo();
                  }
                }
                '''.stripIndent()
        ))
    }

    @Test // this will have compile errors, but it should still work
    void testGetterOnly() {
        performRefactoringAndUndo('getFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  def getFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def getFlar() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    Object o = f.foo;
                    f.getFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    Object o = f.flar;
                    f.getFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                p.First f = new p.First()
                f.foo
                f.getFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                p.First f = new p.First()
                f.flar
                f.getFlar()
                '''.stripIndent()
        ))
    }

    @Test // this will have compile errors, but it should still work
    void testIsserOnly() {
        performRefactoringAndUndo('isFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  def isFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def isFlar() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    Object o = f.foo;
                    f.isFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    Object o = f.flar;
                    f.isFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                p.First f = new p.First()
                f.foo
                f.isFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                p.First f = new p.First()
                f.flar
                f.isFlar()
                '''.stripIndent()
        ))
    }

    @Test // this will have compile errors, but it should still work
    void testSetterOnly() {
        performRefactoringAndUndo('setFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  def setFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def setFlar() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    Object o = f.foo;
                    f.setFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void l() {
                    p.First f = new p.First();
                    Object o = f.flar;
                    f.setFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                p.First f = new p.First()
                f.foo
                f.setFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                p.First f = new p.First()
                f.flar
                f.setFlar()
                '''.stripIndent()
        ))
    }
}
