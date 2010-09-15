/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.eclipse.jdt.groovy.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.jdt.groovy.core";

	// The shared instance
	private static Activator plugin;

	private IEclipsePreferences instanceScope;

	// comma-separated list of regex filters that specify groovy scripts.
	public static final String GROOVY_SCRIPT_FILTER = "groovy.script.filter";

	// default list of regex filters to specify groovy scripts
	public static final String DEFAULT_GROOVY_SCRIPT_FILTER = "scripts/**/*.groovy,src/main/resources/**/*.groovy,src/test/resources/**/*.groovy";

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public void setPreference(String key, List<String> vals) {
		String concat;
		if (vals == null) {
			concat = "";
		} else {
			// we should escape all ',' that happen to exist in the string, but
			// these should not be here since the strings were validated on entry
			StringBuilder sb = new StringBuilder();
			for (Iterator<String> valIter = vals.iterator(); valIter.hasNext();) {
				sb.append(valIter.next());
				if (valIter.hasNext()) {
					sb.append(",");
				}
			}
			concat = sb.toString();
		}
		getPreferences().put(key, concat);
	}

	public void setPreference(String key, String val) {
		if (val == null) {
			val = "";
		}
		getPreferences().put(key, val);
	}

	public List<String> getListStringPreference(String key, String def) {
		String result = getPreferences().get(key, def);
		if (result == null) {
			result = "";
		}
		String[] splits = result.split(",");
		return Arrays.asList(splits);
	}

	public String getStringPreference(String key, String def) {
		return getPreferences().get(key, def);
	}

	private IEclipsePreferences getPreferences() {
		if (instanceScope == null) {
			instanceScope = ((IScopeContext) new InstanceScope()).getNode(Activator.PLUGIN_ID);
		}
		return instanceScope;
	}
}
