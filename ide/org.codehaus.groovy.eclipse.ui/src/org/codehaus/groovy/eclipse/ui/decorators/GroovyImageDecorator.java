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

import static org.eclipse.jdt.groovy.core.util.ContentTypeUtils.isGroovyLikeFileName;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class GroovyImageDecorator extends BaseLabelProvider implements ILabelDecorator {

    private ILabelDecorator problemsDecorator = new ProblemsLabelDecorator();
    private Map<IProject, ScriptFolderSelector> scriptFolderSelectors = new HashMap<IProject, ScriptFolderSelector>(); // TODO: GroovyParser maintains this same cache

    public Image decorateImage(Image image, Object element) {
        if (preventRecursion) {
            return null;
        }

        boolean isGroovyFile = false, noBaseImage = (image == null);
        if (element instanceof String && (isGroovyFile = isGroovyLikeFileName((String) element))) {
            // a request where an IResource cannot be located (probably opening from source control)
            image = getImage(new JavaElementImageDescriptor(GroovyPluginImages.DESC_GROOVY_FILE, 0, JavaElementImageProvider.SMALL_SIZE));
        } else {
            IResource resource = null;
            if (element instanceof IFile) {
                resource = (IResource) element;
            } else if (element instanceof ICompilationUnit) {
                resource = ((ICompilationUnit) element).getResource();
            }
            if (resource != null && (isGroovyFile = isGroovyLikeFileName(resource.getName()))) {
                image = getJavaElementImageDescriptor(image, resource);
            }
        }

        if (image != null && isGroovyFile) {
            preventRecursion = true;
            try {
                // the Java ProblemsDecorator is not registered in the official
                // decorator list of eclipse, so we need it to call ourself.
                // problem: if jdt includes more decorators, we won't know it.
                image = problemsDecorator.decorateImage(image, element);

                // add non-problem decor, like type indicator and version control status, only if not for editor title
                if (!noBaseImage) {
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
        int flags = 0;
        Point size = JavaElementImageProvider.SMALL_SIZE;
        if (image != null && image.getBounds().width > 16) {
            size = JavaElementImageProvider.BIG_SIZE;
        }
        ImageDescriptor desc = GroovyPluginImages.DESC_GROOVY_FILE_NO_BUILD;
        try {
            if (isGroovyProject(resource.getProject())) {
                if (isRuntimeCompiled(resource)) {
                    desc = GroovyPluginImages.DESC_GROOVY_FILE;
                    //flags |= JavaElementImageDescriptor.STATIC;
                    flags |= JavaElementImageDescriptor.IGNORE_OPTIONAL_PROBLEMS;
                } else {
                    desc = GroovyPluginImages.DESC_GROOVY_FILE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getImage(new JavaElementImageDescriptor(desc, flags, size));
    }

    private boolean isGroovyProject(IProject project) throws CoreException {
        return project.hasNature("org.eclipse.jdt.groovy.core.groovyNature");
    }

    private boolean isRuntimeCompiled(IResource resource) throws CoreException {
        ScriptFolderSelector scriptFolderSelector = scriptFolderSelectors.get(resource.getProject());
        if (scriptFolderSelector == null) {
            scriptFolderSelectors.put(resource.getProject(), (scriptFolderSelector = new ScriptFolderSelector(resource.getProject())));
        }
        return scriptFolderSelector.isScript(resource);
    }

    private Image getImage(ImageDescriptor descriptor) {
        return JavaPlugin.getImageDescriptorRegistry().get(descriptor);
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public String decorateText(String text, Object element) {
        return null;
    }
}
