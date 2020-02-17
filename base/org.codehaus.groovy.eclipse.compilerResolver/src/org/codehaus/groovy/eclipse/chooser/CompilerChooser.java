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
package org.codehaus.groovy.eclipse.chooser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Chooses which Groovy compiler/runtime to use and provides some other useful
 * information about the bundle.
 */
public class CompilerChooser implements BundleActivator {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.compilerResolver";

    private static final String ECLIPSE_COMMANDS = "eclipse.commands";
    private static final String GROOVY_PLUGIN_ID = "org.codehaus.groovy";
    private static final String GROOVY_COMPILER_LEVEL = "groovy.compiler.level";
    private static final String DASH_GROOVY_COMPILER_LEVEL = "-" + GROOVY_COMPILER_LEVEL;

    private static CompilerChooser instance;

    private int activeIndex = -1;
    private Version[] allVersions = {};
    private SpecifiedVersion[] allSpecifiedVersions = {};

    public static CompilerChooser getInstance() {
        return instance;
    }

    public CompilerChooser() {
        instance = this;
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

    public Version getActiveVersion() {
        if (activeIndex == -1) {
            Bundle bundle = getActiveBundle();
            return bundle == null ? null : bundle.getVersion();
        } else {
            return allVersions[activeIndex];
        }
    }

    public Version getAssociatedVersion(SpecifiedVersion specifiedVersion) {
        for (int i = 0, n = allSpecifiedVersions.length; i < n; i += 1) {
            if (allSpecifiedVersions[i] == specifiedVersion) {
                return allVersions[i];
            }
        }
        return null;
    }

    public SpecifiedVersion[] getAllSpecifiedVersions() {
        return allSpecifiedVersions;
    }

    /**
     * @return the active Groovy (specified) version
     */
    public SpecifiedVersion getActiveSpecifiedVersion() {
        if (activeIndex == -1) {
            return SpecifiedVersion.findVersion(getActiveVersion());
        } else {
            return allSpecifiedVersions[activeIndex];
        }
    }

    private SpecifiedVersion getVersionFromProperties() {
        SpecifiedVersion version = SpecifiedVersion.findVersionFromString(System.getProperty(GROOVY_COMPILER_LEVEL));
        if (version == SpecifiedVersion.UNSPECIFIED) {
            // now look at the non-vmargs
            String property = System.getProperty(ECLIPSE_COMMANDS);
            if (property != null) {
                String[] split = property.split("\\\n");
                for (int i = 0, n = split.length; i < n; i += 1) {
                    if (DASH_GROOVY_COMPILER_LEVEL.equals(split[i]) && i < split.length - 1) {
                        version = SpecifiedVersion.findVersionFromString(split[i + 1]);
                        break;
                    }
                }
            }
        }
        return version;
    }

    /**
     * Finds the compiler version that is specified in this plugin's configuration area, if it exists.
     */
    private SpecifiedVersion getVersionFromPrefenences() {
        IEclipsePreferences node = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
        String versionText = node.get(GROOVY_COMPILER_LEVEL, null);
        return SpecifiedVersion.findVersionFromString(versionText);
    }

    /**
     * Stores the {@link SpecifiedVersion} in Eclipse preferences.
     */
    public void storeVersion(SpecifiedVersion version) throws BackingStoreException {
        IEclipsePreferences node = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
        node.put(GROOVY_COMPILER_LEVEL, version.versionName);
        node.flush();
    }

    //--------------------------------------------------------------------------

    private BundleContext bundleContext;
    private volatile boolean initialized;
    private ServiceListener serviceListener;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;

        // There is a small window where the chooser can be initialized. It has
        // to be after the workspace has started (in order to ensure the choose
        // workspace dialog still shows) but before JDT is initialized (so that
        // the Groovy bundles aren't fully loaded).

        // the service listener is called synchronously as the resources bundle is started
        String filter = "(" + Constants.OBJECTCLASS + "=org.eclipse.core.resources.IWorkspace)";
        serviceListener = event -> {
            if (event.getType() == ServiceEvent.REGISTERED) {
                this.bundleContext.removeServiceListener(serviceListener);
                serviceListener = null;

                SafeRunner.run(this::initialize);
            }
        };
        bundleContext.addServiceListener(serviceListener, filter);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (serviceListener != null) {
            bundleContext.removeServiceListener(serviceListener);
        }
        this.bundleContext = null;
        serviceListener = null;
        initialized = false;
    }

    public boolean isInitiailzed() {
        return initialized;
    }

    //VisibleForTesting
    public CompilerChooser initialize() throws BundleException {
        if (!initialized) {
            initialized = true;

            SpecifiedVersion specifiedVersion = getVersionFromProperties();
            if (specifiedVersion == SpecifiedVersion.UNSPECIFIED) {
                // system property was unspecified, now try looking at configuration
                specifiedVersion = getVersionFromPrefenences();
            }

            System.out.println("Starting Groovy-Eclipse compiler resolver. Specified compiler level: " + specifiedVersion.toReadableVersionString());

            Bundle[] bundles = Platform.getBundles(GROOVY_PLUGIN_ID, null);
            if (bundles == null || bundles.length == 0) {
                System.out.println("No Groovy bundles found...this will cause some problems.");
                bundles = new Bundle[0];
            } else {
                // print debug infos about the bundles to debug screwy behavior
                dump(Arrays.asList(bundles));
            }

            allVersions = new Version[bundles.length];
            allSpecifiedVersions = new SpecifiedVersion[bundles.length];
            for (int i = 0, n = bundles.length; i < n; i += 1) {
                Bundle bundle = bundles[i];
                allVersions[i] = bundle.getVersion();
                allSpecifiedVersions[i] = SpecifiedVersion.findVersion(bundle.getVersion());
                if (allSpecifiedVersions[i] == specifiedVersion && activeIndex == -1) {
                    activeIndex = i;
                }
            }

            if (bundles.length > 1) {
                int skip = Math.max(0, activeIndex);
                Collection<Bundle> dirty = new ArrayList<>();
                for (int i = 0, n = bundles.length; i < n; i += 1) {
                    Bundle bundle = bundles[i];
                    if (i == skip) {
                        System.out.println("Skipped bundle version " + bundle.getVersion());
                    } else {
                        System.out.println("Stopped bundle version " + bundle.getVersion());
                        bundle.uninstall();
                        dirty.add(bundle);
                    }
                }
                refreshPackages(dirty);
            }
        }
        return this;
    }

    private void dump(Collection<Bundle> bundles) {
        for (Bundle b : bundles) {
            System.out.printf("%3d %s_%s %s%n", b.getBundleId(), b.getSymbolicName(), b.getVersion(), stateString(b.getState()));
        }
    }

    /*private void logMessage(String s) {
        ILog log = Platform.getLog(bundleContext.getBundle());
        log.log(new Status(IStatus.INFO, PLUGIN_ID, "GroovyCompilerChooser: " + s));
    }

    private void logWarning(String s) {
        ILog log = Platform.getLog(bundleContext.getBundle());
        log.log(new Status(IStatus.WARNING, PLUGIN_ID, "GroovyCompilerChooser: " + s));
    }

    private void logError(Throwable t) {
        ILog log = Platform.getLog(bundleContext.getBundle());
        log.log(new Status(IStatus.ERROR, PLUGIN_ID, "GroovyCompilerChooser: " + t.getMessage(), t));
    }*/

    private void refreshPackages(Collection<Bundle> bundles) {
        FrameworkWiring wiring = bundleContext.getBundle(0).adapt(FrameworkWiring.class);

        System.out.println("Refresh bundles:");
        dump(wiring.getDependencyClosure(bundles));

        final CountDownLatch latch = new CountDownLatch(1);
        wiring.refreshBundles(bundles, event -> {
            if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String stateString(int bundleState) {
        switch (bundleState) {
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
        case Bundle.ACTIVE:
            return "ACTIVE";
        }
        return "UNKNOWN(" + bundleState + ")";
    }
}
