/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *     						Bug 320618 - inconsistent initialization of classpath container backed by external class folder
 *     						Bug 346010 - [model] strange initialization dependency in OptionTests
 *******************************************************************************/
package org.eclipse.jdt.internal.core;
// GROOVY PATCHED

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.JavaModelManager.PerProjectInfo;
import org.eclipse.jdt.internal.core.JavaProjectElementInfo.ProjectCache;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.eval.EvaluationContextWrapper;
import org.eclipse.jdt.internal.core.util.JavaElementFinder;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.eval.EvaluationContext;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Handle for a Java Project.
 *
 * <p>A Java Project internally maintains a devpath that corresponds
 * to the project's classpath. The classpath may include source folders
 * from the current project; jars in the current project, other projects,
 * and the local file system; and binary folders (output location) of other
 * projects. The Java Model presents source elements corresponding to output
 * .class files in other projects, and thus uses the devpath rather than
 * the classpath (which is really a compilation path). The devpath mimics
 * the classpath, except has source folder entries in place of output
 * locations in external projects.
 *
 * <p>Each JavaProject has a NameLookup facility that locates elements
 * on by name, based on the devpath.
 *
 * @see IJavaProject
 */
public class JavaProject
	extends Openable
	implements IJavaProject, IProjectNature, SuffixConstants {

	/**
	 * Name of file containing project classpath
	 */
	public static final String CLASSPATH_FILENAME = IJavaProject.CLASSPATH_FILE_NAME;

	/**
	 * Value of the project's raw classpath if the .classpath file contains invalid entries.
	 */
	public static final IClasspathEntry[] INVALID_CLASSPATH = new IClasspathEntry[0];

	/**
	 * Whether the underlying file system is case sensitive.
	 */
	protected static final boolean IS_CASE_SENSITIVE = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES = CharOperation.NO_STRINGS;

	/**
	 * Name of file containing custom project preferences
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=59258">bug 59258</a>
	 */
	private static final String PREF_FILENAME = ".jprefs";  //$NON-NLS-1$

	/**
	 * Name of directory containing preferences file
	 */
	public static final String DEFAULT_PREFERENCES_DIRNAME = ".settings"; //$NON-NLS-1$

	/**
	 * Extension for file containing custom project preferences
	 */
	public static final String JAVA_CORE_PREFS_FILE = JavaCore.PLUGIN_ID+".prefs"; //$NON-NLS-1$

	/*
	 * Value of project's resolved classpath while it is being resolved
	 */
	private static final IClasspathEntry[] RESOLUTION_IN_PROGRESS = new IClasspathEntry[0];

	/*
	 * For testing purpose only
	 */
	private static ArrayList CP_RESOLUTION_BP_LISTENERS;
	public static class ClasspathResolutionBreakpointListener {
		public void breakpoint(int bp) {
			// override in listener implementation
		}
	}

	/**
	 * The platform project this <code>IJavaProject</code> is based on
	 */
	protected IProject project;

	/**
	 * Preferences listeners
	 */
	private IEclipsePreferences.INodeChangeListener preferencesNodeListener;
	private IEclipsePreferences.IPreferenceChangeListener preferencesChangeListener;

	/**
	 * Constructor needed for <code>IProject.getNature()</code> and <code>IProject.addNature()</code>.
	 *
	 * @see #setProject(IProject)
	 */
	public JavaProject() {
		super(null);
	}

	public JavaProject(IProject project, JavaElement parent) {
		super(parent);
		this.project = project;
	}

	/*
	 * For testing purpose only
	 */
	public static synchronized void addCPResolutionBPListener(ClasspathResolutionBreakpointListener listener) {
		if (CP_RESOLUTION_BP_LISTENERS == null)
			CP_RESOLUTION_BP_LISTENERS = new ArrayList();
		CP_RESOLUTION_BP_LISTENERS.add(listener);
	}

	/*
	 * For testing purpose only
	 */
	public static synchronized void removeCPResolutionBPListener(ClasspathResolutionBreakpointListener listener) {
		if (CP_RESOLUTION_BP_LISTENERS == null)
			return;
		CP_RESOLUTION_BP_LISTENERS.remove(listener);
		if (CP_RESOLUTION_BP_LISTENERS.size() == 0)
			CP_RESOLUTION_BP_LISTENERS = null;
	}

	private static synchronized ClasspathResolutionBreakpointListener[] getBPListeners() {
		if (CP_RESOLUTION_BP_LISTENERS == null)
			return null;
		return (ClasspathResolutionBreakpointListener[]) CP_RESOLUTION_BP_LISTENERS.toArray(new ClasspathResolutionBreakpointListener[CP_RESOLUTION_BP_LISTENERS.size()]);
	}

	private static void breakpoint(int bp, JavaProject project) {
		ClasspathResolutionBreakpointListener[] listeners = getBPListeners();
		if (listeners == null)
			return;
		for (int j = 0, length = listeners.length; j < length; j++) {
			listeners[j].breakpoint(bp);
		}
	}

	public static boolean areClasspathsEqual(
			IClasspathEntry[] firstClasspath, IClasspathEntry[] secondClasspath,
			IPath firstOutputLocation, IPath secondOutputLocation) {
		int length = firstClasspath.length;
		if (length != secondClasspath.length) return false;
		for (int i = 0; i < length; i++) {
			if (!firstClasspath[i].equals(secondClasspath[i]))
				return false;
		}
		if (firstOutputLocation == null)
			return secondOutputLocation == null;
		return firstOutputLocation.equals(secondOutputLocation);
	}

	/**
	 * Compare current classpath with given one to see if any different.
	 * Note that the argument classpath contains its binary output.
	 * @param newClasspath IClasspathEntry[]
	 * @param newOutputLocation IPath
	 * @param otherClasspathWithOutput IClasspathEntry[]
	 * @return boolean
	 */
	private static boolean areClasspathsEqual(IClasspathEntry[] newClasspath, IPath newOutputLocation, IClasspathEntry[] otherClasspathWithOutput) {

		if (otherClasspathWithOutput == null || otherClasspathWithOutput.length == 0)
			return false;

		int length = otherClasspathWithOutput.length;
		if (length != newClasspath.length + 1)
				// output is amongst file entries (last one)
				return false;


		// compare classpath entries
		for (int i = 0; i < length - 1; i++) {
			if (!otherClasspathWithOutput[i].equals(newClasspath[i]))
				return false;
		}
		// compare binary outputs
		IClasspathEntry output = otherClasspathWithOutput[length - 1];
		if (output.getContentKind() != ClasspathEntry.K_OUTPUT
				|| !output.getPath().equals(newOutputLocation))
			return false;
		return true;
	}
	
	private static boolean areClasspathsEqual(IClasspathEntry[] first, IClasspathEntry[] second) {
		if (first != second){
		    if (first == null) return false;
			int length = first.length;
			if (second == null || second.length != length)
				return false;
			for (int i = 0; i < length; i++) {
				if (!first[i].equals(second[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns a canonicalized path from the given external path.
	 * Note that the return path contains the same number of segments
	 * and it contains a device only if the given path contained one.
	 * @param externalPath IPath
	 * @see java.io.File for the definition of a canonicalized path
	 * @return IPath
	 */
	public static IPath canonicalizedPath(IPath externalPath) {

		if (externalPath == null)
			return null;

		if (IS_CASE_SENSITIVE) {
			return externalPath;
		}
		
		// if not external path, return original path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null) return externalPath; // protection during shutdown (30487)
		if (workspace.getRoot().findMember(externalPath) != null) {
			return externalPath;
		}

		IPath canonicalPath = null;
		try {
			canonicalPath =
				new Path(new File(externalPath.toOSString()).getCanonicalPath());
		} catch (IOException e) {
			// default to original path
			return externalPath;
		}

		IPath result;
		int canonicalLength = canonicalPath.segmentCount();
		if (canonicalLength == 0) {
			// the java.io.File canonicalization failed
			return externalPath;
		} else if (externalPath.isAbsolute()) {
			result = canonicalPath;
		} else {
			// if path is relative, remove the first segments that were added by the java.io.File canonicalization
			// e.g. 'lib/classes.zip' was converted to 'd:/myfolder/lib/classes.zip'
			int externalLength = externalPath.segmentCount();
			if (canonicalLength >= externalLength) {
				result = canonicalPath.removeFirstSegments(canonicalLength - externalLength);
			} else {
				return externalPath;
			}
		}

		// keep device only if it was specified (this is because File.getCanonicalPath() converts '/lib/classes.zip' to 'd:/lib/classes/zip')
		if (externalPath.getDevice() == null) {
			result = result.setDevice(null);
		}
		// keep trailing separator only if it was specified (this is because File.getCanonicalPath() converts 'd:/lib/classes/' to 'd:/lib/classes')
		if (externalPath.hasTrailingSeparator()) {
			result = result.addTrailingSeparator();
		}
		return result;
	}

	/**
	 * Returns true if the given project is accessible and it has
	 * a java nature, otherwise false.
	 * @param project IProject
	 * @return boolean
	 */
	public static boolean hasJavaNature(IProject project) {
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			if (ExternalJavaProject.EXTERNAL_PROJECT_NAME.equals(project.getName()))
				return true;
			// project does not exist or is not open
		}
		return false;
	}

	/*
	 * Detect cycles in the classpath of the workspace's projects
	 * and create markers if necessary.
	 * @param preferredClasspaths Map
	 * @throws JavaModelException
	 */
	public static void validateCycles(Map preferredClasspaths) throws JavaModelException {

		//long start = System.currentTimeMillis();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] rscProjects = workspaceRoot.getProjects();
		int length = rscProjects.length;
		JavaProject[] projects = new JavaProject[length];

		LinkedHashSet cycleParticipants = new LinkedHashSet();
		HashSet traversed = new HashSet();

		// compute cycle participants
		ArrayList prereqChain = new ArrayList();
		for (int i = 0; i < length; i++){
			if (hasJavaNature(rscProjects[i])) {
				JavaProject project = (projects[i] = (JavaProject)JavaCore.create(rscProjects[i]));
				if (!traversed.contains(project.getPath())){
					prereqChain.clear();
					project.updateCycleParticipants(prereqChain, cycleParticipants, workspaceRoot, traversed, preferredClasspaths);
				}
			}
		}
		//System.out.println("updateAllCycleMarkers: " + (System.currentTimeMillis() - start) + " ms");

		for (int i = 0; i < length; i++){
			JavaProject project = projects[i];
			if (project != null) {
				if (cycleParticipants.contains(project.getPath())){
					IMarker cycleMarker = project.getCycleMarker();
					String circularCPOption = project.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true);
					int circularCPSeverity = JavaCore.ERROR.equals(circularCPOption) ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
					if (cycleMarker != null) {
						// update existing cycle marker if needed
						try {
							int existingSeverity = ((Integer)cycleMarker.getAttribute(IMarker.SEVERITY)).intValue();
							if (existingSeverity != circularCPSeverity) {
								cycleMarker.setAttribute(IMarker.SEVERITY, circularCPSeverity);
							}
						} catch (CoreException e) {
							throw new JavaModelException(e);
						}
					} else {
						IJavaProject[] projectsInCycle;
						String cycleString = "";	 //$NON-NLS-1$
						if (cycleParticipants.isEmpty()) {
							projectsInCycle = null;
						} else {
							projectsInCycle = new IJavaProject[cycleParticipants.size()];
							Iterator it = cycleParticipants.iterator();
							int k = 0;
							while (it.hasNext()) {
								//projectsInCycle[i++] = (IPath) it.next();
								IResource member = workspaceRoot.findMember((IPath) it.next());
								if (member != null && member.getType() == IResource.PROJECT){
									projectsInCycle[k] = JavaCore.create((IProject)member);
									if (projectsInCycle[k] != null) {
										if (k != 0) cycleString += ", "; //$NON-NLS-1$
										cycleString += projectsInCycle[k++].getElementName();
									}
								}
							}
						}
						// create new marker
						project.createClasspathProblemMarker(
							new JavaModelStatus(IJavaModelStatusConstants.CLASSPATH_CYCLE, project, cycleString));
					}
				} else {
					project.flushClasspathProblemMarkers(true, false, false);
				}
			}
		}
	}

	/**
	 * Adds a builder to the build spec for the given project.
	 */
	protected void addToBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = this.project.getDescription();
		int javaCommandIndex = getJavaCommandIndex(description.getBuildSpec());

		if (javaCommandIndex == -1) {

			// Add a Java command to the build spec
			ICommand command = description.newCommand();
			command.setBuilderName(builderID);
			setJavaCommand(description, command);
		}
	}
	/**
	 * @see Openable
	 */
	protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {
		// cannot refresh cp markers on opening (emulate cp check on startup) since can create deadlocks (see bug 37274)
		IClasspathEntry[] resolvedClasspath = getResolvedClasspath();

		// compute the pkg fragment roots
		info.setChildren(computePackageFragmentRoots(resolvedClasspath, false, null /*no reverse map*/));

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.JavaElement#close()
	 */
	public void close() throws JavaModelException {
		if (JavaProject.hasJavaNature(this.project)) {
			// Get cached preferences if exist
			JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(this.project, false);
			if (perProjectInfo != null && perProjectInfo.preferences != null) {
				IEclipsePreferences eclipseParentPreferences = (IEclipsePreferences) perProjectInfo.preferences.parent();
				if (this.preferencesNodeListener != null) {
					eclipseParentPreferences.removeNodeChangeListener(this.preferencesNodeListener);
					this.preferencesNodeListener = null;
				}
				if (this.preferencesChangeListener != null) {
					perProjectInfo.preferences.removePreferenceChangeListener(this.preferencesChangeListener);
					this.preferencesChangeListener = null;
				}
			}
		}
		// GROOVY start
		LanguageSupportFactory.getEventHandler().handle(this,"close");
		// GROOVY end
		super.close();
	}

	/**
	 * Internal computation of an expanded classpath. It will eliminate duplicates, and produce copies
	 * of exported or restricted classpath entries to avoid possible side-effects ever after.
	 */
	private void computeExpandedClasspath(
		ClasspathEntry referringEntry,
		HashSet rootIDs,
		ObjectVector accumulatedEntries) throws JavaModelException {

		String projectRootId = rootID();
		if (rootIDs.contains(projectRootId)){
			return; // break cycles if any
		}
		rootIDs.add(projectRootId);

		IClasspathEntry[] resolvedClasspath = getResolvedClasspath();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		boolean isInitialProject = referringEntry == null;
		for (int i = 0, length = resolvedClasspath.length; i < length; i++){
			ClasspathEntry entry = (ClasspathEntry) resolvedClasspath[i];
			if (isInitialProject || entry.isExported()){
				String rootID = entry.rootID();
				if (rootIDs.contains(rootID)) {
					continue;
				}
				// combine restrictions along the project chain
				ClasspathEntry combinedEntry = entry.combineWith(referringEntry);
				accumulatedEntries.add(combinedEntry);

				// recurse in project to get all its indirect exports (only consider exported entries from there on)
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IResource member = workspaceRoot.findMember(entry.getPath());
					if (member != null && member.getType() == IResource.PROJECT){ // double check if bound to project (23977)
						IProject projRsc = (IProject) member;
						if (JavaProject.hasJavaNature(projRsc)) {
							JavaProject javaProject = (JavaProject) JavaCore.create(projRsc);
							javaProject.computeExpandedClasspath(
								combinedEntry,
								rootIDs,
								accumulatedEntries);
						}
					}
				} else {
					rootIDs.add(rootID);
				}
			}
		}
	}

	/**
	 * Computes the package fragment roots identified by the given entry.
	 * Only works with resolved entry
	 * @param resolvedEntry IClasspathEntry
	 * @return IPackageFragmentRoot[]
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(IClasspathEntry resolvedEntry) {
		try {
			return
				computePackageFragmentRoots(
					new IClasspathEntry[]{ resolvedEntry },
					false, // don't retrieve exported roots
					null /* no reverse map */
				);
		} catch (JavaModelException e) {
			return new IPackageFragmentRoot[] {};
		}
	}

	/**
	 * Returns the package fragment roots identified by the given entry. In case it refers to
	 * a project, it will follow its classpath so as to find exported roots as well.
	 * Only works with resolved entry
	 * @param resolvedEntry IClasspathEntry
	 * @param accumulatedRoots ObjectVector
	 * @param rootIDs HashSet
	 * @param referringEntry the CP entry (project) referring to this entry, or null if initial project
	 * @param retrieveExportedRoots boolean
	 * @throws JavaModelException
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry resolvedEntry,
		ObjectVector accumulatedRoots,
		HashSet rootIDs,
		IClasspathEntry referringEntry,
		boolean retrieveExportedRoots,
		Map rootToResolvedEntries) throws JavaModelException {

		String rootID = ((ClasspathEntry)resolvedEntry).rootID();
		if (rootIDs.contains(rootID)) return;

		IPath projectPath = this.project.getFullPath();
		IPath entryPath = resolvedEntry.getPath();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPackageFragmentRoot root = null;

		switch(resolvedEntry.getEntryKind()){

			// source folder
			case IClasspathEntry.CPE_SOURCE :

				if (projectPath.isPrefixOf(entryPath)){
					Object target = JavaModel.getTarget(entryPath, true/*check existency*/);
					if (target == null) return;

					if (target instanceof IFolder || target instanceof IProject){
						root = getPackageFragmentRoot((IResource)target);
					}
				}
				break;

			// internal/external JAR or folder
			case IClasspathEntry.CPE_LIBRARY :
				if (referringEntry != null  && !resolvedEntry.isExported())
					return;
				Object target = JavaModel.getTarget(entryPath, true/*check existency*/);
				if (target == null)
					return;

				if (target instanceof IResource){
					// internal target
					root = getPackageFragmentRoot((IResource) target, entryPath);
				} else if (target instanceof File) {
					// external target
					if (JavaModel.isFile(target)) {
						root = new JarPackageFragmentRoot(entryPath, this);
					} else if (((File) target).isDirectory()) {
						root = new ExternalPackageFragmentRoot(entryPath, this);
					}
				}
				break;

			// recurse into required project
			case IClasspathEntry.CPE_PROJECT :

				if (!retrieveExportedRoots) return;
				if (referringEntry != null && !resolvedEntry.isExported()) return;

				IResource member = workspaceRoot.findMember(entryPath);
				if (member != null && member.getType() == IResource.PROJECT){// double check if bound to project (23977)
					IProject requiredProjectRsc = (IProject) member;
					if (JavaProject.hasJavaNature(requiredProjectRsc)){ // special builder binary output
						rootIDs.add(rootID);
						JavaProject requiredProject = (JavaProject)JavaCore.create(requiredProjectRsc);
						requiredProject.computePackageFragmentRoots(
							requiredProject.getResolvedClasspath(),
							accumulatedRoots,
							rootIDs,
							rootToResolvedEntries == null ? resolvedEntry : ((ClasspathEntry)resolvedEntry).combineWith((ClasspathEntry) referringEntry), // only combine if need to build the reverse map
							retrieveExportedRoots,
							rootToResolvedEntries);
					}
				break;
			}
		}
		if (root != null) {
			accumulatedRoots.add(root);
			rootIDs.add(rootID);
			if (rootToResolvedEntries != null) rootToResolvedEntries.put(root, ((ClasspathEntry)resolvedEntry).combineWith((ClasspathEntry) referringEntry));
		}
	}

	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 * Only works with resolved entries
	 * @param resolvedClasspath IClasspathEntry[]
	 * @param retrieveExportedRoots boolean
	 * @return IPackageFragmentRoot[]
	 * @throws JavaModelException
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(
					IClasspathEntry[] resolvedClasspath,
					boolean retrieveExportedRoots,
					Map rootToResolvedEntries) throws JavaModelException {

		ObjectVector accumulatedRoots = new ObjectVector();
		computePackageFragmentRoots(
			resolvedClasspath,
			accumulatedRoots,
			new HashSet(5), // rootIDs
			null, // inside original project
			retrieveExportedRoots,
			rootToResolvedEntries);
		IPackageFragmentRoot[] rootArray = new IPackageFragmentRoot[accumulatedRoots.size()];
		accumulatedRoots.copyInto(rootArray);
		return rootArray;
	}

	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 * Only works with resolved entries
	 * @param resolvedClasspath IClasspathEntry[]
	 * @param accumulatedRoots ObjectVector
	 * @param rootIDs HashSet
	 * @param referringEntry project entry referring to this CP or null if initial project
	 * @param retrieveExportedRoots boolean
	 * @throws JavaModelException
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry[] resolvedClasspath,
		ObjectVector accumulatedRoots,
		HashSet rootIDs,
		IClasspathEntry referringEntry,
		boolean retrieveExportedRoots,
		Map rootToResolvedEntries) throws JavaModelException {

		if (referringEntry == null){
			rootIDs.add(rootID());
		}
		for (int i = 0, length = resolvedClasspath.length; i < length; i++){
			computePackageFragmentRoots(
				resolvedClasspath[i],
				accumulatedRoots,
				rootIDs,
				referringEntry,
				retrieveExportedRoots,
				rootToResolvedEntries);
		}
	}
	/**
	 * Compute the file name to use for a given shared property
	 * @param qName QualifiedName
	 * @return String
	 */
	public String computeSharedPropertyFileName(QualifiedName qName) {

		return '.' + qName.getLocalName();
	}

	/**
	 * Configure the project with Java nature.
	 */
	public void configure() throws CoreException {

		// register Java builder
		addToBuildSpec(JavaCore.BUILDER_ID);
	}

	/*
	 * Returns whether the given resource is accessible through the children or the non-Java resources of this project.
	 * Returns true if the resource is not in the project.
	 * Assumes that the resource is a folder or a file.
	 */
	public boolean contains(IResource resource) {

		IClasspathEntry[] classpath;
		IPath output;
		try {
			classpath = getResolvedClasspath();
			output = getOutputLocation();
		} catch (JavaModelException e) {
			return false;
		}

		IPath fullPath = resource.getFullPath();
		IPath innerMostOutput = output.isPrefixOf(fullPath) ? output : null;
		IClasspathEntry innerMostEntry = null;
		ExternalFoldersManager foldersManager = JavaModelManager.getExternalManager();
		for (int j = 0, cpLength = classpath.length; j < cpLength; j++) {
			IClasspathEntry entry = classpath[j];

			IPath entryPath = entry.getPath();
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				IResource linkedFolder = foldersManager.getFolder(entryPath);
				if (linkedFolder != null)
					entryPath = linkedFolder.getFullPath();
			}
			if ((innerMostEntry == null || innerMostEntry.getPath().isPrefixOf(entryPath))
					&& entryPath.isPrefixOf(fullPath)) {
				innerMostEntry = entry;
			}
			IPath entryOutput = classpath[j].getOutputLocation();
			if (entryOutput != null && entryOutput.isPrefixOf(fullPath)) {
				innerMostOutput = entryOutput;
			}
		}
		if (innerMostEntry != null) {
			// special case prj==src and nested output location
			if (innerMostOutput != null && innerMostOutput.segmentCount() > 1 // output isn't project
					&& innerMostEntry.getPath().segmentCount() == 1) { // 1 segment must be project name
				return false;
			}
			if  (resource instanceof IFolder) {
				 // folders are always included in src/lib entries
				 return true;
			}
			switch (innerMostEntry.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE:
					// .class files are not visible in source folders
					return !org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(fullPath.lastSegment());
				case IClasspathEntry.CPE_LIBRARY:
					// .java files are not visible in library folders
					return !org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(fullPath.lastSegment());
			}
		}
		if (innerMostOutput != null) {
			return false;
		}
		return true;
	}

	/**
	 * Record a new marker denoting a classpath problem
	 */
	public void createClasspathProblemMarker(IJavaModelStatus status) {

		IMarker marker = null;
		int severity;
		String[] arguments = CharOperation.NO_STRINGS;
		boolean isCycleProblem = false, isClasspathFileFormatProblem = false, isOutputOverlapping = false;
		switch (status.getCode()) {

			case  IJavaModelStatusConstants.CLASSPATH_CYCLE :
				isCycleProblem = true;
				if (JavaCore.ERROR.equals(getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true))) {
					severity = IMarker.SEVERITY_ERROR;
				} else {
					severity = IMarker.SEVERITY_WARNING;
				}
				break;

			case  IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT :
				isClasspathFileFormatProblem = true;
				severity = IMarker.SEVERITY_ERROR;
				break;

			case  IJavaModelStatusConstants.INCOMPATIBLE_JDK_LEVEL :
				String setting = getOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, true);
				if (JavaCore.ERROR.equals(setting)) {
					severity = IMarker.SEVERITY_ERROR;
				} else if (JavaCore.WARNING.equals(setting)) {
					severity = IMarker.SEVERITY_WARNING;
				} else {
					return; // setting == IGNORE
				}
				break;
			case IJavaModelStatusConstants.OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE :
				isOutputOverlapping = true;
				setting = getOption(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, true);
				if (JavaCore.ERROR.equals(setting)) {
					severity = IMarker.SEVERITY_ERROR;
				} else if (JavaCore.WARNING.equals(setting)) {
					severity = IMarker.SEVERITY_WARNING;
				} else {
					return; // setting == IGNORE
				}
				break;
			default:
				IPath path = status.getPath();
				if (path != null) arguments = new String[] { path.toString() };
				if (JavaCore.ERROR.equals(getOption(JavaCore.CORE_INCOMPLETE_CLASSPATH, true)) &&
					status.getSeverity() != IStatus.WARNING) {
					severity = IMarker.SEVERITY_ERROR;
				} else {
					severity = IMarker.SEVERITY_WARNING;
				}
				break;
		}

		try {
			marker = this.project.createMarker(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] {
					IMarker.MESSAGE,
					IMarker.SEVERITY,
					IMarker.LOCATION,
					IJavaModelMarker.CYCLE_DETECTED,
					IJavaModelMarker.CLASSPATH_FILE_FORMAT,
					IJavaModelMarker.OUTPUT_OVERLAPPING_SOURCE,
					IJavaModelMarker.ID,
					IJavaModelMarker.ARGUMENTS ,
					IJavaModelMarker.CATEGORY_ID,
					IMarker.SOURCE_ID,
				},
				new Object[] {
					status.getMessage(),
					new Integer(severity),
					Messages.classpath_buildPath,
					isCycleProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					isClasspathFileFormatProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					isOutputOverlapping ? "true" : "false", //$NON-NLS-1$ //$NON-NLS-2$
					new Integer(status.getCode()),
					Util.getProblemArgumentsForMarker(arguments) ,
					new Integer(CategorizedProblem.CAT_BUILDPATH),
					JavaBuilder.SOURCE_ID,
				}
			);
		} catch (CoreException e) {
			// could not create marker: cannot do much
			if (JavaModelManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns a new element info for this element.
	 */
	protected Object createElementInfo() {
		return new JavaProjectElementInfo();
	}

	/**
	 * Reads and decode an XML classpath string. Returns a two-dimensional array, where the number of elements in the row is fixed to 2.
	 * The first element is an array of raw classpath entries and the second element is an array of referenced entries that may have been stored
	 * by the client earlier. See {@link IJavaProject#getReferencedClasspathEntries()} for more details. 
	 * 
	 */
	public IClasspathEntry[][] decodeClasspath(String xmlClasspath, Map unknownElements) throws IOException, ClasspathEntry.AssertionFailedException {

		ArrayList paths = new ArrayList();
		IClasspathEntry defaultOutput = null;
		StringReader reader = new StringReader(xmlClasspath);
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(Messages.file_badFormat);
		} catch (ParserConfigurationException e) {
			throw new IOException(Messages.file_badFormat);
		} finally {
			reader.close();
		}

		if (!cpElement.getNodeName().equalsIgnoreCase("classpath")) { //$NON-NLS-1$
			throw new IOException(Messages.file_badFormat);
		}
		NodeList list = cpElement.getElementsByTagName(ClasspathEntry.TAG_CLASSPATHENTRY);
		int length = list.getLength();

		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				IClasspathEntry entry = ClasspathEntry.elementDecode((Element)node, this, unknownElements);
				if (entry != null){
					if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
						defaultOutput = entry; // separate output
					} else {
						paths.add(entry);
					}
				}
			}
		}
		int pathSize = paths.size();
		IClasspathEntry[][] entries = new IClasspathEntry[2][];
		entries[0] = new IClasspathEntry[pathSize + (defaultOutput == null ? 0 : 1)];
		paths.toArray(entries[0]);
		if (defaultOutput != null) entries[0][pathSize] = defaultOutput; // ensure output is last item
		
		paths.clear();
		list = cpElement.getElementsByTagName(ClasspathEntry.TAG_REFERENCED_ENTRY);
		length = list.getLength();

		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				IClasspathEntry entry = ClasspathEntry.elementDecode((Element)node, this, unknownElements);
				if (entry != null){
					paths.add(entry);
				}
			}
		}
		entries[1] = new IClasspathEntry[paths.size()];
		paths.toArray(entries[1]);

		return entries;
	}

	public IClasspathEntry decodeClasspathEntry(String encodedEntry) {

		try {
			if (encodedEntry == null) return null;
			StringReader reader = new StringReader(encodedEntry);
			Element node;

			try {
				DocumentBuilder parser =
					DocumentBuilderFactory.newInstance().newDocumentBuilder();
				node = parser.parse(new InputSource(reader)).getDocumentElement();
			} catch (SAXException e) {
				return null;
			} catch (ParserConfigurationException e) {
				return null;
			} finally {
				reader.close();
			}

			if (!node.getNodeName().equalsIgnoreCase(ClasspathEntry.TAG_CLASSPATHENTRY)
					|| node.getNodeType() != Node.ELEMENT_NODE) {
				return null;
			}
			return ClasspathEntry.elementDecode(node, this, null/*not interested in unknown elements*/);
		} catch (IOException e) {
			// bad format
			return null;
		}
	}

	/**
	/**
	 * Removes the Java nature from the project.
	 */
	public void deconfigure() throws CoreException {

		// deregister Java builder
		removeFromBuildSpec(JavaCore.BUILDER_ID);

		// remove .classpath file
//		getProject().getFile(ClasspathHelper.CLASSPATH_FILENAME).delete(false, null);
	}

	/**
	 * Returns a default class path.
	 * This is the root of the project
	 */
	protected IClasspathEntry[] defaultClasspath() {

		return new IClasspathEntry[] {
			 JavaCore.newSourceEntry(this.project.getFullPath())};
	}

	/**
	 * Returns a default output location.
	 * This is the project bin folder
	 */
	protected IPath defaultOutputLocation() {
		return this.project.getFullPath().append("bin"); //$NON-NLS-1$
	}

	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected String encodeClasspath(IClasspathEntry[] classpath, IClasspathEntry[] referencedEntries, IPath outputLocation, boolean indent, Map unknownElements) throws JavaModelException {
		try {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
			XMLWriter xmlWriter = new XMLWriter(writer, this, true/*print XML version*/);

			xmlWriter.startTag(ClasspathEntry.TAG_CLASSPATH, indent);
			for (int i = 0; i < classpath.length; ++i) {
				((ClasspathEntry)classpath[i]).elementEncode(xmlWriter, this.project.getFullPath(), indent, true, unknownElements, false);
			}

			if (outputLocation != null) {
				outputLocation = outputLocation.removeFirstSegments(1);
				outputLocation = outputLocation.makeRelative();
				HashMap parameters = new HashMap();
				parameters.put(ClasspathEntry.TAG_KIND, ClasspathEntry.kindToString(ClasspathEntry.K_OUTPUT));
				parameters.put(ClasspathEntry.TAG_PATH, String.valueOf(outputLocation));
				xmlWriter.printTag(ClasspathEntry.TAG_CLASSPATHENTRY, parameters, indent, true, true);
			}

			if (referencedEntries != null) {
				for (int i = 0; i < referencedEntries.length; ++i) {
					((ClasspathEntry) referencedEntries[i]).elementEncode(xmlWriter, this.project.getFullPath(), indent, true, unknownElements, true);
				}
			}
			
			xmlWriter.endTag(ClasspathEntry.TAG_CLASSPATH, indent, true/*insert new line*/);
			writer.flush();
			writer.close();
			return s.toString("UTF8");//$NON-NLS-1$
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}

	public String encodeClasspathEntry(IClasspathEntry classpathEntry) {
		try {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
			XMLWriter xmlWriter = new XMLWriter(writer, this, false/*don't print XML version*/);

			((ClasspathEntry)classpathEntry).elementEncode(xmlWriter, this.project.getFullPath(), true/*indent*/, true/*insert new line*/, null/*not interested in unknown elements*/, (classpathEntry.getReferencingEntry() != null));

			writer.flush();
			writer.close();
			return s.toString("UTF8");//$NON-NLS-1$
		} catch (IOException e) {
			return null; // never happens since all is done in memory
		}
	}

	/**
	 * Returns true if this handle represents the same Java project
	 * as the given handle. Two handles represent the same
	 * project if they are identical or if they represent a project with
	 * the same underlying resource and occurrence counts.
	 *
	 * @see JavaElement#equals(Object)
	 */
	public boolean equals(Object o) {

		if (this == o)
			return true;

		if (!(o instanceof JavaProject))
			return false;

		JavaProject other = (JavaProject) o;
		return this.project.equals(other.getProject());
	}

	/**
	 * @see IJavaProject#findElement(IPath)
	 */
	public IJavaElement findElement(IPath path) throws JavaModelException {
		return findElement(path, DefaultWorkingCopyOwner.PRIMARY);
	}

	/**
	 * @see IJavaProject#findElement(IPath, WorkingCopyOwner)
	 */
	public IJavaElement findElement(IPath path, WorkingCopyOwner owner) throws JavaModelException {

		if (path == null || path.isAbsolute()) {
			throw new JavaModelException(
				new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, path));
		}
		try {

			String extension = path.getFileExtension();
			if (extension == null) {
				String packageName = path.toString().replace(IPath.SEPARATOR, '.');
				return findPackageFragment(packageName);
			} else if (Util.isJavaLikeFileName(path.lastSegment())
					|| extension.equalsIgnoreCase(EXTENSION_class)) {
				IPath packagePath = path.removeLastSegments(1);
				String packageName = packagePath.toString().replace(IPath.SEPARATOR, '.');
				String typeName = path.lastSegment();
				typeName = typeName.substring(0, typeName.length() - extension.length() - 1);
				String qualifiedName = null;
				if (packageName.length() > 0) {
					qualifiedName = packageName + "." + typeName; //$NON-NLS-1$
				} else {
					qualifiedName = typeName;
				}

				// lookup type
				NameLookup lookup = newNameLookup(owner);
				NameLookup.Answer answer = lookup.findType(
					qualifiedName,
					false,
					NameLookup.ACCEPT_ALL,
					true/* consider secondary types */,
					false/* do NOT wait for indexes */,
					false/*don't check restrictions*/,
					null);

				if (answer != null) {
					return answer.type.getParent();
				} else {
					return null;
				}
			} else {
				// unsupported extension
				return null;
			}
		} catch (JavaModelException e) {
			if (e.getStatus().getCode()
				== IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
				return null;
			} else {
				throw e;
			}
		}
	}

	public IJavaElement findPackageFragment(String packageName)
			throws JavaModelException {
		NameLookup lookup = newNameLookup((WorkingCopyOwner)null/*no need to look at working copies for pkgs*/);
		IPackageFragment[] pkgFragments = lookup.findPackageFragments(packageName, false);
		if (pkgFragments == null) {
			return null;

		} else {
			// try to return one that is a child of this project
			for (int i = 0, length = pkgFragments.length; i < length; i++) {

				IPackageFragment pkgFragment = pkgFragments[i];
				if (equals(pkgFragment.getParent().getParent())) {
					return pkgFragment;
				}
			}
			// default to the first one
			return pkgFragments[0];
		}
	}

	public IJavaElement findElement(String bindingKey, WorkingCopyOwner owner) throws JavaModelException {
		JavaElementFinder elementFinder = new JavaElementFinder(bindingKey, this, owner);
		elementFinder.parse();
		if (elementFinder.exception != null)
			throw elementFinder.exception;
		return elementFinder.element;
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragment findPackageFragment(IPath path)
		throws JavaModelException {

		return findPackageFragment0(JavaProject.canonicalizedPath(path));
	}
	/*
	 * non path canonicalizing version
	 */
	private IPackageFragment findPackageFragment0(IPath path)
		throws JavaModelException {

		NameLookup lookup = newNameLookup((WorkingCopyOwner)null/*no need to look at working copies for pkgs*/);
		return lookup.findPackageFragment(path);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot findPackageFragmentRoot(IPath path)
		throws JavaModelException {

		return findPackageFragmentRoot0(JavaProject.canonicalizedPath(path));
	}
	/*
	 * no path canonicalization
	 */
	public IPackageFragmentRoot findPackageFragmentRoot0(IPath path)
		throws JavaModelException {

		IPackageFragmentRoot[] allRoots = this.getAllPackageFragmentRoots();
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException(Messages.path_mustBeAbsolute);
		}
		for (int i= 0; i < allRoots.length; i++) {
			IPackageFragmentRoot classpathRoot= allRoots[i];
			if (classpathRoot.getPath().equals(path)) {
				return classpathRoot;
			}
		}
		return null;
	}
	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] findPackageFragmentRoots(IClasspathEntry entry) {
		try {
			IClasspathEntry[] classpath = getRawClasspath();
			for (int i = 0, length = classpath.length; i < length; i++) {
				if (classpath[i].equals(entry)) { // entry may need to be resolved
					return
						computePackageFragmentRoots(
							resolveClasspath(new IClasspathEntry[] {entry}),
							false, // don't retrieve exported roots
							null); /*no reverse map*/
				}
			}
		} catch (JavaModelException e) {
			// project doesn't exist: return an empty array
		}
		return new IPackageFragmentRoot[] {};
	}
	/**
	 * @see IJavaProject#findType(String)
	 */
	public IType findType(String fullyQualifiedName) throws JavaModelException {
		return findType(fullyQualifiedName, DefaultWorkingCopyOwner.PRIMARY);
	}
	/**
	 * @see IJavaProject#findType(String, IProgressMonitor)
	 */
	public IType findType(String fullyQualifiedName, IProgressMonitor progressMonitor) throws JavaModelException {
		return findType(fullyQualifiedName, DefaultWorkingCopyOwner.PRIMARY, progressMonitor);
	}

	/*
	 * Internal findType with instanciated name lookup
	 */
	IType findType(String fullyQualifiedName, NameLookup lookup, boolean considerSecondaryTypes, IProgressMonitor progressMonitor) throws JavaModelException {
		NameLookup.Answer answer = lookup.findType(
			fullyQualifiedName,
			false,
			NameLookup.ACCEPT_ALL,
			considerSecondaryTypes,
			true, /* wait for indexes (only if consider secondary types)*/
			false/*don't check restrictions*/,
			progressMonitor);
		if (answer == null) {
			// try to find enclosing type
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			if (lastDot == -1) return null;
			IType type = findType(fullyQualifiedName.substring(0, lastDot), lookup, considerSecondaryTypes, progressMonitor);
			if (type != null) {
				type = type.getType(fullyQualifiedName.substring(lastDot+1));
				if (!type.exists()) {
					return null;
				}
			}
			return type;
		}
		return answer.type;
	}
	/**
	 * @see IJavaProject#findType(String, String)
	 */
	public IType findType(String packageName, String typeQualifiedName) throws JavaModelException {
		return findType(packageName, typeQualifiedName, DefaultWorkingCopyOwner.PRIMARY);
	}
	/**
	 * @see IJavaProject#findType(String, String, IProgressMonitor)
	 */
	public IType findType(String packageName, String typeQualifiedName, IProgressMonitor progressMonitor) throws JavaModelException {
		return findType(packageName, typeQualifiedName, DefaultWorkingCopyOwner.PRIMARY, progressMonitor);
	}
	/*
	 * Internal findType with instanciated name lookup
	 */
	IType findType(String packageName, String typeQualifiedName, NameLookup lookup, boolean considerSecondaryTypes, IProgressMonitor progressMonitor) throws JavaModelException {
		NameLookup.Answer answer = lookup.findType(
			typeQualifiedName,
			packageName,
			false,
			NameLookup.ACCEPT_ALL,
			considerSecondaryTypes,
			true, // wait for indexes (in case we need to consider secondary types)
			false/*don't check restrictions*/,
			progressMonitor);
		return answer == null ? null : answer.type;
	}
	/**
	 * @see IJavaProject#findType(String, String, WorkingCopyOwner)
	 */
	public IType findType(String packageName, String typeQualifiedName, WorkingCopyOwner owner) throws JavaModelException {
		NameLookup lookup = newNameLookup(owner);
		return findType(
			packageName,
			typeQualifiedName,
			lookup,
			false, // do not consider secondary types
			null);
	}

	/**
	 * @see IJavaProject#findType(String, String, WorkingCopyOwner, IProgressMonitor)
	 */
	public IType findType(String packageName, String typeQualifiedName, WorkingCopyOwner owner, IProgressMonitor progressMonitor) throws JavaModelException {
		NameLookup lookup = newNameLookup(owner);
		return findType(
			packageName,
			typeQualifiedName,
			lookup,
			true, // consider secondary types
			progressMonitor);
	}

	/**
	 * @see IJavaProject#findType(String, WorkingCopyOwner)
	 */
	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner) throws JavaModelException {
		NameLookup lookup = newNameLookup(owner);
		return findType(fullyQualifiedName, lookup, false, null);
	}

	/**
	 * @see IJavaProject#findType(String, WorkingCopyOwner, IProgressMonitor)
	 */
	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner, IProgressMonitor progressMonitor) throws JavaModelException {
		NameLookup lookup = newNameLookup(owner);
		return findType(fullyQualifiedName, lookup, true, progressMonitor);
	}

	/**
	 * Remove all markers denoting classpath problems
	 */ //TODO (philippe) should improve to use a bitmask instead of booleans (CYCLE, FORMAT, VALID)
	protected void flushClasspathProblemMarkers(boolean flushCycleMarkers, boolean flushClasspathFormatMarkers, boolean flushOverlappingOutputMarkers) {
		try {
			if (this.project.isAccessible()) {
				IMarker[] markers = this.project.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					if (flushCycleMarkers && flushClasspathFormatMarkers && flushOverlappingOutputMarkers) {
						marker.delete();
					} else {
						String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
						String classpathFileFormatAttr =  (String)marker.getAttribute(IJavaModelMarker.CLASSPATH_FILE_FORMAT);
						String overlappingOutputAttr = (String) marker.getAttribute(IJavaModelMarker.OUTPUT_OVERLAPPING_SOURCE);
						if ((flushCycleMarkers == (cycleAttr != null && cycleAttr.equals("true"))) //$NON-NLS-1$
							&& (flushOverlappingOutputMarkers == (overlappingOutputAttr != null && overlappingOutputAttr.equals("true"))) //$NON-NLS-1$
							&& (flushClasspathFormatMarkers == (classpathFileFormatAttr != null && classpathFileFormatAttr.equals("true")))){ //$NON-NLS-1$
							marker.delete();
						}
					}
				}
			}
		} catch (CoreException e) {
			// could not flush markers: not much we can do
			if (JavaModelManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the set of patterns corresponding to this project visibility given rules
	 * @return an array of IPath or null if none
	 */
	public IPath[] getAccessRestrictions(String optionName) {
		String sequence = getOption(optionName, true); // inherit from workspace
		if (sequence == null || sequence.length() == 0) return null;
		IPath[] rules = null;
		char[][] patterns = CharOperation.splitOn('|', sequence.toCharArray());
		int patternCount;
		if ((patternCount  = patterns.length) > 0) {
			rules = new IPath[patternCount];
			for (int j = 0; j < patterns.length; j++){
				rules[j] = new Path(new String(patterns[j]));
			}
		}
		return rules;
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getAllPackageFragmentRoots()
		throws JavaModelException {

		return getAllPackageFragmentRoots(null /*no reverse map*/);
	}

	public IPackageFragmentRoot[] getAllPackageFragmentRoots(Map rootToResolvedEntries) throws JavaModelException {

		return computePackageFragmentRoots(getResolvedClasspath(), true/*retrieveExportedRoots*/, rootToResolvedEntries);
	}

	/**
	 * Returns the classpath entry that refers to the given path
	 * or <code>null</code> if there is no reference to the path.
	 * @param path IPath
	 * @return IClasspathEntry
	 * @throws JavaModelException
	 */
	public IClasspathEntry getClasspathEntryFor(IPath path) throws JavaModelException {
		getResolvedClasspath(); // force resolution
		PerProjectInfo perProjectInfo = getPerProjectInfo();
		if (perProjectInfo == null)
			return null;
		Map rootPathToResolvedEntries = perProjectInfo.rootPathToResolvedEntries;
		if (rootPathToResolvedEntries == null)
			return null;
		IClasspathEntry classpathEntry = (IClasspathEntry) rootPathToResolvedEntries.get(path);
		if (classpathEntry == null) {
			path = getProject().getWorkspace().getRoot().getLocation().append(path);
			classpathEntry = (IClasspathEntry) rootPathToResolvedEntries.get(path);
		}
		return classpathEntry;
	}

	/*
	 * Returns the cycle marker associated with this project or null if none.
	 */
	public IMarker getCycleMarker(){
		try {
			if (this.project.isAccessible()) {
				IMarker[] markers = this.project.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
					if (cycleAttr != null && cycleAttr.equals("true")){ //$NON-NLS-1$
						return marker;
					}
				}
			}
		} catch (CoreException e) {
			// could not get markers: return null
		}
		return null;
	}

	/**
	 * Returns the project custom preference pool.
	 * Project preferences may include custom encoding.
	 * @return IEclipsePreferences or <code>null</code> if the project
	 * 	does not have a java nature.
	 */
	public IEclipsePreferences getEclipsePreferences() {
		if (!JavaProject.hasJavaNature(this.project)) return null;
		// Get cached preferences if exist
		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(this.project, true);
		if (perProjectInfo.preferences != null) return perProjectInfo.preferences;
		// Init project preferences
		IScopeContext context = new ProjectScope(getProject());
		final IEclipsePreferences eclipsePreferences = context.getNode(JavaCore.PLUGIN_ID);
		updatePreferences(eclipsePreferences);
		perProjectInfo.preferences = eclipsePreferences;

		// Listen to new preferences node
		final IEclipsePreferences eclipseParentPreferences = (IEclipsePreferences) eclipsePreferences.parent();
		if (eclipseParentPreferences != null) {
			if (this.preferencesNodeListener != null) {
				eclipseParentPreferences.removeNodeChangeListener(this.preferencesNodeListener);
			}
			this.preferencesNodeListener = new IEclipsePreferences.INodeChangeListener() {
				public void added(IEclipsePreferences.NodeChangeEvent event) {
					// do nothing
				}
				public void removed(IEclipsePreferences.NodeChangeEvent event) {
					if (event.getChild() == eclipsePreferences) {
						JavaModelManager.getJavaModelManager().resetProjectPreferences(JavaProject.this);
					}
				}
			};
			eclipseParentPreferences.addNodeChangeListener(this.preferencesNodeListener);
		}

		// Listen to preferences changes
		if (this.preferencesChangeListener != null) {
			eclipsePreferences.removePreferenceChangeListener(this.preferencesChangeListener);
		}
		this.preferencesChangeListener = new IEclipsePreferences.IPreferenceChangeListener() {
			public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
				String propertyName = event.getKey();
				JavaModelManager manager = JavaModelManager.getJavaModelManager();
				if (propertyName.startsWith(JavaCore.PLUGIN_ID)) {
					if (propertyName.equals(JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER) ||
						propertyName.equals(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER) ||
						propertyName.equals(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE) ||
						propertyName.equals(JavaCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER) ||
						propertyName.equals(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH) ||
						propertyName.equals(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS) ||
						propertyName.equals(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS) ||
						propertyName.equals(JavaCore.CORE_INCOMPLETE_CLASSPATH) ||
						propertyName.equals(JavaCore.CORE_CIRCULAR_CLASSPATH) ||
						propertyName.equals(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE) ||
						propertyName.equals(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL) ||
						propertyName.equals(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM))
					{
						manager.deltaState.addClasspathValidation(JavaProject.this);
					}
					manager.resetProjectOptions(JavaProject.this);
					JavaProject.this.resetCaches(); // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=233568
				}
			}
		};
		eclipsePreferences.addPreferenceChangeListener(this.preferencesChangeListener);
		return eclipsePreferences;
	}

	public String getElementName() {
		return this.project.getName();
	}

	/**
	 * @see IJavaElement
	 */
	public int getElementType() {
		return JAVA_PROJECT;
	}

	/**
	 * This is a helper method returning the expanded classpath for the project, as a list of classpath entries,
	 * where all classpath variable entries have been resolved and substituted with their final target entries.
	 * All project exports have been appended to project entries.
	 * @return IClasspathEntry[]
	 * @throws JavaModelException
	 */
	public IClasspathEntry[] getExpandedClasspath()	throws JavaModelException {

			ObjectVector accumulatedEntries = new ObjectVector();
			computeExpandedClasspath(null, new HashSet(5), accumulatedEntries);

			IClasspathEntry[] expandedPath = new IClasspathEntry[accumulatedEntries.size()];
			accumulatedEntries.copyInto(expandedPath);

			return expandedPath;
	}

	/**
	 * The path is known to match a source/library folder entry.
	 * @param path IPath
	 * @return IPackageFragmentRoot
	 */
	public IPackageFragmentRoot getFolderPackageFragmentRoot(IPath path) {
		if (path.segmentCount() == 1) { // default project root
			return getPackageFragmentRoot(this.project);
		}
		return getPackageFragmentRoot(this.project.getWorkspace().getRoot().getFolder(path));
	}

	/*
	 * @see JavaElement
	 */
	public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
		switch (token.charAt(0)) {
			case JEM_PACKAGEFRAGMENTROOT:
				String rootPath = IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH;
				token = null;
				while (memento.hasMoreTokens()) {
					token = memento.nextToken();
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331821
					if (token == MementoTokenizer.PACKAGEFRAGMENT || token == MementoTokenizer.COUNT) {
						break;
					}
					rootPath += token;
				}
				JavaElement root = (JavaElement)getPackageFragmentRoot(new Path(rootPath));
				if (token != null && token.charAt(0) == JEM_PACKAGEFRAGMENT) {
					return root.getHandleFromMemento(token, memento, owner);
				} else {
					return root.getHandleFromMemento(memento, owner);
				}
		}
		return null;
	}

	/**
	 * Returns the <code>char</code> that marks the start of this handles
	 * contribution to a memento.
	 */
	protected char getHandleMementoDelimiter() {

		return JEM_JAVAPROJECT;
	}

	/**
	 * Find the specific Java command amongst the given build spec
	 * and return its index or -1 if not found.
	 */
	private int getJavaCommandIndex(ICommand[] buildSpec) {

		for (int i = 0; i < buildSpec.length; ++i) {
			if (buildSpec[i].getBuilderName().equals(JavaCore.BUILDER_ID)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Convenience method that returns the specific type of info for a Java project.
	 */
	protected JavaProjectElementInfo getJavaProjectElementInfo()
		throws JavaModelException {

		return (JavaProjectElementInfo) getElementInfo();
	}

	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() throws JavaModelException {

		return ((JavaProjectElementInfo) getElementInfo()).getNonJavaResources(this);
	}

	/**
	 * @see org.eclipse.jdt.core.IJavaProject#getOption(String, boolean)
	 */
	public String getOption(String optionName, boolean inheritJavaCoreOptions) {
		return JavaModelManager.getJavaModelManager().getOption(optionName, inheritJavaCoreOptions, getEclipsePreferences());
		}

	/**
	 * @see org.eclipse.jdt.core.IJavaProject#getOptions(boolean)
	 */
	public Map getOptions(boolean inheritJavaCoreOptions) {

		// initialize to the defaults from JavaCore options pool
		Map options = inheritJavaCoreOptions ? JavaCore.getOptions() : new Hashtable(5);

		// Get project specific options
		JavaModelManager.PerProjectInfo perProjectInfo = null;
		Hashtable projectOptions = null;
		JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
		HashSet optionNames = javaModelManager.optionNames;
		try {
			perProjectInfo = getPerProjectInfo();
			projectOptions = perProjectInfo.options;
			if (projectOptions == null) {
				// get eclipse preferences
				IEclipsePreferences projectPreferences= getEclipsePreferences();
				if (projectPreferences == null) return options; // cannot do better (non-Java project)
				// create project options
				String[] propertyNames = projectPreferences.keys();
				projectOptions = new Hashtable(propertyNames.length);
				for (int i = 0; i < propertyNames.length; i++){
					String propertyName = propertyNames[i];
					String value = projectPreferences.get(propertyName, null);
					if (value != null) {
						value = value.trim();
						// Keep the option value, even if it's deprecated
						// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=324987
						projectOptions.put(propertyName, value);
						if (!optionNames.contains(propertyName)) {
							// try to migrate deprecated options
							String[] compatibleOptions = (String[]) javaModelManager.deprecatedOptions.get(propertyName);
							if (compatibleOptions != null) {
								for (int co=0, length=compatibleOptions.length; co < length; co++) {
									String compatibleOption = compatibleOptions[co];
									if (!projectOptions.containsKey(compatibleOption))
										projectOptions.put(compatibleOption, value);
								}
							}
						}
					}
				}
				
				// cache project options
				perProjectInfo.options = projectOptions;
			}
		} catch (JavaModelException jme) {
			projectOptions = new Hashtable();
		} catch (BackingStoreException e) {
			projectOptions = new Hashtable();
		}

		// Inherit from JavaCore options if specified
		if (inheritJavaCoreOptions) {
			Iterator propertyNames = projectOptions.entrySet().iterator();
			while (propertyNames.hasNext()) {
				Map.Entry entry = (Map.Entry) propertyNames.next();
				String propertyName = (String) entry.getKey();
				String propertyValue = (String) entry.getValue();
				if (propertyValue != null && javaModelManager.knowsOption(propertyName)){
					options.put(propertyName, propertyValue.trim());
				}
			}
			Util.fixTaskTags(options);
			return options;
		}
		Util.fixTaskTags(projectOptions);
		return projectOptions;
	}

	/**
	 * @see IJavaProject
	 */
	public IPath getOutputLocation() throws JavaModelException {
		// Do not create marker while getting output location
		JavaModelManager.PerProjectInfo perProjectInfo = getPerProjectInfo();
		IPath outputLocation = perProjectInfo.outputLocation;
		if (outputLocation != null) return outputLocation;

		// force to read classpath - will position output location as well
		getRawClasspath();

		outputLocation = perProjectInfo.outputLocation;
		if (outputLocation == null) {
			return defaultOutputLocation();
		}
		return outputLocation;
	}

	/**
	 * @param path IPath
	 * @return A handle to the package fragment root identified by the given path.
	 * This method is handle-only and the element may or may not exist. Returns
	 * <code>null</code> if unable to generate a handle from the path (for example,
	 * an absolute path that has less than 1 segment. The path may be relative or
	 * absolute.
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IPath path) {
		if (!path.isAbsolute()) {
			path = getPath().append(path);
		}
		int segmentCount = path.segmentCount();
		if (segmentCount == 0) {
			return null;
		}
		if (path.getDevice() != null || JavaModel.getExternalTarget(path, true/*check existence*/) != null) {
			// external path
			return getPackageFragmentRoot0(path);
		}
		IWorkspaceRoot workspaceRoot = this.project.getWorkspace().getRoot();
		IResource resource = workspaceRoot.findMember(path);
		if (resource == null) {
			// resource doesn't exist in workspace
			if (path.getFileExtension() != null) {
				if (!workspaceRoot.getProject(path.segment(0)).exists()) {
					// assume it is an external ZIP archive
					return getPackageFragmentRoot0(path);
				} else {
					// assume it is an internal ZIP archive
					resource = workspaceRoot.getFile(path);
				}
			} else if (segmentCount == 1) {
				// assume it is a project
				String projectName = path.segment(0);
				if (getElementName().equals(projectName)) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=75814
					// default root
					resource = this.project;
				} else {
					// lib being another project
					resource = workspaceRoot.getProject(projectName);
				}
			} else {
				// assume it is an internal folder
				resource = workspaceRoot.getFolder(path);
			}
		}
		return getPackageFragmentRoot(resource);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {
		return getPackageFragmentRoot(resource, null/*no entry path*/);
	}

	private IPackageFragmentRoot getPackageFragmentRoot(IResource resource, IPath entryPath) {
		switch (resource.getType()) {
			case IResource.FILE:
				return new JarPackageFragmentRoot(resource, this);
			case IResource.FOLDER:
				if (ExternalFoldersManager.isInternalPathForExternalFolder(resource.getFullPath()))
					return new ExternalPackageFragmentRoot(resource, entryPath, this);
				return new PackageFragmentRoot(resource, this);
			case IResource.PROJECT:
				return new PackageFragmentRoot(resource, this);
			default:
				return null;
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(String externalLibraryPath) {
		return getPackageFragmentRoot0(JavaProject.canonicalizedPath(new Path(externalLibraryPath)));
	}

	/*
	 * no path canonicalization
	 */
	public IPackageFragmentRoot getPackageFragmentRoot0(IPath externalLibraryPath) {
		IFolder linkedFolder = JavaModelManager.getExternalManager().getFolder(externalLibraryPath);
		if (linkedFolder != null)
			return new ExternalPackageFragmentRoot(linkedFolder, externalLibraryPath, this);
		return new JarPackageFragmentRoot(externalLibraryPath, this);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots()
		throws JavaModelException {

		Object[] children;
		int length;
		IPackageFragmentRoot[] roots;

		System.arraycopy(
			children = getChildren(),
			0,
			roots = new IPackageFragmentRoot[length = children.length],
			0,
			length);

		return roots;
	}

	/**
	 * @see IJavaProject
	 * @deprecated
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {
		return findPackageFragmentRoots(entry);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragment[] getPackageFragments() throws JavaModelException {

		IPackageFragmentRoot[] roots = getPackageFragmentRoots();
		return getPackageFragmentsInRoots(roots);
	}

	/**
	 * Returns all the package fragments found in the specified
	 * package fragment roots.
	 * @param roots IPackageFragmentRoot[]
	 * @return IPackageFragment[]
	 */
	public IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots) {

		ArrayList frags = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			try {
				IJavaElement[] rootFragments = root.getChildren();
				for (int j = 0; j < rootFragments.length; j++) {
					frags.add(rootFragments[j]);
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[frags.size()];
		frags.toArray(fragments);
		return fragments;
	}

	/**
	 * @see IJavaElement
	 */
	public IPath getPath() {
		return this.project.getFullPath();
	}

	public JavaModelManager.PerProjectInfo getPerProjectInfo() throws JavaModelException {
		return JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(this.project);
	}

	private IPath getPluginWorkingLocation() {
		return this.project.getWorkingLocation(JavaCore.PLUGIN_ID);
	}

	/**
	 * @see IJavaProject#getProject()
	 */
	public IProject getProject() {
		return this.project;
	}

	public ProjectCache getProjectCache() throws JavaModelException {
		return ((JavaProjectElementInfo) getElementInfo()).getProjectCache(this);
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getRawClasspath() throws JavaModelException {
		JavaModelManager.PerProjectInfo perProjectInfo = getPerProjectInfo();
		IClasspathEntry[] classpath = perProjectInfo.rawClasspath;
		if (classpath != null) return classpath;

		classpath = perProjectInfo.readAndCacheClasspath(this)[0];

		if (classpath == JavaProject.INVALID_CLASSPATH)
			return defaultClasspath();

		return classpath;
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getReferencedClasspathEntries() throws JavaModelException {
		return getPerProjectInfo().referencedEntries;
	}
	
	/**
	 * @see IJavaProject#getRequiredProjectNames()
	 */
	public String[] getRequiredProjectNames() throws JavaModelException {

		return projectPrerequisites(getResolvedClasspath());
	}

	public IClasspathEntry[] getResolvedClasspath() throws JavaModelException {
		PerProjectInfo perProjectInfo = getPerProjectInfo();
		IClasspathEntry[] resolvedClasspath = perProjectInfo.getResolvedClasspath();
		if (resolvedClasspath == null) {
			resolveClasspath(perProjectInfo, false/*don't use previous session values*/, true/*add classpath change*/);
			resolvedClasspath = perProjectInfo.getResolvedClasspath();
			if (resolvedClasspath == null) {
				// another thread reset the resolved classpath, use a temporary PerProjectInfo
				PerProjectInfo temporaryInfo = newTemporaryInfo();
				resolveClasspath(temporaryInfo, false/*don't use previous session values*/, true/*add classpath change*/);
				resolvedClasspath = temporaryInfo.getResolvedClasspath();
			}
		}
		return resolvedClasspath;
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedEntry) throws JavaModelException {
		if  (JavaModelManager.getJavaModelManager().isClasspathBeingResolved(this)) {
			if (JavaModelManager.CP_RESOLVE_VERBOSE_ADVANCED)
				verbose_reentering_classpath_resolution();
		    return RESOLUTION_IN_PROGRESS;
		}
		PerProjectInfo perProjectInfo = getPerProjectInfo();

		// use synchronized block to ensure consistency
		IClasspathEntry[] resolvedClasspath;
		IJavaModelStatus unresolvedEntryStatus;
		synchronized (perProjectInfo) {
			resolvedClasspath = perProjectInfo.getResolvedClasspath();
			unresolvedEntryStatus = perProjectInfo.unresolvedEntryStatus;
		}

		if (resolvedClasspath == null
				|| (unresolvedEntryStatus != null && !unresolvedEntryStatus.isOK())) { // force resolution to ensure initializers are run again
			resolveClasspath(perProjectInfo, false/*don't use previous session values*/, true/*add classpath change*/);
			synchronized (perProjectInfo) {
				resolvedClasspath = perProjectInfo.getResolvedClasspath();
				unresolvedEntryStatus = perProjectInfo.unresolvedEntryStatus;
			}
			if (resolvedClasspath == null) {
				// another thread reset the resolved classpath, use a temporary PerProjectInfo
				PerProjectInfo temporaryInfo = newTemporaryInfo();
				resolveClasspath(temporaryInfo, false/*don't use previous session values*/, true/*add classpath change*/);
				resolvedClasspath = temporaryInfo.getResolvedClasspath();
				unresolvedEntryStatus = temporaryInfo.unresolvedEntryStatus;
			}
		}
		if (!ignoreUnresolvedEntry && unresolvedEntryStatus != null && !unresolvedEntryStatus.isOK())
			throw new JavaModelException(unresolvedEntryStatus);
		return resolvedClasspath;
	}

	private void verbose_reentering_classpath_resolution() {
		Util.verbose(
			"CPResolution: reentering raw classpath resolution, will use empty classpath instead" + //$NON-NLS-1$
			"	project: " + getElementName() + '\n' + //$NON-NLS-1$
			"	invocation stack trace:"); //$NON-NLS-1$
		new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
	}

	/**
	 * @see IJavaElement
	 */
	public IResource resource(PackageFragmentRoot root) {
		return this.project;
	}

	/**
	 * Retrieve a shared property on a project. If the property is not defined, answers null.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 *
	 * @param key String
	 * @see JavaProject#setSharedProperty(String, String)
	 * @return String
	 * @throws CoreException
	 */
	public String getSharedProperty(String key) throws CoreException {

		String property = null;
		IFile rscFile = this.project.getFile(key);
		if (rscFile.exists()) {
			byte[] bytes = Util.getResourceContentsAsByteArray(rscFile);
			try {
				property = new String(bytes, org.eclipse.jdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
			} catch (UnsupportedEncodingException e) {
				Util.log(e, "Could not read .classpath with UTF-8 encoding"); //$NON-NLS-1$
				// fallback to default
				property = new String(bytes);
			}
		} else {
			// when a project is imported, we get a first delta for the addition of the .project, but the .classpath is not accessible
			// so default to using java.io.File
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96258
			URI location = rscFile.getLocationURI();
			if (location != null) {
				File file = Util.toLocalFile(location, null/*no progress monitor available*/);
				if (file != null && file.exists()) {
					byte[] bytes;
					try {
						bytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(file);
					} catch (IOException e) {
						return null;
					}
					try {
						property = new String(bytes, org.eclipse.jdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
					} catch (UnsupportedEncodingException e) {
						Util.log(e, "Could not read .classpath with UTF-8 encoding"); //$NON-NLS-1$
						// fallback to default
						property = new String(bytes);
					}
				}
			}
		}
		return property;
	}

	/**
	 * @see JavaElement
	 */
	public SourceMapper getSourceMapper() {

		return null;
	}

	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		if (!exists()) throw newNotPresentException();
		return this.project;
	}

	/**
	 * @see IJavaProject
	 */
	public boolean hasBuildState() {

		return JavaModelManager.getJavaModelManager().getLastBuiltState(this.project, null) != null;
	}

	/**
	 * @see IJavaProject
	 */
	public boolean hasClasspathCycle(IClasspathEntry[] preferredClasspath) {
		LinkedHashSet cycleParticipants = new LinkedHashSet();
		HashMap preferredClasspaths = new HashMap(1);
		preferredClasspaths.put(this, preferredClasspath);
		updateCycleParticipants(new ArrayList(2), cycleParticipants, ResourcesPlugin.getWorkspace().getRoot(), new HashSet(2), preferredClasspaths);
		return !cycleParticipants.isEmpty();
	}

	public boolean hasCycleMarker(){
		return getCycleMarker() != null;
	}

	public int hashCode() {
		return this.project.hashCode();
	}
	
	private boolean hasUTF8BOM(byte[] bytes) {
		if (bytes.length > IContentDescription.BOM_UTF_8.length) {
			for (int i = 0, length = IContentDescription.BOM_UTF_8.length; i < length; i++) {
				if (IContentDescription.BOM_UTF_8[i] != bytes[i])
					return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Answers true if the project potentially contains any source. A project which has no source is immutable.
	 * @return boolean
	 */
	public boolean hasSource() {

		// look if any source folder on the classpath
		// no need for resolved path given source folder cannot be abstracted
		IClasspathEntry[] entries;
		try {
			entries = getRawClasspath();
		} catch (JavaModelException e) {
			return true; // unsure
		}
		for (int i = 0, max = entries.length; i < max; i++) {
			if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				return true;
			}
		}
		return false;
	}



	/*
	 * @see IJavaProject
	 */
	public boolean isOnClasspath(IJavaElement element) {
		IClasspathEntry[] rawClasspath;
		try {
			rawClasspath = getRawClasspath();
		} catch(JavaModelException e){
			return false; // not a Java project
		}
		int elementType = element.getElementType();
		boolean isPackageFragmentRoot = false;
		boolean isFolderPath = false;
		boolean isSource = false;
		switch (elementType) {
			case IJavaElement.JAVA_MODEL:
				return false;
			case IJavaElement.JAVA_PROJECT:
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				isPackageFragmentRoot = true;
				break;
			case IJavaElement.PACKAGE_FRAGMENT:
				isFolderPath = !((IPackageFragmentRoot)element.getParent()).isArchive();
				break;
			case IJavaElement.COMPILATION_UNIT:
				isSource = true;
				break;
			default:
				isSource = element.getAncestor(IJavaElement.COMPILATION_UNIT) != null;
				break;
		}
		IPath elementPath = element.getPath();

		// first look at unresolved entries
		int length = rawClasspath.length;
		for (int i = 0; i < length; i++) {
			IClasspathEntry entry = rawClasspath[i];
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_LIBRARY:
				case IClasspathEntry.CPE_PROJECT:
				case IClasspathEntry.CPE_SOURCE:
					if (isOnClasspathEntry(elementPath, isFolderPath, isPackageFragmentRoot, entry))
						return true;
					break;
			}
		}

		// no need to go further for compilation units and elements inside a compilation unit
		// it can only be in a source folder, thus on the raw classpath
		if (isSource)
			return false;

		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=304081
		// All the resolved classpath entries need to be considered, including the referenced classpath entries
		IClasspathEntry[] resolvedClasspath = null;
		try {
			resolvedClasspath = getResolvedClasspath();
		} catch (JavaModelException e) {
			return false; // Perhaps, not a Java project
		}

		for (int index = 0; index < resolvedClasspath.length; index++) {
			if (isOnClasspathEntry(elementPath, isFolderPath, isPackageFragmentRoot, resolvedClasspath[index]))
				return true;
		}
		
		return false;
	}

	/*
	 * @see IJavaProject
	 */
	public boolean isOnClasspath(IResource resource) {
		IPath exactPath = resource.getFullPath();
		IPath path = exactPath;

		// ensure that folders are only excluded if all of their children are excluded
		int resourceType = resource.getType();
		boolean isFolderPath = resourceType == IResource.FOLDER || resourceType == IResource.PROJECT;

		IClasspathEntry[] classpath;
		try {
			classpath = this.getResolvedClasspath();
		} catch(JavaModelException e){
			return false; // not a Java project
		}
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			IPath entryPath = entry.getPath();
			if (entryPath.equals(exactPath)) { // package fragment roots must match exactly entry pathes (no exclusion there)
				return true;
			}
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=276373
			// When a classpath entry is absolute, convert the resource's relative path to a file system path and compare
			// e.g - /P/lib/variableLib.jar and /home/P/lib/variableLib.jar when compared should return true
			if (entryPath.isAbsolute()
					&& entryPath.equals(ResourcesPlugin.getWorkspace().getRoot().getLocation().append(exactPath))) {
				return true;
			}
			if (entryPath.isPrefixOf(path)
					&& !Util.isExcluded(path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), isFolderPath)) {
				return true;
			}
		}
		return false;
	}

	private boolean isOnClasspathEntry(IPath elementPath, boolean isFolderPath, boolean isPackageFragmentRoot, IClasspathEntry entry) {
		IPath entryPath = entry.getPath();
		if (isPackageFragmentRoot) {
			// package fragment roots must match exactly entry pathes (no exclusion there)
			if (entryPath.equals(elementPath))
				return true;
		} else {
			if (entryPath.isPrefixOf(elementPath)
					&& !Util.isExcluded(elementPath, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), isFolderPath))
				return true;
		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=276373
		if (entryPath.isAbsolute()
				&& entryPath.equals(ResourcesPlugin.getWorkspace().getRoot().getLocation().append(elementPath))) {
			return true;
		}
		return false;
	}

	/**
	 * load preferences from a shareable format (VCM-wise)
	 */
	 private IEclipsePreferences loadPreferences() {

	 	IEclipsePreferences preferences = null;
	 	IPath projectMetaLocation = getPluginWorkingLocation();
		if (projectMetaLocation != null) {
			File prefFile = projectMetaLocation.append(PREF_FILENAME).toFile();
			if (prefFile.exists()) { // load preferences from file
				InputStream in = null;
				try {
					in = new BufferedInputStream(new FileInputStream(prefFile));
					preferences = Platform.getPreferencesService().readPreferences(in);
				} catch (CoreException e) { // problems loading preference store - quietly ignore
				} catch (IOException e) { // problems loading preference store - quietly ignore
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) { // ignore problems with close
						}
					}
				}
				// one shot read, delete old preferences
				prefFile.delete();
				return preferences;
			}
		}
		return null;
	 }

	/**
	 * @see IJavaProject#newEvaluationContext()
	 */
	public IEvaluationContext newEvaluationContext() {
		EvaluationContext context = new EvaluationContext();
		context.setLineSeparator(Util.getLineSeparator(null/*no existing source*/, this));
		return new EvaluationContextWrapper(context, this);
	}

	/*
	 * Returns a new name lookup. This name lookup first looks in the given working copies.
	 */
	public NameLookup newNameLookup(ICompilationUnit[] workingCopies) throws JavaModelException {
		return getJavaProjectElementInfo().newNameLookup(this, workingCopies);
	}

	/*
	 * Returns a new name lookup. This name lookup first looks in the working copies of the given owner.
	 */
	public NameLookup newNameLookup(WorkingCopyOwner owner) throws JavaModelException {

		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		ICompilationUnit[] workingCopies = owner == null ? null : manager.getWorkingCopies(owner, true/*add primary WCs*/);
		return newNameLookup(workingCopies);
	}

	/*
	 * Returns a new search name environment for this project. This name environment first looks in the given working copies.
	 */
	public SearchableEnvironment newSearchableNameEnvironment(ICompilationUnit[] workingCopies) throws JavaModelException {
		return new SearchableEnvironment(this, workingCopies);
	}

	/*
	 * Returns a new search name environment for this project. This name environment first looks in the working copies
	 * of the given owner.
	 */
	public SearchableEnvironment newSearchableNameEnvironment(WorkingCopyOwner owner) throws JavaModelException {
		return new SearchableEnvironment(this, owner);
	}

	/*
	 * Returns a PerProjectInfo that doesn't register classpath change
	 * and that should be used as a temporary info.
	 */
	public PerProjectInfo newTemporaryInfo() {
		return 
			new PerProjectInfo(this.project.getProject()) {
				protected ClasspathChange addClasspathChange() {
					return null;
				}
		};
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException {

		return newTypeHierarchy(region, DefaultWorkingCopyOwner.PRIMARY, monitor);
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IRegion region,
		WorkingCopyOwner owner,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (region == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullRegion);
		}
		ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(region, workingCopies, null, true);
		op.runOperation(monitor);
		return op.getResult();
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException {

		return newTypeHierarchy(type, region, DefaultWorkingCopyOwner.PRIMARY, monitor);
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		WorkingCopyOwner owner,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (type == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullFocusType);
		}
		if (region == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullRegion);
		}
		ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(region, workingCopies, type, true/*compute subtypes*/);
		op.runOperation(monitor);
		return op.getResult();
	}
	public String[] projectPrerequisites(IClasspathEntry[] resolvedClasspath)
		throws JavaModelException {

		ArrayList prerequisites = new ArrayList();
		for (int i = 0, length = resolvedClasspath.length; i < length; i++) {
			IClasspathEntry entry = resolvedClasspath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				prerequisites.add(entry.getPath().lastSegment());
			}
		}
		int size = prerequisites.size();
		if (size == 0) {
			return NO_PREREQUISITES;
		} else {
			String[] result = new String[size];
			prerequisites.toArray(result);
			return result;
		}
	}
	/**
	 * Reads the classpath file entries of this project's .classpath file.
	 * Returns a two-dimensional array, where the number of elements in the row is fixed to 2.
	 * The first element is an array of raw classpath entries, which includes the output entry,
	 * and the second element is an array of referenced entries that may have been stored 
	 * by the client earlier. 
	 * See {@link IJavaProject#getReferencedClasspathEntries()} for more details.
	 * As a side effect, unknown elements are stored in the given map (if not null)
	 * Throws exceptions if the file cannot be accessed or is malformed.
	 */
	public IClasspathEntry[][] readFileEntriesWithException(Map unknownElements) throws CoreException, IOException, ClasspathEntry.AssertionFailedException {
		IFile rscFile = this.project.getFile(JavaProject.CLASSPATH_FILENAME);
		byte[] bytes;
		if (rscFile.exists()) {
			bytes = Util.getResourceContentsAsByteArray(rscFile);
		} else {
			// when a project is imported, we get a first delta for the addition of the .project, but the .classpath is not accessible
			// so default to using java.io.File
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96258
			URI location = rscFile.getLocationURI();
			if (location == null)
				throw new IOException("Cannot obtain a location URI for " + rscFile); //$NON-NLS-1$
			File file = Util.toLocalFile(location, null/*no progress monitor available*/);
			if (file == null)
				throw new IOException("Unable to fetch file from " + location); //$NON-NLS-1$
			try {
				bytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(file);
			} catch (IOException e) {
				if (!file.exists())
					return new IClasspathEntry[][]{defaultClasspath(), ClasspathEntry.NO_ENTRIES};
				throw e;
			}
		}
		if (hasUTF8BOM(bytes)) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=240034
			int length = bytes.length-IContentDescription.BOM_UTF_8.length;
			System.arraycopy(bytes, IContentDescription.BOM_UTF_8.length, bytes = new byte[length], 0, length);
		}
		String xmlClasspath;
		try {
			xmlClasspath = new String(bytes, org.eclipse.jdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
		} catch (UnsupportedEncodingException e) {
			Util.log(e, "Could not read .classpath with UTF-8 encoding"); //$NON-NLS-1$
			// fallback to default
			xmlClasspath = new String(bytes);
		}
		return decodeClasspath(xmlClasspath, unknownElements);
	}

	/*
	 * Reads the classpath file entries of this project's .classpath file.
	 * This includes the output entry.
	 * As a side effect, unknown elements are stored in the given map (if not null)
	 */
	private IClasspathEntry[][] readFileEntries(Map unkwownElements) {
		try {
			return readFileEntriesWithException(unkwownElements);
		} catch (CoreException e) {
			Util.log(e, "Exception while reading " + getPath().append(JavaProject.CLASSPATH_FILENAME)); //$NON-NLS-1$
			return new IClasspathEntry[][]{JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES};
		} catch (IOException e) {
			Util.log(e, "Exception while reading " + getPath().append(JavaProject.CLASSPATH_FILENAME)); //$NON-NLS-1$
			return new IClasspathEntry[][]{JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES};
		} catch (ClasspathEntry.AssertionFailedException e) {
			Util.log(e, "Exception while reading " + getPath().append(JavaProject.CLASSPATH_FILENAME)); //$NON-NLS-1$
			return new IClasspathEntry[][]{JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES};
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPath readOutputLocation() {
		// Read classpath file without creating markers nor logging problems
		IClasspathEntry[][] classpath = readFileEntries(null/*not interested in unknown elements*/);
		if (classpath[0] == JavaProject.INVALID_CLASSPATH)
			return defaultOutputLocation();

		// extract the output location
		IPath outputLocation = null;
		if (classpath[0].length > 0) {
			IClasspathEntry entry = classpath[0][classpath[0].length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				outputLocation = entry.getPath();
			}
		}
		return outputLocation;
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] readRawClasspath() {
		// Read classpath file without creating markers nor logging problems
		IClasspathEntry[][] classpath = readFileEntries(null/*not interested in unknown elements*/);
		if (classpath[0] == JavaProject.INVALID_CLASSPATH)
			return defaultClasspath();

		// discard the output location
		if (classpath[0].length > 0) {
			IClasspathEntry entry = classpath[0][classpath[0].length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				IClasspathEntry[] copy = new IClasspathEntry[classpath[0].length - 1];
				System.arraycopy(classpath[0], 0, copy, 0, copy.length);
				classpath[0] = copy;
			}
		}
		return classpath[0];
	}

	/**
	 * Removes the given builder from the build spec for the given project.
	 */
	protected void removeFromBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = this.project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				this.project.setDescription(description, null);
				return;
			}
		}
	}

	/*
	 * Resets this project's caches
	 */
	public void resetCaches() {
		JavaProjectElementInfo info = (JavaProjectElementInfo) JavaModelManager.getJavaModelManager().peekAtInfo(this);
		if (info != null){
			info.resetCaches();
		}
	}

	public ClasspathChange resetResolvedClasspath() {
		try {
			return getPerProjectInfo().resetResolvedClasspath();
		} catch (JavaModelException e) {
			// project doesn't exist
			return null;
		}
	}		
	
	/*
	 * Resolve the given raw classpath.
	 */
	public IClasspathEntry[] resolveClasspath(IClasspathEntry[] rawClasspath) throws JavaModelException {
		return resolveClasspath(rawClasspath, false/*don't use previous session*/, true/*resolve chained libraries*/).resolvedClasspath;
	}
	
	static class ResolvedClasspath {
		IClasspathEntry[] resolvedClasspath;
		IJavaModelStatus unresolvedEntryStatus = JavaModelStatus.VERIFIED_OK;
		HashMap rawReverseMap = new HashMap();
		Map rootPathToResolvedEntries = new HashMap();
		IClasspathEntry[] referencedEntries = null;
	}
	
	public ResolvedClasspath resolveClasspath(IClasspathEntry[] rawClasspath, boolean usePreviousSession, boolean resolveChainedLibraries) throws JavaModelException {
		return resolveClasspath(rawClasspath, null, usePreviousSession, resolveChainedLibraries);
	}

	public ResolvedClasspath resolveClasspath(IClasspathEntry[] rawClasspath, IClasspathEntry[] referencedEntries, boolean usePreviousSession, boolean resolveChainedLibraries) throws JavaModelException {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		ExternalFoldersManager externalFoldersManager = JavaModelManager.getExternalManager();
		ResolvedClasspath result = new ResolvedClasspath();
		Map knownDrives = new HashMap();

		Map referencedEntriesMap = new HashMap();
		List rawLibrariesPath = new ArrayList();
		LinkedHashSet resolvedEntries = new LinkedHashSet();
		
		if(resolveChainedLibraries) {
			for (int index = 0; index < rawClasspath.length; index++) {
				IClasspathEntry currentEntry = rawClasspath[index]; 
				if (currentEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					rawLibrariesPath.add(ClasspathEntry.resolveDotDot(getProject().getLocation(), currentEntry.getPath()));
				}
			}
			if (referencedEntries != null) {
				// The Set is required to keep the order intact while the referencedEntriesMap (Map)
				// is used to map the referenced entries with path
				LinkedHashSet referencedEntriesSet = new LinkedHashSet();
				for (int index = 0; index < referencedEntries.length; index++) {
					IPath path = referencedEntries[index].getPath();
					if (!rawLibrariesPath.contains(path) && referencedEntriesMap.get(path) == null) {
						referencedEntriesMap.put(path, referencedEntries[index]);
						referencedEntriesSet.add(referencedEntries[index]);
					}
				}
				if (referencedEntriesSet.size() > 0) {
					result.referencedEntries = new IClasspathEntry[referencedEntriesSet.size()];
					referencedEntriesSet.toArray(result.referencedEntries);
				}
			}
		}
		
		int length = rawClasspath.length;
		for (int i = 0; i < length; i++) {

			IClasspathEntry rawEntry = rawClasspath[i];
			IClasspathEntry resolvedEntry = rawEntry;

			switch (rawEntry.getEntryKind()){

				case IClasspathEntry.CPE_VARIABLE :
					try {
						resolvedEntry = manager.resolveVariableEntry(rawEntry, usePreviousSession);
					} catch (ClasspathEntry.AssertionFailedException e) {
						// Catch the assertion failure and set status instead
						// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
						result.unresolvedEntryStatus = new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, e.getMessage());
						break;
					}
					if (resolvedEntry == null) {
						result.unresolvedEntryStatus = new JavaModelStatus(IJavaModelStatusConstants.CP_VARIABLE_PATH_UNBOUND, this, rawEntry.getPath());
					} else {
						// If the entry is already present in the rawReversetMap, it means the entry and the chained libraries
						// have already been processed. So, skip it.
						if (resolveChainedLibraries && resolvedEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY
													&& result.rawReverseMap.get(resolvedEntry.getPath()) == null) {
							// resolve Class-Path: in manifest
							ClasspathEntry[] extraEntries = ((ClasspathEntry) resolvedEntry).resolvedChainedLibraries();
							for (int j = 0, length2 = extraEntries.length; j < length2; j++) {
								if (!rawLibrariesPath.contains(extraEntries[j].getPath())) {
									// https://bugs.eclipse.org/bugs/show_bug.cgi?id=305037
									// referenced entries for variable entries could also be persisted with extra attributes, so addAsChainedEntry = true
									addToResult(rawEntry, extraEntries[j], result, resolvedEntries, externalFoldersManager, referencedEntriesMap, true, knownDrives);
								}
							}
						}
						addToResult(rawEntry, resolvedEntry, result, resolvedEntries, externalFoldersManager, referencedEntriesMap, false, knownDrives);
					}
					break;

				case IClasspathEntry.CPE_CONTAINER :
					IClasspathContainer container = usePreviousSession ? manager.getPreviousSessionContainer(rawEntry.getPath(), this) : JavaCore.getClasspathContainer(rawEntry.getPath(), this);
					if (container == null){
						result.unresolvedEntryStatus = new JavaModelStatus(IJavaModelStatusConstants.CP_CONTAINER_PATH_UNBOUND, this, rawEntry.getPath());
						break;
					}

					IClasspathEntry[] containerEntries = container.getClasspathEntries();
					if (containerEntries == null) {
						if (JavaModelManager.CP_RESOLVE_VERBOSE || JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
							JavaModelManager.getJavaModelManager().verbose_missbehaving_container_null_entries(this, rawEntry.getPath());
						}
						break;
					}

					// container was bound
					for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++){
						ClasspathEntry cEntry = (ClasspathEntry) containerEntries[j];
						if (cEntry == null) {
							if (JavaModelManager.CP_RESOLVE_VERBOSE || JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
								JavaModelManager.getJavaModelManager().verbose_missbehaving_container(this, rawEntry.getPath(), containerEntries);
							}
							break;
						}
						// if container is exported or restricted, then its nested entries must in turn be exported  (21749) and/or propagate restrictions
						cEntry = cEntry.combineWith((ClasspathEntry) rawEntry);
						
						if (cEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
							// resolve ".." in library path
							cEntry = cEntry.resolvedDotDot(getProject().getLocation());
							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=313965
							// Do not resolve if the system attribute is set to false	
							if (resolveChainedLibraries
									&& JavaModelManager.getJavaModelManager().resolveReferencedLibrariesForContainers
									&& result.rawReverseMap.get(cEntry.getPath()) == null) {
								// resolve Class-Path: in manifest
								ClasspathEntry[] extraEntries = cEntry.resolvedChainedLibraries();
								for (int k = 0, length2 = extraEntries.length; k < length2; k++) {
									if (!rawLibrariesPath.contains(extraEntries[k].getPath())) {
										addToResult(rawEntry, extraEntries[k], result, resolvedEntries, externalFoldersManager, referencedEntriesMap, false, knownDrives);
									}
								}
							}
						}
						addToResult(rawEntry, cEntry, result, resolvedEntries, externalFoldersManager, referencedEntriesMap, false, knownDrives);
					}
					break;

				case IClasspathEntry.CPE_LIBRARY:
					// resolve ".." in library path
					resolvedEntry = ((ClasspathEntry) rawEntry).resolvedDotDot(getProject().getLocation());
					
					if (resolveChainedLibraries && result.rawReverseMap.get(resolvedEntry.getPath()) == null) {
						// resolve Class-Path: in manifest
						ClasspathEntry[] extraEntries = ((ClasspathEntry) resolvedEntry).resolvedChainedLibraries();
						for (int k = 0, length2 = extraEntries.length; k < length2; k++) {
							if (!rawLibrariesPath.contains(extraEntries[k].getPath())) {
								addToResult(rawEntry, extraEntries[k], result, resolvedEntries, externalFoldersManager, referencedEntriesMap, true, knownDrives);
							}
						}
					}

					addToResult(rawEntry, resolvedEntry, result, resolvedEntries, externalFoldersManager, referencedEntriesMap, false, knownDrives);
					break;
				default :
					addToResult(rawEntry, resolvedEntry, result, resolvedEntries, externalFoldersManager, referencedEntriesMap, false, knownDrives);
					break;
			}
		}
		result.resolvedClasspath = new IClasspathEntry[resolvedEntries.size()];
		resolvedEntries.toArray(result.resolvedClasspath);
		return result;
	}

	private void addToResult(IClasspathEntry rawEntry, IClasspathEntry resolvedEntry, ResolvedClasspath result,
			LinkedHashSet resolvedEntries, ExternalFoldersManager externalFoldersManager,
			Map oldChainedEntriesMap, boolean addAsChainedEntry, Map knownDrives) {

		IPath resolvedPath;
		// If it's already been resolved, do not add to resolvedEntries
		if (result.rawReverseMap.get(resolvedPath = resolvedEntry.getPath()) == null) {
			result.rawReverseMap.put(resolvedPath, rawEntry);
			result.rootPathToResolvedEntries.put(resolvedPath, resolvedEntry);
			resolvedEntries.add(resolvedEntry);
			if (addAsChainedEntry) {
				IClasspathEntry chainedEntry = null;
				chainedEntry = (ClasspathEntry) oldChainedEntriesMap.get(resolvedPath);
				if (chainedEntry != null) {
					// This is required to keep the attributes if any added by the user in
					// the previous session such as source attachment path etc.
					copyFromOldChainedEntry((ClasspathEntry) resolvedEntry, (ClasspathEntry) chainedEntry);
				}
			}
		}
		if (resolvedEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY && ExternalFoldersManager.isExternalFolderPath(resolvedPath)) {
			externalFoldersManager.addFolder(resolvedPath, true/*scheduleForCreation*/); // no-op if not an external folder or if already registered
		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336046
		// The source attachment path could be external too and in which case, must be added.
		IPath sourcePath = resolvedEntry.getSourceAttachmentPath();
		if (sourcePath != null && driveExists(sourcePath, knownDrives) && ExternalFoldersManager.isExternalFolderPath(sourcePath)) {
			externalFoldersManager.addFolder(sourcePath, true);
		}
	}

	private void copyFromOldChainedEntry(ClasspathEntry resolvedEntry, ClasspathEntry chainedEntry) {
		IPath path = chainedEntry.getSourceAttachmentPath();
		if ( path != null) {
			resolvedEntry.sourceAttachmentPath = path;
		}
		path = chainedEntry.getSourceAttachmentRootPath();
		if (path != null) {
			resolvedEntry.sourceAttachmentRootPath = path;
		}
		IClasspathAttribute[] attributes = chainedEntry.getExtraAttributes();
		if (attributes != null) {
			resolvedEntry.extraAttributes = attributes;
		}
	}
	
	/*
	 * File#exists() takes lot of time for an unmapped drive. Hence, cache the info.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=338649
	 */
	private boolean driveExists(IPath sourcePath, Map knownDrives) {	
		String drive = sourcePath.getDevice();
		if (drive == null) return true;
		Boolean good = (Boolean)knownDrives.get(drive);
		if (good == null) {
			if (new File(drive).exists()) {
				knownDrives.put(drive, Boolean.TRUE);
				return true;
			} else {
				knownDrives.put(drive, Boolean.FALSE);
				return false;
			}
		}
		return good.booleanValue();
	}
	
	/*
	 * Resolve the given perProjectInfo's raw classpath and store the resolved classpath in the perProjectInfo.
	 */
	public void resolveClasspath(PerProjectInfo perProjectInfo, boolean usePreviousSession, boolean addClasspathChange) throws JavaModelException {
		if (CP_RESOLUTION_BP_LISTENERS != null)
			breakpoint(1, this);
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		boolean isClasspathBeingResolved = manager.isClasspathBeingResolved(this);
		try {
			if (!isClasspathBeingResolved) {
				manager.setClasspathBeingResolved(this, true);
			}

			// get raw info inside a synchronized block to ensure that it is consistent
			IClasspathEntry[][] classpath = new IClasspathEntry[2][];
			int timeStamp;
			synchronized (perProjectInfo) {
				classpath[0] = perProjectInfo.rawClasspath;
				classpath[1] = perProjectInfo.referencedEntries;
				// Checking null only for rawClasspath enough
				if (classpath[0] == null)
					classpath = perProjectInfo.readAndCacheClasspath(this);
				timeStamp = perProjectInfo.rawTimeStamp;
			}

			ResolvedClasspath result = resolveClasspath(classpath[0], classpath[1], usePreviousSession, true/*resolve chained libraries*/);
			
			if (CP_RESOLUTION_BP_LISTENERS != null)
				breakpoint(2, this);

			// store resolved info along with the raw info to ensure consistency
			perProjectInfo.setResolvedClasspath(result.resolvedClasspath, result.referencedEntries, result.rawReverseMap, result.rootPathToResolvedEntries, usePreviousSession ? PerProjectInfo.NEED_RESOLUTION : result.unresolvedEntryStatus, timeStamp, addClasspathChange);
		} finally {
			if (!isClasspathBeingResolved) {
				manager.setClasspathBeingResolved(this, false);
			}
			if (CP_RESOLUTION_BP_LISTENERS != null)
				breakpoint(3, this);
		}
	}
	
	/**
	 * Answers an ID which is used to distinguish project/entries during package
	 * fragment root computations
	 * @return String
	 */
	public String rootID(){
		return "[PRJ]"+this.project.getFullPath(); //$NON-NLS-1$
	}

	/**
	 * Writes the classpath in a sharable format (VCM-wise) only when necessary, that is, if  it is semantically different
	 * from the existing one in file. Will never write an identical one.
	 *
	 * @param newClasspath IClasspathEntry[]
	 * @param newOutputLocation IPath
	 * @return boolean Return whether the .classpath file was modified.
	 * @throws JavaModelException
	 */
	public boolean writeFileEntries(IClasspathEntry[] newClasspath, IClasspathEntry[] referencedEntries, IPath newOutputLocation) throws JavaModelException {

		if (!this.project.isAccessible()) return false;

		Map unknownElements = new HashMap();
		IClasspathEntry[][] fileEntries = readFileEntries(unknownElements);
		if (fileEntries[0] != JavaProject.INVALID_CLASSPATH && 
				areClasspathsEqual(newClasspath, newOutputLocation, fileEntries[0])
				&& (referencedEntries == null || areClasspathsEqual(referencedEntries, fileEntries[1])) ) {
			// no need to save it, it is the same
			return false;
		}

		// actual file saving
		try {
			setSharedProperty(JavaProject.CLASSPATH_FILENAME, encodeClasspath(newClasspath, referencedEntries, newOutputLocation, true, unknownElements));
			return true;
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	public boolean writeFileEntries(IClasspathEntry[] newClasspath, IPath newOutputLocation) throws JavaModelException {
		return writeFileEntries(newClasspath, ClasspathEntry.NO_ENTRIES, newOutputLocation);
	}

	/**
	 * Update the Java command in the build spec (replace existing one if present,
	 * add one first if none).
	 */
	private void setJavaCommand(
		IProjectDescription description,
		ICommand newCommand)
		throws CoreException {

		ICommand[] oldBuildSpec = description.getBuildSpec();
		int oldJavaCommandIndex = getJavaCommandIndex(oldBuildSpec);
		ICommand[] newCommands;

		if (oldJavaCommandIndex == -1) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldBuildSpec.length + 1];
			System.arraycopy(oldBuildSpec, 0, newCommands, 1, oldBuildSpec.length);
			newCommands[0] = newCommand;
		} else {
		    oldBuildSpec[oldJavaCommandIndex] = newCommand;
			newCommands = oldBuildSpec;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		this.project.setDescription(description, null);
	}

	/**
	 * @see org.eclipse.jdt.core.IJavaProject#setOption(java.lang.String, java.lang.String)
	 */
	public void setOption(String optionName, String optionValue) {
		// Store option value
		IEclipsePreferences projectPreferences = getEclipsePreferences();
		boolean modified = JavaModelManager.getJavaModelManager().storePreference(optionName, optionValue, projectPreferences, null);

		// Write changes
		if (modified) {
		try {
			projectPreferences.flush();
		} catch (BackingStoreException e) {
			// problem with pref store - quietly ignore
		}
	}
	}

	/**
	 * @see org.eclipse.jdt.core.IJavaProject#setOptions(Map)
	 */
	public void setOptions(Map newOptions) {

		IEclipsePreferences projectPreferences = getEclipsePreferences();
		if (projectPreferences == null) return;
		try {
			if (newOptions == null){
				projectPreferences.clear();
			} else {
				Iterator entries = newOptions.entrySet().iterator();
				JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
				while (entries.hasNext()){
					Map.Entry entry = (Map.Entry) entries.next();
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					javaModelManager.storePreference(key, value, projectPreferences, newOptions);
				}

				// reset to default all options not in new map
				// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=26255
				// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=49691
				String[] pNames = projectPreferences.keys();
				int ln = pNames.length;
				for (int i=0; i<ln; i++) {
					String key = pNames[i];
					if (!newOptions.containsKey(key)) {
						projectPreferences.remove(key); // old preferences => remove from preferences table
					}
				}
			}

			// persist options
			projectPreferences.flush();

			// flush cache immediately
			try {
				getPerProjectInfo().options = null;
			} catch (JavaModelException e) {
				// do nothing
			}
		} catch (BackingStoreException e) {
			// problem with pref store - quietly ignore
		}
	}
	/**
	 * @see IJavaProject
	 */
	public void setOutputLocation(IPath path, IProgressMonitor monitor) throws JavaModelException {
		if (path == null) {
			throw new IllegalArgumentException(Messages.path_nullPath);
		}
		if (path.equals(getOutputLocation())) {
			return;
		}
		setRawClasspath(getRawClasspath(), path, monitor);
	}

	/**
	 * Sets the underlying kernel project of this Java project,
	 * and fills in its parent and name.
	 * Called by IProject.getNature().
	 *
	 * @see IProjectNature#setProject(IProject)
	 */
	public void setProject(IProject project) {

		this.project = project;
		this.parent = JavaModelManager.getJavaModelManager().getJavaModel();
	}

	/**
	 * @see IJavaProject#setRawClasspath(IClasspathEntry[],boolean,IProgressMonitor)
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		boolean canModifyResources,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries,
			getOutputLocation()/*don't change output*/,
			canModifyResources,
			monitor);
	}

	/**
	 * @see IJavaProject#setRawClasspath(IClasspathEntry[],IPath,boolean,IProgressMonitor)
	 */
	public void setRawClasspath(
			IClasspathEntry[] newRawClasspath,
			IPath newOutputLocation,
			boolean canModifyResources,
			IProgressMonitor monitor)
			throws JavaModelException {
		setRawClasspath(newRawClasspath, null, newOutputLocation, canModifyResources, monitor);
	}

	/**
	 * @see IJavaProject#setRawClasspath(IClasspathEntry[],IPath,IProgressMonitor)
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IPath outputLocation,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries,
			outputLocation,
			true/*can change resource (as per API contract)*/,
			monitor);
	}
	
	public void setRawClasspath(IClasspathEntry[] entries, IClasspathEntry[] referencedEntries, IPath outputLocation,
			IProgressMonitor monitor) throws JavaModelException {
		setRawClasspath(entries, referencedEntries, outputLocation, true, monitor);
	}
	
	protected void setRawClasspath(IClasspathEntry[] newRawClasspath, IClasspathEntry[] referencedEntries, IPath newOutputLocation,
			boolean canModifyResources,	IProgressMonitor monitor) throws JavaModelException {

		try {
			if (newRawClasspath == null) //are we already with the default classpath
				newRawClasspath = defaultClasspath();

			SetClasspathOperation op =
				new SetClasspathOperation(
					this,
					newRawClasspath,
					referencedEntries,
					newOutputLocation,
					canModifyResources);
			op.runOperation(monitor);
		} catch (JavaModelException e) {
			JavaModelManager.getJavaModelManager().getDeltaProcessor().flush();
			throw e;
		}
	}

	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries,
			getOutputLocation()/*don't change output*/,
			true/*can change resource (as per API contract)*/,
			monitor);
	}

	/**
	 * Record a shared persistent property onto a project.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 * <p>
	 * Shared properties end up in resource files, and thus cannot be modified during
	 * delta notifications (a CoreException would then be thrown).
	 *
	 * @param key String
	 * @param value String
	 * @see JavaProject#getSharedProperty(String key)
	 * @throws CoreException
	 */
	public void setSharedProperty(String key, String value) throws CoreException {

		IFile rscFile = this.project.getFile(key);
		byte[] bytes = null;
		try {
			bytes = value.getBytes(org.eclipse.jdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
		} catch (UnsupportedEncodingException e) {
			Util.log(e, "Could not write .classpath with UTF-8 encoding "); //$NON-NLS-1$
			// fallback to default
			bytes = value.getBytes();
		}
		InputStream inputStream = new ByteArrayInputStream(bytes);
		// update the resource content
		if (rscFile.exists()) {
			if (rscFile.isReadOnly()) {
				// provide opportunity to checkout read-only .classpath file (23984)
				ResourcesPlugin.getWorkspace().validateEdit(new IFile[]{rscFile}, IWorkspace.VALIDATE_PROMPT);
			}
			rscFile.setContents(inputStream, IResource.FORCE, null);
		} else {
			rscFile.create(inputStream, IResource.FORCE, null);
		}
	}

	/**
	 * If a cycle is detected, then cycleParticipants contains all the paths of projects involved in this cycle (directly and indirectly),
	 * no cycle if the set is empty (and started empty)
	 * @param prereqChain ArrayList
	 * @param cycleParticipants HashSet
	 * @param workspaceRoot IWorkspaceRoot
	 * @param traversed HashSet
	 * @param preferredClasspaths Map
	 */
	public void updateCycleParticipants(
			ArrayList prereqChain,
			LinkedHashSet cycleParticipants,
			IWorkspaceRoot workspaceRoot,
			HashSet traversed,
			Map preferredClasspaths){

		IPath path = getPath();
		prereqChain.add(path);
		traversed.add(path);
		try {
			IClasspathEntry[] classpath = null;
			if (preferredClasspaths != null) classpath = (IClasspathEntry[])preferredClasspaths.get(this);
			if (classpath == null) classpath = getResolvedClasspath();
			for (int i = 0, length = classpath.length; i < length; i++) {
				IClasspathEntry entry = classpath[i];

				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT){
					IPath prereqProjectPath = entry.getPath();
					int index = cycleParticipants.contains(prereqProjectPath) ? 0 : prereqChain.indexOf(prereqProjectPath);
					if (index >= 0) { // refer to cycle, or in cycle itself
						for (int size = prereqChain.size(); index < size; index++) {
							cycleParticipants.add(prereqChain.get(index));
						}
					} else {
						if (!traversed.contains(prereqProjectPath)) {
							IResource member = workspaceRoot.findMember(prereqProjectPath);
							if (member != null && member.getType() == IResource.PROJECT){
								JavaProject javaProject = (JavaProject)JavaCore.create((IProject)member);
								javaProject.updateCycleParticipants(prereqChain, cycleParticipants, workspaceRoot, traversed, preferredClasspaths);
							}
						}
					}
				}
			}
		} catch(JavaModelException e){
			// project doesn't exist: ignore
		}
		prereqChain.remove(path);
	}

	/*
	 * Update eclipse preferences from old preferences.
	 */
	 private void updatePreferences(IEclipsePreferences preferences) {

	 	IEclipsePreferences oldPreferences = loadPreferences();
	 	if (oldPreferences != null) {
			try {
		 		String[] propertyNames = oldPreferences.childrenNames();
				for (int i = 0; i < propertyNames.length; i++){
					String propertyName = propertyNames[i];
				    String propertyValue = oldPreferences.get(propertyName, ""); //$NON-NLS-1$
				    if (!"".equals(propertyValue)) { //$NON-NLS-1$
					    preferences.put(propertyName, propertyValue);
				    }
				}
				// save immediately new preferences
				preferences.flush();
			} catch (BackingStoreException e) {
				// fails silently
			}
		}
	 }

	protected IStatus validateExistence(IResource underlyingResource) {
		// check whether the java project can be opened
		try {
			if (!((IProject) underlyingResource).hasNature(JavaCore.NATURE_ID))
				return newDoesNotExistStatus();
		} catch (CoreException e) {
			return newDoesNotExistStatus();
		}
		return JavaModelStatus.VERIFIED_OK;
	}
}
