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

package org.codehaus.groovy.activator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.core.Activator;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.engine.DefaultPhaseSet;
import org.eclipse.equinox.internal.provisional.p2.engine.IEngine;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.engine.InstallableUnitOperand;
import org.eclipse.equinox.internal.provisional.p2.engine.Operand;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointType;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.osgi.framework.BundleContext;

/**
 * @author Andrew Eisenberg
 * @created Oct 9, 2009
 * 
 * Forces a refresh of packages at startup
 * This class uses P2 infrastructure to create an IU that
 * adds a new JVM argument to the installation.
 * The JVM argument will ensure that pacakges are refreshed at startup
 *
 */
public class RefreshPackages {
    public IStatus addJvmArg() throws CoreException {
        return internalChangeJvmArg(true);
    }
    
    public IStatus removeJvmArg() throws CoreException {
        return internalChangeJvmArg(false);
    }
    
    private IStatus internalChangeJvmArg(boolean add) throws CoreException {
        IProfile profile = getCurrentProfile();
        IEngine engine = getEngine();
        if (engine == null) {
            throw new CoreException(new Status(IStatus.ERROR, GroovyActivator.PLUGIN_ID, "Could not find p2 Engine service")); //$NON-NLS-1$
        }
        if (profile == null) {
            throw new CoreException(new Status(IStatus.ERROR, GroovyActivator.PLUGIN_ID, "Could not find current p2 Profile")); //$NON-NLS-1$
        } 
        IInstallableUnit iu = createIU();
        InstallableUnitOperand operand = add ? 
                new InstallableUnitOperand(null, iu) :
                    new InstallableUnitOperand(iu, null);
        Operand[] operands = new Operand[] { operand };
        return engine.perform(profile, new DefaultPhaseSet(), operands, null, null);
    }

    private IProfile getCurrentProfile() {
        IProfileRegistry profileRegistry = (IProfileRegistry) ServiceHelper.getService(getContext(), IProfileRegistry.class.getName());
        if (profileRegistry == null)
            return null;
        return profileRegistry.getProfile(IProfileRegistry.SELF);
    }


    /**
     * the Engine.  Might be null
     * @return
     */
    private IEngine getEngine() {
        return (IEngine) ServiceHelper.getService(getContext(), IEngine.SERVICE_NAME);
    }

    private BundleContext getContext() {
        return Activator.getContext();
    }

    @SuppressWarnings("nls")
    private IInstallableUnit createIU() {
        InstallableUnitDescription iu = new MetadataFactory.InstallableUnitDescription();
        String time = Long.toString(System.currentTimeMillis());
        iu.setId("property.setter");
        iu.setVersion(Version.createOSGi(0, 0, 0, time));
        Map touchpointData = new HashMap();
        String data = "addJvmArg(jvmArg:-Declipse.refreshBundles=true);";
        touchpointData.put("configure", data);
        data = "removeJvmArg(jvmArg:-Declipse.refreshBundles=true);";
        touchpointData.put("unconfigure", data);
        iu.addTouchpointData(MetadataFactory.createTouchpointData(touchpointData));
        ITouchpointType touchpoint = MetadataFactory.createTouchpointType(
            "org.eclipse.equinox.p2.osgi", 
            Version.createOSGi(1, 0, 0));
        iu.setTouchpointType(touchpoint);
        return MetadataFactory.createInstallableUnit(iu);
    }
}