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
package org.codehaus.groovy.eclipse;

import java.net.URL;

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
	
	public static final String IMG_MISC_PUBLIC= "icons/full/obj16/methpub_obj.gif"; 			//$NON-NLS-1$
	public static final String IMG_MISC_PROTECTED= "icons/full/obj16/methpro_obj.gif"; 		//$NON-NLS-1$
	public static final String IMG_MISC_PRIVATE= "icons/full/obj16/methpri_obj.gif"; 		//$NON-NLS-1$
	public static final String IMG_MISC_DEFAULT= "icons/full/obj16/methdef_obj.gif"; 		//$NON-NLS-1$

	public static final String IMG_FIELD_PUBLIC= "icons/full/obj16/field_public_obj.gif"; 			//$NON-NLS-1$
	public static final String IMG_FIELD_PROTECTED= "icons/full/obj16/field_protected_obj.gif"; 		//$NON-NLS-1$
	public static final String IMG_FIELD_PRIVATE= "icons/full/obj16/field_private_obj.gif"; 		//$NON-NLS-1$
	public static final String IMG_FIELD_DEFAULT= "icons/full/obj16/field_default_obj.gif"; 		//$NON-NLS-1$

	public static final String IMG_OBJS_CLASS= "icons/full/obj16/class_obj.gif"; 
	public static final String IMG_OBJS_INTERFACE= "icons/full/obj16/int_obj.gif"; 
	public static final String IMG_OBJS_ANNOTATION= "icons/full/obj16/annotation_obj.gif"; 

	public static final String IMG_NEW_GROOVY_PROJECT= "icons/full/newgroovyprj_wiz.gif"; 
	public static final String IMG_GROOVY_FILE= "icons/groovy_file.gif"; 
	
	public static final ImageDescriptor DESC_MISC_PUBLIC= createDescriptor(IMG_MISC_PUBLIC);
	public static final ImageDescriptor DESC_MISC_PROTECTED= createDescriptor(IMG_MISC_PROTECTED);
	public static final ImageDescriptor DESC_MISC_PRIVATE= createDescriptor(IMG_MISC_PRIVATE);
	public static final ImageDescriptor DESC_MISC_DEFAULT= createDescriptor(IMG_MISC_DEFAULT);

	public static final ImageDescriptor DESC_FIELD_PUBLIC= createDescriptor(IMG_FIELD_PUBLIC); 
	public static final ImageDescriptor DESC_FIELD_PROTECTED= createDescriptor(IMG_FIELD_PROTECTED); 
	public static final ImageDescriptor DESC_FIELD_PRIVATE= createDescriptor(IMG_FIELD_PRIVATE); 
	public static final ImageDescriptor DESC_FIELD_DEFAULT= createDescriptor(IMG_FIELD_DEFAULT); 

	public static final ImageDescriptor DESC_OBJS_CLASS= createDescriptor(IMG_OBJS_CLASS);
	public static final ImageDescriptor DESC_OBJS_INTERFACE= createDescriptor(IMG_OBJS_INTERFACE);
	public static final ImageDescriptor DESC_OBJS_ANNOTATION= createDescriptor(IMG_OBJS_ANNOTATION);

	public static final ImageDescriptor DESC_NEW_GROOVY_PROJECT = createDescriptor(IMG_NEW_GROOVY_PROJECT);
	public static final ImageDescriptor DESC_GROOVY_FILE = createDescriptor(IMG_GROOVY_FILE);

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
