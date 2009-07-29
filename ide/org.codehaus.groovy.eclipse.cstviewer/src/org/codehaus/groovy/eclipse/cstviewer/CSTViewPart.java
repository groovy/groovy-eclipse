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
package org.codehaus.groovy.eclipse.cstviewer;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * @author David Kerber
 *
 */
public class CSTViewPart extends ViewPart {
	private IEditorPart editor; 
	private TreeViewer viewer;
	private IPartListener partListener;
	private IElementChangedListener buildListener; 

	private void hookIntoGroovyEditor() {
		partListener = new IPartListener() {
			public void partBroughtToTop(IWorkbenchPart part) {
				try {
					if (part instanceof IEditorPart) {
						IFile file = (IFile) ((IEditorPart) part).getEditorInput().getAdapter(IFile.class);
						if (ContentTypeUtils.isGroovyLikeFileName(file.getName())) {
							ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
							if (unit instanceof GroovyCompilationUnit) {
								if (editor != part) {
									editor = (IEditorPart) part;
									viewer.setInput(unit.getResource());
								} else {
									// nothing else to do!
								}
								return;
							}
						}
					}
				} catch (Exception e) {
					GroovyCore.logException("Error updating CST Viewer", e);
				}
				editor = null;
				// This is a guard - the content provider should not be null,
				// but sometimes this happens when the
				// part is disposed of for various reasons (unhandled exceptions
				// AFAIK). Without this guard,
				// error message popups continue until Eclipse if forcefully
				// killed.
				if (viewer.getContentProvider() != null) {
					viewer.setInput(null);
				}
			}
			public void partActivated(IWorkbenchPart part) {}
			public void partClosed(IWorkbenchPart part) {}
			public void partDeactivated(IWorkbenchPart part) {}
			public void partOpened(IWorkbenchPart part) {}
		};
		getSite().getPage().addPartListener(partListener);

		partListener.partBroughtToTop(getSite().getPage().getActiveEditor());
		
		buildListener = new IElementChangedListener() {
			public void elementChanged(ElementChangedEvent event) {
				// The editor is currently not a GroovyEditor, so
				// there is not
				// ASTView to refresh.
				if (editor == null) {
					return;
				}
				IJavaElementDelta delta = event.getDelta();
				
				IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
				final ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
				
				// determine if the delta contains the ICompUnit under question
				if (isUnitInDelta(delta, unit)) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							viewer.setInput(unit.getResource());					
						}
					});
				}
			}
			
			private boolean isUnitInDelta(IJavaElementDelta delta, ICompilationUnit unit) {
				
				IJavaElement elt = delta.getElement();
				if (elt.getElementType() == IJavaElement.COMPILATION_UNIT) {
					// comparing with a compilation unit
					// if test fails, no need to go further
					if (elt.getElementName().equals(unit.getElementName())) {
						return true;
					} else {
						return false;
					}
				}
				
				ICompilationUnit candidateUnit = (ICompilationUnit) elt.getAncestor(IJavaElement.COMPILATION_UNIT);
				if (candidateUnit != null) {
					// now if test fails, no need to go further
					if (candidateUnit.getElementName().equals(unit.getElementName())) {
						return true;
					} else {
						return false;
					}
				}
				
				// delta is a potential ancestor of this compilationUnit
				IJavaElementDelta[] deltas = delta.getAffectedChildren();
				if (deltas != null) {
					for (IJavaElementDelta delta2 : deltas) {
						if (isUnitInDelta(delta2, unit)) {
							return true;
						}
					}
				}
				return false;
			}
		};
		
		JavaCore.addElementChangedListener(buildListener, ElementChangedEvent.POST_RECONCILE);
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
    public void dispose() {
		getSite().getPage().removePartListener(partListener);
		JavaCore.removeElementChangedListener(buildListener);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new CSTContentProvider());
		viewer.setLabelProvider(new CSTLabelProvider());
		viewer.setSorter(null);
		viewer.setInput(null);
		
		hookIntoGroovyEditor();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
    public void setFocus() {}
	
}
