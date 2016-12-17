/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Utility class, contains helpers for configuring the compiler options based on the project.
 * If the project is a groovy project it will set the right options, and will also set the groovy classpath.
 *
 * @author Andy Clement
 */
public class CompilerUtils {

	public static final int IsGrails = 0x0001;

	/**
	 * Configure a real compiler options object based on the project.  If anything goes wrong it will configure the options to just build java.
	 */
	public static void configureOptionsBasedOnNature(CompilerOptions compilerOptions, IJavaProject javaProject) {
		if (javaProject == null) {
			compilerOptions.buildGroovyFiles = 1;
			compilerOptions.groovyFlags = 0;
			return;
		}
		IProject project = javaProject.getProject();
		try {
			if (isGroovyNaturedProject(project)) {
				compilerOptions.storeAnnotations = true;
				compilerOptions.buildGroovyFiles = 2;
				setGroovyClasspath(compilerOptions, javaProject);
				if (isProbablyGrailsProject(project)) {
					compilerOptions.groovyFlags = IsGrails;
				} else {
					compilerOptions.groovyFlags = 0;
				}
			} else {
				compilerOptions.buildGroovyFiles = 1;
				compilerOptions.groovyFlags = 0;
			}
		} catch (CoreException e) {
			compilerOptions.buildGroovyFiles = 1;
			compilerOptions.groovyFlags = 0;
		}
	}

	/**
	 * Configure an options map (usually retrieved from a CompilerOptions object) based on the project.
	 * If anything goes wrong it will configure the options to just build java.
	 */
	public static void configureOptionsBasedOnNature(Map<String, String> optionMap, IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		try {
			if (isGroovyNaturedProject(project)) {
				optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
				setGroovyClasspath(optionMap, javaProject);
				if (isProbablyGrailsProject(project)) {
					// will need bit manipulation here when another flag added
					optionMap.put(CompilerOptions.OPTIONG_GroovyFlags, Integer.toString(IsGrails));
				} else {
					optionMap.put(CompilerOptions.OPTIONG_GroovyFlags, "0"); //$NON-NLS-1$
				}
			} else {
				optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
				optionMap.put(CompilerOptions.OPTIONG_GroovyFlags, "0"); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			e.printStackTrace();
			optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
			optionMap.put(CompilerOptions.OPTIONG_GroovyFlags, "0"); //$NON-NLS-1$
		}
	}

	/**
	 * Crude way to determine it... basically check for a folder called 'grails-app'.  The reason we need to know is because of the extra
	 * transform that will run if it is a grails-app (tagging domain classes).
	 */
	private static boolean isProbablyGrailsProject(IProject project) {
		try {
			IFolder folder = project.getFolder("grails-app"); //$NON-NLS-1$
			return folder.exists();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * There are currently two points of configuration here.  The first is the java project which will have its own classpath.
	 * The second is the groovy.properties file.  Due to the 'power' of just going with the java project, because it will cause
	 * us to pick up all sorts of stuff, I am going to make it necessary for groovy.properties to be set in a particular way
	 * if the user wants that power.
	 *
	 * @param compilerOptions the compiler options on which to set groovy options
	 * @param javaProject the project involved right now (may have the groovy nature)
	 */
	public static void setGroovyClasspath(CompilerOptions compilerOptions, IJavaProject javaProject) {
		Map<String, String> newOptions = new HashMap<String, String>();
		setGroovyClasspath(newOptions, javaProject);
		compilerOptions.groovyProjectName = javaProject.getProject().getName();
		if (!newOptions.isEmpty()) {
			compilerOptions.set(newOptions);
		}
	}

	public static void setGroovyClasspath(Map<String, String> optionMap, IJavaProject javaProject) {
		IFile file = javaProject.getProject().getFile("groovy.properties"); //$NON-NLS-1$
		if (file.exists()) {
			try {
				PropertyResourceBundle prb = new PropertyResourceBundle(file.getContents());
				Enumeration<String> e = prb.getKeys();
				while (e.hasMoreElements()) {
					String k = e.nextElement();
					String v = (String) prb.getObject(k);
					v = fixup(v, javaProject);
					if (k.equals(CompilerOptions.OPTIONG_GroovyClassLoaderPath)) {
						optionMap.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, v);
					}
				}
			} catch (IOException ioe) {
				System.err.println("Problem configuring groovy classloader classpath"); //$NON-NLS-1$
				ioe.printStackTrace();
			} catch (CoreException ce) {
				System.err.println("Problem configuring groovy classloader classpath"); //$NON-NLS-1$
				ce.printStackTrace();
			} catch (Throwable t) {
				System.err.println("Problem configuring groovy classloader classpath"); //$NON-NLS-1$
				t.printStackTrace();
			}
		} else {
			try {
				String classpath = calculateClasspath(javaProject);
				optionMap.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, classpath);
			} catch (Throwable t) {
				System.err.println("Problem configuring groovy classloader classpath (not using groovy.properties)"); //$NON-NLS-1$
				t.printStackTrace();
			}
		}
		IProject project = javaProject.getProject();
		try {
			IPath defaultOutputPath = javaProject.getOutputLocation();
			String defaultOutputLocation = pathToString(defaultOutputPath, project);
			optionMap.put(CompilerOptions.OPTIONG_GroovyExcludeGlobalASTScan, defaultOutputLocation);
		} catch (Throwable t) {
			System.err.println("Problem configuring serviceScanExclude"); //$NON-NLS-1$
			t.printStackTrace();
		}
		optionMap.put(CompilerOptions.OPTIONG_GroovyProjectName, project.getName());
	}

	private static String fixup(String someString, IJavaProject javaProject) {
		if (someString.startsWith("%projhome%")) { //$NON-NLS-1$
			someString = javaProject.getProject().getLocation().toOSString()+File.separator+someString.substring("%projhome%".length()); //$NON-NLS-1$
		}
		if (someString.equals("%projclasspath%")) { //$NON-NLS-1$
			someString = calculateClasspath(javaProject);
		}
		return someString;
	}

	/**
	 * @return true if the project has the groovy nature
	 */
	private static boolean isGroovyNaturedProject(IProject project) throws CoreException {
		return project.hasNature("org.eclipse.jdt.groovy.core.groovyNature"); //$NON-NLS-1$
	}

	private static String pathToString(IPath path, IProject project) {
		String realLocation = null;
		if (path != null) {
			String prefix = path.segment(0);
			if (prefix.equals(project.getName())) {
				if (path.segmentCount() == 1) {
					// the path is actually to the project root
					IPath rawPath = project.getRawLocation();
					if (rawPath == null) {
						System.err.println("Failed on call to getRawLocation() against the project: " + project); //$NON-NLS-1$
					} else {
						realLocation = project.getRawLocation().toOSString();
					}
				} else {
					IPath rawLocation = project.getFile(path.removeFirstSegments(1)).getRawLocation();
					if (rawLocation != null) {
						realLocation = rawLocation.toOSString();
					}
				}
			} else {
				realLocation = path.toOSString();
			}
		}
		return realLocation;
	}

	// visible for testing
	public static String calculateClasspath(IJavaProject javaProject) {
		try {
			Set<String> accumulatedPathEntries = new LinkedHashSet<String>();
			IProject project = javaProject.getProject();
			String projectName = project.getName();
			IPath defaultOutputPath = javaProject.getOutputLocation();
			String defaultOutputLocation = pathToString(defaultOutputPath, project);

			IClasspathEntry[] cpes = javaProject.getResolvedClasspath(true);
			if (cpes != null) {
				for (IClasspathEntry cpe : cpes) {
					if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						continue;
					}
					// Two kinds of entry we are interested in - those relative and those absolute
					// relative example: grails/lib/hibernate3-3.3.1.jar  (where grails is the project name)
					// absolute example: f:/grails-111/dist/grails-core-blah.jar
					// javaProject path is f:\grails\grails
					IPath cpePath = cpe.getPath();
					String pathElement = null;
					String segmentZero = cpePath.segment(0);
					if (segmentZero.equals(projectName)) {
						pathElement = project.getFile(cpePath.removeFirstSegments(1)).getRawLocation().toOSString();
					} else {
						// GRECLIPSE-917: Entry is something like /SomeOtherProject/foo/bar/doodah.jar
						if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
							try {
								IProject iproject = project.getWorkspace().getRoot().getProject(segmentZero);
								if (iproject != null) {
									IFile ifile = iproject.getFile(cpePath.removeFirstSegments(1));
									IPath ipath = (ifile == null ? null : ifile.getRawLocation());
									pathElement = (ipath == null ? null : ipath.toOSString());
								}
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
						if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
							// the classpath entry is a dependency on another project
							computeDependenciesFromProject(project, segmentZero, accumulatedPathEntries);
							// FIXASC what does all this look like for batch compilation?  Should it be passed in rather than computed here
						} else if (pathElement == null) {
							pathElement = cpe.getPath().toOSString();
						}
					}
					if (pathElement != null) {
						accumulatedPathEntries.add(pathElement);
					}
				}
				accumulatedPathEntries.add(defaultOutputLocation);

				// Add output locations which are not default
				try {
					if (isGroovyNaturedProject(project)) {
						for (IClasspathEntry entry : javaProject.getRawClasspath()) {
							if (entry.getOutputLocation() != null) {
								String location = pathToString(entry.getOutputLocation(), project);
								if (!defaultOutputLocation.equals(location)) {
									accumulatedPathEntries.add(location);
								}
							}
						}
					}
				} catch (CoreException e) {
					System.err.println("Unexpected error on checking Groovy Nature"); //$NON-NLS-1$
					e.printStackTrace();
				}

				StringBuilder sb = new StringBuilder();
				for (String entry : accumulatedPathEntries) {
					sb.append(entry).append(File.pathSeparator);
				}
				return sb.toString();
			}
		} catch (JavaModelException e) {
			System.err.println("Problem trying to determine classpath of project " + javaProject.getProject().getName() + ':'); //$NON-NLS-1$
			e.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Determine the exposed (exported) dependencies from the project named
	 * 'otherProject' and add them to the accumulatedPathEntries String Set.
	 * This will include the output location of the project plus other kinds
	 * of entry that are re-exported.  If dependent on another project and
	 * that project is re-exported, the method will recurse.
	 *
	 * @param baseProject the original project for which the classpath is being computed
	 * @param otherProject a project something in the dependency chain for the original project
	 * @param accumulatedPathEntries a String set of classpath entries, into which new entries should be added
	 */
	private static void computeDependenciesFromProject(IProject baseProject, String otherProject, Set<String> accumulatedPathEntries)
			throws JavaModelException {

		IProject iproject = baseProject.getWorkspace().getRoot().getProject(otherProject);
		IJavaProject iJavaProject = JavaCore.create(iproject);

		// add the project's output location
		accumulatedPathEntries.add(pathToString(iJavaProject.getOutputLocation(), iproject));

		IClasspathEntry[] cpes = iJavaProject.getResolvedClasspath(true);
		if (cpes != null) {
			for (IClasspathEntry cpe : cpes) {
				if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE && cpe.getOutputLocation() != null) {
					// add the source folder's output location (if different from the project's)
					accumulatedPathEntries.add(pathToString(cpe.getOutputLocation(), iproject));
				} else if (cpe.isExported()) {
					IPath cpePath = cpe.getPath();
					String segmentZero = cpePath.segment(0);
					if (segmentZero != null && segmentZero.equals(otherProject)) {
						accumulatedPathEntries.add(iproject.getFile(cpePath.removeFirstSegments(1)).getRawLocation().toOSString());
					} else if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
						// segmentZero is a project name
						computeDependenciesFromProject(baseProject, segmentZero, accumulatedPathEntries);
					} else {
						String otherPathElement = null;
						if (segmentZero != null && segmentZero.equals(iproject.getName())) {
							otherPathElement = iproject.getFile(cpePath.removeFirstSegments(1)).getRawLocation().toOSString();
						} else {
							otherPathElement = cpePath.toOSString();
						}
						accumulatedPathEntries.add(otherPathElement);
					}
				}
			}
		}
	}
}
