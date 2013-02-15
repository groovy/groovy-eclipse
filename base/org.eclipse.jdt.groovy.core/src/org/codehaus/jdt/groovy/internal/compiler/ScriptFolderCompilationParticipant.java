/*******************************************************************************
 * Copyright (c) 20010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg  - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector.FileKind;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Compilation participant for notification when a compile completes. Copies over specified script files into the output directory
 * 
 * @author Andrew Eisenberg
 * @created Oct 5, 2010
 */
public class ScriptFolderCompilationParticipant extends CompilationParticipant {

	/**
	 * 
	 * @author Andrew Eisenberg
	 * @created Oct 4, 2010
	 * 
	 *          use this to ensure that the longest paths are looked at first. ensures that nested source folders will be
	 *          appropriately found
	 */
	static class PathLengthComparator implements Comparator<IContainer> {
		public int compare(IContainer c1, IContainer c2) {
			if (c1 == null) {
				if (c2 == null) {
					return 0;
				}
				return -1;
			}
			if (c2 == null) {
				return 1;
			}
			int len1 = c1.getFullPath().segmentCount();
			int len2 = c2.getFullPath().segmentCount();
			if (len1 > len2) { // a larger path should come first
				return -1;
			} else if (len1 == len2) {
				// then compare by text:
				return c1.toString().compareTo(c2.toString());
			} else {
				return 1;
			}
		}

	}

	private static final PathLengthComparator comparator = new PathLengthComparator();

	private IJavaProject project;

	/**
	 * We care only about Groovy projects
	 */
	@Override
	public boolean isActive(IJavaProject project) {
		boolean hasGroovyNature = GroovyNature.hasGroovyNature(project.getProject());
		if (!hasGroovyNature) {
			return false;
		}
		this.project = project;
		return true;
	}

	@Override
	public void buildStarting(BuildContext[] compiledFiles, boolean isBatch) {
		if (!sanityCheckBuilder(compiledFiles)) {
			// problem happened, do not copy
			return;
		}

		try {
			IProject iproject = project.getProject();
			if (compiledFiles == null || !ScriptFolderSelector.isEnabled(iproject)) {
				return;
			}

			ScriptFolderSelector selector = new ScriptFolderSelector(iproject);
			Map<IContainer, IContainer> sourceToOut = generateSourceToOut(project);
			for (BuildContext compiledFile : compiledFiles) {
				IFile file = compiledFile.getFile();
				if (selector.getFileKind(file) == FileKind.SCRIPT) {
					IPath filePath = file.getFullPath();
					IContainer containingSourceFolder = findContainingSourceFolder(sourceToOut, filePath);

					// if null, that means the out folder is the same as the source folder
					if (containingSourceFolder != null) {
						IPath packagePath = findPackagePath(filePath, containingSourceFolder);
						IContainer out = sourceToOut.get(containingSourceFolder);
						copyFile(file, packagePath, out);
					}
				}
			}
		} catch (CoreException e) {
			Util.log(e, "Error when copying scripts to output folder"); //$NON-NLS-1$
		}
	}

	/**
	 * Some simple checks that we can do to ensure that the builder is set up properly
	 * 
	 * @param files
	 */
	private boolean sanityCheckBuilder(BuildContext[] files) {
		// GRECLIPSE-1230 also do a check to ensure the proper compiler is being used
		if (!LanguageSupportFactory.isGroovyLanguageSupportInstalled()) {
			for (BuildContext buildContext : files) {
				buildContext.recordNewProblems(createProblem(buildContext));
			}
		}
		// also check if this project has the JavaBuilder.
		// note that other builders (like the ajbuilder) may implement the CompilationParticipant API
		try {
			ICommand[] buildSpec = project.getProject().getDescription().getBuildSpec();
			boolean found = false;
			for (ICommand command : buildSpec) {
				if (command.getBuilderName().equals(JavaCore.BUILDER_ID)) {
					found = true;
					break;
				}
			}
			if (!found) {
				for (BuildContext buildContext : files) {
					buildContext.recordNewProblems(createProblem(buildContext));
				}
			}
			return found;
		} catch (CoreException e) {
			Util.log(e);
			return false;
		}

	}

	/**
	 * @param buildContext
	 * @return
	 */
	private CategorizedProblem[] createProblem(BuildContext buildContext) {
		DefaultProblem problem = new DefaultProblem(buildContext.getFile().getFullPath().toOSString().toCharArray(),
				"Error compiling Groovy project.  Either the Groovy-JDT patch is not installed or JavaBuilder is not being used.",
				0, new String[0], ProblemSeverities.Error, 0, 0, 1, 0);
		return new CategorizedProblem[] { problem };
	}

	@Override
	public void buildFinished(IJavaProject project) {
		// try {
		// IProject iproject = project.getProject();
		// if (compiledFiles == null || !ScriptFolderSelector.isEnabled(iproject)) {
		// return;
		// }
		//
		// ScriptFolderSelector selector = new ScriptFolderSelector(iproject);
		// Map<IContainer, IContainer> sourceToOut = generateSourceToOut(project);
		// for (BuildContext compiledFile : compiledFiles) {
		// IFile file = compiledFile.getFile();
		// if (selector.getFileKind(file) == FileKind.SCRIPT) {
		// IPath filePath = file.getFullPath();
		// IContainer containingSourceFolder = findContainingSourceFolder(sourceToOut, filePath);
		//
		// // if null, that means the out folder is the same as the source folder
		// if (containingSourceFolder != null) {
		// IPath packagePath = findPackagePath(filePath, containingSourceFolder);
		// IContainer out = sourceToOut.get(containingSourceFolder);
		// copyFile(file, packagePath, out);
		// }
		// }
		// }
		// } catch (CoreException e) {
		//			Util.log(e, "Error in Script folder compilation participant"); //$NON-NLS-1$
		// } finally {
		// compiledFiles = null;
		// }
	}

	/**
	 * @param file
	 * @param containingSourceFolder
	 * @return
	 */
	private IPath findPackagePath(IPath filePath, IContainer containingSourceFolder) {
		IPath containerPath = containingSourceFolder.getFullPath();
		filePath = filePath.removeFirstSegments(containerPath.segmentCount());
		filePath = filePath.removeLastSegments(1);
		return filePath;
	}

	private IContainer findContainingSourceFolder(Map<IContainer, IContainer> sourceToOut, IPath filePath) {
		Set<IContainer> sourceFolders = sourceToOut.keySet();
		for (IContainer container : sourceFolders) {
			if (container.getFullPath().isPrefixOf(filePath)) {
				return container;
			}
		}
		return null;
	}

	private void copyFile(IFile file, IPath packagePath, IContainer outputFolder) throws CoreException {
		IContainer createdFolder = createFolder(packagePath, outputFolder, true);
		IFile toFile = createdFolder.getFile(new Path(file.getName()));
		if (toFile.exists()) {
			toFile.delete(true, null);
		}
		file.copy(toFile.getFullPath(), true, null);
		ResourceAttributes newAttrs = new ResourceAttributes();
		newAttrs.setReadOnly(false);
		newAttrs.setHidden(false);
		toFile.setResourceAttributes(newAttrs);
		toFile.setDerived(true, null);
		toFile.refreshLocal(IResource.DEPTH_ZERO, null);
	}

	/**
	 * Creates folder with the given path in the given output folder. This method is taken from
	 * org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.createFolder(..)
	 */
	private IContainer createFolder(IPath packagePath, IContainer outputFolder, boolean derived) throws CoreException {
		if (!outputFolder.exists() && outputFolder instanceof IFolder) {
			((IFolder) outputFolder).create(true, true, null);
		}
		if (packagePath.isEmpty())
			return outputFolder;
		IFolder folder = outputFolder.getFolder(packagePath);
		folder.refreshLocal(IResource.DEPTH_ZERO, null);
		if (!folder.exists()) {
			createFolder(packagePath.removeLastSegments(1), outputFolder, derived);
			folder.create(true, true, null);
			folder.setDerived(derived, null);
			folder.refreshLocal(IResource.DEPTH_ZERO, null);
		}
		return folder;
	}

	private Map<IContainer, IContainer> generateSourceToOut(IJavaProject project) throws JavaModelException {
		IProject p = project.getProject();
		IWorkspaceRoot root = (IWorkspaceRoot) p.getParent();
		IClasspathEntry[] cp = project.getRawClasspath();

		// determine default out folder
		IPath defaultOutPath = project.getOutputLocation();
		IContainer defaultOutContainer;
		if (defaultOutPath.segmentCount() > 1) {
			defaultOutContainer = root.getFolder(defaultOutPath);
		} else {
			defaultOutContainer = p;
		}

		Map<IContainer, IContainer> sourceToOut = new TreeMap<IContainer, IContainer>(comparator);
		for (IClasspathEntry cpe : cp) {
			if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

				// determine source folder
				IContainer sourceContainer;
				IPath sourcePath = cpe.getPath();
				if (sourcePath.segmentCount() > 1) {
					sourceContainer = root.getFolder(sourcePath);
				} else {
					sourceContainer = p;
				}

				// determine out folder
				IPath outPath = cpe.getOutputLocation();
				IContainer outContainer;
				if (outPath == null) {
					outContainer = defaultOutContainer;
				} else if (outPath.segmentCount() > 1) {
					outContainer = root.getFolder(outPath);
				} else {
					outContainer = p;
				}

				// if the two containers are equal, that means no copying should be done
				// do not add to map
				if (!sourceContainer.equals(outContainer)) {
					sourceToOut.put(sourceContainer, outContainer);
				}
			}

		}
		return sourceToOut;
	}

}
