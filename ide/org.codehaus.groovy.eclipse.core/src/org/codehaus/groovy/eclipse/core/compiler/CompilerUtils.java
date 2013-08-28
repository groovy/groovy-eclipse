 /*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.compiler;

import static org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion.UNSPECIFIED;
import static org.eclipse.core.runtime.FileLocator.resolve;
import groovy.lang.GroovySystem;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.frameworkadapter.util.CompilerChooser;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * @author Andrew Eisenberg
 * @created Sep 22, 2009
 *
 */
public class CompilerUtils {
    /**
     * Note: Used by Grails tooling
     */

    public static String getGroovyVersion() {
        return GroovySystem.getVersion();
    }

    /**
     * Note: Used by Grails tooling
     */
    public static boolean isGroovyVersionDisabledOrMissing(SpecifiedVersion version) {
        return getActiveGroovyVersion() == version;
    }

    public static SpecifiedVersion getActiveGroovyVersion() {
        return CompilerChooser.getInstance().getActiveSpecifiedVersion();
    }

    public static Version getBundleVersion(SpecifiedVersion version) {
        return CompilerChooser.getInstance().getAssociatedVersion(version);
    }

    public static Bundle getActiveGroovyBundle() {
        return CompilerChooser.getInstance().getActiveBundle();
    }



    /**
     * Swtiches to or from groovy version 1.6.x depending on the boolean passed
     * in
     * A restart is required immediately after or else many exceptions will be
     * thrown.
     *
     * @return {@link Status.OK_STATUS} if successful or error status that
     *         contains the exception thrown otherwise
     */
    public static IStatus switchVersions(SpecifiedVersion fromVersion, SpecifiedVersion toVersion) {
        try {
            // store new version in Eclipse preferences
            CompilerChooser.getInstance().storeVersion(toVersion);
            return Status.OK_STATUS;
        } catch (Exception e) {
            GroovyCore.logException(e.getMessage(), e);
            return new Status(IStatus.ERROR, GroovyCoreActivator.PLUGIN_ID,
                            e.getMessage()
                            + "\n\nSee the error log for more information.", e);
        }
    }

    public static SortedSet<SpecifiedVersion> getAllGroovyVersions() {
        SpecifiedVersion[] versions = CompilerChooser.getInstance().getAllSpecifiedVersions();
        // remove dups and sort
        SortedSet<SpecifiedVersion> allVersions = new TreeSet<SpecifiedVersion>();
        for (SpecifiedVersion version : versions) {
            allVersions.add(version);
        }
        return allVersions;
    }

    private static String getDotGroovyLibLocation() {
        String home = getDotGroovyLocation();
        if (home != null) {
            home += "/lib";
        }
        return home;
    }

    public static SpecifiedVersion getCompilerLevel(IProject project) {
        SpecifiedVersion version = UNSPECIFIED;
        if (GroovyNature.hasGroovyNature(project)) {
            String groovyCompilerLevelStr = Activator.getDefault().getGroovyCompilerLevel(project);
            if (groovyCompilerLevelStr != null) {
                version = SpecifiedVersion.findVersionFromString(groovyCompilerLevelStr);
            }
        }
        return version;
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
     * Check that the compiler level of the project matches that of the
     * workspace
     * if a msmatch, then a marker is added to the project
     *
     * @param project
     * @param projectLevel
     */
    public static void addCompilerMismatchError(IProject project, SpecifiedVersion projectLevel) {
        try {
            SpecifiedVersion workspaceLevel = CompilerUtils.getWorkspaceCompilerLevel();
            IMarker marker = project.getProject().createMarker(CompilerCheckerParticipant.COMPILER_MISMATCH_PROBLEM);
            marker.setAttribute(IMarker.MESSAGE,
                    "Groovy: compiler mismatch Project level is: " + projectLevel.toReadableVersionString()
                            + " Workspace level is " + workspaceLevel.toReadableVersionString()
                    + "\nGroovy compiler level expected by the project does not match workspace compiler level. "
                    + "\nGo to Project properties -> Groovy compiler to set the Groovy compiler level for this project");
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            marker.setAttribute(IMarker.LOCATION, project.getName());
        } catch (CoreException e) {
            GroovyCore.logException("Error checking Groovy project compiler level compatibility for " + project.getName(), e);
        }
    }

    public static void removeCompilermMismatchProblem(IProject project) {
        try {
            IMarker[] findMarkers = project.findMarkers(CompilerCheckerParticipant.COMPILER_MISMATCH_PROBLEM, true,
                    IResource.DEPTH_ZERO);
            for (IMarker marker : findMarkers) {
                marker.delete();
            }
        } catch (CoreException e) {
            GroovyCore.logException("Error checking Groovy project compiler level compatibility for " + project.getName(), e);
        }
    }

    public static void addMultipleCompilersOnClasspathError(IProject project, SpecifiedVersion compiler1, SpecifiedVersion compiler2) {
        try {
            SpecifiedVersion workspaceLevel = CompilerUtils.getWorkspaceCompilerLevel();
            IMarker marker = project.getProject().createMarker(CompilerCheckerParticipant.COMPILER_MISMATCH_PROBLEM);
            marker.setAttribute(IMarker.MESSAGE,
                    "Multiple Groovy compilers found on classpath. Continuing with compilation will produce unpredictible results. "
                            + "Remove a compiler before continuing.\n" + "Found " + compiler1.toReadableVersionString() + " and "
                            + compiler2.toReadableVersionString());
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            marker.setAttribute(IMarker.LOCATION, project.getName());
        } catch (CoreException e) {
            GroovyCore.logException("Error checking Groovy project compiler level compatibility for " + project.getName(), e);
        }
    }

    public static boolean projectVersionMatchesWorkspaceVersion(SpecifiedVersion version) {
        if (version == UNSPECIFIED || version == SpecifiedVersion.DONT_CARE) {
            return true;
        } else {
            SpecifiedVersion workspaceCompilerLevel = getWorkspaceCompilerLevel();
            return version == workspaceCompilerLevel;
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
    public static URL getExportedGroovyAllJar() {
        try {
            Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
            if (groovyBundle == null) {
                throw new RuntimeException("Could not find groovy bundle");
            }
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

    private static boolean includeServlet = true;

    static {
        try {
            String p = System.getProperty("greclipse.includeServletInClasspathContainer","true");
            if (p.equalsIgnoreCase("false")) {
                includeServlet = false;
            }
        } catch (Exception e) {
            // likely security related
        }
    }

    /**
     * finds and returns the extra jars that belong inside the Groovy Classpath
     * Container
     *
     * @return jline, servlet-api, ivy, and commons-cli
     */
    public static URL[] getExtraJarsForClasspath() {
        try {
            Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
            Enumeration<URL> enu = groovyBundle.findEntries("lib", "*.jar", false);
            if (enu == null) {
                // in some versions of the plugin, the groovy-all jar is in the
                // base directory of the plugins
                enu = groovyBundle.findEntries("", "*.jar", false);
            }
            List<URL> urls = new ArrayList<URL>(9);
            while (enu.hasMoreElements()) {
                URL jar = enu.nextElement();
                if (!jar.getFile().contains("groovy")) {
                    if (includeServlet || jar.getFile().indexOf("servlet") == -1) {
                        // remove the "reference:/" protocol
                        jar = resolve(jar);
                        urls.add(jar);
                    }
                }
            }
            return urls.toArray(new URL[urls.size()]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds a specific jar in the groovy lib folder, or null if not found
     *
     * @param jarName the name of the jar
     * @return the full, resolved url to the jar
     * @throws IOException
     */
    public static URL getJarInGroovyLib(String jarName) throws IOException {
        Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
        Enumeration<URL> enu = groovyBundle.findEntries("lib", jarName, false);
        if (enu == null) {
            // in some versions of the plugin, the groovy-all jar is in the
            // base directory of the plugins
            enu = groovyBundle.findEntries("", jarName, false);
        }
        if (enu != null && enu.hasMoreElements()) {
            URL jar = enu.nextElement();
            jar = resolve(jar);
            if (jar.getFile().indexOf("sources") > 0 && enu.hasMoreElements()) {
                jar = enu.nextElement();
                jar = resolve(jar);
            }
            return jar;
        }
        return null;
    }

    public static URL findDSLDFolder() {
        Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
        Enumeration<URL> enu = groovyBundle.findEntries(".", "plugin_dsld_support", false);
        if (enu != null && enu.hasMoreElements()) {
            URL folder = enu.nextElement();
            // remove the "reference:/" protocol
            try {
                folder = resolve(folder);
                return folder;
            } catch (IOException e) {
                GroovyCore.logException("Exception when looking for DSLD folder", e);
            }
        }
        return null;

    }

    /**
     * @return Best guess at the .groovy location (usually in user.home).
     *         If user.home can't be found, then null is returned
     */
    public static String getDotGroovyLocation() {
        String home = FrameworkProperties.getProperty("user.home");
        if (home != null) {
            home += "/.groovy";
        }
        return home;
    }

    public static File[] findJarsInDotGroovyLocation() {
        String home = getDotGroovyLibLocation();
        if (home != null) {
            File libDir = new File(home);
            if (libDir.isDirectory()) {
                File[] files = libDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return !(new File(dir, name).isDirectory()) &&
                                name.endsWith(".jar");
                    }
                });
                return files;
            }
        }
        return new File[0];
    }

    public static SpecifiedVersion getWorkspaceCompilerLevel() {
        String groovyVersion = GroovySystem.getVersion();
        // convert from major.minor.micro to major.minor
        int dotIndex = groovyVersion.lastIndexOf('.');
        if (dotIndex > 0) {
            groovyVersion = groovyVersion.substring(0, dotIndex);
        }
        return SpecifiedVersion.findVersionFromString(groovyVersion);
    }
}
