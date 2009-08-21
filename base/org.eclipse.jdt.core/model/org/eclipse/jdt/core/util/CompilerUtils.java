package org.eclipse.jdt.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Utility class, contains helpers for configuring the compiler options based on the project.  If the project is a groovy project it
 * will set the right options, and will also set the groovy classpath.
 * 
 * @author Andy Clement
 */
public class CompilerUtils {

	// FIXASC (M2) class is mess, tidy up!
	
	/**
	 * Configure a real compiler options object based on the project.  If anything goes wrong it will configure the options to just build java.
	 */
	public static void configureOptionsBasedOnNature(CompilerOptions compilerOptions, IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		try {
			if (isGroovyNaturedProject(project)) {
				compilerOptions.storeAnnotations=true;
				compilerOptions.buildGroovyFiles=2;
				setGroovyClasspath(compilerOptions, javaProject);
			} else {
				compilerOptions.buildGroovyFiles=1;
			}
		} catch (CoreException e) {
			compilerOptions.buildGroovyFiles=1;
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
			} else {
				optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
			}
		} catch (CoreException e) {
			optionMap.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
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
		// FIXASC (M3) temporary way to get compiler stuff configured when there is no UI for it
		Map newOptions = new HashMap();
		setGroovyClasspath(newOptions, javaProject);
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
				ioe.printStackTrace();
			} catch (CoreException ce) {
				ce.printStackTrace();
			}
		}
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
	
	
	private static String calculateClasspath(IJavaProject javaProject) {
		try {
			StringBuffer path = new StringBuffer();
			IProject project = javaProject.getProject();
			String projectName = project.getName();
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
					// FIXASC (M2) is this really fool proof for determining classpath stuff?
					// FIXASC (M2) need a caching mechanism for this or performance will suffer
					IPath cpePath = cpe.getPath();
					String pathElement = null;
					String prefix = cpePath.segment(0);
					if (prefix.equals(projectName)) {
						pathElement = project.getFile(cpePath.removeFirstSegments(1)).getRawLocation().toOSString();
					} else {
						pathElement = cpe.getPath().toOSString();
					}
					path.append(pathElement);
					path.append(File.pathSeparator);
				}
				String classpath = path.toString();
//				System.out.println("Project classpath for '"+projectName+"' is "+classpath);
				return classpath;
			}
		} catch (JavaModelException jme) {
			System.err.println("Problem trying to determine classpath of project "+javaProject.getProject().getName()+":"); //$NON-NLS-1$ //$NON-NLS-2$
			jme.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}
	

}
