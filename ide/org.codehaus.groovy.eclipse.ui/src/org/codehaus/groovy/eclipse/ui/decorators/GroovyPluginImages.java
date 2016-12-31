/*
 * Copyright 2009-2016 the original author or authors.
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.FrameworkUtil;

public final class GroovyPluginImages {

    public static final ImageDescriptor DESC_GROOVY_FILE = createDescriptor("icons/groovy_file.gif");

    public static final ImageDescriptor DESC_GROOVY_FILE_NO_BUILD = createDescriptor("icons/groovy_file_no_build.gif");

    public static final ImageDescriptor DESC_NEW_GROOVY_PROJECT = createDescriptor("icons/full/newgroovyprj_wiz.gif");

    private static ImageDescriptor createDescriptor(String path) {
        // create the image descriptor without causing this bundle to be activated
        return ImageDescriptor.createFromURL(FrameworkUtil.getBundle(GroovyPluginImages.class).getEntry(path));
    }
}
