/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
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
