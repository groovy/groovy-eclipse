/*******************************************************************************
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
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codeassist;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GroovyContentAssistActivator extends AbstractUIPlugin {


	public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.codeassist.completion";

    public static final String FILTERED_DGMS = PLUGIN_ID + ".filtereddgms";

    private static GroovyContentAssistActivator plugin;

    public GroovyContentAssistActivator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

    public static GroovyContentAssistActivator getDefault() {
		return plugin;
	}

	public static void logError(Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, e.getLocalizedMessage(), e));
	}

	public static void logError(String message, Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, e));
	}

	public Set<String> getFilteredDGMs() {
	    String filtered = getPreferenceStore().getString(FILTERED_DGMS);
	    String[] filteredArr = filtered.split(",");
	    Set<String> filteredSet = new HashSet<String>(filteredArr.length);
	    for (String s : filteredArr) {
	        s = s.trim();
	        if (s.length() > 0) {
	            filteredSet.add(s);
	        }
        }
	    return filteredSet;
	}

	public void setFilteredDGMs(Set<String> filteredSet) {
	    StringBuilder sb = new StringBuilder();
	    for (String s : filteredSet) {
	        if (s == null) continue;
	        s = s.trim();
            if (s.length() > 0) {
                sb.append(s + ",");
	        }
        }
        getPreferenceStore().setValue(FILTERED_DGMS, sb.toString());
	}
}
