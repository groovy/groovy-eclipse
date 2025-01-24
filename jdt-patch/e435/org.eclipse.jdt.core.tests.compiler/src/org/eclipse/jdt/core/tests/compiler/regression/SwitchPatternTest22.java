/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class SwitchPatternTest22 extends AbstractBatchCompilerTest {

	static {
		//	TESTS_NAMES = new String [] { "testNaming" };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(SwitchPatternTest22.class, F_22);
	}

	public SwitchPatternTest22(String name) {
		super(name);
	}

	@Override
	protected Map<String, String> getCompilerOptions() {
		CompilerOptions compilerOptions = new CompilerOptions(super.getCompilerOptions());
		return compilerOptions.getMap();
	}

	public void runConformTest(String[] files, String expectedOutput) {
		super.runConformTest(files, expectedOutput, null, null);
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		runConformTest(testFiles, expectedOutput, "", customOptions);
	}

	protected void runConformTest(String[] testFiles, String expectedOutput, String errorOutput) {
		runConformTest(testFiles, expectedOutput, errorOutput, getCompilerOptions());
	}

	protected void runConformTest(String[] testFiles, String expectedOutput, String expectedErrorOutput, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.expectedErrorString = expectedErrorOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.runConformTest();
	}

	public void testListOfPatterns_000() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Melon(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_001() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {
                            record Point(int x, int y) {}
							public static void main(String... args) {
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Point(_, _), Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_002() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Licorice(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"failure");
	}

	public void testListOfPatterns_003() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Melon(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when 1 == 1 && 2 == 2: System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_004() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when 1 == 1 && 2 == 2 : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_005() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Licorice(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when 1 == 1 && 2 == 2 : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"failure");
	}

	// next three tests: beat the static analysis so that the `when` clause comparison isn't optimized away

	public void testListOfPatterns_006() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								String myString = "asdf";
								Object o = new Pantaloon(new Melon(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when myString.length() == 4: System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_007() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								String myString = "asdf";
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when myString.length() == 4: System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_008() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								String myString = "asdf";
								Object o = new Pantaloon(new Licorice(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when myString.length() == 4: System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"failure");
	}

	public void testTwoCasesWithRecordPatternsShouldNotDominateRegression() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int i) when true: System.out.println("success " + i); break;
								case Pantaloon(Melon _, int i) when true : System.out.println("success " + i); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success 12");
	}

	public void testWhenAtWrongPlace() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				      case String s when s != null, Integer i when i == 3, X x when x != null : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case String s when s != null, Integer i when i == 3, X x when x != null : System.out.println(\"Integer\");\n" +
			"	            ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s when s != null, Integer i when i == 3, X x when x != null : System.out.println(\"Integer\");\n" +
			"	              ^^^^^^^^^^^^^^\n" +
			"Syntax error on token(s), misplaced construct(s)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case String s when s != null, Integer i when i == 3, X x when x != null : System.out.println(\"Integer\");\n" +
			"	                                      ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 4)\n" +
			"	case String s when s != null, Integer i when i == 3, X x when x != null : System.out.println(\"Integer\");\n" +
			"	                                        ^^^^^^^^^^^\n" +
			"Syntax error on token(s), misplaced construct(s)\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 4)\n" +
			"	case String s when s != null, Integer i when i == 3, X x when x != null : System.out.println(\"Integer\");\n" +
			"	                                                       ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 4)\n" +
			"	case String s when s != null, Integer i when i == 3, X x when x != null : System.out.println(\"Integer\");\n" +
			"	                                                              ^\n" +
			"x cannot be resolved to a variable\n" +
			"----------\n");
	}
	public void testIllegalFallThrough() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				      case String s, Integer i, X x when o != null : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case String s, Integer i, X x when o != null : System.out.println(\"Integer\");\n" +
			"	            ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s, Integer i, X x when o != null : System.out.println(\"Integer\");\n" +
			"	                       ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case String s, Integer i, X x when o != null : System.out.println(\"Integer\");\n" +
			"	                            ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n");
	}
	public void testInternalDomination_this() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				    //  case Number _ : System.out.println("A Number");
				      case Object _, Integer _, X _ when o != null : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"Integer\n"
			+ "Object");
	}
	public void testInternalDomination_2() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				    //  case Number _ : System.out.println("A Number");
				      case Object _, Integer _, X _ when true : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Object _, Integer _, X _ when true : System.out.println(\"Integer\");\n" +
			"	               ^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case Object _, Integer _, X _ when true : System.out.println(\"Integer\");\n" +
			"	                          ^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	default: System.out.println(\"Object\");\n" +
			"	^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n");
	}
	public void testInternalDomination_3() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				    //  case Number _ : System.out.println("A Number");
				      case Object _, Integer _, X _  : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Object _, Integer _, X _  : System.out.println(\"Integer\");\n" +
			"	               ^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case Object _, Integer _, X _  : System.out.println(\"Integer\");\n" +
			"	                          ^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	default: System.out.println(\"Object\");\n" +
			"	^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n");
	}
	public void testExternalDomination() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				      case Number _ : System.out.println("A Number");
				      case Integer _, String _  : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Integer _, String _  : System.out.println(\"Integer\");\n" +
			"	     ^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}
	public void testExternalDomination_2() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				      case Number a : System.out.println("A Number");
				      case Integer _, String _  : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Integer _, String _  : System.out.println(\"Integer\");\n" +
			"	     ^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}
	public void testExternalDomination_3() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				      case Number _ : System.out.println("A Number");
				      case X _, Integer _, String _  : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case X _, Integer _, String _  : System.out.println(\"Integer\");\n" +
			"	          ^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}
	public void testExternalDomination_3_1() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				      case Number _ when o != null : System.out.println("A Number");
				      case X _, Integer _, String _  : System.out.println("Multi");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"Multi\n"
			+ "Object");
	}
	public void testExternalDomination_4() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				      case X _, Number _, String _  : System.out.println("Multi label");
				      case Integer _ : System.out.println("A Integer");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Integer _ : System.out.println(\"A Integer\");\n" +
			"	     ^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}
	public void testExternalDomination_4_1() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  static void foo(Object o) {
				    switch (o) {
				      case X _, Number _, String _  when o != null : System.out.println("Multi label");
				      case Integer _ : System.out.println("A Integer");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"Multi label\n"
			+ "A Integer\n"
			+ "Object");
	}
	// javac jdk21 allows components to be named, but they can't be referenced.
	public void testNaming() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				  record Point (int x, int y) {}
				  static void foo(Object o) {
				    switch (o) {
				      case Integer _, Point(int x, int _), String _  : System.out.println("Integer");
				      default: System.out.println("Object");
				    }
				  }
				  public static void main(String [] args) {
				  	foo("");
				  }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Integer _, Point(int x, int _), String _  : System.out.println(\"Integer\");\n" +
			"	                          ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n");
	}
	public void testGuard_0() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when o == null : System.out.println("success"); break;
								default: System.out.println("default"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"default");
	}
	public void testGuard_2() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Melon(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when o == null : System.out.println("success"); break;
								default: System.out.println("default"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"default");
	}
	public void testGuard_3() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when o != null : System.out.println("success"); break;
								default: System.out.println("default"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}
	public void testGuard_4() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Melon(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when o != null : System.out.println("success"); break;
								default: System.out.println("default"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}
	public void testFallThrough() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						sealed interface I permits A, B, C, D {}
						final class A implements I{}
						final class B implements I{}
						final class C implements I{}
						final class D implements I{}
						public class X<T> {
						    public boolean foo(I r) {
						    	boolean ret = false;
						    	switch (r) {
						    	case A _, B _ : case C _ : ret = true; break;
						    		default : ret = false;
						    	};
						    	return ret;
						    }
						    public static void main(String argv[]) {
						    	System.out.println(new X<>().foo(new A()));
						    	System.out.println(new X<>().foo(new B()));
						    	System.out.println(new X<>().foo(new C()));
						    	System.out.println(new X<>().foo(new D()));
						    }
						}
						""", },
				"true\n" +
				"true\n" +
				"true\n" +
				"false");
	}
	public void testTypePattern() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {
						    public static void main(String... args) {
						        Object o = new String();
						        switch (o) {
						        case String _, X _, Integer _ : System.out.println("success"); break;
						        default: System.out.println("failure"); break;
						        }
						    }
						}
						""", },
				"success");
	}
	public void testTypePattern_2() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {
						    public static void main(String... args) {
						        Object o = new X();
						        switch (o) {
						        case String _, X _, Integer _ : System.out.println("success"); break;
						        default: System.out.println("failure"); break;
						        }
						    }
						}
						""", },
				"success");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2070
	// [Switch] Compiler is unable to parse a particular multicase construct
	public void testIssue2070() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    public boolean foo(Object o) {
						    	return switch (o) {
						    	case Integer _, Integer[] _ -> true;
						    		default -> false;
						    	};
						    }
						    public static void main(String argv[]) {
						    	System.out.println(new X().foo(new Object()));
						    }
						}
						"""
				},
			"false");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2956
	// Unnamed patterns inside of multi case patterns fail to parse
	public void testIssue2956() {
		runConformTest(
				new String[] {
						"Main.java",
						"""
						public class Main {
						    public sealed interface MyType {
						        record A() implements MyType {
						        }

						        record B(int value) implements MyType {
						        }

						        record C() implements MyType {
						        }
						    }

						    public static void main(String[] args) {
						        MyType myType = new MyType.A();
						        switch (myType) {
						            case MyType.A(), MyType.B(_) -> { System.out.println("A or B");}
						            case MyType.C() -> {}
						        }
						    }
						}
						"""
				},
			"A or B");
	}


	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2956
	// Unnamed patterns inside of multi case patterns fail to parse
	public void testIssue2956_2() {
		runConformTest(
				new String[] {
						"Main.java",
						"""
						public class Main {
						    public sealed interface MyType {
						        record A() implements MyType {
						        }

						        record B(int value) implements MyType {
						        }

						        record C() implements MyType {
						        }
						    }

						    public static void main(String[] args) {
						        MyType myType = new MyType.A();
						        switch (myType) {
						            case MyType.A()  ->  { System.out.println("A");}
						            case MyType.B(_) ->  { System.out.println("B");}
						            case MyType.C()  ->  { System.out.println("C");}
						        }
						    }
						}
						"""
				},
			"A");
	}


	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2956
	// Unnamed patterns inside of multi case patterns fail to parse
	public void testIssue2956_3() {
		runConformTest(
				new String[] {
						"Main.java",
						"""
						public class Main {
						    public sealed interface MyType {
						        record A() implements MyType {
						        }

						        record B(int value) implements MyType {
						        }

						        record C() implements MyType {
						        }
						    }

						    public static void main(String[] args) {
						        MyType myType = new MyType.A();
						        switch (myType) {
						            case MyType.B(_), MyType.A()  -> { System.out.println("A or B");}
						            case MyType.C() -> {}
						        }
						    }
						}
						"""
				},
			"A or B");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3109
	// [Patterns] Mixed multiple pattern and when is not fully supported in switch expression
	public void testIssue3109() {
		runConformTest(
				new String[] {
						"Maybe.java",
						"""
						import java.util.function.Function;
						import java.util.function.Predicate;

						public sealed interface Maybe<T>  {

						  static <T> Maybe<T> of(T value) {
						    return new Some<>(value);
						  }

						  static <T> Maybe<T> empty() {
						    return new None<>();
						  }

						  default Maybe<T> filter(Predicate<T> predicate){
						    return switch (this) {
						      case Some(T value) when predicate.test(value) -> this; // unsupported line with 4 errors
						      case Some(_), None<T> _ -> empty();
						    };
						  }

						  default Maybe<T> filter2(Predicate<T> predicate){
						    return switch (this) {
						      case Some(_), None<T> _ -> empty(); //also unsupported with 1 error
						    };
						  }
						  record Some<T>(T value) implements Maybe<T> {}
						  record None<T>() implements Maybe<T>{}
						}
						"""
				},
			"");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3318
	// [Enhanced Switch] ECJ tolerates fall through to default from a case with pattern label while javac rejects it.
	public void testIssue3318() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X {
							static /* @NonNull */ Object foo(/*@NonNull */ Object o) {
							    switch (o) {
							        case String s:
							        default:
							        	System.out.println();
							    }
							    return o;
							}
						}
						class X2 {
							static /* @NonNull */ Object foo(/*@NonNull */ Object o) {
							    switch (o) {
							        case String s:
							        default:
							        	System.out.println(s);
							    }
							    return o;
							}
						}
						class X3 {
							static  Object foo( Object o) {
							    switch (o) {
							        case String _ :
							        default :
							        	System.out.println();
							    }
							    return o;
							}
						}
						class X4 {
							static /* @NonNull */ Object foo(/*@NonNull */ Object o) {
							    switch (o) {
							        case String s:
							        	System.out.println();  // first println
							        default:
							        	System.out.println();
							    }
							    return o;
							}
						}
						"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case String s:\n" +
				"	^^^^^^^^^^^^^\n" +
				"Illegal fall-through from a case label pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 16)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3320
	// [Enhanced Switch][Patterns] ECJ generated code hangs
	public void testIssue3320() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
							static boolean guard(String prefix) {
								System.out.println(prefix + " guard");
								return false;
							}
							public static void main(String [] args) {
						        Object o = new Object();
							    switch (o) {
							        case String _, Object _ when guard("First") -> System.out.println("First case");  // first println
							        case String _, Object _ when guard("Second") -> System.out.println("Second case");  // first println
							        default -> System.out.println("Default");
							    }
							}
						}
						"""
				},
			"First guard\nSecond guard\nDefault");
	}
}
