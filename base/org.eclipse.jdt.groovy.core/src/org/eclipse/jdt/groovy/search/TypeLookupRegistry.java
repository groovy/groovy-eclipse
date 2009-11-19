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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Manages type lookups
 * 
 * @author Andrew Eisenberg
 * @created Nov 17, 2009
 */
public class TypeLookupRegistry {
	private final String TYPE_LOOKUP_EXTENSION = "org.eclipse.jdt.groovy.core.typeLookp";

	// maps from project nature to lists of type lookup classes
	private Map<String, List<Class<ITypeLookup>>> natureLookupMap = new HashMap<String, List<Class<ITypeLookup>>>();

	TypeLookupRegistry() {
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

	/**
	 * @param config
	 */
	private void createLookup(IConfigurationElement config) {

	}
}
