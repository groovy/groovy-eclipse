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
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathAttribute;

public class GroovyClasspathContainer implements IClasspathContainer {
    public static final Path CONTAINER_ID = new Path("GROOVY_SUPPORT");

    public static final IClasspathAttribute MINIMAL_ATTRIBUTE = new ClasspathAttribute("minimal", "true");

    public static final String DESC = "Groovy Libraries";

    public static final IClasspathAttribute[] MINIMAL_ATTRIBUTE_ARR = new IClasspathAttribute[] { MINIMAL_ATTRIBUTE };

    private IClasspathEntry[] entries;

    private IProject project;

    public GroovyClasspathContainer(IProject project) {
        this.project = project;
    }

    public synchronized IClasspathEntry[] getClasspathEntries() {
    	if (entries == null) {
    		updateEntries();
    	}
        return entries;
    }

    synchronized void reset() {
        entries = null;
    }

    private void updateEntries() {
        final List<IClasspathEntry> newEntries = newList();
        try {
	    	URL groovyURL = CompilerUtils.getExportedGroovyAllJar();
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

            if (!hasMinimalAttribute(GroovyRuntime.getGroovyClasspathEntry(JavaCore.create(project)))) {
                URL[] extraJars = CompilerUtils.getExtraJarsForClasspath();
                for (URL jar : extraJars) {
                    IPath jarPath = new Path(jar.getPath());
                    newEntries.add(newLibraryEntry(jarPath, null, null));
                }

                if (useGroovyLibs()) {
                    newEntries.addAll(getGroovyJarsInDotGroovyLib());
                }
	        }
	        entries = newEntries.toArray(new IClasspathEntry[0]);
        } catch (Exception e) {
        	GroovyCore.logException("Problem finding groovy runtime", e);
        	entries = new IClasspathEntry[0];
        }
    }

    public static boolean hasMinimalAttribute(IClasspathEntry entry) throws JavaModelException {
        if (entry == null) {
            return false;
        }
        IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
        for (IClasspathAttribute attribute : extraAttributes) {
            if (attribute.getName().equals(MINIMAL_ATTRIBUTE.getName()) && Boolean.valueOf(attribute.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean useGroovyLibs() {
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences projectNode = projectScope
                .getNode(GroovyCoreActivator.PLUGIN_ID);
        String val = projectNode.get(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB, "default");
        if (val.equals(Boolean.TRUE.toString())) {
            return true;
        } else if (val.equals(Boolean.FALSE.toString())) {
            return false;
        } else {
            return GroovyCoreActivator.getDefault().getPreference(
                    PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true);
        }
    }

    /**
     * Finds all the jars in the ~/.groovy/lib directory and adds them
     * to the classpath
     * @return
     */
    private Collection<IClasspathEntry> getGroovyJarsInDotGroovyLib() {
        File[] files = CompilerUtils.findJarsInDotGroovyLocation();
        final List<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>(files.length);
        for (File file : files) {
            IClasspathEntry entry = newLibraryEntry(new Path(file.getAbsolutePath()),
                    null, null, null,
                    new IClasspathAttribute[0], true);
            newEntries.add(entry);
        }
        return newEntries;
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
