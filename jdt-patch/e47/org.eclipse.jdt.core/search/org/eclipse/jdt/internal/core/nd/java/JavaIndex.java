/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.NdNodeTypeRegistry;
import org.eclipse.jdt.internal.core.nd.db.ChunkCache;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex.IResultRank;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex.SearchCriteria;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

public class JavaIndex {
	// Version constants
	static final int CURRENT_VERSION = Nd.version(1, 39);
	static final int MAX_SUPPORTED_VERSION = Nd.version(1, 39);
	static final int MIN_SUPPORTED_VERSION = Nd.version(1, 39);

	// Fields for the search header
	public static final FieldSearchIndex<NdResourceFile> FILES;
	public static final FieldSearchIndex<NdTypeId> SIMPLE_INDEX;
	public static final FieldSearchIndex<NdTypeId> TYPES;
	public static final FieldSearchIndex<NdMethodId> METHODS;

	public static final StructDef<JavaIndex> type;

	static {
		type = StructDef.create(JavaIndex.class);
		FILES = FieldSearchIndex.create(type, NdResourceFile.FILENAME);
		SIMPLE_INDEX = FieldSearchIndex.create(type, NdTypeId.SIMPLE_NAME);
		TYPES = FieldSearchIndex.create(type, NdTypeId.FIELD_DESCRIPTOR);
		METHODS = FieldSearchIndex.create(type, NdMethodId.METHOD_NAME);
		type.done();

		// This struct needs to fit within the first database chunk.
		assert type.getFactory().getRecordSize() <= Database.CHUNK_SIZE;
	}

	private final static class BestResourceFile implements FieldSearchIndex.IResultRank {
		public BestResourceFile() {
		}

		@Override
		public long getRank(Nd resourceFileNd, long resourceFileAddress) {
			return NdResourceFile.TIME_LAST_SCANNED.get(resourceFileNd, resourceFileAddress);
		}
	}

	private static final BestResourceFile bestResourceFile = new BestResourceFile();
	private final long address;
	private Nd nd;
	private IResultRank anyResult = new IResultRank() {
		@Override
		public long getRank(Nd dom, long address1) {
			return 1;
		}
	};
	private static Nd globalNd;
	private static final String INDEX_FILENAME = "index.db"; //$NON-NLS-1$
	private final static Object ndMutex = new Object();

	public JavaIndex(Nd dom, long address) {
		this.address = address;
		this.nd = dom;
	}

	/**
	 * Returns the most-recently-scanned resource file with the given name or null if none
	 */
	public NdResourceFile getResourceFile(char[] location) {
		return FILES.findBest(this.nd, this.address, FieldSearchIndex.SearchCriteria.create(location),
				bestResourceFile);
	}

	/**
	 * Returns true iff the given resource file is up-to-date with the filesystem. Returns false
	 * if the argument is out-of-date with the file system or null.
	 * 
	 * @param file the index file to look up or null
	 * @throws CoreException 
	 */
	public boolean isUpToDate(NdResourceFile file) throws CoreException {
		if (file != null && file.isDoneIndexing()) {
			// TODO(sxenos): It would be much more efficient to mark files as being in one
			// of three states: unknown, dirty, or clean. Files would start in the unknown
			// state and move into the dirty state when we see them in a java model change
			// event. They would move into the clean state after passing this sort of
			// fingerprint test... but by caching the state of all tested files (in memory),
			// it would eliminate the vast majority of these (slow) fingerprint tests.
			
			Path locationPath = new Path(file.getLocation().getString());
			if (file.getFingerprint().test(locationPath, null).matches()) {
				return true;
			}
		}
		return false;
	}

	public List<NdResourceFile> findResourcesWithPath(String thePath) {
		return FILES.findAll(this.nd, this.address, FieldSearchIndex.SearchCriteria.create(thePath.toCharArray()));
	}

	public List<NdResourceFile> getAllResourceFiles() {
		return FILES.asList(this.nd, this.address);
	}

	public NdTypeId findType(char[] fieldDescriptor) {
		SearchCriteria searchCriteria = SearchCriteria.create(fieldDescriptor);
		return TYPES.findBest(this.nd, this.address, searchCriteria, this.anyResult);
	}

	public List<NdTypeId> findTypesBySimpleName(char[] query) {
		SearchCriteria searchCriteria = SearchCriteria.create(query).prefix(true);
		return SIMPLE_INDEX.findAll(this.nd, this.address, searchCriteria);
	}

	public List<NdTypeId> findTypesBySimpleName(char[] query, int count) {
		SearchCriteria searchCriteria = SearchCriteria.create(query).prefix(true);
		return SIMPLE_INDEX.findAll(this.nd, this.address, searchCriteria, count);
	}

	public boolean visitFieldDescriptorsStartingWith(char[] fieldDescriptorPrefix, FieldSearchIndex.Visitor<NdTypeId> visitor) {
		SearchCriteria searchCriteria = SearchCriteria.create(fieldDescriptorPrefix).prefix(true);
		return TYPES.visitAll(this.nd, this.address, searchCriteria, visitor);
	}

	/**
	 * Returns a type ID or creates a new one if it does not exist. The caller must
	 * attach a reference to it after calling this method or it may leak.
	 */
	public NdTypeId createTypeId(char[] fieldDescriptor) {
		NdTypeId existingType = findType(fieldDescriptor);

		if (existingType != null) {
			return existingType;
		}

		if (fieldDescriptor.length > 1) {
			if (fieldDescriptor[0] == 'L') {
				if (fieldDescriptor[fieldDescriptor.length - 1] != ';') {
					throw new IllegalStateException(new String(fieldDescriptor) + " is not a valid field descriptor"); //$NON-NLS-1$
				}
			}
		}

		NdTypeId result = new NdTypeId(this.nd, fieldDescriptor);
		if (!CharArrayUtils.equals(result.getFieldDescriptor().getChars(), fieldDescriptor)) {
			throw new IllegalStateException("Field descriptor didn't match"); //$NON-NLS-1$
		}
		return result;
	}

	public Nd getNd() {
		return this.nd;
	}

	public NdMethodId findMethodId(char[] methodId) {
		SearchCriteria searchCriteria = SearchCriteria.create(methodId);

		return METHODS.findBest(this.nd, this.address, searchCriteria, this.anyResult);
	}

	public NdMethodId createMethodId(char[] methodId) {
		NdMethodId existingMethod = findMethodId(methodId);

		if (existingMethod != null) {
			return existingMethod;
		}

		return new NdMethodId(this.nd, methodId);
	}

	/**
	 * Returns the absolute filesystem location of the given element or null if none
	 */
	public static IPath getLocationForElement(IJavaElement next) {
		IResource resource = next.getResource();

		if (resource != null) {
			return resource.getLocation() == null ? new Path("") : resource.getLocation(); //$NON-NLS-1$
		}

		return next.getPath();
	}

	public static boolean isEnabled() {
		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (preferenceService == null) {
			return true;
		}
		return !preferenceService.getBoolean(JavaCore.PLUGIN_ID, "disableNewJavaIndex", false, //$NON-NLS-1$
				null);
	}

	public static Nd createNd(File databaseFile, ChunkCache chunkCache) {
		return new Nd(databaseFile, chunkCache, createTypeRegistry(),
				MIN_SUPPORTED_VERSION, MAX_SUPPORTED_VERSION, CURRENT_VERSION);
	}

	public static Nd getGlobalNd() {
		Nd localNd;
		synchronized (ndMutex) {
			localNd = globalNd;
		}

		if (localNd != null) {
			return localNd;
		}

		localNd = createNd(getDBFile(), ChunkCache.getSharedInstance());

		synchronized (ndMutex) {
			if (globalNd == null) {
				globalNd = localNd;
			}
			return globalNd;
		}
	}

	public static JavaIndex getIndex(Nd nd) {
		return new JavaIndex(nd, Database.DATA_AREA_OFFSET);
	}

	public static JavaIndex getIndex() {
		return getIndex(getGlobalNd());
	}

	public static int getCurrentVersion() {
		return CURRENT_VERSION;
	}

	static File getDBFile() {
		IPath stateLocation = JavaCore.getPlugin().getStateLocation();
		return stateLocation.append(INDEX_FILENAME).toFile();
	}

	static NdNodeTypeRegistry<NdNode> createTypeRegistry() {
		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		registry.register(0x0001, NdAnnotation.type.getFactory());
		registry.register(0x0004, NdAnnotationInConstant.type.getFactory());
		registry.register(0x0008, NdAnnotationInMethod.type.getFactory());
		registry.register(0x000c, NdAnnotationInMethodParameter.type.getFactory());
		registry.register(0x0010, NdAnnotationInType.type.getFactory());
		registry.register(0x0014, NdAnnotationInVariable.type.getFactory());
		registry.register(0x0020, NdAnnotationValuePair.type.getFactory());
		registry.register(0x0028, NdBinding.type.getFactory());
		registry.register(0x0030, NdComplexTypeSignature.type.getFactory());
		registry.register(0x0038, NdConstant.type.getFactory());
		registry.register(0x0040, NdConstantAnnotation.type.getFactory());
		registry.register(0x0050, NdConstantArray.type.getFactory());
		registry.register(0x0060, NdConstantBoolean.type.getFactory());
		registry.register(0x0070, NdConstantByte.type.getFactory());
		registry.register(0x0080, NdConstantChar.type.getFactory());
		registry.register(0x0090, NdConstantClass.type.getFactory());
		registry.register(0x00A0, NdConstantDouble.type.getFactory());
		registry.register(0x00B0, NdConstantEnum.type.getFactory());
		registry.register(0x00C0, NdConstantFloat.type.getFactory());
		registry.register(0x00D0, NdConstantInt.type.getFactory());
		registry.register(0x00E0, NdConstantLong.type.getFactory());
		registry.register(0x00F0, NdConstantShort.type.getFactory());
		registry.register(0x0100, NdConstantString.type.getFactory());
		registry.register(0x0110, NdMethod.type.getFactory());
		registry.register(0x0120, NdMethodException.type.getFactory());
		registry.register(0x0130, NdMethodId.type.getFactory());
		registry.register(0x0140, NdMethodParameter.type.getFactory());
		registry.register(0x0150, NdResourceFile.type.getFactory());
		registry.register(0x0160, NdTreeNode.type.getFactory());
		registry.register(0x0170, NdType.type.getFactory());
		registry.register(0x0180, NdTypeAnnotation.type.getFactory());
		registry.register(0x0184, NdTypeAnnotationInMethod.type.getFactory());
		registry.register(0x0188, NdTypeAnnotationInType.type.getFactory());
		registry.register(0x018c, NdTypeAnnotationInVariable.type.getFactory());
		registry.register(0x0190, NdTypeArgument.type.getFactory());
		registry.register(0x0194, NdTypeBound.type.getFactory());
		registry.register(0x01A0, NdTypeInterface.type.getFactory());
		registry.register(0x01B0, NdTypeParameter.type.getFactory());
		registry.register(0x01C0, NdTypeSignature.type.getFactory());
		registry.register(0x01D0, NdTypeId.type.getFactory());
		registry.register(0x01E0, NdTypeInterface.type.getFactory());
		registry.register(0x01F0, NdVariable.type.getFactory());
		registry.register(0x0200, NdWorkspaceLocation.type.getFactory());
		return registry;
	}
}
