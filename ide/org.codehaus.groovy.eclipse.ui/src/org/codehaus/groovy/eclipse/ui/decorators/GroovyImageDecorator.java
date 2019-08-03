/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.ui.decorators;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.core.ExternalJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class GroovyImageDecorator extends BaseLabelProvider implements ILabelDecorator {

    @Override
    public void dispose() {
        baseImageProvider.disconnect();
    }

    private boolean preventRecursion;
    private ILabelDecorator defaultDecorator;
    private GroovyImageProvider baseImageProvider = new GroovyImageProvider(); // replaces JavaElementImageProvider
    private Map<IProject, ScriptFolderSelector> scriptFolderSelectors = new HashMap<>(); // TODO: GroovyParser maintains this same cache

    private Image applyDefaultDecorator(Image image, Object element) {
        preventRecursion = true;
        try {
            if (defaultDecorator == null) {
                defaultDecorator = WorkbenchPlugin.getDefault().getDecoratorManager();
            }
            return defaultDecorator.decorateImage(image, element);
        } finally {
            preventRecursion = false;
        }
    }

    @Override
    public  Image decorateImage(Image image, Object element) {
        if (preventRecursion) {
            return null;
        }

        if (element instanceof GroovyCompilationUnit) {
            return decorateImage(new ImageImageDescriptor(image), ((GroovyCompilationUnit) element).getResource(), getImageSize(image));
        }

        boolean isGradle = false;
        if (element instanceof IFile && ContentTypeUtils.isGroovyLikeFileName(((IFile) element).getName()) &&
                           !(isGradle = ContentTypeUtils.isGradleLikeFileName(((IFile) element).getName()))) {
            return decorateImage(new ImageImageDescriptor(image), (IFile) element, getImageSize(image));
        }

        // Gradle files are a special case; image should be from Buildship plugin if it's present otherwise use Groovy's
        if (isGradle) {
            ImageDescriptor base = (GroovyPluginImages.DESC_GRADLE_FILE != null
                ? GroovyPluginImages.DESC_GRADLE_FILE : GroovyPluginImages.DESC_GROOVY_FILE);
            return applyDefaultDecorator(getImage(base, 0, getImageSize(image)), element);
        }

        return null;
    }

    private Image decorateImage(ImageDescriptor base, IResource rsrc, Point size) {
        int flags = 0;

        if (!isExternalProject(rsrc.getProject())) {
            try {
                if (isGroovyProject(rsrc.getProject())) {
                    if (isRuntimeCompiled(rsrc)) {
                        // display "circle slash" in lower left corner
                        flags |= JavaElementImageDescriptor.IGNORE_OPTIONAL_PROBLEMS;
                    }
                } else {
                    // display "red exclamation" in lower left corner
                    flags |= JavaElementImageDescriptor.BUILDPATH_ERROR;
                }
            } catch (Exception e) {
                GroovyPlugin.getDefault().logError("Failed to apply image overlay(s) to: " + rsrc.getName(), e);
            }
        }

        return getImage(base, flags, size);
    }

    private Image getImage(ImageDescriptor base, int flags, Point size) {
        return JavaPlugin.getImageDescriptorRegistry().get(new JavaElementImageDescriptor(base, flags, size));
    }

    private Point getImageSize(Image image) {
        Point size = JavaElementImageProvider.SMALL_SIZE;
        if (image.getBounds().width > 16) {
            size = JavaElementImageProvider.BIG_SIZE;
        }
        return size;
    }

    /**
     * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider#createFakeCompiltationUnit(Object,boolean)
     */
    private boolean isExternalProject(IProject project) {
        return project.getName().equals(ExternalJavaProject.EXTERNAL_PROJECT_NAME);
    }

    private boolean isGroovyProject(IProject project) throws CoreException {
        // do not link to static constant to prevent activation of bundle
        return project.hasNature("org.eclipse.jdt.groovy.core.groovyNature");
    }

    private boolean isRuntimeCompiled(IResource resource) throws CoreException {
        return scriptFolderSelectors.computeIfAbsent(resource.getProject(), ScriptFolderSelector::new).isScript(resource);
    }

    //--------------------------------------------------------------------------

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public String decorateText(String text, Object element) {
        return null;
    }
}
