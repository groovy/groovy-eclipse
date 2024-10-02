/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *								bug 320170
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 386181 - [compiler][null] wrong transition in UnconditionalFlowInfo.mergedWith()
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								Bug 453635 - [compiler][null] Update NullReferenceImplTests and friends
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.compiler.regression.NullReferenceImplTests.State;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo.AssertionFailedException;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A tests series especially meant to validate the internals of our null
 * reference analysis. See NullReferenceTest for tests targetted at
 * the source code compiler behavior level.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullReferenceImplTests extends NullReferenceTest {
	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test2050" };
//    	TESTS_NUMBERS = new int[] { 2061 };
//    	TESTS_NUMBERS = new int[] { 2999 };
//    	TESTS_RANGE = new int[] { 2050, -1 };
  	}

/**
 * A class to hold states as seen by the low level validation tests and machinery.
 * State provides:
 * - singletons for all possible states given the number of bits for the said
 *   states;
 * - semantic names for known states;
 * - printable representation of states as bit fields;
 * - coordination with other classes to perform transitive closure analysis, etc.
 */
	/*
		This is a tabular definition for states. It can be completed/leveraged by
		the Generator class so as to smoothen the transition between differing encodings
		of the states.
	// STATES DEFINITION START
			000000	start
			000001
			000010
			000011
			000100	pot. unknown
			000101
			000110
			000111
			001000	pot. non null
			001001
			001010
			001011
			001100	pot. nn & pot. un
			001101
			001110
			001111
			010000	pot. null
			010001
			010010
			010011
			010100	pot. n & pot. un
			010101
			010110
			010111
			011000	pot. n & pot. nn
			011001
			011010
			011011
			011100  pot. n & pot. nn & pot. un
			011101
			011110
			011111
			100000
			100001
			100010
			100011
			100100	def. unknown
			100101
			100110
			100111
			101000	def. non null
			101001
			101010
			101011
			101100	pot. nn & prot. nn
			101101
			101110
			101111
			110000	def. null
			110001
			110010
			110011
			110100	pot. n & prot. n
			110101
			110110
			110111
			111000	prot. null
			111001
			111010
			111011
			111100	prot. non null
			111101
			111110
			111111
	// STATES DEFINITION END
	*/
	public static class State implements Comparable {
		// PREMATURE consider moving initialization to test setup/dispose
		public final static State[] states = {
			// STATES INITIALIZER START
			new State(0, "start"), // 000000
			new State(1), // 000001
			new State(2), // 000010
			new State(3), // 000011
			new State(4, "pot. unknown"), // 000100
			new State(5), // 000101
			new State(6), // 000110
			new State(7), // 000111
			new State(8, "pot. non null"), // 001000
			new State(9), // 001001
			new State(10), // 001010
			new State(11), // 001011
			new State(12, "pot. nn & pot. un"), // 001100
			new State(13), // 001101
			new State(14), // 001110
			new State(15), // 001111
			new State(16, "pot. null"), // 010000
			new State(17), // 010001
			new State(18), // 010010
			new State(19), // 010011
			new State(20, "pot. n & pot. un"), // 010100
			new State(21), // 010101
			new State(22), // 010110
			new State(23), // 010111
			new State(24, "pot. n & pot. nn"), // 011000
			new State(25), // 011001
			new State(26), // 011010
			new State(27), // 011011
			new State(28, "pot. n & pot. nn & pot. un"), // 011100
			new State(29), // 011101
			new State(30), // 011110
			new State(31), // 011111
			new State(32), // 100000
			new State(33), // 100001
			new State(34), // 100010
			new State(35), // 100011
			new State(36, "def. unknown"), // 100100
			new State(37), // 100101
			new State(38), // 100110
			new State(39), // 100111
			new State(40, "def. non null"), // 101000
			new State(41), // 101001
			new State(42), // 101010
			new State(43), // 101011
			new State(44, "pot. nn & prot. nn"), // 101100
			new State(45), // 101101
			new State(46), // 101110
			new State(47), // 101111
			new State(48, "def. null"), // 110000
			new State(49), // 110001
			new State(50), // 110010
			new State(51), // 110011
			new State(52, "pot. n & prot. n"), // 110100
			new State(53), // 110101
			new State(54), // 110110
			new State(55), // 110111
			new State(56, "prot. null"), // 111000
			new State(57), // 111001
			new State(58), // 111010
			new State(59), // 111011
			new State(60, "prot. non null"), // 111100
			new State(61), // 111101
			new State(62), // 111110
			new State(63), // 111111
			// STATES INITIALIZER END
		};
		public final static State start = states[0];
		public static final int
			stateMaxValue = 0x3F,
			stateWidth = 6,
			statesNb = stateMaxValue + 1;
		String name, printableBitsField, hexString;
		public byte value;
		boolean symbolic;
	private State() {
	}
	private State(int numericValue) {
		this(numericValue, null);
	}
	private State(int numericValue, String publicName) {
		if (numericValue > stateMaxValue) {
			throw new IllegalArgumentException("state value overflow");
		}
		this.value = (byte) numericValue;
		StringBuilder printableValue = new StringBuilder(6);
		for (int i = stateWidth - 1; i >= 0; i--) {
			printableValue.append((numericValue >>> i & 1) != 0 ? '1' : '0');
		}
		this.printableBitsField = printableValue.toString();
		if (this.value > 0xF) {
			this.hexString = "0x" + Integer.toHexString(this.value).toUpperCase();
		}
		else {
			this.hexString = "0x0" + Integer.toHexString(this.value).toUpperCase();
		}
		if (publicName != null) {
			this.name = publicName;
			this.symbolic = true;
		}
		else {
			this.name = this.printableBitsField;
		}
	}
	private State(String commentLine) {
		char current = ' '; // keep the initialization status quiet
		int cursor, length;
		for (cursor = 0, length = commentLine.length();
			cursor < length;
			cursor++) {
			if ((current = commentLine.charAt(cursor)) == '0' ||
					current == '1') {
				break;
			}
		}
		if (cursor == length) {
			throw new RuntimeException("bad state definition format (missing bits field): " + commentLine);
			// PREMATURE adopt consistent error policy
		}
		int valueDigits;
		for (valueDigits = 1; cursor < (length - 1) && valueDigits < stateWidth; valueDigits++) {
			this.value = (byte) ((this.value << 1) + (current - '0'));
			if ((current = commentLine.charAt(++cursor)) != '0' &&
					current != '1') {
				throw new RuntimeException("bad state definition format (inappropriate character in bits field): " + commentLine);
				// PREMATURE adopt consistent error policy
			}
		}
		if (valueDigits < stateWidth) {
			throw new RuntimeException("bad state definition format (bits field is too short): " + commentLine);
			// PREMATURE adopt consistent error policy
		}
		this.value = (byte) ((this.value << 1) + (current - '0'));
		this.printableBitsField = commentLine.substring(cursor - stateWidth + 1, cursor + 1);
		if (this.value > 0xF) {
			this.hexString = "0x" + Integer.toHexString(this.value).toUpperCase();
		}
		else {
			this.hexString = "0x0" + Integer.toHexString(this.value).toUpperCase();
		}
		while (++cursor < length && Character.isWhitespace(current = commentLine.charAt(++cursor)) && current != '\n') {
			// loop
		}
		if (cursor < length && current != '\n') {
			this.name = commentLine.substring(cursor, length);
		}
		if (this.name == null) {
			this.name = this.printableBitsField;
		} else {
			this.symbolic = true;
		}
	}
	private String asInitializer() {
		StringBuilder result = new StringBuilder(70);
		result.append("		new State(");
		result.append(this.value);
		char first;
		boolean nameIsSymbolic = (first = this.name.charAt(0)) != '0'
			&& first != '1';
		if (nameIsSymbolic) {
			result.append(", \"");
			result.append(this.name);
			result.append('"');
		}
		result.append("), // ");
		result.append(this.printableBitsField);
		return result.toString();
	}
	long [] asLongArray() {
		long[] result = new long[stateWidth];
		for (int i = 0; i < stateWidth; i++) {
			result[i] = ((this.value >> (stateWidth - i - 1)) & 1) == 0 ? 0 : 1;
		}
		return result;
	}
	private String asSourceComment() {
		StringBuilder result = new StringBuilder(70);
		result.append("\t\t");
		result.append(this.printableBitsField);
		char first;
		boolean nameIsSymbolic = (first = this.name.charAt(0)) != '0'
			&& first != '1';
		if (nameIsSymbolic) {
			result.append('\t');
			result.append(this.name);
		}
		return result.toString();
	}
	@Override
	public int compareTo(Object o) {
		return this.value - ((State) o).value;
	}
	static State fromLongValues(long bit1, long bit2, long bit3, long bit4, long bit5, long bit6) {
		// PREMATURE consider taking an UnconditionalFlowInfo in parameter
		return states[(int)(
			(bit6 & 1) +
				2 * ((bit5 & 1) +
					2 * ((bit4 & 1) +
						2 * ((bit3 & 1) +
							2 * ((bit2 & 1) +
								2 * (bit1 & 1))))))];
	}
	private static Map namesIndex;
	static State fromSymbolicName (String name) {
		if (namesIndex == null) {
			namesIndex = new HashMap(states.length);
			for (int i = 0; i < states.length; i++) {
				if (states[i].name != null) {
					namesIndex.put(states[i].name, states[i]);
				}
			}
		}
		return (State) namesIndex.get(name);
	}
	private static void grabDefinitionFromComment(BufferedReader input) {
		String line;
		State current;
	// use when the initializer is incomplete, hence needs to be reinitialized
	//	states = new State[stateMaxValue + 1];
	// use when the states field is final, with the appropriate size:
		for (int i = 0; i <= stateMaxValue; i++) {
			states[i] = null;
		}
		try {
			while ((line = input.readLine()) != null && line.indexOf(definitionEndMarker) == -1) {
				current = new State(line);
				if (states[current.value] != null) {
					throw new RuntimeException("duplicate state for index: " + current.value);
				}
				else {
					states[current.value] = current;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < stateMaxValue; i++) {
			if (states[i] == null) {
				states[i] = new State(i);
			}
		}
	}
	// PREMATURE may decide to remove
	//private static void printAsInitializer() {
	//	int i, length;
	//	System.out.println(initializerStartMarker);
	//	for (i = 0, length = states.length; i < length; i++) {
	//		System.out.println(states[i].asInitializer());
	//	}
	//	for (/* continue */; i <= stateMaxValue; i++) {
	//		System.out.println((new State(i)).asInitializer() + " CHECK");
	//	}
	//	System.out.println(initializerEndMarker);
	//}
	// PREMATURE may decide to remove
	//private static void printAsSourceComment() {
	//	int i, length;
	//	System.out.println("/*");
	//	System.out.println(definitionStartMarker);
	//	for (i = 0, length = states.length; i < length; i++) {
	//		System.out.println(states[i].asSourceComment());
	//	}
	//	for (/* continue */; i <= stateMaxValue; i++) {
	//		System.out.println((new State(i)).asSourceComment());
	//	}
	//	System.out.println(definitionEndMarker);
	//	System.out.println("*/");
	//}
	private final static String
		definitionStartMarker = "// STATES " + CodeAnalysis.definitionStartMarker,
		definitionEndMarker = "// STATES " + CodeAnalysis.definitionEndMarker,
		initializerStartMarker = "// STATES " + CodeAnalysis.initializerStartMarker,
		initializerEndMarker = "// STATES " + CodeAnalysis.initializerEndMarker;
	static void reinitializeFromComment(BufferedReader input, BufferedWriter output) {
		String line, tab = "";
		int cursor;
		char c;
		try {
			while ((line = input.readLine()) != null) {
				output.write(line);
				output.write('\n');
				if ((cursor = line.indexOf(definitionStartMarker)) != -1) {
					// check the line format
					boolean reachedStart = true;
					for (int i = 0; i < cursor; i++) {
						if (!Character.isWhitespace(c = line.charAt(i))) {
							reachedStart = false;
							break;
						}
						else {
							tab += c;
						}
					}
					if (reachedStart) {
						grabDefinitionFromComment(input); // consumes up to the END line
						int i, length;
						for (i = 0, length = states.length; i < length; i++) {
							output.write(states[i].asSourceComment());
							output.write('\n');
						}
						output.write(tab + definitionEndMarker + "\n");
					}
				}
				if ((cursor = line.indexOf(initializerStartMarker)) != -1) {
					// check the line format
					boolean reachedStart = true;
					tab = "";
					for (int i = 0; i < cursor; i++) {
						if (!Character.isWhitespace(c = line.charAt(i))) {
							reachedStart = false;
							break;
						}
						else {
							tab += c;
						}
					}
					if (reachedStart) {
						while ((line = input.readLine()) != null &&
								line.indexOf(initializerEndMarker) == -1) {
							// loop
						}
						int i, length;
						for (i = 0, length = states.length; i < length; i++) {
							output.write(states[i].asInitializer());
							output.write('\n');
						}
						output.write(tab + initializerEndMarker + "\n");
					}
				}
			}
			output.flush();
			namesIndex = null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	static Iterator symbolicStates() {
		return new Iterator() {
			int nextSymbolic = -1;
			@Override
			public boolean hasNext() {
				if (this.nextSymbolic == -1) {
					for (this.nextSymbolic = 0; this.nextSymbolic < states.length; this.nextSymbolic++) {
						if (states[this.nextSymbolic].symbolic) {
							break;
						}
					}
				} else {
					for (; this.nextSymbolic < states.length; this.nextSymbolic++) {
						if (states[this.nextSymbolic].symbolic) {
							break;
						}
					}
				}
				return this.nextSymbolic < states.length;
			}
			@Override
			public Object next() {
				State result = null;
				if (this.nextSymbolic < states.length) {
					result = states[this.nextSymbolic];
					this.nextSymbolic++;
				}
				return result;
			}
			@Override
			public void remove() {
				throw new RuntimeException("unimplemented");
			}
		};
	}
		@Override
	public String toString() {
		return this.name;
	}
		@Override
	public boolean equals(Object other) {
		return (other instanceof State) && ((State)other).value == this.value;
	}
		@Override
	public int hashCode() {
		return this.value;
	}
	}

public NullReferenceImplTests(String name) {
    super(name);
}

  	// Tests tuning
	// private static final boolean skipHighOrderBits = false; // define to true when tuning encoding
	private static final int COMBINATION_TESTS_LOOP_NB = 1; // define to 10000s to measure performances
	private static final boolean MEASURE_PERFORMANCES = COMBINATION_TESTS_LOOP_NB > 1;

public static Test suite() {
	// we do not want to run for 1.3, 1.4, 1.5 but once only
    Class clazz = testClass();
    TestSuite all = new TestSuite(clazz.getName());
    List tests = buildTestsList(testClass());
    for (int i = 0, length = tests.size(); i < length; i++) {
    	all.addTest((Test) tests.get(i));
    }
	return all;
}

public static Class testClass() {
    return NullReferenceImplTests.class;
}

public void test2050_markAsComparedEqualToNonNull() {
	int failures = NullReferenceImplTransformations.markAsComparedEqualToNonNull.test();
	assertTrue("nb of failures: " + failures, failures == 0);
}

public void test2051_markAsComparedEqualToNull() {
	int failures = NullReferenceImplTransformations.markAsComparedEqualToNull.test();
	assertTrue("nb of failures: " + failures, failures == 0);
}

public void test2055_markAsDefinitelyNonNull() {
	int failures = NullReferenceImplTransformations.markAsDefinitelyNonNull.test();
	assertTrue("nb of failures: " + failures, failures == 0);
}

public void test2056_markAsDefinitelyNull() {
	int failures = NullReferenceImplTransformations.markAsDefinitelyNull.test();
	assertTrue("nb of failures: " + failures, failures == 0);
}

public void test2057_markAsDefinitelyUnknown() {
	int failures = NullReferenceImplTransformations.markAsDefinitelyUnknown.test();
	assertTrue("nb of failures: " + failures, failures == 0);
}

public void test2060_addInitializationsFrom() {
	int failures = NullReferenceImplTransformations.addInitializationsFrom.test();
	assertTrue("nb of failures: " + failures, failures == 0);
}

public void test2061_addPotentialInitializationsFrom() {
	int failures = NullReferenceImplTransformations.addPotentialInitializationsFrom.test();
	assertTrue("nb of failures: " + failures, failures == 0);
}

public void test2062_mergedWith() {
	int failures = NullReferenceImplTransformations.mergedWith.test();
	assertTrue("nb of failures: " + failures, failures == 0);
}

// PREMATURE rewrite from scratch
//public void _test2058_recode() {
//	long [][][] testData = transitionsTablesData[recode];
//	int failures = 0;
//	long start;
//	if (combinationTestsloopsNb > 1) {
//		start = System.currentTimeMillis();
//	}
//	String header = "recode failures: ";
//	for (int l = 0; l < combinationTestsloopsNb ; l++) {
//		for (int i = 0; i < testData.length; i++) {
//			UnconditionalFlowInfoTestHarness result;
//			result = UnconditionalFlowInfoTestHarness.
//						testUnconditionalFlowInfo(testData[i][0]);
//			result.encode();
//			result.decode();
//
//			if (!result.testEquals(UnconditionalFlowInfoTestHarness.
//						testUnconditionalFlowInfo(testData[i][0]))) {
//				if (failures == 0) {
//					System.out.println(header);
//				}
//				failures++;
//				System.out.println("\t\t{" + result.testString() +
//					"}, // instead of: " + testStringValueOf(testData[i][0]));
//			}
//		}
//	}
//	if (combinationTestsloopsNb > 1) {
//		System.out.println("mergedWith\t\t\t" + combinationTestsloopsNb + "\t" +
//				(System.currentTimeMillis() - start));
//	}
//	for (int i = 0; i < testData.length; i++) {
//		UnconditionalFlowInfoTestHarness result;
//		result = UnconditionalFlowInfoTestHarness.
//					testUnconditionalFlowInfo(testData[i][0], 64);
//		result.encode();
//		result.decode();
//
//		if (!result.testEquals(UnconditionalFlowInfoTestHarness.
//					testUnconditionalFlowInfo(testData[i][0], 64))) {
//			if (failures == 0) {
//				System.out.println(header);
//			}
//			failures++;
//			System.out.println("\t\t{" + result.testString() +
//				"}, // (64) - instead of: " + testStringValueOf(testData[i][0]));
//		}
//	}
//	assertTrue("nb of failures: " + failures, failures == 0);
//}

public void test2400_state_consistency() {
	int failures = 0;
	long start;
	if (MEASURE_PERFORMANCES) {
		start = System.currentTimeMillis();
	}
	String header = "state consistency failures: ";
	for (int l = 0; l < COMBINATION_TESTS_LOOP_NB ; l++) {
		for (int i = 0; i < State.states.length; i++) {
			if (State.states[i].symbolic) {
				UnconditionalFlowInfoTestHarness
					state = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(State.states[i]);
				boolean
					isDefinitelyNonNull = state.isDefinitelyNonNull(TestLocalVariableBinding.local0),
					isDefinitelyNull = state.isDefinitelyNull(TestLocalVariableBinding.local0),
					isDefinitelyUnknown = state.isDefinitelyUnknown(TestLocalVariableBinding.local0),
					isPotentiallyNonNull = state.isPotentiallyNonNull(TestLocalVariableBinding.local0),
					isPotentiallyNull = state.isPotentiallyNull(TestLocalVariableBinding.local0),
					isPotentiallyUnknown = state.isPotentiallyUnknown(TestLocalVariableBinding.local0),
					isProtectedNonNull = state.isProtectedNonNull(TestLocalVariableBinding.local0),
					isProtectedNull = state.isProtectedNull(TestLocalVariableBinding.local0),
					cannotBeDefinitelyNullOrNonNull = state.cannotBeDefinitelyNullOrNonNull(TestLocalVariableBinding.local0),
					cannotBeNull = state.cannotBeNull(TestLocalVariableBinding.local0),
					canOnlyBeNull = state.canOnlyBeNull(TestLocalVariableBinding.local0);
				if (isDefinitelyNonNull
							&& (isDefinitelyNull || isDefinitelyUnknown
									|| isPotentiallyNull
									|| isProtectedNull)) {
					if (failures == 0) {
						System.out.println(header);
					}
					failures++;
					System.out.println("\t\tconsistency breakage for definitely non null state " + State.states[i].name);
				}
				if (isDefinitelyNull
							&& (isDefinitelyNonNull || isDefinitelyUnknown
									|| isPotentiallyUnknown || isProtectedNonNull)) {
					if (failures == 0) {
						System.out.println(header);
					}
					failures++;
					System.out.println("\t\tconsistency breakage for definitely null state " + State.states[i].name);
				}
				if (isDefinitelyUnknown
							&& (isDefinitelyNonNull || isDefinitelyNull
									|| isPotentiallyNull || isProtectedNonNull
									|| isProtectedNull)) {
					if (failures == 0) {
						System.out.println(header);
					}
					failures++;
					System.out.println("\t\tconsistency breakage for definitely unknown state " + State.states[i].name);
				}
				if (isProtectedNonNull && !isDefinitelyNonNull
						|| isProtectedNull && !isDefinitelyNull
						|| i > 0 // not start
							&& !State.states[i].name.equals("pot. non null")
							&& !(isDefinitelyNonNull || isDefinitelyNull
									|| isDefinitelyUnknown || isPotentiallyNull
									|| isPotentiallyUnknown || isProtectedNonNull
									|| isProtectedNull)
						|| cannotBeDefinitelyNullOrNonNull !=
							(isPotentiallyUnknown ||
								isPotentiallyNull && isPotentiallyNonNull)
						|| cannotBeNull != (isProtectedNonNull ||
								isDefinitelyNonNull)
						|| canOnlyBeNull != (isProtectedNull ||
								isDefinitelyNull)) {
					if (failures == 0) {
						System.out.println(header);
					}
					failures++;
					System.out.println("\t\tconsistency breakage for " + State.states[i].name);
				}
			}
		}
	}
	if (MEASURE_PERFORMANCES) {
		System.out.println("mergedWith\t\t\t" + COMBINATION_TESTS_LOOP_NB + "\t" +
				(System.currentTimeMillis() - start));
	}
	for (int i = 0; i < State.states.length; i++) {
		if (State.states[i].symbolic) {
			UnconditionalFlowInfoTestHarness state;
			state = UnconditionalFlowInfoTestHarness.
						testUnconditionalFlowInfo(State.states[i], 64);
			boolean
				isDefinitelyNonNull = state.isDefinitelyNonNull(TestLocalVariableBinding.local64),
				isDefinitelyNull = state.isDefinitelyNull(TestLocalVariableBinding.local64),
				isDefinitelyUnknown = state.isDefinitelyUnknown(TestLocalVariableBinding.local64),
				isPotentiallyNonNull = state.isPotentiallyNonNull(TestLocalVariableBinding.local64),
				isPotentiallyNull = state.isPotentiallyNull(TestLocalVariableBinding.local64),
				isPotentiallyUnknown = state.isPotentiallyUnknown(TestLocalVariableBinding.local64),
				isProtectedNonNull = state.isProtectedNonNull(TestLocalVariableBinding.local64),
				isProtectedNull = state.isProtectedNull(TestLocalVariableBinding.local64),
				cannotBeDefinitelyNullOrNonNull = state.cannotBeDefinitelyNullOrNonNull(TestLocalVariableBinding.local64),
				cannotBeNull = state.cannotBeNull(TestLocalVariableBinding.local64),
				canOnlyBeNull = state.canOnlyBeNull(TestLocalVariableBinding.local64);
				if (isDefinitelyNonNull
							&& (isDefinitelyNull || isDefinitelyUnknown
									|| isPotentiallyNull
									|| isProtectedNull)) {
					if (failures == 0) {
						System.out.println(header);
					}
					failures++;
					System.out.println("\t\tconsistency breakage (64) for definitely non null state " + State.states[i].name);
				}
				if (isDefinitelyNull
							&& (isDefinitelyNonNull || isDefinitelyUnknown
									|| isPotentiallyUnknown || isProtectedNonNull)) {
					if (failures == 0) {
						System.out.println(header);
					}
					failures++;
					System.out.println("\t\tconsistency breakage (64) for definitely null state " + State.states[i].name);
				}
				if (isDefinitelyUnknown
							&& (isDefinitelyNonNull || isDefinitelyNull
									|| isPotentiallyNull || isProtectedNonNull
									|| isProtectedNull)) {
					if (failures == 0) {
						System.out.println(header);
					}
					failures++;
					System.out.println("\t\tconsistency breakage (64) for definitely unknown state " + State.states[i].name);
				}
				if (isProtectedNonNull && !isDefinitelyNonNull
						|| isProtectedNull && !isDefinitelyNull
						|| i > 0 // not start
							&& !State.states[i].name.equals("pot. non null")
							&& !(isDefinitelyNonNull || isDefinitelyNull
									|| isDefinitelyUnknown || isPotentiallyNull
									|| isPotentiallyUnknown || isProtectedNonNull
									|| isProtectedNull)
									|| cannotBeDefinitelyNullOrNonNull !=
										(isPotentiallyUnknown ||
											isPotentiallyNull &&
												isPotentiallyNonNull)
									|| cannotBeNull != (isProtectedNonNull ||
											isDefinitelyNonNull)
									|| canOnlyBeNull != (isProtectedNull ||
											isDefinitelyNull)) {
					if (failures == 0) {
						System.out.println(header);
					}
					failures++;
					System.out.println("\t\tconsistency breakage (64) for " + State.states[i].name);
				}
		}
	}
	assertTrue("nb of failures: " + failures, failures == 0);
}

public void test2500_addInitializationsFrom_for_definites() {
	// when an added initialization is a def. something, it should
	// affect the left hand term as the markAsDefinite* method would
	// do
	int failures = 0;
	for (int i = 0; i < State.states.length; i++) {
		if (State.states[i].symbolic) {
			UnconditionalFlowInfoTestHarness source1, source2, result1, result2;
			source1 = UnconditionalFlowInfoTestHarness.
				testUnconditionalFlowInfo(State.states[i]);
			for (int j = 0; j < State.states.length; j++) {
				if (State.states[j].symbolic) {
					source2 = UnconditionalFlowInfoTestHarness.
						testUnconditionalFlowInfo(State.states[j]);
					result1 = (UnconditionalFlowInfoTestHarness) source1.copy();
					result2 = (UnconditionalFlowInfoTestHarness) source1.copy();
					if (source2.isDefinitelyNonNull(TestLocalVariableBinding.local0)) {
						if (! source2.isProtectedNonNull(TestLocalVariableBinding.local0)) {
							result1.markAsDefinitelyNonNull(TestLocalVariableBinding.local0);
						} else {
							continue;
						}
					}
					else if (source2.isDefinitelyNull(TestLocalVariableBinding.local0)) {
						if (! source2.isProtectedNull(TestLocalVariableBinding.local0)) {
							result1.markAsDefinitelyNull(TestLocalVariableBinding.local0);
						} else {
							continue;
						}
					}
					else if (source2.isDefinitelyUnknown(TestLocalVariableBinding.local0)) {
						result1.markAsDefinitelyUnknown(TestLocalVariableBinding.local0);
					}
					else if (source2.nullBit1 != 0) {
						if (failures == 0) {
							System.out.println("addInitializationsFrom_for_definites failures: "); //$NON-NLS-1$
						}
						failures++;
						System.out.println("\t\t" + State.states[j].name +
							" should answer true to at least one isDefinite* query");
						// PREMATURE move to specific queries test case
					}
					else {
						continue;
					}
					result2.addInitializationsFrom(source2);
					if (!result1.testEquals(result2)) {
						if (failures == 0) {
							System.out.println("addInitializationsFrom_for_definites failures: "); //$NON-NLS-1$
						}
						failures++;
						System.out.println("\t\t" + State.states[i].name +
							" + " + State.states[j].name +
							" => " + result2.asState().name +
							" instead of: " + result1.asState().name);
					}
				}
			}
		}
	}
	assertTrue("nb of failures: " + failures, failures == 0);
}

// Use for coverage tests only. Needs specific instrumentation of code,
// that is controled by UnconditionalFlowInfo#coverageTestFlag.
// Note: coverage tests tend to fill the console with messages, and the
//       instrumented code is slower, so never release code with active
//       coverage tests.
private static int coveragePointsNb = 45;

// PREMATURE reactivate coverage tests
// Coverage by state transition tables methods.
public void test2998_coverage() {
	if (UnconditionalFlowInfo.COVERAGE_TEST_FLAG) {
		// sanity check: need to be sure that the tests execute properly when not
		// trying to check coverage
		UnconditionalFlowInfo.CoverageTestId = 0;
		test0053_array();
		test0070_type_reference();
		test2050_markAsComparedEqualToNonNull();
		test2051_markAsComparedEqualToNull();
		test2055_markAsDefinitelyNonNull();
		test2056_markAsDefinitelyNull();
		test2057_markAsDefinitelyUnknown();
		test2060_addInitializationsFrom();
		test2061_addPotentialInitializationsFrom();
		test2062_mergedWith();
		testBug292478();
		testBug292478c();
		test0331_if_else_nested();
		testBug325755b();
		testBug292478g();
		// coverage check
		int failuresNb = 0;
		for (int i = 1; i <= coveragePointsNb; i++) {
			if (i == 11 || i == 12 || i == 14) {
				continue;
				// these can only be reached via a direct call to addPotentialNullInfoFrom,
				// which is not implemented in low level tests - all those go through
				// addPotentialInitsFrom
			}
			try {
				UnconditionalFlowInfo.CoverageTestId = i;
				test0053_array();
				test0070_type_reference();
				test2050_markAsComparedEqualToNonNull();
				test2051_markAsComparedEqualToNull();
				test2055_markAsDefinitelyNonNull();
				test2056_markAsDefinitelyNull();
				test2057_markAsDefinitelyUnknown();
				test2060_addInitializationsFrom();
				test2061_addPotentialInitializationsFrom();
				test2062_mergedWith();
				testBug292478();
				testBug292478c();
				test0331_if_else_nested();
				testBug325755b();
				testBug292478g();
			}
			catch (AssertionFailedError e) {
				continue;
			}
			catch (AssertionFailedException e) {
				continue;
			}
			failuresNb++;
			System.out.println("Missing coverage point: " + i);
		}
		UnconditionalFlowInfo.CoverageTestId = 0; // reset for other tests
		assertEquals(failuresNb + " missing coverage point(s)", failuresNb, 0);
	}
}

// Coverage by code samples.
public void test2999_coverage() {
	if (UnconditionalFlowInfo.COVERAGE_TEST_FLAG) {
		// sanity check: need to be sure that the tests execute properly when not
		// trying to check coverage
		UnconditionalFlowInfo.CoverageTestId = 0;
		test0001_simple_local();
		test0053_array();
		test0070_type_reference();
		test0327_if_else();
		test0401_while();
		test0420_while();
		test0509_try_finally_embedded();
		test2000_flow_info();
		test2004_flow_info();
		test2008_flow_info();
		test2011_flow_info();
		test2013_flow_info();
		test2018_flow_info();
		test2019_flow_info();
		test2020_flow_info();
		// coverage check
		int failuresNb = 0;
		for (int i = 1; i <= coveragePointsNb; i++) {
			if (i < 3
				|| 4 < i && i < 11
				|| 11 < i && i < 16
				|| 16 < i && i < 20
				|| i == 21
				|| 23 < i && i < 27
				|| 29 < i && i < 33
				|| 36 < i) { // TODO (maxime) complete coverage tests
				continue;
			}
			try {
				UnconditionalFlowInfo.CoverageTestId = i;
				test0001_simple_local();
				test0053_array();
				test0070_type_reference();
				test0327_if_else();
				test0401_while();
				test0420_while();
				test0509_try_finally_embedded();
				test2000_flow_info();
				test2004_flow_info();
				test2008_flow_info();
				test2011_flow_info();
				test2013_flow_info();
				test2018_flow_info();
				test2019_flow_info();
				test2020_flow_info();
			}
			catch (AssertionFailedError e) {
				continue;
			}
			catch (AssertionFailedException e) {
				continue;
			}
			failuresNb++;
			System.out.println("Missing coverage point: " + i);
		}
		UnconditionalFlowInfo.CoverageTestId = 0; // reset for other tests
		assertEquals(failuresNb + " missing coverage point(s)", failuresNb, 0);
	}
}

// only works for info coded on bit 0 - least significant
String testCodedValueOf(long[] data) {
	int length;
	StringBuilder result = new StringBuilder(length = data.length);
	for (int i = 0; i < length; i++) {
		result.append(data[i] == 0 ? '0' : '1');
	}
	return result.toString();
}

static String testStringValueOf(long[] data) {
	int length;
	StringBuilder result = new StringBuilder((length = data.length) * 2 + 1);
	result.append('{');
	for (int i = 0; i < length; i++) {
		if (i > 0) {
			result.append(',');
		}
		result.append(data[i]);
	}
	result.append('}');
	return result.toString();
}
}

/**
 * A specific extension of LocalVariableBinding suitable for flow info
 * manipulation at an implementation level.
 */
class TestLocalVariableBinding extends LocalVariableBinding {
	static class TestTypeBinding extends TypeBinding {
		public TestTypeBinding() {
			this.tagBits = 0L;
		}
		public char[] constantPoolName() {
			return null;
		}
		public PackageBinding getPackage() {
			return null;
		}
		public boolean isCompatibleWith(TypeBinding right, Scope captureScope) {
			return false;
		}
		public char[] qualifiedSourceName() {
			return null;
		}
		public char[] sourceName() {
			return null;
		}
		public char[] readableName() {
			return null;
		}
	}
	final static TypeBinding testTypeBinding = new TestTypeBinding();
	final static char [] testName = {'t', 'e', 's', 't'};
	TestLocalVariableBinding(int id) {
		super(testName, testTypeBinding, 0, false);
		this.id = id;
	}
	public Constant constant() {
		return Constant.NotAConstant;
	}
	static final TestLocalVariableBinding
		local0 = new TestLocalVariableBinding(0),
		local64 = new TestLocalVariableBinding(64),
		local128 = new TestLocalVariableBinding(128);
}

/**
 * A class meant to augment
 * @link{org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo} with
 * capabilities in the test domain. It especially provides factories to build
 * fake flow info instances for use in state transitions validation.
 */
/*
 * Moreover, this class defines the implementation of key operations for the
 * benefit of itself and NullInfoRegistryTestHarness. Given the fact that the
 * latter could not extend UnconditionalFlowInfoTestHarness and
 * NullInfoRegistry, the code is factorized into static methods.
 */
class UnconditionalFlowInfoTestHarness extends UnconditionalFlowInfo {
	int testPosition;
	// Interface
/**
 * Return the state represented by this.
 * @return the state represented by this
 */
NullReferenceImplTests.State asState() {
	return asState(this, 0);
}

/**
 * Return the state represented by this for a variable encoded at a given position.
 * @param position - int the position of the considered variable
 * @return the state represented by this for a variable encoded at a given position
 */
NullReferenceImplTests.State asState(int position) {
	return asState(this, position);
}

public FlowInfo copy() {
	UnconditionalFlowInfoTestHarness copy =
		new UnconditionalFlowInfoTestHarness();
	copy.testPosition = this.testPosition;
	copy(this, copy);
	return copy;
}

public void markAsDefinitelyNonNull(LocalVariableBinding local) {
	grow(local.id + this.maxFieldCount);
	super.markAsDefinitelyNonNull(local);
}

public void markAsDefinitelyNull(LocalVariableBinding local) {
	grow(local.id + this.maxFieldCount);
	super.markAsDefinitelyNull(local);
}

public void markAsDefinitelyUnknown(LocalVariableBinding local) {
	grow(local.id + this.maxFieldCount);
	super.markAsDefinitelyUnknown(local);
}

/**
 * Return a fake unconditional flow info which bit fields represent the given
 * null bits for a local variable of id 0 within a class that would have no
 * field.
 * @param nullBits the bits that must be set, given in the same order as the
 *        nullAssignment* fields in UnconditionalFlowInfo definition; use 0
 *        for a bit that is not set, 1 else
 * @return a fake unconditional flow info which bit fields represent the
 *         null bits given in parameter
 */
public static UnconditionalFlowInfoTestHarness testUnconditionalFlowInfo(
		long [] nullBits) {
	return testUnconditionalFlowInfo(nullBits, 0);
}

/**
 * Return a fake unconditional flow info which bit fields represent the given
 * null bits for a local variable of id position within a class that would have
 * no field.
 * @param nullBits the bits that must be set, given in the same order as the
 *        nullAssignment* fields in UnconditionalFlowInfo definition; use 0
 *        for a bit that is not set, 1 else
 * @param position the position of the variable within the bit fields; use
 *        various values to test different parts of the bit fields, within
 *        or beyond BitCacheSize
 * @return a fake unconditional flow info which bit fields represent the
 *         null bits given in parameter
 */
public static UnconditionalFlowInfoTestHarness testUnconditionalFlowInfo(
		long [] nullBits, int position) {
 	UnconditionalFlowInfoTestHarness result =
 		new UnconditionalFlowInfoTestHarness();
	result.testPosition = position;
	init(result, nullBits, position);
	return result;
}

/**
 * Return a fake unconditional flow info which bit fields represent the given
 * state for a local variable of id 0 within a class that would have
 * no field.
 * @param state - State the desired state for the variable
 * @return a fake unconditional flow info which bit fields represent the
 *         state given in parameter
 */
public static UnconditionalFlowInfoTestHarness testUnconditionalFlowInfo(NullReferenceImplTests.State state) {
	return testUnconditionalFlowInfo(state, 0);
}

/**
 * Return a fake unconditional flow info which bit fields represent the given
 * state for a local variable of id position within a class that would have
 * no field.
 * @param state - State the desired state for the variable
 * @param position the position of the variable within the bit fields; use
 *        various values to test different parts of the bit fields, within
 *        or beyond BitCacheSize
 * @return a fake unconditional flow info which bit fields represent the
 *         state given in parameter
 */
public static UnconditionalFlowInfoTestHarness testUnconditionalFlowInfo(
		NullReferenceImplTests.State state, int position) {
 	UnconditionalFlowInfoTestHarness result =
 		new UnconditionalFlowInfoTestHarness();
 	long[] nullBits = state.asLongArray();
	result.testPosition = position;
	init(result, nullBits, position);
	return result;
}

/**
 * Return true iff this flow info can be considered as equal to the one passed
 * in parameter.
 * @param other the flow info to compare to
 * @return true iff this flow info compares equal to other
 */
public boolean testEquals(UnconditionalFlowInfo other) {
	return testEquals(this, other);
}

/**
 * Return true iff this flow info can be considered as equal to the one passed
 * in parameter in respect with a single local variable which id would be
 * position in a class with no field.
 * @param other the flow info to compare to
 * @param position the position of the local to consider
 * @return true iff this flow info compares equal to other for a given local
 */
public boolean testEquals(UnconditionalFlowInfo other, int position) {
	return testEquals(this, other, position);
}

/**
 * Return a string suitable for use as a representation of this flow info
 * within test series.
 * @return a string suitable for use as a representation of this flow info
 */
public String testString() {
	if (this == DEAD_END) {
		return "FlowInfo.DEAD_END"; //$NON-NLS-1$
	}
	return testString(this, this.testPosition);
}

/**
 * Return a string suitable for use as a representation of this flow info
 * within test series.
 * @param position a position to consider instead of this flow info default
 *                 test position
 * @return a string suitable for use as a representation of this flow info
 */
public String testString(int position) {
	return testString(this, position);
}

	// Factorized implementation
static NullReferenceImplTests.State asState(UnconditionalFlowInfo zis, int position) {
	if ((zis.tagBits & NULL_FLAG_MASK) == 0) {
		return NullReferenceImplTests.State.start;
	}
	if (position < BitCacheSize) {
		return NullReferenceImplTests.State.fromLongValues(
				(zis.nullBit1 >> position) & 1,
				(zis.nullBit2 >> position) & 1,
				(zis.nullBit3 >> position) & 1,
				(zis.nullBit4 >> position) & 1,
				0,
				0);
	}
 	else {
		int vectorIndex = (position / BitCacheSize) - 1;
        position %= BitCacheSize;
        if (vectorIndex >= zis.extra[2].length) {
        	return NullReferenceImplTests.State.start;
        }
		return NullReferenceImplTests.State.fromLongValues(
				(zis.extra[2][vectorIndex] >> position) & 1,
				(zis.extra[3][vectorIndex] >> position) & 1,
				(zis.extra[4][vectorIndex] >> position) & 1,
				(zis.extra[5][vectorIndex] >> position) & 1,
				0 //(zis.extra[6][vectorIndex] >> position) & 1,
				, 0 //(zis.extra[7][vectorIndex] >> position) & 1
				);
	}
}

static void copy(UnconditionalFlowInfo source, UnconditionalFlowInfo target) {
	target.definiteInits = source.definiteInits;
	target.potentialInits = source.potentialInits;
	boolean hasNullInfo = (source.tagBits & NULL_FLAG_MASK) != 0;
	if (hasNullInfo) {
		target.nullBit1 = source.nullBit1;
		target.nullBit2 = source.nullBit2;
		target.nullBit3 = source.nullBit3;
		target.nullBit4 = source.nullBit4;
	}
	target.iNBit = source.iNBit;
	target.iNNBit = source.iNNBit;
	target.iDefNBit = source.iDefNBit;
	target.iDefNNBit = source.iDefNNBit;
	target.tagBits = source.tagBits;
	target.maxFieldCount = source.maxFieldCount;
	if (source.extra != null) {
		int length;
        target.extra = new long[extraLength][];
		System.arraycopy(source.extra[0], 0,
			(target.extra[0] = new long[length = source.extra[0].length]), 0, length);
		System.arraycopy(source.extra[1], 0,
			(target.extra[1] = new long[length]), 0, length);
		if (hasNullInfo) {
            for (int j = 0; j < extraLength; j++) {
			    System.arraycopy(source.extra[j], 0,
				    (target.extra[j] = new long[length]), 0, length);
            }
		}
		else {
            for (int j = 0; j < extraLength; j++) {
			    target.extra[j] = new long[length];
            }
		}
	}
}

public void grow(int position) {
	int vectorIndex = ((position) / BitCacheSize) - 1;
	int length = vectorIndex + 1, oldLength;
	if (this.extra == null) {
		this.extra = new long[extraLength][];
		for (int j = 0; j < extraLength; j++) {
			this.extra[j] = new long[length];
		}
	} else if (length > (oldLength = this.extra[2].length)) {
		for (int j = 0; j < extraLength; j++) {
			System.arraycopy(this.extra[j], 0,
				this.extra[j] = new long[length], 0, oldLength);
		}
	}
}

static void init(UnconditionalFlowInfo zis, long [] nullBits, int position) {
	if (position < BitCacheSize) {
		zis.nullBit1 = nullBits[0] << position;
		zis.nullBit2 = nullBits[1] << position;
		zis.nullBit3 = nullBits[2] << position;
		zis.nullBit4 = nullBits[3] << position;
	}
 	else {
		int vectorIndex = (position / BitCacheSize) - 1,
			length = vectorIndex + 1;
        position %= BitCacheSize;
        zis.extra = new long[extraLength][];
		zis.extra[0] = new long[length];
		zis.extra[1] = new long[length];
        for (int j = 2; j < extraLength; j++) {
		    zis.extra[j] = new long[length];
		    zis.extra[j][vectorIndex] = nullBits[j - 2] << position;
        }
        // FIXME: while IN,INN are not included in nullBits:
        Arrays.fill(zis.extra[UnconditionalFlowInfo.IN],  -1L);
        Arrays.fill(zis.extra[UnconditionalFlowInfo.INN],  -1L);
	}
	zis.iNBit = -1L; // FIXME: nullBits[4] << position;
	zis.iNNBit = -1L; // FIXME: nullBits[5] << position;
	zis.iDefNBit = -1L; // FIXME: nullBits[4] << position;
	zis.iDefNNBit = -1L; // FIXME: nullBits[5] << position;
	if (nullBits[0] != 0 || nullBits[1] != 0
	        || nullBits[2] != 0 || nullBits[3] != 0
	        || nullBits[4] != 0 || nullBits[5] != 0) {
		// cascade better than nullBits[0] | nullBits[1] | nullBits[2] | nullBits[3]
		// by 10%+
		// TODO (maxime) run stats to determine which is the better order
		zis.tagBits |= NULL_FLAG_MASK;
	}
	zis.maxFieldCount = 0;
}

static boolean testEquals(UnconditionalFlowInfo zis, UnconditionalFlowInfo other) {
	if (zis.tagBits != other.tagBits) {
		return false;
	}
	if (zis.nullBit1 != other.nullBit1
			|| zis.nullBit2 != other.nullBit2
			|| zis.nullBit3 != other.nullBit3
			|| zis.nullBit4 != other.nullBit4
			/*|| zis.iNBit != other.iNBit // FIXME: include these bits in comparison?
			|| zis.iNNBit != other.iNNBit */) {
		return false;
	}
	int left = zis.extra == null ? 0 : zis.extra[2].length,
			right = other.extra == null ? 0 : other.extra[2].length,
			both = 0, i;
	if (left > right) {
		both = right;
	}
	else {
		both = left;
	}
	for (i = 0; i < both ; i++) {
		for (int j = 2; j < extraLength; j++) {
			if (zis.extra[j][i] !=
					other.extra[j][i]) {
				return false;
			}
		}
	}
	for (; i < left; i++) {
		for (int j = 2; j < extraLength; j++) {
			if (zis.extra[j][i] != 0) {
				return false;
			}
		}
	}
	for (; i < right; i++) {
		for (int j = 2; j < extraLength; j++) {
			if (other.extra[j][i] != 0) {
				return false;
			}
		}
	}
	return true;
}

static boolean testEquals(UnconditionalFlowInfo zis, UnconditionalFlowInfo other,
		int position) {
	int vectorIndex = position / BitCacheSize - 1;
	if ((zis.tagBits & other.tagBits & NULL_FLAG_MASK) == 0) {
		return true;
	}
	long mask;
	if (vectorIndex < 0) {
		return ((zis.nullBit1 & (mask = (1L << position))) ^
					(other.nullBit1 & mask)) == 0 &&
				((zis.nullBit2 & mask) ^
					(other.nullBit2 & mask)) == 0 &&
				((zis.nullBit3 & mask) ^
					(other.nullBit3 & mask)) == 0 &&
				((zis.nullBit4 & mask) ^
					(other.nullBit4 & mask)) == 0 /* &&  // FIXME: include these bits in comparison?
				((zis.iNBit & mask) ^
					(other.iNBit & mask)) == 0 &&
				((zis.iNNBit & mask) ^
					(other.iNNBit & mask)) == 0 */;
	}
	else {
		int left = zis.extra == null ?
				0 :
				zis.extra[0].length;
		int right = other.extra == null ?
				0 :
				other.extra[0].length;
		int both = left < right ? left : right;
		if (vectorIndex < both) {
			mask = (1L << (position % BitCacheSize));
			for (int j = 2; j < extraLength; j++) {
				if (((zis.extra[j][vectorIndex] & mask)
						^ (other.extra[j][vectorIndex] & mask)) != 0) {
					return false;
				}
			}
			return true;
		}
		if (vectorIndex < left) {
			return ((zis.extra[2][vectorIndex] |
					zis.extra[3][vectorIndex] |
					zis.extra[4][vectorIndex] |
					zis.extra[5][vectorIndex] |
					zis.extra[6][vectorIndex] |
					zis.extra[7][vectorIndex]) &
					(1L << (position % BitCacheSize))) == 0;
		}
		return ((other.extra[2][vectorIndex] |
				other.extra[3][vectorIndex] |
				other.extra[4][vectorIndex] |
				other.extra[5][vectorIndex] |
				other.extra[6][vectorIndex] |
				other.extra[7][vectorIndex]) &
				(1L << (position % BitCacheSize))) == 0;
	}
}

static String testString(UnconditionalFlowInfo zis, int position) {
	if (zis == DEAD_END) {
		return "FlowInfo.DEAD_END"; //$NON-NLS-1$
	}
	if (position < BitCacheSize) {
		return "{" + (zis.nullBit1 >> position) //$NON-NLS-1$
					+ "," + (zis.nullBit2 >> position) //$NON-NLS-1$
					+ "," + (zis.nullBit3 >> position) //$NON-NLS-1$
					+ "," + (zis.nullBit4 >> position) //$NON-NLS-1$
//					+ "," + (zis.iNBit >> position) //$NON-NLS-1$
//					+ "," + (zis.iNNBit >> position) //$NON-NLS-1$
					+ "}"; //$NON-NLS-1$
	}
	else {
		int vectorIndex = position / BitCacheSize - 1,
			shift = position % BitCacheSize;
			return "{" + (zis.extra[2][vectorIndex] //$NON-NLS-1$
			               >> shift)
						+ "," + (zis.extra[3][vectorIndex] //$NON-NLS-1$
						   >> shift)
						+ "," + (zis.extra[4][vectorIndex] //$NON-NLS-1$
						   >> shift)
						+ "," + (zis.extra[5][vectorIndex] //$NON-NLS-1$
						   >> shift)
//						+ "," + (zis.extra[6][vectorIndex] //$NON-NLS-1$
//						   >> shift)
//						+ "," + (zis.extra[7][vectorIndex] //$NON-NLS-1$
//						   >> shift)
						+ "}"; //$NON-NLS-1$
	}
}
}
interface CodeAnalysis {
	public static final String
		definitionStartMarker = "DEFINITION START",
		definitionEndMarker = "DEFINITION END",
		initializerStartMarker = "INITIALIZER START",
		initializerEndMarker = "INITIALIZER END";
}
@SuppressWarnings({ "unchecked", "rawtypes" })
class TransitiveClosureHolder {
static class Element {
	NullReferenceImplTests.State value;
	boolean alreadyKnown;
	Element(NullReferenceImplTests.State value) {
		if (value == null) {
			throw new IllegalArgumentException("not a valid element");
		}
		this.value = value;
	}
}
Map elements = new TreeMap();
public TransitiveClosureHolder() {
	Element start = new Element(NullReferenceImplTests.State.start);
	this.elements.put(start.value, start);
}
void add(NullReferenceImplTests.State value) {
	if (value == null) {
		throw new IllegalArgumentException("not a valid state");
	}
	if (! this.elements.containsKey(value)) {
		this.elements.put(value, new Element(value));
	}
}
void add(NullReferenceImplTests.State[] values) {
	if (values == null) {
		throw new IllegalArgumentException("not a valid states set");
	}
	for (int i = 0, length = values.length; i < length; i++) {
		add(values[i]);
	}
}
NullReferenceImplTests.State[] asArray() {
	int length;
	NullReferenceImplTests.State[] result = new NullReferenceImplTests.State[length = this.elements.size()];
	Iterator elementsIterator = this.elements.keySet().iterator();
	for (int j = 0; j < length; j++) {
		result[j] = (NullReferenceImplTests.State) elementsIterator.next();
	}
	return result;
}
NullReferenceImplTests.State[] notAlreadyKnowns() {
	List resultAccumulator = new ArrayList(this.elements.size());
	Iterator i = this.elements.values().iterator();
	Element current;
	while (i.hasNext()) {
		if (! (current = (Element) i.next()).alreadyKnown) {
			resultAccumulator.add(current.value);
		}
	}
	int length;
	NullReferenceImplTests.State[] result = new NullReferenceImplTests.State[length = resultAccumulator.size()];
	for (int j = 0; j < length; j++) {
		result[j] = (NullReferenceImplTests.State) resultAccumulator.get(j);
	}
	return result;
}
void markAllAsAlreadyKnown() {
	Iterator i = this.elements.values().iterator();
	while (i.hasNext()) {
		((Element) i.next()).alreadyKnown = true;
	}
}
@Override
public String toString() {
	StringBuilder output = new StringBuilder();
	output.append("Transitive closure:\n");
	SortedMap sorted = new TreeMap(this.elements);
	Iterator i = sorted.keySet().iterator();
	while (i.hasNext()) {
		output.append(i.next().toString());
		output.append('\n');
	}
	return output.toString();
}
}

// PREMATURE segregate pure tooling into a separate project, keep tests only here
/**
 * The Generator class is meant to generate the tabular data needed by the
 * flow information implementation level tests. While the tests should ensure
 * non regression by leveraging their initialization tables only, any change
 * into the flow information logic or encoding is due to yield considerable
 * changes into the literal values sets of the initializers themselves.
 * Tooling the production of those literals buys us flexibility.
 * {@link #printHelp printHelp} for details.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
class Generator {
static NullReferenceImplTests.State[] computeTransitiveClosure() {
	TransitiveClosureHolder transitiveClosure = new TransitiveClosureHolder();
	NullReferenceImplTests.State[] unknowns;
	unknowns = transitiveClosure.notAlreadyKnowns();
	while (unknowns.length != 0) {
		transitiveClosure.markAllAsAlreadyKnown();
		for (int i = 0, length = NullReferenceImplTransformations.transformations.length;	i < length; i ++) {
			transitiveClosure.add(
				NullReferenceImplTransformations.transformations[i].
					computeOutputs(transitiveClosure.asArray()));
		}
		unknowns = transitiveClosure.notAlreadyKnowns();
	}
	return transitiveClosure.asArray();
}
public static void main(String[] args) {
	if (args.length == 0) {
		printHelp(false);
		System.exit(1);
	}
	switch (args.length) {
		case 1:
			if (args[0].equals("--help")) {
				printHelp(true);
				System.exit(0);
			}
			else {
				printHelp(false);
				System.exit(1);
			}
			break;
		case 2:
			if (args[0].equals("--printTruthTables")) {
				File outputDir = new File(args[1]);
				if (outputDir.isDirectory()) {
					for (int i = 0, length = NullReferenceImplTransformations.transformations.length; i < length; i++) {
						NullReferenceImplTransformations.transformations[i].printTruthTables(outputDir);
					}
				}
				else {
					// PREMATURE error handling
				}
				System.exit(0);
			}
			else {
				printHelp(false);
				System.exit(1);
			}
			break;
		case 3:
			if (args[0].equals("--reinitializeFromComputedValues")) {
				reinitializeFromComputedValues(args[1], args[2]);
				System.out.println("Generator generated new file into " + args[2]);
				System.exit(0);
			}
			//$FALL-THROUGH$
		case 5:
			if (args[0].equals("--reinitializeFromComments")) {
				reinitializeFromComments(args[1], args[2], args[3], args[4]);
				System.out.println("Generator generated new files into " + args[2]
					+ " and " + args[4]);
				System.exit(0);
			}
			//$FALL-THROUGH$
		default:
			printHelp(false);
			System.exit(1);
	}
}

private static void reinitializeFromComments(
		String statesSource, String statesTarget,
		String transformationsSource, String transformationsTarget) {
	if (statesSource.equals(transformationsSource) ||
			statesTarget.equals(transformationsTarget)) {
		throw new RuntimeException();
	}
	try {
		BufferedReader in;
		BufferedWriter out;
		NullReferenceImplTests.State.reinitializeFromComment(
			in = new BufferedReader(
				new FileReader(statesSource)),
			out = new BufferedWriter(new FileWriter(statesTarget)));
		in.close();
		out.close();
		File[] tempFiles = new File[2];
		tempFiles[0] = File.createTempFile("generator", "java");
		tempFiles[1] = File.createTempFile("generator", "java");
		NullReferenceImplTransformations.transformations[0].reinitializeFromComments(
			in = new BufferedReader(
				new FileReader(transformationsSource)),
			out = new BufferedWriter(new FileWriter(tempFiles[0])));
		in.close();
		out.close();
		int i, length;
		for (i = 1, length = NullReferenceImplTransformations.transformations.length - 1; i < length; i++) {
			NullReferenceImplTransformations.transformations[i].reinitializeFromComments(
				in = new BufferedReader(
					new FileReader(tempFiles[(i + 1) % 2])),
				out = new BufferedWriter(new FileWriter(tempFiles[i % 2])));
			in.close();
			out.close();
		}
		NullReferenceImplTransformations.transformations[i].reinitializeFromComments(
			in = new BufferedReader(
				new FileReader(tempFiles[(i + 1) % 2])),
			out = new BufferedWriter(new FileWriter(transformationsTarget)));
		in.close();
		out.close();
	} catch (Throwable t) {
		System.err.println("Generator error:");
		t.printStackTrace(System.err);
		System.exit(2);
	}
}

private static void reinitializeFromComputedValues(String source, String target) {
	for (int i = 0, length = NullReferenceImplTransformations.transformations.length;
			i < length; i++) {
		NullReferenceImplTransformations.transformations[i].hydrate();
	}
	NullReferenceImplTests.State[] transitiveClosure = computeTransitiveClosure(); // need for initialization?
	transitiveClosure = addSymbolicStates(transitiveClosure); // don't rely on reachibility alone, since we don't cover all operations in these tests.
	Arrays.sort(transitiveClosure, new Comparator() {
		@Override
		public int compare(Object o1, Object o2) {
			return Integer.valueOf(((State)o1).value).compareTo(Integer.valueOf(((State)o2).value));
		}
	});
	try {
		BufferedReader in;
		BufferedWriter out;
		File[] tempFiles = new File[2];
		tempFiles[0] = File.createTempFile("generator", "java");
		tempFiles[1] = File.createTempFile("generator", "java");
		NullReferenceImplTransformations.transformations[0].reinitializeFromComputedValues(
			in = new BufferedReader(
				new FileReader(source)),
			out = new BufferedWriter(new FileWriter(tempFiles[0])),
			transitiveClosure);
		in.close();
		out.close();
		int i, length;
		for (i = 1, length = NullReferenceImplTransformations.transformations.length - 1; i < length; i++) {
			NullReferenceImplTransformations.transformations[i].reinitializeFromComputedValues(
				in = new BufferedReader(
					new FileReader(tempFiles[(i + 1) % 2])),
				out = new BufferedWriter(new FileWriter(tempFiles[i % 2])),
				transitiveClosure);
			in.close();
			out.close();
		}
		NullReferenceImplTransformations.transformations[i].reinitializeFromComputedValues(
			in = new BufferedReader(
				new FileReader(tempFiles[(i + 1) % 2])),
			out = new BufferedWriter(new FileWriter(target)),
			transitiveClosure);
		in.close();
		out.close();
	} catch (Throwable t) {
		System.err.println("Generator error:");
		t.printStackTrace(System.err);
		System.exit(2);
	}
}
private static State[] addSymbolicStates(State[] transitiveClosure) {
	Set allStates = new HashSet();
	allStates.addAll(Arrays.asList(transitiveClosure));
	for (int i=0; i < State.statesNb; i++)
		if (State.states[i].symbolic)
			allStates.add(State.states[i]);
	return (State[]) allStates.toArray(new State[allStates.size()]);
}

private static void printHelp(boolean longText) {
	if (longText) {
		System.out.println(
			"Generator use cases\n" +
			" - when a brand new logic is experimented for the transitions, the best\n" +
			"   way to go is to write explicit (inefficient) transformation code within\n" +
			"   UnconditionalFlowInfo, then generate the literal initializers from\n" +
			"   there; use the command\n" +
			"   --reinitializeFromComputedValues <source file> <target file>\n" +
			"   to this effect; in case of inconsistencies or errors, messages are\n" +
			"   printed to the error output stream and the result should be considered as non reliable;\n" +
			" - when only a few changes are made to state names or a specific\n" +
			"   transitions, it should be possible to get the test initializers fixed\n" +
			"   before UnconditionalFlowInfo implements those changes; use the command\n" +
	        "   --reinitializeFromComments <states source file> <states target file> <transformations source file> <transformations target file>\n" +
	        "   to this effect;\n" +
	        " - the same command can be used when, while the semantics of the system\n" +
	        "   are unchanged, the encoding is modified; it should then produce the\n" +
	        "   initializers according to the new encoding, as defined by the comment\n" +
	        "   for State.states, and the transformations as defined by their\n" +
	        "   respective comment;\n" +
	        " - when a given encoding is retained, its optimization may leverage truth\n" +
	        "   tables; use the --printTruthTables command to this effect.\n" +
	        "   \n\n");
		printHelp(false);
	}
	else {
		System.out.println(
	        "Usage:\n" +
	        "Generator --help\n" +
	        "  prints a more detailed help message\n" +
	        "Generator --printTruthTables\n" +
	        "  prints the truth tables of the transformations\n" +
	        "Generator --reinitializeFromComments <source file> <target file>\n" +
	        "  generates into target file a copy of source file into which\n" +
	        "  transformations initializers have been reset from their definitions\n" +
			"Generator --reinitializeFromComputedValues <source file> <target file>\n"  +
	        "  generates into target file a copy of source file into which\n" +
	        "  transformations definitions and initializers have been reset\n" +
	        "  according to the behavior of the current UnconditionalFlowInfo\n"
	        );
	}
}
}

