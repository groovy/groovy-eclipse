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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * Decorates Groovy Java elements with a GR decorator.
 */
public class GroovyElementDecorator implements ILightweightLabelDecorator {

    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof IAdaptable) {
            @SuppressWarnings("cast")
            IMember member = (IMember) ((IAdaptable) element).getAdapter(IMember.class);
            if (member != null) {
                try {
                    ICompilationUnit unit = member.getCompilationUnit();
                    if (unit != null) {
                        IResource resource = unit.getResource();
                        if (resource != null &&
                                ContentTypeUtils.isGroovyLikeFileName(resource.getName())) {
                            decoration.addOverlay(
                                    GroovyPluginImages.DESC_GROOVY_OVERLAY,
                                    IDecoration.BOTTOM_RIGHT);
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
}
