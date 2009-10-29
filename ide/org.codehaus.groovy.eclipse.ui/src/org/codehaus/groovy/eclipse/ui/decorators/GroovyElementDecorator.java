/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
	    if (element instanceof IAdaptable) {
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
	            } catch (Exception e) {
	                // ignore???
	            }
	        }
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}