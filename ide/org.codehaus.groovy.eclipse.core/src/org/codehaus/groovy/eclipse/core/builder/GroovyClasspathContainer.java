/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.builder;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;
import static org.eclipse.core.runtime.FileLocator.resolve;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class GroovyClasspathContainer implements IClasspathContainer {
    public static Path CONTAINER_ID = new Path("GROOVY_SUPPORT");

    public static String DESC = "Groovy Libraries";

    private IClasspathEntry[] entries;

    public IClasspathEntry[] getClasspathEntries() {
    	if (entries == null) {
    		updateEntries();
    	}
        return entries;
    }

    // Theoretically, we can support multiple versions of org.codehaus.groovy here
    private void updateEntries() {
        final List<IClasspathEntry> newEntries = newList();
        try {
	    	URL groovyURL = getExportedGroovyAllJar();
	        IPath runtimeJarPath = new Path(groovyURL.getPath());
	        File srcJarFile = new File(groovyURL.getPath().replace(".jar", "-sources.jar"));
	        IPath srcJarPath = srcJarFile.exists() ? 
	        		new Path(srcJarFile.getAbsolutePath()) : null;

	        		
	        File javadocJarFile = new File(groovyURL.getPath().replace(".jar", "-javadoc.jar"));
	        IClasspathAttribute[] attrs;
	        if (javadocJarFile.exists()) {
	            String javadocJarPath = javadocJarFile.getAbsolutePath();
	            final IClasspathAttribute cpattr = new ClasspathAttribute(
	                    IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME,
	                    javadocJarPath);
	            
	            attrs = new IClasspathAttribute[] { cpattr };
	        } else {
	            attrs = new IClasspathAttribute[0];
	        }
	        IClasspathEntry entry = newLibraryEntry(runtimeJarPath,
	        		srcJarPath, null, null,
	                attrs, true);
	        newEntries.add(entry);
	        
	        
	        List<URL> otherJars = getOtherJars();
	        for (URL otherJar : otherJars) {
	            IPath otherJarPath = new Path(otherJar.getPath());
	            newEntries.add(newLibraryEntry(otherJarPath,
	                    null, null, null,
	                    new IClasspathAttribute[0], true));
            }
	        
	        entries = newEntries.toArray(new IClasspathEntry[0]);
        } catch (Exception e) {
        	GroovyCore.logException("Problem finding groovy runtime", e);
        	entries = new IClasspathEntry[0];
        }
    }


    /*
     * get the asm-*.jar, asm-tree-*.jar, and antlr-*.jar
     */
    @SuppressWarnings("unchecked")
    private List<URL> getOtherJars() throws IOException {
        Bundle groovyBundle = Platform.getBundle("org.codehaus.groovy");
        Enumeration<URL> enu = groovyBundle.findEntries("", "asm-*.jar", false);
        List<URL> urls = new ArrayList<URL>(2);

        while (enu.hasMoreElements()) {
            urls.add(resolve(enu.nextElement()));
        }
        
        enu = groovyBundle.findEntries("", "antlr-*.jar", false);
        if (enu.hasMoreElements()) {
            urls.add(resolve(enu.nextElement()));
        } else {
            throw new RuntimeException("Could not find antlr jar");
        }
        return urls;
    }
    
    
    /**
     * Returns the groovy-*.jar that is used in the Eclipse project. We know
     * there should only be one specified in the header for org.codehaus.groovy
     * right now.
     * 
     * @return Returns the names of the jars that are exported by the
     *         org.codehaus.groovy project.
     * @throws BundleException
     */
    @SuppressWarnings("unchecked")
    private URL getExportedGroovyAllJar() {
        try {
        	Bundle groovyBundle = Platform.getBundle("org.codehaus.groovy");
        	Enumeration<URL> enu = groovyBundle.findEntries("", "groovy-*.jar", false);
        	while (enu.hasMoreElements()) {
        		URL jar = enu.nextElement();
        		if (jar.getFile().indexOf("-sources") == -1 &&
        				jar.getFile().indexOf("-javadoc") == -1 &&
        				jar.getFile().indexOf("-eclipse") == -1) {
        			// remove the "reference:/" protocol
        			jar = resolve(jar);
        			return jar;
        		}
        	}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException("Could not find groovy jar");
    }
    
    public String getDescription() {
        return DESC;
    }

    public int getKind() {
        return K_APPLICATION;
    }

    public IPath getPath() {
        return CONTAINER_ID;
    }
}
