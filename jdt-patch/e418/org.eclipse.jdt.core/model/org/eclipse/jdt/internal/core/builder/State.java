/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *     Karsten Thoms - Bug 532505
 *     Sebastian Zarnekow - Contribution for
 *								Bug 545491 - Poor performance of ReferenceCollection with many source files
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule.AddExports;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule.AddReads;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdateKind;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;

@SuppressWarnings({"rawtypes", "unchecked"})
public class State {
// NOTE: this state cannot contain types that are not defined in this project

String javaProjectName;
public ClasspathMultiDirectory[] sourceLocations;
public ClasspathMultiDirectory[] testSourceLocations;
public ClasspathLocation[] binaryLocations;
public ClasspathLocation[] testBinaryLocations;
// keyed by the project relative path of the type (i.e. "src1/p1/p2/A.java"), value is a ReferenceCollection or an AdditionalTypeCollection
Map<String, ReferenceCollection> references;
// keyed by qualified type name "p1/p2/A", value is the project relative path which defines this type "src1/p1/p2/A.java"
public Map<String, String> typeLocators;

int buildNumber;
long lastStructuralBuildTime;
SimpleLookupTable structuralBuildTimes;

private String[] knownPackageNames; // of the form "p1/p2"

private long previousStructuralBuildTime;
private StringSet structurallyChangedTypes;
public static int MaxStructurallyChangedTypes = 100; // keep track of ? structurally changed types, otherwise consider all to be changed

public static final byte VERSION = 0x0023;

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
	this.testSourceLocations = javaBuilder.testNameEnvironment.sourceLocations;
	this.testBinaryLocations = javaBuilder.testNameEnvironment.binaryLocations;
	this.references = new LinkedHashMap<>(7);
	this.typeLocators = new LinkedHashMap<>(7);

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

	this.references = new LinkedHashMap<>(lastState.references);
	this.typeLocators = new LinkedHashMap<>(lastState.typeLocators);
}
public char[][] getDefinedTypeNamesFor(String typeLocator) {
	Object c = this.references.get(typeLocator);
	if (c instanceof AdditionalTypeCollection)
		return ((AdditionalTypeCollection) c).definedTypeNames;
	return null; // means only one type is defined with the same name as the file... saves space
}

public Map<String, ReferenceCollection> getReferences() {
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
	String existing = this.typeLocators.get(qualifiedTypeName);
	return existing != null && !existing.equals(typeLocator);
}

public boolean isKnownPackage(String qualifiedPackageName) {
	if (this.knownPackageNames == null) {
		LinkedHashSet<String> names = new LinkedHashSet<>(this.typeLocators.size());
		Set<Entry<String, String>> keyTable = this.typeLocators.entrySet();
		for (Entry<String, String> entry : keyTable) {
			String packageName = entry.getKey(); // is a type name of the form p1/p2/A
			int last = packageName.lastIndexOf('/');
			packageName = last == -1 ? null : packageName.substring(0, last);
			while (packageName != null && !names.contains(packageName)) {
				names.add(packageName);
				last = packageName.lastIndexOf('/');
				packageName = last == -1 ? null : packageName.substring(0, last);
			}
		}
		this.knownPackageNames = new String[names.size()];
		names.toArray(this.knownPackageNames);
		Arrays.sort(this.knownPackageNames);
	}
	int result = Arrays.binarySearch(this.knownPackageNames, qualifiedPackageName);
	return result >= 0;
}

public boolean isKnownType(String qualifiedTypeName) {
	return this.typeLocators.containsKey(qualifiedTypeName);
}

boolean isSourceFolderEmpty(IContainer sourceFolder) {
	String sourceFolderName = sourceFolder.getProjectRelativePath().addTrailingSeparator().toString();
	for (String value : this.typeLocators.values()) {
		if (value.startsWith(sourceFolderName)) {
			return false;
		}
	}
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
			this.structuralBuildTimes.put(prereqProject.getName(), Long.valueOf(prereqState.lastStructuralBuildTime));
}

void removeLocator(String typeLocatorToRemove) {
	this.knownPackageNames = null;
	this.references.remove(typeLocatorToRemove);
	this.typeLocators.values().removeIf(v -> typeLocatorToRemove.equals(v));
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
	this.typeLocators.remove(qualifiedTypeNameToRemove);
}

static State read(IProject project, DataInputStream in) throws IOException, CoreException {
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

	newState.sourceLocations = readSourceLocations(project, in);
	newState.binaryLocations = readBinaryLocations(project, in, newState.sourceLocations);

	newState.testSourceLocations = readSourceLocations(project, in);
	newState.testBinaryLocations = readBinaryLocations(project, in, newState.testSourceLocations);

	int length;
	newState.structuralBuildTimes = new SimpleLookupTable(length = in.readInt());
	for (int i = 0; i < length; i++)
		newState.structuralBuildTimes.put(in.readUTF(), Long.valueOf(in.readLong()));

	String[] internedTypeLocators = new String[length = in.readInt()];
	for (int i = 0; i < length; i++)
		internedTypeLocators[i] = in.readUTF();

	length = in.readInt();
	newState.typeLocators = new LinkedHashMap<>((int) (length / 0.75 + 1));
	for (int i = 0; i < length; i++)
		newState.recordLocatorForType(in.readUTF(), internedTypeLocators[in.readInt()]);

	/*
	 * Here we read global arrays of names for the entire project - do not mess up the ordering while interning
	 */
	char[][] internedRootNames = ReferenceCollection.internSimpleNames(readNames(in), false /* keep well known */, false /* do not sort */);
	char[][] internedSimpleNames = ReferenceCollection.internSimpleNames(readNames(in), false /* keep well known */, false /* do not sort */);
	char[][][] internedQualifiedNames = new char[length = in.readInt()][][];
	for (int i = 0; i < length; i++) {
		int qLength = in.readInt();
		char[][] qName = new char[qLength][];
		for (int j = 0; j < qLength; j++)
			qName[j] = internedSimpleNames[in.readInt()];
		internedQualifiedNames[i] = qName;
	}
	internedQualifiedNames = ReferenceCollection.internQualifiedNames(internedQualifiedNames, false /* drop well known */, false /* do not sort */);

	length = in.readInt();
	newState.references = new LinkedHashMap((int) (length / 0.75 + 1));
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

private static ClasspathMultiDirectory[] readSourceLocations(IProject project, DataInputStream in) throws IOException {
	int length = in.readInt();
	ClasspathMultiDirectory[] sourceLocations = new ClasspathMultiDirectory[length];
	for (int i = 0; i < length; i++) {
		IContainer sourceFolder = project, outputFolder = project;
		String folderName;
		if ((folderName = in.readUTF()).length() > 0) sourceFolder = project.getFolder(folderName);
		if ((folderName = in.readUTF()).length() > 0) outputFolder = project.getFolder(folderName);
		ClasspathMultiDirectory md =
			(ClasspathMultiDirectory) ClasspathLocation.forSourceFolder(sourceFolder, outputFolder, readNames(in), readNames(in), in.readBoolean());
		if (in.readBoolean())
			md.hasIndependentOutputFolder = true;
		sourceLocations[i] = md;
	}
	return sourceLocations;
}

private static ClasspathLocation[] readBinaryLocations(IProject project, DataInputStream in, ClasspathMultiDirectory[] sourceLocations) throws IOException, CoreException {
	int length = in.readInt();
	ClasspathLocation[] locations = new ClasspathLocation[length];
	IWorkspaceRoot root = project.getWorkspace().getRoot();
	for (int i = 0; i < length; i++) {
		switch (in.readByte()) {
			case SOURCE_FOLDER :
				locations[i] = sourceLocations[in.readInt()];
				break;
			case BINARY_FOLDER :
				IPath path = new Path(in.readUTF());
				IContainer outputFolder = path.segmentCount() == 1
					? (IContainer) root.getProject(path.toString())
					: (IContainer) root.getFolder(path);
					locations[i] = ClasspathLocation.forBinaryFolder(outputFolder, in.readBoolean(),
							readRestriction(in), new Path(in.readUTF()), in.readBoolean());
				break;
			case EXTERNAL_JAR :
				String jarPath = in.readUTF();
				if (Util.isJrt(jarPath)) {
					locations[i] = ClasspathLocation.forJrtSystem(jarPath, readRestriction(in), new Path(in.readUTF()), in.readUTF());
				} else {
					locations[i] = ClasspathLocation.forLibrary(jarPath, in.readLong(),
							readRestriction(in), new Path(in.readUTF()), in.readBoolean(), in.readUTF());
				}
				break;
			case INTERNAL_JAR :
					locations[i] = ClasspathLocation.forLibrary(root.getFile(new Path(in.readUTF())),
							readRestriction(in), new Path(in.readUTF()), in.readBoolean(), in.readUTF());
					break;
		}
		ClasspathLocation loc = locations[i];
		char[] patchName = readName(in);
		loc.patchModuleName = patchName.length > 0 ? new String(patchName) : null;
		int limitSize = in.readInt();
		if (limitSize != 0) {
			loc.limitModuleNames = new LinkedHashSet<>(limitSize);
			for (int j = 0; j < limitSize; j++) {
				loc.limitModuleNames.add(in.readUTF());
			}
		} else {
			loc.limitModuleNames = null;
		}
		IUpdatableModule.UpdatesByKind updates = new IUpdatableModule.UpdatesByKind();
		List<Consumer<IUpdatableModule>> packageUpdates = null;
		int packageUpdatesSize = in.readInt();
		if (packageUpdatesSize != 0) {
			packageUpdates = updates.getList(UpdateKind.PACKAGE, true);
			for (int j = 0; j < packageUpdatesSize; j++) {
				char[] pkgName = readName(in);
				char[][] targets = readNames(in);
				packageUpdates.add(new AddExports(pkgName, targets));
			}
		}
		List<Consumer<IUpdatableModule>> moduleUpdates = null;
		int moduleUpdatesSize = in.readInt();
		if (moduleUpdatesSize != 0) {
			moduleUpdates = updates.getList(UpdateKind.MODULE, true);
			char[] modName = readName(in);
			moduleUpdates.add(new AddReads(modName));
		}
		if (packageUpdates != null || moduleUpdates != null)
			loc.updates = updates;
	}
	return locations;
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
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	for (int i = 0; i < length; i++) {
		char[] pattern = readName(in);
		int problemId = in.readInt();
		accessRules[i] = manager.getAccessRuleForProblemId(pattern,problemId);
	}
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
	writeSourceLocations(out, this.sourceLocations);

/*
 * ClasspathLocation[]
 * int			id
 * String		path(s)
 */
	writeBinaryLocations(out, this.binaryLocations, this.sourceLocations);

/*
 * ClasspathMultiDirectory[]
 * int			id
 * String		path(s)
 */
	writeSourceLocations(out, this.testSourceLocations);

/*
 * ClasspathLocation[]
 * int			id
 * String		path(s)
 */
	writeBinaryLocations(out, this.testBinaryLocations, this.testSourceLocations);

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
	out.writeInt(length = this.references.size());
	SimpleLookupTable internedTypeLocators = new SimpleLookupTable(length);
	if (length > 0) {
		Set<String> keys = this.references.keySet();
		for (String key : keys) {
			if (key != null) {
				length--;
				out.writeUTF(key);
				internedTypeLocators.put(key, Integer.valueOf(internedTypeLocators.elementSize));
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
	out.writeInt(length = this.typeLocators.size());
	if (length > 0) {
		Set<Entry<String, String>> entries = this.typeLocators.entrySet();
		for (Entry<String, String> entry : entries) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key != null) {
				length--;
				out.writeUTF(key);
				Integer index = (Integer) internedTypeLocators.get(value);
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
	for (ReferenceCollection collection : this.references.values()) {
		char[][] rNames = collection.rootReferences;
		for (int j = 0, m = rNames.length; j < m; j++) {
			char[] rName = rNames[j];
			if (!internedRootNames.containsKey(rName)) // remember the names have been interned
				internedRootNames.put(rName, Integer.valueOf(internedRootNames.elementSize));
		}
		char[][][] qNames = collection.qualifiedNameReferences;
		for (int j = 0, m = qNames.length; j < m; j++) {
			char[][] qName = qNames[j];
			if (!internedQualifiedNames.containsKey(qName)) { // remember the names have been interned
				internedQualifiedNames.put(qName, Integer.valueOf(internedQualifiedNames.elementSize));
				for (int k = 0, n = qName.length; k < n; k++) {
					char[] sName = qName[k];
					if (!internedSimpleNames.containsKey(sName)) // remember the names have been interned
						internedSimpleNames.put(sName, Integer.valueOf(internedSimpleNames.elementSize));
				}
			}
		}
		char[][] sNames = collection.simpleNameReferences;
		for (int j = 0, m = sNames.length; j < m; j++) {
			char[] sName = sNames[j];
			if (!internedSimpleNames.containsKey(sName)) // remember the names have been interned
				internedSimpleNames.put(sName, Integer.valueOf(internedSimpleNames.elementSize));
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
	out.writeInt(length = this.references.size());
	if (length > 0) {
		for (Entry<String, ReferenceCollection> entry : this.references.entrySet()) {
			String key = entry.getKey();
			length--;
			Integer index = (Integer) internedTypeLocators.get(key);
			out.writeInt(index.intValue());
			ReferenceCollection collection = entry.getValue();
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
		if (JavaBuilder.DEBUG && length != 0)
			System.out.println("references table is inconsistent"); //$NON-NLS-1$
	}
}

private void writeSourceLocations(DataOutputStream out, ClasspathMultiDirectory[] srcLocations) throws IOException {
	int length;
	out.writeInt(length = srcLocations.length);
	for (int i = 0; i < length; i++) {
		ClasspathMultiDirectory md = srcLocations[i];
		out.writeUTF(md.sourceFolder.getProjectRelativePath().toString());
		out.writeUTF(md.binaryFolder.getProjectRelativePath().toString());
		writeNames(md.inclusionPatterns, out);
		writeNames(md.exclusionPatterns, out);
		out.writeBoolean(md.ignoreOptionalProblems);
		out.writeBoolean(md.hasIndependentOutputFolder);
	}
}

private void writeBinaryLocations(DataOutputStream out, ClasspathLocation[] locations, ClasspathMultiDirectory[] srcLocations)
		throws IOException {
	/*
	 * ClasspathLocation[]
	 * int			id
	 * String		path(s)
	 * String       module updates
	*/

	out.writeInt(locations.length);
	for (int i = 0; i < locations.length; i++) {
		ClasspathLocation c = locations[i];
		if (c instanceof ClasspathMultiDirectory) {
			out.writeByte(SOURCE_FOLDER);
			for (int j = 0, m = srcLocations.length; j < m; j++) {
				if (srcLocations[j] == c) {
					out.writeInt(j);
					//continue next;
				}
			}
		} else if (c instanceof ClasspathDirectory) {
			out.writeByte(BINARY_FOLDER);
			ClasspathDirectory cd = (ClasspathDirectory) c;
			out.writeUTF(cd.binaryFolder.getFullPath().toString());
			out.writeBoolean(cd.isOutputFolder);
			writeRestriction(cd.accessRuleSet, out);
			out.writeUTF(cd.externalAnnotationPath != null ? cd.externalAnnotationPath : ""); //$NON-NLS-1$
			out.writeBoolean(cd.isOnModulePath);
		} else if (c instanceof ClasspathJar) {
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
			out.writeUTF(jar.externalAnnotationPath != null ? jar.externalAnnotationPath : ""); //$NON-NLS-1$
			out.writeBoolean(jar.isOnModulePath);
			out.writeUTF(jar.compliance == null ? "" : jar.compliance); //$NON-NLS-1$

		} else if (c instanceof ClasspathJrt) {
			ClasspathJrt jrt = (ClasspathJrt) c;
			out.writeByte(EXTERNAL_JAR);
			out.writeUTF(jrt.zipFilename);
			writeRestriction(jrt.accessRuleSet, out);
			out.writeUTF(jrt.externalAnnotationPath != null ? jrt.externalAnnotationPath : ""); //$NON-NLS-1$
			if (jrt instanceof ClasspathJrtWithReleaseOption)
				out.writeUTF(((ClasspathJrtWithReleaseOption) jrt).release);
			else
				out.writeUTF(""); //$NON-NLS-1$
		}
		char[] patchName = c.patchModuleName == null ? CharOperation.NO_CHAR : c.patchModuleName.toCharArray();
		writeName(patchName, out);
		if (c.limitModuleNames != null) {
			out.writeInt(c.limitModuleNames.size());
			for (String name : c.limitModuleNames) {
				out.writeUTF(name);
			}
		} else {
			out.writeInt(0);
		}
		if (c.updates != null) {
			List<Consumer<IUpdatableModule>> pu = c.updates.getList(UpdateKind.PACKAGE, false);
			if (pu != null) {
				Map<String, List<Consumer<IUpdatableModule>>> map = pu.stream().
						collect(Collectors.groupingBy(
								update -> CharOperation.charToString(((IUpdatableModule.AddExports)update).getName())));
				out.writeInt(map.size());
				map.entrySet().stream().forEach(entry -> {
					String pkgName = entry.getKey();
					try {
						writeName(pkgName.toCharArray(), out);
						char[][] targetModules = entry.getValue().stream()
								.map(consumer -> ((IUpdatableModule.AddExports) consumer).getTargetModules())
								.filter(targets -> targets != null)
								.reduce((f,s) -> CharOperation.arrayConcat(f,s))
								.orElse(null);
						writeNames(targetModules, out);
					} catch (IOException e) {
						// ignore
					}

				});
			} else {
				out.writeInt(0);
			}
			List<Consumer<IUpdatableModule>> mu = c.updates.getList(UpdateKind.MODULE, false);
			if (mu != null) {
				out.writeInt(mu.size());
				for (Consumer<IUpdatableModule> cons : mu) {
					AddReads m = (AddReads) cons;
					writeName(m.getTarget(), out);
				}
			} else {
				out.writeInt(0);
			}
		} else {
			out.writeInt(0);
			out.writeInt(0);
		}
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
@Override
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
