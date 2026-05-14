/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import static org.eclipse.jdt.internal.compiler.parser.TerminalToken.TokenNameNotAToken;

import junit.framework.Test;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalToken;
@SuppressWarnings({ "rawtypes" })
public class InternalScannerTest extends AbstractRegressionTest {

	public InternalScannerTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return InternalScannerTest.class;
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test001() {
		String source =	"//Comment";
		Scanner scanner = new Scanner();
		scanner.setSource(source.toCharArray());
		TerminalToken token = TokenNameNotAToken;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			// ignore
		}
		assertEquals("Wrong token type", TerminalToken.TokenNameEOF, token);
		assertEquals("Wrong comment start", 0,  scanner.commentStarts[0]);
		assertEquals("Wrong comment start", -9, scanner.commentStops[0]);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=73762
	 */
	public void test002() throws InvalidInputException {
		Scanner scanner = new Scanner();
		scanner.recordLineSeparator = true;
		scanner.setSource("a\nb\nc\n".toCharArray());
		TerminalToken token = TokenNameNotAToken;
		while (token !=  TerminalToken.TokenNameEOF) {
			token = scanner.getNextToken();
		}
		scanner.setSource("a\nb\n".toCharArray());
		token = TokenNameNotAToken;
		while (token !=  TerminalToken.TokenNameEOF) {
			token = scanner.getNextToken();
		}
		assertEquals("Wrong number of line ends", 2, scanner.getLineEnds().length);
	}

}
