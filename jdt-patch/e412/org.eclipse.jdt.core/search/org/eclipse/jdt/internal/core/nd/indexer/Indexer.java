/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.indexer;

import static org.eclipse.jdt.internal.compiler.util.Util.UTF_8;
import static org.eclipse.jdt.internal.compiler.util.Util.getInputStreamAsCharArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobGroup;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IDependent;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElementDelta;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.java.FileFingerprint;
import org.eclipse.jdt.internal.core.nd.java.FileFingerprint.FingerprintTestResult;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeId;
import org.eclipse.jdt.internal.core.nd.java.NdWorkspaceLocation;
import org.eclipse.jdt.internal.core.nd.java.TypeRef;
import org.eclipse.jdt.internal.core.nd.java.model.BinaryTypeDescriptor;
import org.eclipse.jdt.internal.core.nd.java.model.BinaryTypeFactory;
import org.eclipse.jdt.internal.core.nd.java.model.IndexBinaryType;
import org.eclipse.jdt.internal.core.search.processing.IJob;

public final class Indexer {
	private Nd nd;
	private IWorkspaceRoot root;

	private static Indexer indexer;
	public static boolean DEBUG;
	public static boolean DEBUG_ALLOCATIONS;
	public static boolean DEBUG_TIMING;
	public static boolean DEBUG_SCHEDULING;
	public static boolean DEBUG_INSERTIONS;
	public static boolean DEBUG_SELFTEST;
	public static int DEBUG_LOG_SIZE_MB;
	
	// New index is disabled, see bug 544898
//	private static final String ENABLE_NEW_JAVA_INDEX = "enableNewJavaIndex"; //$NON-NLS-1$
//	private static IPreferenceChangeListener listener = new IPreferenceChangeListener() {
//		@Override
//		public void preferenceChange(PreferenceChangeEvent event) {
//			if (ENABLE_NEW_JAVA_INDEX.equals(event.getKey())) {
//				if (JavaIndex.isEnabled()) {
//					getInstance().rescanAll();
//				} else {
//					ChunkCache.getSharedInstance().clear();
//				}
//			}
//		}
//	};

	// This is an arbitrary constant that is larger than the maximum number of ticks
	// reported by SubMonitor and small enough that it won't overflow a long when multiplied by a large
	// database size.
	private final static int TOTAL_TICKS_TO_REPORT_DURING_INDEXING = 1000;

	/**
	 * True iff automatic reindexing (that is, the {@link #rescanAll()} method) is disabled. Synchronize on
	 * {@link #automaticIndexingMutex} while accessing.
	 */
	private boolean enableAutomaticIndexing = true;
	/**
	 * True iff any code tried to schedule reindexing while automatic reindexing was disabled. Synchronize on
	 * {@link #automaticIndexingMutex} while accessing.
	 */
	private boolean indexerDirtiedWhileDisabled = false;
	private final Object automaticIndexingMutex = new Object();

	private final FileStateCache fileStateCache;
	private static final Object mutex = new Object();

	private Object listenersMutex = new Object();
	/**
	 * Listener list. Copy-on-write. Synchronize on "listenersMutex" before accessing.
	 */
	private Set<Listener> listeners = Collections.newSetFromMap(new WeakHashMap<Listener, Boolean>());

	private JobGroup group = new JobGroup(Messages.Indexer_updating_index_job_name, 1, 1);

	private Job rescanJob = Job.create(Messages.Indexer_updating_index_job_name, monitor -> {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		try {
			rescan(subMonitor);
		} catch (IndexException e) {
			Package.log("Database corruption detected during indexing. Deleting and rebuilding the index.", e); //$NON-NLS-1$
			// If we detect corruption during indexing, delete and rebuild the entire index
			rebuildIndex(subMonitor);
		}
	});

	private Job rebuildIndexJob = Job.create(Messages.Indexer_updating_index_job_name, monitor -> {
		rebuildIndex(monitor);
	});

	public static interface Listener {
		void consume(IndexerEvent event);
	}

	public static Indexer getInstance() {
		synchronized (mutex) {
			if (indexer == null) {
				indexer = new Indexer(JavaIndex.getGlobalNd(), ResourcesPlugin.getWorkspace().getRoot());
//				IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
//				preferences.addPreferenceChangeListener(listener);
			}
			return indexer;
		}
	}

	/**
	 * Enables or disables the "rescanAll" method. When set to false, rescanAll does nothing
	 * and indexing will only be triggered when invoking {@link #waitForIndex}.
	 * <p>
	 * Normally the indexer runs automatically and asynchronously when resource changes occur.
	 * However, if this variable is set to false the indexer only runs when someone invokes
	 * {@link #waitForIndex(IProgressMonitor)}. This can be used to eliminate race conditions
	 * when running the unit tests, since indexing will not occur unless it is triggered
	 * explicitly.
	 * <p>
	 * Synchronize on {@link #automaticIndexingMutex} before accessing. 
	 */
	public void enableAutomaticIndexing(boolean enabled) {
		boolean runRescan = false;
		synchronized (this.automaticIndexingMutex) {
			if (this.enableAutomaticIndexing == enabled) {
				return;
			}
			this.enableAutomaticIndexing = enabled;
			if (enabled && this.indexerDirtiedWhileDisabled) {
				runRescan = true;
			}
		}

		if (JavaIndex.isEnabled()) {
			if (runRescan) {
				// Force a rescan when re-enabling automatic indexing since we may have missed an update
				this.rescanJob.schedule();
			}
	
			if (!enabled) {
				// Wait for any existing indexing operations to finish when disabling automatic indexing since
				// we only want explicitly-triggered indexing operations to run after the method returns
				try {
					this.rescanJob.join(0, null);
				} catch (OperationCanceledException | InterruptedException e) {
					// Don't care
				}
			}
		}
	}

	/**
	 * Amount of time (milliseconds) unreferenced files are allowed to sit in the index before they are discarded.
	 * Making this too short will cause some operations (classpath modifications, closing/reopening projects, etc.)
	 * to become more expensive. Making this too long will waste space in the database.
	 * <p>
	 * The value of this is stored in the JDT core preference called "garbageCleanupTimeoutMs". The default value
	 * is 3 days.
	 */
	private static long getGarbageCleanupTimeout() {
		return Platform.getPreferencesService().getLong(JavaCore.PLUGIN_ID, "garbageCleanupTimeoutMs", //$NON-NLS-1$
				1000 * 60 * 60 * 24 * 3,
				null);
	}

	/**
	 * Amount of time (milliseconds) before we update the "used" timestamp on a file in the index. We don't update
	 * the timestamps every update since doing so would be unnecessarily inefficient... but if any of the timestamps
	 * is older than this update period, we refresh it.
	 */
	private static long getUsageTimestampUpdatePeriod() {
		return getGarbageCleanupTimeout() / 4;
	}

	public void rescan(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		Database db = this.nd.getDB();
		db.resetCacheCounters();
		db.getLog().setBufferSize(DEBUG_LOG_SIZE_MB);

		synchronized (this.automaticIndexingMutex) {
			this.indexerDirtiedWhileDisabled = false;
		}

		long currentTimeMs = System.currentTimeMillis();
		if (DEBUG) {
			Package.logInfo("Indexer running rescan"); //$NON-NLS-1$
		}

		this.fileStateCache.clear();
		WorkspaceSnapshot snapshot = WorkspaceSnapshot.create(this.root, subMonitor.split(1));
		Set<IPath> locations = snapshot.allLocations();

		long startGarbageCollectionMs = System.currentTimeMillis();

		// Remove all files in the index which aren't referenced in the workspace
		int gcFiles = cleanGarbage(currentTimeMs, locations, subMonitor.split(1));

		long startFingerprintTestMs = System.currentTimeMillis();

		Map<IPath, FingerprintTestResult> fingerprints = testFingerprints(locations, subMonitor.split(1));
		Set<IPath> indexablesWithChanges = new HashSet<>(
				getIndexablesThatHaveChanged(locations, fingerprints));

		// Compute the total number of bytes to be read in and indexed
		long startIndexingMs = System.currentTimeMillis();
		long totalSizeToIndex = 0;
		for (IPath next : indexablesWithChanges) {
			FingerprintTestResult nextFingerprint = fingerprints.get(next);
			totalSizeToIndex += nextFingerprint.getNewFingerprint().getSize();
		}
		double tickCoefficient = totalSizeToIndex == 0 ? 0.0
				: (double) TOTAL_TICKS_TO_REPORT_DURING_INDEXING / (double) totalSizeToIndex;

		int classesIndexed = 0;
		SubMonitor loopMonitor = subMonitor.split(94).setWorkRemaining(TOTAL_TICKS_TO_REPORT_DURING_INDEXING);
		for (IPath next : indexablesWithChanges) {
			FingerprintTestResult nextFingerprint = fingerprints.get(next);
			int ticks = (int) (nextFingerprint.getNewFingerprint().getSize() * tickCoefficient);

			classesIndexed += rescanArchive(currentTimeMs, next, snapshot.get(next),
					fingerprints.get(next).getNewFingerprint(), loopMonitor.split(ticks));
		}

		long endIndexingMs = System.currentTimeMillis();

		Map<IPath, List<IJavaElement>> pathsToUpdate = new HashMap<>();

		for (IPath next : locations) {
			if (!indexablesWithChanges.contains(next)) {
				pathsToUpdate.put(next, snapshot.get(next));
				continue;
			}
		}

		updateResourceMappings(pathsToUpdate, subMonitor.split(1));

		// Flush the database to disk
		this.nd.acquireWriteLock(subMonitor.split(1));
		try {
			this.nd.getDB().flush();
		} finally {
			this.nd.releaseWriteLock();
		}

		fireDelta(indexablesWithChanges, subMonitor.split(1));

		if (DEBUG) {
			Package.logInfo("Rescan finished"); //$NON-NLS-1$
		}

		long endResourceMappingMs = System.currentTimeMillis();

		long locateIndexablesTimeMs = startGarbageCollectionMs - currentTimeMs;
		long garbageCollectionMs = startFingerprintTestMs - startGarbageCollectionMs;
		long fingerprintTimeMs = startIndexingMs - startFingerprintTestMs;
		long indexingTimeMs = endIndexingMs - startIndexingMs;
		long resourceMappingTimeMs = endResourceMappingMs - endIndexingMs;

		double averageGcTimeMs = gcFiles == 0 ? 0 : (double) garbageCollectionMs / (double) gcFiles;
		double averageIndexTimeMs = classesIndexed == 0 ? 0 : (double) indexingTimeMs / (double) classesIndexed;
		double averageFingerprintTimeMs = locations.size() == 0 ? 0
				: (double) fingerprintTimeMs / (double) locations.size();
		double averageResourceMappingMs = pathsToUpdate.size() == 0 ? 0
				: (double) resourceMappingTimeMs / (double) pathsToUpdate.size();

		if (DEBUG_TIMING) {
			DecimalFormat msFormat = new DecimalFormat("#0.###"); //$NON-NLS-1$
			DecimalFormat percentFormat = new DecimalFormat("#0.###"); //$NON-NLS-1$
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS\n"); //$NON-NLS-1$
			System.out.println("Indexing done at " + format.format(new Date(endResourceMappingMs)) //$NON-NLS-1$
					+ "  Located " + locations.size() + " indexables in " + locateIndexablesTimeMs + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (gcFiles != 0) {
				System.out.println("  Collected garbage from " + gcFiles + " files in " + garbageCollectionMs //$NON-NLS-1$//$NON-NLS-2$
						+ "ms, average time = " + msFormat.format(averageGcTimeMs) + "ms"); //$NON-NLS-1$//$NON-NLS-2$
			}
			System.out.println("  Tested " + locations.size() + " fingerprints in " + fingerprintTimeMs //$NON-NLS-1$ //$NON-NLS-2$
					+ "ms, average time = " + msFormat.format(averageFingerprintTimeMs) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			if (classesIndexed != 0) {
				System.out.println("  Indexed " + classesIndexed + " classes (from " + indexablesWithChanges.size() //$NON-NLS-1$//$NON-NLS-2$
						+ " files containing " + Database.formatByteString(totalSizeToIndex) + ") in " + indexingTimeMs //$NON-NLS-1$ //$NON-NLS-2$
						+ "ms, average time per class = " + msFormat.format(averageIndexTimeMs) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (pathsToUpdate.size() != 0) {
				System.out.println("  Updated " + pathsToUpdate.size() + " paths in " + resourceMappingTimeMs //$NON-NLS-1$//$NON-NLS-2$
						+ "ms, average time = " + msFormat.format(averageResourceMappingMs) + "ms"); //$NON-NLS-1$//$NON-NLS-2$
			}
			System.out.println("  " + db.getChunkStats()); //$NON-NLS-1$
			long cacheHits = db.getCacheHits();
			long cacheMisses = db.getCacheMisses();
			long totalReads = cacheMisses + cacheHits;
			double cacheMissPercent = totalReads == 0 ? 0 : (cacheMisses * 100.0) / totalReads;
			System.out.println("  Cache misses = " + cacheMisses + " (" //$NON-NLS-1$//$NON-NLS-2$
					+ percentFormat.format(cacheMissPercent) + "%)"); //$NON-NLS-1$

			long bytesRead = db.getBytesRead();
			long bytesWritten = db.getBytesWritten();
			double totalTimeMs = endResourceMappingMs - currentTimeMs;
			long flushTimeMs = db.getCumulativeFlushTimeMs();
			double flushPercent = totalTimeMs == 0 ? 0 : flushTimeMs * 100.0 / totalTimeMs;
			System.out.println("  Reads = " + Database.formatByteString(bytesRead) + ", writes = " + Database.formatByteString(bytesWritten)); //$NON-NLS-1$//$NON-NLS-2$
			double averageReadBytesPerSecond = db.getAverageReadBytesPerMs() * 1000;
			double averageWriteBytesPerSecond = db.getAverageWriteBytesPerMs() * 1000;
			if (bytesRead > Database.CHUNK_SIZE * 100) {
				System.out.println(
						"  Read speed = " + Database.formatByteString((long) averageReadBytesPerSecond) + "/s"); //$NON-NLS-1$//$NON-NLS-2$
			}
			if (bytesWritten > Database.CHUNK_SIZE * 100) {
				System.out.println(
						"  Write speed = " + Database.formatByteString((long) averageWriteBytesPerSecond) + "/s"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			System.out.println("  Time spent performing flushes = " //$NON-NLS-1$
					+ msFormat.format(flushTimeMs) + "ms (" //$NON-NLS-1$
					+ percentFormat.format(flushPercent) + "%)"); //$NON-NLS-1$
			System.out.println("  Total indexing time = " + msFormat.format(totalTimeMs) + "ms"); //$NON-NLS-1$//$NON-NLS-2$
		}

		if (DEBUG_ALLOCATIONS) {
			try (IReader readLock = this.nd.acquireReadLock()) {
				this.nd.getDB().reportFreeBlocks();
				this.nd.getDB().getMemoryStats().printMemoryStats(this.nd.getTypeRegistry());
			}
		}
	}

	private void fireDelta(Set<IPath> indexablesWithChanges, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
		IProject[] projects = this.root.getProjects();

		List<IProject> projectsToScan = new ArrayList<>();

		for (IProject next : projects) {
			if (next.isOpen()) {
				projectsToScan.add(next);
			}
		}
		JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		boolean hasChanges = false;
		JavaElementDelta delta = new JavaElementDelta(model);
		SubMonitor projectLoopMonitor = subMonitor.split(1).setWorkRemaining(projectsToScan.size());
		for (IProject project : projectsToScan) {
			projectLoopMonitor.split(1);
			try {
				if (project.isOpen() && project.isNatureEnabled(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = JavaCore.create(project);

					IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();

					for (IPackageFragmentRoot next : roots) {
						if (next.isArchive()) {
							IPath location = JavaIndex.getLocationForElement(next);

							if (indexablesWithChanges.contains(location)) {
								hasChanges = true;
								delta.changed(next,
										IJavaElementDelta.F_CONTENT | IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED);
							}
						}
					}
				}
			} catch (CoreException e) {
				Package.log(e);
			}
		}

		if (hasChanges) {
			fireChange(IndexerEvent.createChange(delta));
		}
	}

	private void updateResourceMappings(Map<IPath, List<IJavaElement>> pathsToUpdate, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, pathsToUpdate.keySet().size());

		JavaIndex index = JavaIndex.getIndex(this.nd);

		for (Entry<IPath, List<IJavaElement>> entry : pathsToUpdate.entrySet()) {
			SubMonitor iterationMonitor = subMonitor.split(1).setWorkRemaining(10);

			this.nd.acquireWriteLock(iterationMonitor.split(1));
			try {
				NdResourceFile resourceFile = index.getResourceFile(entry.getKey().toString().toCharArray());
				if (resourceFile == null) {
					continue;
				}

				attachWorkspaceFilesToResource(entry.getValue(), resourceFile);
			} finally {
				this.nd.releaseWriteLock();
			}

		}
	}

	/**
	 * Clean up unneeded files here, but only do so if it's been a long time since the file was last referenced. Being
	 * too eager about removing old files means that operations which temporarily cause a file to become unreferenced
	 * will run really slowly. also eagerly clean up any partially-indexed files we discover during the scan. That is,
	 * if we discover a file with a timestamp of 0, it indicates that the indexer or all of Eclipse crashed midway
	 * through indexing the file. Such garbage should be cleaned up as soon as possible, since it will never be useful.
	 *
	 * @param currentTimeMillis timestamp of the time at which the indexing operation started
	 * @param allIndexables list of all referenced java roots
	 * @param monitor progress monitor
	 * @return the number of indexables in the index, prior to garbage collection
	 */
	private int cleanGarbage(long currentTimeMillis, Collection<IPath> allIndexables, IProgressMonitor monitor) {
		JavaIndex index = JavaIndex.getIndex(this.nd);

		int result = 0; 
		HashSet<IPath> paths = new HashSet<>();
		paths.addAll(allIndexables);
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);

		List<NdResourceFile> garbage = new ArrayList<>();
		List<NdResourceFile> needsUpdate = new ArrayList<>();

		long usageTimestampUpdatePeriod = getUsageTimestampUpdatePeriod();
		long garbageCleanupTimeout = getGarbageCleanupTimeout();
		// Build up the list of NdResourceFiles that either need to be garbage collected or
		// have their read timestamps updated.
		try (IReader reader = this.nd.acquireReadLock()) {
			List<NdResourceFile> resourceFiles = index.getAllResourceFiles();

			result = resourceFiles.size();
			SubMonitor testMonitor = subMonitor.split(1).setWorkRemaining(resourceFiles.size());
			for (NdResourceFile next : resourceFiles) {
				testMonitor.split(1);
				if (!next.isDoneIndexing()) {
					garbage.add(next);
				} else {
					IPath nextPath = new Path(next.getLocation().toString());
					long timeLastUsed = next.getTimeLastUsed();
					long timeSinceLastUsed = currentTimeMillis - timeLastUsed;

					if (paths.contains(nextPath)) {
						if (timeSinceLastUsed > usageTimestampUpdatePeriod) {
							needsUpdate.add(next);
						}
					} else {
						if (timeSinceLastUsed > garbageCleanupTimeout) {
							garbage.add(next);
						}
					}
				}
			}
		}

		SubMonitor deleteMonitor = subMonitor.split(1).setWorkRemaining(garbage.size());
		for (NdResourceFile next : garbage) {
			deleteResource(next, deleteMonitor.split(1));
		}

		SubMonitor updateMonitor = subMonitor.split(1).setWorkRemaining(needsUpdate.size());
		for (NdResourceFile next : needsUpdate) {
			this.nd.acquireWriteLock(updateMonitor.split(1));
			try {
				if (next.isInIndex()) {
					next.setTimeLastUsed(currentTimeMillis);
				}
			} finally {
				this.nd.releaseWriteLock();
			}
		}

		return result;
	}

	/**
	 * Performs a non-atomic delete of the given resource file. First, it marks the file as being invalid
	 * (by clearing out its timestamp). Then it deletes the children of the resource file, one child at a time.
	 * Once all the children are deleted, the resource itself is deleted. The result on the database is exactly
	 * the same as if the caller had called toDelete.delete(), but doing it this way ensures that a write lock
	 * will never be held for a nontrivial amount of time.
	 */
	protected void deleteResource(NdResourceFile toDelete, IProgressMonitor monitor) {
		SubMonitor deletionMonitor = SubMonitor.convert(monitor, 10);

		this.nd.acquireWriteLock(deletionMonitor.split(1));
		try {
			if (toDelete.isInIndex()) {
				toDelete.markAsInvalid();
			}
		} finally {
			this.nd.releaseWriteLock();
		}

		for (;;) {
			this.nd.acquireWriteLock(deletionMonitor.split(1));
			try {
				if (!toDelete.isInIndex()) {
					break;
				}
		
				int numChildren = toDelete.getTypeCount();
				deletionMonitor.setWorkRemaining(numChildren + 1);
				if (numChildren == 0) {
					break;
				}

				NdType nextDeletion = toDelete.getType(numChildren - 1);
				if (DEBUG_INSERTIONS) {
					Package.logInfo("Deleting " + nextDeletion.getTypeId().getFieldDescriptor().getString() + " from "  //$NON-NLS-1$//$NON-NLS-2$
							+ toDelete.getLocation().getString() + " " + toDelete.address); //$NON-NLS-1$
				}
				nextDeletion.delete();
			} finally {
				this.nd.releaseWriteLock();
			}
		}

		this.nd.acquireWriteLock(deletionMonitor.split(1));
		try {
			if (toDelete.isInIndex()) {
				toDelete.delete();
			}
		} finally {
			this.nd.releaseWriteLock();
		}
	}

	private Map<IPath, FingerprintTestResult> testFingerprints(Collection<IPath> allIndexables,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, allIndexables.size());
		Map<IPath, FingerprintTestResult> result = new HashMap<>();

		for (IPath next : allIndexables) {
			result.put(next, testForChanges(next, subMonitor.split(1)));
		}

		return result;
	}

	/**
	 * Rescans an archive (a jar, zip, or class file on the filesystem). Returns the number of classes indexed.
	 * @throws JavaModelException
	 */
	private int rescanArchive(long currentTimeMillis, IPath thePath, List<IJavaElement> elementsMappingOntoLocation,
			FileFingerprint fingerprint, IProgressMonitor monitor) throws JavaModelException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		if (elementsMappingOntoLocation.isEmpty()) {
			return 0;
		}

		IJavaElement element = elementsMappingOntoLocation.get(0);

		String pathString = thePath.toString();
		JavaIndex javaIndex = JavaIndex.getIndex(this.nd);

		NdResourceFile resourceFile;

		this.nd.acquireWriteLock(subMonitor.split(5));
		try {
			resourceFile = new NdResourceFile(this.nd);
			resourceFile.setTimeLastUsed(currentTimeMillis);
			resourceFile.setLocation(pathString);
			IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) element
					.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			IPath rootPathString = JavaIndex.getLocationForElement(packageFragmentRoot);
			if (!rootPathString.equals(thePath)) {
				resourceFile.setPackageFragmentRoot(rootPathString.toString().toCharArray());
			}
			attachWorkspaceFilesToResource(elementsMappingOntoLocation, resourceFile);
		} finally {
			this.nd.releaseWriteLock();
		}

		if (DEBUG) {
			Package.logInfo("rescanning " + thePath.toString() + ", " + fingerprint); //$NON-NLS-1$ //$NON-NLS-2$
		}
		int result = 0;
		try {
			if (fingerprint.fileExists()) {
				result = addElement(resourceFile, element, subMonitor.split(50));
			}
		} catch (JavaModelException e) {
			if (DEBUG) {
				Package.log("the file " + pathString + " cannot be indexed due to a recoverable error", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// If this file can't be indexed due to a recoverable error, delete the NdResourceFile entry for it.
			this.nd.acquireWriteLock(subMonitor.split(5));
			try {
				if (resourceFile.isInIndex()) {
					resourceFile.delete();
				}
			} finally {
				this.nd.releaseWriteLock();
			}
			return 0;
		} catch (RuntimeException e) {
			if (DEBUG) {
				Package.log("A RuntimeException occurred while indexing " + pathString, e); //$NON-NLS-1$
			}
			throw e;
		} catch (FileNotFoundException e) {
			fingerprint = FileFingerprint.getEmpty();
		}

		if (DEBUG && !fingerprint.fileExists()) {
			Package.log("the file " + pathString + " was not indexed because it does not exist", null); //$NON-NLS-1$ //$NON-NLS-2$
		}

		List<NdResourceFile> allResourcesWithThisPath = Collections.emptyList();
		// Now update the timestamp and delete all older versions of this resource that exist in the index
		this.nd.acquireWriteLock(subMonitor.split(1));
		try {
			if (resourceFile.isInIndex()) {
				resourceFile.setFingerprint(fingerprint);
				allResourcesWithThisPath = javaIndex.findResourcesWithPath(pathString);
				// Remove this file from the file state cache, since the act of indexing it may have changed its
				// up-to-date status. Note that it isn't necessarily up-to-date now -- it may have changed again
				// while we were indexing it.
				this.fileStateCache.remove(resourceFile.getLocation().getString());
			}
		} finally {
			this.nd.releaseWriteLock();
		}

		SubMonitor deletionMonitor = subMonitor.split(40).setWorkRemaining(allResourcesWithThisPath.size() - 1);
		for (NdResourceFile next : allResourcesWithThisPath) {
			if (!next.equals(resourceFile)) {
				deleteResource(next, deletionMonitor.split(1));
			}
		}

		return result;
	}

	private void attachWorkspaceFilesToResource(List<IJavaElement> elementsMappingOntoLocation,
			NdResourceFile resourceFile) {
		for (IJavaElement next : elementsMappingOntoLocation) {
			IResource nextResource = next.getResource();
			if (nextResource != null) {
				new NdWorkspaceLocation(this.nd, resourceFile,
						nextResource.getFullPath().toString().toCharArray());
			}
		}
	}

	/**
	 * Adds an archive to the index, under the given NdResourceFile.
	 * @throws FileNotFoundException if the file does not exist
	 */
	private int addElement(NdResourceFile resourceFile, IJavaElement element, IProgressMonitor monitor)
			throws JavaModelException, FileNotFoundException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);

		if (element instanceof JarPackageFragmentRoot) {
			JarPackageFragmentRoot jarRoot = (JarPackageFragmentRoot) element;

			IPath workspacePath = jarRoot.getPath();
			IPath location = JavaIndex.getLocationForElement(jarRoot);

			int classesIndexed = 0;
			try (ZipFile zipFile = new ZipFile(JavaModelManager.getLocalFile(jarRoot.getPath()))) {
				// Used for the error-handling unit tests
				if (JavaModelManager.throwIoExceptionsInGetZipFile) {
					if (DEBUG) {
						Package.logInfo("Throwing simulated IOException for error handling test case"); //$NON-NLS-1$
					}
					throw new IOException();
				}
				subMonitor.setWorkRemaining(zipFile.size());

				// Preallocate memory for the zipfile entries
				this.nd.acquireWriteLock(subMonitor.split(5));
				try {
					resourceFile.allocateZipEntries(zipFile.size());
				} finally {
					this.nd.releaseWriteLock();
				}
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
					SubMonitor nextEntry = subMonitor.split(1).setWorkRemaining(2);
					ZipEntry member = e.nextElement();
					String fileName = member.getName();
					boolean classFileName = org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(fileName);
					if (member.isDirectory() || !classFileName) {
						this.nd.acquireWriteLock(subMonitor.split(5));
						try {
							if (resourceFile.isInIndex()) {
								if (DEBUG_INSERTIONS) {
									Package.logInfo("Inserting non-class file " + fileName + " into " //$NON-NLS-1$//$NON-NLS-2$
											+ resourceFile.getLocation().getString() + " " + resourceFile.address); //$NON-NLS-1$
								}
								resourceFile.addZipEntry(fileName);

								if (fileName.equals(TypeConstants.META_INF_MANIFEST_MF)) {
									try (InputStream inputStream = zipFile.getInputStream(member)) {
										char[] chars = getInputStreamAsCharArray(inputStream, -1, UTF_8);

										resourceFile.setManifestContent(chars);
									}
								}
							}
						} finally {
							this.nd.releaseWriteLock();
						}
					}
					if (member.isDirectory()) {
						// Note that non-empty directories are stored implicitly (as the parent directory of a file
						// or class within the jar). Empty directories are not currently stored in the index.
						continue;
					}
					nextEntry.split(1);

					if (classFileName) {
						String binaryName = fileName.substring(0,
								fileName.length() - SuffixConstants.SUFFIX_STRING_class.length());
						char[] fieldDescriptor = JavaNames.binaryNameToFieldDescriptor(binaryName.toCharArray());
						String indexPath = jarRoot.getHandleIdentifier() + IDependent.JAR_FILE_ENTRY_SEPARATOR
								+ binaryName;
						BinaryTypeDescriptor descriptor = new BinaryTypeDescriptor(location.toString().toCharArray(),
								fieldDescriptor, workspacePath.toString().toCharArray(), indexPath.toCharArray());
						try {
							byte[] contents = org.eclipse.jdt.internal.compiler.util.Util.getZipEntryByteContent(member,
									zipFile);
							ClassFileReader classFileReader = new ClassFileReader(contents, descriptor.indexPath, true);
							if (addClassToIndex(resourceFile, descriptor.fieldDescriptor, descriptor.indexPath,
									classFileReader, nextEntry.split(1))) {
								classesIndexed++;
							}
						} catch (CoreException | ClassFormatException exception) {
							Package.log("Unable to index " + descriptor.toString(), exception); //$NON-NLS-1$
						}
					}
				}
			} catch (ZipException e) {
				Package.log("The zip file " + jarRoot.getPath() + " was corrupt", e);  //$NON-NLS-1$//$NON-NLS-2$
				// Indicates a corrupt zip file. Treat this like an empty zip file.
				this.nd.acquireWriteLock(null);
				try {
					if (resourceFile.isInIndex()) {
						resourceFile.setFlags(NdResourceFile.FLG_CORRUPT_ZIP_FILE);
					}
				} finally {
					this.nd.releaseWriteLock();
				}
			} catch (FileNotFoundException e) {
				throw e;
			} catch (IOException ioException) {
				throw new JavaModelException(ioException, IJavaModelStatusConstants.IO_EXCEPTION);
			} catch (CoreException coreException) {
				throw new JavaModelException(coreException);
			}

			if (DEBUG && classesIndexed == 0) {
				Package.logInfo("The path " + element.getPath() + " contained no class files"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return classesIndexed;
		} else if (element instanceof IOrdinaryClassFile) {
			IOrdinaryClassFile classFile = (IOrdinaryClassFile) element;

			SubMonitor iterationMonitor = subMonitor.split(1);
			BinaryTypeDescriptor descriptor = BinaryTypeFactory.createDescriptor(classFile);

			boolean indexed = false;
			try {
				ClassFileReader classFileReader = BinaryTypeFactory.rawReadTypeTestForExists(descriptor, true, false);
				if (classFileReader != null) {
					indexed = addClassToIndex(resourceFile, descriptor.fieldDescriptor, descriptor.indexPath,
							classFileReader, iterationMonitor);
				}
			} catch (CoreException | ClassFormatException e) {
				Package.log("Unable to index " + classFile.toString(), e); //$NON-NLS-1$
			}

			return indexed ? 1 : 0;
		} else {
			Package.logInfo("Unable to index elements of type " + element); //$NON-NLS-1$
			return 0;
		}
	}

	private boolean addClassToIndex(NdResourceFile resourceFile, char[] fieldDescriptor, char[] indexPath,
			ClassFileReader binaryType, IProgressMonitor monitor) throws ClassFormatException, CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		ClassFileToIndexConverter converter = new ClassFileToIndexConverter(resourceFile);

		boolean indexed = false;
		this.nd.acquireWriteLock(subMonitor.split(5));
		try {
			if (resourceFile.isInIndex()) {
				if (DEBUG_INSERTIONS) {
					Package.logInfo("Inserting " + new String(fieldDescriptor) + " into " //$NON-NLS-1$//$NON-NLS-2$
							+ resourceFile.getLocation().getString() + " " + resourceFile.address); //$NON-NLS-1$
				}
				converter.addType(binaryType, fieldDescriptor, subMonitor.split(45));
				resourceFile.setJdkLevel(binaryType.getVersion());
				indexed = true;
			}
		} finally {
			this.nd.releaseWriteLock();
		}

		if (DEBUG_SELFTEST && indexed) {
			// When this debug flag is on, we test everything written to the index by reading it back immediately after
			// indexing and comparing it with the original class file.
			JavaIndex index = JavaIndex.getIndex(this.nd);
			try (IReader readLock = this.nd.acquireReadLock()) {
				NdTypeId typeId = index.findType(fieldDescriptor);
				NdType targetType = null;
				if (typeId != null) {
					List<NdType> implementations = typeId.getTypes();
					for (NdType nextType : implementations) {
						NdResourceFile nextResourceFile = nextType.getResourceFile();
						if (nextResourceFile.equals(resourceFile)) {
							targetType = nextType;
							break;
						}
					}
				}

				if (targetType != null) {
					IndexBinaryType actualType = new IndexBinaryType(TypeRef.create(targetType), indexPath);
					IndexTester.testType(binaryType, actualType);
				} else {
					Package.logInfo(
							"Could not find class in index immediately after indexing it: " + new String(indexPath)); //$NON-NLS-1$
				}
			} catch (RuntimeException e) {
				Package.log("Error during indexing: " + new String(indexPath), e); //$NON-NLS-1$
			}
		}
		return indexed;
	}

	/**
	 * Given a list of fragment roots, returns the subset of roots that have changed since the last time they were
	 * indexed.
	 */
	private List<IPath> getIndexablesThatHaveChanged(Collection<IPath> indexables,
			Map<IPath, FingerprintTestResult> fingerprints) {
		List<IPath> indexablesWithChanges = new ArrayList<>();
		for (IPath next : indexables) {
			FingerprintTestResult testResult = fingerprints.get(next);

			if (!testResult.matches()) {
				indexablesWithChanges.add(next);
			}
		}
		return indexablesWithChanges;
	}

	private FingerprintTestResult testForChanges(IPath thePath, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		JavaIndex javaIndex = JavaIndex.getIndex(this.nd);
		String pathString = thePath.toString();

		subMonitor.split(50);
		NdResourceFile resourceFile = null;
		FileFingerprint fingerprint = FileFingerprint.getEmpty();
		this.nd.acquireReadLock();
		try {
			resourceFile = javaIndex.getResourceFile(pathString.toCharArray());

			if (resourceFile != null) {
				fingerprint = resourceFile.getFingerprint();
			}
		} finally {
			this.nd.releaseReadLock();
		}

		FingerprintTestResult result = fingerprint.test(thePath, subMonitor.split(40));

		// If this file hasn't changed but its timestamp has, write an updated fingerprint to the database
		if (resourceFile != null && result.matches() && result.needsNewFingerprint()) {
			this.nd.acquireWriteLock(subMonitor.split(10));
			try {
				if (resourceFile.isInIndex()) {
					if (DEBUG) {
						Package.logInfo(
								"Writing updated fingerprint for " + thePath + ": " + result.getNewFingerprint()); //$NON-NLS-1$//$NON-NLS-2$
					}
					resourceFile.setFingerprint(result.getNewFingerprint());
				}
			} finally {
				this.nd.releaseWriteLock();
			}
		}

		return result;
	}

	public Indexer(Nd toPopulate, IWorkspaceRoot workspaceRoot) {
		this.nd = toPopulate;
		this.root = workspaceRoot;
		this.rescanJob.setSystem(true);
		this.rescanJob.setJobGroup(this.group);
		this.rebuildIndexJob.setSystem(true);
		this.rebuildIndexJob.setJobGroup(this.group);
		this.fileStateCache = FileStateCache.getCache(toPopulate);
	}

	public void rescanAll() {
		if (DEBUG_SCHEDULING) {
			Package.logInfo("Scheduling rescanAll now"); //$NON-NLS-1$
		}
		synchronized (this.automaticIndexingMutex) {
			if (!this.enableAutomaticIndexing) {
				if (!this.indexerDirtiedWhileDisabled) {
					this.indexerDirtiedWhileDisabled = true;
				}
				return;
			}
		}
		if (!JavaIndex.isEnabled()) {
			return;
		}
		this.rescanJob.schedule();
	}

	/**
	 * Adds the given listener. It will be notified when Nd changes. No strong references
	 * will be retained to the listener.
	 */
	public void addListener(Listener newListener) {
		synchronized (this.listenersMutex) {
			Set<Listener> oldListeners = this.listeners;
			this.listeners = Collections.newSetFromMap(new WeakHashMap<>());
			this.listeners.addAll(oldListeners);
			this.listeners.add(newListener);
		}
	}

	public void removeListener(Listener oldListener) {
		synchronized (this.listenersMutex) {
			if (!this.listeners.contains(oldListener)) {
				return;
			}
			Set<Listener> oldListeners = this.listeners;
			this.listeners = Collections.newSetFromMap(new WeakHashMap<>());
			this.listeners.addAll(oldListeners);
			this.listeners.remove(oldListener);
		}
	}

	private void fireChange(IndexerEvent event) {
		Set<Listener> localListeners;
		synchronized (this.listenersMutex) {
			localListeners = this.listeners;
		}

		for (Listener next : localListeners) {
			next.consume(event);
		}
	}

	public void waitForIndex(IProgressMonitor monitor) {
		try {
			boolean shouldRescan = false;
			synchronized (this.automaticIndexingMutex) {
				if (!this.enableAutomaticIndexing && this.indexerDirtiedWhileDisabled) {
					shouldRescan = true;
				}
			}
			if (shouldRescan) {
				this.rescanJob.schedule();
			}
			this.rescanJob.join(0, monitor);
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

	public void waitForIndex(int waitingPolicy, IProgressMonitor monitor) {
		if (!JavaIndex.isEnabled()) {
			return;
		}
		switch (waitingPolicy) {
			case IJob.ForceImmediate: {
				break;
			}
			case IJob.CancelIfNotReady: {
				if (this.rescanJob.getState() != Job.NONE) {
					throw new OperationCanceledException();
				}
				break;
			}
			case IJob.WaitUntilReady: {
				waitForIndex(monitor);
				break;
			}
		}
	}

	public void rebuildIndex(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		this.rescanJob.cancel();
		try {
			this.rescanJob.join(0, subMonitor.split(1));
		} catch (InterruptedException e) {
			// Nothing to do.
		}
		this.nd.acquireWriteLock(subMonitor.split(1));
		try {
			this.nd.clear(subMonitor.split(2));
			this.nd.getDB().flush();
		} finally {
			this.nd.releaseWriteLock();
		}
		if (!JavaIndex.isEnabled()) {
			return;
		}
		rescan(subMonitor.split(97));
	}

	public void requestRebuildIndex() {
		this.rebuildIndexJob.schedule();
	}

	/**
	 * Dirties the given filesystem location. This must point to a single file (not a folder) that needs to be
	 * rescanned. The file may have been added, removed, or changed.
	 * 
	 * @param location an absolute filesystem location
	 */
	public void makeDirty(IPath location) {
		this.fileStateCache.remove(location.toString());
		rescanAll();
	}

	/**
	 * Schedules a rescan of the given project.
	 */
	public void makeDirty(IProject project) {
		this.fileStateCache.clear();
		rescanAll();
	}

	/**
	 * Schedules a rescan of the given path (which may be either a workspace path or an absolute path on the local
	 * filesystem). This may point to either a single file or a folder that needs to be rescanned. Any resource that
	 * has this path as a prefix will be rescanned.
	 * 
	 * @param pathToRescan
	 */
	public void makeWorkspacePathDirty(IPath pathToRescan) {
		this.fileStateCache.clear();
		rescanAll();
	}
}
