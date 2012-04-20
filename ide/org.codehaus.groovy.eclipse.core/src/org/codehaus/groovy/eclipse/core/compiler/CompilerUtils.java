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
package org.codehaus.groovy.eclipse.core.compiler;

import static org.eclipse.core.runtime.FileLocator.resolve;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.frameworkadapter.util.CompilerLevelUtils;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
 * @author Andy Clement
 * @created Sep 22, 2009
 * 
 */
public class CompilerUtils {

    /**
     * Note: Used by Grails
     */
    public static String getGroovyVersion() {
        return getGroovyVersion(true);
    }

    public static String getGroovyVersion(boolean includeQualifier) {
        Bundle groovyBundle = getActiveGroovyBundle();
        if (groovyBundle == null) {
            return "NONE";
        }
        if (includeQualifier) {
            return groovyBundle.getVersion().toString();
        } else {
            Version v = groovyBundle.getVersion();
            return v.getMajor() + "." + v.getMinor() + "." + v.getMicro();
        }
    }

    public static String getOtherVersion() {
        return isGroovy18DisabledOrMissing() ? "1.8" : "1.7";
    }

    /**
     * @param the current version, major.minor.micro (e.g. "1.7.10")
     * @return array of versions that are candidates for switching to e.g.
     *         {"1.7","1.8"}
     */
    public static String[] getOtherVersions(String currentVersion) {
        if (currentVersion.startsWith("1.7.")) {
            return new String[] { "1.8", "2.0" };
        } else if (currentVersion.startsWith("1.8.")) {
            return new String[] { "1.7", "2.0" };
        } else if (currentVersion.startsWith("2.0.")) {
            return new String[] { "1.7", "1.8" };
        }
        return new String[] {};
    }

    public static boolean isGroovy17DisabledOrMissing() {
        BundleDescription disabled17Bundle = getDisabledBundleDescription(1, 7);
        if (disabled17Bundle != null) {
            return true;
        }
        Bundle[] active = Platform.getBundles("org.codehaus.groovy", "1.7.10");
        return active == null || active.length == 0;
    }

    public static boolean isGroovy18DisabledOrMissing() {
        BundleDescription disabled18Bundle = getDisabled18BundleDescription();
        if (disabled18Bundle != null) {
            return true;
        }
        Bundle[] active = Platform.getBundles("org.codehaus.groovy", "1.8.6");
        return active == null || active.length == 0;
    }

    public static boolean isGroovy20DisabledOrMissing() {
        BundleDescription disabled20Bundle = getDisabled20BundleDescription();
        if (disabled20Bundle != null) {
            return true;
        }
        Bundle[] active = Platform.getBundles("org.codehaus.groovy", "2.0.0");
        return active == null || active.length == 0;
    }

    private static BundleDescription getDisabledBundleDescription(int majorVersion, int minorVersion) {
        BundleDescription[] bundles = Platform.getPlatformAdmin().getState(false).getDisabledBundles();
        for (BundleDescription bundle : bundles) {
            if (bundle.getSymbolicName().equals("org.codehaus.groovy") && bundle.getVersion().getMajor() == majorVersion
                    && bundle.getVersion().getMinor() == minorVersion) {
                return bundle;
            }
        }
        return null;
    }

    private static BundleDescription getDisabled18BundleDescription() {
        BundleDescription[] bundles = Platform.getPlatformAdmin().getState(false).getDisabledBundles();
        for (BundleDescription bundle : bundles) {
            if (bundle.getSymbolicName().equals("org.codehaus.groovy") && bundle.getVersion().getMajor() == 1
                    && bundle.getVersion().getMinor() == 8) {
                return bundle;
            }
        }
        return null;
    }

    private static BundleDescription getDisabled20BundleDescription() {
        BundleDescription[] bundles = Platform.getPlatformAdmin().getState(false).getDisabledBundles();
        for (BundleDescription bundle : bundles) {
            if (bundle.getSymbolicName().equals("org.codehaus.groovy") && bundle.getVersion().getMajor() == 2
                    && bundle.getVersion().getMinor() == 0) {
                return bundle;
            }
        }
        return null;
    }

    public static Bundle getActiveGroovyBundle() {
        String version17 = "[1.7.0,1.7.99)";
        String version18 = "1.8.6";
        String version20 = "2.0.0";
        String versionToUse = version20;
        if (isGroovy20DisabledOrMissing()) {
            if (isGroovy18DisabledOrMissing()) {
                versionToUse = version17;
            } else {
                versionToUse = version18;
            }
        }
        Bundle[] active = Platform.getBundles("org.codehaus.groovy", versionToUse);
        return active != null && active.length > 0 ? active[0] : null;
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

    private static List<Bundle> accumulateBundlesForDisabling(String... versions) {
        List<Bundle> toDisable = new ArrayList<Bundle>();
        for (String version : versions) {
            Bundle[] forDisabling = Platform.getBundles("org.codehaus.groovy", version);
            if (forDisabling != null) {
                for (Bundle b : forDisabling) {
                    toDisable.add(b);
                }
            }
        }
        return toDisable;
    }

    /**
     * Attempt to switch to a specified version. The version is specified as
     * major.minor, for example "1.7".
     * A restart is required immediately after or else many exceptions will be
     * thrown.
     *
     * @param version to switch to, e.g. "1.7", "1.8" or "2.0"
     * @return {@link Status.OK_STATUS} if successful or error status that
     *         contains the exception thrown otherwise
     */
    public static IStatus switchVersions(String versionToSwitchTo) {
        String version17 = "[1.7.0,1.8.0)";
        String version18 = "[1.8.0,1.9.0)";
        String version20 = "[2.0.0,2.1.0)";
        boolean to17 = versionToSwitchTo.equals("1.7");
        boolean to18 = versionToSwitchTo.equals("1.8");
        boolean to20 = versionToSwitchTo.equals("2.0");

        try {
            State state = ((StateManager) Platform.getPlatformAdmin()).getSystemState();
            if (to17) {
                BundleDescription disabledBundle = getDisabledBundleDescription(1, 7);
                if (disabledBundle != null) {
                    // remove the disabled info so that we can find it
                    DisabledInfo info = createDisabledInfo(state, disabledBundle.getBundleId());
                    Platform.getPlatformAdmin().removeDisabledInfo(info);
                }
            }
            if (to18) {
                BundleDescription disabledBundle = getDisabled18BundleDescription();
                if (disabledBundle != null) {
                    // remove the disabled info so that we can find it
                    DisabledInfo info = createDisabledInfo(state, disabledBundle.getBundleId());
                    Platform.getPlatformAdmin().removeDisabledInfo(info);
                }
            }
            if (to20) {
                BundleDescription disabledBundle = getDisabled20BundleDescription();
                if (disabledBundle != null) {
                    // remove the disabled info so that we can find it
                    DisabledInfo info = createDisabledInfo(state, disabledBundle.getBundleId());
                    Platform.getPlatformAdmin().removeDisabledInfo(info);
                }
            }

            List<Bundle> toDisable = new ArrayList<Bundle>();
            if (to17) {
                toDisable = accumulateBundlesForDisabling(version18, version20);
            } else if (to18) {
                toDisable = accumulateBundlesForDisabling(version17, version20);
            } else if (to20) {
                toDisable = accumulateBundlesForDisabling(version17, version18);
            }

            Bundle toEnable = null;
            if (to17) {
                toEnable = getHighestVersion(state,1,7);
            } else if (to18) {
                toEnable = getHighestVersion(state,1,8);
            } else if (to20) {
                toEnable = getHighestVersion(state,2,0);
            }

            if (toDisable == null || toDisable.size() == 0) {
                String insert = "";
                if (to17)
                    insert = "1.8 or 2.0";
                else if (to18)
                    insert = "1.7 or 2.0";
                else if (to20)
                    insert = "1.7 or 1.8";
                throw new Exception("Could not find any " + insert + " groovy versions to disable");
            }
            if (toEnable == null) {
                String insert = "";
                if (to17)
                    insert = "1.8 or 2.0";
                else if (to18)
                    insert = "1.7 or 2.0";
                else if (to20)
                    insert = "1.7 or 1.8";
                throw new Exception("Could not find any " + insert + " groovy versions to enable");
            }

            for (Bundle bundle : toDisable) {
                // bundle.stop();
                DisabledInfo info = createDisabledInfo(state, bundle.getBundleId());
                Platform.getPlatformAdmin().addDisabledInfo(info);
                switch (bundle.getState()) {
                    case Bundle.ACTIVE:
                    case Bundle.INSTALLED:
                    case Bundle.STARTING:
                    case Bundle.RESOLVED:
                        bundle.stop();
                }
            }

            SpecifiedVersion sv = null;
            if (to17)
                sv = SpecifiedVersion._17;
            else if (to18)
                sv = SpecifiedVersion._18;
            else if (to20)
                sv = SpecifiedVersion._20;

            CompilerLevelUtils.writeConfigurationVersion(sv,
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


    /**
     * @param state the SystemState object
     * @param majorVersion
     * @param minorVersion
     * @return the highest version of the org.codehause.groovy bundle with
     *         the specified major/minor versions
     */
    private static Bundle getHighestVersion(State state, int majorVersion, int minorVersion) {
        BundleDescription[] allGroovyBundles = state.getBundles("org.codehaus.groovy");
        BundleDescription highestMinorVersion = null;
        for (BundleDescription groovyBundle : allGroovyBundles) {
            if (groovyBundle.getVersion().getMajor() == majorVersion) {
                if (groovyBundle.getVersion().getMinor() == minorVersion) {
                    if (highestMinorVersion == null) {
                        highestMinorVersion = groovyBundle;
                    } else {
                        highestMinorVersion = highestMinorVersion.getVersion().compareTo(groovyBundle.getVersion()) == 1 ? highestMinorVersion
                                : groovyBundle;
                    }
                }
            }
        }

        if (highestMinorVersion == null) {
            return null;
        }
        return Platform.getBundle("org.codehaus.groovy.eclipse.core").getBundleContext().getBundle(highestMinorVersion.getBundleId());
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
}
