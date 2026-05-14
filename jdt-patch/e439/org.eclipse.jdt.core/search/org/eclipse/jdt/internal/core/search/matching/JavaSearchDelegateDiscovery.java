/*******************************************************************************
 * Copyright (c) 2025 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchDelegate;

public class JavaSearchDelegateDiscovery {
	private static final String SELECTED_SYSPROP = "IJavaSearchDelegate"; //$NON-NLS-1$
	private static final String EXTPOINT_ID = "javaSearchDelegate" ; //$NON-NLS-1$
	private static boolean ERROR_LOGGED = false;

	private static String lastId;
	private static IConfigurationElement lastExtension;

	private static final Map<String, IJavaSearchDelegate> JAVA_SEARCH_DELEGATE_CACHE = new HashMap<>();

	public static IJavaSearchDelegate getInstance() {
		String id = System.getProperty(SELECTED_SYSPROP);
		IConfigurationElement configElement = getConfigurationElement(id);
		lastId = id;
		lastExtension = configElement;
		if (configElement != null) {
			try {
				if (JAVA_SEARCH_DELEGATE_CACHE.get(id) != null) {
					return JAVA_SEARCH_DELEGATE_CACHE.get(id);
				}
				Object executableExtension = configElement.createExecutableExtension("class"); //$NON-NLS-1$
				if (executableExtension instanceof IJavaSearchDelegate icep) {
					JAVA_SEARCH_DELEGATE_CACHE.put(id, icep);
					return icep;
				}
			} catch (CoreException e) {
				if (!setErrorLogged()) {
					ILog.get().error("Could not instantiate IJavaSearchDelegate: '" + id + "' with class: " + configElement.getAttribute("class"), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param id
	 * @return The extension element with the given id or <code>null</null> if not found
	 */
	private static IConfigurationElement getConfigurationElement(String id) {
		if (id == null || id.isBlank()) {
			return null;
		}
		if (lastExtension != null && Objects.equals(id, lastId)) {
			return lastExtension;
		}
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(JavaCore.PLUGIN_ID, EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				IConfigurationElement[] configElements = ext.getConfigurationElements();
				for (final IConfigurationElement configElement : configElements) {
					String elementId = configElement.getAttribute("id"); //$NON-NLS-1$
					if (id.equals(elementId) && "searchDelegate".equals(configElement.getName())) { //$NON-NLS-1$
						return configElement;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Set the ERROR_LOGGED field to <code>true</code>.
	 * @return the previous value of ERROR_LOGGED.
	 */
	private static synchronized boolean setErrorLogged() {
		boolean prev = ERROR_LOGGED;
		ERROR_LOGGED = true;
		return prev;
	}
}
