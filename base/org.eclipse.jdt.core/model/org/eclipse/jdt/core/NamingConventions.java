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
package org.eclipse.jdt.core;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.core.INamingRequestor;
import org.eclipse.jdt.internal.core.InternalNamingConventions;


/**
 * Provides methods for computing Java-specific names.
 * <p>
 * The behavior of the methods is dependent of several JavaCore options.
 * <p>
 * The possible options are :
 * <ul>
 * <li> {@link JavaCore#CODEASSIST_FIELD_PREFIXES} : Define the Prefixes for Field Name.</li>
 * <li> {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES} : Define the Prefixes for Static Field Name.</li>
 * <li> {@link JavaCore#CODEASSIST_LOCAL_PREFIXES} : Define the Prefixes for Local Variable Name.</li>
 * <li> {@link JavaCore#CODEASSIST_ARGUMENT_PREFIXES} : Define the Prefixes for Argument Name.</li>
 * <li> {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} : Define the Suffixes for Field Name.</li>
 * <li> {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} : Define the Suffixes for Static Field Name.</li>
 * <li> {@link JavaCore#CODEASSIST_LOCAL_SUFFIXES} : Define the Suffixes for Local Variable Name.</li>
 * <li> {@link JavaCore#CODEASSIST_ARGUMENT_SUFFIXES} : Define the Suffixes for Argument Name.</li>
 * </ul>
 * </p>
 * <p>
 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
 * </p>
 * <p>
 * This class provides static methods and constants only.
 * </p>
 * 
 * @see JavaCore#setOptions(java.util.Hashtable)
 * @see JavaCore#getDefaultOptions()
 * @since 2.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class NamingConventions {
	private static final char[] GETTER_BOOL_NAME = "is".toCharArray(); //$NON-NLS-1$
	private static final char[] GETTER_NAME = "get".toCharArray(); //$NON-NLS-1$
	private static final char[] SETTER_NAME = "set".toCharArray(); //$NON-NLS-1$
	
	static class NamingRequestor implements INamingRequestor {
		private final static int SIZE = 10;
		
		// for acceptNameWithPrefixAndSuffix
		private char[][] firstPrefixAndFirstSuffixResults = new char[SIZE][];
		private int firstPrefixAndFirstSuffixResultsCount = 0;
		private char[][] firstPrefixAndSuffixResults = new char[SIZE][];
		private int firstPrefixAndSuffixResultsCount = 0;
		private char[][] prefixAndFirstSuffixResults = new char[SIZE][];
		private int prefixAndFirstSuffixResultsCount = 0;
		private char[][] prefixAndSuffixResults = new char[SIZE][];
		private int prefixAndSuffixResultsCount = 0;
		
		// for acceptNameWithPrefix
		private char[][] firstPrefixResults = new char[SIZE][];
		private int firstPrefixResultsCount = 0;
		private char[][] prefixResults = new char[SIZE][];
		private int prefixResultsCount = 0;
		
		// for acceptNameWithSuffix
		private char[][] firstSuffixResults = new char[SIZE][];
		private int firstSuffixResultsCount = 0;
		private char[][] suffixResults = new char[SIZE][];
		private int suffixResultsCount = 0;
		
		// for acceptNameWithoutPrefixAndSuffix
		private char[][] otherResults = new char[SIZE][];
		private int otherResultsCount = 0;
		public void acceptNameWithPrefixAndSuffix(char[] name, boolean isFirstPrefix, boolean isFirstSuffix, int reusedCharacters) {
			if(isFirstPrefix && isFirstSuffix) {
				int length = this.firstPrefixAndFirstSuffixResults.length;
				if(length == this.firstPrefixAndFirstSuffixResultsCount) {
					System.arraycopy(
						this.firstPrefixAndFirstSuffixResults,
						0,
						this.firstPrefixAndFirstSuffixResults = new char[length * 2][],
						0,
						length);
				}
				this.firstPrefixAndFirstSuffixResults[this.firstPrefixAndFirstSuffixResultsCount++] = name;			
			} else if (isFirstPrefix) {
				int length = this.firstPrefixAndSuffixResults.length;
				if(length == this.firstPrefixAndSuffixResultsCount) {
					System.arraycopy(
						this.firstPrefixAndSuffixResults,
						0,
						this.firstPrefixAndSuffixResults = new char[length * 2][],
						0,
						length);
				}
				this.firstPrefixAndSuffixResults[this.firstPrefixAndSuffixResultsCount++] = name;
			} else if(isFirstSuffix) {
				int length = this.prefixAndFirstSuffixResults.length;
				if(length == this.prefixAndFirstSuffixResultsCount) {
					System.arraycopy(
						this.prefixAndFirstSuffixResults,
						0,
						this.prefixAndFirstSuffixResults = new char[length * 2][],
						0,
						length);
				}
				this.prefixAndFirstSuffixResults[this.prefixAndFirstSuffixResultsCount++] = name;
			} else {
				int length = this.prefixAndSuffixResults.length;
				if(length == this.prefixAndSuffixResultsCount) {
					System.arraycopy(
						this.prefixAndSuffixResults,
						0,
						this.prefixAndSuffixResults = new char[length * 2][],
						0,
						length);
				}
				this.prefixAndSuffixResults[this.prefixAndSuffixResultsCount++] = name;
			}
		}

		public void acceptNameWithPrefix(char[] name, boolean isFirstPrefix, int reusedCharacters) {
			if(isFirstPrefix) {
				int length = this.firstPrefixResults.length;
				if(length == this.firstPrefixResultsCount) {
					System.arraycopy(
						this.firstPrefixResults,
						0,
						this.firstPrefixResults = new char[length * 2][],
						0,
						length);
				}
				this.firstPrefixResults[this.firstPrefixResultsCount++] = name;
			} else{
				int length = this.prefixResults.length;
				if(length == this.prefixResultsCount) {
					System.arraycopy(
						this.prefixResults,
						0,
						this.prefixResults = new char[length * 2][],
						0,
						length);
				}
				this.prefixResults[this.prefixResultsCount++] = name;
			}
		}

		public void acceptNameWithSuffix(char[] name, boolean isFirstSuffix, int reusedCharacters) {
			if(isFirstSuffix) {
				int length = this.firstSuffixResults.length;
				if(length == this.firstSuffixResultsCount) {
					System.arraycopy(
						this.firstSuffixResults,
						0,
						this.firstSuffixResults = new char[length * 2][],
						0,
						length);
				}
				this.firstSuffixResults[this.firstSuffixResultsCount++] = name;
			} else {
				int length = this.suffixResults.length;
				if(length == this.suffixResultsCount) {
					System.arraycopy(
						this.suffixResults,
						0,
						this.suffixResults = new char[length * 2][],
						0,
						length);
				}
				this.suffixResults[this.suffixResultsCount++] = name;
			}
		}

		public void acceptNameWithoutPrefixAndSuffix(char[] name, int reusedCharacters) {
			int length = this.otherResults.length;
			if(length == this.otherResultsCount) {
				System.arraycopy(
					this.otherResults,
					0,
					this.otherResults = new char[length * 2][],
					0,
					length);
			}
			this.otherResults[this.otherResultsCount++] = name;
		}
		public char[][] getResults(){
			int count = 
				this.firstPrefixAndFirstSuffixResultsCount
				+ this.firstPrefixAndSuffixResultsCount
				+ this.prefixAndFirstSuffixResultsCount
				+ this.prefixAndSuffixResultsCount
				+ this.firstPrefixResultsCount
				+ this.prefixResultsCount
				+ this.firstSuffixResultsCount
				+ this.suffixResultsCount
				+ this.otherResultsCount;
				
			char[][] results = new char[count][];
			
			int index = 0;
			System.arraycopy(this.firstPrefixAndFirstSuffixResults, 0, results, index, this.firstPrefixAndFirstSuffixResultsCount);
			index += this.firstPrefixAndFirstSuffixResultsCount;
			System.arraycopy(this.firstPrefixAndSuffixResults, 0, results, index, this.firstPrefixAndSuffixResultsCount);
			index += this.firstPrefixAndSuffixResultsCount;
			System.arraycopy(this.prefixAndFirstSuffixResults, 0, results, index, this.prefixAndFirstSuffixResultsCount);
			index += this.prefixAndFirstSuffixResultsCount;		
			System.arraycopy(this.prefixAndSuffixResults, 0, results, index, this.prefixAndSuffixResultsCount);
			index += this.prefixAndSuffixResultsCount;
			System.arraycopy(this.firstPrefixResults, 0, results, index, this.firstPrefixResultsCount);
			index += this.firstPrefixResultsCount;
			System.arraycopy(this.prefixResults, 0, results, index, this.prefixResultsCount);
			index += this.prefixResultsCount;
			System.arraycopy(this.firstSuffixResults, 0, results, index, this.firstSuffixResultsCount);
			index += this.firstSuffixResultsCount;
			System.arraycopy(this.suffixResults, 0, results, index, this.suffixResultsCount);
			index += this.suffixResultsCount;
			System.arraycopy(this.otherResults, 0, results, index, this.otherResultsCount);
			
			return results;
		}
	}

	
	private NamingConventions() {
		// Not instantiable
	}

	private static char[] removePrefixAndSuffix(char[] name, char[][] prefixes, char[][] suffixes) {
		// remove longer prefix
		char[] withoutPrefixName = name;
		if (prefixes != null) {
			int bestLength = 0;
			for (int i= 0; i < prefixes.length; i++) {
				char[] prefix = prefixes[i];
				if (CharOperation.prefixEquals(prefix, name)) {
					int currLen = prefix.length;
					boolean lastCharIsLetter = ScannerHelper.isLetter(prefix[currLen - 1]);
					if(!lastCharIsLetter || (lastCharIsLetter && name.length > currLen && ScannerHelper.isUpperCase(name[currLen]))) {
						if (bestLength < currLen && name.length != currLen) {
							withoutPrefixName = CharOperation.subarray(name, currLen, name.length);
							bestLength = currLen;
						}
					}
				}
			}
		}
		
		// remove longer suffix
		char[] withoutSuffixName = withoutPrefixName;
		if(suffixes != null) {
			int bestLength = 0;
			for (int i = 0; i < suffixes.length; i++) {
				char[] suffix = suffixes[i];
				if(CharOperation.endsWith(withoutPrefixName, suffix)) {
					int currLen = suffix.length;
					if(bestLength < currLen && withoutPrefixName.length != currLen) {
						withoutSuffixName = CharOperation.subarray(withoutPrefixName, 0, withoutPrefixName.length - currLen);
						bestLength = currLen;
					}
				}
			}
		}
		
		withoutSuffixName[0] = ScannerHelper.toLowerCase(withoutSuffixName[0]);
		return withoutSuffixName;
	}

	/**
	 * Remove prefix and suffix from an argument name.
	 * <p>
	 * If argument name prefix is <code>pre</code> and argument name suffix is <code>suf</code>
	 * then for an argument named <code>preArgsuf</code> the result of this method is <code>arg</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preArgsuf</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_ARGUMENT_PREFIXES} and
	 *  {@link JavaCore#CODEASSIST_ARGUMENT_SUFFIXES}.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
 	 * 
	 * @param javaProject project which contains the argument.
	 * @param argumentName argument's name.
	 * @return char[] the name without prefix and suffix.
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static char[] removePrefixAndSuffixForArgumentName(IJavaProject javaProject, char[] argumentName) {
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			argumentName,
			assistOptions.argumentPrefixes,
			assistOptions.argumentSuffixes);
	}
	
	/**
	 * Remove prefix and suffix from an argument name.
	 * <p>
	 * If argument name prefix is <code>pre</code> and argument name suffix is <code>suf</code>
	 * then for an argument named <code>preArgsuf</code> the result of this method is <code>arg</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preArgsuf</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_ARGUMENT_PREFIXES} and
	 *  {@link JavaCore#CODEASSIST_ARGUMENT_SUFFIXES}.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
 	 * 
	 * @param javaProject project which contains the argument.
	 * @param argumentName argument's name.
	 * @return char[] the name without prefix and suffix.
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static String removePrefixAndSuffixForArgumentName(IJavaProject javaProject, String argumentName) {
		return String.valueOf(removePrefixAndSuffixForArgumentName(javaProject, argumentName.toCharArray()));
	}

	/**
	 * Remove prefix and suffix from a field name.
	 * <p>
	 * If field name prefix is <code>pre</code> and field name suffix is <code>suf</code>
	 * then for a field named <code>preFieldsuf</code> the result of this method is <code>field</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preFieldsuf</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options : {@link JavaCore#CODEASSIST_FIELD_PREFIXES} } , 
	 *  {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} for instance field and  {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES},
	 *  {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} for static field.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the field.
	 * @param fieldName field's name.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @return char[] the name without prefix and suffix.
	 * @see Flags
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static char[] removePrefixAndSuffixForFieldName(IJavaProject javaProject, char[] fieldName, int modifiers) {
		boolean isStatic = Flags.isStatic(modifiers);
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			fieldName,
			isStatic ? assistOptions.staticFieldPrefixes : assistOptions.fieldPrefixes,
			isStatic ? assistOptions.staticFieldSuffixes : assistOptions.fieldSuffixes);
	}

	/**
	 * Remove prefix and suffix from a field name.
	 * <p>
	 * If field name prefix is <code>pre</code> and field name suffix is <code>suf</code>
	 * then for a field named <code>preFieldsuf</code> the result of this method is <code>field</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preFieldsuf</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_FIELD_PREFIXES}, 
	 *  {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} for instance field and  {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES},
	 *  {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} for static field.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the field.
	 * @param fieldName field's name.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @return char[] the name without prefix and suffix.
	 * @see Flags
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static String removePrefixAndSuffixForFieldName(IJavaProject javaProject, String fieldName, int modifiers) {
		return String.valueOf(removePrefixAndSuffixForFieldName(javaProject, fieldName.toCharArray(), modifiers));
	}
	/**
	 * Remove prefix and suffix from a local variable name.
	 * <p>
	 * If local variable name prefix is <code>pre</code> and local variable name suffix is <code>suf</code>
	 * then for a local variable named <code>preLocalsuf</code> the result of this method is <code>local</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preLocalsuf</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_LOCAL_PREFIXES} and 
	 *  {@link JavaCore#CODEASSIST_LOCAL_SUFFIXES}.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the variable.
	 * @param localName variable's name.
	 * @return char[] the name without prefix and suffix.
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static char[] removePrefixAndSuffixForLocalVariableName(IJavaProject javaProject, char[] localName) {
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			localName,
			assistOptions.localPrefixes,
			assistOptions.localSuffixes);
	}
	
	/**
	 * Remove prefix and suffix from a local variable name.
	 * <p>
	 * If local variable name prefix is <code>pre</code> and local variable name suffix is <code>suf</code>
	 * then for a local variable named <code>preLocalsuf</code> the result of this method is <code>local</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preLocalsuf</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_LOCAL_PREFIXES} and 
	 *  {@link JavaCore#CODEASSIST_LOCAL_SUFFIXES}.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the variable.
	 * @param localName variable's name.
	 * @return char[] the name without prefix and suffix.
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static String removePrefixAndSuffixForLocalVariableName(IJavaProject javaProject, String localName) {
		return String.valueOf(removePrefixAndSuffixForLocalVariableName(javaProject, localName.toCharArray()));
	}

	/**
	 * Suggest names for an argument. The name is computed from argument's type
	 * and possible prefixes or suffixes are added.
	 * <p>
	 * If the type of the argument is <code>TypeName</code>, the prefix for argument is <code>pre</code>
	 * and the suffix for argument is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_ARGUMENT_PREFIXES} and 
	 *  {@link JavaCore#CODEASSIST_ARGUMENT_SUFFIXES}.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the argument.
	 * @param packageName package of the argument's type.
	 * @param qualifiedTypeName argument's type.
	 * @param dim argument's dimension (0 if the argument is not an array).
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static char[][] suggestArgumentNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[][] excludedNames) {
		NamingRequestor requestor = new NamingRequestor();
		InternalNamingConventions.suggestArgumentNames(
			javaProject,
			packageName,
			qualifiedTypeName,
			dim,
			null,
			excludedNames,
			requestor);

		return requestor.getResults();
	}
	
	/**
	 * Suggest names for an argument. The name is computed from argument's type
	 * and possible prefixes or suffixes are added.
	 * <p>
	 * If the type of the argument is <code>TypeName</code>, the prefix for argument is <code>pre</code>
	 * and the suffix for argument is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_ARGUMENT_PREFIXES} and 
	 *  {@link JavaCore#CODEASSIST_ARGUMENT_SUFFIXES}.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the argument.
	 * @param packageName package of the argument's type.
	 * @param qualifiedTypeName argument's type.
	 * @param dim argument's dimension (0 if the argument is not an array).
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static String[] suggestArgumentNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, String[] excludedNames) {
		return convertCharsToString(
			suggestArgumentNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				convertStringToChars(excludedNames)));
	}
	/**
	 * Suggest names for a field. The name is computed from field's type
	 * and possible prefixes or suffixes are added.
	 * <p>
	 * If the type of the field is <code>TypeName</code>, the prefix for field is <code>pre</code>
	 * and the suffix for field is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_FIELD_PREFIXES}, 
	 *  {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} and for instance field and  {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES},
	 *  {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} for static field.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the field.
	 * @param packageName package of the field's type.
	 * @param qualifiedTypeName field's type.
	 * @param dim field's dimension (0 if the field is not an array).
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see Flags
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static char[][] suggestFieldNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, int modifiers, char[][] excludedNames) {
		NamingRequestor requestor = new NamingRequestor();
		InternalNamingConventions.suggestFieldNames(
			javaProject,
			packageName,
			qualifiedTypeName,
			dim,
			modifiers,
			null,
			excludedNames,
			requestor);

		return requestor.getResults();
	}
	
	/**
	 * Suggest names for a field. The name is computed from field's type
	 * and possible prefixes or suffixes are added.
	 * <p>
	 * If the type of the field is <code>TypeName</code>, the prefix for field is <code>pre</code>
	 * and the suffix for field is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_FIELD_PREFIXES}, 
	 *  {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} and for instance field and  {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES},
	 *  {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} for static field.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the field.
	 * @param packageName package of the field's type.
	 * @param qualifiedTypeName field's type.
	 * @param dim field's dimension (0 if the field is not an array).
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see Flags
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static String[] suggestFieldNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, int modifiers, String[] excludedNames) {
		return convertCharsToString(
			suggestFieldNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				modifiers,
				convertStringToChars(excludedNames)));
	}
	
	/**
	 * Suggest names for a local variable. The name is computed from variable's type
	 * and possible prefixes or suffixes are added.
	 * <p>
	 * If the type of the local variable is <code>TypeName</code>, the prefix for local variable is <code>pre</code>
	 * and the suffix for local variable is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_LOCAL_PREFIXES} and
	 *  {@link JavaCore#CODEASSIST_LOCAL_SUFFIXES}.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the variable.
	 * @param packageName package of the variable's type.
	 * @param qualifiedTypeName variable's type.
	 * @param dim variable's dimension (0 if the variable is not an array).
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static char[][] suggestLocalVariableNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[][] excludedNames) {
		NamingRequestor requestor = new NamingRequestor();
		InternalNamingConventions.suggestLocalVariableNames(
			javaProject,
			packageName,
			qualifiedTypeName,
			dim,
			null,
			excludedNames,
			requestor);

		return requestor.getResults();
	}
	
	/**
	 * Suggest names for a local variable. The name is computed from variable's type
	 * and possible prefixes or suffixes are added.
	 * <p>
	 * If the type of the local variable is <code>TypeName</code>, the prefix for local variable is <code>pre</code>
	 * and the suffix for local variable is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_LOCAL_PREFIXES} and
	 *  {@link JavaCore#CODEASSIST_LOCAL_SUFFIXES}.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param javaProject project which contains the variable.
	 * @param packageName package of the variable's type.
	 * @param qualifiedTypeName variable's type.
	 * @param dim variable's dimension (0 if the variable is not an array).
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static String[] suggestLocalVariableNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, String[] excludedNames) {
		return convertCharsToString(
			suggestLocalVariableNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				convertStringToChars(excludedNames)));
	}
	
	/**
	 * Suggest name for a getter method. The name is computed from field's name
	 * and possible prefixes or suffixes are removed.
	 * <p>
	 * If the field name is <code>preFieldNamesuf</code> and the prefix for field is <code>pre</code> and
	 * the suffix for field is <code>suf</code> then the prosposed name is <code>isFieldName</code> for boolean field or
	 * <code>getFieldName</code> for others. If there is no prefix and suffix the proposal is <code>isPreFieldNamesuf</code>
	 * for boolean field or <code>getPreFieldNamesuf</code> for others.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_FIELD_PREFIXES}, 
	 *  {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} for instance field and  {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES},
	 *  {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} for static field.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 * @see Flags
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static char[] suggestGetterName(IJavaProject project, char[] fieldName, int modifiers, boolean isBoolean, char[][] excludedNames) {
		if (isBoolean) {
			char[] name = removePrefixAndSuffixForFieldName(project, fieldName, modifiers);
			int prefixLen =  GETTER_BOOL_NAME.length;
			if (CharOperation.prefixEquals(GETTER_BOOL_NAME, name) 
				&& name.length > prefixLen && ScannerHelper.isUpperCase(name[prefixLen])) {
				return suggestNewName(name, excludedNames);
			} else {
				return suggestNewName(
					CharOperation.concat(GETTER_BOOL_NAME, suggestAccessorName(project, fieldName, modifiers)),
					excludedNames
				);
			}
		} else {
			return suggestNewName(
				CharOperation.concat(GETTER_NAME, suggestAccessorName(project, fieldName, modifiers)),
				excludedNames
			);
		}
	}
	
	/**
	 * Suggest name for a getter method. The name is computed from field's name
	 * and possible prefixes or suffixes are removed.
	 * <p>
	 * If the field name is <code>preFieldNamesuf</code> and the prefix for field is <code>pre</code> and
	 * the suffix for field is <code>suf</code> then the prosposed name is <code>isFieldName</code> for boolean field or
	 * <code>getFieldName</code> for others. If there is no prefix and suffix the proposal is <code>isPreFieldNamesuf</code>
	 * for boolean field or <code>getPreFieldNamesuf</code> for others.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_FIELD_PREFIXES}, 
	 *  {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} for instance field and  {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES},
	 *  {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} for static field.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 * @see Flags
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static String suggestGetterName(IJavaProject project, String fieldName, int modifiers, boolean isBoolean, String[] excludedNames) {
		return String.valueOf(
			suggestGetterName(
				project,
				fieldName.toCharArray(),
				modifiers,
				isBoolean,
				convertStringToChars(excludedNames)));
	}

	/**
	 * Suggest name for a setter method. The name is computed from field's name
	 * and possible prefixes or suffixes are removed.
	 * <p>
	 * If the field name is <code>preFieldNamesuf</code> and the prefix for field is <code>pre</code> and
	 * the suffix for field is <code>suf</code> then the proposed name is <code>setFieldName</code>.
	 * If there is no prefix and suffix the proposal is <code>setPreFieldNamesuf</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_FIELD_PREFIXES}, 
	 *  {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} for instance field and  {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES},
	 *  {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} for static field.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 * @see Flags
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static char[] suggestSetterName(IJavaProject project, char[] fieldName, int modifiers, boolean isBoolean, char[][] excludedNames) {

		if (isBoolean) {
			char[] name = removePrefixAndSuffixForFieldName(project, fieldName, modifiers);
			int prefixLen =  GETTER_BOOL_NAME.length;
			if (CharOperation.prefixEquals(GETTER_BOOL_NAME, name) 
				&& name.length > prefixLen && ScannerHelper.isUpperCase(name[prefixLen])) {
				name = CharOperation.subarray(name, prefixLen, name.length);
				return suggestNewName(
					CharOperation.concat(SETTER_NAME, suggestAccessorName(project, name, modifiers)),
					excludedNames
				);
			} else {
				return suggestNewName(
					CharOperation.concat(SETTER_NAME, suggestAccessorName(project, fieldName, modifiers)),
					excludedNames
				);
			}
		} else {
			return suggestNewName(
				CharOperation.concat(SETTER_NAME, suggestAccessorName(project, fieldName, modifiers)),
				excludedNames
			);
		}
	}
	
	/**
	 * Suggest name for a setter method. The name is computed from field's name
	 * and possible prefixes or suffixes are removed.
	 * <p>
	 * If the field name is <code>preFieldNamesuf</code> and the prefix for field is <code>pre</code> and
	 * the suffix for field is <code>suf</code> then the proposed name is <code>setFieldName</code>.
	 * If there is no prefix and suffix the proposal is <code>setPreFieldNamesuf</code>.
	 * </p>
	 * <p>
	 * This method is affected by the following JavaCore options :  {@link JavaCore#CODEASSIST_FIELD_PREFIXES}, 
	 *  {@link JavaCore#CODEASSIST_FIELD_SUFFIXES} for instance field and  {@link JavaCore#CODEASSIST_STATIC_FIELD_PREFIXES},
	 *  {@link JavaCore#CODEASSIST_STATIC_FIELD_SUFFIXES} for static field.
	 * </p>
	 * <p>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * </p>
	 * 
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 * @see Flags
	 * @see JavaCore#setOptions(java.util.Hashtable)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static String suggestSetterName(IJavaProject project, String fieldName, int modifiers, boolean isBoolean, String[] excludedNames) {
		return String.valueOf(
			suggestSetterName(
				project,
				fieldName.toCharArray(),
				modifiers,
				isBoolean,
				convertStringToChars(excludedNames)));
	}
	
	private static char[] suggestAccessorName(IJavaProject project, char[] fieldName, int modifiers) {
		char[] name = removePrefixAndSuffixForFieldName(project, fieldName, modifiers);
		if (name.length > 0 && ScannerHelper.isLowerCase(name[0])) {
			name[0] = ScannerHelper.toUpperCase(name[0]);
		}
		return name;
	}
	
	private static char[] suggestNewName(char[] name, char[][] excludedNames){
		if(excludedNames == null) {
			return name;
		}
		
		char[] newName = name;
		int count = 2;
		int i = 0;
		while (i < excludedNames.length) {
			if(CharOperation.equals(newName, excludedNames[i], false)) {
				newName = CharOperation.concat(name, String.valueOf(count++).toCharArray());
				i = 0;
			} else {
				i++;
			}
		}
		return newName;
	}
	
	private static String[] convertCharsToString(char[][] c) {
		int length = c == null ? 0 : c.length;
		String[] s = new String[length];
		for (int i = 0; i < length; i++) {
			s[i] = String.valueOf(c[i]);
		}
		return s;
	}
	
	private static char[][] convertStringToChars(String[] s) {
		int length = s == null ? 0 : s.length;
		char[][] c = new char[length][];
		for (int i = 0; i < length; i++) {
			if(s[i] == null) {
				c[i] = CharOperation.NO_CHAR;
			} else {
				c[i] = s[i].toCharArray();
			}
		}
		return c;
	}
}
