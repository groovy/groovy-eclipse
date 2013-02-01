package org.eclipse.jdt.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
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
 * Utility class, contains helpers for configuring the compiler options based on the project.  If the project is a groovy project it
 * will set the right options, and will also set the groovy classpath.
 * 
 * @author Andy Clement
 */
public class CompilerUtils {

	public static final int IsGrails = 0x0001;

	
	/**
	 * Configure a real compiler options object based on the project.  If anything goes wrong it will configure the options to just build java.
	 */
	public static void configureOptionsBasedOnNature(CompilerOptions compilerOptions, IJavaProject javaProject) {
		if (javaProject==null) {
			compilerOptions.buildGroovyFiles=1;
			compilerOptions.groovyFlags = 0;
			return;
		}
		IProject project = javaProject.getProject();
		try {
			if (isGroovyNaturedProject(project)) {
				compilerOptions.storeAnnotations=true;
				compilerOptions.buildGroovyFiles=2;
				setGroovyClasspath(compilerOptions, javaProject);
				if (isProbablyGrailsProject(project)) {
					compilerOptions.groovyFlags = IsGrails;
				} else {
					compilerOptions.groovyFlags = 0;
				}
			} else {
				compilerOptions.buildGroovyFiles=1;
				compilerOptions.groovyFlags = 0;
			}
		} catch (CoreException e) {
			compilerOptions.buildGroovyFiles=1;
			compilerOptions.groovyFlags = 0;
		}
	}
	
	/**
	 * Configure an options map (usually retrieved from a CompilerOptions object) based on the project. 
	 * If anything goes wrong it will configure the options to just build java.
	 */
	public static void configureOptionsBasedOnNature(Map optionMap, IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		try {
			if (isGroovyNaturedProject(project)) {
				optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
				setGroovyClasspath(optionMap, javaProject);
				if (isProbablyGrailsProject(project)) {
					// will need bit manipulation here when another flag added
					optionMap.put(CompilerOptions.OPTIONG_GroovyFlags, Integer.toString(IsGrails));
				} else {
					optionMap.put(CompilerOptions.OPTIONG_GroovyFlags,"0"); //$NON-NLS-1$
				}
			} else {
				optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
				optionMap.put(CompilerOptions.OPTIONG_GroovyFlags,"0"); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			e.printStackTrace();
			optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
			optionMap.put(CompilerOptions.OPTIONG_GroovyFlags,"0"); //$NON-NLS-1$
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
		Map newOptions = new HashMap();
		setGroovyClasspath(newOptions, javaProject);
		compilerOptions.groovyProjectName = javaProject.getProject().getName();
		if (!newOptions.isEmpty()) {
			compilerOptions.set(newOptions);
		}
	}
	
	public static void setGroovyClasspath(Map optionMap, IJavaProject javaProject) {
		IFile file = javaProject.getProject().getFile("groovy.properties"); //$NON-NLS-1$
		if (file.exists()) {
			try {
				PropertyResourceBundle prb = new PropertyResourceBundle(file.getContents());
				Enumeration e = prb.getKeys();
				// System.err.println("Loading groovy settings for project '"+project.getName()+"'");
				while (e.hasMoreElements()) {
					String k = (String)e.nextElement();
					String v = (String)prb.getObject(k);
					v = fixup(v,javaProject);
					// System.out.println(k+"="+v);
					if (k.equals(CompilerOptions.OPTIONG_GroovyClassLoaderPath)) {
						optionMap.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath,v);
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
				optionMap.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath,classpath);
			} catch (Throwable t) {
				System.err.println("Problem configuring groovy classloader classpath (not using groovy.properties)"); //$NON-NLS-1$
				t.printStackTrace();				
			}
		}
		optionMap.put(CompilerOptions.OPTIONG_GroovyProjectName,javaProject.getProject().getName());
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
		if (path!=null) {
			String prefix = path.segment(0);
			if (prefix.equals(project.getName())) {
				if (path.segmentCount()==1) {		
					// the path is actually to the project root
					IPath rawPath = project.getRawLocation();
					if (rawPath==null) {
						System.err.println("Failed on call to getRawLocation() against the project: "+project); //$NON-NLS-1$
					} else {
					realLocation =  project.getRawLocation().toOSString();
					}
				} else {
					IPath rawLocation = project.getFile(path.removeFirstSegments(1)).getRawLocation();
					if (rawLocation != null) {
						realLocation =  rawLocation.toOSString();
					}
				}
			} else {
				realLocation = path.toOSString();
			}
		}
		return realLocation;
	}
	
	// public for testing
	public static String calculateClasspath(IJavaProject javaProject) {
		try {
			Set accumulatedPathEntries = new LinkedHashSet();
			IProject project = javaProject.getProject();
			String projectName = project.getName();
			IPath defaultOutputPath = javaProject.getOutputLocation();
			String defaultOutputLocation = pathToString(defaultOutputPath,project);

			IClasspathEntry[] cpes = javaProject.getResolvedClasspath(true);
			if (cpes!=null) {
				for (int i=0,max=cpes.length;i<max;i++) {
					IClasspathEntry cpe = cpes[i];
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
						
						// for GRECLIPSE-917.  Entry is something like /SomeOtherProject/foo/bar/doodah.jar
						if (cpe.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
							try {
								IProject iproject = project.getWorkspace().getRoot().getProject(segmentZero);
								if (iproject!=null) {
									IFile ifile = iproject.getFile(cpePath.removeFirstSegments(1));
									IPath ipath = (ifile==null?null:ifile.getRawLocation());
									pathElement = (ipath==null?null:ipath.toOSString());
								}
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
						if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
							// the classpath entry is a dependency on another project
							computeDependenciesFromProject(project,segmentZero,accumulatedPathEntries);
                            // FIXASC this ought to also allow for separate output folders in the project we depend upon *sigh*
							// FIXASC what does all this look like for batch compilation?  Should it be passed in rather than computed here
						} else {
							if (pathElement==null) {
								pathElement = cpe.getPath().toOSString();
							}
						}
					}
					if (pathElement!=null) {
						accumulatedPathEntries.add(pathElement);
					}
				}
				accumulatedPathEntries.add(defaultOutputLocation);
				StringBuilder sb = new StringBuilder();
				Iterator iter = accumulatedPathEntries.iterator();
				while (iter.hasNext()) {
					sb.append((String)iter.next());
					sb.append(File.pathSeparator);
				}
				String classpath = sb.toString();
//				System.out.println("Project classpath for '"+projectName+"' is "+classpath);
				return classpath;
			}
		} catch (JavaModelException jme) {
			System.err.println("Problem trying to determine classpath of project "+javaProject.getProject().getName()+":"); //$NON-NLS-1$ //$NON-NLS-2$
			jme.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Determine the exposed (exported) dependencies from the project named 'otherProject' and add them to the accumulatedPathEntries String Set.
	 * This will include the output location of the project plus other kinds of entry that are re-exported.  If dependent on another project
	 * and that project is re-exported, the method will recurse.
	 * 
	 * @param baseProject the original project for which the classpath is being computed
	 * @param otherProject a project something in the dependency chain for the original project
	 * @param accumulatedPathEntries a String set of classpath entries, into which new entries should be added
	 */
	private static void computeDependenciesFromProject(IProject baseProject, String otherProject, Set accumulatedPathEntries) throws JavaModelException {
		 
		// First the output location for the project:
		IProject iproject = baseProject.getWorkspace().getRoot().getProject(otherProject);
		IJavaProject ijp = JavaCore.create(iproject);
		accumulatedPathEntries.add(pathToString(ijp.getOutputLocation(),iproject));
	        
        // Look for exported entries from otherProject
        IClasspathEntry[] cpes = ijp.getResolvedClasspath(true);
        if (cpes!=null) {
        	for (int j=0;j<cpes.length;j++) {
        		IClasspathEntry cpe = cpes[j];
				if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					continue;
				}
                if (cpe.isExported()) {
                	// TODO should quickly dismiss source entries? others?
                    IPath cpePath = cpes[j].getPath();
                    String segmentZero = cpePath.segment(0);
                    if (segmentZero!=null && segmentZero.equals(otherProject)) {
						accumulatedPathEntries.add(iproject.getFile(cpePath.removeFirstSegments(1)).getRawLocation().toOSString());
					} else {
	                    if (cpe.getEntryKind()==IClasspathEntry.CPE_PROJECT) {
	                    	// segmentZero is a project name
	    					computeDependenciesFromProject(baseProject, segmentZero, accumulatedPathEntries);
	                    } else {
	                        String otherPathElement = null;
		                    if (segmentZero!=null && segmentZero.equals(iproject.getName())) {
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
	

}
