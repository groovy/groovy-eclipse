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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class GroovyImageDecorator implements ILabelDecorator {

    protected ILabelDecorator problemsDecorator = new ProblemsLabelDecorator();
    protected ScriptFolderSelector scriptFolderSelector = new ScriptFolderSelector(null);
    // declare locally to prevent accidentally loading the GroovyNature class or its bundle
    protected static final String GROOVY_NATURE = "org.eclipse.jdt.groovy.core.groovyNature";

    public GroovyImageDecorator() {
        // receive notification when script folders change
        InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).addPreferenceChangeListener(new IPreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent event) {
                // preference has changed; ensure that the new preferece is used
                scriptFolderSelector = new ScriptFolderSelector(null);
                // TODO: we should automatically do a refresh of all places where this is used, but we are not doing that now
            }
        });
    }

    public Image decorateImage(Image image, Object element) {
        if (preventRecursion) {
            return null;
        }

        if (element instanceof String) {
            // a request where an IResource cannot be found (probably opening from source control)
            image = getImageLabel(new JavaElementImageDescriptor(GroovyPluginImages.DESC_GROOVY_FILE, 0, JavaElementImageProvider.SMALL_SIZE));
        } else {
            IResource resource = null;
            if (element instanceof IResource) {
                resource = (IResource) element;
            } else if (element instanceof ICompilationUnit) {
                resource = ((ICompilationUnit) element).getResource();
            }
            if (resource != null && ContentTypeUtils.isGroovyLikeFileName(resource.getName())) {
                image = getJavaElementImageDescriptor(image, resource);
            }
        }

        if (image != null) {
            preventRecursion = true;
            try {
                // the Java ProblemsDecorator is not registered in the official
                // decorator list of eclipse, so we need it to call ourself.
                // problem: if jdt includes more decorators, we won't know it.
                image = problemsDecorator.decorateImage(image, element);

                if (element instanceof ICompilationUnit) {
                    if (defaultDecorator == null) {
                        defaultDecorator = WorkbenchPlugin.getDefault().getDecoratorManager();
                    }
                    image = defaultDecorator.decorateImage(image, element);
                }
            } finally {
                preventRecursion = false;
            }
            return image;
        }

        return null;
    }
    private boolean preventRecursion;
    private ILabelDecorator defaultDecorator;

    private Image getJavaElementImageDescriptor(Image image, IResource resource) {
        int flags;
        if (image != null) {
            Rectangle rect = image.getBounds();
            flags = (rect.width == 16) ? JavaElementImageProvider.SMALL_ICONS : 0;
        } else {
            flags = JavaElementImageProvider.SMALL_ICONS;
        }
        Point size = useSmallSize(flags) ? JavaElementImageProvider.SMALL_SIZE : JavaElementImageProvider.BIG_SIZE;
        ImageDescriptor desc;
        try {
            if (resource.getProject().hasNature(GROOVY_NATURE)) {
                if (scriptFolderSelector.isScript(resource.getProjectRelativePath().toPortableString().toCharArray())) {
                    desc = GroovyPluginImages.DESC_GROOVY_FILE_SCRIPT;
                } else {
                    desc = GroovyPluginImages.DESC_GROOVY_FILE;
                }
            } else {
                desc = GroovyPluginImages.DESC_GROOVY_FILE_NO_BUILD;
            }
        } catch (CoreException e) {
            desc = GroovyPluginImages.DESC_GROOVY_FILE_NO_BUILD;
        }
        return getImageLabel(new JavaElementImageDescriptor(desc, 0, size));
    }

    private static boolean useSmallSize(int flags) {
        return (flags & JavaElementImageProvider.SMALL_ICONS) != 0;
    }

    private Image getImageLabel(ImageDescriptor descriptor) {
        if (descriptor == null)
            return null;
        if (registry == null) {
            registry = JavaPlugin.getImageDescriptorRegistry();
        }
        return registry.get(descriptor);
    }
    private ImageDescriptorRegistry registry;

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }

    public String decorateText(String text, Object element) {
        return null;
    }
}
