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

/**
 * @author Andrew Eisenberg
 * @created Sep 22, 2009
 *
 */
public class CompilerUtils {


    /**
     * @return
     */
    public static String getGroovyVersion() {
        Bundle groovyBundle = getActiveGroovyBundle();
        return groovyBundle != null ? groovyBundle.getVersion().toString() : "NONE";
    }

    public static String getOtherVersion() {
        return isGroovy18DisabledOrMissing() ? "1.8" : "1.7";
    }

    public static boolean isGroovy18DisabledOrMissing() {
        BundleDescription disabled18Bundle = null;
        disabled18Bundle = getDisabled18BundleDescription();
        if (disabled18Bundle != null) {
            return true;
        }

        // it might be that there is no 1.8 jar. If the 1.8 jar is missing, we
        // use 1.7
        Bundle[] active = Platform.getBundles("org.codehaus.groovy", "1.8.0");
        return active == null || active.length == 0;
    }

    /**
     * @param disabled17Bundle
     * @return
     */
    private static BundleDescription getDisabled18BundleDescription() {
        BundleDescription[] bundles = Platform.getPlatformAdmin().getState(false).getDisabledBundles();
        for (BundleDescription bundle : bundles) {
            if (bundle.getSymbolicName().equals("org.codehaus.groovy") &&
 bundle.getVersion().getMajor() == 1
                    && bundle.getVersion().getMinor() == 8) {
                return bundle;
            }
        }
        return null;
    }

    public static Bundle getActiveGroovyBundle() {
        String version17 = "[1.7.0,1.8.0)";
        String version18 = "1.8.0";
        Bundle[] active = Platform.getBundles("org.codehaus.groovy", (isGroovy18DisabledOrMissing() ? version17 : version18));
        return active.length > 0 ? active[0] : null;
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
    public static URL getExportedGroovyAllJar() {
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

    /**
     * finds and returns the extra jars that belong inside the Groovy Classpath
     * Container
     *
     * @return jline, servlet-api, ivy, and commons-cli
     */
    @SuppressWarnings("unchecked")
    public static URL[] getExtraJarsForClasspath() {
        try {
            Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
            Enumeration<URL> enu = groovyBundle.findEntries("lib", "*.jar", false);
            if (enu == null) {
                // in some versions of the plugin, the groovy-all jar is in the
                // base directory of the plugins
                enu = groovyBundle.findEntries("", "*.jar", false);
            }
            List<URL> urls = new ArrayList<URL>(4);
            while (enu.hasMoreElements()) {
                URL jar = enu.nextElement();
                if (jar.getFile().indexOf("jline") >= 0 || jar.getFile().indexOf("servlet-api") >= 0
                        || jar.getFile().indexOf("ivy") >= 0 || jar.getFile().indexOf("commons") >= 0) {
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
    public static IStatus switchVersions(boolean toVersion18) {
        String version17 = "[1.7.0,1.8.0)";
        String version18 = "1.8.0";


        try {
            State state = ((StateManager) Platform.getPlatformAdmin()).getSystemState();
            if (toVersion18) {
                BundleDescription disabled18 = getDisabled18BundleDescription();
                if (disabled18 != null) {
                    // remove the disabled info so that we can find the 1.8
                    // version of the plugin
                    DisabledInfo info = createDisabledInfo(state, disabled18.getBundleId());
                    Platform.getPlatformAdmin().removeDisabledInfo(info);
                }
            }

            Bundle[] toDisable = Platform.getBundles("org.codehaus.groovy", (toVersion18 ? version17 : version18));
            Bundle toEnable = toVersion18 ? getHighestVersion(state, 8) : getHighestVersion(state, 7);

            if (toDisable == null || toDisable.length == 0) {
                throw new Exception("Could not find any " + (toVersion18 ? "1.7" : "1.8") + " groovy version to disable");
            }
            if (toEnable == null) {
                throw new Exception("Could not find any " + (toVersion18 ? "1.8" : "1.7") + " groovy version to enable");
            }

            for (Bundle bundle : toDisable) {
                // bundle.stop();
                if (!toVersion18) {
                    DisabledInfo info = createDisabledInfo(state, bundle.getBundleId());
                    Platform.getPlatformAdmin().addDisabledInfo(info);
                }
            }
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
     * @param minorVersion
     * @return the highest version of the org.codehause.groovy bundle with
     * the specified minor version number
     */
    private static Bundle getHighestVersion(State state, int minorVersion) {
        BundleDescription[] allGroovyBundles = state.getBundles("org.codehaus.groovy");
        BundleDescription highestMinorVersion = null;
        for (BundleDescription groovyBundle : allGroovyBundles) {
            if (groovyBundle.getVersion().getMinor() == minorVersion) {
                if (highestMinorVersion == null) {
                    highestMinorVersion = groovyBundle;
                } else {
                    highestMinorVersion = highestMinorVersion.getVersion()
                        .compareTo(groovyBundle.getVersion()) == 1 ?
                                highestMinorVersion : groovyBundle;
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
