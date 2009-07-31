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
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.TreeHierarchyLayoutProblemsDecorator;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorManager;

public class GroovyImageDecorator implements ILabelDecorator {

    private ImageDescriptorRegistry fRegistry;
    private TreeHierarchyLayoutProblemsDecorator problemsDecorator;
    private DecoratorManager decman;
    private boolean preventRecursion = false;
    
    public GroovyImageDecorator() {
        problemsDecorator = new TreeHierarchyLayoutProblemsDecorator();
        decman = WorkbenchPlugin.getDefault().getDecoratorManager();
    }

    public Image decorateImage(Image image, Object element) {
        if (preventRecursion) {
            return null;
        }
        
        boolean isApplicable = false;
        if (element instanceof GroovyCompilationUnit) {
            image = getJavaElementImageDescriptor(image, ((GroovyCompilationUnit) element).getResource());
            isApplicable = true;
        } else if (element instanceof IResource && ContentTypeUtils.isGroovyLikeFileName(((IResource) element).getName())) {
            image = getJavaElementImageDescriptor(image, (IResource) element);
            isApplicable = true;
        }
        
        if (isApplicable) {
            preventRecursion = true;
            try {
                //the Java ProblemsDecorator is not registered in the official
                //decorator list of eclipse, so we need it to call ourself.
                //problem: if jdt includes more decorators, we won't know it.
                image = problemsDecorator.decorateImage(image, element);
                
                //apply standard decorators (eg cvs)
                image = decman.decorateImage(image, element);
            } finally {
                preventRecursion = false;
            }
            return image;
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
        return getImageLabel(new JavaElementImageDescriptor(GroovyPluginImages.DESC_GROOVY_FILE, 0, size));
    }
    private static boolean useSmallSize(int flags) {
        return (flags & JavaElementImageProvider.SMALL_ICONS) != 0;
    }

    private Image getImageLabel(ImageDescriptor descriptor){
        if (descriptor == null) 
            return null;    
        return getRegistry().get(descriptor);
    }
    
    private ImageDescriptorRegistry getRegistry() {
        if (fRegistry == null) {
            fRegistry= JavaPlugin.getImageDescriptorRegistry();
        }
        return fRegistry;
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
