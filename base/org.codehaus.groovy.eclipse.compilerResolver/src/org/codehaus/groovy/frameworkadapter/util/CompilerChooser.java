/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.frameworkadapter.util;

import static org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion.UNSPECIFIED;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This class chooses which Groovy compiler to use on startup
 * And it provides some other useful information about the compiler
 * @author Andrew Eisenberg
 * @created 2013-04-12
 */
@SuppressWarnings("deprecation")
public class CompilerChooser {

    private static CompilerChooser INSTANCE;

    private static final String GROOVY_COMPILER_LEVEL = "groovy.compiler.level";
    private static final String DASH_GROOVY_COMPILER_LEVEL = "-groovy.compiler.level";
    private static final String ECLIPSE_COMMANDS = "eclipse.commands";
    private static final String GROOVY_PLUGIN_ID = "org.codehaus.groovy";
    private Version[] allVersions = new Version[0];
    private SpecifiedVersion[] allSpecifiedVersions = new SpecifiedVersion[0];
    private int activeIndex = -1;

    private boolean initialized;

    CompilerChooser() {
        INSTANCE = this;
    }

    public static CompilerChooser getInstance() {
        return INSTANCE;
    }

    void initialize(final BundleContext context) throws BundleException {
        if (!initialized) {
            initialized = true;
            doInitialize(context);
        }
    }

    public boolean isInitiailzed() {
        return initialized;
    }

    private void doInitialize(BundleContext context) throws BundleException {

        SpecifiedVersion specifiedVersion = findSysPropVersion();
        if (specifiedVersion == SpecifiedVersion.UNSPECIFIED) {
            // system property was unspecified, now try looking at configuration
            specifiedVersion = getVersionFromPrefenences(context);
        }

        System.out.println("Starting Groovy-Eclipse compiler resolver.  Specified compiler level: " + specifiedVersion.toReadableVersionString());

        Bundle[] bundles = Platform.getBundles(GROOVY_PLUGIN_ID, null);

        //Print debug infos about the bundles, to debug screwy behavior
        dump(bundles);


        if (bundles == null || bundles.length == 0) {
            System.out.println("No Groovy bundles found...this will cause some problems.");
            bundles = new Bundle[0];
        }

        allVersions = new Version[bundles.length];
        allSpecifiedVersions = new SpecifiedVersion[bundles.length];

        if (specifiedVersion != SpecifiedVersion.UNSPECIFIED) {
            // if there are multiple bundles that match the SpecifiedVersion, let the latest one win out
            boolean found = false;
            for (int i = 0; i < bundles.length; i++) {
                Bundle bundle = bundles[i];
                allVersions[i] = bundle.getVersion();
                allSpecifiedVersions[i] = SpecifiedVersion.findVersion(bundle.getVersion());
                if (allSpecifiedVersions[i] == specifiedVersion && !found) {
                    activeIndex = i;
                    found = true;
                }
            }

            // if activeIndex == 0, then there's nothing to do since specified bundle is already first
            // WRONG on e4.4 it looks like osgi remember which bundle was activated last time and will
            // use that one again, rather than automatically use latest available version.
            if (found /*&& activeIndex > 0*/) {
                for (int i = 0; i < bundles.length; i++) {
                    Bundle bundle = bundles[i];
                    if (i != activeIndex) {
                        System.out.println("Avoided bundle version = "+bundle.getVersion());
                        bundle.uninstall();
                    } else {
                        System.out.println("Blessed bundle version = "+bundle.getVersion());
                    }
                }
                PackageAdmin pkgAdmin = context.getService(context.getServiceReference(org.osgi.service.packageadmin.PackageAdmin.class));
                try {
                    Method method = pkgAdmin.getClass().getMethod("refreshPackages", Bundle[].class, boolean.class, FrameworkListener[].class);
                    if (method == null) {
                        pkgAdmin.refreshPackages(bundles);
                    } else {
                        method.setAccessible(true);
                        method.invoke(pkgAdmin, bundles, true, null);
                    }
                } catch (Exception e) {
                    pkgAdmin.refreshPackages(bundles);
                }
            } else {
                if (!found) {
                    System.out.println("Specified version not found, using " + allVersions[0] + " instead.");
                }
            }
        } else {
            for (int i = 0; i < bundles.length; i++) {
                Bundle bundle = bundles[i];
                allVersions[i] = bundle.getVersion();
                allSpecifiedVersions[i] = SpecifiedVersion.findVersion(bundle.getVersion());
            }
        }
        //Print debug infos about the bundles, to debug screwy behavior
        //dump(bundles);
    }

    private void dump(Bundle[] bundles) {
        for (int i = 0; i < bundles.length; i++) {
            Bundle b = bundles[i];
            System.out.println(b.getBundleId() + " "+b.getVersion()+" = "+stateString(b.getState()));
        }
    }

    private static String stateString(int state) {
        switch (state) {
        case Bundle.ACTIVE:
            return "ACTIVE";
        case Bundle.UNINSTALLED:
            return "UNINSTALLED";
        case Bundle.INSTALLED:
            return "INSTALLED";
        case Bundle.RESOLVED:
            return "RESOLVED";
        case Bundle.STARTING:
            return "STARTING";
        case Bundle.STOPPING:
            return "STOPPING";
        default:
            return "UNKOWN("+state+")";
        }
    }


    /**
     * Finds the compiler version that is specified in the system properties
     */
    private SpecifiedVersion findSysPropVersion() {
        SpecifiedVersion version = SpecifiedVersion.findVersionFromString(System.getProperty(GROOVY_COMPILER_LEVEL));
        if (version == UNSPECIFIED) {
            // now look at the non vmwargs
            version = internalFindCommandLineVersion(System.getProperty(ECLIPSE_COMMANDS));
        }
        return version;
    }

    /**
     * @param property
     * @return
     */
    private SpecifiedVersion internalFindCommandLineVersion(
            String property) {
        if (property == null) {
            return UNSPECIFIED;
        }

        String[] split = property.split("\\\n");
        String versionText = null;
        for (int i = 0; i < split.length; i++) {
            if (DASH_GROOVY_COMPILER_LEVEL.equals(split[i]) && i < split.length-1) {
                versionText = split[i+1];
                break;
            }
        }
        return SpecifiedVersion.findVersionFromString(versionText);
    }

    /**
     * Finds the compiler version that is specified in this plugin's configuration area, if it exists
     */
    private SpecifiedVersion getVersionFromPrefenences(BundleContext context) {
        IEclipsePreferences prefNode = InstanceScope.INSTANCE.getNode(ResolverActivator.PLUGIN_ID);
        return SpecifiedVersion.findVersionFromString(prefNode.get(GROOVY_COMPILER_LEVEL, null));
    }

    /**
     * Stores the {@link SpecifiedVersion} in Eclipse preferences
     * @param version the version to switch to
     * @throws BackingStoreException
     */
    public void storeVersion(SpecifiedVersion version) throws BackingStoreException {
        IEclipsePreferences prefNode = InstanceScope.INSTANCE.getNode(ResolverActivator.PLUGIN_ID);
        prefNode.put(GROOVY_COMPILER_LEVEL, version.versionName);
        prefNode.flush();
    }

    /**
     * @return the active groovy (specified) version
     */
    public SpecifiedVersion getActiveSpecifiedVersion() {
        if (activeIndex == -1) {
            return SpecifiedVersion.findVersion(getActiveVersion());
        } else {
            return allSpecifiedVersions[activeIndex];
        }
    }


    public Version getActiveVersion() {
        if (activeIndex == -1) {
            Bundle bundle = getActiveBundle();
            return bundle == null ? null : bundle.getVersion();
        } else {
            return allVersions[activeIndex];
        }
    }

    public Bundle getActiveBundle() {
        if (activeIndex == -1) {
            // Check if any of the org.codehaus.groovy bundles are active
            for (Bundle bundle : Platform.getBundles(GROOVY_PLUGIN_ID, null)) {
                if (bundle.getState() == Bundle.ACTIVE) {
                    return bundle;
                }
            }
            // If none active just return the latest version bundle
            return Platform.getBundle(GROOVY_PLUGIN_ID);
        } else {
            Bundle[] bundles = Platform.getBundles(GROOVY_PLUGIN_ID, allVersions[activeIndex].toString());
            if (bundles != null && bundles.length > 0) {
                // we are guaranteed that the highest installed bundle is the one being used.
                return bundles[0];
            }
        }
        return null;
    }

    public SpecifiedVersion[] getAllSpecifiedVersions() {
        return allSpecifiedVersions;
    }

    public Version getAssociatedVersion(SpecifiedVersion specifiedVersion) {
        for (int i = 0; i < allSpecifiedVersions.length; i++) {
            if (allSpecifiedVersions[i] == specifiedVersion) {
                return allVersions[i];
            }
        }
        return null;
    }
}
