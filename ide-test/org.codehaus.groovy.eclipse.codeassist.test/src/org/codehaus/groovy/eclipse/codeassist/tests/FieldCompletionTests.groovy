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
package org.codehaus.groovy.eclipse.codeassist.tests

import groovy.transform.NotYetImplemented

import org.eclipse.jdt.internal.codeassist.impl.AssistOptions
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

final class FieldCompletionTests extends CompletionTestSuite {

    @Test
    void testSafeDeferencing() {
        String contents = '''\
            |class SomeClass {
            |  int someProperty
            |  void someMethod() {
            |    someProperty?.x
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '?.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test
    void testSpaces1() {
        String contents = '''\
            |class SomeClass {
            |  int someProperty
            |  void someMethod() {
            |    new SomeClass()    .  ;
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'someProperty', 1)
    }

    @Test
    void testSpaces2() {
        String contents = '''\
            |class SomeClass {
            |  int someProperty
            |  void someMethod() {
            |    new SomeClass()    .  ;
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '. '))
        proposalExists(proposals, 'someProperty', 1)
    }

    @Test
    void testSpaces3() {
        String contents = '''\
            |class SomeClass {
            |  int someProperty
            |  void someMethod() {
            |    new SomeClass()    .  ;
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '. '))
        proposalExists(proposals, 'someProperty', 1)
    }

    @Test // GRECLIPSE-616
    void testProperties1() {
        String contents = '''\
            |class Other {
            |  def x
            |}
            |new Other().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 1)
        proposalExists(proposals, 'setX', 1)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties1a() {
        String contents = '''\
            |class Other {
            |  def x
            |}
            |Other o
            |o.
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 1)
        proposalExists(proposals, 'setX', 1)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties2() {
        String contents = '''\
            |class Other {
            |  public def x
            |}
            |new Other().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 0)
        proposalExists(proposals, 'setX', 0)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties2a() {
        String contents = '''\
            |class Other {
            |  public def x
            |}
            |Other o
            |o.
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 0)
        proposalExists(proposals, 'setX', 0)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties3() {
        String contents = '''\
            |class Other {
            |  private def x
            |}
            |new Other().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 0)
        proposalExists(proposals, 'setX', 0)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties3a() {
        String contents = '''\
            |class Other {
            |  private def x
            |}
            |Other o
            |o.
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 0)
        proposalExists(proposals, 'setX', 0)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties4() {
        String contents = '''\
            |class Other {
            |  public static final int x = 9
            |}
            |new Other().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 0)
        proposalExists(proposals, 'setX', 0)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties4a() {
        String contents = '''\
            |class Other {
            |  public static final int x = 9
            |}
            |Other o
            |o.
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 0)
        proposalExists(proposals, 'setX', 0)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties5() {
        addJavaSource 'class Other { int x = 9; }', 'Other'

        String contents = 'new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 0)
        proposalExists(proposals, 'setX', 0)
        proposalExists(proposals, 'x', 1)
    }

    @Test
    void testProperties5a() {
        addJavaSource 'class Other { int x = 9; }', 'Other'

        String contents = 'Other o; o.'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getX', 0)
        proposalExists(proposals, 'setX', 0)
        proposalExists(proposals, 'x', 1)
    }

    @Test // GRECLIPSE-1162
    void testProperties6() {
        String contents = 'class Other { boolean x }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'setX(boolean value) : void', 1)
        proposalExists(proposals, 'getX() : boolean', 1)
        proposalExists(proposals, 'isX() : boolean', 1)
        proposalExists(proposals, 'x : boolean', 1)
    }

    @Test // GRECLIPSE-1162
    void testProperties6a() {
        String contents = 'class Other { boolean xx }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'setXx(boolean value) : void', 1)
        proposalExists(proposals, 'getXx() : boolean', 1)
        proposalExists(proposals, 'isXx() : boolean', 1)
        proposalExists(proposals, 'xx : boolean', 1)
    }

    @Test // GRECLIPSE-1162
    void testProperties7() {
        String contents = 'class Other { boolean isX() {} }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'isX() : boolean', 1)
        proposalExists(proposals, 'x : boolean', 1)
    }

    @Test // GRECLIPSE-1162
    void testProperties7a() {
        String contents = 'class Other { boolean isXx() {} }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'isXx() : boolean', 1)
        proposalExists(proposals, 'xx : boolean', 1)
    }

    @Test // GRECLIPSE-1162
    void testProperties8() {
        String contents = 'class Other { boolean isxx() {} }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'isxx() : boolean', 1)
        proposalExists(proposals, 'xx : boolean', 0)
    }

    @Test // GRECLIPSE-1162
    void testProperties9() {
        String contents = 'class Other { boolean getxx() {} }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getxx() : boolean', 1)
        proposalExists(proposals, 'xx : boolean', 0)
    }

    @Test // GRECLIPSE-1698
    void testProperties10() {
        String contents = 'class Other { boolean getXXxx() {} }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getXXxx() : boolean', 1)
        proposalExists(proposals, 'XXxx : boolean', 1)
    }

    @Test // GRECLIPSE-1698
    void testProperties11() {
        String contents = 'class Other { boolean isXXxx() {} }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'isXXxx() : boolean', 1)
        proposalExists(proposals, 'XXxx : boolean', 1)
    }

    @Test // GRECLIPSE-1698
    void testProperties12() {
        String contents = 'class Other { boolean getxXxx() {} }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'getxXxx() : boolean', 1)
        proposalExists(proposals, 'xXxx : boolean', 0)
    }

    @Test // GRECLIPSE-1698
    void testProperties13() {
        String contents = 'class Other { boolean isxXxx() {} }\n new Other().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'isxXxx() : boolean', 1)
        proposalExists(proposals, 'xXxx : boolean', 0)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/651
    void testProperties14() {
        String contents = '''\
            |class Other {
            |  void setXxx(String xxx) {}
            |}
            |new Other().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'setXxx(String xxx) : void', 1)
        proposalExists(proposals, 'xxx : String', 1)
    }

    @Test
    void testProperties15() {
        String contents = '''\
            |class C {
            |  def x = 42
            |  def m() {
            |    x.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'intValue() : int', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1080
    void testProperties16() {
        String contents = '''\
            |class C {
            |  def x
            |  C() {
            |    x = 42
            |  }
            |  def m() {
            |    x.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'intValue() : int', 1)
    }

    @Test
    void testProperties16a() {
        String contents = '''\
            |class C {
            |  def m() {
            |    x.
            |  }
            |  C() {
            |    x = 42
            |  }
            |  def x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'intValue() : int', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1080
    void testProperties17() {
        String contents = '''\
            |import javax.annotation.PostConstruct
            |class C {
            |  def x
            |  @PostConstruct
            |  void init() {
            |    x = 42
            |  }
            |  def m() {
            |    x.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'intValue() : int', 1)
    }

    @Test
    void testProperties17a() {
        String contents = '''\
            |class C {
            |  def x
            |  @javax.annotation.PostConstruct
            |  void init() {
            |    x = 42
            |  }
            |  def m() {
            |    x.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'intValue() : int', 1)
    }

    @Test
    void testProperties17b() {
        String contents = '''\
            |class C {
            |  def x
            |  C() {
            |    x.
            |  }
            |  @javax.annotation.PostConstruct
            |  void init() {
            |    x = 42
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'intValue() : int', 0)
    }

    @Test
    void testProperties17c() {
        String contents = '''\
            |class C {
            |  @javax.annotation.PostConstruct
            |  void init() {
            |    x = 42
            |  }
            |  C() {
            |    x.
            |  }
            |  def x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'intValue() : int', 0)
    }

    @Test
    void testClosure1() {
        String contents = '''\
            |class Other {
            |  def xxx = { a, b -> }
            |}
            |new Other().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'xxx : Object', 1)
        proposalExists(proposals, 'xxx(Object a, Object b)', 1)
    }

    @Test
    void testClosure2() {
        String contents = '''\
            |class Other {
            |  def xxx = { int a, int b -> }
            |}
            |new Other().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'xxx : Object', 1)
        proposalExists(proposals, 'xxx(int a, int b)', 1)
    }

    @Test
    void testClosure3() {
        String contents = '''\
            |class Other {
            |  def xxx = { }
            |}
            |new Other().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'xxx : Object', 1)
        proposalExists(proposals, 'xxx()', 1)
    }

    @Test // GRECLIPSE-1114
    void testClosure4() {
        String contents = 'def xxx = { def bot\n b }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'b'))
        proposalExists(proposals, 'binding', 1) // from the delegate
        proposalExists(proposals, 'bot', 1) // from inside closure
    }

    @Test // GRECLIPSE-1114
    void testClosure5() {
        String contents = '''\
            |def xxx() { }
            |(0..10).each {
            |  xx
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'xx'))
        proposalExists(proposals, 'xxx', 1) // from the delegate
    }

    @Test // GRECLIPSE-1114
    void testClosure6() {
        String contents = '''\
            |class Super {
            |  def xxx() { }
            |}
            |class Sub extends Super {
            |  def meth() {
            |    (0..10).each {
            |      xx
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'xx'))
        proposalExists(proposals, 'xxx', 1) // from the delegate
    }

    @Test // GRECLIPSE-1114
    void testClosure7() {
        String contents = '''\
            |class Super {
            |  def xxx
            |}
            |class Sub extends Super {
            |  def meth() {
            |    (0..10).each {
            |      xx
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'xx'))
        proposalExists(proposals, 'xxx', 1) // from the delegate
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/360
    void testClosure8() {
        String contents = '''\
            |class A {
            |  def xxx
            |}
            |class B {
            |  def xyz
            |  void meth(A a) {
            |    a.with {
            |      x
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'x'))
        proposalExists(proposals, 'xxx', 1) // from the delegate
        proposalExists(proposals, 'xyz', 1) // from the owner
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/360
    void testClosure9() {
        String contents = '''\
            |class A {
            |  def xxx
            |}
            |class B {
            |  def xyz
            |  static void meth(A a) {
            |    a.with {
            |      x
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'x'))
        proposalExists(proposals, 'xxx', 1) // from the delegate
        proposalExists(proposals, 'xyz', 0) // *not* from the owner
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/364
    void testClosure10() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.DELEGATE_FIRST) Closure c) {}
            |  void test() {
            |    foo {
            |      zz // delegate is B, owner is A
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 2)
        def one = indexOfProposal(proposals, 'zzz')
        def two = indexOfProposal(proposals, 'zzz', one + 1)
        applyProposalAndCheck(proposals[one].displayString == 'zzz : String - A' ? proposals[one] : proposals[two], contents.replace('zz //', 'owner.zzz //'))
    }

    @Test
    void testClosure10a() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.DELEGATE_FIRST) Closure c) {}
            |  void test() {
            |    foo {
            |      zz // delegate is B, owner is A
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 2)
        def one = indexOfProposal(proposals, 'zzz')
        def two = indexOfProposal(proposals, 'zzz', one + 1)
        applyProposalAndCheck(proposals[one].displayString != 'zzz : String - A'
            ? proposals[one] : proposals[two], contents.replace('zz //', 'zzz //')) // no qualifier
    }

    @Test
    void testClosure11() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.OWNER_FIRST) Closure c) {}
            |  void test() {
            |    foo {
            |      zz // delegate is B, owner is A
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 2)
        def one = indexOfProposal(proposals, 'zzz')
        def two = indexOfProposal(proposals, 'zzz', one + 1)
        applyProposalAndCheck(proposals[one].displayString == 'zzz : String - B'
            ? proposals[one] : proposals[two], contents.replace('zz //', 'delegate.zzz //'))
    }

    @Test
    void testClosure11a() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.OWNER_FIRST) Closure c) {}
            |  void test() {
            |    foo {
            |      zz // delegate is B, owner is A
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 2)
        def one = indexOfProposal(proposals, 'zzz')
        def two = indexOfProposal(proposals, 'zzz', one + 1)
        applyProposalAndCheck(proposals[one].displayString != 'zzz : String - B'
            ? proposals[one] : proposals[two], contents.replace('zz //', 'zzz //')) // no qualifier
    }

    @Test
    void testClosure12() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.TO_SELF) Closure c) {}
            |  void test() {
            |    foo {
            |      zz // delegate is B, owner is A
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 2)
        def one = indexOfProposal(proposals, 'zzz')
        def two = indexOfProposal(proposals, 'zzz', one + 1)
        applyProposalAndCheck(proposals[one].displayString == 'zzz : String - A'
            ? proposals[one] : proposals[two], contents.replace('zz //', 'owner.zzz //'))
    }

    @Test
    void testClosure12a() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.TO_SELF) Closure c) {}
            |  void test() {
            |    foo {
            |      zz // delegate is B, owner is A
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 2)
        def one = indexOfProposal(proposals, 'zzz')
        def two = indexOfProposal(proposals, 'zzz', one + 1)
        applyProposalAndCheck(proposals[one].displayString != 'zzz : String - A'
            ? proposals[one] : proposals[two], contents.replace('zz //', 'delegate.zzz //'))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/803
    void testClosure13() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  static class C {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.DELEGATE_FIRST) Closure c) {}
            |  def bar(@DelegatesTo(value=C, strategy=Closure.DELEGATE_FIRST) Closure c) {}
            |  void test() {
            |    foo {
            |      bar {
            |        zz // delegate is C, owner.delegate is B, owner.owner is A
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 3)

        proposals.each { ICompletionProposal proposal ->
            switch (proposal.displayString) {
            case 'zzz : String - A':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'owner.owner.zzz //'))
                break
            case 'zzz : String - B':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'owner.zzz //'))
                break
            case 'zzz : String - C':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'zzz //'))
                break
            }
        }
    }

    @Test
    void testClosure13a() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  static class C {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.OWNER_FIRST) Closure c) {}
            |  def bar(@DelegatesTo(value=C, strategy=Closure.OWNER_FIRST) Closure c) {}
            |  void test() {
            |    foo {
            |      bar {
            |        zz // delegate is C, owner.delegate is B, owner.owner is A
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 3)

        proposals.each { ICompletionProposal proposal ->
            switch (proposal.displayString) {
            case 'zzz : String - A':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'zzz //'))
                break
            case 'zzz : String - B':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'owner.delegate.zzz //'))
                break
            case 'zzz : String - C':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'delegate.zzz //'))
                break
            }
        }
    }

    @Test
    void testClosure13b() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  static class C {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.DELEGATE_FIRST) Closure c) {}
            |  def bar(@DelegatesTo(value=C, strategy=Closure.OWNER_FIRST) Closure c) {}
            |  void test() {
            |    foo {
            |      bar {
            |        zz // delegate is C, owner.delegate is B, owner.owner is A
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 3)

        proposals.each { ICompletionProposal proposal ->
            switch (proposal.displayString) {
            case 'zzz : String - A':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'owner.owner.zzz //'))
                break
            case 'zzz : String - B':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'zzz //'))
                break
            case 'zzz : String - C':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'delegate.zzz //'))
                break
            }
        }
    }

    @Test
    void testClosure13c() {
        String contents = '''\
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  static class C {
            |    String zzz
            |  }
            |  def foo(@DelegatesTo(value=B, strategy=Closure.OWNER_FIRST) Closure c) {}
            |  def bar(@DelegatesTo(value=C, strategy=Closure.DELEGATE_FIRST) Closure c) {}
            |  void test() {
            |    foo {
            |      bar {
            |        zz // delegate is C, owner.delegate is B, owner.owner is A
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 3)

        proposals.each { ICompletionProposal proposal ->
            switch (proposal.displayString) {
            case 'zzz : String - A':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'owner.zzz //'))
                break
            case 'zzz : String - B':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'owner.delegate.zzz //'))
                break
            case 'zzz : String - C':
                applyProposalAndCheck(proposal, contents.replace('zz //', 'zzz //'))
                break
            }
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/803
    void testClosure14() {
        String contents = '''\
            |import groovy.transform.stc.*
            |class A {
            |  String zzz
            |  static class B {
            |    String zzz
            |  }
            |  static class C {
            |    String zzz
            |  }
            |  def foo(@ClosureParams(value=SimpleType, options='A.B') Closure block) {}
            |  def bar(@ClosureParams(value=SimpleType, options='A.C') Closure block) {}
            |  void test() {
            |    foo { b ->
            |      bar { c ->
            |        zz // delegate is Closure, owner is Closure, owner.delegate is Closure, owner.owner is A
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'zz'))

        proposalExists(proposals, 'zzz', 1)
        findFirstProposal(proposals, 'zzz').with {
            assert displayString == 'zzz : String - A' // not B or C
            applyProposalAndCheck(it, contents.replace('zz //', 'zzz //'))
        }
    }

    @Test
    void testArrayLength1() {
        String contents = 'int[] arr; arr.len'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'length', 1)
    }

    @Test
    void testArrayLength2() {
        String contents = 'Object[] arr; arr.len'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'length', 1)
    }

    @Test
    void testArrayLength3() {
        String contents = '[].toArray().len'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'length', 1)
    }

    @Test
    void testArrayLength4() {
        String contents = '''\
            |static <T> T[] array() {
            |}
            |array().len
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.lastIndexOf('len') + 3)
        proposalExists(proposals, 'length', 1)
    }

    @Test
    void testEnumReceiver1() {
        addJavaSource '''\
            |enum E {
            |  CONST;
            |  public static final String VALUE = "";
            |}
            |'''.stripMargin(), 'E'

        String contents = 'E e = '
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'CONST', 1)
        proposalExists(proposals, 'VALUE', 0)
    }

    @Test
    void testEnumReceiver1a() {
        addJavaSource '''\
            |enum E {
            |  CONST;
            |  public static final String VALUE = "";
            |}
            |'''.stripMargin(), 'E'

        String contents = 'E e = ;'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length() - 1)
        proposalExists(proposals, 'CONST', 1)
        proposalExists(proposals, 'VALUE', 0)
    }

    @Test
    void testEnumReceiver1b() {
        addJavaSource '''\
            |enum E {
            |  CONST;
            |  public static final String VALUE = "";
            |}
            |'''.stripMargin(), 'E'

        String contents = 'E e = \n'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length() - 1)
        proposalExists(proposals, 'CONST', 1)
        proposalExists(proposals, 'VALUE', 0)
    }

    @Test
    void testEnumReceiver2() {
        addJavaSource '''\
            |public enum Color {
            |  RED, BLACK
            |}
            |'''.stripMargin(), 'Color', 'tree.node'

        String contents = '''\
            |def meth(tree.node.Color c) {}
            |meth(B)
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'B', 'BLACK')
        applyProposalAndCheck(proposal, '''\
            |import static tree.node.Color.BLACK
            |
            |def meth(tree.node.Color c) {}
            |meth(BLACK)
            |'''.stripMargin())
    }

    @Test @NotYetImplemented
    void testEnumReceiver2a() {
        addJavaSource '''\
            |public enum Color {
            |  RED, BLACK
            |}
            |'''.stripMargin(), 'Color', 'tree.node'
        addJavaSource '''\
            |public interface D {
            |  String BLACK= "";
            |}
            |'''.stripMargin(), 'D', 'a.b.c'

        String contents = '''\
            |import static a.b.c.D.BLACK
            |
            |def meth(tree.node.Color c) {}
            |meth(BL)
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'BL'))
        proposalExists(proposals, 'BLACK', 2)

        applyProposalAndCheck(orderByRelevance(proposals)[0], '''\
            |import static a.b.c.D.BLACK
            |
            |import tree.node.Color
            |
            |def meth(tree.node.Color c) {}
            |meth(Color.BLACK)
            |'''.stripMargin())
    }

    @Test
    void testEnumReceiver3() {
        addJavaSource '''\
            |public enum Color {
            |  RED, BLACK
            |}
            |'''.stripMargin(), 'Color', 'tree.node'

        String contents = '''\
            |def meth(tree.node.Color... colors) {}
            |meth(RED, BL)
            |'''.stripMargin()
        checkUniqueProposal(contents, 'BL', 'BLACK')
    }

    @Test
    void testMapReceiver1() {
        String contents = '''\
            |HashMap map = [:]
            |map.e
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'e'))
        proposalExists(proposals, 'DEFAULT_LOAD_FACTOR', 0) // static
        proposalExists(proposals, 'threshold', 0) // non-static
        proposalExists(proposals, 'table', 0) // non-static
    }

    @Test
    void testMapReceiver1a() {
        String contents = '''\
            |HashMap map = [:]
            |map.@e
            |'''.stripMargin()
            ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'e'))
            proposalExists(proposals, 'DEFAULT_LOAD_FACTOR', 1) // static
            proposalExists(proposals, 'threshold', 1) // non-static
            proposalExists(proposals, 'table', 1) // non-static
    }

    @Test // GRECLIPSE-1175
    void testInitializer() {
        String contents = '''\
            |class MyClass {
            |  def something = Class.
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'forName', Float.parseFloat(System.getProperty('java.specification.version')) < 9 ? 2 : 3)
    }

    @Test
    void testStaticFields1() {
        String contents = '''\
            |import java.util.regex.Pattern
            |Pattern.
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'buffer', 0) // non-static
        proposalExists(proposals, 'DOTALL', 1) // static
    }

    @Test
    void testStaticFields2() {
        addGroovySource '''\
            |abstract class A {
            |  public static final Number SOME_THING = 42
            |}
            |class C extends A {
            |  public static final Number SOME_THANG = -1
            |}
            |'''.stripMargin(), 'C', 'p'
        String contents = '''\
            |import p.C
            |C.SOME
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'SOME'))
        proposalExists(proposals, 'SOME_THANG', 1)
        proposalExists(proposals, 'SOME_THING', 1)
    }

    @Test
    void testImportStaticField1() {
        String contents = '''\
            |import static java.util.regex.Pattern.DOTALL
            |DOT
            |'''.stripMargin()
        checkUniqueProposal(contents, 'DOT', 'DOTALL')
    }

    @Test
    void testImportStaticField2() {
        addGroovySource '''\
            |abstract class A {
            |  public static final Number SOME_THING = 42
            |}
            |class C extends A {
            |  public static final Number SOME_THANG = -1
            |}
            |'''.stripMargin(), 'C', 'p'
        String contents = '''\
            |import static p.C.SOME_THANG
            |import static p.C.SOME_THING
            |SOME
            |'''.stripMargin()
        checkUniqueProposal(contents, 'SOME', 'SOME_THANG')
        checkUniqueProposal(contents, 'SOME', 'SOME_THING')
    }

    @Test
    void testImportStaticStarField1() {
        String contents = '''\
            |import static java.util.regex.Pattern.*
            |DOT
            |'''.stripMargin()
        checkUniqueProposal(contents, 'DOT', 'DOTALL')
    }

    @Test
    void testImportStaticStarField2() {
        addGroovySource '''\
            |abstract class A {
            |  public static final Number SOME_THING = 42
            |}
            |class C extends A {
            |  public static final Number SOME_THANG = -1
            |}
            |'''.stripMargin(), 'C', 'p'
        String contents = '''\
            |import static p.C.*
            |SOME
            |'''.stripMargin()
        checkUniqueProposal(contents, 'SOME', 'SOME_THANG')
        checkUniqueProposal(contents, 'SOME', 'SOME_THING')
    }

    @Test
    void testFavoriteStaticStarField1() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.*')

        String contents = '''\
            |DOT
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'DOT', 'DOTALL')

        applyProposalAndCheck(proposal, '''\
            |import static java.util.regex.Pattern.DOTALL
            |
            |DOTALL
            |'''.stripMargin())
    }

    @Test
    void testFavoriteStaticField() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.DOTALL')

        String contents = '''\
            |DOT
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'DOT', 'DOTALL')

        applyProposalAndCheck(proposal, '''\
            |import static java.util.regex.Pattern.DOTALL
            |
            |DOTALL
            |'''.stripMargin())
    }

    @Test
    void testFavoriteStaticField2() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.DOTALL')
        setJavaPreference(AssistOptions.OPTION_SuggestStaticImports, AssistOptions.DISABLED)
        try {
            String contents = '''\
                |DOT
                |'''.stripMargin()
            ICompletionProposal proposal = checkUniqueProposal(contents, 'DOT', 'DOTALL')

            applyProposalAndCheck(proposal, '''\
                |import java.util.regex.Pattern
                |
                |Pattern.DOTALL
                |'''.stripMargin())
        } finally {
            setJavaPreference(AssistOptions.OPTION_SuggestStaticImports, AssistOptions.ENABLED)
        }
    }

    @Test
    void testFavoriteStaticField3() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.DOTALL')
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, false)

        String contents = '''\
            |DOT
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'DOT', 'DOTALL')

        applyProposalAndCheck(proposal, '''\
            |java.util.regex.Pattern.DOTALL
            |'''.stripMargin())
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/984
    void testFavoriteStaticField4() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'zzz')

        assert createProposalsAtOffset('zz', 2).length == 0
    }

    @Test
    void testRangeExpressionCompletion1() {
        String contents = '''\
            |(0..1).
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'to : Comparable', 1)
        proposalExists(proposals, 'from : Comparable', 1)
        proposalExists(proposals, 'reverse : boolean', 1)
    }

    @Test
    void testRangeExpressionCompletion2() {
        setJavaPreference(PreferenceConstants.CODEASSIST_AUTOACTIVATION, true)
        String contents = '''\
            |def range = 0.
            |'''.stripMargin()
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, getLastIndexOf(contents, '.')), 'BYTES')
        char[] triggers = proposal.triggerCharacters
        assert !triggers.contains('.' as char)

        // simulate typing while content assist popup is displayed
        proposal.isPrefix('b', proposal.displayString)

        triggers = proposal.triggerCharacters
        assert triggers.contains('.' as char)
    }

    @Test
    void testRangeExpressionCompletion3() {
        setJavaPreference(PreferenceConstants.CODEASSIST_AUTOACTIVATION, true)
        String contents = '''\
            |def other = 0.b
            |'''.stripMargin()
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, getLastIndexOf(contents, 'b')), 'BYTES')
        char[] triggers = proposal.triggerCharacters
        assert triggers.contains('.' as char)

        // simulate typing while content assist popup is displayed
        proposal.isPrefix('', proposal.displayString)

        //triggers = proposal.triggerCharacters
        //assert !triggers.contains('.' as char)
    }

    @Test
    void testTraitFields1() {
        String contents = '''\
            |trait T {
            |  def m() {
            |    #
            |  }
            |  private String field1
            |  private static String field2
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('#', ''), contents.indexOf('#'))
        proposalExists(proposals, 'field1', 1)
        proposalExists(proposals, 'field2', 1)
    }

    @Test
    void testTraitFields2() {
        String contents = '''\
            |trait T {
            |  static def m() {
            |    #
            |  }
            |  private String field1
            |  private static String field2
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('#', ''), contents.indexOf('#'))
        proposalExists(proposals, 'field1', 0)
        proposalExists(proposals, 'field2', 1)
    }

    @Test
    void testTraitFields3() {
        String contents = '''\
            |trait T {
            |  private String field1
            |  private static String field2
            |}
            |class C implements T {
            |  def m() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('#', ''), contents.indexOf('#'))
        proposalExists(proposals, 'T__field1', 1)
        proposalExists(proposals, 'T__field2', 1)
        proposalExists(proposals, 'field1', 0)
        proposalExists(proposals, 'field2', 0)
    }

    @Test
    void testTraitFields4() {
        String contents = '''\
            |trait T {
            |  private String field1
            |  private static String field2
            |}
            |class C implements T {
            |  static def m() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('#', ''), contents.indexOf('#'))
        proposalExists(proposals, 'T__field1', 0)
        proposalExists(proposals, 'T__field2', 1)
        proposalExists(proposals, 'field1', 0)
        proposalExists(proposals, 'field2', 0)
    }
}
