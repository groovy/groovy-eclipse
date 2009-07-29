/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Visitor to visit groovy sourcefiles inside a eclipse workspace
 * @author reto kleeb
 */
public class GroovySourceFileVisitor implements IResourceVisitor {

	private List<IFile> groovySourceFiles = new LinkedList<IFile>();
	private IProject project;

	public GroovySourceFileVisitor(IProject project) {
		this.project = project;
		try {
			project.accept(this);
		} catch (CoreException e) {
			//ignore these projects (closed projects)
		}
	}

	/**
	 * return true if resources below the current should be visited
	 */
	public boolean visit(IResource resource) throws CoreException {
//		if (resource.getType() != IResource.FILE) {
//			return true;
//		}
		IFile file = (IFile) resource.getAdapter(IFile.class);

		if (isGroovyScript(resource) && isInSourceFolder(file, project)) {
			groovySourceFiles.add(file);
		}
		return true;
	}

	private static boolean isGroovyScript(final IResource resource) {
		return (	resource != null && 
					resource.getFileExtension() != null && 
					resource.getFileExtension().equals("groovy"));
	}

	private static boolean isInSourceFolder(IFile file, IProject project) {
		if (file == null || project == null){
			return false;
		}
		IJavaProject javaproject = JavaCore.create(project);
		try {
			IClasspathEntry[] entries = javaproject.getResolvedClasspath(false);
			for (IClasspathEntry entry : entries) {
				if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE)
					continue;
				if (entry.getPath().isPrefixOf(file.getFullPath()))
					return true;
			}
		} catch (final JavaModelException e) {
			GroovyCore.logException("Error getting resolved classpath from: "
					+ javaproject.getElementName() + ". " + e, e);
		}
		return false;
	}

	public List<IFile> getGroovySourceFiles() {
		return groovySourceFiles;
	}

}
