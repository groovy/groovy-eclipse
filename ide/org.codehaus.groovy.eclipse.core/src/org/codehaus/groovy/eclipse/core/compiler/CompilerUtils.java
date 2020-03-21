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
package org.codehaus.groovy.eclipse.core.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import groovy.lang.GroovySystem;

import org.codehaus.groovy.eclipse.chooser.CompilerChooser;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.groovy.core.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class CompilerUtils {

    public static String getGroovyVersion() {
        return GroovySystem.getVersion();
    }

    public static Bundle getActiveGroovyBundle() {
        return CompilerChooser.getInstance().getActiveBundle();
    }

    public static SpecifiedVersion getActiveGroovyVersion() {
        return CompilerChooser.getInstance().getActiveSpecifiedVersion();
    }

    public static Version getBundleVersion(SpecifiedVersion version) {
        return CompilerChooser.getInstance().getAssociatedVersion(version);
    }

    public static boolean isGroovyVersionDisabledOrMissing(SpecifiedVersion version) {
        return getActiveGroovyVersion() == version;
    }

    public static SortedSet<SpecifiedVersion> getAllGroovyVersions() {
        SpecifiedVersion[] versions = CompilerChooser.getInstance().getAllSpecifiedVersions();
        // remove dups and sort
        SortedSet<SpecifiedVersion> allVersions = new TreeSet<>();
        for (SpecifiedVersion version : versions) {
            allVersions.add(version);
        }
        return allVersions;
    }

    /**
     * Swtiches to or from groovy version 1.6.x depending on the boolean passed in.
     * A restart is required immediately after or else many exceptions will be thrown.
     *
     * @return {@link Status.OK_STATUS} if successful or error status that contains the exception thrown otherwise
     */
    public static IStatus switchVersions(SpecifiedVersion fromVersion, SpecifiedVersion toVersion) {
        try {
            // store new version in Eclipse preferences
            CompilerChooser.getInstance().storeVersion(toVersion);
            return Status.OK_STATUS;
        } catch (Exception e) {
            GroovyCore.logException(e.getMessage(), e);
            return new Status(IStatus.ERROR, GroovyCoreActivator.PLUGIN_ID, e.getMessage() + "\n\nSee the error log for more information.", e);
        }
    }

    /**
     * @return Best guess at the .groovy location (usually in user.home).
     *         If user.home can't be found, then null is returned
     */
    public static String getDotGroovyLocation() {
        String home = System.getProperty("user.home");
        if (home != null) {
            home += "/.groovy";
        }
        return home;
    }

    private static String getDotGroovyLibLocation() {
        String home = getDotGroovyLocation();
        if (home != null) {
            home += "/lib";
        }
        return home;
    }

    public static SpecifiedVersion getCompilerLevel(IProject project) {
        SpecifiedVersion version = SpecifiedVersion.UNSPECIFIED;
        if (GroovyNature.hasGroovyNature(project)) {
            String groovyCompilerLevelStr = Activator.getDefault().getGroovyCompilerLevel(project);
            if (groovyCompilerLevelStr != null) {
                version = SpecifiedVersion.findVersionFromString(groovyCompilerLevelStr);
            }
        }
        return version;
    }

    public static SpecifiedVersion getWorkspaceCompilerLevel() {
        String groovyVersion = getGroovyVersion();
        // convert from major.minor.micro to major.minor
        int dotIndex = groovyVersion.lastIndexOf('.');
        if (dotIndex > 0) {
            groovyVersion = groovyVersion.substring(0, dotIndex);
        }
        return SpecifiedVersion.findVersionFromString(groovyVersion);
    }

    public static void setCompilerLevel(IProject project, SpecifiedVersion projectLevel) {
        setCompilerLevel(project, projectLevel, false);
    }

    public static void setCompilerLevel(IProject project, SpecifiedVersion projectLevel, boolean assertCompatible) {
        Activator.getDefault().setGroovyCompilerLevel(project, projectLevel.versionName);
        if (assertCompatible) {
            if (projectVersionMatchesWorkspaceVersion(projectLevel)) {
                removeCompilermMismatchProblem(project);
            } else {
                addCompilerMismatchError(project, projectLevel);
            }
        }
    }

    /**
     * Checks that the compiler level of the project matches that of the workspace.
     * If a mismatch, then a marker is added to the project.
     */
    public static void addCompilerMismatchError(IProject project, SpecifiedVersion projectLevel) {
        try {
            SpecifiedVersion workspaceLevel = CompilerUtils.getWorkspaceCompilerLevel();
            IMarker marker = project.getProject().createMarker(CompilerCheckerParticipant.COMPILER_MISMATCH_PROBLEM);
            marker.setAttribute(IMarker.MESSAGE, "Groovy: compiler mismatch: project level is " + projectLevel.toReadableVersionString() + ", workspace level is " + workspaceLevel.toReadableVersionString());
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            marker.setAttribute(IMarker.LOCATION, project.getName());
        } catch (CoreException e) {
            GroovyCore.logException("Error checking Groovy project compiler level compatibility for " + project.getName(), e);
        }
    }

    public static void removeCompilermMismatchProblem(IProject project) {
        try {
            IMarker[] findMarkers = project.findMarkers(CompilerCheckerParticipant.COMPILER_MISMATCH_PROBLEM, true, IResource.DEPTH_ZERO);
            for (IMarker marker : findMarkers) {
                marker.delete();
            }
        } catch (CoreException e) {
            GroovyCore.logException("Error checking Groovy project compiler level compatibility for " + project.getName(), e);
        }
    }

    public static void addMultipleCompilersOnClasspathError(IProject project, SpecifiedVersion compiler1, SpecifiedVersion compiler2) {
        try {
            IMarker marker = project.getProject().createMarker(CompilerCheckerParticipant.COMPILER_MISMATCH_PROBLEM);
            marker.setAttribute(IMarker.MESSAGE, "Multiple Groovy compilers found on classpath. Continuing with compilation will produce unpredictible results. " +
                "Remove a compiler before continuing.\n" + "Found " + compiler1.toReadableVersionString() + " and " + compiler2.toReadableVersionString());
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            marker.setAttribute(IMarker.LOCATION, project.getName());
        } catch (CoreException e) {
            GroovyCore.logException("Error checking Groovy project compiler level compatibility for " + project.getName(), e);
        }
    }

    public static boolean projectVersionMatchesWorkspaceVersion(SpecifiedVersion version) {
        if (version == SpecifiedVersion.UNSPECIFIED || version == SpecifiedVersion.DONT_CARE) {
            return true;
        }
        return version == getWorkspaceCompilerLevel();
    }

    /**
     * Returns the Groovy jar that is used in the Eclipse project.
     */
    public static IPath getExportedGroovyAllJar() {
        Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
        for (URL jarUrl : Collections.list(groovyBundle.findEntries("lib", "groovy-*.jar", false))) {
            if (jarUrl.getFile().matches(".+/groovy(?:-all)?-\\d+\\.\\d+\\.\\d+(?:-indy)?\\.jar")) {
                return toFilePath(jarUrl);
            }
        }
        throw new RuntimeException("Could not find groovy jar");
    }

    /**
     * Returns the extra jars that belong inside the Groovy Classpath Container.
     */
    public static List<IPath> getExtraJarsForClasspath() {
        List<IPath> jarPaths = new ArrayList<>();
        Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
        for (URL jarUrl : Collections.list(groovyBundle.findEntries("lib", "*.jar", false))) {
            if (!jarUrl.getFile().endsWith("-javadoc.jar") && !jarUrl.getFile().endsWith("-sources.jar")) {
                jarPaths.add(toFilePath(jarUrl));
            }
        }
        jarPaths.remove(getExportedGroovyAllJar());
        return jarPaths;
    }

    /**
     * Finds a specific jar in the groovy lib folder, or null if not found.
     *
     * @param jarName the name of the jar
     * @return the full, resolved url to the jar
     */
    public static IPath getJarInGroovyLib(String jarName) {
        Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
        Enumeration<URL> enu = groovyBundle.findEntries("lib", jarName, false);
        if (enu != null && enu.hasMoreElements()) {
            return toFilePath(enu.nextElement());
        }
        return null;
    }

    public static File[] findJarsInDotGroovyLocation() {
        String home = getDotGroovyLibLocation();
        if (home != null) {
            File libDir = new File(home);
            if (libDir.isDirectory()) {
                File[] files = libDir.listFiles((dir, name) -> !(new File(dir, name).isDirectory()) && name.endsWith(".jar"));
                return files;
            }
        }
        return new File[0];
    }

    /**
     * Converts "bundleentry:/514.fwk1995952705/lib/groovy-all-x.y.z.jar" to file path.
     */
    private static IPath toFilePath(URL url) {
        try {
            url = FileLocator.toFileURL(url);
            return new Path(url.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
