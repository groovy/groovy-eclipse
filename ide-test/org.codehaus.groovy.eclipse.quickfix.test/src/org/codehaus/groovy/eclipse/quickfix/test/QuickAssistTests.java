/*
 * Copyright 2011, 2014 SpringSource, a division of Pivotal Software, Inc
 * 
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.eclipse.quickfix.test;

import java.lang.reflect.InvocationTargetException;

import org.codehaus.groovy.eclipse.quickassist.AbstractGroovyCompletionProposal;
import org.codehaus.groovy.eclipse.quickassist.AssignStatementToNewLocalProposal;
import org.codehaus.groovy.eclipse.quickassist.ConvertLocalToFieldProposal;
import org.codehaus.groovy.eclipse.quickassist.ConvertToClosureCompletionProposal;
import org.codehaus.groovy.eclipse.quickassist.ConvertToMethodCompletionProposal;
import org.codehaus.groovy.eclipse.quickassist.ConvertToMultiLineStringCompletionProposal;
import org.codehaus.groovy.eclipse.quickassist.ConvertToSingleLineStringCompletionProposal;
import org.codehaus.groovy.eclipse.quickassist.ExtractToConstantProposal;
import org.codehaus.groovy.eclipse.quickassist.ExtractToLocalProposal;
import org.codehaus.groovy.eclipse.quickassist.RemoveUnnecessarySemicolonsCompletionProposal;
import org.codehaus.groovy.eclipse.quickassist.SplitAssigmentCompletionProposal;
import org.codehaus.groovy.eclipse.quickassist.SwapOperandsCompletionProposal;
import org.codehaus.groovy.eclipse.refactoring.test.extract.ConvertLocalToFieldTestsData;
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractConstantTestData;
import org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractLocalTestsData;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * tests for the {@link ConvertToMethodCompletionProposal} class
 * 
 * @author Andrew Eisenberg
 * @created Oct 28, 2011
 */
@SuppressWarnings("restriction")
public class QuickAssistTests extends EclipseTestCase {

	public void testConvertToClosure1() throws Exception {
		assertConversion("def x()  { }", "def x = { }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure2() throws Exception {
		assertConversion("class X { \ndef x()  { } }",
				"class X { \ndef x = { } }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure3() throws Exception {
		assertConversion("def x(a)  { }", "def x = { a -> }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure4() throws Exception {
		assertConversion("def x(int a, int b)  { }",
				"def x = { int a, int b -> }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure5() throws Exception {
		assertConversion("def x(int a, int b)  { fdafsd }",
				"def x = { int a, int b -> fdafsd }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure6() throws Exception {
		assertConversion("def x(int a, int b)\n { fdafsd }",
				"def x = { int a, int b -> fdafsd }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure7() throws Exception {
		assertConversion("def x(int a, int b   )\n { fdafsd }",
				"def x = { int a, int b    -> fdafsd }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure8() throws Exception {
		assertConversion("def x   (int a, int b   )\n { fdafsd }",
				"def x    = { int a, int b    -> fdafsd }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure9() throws Exception {
		assertConversion("def x(int a, int b)  {\n  fdsafds }",
				"def x = { int a, int b ->\n  fdsafds }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure10() throws Exception {
		assertConversion("def xxxx(int a, int b)  {\n  fdsafds }",
				"def xxxx = { int a, int b ->\n  fdsafds }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	public void testConvertToClosure11() throws Exception {
		assertConversion("def \"xx  xx\"(int a, int b)  {\n  fdsafds }",
				"def \"xx  xx\" = { int a, int b ->\n  fdsafds }", "x",
				ConvertToClosureCompletionProposal.class);
	}

	// convert to method must be wrapped inside of a class declaration
	public void testConvertToMethod1() throws Exception {
		assertConversion("class X { \ndef x = { } }",
				"class X { \ndef x() { } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod3() throws Exception {
		assertConversion("class X { \ndef x = { a ->  } }",
				"class X { \ndef x(a) {  } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod4() throws Exception {
		assertConversion("class X { \ndef x = {int a, int b -> } }",
				"class X { \ndef x(int a, int b) { } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod5() throws Exception {
		assertConversion("class X { \ndef x = {int a, int b -> fdafsd } }",
				"class X { \ndef x(int a, int b) { fdafsd } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod6() throws Exception {
		assertConversion("class X { \ndef x = {int a, int b -> fdafsd } }",
				"class X { \ndef x(int a, int b) { fdafsd } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod7() throws Exception {
		assertConversion("class X { \ndef x = {int a, int b   -> fdafsd } }",
				"class X { \ndef x(int a, int b) { fdafsd } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod8() throws Exception {
		assertConversion(
				"class X { \ndef x    = {    int a, int b   -> fdafsd } }",
				"class X { \ndef x(int a, int b) { fdafsd } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod9() throws Exception {
		assertConversion(
				"class X { \ndef x = {int a, int b\n ->\n  fdsafds } }",
				"class X { \ndef x(int a, int b) {\n  fdsafds } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod10() throws Exception {
		assertConversion(
				"class X { \ndef xxxx = {int a, int b -> \n  fdsafds } }",
				"class X { \ndef xxxx(int a, int b) { \n  fdsafds } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMethod11() throws Exception {
		assertConversion(
				"class X { \ndef xxxx = {int a, int b ->\n  fdsafds } }",
				"class X { \ndef xxxx(int a, int b) {\n  fdsafds } }", "x",
				ConvertToMethodCompletionProposal.class);
	}

	public void testConvertToMultiLine1() throws Exception {
		assertConversion("\"fadfsad\\n\\t' \\\"\\nggggg\"",
				"\"\"\"fadfsad\n\t' \"\nggggg\"\"\"", "f",
				ConvertToMultiLineStringCompletionProposal.class);
	}

	public void testConvertToMultiLine2() throws Exception {
		assertConversion("'fadfsad\\n\\t\\' \"\\nggggg'",
				"'''fadfsad\n\t' \"\nggggg'''", "f",
				ConvertToMultiLineStringCompletionProposal.class);
	}

	public void testConvertToSingleLine1() throws Exception {
		assertConversion("\"\"\"fadfsad\n\t' \"\nggggg\"\"\"",
				"\"fadfsad\\n\\t' \\\"\\nggggg\"", "f",
				ConvertToSingleLineStringCompletionProposal.class);
	}

	public void testConvertToSingleLine2() throws Exception {
		assertConversion("'''fadfsad\n\t' \"\nggggg'''",
				"'fadfsad\\n\\t\\' \"\\nggggg'", "f",
				ConvertToSingleLineStringCompletionProposal.class);
	}

	public void testRemoveUnnecessarySemicolons1() throws Exception {
		String original = "def a = 1;";
		assertConversion(original, "def a = 1", original,
				RemoveUnnecessarySemicolonsCompletionProposal.class);
	}

	public void testRemoveUnnecessarySemicolons2() throws Exception {
		String original = "def z = 1;def a = 1;";
		String expected = "def z = 1;def a = 1";
		assertConversion(original, expected, null,
				RemoveUnnecessarySemicolonsCompletionProposal.class);
	}

	public void testSwapOperands1() throws Exception {
		assertConversion("if (c && ba) { }", "if (ba && c) { }", 7, 1,
				SwapOperandsCompletionProposal.class);
	}

	public void testSwapOperands2() throws Exception {
		assertConversion("if (c && ba && hello) { }",
				"if (hello && c && ba) { }", 13, 1,
				SwapOperandsCompletionProposal.class);
	}

	public void testSwapOperands3() throws Exception {
		assertConversion("if (c && ba && hello) { }",
				"if (ba && c && hello) { }", 7, 1,
				SwapOperandsCompletionProposal.class);
	}

	public void testSwapOperands4() throws Exception {
		assertConversion("if (c && (ba && hello)) { }",
				"if ((ba && hello) && c) { }", 7, 1,
				SwapOperandsCompletionProposal.class);
	}

	public void testSwapOperands5() throws Exception {
		assertConversion("def r = ba == c.q.q.q.q == ddd",
				"def r = ddd == ba == c.q.q.q.q", 25, 1,
				SwapOperandsCompletionProposal.class);
	}

	public void testSwapOperands6() throws Exception {
		assertConversion("def r = ba == c.q.q.q.q == ddd",
				"def r = c.q.q.q.q == ba == ddd", 12, 1,
				SwapOperandsCompletionProposal.class);
	}

	public void testSwapOperands7() throws Exception {
		assertConversion("v  && g && a", "g  && v && a", "&&",
				SwapOperandsCompletionProposal.class);
	}

	public void testSwapOperands8() throws Exception {
		assertConversion("g  || a && v", "g  || v && a", "&&",
				SwapOperandsCompletionProposal.class);
	}

	public void testSplitAssignment1() throws Exception {
		assertConversion("def foo = 1 + 4", "def foo\n" + "foo = 1 + 4", "=",
				SplitAssigmentCompletionProposal.class);
	}

	public void testSplitAssignment2() throws Exception {
		assertConversion("def foo = 1 + 4", "def foo\n" + "foo = 1 + 4",
				"def foo = 1 + 4", SplitAssigmentCompletionProposal.class);
	}

	public void testSplitAssignment3() throws Exception {
		assertConversion("String foo = '1 + 4'", "String foo\n"
				+ "foo = '1 + 4'", "=", SplitAssigmentCompletionProposal.class);
	}

	public void testSplitAssignment4() throws Exception {
		assertConversion("def foo  =  1 + 4", "def foo\n" + "foo  =  1 + 4",
				"def foo  =  1 + 4", SplitAssigmentCompletionProposal.class);
	}

	public void testSplitAssignment5() throws Exception {
		assertConversion("def foo  =  1 + 4", "def foo\n" + "foo  =  1 + 4",
				"=", SplitAssigmentCompletionProposal.class);
	}

	public void testSplitAssignment6() throws Exception {
		assertConversion("/*something*/ def foo = 1 + 4",
				"/*something*/ def foo\n" + "foo = 1 + 4", "=",
				SplitAssigmentCompletionProposal.class);
	}

	public void testSplitAssignment7() throws Exception {
		assertConversion("/*something*/ def foo = 1 + 4",
				"/*something*/ def foo\n" + "foo = 1 + 4", "def foo = 1 + 4",
				SplitAssigmentCompletionProposal.class);
	}

	public void testSplitAssignment7a() throws Exception {
		assertConversion("def z = b = 8", "def z\n" + "z = b = 8",
				"def z = b = 8", SplitAssigmentCompletionProposal.class);
	}

	public void testSplitAssignment8() throws Exception {
		assertConversion(
		// original
				"class Foo {\n" + "\n" + "	def foo() {\n"
						+ "		def bar = 1 + 4\n" + "	}\n" + "}",
				// expect:
				"class Foo {\n" + "\n" + "	def foo() {\n" + "		def bar\n"
						+ "		bar = 1 + 4\n" + "	}\n" + "}",
				// Selection:
				"def bar = 1 + 4", SplitAssigmentCompletionProposal.class);
	}

	public void testAssignStatementToLocalRefactoring1() throws Exception {
		assertConversion("import java.awt.Point\n" + "class Foo {\n"
				+ "	void bar(){\n" + "new Point(1,2)\n" + "}}",
				"import java.awt.Point\n" + "class Foo {\n" + "	void bar(){\n"
						+ "def temp = new Point(1,2)\n" + "}}", "new Point",
				AssignStatementToNewLocalProposal.class);
	}

	public void testAssignStatementToLocalRefactoring2() throws Exception {
		assertConversion("import java.awt.Point\n" + "class Foo {\n"
				+ "	void bar(int a){\n" + "bar(5)\n" + "}}",
				"import java.awt.Point\n" + "class Foo {\n"
						+ "	void bar(int a){\n" + "def bar = bar(5)\n" + "}}",
				"bar(5)", AssignStatementToNewLocalProposal.class);
	}

	public void testAssignStatementToLocalRefactoring3() throws Exception {
		assertConversion("class Foo {\n" + "	void bar(int a){\n" + "2 + 2\n"
				+ "}}", "class Foo {\n" + "	void bar(int a){\n"
				+ "def temp = 2 + 2\n" + "}}", "2 + 2",
				AssignStatementToNewLocalProposal.class);
	}

	public void testAssignStatementToLocalRefactoring4() throws Exception {
		assertConversion("class Foo {\n" + "	void bar(){\n" + "false\n" + "}}",
				"class Foo {\n" + "	void bar(){\n" + "def false1 = false\n"
						+ "}}", "false",
				AssignStatementToNewLocalProposal.class);
	}

	public void testAssignStatementToLocalRefactoring5() throws Exception {
		assertConversion("class Foo {\n" + "	void bar(){\n"
				+ "def false1 = true\n" + "false\n" + "}}", "class Foo {\n"
				+ "	void bar(){\n" + "def false1 = true\n"
				+ "def false2 = false\n" + "}}", "false\n",
				AssignStatementToNewLocalProposal.class);
	}

	public void testAssignStatementToLocalRefactoring6() throws Exception {
		assertConversion("class Foo {\n" + "	void bar(int a){\n" + "2\n"
				+ "}}", "class Foo {\n" + "	void bar(int a){\n"
				+ "def name = 2\n" + "}}", "2",
				AssignStatementToNewLocalProposal.class);
	}

	public void testAssignStatementToLocalRefactoring7() throws Exception {
		assertConversion("class Foo {\n" + "	void bar(int a){\n" + "a == 2\n"
				+ "}}", "class Foo {\n" + "	void bar(int a){\n"
				+ "def temp = a == 2\n" + "}}", "a == 2",
				AssignStatementToNewLocalProposal.class);
	}

	public void testAssignStatementToLocalRefactoring8() throws Exception {
		assertConversion("class Foo {\n" + "	void bar(int a){\n" + "[1, 2]\n"
				+ "}}", "class Foo {\n" + "	void bar(int a){\n"
				+ "def list = [1, 2]\n" + "}}", "[1, 2]",
				AssignStatementToNewLocalProposal.class);
	}
	
	public void testAssignStatementToLocalRefactoring9() throws Exception {
		assertConversion("class Foo {\n" + "int bar(int a, int b){\n" + "def aB\n" + "a + b\n"
				+ "}}", "class Foo {\n" + "int bar(int a, int b){\n"
				+ "def aB\n" + "def temp = a + b\n" + "}}", "a + b",
				AssignStatementToNewLocalProposal.class);
	}
	
	public void testAssignStatementLocalRefactoring10() throws Exception {
		assertConversion("class Foo {def myClosure = {'foo'.indexOf('qwerty')}}",
				"class Foo {def myClosure = {def indexOf = 'foo'.indexOf('qwerty')}}",
				"'foo'.indexOf('qwerty')",
				AssignStatementToNewLocalProposal.class);
	}
	
	public void test_ExtractToLocalRefactoring_1() throws Exception {
		assertConversionAllOccurrences(ExtractLocalTestsData.getTest1In(),
				ExtractLocalTestsData.getTest1Out(),
				ExtractLocalTestsData.findLocation("foo + bar", "test1"),
				"foo + bar".length(), ExtractToLocalProposal.class);
	}

	public void test_ExtractToLocalRefactoring_2() throws Exception {
		assertConversionAllOccurrences(ExtractLocalTestsData.getTest2In(),
				ExtractLocalTestsData.getTest2Out(),
				ExtractLocalTestsData.findLocation("foo.bar", "test2"),
				"foo.bar".length(), ExtractToLocalProposal.class);
	}

	public void test_ExtractToLocalRefactoring_3() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest3In(),
				ExtractLocalTestsData.getTest3Out(),
				ExtractLocalTestsData.findLocation("baz.foo.&bar", "test3"),
				"baz.foo.&bar".length(), ExtractToLocalProposal.class);
	}

	public void test_ExtractToLocalRefactoring_4() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest4In(),
				ExtractLocalTestsData.getTest4Out(),
				ExtractLocalTestsData.findLocation("first + 1", "test4"),
				"first + 1".length(), ExtractToLocalProposal.class);
	}

	public void test_ExtractToLocalRefactoring_5() throws Exception {
		assertConversionAllOccurrences(ExtractLocalTestsData.getTest5In(),
				ExtractLocalTestsData.getTest5Out(),
				ExtractLocalTestsData.findLocation("foo + bar", "test5"),
				"foo + bar".length(), ExtractToLocalProposal.class);
	}

	public void test_ExtractToLocalRefactoring_6() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest6In(),
				ExtractLocalTestsData.getTest6Out(),
				ExtractLocalTestsData.findLocation("foo + bar", "test6"),
				"foo + bar".length(), ExtractToLocalProposal.class);
	}

	public void test_ExtractToLocalRefactoring_7() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest7In(),
				ExtractLocalTestsData.getTest7Out(),
				ExtractLocalTestsData.findLocation("foo + bar", "test7"),
				"foo + bar".length(), ExtractToLocalProposal.class);
	}

	public void test_ExtractToLocalRefactoring_8() throws Exception {
		assertConversionAllOccurrences(ExtractLocalTestsData.getTest8In(),
				ExtractLocalTestsData.getTest8Out(),
				ExtractLocalTestsData.findLocation("foo+  bar", "test8"),
				"foo+  bar".length(), ExtractToLocalProposal.class);
	}

	public void test_ExtractToLocalRefactoring_9() throws Exception {
		assertConversionAllOccurrences(ExtractLocalTestsData.getTest9In(),
	            ExtractLocalTestsData.getTest9Out(),
	            ExtractLocalTestsData.findLocation("map.one", "test9"),
	            "map.one".length(), ExtractToLocalProposal.class);
	}
	
	public void test_ExtractToLocalRefactoring_10() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest10In(),
	            ExtractLocalTestsData.getTest10Out(),
	            ExtractLocalTestsData.findLocation("model.farInstance()", "test10"),
	            "model.farInstance()".length(), ExtractToLocalProposal.class);
	}
	
	public void test_ExtractToLocalRefactoring_10a() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest10In(),
	            ExtractLocalTestsData.getTest10Out(),
	            ExtractLocalTestsData.findLocation("model.farInstance() ", "test10"),
	            "model.farInstance() ".length(), ExtractToLocalProposal.class);
	}
	
	public void test_ExtractToLocalRefactoring_10b() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest10In(),
	            ExtractLocalTestsData.getTest10Out(),
	            ExtractLocalTestsData.findLocation("model.farInstance()  ", "test10"),
	            "model.farInstance()  ".length(), ExtractToLocalProposal.class);
	}
	
	public void test_ExtractToLocalRefactoring_11() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest11In(),
	            ExtractLocalTestsData.getTest11Out(),
	            ExtractLocalTestsData.findLocation("println \"here\"", "test11"),
	            "println \"here\"".length(), ExtractToLocalProposal.class);
	}
	
	public void test_ExtractToLocalRefactoring_12() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest12In(),
	            ExtractLocalTestsData.getTest12Out(),
	            ExtractLocalTestsData.findLocation("println \"here\"", "test12"),
	            "println \"here\"".length(), ExtractToLocalProposal.class);
	}
	
	public void test_ExtractToLocalRefactoring_13() throws Exception {
		assertConversion(ExtractLocalTestsData.getTest13In(),
	            ExtractLocalTestsData.getTest13Out(),
	            ExtractLocalTestsData.findLocation("a + b", "test13"),
	            "a + b".length(), ExtractToLocalProposal.class);
	}
	
    public void test_ExtractToConstant_1() throws Exception {
    	assertConversionAllOccurrences(ExtractConstantTestData.getTest1In(),
				ExtractConstantTestData.getTest1Out(),
				ExtractConstantTestData.findLocation("Foo + Bar", "test1"),
				"Foo + Bar".length(), ExtractToConstantProposal.class);
    }

    public void test_ExtractToConstant_2() throws Exception {
    	assertConversionAllOccurrences(ExtractConstantTestData.getTest2In(),
				ExtractConstantTestData.getTest2Out(),
				ExtractConstantTestData.findLocation("Foo + Bar", "test2"),
				"Foo + Bar".length(), ExtractToConstantProposal.class);
    }

    public void test_ExtractToConstant_3() throws Exception {
    	assertConversionAllOccurrences(ExtractConstantTestData.getTest3In(),
				ExtractConstantTestData.getTest3Out(),
				ExtractConstantTestData.findLocation("Foo+Bar+A.frax()", "test3"),
				"Foo+Bar+A.frax()".length(), ExtractToConstantProposal.class);
    }

    public void test_ExtractToConstant_4() throws Exception {
    	assertConversionAllOccurrences(ExtractConstantTestData.getTest4In(),
				ExtractConstantTestData.getTest4Out(),
				ExtractConstantTestData.findLocation("Foo+Bar+A.frax()", "test4"),
				"Foo+Bar+A.frax()".length(), ExtractToConstantProposal.class);
    }

    public void test_ExtractToConstant_5a() throws Exception {
    	assertConversionAllOccurrences(ExtractConstantTestData.getTest5aIn(),
				ExtractConstantTestData.getTest5aOut(),
				ExtractConstantTestData.findLocation("Foo+Bar+A.frax()", "test5a"),
				"Foo+Bar+A.frax()".length(), ExtractToConstantProposal.class);
    }

    public void test_ExtractToConstant_6a() throws Exception {
    	assertConversionAllOccurrences(ExtractConstantTestData.getTest6aIn(),
				ExtractConstantTestData.getTest6aOut(),
				ExtractConstantTestData.findLocation("Foo+Bar+A.frax()", "test6a"),
				"Foo+Bar+A.frax()".length(), ExtractToConstantProposal.class);
    }

    public void test_ExtractToConstant_7() throws Exception {
    	assertProposalNotOffered(ExtractConstantTestData.getTest7In(),
				ExtractConstantTestData.findLocation("Foo + Bar", "test7"),
				"Foo + Bar".length(), ExtractToConstantProposal.class);
    }

    public void test_ExtractToConstant_8() throws Exception {
    	assertConversion(ExtractConstantTestData.getTest8In(),
				ExtractConstantTestData.getTest8Out(),
				ExtractConstantTestData.findLocation("Foo + Bar", "test8"),
				"Foo + Bar".length(), ExtractToConstantProposal.class);
    }

    public void test_ExtractToConstant_NoReplaceOccurrences1() throws Exception {
    	assertConversion(ExtractConstantTestData.getTestNoReplaceOccurrences1In(),
				ExtractConstantTestData.getTestNoReplaceOccurrences1Out(),
				ExtractConstantTestData.findLocation("Foo+Bar+A.frax()", "testNoReplaceOccurrences1"),
				"Foo+Bar+A.frax()".length(), ExtractToConstantProposal.class);
    }

	public void test_ExtractToField_MethodToModule() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testMethodToModule");
		assertProposalNotOffered(testCase.getInput(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_ClosureToModule() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosureToModule");
		assertProposalNotOffered(testCase.getInput(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_DeclarationWithDef() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testDeclarationWithDef");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_DeclarationWithType() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testDeclarationWithType");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}

	public void test_ExtractToField_Reference() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testReference");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_TupleDeclaration() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testTupleDeclaration");
		assertProposalNotOffered(testCase.getInput(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_Initialization() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testInitialization");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_FieldReference() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testFieldReference");
		assertProposalNotOffered(testCase.getInput(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_Exception() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testException");
		assertProposalNotOffered(testCase.getInput(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_Prefix() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testPrefix");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_MethodInvocation() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testMethodInvocation");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_ParameterList() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testParameterList");
		assertProposalNotOffered(testCase.getInput(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_ArgumentList() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testArgumentList");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_InnerClass() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testInnerClass");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_FakeField() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testFakeField");
		assertConversion(testCase.getInput(), testCase.getExpected(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	public void test_ExtractToField_ClosureParameterList() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosureParameterList");
		assertProposalNotOffered(testCase.getInput(),
				testCase.getSelectionOffset(), testCase.getSelectionLength(),
				ConvertLocalToFieldProposal.class);
	}
	
	private void assertConversion(String original, String expected,
			String searchFor,
			Class<? extends AbstractGroovyCompletionProposal> proposalClass)
			throws Exception, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		int start = searchFor == null ? 0 : original.indexOf(searchFor);
		int length = searchFor == null ? 0 : searchFor.length();
		assertConversion(original, expected, start, length, proposalClass);
	}
	
	private void assertProposalNotOffered(String original, int offset,
			int length,
			Class<? extends AbstractGroovyCompletionProposal> proposalClass) throws Exception {
		ICompilationUnit unit = testProject.createUnit("", "QuickFix.groovy",
				original);

		IInvocationContext context = new AssistContext(unit, offset, length);
		AbstractGroovyCompletionProposal proposal = proposalClass
				.getConstructor(IInvocationContext.class).newInstance(context);
		assertFalse(
				"Expecting that proposals not offered for '"
						+ proposal.getDisplayString() + "'",
				proposal.hasProposals());
	}

	private void assertConversion(String original, String expected, int offset,
			int length,
			Class<? extends AbstractGroovyCompletionProposal> proposalClass)
			throws Exception, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		ICompilationUnit unit = testProject.createUnit("", "QuickFix.groovy",
				original);

		IInvocationContext context = new AssistContext(unit, offset, length);
		AbstractGroovyCompletionProposal proposal = proposalClass
				.getConstructor(IInvocationContext.class).newInstance(context);
		assertTrue(
				"Expecting that proposals exist for '"
						+ proposal.getDisplayString() + "'",
				proposal.hasProposals());
		IDocument document = new Document(
				String.valueOf(((CompilationUnit) unit).getContents()));
		proposal.apply(document);

		assertEquals("Invalid application of quick assist", expected,
				document.get());
	}
	
	private void assertConversionAllOccurrences(String original, String expected, int offset,
			int length,
			Class<? extends AbstractGroovyCompletionProposal> proposalClass)
			throws Exception, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		ICompilationUnit unit = testProject.createUnit("", "QuickFix.groovy",
				original);

		IInvocationContext context = new AssistContext(unit, offset, length);
		AbstractGroovyCompletionProposal proposal = proposalClass
				.getConstructor(IInvocationContext.class, boolean.class).newInstance(context, true);
		assertTrue(
				"Expecting that proposals exist for '"
						+ proposal.getDisplayString() + "'",
				proposal.hasProposals());
		
		IDocument document = new Document(
				String.valueOf(((CompilationUnit) unit).getContents()));
		proposal.apply(document);

		assertEquals("Invalid application of quick assist", expected,
				document.get());
	}
}
