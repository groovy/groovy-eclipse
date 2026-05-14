/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.JarPackageFragmentRootInfo.PackageContent;
import org.eclipse.jdt.internal.core.JavaModelManager.PerProjectInfo;
import org.eclipse.jdt.internal.core.util.DeduplicationUtil;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A package fragment root that corresponds to a .jar or .zip.
 *
 * <p>NOTE: The only visible entries from a .jar or .zip package fragment root
 * are .class files.
 * <p>NOTE: A jar package fragment root may or may not have an associated resource.
 *
 * @see org.eclipse.jdt.core.IPackageFragmentRoot
 * @see org.eclipse.jdt.internal.core.JarPackageFragmentRootInfo
 */
public class JarPackageFragmentRoot extends PackageFragmentRoot {

	/**
	 * The path to the jar file
	 * (a workspace relative path if the jar is internal,
	 * or an OS path if the jar is external)
	 */
	protected final IPath jarPath;

	private volatile boolean knownToBeModuleLess;

	private volatile boolean multiVersion;

	/**
	 * Reflects the extra attributes of the classpath entry declaring this root.
	 * Caution, this field is used in {@link #hashCode()} and {@link #equals(Object)} to avoid overzealous sharing.
	 * Can be null, if lookup via the corresponding classpath entry failed.
	 */
	protected final IClasspathAttribute[] extraAttributes;

	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy
	 * based on a JAR file.
	 */
	public JarPackageFragmentRoot(IResource resource, IPath externalJarPath, JavaProject project, IClasspathAttribute[] attributes) {
		super(resource, project);
		this.jarPath = externalJarPath;
		if (attributes == null) {
			// attributes could either be
			// (1) provided by the caller (particularly when creating from memento),
			// (2) retrieved from the corresponding classpath entry (if a resolved classpath is available).
			// These two cases should cover all normal scenarios, else extraAttributes will be null.
			try {
				PerProjectInfo perProjectInfo = project.getPerProjectInfo();
				synchronized (perProjectInfo) {
					if (perProjectInfo.resolvedClasspath != null && perProjectInfo.unresolvedEntryStatus == JavaModelStatus.VERIFIED_OK) {
						IClasspathEntry classpathEntry = project.getClasspathEntryFor(externalJarPath);
						if (classpathEntry != null)
							attributes = classpathEntry.getExtraAttributes();
					}
				}
			} catch (JavaModelException e) {
				// ignore
			}
		}
		this.extraAttributes = attributes;
	}

	/**
	 * Compute the package fragment children of this package fragment root.
	 * These are all of the directory zip entries, and any directories implied
	 * by the path of class files contained in the jar of this package fragment root.
	 */
	@Override
	protected boolean computeChildren(OpenableElementInfo info, IResource underlyingResource) throws JavaModelException {
		Map<List<String>, PackageContent> rawPackageInfo= new HashMap<>();
		Map<String, String> overridden = new HashMap<>();
		IJavaElement[] children = NO_ELEMENTS;
		try {
			// always create the default package
			rawPackageInfo.put(new ArrayList<>(), new PackageContent());

			Object file = JavaModel.getTarget(this, true);
			long classLevel = Util.getJdkLevel(file);
			String projectCompliance = this.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
			long projectLevel = CompilerOptions.versionToJdkLevel(projectCompliance);
			ZipFile jar = null;
			try {
				jar = getJar();
				String version = "META-INF/versions/";  //$NON-NLS-1$
				List<String> versions = new ArrayList<>();
				if (projectLevel >= ClassFileConstants.JDK9 && jar.getEntry(version) != null) {
					int earliestJavaVersion = ClassFileConstants.MAJOR_VERSION_9;
					long latestJDK = CompilerOptions.versionToJdkLevel(projectCompliance);
					int latestJavaVer = (int) (latestJDK >> 16);

					for(int i = latestJavaVer; i >= earliestJavaVersion; i--) {
						String s = "" + + (i - 44); //$NON-NLS-1$
						String versionPath = version + s;
						if (jar.getEntry(versionPath) != null) {
							versions.add(s);
						}
					}
				}

				String[] supportedVersions = versions.toArray(new String[versions.size()]);
				if (supportedVersions.length > 0) {
					this.multiVersion = true;
				}
				int length = version.length();
				for (Enumeration<? extends ZipEntry> e= jar.entries(); e.hasMoreElements();) {
					ZipEntry member= e.nextElement();
					String name = Util.getEntryName(jar.getName(), member);
					if (name == null)  {
						continue;
					}
					if (this.multiVersion && name.length() > (length + 2) && name.startsWith(version)) {
						int end = name.indexOf('/', length);
						if (end >= name.length()) continue;
						String versionPath = name.substring(0, end);
						String ver = name.substring(length, end);
						if(versions.contains(ver) && org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(name)) {
							name = name.substring(end + 1);
							overridden.put(name, versionPath);
						}
					}
					initRawPackageInfo(rawPackageInfo, getClassNameSubFolder(), name, member.isDirectory(), CompilerOptions.versionFromJdkLevel(classLevel));
				}
			}  finally {
				JavaModelManager.getJavaModelManager().closeZipFile(jar);
			}
			rawPackageInfo = unmodifiableCopy(rawPackageInfo);
			children = createChildren(rawPackageInfo.keySet());
		} catch (ZipException zipex) {
			// malcious ZIP archive, leave the children empty
			Util.log(zipex, "Invalid ZIP archive: " + toStringWithAncestors()); //$NON-NLS-1$
			children = NO_ELEMENTS;
			rawPackageInfo= Map.of();
			overridden = Map.of();
		} catch (CoreException e) {
			if (e.getCause() instanceof ZipException zipex) {
				// not a ZIP archive, leave the children empty
				Util.log(zipex, "Invalid ZIP archive: " + toStringWithAncestors()); //$NON-NLS-1$
				children = NO_ELEMENTS;
				rawPackageInfo= Map.of();
				overridden = Map.of();
			} else if (e instanceof JavaModelException) {
				throw (JavaModelException)e;
			} else {
				throw new JavaModelException(e);
			}
		}
		info.setChildren(children);
		((JarPackageFragmentRootInfo) info).rawPackageInfo = rawPackageInfo;
		((JarPackageFragmentRootInfo) info).overriddenClasses = overridden;
		return true;
	}

	protected IJavaElement[] createChildren(Collection<List<String>> packagenames) {
		// XXX sorting the children is unnecessary by contract - see org.eclipse.jdt.core.IParent#getChildren()
		// but some tests like JavaProjectTests rely on a fixed child order
		ArrayList<String[]> keys = new ArrayList<>(packagenames.stream().map(s->s.toArray(String[]::new)).toList() );
		Collections.sort(keys, Arrays::compare);

		IJavaElement[] children = new IJavaElement[packagenames.size()];
		// loop through all of referenced packages, creating package fragments if necessary
		int index = 0;
		for (String[] pkgName : keys) {
			children[index++] = getPackageFragment(pkgName);
		}
		return children;
	}
	/**
	 * Returns a new element info for this element.
	 */
	@Override
	protected JarPackageFragmentRootInfo createElementInfo() {
		return new JarPackageFragmentRootInfo();
	}
	/**
	 * A Jar is always K_BINARY.
	 */
	@Override
	protected int determineKind(IResource underlyingResource) {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns true if this handle represents the same jar
	 * as the given handle. Two jars are equal if they share
	 * the same zip file.
	 *
	 * @see Object#equals
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof JarPackageFragmentRoot other) {
			return this.jarPath.equals(other.jarPath) && Arrays.equals(this.extraAttributes, other.extraAttributes);
		}
		return false;
	}

	@Override
	protected int calculateHashCode() {
		return this.jarPath.hashCode() + Arrays.hashCode(this.extraAttributes);
	}

	@Override
	public String getElementName() {
		return this.jarPath.lastSegment();
	}
	/**
	 * Returns the underlying ZipFile for this Jar package fragment root.
	 *
	 * @exception CoreException if an error occurs accessing the jar
	 */
	public ZipFile getJar() throws CoreException {
		return JavaModelManager.getJavaModelManager().getZipFile(getPath());
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	@Override
	public int getKind() {
		return IPackageFragmentRoot.K_BINARY;
	}
	@Override
	int internalKind() throws JavaModelException {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	@Override
	public Object[] getNonJavaResources() throws JavaModelException {
		// We want to show non java resources of the default package at the root (see PR #1G58NB8)
		Object[] defaultPkgResources =  ((JarPackageFragment) getPackageFragment(CharOperation.NO_STRINGS)).storedNonJavaResources();
		int length = defaultPkgResources.length;
		if (length == 0)
			return defaultPkgResources;
		Object[] nonJavaResources = new Object[length];
		for (int i = 0; i < length; i++) {
			JarEntryResource nonJavaResource = (JarEntryResource) defaultPkgResources[i];
			nonJavaResources[i] = nonJavaResource.clone(this);
		}
		return nonJavaResources;
	}
	@Override
	public PackageFragment getPackageFragment(String[] pkgName) {
		return new JarPackageFragment(this, pkgName);
	}
	@Override
	public PackageFragment getPackageFragment(String[] pkgName, String mod) {
		return new JarPackageFragment(this, pkgName); // Overridden in JImageModuleFragmentBridge
	}

	@Override
	public String getClassFilePath(String classname) {
		if (this.multiVersion) {
			JarPackageFragmentRootInfo elementInfo;
			try {
				elementInfo = (JarPackageFragmentRootInfo) getElementInfo();
				String versionPath = elementInfo.overriddenClasses.get(classname);
				return versionPath == null ? classname : versionPath + '/' + classname;
			} catch (JavaModelException e) {
				// move on
			}
		}
		return classname;
	}
	@Override
	public IModuleDescription getModuleDescription() {
		if (this.knownToBeModuleLess)
			return null;
		IModuleDescription module = super.getModuleDescription();
		if (module == null)
			this.knownToBeModuleLess = true;
		return module;
	}

	@Override
	public IPath internalPath() {
		if (isExternal()) {
			return this.jarPath;
		} else {
			return super.internalPath();
		}
	}
	@Override
	public IResource resource(PackageFragmentRoot root) {
		if (this.resource == null) {
			// external jar
			return null;
		}
		return super.resource(root);
	}


	/**
	 * @see IJavaElement
	 */
	@Override
	public IResource getUnderlyingResource() throws JavaModelException {
		if (isExternal()) {
			if (!exists()) throw newNotPresentException();
			return null;
		} else {
			return super.getUnderlyingResource();
		}
	}

	/** make sure all parent packages exit in rawPackageInfo and adds the given file to the given package
	 * static implementation to make sure the result can be shared across instances */
	static void initRawPackageInfo(Map<List<String>, PackageContent> rawPackageInfo, String classNameSubFolder,
			String entryName, boolean isDirectory, String compliance) {
		String className = entryToClassName(classNameSubFolder, entryName);
		int lastSeparator;
		if (isDirectory) {
			if (className.charAt(className.length() - 1) == '/') {
				lastSeparator = className.length() - 1;
			} else {
				lastSeparator = className.length();
			}
		} else {
			lastSeparator = className.lastIndexOf('/');
		}
		ArrayList<String> pkgName = new ArrayList<>(Arrays.asList(Util.splitOn('/', className, 0, lastSeparator)));
		PackageContent existing = null;
		int length = pkgName.size();
		int existingLength;
		for (existingLength = length; existing == null; existingLength--) {
			existing = rawPackageInfo.get(pkgName.subList(0, existingLength));
		}
		existingLength++;
		for (int i = existingLength; i < length; i++) {
			// sourceLevel must be null because we know nothing about it based on a jar file
			if (Util.isValidFolderNameForPackage(pkgName.get(i), null, compliance)) {
				List<String> path = pkgName.subList(0, i + 1);
				existing = new PackageContent();
				rawPackageInfo.put(path, existing);
			} else {
				// non-Java resource folder
				if (!isDirectory) {
					existing.resources().add(className);
				}
				return;
			}
		}
		if (isDirectory) {
			return;
		}

		// add classfile info amongst children
		PackageContent children = rawPackageInfo.get(pkgName);
		if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(className)) {
			String nameWithoutExtension = className.substring(lastSeparator + 1, className.length() - 6);
			children.javaClasses().add(nameWithoutExtension);
		} else {
			children.resources().add(className);
		}
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	@Override
	public boolean isArchive() {
		return true;
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	@Override
	public boolean isExternal() {
		return resource() == null;
	}
	/**
	 * Jars and jar entries are all read only
	 */
	@Override
	public boolean isReadOnly() {
		return true;
	}
	/**
	 * Returns whether the corresponding resource or associated file exists
	 */
	@Override
	protected boolean resourceExists(IResource underlyingResource) {
		if (underlyingResource == null) {
			return
				JavaModel.getExternalTarget(
					getPath()/*don't make the path relative as this is an external archive*/,
					true/*check existence*/) != null;
		} else {
			return super.resourceExists(underlyingResource);
		}
	}

	@Override
	protected void toStringAncestors(StringBuilder buffer) {
		if (isExternal())
			// don't show project as it is irrelevant for external jar files.
			// also see https://bugs.eclipse.org/bugs/show_bug.cgi?id=146615
			return;
		super.toStringAncestors(buffer);
	}

	public URL getIndexPath() {
		try {
			IClasspathEntry entry = ((JavaProject) getParent()).getClasspathEntryFor(getPath());
			if (entry != null) return ((ClasspathEntry)entry).getLibraryIndexLocation();
		} catch (JavaModelException e) {
			// ignore exception
		}
		return null;
	}

	@Override
	public Manifest getManifest() {
		ZipFile jar = null;
		try {
			jar = getJar();
			ZipEntry mfEntry = jar.getEntry(TypeConstants.META_INF_MANIFEST_MF);
			if (mfEntry != null) {
				try (InputStream is = jar.getInputStream(mfEntry)) {
					return new Manifest(is);
				}
			}
		} catch (CoreException | IOException e) {
			// must do without manifest
		} finally {
			JavaModelManager.getJavaModelManager().closeZipFile(jar);
		}
		return null;
	}

	/** overridden by JModPackageFragmentRoot */
	protected String getClassNameSubFolder() {
		return null;
	}

	static String entryToClassName(String classNameSubFolder, String entryName) {
		if (classNameSubFolder != null && entryName.startsWith(classNameSubFolder)) {
			return entryName.substring(classNameSubFolder.length());
		} else {
			return entryName;
		}
	}

	/** creates a unmodifiable copy that is thread-safe and has it's Strings deduplicated **/
	static Map<List<String>, PackageContent> unmodifiableCopy(Map<List<String>, PackageContent> rawPackageInfo) {
		Map<List<String>, PackageContent> deduplicatedPackageInfo = new HashMap<>();

		for (Entry<List<String>, PackageContent> e : rawPackageInfo.entrySet()) {
			List<String> key = e.getKey();
			PackageContent p = e.getValue();
			PackageContent packageContent = new PackageContent(DeduplicationUtil.intern(p.javaClasses()), DeduplicationUtil.intern(p.resources()));
			deduplicatedPackageInfo.put(DeduplicationUtil.intern(key), packageContent);
		}
		return Map.copyOf(deduplicatedPackageInfo);
	}

//	@Override
//	public boolean isModule() {
//	 	try {
//	 		return ((PackageFragmentRootInfo) getElementInfo()).isModule(resource(), this);
//	 	} catch (JavaModelException e) {
//	 		return false;
//	 	}
//	}
}
