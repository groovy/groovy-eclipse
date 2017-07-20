/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.chooser;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Chooses which Groovy compiler/runtime to use and provides some other useful
 * information about the bundle.
 */
@SuppressWarnings("deprecation")
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

    public CompilerChooser() {
        instance = this;
    }

    public static CompilerChooser getInstance() {
        return instance;
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

    public SpecifiedVersion[] getAllSpecifiedVersions() {
        return allSpecifiedVersions;
    }

    public Version getAssociatedVersion(SpecifiedVersion specifiedVersion) {
        for (int i = 0, n = allSpecifiedVersions.length; i < n; i += 1) {
            if (allSpecifiedVersions[i] == specifiedVersion) {
                return allVersions[i];
            }
        }
        return null;
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

    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;

        // There is a small window where the chooser can be initialized.
        // It has to be after the workspace has started (in order to ensure
        // the choose workspace dialog still shows) but before JDT is initialized
        // (so that the groovy bundles aren't loaded).

        // the service listener is called synchronously as the resources bundle is actived
        String filter = "(" + Constants.OBJECTCLASS + "=org.eclipse.core.resources.IWorkspace)";
        serviceListener = new ServiceListener() {
            public void serviceChanged(ServiceEvent event) {
                if (event.getType() == ServiceEvent.REGISTERED) {
                    try {
                        initialize();

                    } catch (BundleException e) {
                        logError(e);
                        throw new RuntimeException(e);
                    } catch (RuntimeException e) {
                        logError(e);
                        throw e;
                    } finally {
                        CompilerChooser.this.bundleContext.removeServiceListener(serviceListener);
                    }
                }
            }
        };
        bundleContext.addServiceListener(serviceListener, filter);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        this.bundleContext.removeServiceListener(serviceListener);
        this.bundleContext = null;
        serviceListener = null;
        initialized = false;
    }

    public boolean isInitiailzed() {
        return initialized;
    }

    //VisibleForTesting
    public CompilerChooser initialize() throws BundleException {
        if (initialized) return this;
        initialized = true;

        SpecifiedVersion specifiedVersion = findSysPropVersion();
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
            dump(bundles);
        }

        allVersions = new Version[bundles.length];
        allSpecifiedVersions = new SpecifiedVersion[bundles.length];

        if (specifiedVersion != SpecifiedVersion.UNSPECIFIED) {
            // if there are multiple bundles that match the SpecifiedVersion, let the latest one win out
            boolean found = false;
            for (int i = 0, n = bundles.length; i < n; i += 1) {
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
                for (int i = 0, n = bundles.length; i < n; i += 1) {
                    Bundle bundle = bundles[i];
                    if (i != activeIndex) {
                        System.out.println("Avoided bundle version " + bundle.getVersion());
                        bundle.uninstall();
                    } else {
                        System.out.println("Blessed bundle version " + bundle.getVersion());
                    }
                }
                PackageAdmin pkgAdmin = bundleContext.getService(bundleContext.getServiceReference(PackageAdmin.class));
                try {
                    java.lang.reflect.Method method = pkgAdmin.getClass().getMethod("refreshPackages", Bundle[].class, boolean.class, FrameworkListener[].class);
                    if (method == null) {
                        pkgAdmin.refreshPackages(bundles);
                    } else {
                        method.setAccessible(true);
                        method.invoke(pkgAdmin, bundles, Boolean.TRUE, null);
                    }
                } catch (Exception e) {
                    pkgAdmin.refreshPackages(bundles);
                }
                dump(bundles);
            } else {
                System.out.println("Specified version not found, using " + allVersions[0] + " instead.");
            }
        } else {
            for (int i = 0, n = bundles.length; i < n; i += 1) {
                Bundle bundle = bundles[i];
                allVersions[i] = bundle.getVersion();
                allSpecifiedVersions[i] = SpecifiedVersion.findVersion(bundle.getVersion());
            }
        }

        return this;
    }

    private void dump(Bundle[] bundles) {
        for (Bundle b : bundles) {
            String state;
            switch (b.getState()) {
            case Bundle.ACTIVE:
                state = "ACTIVE";
                break;
            case Bundle.UNINSTALLED:
                state = "UNINSTALLED";
                break;
            case Bundle.INSTALLED:
                state = "INSTALLED";
                break;
            case Bundle.RESOLVED:
                state = "RESOLVED";
                break;
            case Bundle.STARTING:
                state = "STARTING";
                break;
            case Bundle.STOPPING:
                state = "STOPPING";
                break;
            default:
                state = "UNKOWN(" + b.getState() + ")";
            }

            String message = b.getBundleId() + " " + b.getVersion() + " " + state;
            System.out.println(message);
        }
    }

    private void logWarning(String s) {
        ILog log = Platform.getLog(bundleContext.getBundle());
        log.log(new Status(IStatus.WARNING, PLUGIN_ID, "GroovyCompilerChooser: " + s));
    }

    private void logError(Throwable t) {
        ILog log = Platform.getLog(bundleContext.getBundle());
        log.log(new Status(IStatus.ERROR, PLUGIN_ID, "GroovyCompilerChooser: " + t.getMessage(), t));
    }

    /**
     * Finds the compiler version that is specified in the system properties
     */
    private SpecifiedVersion findSysPropVersion() {
        SpecifiedVersion version = SpecifiedVersion.findVersionFromString(System.getProperty(GROOVY_COMPILER_LEVEL));
        if (version == SpecifiedVersion.UNSPECIFIED) {
            // now look at the non vmwargs
            version = internalFindCommandLineVersion(System.getProperty(ECLIPSE_COMMANDS));
        }
        return version;
    }

    private SpecifiedVersion internalFindCommandLineVersion(String property) {
        if (property == null) {
            return SpecifiedVersion.UNSPECIFIED;
        }
        String[] split = property.split("\\\n");
        String versionText = null;
        for (int i = 0, n = split.length; i < n; i += 1) {
            if (DASH_GROOVY_COMPILER_LEVEL.equals(split[i]) && i < split.length - 1) {
                versionText = split[i + 1];
                break;
            }
        }
        return SpecifiedVersion.findVersionFromString(versionText);
    }
}
