/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.framework.internal.core.BundleContextImpl;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This class chooses which Groovy compiler to use on startup
 * And it provides some other useful information about the compiler
 * @author Andrew Eisenberg
 * @created 2013-04-12
 */
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
        
        Bundle[] bundles = ((BundleContextImpl) context)
                .getFramework()
                .getPackageAdmin()
                .getBundles(GROOVY_PLUGIN_ID, null);

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
            if (found && activeIndex > 0) {
                for (int i = 0; i < bundles.length; i++) {
                    if (i != activeIndex) {
                        Bundle bundle = bundles[i];
                        bundle.uninstall();
                    }
                }
                ((BundleContextImpl) context).getFramework().getPackageAdmin().refreshPackages(bundles, true, (FrameworkListener[]) null);
//                ((BundleContextImpl) context).getFramework().getPackageAdmin().refreshPackages(bundles);
            } else {
                if (!found) {
                    System.out.println("Specified version not found, using " + allVersions[0] + " instead.");
                }
            }
        } else {
            // just use highest version
            activeIndex = 0;
            // no need to uninstall unused bundles since they aren't wired
            for (int i = 0; i < bundles.length; i++) {
                Bundle bundle = bundles[i];
                allVersions[i] = bundle.getVersion();
                allSpecifiedVersions[i] = SpecifiedVersion.findVersion(bundle.getVersion());
            }            
        }
    }
    
    /**
     * Finds the compiler version that is specified in the system properties
     */
    private SpecifiedVersion findSysPropVersion() {
        SpecifiedVersion version = SpecifiedVersion.findVersionFromString(FrameworkProperties.getProperty(GROOVY_COMPILER_LEVEL));
        if (version == UNSPECIFIED) {
            // now look at the non vmwargs
            version = internalFindCommandLineVersion(FrameworkProperties.getProperty(ECLIPSE_COMMANDS));
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
			return allSpecifiedVersions.length > 0 ? allSpecifiedVersions[0] : SpecifiedVersion.UNSPECIFIED;
    	} else {
			return allSpecifiedVersions[activeIndex];
		}
    }
        
    public Version getActiveVersion() {
        if (activeIndex == -1) {
			Bundle bundle = Platform.getBundle(GROOVY_PLUGIN_ID);
			return bundle == null ? null : bundle.getVersion();
        } else {
			return allVersions[activeIndex];
        }
    }
    
    public Bundle getActiveBundle() {
        if (activeIndex == -1) {
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
        if (activeIndex >= 0) {
            for (int i = 0; i < allSpecifiedVersions.length; i++) {
                if (allSpecifiedVersions[i] == specifiedVersion) {
                    return allVersions[i];
                }
            }
        }
        return null;
    }
}