package org.eclipse.jdt.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * @author Andy Clement
 */
public class CompilerUtils {
	
	public static void configureOptionsBasedOnNature(CompilerOptions compilerOptions, IProject project) {
		try {
			if (project.hasNature("org.eclipse.jdt.groovy.core.groovyNature")) { //$NON-NLS-1$
				compilerOptions.storeAnnotations=true;
				compilerOptions.buildGroovyFiles=2;
			} else {
				compilerOptions.buildGroovyFiles=1;
			}
		} catch (CoreException e) {
			compilerOptions.buildGroovyFiles=1;
		}
		// FIXASC (M3) temporary way to get compiler stuff configured when there is no UI for it
		IFile file = project.getFile("groovy.properties"); //$NON-NLS-1$
		if (file.exists()) {
			try {
				Map newOptions = new HashMap();
				PropertyResourceBundle prb = new PropertyResourceBundle(file.getContents());
				Enumeration e = prb.getKeys();
				// System.err.println("Loading groovy settings for project '"+project.getName()+"'");
				while (e.hasMoreElements()) {
					String k = (String)e.nextElement();
					String v = (String)prb.getObject(k);
					v = fixup(v,project);
					// System.out.println(k+"="+v);
					if (k.equals(CompilerOptions.OPTIONG_GroovyClassLoaderPath)) {
						newOptions.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath,v);
					}
				}
				if (!newOptions.isEmpty()) {
					compilerOptions.set(newOptions);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private static String fixup(String someString, IProject project) {
		if (someString.startsWith("%projhome%")) {
			someString = project.getLocation().toOSString()+File.separator+someString.substring("%projhome%".length());
		}
		return someString;
	}
}
