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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.frameworkadapter.util.CompilerLevelUtils;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.internal.baseadaptor.StateManager;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.DisabledInfo;
import org.eclipse.osgi.service.resolver.State;
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
        BundleDescription groovyBundle = getActiveGroovyBundleDescription();
        return groovyBundle != null ? groovyBundle.getVersion().toString() : "NONE";
    }

    public static SpecifiedVersion getActiveGroovyVersion() {
        BundleDescription groovyBundle = getActiveGroovyBundleDescription();
        return SpecifiedVersion.findVersion(groovyBundle.getVersion());
    }

    /**
     * Note: Used by Grails tooling
     */
    public static boolean isGroovyVersionDisabledOrMissing(SpecifiedVersion version) {
        BundleDescription disabledBundle = null;
        disabledBundle = getDisabledBundleDescription(version);
        if (disabledBundle != null) {
            return true;
        }

        Bundle[] active = Platform.getBundles("org.codehaus.groovy", version.toVersionString());
        if (active == null) {
            return true;
        }

        // getBundles returns bundles with version >= specified version,
        // so must do one more check to see if there is a bundle where the
        // major.minor version matches
        for (Bundle bundle : active) {
            Version bundleVersion = bundle.getVersion();
            if (bundleVersion.getMajor() == version.majorVersion && bundleVersion.getMajor() == version.majorVersion) {
                return false;
            }
        }
        // no bundle with specifed version has been found
        return true;
    }

    public static BundleDescription getBundleDescription(SpecifiedVersion version) {
        BundleDescription[] active = getAllGroovyBundleDescriptions();
        // return highest bundle version that matches the major.minor specified
        // version
        for (BundleDescription bundle : active) {
            if (bundle.getVersion().getMajor() == version.majorVersion && bundle.getVersion().getMinor() == version.minorVersion) {
                return bundle;
            }
        }
        return null;
    }

    private static BundleDescription getActiveGroovyBundleDescription() {
        BundleDescription[] active = getAllGroovyBundleDescriptions();
        if (active == null || active.length == 0) {
            return null;
        }
        // go through each bundle in versioned order and see if it is disabled
        // The highest bundle that is not disabled is the active bundle
        BundleDescription[] disabled = Platform.getPlatformAdmin().getState(false).getDisabledBundles();
        for (BundleDescription bundle : active) {
            boolean isAvailable = true;
            for (BundleDescription d : disabled) {
                if (d.getVersion().equals(bundle.getVersion()) && d.getSymbolicName().equals(bundle.getSymbolicName())) {
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable) {
                return bundle;
            }
        }
        return null;
    }

    public static Bundle getActiveGroovyBundle() {
        BundleDescription bundleDesc = getActiveGroovyBundleDescription();
        if (bundleDesc == null) {
            return null;
        }
        Bundle[] allBundles = Platform.getBundles("org.codehaus.groovy", bundleDesc.getVersion().toString());
        if (allBundles == null || allBundles.length == 0) {
            return null;
        }
        return allBundles[0];
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
                    // remove the "reference:/" protocol
                    jar = resolve(jar);
                    urls.add(jar);
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
     * Swtiches to or from groovy version 1.6.x depending on the boolean passed
     * in
     * A restart is required immediately after or else many exceptions will be
     * thrown.
     *
     * @param toVersion16
     * @return {@link Status.OK_STATUS} if successful or error status that
     *         contains the exception thrown otherwise
     */
    public static IStatus switchVersions(SpecifiedVersion fromVersion, SpecifiedVersion toVersion) {
        try {
            State state = ((StateManager) Platform.getPlatformAdmin()).getSystemState();
            BundleDescription toBundle = getBundleDescription(toVersion);
            BundleDescription[] allBundles = getAllGroovyBundleDescriptions();
            if (toBundle == null) {
                throw new Exception("Could not find any " + toVersion + " groovy version to enable");
            }

            // go through all bundles and ensure disabled
            for (BundleDescription bundle : allBundles) {
                DisabledInfo info = createDisabledInfo(state, bundle.getBundleId());
                if (bundle.equals(toBundle)) {
                    // ensure enabled
                    Platform.getPlatformAdmin().removeDisabledInfo(info);
                } else {
                    // don't actually stop
                    // switch (bundle.getState()) {
                    // case Bundle.ACTIVE:
                    // case Bundle.INSTALLED:
                    // case Bundle.STARTING:
                    // case Bundle.RESOLVED:
                    // bundle.stop();
                    // }
                    // ensure disabled
                    Platform.getPlatformAdmin().addDisabledInfo(info);
                }
            }
            CompilerLevelUtils.writeConfigurationVersion(toVersion,
            // need to get the system bundle
                    GroovyCoreActivator.getDefault().getBundle().getBundleContext().getBundle(0).getBundleContext());
            return Status.OK_STATUS;
        } catch (Exception e) {
            GroovyCore.logException(e.getMessage(), e);
            return new Status(IStatus.ERROR, GroovyCoreActivator.PLUGIN_ID,
                            e.getMessage()
                            + "\n\nSee the error log for more information.", e);
        }
    }

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

    public static SortedSet<SpecifiedVersion> getAllGroovyVersions() {
        BundleDescription[] allBundles = getAllGroovyBundleDescriptions();
        SortedSet<SpecifiedVersion> allVersions = new TreeSet<SpecifiedVersion>();
        for (BundleDescription bundle : allBundles) {
            allVersions.add(SpecifiedVersion.findVersion(bundle.getVersion()));
        }
        return allVersions;
    }

    private static BundleDescription[] getAllGroovyBundleDescriptions() {
        BundleDescription[] bundles = Platform.getPlatformAdmin().getState(false).getBundles("org.codehaus.groovy");
        // sort by version
        Arrays.sort(bundles, new Comparator<BundleDescription>() {
            public int compare(BundleDescription l, BundleDescription r) {
                // reverse order so highest version is first
                return r.getVersion().compareTo(l.getVersion());
            }
        });
        return bundles;
    }

    private static BundleDescription getDisabledBundleDescription(SpecifiedVersion version) {
        BundleDescription[] bundles = Platform.getPlatformAdmin().getState(false).getDisabledBundles();
        for (BundleDescription bundle : bundles) {
            if (bundle.getSymbolicName().equals("org.codehaus.groovy") && bundle.getVersion().getMajor() == version.majorVersion
                    && bundle.getVersion().getMinor() == version.minorVersion) {
                return bundle;
            }
        }
        return null;
    }

    /**
     * @param state
     * @param bundle
     * @return
     */
    private static DisabledInfo createDisabledInfo(State state, long bundleId) {
        BundleDescription desc = state.getBundle(bundleId);
        DisabledInfo info = new DisabledInfo(
                "org.eclipse.pde.ui", //$NON-NLS-1$
                "Disabled via PDE", desc); //$NON-NLS-1$
        return info;
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
        if (assertCompatible && !projectVersionMatchesWorkspaceVersion(projectLevel)) {
            addCompilerMismatchError(project, projectLevel);
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
                    "Groovy compiler level expected by the project does not match workspace compiler level. "
                            + "\nProject compiler level is: " + projectLevel.toReadableVersionString()
                            + "\nWorkspace compiler level is " + workspaceLevel.toReadableVersionString()
                            + "\nGo to Project properties -> Groovy compiler to set the Groovy compiler level for this project");
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            marker.setAttribute(IMarker.LOCATION, project.getName());
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
                            + "Remove a compiler before coninuing.\n" + "Found " + compiler1.toReadableVersionString() + " and "
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

    static SpecifiedVersion getWorkspaceCompilerLevel() {
        String groovyVersion = GroovySystem.getVersion();
        // convert from major.minor.micro to major.minor
        int dotIndex = groovyVersion.lastIndexOf('.');
        if (dotIndex > 0) {
            groovyVersion = groovyVersion.substring(0, dotIndex);
        }
        return SpecifiedVersion.findVersionFromString(groovyVersion);
    }

}
