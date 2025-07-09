/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class JEP441SnippetsTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test26"};
	}

	public static Class<?> testClass() {
		return JEP441SnippetsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}
	public JEP441SnippetsTest(String testName){
		super(testName);
	}

	public void test01() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {

						static String formatterPatternSwitch(Object obj) {
						    return switch (obj) {
						        case Integer i -> String.format("int %d", i);
						        case Long l    -> String.format("long %d", l);
						        case Double d  -> String.format("double %f", d);
						        case String s  -> String.format("String %s", s);
						        default        -> obj.toString();
						    };
						}

						@Override
						public String toString() {
							return "X X";
						}

						public static void main(String[] args) {
							System.out.println(formatterPatternSwitch(10));
							System.out.println(formatterPatternSwitch(10L));
							System.out.println(formatterPatternSwitch(10.0));
							System.out.println(formatterPatternSwitch("String"));
							System.out.println(formatterPatternSwitch(new X()));
						}
					}
					"""
				},
				"int 10\n" +
				"long 10\n" +
				"double 10.000000\n" +
				"String String\n" +
				"X X");
	}

	public void test02() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {

						static void testFooBarNew(String s) {
						    switch (s) {
						        case null         -> System.out.println("Oops");
						        case "Foo", "Bar" -> System.out.println("Great");
						        default           -> System.out.println("Ok");
						    }
						}


						public static void main(String[] args) {
							testFooBarNew(null);
							testFooBarNew("foo");
							testFooBarNew("Foo");
							testFooBarNew("Bar");
						}
					}
					"""
				},
				"Oops\n" +
				"Ok\n" +
				"Great\n" +
				"Great");
	}

	public void test03() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {

						static void testStringOld(String response) {
						    switch (response) {
						        case null -> { }
						        case String s -> {
						            if (s.equalsIgnoreCase("YES"))
						                System.out.println("You got it");
						            else if (s.equalsIgnoreCase("NO"))
						                System.out.println("Shame");
						            else
						                System.out.println("Sorry?");
						        }
						    }
						}

						static void testStringNew(String response) {
						    switch (response) {
						        case null -> { }
						        case String s
						        when s.equalsIgnoreCase("YES") -> {
						            System.out.println("You got it");
						        }
						        case String s
						        when s.equalsIgnoreCase("NO") -> {
						            System.out.println("Shame");
						        }
						        case String s -> {
						            System.out.println("Sorry?");
						        }
						    }
						}

						static void testStringEnhanced(String response) {
						    switch (response) {
						        case null -> { }
						        case "y", "Y" -> {
						            System.out.println("You got it");
						        }
						        case "n", "N" -> {
						            System.out.println("Shame");
						        }
						        case String s
						        when s.equalsIgnoreCase("YES") -> {
						            System.out.println("You got it");
						        }
						        case String s
						        when s.equalsIgnoreCase("NO") -> {
						            System.out.println("Shame");
						        }
						        case String s -> {
						            System.out.println("Sorry?");
						        }
						    }
						}


						public static void main(String[] args) {
							testStringOld(null);
							testStringOld("Yes");
							testStringOld("NO");
							testStringOld("Bar");

							testStringNew(null);
							testStringNew("Yes");
							testStringNew("NO");
							testStringNew("Bar");

							testStringEnhanced(null);
							testStringEnhanced("Y");
							testStringEnhanced("y");
							testStringEnhanced("N");
							testStringEnhanced("n");
							testStringEnhanced("Yes");
							testStringEnhanced("NO");
							testStringEnhanced("Bar");

						}
					}
					"""
				},
				"You got it\n" +
				"Shame\n" +
				"Sorry?\n" +
				"You got it\n" +
				"Shame\n" +
				"Sorry?\n" +
				"You got it\n" +
				"You got it\n" +
				"Shame\n" +
				"Shame\n" +
				"You got it\n" +
				"Shame\n" +
				"Sorry?");
	}

	public void test04() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				sealed interface CardClassification permits Suit, Tarot {
				}

				enum Suit implements CardClassification {
					CLUBS, DIAMONDS, HEARTS, SPADES
				}

				final class Tarot implements CardClassification {
				}

				public class X {
					static void exhaustiveSwitchWithoutEnumSupport(CardClassification c) {
					    switch (c) {
					        case Suit s when s == Suit.CLUBS -> {
					            System.out.println("It's clubs");
					        }
					        case Suit s when s == Suit.DIAMONDS -> {
					            System.out.println("It's diamonds");
					        }
					        case Suit s when s == Suit.HEARTS -> {
					            System.out.println("It's hearts");
					        }
					        case Suit s -> {
					            System.out.println("It's spades");
					        }
					        case Tarot t -> {
					            System.out.println("It's a tarot");
					        }
					    }
					}

					public static void main(String[] args) {
						exhaustiveSwitchWithoutEnumSupport(Suit.CLUBS);
						exhaustiveSwitchWithoutEnumSupport(Suit.DIAMONDS);
						exhaustiveSwitchWithoutEnumSupport(Suit.HEARTS);
						exhaustiveSwitchWithoutEnumSupport(Suit.SPADES);
						exhaustiveSwitchWithoutEnumSupport(new Tarot());
					}
				}
				"""
			},
			"It's clubs\n" +
			"It's diamonds\n" +
			"It's hearts\n" +
			"It's spades\n" +
			"It's a tarot");
	}

	public void test05() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				sealed interface CardClassification permits Suit, Tarot {}
				enum Suit implements CardClassification { CLUBS, DIAMONDS, HEARTS, SPADES }
				final class Tarot implements CardClassification {}
				public class X {
					static void exhaustiveSwitchWithBetterEnumSupport(CardClassification c) {
					    switch (c) {
					        case Suit.CLUBS -> {
					            System.out.println("It's clubs");
					        }
					        case Suit.DIAMONDS -> {
					            System.out.println("It's diamonds");
					        }
					        case Suit.HEARTS -> {
					            System.out.println("It's hearts");
					        }
					        case Suit.SPADES -> {
					            System.out.println("It's spades");
					        }
					        case Tarot t -> {
					            System.out.println("It's a tarot");
					        }
					    }
					}
					public static void main(String[] args) {
						exhaustiveSwitchWithBetterEnumSupport(Suit.CLUBS);
						exhaustiveSwitchWithBetterEnumSupport(Suit.DIAMONDS);
						exhaustiveSwitchWithBetterEnumSupport(Suit.HEARTS);
						exhaustiveSwitchWithBetterEnumSupport(Suit.SPADES);
						exhaustiveSwitchWithBetterEnumSupport(new Tarot());
					}
				}
				"""
			},
			"It's clubs\n" +
			"It's diamonds\n" +
			"It's hearts\n" +
			"It's spades\n" +
			"It's a tarot");
	}

	public void test06() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				// As of Java 21
				sealed interface Currency permits Coin {}
				enum Coin implements Currency { HEADS, TAILS }

				public class X {
					static void goodEnumSwitch1(Currency c) {
					    switch (c) {
					        case Coin.HEADS -> {    // Qualified name of enum constant as a label
					            System.out.println("Heads");
					        }
					        case Coin.TAILS -> {
					            System.out.println("Tails");
					        }
					    }
					}

					static void goodEnumSwitch2(Coin c) {
					    switch (c) {
					        case HEADS -> {
					            System.out.println("Heads");
					        }
					        case Coin.TAILS -> {    // Unnecessary qualification but allowed
					            System.out.println("Tails");
					        }
					    }
					}

					public static void main(String[] args) {
						goodEnumSwitch1(Coin.HEADS);
						goodEnumSwitch1(Coin.TAILS);

						goodEnumSwitch2(Coin.HEADS);
						goodEnumSwitch2(Coin.TAILS);

					}
				}
				"""
			},
			"Heads\n" +
			"Tails\n" +
			"Heads\n" +
			"Tails");
	}

	public void test07() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				// As of Java 21
				sealed interface Currency permits Coin {}
				enum Coin implements Currency { HEADS, TAILS }

				public class X {
					static void badEnumSwitch(Currency c) {
					    switch (c) {
					        case Coin.HEADS -> {
					            System.out.println("Heads");
					        }
					        case TAILS -> {         // Error - TAILS must be qualified
					            System.out.println("Tails");
					        }
					        default -> {
					            System.out.println("Some currency");
					        }
					    }
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	case TAILS -> {         // Error - TAILS must be qualified\n" +
			"	     ^^^^^\n" +
			"TAILS cannot be resolved to a variable\n" +
			"----------\n");
	}

	public void test08() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {

					static void testNew(Object obj) {
					    switch (obj) {
					        case String s when s.length() == 4 -> System.out.println("Bad word!");
					        case String s when s.length() == 1 -> System.out.println("Monsyllable");
					        default -> System.out.println("  ");
					    }
					}

					public static void main(String[] args) {
						testNew("X");
						testNew("***");
						testNew("*!#^");
					}
				}
				"""
			},
			"Monsyllable\n" +
			"  \n" +
			"Bad word!");
	}

	public void test09() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					record Point(int i, int j) {}
					enum Color { RED, GREEN, BLUE; }

					static void typeTester(Object obj) {
					    switch (obj) {
					        case null     -> System.out.println("null");
					        case String s -> System.out.println("String");
					        case Color c  -> System.out.println("Color: " + c.toString());
					        case Point p  -> System.out.println("Record class: " + p.toString());
					        case int[] ia -> System.out.println("Array of ints of length" + ia.length);
					        default       -> System.out.println("Something else");
					    }
					}

					public static void main(String[] args) {
						typeTester(null);
						typeTester("Hello");
						typeTester(Color.RED);
						typeTester(new Point(0, 0));
						typeTester(new int[10]);
						typeTester(10);
					}
				}
				"""
			},
			"null\n" +
			"String\n" +
			"Color: RED\n" +
			"Record class: Point[i=0, j=0]\n" +
			"Array of ints of length10\n" +
			"Something else");
	}

	public void test10() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {

					static void first(Object obj) {
					    switch (obj) {
					        case String s ->
					            System.out.println("A string: " + s);
					        case CharSequence cs ->
					            System.out.println("A sequence of length " + cs.length());
					        default -> {
					            break;
					        }
					    }
					}

					public static void main(String[] args) {
						first("Hello");
						first(new StringBuilder("Hello"));
					}
				}
				"""
			},
			"A string: Hello\n" +
			"A sequence of length 5");
	}

	public void test11() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {

					static void first(Object obj) {
					    switch (obj) {
					        case CharSequence cs ->
					            System.out.println("A sequence of length " + cs.length());
					        case String s ->
					            System.out.println("A string: " + s);
					        default -> {
					            break;
					        }
					    }
					}

					public static void main(String[] args) {
						first("Hello");
						first(new StringBuilder("Hello"));
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case String s ->\n" +
			"	     ^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}

	public void test12() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {

					static void first(Object obj) {
					    switch (obj) {
					        case String s ->
					        	System.out.println("A String: " + s);
					        case String s when s.length() > 0 ->
					            System.out.println("A string: " + s);
					        default -> {
					            break;
					        }
					    }
					}

					public static void main(String[] args) {
						first("Hello");
						first(new StringBuilder("Hello"));
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case String s when s.length() > 0 ->\n" +
			"	     ^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}

	public void test13() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {

					static void first(Object obj) {
					    switch (obj) {
					        case String s when s.length() > 0 ->
					            System.out.println("A guarded string: " + s);
					        case String s ->
				        		System.out.println("A String: " + s);
					        default -> {
					            break;
					        }
					    }
					}

					public static void main(String[] args) {
						first("Hello");
						first(new StringBuilder("Hello"));
					}
				}
				"""
			},
			"A guarded string: Hello");
	}

	public void test14() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {

					static void first(Object obj) {
					    switch (obj) {
					    	case String s when true ->
					    		System.out.println("A pseudo guarded string: " + s);
					        case String s when s.length() > 0 ->
					            System.out.println("A guarded string: " + s);
					        case String s ->
				        		System.out.println("A String: " + s);
					        default -> {
					            break;
					        }
					    }
					}

					public static void main(String[] args) {
						first("Hello");
						first(new StringBuilder("Hello"));
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case String s when s.length() > 0 ->\n" +
			"	     ^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	case String s ->\n" +
			"	     ^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}

	public void test15() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					static void first(Integer obj) {
					    switch (obj) {
					    	case Integer i ->
					    		System.out.println("An integer: " + i);
					    	case 42 ->
					    		System.out.println("42");
					        default -> {
					            break;
					        }
					    }
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	case 42 ->\n" +
			"	     ^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	default -> {\n" +
			"	^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n");
	}

	public void test16() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					enum E {
						ONE, TWO;
					}
					static void first(E e) {
					    switch (e) {
					    	case E e1 ->
					    		System.out.println("An ENUM: " + e1);
					    	case ONE ->
					    		System.out.println("42");
					        default -> {
					            break;
					        }
					    }
					}

					static void second(E e) {
					    switch (e) {
					    	case ONE ->
					    		System.out.println("An ENUM: ");
					    	case TWO ->
					    		System.out.println("42");
					        default -> {
					            break;
					        }
					    }
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	case ONE ->\n" +
			"	     ^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	default -> {\n" +
			"	^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n");
	}

	public void test17() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					static void first(Object selector) {
					    switch (selector) {
					    	case String s when s.length() > 1 ->
					    		System.out.println("Some string");
					    	case "Hello" ->
					    		System.out.println("42");
					        default -> {
					            break;
					        }
					    }
					}

					static void second(String selector) {
					    switch (selector) {
					    	case String s when s.length() > 1 ->
					    		System.out.println("Some string");
					    	case "Hello" ->
					    		System.out.println("42");
					    	case String s ->
					    		System.out.println("unconditional");
					        default -> {
					            break;
					        }
					    }
					}

					static void third(String selector) {
					    switch (selector) {
					    	case String s when s.length() > 1 ->
					    		System.out.println("Some string");
					    	case String s ->
				    			System.out.println("unconditional");
					    	case "Hello" ->
					    		System.out.println("42");
					        default -> {
					            break;
					        }
					    }
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	case \"Hello\" ->\n" +
			"	     ^^^^^^^\n" +
			"Case constant of type String is incompatible with switch selector type Object\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 22)\n" +
			"	default -> {\n" +
			"	^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 34)\n" +
			"	case \"Hello\" ->\n" +
			"	     ^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 36)\n" +
			"	default -> {\n" +
			"	^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n");
	}

	public void test18() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					static void first(Integer i) {
						switch (i) {
						    case -1, 1 -> System.out.println("Special cases");
						    case Integer j when j > 0 -> System.out.println("Positive integer cases");
						    case Integer j -> System.out.println("All the remaining integers");
						}
					}

					public static void main(String[] args) {
						first(-1);
						first(1);
						first(42);
						first(0);
					}
				}
				"""
			},
			"Special cases\n" +
			"Special cases\n" +
			"Positive integer cases\n" +
			"All the remaining integers");
	}

	public void test19() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					// As of Java 21
					static void matchAll(String s) {
					    switch(s) {
					        case String t:
					            System.out.println(t);
					            break;
					        default:
					            System.out.println("Something else");  // Error - dominated!
					    }
					}

					static void matchAll2(String s) {
					    switch(s) {
					        case Object o:
					            System.out.println("An Object");
					            break;
					        default:
					            System.out.println("Something else");  // Error - dominated!
					    }
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	default:\n" +
			"	^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 18)\n" +
			"	default:\n" +
			"	^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n");
	}

	public void test20() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					// As of Java 21
					static int coverage(Object obj) {
					    return switch (obj) {           // Error - not exhaustive
					        case String s -> s.length();
					    };
					}

					// As of Java 21
					static int coverage(Object obj) {
					    return switch (obj) {           // Error - still not exhaustive
					        case String s  -> s.length();
					        case Integer i -> i;
					    };
					}

					// As of Java 21
					static int coverage(Object obj) {
					    return switch (obj) {
					        case String s  -> s.length();
					        case Integer i -> i;
					        default -> 0;
					    };
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	static int coverage(Object obj) {\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^\n" +
			"Duplicate method coverage(Object) in type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	return switch (obj) {           // Error - not exhaustive\n" +
			"	               ^^^\n" +
			"A switch expression should have a default case\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	static int coverage(Object obj) {\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^\n" +
			"Duplicate method coverage(Object) in type X\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\n" +
			"	return switch (obj) {           // Error - still not exhaustive\n" +
			"	               ^^^\n" +
			"A switch expression should have a default case\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 18)\n" +
			"	static int coverage(Object obj) {\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^\n" +
			"Duplicate method coverage(Object) in type X\n" +
			"----------\n");
	}

	public void test21() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					enum Color { RED, YELLOW, GREEN }

					// As of Java 21
					static int coverage(Color color) {

						int numLetters = switch (color) {   // Exhaustive!
					    	case RED -> 3;
					    	case GREEN -> 5;
					    	case YELLOW -> 6;
						};

						numLetters = switch (color) {   // Error - not exhaustive!
					    	case RED -> 3;
					    	case GREEN -> 5;
						};

						numLetters = switch (color) {
					    	case RED -> 3;
					    	case GREEN -> 5;
					    	case YELLOW -> 6;
					    	default -> throw new UnsupportedOperationException(color.toString());
						};
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	numLetters = switch (color) {   // Error - not exhaustive!\n" +
			"	                     ^^^^^\n" +
			"A Switch expression should cover all possible values\n" +
			"----------\n");
	}

	public void test22() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					sealed interface S permits A, B, C {}
					final class A implements S {}
					final class B implements S {}
					record C(int i) implements S {}    // Implicitly final

					static int testSealedExhaustive(S s) {
					    return switch (s) {
					        case A a -> 1;
					        case B b -> 2;
					        case C c -> 3;
					    };
					}

					public static void main(String[] args) {
						System.out.println(testSealedExhaustive(new X().new A()));
						System.out.println(testSealedExhaustive(new X().new B()));
						System.out.println(testSealedExhaustive(new C(10)));

					}
				}
				"""
			},
			"1\n2\n3");
	}

	public void test23() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					sealed interface S permits A, B, C {}
					final class A implements S {}
					final class B implements S {}
					record C(int i) implements S {}    // Implicitly final

					static int testSealedExhaustive(S s) {
					    return switch (s) {
					        case A a -> 1;
					        case B b -> 2;
					    };
					}

					public static void main(String[] args) {
						System.out.println(testSealedExhaustive(new X().new A()));
						System.out.println(testSealedExhaustive(new X().new B()));
						System.out.println(testSealedExhaustive(new C(10)));

					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\r\n" +
			"	return switch (s) {\r\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}

	public void test24() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					sealed interface I<T> permits A, B {}
					final class A<X> implements I<String> {}
					final class B<Y> implements I<Y> {}

					static int testGenericSealedExhaustive(I<Integer> i) {
					    return switch (i) {
					        // Exhaustive as no A case possible!
					        case B<Integer> bi -> 42;
					    };
					}

					static int testGenericSealedExhaustive2(I<Integer> i) {
					    return switch (i) {
					        // Exhaustive as no A case possible!
					        case B<Integer> bi -> 42;
					        case A<Integer> ai -> 42;
					    };
					}
				}
				"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	final class A<X> implements I<String> {}\n" +
			"	              ^\n" +
			"The type parameter X is hiding the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 17)\n" +
			"	case A<Integer> ai -> 42;\n" +
			"	     ^^^^^^^^^^^^^\n" +
			"Incompatible operand types X.I<Integer> and X.A<Integer>\n" +
			"----------\n");
	}

	public void test25() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					// As of Java 21
					sealed interface S permits A, B, C {}
					final class A implements S {}
					final class B implements S {}
					record C(int i) implements S {}    // Implicitly final

					static void switchStatementExhaustive(S s) {
					    switch (s) {                   // Error - not exhaustive;
					                                   // missing clause for permitted class B!
					        case A a :
					            System.out.println("A");
					            break;
					        case C c :
					            System.out.println("C");
					            break;
					    };
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	switch (s) {                   // Error - not exhaustive;\n" +
			"	        ^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n");
	}

	public void test26() {
		if (this.complianceLevel < ClassFileConstants.JDK22) // multi pattern case labels used
			return;
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					static void testScope1(Object obj) {
					    switch (obj) {
					        case Character c
					        when c.charValue() == 7:
					            System.out.println("Ding!");
					            break;
					        default:
					            break;
					    }
					}

					static void testScope2(Object obj) {
					    switch (obj) {
					        case Character c -> {
					            if (c.charValue() == 7) {
					                System.out.println("Ding!");
					            }
					            System.out.println("Character");
					        }
					        case Integer i ->
					            throw new IllegalStateException("Invalid Integer argument: "
					                                            + i.intValue());
					        default -> {
					            break;
					        }
					    }
					}

					static void testScope3(Object obj) {
					    switch (obj) {
					        case Character c:
					            if (c.charValue() == 7) {
					                System.out.print("Ding ");
					            }
					            if (c.charValue() == 9) {
					                System.out.print("Tab ");
					            }
					            System.out.println("Character");
					        default:
					            System.out.println(c);
					    }
					}

					static void testScopeError(Object obj) {
					    switch (obj) {
					        case Character c:
					            if (c.charValue() == 7) {
					                System.out.print("Ding ");
					            }
					            if (c.charValue() == 9) {
					                System.out.print("Tab ");
					            }
					            System.out.println("character");
					        case Integer i:                 // Compile-time error
					            System.out.println("An integer " + i);
					        default:
					            break;
					    }
					}

					static void testScopeError2(Object obj) {
					    switch (obj) {
					        case Character c:
					            if (c.charValue() == 7) {
					                System.out.print("Ding ");
					            }
					            if (c.charValue() == 9) {
					                System.out.print("Tab ");
					            }
					            System.out.println("character");
					        case Character c, Integer i:                 // Compile-time error
					            System.out.println("An integer " + i);
					        default:
					            break;
					    }
					}

					void testScope4(Object obj) {
					    switch (obj) {
					        case String s:
					            System.out.println("A string: " + s);  // s in scope here!
					        default:
					            System.out.println("Done");            // s not in scope here
					    }
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 41)\n" +
			"	System.out.println(c);\n" +
			"	                   ^\n" +
			"c cannot be resolved to a variable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 55)\n" +
			"	case Integer i:                 // Compile-time error\n" +
			"	^^^^^^^^^^^^^^\n" +
			"Illegal fall-through to a pattern\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 72)\n" +
			"	case Character c, Integer i:                 // Compile-time error\n" +
			"	     ^^^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 72)\n" +
			"	case Character c, Integer i:                 // Compile-time error\n" +
			"	               ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 72)\n" +
			"	case Character c, Integer i:                 // Compile-time error\n" +
			"	                          ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 73)\n" +
			"	System.out.println(\"An integer \" + i);\n" +
			"	                                   ^\n" +
			"i cannot be resolved to a variable\n" +
			"----------\n");
	}

	public void test27() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					static void nullMatch(Object obj) {
					    switch (obj) {
					        case null     -> System.out.println("null!");
					        case String s -> System.out.println("String");
					        default       -> System.out.println("Something else");
					    }
					}

					static void nullMatch2(Object obj) {
					    switch (obj) {
					        case String s  -> System.out.println("String: " + s);
					        case Integer i -> System.out.println("Integer");
					        default        -> System.out.println("default");
					    }
					}

					static void nullMatch3(Object obj) {
					    switch (obj) {
					    	case null      -> throw new NullPointerException();
					        case String s  -> System.out.println("String: " + s);
					        case Integer i -> System.out.println("Integer");
					        default        -> System.out.println("default");
					    }
					}

					static void nullMatch4(Object obj) {
					    switch (obj) {
					        case String s  -> System.out.println("String: " + s);
					        case Integer i -> System.out.println("Integer");
					        case null, default        -> System.out.println("null|default");
					    }
					}

					public static void main(String[] args) {
						nullMatch(null);
						int nulls = 0;
						try {
							nullMatch2(null);
						} catch(NullPointerException npe) {
							nulls++;
						}
						try {
							nullMatch3(null);
						} catch(NullPointerException npe) {
							nulls++;
						}
						nullMatch4(null);
						if (nulls == 2) {
							System.out.println("OK!");
						}

					}
				}
				"""
			},
			"null!\nnull|default\nOK!");
	}

	public void test28() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					// As of Java 21
					record R(int i) {
					    public int i() {    // bad (but legal) accessor method for i
					        return i / 0;
					    }
					}

					static void exampleAnR(R r) {
					    switch(r) {
					        case R(var i): System.out.println(i);
					    }
					}

					static void example(Object obj) {
					    switch (obj) {
					        case R r when (r.i / 0 == 1): System.out.println("It's an R!");
					        default: break;
					    }
					}

					public static void main(String [] args) {
						try {
							exampleAnR(new R(42));
						} catch (MatchException m) {
							System.out.println("ME:Ok!");
						}

						try {
							example(new R(42));
						} catch (MatchException m) {
							System.out.println("Broken!");
						} catch(ArithmeticException ae) {
							System.out.println("AE:Ok!");
						}
					}

				}
				"""
			},
			"ME:Ok!\nAE:Ok!");
	}
}