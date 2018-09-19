/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.ui;

import static org.eclipse.jdt.internal.ui.JavaPlugin.getImageDescriptorRegistry;

import org.codehaus.groovy.eclipse.ui.decorators.GroovyPluginImages;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class GroovyNavigatorLabelProvider implements ILabelProvider {

    protected ILabelDecorator labelDecorator = new ProblemsLabelDecorator(getImageDescriptorRegistry());

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof GroovyCompilationUnit) {
            return labelDecorator.decorateImage(newGroovySourceImage(), element);
        }

        boolean isGradle = false;
        if (element instanceof IFile && ContentTypeUtils.isGroovyLikeFileName(((IFile) element).getName()) &&
                           !(isGradle = ContentTypeUtils.isGradleLikeFileName(((IFile) element).getName()))) {
            return newGroovySourceImage();
        }
        if (isGradle) {
            newGradleScriptImage();
        }

        return null;
    }

    @Override
    public String getText(Object element) {
        return null;
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    protected static Image newGradleScriptImage() {
        return getImageDescriptorRegistry().get(
            // Gradle files are a special case; image should be from Buildship plugin if it's present otherwise use Groovy's
            GroovyPluginImages.DESC_GRADLE_FILE != null ? GroovyPluginImages.DESC_GRADLE_FILE : GroovyPluginImages.DESC_GROOVY_FILE);
    }

    protected static Image newGroovySourceImage() {
        return getImageDescriptorRegistry().get(GroovyPluginImages.DESC_GROOVY_FILE);
    }
}
