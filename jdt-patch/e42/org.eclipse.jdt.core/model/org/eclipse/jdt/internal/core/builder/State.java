/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.ClasspathAccessRule;
import org.eclipse.jdt.internal.core.JavaModelManager;

import java.io.*;
import java.util.*;

public class State {
// NOTE: this state cannot contain types that are not defined in this project

String javaProjectName;
public ClasspathMultiDirectory[] sourceLocations;
ClasspathLocation[] binaryLocations;
// keyed by the project relative path of the type (i.e. "src1/p1/p2/A.java"), value is a ReferenceCollection or an AdditionalTypeCollection
SimpleLookupTable references;
// keyed by qualified type name "p1/p2/A", value is the project relative path which defines this type "src1/p1/p2/A.java"
public SimpleLookupTable typeLocators;

int buildNumber;
long lastStructuralBuildTime;
SimpleLookupTable structuralBuildTimes;

private String[] knownPackageNames; // of the form "p1/p2"

private long previousStructuralBuildTime;
private StringSet structurallyChangedTypes;
public static int MaxStructurallyChangedTypes = 100; // keep track of ? structurally changed types, otherwise consider all to be changed

public static final byte VERSION = 0x001B;

static final byte SOURCE_FOLDER = 1;
static final byte BINARY_FOLDER = 2;
static final byte EXTERNAL_JAR = 3;
static final byte INTERNAL_JAR = 4;

State() {
	// constructor with no argument
}

protected State(JavaBuilder javaBuilder) {
	this.knownPackageNames = null;
	this.previousStructuralBuildTime = -1;
	this.structurallyChangedTypes = null;
	this.javaProjectName = javaBuilder.currentProject.getName();
	this.sourceLocations = javaBuilder.nameEnvironment.sourceLocations;
	this.binaryLocations = javaBuilder.nameEnvironment.binaryLocations;
	this.references = new SimpleLookupTable(7);
	this.typeLocators = new SimpleLookupTable(7);

	this.buildNumber = 0; // indicates a full build
	this.lastStructuralBuildTime = computeStructuralBuildTime(javaBuilder.lastState == null ? 0 : javaBuilder.lastState.lastStructuralBuildTime);
	this.structuralBuildTimes = new SimpleLookupTable(3);
}

long computeStructuralBuildTime(long previousTime) {
	long newTime = System.currentTimeMillis();
	if (newTime <= previousTime)
		newTime = previousTime + 1;
	return newTime;
}

void copyFrom(State lastState) {
	this.knownPackageNames = null;
	this.previousStructuralBuildTime = lastState.previousStructuralBuildTime;
	this.structurallyChangedTypes = lastState.structurallyChangedTypes;
	this.buildNumber = lastState.buildNumber + 1;
	this.lastStructuralBuildTime = lastState.lastStructuralBuildTime;
	this.structuralBuildTimes = lastState.structuralBuildTimes;

	try {
		this.references = (SimpleLookupTable) lastState.references.clone();
		this.typeLocators = (SimpleLookupTable) lastState.typeLocators.clone();
	} catch (CloneNotSupportedException e) {
		this.references = new SimpleLookupTable(lastState.references.elementSize);
		Object[] keyTable = lastState.references.keyTable;
		Object[] valueTable = lastState.references.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++)
			if (keyTable[i] != null)
				this.references.put(keyTable[i], valueTable[i]);

		this.typeLocators = new SimpleLookupTable(lastState.typeLocators.elementSize);
		keyTable = lastState.typeLocators.keyTable;
		valueTable = lastState.typeLocators.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++)
			if (keyTable[i] != null)
				this.typeLocators.put(keyTable[i], valueTable[i]);
	}
}
public char[][] getDefinedTypeNamesFor(String typeLocator) {
	Object c = this.references.get(typeLocator);
	if (c instanceof AdditionalTypeCollection)
		return ((AdditionalTypeCollection) c).definedTypeNames;
	return null; // means only one type is defined with the same name as the file... saves space
}

public SimpleLookupTable getReferences() {
	return this.references;
}

StringSet getStructurallyChangedTypes(State prereqState) {
	if (prereqState != null && prereqState.previousStructuralBuildTime > 0) {
		Object o = this.structuralBuildTimes.get(prereqState.javaProjectName);
		long previous = o == null ? 0 : ((Long) o).longValue();
		if (previous == prereqState.previousStructuralBuildTime)
			return prereqState.structurallyChangedTypes;
	}
	return null;
}

public boolean isDuplicateLocator(String qualifiedTypeName, String typeLocator) {
	String existing = (String) this.typeLocators.get(qualifiedTypeName);
	return existing != null && !existing.equals(typeLocator);
}

public boolean isKnownPackage(String qualifiedPackageName) {
	if (this.knownPackageNames == null) {
		ArrayList names = new ArrayList(this.typeLocators.elementSize);
		Object[] keyTable = this.typeLocators.keyTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			if (keyTable[i] != null) {
				String packageName = (String) keyTable[i]; // is a type name of the form p1/p2/A
				int last = packageName.lastIndexOf('/');
				packageName = last == -1 ? null : packageName.substring(0, last);
				while (packageName != null && !names.contains(packageName)) {
					names.add(packageName);
					last = packageName.lastIndexOf('/');
					packageName = last == -1 ? null : packageName.substring(0, last);
				}
			}
		}
		this.knownPackageNames = new String[names.size()];
		names.toArray(this.knownPackageNames);
	}
	for (int i = 0, l = this.knownPackageNames.length; i < l; i++)
		if (this.knownPackageNames[i].equals(qualifiedPackageName))
			return true;
	return false;
}

public boolean isKnownType(String qualifiedTypeName) {
	return this.typeLocators.containsKey(qualifiedTypeName);
}

boolean isSourceFolderEmpty(IContainer sourceFolder) {
	String sourceFolderName = sourceFolder.getProjectRelativePath().addTrailingSeparator().toString();
	Object[] table = this.typeLocators.valueTable;
	for (int i = 0, l = table.length; i < l; i++)
		if (table[i] != null && ((String) table[i]).startsWith(sourceFolderName))
			return false;
	return true;
}

void record(String typeLocator, char[][][] qualifiedRefs, char[][] simpleRefs, char[][] rootRefs, char[] mainTypeName, ArrayList typeNames) {
	if (typeNames.size() == 1 && CharOperation.equals(mainTypeName, (char[]) typeNames.get(0))) {
		this.references.put(typeLocator, new ReferenceCollection(qualifiedRefs, simpleRefs, rootRefs));
	} else {
		char[][] definedTypeNames = new char[typeNames.size()][]; // can be empty when no types are defined
		typeNames.toArray(definedTypeNames);
		this.references.put(typeLocator, new AdditionalTypeCollection(definedTypeNames, qualifiedRefs, simpleRefs, rootRefs));
	}
}

void recordLocatorForType(String qualifiedTypeName, String typeLocator) {
	this.knownPackageNames = null;
	// in the common case, the qualifiedTypeName is a substring of the typeLocator so share the char[] by using String.substring()
	int start = typeLocator.indexOf(qualifiedTypeName, 0);
	if (start > 0)
		qualifiedTypeName = typeLocator.substring(start, start + qualifiedTypeName.length());
	this.typeLocators.put(qualifiedTypeName, typeLocator);
}

void recordStructuralDependency(IProject prereqProject, State prereqState) {
	if (prereqState != null)
		if (prereqState.lastStructuralBuildTime > 0) // can skip if 0 (full build) since its assumed to be 0 if unknown
			this.structuralBuildTimes.put(prereqProject.getName(), new Long(prereqState.lastStructuralBuildTime));
}

void removeLocator(String typeLocatorToRemove) {
	this.knownPackageNames = null;
	this.references.removeKey(typeLocatorToRemove);
	this.typeLocators.removeValue(typeLocatorToRemove);
}

void removePackage(IResourceDelta sourceDelta) {
	IResource resource = sourceDelta.getResource();
	switch(resource.getType()) {
		case IResource.FOLDER :
			IResourceDelta[] children = sourceDelta.getAffectedChildren();
			for (int i = 0, l = children.length; i < l; i++)
				removePackage(children[i]);
			return;
		case IResource.FILE :
			IPath typeLocatorPath = resource.getProjectRelativePath();
			if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(typeLocatorPath.lastSegment()))
				removeLocator(typeLocatorPath.toString());
	}
}

void removeQualifiedTypeName(String qualifiedTypeNameToRemove) {
	this.knownPackageNames = null;
	this.typeLocators.removeKey(qualifiedTypeNameToRemove);
}

static State read(IProject project, DataInputStream in) throws IOException {
	if (JavaBuilder.DEBUG)
		System.out.println("About to read state " + project.getName()); //$NON-NLS-1$
	if (VERSION != in.readByte()) {
		if (JavaBuilder.DEBUG)
			System.out.println("Found non-compatible state version... answered null for " + project.getName()); //$NON-NLS-1$
		return null;
	}

	State newState = new State();
	newState.javaProjectName = in.readUTF();
	if (!project.getName().equals(newState.javaProjectName)) {
		if (JavaBuilder.DEBUG)
			System.out.println("Project's name does not match... answered null"); //$NON-NLS-1$
		return null;
	}
	newState.buildNumber = in.readInt();
	newState.lastStructuralBuildTime = in.readLong();

	int length = in.readInt();
	newState.sourceLocations = new ClasspathMultiDirectory[length];
	for (int i = 0; i < length; i++) {
		IContainer sourceFolder = project, outputFolder = project;
		String folderName;
		if ((folderName = in.readUTF()).length() > 0) sourceFolder = project.getFolder(folderName);
		if ((folderName = in.readUTF()).length() > 0) outputFolder = project.getFolder(folderName);
		ClasspathMultiDirectory md =
			(ClasspathMultiDirectory) ClasspathLocation.forSourceFolder(sourceFolder, outputFolder, readNames(in), readNames(in), in.readBoolean());
		if (in.readBoolean())
			md.hasIndependentOutputFolder = true;
		newState.sourceLocations[i] = md;
	}

	length = in.readInt();
	newState.binaryLocations = new ClasspathLocation[length];
	IWorkspaceRoot root = project.getWorkspace().getRoot();
	for (int i = 0; i < length; i++) {
		switch (in.readByte()) {
			case SOURCE_FOLDER :
				newState.binaryLocations[i] = newState.sourceLocations[in.readInt()];
				break;
			case BINARY_FOLDER :
				IPath path = new Path(in.readUTF());
				IContainer outputFolder = path.segmentCount() == 1
					? (IContainer) root.getProject(path.toString())
					: (IContainer) root.getFolder(path);
				newState.binaryLocations[i] = ClasspathLocation.forBinaryFolder(outputFolder, in.readBoolean(), readRestriction(in));
				break;
			case EXTERNAL_JAR :
				newState.binaryLocations[i] = ClasspathLocation.forLibrary(in.readUTF(), in.readLong(), readRestriction(in));
				break;
			case INTERNAL_JAR :
				newState.binaryLocations[i] = ClasspathLocation.forLibrary(root.getFile(new Path(in.readUTF())), readRestriction(in));
		}
	}

	newState.structuralBuildTimes = new SimpleLookupTable(length = in.readInt());
	for (int i = 0; i < length; i++)
		newState.structuralBuildTimes.put(in.readUTF(), new Long(in.readLong()));

	String[] internedTypeLocators = new String[length = in.readInt()];
	for (int i = 0; i < length; i++)
		internedTypeLocators[i] = in.readUTF();

	newState.typeLocators = new SimpleLookupTable(length = in.readInt());
	for (int i = 0; i < length; i++)
		newState.recordLocatorForType(in.readUTF(), internedTypeLocators[in.readInt()]);

	char[][] internedRootNames = ReferenceCollection.internSimpleNames(readNames(in), false);
	char[][] internedSimpleNames = ReferenceCollection.internSimpleNames(readNames(in), false);
	char[][][] internedQualifiedNames = new char[length = in.readInt()][][];
	for (int i = 0; i < length; i++) {
		int qLength = in.readInt();
		char[][] qName = new char[qLength][];
		for (int j = 0; j < qLength; j++)
			qName[j] = internedSimpleNames[in.readInt()];
		internedQualifiedNames[i] = qName;
	}
	internedQualifiedNames = ReferenceCollection.internQualifiedNames(internedQualifiedNames, false);

	newState.references = new SimpleLookupTable(length = in.readInt());
	for (int i = 0; i < length; i++) {
		String typeLocator = internedTypeLocators[in.readInt()];
		ReferenceCollection collection = null;
		switch (in.readByte()) {
			case 1 :
				char[][] additionalTypeNames = readNames(in);
				char[][][] qualifiedNames = new char[in.readInt()][][];
				for (int j = 0, m = qualifiedNames.length; j < m; j++)
					qualifiedNames[j] = internedQualifiedNames[in.readInt()];
				char[][] simpleNames = new char[in.readInt()][];
				for (int j = 0, m = simpleNames.length; j < m; j++)
					simpleNames[j] = internedSimpleNames[in.readInt()];
				char[][] rootNames = new char[in.readInt()][];
				for (int j = 0, m = rootNames.length; j < m; j++)
					rootNames[j] = internedRootNames[in.readInt()];
				collection = new AdditionalTypeCollection(additionalTypeNames, qualifiedNames, simpleNames, rootNames);
				break;
			case 2 :
				char[][][] qNames = new char[in.readInt()][][];
				for (int j = 0, m = qNames.length; j < m; j++)
					qNames[j] = internedQualifiedNames[in.readInt()];
				char[][] sNames = new char[in.readInt()][];
				for (int j = 0, m = sNames.length; j < m; j++)
					sNames[j] = internedSimpleNames[in.readInt()];
				char[][] rNames = new char[in.readInt()][];
				for (int j = 0, m = rNames.length; j < m; j++)
					rNames[j] = internedRootNames[in.readInt()];
				collection = new ReferenceCollection(qNames, sNames, rNames);
		}
		newState.references.put(typeLocator, collection);
	}
	if (JavaBuilder.DEBUG)
		System.out.println("Successfully read state for " + newState.javaProjectName); //$NON-NLS-1$
	return newState;
}

private static char[] readName(DataInputStream in) throws IOException {
	int nLength = in.readInt();
	char[] name = new char[nLength];
	for (int j = 0; j < nLength; j++)
		name[j] = in.readChar();
	return name;
}

private static char[][] readNames(DataInputStream in) throws IOException {
	int length = in.readInt();
	char[][] names = new char[length][];
	for (int i = 0; i < length; i++)
		names[i] = readName(in);
	return names;
}

private static AccessRuleSet readRestriction(DataInputStream in) throws IOException {
	int length = in.readInt();
	if (length == 0) return null; // no restriction specified
	AccessRule[] accessRules = new AccessRule[length];
	for (int i = 0; i < length; i++) {
		char[] pattern = readName(in);
		int problemId = in.readInt();
		accessRules[i] = new ClasspathAccessRule(pattern, problemId);
	}
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	return new AccessRuleSet(accessRules, in.readByte(), manager.intern(in.readUTF()));
}

void tagAsNoopBuild() {
	this.buildNumber = -1; // tag the project since it has no source folders and can be skipped
}

boolean wasNoopBuild() {
	return this.buildNumber == -1;
}

void tagAsStructurallyChanged() {
	this.previousStructuralBuildTime = this.lastStructuralBuildTime;
	this.structurallyChangedTypes = new StringSet(7);
	this.lastStructuralBuildTime = computeStructuralBuildTime(this.previousStructuralBuildTime);
}

boolean wasStructurallyChanged(IProject prereqProject, State prereqState) {
	if (prereqState != null) {
		Object o = this.structuralBuildTimes.get(prereqProject.getName());
		long previous = o == null ? 0 : ((Long) o).longValue();
		if (previous == prereqState.lastStructuralBuildTime) return false;
	}
	return true;
}

void wasStructurallyChanged(String typeName) {
	if (this.structurallyChangedTypes != null) {
		if (this.structurallyChangedTypes.elementSize > MaxStructurallyChangedTypes)
			this.structurallyChangedTypes = null; // too many to keep track of
		else
			this.structurallyChangedTypes.add(typeName);
	}
}

void write(DataOutputStream out) throws IOException {
	int length;
	Object[] keyTable;
	Object[] valueTable;

/*
 * byte		VERSION
 * String		project name
 * int			build number
 * int			last structural build number
*/
	out.writeByte(VERSION);
	out.writeUTF(this.javaProjectName);
	out.writeInt(this.buildNumber);
	out.writeLong(this.lastStructuralBuildTime);

/*
 * ClasspathMultiDirectory[]
 * int			id
 * String		path(s)
*/
	out.writeInt(length = this.sourceLocations.length);
	for (int i = 0; i < length; i++) {
		ClasspathMultiDirectory md = this.sourceLocations[i];
		out.writeUTF(md.sourceFolder.getProjectRelativePath().toString());
		out.writeUTF(md.binaryFolder.getProjectRelativePath().toString());
		writeNames(md.inclusionPatterns, out);
		writeNames(md.exclusionPatterns, out);
		out.writeBoolean(md.ignoreOptionalProblems);
		out.writeBoolean(md.hasIndependentOutputFolder);
	}

/*
 * ClasspathLocation[]
 * int			id
 * String		path(s)
*/
	out.writeInt(length = this.binaryLocations.length);
	next : for (int i = 0; i < length; i++) {
		ClasspathLocation c = this.binaryLocations[i];
		if (c instanceof ClasspathMultiDirectory) {
			out.writeByte(SOURCE_FOLDER);
			for (int j = 0, m = this.sourceLocations.length; j < m; j++) {
				if (this.sourceLocations[j] == c) {
					out.writeInt(j);
					continue next;
				}
			}
		} else if (c instanceof ClasspathDirectory) {
			out.writeByte(BINARY_FOLDER);
			ClasspathDirectory cd = (ClasspathDirectory) c;
			out.writeUTF(cd.binaryFolder.getFullPath().toString());
			out.writeBoolean(cd.isOutputFolder);
			writeRestriction(cd.accessRuleSet, out);
		} else {
			ClasspathJar jar = (ClasspathJar) c;
			if (jar.resource == null) {
				out.writeByte(EXTERNAL_JAR);
				out.writeUTF(jar.zipFilename);
				out.writeLong(jar.lastModified());
			} else {
				out.writeByte(INTERNAL_JAR);
				out.writeUTF(jar.resource.getFullPath().toString());
			}
			writeRestriction(jar.accessRuleSet, out);
		}
	}

/*
 * Structural build numbers table
 * String		prereq project name
 * int			last structural build number
*/
	out.writeInt(length = this.structuralBuildTimes.elementSize);
	if (length > 0) {
		keyTable = this.structuralBuildTimes.keyTable;
		valueTable = this.structuralBuildTimes.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			if (keyTable[i] != null) {
				length--;
				out.writeUTF((String) keyTable[i]);
				out.writeLong(((Long) valueTable[i]).longValue());
			}
		}
		if (JavaBuilder.DEBUG && length != 0)
			System.out.println("structuralBuildNumbers table is inconsistent"); //$NON-NLS-1$
	}

/*
 * String[]	Interned type locators
 */
	out.writeInt(length = this.references.elementSize);
	SimpleLookupTable internedTypeLocators = new SimpleLookupTable(length);
	if (length > 0) {
		keyTable = this.references.keyTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			if (keyTable[i] != null) {
				length--;
				String key = (String) keyTable[i];
				out.writeUTF(key);
				internedTypeLocators.put(key, new Integer(internedTypeLocators.elementSize));
			}
		}
		if (JavaBuilder.DEBUG && length != 0)
			System.out.println("references table is inconsistent"); //$NON-NLS-1$
	}

/*
 * Type locators table
 * String		type name
 * int			interned locator id
 */
	out.writeInt(length = this.typeLocators.elementSize);
	if (length > 0) {
		keyTable = this.typeLocators.keyTable;
		valueTable = this.typeLocators.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			if (keyTable[i] != null) {
				length--;
				out.writeUTF((String) keyTable[i]);
				Integer index = (Integer) internedTypeLocators.get(valueTable[i]);
				out.writeInt(index.intValue());
			}
		}
		if (JavaBuilder.DEBUG && length != 0)
			System.out.println("typeLocators table is inconsistent"); //$NON-NLS-1$
	}

/*
 * char[][]	Interned root names
 * char[][][]	Interned qualified names
 * char[][]	Interned simple names
 */
	SimpleLookupTable internedRootNames = new SimpleLookupTable(3);
	SimpleLookupTable internedQualifiedNames = new SimpleLookupTable(31);
	SimpleLookupTable internedSimpleNames = new SimpleLookupTable(31);
	valueTable = this.references.valueTable;
	for (int i = 0, l = valueTable.length; i < l; i++) {
		if (valueTable[i] != null) {
			ReferenceCollection collection = (ReferenceCollection) valueTable[i];
			char[][] rNames = collection.rootReferences;
			for (int j = 0, m = rNames.length; j < m; j++) {
				char[] rName = rNames[j];
				if (!internedRootNames.containsKey(rName)) // remember the names have been interned
					internedRootNames.put(rName, new Integer(internedRootNames.elementSize));
			}
			char[][][] qNames = collection.qualifiedNameReferences;
			for (int j = 0, m = qNames.length; j < m; j++) {
				char[][] qName = qNames[j];
				if (!internedQualifiedNames.containsKey(qName)) { // remember the names have been interned
					internedQualifiedNames.put(qName, new Integer(internedQualifiedNames.elementSize));
					for (int k = 0, n = qName.length; k < n; k++) {
						char[] sName = qName[k];
						if (!internedSimpleNames.containsKey(sName)) // remember the names have been interned
							internedSimpleNames.put(sName, new Integer(internedSimpleNames.elementSize));
					}
				}
			}
			char[][] sNames = collection.simpleNameReferences;
			for (int j = 0, m = sNames.length; j < m; j++) {
				char[] sName = sNames[j];
				if (!internedSimpleNames.containsKey(sName)) // remember the names have been interned
					internedSimpleNames.put(sName, new Integer(internedSimpleNames.elementSize));
			}
		}
	}
	char[][] internedArray = new char[internedRootNames.elementSize][];
	Object[] rootNames = internedRootNames.keyTable;
	Object[] positions = internedRootNames.valueTable;
	for (int i = positions.length; --i >= 0; ) {
		if (positions[i] != null) {
			int index = ((Integer) positions[i]).intValue();
			internedArray[index] = (char[]) rootNames[i];
		}
	}
	writeNames(internedArray, out);
	// now write the interned simple names
	internedArray = new char[internedSimpleNames.elementSize][];
	Object[] simpleNames = internedSimpleNames.keyTable;
	positions = internedSimpleNames.valueTable;
	for (int i = positions.length; --i >= 0; ) {
		if (positions[i] != null) {
			int index = ((Integer) positions[i]).intValue();
			internedArray[index] = (char[]) simpleNames[i];
		}
	}
	writeNames(internedArray, out);
	// now write the interned qualified names as arrays of interned simple names
	char[][][] internedQArray = new char[internedQualifiedNames.elementSize][][];
	Object[] qualifiedNames = internedQualifiedNames.keyTable;
	positions = internedQualifiedNames.valueTable;
	for (int i = positions.length; --i >= 0; ) {
		if (positions[i] != null) {
			int index = ((Integer) positions[i]).intValue();
			internedQArray[index] = (char[][]) qualifiedNames[i];
		}
	}
	out.writeInt(length = internedQArray.length);
	for (int i = 0; i < length; i++) {
		char[][] qName = internedQArray[i];
		int qLength = qName.length;
		out.writeInt(qLength);
		for (int j = 0; j < qLength; j++) {
			Integer index = (Integer) internedSimpleNames.get(qName[j]);
			out.writeInt(index.intValue());
		}
	}

/*
 * References table
 * int		interned locator id
 * ReferenceCollection
*/
	out.writeInt(length = this.references.elementSize);
	if (length > 0) {
		keyTable = this.references.keyTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			if (keyTable[i] != null) {
				length--;
				Integer index = (Integer) internedTypeLocators.get(keyTable[i]);
				out.writeInt(index.intValue());
				ReferenceCollection collection = (ReferenceCollection) valueTable[i];
				if (collection instanceof AdditionalTypeCollection) {
					out.writeByte(1);
					AdditionalTypeCollection atc = (AdditionalTypeCollection) collection;
					writeNames(atc.definedTypeNames, out);
				} else {
					out.writeByte(2);
				}
				char[][][] qNames = collection.qualifiedNameReferences;
				int qLength = qNames.length;
				out.writeInt(qLength);
				for (int j = 0; j < qLength; j++) {
					index = (Integer) internedQualifiedNames.get(qNames[j]);
					out.writeInt(index.intValue());
				}
				char[][] sNames = collection.simpleNameReferences;
				int sLength = sNames.length;
				out.writeInt(sLength);
				for (int j = 0; j < sLength; j++) {
					index = (Integer) internedSimpleNames.get(sNames[j]);
					out.writeInt(index.intValue());
				}
				char[][] rNames = collection.rootReferences;
				int rLength = rNames.length;
				out.writeInt(rLength);
				for (int j = 0; j < rLength; j++) {
					index = (Integer) internedRootNames.get(rNames[j]);
					out.writeInt(index.intValue());
				}
			}
		}
		if (JavaBuilder.DEBUG && length != 0)
			System.out.println("references table is inconsistent"); //$NON-NLS-1$
	}
}

private void writeName(char[] name, DataOutputStream out) throws IOException {
	int nLength = name.length;
	out.writeInt(nLength);
	for (int j = 0; j < nLength; j++)
		out.writeChar(name[j]);
}

private void writeNames(char[][] names, DataOutputStream out) throws IOException {
	int length = names == null ? 0 : names.length;
	out.writeInt(length);
	for (int i = 0; i < length; i++)
		writeName(names[i], out);
}

private void writeRestriction(AccessRuleSet accessRuleSet, DataOutputStream out) throws IOException {
	if (accessRuleSet == null) {
		out.writeInt(0);
	} else {
		AccessRule[] accessRules = accessRuleSet.getAccessRules();
		int length = accessRules.length;
		out.writeInt(length);
		if (length != 0) {
			for (int i = 0; i < length; i++) {
				AccessRule accessRule = accessRules[i];
				writeName(accessRule.pattern, out);
				out.writeInt(accessRule.problemId);
			}
			out.writeByte(accessRuleSet.classpathEntryType);
			out.writeUTF(accessRuleSet.classpathEntryName);
		}
	}
}

/**
 * Returns a string representation of the receiver.
 */
public String toString() {
	return "State for " + this.javaProjectName //$NON-NLS-1$
		+ " (#" + this.buildNumber //$NON-NLS-1$
			+ " @ " + new Date(this.lastStructuralBuildTime) //$NON-NLS-1$
				+ ")"; //$NON-NLS-1$
}

/* Debug helper
void dump() {
	System.out.println("State for " + javaProjectName + " (" + buildNumber + " @ " + new Date(lastStructuralBuildTime) + ")");
	System.out.println("\tClass path source locations:");
	for (int i = 0, l = sourceLocations.length; i < l; i++)
		System.out.println("\t\t" + sourceLocations[i]);
	System.out.println("\tClass path binary locations:");
	for (int i = 0, l = binaryLocations.length; i < l; i++)
		System.out.println("\t\t" + binaryLocations[i]);

	System.out.print("\tStructural build numbers table:");
	if (structuralBuildTimes.elementSize == 0) {
		System.out.print(" <empty>");
	} else {
		Object[] keyTable = structuralBuildTimes.keyTable;
		Object[] valueTable = structuralBuildTimes.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++)
			if (keyTable[i] != null)
				System.out.print("\n\t\t" + keyTable[i].toString() + " -> " + valueTable[i].toString());
	}

	System.out.print("\tType locators table:");
	if (typeLocators.elementSize == 0) {
		System.out.print(" <empty>");
	} else {
		Object[] keyTable = typeLocators.keyTable;
		Object[] valueTable = typeLocators.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++)
			if (keyTable[i] != null)
				System.out.print("\n\t\t" + keyTable[i].toString() + " -> " + valueTable[i].toString());
	}

	System.out.print("\n\tReferences table:");
	if (references.elementSize == 0) {
		System.out.print(" <empty>");
	} else {
		Object[] keyTable = references.keyTable;
		Object[] valueTable = references.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			if (keyTable[i] != null) {
				System.out.print("\n\t\t" + keyTable[i].toString());
				ReferenceCollection c = (ReferenceCollection) valueTable[i];
				char[][][] qRefs = c.qualifiedNameReferences;
				System.out.print("\n\t\t\tqualified:");
				if (qRefs.length == 0)
					System.out.print(" <empty>");
				else for (int j = 0, m = qRefs.length; j < m; j++)
						System.out.print("  '" + CharOperation.toString(qRefs[j]) + "'");
				char[][] sRefs = c.simpleNameReferences;
				System.out.print("\n\t\t\tsimple:");
				if (sRefs.length == 0)
					System.out.print(" <empty>");
				else for (int j = 0, m = sRefs.length; j < m; j++)
						System.out.print("  " + new String(sRefs[j]));
				if (c instanceof AdditionalTypeCollection) {
					char[][] names = ((AdditionalTypeCollection) c).definedTypeNames;
					System.out.print("\n\t\t\tadditional type names:");
					for (int j = 0, m = names.length; j < m; j++)
						System.out.print("  " + new String(names[j]));
				}
			}
		}
	}
	System.out.print("\n\n");
}
*/
}
