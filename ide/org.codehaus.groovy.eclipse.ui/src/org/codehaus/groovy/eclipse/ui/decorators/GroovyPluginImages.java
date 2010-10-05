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
package org.codehaus.groovy.eclipse.ui.decorators;

import java.net.URL;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

/**
 * @author Thorsten Kamann
 *
 */
public class GroovyPluginImages {

	public static final IPath ICONS_PATH= new Path("icons"); //$NON-NLS-1$

	// The plug-in registry
	private static final ImageRegistry imageRegistry= new ImageRegistry();
	private static final Bundle bundle = GroovyPlugin.getDefault().getBundle();

	public static final String IMG_NEW_GROOVY_PROJECT = "icons/full/newgroovyprj_wiz.gif";
	public static final String IMG_GROOVY_FILE = "icons/groovy_file.gif";

    public static final String IMG_GROOVY_FILE_NO_BUILD = "icons/groovy_file_no_build.gif";

    public static final String IMG_GROOVY_FILE_SCRIPT = "icons/groovy_script_file.gif";

	public static final String IMG_GROOVY_OVERLAY = "icons/groovy-project-overlay.gif";


	public static final ImageDescriptor DESC_NEW_GROOVY_PROJECT = createDescriptor(IMG_NEW_GROOVY_PROJECT);
	public static final ImageDescriptor DESC_GROOVY_FILE = createDescriptor(IMG_GROOVY_FILE);
	public static final ImageDescriptor DESC_GROOVY_FILE_NO_BUILD = createDescriptor(IMG_GROOVY_FILE_NO_BUILD);

    public static final ImageDescriptor DESC_GROOVY_FILE_SCRIPT = createDescriptor(IMG_GROOVY_FILE_SCRIPT);
	public static final ImageDescriptor DESC_GROOVY_OVERLAY = createDescriptor(IMG_GROOVY_OVERLAY);

	public static ImageDescriptor createDescriptor(String path) {
        URL url = bundle.getEntry(path);
        ImageDescriptor descriptor = url == null ?
                ImageDescriptor.getMissingImageDescriptor() :
                    ImageDescriptor.createFromURL(url);
        imageRegistry.put(path, descriptor);
        return descriptor;
    }



	/**
	 * Returns the image managed under the given key in this registry.
	 *
	 * @param key the image's key
	 * @return the image managed under the given key
	 */
	public static Image get(String key) {
		return imageRegistry.get(key);
	}

	/**
	 * Returns the image descriptor for the given key in this registry. Might be called in a non-UI thread.
	 *
	 * @param key the image's key
	 * @return the image descriptor for the given key
	 */
	public static ImageDescriptor getDescriptor(String key) {
		return imageRegistry.getDescriptor(key);
	}
}
