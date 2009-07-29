/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.ui.decorators;

import org.codehaus.groovy.eclipse.GroovyPluginImages;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class GroovyImageDecorator implements ILabelDecorator {
    private static final int ERRORTICK_WARNING= JavaElementImageDescriptor.WARNING;
    private static final int ERRORTICK_ERROR= JavaElementImageDescriptor.ERROR; 

    public Image decorateImage(Image image, Object element) {
        if (element instanceof GroovyCompilationUnit) {
            return getJavaElementImageDescriptor(image, ((GroovyCompilationUnit) element).getResource());
        } else if (element instanceof IResource && ContentTypeUtils.isGroovyLikeFileName(((IResource) element).getName())) {
            return getJavaElementImageDescriptor(image, (IResource) element);
        }
        return null;
    }
    private Image getJavaElementImageDescriptor(Image image, IResource resource) {
        int flags;
        if (image != null) {
            Rectangle rect = image.getBounds();
            flags = (rect.width == 16) ? JavaElementImageProvider.SMALL_ICONS : 0;
        } else {
            flags = JavaElementImageProvider.SMALL_ICONS;
        }
        Point size= useSmallSize(flags) ? JavaElementImageProvider.SMALL_SIZE : JavaElementImageProvider.BIG_SIZE;
        return new JavaElementImageDescriptor(GroovyPluginImages.DESC_GROOVY_FILE, getAdornments(resource), size).createImage();
    }
    private static boolean useSmallSize(int flags) {
        return (flags & JavaElementImageProvider.SMALL_ICONS) != 0;
    }

    private static int getAdornments(IResource resource) {
        try {
            int severity= resource.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            if (severity == IMarker.SEVERITY_ERROR) {
                return ERRORTICK_ERROR;
            } else if (severity == IMarker.SEVERITY_WARNING) {
                return ERRORTICK_WARNING;
            }
            return 0;
        } catch (CoreException e) {
            return 0;
        }
    }
    
    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose()  {
    }

    public boolean isLabelProperty(Object element, String property)  {
        return false;
    }

    public void removeListener(ILabelProviderListener listener)  {
    }

    public String decorateText(String text, Object element) {
        return null;
    }
}
