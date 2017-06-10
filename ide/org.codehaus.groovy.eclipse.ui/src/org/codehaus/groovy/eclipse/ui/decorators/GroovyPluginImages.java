/*
 * Copyright 2009-2017 the original author or authors.
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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public final class GroovyPluginImages {

    public static final ImageDescriptor DESC_GRADLE_FILE = createDescriptor2(
            "platform:/plugin/org.eclipse.buildship.ui/icons/full/obj16/gradle_file.png");
    public static final ImageDescriptor DESC_GROOVY_FILE = createDescriptor("$nl$/icons/groovy_file.gif");
    public static final ImageDescriptor DESC_NEW_GROOVY_PROJECT = createDescriptor("$nl$/icons/full/newgroovyprj_wiz.gif");

    private static ImageDescriptor createDescriptor(String path) {
        // create the image descriptor without causing this bundle to be activated
        return AbstractUIPlugin.imageDescriptorFromPlugin(GroovyPlugin.PLUGIN_ID, path);
    }

    private static ImageDescriptor createDescriptor2(String path) {
        try {
            java.net.URL url = new java.net.URL(path);
            url.openConnection(); // does image exist?
            return ImageDescriptor.createFromURL(url);
        } catch (Exception e) {
            return null;
        }
    }
}
