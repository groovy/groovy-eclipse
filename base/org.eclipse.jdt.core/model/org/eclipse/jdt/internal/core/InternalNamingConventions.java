/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.util.SimpleSetOfCharArray;

public class InternalNamingConventions {
	private static final char[] DEFAULT_NAME = "name".toCharArray(); //$NON-NLS-1$
	
	private static Scanner getNameScanner(CompilerOptions compilerOptions) {
		return
			new Scanner(
				false /*comment*/, 
				false /*whitespace*/, 
				false /*nls*/, 
				compilerOptions.sourceLevel /*sourceLevel*/, 
				null /*taskTags*/, 
				null/*taskPriorities*/,
				true/*taskCaseSensitive*/);
	}
	public static void suggestArgumentNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[] internalPrefix, char[][] excludedNames, INamingRequestor requestor) {
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		suggestNames(
			packageName,
			qualifiedTypeName,
			dim,
			internalPrefix,
			assistOptions.argumentPrefixes,
			assistOptions.argumentSuffixes,
			excludedNames,
			getNameScanner(compilerOptions),
			requestor);
	}
	public static void suggestFieldNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, int modifiers, char[] internalPrefix, char[][] excludedNames, INamingRequestor requestor) {
		boolean isStatic = Flags.isStatic(modifiers);
		
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		suggestNames(
			packageName,
			qualifiedTypeName,
			dim,
			internalPrefix,
			isStatic ? assistOptions.staticFieldPrefixes : assistOptions.fieldPrefixes,
			isStatic ? assistOptions.staticFieldSuffixes : assistOptions.fieldSuffixes,
			excludedNames,
			getNameScanner(compilerOptions),
			requestor);
	}
	public static void suggestLocalVariableNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[] internalPrefix, char[][] excludedNames, INamingRequestor requestor) {
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		suggestNames(
			packageName,
			qualifiedTypeName,
			dim,
			internalPrefix,
			assistOptions.localPrefixes,
			assistOptions.localSuffixes,
			excludedNames,
			getNameScanner(compilerOptions),
			requestor);
	}
	
	private static void suggestNames(
		char[] packageName,
		char[] qualifiedTypeName,
		int dim,
		char[] internalPrefix,
		char[][] prefixes,
		char[][] suffixes,
		char[][] excludedNames,
		Scanner nameScanner,
		INamingRequestor requestor){
		
		if(qualifiedTypeName == null || qualifiedTypeName.length == 0)
			return;
		
		if(internalPrefix == null) {
			internalPrefix = CharOperation.NO_CHAR;
		} else {
			internalPrefix = removePrefix(internalPrefix, prefixes);
		}
		
		char[] typeName = CharOperation.lastSegment(qualifiedTypeName, '.');
	
		if(prefixes == null || prefixes.length == 0) {
			prefixes = new char[1][0];
		} else {
			int length = prefixes.length;
			System.arraycopy(prefixes, 0, prefixes = new char[length+1][], 0, length);
			prefixes[length] = CharOperation.NO_CHAR;
		}
	
		if(suffixes == null || suffixes.length == 0) {
			suffixes = new char[1][0];
		} else {
			int length = suffixes.length;
			System.arraycopy(suffixes, 0, suffixes = new char[length+1][], 0, length);
			suffixes[length] = CharOperation.NO_CHAR;
		}
	
		char[][] tempNames = null;
	
		// compute variable name for base type
		try{
			nameScanner.setSource(typeName);
			switch (nameScanner.getNextToken()) {
				case TerminalTokens.TokenNameint :
				case TerminalTokens.TokenNamebyte :
				case TerminalTokens.TokenNameshort :
				case TerminalTokens.TokenNamechar :
				case TerminalTokens.TokenNamelong :
				case TerminalTokens.TokenNamefloat :
				case TerminalTokens.TokenNamedouble :
				case TerminalTokens.TokenNameboolean :
					
					if (internalPrefix != null && internalPrefix.length > 0) return;
					
					char[] name = computeBaseTypeNames(typeName[0], excludedNames);
					if(name != null) {
						tempNames =  new char[][]{name};
					}
					break;
			}	
		} catch(InvalidInputException e){
			// ignore
		}

		// compute variable name for non base type
		if(tempNames == null) {
			tempNames = computeNames(typeName);
		}
	
		boolean acceptDefaultName = true;
		SimpleSetOfCharArray foundNames = new SimpleSetOfCharArray();
		
		next : for (int i = 0; i < tempNames.length; i++) {
			char[] tempName = tempNames[i];
			if(dim > 0) {
				int length = tempName.length;
				if (tempName[length-1] == 's'){
					if(tempName.length > 1 && tempName[length-2] == 's') {
						System.arraycopy(tempName, 0, tempName = new char[length + 2], 0, length);
						tempName[length] = 'e';
						tempName[length+1] = 's';
					}
				} else if(tempName[length-1] == 'y') {
					System.arraycopy(tempName, 0, tempName = new char[length + 2], 0, length);
					tempName[length-1] = 'i';
					tempName[length] = 'e';
					tempName[length+1] = 's';
				} else {
					System.arraycopy(tempName, 0, tempName = new char[length + 1], 0, length);
					tempName[length] = 's';
				}
			}
		
			char[] unprefixedName = tempName;
			unprefixedName[0] = ScannerHelper.toUpperCase(unprefixedName[0]);
			for (int j = 0; j <= internalPrefix.length; j++) {
				if(j == internalPrefix.length ||
						CharOperation.prefixEquals(CharOperation.subarray(internalPrefix, j, -1), unprefixedName, j != 0 /*do not check case when there is no prefix*/)) {
					tempName = CharOperation.concat(CharOperation.subarray(internalPrefix, 0, j), unprefixedName);
					if(j == 0) tempName[0] = ScannerHelper.toLowerCase(tempName[0]);
					for (int k = 0; k < prefixes.length; k++) {
						if(prefixes[k].length > 0
							&& ScannerHelper.isLetterOrDigit(prefixes[k][prefixes[k].length - 1])) {
							tempName[0] = ScannerHelper.toUpperCase(tempName[0]);
						} else {
							tempName[0] = ScannerHelper.toLowerCase(tempName[0]);
						}
						char[] prefixName = CharOperation.concat(prefixes[k], tempName);
						for (int l = 0; l < suffixes.length; l++) {
							char[] suffixName = CharOperation.concat(prefixName, suffixes[l]);
							suffixName =
								excludeNames(
									suffixName,
									prefixName,
									suffixes[l],
									excludedNames);
							try{
								nameScanner.setSource(suffixName);
								switch (nameScanner.getNextToken()) {
									case TerminalTokens.TokenNameIdentifier :
										int token = nameScanner.getNextToken();
										if (token == TerminalTokens.TokenNameEOF && nameScanner.startPosition == suffixName.length) {
											if (!foundNames.includes(suffixName)) {
												acceptName(suffixName, prefixes[k], suffixes[l],  k == 0, l == 0, internalPrefix.length - j, requestor);
												foundNames.add(suffixName);
												acceptDefaultName = false;
											}
										}
										break;
									default:
										suffixName = CharOperation.concat(
											prefixName,
											String.valueOf(1).toCharArray(),
											suffixes[l]
										);
										suffixName =
											excludeNames(
												suffixName,
												prefixName,
												suffixes[l],
												excludedNames);
										nameScanner.setSource(suffixName);
										switch (nameScanner.getNextToken()) {
											case TerminalTokens.TokenNameIdentifier :
												token = nameScanner.getNextToken();
												if (token == TerminalTokens.TokenNameEOF && nameScanner.startPosition == suffixName.length) {
													if (!foundNames.includes(suffixName)) {
														acceptName(suffixName, prefixes[k], suffixes[l], k == 0, l == 0, internalPrefix.length - j, requestor);
														foundNames.add(suffixName);
														acceptDefaultName = false;
													}
												}
										}
								}
							} catch(InvalidInputException e){
								// ignore
							}
						}
					}
					continue next;
				}
			}
		}
		// if no names were found
		if(acceptDefaultName) {
			char[] name = excludeNames(DEFAULT_NAME, DEFAULT_NAME, CharOperation.NO_CHAR, excludedNames);
			requestor.acceptNameWithoutPrefixAndSuffix(name, 0);
		}
	}
	
	private static void acceptName(
		char[] name,
		char[] prefix,
		char[] suffix,
		boolean isFirstPrefix,
		boolean isFirstSuffix,
		int reusedCharacters,
		INamingRequestor requestor) {
		if(prefix.length > 0 && suffix.length > 0) {
			requestor.acceptNameWithPrefixAndSuffix(name, isFirstPrefix, isFirstSuffix, reusedCharacters);
		} else if(prefix.length > 0){
			requestor.acceptNameWithPrefix(name, isFirstPrefix, reusedCharacters);
		} else if(suffix.length > 0){
			requestor.acceptNameWithSuffix(name, isFirstSuffix, reusedCharacters);
		} else {
			requestor.acceptNameWithoutPrefixAndSuffix(name, reusedCharacters);
		}
	}
	
	private static char[] computeBaseTypeNames(char firstName, char[][] excludedNames){
		char[] name = new char[]{firstName};
		
		for(int i = 0 ; i < excludedNames.length ; i++){
			if(CharOperation.equals(name, excludedNames[i], false)) {
				name[0]++;
				if(name[0] > 'z')
					name[0] = 'a';
				if(name[0] == firstName)
					return null;
				i = 0;
			}	
		}
		
		return name;
	}
	
	private static char[][] computeNames(char[] sourceName){
		char[][] names = new char[5][];
		int nameCount = 0;
		boolean previousIsUpperCase = false;
		boolean previousIsLetter = true;
		for(int i = sourceName.length - 1 ; i >= 0 ; i--){
			boolean isUpperCase = ScannerHelper.isUpperCase(sourceName[i]);
			boolean isLetter = ScannerHelper.isLetter(sourceName[i]);
			if(isUpperCase && !previousIsUpperCase && previousIsLetter){
				char[] name = CharOperation.subarray(sourceName,i,sourceName.length);
				if(name.length > 1){
					if(nameCount == names.length) {
						System.arraycopy(names, 0, names = new char[nameCount * 2][], 0, nameCount);
					}
					name[0] = ScannerHelper.toLowerCase(name[0]);
					names[nameCount++] = name;
				}
			}
			previousIsUpperCase = isUpperCase;
			previousIsLetter = isLetter;
		}
		if(nameCount == 0){
			names[nameCount++] = CharOperation.toLowerCase(sourceName);				
		}
		System.arraycopy(names, 0, names = new char[nameCount][], 0, nameCount);
		return names;
	}

	private static char[] excludeNames(
		char[] suffixName,
		char[] prefixName,
		char[] suffix,
		char[][] excludedNames) {
		int count = 2;
		int m = 0;
		while (m < excludedNames.length) {
			if(CharOperation.equals(suffixName, excludedNames[m], false)) {
				suffixName = CharOperation.concat(
					prefixName,
					String.valueOf(count++).toCharArray(),
					suffix
				);
				m = 0;
			} else {
				m++;
			}
		}
		return suffixName;
	}
	
	private static char[] removePrefix(char[] name, char[][] prefixes) {
		// remove longer prefix
		char[] withoutPrefixName = name;
		if (prefixes != null) {
			int bestLength = 0;
			int nameLength = name.length;
			for (int i= 0; i < prefixes.length; i++) {
				char[] prefix = prefixes[i];
				
				int prefixLength = prefix.length;
				if(prefixLength <= nameLength) {
					if(CharOperation.prefixEquals(prefix, name, false)) {
						if (prefixLength > bestLength) {
							bestLength = prefixLength;
						}
					}
				} else {
					int currLen = 0;
					for (; currLen < nameLength; currLen++) {
						if(ScannerHelper.toLowerCase(prefix[currLen]) != ScannerHelper.toLowerCase(name[currLen])) {
							if (currLen > bestLength) {
								bestLength = currLen;
							}
							break;
						}
					}
					if(currLen == nameLength && currLen > bestLength) {
						bestLength = currLen;
					}
				}
			}
			if(bestLength > 0) {
				if(bestLength == nameLength) {
					withoutPrefixName = CharOperation.NO_CHAR;
				} else {
					withoutPrefixName = CharOperation.subarray(name, bestLength, nameLength);
				}
			}
		}
//		
//		
//		// remove longer prefix
//		char[] withoutPrefixName = name;
//		if (prefixes != null) {
//			int bestLength = 0;
//			for (int i= 0; i < prefixes.length; i++) {
//				char[] prefix = prefixes[i];
//				int max = prefix.length < name.length ? prefix.length : name.length;
//				int currLen = 0;
//				for (; currLen < max; currLen++) {
//					if(Character.toLowerCase(prefix[currLen]) != Character.toLowerCase(name[currLen])) {
//						if (currLen > bestLength) {
//							bestLength = currLen;
//						}
//						break;
//					}
//				}
//				if(currLen == max && currLen > bestLength) {
//					bestLength = max;
//				}
//			}
//			if(bestLength > 0) {
//				if(bestLength == name.length) {
//					withoutPrefixName = CharOperation.NO_CHAR;
//				} else {
//					withoutPrefixName = CharOperation.subarray(name, bestLength, name.length);
//				}
//			}
//		}
		
		return withoutPrefixName;
	}
	
	public static final boolean prefixEquals(char[] prefix, char[] name) {

		int max = prefix.length;
		if (name.length < max)
			return false;
		for (int i = max;
			--i >= 0;
			) // assumes the prefix is not larger than the name
				if (prefix[i] != name[i])
					return false;
			return true;
	}
}
