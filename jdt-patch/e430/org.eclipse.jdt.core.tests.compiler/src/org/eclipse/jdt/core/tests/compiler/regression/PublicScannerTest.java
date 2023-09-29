/*******************************************************************************
 * Copyright (c) 2022, 2023 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.util.PublicScanner;

import junit.framework.Test;

/**
 * Test that validates {@link Scanner} and {@link PublicScanner} use of tokens.
 */
@SuppressWarnings({ "rawtypes" })
public class PublicScannerTest extends AbstractRegressionTest {

	private Map<Integer, String> ttValueToName;
	private Map<String, Integer> ttNameToValue;
	private Field[] ttFields;
	private Map<Integer, String> tsValueToName;
	private Map<String, Integer> tsNameToValue;
	private Field[] tsFields;
	private MyPublicScanner ps;

	/**
	 * Replacement map for tokens that shouldn't be exposed to clients
	 * Key is the token, value is the replacement token
	 */
	static final Map<Integer, Integer> SYNTHETIC_REPLACE_TOKENS;

	/**
	 * Replacement list for tokens that shouldn't be exposed to clients and scanner should
	 * skip to next token
	 */
	static final List<Integer> SYNTHETIC_SKIP_TOKENS;

	/**
	 * List of tokens that shouldn't be exposed to clients because they can never be
	 * produced without declaring the source to be "module-info.java".
	 */
	static final List<Integer> MODULE_TOKENS;

	static {
		Map<Integer, Integer> map = new HashMap<>();
		map.put(TerminalTokens.TokenNameAT308DOTDOTDOT, TerminalTokens.TokenNameAT);
		map.put(TerminalTokens.TokenNameAT308, TerminalTokens.TokenNameAT);
		SYNTHETIC_REPLACE_TOKENS = Collections.unmodifiableMap(map);

		List<Integer> list = new ArrayList<>();
		list.add(TerminalTokens.TokenNameBeginCaseElement);
		list.add(TerminalTokens.TokenNameBeginCaseExpr);
		list.add(TerminalTokens.TokenNameBeginIntersectionCast);
		list.add(TerminalTokens.TokenNameBeginLambda);
		list.add(TerminalTokens.TokenNameBeginRecordPattern);
		list.add(TerminalTokens.TokenNameBeginTypeArguments);
		list.add(TerminalTokens.TokenNameElidedSemicolonAndRightBrace);
		SYNTHETIC_SKIP_TOKENS = Collections.unmodifiableList(list);

		list = new ArrayList<>();
		list.add(TerminalTokens.TokenNamemodule);
		list.add(TerminalTokens.TokenNamerequires);
		list.add(TerminalTokens.TokenNameexports);
		list.add(TerminalTokens.TokenNameto);
		list.add(TerminalTokens.TokenNameopen);
		list.add(TerminalTokens.TokenNameopens);
		list.add(TerminalTokens.TokenNameprovides);
		list.add(TerminalTokens.TokenNamewith);
		list.add(TerminalTokens.TokenNametransitive);
		list.add(TerminalTokens.TokenNameuses);
		MODULE_TOKENS = Collections.unmodifiableList(list);
	}

	public PublicScannerTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return PublicScannerTest.class;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.ttValueToName = new TreeMap<>();
		this.ttNameToValue = new TreeMap<>();
		this.ttFields = TerminalTokens.class.getFields();
		for (Field field : this.ttFields) {
			this.ttValueToName.put(field.getInt(null), field.getName());
			this.ttNameToValue.put(field.getName(), field.getInt(null));
		}
		if(this.ttValueToName.size() != this.ttNameToValue.size()) {
			this.ttNameToValue.keySet().removeAll(this.ttValueToName.values());
			fail("TerminalTokens constants use already defined values: " + this.ttNameToValue.keySet());
		}
		this.tsValueToName = new TreeMap<>();
		this.tsNameToValue = new TreeMap<>();
		this.tsFields = ITerminalSymbols.class.getFields();
		for (Field field : this.tsFields) {
			this.tsValueToName.put(field.getInt(null), field.getName());
			this.tsNameToValue.put(field.getName(), field.getInt(null));
		}
		if(this.tsValueToName.size() != this.tsNameToValue.size()) {
			this.tsNameToValue.keySet().removeAll(this.tsValueToName.values());
			fail("ITerminalSymbols constants use already defined values: " + this.tsNameToValue.keySet());
		}
		this.ps = new MyPublicScanner();
	}

	/**
	 * Tests that all constants defined in @link {@link TerminalTokens} are properly handled by {@link PublicScanner#getNextToken()}
	 */
	public void testGetNextToken() throws Exception {
		Set<Entry<String, Integer>> entrySet = this.ttNameToValue.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			this.ps.reset();
			String fieldName = entry.getKey();
			Integer fieldValue = entry.getValue();
			if (MODULE_TOKENS.contains(fieldValue)) {
				continue;
			}
			this.ps.setNextToken(fieldValue);
			int nextToken = -1;
			try {
				nextToken = this.ps.getNextToken();
			} catch (InvalidInputException e) {
				fail("Scanner.getNextToken() returns token unknown by PublicScanner: " + fieldName);
			}

			if(this.tsNameToValue.containsKey(fieldName)) {
				int actual = ITerminalSymbols.class.getField(fieldName).getInt(null);
				assertEquals("getNextToken() returns value not specified in ITerminalSymbols for token " + fieldName, actual, nextToken);
				assertEquals(1, this.ps.nextTokenCalls);
			} else {
				Integer value = TerminalTokens.class.getField(fieldName).getInt(null);
				if(SYNTHETIC_SKIP_TOKENS.contains(value)){
					assertEquals(2, this.ps.nextTokenCalls);
				} else {
					assertEquals(1, this.ps.nextTokenCalls);
					Integer target = SYNTHETIC_REPLACE_TOKENS.get(value);
					if(target == null) {
						fail("TerminalTokens." + fieldName + " should be added to ITerminalSymbols or SYNTHETIC_*_TOKENS in PublicScannerTest*!");
					} else {
						String replaceName = this.ttValueToName.get(target);
						Integer replaceValue = this.tsNameToValue.get(replaceName);
						assertEquals("getNextToken() returns unexpected value for " + fieldName, nextToken, replaceValue.intValue());
					}
				}
			}
		}
	}

	/**
	 * Tests that all constants defined in {@link TerminalTokens} are either defined in {@link ITerminalSymbols}
	 * or defined in {@link #SYNTHETIC_REPLACE_TOKENS}, or {@link #SYNTHETIC_SKIP_TOKENS}, or {@link #MODULE_TOKENS}
	 * and no constants defined in {@link ITerminalSymbols} are missing in {@link TerminalTokens}
	 */
	public void testTokensAndSymbolsSync() throws Exception {
		Set<Entry<String, Integer>> entrySet = this.tsNameToValue.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			String fieldName = entry.getKey();
			if (!this.ttNameToValue.containsKey(fieldName)) {
				fail("ITerminalSymbols." + fieldName + " does not exist in TerminalTokens");
			}
		}
		entrySet = this.ttNameToValue.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			String fieldName = entry.getKey();
			if(this.tsNameToValue.containsKey(fieldName)) {
				// OK, constant present
			} else {
				Integer value = TerminalTokens.class.getField(fieldName).getInt(null);
				if(SYNTHETIC_SKIP_TOKENS.contains(value) || MODULE_TOKENS.contains(value)){
					// OK, constant present
				} else {
					Integer target = SYNTHETIC_REPLACE_TOKENS.get(value);
					if(target != null) {
						// OK, constant present
					} else {
						fail("TerminalTokens." + fieldName + " should be added to ITerminalSymbols or SYNTHETIC_*_TOKENS in PublicScannerTest*!");
					}
				}
			}
		}
	}

	class MyPublicScanner extends PublicScanner {
		MyScanner delegate;
		int nextTokenCalls;
		boolean inNextCall;

		public MyPublicScanner() {
			super(false /* comment */,
					false /* whitespace */,
					false /* nls */,
					ClassFileConstants.JDK17 /* sourceLevel */,
					ClassFileConstants.JDK17 /* complianceLevel */,
					null/* taskTag */,
					null/* taskPriorities */,
					true /* taskCaseSensitive */,
					true,
					true);
		}

		@Override
		protected Scanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace,
				boolean checkNonExternalizedStringLiterals, long sourceLevel, long complianceLevel1, char[][] taskTags,
				char[][] taskPriorities, boolean isTaskCaseSensitive, boolean isPreviewEnabled) {
			MyScanner myScanner = new MyScanner(tokenizeComments, tokenizeWhiteSpace, checkNonExternalizedStringLiterals, sourceLevel,
							complianceLevel1, taskTags, taskPriorities, isTaskCaseSensitive, isPreviewEnabled);
			this.delegate = myScanner;
			return myScanner;
		}

		void setNextToken(int next) {
			this.delegate.next = next;
		}

		void reset() {
			this.delegate.next = -1;
			this.nextTokenCalls = 0;
		}

		@Override
		public int getNextToken() throws InvalidInputException {
			this.nextTokenCalls ++;
			if (this.inNextCall) {
				return this.delegate.next;
			} else {
				this.inNextCall = true;
			}
			try {
				return super.getNextToken();
			} finally {
				this.inNextCall = false;
			}
		}
	}


	class MyScanner extends Scanner {
		int next;
		public MyScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace,
				boolean checkNonExternalizedStringLiterals, long sourceLevel, long complianceLevel, char[][] taskTags,
				char[][] taskPriorities, boolean isTaskCaseSensitive, boolean isPreviewEnabled) {
			super(tokenizeComments, tokenizeWhiteSpace, checkNonExternalizedStringLiterals, sourceLevel,
					complianceLevel, taskTags, taskPriorities, isTaskCaseSensitive, isPreviewEnabled);
			this.next = -1;
		}

		@Override
		public int getNextToken() throws InvalidInputException {
			return this.next;
		}
	}

	/**
	 * Run this if {@link TerminalTokens} is updated and the test fails - that generates the body of the
	 * switch in the {@link PublicScanner#getNextToken()}
	 */
	public static void main(String[] args) throws Exception {
		printGeneratedSwitchForPublicScanner();
	}

	private static void printGeneratedSwitchForPublicScanner() throws Exception {
		Map<Integer, String> valueToName = new TreeMap<>();
		Map<String, Integer> nameToValue = new TreeMap<>();
		Field[] ttFields = TerminalTokens.class.getFields();
		for (Field field : ttFields) {
			valueToName.put(field.getInt(null), field.getName());
			nameToValue.put(field.getName(), field.getInt(null));
		}

		Field[] tsFields = ITerminalSymbols.class.getFields();
		Set<String> ttNames = nameToValue.keySet();
		Set<String> tsSet = Arrays.asList(tsFields).stream().map(x -> x.getName()).collect(Collectors.toSet());
		StringBuilder sb = new StringBuilder();
		String ident = "\t\t\t";
		for (String ttName : ttNames) {
			if(tsSet.contains(ttName)) {
				sb.append(ident + "case TerminalTokens." + ttName + " : nextToken = ITerminalSymbols." + ttName + "; break;\n");
			} else {
				Integer value = TerminalTokens.class.getField(ttName).getInt(null);
				if (MODULE_TOKENS.contains(value)) {
					continue;
				}
				if(SYNTHETIC_SKIP_TOKENS.contains(value)){
					sb.append(ident + "case TerminalTokens." + ttName + " : nextToken = getNextToken(); break;\n");
				} else {
					Integer target = SYNTHETIC_REPLACE_TOKENS.get(value);
					if(target == null) {
						sb.append("// TODO: add constant " + ttName + " to ITerminalSymbols or update SYNTHETIC_*_TOKENS in PublicScannerTest!\n");
						sb.append("// case TerminalTokens." + ttName + " : nextToken = ITerminalSymbols." + ttName + "; break;\n");
					} else {
						String replaceName = valueToName.get(target);
						sb.append(ident + "case TerminalTokens." + ttName + " : nextToken = ITerminalSymbols." + replaceName + "; break;\n");
					}
				}
			}
		}
		System.out.println(sb);
	}
}
