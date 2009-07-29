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
package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.ui.decorators.GroovyImageDecorator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;

public class GroovyEditor extends CompilationUnitEditor {
    public static final String EDITOR_ID = "org.codehaus.groovy.eclipse.editor.GroovyEditor";

    private GroovyImageDecorator decorator = new GroovyImageDecorator();
    
    public GroovyEditor() {
		super();
        setRulerContextMenuId("#GroovyCompilationUnitRulerContext"); //$NON-NLS-1$  
	}

    protected void setPreferenceStore(IPreferenceStore store) {
        super.setPreferenceStore(store);
        GroovyTextTools textTools= GroovyPlugin.getDefault().getTextTools();
        setSourceViewerConfiguration(new GroovyConfiguration(textTools.getColorManager(), store, this));    
    }

    
	@Override
	public IEditorInput getEditorInput() {
	    return super.getEditorInput();
	}

    public int getCaretOffset() {
        ISourceViewer viewer = getSourceViewer();
        return viewer.getTextWidget().getCaretOffset();
    }
    
    @Override
    public Image getTitleImage() {
        return decorator.decorateImage(null, getEditorInput().getAdapter(IFile.class));
    }
    
    @Override
    protected void setSelection(ISourceReference reference, boolean moveCursor) {
    	super.setSelection(reference, moveCursor);

    	// must override functionality because JavaEditor expects that there is a ';' at end of declaration
    	try {
			if (reference instanceof IImportDeclaration) {
				int offset;
				int length;
				ISourceRange range = ((ISourceReference) reference).getSourceRange();
				String content= reference.getSource();
				if (content != null) {
					int start= content.indexOf("import") + 6; //$NON-NLS-1$
					while (start < content.length() && content.charAt(start) == ' ')
						start++;
					
					int end= content.trim().length()-1;
					do {
						end--;
					} while (end >= 0 && content.charAt(end) == ' ');
					
					offset= range.getOffset() + start;
					length= end - start + 1;
				} else {
					// fallback
					offset= range.getOffset();
					length= range.getLength();
				}
				
				if (offset > -1 && length > 0) {

					try  {
						getSourceViewer().getTextWidget().setRedraw(false);
						getSourceViewer().revealRange(offset, length);
						getSourceViewer().setSelectedRange(offset, length);
					} finally {
						getSourceViewer().getTextWidget().setRedraw(true);
					}

					markInNavigationHistory();
				}

			}
		} catch (JavaModelException e) {
			GroovyCore.logException("Error selecting import statement", e);
		}
    }
    
    
    /*
     * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#createJavaSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, org.eclipse.jface.text.source.IOverviewRuler, boolean, int)
     */
    protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
        return new GroovySourceViewer(this, parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);
    }
    
    /*
     * Make accessible to source viewer
     */
    @Override
    protected void setStatusLineErrorMessage(String message) {
        super.setStatusLineErrorMessage(message);
    }
    
    /*
     * Make accessible to source viewer
     */
    @Override
    protected ITypeRoot getInputJavaElement() {
        return super.getInputJavaElement();
    }

}