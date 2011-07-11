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

package org.eclipse.jdt.groovy.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Manages type lookups
 * 
 * @author Andrew Eisenberg
 * @created Nov 17, 2009
 */
public class TypeLookupRegistry {
	private static final String APPLIES_TO = "appliesTo"; //$NON-NLS-1$

	private static final String NATURE = "projectNature"; //$NON-NLS-1$

	private static final String LOOKUP = "lookup"; //$NON-NLS-1$

	private static final String TYPE_LOOKUP_EXTENSION = "org.eclipse.jdt.groovy.core.typeLookup"; //$NON-NLS-1$

	private final static TypeLookupRegistry DEFAULT = new TypeLookupRegistry();

	static TypeLookupRegistry getRegistry() {
		return DEFAULT;
	}

	// maps from project nature to lists of type lookup classes
	private Map<String, List<IConfigurationElement>> natureLookupMap = new HashMap<String, List<IConfigurationElement>>();

	List<ITypeLookup> getLookupsFor(IProject project) throws CoreException {
		if (!project.exists()) {
			return new ArrayList<ITypeLookup>(3);
		}
		String[] natures = project.getDescription().getNatureIds();
		List<ITypeLookup> lookups = new ArrayList<ITypeLookup>();
		for (String nature : natures) {
			List<IConfigurationElement> configs = natureLookupMap.get(nature);
			if (configs != null) {
				for (IConfigurationElement config : configs) {
					try {
						lookups.add((ITypeLookup) config.createExecutableExtension(LOOKUP));
					} catch (CoreException e) {
						Util.log(e, "Problem creating lookup for type " + config.getAttribute(LOOKUP)); //$NON-NLS-1$
					}
				}
			}
		}
		return lookups;
	}

	private TypeLookupRegistry() {
		initialize();
	}

	private void initialize() {
		IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(TYPE_LOOKUP_EXTENSION);
		IExtension[] exts = extPoint.getExtensions();
		for (IExtension ext : exts) {
			IConfigurationElement[] configs = ext.getConfigurationElements();
			for (IConfigurationElement config : configs) {
				createLookup(config);
			}
		}
	}

	private void createLookup(IConfigurationElement config) {
		try {
			if (config.getName().equals(LOOKUP)) {
				if (config.getAttribute(LOOKUP) != null) {
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
					Util.log(new RuntimeException(), "Type lookup registry extension found with no type lookup class."); //$NON-NLS-1$
				}
			}
		} catch (Exception e) {
			Util.log(e, "Problem registering type lookups"); //$NON-NLS-1$
		}
	}
}
