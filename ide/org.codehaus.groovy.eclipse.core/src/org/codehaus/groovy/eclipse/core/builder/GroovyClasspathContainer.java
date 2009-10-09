 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.builder;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;
import static org.eclipse.core.runtime.FileLocator.resolve;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
	        entries = newEntries.toArray(new IClasspathEntry[0]);
        } catch (Exception e) {
        	GroovyCore.logException("Problem finding groovy runtime", e);
        	entries = new IClasspathEntry[0];
        }
    }


    
    /**
     * Returns the groovy-all-*.jar that is used in the Eclipse project. We know
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
        	Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
        	Enumeration<URL> enu = groovyBundle.findEntries("lib", "groovy-all-*.jar", false);
        	if (enu == null) {
        	    // in some versions of the plugin, the groovy-all jar is in the base directory of the plugins
        	    enu = groovyBundle.findEntries("", "groovy-all-*.jar", false);
        	}
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
		throw new RuntimeException("Could not find groovy all jar");
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
