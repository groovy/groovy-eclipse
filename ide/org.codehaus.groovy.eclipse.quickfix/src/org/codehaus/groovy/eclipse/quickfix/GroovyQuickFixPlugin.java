 /*
 * Copyright 2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GroovyQuickFixPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.quickfix"; //$NON-NLS-1$

	// The shared instance
	private static GroovyQuickFixPlugin plugin;

	/**
	 * The constructor
	 */
	public GroovyQuickFixPlugin() {
		//
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static GroovyQuickFixPlugin getDefault() {
		return plugin;
	}

	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		log(status);
	}

	public static void logWarning(String message, Throwable exception) {
		log(createWarningStatus(message, exception));
	}

	public static void log(Throwable exception) {
		log(createErrorStatus("Internal Error", exception));
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static IStatus createWarningStatus(String message,
			Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, exception);
	}

}
