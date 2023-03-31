/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.test

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.junit.Assume.assumeTrue

import groovy.test.NotYetImplemented

import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistContext
import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.AssignStatementToNewLocalProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertAccessorToPropertyProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertClosureDefToMethodProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertMethodDefToClosureProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertToMultiLineStringProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertToSingleLineStringProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertVariableToFieldProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ExtractToConstantProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ExtractToLocalProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.InlineLocalVariableProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.RemoveSpuriousSemicolonsProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.ReplaceDefWithStaticTypeProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.SplitVariableDeclAndInitProposal
import org.codehaus.groovy.eclipse.quickassist.proposals.SwapLeftAndRightOperandsProposal
import org.codehaus.groovy.eclipse.refactoring.test.extract.ConvertLocalToFieldTestsData
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractConstantTestsData
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractLocalTestsData
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.ui.text.correction.AssistContext
import org.eclipse.jface.text.IDocument
import org.junit.Test

final class QuickAssistTests extends QuickFixTestSuite {

    @Test
    void testConvertToClosure1() {
        assertConversion(
            'def x()  { }',
            'def x = { }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure2() {
        assertConversion(
            'class X { \ndef x()  { } }',
            'class X { \ndef x = { } }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure3() {
        assertConversion(
            'def x(a)  { }',
            'def x = { a -> }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure4() {
        assertConversion(
            'def x(int a, int b)  { }',
            'def x = { int a, int b -> }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure5() {
        assertConversion(
            'def x(int a, int b)  { fdafsd }',
            'def x = { int a, int b -> fdafsd }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure6() {
        assertConversion(
            'def x(int a, int b)\n { fdafsd }',
            'def x = { int a, int b -> fdafsd }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure7() {
        assertConversion(
            'def x(int a, int b   )\n { fdafsd }',
            'def x = { int a, int b    -> fdafsd }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure8() {
        assertConversion(
            'def x   (int a, int b   )\n { fdafsd }',
            'def x    = { int a, int b    -> fdafsd }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure9() {
        assertConversion(
            'def x(int a, int b)  {\n  fdsafds }',
            'def x = { int a, int b ->\n  fdsafds }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure10() {
        assertConversion(
            'def xxxx(int a, int b)  {\n  fdsafds }',
            'def xxxx = { int a, int b ->\n  fdsafds }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToClosure11() {
        assertConversion(
            'def "xx  xx"(int a, int b)  {\n  fdsafds }',
            'def "xx  xx" = { int a, int b ->\n  fdsafds }',
            'x', new ConvertMethodDefToClosureProposal())
    }

    @Test
    void testConvertToMethod1() {
        assertProposalNotOffered(
            'class X { def x = 1 }',
            15, 0, new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod2() {
        assertConversion(
            'class X { \ndef x = { } }',
            'class X { \ndef x() { } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod3() {
        assertConversion(
            'class X { \ndef x = { a ->  } }',
            'class X { \ndef x(a) {  } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod4() {
        assertConversion(
            'class X { \ndef x = {int a, int b -> } }',
            'class X { \ndef x(int a, int b) { } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod5() {
        assertConversion(
            'class X { \ndef x = {int a, int b -> fdafsd } }',
            'class X { \ndef x(int a, int b) { fdafsd } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod6() {
        assertConversion(
            'class X { \ndef x = {int a, int b -> fdafsd } }',
            'class X { \ndef x(int a, int b) { fdafsd } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod7() {
        assertConversion(
            'class X { \ndef x = {int a, int b   -> fdafsd } }',
            'class X { \ndef x(int a, int b) { fdafsd } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod8() {
        assertConversion(
            'class X { \ndef x    = {    int a, int b   -> fdafsd } }',
            'class X { \ndef x(int a, int b) { fdafsd } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod9() {
        assertConversion(
            'class X { \ndef x = {int a, int b\n ->\n  fdsafds } }',
            'class X { \ndef x(int a, int b) {\n  fdsafds } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod10() {
        assertConversion(
            'class X { \ndef xxxx = {int a, int b -> \n  fdsafds } }',
            'class X { \ndef xxxx(int a, int b) { \n  fdsafds } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToMethod11() {
        assertConversion(
            'class X { \ndef xxxx = {int a, int b ->\n  fdsafds } }',
            'class X { \ndef xxxx(int a, int b) {\n  fdsafds } }',
            'x', new ConvertClosureDefToMethodProposal())
    }

    @Test
    void testConvertToProperty1() {
        assertProposalNotOffered(
            '"".length()',
            4, 0, new ConvertAccessorToPropertyProposal())
    }

    @Test
    void testConvertToProperty2() {
        assertProposalNotOffered(
            '[].set(1, null)',
            4, 0, new ConvertAccessorToPropertyProposal())
    }

    @Test
    void testConvertToProperty3() {
        assertConversion(
            '"".isEmpty()',
            '"".empty',
            4, 0, new ConvertAccessorToPropertyProposal())
    }

    @Test
    void testConvertToProperty4() {
        assertConversion(
            '"".getBytes()',
            '"".bytes',
            4, 0, new ConvertAccessorToPropertyProposal())
    }

    @Test
    void testConvertToProperty5() {
        assertProposalNotOffered(
            '"".getBytes("UTF-8")',
            4, 0, new ConvertAccessorToPropertyProposal())
    }

    @Test
    void testConvertToProperty6() {
        assertConversion(
            'new Date().setTime(1L);',
            'new Date().time = 1L;',
            'set', new ConvertAccessorToPropertyProposal())
    }

    @Test
    void testConvertToProperty6a() {
        setJavaPreference(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, JavaCore.DO_NOT_INSERT)
        try {
            assertConversion(
                'new Date().setTime(1L);',
                'new Date().time= 1L;',
                'set', new ConvertAccessorToPropertyProposal())
        } finally {
            setJavaPreference(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, JavaCore.INSERT)
        }
    }

    @Test
    void testConvertToProperty7() {
        assertConversion('''\
            |class Foo {
            |  def bar
            |  void test() {
            |    getBar()
            |  }
            |}
            |'''.stripMargin(), '''\
            |class Foo {
            |  def bar
            |  void test() {
            |    bar
            |  }
            |}
            |'''.stripMargin(),
            'getBar', new ConvertAccessorToPropertyProposal())
    }

    @Test
    void testConvertToProperty8() {
        assertConversion('''\
            |class Foo {
            |  static getBar() {}
            |  static test() {
            |    getBar()
            |  }
            |}
            |'''.stripMargin(), '''\
            |class Foo {
            |  static getBar() {}
            |  static test() {
            |    bar
            |  }
            |}
            |'''.stripMargin(),
            'getBar', new ConvertAccessorToPropertyProposal())
    }

    @Test
    void testConvertToMultiLine1() {
        def assertConversion = { String pre, String post ->
            assertConversion("'${pre}'", "'''${post}'''", 0, 0, new ConvertToMultiLineStringProposal())
        }
        assertConversion('a', 'a')
        assertConversion('.', '.')
        assertConversion('$', '$')
        assertConversion('\\"', '"')
        assertConversion('\\\'', '\\\'') // leading and trailing single-quote is special case
        assertConversion('\\\' ', '\\\' ')
        assertConversion(' \\\'', ' \\\'')
        assertConversion(' \\\' ', ' \' ')
        assertConversion('\\t', '\t')
        assertConversion('\\n', '\n')
        assertConversion('\\r', '\\r')
        assertConversion('\\f', '\\f')
        assertConversion('\\b', '\\b')
        assertConversion('\\\\', '\\')
        assertConversion('\u00A7', '\u00A7')
      //assertConversion('\\u00A7', '\\u00A7') -- bug in AntlrParserPlugin/UnicodeEscapingReader; start of literal is wrong
    }

    @Test
    void testConvertToMultiLine2() {
        assertConversion(
            '\'fadfsad\\n\\t\\\' "\\nggggg\'',
            '\'\'\'fadfsad\n\t\' "\nggggg\'\'\'',
            'f', new ConvertToMultiLineStringProposal())
    }

    @Test
    void testConvertToMultiLine3() {
        assertConversion(
            'int a,b,c; def eq = "$a is $b plus ${c}";',
            'int a,b,c; def eq = """$a is $b plus ${c}""";',
            20, 20, new ConvertToMultiLineStringProposal())
    }

    @Test
    void testConvertToMultiLine4() {
        assertConversion(
            '\'$1\' + " $i \\n"',
            '"""\\$1 $i \n"""',
            0, 15, new ConvertToMultiLineStringProposal())
    }

    @Test
    void testConvertToMultiLine5() {
        assertConversion(
            '\'"\' + i + \'"\'', // '"' + i + '"'
            '"""\\"${i}\\""""', // """\"${i}\""""
            0, 13, new ConvertToMultiLineStringProposal())
    }

    @Test
    void testConvertToMultiLine6() {
        assertConversion(
            $/"' ''' '"/$,
            $/'''\' \'\'\' \''''/$,
            0, 9, new ConvertToMultiLineStringProposal())
    }

    @Test
    void testConvertToMultiLine7() {
        assertConversion(
            '\'one \' + { -> 2 } + " $three"',
            '"""one ${ -> 2 } $three"""',
            0, 29, new ConvertToMultiLineStringProposal())
    }

    @Test
    void testConvertToMultiLine8() {
        assertConversion('''\
            |'A\\n' +
            |"B\\n" +
            |"""C\\n""" +
            |D
            |'''.stripMargin(), '''\
            |"""A
            |B
            |C
            |${D}"""
            |'''.stripMargin(), 0, 29, new ConvertToMultiLineStringProposal())
    }

    @Test
    void testConvertToSingleLine1() {
        assertConversion(
            '"""fadfsad\n\t\' "\nggggg"""',
            '"fadfsad\\n\\t\' \\"\\nggggg"',
            'f', new ConvertToSingleLineStringProposal())
    }

    @Test
    void testConvertToSingleLine2() {
        assertConversion(
            '\'\'\'fadfsad\n\t\' "\nggggg\'\'\'',
            '\'fadfsad\\n\\t\\\' "\\nggggg\'',
            'f', new ConvertToSingleLineStringProposal())
    }

    @Test
    void testRemoveSemicolons1() {
        assertConversion(
            'def a = 1;',
            'def a = 1',
            null, new RemoveSpuriousSemicolonsProposal())
    }

    @Test
    void testRemoveSemicolons2() {
        assertConversion(
            'def z = 1;def a = 1;',
            'def z = 1;def a = 1',
            null, new RemoveSpuriousSemicolonsProposal())
    }

    @Test
    void testReplaceDef1() {
        assertConversion(
            'int bar = 1; def foo = bar',
            'int bar = 1; int foo = bar',
            13, 0, new ReplaceDefWithStaticTypeProposal())
    }

    @Test
    void testReplaceDef2() {
        assumeTrue(isParrotParser())

        assertConversion(
            'int bar = 1; var foo = bar',
            'int bar = 1; int foo = bar',
            13, 0, new ReplaceDefWithStaticTypeProposal())
    }

    // TODO: retain comments, modifiers, and annotations

    @Test
    void testReplaceDef3() {
        assertConversion(
            'def bar = 1g; def foo = bar',
            'def bar = 1g; BigInteger foo = bar',
            14, 3, new ReplaceDefWithStaticTypeProposal())
    }

    @Test
    void testReplaceDef4() {
        assertConversion(
            'def bar = [1g, 2g]; def foo = bar',
            'def bar = [1g, 2g]; List<BigInteger> foo = bar',
            20, 3, new ReplaceDefWithStaticTypeProposal())
    }

    @Test
    void testReplaceDef5() {
        assertConversion(
            'def bar = [1g, 2g] as BigInteger[]; def foo = bar',
            'def bar = [1g, 2g] as BigInteger[]; BigInteger[] foo = bar',
            36, 3, new ReplaceDefWithStaticTypeProposal())
    }

    @Test
    void testReplaceDef6() {
        assertProposalNotOffered(
            'int bar = 1; def foo = bar',
            17, 0, new ReplaceDefWithStaticTypeProposal())
    }

    @Test
    void testReplaceDef7() {
        assertProposalNotOffered(
            'def method() { return null }',
            0, 3, new ReplaceDefWithStaticTypeProposal())
    }

    @Test
    void testSwapOperands1() {
        assertConversion(
            'if (c && ba) { }',
            'if (ba && c) { }',
            7, 1, new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSwapOperands2() {
        assertConversion(
            'if (c && ba && hello) { }',
            'if (hello && c && ba) { }',
            13, 1, new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSwapOperands3() {
        assertConversion(
            'if (c && ba && hello) { }',
            'if (ba && c && hello) { }',
            7, 1, new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSwapOperands4() {
        assertConversion(
            'if (c && (ba && hello)) { }',
            'if ((ba && hello) && c) { }',
            7, 1, new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSwapOperands5() {
        assertConversion(
            'def r = ba == c.q.q.q.q == ddd',
            'def r = ddd == ba == c.q.q.q.q',
            25, 1, new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSwapOperands6() {
        assertConversion(
            'def r = ba == c.q.q.q.q == ddd',
            'def r = c.q.q.q.q == ba == ddd',
            12, 1, new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSwapOperands7() {
        assertConversion(
            'v  && g && a',
            'g  && v && a',
            '  &&', new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSwapOperands8() {
        assertConversion(
            'g  || a && v',
            'g  || v && a',
            '&&', new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSwapOperands9() {
        assumeTrue(isParrotParser())

        assertConversion(
            'g  || a === v',
            'g  || v === a',
            '===', new SwapLeftAndRightOperandsProposal())

        assertConversion(
            'g  || a !== v',
            'g  || v !== a',
            '!==', new SwapLeftAndRightOperandsProposal())
    }

    @Test
    void testSplitAssignment1() {
        assertConversion(
            'def foo = 1 + 4\n',
            'def foo\nfoo = 1 + 4\n',
            '=', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment2() {
        assertConversion(
            'def foo = 1 + 4\n',
            'def foo\nfoo = 1 + 4\n',
            'def foo = 1 + 4', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment3() {
        assertConversion(
            'String foo = "1 + 4"\n',
            'String foo\nfoo = "1 + 4"\n',
            '=', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment4() {
        assertConversion(
            'def foo  =  1 + 4\n',
            'def foo\nfoo  =  1 + 4\n',
            'def foo  =  1 + 4', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment5() {
        assertConversion(
            'def foo  =  1 + 4\n',
            'def foo\nfoo  =  1 + 4\n',
            '=', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment6() {
        assertConversion(
            '/*something*/ def foo = 1 + 4\n',
            '/*something*/ def foo\nfoo = 1 + 4\n',
            '=', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment7() {
        assertConversion(
            '/*something*/ def foo = 1 + 4\n',
            '/*something*/ def foo\nfoo = 1 + 4\n',
            'def foo = 1 + 4', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment8() {
        assertConversion(
            'def z = b = 8\n',
            'def z\nz = b = 8\n',
            'def z = b = 8', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment9() {
        String original = '''\
            |class Foo {
            |\tdef foo() {
            |\t\tdef bar = 1 + 4
            |\t}
            |}
            |'''.stripMargin()

        String expected = '''\
            |class Foo {
            |\tdef foo() {
            |\t\tdef bar
            |\t\tbar = 1 + 4
            |\t}
            |}
            |'''.stripMargin()

        assertConversion(original, expected, 'def bar = 1 + 4', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testSplitAssignment10() {
        String original = '''\
            |class X {
            |  def foo() {
            |    def x = 1
            |  }
            |}
            |'''.stripMargin()

        String expected = '''\
            |class X {
            |  def foo() {
            |    def x
            |    x = 1
            |  }
            |}
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new SplitVariableDeclAndInitProposal())
    }

    @Test
    void testInlineLocalVariable1() {
        String original = '''\
            |def x = 1
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |1.intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable2() {
        String original = '''\
            |def x = 1;;
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |1.intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable3() {
        String original = '''\
            |def x = 1\t\t
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |1.intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable4() {
        String original = '''\
            |def x = 1 // comment
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |// comment
            |1.intValue()
            |'''.stripMargin()

        if (!isParrotParser()) {
            expected = '''\
                |1.intValue()
                |'''.stripMargin()
        }

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable5() {
        String original = '''\
            |def x = 1 + 1
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(1 + 1).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable6() {
        String original = '''\
            |def x = (1 + 1)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(1 + 1).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable7() {
        String original = '''\
            |def x = (1) + (1)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((1) + (1)).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable8() {
        String original = '''\
            |def x = a.b.c()
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |a.b.c().intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable9() {
        String original = '''\
            |def x = (a.b).c()
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(a.b).c().intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable10() {
        String original = '''\
            |def x = (a.b) + c()
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((a.b) + c()).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable11() {
        String original = '''\
            |def x = { -> }
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |{ -> }.intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable12() {
        String original = '''\
            |def x = y[0]
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |y[0].intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable13() {
        String original = '''\
            |def x = (Object) y
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((Object) y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable14() {
        String original = '''\
            |def x = ((Object) y)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((Object) y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable15() {
        String original = '''\
            |def x = y as Object
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y as Object).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable16() {
        String original = '''\
            |def x = (y as Object)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y as Object).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable17() {
        String original = '''\
            |def x = y ?: 0
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y ?: 0).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable18() {
        String original = '''\
            |def x = (y ?: 0)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y ?: 0).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable19() {
        String original = '''\
            |def x = y ? 1 : 0
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y ? 1 : 0).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable20() {
        String original = '''\
            |def x = (y ? 1 : 0)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y ? 1 : 0).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable21() {
        String original = '''\
            |def x = (y) ? 1 : 0
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((y) ? 1 : 0).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable22() {
        String original = '''\
            |def x = y ? 1 : (0)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y ? 1 : (0)).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable23() {
        String original = '''\
            |def x = (y) ? (1) : (0)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((y) ? (1) : (0)).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable24() {
        String original = '''\
            |def x = y++
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y++).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable25() {
        String original = '''\
            |def x = (y++)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y++).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable26() {
        String original = '''\
            |def x = y--
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y--).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable27() {
        String original = '''\
            |def x = (y--)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(y--).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable28() {
        String original = '''\
            |def x = ++y
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(++y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable29() {
        String original = '''\
            |def x = (++y)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(++y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable30() {
        String original = '''\
            |def x = --y
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(--y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable31() {
        String original = '''\
            |def x = (--y)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(--y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable32() {
        String original = '''\
            |def x = !y
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(!y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable33() {
        String original = '''\
            |def x = (!y)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(!y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable34() {
        String original = '''\
            |def x = -y
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(-y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable35() {
        String original = '''\
            |def x = (-y)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(-y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable36() {
        String original = '''\
            |def x = +y
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(+y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable37() {
        String original = '''\
            |def x = (+y)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(+y).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable38() {
        String original = '''\
            |def x = ~/y/
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(~/y/).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable39() {
        String original = '''\
            |def x = (~/y/)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(~/y/).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable40() {
        String original = '''\
            |def x = 0..1
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(0..1).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable41() {
        String original = '''\
            |def x = (0)..1
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((0)..1).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable42() {
        String original = '''\
            |def x = 0..(1)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(0..(1)).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable43() {
        String original = '''\
            |def x = (0)..(1)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((0)..(1)).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable44() {
        String original = '''\
            |def x = (0..1)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(0..1).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable45() {
        String original = '''\
            |def x = ((0)..1)
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((0)..1).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable46() {
        String original = '''\
            |def x = (0..(1))
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(0..(1)).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable47() {
        String original = '''\
            |def x = ((0)..(1))
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |((0)..(1)).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable48() {
        String original = '''\
            |def x = a[1]
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |a[1].intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable49() {
        String original = '''\
            |def x = (a[1])
            |x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |(a[1]).intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable50() {
        String original = '''\
            |def x = 1
            |x.intValue()
            |def y = x.intValue()
            |'''.stripMargin()

        String expected = '''\
            |1.intValue()
            |def y = 1.intValue()
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable51() {
        String original = '''\
            |def x = 1
            |x.intValue()
            |if (x == 42) {
            |  def y = x.intValue()
            |  println y
            |} else {
            |  x.multiply(x)
            |}
            |'''.stripMargin()

        String expected = '''\
            |1.intValue()
            |if (1 == 42) {
            |  def y = 1.intValue()
            |  println y
            |} else {
            |  1.multiply(1)
            |}
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test
    void testInlineLocalVariable52() {
        String original = '''\
            |def x = 1
            |def y = x = 2
            |def sum = x + y
            |'''.stripMargin()

        String expected = '''\
            |def x = 1
            |def sum = x + (x = 2)
            |'''.stripMargin()

        assertConversion(original, expected, 'y', new InlineLocalVariableProposal())
    }

    @Test @NotYetImplemented
    void testInlineLocalVariable53() {
        String original = '''\
            |def x = 1, y = 2
            |def sum = x + y
            |'''.stripMargin()

        String expected = '''\
            |def y = 2
            |def sum = 1 + y
            |'''.stripMargin()

        assertConversion(original, expected, 'x', new InlineLocalVariableProposal())
    }

    @Test @NotYetImplemented
    void testInlineLocalVariable54() {
        String original = '''\
            |def x = 1, y = 2
            |def sum = x + y
            |'''.stripMargin()

        String expected = '''\
            |def x = 1
            |def sum = x + 2
            |'''.stripMargin()

        assertConversion(original, expected, 'y', new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable1() {
        String contents = '''\
            |def x
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable2() {
        String contents = '''\
            |def x = 1
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable3() {
        String contents = '''\
            |def x = 1
            |x += 1
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable4() {
        String contents = '''\
            |def x = 1
            |def y = x++
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable4a() {
        String contents = '''\
            |def x = 1
            |def y = x--
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable5() {
        String contents = '''\
            |def x = 1
            |def y = ++x
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable5a() {
        String contents = '''\
            |def x = 1
            |def y = --x
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable6() {
        String contents = '''\
            |def x = 1
            |print x
            |x = 2
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable7() {
        String contents = '''\
            |for (int x = 0; x < n; x += 1) {
            |}
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable8() {
        String contents = '''\
            |for (Iterator it = [].iterator(); it.hasNext();) {
            |  def item = it.next()
            |}
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('it'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable9() {
        String contents = '''\
            |try (def x = getClass().getResourceAsStream('')) {
            |}
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable10() {
        String contents = '''\
            |def meth(int x = 0) {
            |  x + 1
            |}
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable11() {
        String contents = '''\
            |class X {
            |  int x = 0
            |  def meth() {
            |    x + 1
            |  }
            |}
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testNoInlineLocalVariable12() {
        String contents = '''\
            |def (x, y) = [1, 2]
            |def sum = x + y
            |'''.stripMargin()
        assertProposalNotOffered(contents, contents.indexOf('x'), 1, new InlineLocalVariableProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable1() {
        assertConversion(
            'import java.awt.Point\n' + 'class Foo {\n' + '\tvoid bar(){\n' + 'new Point(1,2)\n' + '}}',
            'import java.awt.Point\n' + 'class Foo {\n' + '\tvoid bar(){\n' + 'def temp = new Point(1,2)\n' + '}}',
            'new Point', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable2() {
        assertConversion(
            'import java.awt.Point\n' + 'class Foo {\n' + '\tvoid bar(int a){\n' + 'bar(5)\n' + '}}',
            'import java.awt.Point\n' + 'class Foo {\n' + '\tvoid bar(int a){\n' + 'def bar = bar(5)\n' + '}}',
            'bar(5)', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable3() {
        assertConversion(
            'class Foo {\n' + '\tvoid bar(int a){\n' + '2 + 2\n' + '}}',
            'class Foo {\n' + '\tvoid bar(int a){\n' + 'def temp = 2 + 2\n' + '}}',
            '2 + 2', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable4() {
        assertConversion(
            'class Foo {\n' + '\tvoid bar(){\n' + 'false\n' + '}}',
            'class Foo {\n' + '\tvoid bar(){\n' + 'def false1 = false\n' + '}}',
            'false', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable5() {
        assertConversion(
            'class Foo {\n' + '\tvoid bar(){\n' + 'def false1 = true\n' + 'false\n' + '}}',
            'class Foo {\n' + '\tvoid bar(){\n' + 'def false1 = true\n' + 'def false2 = false\n' + '}}',
            'false\n', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable6() {
        assertConversion(
            'class Foo {\n' + '\tvoid bar(int a){\n' + '2\n' + '}}',
            'class Foo {\n' + '\tvoid bar(int a){\n' + 'def name = 2\n' + '}}',
            '2', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable7() {
        assertConversion(
            'class Foo {\n' + '\tvoid bar(int a){\n' + 'a == 2\n' + '}}',
            'class Foo {\n' + '\tvoid bar(int a){\n' + 'def temp = a == 2\n' + '}}',
            'a == 2', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable8() {
        assertConversion(
            'class Foo {\n' + '\tvoid bar(int a){\n' + '[1, 2]\n' + '}}',
            'class Foo {\n' + '\tvoid bar(int a){\n' + 'def list = [1, 2]\n' + '}}',
            '[1, 2]', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable9() {
        assertConversion(
            'class Foo {\n' + 'int bar(int a, int b){\n' + 'def aB\n' + 'a + b\n' + '}}',
            'class Foo {\n' + 'int bar(int a, int b){\n' + 'def aB\n' + 'def temp = a + b\n' + '}}',
            'a + b', new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable10() {
        assertConversion(
            'class Foo { def myClosure = { "foo".indexOf("qwerty") } }',
            'class Foo { def myClosure = { def indexOf = "foo".indexOf("qwerty") } }',
            '"foo".indexOf("qwerty")', new AssignStatementToNewLocalProposal())
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/393
    void testAssignStatementToNewLocalVariable11() {
        def source = 'class Foo { def bar() { return System.out } }'
        def target = 'System.out'
        int offset = source.indexOf(target)
        assertProposalNotOffered(source, offset, offset + target.length(), new AssignStatementToNewLocalProposal())
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1068
    void testAssignStatementToNewLocalVariable12() {
        def source = 'package p'
        assertProposalNotOffered(source, 0, 0, new AssignStatementToNewLocalProposal())
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1068
    void testAssignStatementToNewLocalVariable13() {
        def source = 'import groovy.lang.GroovyObject'
        assertProposalNotOffered(source, 0, 0, new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable14() {
        def source = 'package p\n\nimport groovy.lang.GroovyObject\n'
        assertProposalNotOffered(source, source.indexOf('import'), 0, new AssignStatementToNewLocalProposal())
    }

    @Test
    void testAssignStatementToNewLocalVariable15() {
        def source = 'package p\n\n@groovy.transform.Field Object o\n'
        assertProposalNotOffered(source, source.indexOf('@'), 0, new AssignStatementToNewLocalProposal())
    }

    @Test
    void testExtractToLocalVariable1() {
        assertConversion(
            ExtractLocalTestsData.getTest1In(),
            ExtractLocalTestsData.getTest1Out(),
            ExtractLocalTestsData.findLocation('foo + bar', 'test1'),
            'foo + bar'.length(), new ExtractToLocalProposal(true))
    }

    @Test
    void testExtractToLocalVariable2() {
        assertConversion(
            ExtractLocalTestsData.getTest2In(),
            ExtractLocalTestsData.getTest2Out(),
            ExtractLocalTestsData.findLocation('foo.bar', 'test2'),
            'foo.bar'.length(), new ExtractToLocalProposal(true))
    }

    @Test
    void testExtractToLocalVariable3() {
        assertConversion(
            ExtractLocalTestsData.getTest3In(),
            ExtractLocalTestsData.getTest3Out(),
            ExtractLocalTestsData.findLocation('baz.foo.&bar', 'test3'),
            'baz.foo.&bar'.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable4() {
        assertConversion(
            ExtractLocalTestsData.getTest4In(),
            ExtractLocalTestsData.getTest4Out(),
            ExtractLocalTestsData.findLocation('first + 1', 'test4'),
            'first + 1'.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable5() {
        assertConversion(
            ExtractLocalTestsData.getTest5In(),
            ExtractLocalTestsData.getTest5Out(),
            ExtractLocalTestsData.findLocation('foo + bar', 'test5'),
            'foo + bar'.length(), new ExtractToLocalProposal(true))
    }

    @Test
    void testExtractToLocalVariable6() {
        assertConversion(
            ExtractLocalTestsData.getTest6In(),
            ExtractLocalTestsData.getTest6Out(),
            ExtractLocalTestsData.findLocation('foo + bar', 'test6'),
            'foo + bar'.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable7() {
        assertConversion(
            ExtractLocalTestsData.getTest7In(),
            ExtractLocalTestsData.getTest7Out(),
            ExtractLocalTestsData.findLocation('foo + bar', 'test7'),
            'foo + bar'.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable8() {
        assertConversion(
            ExtractLocalTestsData.getTest8In(),
            ExtractLocalTestsData.getTest8Out(),
            ExtractLocalTestsData.findLocation('foo+  bar', 'test8'),
            'foo+  bar'.length(), new ExtractToLocalProposal(true))
    }

    @Test
    void testExtractToLocalVariable9() {
        assertConversion(
            ExtractLocalTestsData.getTest9In(),
            ExtractLocalTestsData.getTest9Out(),
            ExtractLocalTestsData.findLocation('map.one', 'test9'),
            'map.one'.length(), new ExtractToLocalProposal(true))
    }

    @Test
    void testExtractToLocalVariable10() {
        assertConversion(
            ExtractLocalTestsData.getTest10In(),
            ExtractLocalTestsData.getTest10Out(),
            ExtractLocalTestsData.findLocation('model.farInstance()', 'test10'),
            'model.farInstance()'.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable10a() {
        assertConversion(
            ExtractLocalTestsData.getTest10In(),
            ExtractLocalTestsData.getTest10Out(),
            ExtractLocalTestsData.findLocation('model.farInstance() ', 'test10'),
            'model.farInstance() '.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable10b() {
        assertConversion(
            ExtractLocalTestsData.getTest10In(),
            ExtractLocalTestsData.getTest10Out(),
            ExtractLocalTestsData.findLocation('model.farInstance()  ', 'test10'),
            'model.farInstance()  '.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable11() {
        assertConversion(
            ExtractLocalTestsData.getTest11In(),
            ExtractLocalTestsData.getTest11Out(),
            ExtractLocalTestsData.findLocation('println "here"', 'test11'),
            'println "here"'.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable12() {
        assertConversion(
            ExtractLocalTestsData.getTest12In(),
            ExtractLocalTestsData.getTest12Out(),
            ExtractLocalTestsData.findLocation('println "here"', 'test12'),
            'println "here"'.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToLocalVariable13() {
        assertConversion(
            ExtractLocalTestsData.getTest13In(),
            ExtractLocalTestsData.getTest13Out(),
            ExtractLocalTestsData.findLocation('a + b', 'test13'),
            'a + b'.length(), new ExtractToLocalProposal(false))
    }

    @Test
    void testExtractToConstant_1() {
        assertConversion(
            ExtractConstantTestsData.getTest1In(),
            ExtractConstantTestsData.getTest1Out(),
            ExtractConstantTestsData.findLocation('Foo + Bar', 'test1'),
            'Foo + Bar'.length(), new ExtractToConstantProposal(true))
    }

    @Test
    void testExtractToConstant_2() {
        assertConversion(
            ExtractConstantTestsData.getTest2In(),
            ExtractConstantTestsData.getTest2Out(),
            ExtractConstantTestsData.findLocation('Foo + Bar', 'test2'),
            'Foo + Bar'.length(), new ExtractToConstantProposal(true))
    }

    @Test
    void testExtractToConstant_3() {
        assertConversion(
            ExtractConstantTestsData.getTest3In(),
            ExtractConstantTestsData.getTest3Out(),
            ExtractConstantTestsData.findLocation('Foo+Bar+A.frax()', 'test3'),
            'Foo+Bar+A.frax()'.length(), new ExtractToConstantProposal(true))
    }

    @Test
    void testExtractToConstant_4() {
        assertConversion(
            ExtractConstantTestsData.getTest4In(),
            ExtractConstantTestsData.getTest4Out(),
            ExtractConstantTestsData.findLocation('Foo+Bar+A.frax()', 'test4'),
            'Foo+Bar+A.frax()'.length(), new ExtractToConstantProposal(true))
    }

    @Test
    void testExtractToConstant_5a() {
        assertConversion(
            ExtractConstantTestsData.getTest5aIn(),
            ExtractConstantTestsData.getTest5aOut(),
            ExtractConstantTestsData.findLocation('Foo+Bar+A.frax()', 'test5a'),
            'Foo+Bar+A.frax()'.length(), new ExtractToConstantProposal(true))
    }

    @Test
    void testExtractToConstant_6a() {
        assertConversion(
            ExtractConstantTestsData.getTest6aIn(),
            ExtractConstantTestsData.getTest6aOut(),
            ExtractConstantTestsData.findLocation('Foo+Bar+A.frax()', 'test6a'),
            'Foo+Bar+A.frax()'.length(), new ExtractToConstantProposal(true))
    }

    @Test
    void testExtractToConstant_7() {
        assertProposalNotOffered(
            ExtractConstantTestsData.getTest7In(),
            ExtractConstantTestsData.findLocation('Foo + Bar', 'test7'),
            'Foo + Bar'.length(), new ExtractToConstantProposal(false))
    }

    @Test
    void testExtractToConstant_8() {
        assertConversion(
            ExtractConstantTestsData.getTest8In(),
            ExtractConstantTestsData.getTest8Out(),
            ExtractConstantTestsData.findLocation('Foo + Bar', 'test8'),
            'Foo + Bar'.length(), new ExtractToConstantProposal(false))
    }

    @Test
    void testExtractToConstant_NoReplaceOccurrences1() {
        assertConversion(
            ExtractConstantTestsData.getTestNoReplaceOccurrences1In(),
            ExtractConstantTestsData.getTestNoReplaceOccurrences1Out(),
            ExtractConstantTestsData.findLocation('Foo+Bar+A.frax()', 'testNoReplaceOccurrences1'),
            'Foo+Bar+A.frax()'.length(), new ExtractToConstantProposal(false))
    }

    @Test
    void testExtractToField_MethodToModule() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testMethodToModule')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_ClosureToModule() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testClosureToModule')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_DeclarationWithDef() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testDeclarationWithDef')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_DeclarationWithType() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testDeclarationWithType')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_Reference() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testReference')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_TupleDeclaration() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testTupleDeclaration')
        assertProposalNotOffered(testCase.input, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_Initialization() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testInitialization')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_FieldReference() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testFieldReference')
        assertProposalNotOffered(testCase.input, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_Exception() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testException')
        assertProposalNotOffered(testCase.input, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_Prefix() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testPrefix')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_MethodInvocation() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testMethodInvocation')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_ParameterList() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testParameterList')
        assertProposalNotOffered(testCase.input, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_ArgumentList() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testArgumentList')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_InnerClass() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testInnerClass')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_FakeField() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testFakeField')
        assertConversion(testCase.input, testCase.expected, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    @Test
    void testExtractToField_ClosureParameterList() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.testCases.get('testClosureParameterList')
        assertProposalNotOffered(testCase.input, testCase.selectionOffset, testCase.selectionLength, new ConvertVariableToFieldProposal())
    }

    //

    private void assertConversion(String original, String expected, String target, GroovyQuickAssistProposal proposal) {
        int offset = (target == null ? 0 : original.lastIndexOf(target)),
            length = (target == null ? 0 : target.length())
        assertConversion(original, expected, offset, length, proposal)
    }

    private void assertConversion(String original, String expected, int offset, int length, GroovyQuickAssistProposal proposal) {
        GroovyQuickAssistContext context = new GroovyQuickAssistContext(new AssistContext(addGroovySource(original), offset, length))
        assertTrue("Expected proposal '${ -> proposal.displayString }' to be relevant", proposal.withContext(context).relevance > 0)
        IDocument document = context.newTempDocument(); proposal.apply(document)
        assertEquals('Invalid application of quick assist', expected, document.get())
    }

    private void assertProposalNotOffered(String original, int offset, int length, GroovyQuickAssistProposal proposal) {
        GroovyQuickAssistContext context = new GroovyQuickAssistContext(new AssistContext(addGroovySource(original), offset, length))
        assertFalse("Expected proposal '${ -> proposal.displayString }' to be irrelevant", proposal.withContext(context).relevance > 0)
    }
}
