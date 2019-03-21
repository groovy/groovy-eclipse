/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.util;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

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
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Utility class, contains helpers for configuring the compiler options based
 * on the project. If the project is a groovy project it will set the right
 * options, and will also set the groovy classpath.
 */
public class CompilerUtils {

    public static final int IsGrails = 0x1;
    public static final int InvokeDynamic = 0x2;

	/**
	 * Configure a real compiler options object based on the project. If anything
	 * goes wrong it will configure the options to just build java.
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
	 * Configure an options map (usually retrieved from a CompilerOptions object)
	 * based on the project. If anything goes wrong it will configure the options
	 * to just build java.
	 */
	public static void configureOptionsBasedOnNature(Map<String, String> optionMap, IJavaProject javaProject) {
		try {
			IProject project = javaProject.getProject();
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
			Util.log(e, "configureOptionsBasedOnNature failed"); //$NON-NLS-1$
			optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
			optionMap.put(CompilerOptions.OPTIONG_GroovyFlags, "0"); //$NON-NLS-1$
		}
	}

	/**
	 * Crude way to determine it... check for a folder called 'grails-app'. The
	 * reason we need to know is because of the extra transform that will run if
	 * it is a grails-app (tagging domain classes).
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
	 * There are currently two points of configuration here.  The first is the
	 * java project which will have its own classpath. The second is the 
	 * <tt>groovy.properties</tt> file.  Due to the 'power' of just going with
	 * the java project, because it will cause us to pick up all sorts of stuff,
	 * I am going to make it necessary for groovy.properties to be set in a
	 * particular way if the user wants that power.
	 *
	 * @param compilerOptions the compiler options on which to set groovy options
	 * @param javaProject the project involved right now (may have the groovy nature)
	 */
	public static void setGroovyClasspath(CompilerOptions compilerOptions, IJavaProject javaProject) {
		Map<String, String> newOptions = new HashMap<>();
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
				for (String k : prb.keySet()) {
					String v = fixup(prb.getString(k), javaProject);
					if (k.equals(CompilerOptions.OPTIONG_GroovyClassLoaderPath)) {
						optionMap.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, v);
					}
				}
			} catch (Throwable t) {
				Util.log(t, "configuring groovy classloader classpath failed"); //$NON-NLS-1$
			}
		} else {
			try {
				String classpath = calculateClasspath(javaProject);
				optionMap.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, classpath);
			} catch (Throwable t) {
				Util.log(t, "configuring groovy classloader classpath (not using groovy.properties) failed"); //$NON-NLS-1$
			}
		}
		IProject project = javaProject.getProject();
		try {
			IPath defaultOutputPath = javaProject.getOutputLocation();
			String defaultOutputLocation = pathToString(defaultOutputPath, project);
			optionMap.put(CompilerOptions.OPTIONG_GroovyExcludeGlobalASTScan, defaultOutputLocation);
		} catch (Throwable t) {
			Util.log(t, "configuring exclude global AST scan failed"); //$NON-NLS-1$
		}
		optionMap.put(CompilerOptions.OPTIONG_GroovyProjectName, project.getName());
	}

	private static String fixup(String someString, IJavaProject javaProject) {
		if (someString.startsWith("%projhome%")) { //$NON-NLS-1$
			someString = javaProject.getProject().getLocation().toOSString() + File.separator + someString.substring("%projhome%".length()); //$NON-NLS-1$
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

	/** Cache of results from {@link #calculateClasspath} used to prevent re-calculations. */
	private static final Map<IClasspathEntry[], String> CLASSPATH_CACHE = new WeakHashMap<>();

	// visible for testing
	public static String calculateClasspath(IJavaProject javaProject) {
		String classpath = null;
		IProject project = javaProject.getProject();
		String projectName = project.getName();
		try {
			IClasspathEntry[] cpes = javaProject.getResolvedClasspath(true);
			if (cpes != null && (classpath = CLASSPATH_CACHE.get(cpes)) == null) {
				Set<String> accumulatedPathEntries = new LinkedHashSet<>();
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
								Util.log(t, "getting library path failed"); //$NON-NLS-1$
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

				String defaultOutputLocation = pathToString(javaProject.getOutputLocation(), project);
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
					Util.log(e, "checking Groovy Nature failed"); //$NON-NLS-1$
				}

				classpath = accumulatedPathEntries.stream().collect(Collectors.joining(File.pathSeparator));
				CLASSPATH_CACHE.put(cpes, classpath);
			}
		} catch (JavaModelException e) {
			Util.log(e, "determining classpath of project " + projectName + " failed"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return classpath != null ? classpath : ""; //$NON-NLS-1$
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

	private static String pathToString(IPath path, IProject project) {
		String realLocation = null;
		if (path != null) {
			String prefix = path.segment(0);
			if (prefix.equals(project.getName())) {
				if (path.segmentCount() == 1) {
					// the path is actually to the project root
					IPath rawPath = project.getRawLocation();
					if (rawPath == null) {
						Util.log(null, "getRawLocation() against the project: " + project + " failed"); //$NON-NLS-1$ //$NON-NLS-2$
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
}
