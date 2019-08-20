/*
 * Copyright 2009-2019 the original author or authors.
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

import static org.eclipse.jdt.core.JavaCore.newClasspathAttribute;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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

public class GroovyClasspathContainer implements IClasspathContainer {

    public static final String DESC = "Groovy Libraries";

    public static final IPath CONTAINER_ID = new Path("GROOVY_SUPPORT");

    public static final IClasspathAttribute MINIMAL_ATTRIBUTE = newClasspathAttribute("minimal", "true");

    private IJavaProject project;

    private IClasspathEntry[] entries;

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
                    attrs.add(newIsTestClasspathAttribute());
                }
                if (docPath != null) {
                    attrs.add(newJavadocLocationClasspathAttribute(docPath));
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
     * Finds all the jars in the ~/.groovy/lib directory.
     */
    private static Collection<IClasspathEntry> getGroovyJarsInDotGroovyLib() {
        File[] jars = CompilerUtils.findJarsInDotGroovyLocation();
        Predicate<File> isSources = file -> file.getName().endsWith("-sources.jar");
        Predicate<File> isJavadoc = file -> file.getName().endsWith("-javadoc.jar");

        return Arrays.stream(jars).filter(isSources.or(isJavadoc).negate()).map(file -> {
            IPath jarPath = new Path(file.getAbsolutePath());

            String srcName = jarPath.removeFileExtension().lastSegment().replace("-indy", "") + "-sources.jar";
            String docName = jarPath.removeFileExtension().lastSegment().replace("-indy", "") + "-javadoc.jar";

            // check for sources
            IPath srcPath = Arrays.stream(jars)
                .filter(isSources.and(srcFile -> srcFile.getName().equals(srcName)))
                .findFirst().map(srcFile -> new Path(srcFile.getAbsolutePath())).orElse(null);

            // check for javadoc
            IPath docPath = Arrays.stream(jars)
                .filter(isJavadoc.and(docFile -> docFile.getName().equals(docName)))
                .findFirst().map(docFile -> new Path(docFile.getAbsolutePath())).orElse(null);

            IClasspathAttribute[] icas = (docPath == null ? null : new IClasspathAttribute[] {newJavadocLocationClasspathAttribute(docPath)});

            return newLibraryEntry(jarPath, srcPath, null, null, icas, true);
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

    private static IClasspathAttribute newIsTestClasspathAttribute() {
        return newClasspathAttribute(IClasspathAttribute.TEST, "true");
    }

    private static IClasspathAttribute newJavadocLocationClasspathAttribute(IPath docPath) {
        try {
            String name = IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME;
            String path = docPath.toFile().toURI().toURL().toString();
            return newClasspathAttribute(name, "jar:" + path + "!/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
