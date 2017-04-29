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
package org.codehaus.groovy.eclipse.quickfix.test

import org.codehaus.groovy.eclipse.quickassist.AbstractGroovyCompletionProposal
import org.codehaus.groovy.eclipse.quickassist.AssignStatementToNewLocalProposal
import org.codehaus.groovy.eclipse.quickassist.ConvertLocalToFieldProposal
import org.codehaus.groovy.eclipse.quickassist.ConvertToClosureCompletionProposal
import org.codehaus.groovy.eclipse.quickassist.ConvertToMethodCompletionProposal
import org.codehaus.groovy.eclipse.quickassist.ConvertToMultiLineStringCompletionProposal
import org.codehaus.groovy.eclipse.quickassist.ConvertToSingleLineStringCompletionProposal
import org.codehaus.groovy.eclipse.quickassist.ExtractToConstantProposal
import org.codehaus.groovy.eclipse.quickassist.ExtractToLocalProposal
import org.codehaus.groovy.eclipse.quickassist.RemoveUnnecessarySemicolonsCompletionProposal
import org.codehaus.groovy.eclipse.quickassist.SplitAssigmentCompletionProposal
import org.codehaus.groovy.eclipse.quickassist.SwapOperandsCompletionProposal
import org.codehaus.groovy.eclipse.refactoring.test.extract.ConvertLocalToFieldTestsData
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractConstantTestsData
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractLocalTestsData
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.internal.ui.text.correction.AssistContext
import org.eclipse.jdt.ui.text.java.IInvocationContext
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.junit.Test

/**
 * Tests for {@link ConvertToMethodCompletionProposal}.
 */
final class QuickAssistTests extends QuickFixTestSuite {

    @Test
    void testConvertToClosure1() {
        assertConversion(
            "def x()  { }",
            "def x = { }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure2() {
        assertConversion(
            "class X { \ndef x()  { } }",
            "class X { \ndef x = { } }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure3() {
        assertConversion(
            "def x(a)  { }",
            "def x = { a -> }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure4() {
        assertConversion(
            "def x(int a, int b)  { }",
            "def x = { int a, int b -> }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure5() {
        assertConversion(
            "def x(int a, int b)  { fdafsd }",
            "def x = { int a, int b -> fdafsd }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure6() {
        assertConversion(
            "def x(int a, int b)\n { fdafsd }",
            "def x = { int a, int b -> fdafsd }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure7() {
        assertConversion(
            "def x(int a, int b   )\n { fdafsd }",
            "def x = { int a, int b    -> fdafsd }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure8() {
        assertConversion(
            "def x   (int a, int b   )\n { fdafsd }",
            "def x    = { int a, int b    -> fdafsd }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure9() {
        assertConversion(
            "def x(int a, int b)  {\n  fdsafds }",
            "def x = { int a, int b ->\n  fdsafds }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure10() {
        assertConversion(
            "def xxxx(int a, int b)  {\n  fdsafds }",
            "def xxxx = { int a, int b ->\n  fdsafds }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test
    void testConvertToClosure11() {
        assertConversion(
            "def \"xx  xx\"(int a, int b)  {\n  fdsafds }",
            "def \"xx  xx\" = { int a, int b ->\n  fdsafds }",
            "x",
            ConvertToClosureCompletionProposal)
    }

    @Test // convert to method must be wrapped inside of a class declaration
    void testConvertToMethod1() {
        assertConversion(
            "class X { \ndef x = { } }",
            "class X { \ndef x() { } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod3() {
        assertConversion(
            "class X { \ndef x = { a ->  } }",
            "class X { \ndef x(a) {  } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod4() {
        assertConversion(
            "class X { \ndef x = {int a, int b -> } }",
            "class X { \ndef x(int a, int b) { } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod5() {
        assertConversion(
            "class X { \ndef x = {int a, int b -> fdafsd } }",
            "class X { \ndef x(int a, int b) { fdafsd } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod6() {
        assertConversion(
            "class X { \ndef x = {int a, int b -> fdafsd } }",
            "class X { \ndef x(int a, int b) { fdafsd } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod7() {
        assertConversion(
            "class X { \ndef x = {int a, int b   -> fdafsd } }",
            "class X { \ndef x(int a, int b) { fdafsd } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod8() {
        assertConversion(
            "class X { \ndef x    = {    int a, int b   -> fdafsd } }",
            "class X { \ndef x(int a, int b) { fdafsd } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod9() {
        assertConversion(
            "class X { \ndef x = {int a, int b\n ->\n  fdsafds } }",
            "class X { \ndef x(int a, int b) {\n  fdsafds } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod10() {
        assertConversion(
            "class X { \ndef xxxx = {int a, int b -> \n  fdsafds } }",
            "class X { \ndef xxxx(int a, int b) { \n  fdsafds } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMethod11() {
        assertConversion(
            "class X { \ndef xxxx = {int a, int b ->\n  fdsafds } }",
            "class X { \ndef xxxx(int a, int b) {\n  fdsafds } }",
            "x",
            ConvertToMethodCompletionProposal)
    }

    @Test
    void testConvertToMultiLine1() {
        assertConversion(
            "\"fadfsad\\n\\t' \\\"\\nggggg\"",
            "\"\"\"fadfsad\n\t' \"\nggggg\"\"\"",
            "f",
            ConvertToMultiLineStringCompletionProposal)
    }

    @Test
    void testConvertToMultiLine2() {
        assertConversion(
            "'fadfsad\\n\\t\\' \"\\nggggg'",
            "'''fadfsad\n\t' \"\nggggg'''",
            "f",
            ConvertToMultiLineStringCompletionProposal)
    }

    @Test
    void testConvertToSingleLine1() {
        assertConversion(
            "\"\"\"fadfsad\n\t' \"\nggggg\"\"\"",
            "\"fadfsad\\n\\t' \\\"\\nggggg\"",
            "f",
            ConvertToSingleLineStringCompletionProposal)
    }

    @Test
    void testConvertToSingleLine2() {
        assertConversion(
            "'''fadfsad\n\t' \"\nggggg'''",
            "'fadfsad\\n\\t\\' \"\\nggggg'",
            "f",
            ConvertToSingleLineStringCompletionProposal)
    }

    @Test
    void testRemoveUnnecessarySemicolons1() {
        String original = "def a = 1;"
        assertConversion(
            original,
            "def a = 1",
            original,
            RemoveUnnecessarySemicolonsCompletionProposal)
    }

    @Test
    void testRemoveUnnecessarySemicolons2() {
        String original = "def z = 1;def a = 1;"
        String expected = "def z = 1;def a = 1"
        assertConversion(original, expected, null,
            RemoveUnnecessarySemicolonsCompletionProposal)
    }

    @Test
    void testSwapOperands1() {
        assertConversion("if (c && ba) { }", "if (ba && c) { }", 7, 1,
                SwapOperandsCompletionProposal)
    }

    @Test
    void testSwapOperands2() {
        assertConversion("if (c && ba && hello) { }",
                "if (hello && c && ba) { }", 13, 1,
                SwapOperandsCompletionProposal)
    }

    @Test
    void testSwapOperands3() {
        assertConversion("if (c && ba && hello) { }",
                "if (ba && c && hello) { }", 7, 1,
                SwapOperandsCompletionProposal)
    }

    @Test
    void testSwapOperands4() {
        assertConversion("if (c && (ba && hello)) { }",
                "if ((ba && hello) && c) { }", 7, 1,
                SwapOperandsCompletionProposal)
    }

    @Test
    void testSwapOperands5() {
        assertConversion("def r = ba == c.q.q.q.q == ddd",
                "def r = ddd == ba == c.q.q.q.q", 25, 1,
                SwapOperandsCompletionProposal)
    }

    @Test
    void testSwapOperands6() {
        assertConversion("def r = ba == c.q.q.q.q == ddd",
                "def r = c.q.q.q.q == ba == ddd", 12, 1,
                SwapOperandsCompletionProposal)
    }

    @Test
    void testSwapOperands7() {
        assertConversion("v  && g && a", "g  && v && a", "&&",
                SwapOperandsCompletionProposal)
    }

    @Test
    void testSwapOperands8() {
        assertConversion("g  || a && v", "g  || v && a", "&&",
                SwapOperandsCompletionProposal)
    }

    @Test
    void testSplitAssignment1() {
        assertConversion("def foo = 1 + 4", "def foo\n" + "foo = 1 + 4", "=",
                SplitAssigmentCompletionProposal)
    }

    @Test
    void testSplitAssignment2() {
        assertConversion("def foo = 1 + 4", "def foo\n" + "foo = 1 + 4",
                "def foo = 1 + 4", SplitAssigmentCompletionProposal)
    }

    @Test
    void testSplitAssignment3() {
        assertConversion("String foo = '1 + 4'", "String foo\n"
                + "foo = '1 + 4'", "=", SplitAssigmentCompletionProposal)
    }

    @Test
    void testSplitAssignment4() {
        assertConversion("def foo  =  1 + 4", "def foo\n" + "foo  =  1 + 4",
                "def foo  =  1 + 4", SplitAssigmentCompletionProposal)
    }

    @Test
    void testSplitAssignment5() {
        assertConversion("def foo  =  1 + 4", "def foo\n" + "foo  =  1 + 4",
                "=", SplitAssigmentCompletionProposal)
    }

    @Test
    void testSplitAssignment6() {
        assertConversion("/*something*/ def foo = 1 + 4",
                "/*something*/ def foo\n" + "foo = 1 + 4", "=",
                SplitAssigmentCompletionProposal)
    }

    @Test
    void testSplitAssignment7() {
        assertConversion("/*something*/ def foo = 1 + 4",
                "/*something*/ def foo\n" + "foo = 1 + 4", "def foo = 1 + 4",
                SplitAssigmentCompletionProposal)
    }

    @Test
    void testSplitAssignment7a() {
        assertConversion("def z = b = 8", "def z\n" + "z = b = 8",
                "def z = b = 8", SplitAssigmentCompletionProposal)
    }

    @Test
    void testSplitAssignment8() {
        assertConversion(
        // original
        "class Foo {\n" + "\n" + "	def foo() {\n" + "		def bar = 1 + 4\n" + "	}\n" + "}",
        // expect:
        "class Foo {\n" + "\n" + "	def foo() {\n" + "		def bar\n" + "		bar = 1 + 4\n" + "	}\n" + "}",
        // Selection:
        "def bar = 1 + 4",
        SplitAssigmentCompletionProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring1() {
        assertConversion("import java.awt.Point\n" + "class Foo {\n"
                + "	void bar(){\n" + "new Point(1,2)\n" + "}}",
                "import java.awt.Point\n" + "class Foo {\n" + "	void bar(){\n"
                        + "def temp = new Point(1,2)\n" + "}}", "new Point",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring2() {
        assertConversion("import java.awt.Point\n" + "class Foo {\n"
                + "	void bar(int a){\n" + "bar(5)\n" + "}}",
                "import java.awt.Point\n" + "class Foo {\n"
                        + "	void bar(int a){\n" + "def bar = bar(5)\n" + "}}",
                "bar(5)", AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring3() {
        assertConversion("class Foo {\n" + "	void bar(int a){\n" + "2 + 2\n"
                + "}}", "class Foo {\n" + "	void bar(int a){\n"
                + "def temp = 2 + 2\n" + "}}", "2 + 2",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring4() {
        assertConversion("class Foo {\n" + "	void bar(){\n" + "false\n" + "}}",
                "class Foo {\n" + "	void bar(){\n" + "def false1 = false\n"
                        + "}}", "false",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring5() {
        assertConversion("class Foo {\n" + "	void bar(){\n"
                + "def false1 = true\n" + "false\n" + "}}", "class Foo {\n"
                + "	void bar(){\n" + "def false1 = true\n"
                + "def false2 = false\n" + "}}", "false\n",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring6() {
        assertConversion("class Foo {\n" + "	void bar(int a){\n" + "2\n"
                + "}}", "class Foo {\n" + "	void bar(int a){\n"
                + "def name = 2\n" + "}}", "2",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring7() {
        assertConversion("class Foo {\n" + "	void bar(int a){\n" + "a == 2\n"
                + "}}", "class Foo {\n" + "	void bar(int a){\n"
                + "def temp = a == 2\n" + "}}", "a == 2",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring8() {
        assertConversion("class Foo {\n" + "	void bar(int a){\n" + "[1, 2]\n"
                + "}}", "class Foo {\n" + "	void bar(int a){\n"
                + "def list = [1, 2]\n" + "}}", "[1, 2]",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementToLocalRefactoring9() {
        assertConversion("class Foo {\n" + "int bar(int a, int b){\n" + "def aB\n" + "a + b\n"
                + "}}", "class Foo {\n" + "int bar(int a, int b){\n"
                + "def aB\n" + "def temp = a + b\n" + "}}", "a + b",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testAssignStatementLocalRefactoring10() {
        assertConversion("class Foo {def myClosure = {'foo'.indexOf('qwerty')}}",
                "class Foo {def myClosure = {def indexOf = 'foo'.indexOf('qwerty')}}",
                "'foo'.indexOf('qwerty')",
                AssignStatementToNewLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_1() {
        assertConversionAllOccurrences(ExtractLocalTestsData.getTest1In(),
                ExtractLocalTestsData.getTest1Out(),
                ExtractLocalTestsData.findLocation("foo + bar", "test1"),
                "foo + bar".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_2() {
        assertConversionAllOccurrences(ExtractLocalTestsData.getTest2In(),
                ExtractLocalTestsData.getTest2Out(),
                ExtractLocalTestsData.findLocation("foo.bar", "test2"),
                "foo.bar".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_3() {
        assertConversion(ExtractLocalTestsData.getTest3In(),
                ExtractLocalTestsData.getTest3Out(),
                ExtractLocalTestsData.findLocation("baz.foo.&bar", "test3"),
                "baz.foo.&bar".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_4() {
        assertConversion(ExtractLocalTestsData.getTest4In(),
                ExtractLocalTestsData.getTest4Out(),
                ExtractLocalTestsData.findLocation("first + 1", "test4"),
                "first + 1".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_5() {
        assertConversionAllOccurrences(ExtractLocalTestsData.getTest5In(),
                ExtractLocalTestsData.getTest5Out(),
                ExtractLocalTestsData.findLocation("foo + bar", "test5"),
                "foo + bar".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_6() {
        assertConversion(ExtractLocalTestsData.getTest6In(),
                ExtractLocalTestsData.getTest6Out(),
                ExtractLocalTestsData.findLocation("foo + bar", "test6"),
                "foo + bar".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_7() {
        assertConversion(ExtractLocalTestsData.getTest7In(),
                ExtractLocalTestsData.getTest7Out(),
                ExtractLocalTestsData.findLocation("foo + bar", "test7"),
                "foo + bar".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_8() {
        assertConversionAllOccurrences(ExtractLocalTestsData.getTest8In(),
                ExtractLocalTestsData.getTest8Out(),
                ExtractLocalTestsData.findLocation("foo+  bar", "test8"),
                "foo+  bar".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_9() {
        assertConversionAllOccurrences(ExtractLocalTestsData.getTest9In(),
                ExtractLocalTestsData.getTest9Out(),
                ExtractLocalTestsData.findLocation("map.one", "test9"),
                "map.one".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_10() {
        assertConversion(ExtractLocalTestsData.getTest10In(),
                ExtractLocalTestsData.getTest10Out(),
                ExtractLocalTestsData.findLocation("model.farInstance()", "test10"),
                "model.farInstance()".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_10a() {
        assertConversion(ExtractLocalTestsData.getTest10In(),
                ExtractLocalTestsData.getTest10Out(),
                ExtractLocalTestsData.findLocation("model.farInstance() ", "test10"),
                "model.farInstance() ".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_10b() {
        assertConversion(ExtractLocalTestsData.getTest10In(),
                ExtractLocalTestsData.getTest10Out(),
                ExtractLocalTestsData.findLocation("model.farInstance()  ", "test10"),
                "model.farInstance()  ".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_11() {
        assertConversion(ExtractLocalTestsData.getTest11In(),
                ExtractLocalTestsData.getTest11Out(),
                ExtractLocalTestsData.findLocation("println \"here\"", "test11"),
                "println \"here\"".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_12() {
        assertConversion(ExtractLocalTestsData.getTest12In(),
                ExtractLocalTestsData.getTest12Out(),
                ExtractLocalTestsData.findLocation("println \"here\"", "test12"),
                "println \"here\"".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToLocalRefactoring_13() {
        assertConversion(ExtractLocalTestsData.getTest13In(),
                ExtractLocalTestsData.getTest13Out(),
                ExtractLocalTestsData.findLocation("a + b", "test13"),
                "a + b".length(), ExtractToLocalProposal)
    }

    @Test
    void testExtractToConstant_1() {
        assertConversionAllOccurrences(ExtractConstantTestsData.getTest1In(),
                ExtractConstantTestsData.getTest1Out(),
                ExtractConstantTestsData.findLocation("Foo + Bar", "test1"),
                "Foo + Bar".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToConstant_2() {
        assertConversionAllOccurrences(ExtractConstantTestsData.getTest2In(),
                ExtractConstantTestsData.getTest2Out(),
                ExtractConstantTestsData.findLocation("Foo + Bar", "test2"),
                "Foo + Bar".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToConstant_3() {
        assertConversionAllOccurrences(ExtractConstantTestsData.getTest3In(),
                ExtractConstantTestsData.getTest3Out(),
                ExtractConstantTestsData.findLocation("Foo+Bar+A.frax()", "test3"),
                "Foo+Bar+A.frax()".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToConstant_4() {
        assertConversionAllOccurrences(ExtractConstantTestsData.getTest4In(),
                ExtractConstantTestsData.getTest4Out(),
                ExtractConstantTestsData.findLocation("Foo+Bar+A.frax()", "test4"),
                "Foo+Bar+A.frax()".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToConstant_5a() {
        assertConversionAllOccurrences(ExtractConstantTestsData.getTest5aIn(),
                ExtractConstantTestsData.getTest5aOut(),
                ExtractConstantTestsData.findLocation("Foo+Bar+A.frax()", "test5a"),
                "Foo+Bar+A.frax()".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToConstant_6a() {
        assertConversionAllOccurrences(ExtractConstantTestsData.getTest6aIn(),
                ExtractConstantTestsData.getTest6aOut(),
                ExtractConstantTestsData.findLocation("Foo+Bar+A.frax()", "test6a"),
                "Foo+Bar+A.frax()".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToConstant_7() {
        assertProposalNotOffered(ExtractConstantTestsData.getTest7In(),
                ExtractConstantTestsData.findLocation("Foo + Bar", "test7"),
                "Foo + Bar".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToConstant_8() {
        assertConversion(ExtractConstantTestsData.getTest8In(),
                ExtractConstantTestsData.getTest8Out(),
                ExtractConstantTestsData.findLocation("Foo + Bar", "test8"),
                "Foo + Bar".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToConstant_NoReplaceOccurrences1() {
        assertConversion(ExtractConstantTestsData.getTestNoReplaceOccurrences1In(),
                ExtractConstantTestsData.getTestNoReplaceOccurrences1Out(),
                ExtractConstantTestsData.findLocation("Foo+Bar+A.frax()", "testNoReplaceOccurrences1"),
                "Foo+Bar+A.frax()".length(), ExtractToConstantProposal)
    }

    @Test
    void testExtractToField_MethodToModule() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testMethodToModule")
        assertProposalNotOffered(testCase.getInput(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_ClosureToModule() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosureToModule")
        assertProposalNotOffered(testCase.getInput(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_DeclarationWithDef() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testDeclarationWithDef")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_DeclarationWithType() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testDeclarationWithType")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_Reference() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testReference")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_TupleDeclaration() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testTupleDeclaration")
        assertProposalNotOffered(testCase.getInput(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_Initialization() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testInitialization")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_FieldReference() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testFieldReference")
        assertProposalNotOffered(testCase.getInput(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_Exception() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testException")
        assertProposalNotOffered(testCase.getInput(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_Prefix() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testPrefix")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_MethodInvocation() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testMethodInvocation")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_ParameterList() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testParameterList")
        assertProposalNotOffered(testCase.getInput(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_ArgumentList() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testArgumentList")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_InnerClass() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testInnerClass")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_FakeField() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testFakeField")
        assertConversion(testCase.getInput(), testCase.getExpected(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    @Test
    void testExtractToField_ClosureParameterList() {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosureParameterList")
        assertProposalNotOffered(testCase.getInput(),
                testCase.getSelectionOffset(), testCase.getSelectionLength(),
                ConvertLocalToFieldProposal)
    }

    //

    private void assertConversion(String original, String expected, String searchFor, Class<? extends AbstractGroovyCompletionProposal> proposalClass) {
        int start = (searchFor == null ? 0 : original.indexOf(searchFor))
        int length = (searchFor == null ? 0 : searchFor.length())
        assertConversion(original, expected, start, length, proposalClass)
    }

    private void assertConversion(String original, String expected, int offset, int length, Class<? extends AbstractGroovyCompletionProposal> proposalClass) {
        ICompilationUnit unit = addGroovySource(original)
        IInvocationContext context = new AssistContext(unit, offset, length)
        AbstractGroovyCompletionProposal proposal = proposalClass.getConstructor(IInvocationContext).newInstance(context)
        assert proposal.hasProposals() : "Expecting that proposals exist for '$proposal.displayString'"
        IDocument document = new Document(String.valueOf(unit.getContents()))
        proposal.apply(document)

        assert document.get() == expected : 'Invalid application of quick assist'
    }

    private void assertConversionAllOccurrences(String original, String expected, int offset, int length, Class<? extends AbstractGroovyCompletionProposal> proposalClass) {
        ICompilationUnit unit = addGroovySource(original)
        IInvocationContext context = new AssistContext(unit, offset, length)
        AbstractGroovyCompletionProposal proposal = proposalClass.getConstructor(IInvocationContext, boolean).newInstance(context, true)
        assert proposal.hasProposals() : "Expecting that proposals exist for '$proposal.displayString'"
        IDocument document = new Document(String.valueOf(unit.getContents()))
        proposal.apply(document)

        assert document.get() == expected : 'Invalid application of quick assist'
    }

    private void assertProposalNotOffered(String original, int offset, int length, Class<? extends AbstractGroovyCompletionProposal> proposalClass) {
        ICompilationUnit unit = addGroovySource(original)
        IInvocationContext context = new AssistContext(unit, offset, length)
        AbstractGroovyCompletionProposal proposal = proposalClass.getConstructor(IInvocationContext).newInstance(context)
        assert !proposal.hasProposals() : "Expecting that proposals not offered for '$proposal.displayString'"
    }
}
