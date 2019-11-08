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
        String kind = (toRename instanceof IField ? IJavaRefactorings.RENAME_FIELD : IJavaRefactorings.RENAME_METHOD)
        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(kind)

        descriptor.newName = newName
        descriptor.javaElement = toRename
        descriptor.renameGetters = renameGetters
        descriptor.renameSetters = renameSetters
        descriptor.updateReferences = updateReferences

        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor)
        RefactoringStatus result = performRefactoring(refactoring, true)
        assertContents(units, sources*.finalContents)

        RefactoringCore.undoManager.with {
            // undo
            performUndo(null, new NullProgressMonitor())
            assertContents(units, sources*.contents)

            // redo
            assert anythingToRedo() : 'anythingToRedo'
            assert !anythingToUndo() : '!anythingToUndo'
            performRedo(null, new NullProgressMonitor())
            assertContents(units, sources*.finalContents)
        }
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
                    isFoo()
                    getFoo()
                    setFoo(true)
                    this.&isFoo
                    this.&getFoo
                    this.&setFoo
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  boolean flar
                  def run() {
                    flar
                    isFlar()
                    getFlar()
                    setFlar(true)
                    this.&isFlar
                    this.&getFlar
                    this.&setFlar
                  }
                }
                '''.stripIndent(),
        ))
    }

    @Test
    void testSingleFileRename2() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static boolean foo
                  static {
                    foo
                    isFoo()
                    getFoo()
                    setFoo(true)
                    this.&isFoo
                    this.&getFoo
                    this.&setFoo
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static boolean flar
                  static {
                    flar
                    isFlar()
                    getFlar()
                    setFlar(true)
                    this.&isFlar
                    this.&getFlar
                    this.&setFlar
                  }
                }
                '''.stripIndent(),
        ))
    }

    @Test // don't automatically rename if the accessors are explicitly named
    void testSingleFileRename3() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  def foo
                  def getFoo() {}
                  boolean isFoo() {}
                  void setFoo(val) {}
                  def run() {
                    foo
                    isFoo()
                    getFoo()
                    setFoo(null)
                    this.&isFoo
                    this.&getFoo
                    this.&setFoo
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def flar
                  def getFoo() {}
                  boolean isFoo() {}
                  void setFoo(val) {}
                  def run() {
                    flar
                    isFoo()
                    getFoo()
                    setFoo(null)
                    this.&isFoo
                    this.&getFoo
                    this.&setFoo
                  }
                }
                '''.stripIndent()
        ))
    }

    @Test // don't automatically rename if the accessors are explicitly named
    void testSingleFileRename4() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static def foo
                  static def getFoo() {}
                  static boolean isFoo() {}
                  static void setFoo(val) {}
                  static {
                    foo
                    isFoo()
                    getFoo()
                    setFoo(null)
                    this.&isFoo
                    this.&getFoo
                    this.&setFoo
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static def flar
                  static def getFoo() {}
                  static boolean isFoo() {}
                  static void setFoo(val) {}
                  static {
                    flar
                    isFoo()
                    getFoo()
                    setFoo(null)
                    this.&isFoo
                    this.&getFoo
                    this.&setFoo
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
                f.isFoo()
                f.getFoo()
                f.setFoo(true)
                '''.stripIndent(),
            finalContents: '''\
                package q
                def f = new p.First()
                f.flar
                f.isFlar()
                f.getFlar()
                f.setFlar(true)
                '''.stripIndent()
        ))
    }

    @Test
    void testMultiFileRename2() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static boolean foo
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static boolean flar
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                p.First.foo
                p.First.isFoo()
                p.First.getFoo()
                p.First.setFoo(true)
                '''.stripIndent(),
            finalContents: '''\
                package q
                p.First.flar
                p.First.isFlar()
                p.First.getFlar()
                p.First.setFlar(true)
                '''.stripIndent()
        ))
    }

    @Test // don't automatically rename if the accessors are explicitly named
    void testMultiFileRename3() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  def foo
                  def getFoo() {}
                  boolean isFoo() {}
                  void setFoo(val) {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def flar
                  def getFoo() {}
                  boolean isFoo() {}
                  void setFoo(val) {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def f = new p.First()
                f.foo
                f.isFoo()
                f.getFoo()
                f.setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package q
                def f = new p.First()
                f.foo
                f.isFoo()
                f.getFoo()
                f.setFoo(null)
                '''.stripIndent()
        ))
    }

    @Test // don't automatically rename if the accessors are explicitly named
    void testMultiFileRename4() {
        performRefactoringAndUndo('flar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static def foo
                  static def getFoo() {}
                  static boolean isFoo() {}
                  static void setFoo(val) {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static def flar
                  static def getFoo() {}
                  static boolean isFoo() {}
                  static void setFoo(val) {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                p.First.foo
                p.First.isFoo()
                p.First.getFoo()
                p.First.setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package q
                p.First.foo
                p.First.isFoo()
                p.First.getFoo()
                p.First.setFoo(null)
                '''.stripIndent()
        ))
    }

    @Test
    void testMultiFileRename5() {
        performRefactoringAndUndo('getBar', new TestSource(
            pack: 'p', name: 'Pojo.java',
            contents: '''\
                package p;
                public class Pojo {
                  public int getFoo() { return this.foo; } // target
                  public void setFoo(int foo) { this.foo = foo; }
                  private int foo = -1;
                }
                '''.stripIndent(),
            finalContents: '''\
                package p;
                public class Pojo {
                  public int getBar() { return this.foo; } // target
                  public void setFoo(int foo) { this.foo = foo; }
                  private int foo = -1;
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                import p.Pojo
                new Pojo().with {
                    foo = 1
                    foo += 1
                    getFoo()
                    setFoo(3)
                    def val = foo + 456
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                import p.Pojo
                new Pojo().with {
                    foo = 1
                    foo += 1
                    getBar()
                    setFoo(3)
                    def val = bar + 456
                }
                '''.stripIndent()
        ))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/784
    void testMultiFileRename6() {
        performRefactoringAndUndo('setBar', new TestSource(
            pack: 'p', name: 'Pojo.java',
            contents: '''\
                package p;
                public class Pojo {
                  public void setFoo(int foo) { this.foo = foo; }
                  public int getFoo() { return this.foo; }
                  private int foo = -1;
                }
                '''.stripIndent(),
            finalContents: '''\
                package p;
                public class Pojo {
                  public void setBar(int foo) { this.foo = foo; }
                  public int getFoo() { return this.foo; }
                  private int foo = -1;
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                import p.Pojo
                new Pojo().with {
                    foo = 1
                    foo += 1
                    getFoo()
                    setFoo(3)
                    def val = foo + 456
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                import p.Pojo
                new Pojo().with {
                    bar = 1
                    bar += 1
                    getFoo()
                    setBar(3)
                    def val = foo + 456
                }
                '''.stripIndent()
        ))
    }

    @Test
    void testMultiFileRename7() {
        renameGetters = renameSetters = true

        performRefactoringAndUndo('bar', new TestSource(
            pack: 'p', name: 'Pojo.java',
            contents: '''\
                package p;
                public class Pojo {
                  private int foo = -1;
                  public int getFoo() { return this.foo; }
                  public void setFoo(int val) { this.foo = val; }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p;
                public class Pojo {
                  private int bar = -1;
                  public int getBar() { return this.bar; }
                  public void setBar(int val) { this.bar = val; }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                import p.Pojo
                new Pojo().with {
                    foo = 1
                    foo += 1
                    getFoo()
                    setFoo(3)
                    def val = foo + 456
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                import p.Pojo
                new Pojo().with {
                    bar = 1
                    bar += 1
                    getBar()
                    setBar(3)
                    def val = bar + 456
                }
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
                    f.isFoo();
                    f.getFoo();
                    f.setFoo(true);
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void m(p.First f) {
                    f.flar = null;
                    f.isFlar();
                    f.getFlar();
                    f.setFlar(true);
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
                  def getFoo() {}
                  boolean isFoo() {}
                  void setFoo(val) {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def flar
                  def getFoo() {}
                  boolean isFoo() {}
                  void setFoo(val) {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Java.java',
            contents: '''\
                package q;
                class Java {
                  void m(p.First f) {
                    f.foo = null;
                    f.isFoo();
                    f.getFoo();
                    f.setFoo(null);
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package q;
                class Java {
                  void m(p.First f) {
                    f.flar = null;
                    f.isFoo();
                    f.getFoo();
                    f.setFoo(null);
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
                  def getFoo() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def getFlar() {}
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
                  def isFoo() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  def isFlar() {}
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
                  void setFoo(value) {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  void setFlar(value) {}
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
                  void setFoo(value) {}
                  void doSomething() {
                    foo // ambiguous reference
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  void setFooBar(value) {}
                  void doSomething() {
                    fooBar // ambiguous reference
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def m(p.First f) {
                  f.foo // ambiguous reference
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                def m(p.First f) {
                  f.fooBar // ambiguous reference
                }
                '''.stripIndent()
        ))
    }

    @Test
    void testSetterOnly2a() {
        performRefactoringAndUndo('setFooBar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                @groovy.transform.CompileStatic
                class First {
                  void setFoo(value) {}
                  void doSomething() {
                    foo // ambiguous reference
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                @groovy.transform.CompileStatic
                class First {
                  void setFooBar(value) {}
                  void doSomething() {
                    fooBar // ambiguous reference
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                @groovy.transform.CompileStatic
                def m(p.First f) {
                  f.foo // ambiguous reference
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                @groovy.transform.CompileStatic
                def m(p.First f) {
                  f.fooBar // ambiguous reference
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
                  static def getFoo() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static def getFlar() {}
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

    @Test // this has compile errors, but it should still work
    void testStaticGetterOnly2() {
        performRefactoringAndUndo('getFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static def getFoo() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static def getFlar() {}
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
                getFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.getFlar
                getFlar()
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented
    void testStaticGetterOnly3() {
        performRefactoringAndUndo('getFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static def getFoo() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static def getFlar() {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                import static p.First.getFoo
                foo
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.getFlar
                flar
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
                  static boolean isFoo() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static boolean isFlar() {}
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

    @Test // this has compile errors, but it should still work
    void testStaticIsserOnly2() {
        performRefactoringAndUndo('isFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static boolean isFoo() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static boolean isFlar() {}
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
                isFoo()
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.isFlar
                isFlar()
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented
    void testStaticIsserOnly3() {
        performRefactoringAndUndo('isFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static boolean isFoo() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static boolean isFlar() {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                import static p.First.isFoo
                foo
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.isFlar
                flar
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
                  static void setFoo(value) {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static void setFlar(value) {}
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

    @Test // this has compile errors, but it should still work
    void testStaticSetterOnly2() {
        performRefactoringAndUndo('setFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static void setFoo(value) {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static void setFlar(value) {}
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
                setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.setFlar
                setFlar(null)
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented
    void testStaticSetterOnly3() {
        performRefactoringAndUndo('setFlar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static void setFoo(value) {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static void setFlar(value) {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                import static p.First.setFoo
                foo = null
                '''.stripIndent(),
            finalContents: '''\
                package r
                import static p.First.setFlar
                flar = null
                '''.stripIndent()
        ))
    }

    @Test
    void testStaticSetterOnly4() {
        performRefactoringAndUndo('setFooBar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                @groovy.transform.CompileStatic
                class First {
                  static void setFoo(value) {}
                  static void doSomething() {
                    foo // ambiguous reference
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                @groovy.transform.CompileStatic
                class First {
                  static void setFooBar(value) {}
                  static void doSomething() {
                    fooBar // ambiguous reference
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                @groovy.transform.CompileStatic
                def m() {
                  p.First.foo // ambiguous reference
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                @groovy.transform.CompileStatic
                def m() {
                  p.First.fooBar // ambiguous reference
                }
                '''.stripIndent()
        ))
    }

    @Test
    void testStaticSetterOnly4a() {
        performRefactoringAndUndo('setFooBar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                class First {
                  static void setFoo(value) {}
                  static void doSomething() {
                    foo // ambiguous reference
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class First {
                  static void setFooBar(value) {}
                  static void doSomething() {
                    fooBar // ambiguous reference
                  }
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def m() {
                  p.First.foo // ambiguous reference
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                def m() {
                  p.First.fooBar // ambiguous reference
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

    @Test
    void testFieldAndGetterAndSetter3() {
        renameGetters = renameSetters = true

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
                  int getFooBar() { return this.fooBar }
                  void setFooBar(int value) { this.fooBar = value }
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
                  i = f.fooBar
                  f.fooBar = i
                }
                '''.stripIndent()
        ))
    }

    @Test // field and accessor types don't match exactly
    void testFieldAndGetterAndSetter4() {
        renameGetters = renameSetters = true

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
                  int getFooBar() { this.fooBar.get() }
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
                  i = f.fooBar
                  f.foo = i
                }
                '''.stripIndent()
        ))
    }

    @Test
    void testAliasedAccess() {
        performRefactoringAndUndo('getFooBar', new TestSource(
            pack: 'p', name: 'First.groovy',
            contents: '''\
                package p
                import static p.First.getFoo as blah
                import static p.First.getFoo as getF
                class First {
                  static def getFoo() { 'foo' }
                  static def foo
                  static {
                    blah()
                    foo
                    f
                  }
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                import static p.First.getFooBar as blah
                import static p.First.getFooBar as getF
                class First {
                  static def getFooBar() { 'foo' }
                  static def foo
                  static {
                    blah()
                    foo
                    f
                  }
                }
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented
    void testDelegateAccess() {
        performRefactoringAndUndo('fooBar', new TestSource(
            pack: 'p', name: 'OneTwo.groovy',
            contents: '''\
                package p
                class One {
                  def foo
                }
                class Two {
                  @Delegate One one
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class One {
                  def fooBar
                }
                class Two {
                  @Delegate One one
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                import p.Two
                def two = new Two()
                two.foo
                two.getFoo()
                two.setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package q
                import p.Two
                def two = new Two()
                two.fooBar
                two.getFooBar()
                two.setFooBar(null)
                '''.stripIndent()
        ))
    }

    @Test
    void testSubclassAccess1() {
        performRefactoringAndUndo('fooBar', new TestSource(
            pack: 'p', name: 'OneTwo.groovy',
            contents: '''\
                package p
                class One {
                  static def foo
                }
                class Two extends One {
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class One {
                  static def fooBar
                }
                class Two extends One {
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                p.Two.foo
                p.Two.getFoo()
                p.Two.setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package q
                p.Two.fooBar
                p.Two.getFooBar()
                p.Two.setFooBar(null)
                '''.stripIndent()
        ))
    }

    @Test
    void testSubclassAccess2() {
        performRefactoringAndUndo('fooBar', new TestSource(
            pack: 'p', name: 'OneTwo.groovy',
            contents: '''\
                package p
                class One {
                  def foo
                }
                class Two extends One {
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class One {
                  def fooBar
                }
                class Two extends One {
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def two = new p.Two()
                two.foo
                two.getFoo()
                two.setFoo(null)
                '''.stripIndent(),
            finalContents: '''\
                package q
                def two = new p.Two()
                two.fooBar
                two.getFooBar()
                two.setFooBar(null)
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented
    void testSubclassOverride1() {
        renameGetters = renameSetters = true
        performRefactoringAndUndo('ecks', new TestSource(
            pack: 'p', name: 'One.groovy',
            contents: '''\
                package p
                class One {
                  def x
                  def getX() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class One {
                  def ecks
                  def getEcks() {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Two.groovy',
            contents: '''\
                package q
                class Two extends One {
                  @Override
                  def getX() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                class Two extends One {
                  @Override
                  def getEcks() {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                def two = new q.Two()
                two.x
                two.getX()
                two.setX(null)
                '''.stripIndent(),
            finalContents: '''\
                package r
                def two = new q.Two()
                two.ecks
                two.getEcks()
                two.setEcks(null)
                '''.stripIndent()
        ))
    }

    @Test @NotYetImplemented
    void testSubclassOverride2() {
        renameGetters = renameSetters = true
        performRefactoringAndUndo('ecks', new TestSource(
            pack: 'p', name: 'One.groovy',
            contents: '''\
                package p
                class One {
                  def x
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class One {
                  def ecks
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Two.groovy',
            contents: '''\
                package q
                class Two extends One {
                  @Override
                  def getX() {}
                }
                '''.stripIndent(),
            finalContents: '''\
                package q
                class Two extends One {
                  @Override
                  def getEcks() {}
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'r', name: 'Script.groovy',
            contents: '''\
                package r
                def two = new q.Two()
                two.x
                two.getX()
                two.setX(null)
                '''.stripIndent(),
            finalContents: '''\
                package r
                def two = new q.Two()
                two.ecks
                two.getEcks()
                two.setEcks(null)
                '''.stripIndent()
        ))
    }

    @Test
    void testConstructorAccess() {
        performRefactoringAndUndo('boo', new TestSource(
            pack: 'p', name: 'OneTwo.groovy',
            contents: '''\
                package p
                class One {
                  def foo
                }
                class Two extends One {
                  def bar
                }
                '''.stripIndent(),
            finalContents: '''\
                package p
                class One {
                  def boo
                }
                class Two extends One {
                  def bar
                }
                '''.stripIndent()
        ), new TestSource(
            pack: 'q', name: 'Script.groovy',
            contents: '''\
                package q
                def one = new p.One(foo: null)
                def two = new p.Two(foo: null, bar: null)
                '''.stripIndent(),
            finalContents: '''\
                package q
                def one = new p.One(boo: null)
                def two = new p.Two(boo: null, bar: null)
                '''.stripIndent()
        ))
    }

    // TODO: .&getFoo, ::getFoo, .'getFoo'
}
