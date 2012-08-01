/*******************************************************************************
 * Copyright (c) 2009, 2010 Codehaus.org, SpringSource, and others.
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.jdt.groovy.core";

	// The shared instance
	private static Activator plugin;

	private IEclipsePreferences instanceScope;

	public static final String GROOVY_CHECK_FOR_COMPILER_MISMATCH = "groovy.check.for.compiler.mismatch";

	// comma-separated list of regex filters that specify groovy scripts.
	public static final String GROOVY_SCRIPT_FILTERS = "groovy.script.filters";
	public static final String GROOVY_SCRIPT_FILTERS_ENABLED = "groovy.script.filters.enabled";

	// default list of regex filters to specify groovy scripts
	public static final String DEFAULT_GROOVY_SCRIPT_FILTER = "**/*.dsld,y,scripts/**/*.groovy,y,src/main/resources/**/*.groovy,y,src/test/resources/**/*.groovy,y";

	// preference constant that if true means this project uses its own compiler settings
	public static final String USING_PROJECT_PROPERTIES = "org.codehaus.groovy.eclipse.preferences.compiler.project";

	public static final String GROOVY_COMPILER_LEVEL = "groovy.compiler.level";

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

	public void setPreference(IEclipsePreferences preferences, String key, List<String> vals) {
		if (preferences == null) {
			preferences = getProjectOrWorkspacePreferences(null);
		}
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
		preferences.put(key, concat);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			Util.log(e);
		}
	}

	public void setPreference(IEclipsePreferences preferences, String key, String val) {
		if (val == null) {
			val = "";
		}
		if (preferences == null) {
			preferences = getProjectOrWorkspacePreferences(null);
		}
		preferences.put(key, val);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			Util.log(e);
		}
	}

	public List<String> getListStringPreference(IEclipsePreferences preferences, String key, String def) {
		if (preferences == null) {
			preferences = getProjectOrWorkspacePreferences(null);
		}
		String result = preferences.get(key, def);
		if (result == null) {
			result = "";
		}
		String[] splits = result.split(",");
		return Arrays.asList(splits);
	}

	public String getStringPreference(IEclipsePreferences preferences, String key, String def) {
		if (preferences == null) {
			preferences = getProjectOrWorkspacePreferences(null);
		}
		return preferences.get(key, def);
	}

	public IEclipsePreferences getProjectOrWorkspacePreferences(IProject project) {
		IEclipsePreferences projectPreferences = getProjectScope(project);
		if (projectPreferences != null && projectPreferences.getBoolean(USING_PROJECT_PROPERTIES, false)) {
			return projectPreferences;
		} else {
			if (instanceScope == null) {
				instanceScope = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			}
			return instanceScope;
		}
	}

	private IEclipsePreferences getProjectScope(IProject project) {
		if (project == null) {
			return null;
		}

		IScopeContext projectScope = new ProjectScope(project);
		return projectScope.getNode(PLUGIN_ID);
	}

	public String getGroovyCompilerLevel(IProject project) {
		IEclipsePreferences projectPreferences = getProjectScope(project);
		if (projectPreferences != null) {
			return projectPreferences.get(GROOVY_COMPILER_LEVEL, null);
		} else {
			return null;
		}
	}

	public void setGroovyCompilerLevel(IProject project, String level) {
		IEclipsePreferences projectPreferences = getProjectScope(project);
		if (projectPreferences != null) {
			projectPreferences.put(GROOVY_COMPILER_LEVEL, level);
			try {
				projectPreferences.flush();
			} catch (BackingStoreException e) {
				Util.log(e);
			}
		}
	}

	/**
	 * @param groovyScriptFilter
	 * @param b
	 * @return
	 */
	public boolean getBooleanPreference(IEclipsePreferences preferences, String key, boolean def) {
		if (preferences == null) {
			preferences = getProjectOrWorkspacePreferences(null);
		}
		return preferences.getBoolean(key, def);
	}
}
