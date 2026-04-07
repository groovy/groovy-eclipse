/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
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
package org.eclipse.jdt.internal.compiler.util;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

public class TextBlockUtil {

	private static char[] normalize(char[] content) {
		StringBuilder result = new StringBuilder();
		boolean isCR = false;
		for (char c : content) {
			switch (c) {
				case '\r':
					result.append(c);
					isCR = true;
					break;
				case '\n':
					if (!isCR) {
						result.append(c);
					}
					isCR = false;
					break;
				default:
					result.append(c);
					isCR = false;
					break;
			}
		}
		return result.toString().toCharArray();
	}
	// This method is for handling the left over escaped characters during the first
	// scanning (scanForStringLiteral). Admittedly this goes over the text block
	// content again char by char, but this is required in order to correctly
	// treat all the white space and line endings
	private static boolean getLineContent(StringBuilder result, char[] line, int start, int end, boolean merge, boolean lastLine) {
		int lastPointer = 0;
		for(int i = start; i < end;) {
			char c = line[i];
			if (c != '\\') {
				i++;
				continue;
			}
			if (i < end) {
				if (lastPointer + 1 <= i) {
					result.append(CharOperation.subarray(line, lastPointer == 0 ? start : lastPointer, i));
				}
				char next = line[++i];
				switch (next) {
					case '\\' :
						result.append('\\');
						if (i == end)
							merge = false;
						break;
					case 's' :
						result.append(' ');
						break;
					case '"':
						result.append('"');
						break;
					case 'b' :
						result.append('\b');
						break;
					case 'n' :
						result.append('\n');
						break;
					case 'r' :
						result.append('\r');
						break;
					case 't' :
						result.append('\t');
						break;
					case 'f' :
						result.append('\f');
						break;
					default :
						// Direct copy from Scanner#scanEscapeCharacter
						int pos = i + 1;
						int number = ScannerHelper.getHexadecimalValue(next);
						if (number >= 0 && number <= 7) {
							boolean zeroToThreeNot = number > 3;
							try {
								if (pos <= end && ScannerHelper.isDigit(next = line[pos])) {
									pos++;
									int digit = ScannerHelper.getHexadecimalValue(next);
									if (digit >= 0 && digit <= 7) {
										number = (number * 8) + digit;
										if (pos <= end && ScannerHelper.isDigit(next = line[pos])) {
											pos++;
											if (zeroToThreeNot) {
												// has read \NotZeroToThree OctalDigit Digit --> ignore last character
											} else {
												digit = ScannerHelper.getHexadecimalValue(next);
												if (digit >= 0 && digit <= 7){ // has read \ZeroToThree OctalDigit OctalDigit
													number = (number * 8) + digit;
												} else {
													// has read \ZeroToThree OctalDigit NonOctalDigit --> ignore last character
												}
											}
										} else {
											// has read \OctalDigit NonDigit--> ignore last character
										}
									} else {
										// has read \OctalDigit NonOctalDigit--> ignore last character
									}
								} else {
									// has read \OctalDigit --> ignore last character
								}
							} catch (InvalidInputException e) {
								// Unlikely as this has already been processed in scanForStringLiteral()
							}
							if (number < 255) {
								next = (char) number;
							}
							result.append(next);
							lastPointer = i = pos;
							continue;
						} else {
							// Dealing with just '\'
							result.append(c);
							lastPointer = i;
							continue;
						}
				}
				lastPointer = ++i;
			}
		}
		end = merge ? end : end >= line.length ? end : end + 1;
		char[] chars = lastPointer == 0 ?
				CharOperation.subarray(line, start, end) :
					CharOperation.subarray(line, lastPointer, end);
		// The below check is because CharOperation.subarray tend to return null when the
		// boundaries produce a zero sized char[]
		if (chars != null && chars.length > 0)
			result.append(chars);
		return (!merge && !lastLine);
	}
	/**
	 * Converts the given string content into separate lines by
	 * <ul>
	 *  <li> Normalizing all the CR and CRLT to LF </li>
	 *  <li> Split them with LF as delimiters</li>
	 * </ul>
	 * @param all
	 * @return an array or lines, each line represented by char[]
	 */
	public static char[][] convertTextBlockToLines(char[] all) {
		// 1. Normalize, i.e. convert all CR CRLF to LF
		all = normalize(all);
		// 2. Split into lines. Consider both \n and \r as line separators
		char[][] lines = CharOperation.splitOn('\n', all);
		int size = lines.length;
		List<char[]> list = new ArrayList<>(lines.length);
		for(int i = 0; i < lines.length; i++) {
			char[] line = lines[i];
			if (i + 1 == size && line.length == 0) {
				list.add(line);
				break;
			}
			char[][] sub = CharOperation.splitOn('\r', line);
			if (sub.length == 0) {
				list.add(line);
			} else {
				for (char[] cs : sub) {
					list.add(cs);
				}
			}
		}
		size = list.size();
		lines = list.toArray(new char[size][]);
		return lines;
	}
	/**
	 * Computes the common whitespace prefix of the given set of lines
	 * and returns. Only non empty lines, except the last line, are
	 * considered for this.
	 *
	 * @param lines
	 * @return the common whitespace prefix
	 */
	public static int getWhitespacePrefix(char[][] lines) {
		int prefix = -1;
		int size = lines.length;
		for(int i = 0; i < size; i++) {
			char[] line = lines[i];
			boolean blank = true;
			int whitespaces = 0;
	 		for (char c : line) {
				if (blank) {
					if (ScannerHelper.isWhitespace(c)) {
						whitespaces++;
					} else {
						blank = false;
					}
				}
			}
	 		// The last line with closing delimiter is part of the
	 		// determining line list even if empty
			if (!blank || (i+1 == size)) {
				if (prefix < 0 || whitespaces < prefix) {
	 				prefix = whitespaces;
				}
			}
		}
		return prefix == -1 ? 0 : prefix;
	}

	public static char[] formatTextBlock(char[][] lines, int indent) {
		return formatTextBlock(lines, indent, false, false);
	}
	public static char[] formatTextBlock(char[][] lines, int indent, boolean followsExp, boolean precedesExp) {
		// Handle incidental white space
		// Split into lines and identify determining lines
		// Remove the common white space prefix
		// Handle escape sequences  that are not already done in getNextToken0()
		int size = lines.length;
		StringBuilder result = new StringBuilder();
		boolean newLine = false;
		for(int i = 0; i < size; i++) {
			if (i > 0)
				followsExp = false;
			char[] l  = lines[i];
			int length = l.length;
			int prefix = followsExp ? 0 : indent;
			// Remove the common prefix from each line
			// And remove all trailing whitespace
			// Finally append the \n at the end of the line (except the last line)
			int trail = length;
			// Only the last line is really prefixed to the embedded
			// expression in a string template
			if (!precedesExp || i < (size -1)) {
				for(;trail > 0;) {
					if (!ScannerHelper.isWhitespace(l[trail-1])) {
						break;
					}
					trail--;
				}
			}
			if (i >= (size -1)) {
				if (newLine) result.append('\n');
				if (trail < prefix)
					continue;
				newLine = getLineContent(result, l, prefix, trail-1, false, true);
			} else {
				if (i > 0 && newLine)
					result.append('\n');
				if (trail <= prefix) {
					newLine = true;
				} else {
					boolean merge = length > 0 && l[length - 1] == '\\';
					newLine = getLineContent(result, l, prefix, trail-1, merge, false);
				}
			}
		}
		return result.toString().toCharArray();
	}
}
