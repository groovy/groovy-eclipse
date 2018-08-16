/*
 * Copyright 2009-2018 the original author or authors.
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

import groovy.transform.NotYetImplemented

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

    protected boolean renameGetters, renameSetters, updateReferences = true

    // assume we are renaming the first memebr of the first type to the new name
    private void performRefactoringAndUndo(String newName, TestSource... sources) {
        def units = createUnits(sources)
        def toRename = units[0].types[0].children[0]
        String id = toRename instanceof IField ? IJavaRefactorings.RENAME_FIELD : IJavaRefactorings.RENAME_METHOD
        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(id)

        descriptor.newName = newName
        descriptor.javaElement = toRename
        descriptor.renameGetters = renameGetters
        descriptor.renameSetters = renameSetters
        descriptor.updateReferences = updateReferences

        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor)
        RefactoringStatus result = performRefactoring(refactoring, true)
        assertContents(units, sources*.finalContents)

        // undo
        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '!anythingToRedo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())
        assertContents(units, sources*.contents)

        // redo
        assert !RefactoringCore.getUndoManager().anythingToUndo() : '!anythingToUndo'
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
                  void setFoo() { }
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
                  void setFoo() { }
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
                  void setFoo() { }
                  def isFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def flar
                  def getFoo() { }
                  void setFoo() { }
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
                f.foo
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
                  void m(p.First f) {
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
                  void m(p.First f) {
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
                  void setFoo(arg) { }
                  def isFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def flar
                  def getFoo() { }
                  void setFoo(arg) { }
                  def isFoo() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void m(p.First f) {
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
                  void m(p.First f) {
                    f.flar = null;
                    f.getFoo();
                    f.setFoo(null);
                    f.isFoo();
                  }
                }
                '''.stripIndent()
        ))
    }

    @Test // this has compile errors, but it should still work
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
                  void m(p.First f) {
                    Object o = f.foo; // error
                    f.getFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void m(p.First f) {
                    Object o = f.flar; // error
                    f.getFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                def f = new p.First()
                f.foo
                f.getFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                def f = new p.First()
                f.flar
                f.getFlar()
                '''.stripIndent()
        ))
    }

    @Test // this has compile errors, but it should still work
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
                  void m(p.First f) {
                    Object o = f.foo; // error
                    f.isFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void m(p.First f) {
                    Object o = f.flar; // error
                    f.isFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                def f = new p.First()
                f.foo
                f.isFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                def f = new p.First()
                f.flar
                f.isFlar()
                '''.stripIndent()
        ))
    }

    @Test // this has compile errors, but it should still work
    void testSetterOnly1() {
        performRefactoringAndUndo('setFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  void setFoo(value) { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  void setFlar(value) { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void m(p.First f) {
                    f.foo = null; // error
                    f.setFoo(null);
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void m(p.First f) {
                    f.flar = null; // error
                    f.setFlar(null);
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                def f = new p.First()
                f.foo = null
                f.setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package r
                def f = new p.First()
                f.flar = null
                f.setFlar(null)
                '''.stripIndent()
        ))
    }

    @Test
    void testSetterOnly2() {
        performRefactoringAndUndo('setFooBar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  void setFoo(value) { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  void setFooBar(value) { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def m(p.First f) {
                  f.foo // potential match
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                def m(p.First f) {
                  f.fooBar // potential match
                }
                '''.stripIndent()
        ))
    }

    @Test // this has compile errors, but it should still work
    void testStaticGetterOnly1() {
        performRefactoringAndUndo('getFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static def getFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static def getFlar() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void m() {
                    Object o = p.First.foo; // error
                    o = p.First.getFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void m() {
                    Object o = p.First.flar; // error
                    o = p.First.getFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                p.First.foo
                p.First.getFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                p.First.flar
                p.First.getFlar()
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented // this has compile errors, but it should still work
    void testStaticGetterOnly2() {
        performRefactoringAndUndo('getFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static def getFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static def getFlar() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                import static p.First.getFoo;
                class Java {
                  void m() {
                    Object o = foo; // error
                    o = getFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                import static p.First.getFlar;
                class Java {
                  void m() {
                    Object o = foo; // error
                    o = getFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                import static p.First.getFoo
                foo
                getFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.getFlar
                flar
                getFlar()
                '''.stripIndent()
        ))
    }

    @Test // this has compile errors, but it should still work
    void testStaticIsserOnly1() {
        performRefactoringAndUndo('isFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static boolean isFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static boolean isFlar() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void m() {
                    boolean b = p.First.foo; // error
                    b = p.First.isFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void m() {
                    boolean b = p.First.flar; // error
                    b = p.First.isFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                p.First.foo
                p.First.isFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                p.First.flar
                p.First.isFlar()
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented // this has compile errors, but it should still work
    void testStaticIsserOnly2() {
        performRefactoringAndUndo('isFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static boolean isFoo() { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static boolean isFlar() { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                import static p.First.isFoo;
                class Java {
                  void m() {
                    boolean b = foo; // error
                    b = isFoo();
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                import static p.First.isFlar;
                class Java {
                  void m() {
                    boolean b = foo; // error
                    b = isFlar();
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                import static p.First.isFoo
                foo
                isFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.isFlar
                flar
                isFlar()
                '''.stripIndent()
        ))
    }

    @Test // this has compile errors, but it should still work
    void testStaticSetterOnly1() {
        performRefactoringAndUndo('setFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static void setFoo(value) { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static void setFlar(value) { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void m() {
                    p.First.foo = null; // error
                    p.First.setFoo(null);
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void m() {
                    p.First.flar = null; // error
                    p.First.setFlar(null);
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                p.First.foo = null
                p.First.setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package r
                p.First.flar = null
                p.First.setFlar(null)
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented // this has compile errors, but it should still work
    void testStaticSetterOnly2() {
        performRefactoringAndUndo('setFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static void setFoo(value) { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static void setFlar(value) { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                import static p.First.setFoo;
                class Java {
                  void m() {
                    foo = null; // error
                    setFoo(null);
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                import static p.First.setFlar;
                class Java {
                  void m() {
                    foo = null; // error
                    setFlar(null);
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                import static p.First.setFoo
                foo = null
                setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.setFlar
                flar = null
                setFlar(null)
                '''.stripIndent()
        ))
    }

    @Test
    void testStaticSetterOnly3() {
        performRefactoringAndUndo('setFooBar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static void setFoo(value) { }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static void setFooBar(value) { }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def m() {
                  p.First.foo // potential match
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                def m() {
                  p.First.fooBar // potential match
                }
                '''.stripIndent()
        ))
    }

    @Test
    void testFieldAndGetterAndSetter1() {
        performRefactoringAndUndo('fooBar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  private int foo = 0
                  int getFoo() { return this.foo }
                  void setFoo(int value) { this.foo = value }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  private int fooBar = 0
                  int getFoo() { return this.fooBar }
                  void setFoo(int value) { this.fooBar = value }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def m(p.First f) {
                  int i = f.@foo
                  i = f.foo
                  f.foo = i
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                def m(p.First f) {
                  int i = f.@fooBar
                  i = f.foo
                  f.foo = i
                }
                '''.stripIndent()
        ))
    }

    @Test // field and accessor types don't match exactly
    void testFieldAndGetterAndSetter2() {
        performRefactoringAndUndo('fooBar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                import java.util.concurrent.atomic.AtomicInteger
                class First {
                  private final AtomicInteger foo = new AtomicInteger()
                  int getFoo() { this.foo.get() }
                  void setFoo(int value) { this.foo.set(value) }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                import java.util.concurrent.atomic.AtomicInteger
                class First {
                  private final AtomicInteger fooBar = new AtomicInteger()
                  int getFoo() { this.fooBar.get() }
                  void setFoo(int value) { this.fooBar.set(value) }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def m(p.First f) {
                  int i = f.@foo.get()
                  i = f.foo
                  f.foo = i
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                def m(p.First f) {
                  int i = f.@fooBar.get()
                  i = f.foo
                  f.foo = i
                }
                '''.stripIndent()
        ))
    }

    // TODO: Repeat testFieldAndGetterAndSetterN with rename accessors enabled.
}
