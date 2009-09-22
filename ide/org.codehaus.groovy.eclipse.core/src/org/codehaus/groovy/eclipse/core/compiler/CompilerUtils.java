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
        Bundle groovyBundle = Platform.getBundle("org.codehaus.groovy");
        return groovyBundle.getVersion().toString();
    }
    
    public static boolean isUsingGroovy16() {
        Bundle groovyBundle = Platform.getBundle("org.codehaus.groovy");
        return groovyBundle.getVersion().getMajor() == 1 && groovyBundle.getVersion().getMinor() == 6;
    }
    
    public static String getOtherVersion() {
        return isUsingGroovy16() ? "1.7" : "1.6";
    }

    
    /**
     * Swtiches to or from groovy version 1.6.x depending on the boolean passed in
     * A restart is required immediately after or else many exceptions will be thrown.
     * @param toVersion16
     * @return {@link Status.OK_STATUS} if successful or error status that contains the exception thrown otherwise 
     */
    public static IStatus switchVersions(boolean toVersion16) {
        String version16 = "[1.6.0,1.7.0)";
        String version17 = "1.7.0";
        
        
        try {
            Bundle[] toDisable = Platform.getBundles("org.codehaus.groovy", (toVersion16 ? version17 : version16));
            Bundle[] toEnable = Platform.getBundles("org.codehaus.groovy", (toVersion16 ? version16 : version17));

            if (toDisable == null || toDisable.length == 0) {
                throw new Exception("Could not find any " + (toVersion16 ? "1.7" : "1.6") + " groovy version to disable");
            }
            if (toEnable == null || toEnable.length == 0) {
                throw new Exception("Could not find any " + (toVersion16 ? "1.6" : "1.7") + " groovy version to enable");
            }
            
            State state = Platform.getPlatformAdmin().getState(false);
            for (Bundle bundle : toDisable) {
                bundle.stop();
                DisabledInfo info = createDisabledInfo(state, bundle);
                Platform.getPlatformAdmin().addDisabledInfo(info);
            }
            toEnable[0].start();
            DisabledInfo info = createDisabledInfo(state, toEnable[0]);
            Platform.getPlatformAdmin().removeDisabledInfo(info);
            
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
    private static DisabledInfo createDisabledInfo(State state, Bundle bundle) {
        BundleDescription desc = state.getBundle(bundle.getBundleId());
        DisabledInfo info = new DisabledInfo(
                "org.codehaus.groovy.eclipse", //$NON-NLS-1$
                "Disabled Groovy Compiler", desc); //$NON-NLS-1$
        return info;
    }
}
