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

import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Ignore
import org.junit.Test

/**
 * Tests to ensure proper ordering of completion proposals.
 */
final class RelevanceTests extends CompletionTestSuite {

    @Test
    void testExactMatchThenPrefixMatchThenIgnoreCaseThenCamelCaseThenSubstring() {
        String contents = '''\
            |class Outer {
            |  def aToZ() {}
            |  def toz() {}
            |  def Toz() {}
            |  def toZAbc() {}
            |  def tozXyz() {}
            |}
            |new Outer().toz
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'toz')))
        assertProposalOrdering(proposals, 'toz', 'tozXyz', 'Toz', 'toZAbc', 'aToZ')
    }

    @Test
    void testLocalThenFieldThenMethodThenDGM() {
        String contents = '''\
            |class Outer {
            |  def f
            |  def m() {
            |    def v
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'def v\n')))
        assertProposalOrdering(proposals, 'v', 'f', 'getF', 'm', 'find')
    }

    @Test
    void testParamThenFieldThenMethodThenDGM() {
        String contents = '''\
            |class Outer {
            |  def f
            |  def m(p) {
            |    null
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'null\n')))
        assertProposalOrdering(proposals, 'p', 'f', 'getF', 'm', 'find')
    }

    @Test
    void testObjectMethods() {
        String contents = '''\
            |class Outer {
            |  def toZZZ(p) {
            |    this.to
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'this.to')))
        assertProposalOrdering(proposals, 'toZZZ', 'toString')
    }

    @Test
    void testOverriddenObjectMethods() {
        String contents = '''\
            |class Outer {
            |  def toZZZ(p) {
            |    this.to
            |  }
            |  String toString() {
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'this.to')))
        assertProposalOrdering(proposals, 'toString', 'toZZZ')
    }

    @Test
    void testClosureMethodsAndProperties1() {
        String contents = '''\
            |def closure = { ->
            |  d
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'd')))
        assertProposalOrdering(proposals, 'dump()', 'delegate', 'directive', 'defaultMetaClass')
    }

    @Test
    void testClosureMethodsAndProperties2() {
        String contents = '''\
            |def closure = { ->
            |  get
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'get')))
        assertProposalOrdering(proposals, 'getAt', 'getDelegate()', 'getDirective()', 'getMetaClass()', 'getDefaultMetaClass()')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/787
    void testClosureMethodsAndProperties3() {
        String contents = '''\
            |class Outer {
            |  static class Foo {
            |    String string
            |  }
            |  static class Bar {
            |    String string
            |    void meth(Foo foo) {
            |      foo.with {
            |        s
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 's')))
        assertProposalOrdering(proposals, 'string : String - Foo', 'string : String - Bar', 'setString(String value) : void - Foo', 'setString(String value) : void - Bar', 'sleep')
    }

    @Test
    void testClosureMethodsAndProperties4() {
        String contents = '''\
            |class Outer {
            |  static class Foo {
            |    String string
            |  }
            |  static class Bar {
            |    String string
            |    def foo(@DelegatesTo(value=Foo, strategy=Closure.OWNER_FIRST) Closure c) {}
            |    void meth() {
            |      foo { ->
            |        s
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 's')))
        assertProposalOrdering(proposals, 'string : String - Bar', 'string : String - Foo', 'setString(String value) : void - Bar', 'setString(String value) : void - Foo', 'sleep')
    }

    @Test
    void testStaticImportFieldsAndMethods() {
        String contents = '''\
            |import static java.util.Arrays.*
            |def method() {
            |  #
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents.replace('#', ' '), getIndexOf(contents, '#')))
        assertProposalOrdering(proposals, 'asList', 'copyOf', 'INSERTIONSORT_THRESHOLD', 'MIN_ARRAY_SORT_GRAN', 'legacyMergeSort', 'rangeCheck', 'defaultMetaClass', 'getDefaultMetaClass()')
    }

    @Test @Ignore('Need to find "public" in assertProposalOrdering without breaking other tests')
    void testNewMethodThenModifier() {
        String contents = '''\
            |class Other extends Outer {
            |  pu
            |  def x() { }
            |}
            |class Outer {
            |  def pub() {
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'pu')))
        assertProposalOrdering(proposals, 'pub', 'public')
    }

    @Test // this one should do alphabetical ordering
    void testFieldOfAssignedType1() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    def f = a
            |  }
            |  String az
            |  int aa
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'aa', 'az')
    }

    @Test // this one should do the string first
    void testFieldOfAssignedType2() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    String f = a
            |  }
            |  String az
            |  int aa
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'az', 'aa')
    }

    @Test // this one should do the int first
    void testFieldOfAssignedType3() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    int f = a
            |  }
            |  String aa
            |  int az
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'az', 'aa')
    }

    @Test // this one should do alphabetical ordering
    void testMethodOfAssignedType1() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    def f = a
            |  }
            |  String az() { }
            |  int aa() { }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'aa', 'az')
    }

    @Test // this one should do the string first
    void testMethodOfAssignedType2() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    String f = a
            |  }
            |  int aa() { }
            |  String az() { }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'az', 'aa')
    }

    @Test // this one should do the string first
    void testMethodOfAssignedType2a() {
        String contents = '''\
            |class Other {
            |  private String f
            |  def x() {
            |    this.f = a // property expression
            |  }
            |  int aa() { }
            |  String az() { }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'az', 'aa')
    }

    @Test // this one should do the string first
    void testMethodOfAssignedType2b() {
        String contents = '''\
            |class Other {
            |  String f
            |  def x() {
            |    this.f = a  // property expression
            |  }
            |  int aa() { }
            |  String az() { }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'az', 'aa')
    }

    @Test // this one should do the int first
    void testMethodOfAssignedType3() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    int f = a
            |  }
            |  String aa() { }
            |  int az() { }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'az', 'aa')
    }

    @Test // this one should do alphabetical ordering
    void testMethodAndFieldOfAssignedType1() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    def f = a
            |  }
            |  String az() { }
            |  int aa
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'aa', 'az')
    }

    @Test // this one should do the string first
    void testMethodAndFieldOfAssignedType2() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    String f = a
            |  }
            |  String az() { }
            |  int aa
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'az', 'aa')
    }

    @Test // this one should do the int first
    void testMethodAndFieldOfAssignedType3() {
        String contents = '''\
            |class Other {
            |  def x() {
            |    int f = a
            |  }
            |  String aa() { }
            |  int az
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ' = a')))
        assertProposalOrdering(proposals, 'az', 'aa')
    }

    @Test
    void testNamedArgumentAssignedType1() {
        String contents = '''\
            |class B {
            |  String aa() { }
            |  int    ab() { }
            |  Long   ac() { }
            |  String ay
            |  Number az
            |}
            |class X {
            |  void setF(Number f) {}
            |}
            |
            |B b; X x = new X(f: b.a)
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'b.a')))
        assertProposalOrdering(proposals, 'az', 'ab', 'ac', 'ay', 'aa')
    }

    @Test
    void testNamedArgumentAssignedType2() {
        String contents = '''\
            |class B {
            |  String aa() { }
            |  int    ab() { }
            |  Long   ac() { }
            |  String ay
            |  Number az
            |}
            |class X {
            |  void setF(String f) {}
            |}
            |
            |B b; X x = new X(f: b.a)
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'b.a')))
        assertProposalOrdering(proposals, 'ay', 'aa', 'az', 'ab', 'ac')
    }

    @Test
    void testNamedArgumentAssignedType3() {
        String contents = '''\
            |class B {
            |  String aa() { }
            |  int    ab() { }
            |  Long   ac() { }
            |  String ay
            |  Number az
            |}
            |class X {
            |  int f // property instead of setter
            |}
            |
            |B b; X x = new X(f: b.a)
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'b.a')))
        assertProposalOrdering(proposals, 'ab', 'ay', 'az', 'aa', 'ac')
    }

    @Test
    void testClassVariableAssignedType1() {
        String contents = '''\
            |Class<? extends CharSequence> cs = St
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'St')))
        assertProposalOrdering(proposals, 'String - java.lang', 'StringBuffer - java.lang', 'StringBuilder - java.lang', 'Stack - java.util')
    }

    @Test
    void testClassVariableAssignedType2() {
        String contents = '''\
            |for (Class<? extends CharSequence> cs = St; condition;) {
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'St')))
        assertProposalOrdering(proposals, 'String - java.lang', 'StringBuffer - java.lang', 'StringBuilder - java.lang', 'Stack - java.util')
    }

    @Test
    void testClassVariableAssignedType3() {
        String contents = '''\
            |class X {
            |  void meth(Class<? extends CharSequence> cs = St) {
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'St')))
        assertProposalOrdering(proposals, 'String - java.lang', 'StringBuffer - java.lang', 'StringBuilder - java.lang', 'Stack - java.util')
    }

    @Test
    void testClassVariableAssignedType4() {
        String contents = '''\
            |class X {
            |  Class<? extends CharSequence> cs = St
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'St')))
        assertProposalOrdering(proposals, 'String - java.lang', 'StringBuffer - java.lang', 'StringBuilder - java.lang', 'Stack - java.util')
    }

    @Test
    void testClassVariableAssignedType5() {
        String contents = '''\
            |class X {
            |  public Class<? extends CharSequence> cs = St
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'St')))
        assertProposalOrdering(proposals, 'String - java.lang', 'StringBuffer - java.lang', 'StringBuilder - java.lang', 'Stack - java.util')
    }

    @Test
    void testClassVariableAssignedType6() {
        addJavaSource '''\
            |import java.lang.annotation.*;
            |
            |@Retention(RetentionPolicy.RUNTIME)
            |@Target(ElementType.TYPE)
            |@Inherited
            |public @interface Anno {
            |  Class<? extends CharSequence> value();
            |}
            |'''.stripMargin(), 'Anno'

        String contents = '''\
            |@Anno(value = St)
            |class X {
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'St')))
        assertProposalOrdering(proposals, 'String - java.lang', 'StringBuffer - java.lang', 'StringBuilder - java.lang', 'Stack - java.util')
    }

    @Test
    void testClassAttributeDefaultType() {
        String contents = '''\
            |@interface X {
            |  Class<? extends CharSequence> value() default St
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'St')))
        assertProposalOrdering(proposals, 'String - java.lang', 'StringBuffer - java.lang', 'StringBuilder - java.lang', 'Stack - java.util')
    }

    @Test
    void testCatchParameterType() {
        String contents = '''\
            |try {
            |} catch (St) {
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'St')))
        assertProposalOrdering(proposals, 'StringWriterIOException - groovy.lang', 'StackOverflowError - java.lang', 'Stack - java.util')
    }

    @Test
    void testInnerClassType() {
        // types normally rank very low; but when an inner type matches qualifier ending in '.', the
        // proposal needs some help to be seen amongst DGMs, Class, Object, and GroovyObject members
        String contents = '''\
            |Map.
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, '.')))
        assertProposalOrdering(proposals, 'Entry - java.util.Map', 'any() : boolean', 'array : boolean')
    }
}
