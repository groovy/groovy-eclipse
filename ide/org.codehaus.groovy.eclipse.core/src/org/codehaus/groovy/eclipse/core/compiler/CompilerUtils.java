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

import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.DisabledInfo;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;

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
        return isGroovy17DisabledOrMissing() ? "1.7" : "1.6";
    }

    public static boolean isGroovy17DisabledOrMissing() {
        BundleDescription disabled17Bundle = null;
        disabled17Bundle = getDisabled17BundleDescription();
        if (disabled17Bundle != null) {
            return true;
        }
        
        // it might be that there is no 1.7 jar.  If the 1.7 jar is missing, we use 1.6
        Bundle[] active = Platform.getBundles("org.codehaus.groovy", "1.7.0");
        return active == null || active.length == 0;
    }

    /**
     * @param disabled17Bundle
     * @return
     */
    private static BundleDescription getDisabled17BundleDescription() {
        BundleDescription[] bundles = Platform.getPlatformAdmin().getState(false).getDisabledBundles();
        for (BundleDescription bundle : bundles) {
            if (bundle.getSymbolicName().equals("org.codehaus.groovy") && 
                    bundle.getVersion().getMajor() == 1 && bundle.getVersion().getMinor() == 7) {
                return bundle;
            }
        }
        return null;
    } 
    
    public static Bundle getActiveGroovyBundle() {
        String version16 = "[1.6.0,1.7.0)";
        String version17 = "1.7.0";
        Bundle[] active = Platform.getBundles("org.codehaus.groovy", (isGroovy17DisabledOrMissing() ? version16 : version17));
        return active.length > 0 ? active[0] : null;
    }
    
    /**
     * Swtiches to or from groovy version 1.6.x depending on the boolean passed in
     * A restart is required immediately after or else many exceptions will be thrown.
     * @param toVersion16
     * @return {@link Status.OK_STATUS} if successful or error status that contains the exception thrown otherwise 
     */
    public static IStatus switchVersions(boolean toVersion17) {
        String version16 = "[1.6.0,1.7.0)";
        String version17 = "1.7.0";
        
        
        try {
            State state = Platform.getPlatformAdmin().getState(false);
            if (toVersion17) {
                BundleDescription disabled17 = getDisabled17BundleDescription();
                if (disabled17 != null) {
                    // remove the disabled info so that we can find it
                    DisabledInfo info = createDisabledInfo(state, disabled17.getBundleId());
                    Platform.getPlatformAdmin().removeDisabledInfo(info);
                    // just in case this bundle was disabled from pde, we need to create another
                    // kind of disabled info
                    info = createDisabledInfoForPDE(state, disabled17.getBundleId());
                    Platform.getPlatformAdmin().removeDisabledInfo(info);
                }
            }
            
            Bundle[] toDisable = Platform.getBundles("org.codehaus.groovy", (toVersion17 ? version16 : version17));
            Bundle[] toEnable  = Platform.getBundles("org.codehaus.groovy", (toVersion17 ? version17 : version16));

            if (toDisable == null || toDisable.length == 0) {
                throw new Exception("Could not find any " + (toVersion17 ? "1.6" : "1.7") + " groovy version to disable");
            }
            if (toEnable == null || toEnable.length == 0) {
                throw new Exception("Could not find any " + (toVersion17 ? "1.7" : "1.6") + " groovy version to enable");
            }
            
            for (Bundle bundle : toDisable) {
                bundle.stop();
                if (!toVersion17) {
                    DisabledInfo info = createDisabledInfo(state, bundle.getBundleId());
                    Platform.getPlatformAdmin().addDisabledInfo(info);
                }
            }
            toEnable[0].start();
            
            return Status.OK_STATUS;
        } catch (Exception e) {
            return new Status(IStatus.ERROR, GroovyCoreActivator.PLUGIN_ID,
                            e.getMessage()
                            + "\n\nSee the error log for more information.", e);
        }
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
    private static DisabledInfo createDisabledInfoForPDE(State state, long bundleId) {
        BundleDescription desc = state.getBundle(bundleId);
        DisabledInfo info = new DisabledInfo(
                "org.eclipse.pde.ui", //$NON-NLS-1$
                "Disabled via PDE", desc); //$NON-NLS-1$
        return info;
    }
}
