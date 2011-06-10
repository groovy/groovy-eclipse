/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.codehaus.groovy.activator.GroovyActivator;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;

public class TestingEnvironment {
    
	private boolean fIsOpen = false;

	private IWorkspace fWorkspace = null;
	private Hashtable fProjects = null;

	private void addBuilderSpecs(String projectName) {
		try {
			IProject project = getProject(projectName);
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID, GroovyNature.GROOVY_NATURE });
			project.setDescription(description, null);
		} catch (CoreException e) {
			handleCoreException(e);
		}
	}
	
	
	/**
	 * Removes groovy nature from project and keeps only the JavaNature
	 */
	public void removeGroovyNature(String projectName) {
        try {
            IProject project = getProject(projectName);
            IProjectDescription description = project.getDescription();
            description.setNatureIds(new String[] { JavaCore.NATURE_ID });
            project.setDescription(description, null);
        } catch (CoreException e) {
            handleCoreException(e);
        }
	}

	/**
     * Removes groovy nature from project and keeps only the JavaNature
     */
    public void addGroovyNature(String projectName) {
        try {
            IProject project = getProject(projectName);
            IProjectDescription description = project.getDescription();
            description.setNatureIds(new String[] { JavaCore.NATURE_ID, GroovyNature.GROOVY_NATURE });
            project.setDescription(description, null);
        } catch (CoreException e) {
            handleCoreException(e);
        }
    }
    
    /** 
     * Adds the specified nature to the project
     * @param projectName
     * @param natureId
     */
    public void addNature(String projectName, String natureId) {
        try {
            IProject project = getProject(projectName);
            IProjectDescription description = project.getDescription();
            String[] existingNatures = description.getNatureIds();
            String[] newNatures = new String[existingNatures.length + 1];
            System.arraycopy(existingNatures, 0, newNatures, 0, existingNatures.length);
            newNatures[existingNatures.length] = natureId;
            description.setNatureIds(newNatures);
            project.setDescription(description, null);
        } catch (CoreException e) {
            handleCoreException(e);
        }
    }

	/** Adds a binary class with the given contents to the
	 * given package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".class".
	 * Returns the path of the added class.
	 */
	public IPath addBinaryClass(IPath packagePath, String className, byte[] contents) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath classPath = packagePath.append(className + ".class"); //$NON-NLS-1$
		createFile(classPath, contents);
		return classPath;
	}
	
	/** Adds a binary class with the given contents to the
	 * given package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".class".
	 * Returns the path of the added class.
	 */
	public IPath addBinaryClass(IPath packageFragmentRootPath, String packageName, String className, byte[] contents) {
		/* make sure the package exists */
		if(packageName != null && packageName.length() >0){
			IPath packagePath = addPackage(packageFragmentRootPath, packageName);

			return addBinaryClass(packagePath, className, contents);
		}
		return addBinaryClass(packageFragmentRootPath, className, contents);
			
	}
	
	/** Adds a class with the given contents to the given
	 * package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".java".
	 * Returns the path of the added class.
	 */
	public IPath addClass(IPath packagePath, String className, String contents) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath classPath = packagePath.append(className + ".java"); //$NON-NLS-1$
		try {
			createFile(classPath, contents.getBytes("UTF8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			checkAssertion("e1", false); //$NON-NLS-1$
		}
		return classPath;
	}
	
    /** Adds a groovy class with the given contents to the given
     * package in the workspace.  The package is created
     * if necessary.  If a class with the same name already
     * exists, it is replaced.  A workspace must be open,
     * and the given class name must not end with ".java".
     * Returns the path of the added class.
     */
	public IPath addGroovyClass(IPath packagePath, String className, String contents) {
	    return addGroovyClassExtension(packagePath, className, contents, null);
	}
	
    /** 
     * Adds a groovy class with the given contents to the given
     * package in the workspace, the file will use the specified file suffix.  
     * The package is created if necessary.  If a class with the same name already
     * exists, it is replaced.
     * Returns the path of the added class.
     */
	public IPath addGroovyClassWithSuffix(IPath packagePath, String className, String suffix, String contents) {
	    return addGroovyClassExtension(packagePath, className, suffix, contents, suffix);
	}

	
	/** Adds a groovy class with the given contents to the given
	 * package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".java".
	 * Returns the path of the added class.
	 * @param fileExtension file extension of the groovy class to create (without a '.')
	 */
	public IPath addGroovyClassExtension(IPath packagePath, String className, String contents, String fileExtension) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		if (fileExtension == null) {
		    fileExtension = "groovy";
		}
		IPath classPath = packagePath.append(className + "." + fileExtension); //$NON-NLS-1$
		try {
			createFile(classPath, contents.getBytes("UTF8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			checkAssertion("e1", false); //$NON-NLS-1$
		}
		return classPath;
	}

	
	/** Adds a class with the given contents to the given
	 * package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".java".
	 * Returns the path of the added class.
	 */
	public IPath addClass(IPath packageFragmentRootPath, String packageName, String className, String contents) {
		/* make sure the package exists */
		if(packageName != null && packageName.length() >0){
			IPath packagePath = addPackage(packageFragmentRootPath, packageName);

			return addClass(packagePath, className, contents);
		}
		return addClass(packageFragmentRootPath, className, contents);
	}

	
    /** Adds a groovy class with the given contents to the given
     * package in the workspace.  The package is created
     * if necessary.  If a class with the same name already
     * exists, it is replaced.  A workspace must be open,
     * and the given class name must not end with ".java".
     * Returns the path of the added class.
     */
	public IPath addGroovyClass(IPath packageFragmentRootPath, String packageName, String className, String contents) {
	    return addGroovyClassExtension(packageFragmentRootPath, packageName, className, contents, null);
	}
	
	public IPath addGroovyClassWithSuffix(IPath packageFragmentRootPath, String packageName, String className, String suffix, String contents) {
	    return addGroovyClassExtension(packageFragmentRootPath, packageName, className, contents, suffix);
	}

	/** Adds a groovy class with the given contents to the given
	 * package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".java".
	 * Returns the path of the added class.
     * @param fileExtension file extension of the groovy class to create (without a '.')
	 */
	public IPath addGroovyClassExtension(IPath packageFragmentRootPath, String packageName, String className, String contents, String fileExtension) {
		/* make sure the package exists */
		if(packageName != null && packageName.length() >0){
			IPath packagePath = addPackage(packageFragmentRootPath, packageName);

			return addGroovyClassExtension(packagePath, className, contents, fileExtension);
		}
		return addGroovyClassExtension(packageFragmentRootPath, className, contents, fileExtension);
	}

/** 
 * Add a class folder to the classpath of a project.
 */
public void addClassFolder(IPath projectPath, IPath classFolderPath, boolean isExported) throws JavaModelException {
	addEntry(projectPath, JavaCore.newLibraryEntry(classFolderPath, null, null, isExported));
}
	
	/** Adds a package to the given package fragment root
	 * in the workspace.  The package fragment root is created
	 * if necessary.  If a package with the same name already
	 * exists, it is not replaced.  A workspace must be open.
	 * Returns the path of the added package.
	 */
	public IPath addPackage(IPath packageFragmentRootPath, String packageName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath path =
			packageFragmentRootPath.append(packageName.replace('.', IPath.SEPARATOR));
		createFolder(path);
		return path;
	}

	public IPath addPackageFragmentRoot(IPath projectPath, String sourceFolderName) throws JavaModelException {
		return addPackageFragmentRoot(projectPath, sourceFolderName, null, null); //$NON-NLS-1$
	}

	/** Adds a package fragment root to the workspace.  If
	 * a package fragment root with the same name already
	 * exists, it is not replaced.  A workspace must be open.
	 * Returns the path of the added package fragment root.
	 */
	public IPath addPackageFragmentRoot(IPath projectPath, String sourceFolderName, IPath[] exclusionPatterns, String specificOutputLocation) throws JavaModelException {
		return addPackageFragmentRoot(projectPath, sourceFolderName, null, exclusionPatterns, specificOutputLocation);
	}
	
	/** Adds a package fragment root to the workspace.  If
	 * a package fragment root with the same name already
	 * exists, it is not replaced.  A workspace must be open.
	 * Returns the path of the added package fragment root.
	 */
	public IPath addPackageFragmentRoot(IPath projectPath, String sourceFolderName, IPath[] inclusionPatterns, IPath[] exclusionPatterns, String specificOutputLocation) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath path = getPackageFragmentRootPath(projectPath, sourceFolderName);
		createFolder(path);
		IPath outputPath = null;
		if (specificOutputLocation != null) {
			outputPath = getPackageFragmentRootPath(projectPath, specificOutputLocation);
			createFolder(outputPath);
		}
		IClasspathEntry entry = JavaCore.newSourceEntry(
			path,
			inclusionPatterns == null ? new Path[0] : inclusionPatterns,
			exclusionPatterns == null ? new Path[0] : exclusionPatterns,
			outputPath);
		addEntry(projectPath, entry);
		return path;
	}

	public void addProject(IProject project){
		fProjects.put(project.getName(), project);
	}

	public IPath addProject(String projectName){
		return addProject(projectName, "1.4");
	}

	public IPath addProject(String projectName, String compliance){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IProject project = createProject(projectName);
		int requiredComplianceFlag = 0;
		String compilerVersion = null;
		if ("1.5".equals(compliance)) {
			requiredComplianceFlag = AbstractCompilerTest.F_1_5;
			compilerVersion = CompilerOptions.VERSION_1_5;
		}
		else if ("1.6".equals(compliance)) {
			requiredComplianceFlag = AbstractCompilerTest.F_1_6;
			compilerVersion = CompilerOptions.VERSION_1_6;
		}
		else if ("1.7".equals(compliance)) {
			requiredComplianceFlag = AbstractCompilerTest.F_1_7;
			compilerVersion = CompilerOptions.VERSION_1_7;
		}
		else if (!"1.4".equals(compliance) && !"1.3".equals(compliance)) {
			throw new UnsupportedOperationException("Test framework doesn't support compliance level: " + compliance);
		}
		if (requiredComplianceFlag != 0) {
			if ((AbstractCompilerTest.getPossibleComplianceLevels() & requiredComplianceFlag) == 0)
				throw new RuntimeException("This test requires a " + compliance + " JRE");
			IJavaProject javaProject = JavaCore.create(project);
			Map options = new HashMap();
			options.put(CompilerOptions.OPTION_Compliance, compilerVersion);
			options.put(CompilerOptions.OPTION_Source, compilerVersion);
			options.put(CompilerOptions.OPTION_TargetPlatform, compilerVersion);
			javaProject.setOptions(options);
		}
		return project.getFullPath();
	}

	public void addRequiredProject(IPath projectPath, IPath requiredProjectPath) throws JavaModelException {
		addRequiredProject(projectPath, requiredProjectPath, new IPath[]{}/*include all*/, new IPath[]{}/*exclude none*/, false);
	}
	
	/** Adds a project to the classpath of a project.
	 */
	public void addRequiredProject(IPath projectPath, IPath requiredProjectPath, IPath[] accessibleFiles, IPath[] nonAccessibleFiles, boolean isExported) throws JavaModelException {
		checkAssertion("required project must not be in project", !projectPath.isPrefixOf(requiredProjectPath)); //$NON-NLS-1$
		IAccessRule[] accessRules = ClasspathEntry.getAccessRules(accessibleFiles, nonAccessibleFiles);
		addEntry(projectPath, JavaCore.newProjectEntry(requiredProjectPath, accessRules, true, new IClasspathAttribute[0], isExported));
	}
	
	public void addRequiredProject(IPath projectPath, IPath requiredProjectPath, IPath rule, int ruleKind) throws JavaModelException {
		checkAssertion("required project must not be in project", !projectPath.isPrefixOf(requiredProjectPath)); //$NON-NLS-1$
		IAccessRule accessRule = JavaCore.newAccessRule(rule, ruleKind);
		addEntry(projectPath, JavaCore.newProjectEntry(requiredProjectPath, new IAccessRule[] {accessRule}, true, new IClasspathAttribute[0], false));
	}

	public void addRequiredProject(IPath projectPath, IPath requiredProjectPath, boolean isOptional) throws JavaModelException {
		checkAssertion("required project must not be in project", !projectPath.isPrefixOf(requiredProjectPath)); //$NON-NLS-1$
		IClasspathAttribute[] attributes = isOptional ? new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, "true")} : new IClasspathAttribute[0];
		addEntry(projectPath, JavaCore.newProjectEntry(requiredProjectPath, null, true, attributes, false));
	}

	public void addExternalFolders(IPath projectPath, String[] folders) throws JavaModelException {
		addExternalFolders(projectPath, folders, false);
	}

	public void addExternalFolders(IPath projectPath, String[] folders, boolean isExported) throws JavaModelException {
		for (int i = 0, max = folders.length; i < max; i++) {
			String folder = folders[i];
			checkAssertion("folder name must not end with .zip or .jar", !folder.endsWith(".zip") && !folder.endsWith(".jar")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			addEntry(projectPath, JavaCore.newLibraryEntry(new Path(folder), null, null, isExported));
		}
	}

	public void addExternalJars(IPath projectPath, String[] jars) throws JavaModelException {
		addExternalJars(projectPath, jars, false);
	}

	public void addExternalJar(IPath projectPath, String jar) throws JavaModelException {
		addExternalJar(projectPath, jar, false);
	}
	
	/** Adds an external jar to the classpath of a project.
	 */
	public void addExternalJars(IPath projectPath, String[] jars, boolean isExported) throws JavaModelException {
		for (int i = 0, max = jars.length; i < max; i++) {
			String jar = jars[i];
			addEntry(projectPath, JavaCore.newLibraryEntry(new Path(jar), null, null, isExported));
		}
	}

	/** Adds an external jar to the classpath of a project.
	 */
	public void addExternalJar(IPath projectPath, String jar, boolean isExported) throws JavaModelException {
		addEntry(projectPath, JavaCore.newLibraryEntry(new Path(jar), null, null, isExported));
	}
	
public void addLibrary(IPath projectPath, IPath libraryPath, IPath sourceAttachmentPath, IPath sourceAttachmentRootPath) 
		throws JavaModelException {
	addEntry(projectPath, 		
		JavaCore.newLibraryEntry(libraryPath, sourceAttachmentPath,	sourceAttachmentRootPath));
}
	public void addEntry(IPath projectPath, IClasspathEntry entryPath) throws JavaModelException {
		IClasspathEntry[] classpath = getClasspath(projectPath);
		// first look to see if the entry already exists:
		for (IClasspathEntry entry : classpath) {
            if (entry.equals(entryPath)) {
                return;
            }
        }
		IClasspathEntry[] newClaspath = new IClasspathEntry[classpath.length + 1];
		System.arraycopy(classpath, 0, newClaspath, 0, classpath.length);
		newClaspath[classpath.length] = entryPath;
		setClasspath(projectPath, newClaspath);
	}
	
	/** Adds a file.
	 */
	public IPath addFile(IPath root, String fileName, String contents){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath filePath = root.append(fileName);
		try {
			createFile(filePath, contents.getBytes("UTF8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			checkAssertion("e1", false); //$NON-NLS-1$
		}
		return filePath;
	}
	
	/** Adds a folder.
	 */
	public IPath addFolder(IPath root, String folderName){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath folderPath = root.append(folderName);
		createFolder(folderPath);
		return folderPath;
	}

	public IPath addInternalJar(IPath projectPath, String zipName, byte[] contents) throws JavaModelException {
		return addInternalJar(projectPath, zipName, contents, false);
	}

	/** Adds a jar with the given contents to the the workspace.
	 * If a jar with the same name already exists, it is
	 * replaced.  A workspace must be open.  Returns the path of
	 * the added jar.
	 */
	public IPath addInternalJar(IPath projectPath, String zipName, byte[] contents, boolean isExported) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath path = projectPath.append(zipName);
		
		/* remove any existing zip from the java model */
		removeInternalJar(projectPath, zipName);

		createFile(path, contents);
		addEntry(projectPath, JavaCore.newLibraryEntry(path, null, null, isExported));
		return path;
	}

	private void checkAssertion(String message, boolean b) {
		Assert.isTrue(b, message);
	}

public void cleanBuild() {
	checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
	try {
		getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
	} catch (CoreException e) {
		handle(e);
	}
}
	
	/** Closes the testing environment and frees up any
	 * resources.  Once the testing environment is closed,
	 * it shouldn't be used any more.
	 */
	public void close() {
		try {
			if (fProjects != null) {
				Enumeration projectNames = fProjects.keys();
				while (projectNames.hasMoreElements()) {
					String projectName = (String) projectNames.nextElement();
					getJavaProject(projectName).getJavaModel().close();
				}
			}
			closeWorkspace();
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	/** Close a project from the workspace.
	 */
	public void closeProject(IPath projectPath){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getJavaProject(projectPath).getProject().close(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void closeWorkspace() {
		fIsOpen = false;
	}

	private IFile createFile(IPath path, byte[] contents) {
		try {
			IFile file = fWorkspace.getRoot().getFile(path);

			ByteArrayInputStream is = new ByteArrayInputStream(contents);
			if (file.exists()) {
				file.setContents(is, true, false, null);
			} else {
				file.create(is, true, null);
			}
			return file;
		} catch (CoreException e) {
			handle(e);
		}
		return null;
	}

	private IFolder createFolder(IPath path) {
		checkAssertion("root", !path.isRoot()); //$NON-NLS-1$

		/* don't create folders for projects */
		if (path.segmentCount() <= 1) {
			return null;
		}

		IFolder folder = fWorkspace.getRoot().getFolder(path);
		if (!folder.exists()) {
			/* create the parent folder if necessary */
			createFolder(path.removeLastSegments(1));

			try {
				folder.create(true, true, null);
			} catch (CoreException e) {
				handle(e);
			}
		}
		return folder;
	}

	private IProject createProject(String projectName) {
		final IProject project = fWorkspace.getRoot().getProject(projectName);
		try {
			IWorkspaceRunnable create = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					project.create(null, null);
					project.open(null);
				}
			};
			fWorkspace.run(create, null);
			fProjects.put(projectName, project);
			addBuilderSpecs(projectName);
		} catch (CoreException e) {
			handle(e);
		}
		return project;
	}

	/** Batch builds the workspace.  A workspace must be
	 * open.
	 */
	public void fullBuild() {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/**
	 * Batch builds a project.  A workspace must be open.
	 */
	public void fullBuild(IPath projectPath) {
		fullBuild(projectPath.lastSegment());
	}

	/**
	 * Batch builds a project.  A workspace must be open.
	 */
	public void fullBuild(String projectName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getProject(projectName).build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/**
	* Returns the class path.
	*/
	public IClasspathEntry[] getClasspath(IPath projectPath) {
		try {
			checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
			JavaProject javaProject = (JavaProject) JavaCore.create(getProject(projectPath));
			return javaProject.getExpandedClasspath();
//			IPath[] packageFragmentRootsPath = new IPath[entries.length];
//			for (int i = 0; i < entries.length; ++i)
//				packageFragmentRootsPath[i] = entries[i].getPath();
//			return packageFragmentRootsPath;
		} catch (JavaModelException e) {
			e.printStackTrace();
			checkAssertion("JavaModelException", false); //$NON-NLS-1$
			return null; // not reachable
		}
	}
	
	/**
	* Returns the Java Model element for the project.
	*/
	public IJavaProject getJavaProject(IPath projectPath) {
		IJavaProject javaProject = JavaCore.create(getProject(projectPath));
		Assert.isNotNull(javaProject);
		return javaProject;
	}
	
	/**
	* Returns the Java Model element for the project.
	*/
	public IJavaProject getJavaProject(String projectName) {
		IJavaProject javaProject = JavaCore.create(getProject(projectName));
		Assert.isNotNull(javaProject);
		return javaProject;
	}
	
	/**
	 * Return output location for a project.
	 */
	public IPath getOutputLocation(IPath projectPath){
		try {
			IJavaProject javaProject = JavaCore.create(getProject(projectPath));
			return javaProject.getOutputLocation();
		} catch(CoreException e){
			// ignore
		}
		return null;
	}
	
	/**
	 * Return all problems with workspace.
	 */
	public Problem[] getProblems(){
		return getProblemsFor(getWorkspaceRootPath());
	}
	
	/**
	 * Return all problems with the specified element.
	 */
	public Problem[] getProblemsFor(IPath path){
		return getProblemsFor(path, null);
	}
	/**
	 * Return all problems with the specified element.
	 */
	public Problem[] getProblemsFor(IPath path, String additionalMarkerType){	
		IResource resource;
		if(path.equals(getWorkspaceRootPath())){
			resource = getWorkspace().getRoot();
		} else {
			IProject p = getProject(path);
			if(p != null && path.equals(p.getFullPath())) {
				resource = getProject(path.lastSegment());
			} else if(path.getFileExtension() == null) {
				resource = getWorkspace().getRoot().getFolder(path);
			} else {
				resource = getWorkspace().getRoot().getFile(path);
			}
		}
		try {
			ArrayList problems = new ArrayList();
			IMarker[] markers = resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++)
				problems.add(new Problem(markers[i]));

			markers = resource.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++)
				problems.add(new Problem(markers[i]));
			
			markers = resource.findMarkers(IJavaModelMarker.TASK_MARKER, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++)
				problems.add(new Problem(markers[i]));
			
			if (additionalMarkerType != null) {
				markers = resource.findMarkers(additionalMarkerType, true, IResource.DEPTH_INFINITE);
				for (int i = 0; i < markers.length; i++)
					problems.add(new Problem(markers[i]));
			}

			Problem[] result = new Problem[problems.size()];
			problems.toArray(result);
			return result;
		} catch(CoreException e){
			// ignore
		}
		return new Problem[0];
	}
	
	
	/**
	 * Return all problems with the specified element.
	 */
	public IMarker[] getTaskMarkersFor(IPath path){
		IResource resource = null;
		if(path.equals(getWorkspaceRootPath())){
			resource = getWorkspace().getRoot();
		} else {
			IProject p = getProject(path);
			if(p != null && path.equals(p.getFullPath())) {
				resource = getProject(path.lastSegment());
			} else if(path.getFileExtension() == null) {
				resource = getWorkspace().getRoot().getFolder(path);
			} else {
				resource = getWorkspace().getRoot().getFile(path);
			}
		}
		try {
			if (resource != null) {
				final IMarker[] markers = resource.findMarkers(IJavaModelMarker.TASK_MARKER, true, IResource.DEPTH_INFINITE);
				if (markers.length > 1) {
					Arrays.sort(markers, new Comparator() {
						public int compare(Object o1, Object o2) {
							IMarker marker1 = (IMarker) o1;
							IMarker marker2 = (IMarker) o2;
							try {
								final int start1 = ((Integer) marker1.getAttribute(IMarker.CHAR_START)).intValue();
								final int start2 = ((Integer) marker2.getAttribute(IMarker.CHAR_START)).intValue();
								return start1 - start2;
							} catch (CoreException e) {
								return 0;
							}
						}
					});
				}
				return markers;
			}
		} catch(CoreException e){
			// ignore
		}
		return new IMarker[0];
	}
	
	/** Return the path of the package
	 * with the given name.  A workspace must be open, and
	 * the package must exist.
	 */
	public IPath getPackagePath(IPath root, String packageName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		if (packageName.length() == 0) {
			return root;
		}
		return root.append(packageName.replace('.', IPath.SEPARATOR));
	}

	/** Return the path of the package fragment root
	 * with the given name.  A workspace must be open, and
	 * the package fragment root must exist.
	 */
	public IPath getPackageFragmentRootPath(IPath projectPath, String name) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		if (name.length() == 0) {
			return projectPath;
		}
		return projectPath.append(name);
	}
	
	/**
	* Returns the core project.
	*/
	public IProject getProject(String projectName) {
		return (IProject)fProjects.get(projectName);
	}
	
	/**
	* Returns the core project.
	*/
	public IProject getProject(IPath projectPath) {
		return (IProject)fProjects.get(projectPath.lastSegment());
	}

	private File tmpDirectory;
	File getTmpDirectory() {
		if (this.tmpDirectory == null) {
			this.tmpDirectory = new File(System.getProperty("java.io.tmpdir") + 
					File.separator + "org.eclipse.jdt.core.builder.tests.tmp");
			if (this.tmpDirectory.exists() && !this.tmpDirectory.isDirectory()) {
				Util.delete(this.tmpDirectory);
			}
			this.tmpDirectory.mkdir();
		}
		return this.tmpDirectory;
	}
	void deleteTmpDirectory() {
		if (this.tmpDirectory != null) {
			Util.delete(this.tmpDirectory);
			this.tmpDirectory = null;
		}
	}

	/**
	* Returns the workspace.
	*/
	public IWorkspace getWorkspace() {
		return fWorkspace;
	}
	
	/**
	* Returns the path of workspace root.
	*/
	public IPath getWorkspaceRootPath(){
		return getWorkspace().getRoot().getLocation();
	}

	private IPath getJarRootPath(IPath projectPath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		return getProject(projectPath).getFullPath();
	}

	void handle(Exception e) {
		if (e instanceof CoreException) {
			handleCoreException((CoreException) e);
		} else {
			e.printStackTrace();
			Assert.isTrue(false);
		}
	}

	/**
	* Handles a core exception thrown during a testing environment operation
	*/
	private void handleCoreException(CoreException e) {
		e.printStackTrace();
		IStatus status = e.getStatus();
		String message = e.getMessage();
		if (status.isMultiStatus()) {
			MultiStatus multiStatus = (MultiStatus) status;
			IStatus[] children = multiStatus.getChildren();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0, max = children.length; i < max; i++) {
				IStatus child = children[i];
				if (child != null) {
					buffer.append(child.getMessage());
					buffer.append(System.getProperty("line.separator"));//$NON-NLS-1$
					Throwable childException = child.getException();
					if (childException != null) {
						childException.printStackTrace();
					}
				}
			}
			message = buffer.toString();
		}
		Assert.isTrue(
			false,
			"Core exception in testing environment: " + message); //$NON-NLS-1$
	}

	/** Incrementally builds the workspace.  A workspace must be
	 * open.
	 */
	public void incrementalBuild() {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/** Incrementally builds a project.  A workspace must be
	 * open.
	 */
	public void incrementalBuild(IPath projectPath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getProject(projectPath).build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			handle(e);
		} catch(Throwable e) {
			e.printStackTrace();
	}
	}
	
	public boolean isAutoBuilding() {
		IWorkspace w = getWorkspace();
		IWorkspaceDescription d = w.getDescription();
		return d.isAutoBuilding();
	}

	/** Open an empty workspace.
 	*/
	public void openEmptyWorkspace() {
		close();
		openWorkspace();
		fProjects = new Hashtable(10);
		setup();
	}
	
	/** Close a project from the workspace.
	 */
	public void openProject(IPath projectPath){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getJavaProject(projectPath).getProject().open(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void openWorkspace() {
		try {
			closeWorkspace();

			fWorkspace = ResourcesPlugin.getWorkspace();

			// turn off auto-build -- the tests determine when builds occur
			IWorkspaceDescription description = fWorkspace.getDescription();
			description.setAutoBuilding(false);
			fWorkspace.setDescription(description);
		} catch (Exception e) {
			handle(e);
		}
	}

	/** Renames a compilation unit int the given package in the workspace.
	 * A workspace must be open.
	 */
	public void renameCU(IPath packagePath, String cuName, String newName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IFolder packageFolder = fWorkspace.getRoot().getFolder(packagePath);
		try {
			packageFolder.getFile(cuName).move(packageFolder.getFile(newName).getFullPath(), true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Removes a binary class from the given package in
	 * the workspace.  A workspace must be open, and the
	 * given class name must not end with ".class".
	 */
	public void removeBinaryClass(IPath packagePath, String className) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		className += ".class"; //$NON-NLS-1$
		IFolder packageFolder = fWorkspace.getRoot().getFolder(packagePath);
		try {
			packageFolder.getFile(className).delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Removes a class from the given package in the workspace.
	 * A workspace must be open, and the given class name must
	 * not end with ".java".
	 */
	public void removeClass(IPath packagePath, String className) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		className += ".java"; //$NON-NLS-1$
		IFolder packageFolder = fWorkspace.getRoot().getFolder(packagePath);
		try {
			packageFolder.getFile(className).delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Removes a package from the given package fragment root
	 * in the workspace.  A workspace must be open.
	 */
	public void removePackage(IPath packageFragmentRootPath, String packageName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath path =
			packageFragmentRootPath.append(packageName.replace('.', IPath.SEPARATOR));
		IFolder folder = fWorkspace.getRoot().getFolder(path);
		try {
			folder.delete(false, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Removes the given package fragment root from the
	 * the workspace.  A workspace must be open.
	 */
	public void removePackageFragmentRoot(IPath projectPath, String packageFragmentRootName) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		if (packageFragmentRootName.length() > 0) {
			IFolder folder = getProject(projectPath).getFolder(packageFragmentRootName);
			if (folder.exists()) {
				try {
					folder.delete(false, null);
				} catch (CoreException e) {
					handle(e);
				}
			}
		}
		IPath rootPath = getPackageFragmentRootPath(projectPath, packageFragmentRootName);
		removeEntry(projectPath, rootPath);
	}
	
	/** Remove a project from the workspace.
	 */
	public void removeProject(IPath projectPath){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getJavaProject(projectPath).close();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		IProject project = getProject(projectPath);
		try {
			project.delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
		
	}
	
	/** Remove a required project from the classpath
	 */
	public void removeRequiredProject(IPath projectPath, IPath requiredProject) throws JavaModelException {
		removeEntry(projectPath, requiredProject);
	}
	
	/** Remove all elements in the workspace.
	 */
	public void resetWorkspace(){
		if (fProjects != null) {
			Enumeration projectNames = fProjects.keys();
			while (projectNames.hasMoreElements()) {
				String projectName = (String) projectNames.nextElement();
				removeProject(getProject(projectName).getFullPath());
			}
		}
	}

	/** Removes the given internal jar from the workspace.
	 * A workspace must be open.
	 */
	public void removeInternalJar(IPath projectPath, String zipName) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		checkAssertion("zipName must end with .zip or .jar", zipName.endsWith(".zip") || zipName.endsWith(".jar")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		/* remove zip from the java model (it caches open zip files) */
		IPath zipPath = getJarRootPath(projectPath).append(zipName);
		try {
			getJavaProject(projectPath)
				.getPackageFragmentRoot(getWorkspace().getRoot().getFile(zipPath))
				.close();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		removePackageFragmentRoot(projectPath, zipName);

		IFile file = getProject(projectPath).getFile(zipName);
		try {
			file.delete(false, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/**
	 * Remove an external jar from the classpath.
	 */
	public void removeExternalJar(IPath projectPath, IPath jarPath) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		removeEntry(projectPath, jarPath);
	}
	
	private void removeEntry(IPath projectPath, IPath entryPath) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IClasspathEntry[] oldEntries = getClasspath(projectPath);
		for (int i = 0; i < oldEntries.length; ++i) {
			if (oldEntries[i].getPath().equals(entryPath)) {
				IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length - 1];
				System.arraycopy(oldEntries, 0, newEntries, 0, i);
				System.arraycopy(oldEntries, i + 1, newEntries, i, oldEntries.length - i - 1);
				setClasspath(projectPath, newEntries);
			}
		}
	}

	/** Remove a file
	 */
	public void removeFile(IPath filePath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			fWorkspace.getRoot().getFile(filePath).delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/** Remove a folder
	 */
	public void removeFolder(IPath folderPath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IFolder folder = fWorkspace.getRoot().getFolder(folderPath);
		try {
			folder.delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/** Sets the classpath to the given package fragment
	 * roots.  The builder searches the classpath to
	 * find the java files it needs during a build.
	 */
//	public void setClasspath(IPath projectPath, IPath[] packageFragmentRootsPath) {
//		try {
//			checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
//			IJavaProject javaProject = JavaCore.create(getProject(projectPath));
//			IClasspathEntry[] entries =
//				new IClasspathEntry[packageFragmentRootsPath.length];
//			for (int i = 0; i < packageFragmentRootsPath.length; ++i) {
//				IPath path = packageFragmentRootsPath[i];
//				if ("jar".equals(path.getFileExtension()) //$NON-NLS-1$
//					|| "zip".equals(path.getFileExtension())) { //$NON-NLS-1$
//					entries[i] = JavaCore.newLibraryEntry(path, null, null, isExported);
//				} else if (projectPath.isPrefixOf(packageFragmentRootsPath[i])) {
//					entries[i] = JavaCore.newSourceEntry(path, IPath[] exclusionPatterns, IPath specificOutputLocation)
//				} else {
//					entries[i] = JavaCore.newProjectEntry(path, isExported);
//				}
//			}
//			javaProject.setRawClasspath(entries, null);
//		} catch (JavaModelException e) {
//			e.printStackTrace();
//			checkAssertion("JavaModelException", false); //$NON-NLS-1$
//		}
//	}
	
	public void setAutoBuilding(boolean value) {
		try {
			IWorkspace w = getWorkspace();
			IWorkspaceDescription d = w.getDescription();
			d.setAutoBuilding(value);
			w.setDescription(d);
		} catch (CoreException e) {
			e.printStackTrace();
			checkAssertion("CoreException", false); //$NON-NLS-1$
		}
	}

	public void setBuildOrder(String[] projects) {
		try {
			IWorkspace w = getWorkspace();
			IWorkspaceDescription d = w.getDescription();
			d.setBuildOrder(projects);
			w.setDescription(d);
		} catch (CoreException e) {
			e.printStackTrace();
			checkAssertion("CoreException", false); //$NON-NLS-1$
		}
	}

	public void setClasspath(IPath projectPath, IClasspathEntry[] entries) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IJavaProject javaProject = JavaCore.create(getProject(projectPath));
		javaProject.setRawClasspath(entries, null);
	}
	
	public IPath setExternalOutputFolder(IPath projectPath, String name, IPath externalOutputLocation){
		IPath result = null;
		try {
			checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
			IProject p = getProject(projectPath);
			IFolder f = p.getFolder(name);
			f.createLink(externalOutputLocation, IResource.ALLOW_MISSING_LOCAL, null);

			result = f.getFullPath();
			IJavaProject javaProject = JavaCore.create(p);
			javaProject.setOutputLocation(result, null);
		} catch (CoreException e) {
			e.printStackTrace();
			checkAssertion("CoreException", false); //$NON-NLS-1$
		}
		return result;
	}
	
	public IPath setOutputFolder(IPath projectPath, String outputFolder){
		IPath outputPath = null;
		try {
			checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
			IJavaProject javaProject = JavaCore.create(getProject(projectPath));
			outputPath = projectPath.append(outputFolder);
			javaProject.setOutputLocation(outputPath, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
			checkAssertion("JavaModelException", false); //$NON-NLS-1$
		}
		return outputPath;
	}

	private void setup() {
		fIsOpen = true;
	}
	
	/**
	 * Wait for autobuild notification to occur
	 */
	public void waitForAutoBuild() {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				handle(e);
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}	
	
	public void waitForManualRefresh() {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				handle(e);
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}
	
	public void addJar(IPath projectPath, String path) throws Exception {
		URL jar = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.builder").getEntry(path);
        addExternalJar(projectPath, FileLocator.resolve(jar).getFile());
	}

    public void addGroovyJars(IPath projectPath) throws Exception {
        addExternalJar(projectPath, GroovyActivator.GROOVY_JAR_URL.getFile());
        addExternalJar(projectPath, GroovyActivator.ASM_JAR_URL.getFile());
    }   
    
    public void addGroovyPlusPlusJar(IPath projectPath) throws Exception {
        addExternalJar(projectPath, GroovyActivator.GROOVY_GPP_URL.getFile());
    }
    
    public void addJUnitJar(IPath projectPath) throws Exception {
        addExternalJar(projectPath,FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.builder").getEntry("lib/junit-4.3.1.jar")).getFile());
    }
}
