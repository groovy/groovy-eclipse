/*
 * Copyright 2009-2020 the original author or authors.
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
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

public class GroovyClasspathContainer implements IClasspathContainer {

    public static final String ID = "GROOVY_SUPPORT";

    public static final String NAME = "Groovy Libraries";

    //

    private final IPath containerPath;

    private IClasspathEntry[] entries;

    public GroovyClasspathContainer(final IPath containerPath) {
        this.containerPath = containerPath;
    }

    @Override
    public synchronized IClasspathEntry[] getClasspathEntries() {
        if (entries == null) {
            entries = resolveEntries(containerPath);
        }
        return entries;
    }

    @Override
    public String getDescription() {
        return NAME;
    }

    @Override
    public int getKind() {
        return K_APPLICATION;
    }

    @Override
    public IPath getPath() {
        return containerPath;
    }

    public synchronized void reset() {
        entries = null;
    }

    private static IClasspathEntry[] resolveEntries(final IPath containerPath) {
        try {
            boolean minimalLibraries = containerPath.lastSegment().equals("minimal");

            Set<IPath> libraries = new LinkedHashSet<>();
            libraries.add(CompilerUtils.getExportedGroovyAllJar());
            if (!minimalLibraries) {
                libraries.addAll(CompilerUtils.getExtraJarsForClasspath());
            }

            List<IClasspathEntry> cpEntries = new ArrayList<>(libraries.size());

            for (IPath jarPath : libraries) {
                // check for sources
                IPath srcPath = CompilerUtils.getJarInGroovyLib(jarPath.removeFileExtension().lastSegment().replace("-indy", "") + "-sources.jar");

                // check for javadoc
                IPath docPath = CompilerUtils.getJarInGroovyLib(jarPath.removeFileExtension().lastSegment().replace("-indy", "") + "-javadoc.jar");

                List<IClasspathAttribute> attrs = new ArrayList<>();
                if (jarPath.lastSegment().startsWith("groovy-test")) {
                    attrs.add(newClasspathAttribute(IClasspathAttribute.TEST, "true"));
                }
                if (docPath != null) {
                    attrs.add(newJavadocLocationClasspathAttribute(docPath));
                }

                cpEntries.add(newLibraryEntry(jarPath, srcPath, null, null, attrs.toArray(new IClasspathAttribute[attrs.size()]), true));
            }

            if (!minimalLibraries && useGroovyLibs(containerPath)) {
                cpEntries.addAll(getJarsInDotGroovyLib());
            }

            return cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
        } catch (Exception e) {
            GroovyCore.logException("Failed to populate " + NAME, e);
            return new IClasspathEntry[0];
        }
    }

    private static boolean useGroovyLibs(final IPath containerPath) {
        if (containerPath.lastSegment().equals("user-libs=false")) {
            return false;
        }
        if (containerPath.lastSegment().equals("user-libs=true")) {
            return true;
        }
        // defer to workspace preference
        return GroovyCoreActivator.getDefault().getPreference(
            PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true);
    }

    //--------------------------------------------------------------------------

    /**
     * Finds all the jars in the<code>~/.groovy/lib</code> directory.
     */
    private static Collection<IClasspathEntry> getJarsInDotGroovyLib() {
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

    public static String getLegacyUserLibsPreference(final IJavaProject project) {
        return new ProjectScope(project.getProject()).getNode(GroovyCoreActivator.PLUGIN_ID).get("groovy.classpath.use.groovy,lib", "default");
    }

    public static boolean hasLegacyMinimalAttribute(final IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("minimal")) {
                return Boolean.parseBoolean(attribute.getValue());
            }
        }
        return false;
    }

    public static boolean hasMinimalAttribute(final IClasspathEntry entry) {
        if (entry != null) {
            if (entry.getPath().lastSegment().equals("minimal")) {
                return true;
            }
            return hasLegacyMinimalAttribute(entry);
        }
        return false;
    }

    private static IClasspathAttribute newJavadocLocationClasspathAttribute(final IPath docPath) {
        try {
            String name = IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME;
            String path = docPath.toFile().toURI().toURL().toString();
            return newClasspathAttribute(name, "jar:" + path + "!/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
