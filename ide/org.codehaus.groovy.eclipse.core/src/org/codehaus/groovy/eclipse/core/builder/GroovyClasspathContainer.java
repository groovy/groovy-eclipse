/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.builder;

import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.ClasspathAttribute;

public class GroovyClasspathContainer implements IClasspathContainer {

    public static final String DESC = "Groovy Libraries";

    public static final IPath CONTAINER_ID = new Path("GROOVY_SUPPORT");

    public static final IClasspathAttribute MINIMAL_ATTRIBUTE = new ClasspathAttribute("minimal", "true");

    private IClasspathEntry[] entries;

    private IJavaProject project;

    public GroovyClasspathContainer(IJavaProject project) {
        this.project = project;
    }

    @Override
    public synchronized IClasspathEntry[] getClasspathEntries() {
        if (entries == null) {
            updateEntries();
        }
        return entries;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public int getKind() {
        return K_APPLICATION;
    }

    @Override
    public IPath getPath() {
        return CONTAINER_ID;
    }

    synchronized void reset() {
        entries = null;
    }

    private void updateEntries() {
        try {
            boolean minimalLibraries = hasMinimalAttribute(GroovyRuntime.getGroovyClasspathEntry(project));

            Set<IPath> libraries = new LinkedHashSet<>();
            libraries.add(CompilerUtils.getExportedGroovyAllJar());
            if (!minimalLibraries) {
                libraries.addAll(CompilerUtils.getExtraJarsForClasspath());
            }

            final List<IClasspathEntry> cpEntries = new ArrayList<>(libraries.size());

            for (IPath jarPath : libraries) {
                // check for sources
                IPath srcPath = CompilerUtils.getJarInGroovyLib(jarPath.removeFileExtension().lastSegment().replace("-indy", "") + "-sources.jar");

                // check for javadoc
                IPath docPath = CompilerUtils.getJarInGroovyLib(jarPath.removeFileExtension().lastSegment().replace("-indy", "") + "-javadoc.jar");

                List<IClasspathAttribute> attrs = new ArrayList<>();
                if (jarPath.lastSegment().startsWith("groovy-test")) {
                    attrs.add(newTestClasspathAttribute());
                }
                if (docPath != null) {
                    attrs.add(new ClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, docPath.toFile().toURI().toURL().toString()));
                }

                cpEntries.add(newLibraryEntry(jarPath, srcPath, null, null, attrs.toArray(new IClasspathAttribute[attrs.size()]), true));
            }

            if (!minimalLibraries && useGroovyLibs()) {
                cpEntries.addAll(getGroovyJarsInDotGroovyLib());
            }

            entries = cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);

        } catch (Exception e) {
            GroovyCore.logException("Problem finding Groovy runtime", e);

            entries = new IClasspathEntry[0];
        }
    }

    private boolean useGroovyLibs() {
        IScopeContext projectScope = new ProjectScope(project.getProject());
        IEclipsePreferences projectNode = projectScope.getNode(GroovyCoreActivator.PLUGIN_ID);
        String val = projectNode.get(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB, "default");
        if (val.equals(Boolean.TRUE.toString())) {
            return true;
        } else if (val.equals(Boolean.FALSE.toString())) {
            return false;
        } else {
            return GroovyCoreActivator.getDefault().getPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Finds all the jars in the ~/.groovy/lib directory and adds them to the classpath.
     */
    private static Collection<IClasspathEntry> getGroovyJarsInDotGroovyLib() {
        return Arrays.stream(CompilerUtils.findJarsInDotGroovyLocation()).map(file -> {
            IPath path = new Path(file.getAbsolutePath());
            IClasspathAttribute[] icas = (!file.getName().startsWith("groovy-test")
                ? null : new IClasspathAttribute[] {newTestClasspathAttribute()});
            return newLibraryEntry(path, null, null, null, icas, true);
        }).collect(Collectors.toList());
    }

    public static boolean hasMinimalAttribute(IClasspathEntry entry) {
        if (entry != null) {
            IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
            for (IClasspathAttribute attribute : extraAttributes) {
                if (attribute.getName().equals(MINIMAL_ATTRIBUTE.getName())) {
                    return Boolean.parseBoolean(attribute.getValue());
                }
            }
        }
        return false;
    }

    private static IClasspathAttribute newTestClasspathAttribute() {
        return new ClasspathAttribute(/*IClasspathAttribute.TEST*/"test", "true");
    }
}
