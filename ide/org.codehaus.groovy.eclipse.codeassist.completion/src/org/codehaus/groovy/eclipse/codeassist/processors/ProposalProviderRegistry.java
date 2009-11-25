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

package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Nov 23, 2009
 *
 */
public class ProposalProviderRegistry {

    private static final String APPLIES_TO = "appliesTo"; //$NON-NLS-1$

    private static final String NATURE = "projectNature"; //$NON-NLS-1$

    private static final String PROVIDER = "proposalProvider"; //$NON-NLS-1$

    private static final String PROPOSAL_PROVIDER_EXTENSION = "org.codehaus.groovy.eclipse.codeassist.completion.completionProposalProvider"; //$NON-NLS-1$

    private final static ProposalProviderRegistry DEFAULT = new ProposalProviderRegistry();

    static ProposalProviderRegistry getRegistry() {
        return DEFAULT;
    }

    // maps from project nature to lists of type lookup classes
    private Map<String, List<IConfigurationElement>> natureLookupMap = new HashMap<String, List<IConfigurationElement>>();

    List<IProposalProvider> getProvidersFor(IProject project) throws CoreException {
        String[] natures = project.getDescription().getNatureIds();
        List<IProposalProvider> lookups = new ArrayList<IProposalProvider>();
        for (String nature : natures) {
            List<IConfigurationElement> configs = natureLookupMap.get(nature);
            if (configs != null) {
                for (IConfigurationElement config : configs) {
                    try {
                        lookups.add((IProposalProvider) config.createExecutableExtension(PROVIDER));
                    } catch (CoreException e) {
                        Util.log(e, "Problem creating completion provider for type " + config.getAttribute(PROVIDER));
                    }
                }
            }
        }
        return lookups;
    }

    private ProposalProviderRegistry() {
        initialize();
    }

    private void initialize() {
        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(PROPOSAL_PROVIDER_EXTENSION);
        IExtension[] exts = extPoint.getExtensions();
        for (IExtension ext : exts) {
            IConfigurationElement[] configs = ext.getConfigurationElements();
            for (IConfigurationElement config : configs) {
                createLookup(config);
            }
        }
    }

    /**
     * @param config
     */
    private void createLookup(IConfigurationElement config) {
        try {
            if (config.getName().equals(PROVIDER)) {
                if (config.getAttribute(PROVIDER) != null) {
                    IConfigurationElement[] appliesTos = config.getChildren(APPLIES_TO);
                    for (IConfigurationElement appliesTo : appliesTos) {
                        String nature = appliesTo.getAttribute(NATURE);
                        List<IConfigurationElement> elts;
                        if (natureLookupMap.containsKey(nature)) {
                            elts = natureLookupMap.get(nature);
                        } else {
                            elts = new ArrayList<IConfigurationElement>(3);
                            natureLookupMap.put(nature, elts);
                        }
                        elts.add(config);
                    }
                } else {
                    Util.log(new RuntimeException(), "Type lookup registry extension found with no type lookup class.");
                }
            }
        } catch (Exception e) {
            Util.log(e, "Problem registering type lookups");
        }
    }

    List<IProposalProvider> getProvidersFor(GroovyCompilationUnit unit) throws CoreException {
        return getProvidersFor(unit.getResource().getProject());
    }
}
