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

import static org.eclipse.jdt.internal.compiler.parser.TerminalToken.TokenNameInvalid;

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
import junit.framework.Test;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalToken;
import org.eclipse.jdt.internal.core.util.PublicScanner;

/**
 * Test that validates {@link Scanner} and {@link PublicScanner} use of tokens.
 */
@SuppressWarnings({ "rawtypes" })
public class PublicScannerTest extends AbstractRegressionTest {

	private Map<Integer, String> ttValueToName;
	private Map<String, TerminalToken> ttNameToValue;
	private TerminalToken[] ttFields;
	private Map<Integer, String> tsValueToName;
	private Map<String, Integer> tsNameToValue;
	private Field[] tsFields;
	private MyPublicScanner ps;

	/**
	 * Replacement map for tokens that shouldn't be exposed to clients
	 * Key is the token, value is the replacement token
	 */
	static final Map<TerminalToken, TerminalToken> SYNTHETIC_REPLACE_TOKENS;

	/**
	 * Replacement list for tokens that shouldn't be exposed to clients and scanner should
	 * skip to next token
	 */
	static final List<TerminalToken> SYNTHETIC_SKIP_TOKENS;

	/**
	 * List of tokens that shouldn't be exposed to clients because they can never be
	 * produced without declaring the source to be "module-info.java".
	 */
	static final List<TerminalToken> MODULE_TOKENS;

	static {
		Map<TerminalToken, TerminalToken> map = new HashMap<>();
		map.put(TerminalToken.TokenNameAT308DOTDOTDOT, TerminalToken.TokenNameAT);
		map.put(TerminalToken.TokenNameAT308, TerminalToken.TokenNameAT);
		map.put(TerminalToken.TokenNameCaseArrow, TerminalToken.TokenNameARROW);
		SYNTHETIC_REPLACE_TOKENS = Collections.unmodifiableMap(map);

		List<TerminalToken> list = new ArrayList<>();
		list.add(TerminalToken.TokenNameBeginCasePattern);
		list.add(TerminalToken.TokenNameBeginIntersectionCast);
		list.add(TerminalToken.TokenNameBeginLambda);
		list.add(TerminalToken.TokenNameBeginTypeArguments);
		list.add(TokenNameInvalid);
		SYNTHETIC_SKIP_TOKENS = Collections.unmodifiableList(list);

		list = new ArrayList<>();
		list.add(TerminalToken.TokenNamemodule);
		list.add(TerminalToken.TokenNamerequires);
		list.add(TerminalToken.TokenNameexports);
		list.add(TerminalToken.TokenNameto);
		list.add(TerminalToken.TokenNameopen);
		list.add(TerminalToken.TokenNameopens);
		list.add(TerminalToken.TokenNameprovides);
		list.add(TerminalToken.TokenNamewith);
		list.add(TerminalToken.TokenNametransitive);
		list.add(TerminalToken.TokenNameuses);
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
		this.ttFields = TerminalToken.values();
		for (TerminalToken field : this.ttFields) {
			// we are stuck with the clunkier names for API - map
			String fName = field.name();
			if (fName.equals("TokenNamesealed"))
				fName = "TokenNameRestrictedIdentifiersealed";
			else if (fName.equals("TokenNamepermits"))
				fName = "TokenNameRestrictedIdentifierpermits";
			this.ttValueToName.put(field.tokenNumber(), fName);
			this.ttNameToValue.put(fName, field);
		}
		if(this.ttValueToName.size() != this.ttNameToValue.size()) {
			this.ttNameToValue.keySet().removeAll(this.ttValueToName.values());
			fail("TerminalToken constants use already defined values: " + this.ttNameToValue.keySet());
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
	 * Tests that all constants defined in @link {@link TerminalToken} are properly handled by {@link PublicScanner#getNextToken()}
	 */
	public void testGetNextToken() throws Exception {
		Set<Entry<String, TerminalToken>> entrySet = this.ttNameToValue.entrySet();
		for (Entry<String, TerminalToken> entry : entrySet) {
			this.ps.reset();
			String fieldName = entry.getKey();
			TerminalToken fieldValue = entry.getValue();
			if (MODULE_TOKENS.contains(fieldValue)) {
				continue;
			}
			if (fieldValue == TokenNameInvalid) {
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
				TerminalToken value = TerminalToken.valueOf(fieldName);
				if(SYNTHETIC_SKIP_TOKENS.contains(value)){
					assertEquals(2, this.ps.nextTokenCalls);
				} else {
					assertEquals(1, this.ps.nextTokenCalls);
					TerminalToken target = SYNTHETIC_REPLACE_TOKENS.get(value);
					if(target == null) {
						fail("TerminalToken." + fieldName + " should be added to ITerminalSymbols or SYNTHETIC_*_TOKENS in PublicScannerTest*!");
					} else {
						String replaceName = this.ttValueToName.get(target.tokenNumber());
						Integer replaceValue = this.tsNameToValue.get(replaceName);
						assertEquals("getNextToken() returns unexpected value for " + fieldName, nextToken, replaceValue.intValue());
					}
				}
			}
		}
	}

	/**
	 * Tests that all constants defined in {@link TerminalToken} are either defined in {@link ITerminalSymbols}
	 * or defined in {@link #SYNTHETIC_REPLACE_TOKENS}, or {@link #SYNTHETIC_SKIP_TOKENS}, or {@link #MODULE_TOKENS}
	 * and no constants defined in {@link ITerminalSymbols} are missing in {@link TerminalToken}
	 */
	public void testTokensAndSymbolsSync() throws Exception {
		Set<Entry<String, Integer>> entrySet = this.tsNameToValue.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			String fieldName = entry.getKey();
			if (!this.ttNameToValue.containsKey(fieldName)) {
				fail("ITerminalSymbols." + fieldName + " does not exist in TerminalToken");
			}
		}
		Set<Entry<String, TerminalToken>> ttEntrySet = this.ttNameToValue.entrySet();
		for (Entry<String, TerminalToken> entry : ttEntrySet) {
			String fieldName = entry.getKey();
			if(this.tsNameToValue.containsKey(fieldName)) {
				// OK, constant present
			} else {
				TerminalToken value = entry.getValue();
				if(SYNTHETIC_SKIP_TOKENS.contains(value) || MODULE_TOKENS.contains(value)){
					// OK, constant present
				} else {
					TerminalToken target = SYNTHETIC_REPLACE_TOKENS.get(value);
					if(target != null) {
						// OK, constant present
					} else {
						fail("TerminalToken." + fieldName + " should be added to ITerminalSymbols or SYNTHETIC_*_TOKENS in PublicScannerTest*!");
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

		void setNextToken(TerminalToken next) {
			this.delegate.next = next;
		}

		void reset() {
			this.delegate.next = TokenNameInvalid;
			this.nextTokenCalls = 0;
		}

		@Override
		public int getNextToken() throws InvalidInputException {
			this.nextTokenCalls ++;
			if (this.inNextCall) {
				return this.delegate.next.tokenNumber();
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
		TerminalToken next;
		public MyScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace,
				boolean checkNonExternalizedStringLiterals, long sourceLevel, long complianceLevel, char[][] taskTags,
				char[][] taskPriorities, boolean isTaskCaseSensitive, boolean isPreviewEnabled) {
			super(tokenizeComments, tokenizeWhiteSpace, checkNonExternalizedStringLiterals, sourceLevel,
					complianceLevel, taskTags, taskPriorities, isTaskCaseSensitive, isPreviewEnabled);
			this.next = TokenNameInvalid;
		}

		@Override
		public TerminalToken getNextToken() throws InvalidInputException {
			return this.next;
		}
	}

	/**
	 * Run this if {@link TerminalToken} is updated and the test fails - that generates the body of the
	 * switch in the {@link PublicScanner#getNextToken()}
	 */
	public static void main(String[] args) throws Exception {
		printGeneratedSwitchForPublicScanner();
	}

	private static void printGeneratedSwitchForPublicScanner() throws Exception {
		Map<Integer, String> valueToName = new TreeMap<>();
		Map<String, Integer> nameToValue = new TreeMap<>();
		TerminalToken[] ttValues = TerminalToken.values();
		for (TerminalToken field : ttValues) {
			valueToName.put(field.tokenNumber(), field.name());
			nameToValue.put(field.name(), field.tokenNumber());
		}

		Field[] tsFields = ITerminalSymbols.class.getFields();
		Set<String> ttNames = nameToValue.keySet();
		Set<String> tsSet = Arrays.asList(tsFields).stream().map(Field::getName).collect(Collectors.toSet());
		StringBuilder sb = new StringBuilder();
		String ident = "\t\t\t";
		for (String ttName : ttNames) {
			if(tsSet.contains(ttName)) {
				sb.append(ident + "case TerminalToken." + ttName + " : nextToken = ITerminalSymbols." + ttName + "; break;\n");
			} else {
				TerminalToken value = TerminalToken.valueOf(ttName);
				if (MODULE_TOKENS.contains(value)) {
					continue;
				}
				if(SYNTHETIC_SKIP_TOKENS.contains(value)){
					sb.append(ident + "case TerminalToken." + ttName + " : nextToken = getNextToken(); break;\n");
				} else {
					TerminalToken target = SYNTHETIC_REPLACE_TOKENS.get(value);
					if(target == null) {
						sb.append("// TODO: add constant " + ttName + " to ITerminalSymbols or update SYNTHETIC_*_TOKENS in PublicScannerTest!\n");
						sb.append("// case TerminalToken." + ttName + " : nextToken = ITerminalSymbols." + ttName + "; break;\n");
					} else {
						String replaceName = target.name();
						sb.append(ident + "case TerminalToken." + ttName + " : nextToken = ITerminalSymbols." + replaceName + "; break;\n");
					}
				}
			}
		}
		System.out.println(sb);
	}
}
