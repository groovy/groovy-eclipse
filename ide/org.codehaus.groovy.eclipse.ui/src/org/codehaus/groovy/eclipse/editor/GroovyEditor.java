 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.actions.OrganizeGroovyImportsAction;
import org.codehaus.groovy.eclipse.ui.decorators.GroovyImageDecorator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.actions.GenerateActionGroup;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

public class GroovyEditor extends CompilationUnitEditor {
    public static final String EDITOR_ID = "org.codehaus.groovy.eclipse.editor.GroovyEditor";

    private GroovyImageDecorator decorator = new GroovyImageDecorator();
    
    public GroovyEditor() {
		super();
		setDocumentProvider(GroovyPlugin.getDefault().getDocumentProvider());
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
        Object element = getEditorInput().getAdapter(IFile.class);
        if (element == null) {
            // will be null if coming from a code repository such as svn or cvs
            element = getEditorInput().getName();
        }
        return decorator.decorateImage(null, element);
    }
    
    @Override
    protected void setSelection(ISourceReference reference, boolean moveCursor) {
    	super.setSelection(reference, moveCursor);

    	// must override functionality because JavaEditor expects that there is a ';' at end of declaration
    	try {
			if (reference instanceof IImportDeclaration && moveCursor) {
				int offset;
				int length;
				ISourceRange range = ((ISourceReference) reference).getSourceRange();
				String content= reference.getSource();
				if (content != null) {
					int start= content.indexOf("import") + 6; //$NON-NLS-1$
					while (start < content.length() && content.charAt(start) == ' ')
						start++;
					
					int end= content.trim().length();
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
    
    
    @Override
    protected void createActions() {
        super.createActions();
        
        
        // use our Organize Imports instead
        GenerateActionGroup group = getGenerateActionGroup();
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fOrganizeImports", group, new OrganizeGroovyImportsAction(this));
        
        IAction organizeImports = new OrganizeGroovyImportsAction(this);
        organizeImports
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.ORGANIZE_IMPORTS);
        setAction("OrganizeImports", organizeImports); //$NON-NLS-1$

    }
    
    // Causes class cast exceptions when setting preferences, so don't use
//    /*
//     * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#createJavaSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, org.eclipse.jface.text.source.IOverviewRuler, boolean, int)
//     */
//    protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
//        return new GroovySourceViewer(this, parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);
//    }
    
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