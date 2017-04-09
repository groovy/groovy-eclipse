/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tim Hanson <thanson@bea.com> - fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=137634
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class ReferenceCollection {

char[][][] qualifiedNameReferences; // contains no simple names as in just 'a' which is kept in simpleNameReferences instead
char[][] simpleNameReferences;
char[][] rootReferences;

protected ReferenceCollection(char[][][] qualifiedNameReferences, char[][] simpleNameReferences, char[][] rootReferences) {
	this.qualifiedNameReferences = internQualifiedNames(qualifiedNameReferences, false);
	this.simpleNameReferences = internSimpleNames(simpleNameReferences, true);
	this.rootReferences = internSimpleNames(rootReferences, false);
}

public void addDependencies(String[] typeNameDependencies) {
	// if each qualified type name is already known then all of its subNames can be skipped
	// and its expected that very few qualified names in typeNameDependencies need to be added
	// but could always take 'p1.p2.p3.X' and make all qualified names 'p1' 'p1.p2' 'p1.p2.p3' 'p1.p2.p3.X', then intern
	char[][][] qNames = new char[typeNameDependencies.length][][];
	for (int i = typeNameDependencies.length; --i >= 0;)
		qNames[i] = CharOperation.splitOn('.', typeNameDependencies[i].toCharArray());
	qNames = internQualifiedNames(qNames, false);

	next : for (int i = qNames.length; --i >= 0;) {
		char[][] qualifiedTypeName = qNames[i];
		while (!includes(qualifiedTypeName)) {
			if (!includes(qualifiedTypeName[qualifiedTypeName.length - 1])) {
				int length = this.simpleNameReferences.length;
				System.arraycopy(this.simpleNameReferences, 0, this.simpleNameReferences = new char[length + 1][], 0, length);
				this.simpleNameReferences[length] = qualifiedTypeName[qualifiedTypeName.length - 1];
			}
			if (!insideRoot(qualifiedTypeName[0])) {
				int length = this.rootReferences.length;
				System.arraycopy(this.rootReferences, 0, this.rootReferences = new char[length + 1][], 0, length);
				this.rootReferences[length] = qualifiedTypeName[0];
			}
			int length = this.qualifiedNameReferences.length;
			System.arraycopy(this.qualifiedNameReferences, 0, this.qualifiedNameReferences = new char[length + 1][][], 0, length);
			this.qualifiedNameReferences[length] = qualifiedTypeName;

			qualifiedTypeName = CharOperation.subarray(qualifiedTypeName, 0, qualifiedTypeName.length - 1);
			char[][][] temp = internQualifiedNames(new char[][][] {qualifiedTypeName}, false);
			if (temp == EmptyQualifiedNames)
				continue next; // qualifiedTypeName is a well known name
			qualifiedTypeName = temp[0];
		}
	}
}

public boolean includes(char[] simpleName) {
	for (int i = 0, l = this.simpleNameReferences.length; i < l; i++)
		if (simpleName == this.simpleNameReferences[i]) return true;
	return false;
}

public boolean includes(char[][] qualifiedName) {
	for (int i = 0, l = this.qualifiedNameReferences.length; i < l; i++)
		if (qualifiedName == this.qualifiedNameReferences[i]) return true;
	return false;
}

/**
 * @deprecated
 */
public boolean includes(char[][][] qualifiedNames, char[][] simpleNames) {
	return includes(qualifiedNames, simpleNames, null);
}

public boolean includes(char[][][] qualifiedNames, char[][] simpleNames, char[][] rootNames) {
	// if either collection of names is null, it means it contained a well known name so we know it already has a match
	if (rootNames != null) {
		boolean foundRoot = false;
		for (int i = 0, l = rootNames.length; !foundRoot && i < l; i++)
			foundRoot = insideRoot(rootNames[i]);
		if (!foundRoot)
			return false;
	}
	if (simpleNames == null || qualifiedNames == null) {
		if (simpleNames == null && qualifiedNames == null) {
			if (JavaBuilder.DEBUG)
				System.out.println("Found well known match"); //$NON-NLS-1$
			return true;
		} else if (qualifiedNames == null) {
			for (int i = 0, l = simpleNames.length; i < l; i++) {
				if (includes(simpleNames[i])) {
					if (JavaBuilder.DEBUG)
						System.out.println("Found match in well known package to " + new String(simpleNames[i])); //$NON-NLS-1$
					return true;
				}
			}
		} else {
			for (int i = 0, l = qualifiedNames.length; i < l; i++) {
				char[][] qualifiedName = qualifiedNames[i];
				if (qualifiedName.length == 1 ? includes(qualifiedName[0]) : includes(qualifiedName)) {
					if (JavaBuilder.DEBUG)
						System.out.println("Found well known match in " + CharOperation.toString(qualifiedName)); //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}

	int sLength = simpleNames.length;
	int qLength = qualifiedNames.length;
	if (sLength <= qLength) {
		for (int i = 0; i < sLength; i++) {
			if (includes(simpleNames[i])) {
				for (int j = 0; j < qLength; j++) {
					char[][] qualifiedName = qualifiedNames[j];
					if (qualifiedName.length == 1 ? includes(qualifiedName[0]) : includes(qualifiedName)) {
						if (JavaBuilder.DEBUG)
							System.out.println("Found match in " + CharOperation.toString(qualifiedName) //$NON-NLS-1$
								+ " to " + new String(simpleNames[i])); //$NON-NLS-1$
						return true;
					}
				}
				return false;
			}
		}
	} else {
		for (int i = 0; i < qLength; i++) {
			char[][] qualifiedName = qualifiedNames[i];
			if (qualifiedName.length == 1 ? includes(qualifiedName[0]) : includes(qualifiedName)) {
				for (int j = 0; j < sLength; j++) {
					if (includes(simpleNames[j])) {
						if (JavaBuilder.DEBUG)
							System.out.println("Found match in " + CharOperation.toString(qualifiedName) //$NON-NLS-1$
								+ " to " + new String(simpleNames[j])); //$NON-NLS-1$
						return true;
					}
				}
				return false;
			}
		}
	}
	return false;
}

public boolean insideRoot(char[] rootName) {
	for (int i = 0, l = this.rootReferences.length; i < l; i++)
		if (rootName == this.rootReferences[i]) return true;
	return false;
}


// When any type is compiled, its methods are verified for certain problems
// the MethodVerifier requests 3 well known types which end up in the reference collection
// having WellKnownQualifiedNames & WellKnownSimpleNames, saves every type 40 bytes
// NOTE: These collections are sorted by length
static final char[][][] WellKnownQualifiedNames = new char[][][] {
	TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION,
	TypeConstants.JAVA_LANG_THROWABLE,
	TypeConstants.JAVA_LANG_OBJECT,
	TypeConstants.JAVA_LANG,
	new char[][] {TypeConstants.JAVA},
	new char[][] {new char[] {'o', 'r', 'g'}},
	new char[][] {new char[] {'c', 'o', 'm'}},
	CharOperation.NO_CHAR_CHAR}; // default package
static final char[][] WellKnownSimpleNames = new char[][] {
	TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION[2],
	TypeConstants.JAVA_LANG_THROWABLE[2],
	TypeConstants.JAVA_LANG_OBJECT[2],
	TypeConstants.JAVA,
	TypeConstants.LANG,
	new char[] {'o', 'r', 'g'},
	new char[] {'c', 'o', 'm'}};

static final char[][][] EmptyQualifiedNames = new char[0][][];
static final char[][] EmptySimpleNames = CharOperation.NO_CHAR_CHAR;

// each array contains qualified char[][], one for size 2, 3, 4, 5, 6, 7 & the rest
static final int MaxQualifiedNames = 7;
static QualifiedNameSet[] InternedQualifiedNames = new QualifiedNameSet[MaxQualifiedNames];
// each array contains simple char[], one for size 1 to 29 & the rest
static final int MaxSimpleNames = 30;
static NameSet[] InternedSimpleNames = new NameSet[MaxSimpleNames];
static {
	for (int i = 0; i < MaxQualifiedNames; i++)
		InternedQualifiedNames[i] = new QualifiedNameSet(37);
	for (int i = 0; i < MaxSimpleNames; i++)
		InternedSimpleNames[i] = new NameSet(37);
}

public static char[][][] internQualifiedNames(StringSet qualifiedStrings) {
	if (qualifiedStrings == null) return EmptyQualifiedNames;
	int length = qualifiedStrings.elementSize;
	if (length == 0) return EmptyQualifiedNames;

	char[][][] result = new char[length][][];
	String[] strings = qualifiedStrings.values;
	for (int i = 0, l = strings.length; i < l; i++)
		if (strings[i] != null)
			result[--length] = CharOperation.splitOn('/', strings[i].toCharArray());
	return internQualifiedNames(result, false);
}

public static char[][][] internQualifiedNames(char[][][] qualifiedNames) {
	return internQualifiedNames(qualifiedNames, false);
}

public static char[][][] internQualifiedNames(char[][][] qualifiedNames, boolean keepWellKnown) {
	if (qualifiedNames == null) return EmptyQualifiedNames;
	int length = qualifiedNames.length;
	if (length == 0) return EmptyQualifiedNames;

	char[][][] keepers = new char[length][][];
	int index = 0;
	next : for (int i = 0; i < length; i++) {
		char[][] qualifiedName = qualifiedNames[i];
		int qLength = qualifiedName.length;
		for (int j = 0, m = WellKnownQualifiedNames.length; j < m; j++) {
			char[][] wellKnownName = WellKnownQualifiedNames[j];
			if (qLength > wellKnownName.length)
				break; // all remaining well known names are shorter
			if (CharOperation.equals(qualifiedName, wellKnownName)) {
				if (keepWellKnown) {
					keepers[index++] = wellKnownName;
				}
				continue next;
			}
		}

		// InternedQualifiedNames[0] is for the rest (> 7 & 1)
		// InternedQualifiedNames[1] is for size 2...
		// InternedQualifiedNames[6] is for size 7
		QualifiedNameSet internedNames = InternedQualifiedNames[qLength <= MaxQualifiedNames ? qLength - 1 : 0];
		qualifiedName = internSimpleNames(qualifiedName, false);
		keepers[index++] = internedNames.add(qualifiedName);
	}
	if (length > index) {
		if (index == 0) return EmptyQualifiedNames;
		System.arraycopy(keepers, 0, keepers = new char[index][][], 0, index);
	}
	return keepers;
}

/**
 * @deprecated
 */
public static char[][] internSimpleNames(StringSet simpleStrings) {
	return internSimpleNames(simpleStrings, true);
}

public static char[][] internSimpleNames(StringSet simpleStrings, boolean removeWellKnown) {
	if (simpleStrings == null) return EmptySimpleNames;
	int length = simpleStrings.elementSize;
	if (length == 0) return EmptySimpleNames;

	char[][] result = new char[length][];
	String[] strings = simpleStrings.values;
	for (int i = 0, l = strings.length; i < l; i++)
		if (strings[i] != null)
			result[--length] = strings[i].toCharArray();
	return internSimpleNames(result, removeWellKnown);
}

public static char[][] internSimpleNames(char[][] simpleNames, boolean removeWellKnown) {
	if (simpleNames == null) return EmptySimpleNames;
	int length = simpleNames.length;
	if (length == 0) return EmptySimpleNames;

	char[][] keepers = new char[length][];
	int index = 0;
	next : for (int i = 0; i < length; i++) {
		char[] name = simpleNames[i];
		int sLength = name.length;
		for (int j = 0, m = WellKnownSimpleNames.length; j < m; j++) {
			char[] wellKnownName = WellKnownSimpleNames[j];
			if (sLength > wellKnownName.length)
				break; // all remaining well known names are shorter
			if (CharOperation.equals(name, wellKnownName)) {
				if (!removeWellKnown)
					keepers[index++] = WellKnownSimpleNames[j];
				continue next;
			}
		}

		// InternedSimpleNames[0] is for the rest (> 29)
		// InternedSimpleNames[1] is for size 1...
		// InternedSimpleNames[29] is for size 29
		NameSet internedNames = InternedSimpleNames[sLength < MaxSimpleNames ? sLength : 0];
		keepers[index++] = internedNames.add(name);
	}
	if (length > index) {
		if (index == 0) return EmptySimpleNames;
		System.arraycopy(keepers, 0, keepers = new char[index][], 0, index);
	}
	return keepers;
}
}
