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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany.Visitor;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex.IResultRank;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex.SearchCriteria;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Represents a source of java classes (such as a .jar or .class file).
 */
public class NdResourceFile extends NdTreeNode {
	public static final FieldSearchKey<JavaIndex> FILENAME;
	public static final FieldOneToMany<NdBinding> ALL_NODES;
	public static final FieldLong TIME_LAST_USED;
	public static final FieldLong TIME_LAST_SCANNED;
	public static final FieldLong SIZE_LAST_SCANNED;
	public static final FieldLong HASHCODE_LAST_SCANNED;
	public static final FieldOneToMany<NdWorkspaceLocation> WORKSPACE_MAPPINGS;
	public static final FieldString JAVA_ROOT;

	@SuppressWarnings("hiding")
	public static final StructDef<NdResourceFile> type;

	static {
		type = StructDef.create(NdResourceFile.class, NdTreeNode.type);
		FILENAME = FieldSearchKey.create(type, JavaIndex.FILES);
		ALL_NODES = FieldOneToMany.create(type, NdBinding.FILE, 16);
		TIME_LAST_USED = type.addLong();
		TIME_LAST_SCANNED = type.addLong();
		SIZE_LAST_SCANNED = type.addLong();
		HASHCODE_LAST_SCANNED = type.addLong();
		WORKSPACE_MAPPINGS = FieldOneToMany.create(type, NdWorkspaceLocation.RESOURCE);
		JAVA_ROOT = type.addString();
		type.done();
	}

	public NdResourceFile(Nd dom, long address) {
		super(dom, address);
	}

	public NdResourceFile(Nd nd) {
		super(nd, null);
	}

	public List<NdTreeNode> getChildren() {
		return CHILDREN.asList(this.getNd(), this.address);
	}

	/**
	 * Determines whether this file is still in the index. If a {@link NdResourceFile} instance is retained while the
	 * database lock is released and reobtained, this method should be invoked to ensure that the {@link NdResourceFile}
	 * has not been deleted in the meantime.
	 */
	public boolean isInIndex() {
		try {
			Nd nd = getNd();
			// In the common case where the resource file was deleted and the memory hasn't yet been reused,
			// this will fail.
			if (!nd.isValidAddress(this.address) || NODE_TYPE.get(nd, this.address) != nd.getNodeType(getClass())) {
				return false;
			}

			char[] filename = FILENAME.get(getNd(), this.address).getChars();

			NdResourceFile result = JavaIndex.FILES.findBest(nd, Database.DATA_AREA_OFFSET,
					SearchCriteria.create(filename), new IResultRank() {
						@Override
						public long getRank(Nd testNd, long testAddress) {
							if (testAddress == NdResourceFile.this.address) {
								return 1;
							}
							return -1;
						}
					});

			return (this.equals(result));
		} catch (IndexException e) {
			// Read errors are expected here. It's possible that the resource file has been deleted and something
			// new was written to this address, in which case we may be reading random gibberish from the database.
			// This is likely to cause an exception.
			return false;
		}
	}

	public List<IPath> getAllWorkspaceLocations() {
		final List<IPath> result = new ArrayList<>();

		WORKSPACE_MAPPINGS.accept(getNd(), this.address, new Visitor<NdWorkspaceLocation>() {
			@Override
			public void visit(int index, NdWorkspaceLocation toVisit) {
				result.add(new Path(toVisit.getPath().getString()));
			}
		});

		return result;
	}

	public IPath getFirstWorkspaceLocation() {
		if (WORKSPACE_MAPPINGS.isEmpty(getNd(), this.address)) {
			return Path.EMPTY;
		}

		return new Path(WORKSPACE_MAPPINGS.get(getNd(), this.address, 0).getPath().toString());
	}

	public IPath getAnyOpenWorkspaceLocation(IWorkspaceRoot root) {
		int numMappings = WORKSPACE_MAPPINGS.size(getNd(), this.address);

		for (int mapping = 0; mapping < numMappings; mapping++) {
			NdWorkspaceLocation nextMapping = WORKSPACE_MAPPINGS.get(getNd(), this.address, mapping);

			IPath nextPath = new Path(nextMapping.getPath().getString());
			if (nextPath.isEmpty()) {
				continue;
			}

			IProject project = root.getProject(nextPath.segment(0));
			if (project.isOpen()) {
				return nextPath;
			}
		}

		return Path.EMPTY;
	}

	/**
	 * Returns a workspace path to this resource if possible and the absolute filesystem location if not.
	 */
	public IPath getPath() {
		IPath workspacePath = getFirstWorkspaceLocation();

		if (workspacePath.isEmpty()) {
			return new Path(getLocation().getString());
		}

		return workspacePath;
	}

	public List<NdWorkspaceLocation> getWorkspaceMappings() {
		return WORKSPACE_MAPPINGS.asList(getNd(), this.address);
	}

	public IString getLocation() {
		return FILENAME.get(getNd(), this.address);
	}

	public void setLocation(String filename) {
		FILENAME.put(getNd(), this.address, filename);
	}

	public FileFingerprint getFingerprint() {
		return new FileFingerprint(
				getTimeLastScanned(),
				getSizeLastScanned(),
				getHashcodeLastScanned());
	}

	private long getHashcodeLastScanned() {
		return HASHCODE_LAST_SCANNED.get(getNd(), this.address);
	}

	/**
	 * Returns true iff the indexer has finished writing the contents of this file to the index. Returns false if
	 * indexing may still be going on. If this returns false, readers should ignore all contents of this file.
	 *
	 * @return true iff the contents of this file are usable
	 */
	public boolean isDoneIndexing() {
		return getTimeLastScanned() != 0;
	}

	public long getTimeLastScanned() {
		return TIME_LAST_SCANNED.get(getNd(), this.address);
	}

	public long getSizeLastScanned() {
		return SIZE_LAST_SCANNED.get(getNd(), this.address);
	}

	public long getTimeLastUsed() {
		return TIME_LAST_USED.get(getNd(), this.address);
	}

	public void setTimeLastUsed(long timeLastUsed) {
		TIME_LAST_USED.put(getNd(), this.address, timeLastUsed);
	}

	public void setFingerprint(FileFingerprint newFingerprint) {
		TIME_LAST_SCANNED.put(getNd(), this.address, newFingerprint.getTime());
		HASHCODE_LAST_SCANNED.put(getNd(), this.address, newFingerprint.getHash());
		SIZE_LAST_SCANNED.put(getNd(), this.address, newFingerprint.getSize());
	}

	public void setPackageFragmentRoot(char[] javaRoot) {
		JAVA_ROOT.put(getNd(), this.address, javaRoot);
	}

	/**
	 * Returns the absolute path to the java root for this .jar or .class file. If this is a .jar file, it returns its
	 * own filename.
	 */
	public IString getPackageFragmentRoot() {
		IString javaRoot = JAVA_ROOT.get(getNd(), this.address);
		if (javaRoot.length() == 0) {
			return getLocation();
		}
		return javaRoot;
	}

	public void markAsInvalid() {
		TIME_LAST_SCANNED.put(getNd(), this.address, 0);
	}

	public int getBindingCount() {
		return ALL_NODES.size(getNd(), this.address);
	}

	public List<NdBinding> getBindings() {
		return ALL_NODES.asList(getNd(), this.address);
	}

	public NdBinding getBinding(int index) {
		return ALL_NODES.get(getNd(), this.address, index);
	}

	public String toString() {
		try {
			return FILENAME.get(getNd(), this.address).toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}
}
