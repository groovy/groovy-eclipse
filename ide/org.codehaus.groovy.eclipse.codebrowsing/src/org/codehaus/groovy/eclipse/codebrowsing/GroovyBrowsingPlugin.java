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
package org.codehaus.groovy.eclipse.codebrowsing;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class GroovyBrowsingPlugin extends AbstractUIPlugin {
	// The shared instance.
	private static GroovyBrowsingPlugin plugin;

	public GroovyBrowsingPlugin() {
		plugin = this;
	}

	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
        // TODO consider turning this into an extension point.
		// This object needs to be injected into GroovyCompilationUnit
		// before code selection starts.
		GroovyCompilationUnit.setSelectHelper(new CodeSelectHelper());
	}

	@Override
    public void stop(BundleContext context) throws Exception {
		super.stop(context);
        GroovyCompilationUnit.setSelectHelper(null);
		plugin = null;
	}

	public static GroovyBrowsingPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
				"org.codehaus.groovy.eclipse.codebrowsing", path);
	}
}
