/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.util.PublicScanner;

public class ScannerTest extends AbstractRegressionTest {

	public ScannerTest(String name) {
		super(name);
	}
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 58 };
//		TESTS_RANGE = new int[] { 54, -1 };
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return ScannerTest.class;
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test001() {
		String sourceA001 = "\\u003b";
		IScanner scanner = ToolFactory.createScanner(false, true, false, false);
		scanner.setSource(sourceA001.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong token type", ITerminalSymbols.TokenNameSEMICOLON, token);
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test002() {
		String sourceA002 = "// tests\n  ";
		IScanner scanner = ToolFactory.createScanner(false, true, false, false);
		scanner.setSource(sourceA002.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameWHITESPACE, token);
			assertEquals("Wrong size", 2, scanner.getCurrentTokenSource().length);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test003() {
		String sourceA003 = "// tests\n  ";
		IScanner scanner = ToolFactory.createScanner(true, true, false, false);
		scanner.setSource(sourceA003.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameCOMMENT_LINE, token);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameWHITESPACE, token);
			assertEquals("Wrong size", 2, scanner.getCurrentTokenSource().length);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}

	/**
	 * float constant can have exponent part without dot: 01e0f
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=30704
	 */
	public void test004() {
		String source = "01e0f";
		IScanner scanner = ToolFactory.createScanner(false, false, false, false);
		scanner.setSource(source.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameFloatingPointLiteral, token);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43437
	 */
	public void test005() {
		StringBuffer buf = new StringBuffer();
		buf.append("\"Hello\"");
		String str = buf.toString();
		IScanner scanner = ToolFactory.createScanner(true, false, false, false);
		scanner.setSource(str.toCharArray());
		scanner.resetTo(0, str.length() - 1);
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameStringLiteral, token);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43485
	 */
	public void test006() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, false);
		try {
			scanner.setSource(null);
		} catch (NullPointerException e) {
			assertTrue(false);
		}
	}

	/*
	 * Check that bogus resetTo issues EOFs
	 */
	public void test007() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, false);
		char[] source = "int i = 0;".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(source.length + 50, source.length - 1);
		int token = -1;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Expecting EOF", ITerminalSymbols.TokenNameEOF, token);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test008() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x11aa.aap-3333f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int token = -1;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameFloatingPointLiteral, token);
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Expecting EOF", ITerminalSymbols.TokenNameEOF, token);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test009() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_4);
		char[] source = "0x11aa.aap-3333f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			scanner.getNextToken();
		} catch (InvalidInputException e) {
			assertEquals("Wrong message", PublicScanner.ILLEGAL_HEXA_LITERAL, e.getMessage());
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test010() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x11aa.aap-3333f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test011() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x.aap-3333f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test012() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaap3f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test013() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaapaf".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				// ignore
			}
		} catch (InvalidInputException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test014() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaap.1f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				// ignore
			}
		} catch (InvalidInputException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test015() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaa.p1f".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test016() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaa.p1F".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test017() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaa.p1D".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test018() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0xaa.p1d".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74934
	 */
	public void test019() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_4);
		char[] source = "0x".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				// ignore
			}
		} catch (InvalidInputException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74934
	 */
	public void test020() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				// ignore
			}
		} catch (InvalidInputException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74934
	 */
	public void test021() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_4);
		char[] source = "0x1".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74934
	 */
	public void test022() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x1".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
		assertEquals("Wrong number of tokens", 1, counter);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78905
	 */
	public void test023() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_5);
		char[] source = "0x.p-2".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
			}
			assertTrue(false);
		} catch (InvalidInputException e) {
			assertTrue(true);
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84398
	 */
	public void test024() {
		IScanner scanner = ToolFactory.createScanner(false, false, true, JavaCore.VERSION_1_5);
		char[] source = "public class X {\n\n}".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		int counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
		}

		assertEquals("wrong number of tokens", 5, counter);
		int[] lineEnds = scanner.getLineEnds();
		assertNotNull("No line ends", lineEnds);
		assertEquals("wrong length", 2, lineEnds.length);
		source = "public class X {}".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		lineEnds = scanner.getLineEnds();
		assertNotNull("No line ends", lineEnds);
		assertEquals("wrong length", 0, lineEnds.length);

		counter = 0;
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
				counter++;
			}
		} catch (InvalidInputException e) {
		}

		assertEquals("wrong number of tokens", 5, counter);
		lineEnds = scanner.getLineEnds();
		assertNotNull("No line ends", lineEnds);
		assertEquals("wrong length", 0, lineEnds.length);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84398
	 */
	public void test025() {
		IScanner scanner = ToolFactory.createScanner(true, true, false, true);
		scanner.setSource("String\r\nwith\r\nmany\r\nmany\r\nline\r\nbreaks".toCharArray());

		try {
			while(scanner.getNextToken()!=ITerminalSymbols.TokenNameEOF){}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}

		assertEquals("Wrong size", 5, scanner.getLineEnds().length);

		scanner.setSource("No line breaks here".toCharArray()); // expecting line breaks to reset
		assertEquals("Wrong size", 0, scanner.getLineEnds().length);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=86611
	 */
	public void test026() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_4);
		char[] source = "0x.p-2".toCharArray(); //$NON-NLS-1$
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			while (scanner.getNextToken() != ITerminalSymbols.TokenNameEOF) {
			}
			assertTrue(false);
		} catch (InvalidInputException e) {
			assertTrue(true);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test027() {
		char[] source = ("class Test {\n" +
				"  char  C = \"\\u005Cn\";\n" +
				"}").toCharArray();
		Scanner scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_4, null, null, false);
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			StringBuffer buffer = new StringBuffer();
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				try {
					switch(token) {
						case TerminalTokens.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertEquals("Wrong contents", "classTest{charC=\"\n\";}", String.valueOf(buffer));
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test028() {
		char[] source = ("class Test {\n" +
				"  char  C = \'\\u005Cn\';\n" +
				"}").toCharArray();
		Scanner scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_4, null, null, false);
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			StringBuffer buffer = new StringBuffer();
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				try {
					switch(token) {
						case TerminalTokens.TokenNameStringLiteral :
							buffer.append(new String(scanner.getCurrentTokenSourceString()));
							break;
						case TerminalTokens.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertEquals("Wrong contents", "classTest{charC=\'\\n\';}", String.valueOf(buffer));
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test029() {
		char[] source = ("class Test {\n" +
				"  char  C = \"\\n\";\n" +
				"}").toCharArray();
		Scanner scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_4, null, null, false);
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			StringBuffer buffer = new StringBuffer();
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				try {
					switch(token) {
						case TerminalTokens.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertEquals("Wrong contents", "classTest{charC=\"\n\";}", String.valueOf(buffer));
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test030() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static String C = \"\\n\";\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C.length());\n" +
					"  	System.out.print(C.charAt(0) == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"1true");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test031() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"  static String C = \"\\u005Cn\";\n" +
				"  \n" +
				"  public static void main(String[] args) {\n" +
				"  	System.out.print(C.length());\n" +
				"  	System.out.print(C.charAt(0) == \'\\n\');\n" +
				"  }\n" +
				"}"
			},
			"1true");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test032() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static char C = \'\\u005Cn\';\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"true");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test033() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static char C = \\u0027\\u005Cn\\u0027;\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"true");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test034() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test {\n" +
					"  static String C = \"\u0043\\n\\u0043\";\n" +
					"  \n" +
					"  public static void main(String[] args) {\n" +
					"  	System.out.print(C.length());\n" +
					"  	System.out.print(C.charAt(1) == \'\\n\');\n" +
					"  }\n" +
					"}"
				},
				"3true");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90414
	public void test035() {
		/*
		 * Corresponding source:
		 *
		 * public class Test {
		 * 	  static String C = "\n";
		 *    public static void main(String[] args) {
		 * 	  	System.out.print(C.length());
		 * 		  	System.out.print(C.charAt(0) == '\n');
		 *    }
		 * }
		 */
		this.runConformTest(
				new String[] {
					"Test.java",
					"\\u0070\\u0075\\u0062\\u006c\\u0069\\u0063\\u0020\\u0063\\u006c\\u0061\\u0073\\u0073\\u0020\\u0054\\u0065\\u0073\\u0074\\u0020\\u007b\\u000A\n" +
					"\\u0020\\u0020\\u0073\\u0074\\u0061\\u0074\\u0069\\u0063\\u0020\\u0053\\u0074\\u0072\\u0069\\u006e\\u0067\\u0020\\u0043\\u0020\\u003d\\u0020\\u0022\\u005c\\u006e\\u0022\\u003b\\u000A\n" +
					"\\u0020\\u0020\\u000A\n" +
					"\\u0020\\u0020\\u0070\\u0075\\u0062\\u006c\\u0069\\u0063\\u0020\\u0073\\u0074\\u0061\\u0074\\u0069\\u0063\\u0020\\u0076\\u006f\\u0069\\u0064\\u0020\\u006d\\u0061\\u0069\\u006e\\u0028\\u0053\\u0074\\u0072\\u0069\\u006e\\u0067\\u005b\\u005d\\u0020\\u0061\\u0072\\u0067\\u0073\\u0029\\u0020\\u007b\\u000A\n" +
					"\\u0020\\u0020\\u0009\\u0053\\u0079\\u0073\\u0074\\u0065\\u006d\\u002e\\u006f\\u0075\\u0074\\u002e\\u0070\\u0072\\u0069\\u006e\\u0074\\u0028\\u0043\\u002e\\u006c\\u0065\\u006e\\u0067\\u0074\\u0068\\u0028\\u0029\\u0029\\u003b\\u000A\n" +
					"\\u0020\\u0020\\u0009\\u0053\\u0079\\u0073\\u0074\\u0065\\u006d\\u002e\\u006f\\u0075\\u0074\\u002e\\u0070\\u0072\\u0069\\u006e\\u0074\\u0028\\u0043\\u002e\\u0063\\u0068\\u0061\\u0072\\u0041\\u0074\\u0028\\u0030\\u0029\\u0020\\u003d\\u003d\\u0020\\u0027\\u005c\\u006e\\u0027\\u0029\\u003b\\u000A\n" +
					"\\u0020\\u0020\\u007d\\u0020\\u0009\\u000A\n" +
					"\\u007d"
				},
				"1true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106403
	public void test036() {
		try {
			IScanner s = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
			char[] source = { ';', ' ' };
			s.setSource(source);
			s.resetTo(0, 0);
			int token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameSEMICOLON, token);
			char[] tokenSource = s.getCurrentTokenSource();
			assertEquals("wront size", 1, tokenSource.length);
			assertEquals("Wrong character", ';', tokenSource[0]);
			token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue("Should not happen", false);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106403
	public void test037() {
		try {
			IScanner s = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
			char[] source = { ';', ' ' };
			s.setSource(source);
			int token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameSEMICOLON, token);
			char[] tokenSource = s.getCurrentTokenSource();
			assertEquals("wront size", 1, tokenSource.length);
			assertEquals("Wrong character", ';', tokenSource[0]);
			token = s.getNextToken();
			tokenSource = s.getCurrentTokenSource();
			assertEquals("wront size", 1, tokenSource.length);
			assertEquals("Wrong character", ' ', tokenSource[0]);
			assertEquals("Wrong token", ITerminalSymbols.TokenNameWHITESPACE, token);
			token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue("Should not happen", false);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106403
	public void test038() {
		try {
			IScanner s = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
			char[] source = { ';', ' ' };
			s.setSource(source);
			s.resetTo(0, 1);
			int token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameSEMICOLON, token);
			char[] tokenSource = s.getCurrentTokenSource();
			assertEquals("wront size", 1, tokenSource.length);
			assertEquals("Wrong character", ';', tokenSource[0]);
			token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameWHITESPACE, token);
			tokenSource = s.getCurrentTokenSource();
			assertEquals("wront size", 1, tokenSource.length);
			assertEquals("Wrong character", ' ', tokenSource[0]);
			token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue("Should not happen", false);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106403
	public void test039() {
		try {
			IScanner s = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
			char[] source = { ';', ' ' };
			s.setSource(source);
			s.resetTo(1, 1);
			int token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameWHITESPACE, token);
			char[] tokenSource = s.getCurrentTokenSource();
			assertEquals("wront size", 1, tokenSource.length);
			assertEquals("Wrong character", ' ', tokenSource[0]);
			token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue("Should not happen", false);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106403
	public void test040() {
		try {
			IScanner s = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
			char[] source = { ';', ' ' };
			s.setSource(source);
			s.resetTo(2, 1);
			int token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue("Should not happen", false);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106403
	public void test041() {
		try {
			IScanner s = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
			char[] source = "\\u003B\\u0020".toCharArray();
			assertEquals("wrong size", 12, source.length);
			s.setSource(source);
			s.resetTo(0, 5);
			int token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameSEMICOLON, token);
			char[] tokenSource = s.getRawTokenSource();
			assertEquals("wront size", 6, tokenSource.length);
			assertEquals("Wrong character", "\\u003B", new String(tokenSource));
			token = s.getNextToken();
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
			assertTrue("Should not happen", false);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=112223
	public void test042() {
		IScanner scanner = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
		final char[] source = "\"a\\u000D\"".toCharArray();
		scanner.setSource(source);
		final StringBuffer buffer = new StringBuffer();
		try {
			int token;
			while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				try {
					switch(token) {
						case ITerminalSymbols.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertTrue("Should have failed", false);
		} catch (InvalidInputException e) {
			buffer.append(scanner.getRawTokenSource());
			assertEquals("Unexpected contents", "\"a\\u000D\"", String.valueOf(buffer));
			assertEquals("Wrong exception", PublicScanner.INVALID_CHAR_IN_STRING, e.getMessage());
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=112223
	public void test043() {
		IScanner scanner = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
		final char[] source = "\"\\u004Ca\\u000D\"".toCharArray();
		scanner.setSource(source);
		final StringBuffer buffer = new StringBuffer();
		try {
			int token;
			while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				try {
					switch(token) {
						case ITerminalSymbols.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertTrue("Should have failed", false);
		} catch (InvalidInputException e) {
			buffer.append(scanner.getRawTokenSource());
			assertEquals("Unexpected contents", "\"\\u004Ca\\u000D\"", String.valueOf(buffer));
			assertEquals("Wrong exception", PublicScanner.INVALID_CHAR_IN_STRING, e.getMessage());
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=112223
	public void test044() {
		IScanner scanner = ToolFactory.createScanner(true, true, true, "1.5", "1.5");
		final char[] source = "\"\\u004Ca\\u000D\\u0022".toCharArray();
		scanner.setSource(source);
		final StringBuffer buffer = new StringBuffer();
		try {
			int token;
			while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				try {
					switch(token) {
						case ITerminalSymbols.TokenNameEOF :
							break;
						default :
							buffer.append(scanner.getCurrentTokenSource());
							break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			assertTrue("Should have failed", false);
		} catch (InvalidInputException e) {
			buffer.append(scanner.getRawTokenSource());
			assertEquals("Unexpected contents", "\"\\u004Ca\\u000D\\u0022", String.valueOf(buffer));
			assertEquals("Wrong exception", PublicScanner.INVALID_CHAR_IN_STRING, e.getMessage());
		}
	}

	public void test045() {
		/*
		 * Corresponding source:
		 *
		 * public class Test {
		 * 	  static String C = "\n";
		 *    public static void main(String[] args) {
		 * 	  	System.out.print(C.length());
		 * 		  	System.out.print(C.charAt(0) == '\n');
		 *    }
		 * }
		 */
		this.runConformTest(
				new String[] {
					"_X.java",
					"import java.lang.reflect.Field;\n" +
					"public class _X {\n" +
					"	public static void main(String[] args) {\n" +
					"		String i\\u0000;\n" +
					"		String i\\u0001;\n" +
					"		String i\\u0002;\n" +
					"		String i\\u0003;\n" +
					"		String i\\u0004;\n" +
					"		String i\\u0005;\n" +
					"		String i\\u0006;\n" +
					"		String i\\u0007;\n" +
					"		String i\\u0008;\n" +
					"		String i\\u000e;\n" +
					"		String i\\u000f;\n" +
					"		String i\\u0010;\n" +
					"		String i\\u0011;\n" +
					"		String i\\u0012;\n" +
					"		String i\\u0013;\n" +
					"		String i\\u0014;\n" +
					"		String i\\u0015;\n" +
					"		String i\\u0016;\n" +
					"		String i\\u0017;\n" +
					"		String i\\u0018;\n" +
					"		String i\\u0019;\n" +
					"		String i\\u001a;\n" +
					"		String i\\u001b;\n" +
					"		String i\\u007f;\n" +
					"		System.out.print(\"SUCCESS\");\n" +
					"	}\n" +
					"}"
				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=264950
	public void test046() {
		IScanner scanner = ToolFactory.createScanner(
				true,
				true,
				true,
				JavaCore.getOption(JavaCore.COMPILER_SOURCE),
				JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
		final char[] source = "{\r\n\t}".toCharArray();
		scanner.setSource(source);
		scanner.resetTo(1, 3);
		try {
			assertEquals("Wrong token", ITerminalSymbols.TokenNameWHITESPACE, scanner.getNextToken());
			assertEquals("Wrong source", "\r\n\t", new String(scanner.getCurrentTokenSource()));
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, scanner.getNextToken());
		} catch (InvalidInputException e) {
			assertTrue("Wrong exception", false);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294529
	public void test047() {
		IScanner scanner = ToolFactory.createScanner(
				true,
				true,
				true,
				JavaCore.getOption(JavaCore.COMPILER_SOURCE),
				JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
		final char[] source = "// a comment, longer than the offset".toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, 5);
		try {
			assertEquals("Wrong token", ITerminalSymbols.TokenNameCOMMENT_LINE, scanner.getNextToken());
			assertEquals("Wrong source", "// a c", new String(scanner.getCurrentTokenSource()));
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, scanner.getNextToken());
		} catch (InvalidInputException e) {
			assertTrue("Wrong exception", false);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294529
	public void test048() {
		IScanner scanner = ToolFactory.createScanner(
				true,
				true,
				true,
				JavaCore.getOption(JavaCore.COMPILER_SOURCE),
				JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
		final char[] source = "/*a comment, longer\n than the\noffset*/".toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, 5);
		try {
			assertEquals("Wrong token", ITerminalSymbols.TokenNameCOMMENT_BLOCK, scanner.getNextToken());
			assertTrue("Should fail with InvalidInputException", false);
		} catch (InvalidInputException e) {
			assertEquals("Wrong source", "/*a co", new String(scanner.getCurrentTokenSource()));
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294529
	public void test049() {
		IScanner scanner = ToolFactory.createScanner(
				true,
				true,
				true,
				JavaCore.getOption(JavaCore.COMPILER_SOURCE),
				JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
		final char[] source = "/*a coabstract, longer\n than the\noffset*/".toCharArray();
		scanner.setSource(source);
		scanner.resetTo(6, 13);
		try {
			assertEquals("Wrong token", ITerminalSymbols.TokenNameabstract, scanner.getNextToken());
			assertEquals("Wrong source", "abstract", new String(scanner.getCurrentTokenSource()));
			assertEquals("Wrong token", ITerminalSymbols.TokenNameEOF, scanner.getNextToken());
		} catch (InvalidInputException e) {
			assertTrue("Wrong exception", false);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294529
	public void test050() {
		IScanner scanner = ToolFactory.createScanner(
				true,
				true,
				true,
				JavaCore.getOption(JavaCore.COMPILER_SOURCE),
				JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
		final char[] source = "\"a comment, longer\\n than the\\noffset \"".toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, 5);
		try {
			assertEquals("Wrong token", ITerminalSymbols.TokenNameStringLiteral, scanner.getNextToken());
			assertTrue("Should fail with InvalidInputException", false);
		} catch (InvalidInputException e) {
			assertEquals("Wrong source", "\"a com", new String(scanner.getCurrentTokenSource()));
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294529
	public void test051() {
		IScanner scanner = ToolFactory.createScanner(
				true,
				true,
				true,
				JavaCore.getOption(JavaCore.COMPILER_SOURCE),
				JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
		final char[] source = "\"a co\\u00E9mment, longer\\n than the\\noffset \"".toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, 5);
		try {
			assertEquals("Wrong token", ITerminalSymbols.TokenNameStringLiteral, scanner.getNextToken());
			assertTrue("Should fail with InvalidInputException", false);
		} catch (InvalidInputException e) {
			assertEquals("Wrong source", "\"a co\\", new String(scanner.getCurrentTokenSource()));
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294529
	public void test052() {
		IScanner scanner = ToolFactory.createScanner(
				true,
				true,
				true,
				JavaCore.getOption(JavaCore.COMPILER_SOURCE),
				JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
		final char[] source = "\"\\u00E9mment, longer\\n than the\\noffset \"".toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, 5);
		try {
			assertEquals("Wrong token", ITerminalSymbols.TokenNameStringLiteral, scanner.getNextToken());
			assertTrue("Should fail with InvalidInputException", false);
		} catch (InvalidInputException e) {
			assertEquals("Wrong source", "\"\\u00E", new String(scanner.getCurrentTokenSource()));
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330081
	public void test053() {
		IScanner scanner = ToolFactory.createScanner(
				true,
				true,
				true,
				JavaCore.VERSION_1_4,
				JavaCore.VERSION_1_4);
		final char[] source = "elnu".toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			assertEquals("Wrong token", ITerminalSymbols.TokenNameIdentifier, scanner.getNextToken());
		} catch (InvalidInputException e) {
			assertTrue("Should not fail with InvalidInputException", false);
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=340513
	 */
	public void test054() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_6, JavaCore.VERSION_1_6);
		char[] source =
				("class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String \ud804\udc09 = \"Brahmi\";\n" + 
				"		System.out.println(\ud804\udc09);\n" + 
				"	}\n" + 
				"}").toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			boolean foundError = false;
			while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				foundError |= token == ITerminalSymbols.TokenNameERROR;
			}
			assertTrue("Did not find error token", foundError);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=340513
	 */
	public void test055() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_7, JavaCore.VERSION_1_7);
		char[] source =
				("class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String \ud804\udc09 = \"Brahmi\";\n" + 
				"		System.out.println(\ud804\udc09);\n" + 
				"	}\n" + 
				"}").toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				assertFalse("found error token", token == ITerminalSymbols.TokenNameERROR);
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=340513
	 */
	public void test056() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_6, JavaCore.VERSION_1_6);
		char[] source =
				("class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String \u20B9 = \"Rupee symbol\";\n" + 
				"		System.out.println(\u20B9);\n" + 
				"	}\n" + 
				"}").toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			boolean foundError = false;
			while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				foundError |= token == ITerminalSymbols.TokenNameERROR;
			}
			assertTrue("Did not find error token", foundError);
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=340513
	 */
	public void test057() {
		IScanner scanner = ToolFactory.createScanner(false, false, false, JavaCore.VERSION_1_7, JavaCore.VERSION_1_7);
		char[] source =
				("class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String \u20B9 = \"Rupee symbol\";\n" + 
				"		System.out.println(\u20B9);\n" + 
				"	}\n" + 
				"}").toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);
		try {
			int token;
			while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				assertFalse("found error token", token == ITerminalSymbols.TokenNameERROR);
			}
		} catch (InvalidInputException e) {
			assertTrue(false);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=352014
	public void test058() {
		String source =
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		int a\\u1369b;\n" + 
				"	}\n" + 
				"}";
		if (this.complianceLevel <= ClassFileConstants.JDK1_6) {
			this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"");
		} else {
			this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	int a\\u1369b;\n" + 
				"	     ^^^^^^\n" + 
				"Syntax error on token \"Invalid Character\", = expected\n" + 
				"----------\n");
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=352553
	public void test059() {
		String source =
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		int a\\u200B;\n" + 
				"	}\n" + 
				"}";
		if (this.complianceLevel > ClassFileConstants.JDK1_6) {
			this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"");
		} else {
			this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	int a\\u200B;\n" + 
				"	     ^^^^^^\n" + 
				"Syntax error on token \"Invalid Character\", delete this token\n" + 
				"----------\n");
		}
	}
}
