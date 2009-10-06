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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatAllGroovyAction;
import org.codehaus.groovy.eclipse.refactoring.actions.GroovyRenameAction;
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImportsAction;
import org.codehaus.groovy.eclipse.refactoring.actions.FormatAllGroovyAction.FormatKind;
import org.codehaus.groovy.eclipse.ui.decorators.GroovyImageDecorator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.debug.ui.BreakpointMarkerUpdater;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.actions.GenerateActionGroup;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.RefactorActionGroup;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;

public class GroovyEditor extends CompilationUnitEditor {
    /**
     * 
     * @author Andrew Eisenberg
     * @created Aug 20, 2009
     * ensure that this class is a noop
     */
    private class NoopCleanUpsAction extends AllCleanUpsAction {
    
        public NoopCleanUpsAction(IWorkbenchSite site) {
            super(site);
        }
    
        @Override
        public void dispose() {
        }
    
        @Override
        protected ICleanUp[] getCleanUps(ICompilationUnit[] units) {
            return new ICleanUp[0];
        }
    
        @Override
        protected void performRefactoring(ICompilationUnit[] cus,
                ICleanUp[] cleanUps) throws InvocationTargetException {
        }
    
        @Override
        public ICompilationUnit[] getCompilationUnits(
                IStructuredSelection selection) {
            return new ICompilationUnit[0];
        }
    
        @Override
        public void run(IStructuredSelection selection) {
        }
    
        @Override
        public void run(ITextSelection selection) {
        }
    
        @Override
        public void selectionChanged(IStructuredSelection selection) {
        }
    
        @Override
        public void selectionChanged(ITextSelection selection) {
        }
    }

    public static final String EDITOR_ID = "org.codehaus.groovy.eclipse.editor.GroovyEditor";

    private GroovyImageDecorator decorator = new GroovyImageDecorator();
    
    public GroovyEditor() {
		super();
//		setDocumentProvider(GroovyPlugin.getDefault().getDocumentProvider());
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
        Image image = decorator.decorateImage(null, element);
        // cannot return null GRECLIPSE-257
        return image != null? image : super.getTitleImage();
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
        
        GenerateActionGroup group = getGenerateActionGroup();
        
        // use our Organize Imports instead
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fOrganizeImports", group, new OrganizeGroovyImportsAction(this));
        IAction organizeImports = new OrganizeGroovyImportsAction(this);
        organizeImports
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.ORGANIZE_IMPORTS);
        setAction("OrganizeImports", organizeImports); //$NON-NLS-1$
        
        // use our Format instead
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fFormatAll", group, new FormatAllGroovyAction(this.getEditorSite(), FormatKind.FORMAT));
        IAction formatAction = new FormatAllGroovyAction(this.getEditorSite(), FormatKind.FORMAT);
        formatAction
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.FORMAT);
        setAction("Format", formatAction); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(formatAction, IJavaHelpContextIds.FORMAT_ACTION);
        
        // use our Indent instead
        IAction indentAction = new FormatAllGroovyAction(this.getEditorSite(), FormatKind.INDENT_ONLY);
        indentAction
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.INDENT);
        setAction("Indent", indentAction); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(indentAction, IJavaHelpContextIds.INDENT_ACTION);
        
        // now remove some actions:
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fAddGetterSetter", group, null);
        ReflectionUtils.setPrivateField(GenerateActionGroup.class, "fCleanUp", group, new NoopCleanUpsAction(getEditorSite()));
        
        // remove most refactorings since they are not yet really supported
        removeRefactoringAction("fSelfEncapsulateField");
        removeRefactoringAction("fMoveAction");
        removeRefactoringAction("fRenameAction");
        removeRefactoringAction("fModifyParametersAction");
        // fPullUpAction
        // fPushDownAction
        removeRefactoringAction("fIntroduceParameterAction");
        removeRefactoringAction("fIntroduceParameterObjectAction");
        removeRefactoringAction("fIntroduceFactoryAction");
        removeRefactoringAction("fExtractMethodAction");
        removeRefactoringAction("fExtractInterfaceAction");
        removeRefactoringAction("fExtractClassAction");
        removeRefactoringAction("fExtractSupertypeAction");
        removeRefactoringAction("fChangeTypeAction");
        removeRefactoringAction("fConvertNestedToTopAction");
        removeRefactoringAction("fInferTypeArgumentsAction");
        removeRefactoringAction("fConvertLocalToFieldAction");
        removeRefactoringAction("fConvertAnonymousToNestedAction");
        removeRefactoringAction("fIntroduceIndirectionAction");
        // fInlineAction
        removeRefactoringAction("fUseSupertypeAction");
        
        // use our Rename action instead
        IAction renameAction = new GroovyRenameAction(this);
        renameAction
                .setActionDefinitionId(IJavaEditorActionDefinitionIds.RENAME_ELEMENT);
        setAction("RenameElement", renameAction); //$NON-NLS-1$
    }
    
    private void removeRefactoringAction(String actionFieldName) {
        RefactorActionGroup group = getRefactorActionGroup();
        ISelectionChangedListener action = (ISelectionChangedListener) 
                ReflectionUtils.getPrivateField(RefactorActionGroup.class, actionFieldName, group);
        getSite().getSelectionProvider().removeSelectionChangedListener(action);
        ReflectionUtils.setPrivateField(RefactorActionGroup.class, actionFieldName, group, null);
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

    private IFile getFile() {
        IEditorInput input = getEditorInput();
        if (input instanceof FileEditorInput) {
            return ((FileEditorInput) input).getFile();
        } else {
            return null;
        }
    }
    
    private GroovyCompilationUnit getGroovyCompilationUnit() {
        IFile file = getFile();
        if (file != null) {
            return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        } else {
            return null;    
        }
    }
    
    private ModuleNode getModuleNode() {
        GroovyCompilationUnit unit = getGroovyCompilationUnit();
        if (unit != null) {
            return unit.getModuleNode();
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {
        if (IResource.class == required || IFile.class == required) {
            return this.getFile();
        }
        if (GroovyCompilationUnit.class == required || ICompilationUnit.class == required || CompilationUnit.class == required) {
            return this.getGroovyCompilationUnit();
        }
        
        if (ModuleNode.class == required) {
            return this.getModuleNode();
        }
        return super.getAdapter(required);
    }

    /**
     * Override this method so that we can get access to the newly initialized
     * annotation model
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        unsetJavaBreakpointUpdater();
    }
    
    
    /**
     * Override this method so that we can get access to the newly initialized
     * annotation model
     */
    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        super.doSetInput(input);
        unsetJavaBreakpointUpdater();
    }
    
    /**
     * Ensure that the Java breakpoint updater is removed because we need to use
     * Groovy's breakpoint updater instead
     */
    @SuppressWarnings("unchecked")
    private void unsetJavaBreakpointUpdater() {
        ISourceViewer viewer = getSourceViewer();
        if (viewer != null) {
            IAnnotationModel model = viewer.getAnnotationModel();
            if (model instanceof AbstractMarkerAnnotationModel) {
                // force instantiation of the extension points 
                ReflectionUtils.executePrivateMethod(AbstractMarkerAnnotationModel.class, "installMarkerUpdaters", 
                        new Class<?>[0], model, new Object[0]);
                // remove the marker updater for Java breakpoints, the groovy one will be used instead
                List<IConfigurationElement> updaterSpecs = (List<IConfigurationElement>) 
                        ReflectionUtils.getPrivateField(AbstractMarkerAnnotationModel.class, 
                        "fMarkerUpdaterSpecifications", model);
                for (Iterator<IConfigurationElement> specIter = updaterSpecs.iterator(); specIter
                        .hasNext();) {
                    IConfigurationElement spec = specIter.next();
                    if (spec.getAttribute("class").equals(BreakpointMarkerUpdater.class.getCanonicalName())) {
                        specIter.remove();
                        break;
                    }
                }
            }
        }
    }
}