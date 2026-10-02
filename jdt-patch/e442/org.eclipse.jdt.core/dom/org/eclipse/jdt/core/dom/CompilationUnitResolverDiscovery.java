/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.Objects;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.dom.ICompilationUnitResolver;

class CompilationUnitResolverDiscovery {
	private static final String SELECTED_SYSPROP = "ICompilationUnitResolver"; //$NON-NLS-1$
	private static final String COMPILATION_UNIT_RESOLVER_EXTPOINT_ID = "compilationUnitResolver" ; //$NON-NLS-1$
	private static boolean ERROR_LOGGED = false;

	private static String lastId;
	private static IConfigurationElement lastExtension;

	static ICompilationUnitResolver getInstance() {
		String id = System.getProperty(SELECTED_SYSPROP);
		IConfigurationElement configElement = getConfigurationElement(id);
		lastId = id;
		lastExtension = configElement;
		if (configElement != null) {
			try {
				// We do prefer creating a new instance on each call, as it can allow the extension
				// to store some state more easily than by using a singleton.
				Object executableExtension = configElement.createExecutableExtension("class"); //$NON-NLS-1$
				if (executableExtension instanceof ICompilationUnitResolver icur) {
					return icur;
				}
			} catch (CoreException e) {
				if (!setErrorLogged()) {
					ILog.get().error("Could not instantiate ICompilationUnitResolver: '" + id + "' with class: " + configElement.getAttribute("class"), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		return CompilationUnitResolver.getInstance();
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
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(JavaCore.PLUGIN_ID, COMPILATION_UNIT_RESOLVER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				IConfigurationElement[] configElements = ext.getConfigurationElements();
				for (final IConfigurationElement configElement : configElements) {
					String elementId = configElement.getAttribute("id"); //$NON-NLS-1$
					if (id.equals(elementId) && "resolver".equals(configElement.getName())) { //$NON-NLS-1$
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
