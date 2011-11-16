/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.wizards;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * @author MelamedZ
 * @author Thorsten Kamann <thorsten.kamann@googlemail.com>
 */
public class NewClassWizardPage extends org.eclipse.jdt.ui.wizards.NewClassWizardPage {

    private static final int FINAL_INDEX = 1;

    private IStatus fStatus;

    /**
     * Creates a new <code>NewClassWizardPage</code>
     */
    public NewClassWizardPage() {
        super();
        setTitle("Groovy Class");
        setDescription("Create a new Groovy class");
    }


    @Override
    protected void createModifierControls(Composite composite, int nColumns) {
        super.createModifierControls(composite, nColumns);
        SelectionButtonDialogFieldGroup group = getOtherModifierButtonsFieldGroup();
        group.getSelectionButton(FINAL_INDEX).setText("Create Script");
    }

    @Override
    protected String getCompilationUnitName(String typeName) {
        return typeName + ".groovy";
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        super.createTypeMembers(type, imports, monitor);
        if (isCreateMain()) {
            // replace main method with a more groovy version
            IMethod main = type.getMethod("main", new String[] {"[QString;"} );
            if (main != null && main.exists()) {
                main.delete(true, monitor);
                type.createMethod("static main(args) {\n\n}", null, true, monitor);
            }
        }
    }

    @Override
    protected IStatus typeNameChanged() {
        StatusInfo status = (StatusInfo) super.typeNameChanged();
        IPackageFragment pack = getPackageFragment();
        if (pack == null) {
            return status;
        }

        IJavaProject project = pack.getJavaProject();
        try {
            if (!project.getProject().hasNature(GroovyNature.GROOVY_NATURE)) {
                status.setWarning(project.getElementName()
                        + " is not a groovy project.  Groovy Nature will be added to project upon completion.");
            }
        } catch (CoreException e) {
            status.setError("Exception when accessing project natures for " + project.getElementName());
        }

        String typeName = getTypeNameWithoutParameters();
        // must not exist as a .groovy file
        if (!isEnclosingTypeSelected()
                && (status.getSeverity() < IStatus.ERROR)) {
            if (pack != null) {
                IType type = null;
                try {
                    type = project.findType(pack.getElementName(), typeName);
                } catch (JavaModelException e) {
                    // can ignore
                }
                if (type != null && type.getPackageFragment().equals(pack)) {
                    status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExists);
                }
            }
        }

        // lastly, check exclusion filters to see if Groovy files are allowed in
        // the source folder
        if (status.getSeverity() < IStatus.ERROR) {
            try {
                ClasspathEntry entry = (ClasspathEntry) ((IPackageFragmentRoot) pack.getParent()).getRawClasspathEntry();
                if (entry != null) {
                    char[][] inclusionPatterns = entry.fullInclusionPatternChars();
                    char[][] exclusionPatterns = entry.fullExclusionPatternChars();
                    if (Util.isExcluded(pack.getResource().getFullPath().append(getCompilationUnitName(typeName)),
                            inclusionPatterns, exclusionPatterns, false)) {
                        status.setError("Cannot create Groovy type because of exclusion patterns on the source folder.");
                    }

                }
            } catch (JavaModelException e) {
                status.setError(e.getLocalizedMessage());
                GroovyCore.logException("Exception inside new Groovy class wizard", e);
            }
        }

        return status;
    }

    @Override
    public void createType(IProgressMonitor monitor) throws CoreException,
    InterruptedException {
        IPackageFragment pack = getPackageFragment();
        if (pack != null) {
            IProject project = pack.getJavaProject().getProject();
            if (!GroovyNature.hasGroovyNature(project)) {
                // add groovy nature
                GroovyRuntime.addGroovyNature(project);
            }
        }

        super.createType(monitor);
        monitor = new SubProgressMonitor(monitor, 1);


        GroovyCompilationUnit unit = (GroovyCompilationUnit) pack.getCompilationUnit(getCompilationUnitName(getTypeName()));
        try {
            monitor.beginTask("Remove semi-colons", 1);
            unit.becomeWorkingCopy(new SubProgressMonitor(monitor, 1));

            // remove ';' on package declaration
            IPackageDeclaration[] packs = unit.getPackageDeclarations();
            char[] contents = unit.getContents();
            MultiTextEdit multi = new MultiTextEdit();
            if (packs.length > 0) {
                ISourceRange range = packs[0].getSourceRange();
                int position = range.getOffset() + range.getLength();
                if (contents[position] == ';') {
                    multi.addChild(new ReplaceEdit(position, 1, ""));
                }
            }

            // remove ';' on import declaration
            IImportDeclaration[] imports = unit.getImports();
            if (imports != null && imports.length > 0) {
                ISourceRange range = imports[0].getSourceRange();
                int position = range.getOffset() + range.getLength() - 1;
                if (contents[position] == ';') {
                    multi.addChild(new ReplaceEdit(position, 1, ""));
                }
            }

            // remove type declaration for scripts
            if (isScript()) {
                ISourceRange range = unit.getTypes()[0].getSourceRange();
                multi.addChild(new DeleteEdit(range.getOffset(), range.getLength()));
            }
            if (multi.hasChildren()) {
                unit.applyTextEdit(multi, new SubProgressMonitor(monitor, 1));
                unit.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
            }
            monitor.worked(1);
        } finally {
            if (unit != null) {
                unit.discardWorkingCopy();
            }
            monitor.done();
        }
    }

    private boolean isScript() {
        // the final check box has been usurped and is now the checkbox for
        // script
        SelectionButtonDialogFieldGroup group = getOtherModifierButtonsFieldGroup();
        return group.isSelected(FINAL_INDEX);
    }

    /**
     * @return
     */
    public SelectionButtonDialogFieldGroup getOtherModifierButtonsFieldGroup() {
        return (SelectionButtonDialogFieldGroup) ReflectionUtils.getPrivateField(
                NewTypeWizardPage.class, "fOtherMdfButtons", this);
    }

    private String getTypeNameWithoutParameters() {
        String typeNameWithParameters= getTypeName();
        int angleBracketOffset= typeNameWithParameters.indexOf('<');
        if (angleBracketOffset == -1) {
            return typeNameWithParameters;
        } else {
            return typeNameWithParameters.substring(0, angleBracketOffset);
        }
    }

    @Override
    public int getModifiers() {
        int modifiers = super.getModifiers();
        modifiers &= ~F_PUBLIC;
        modifiers &= ~F_PRIVATE;
        modifiers &= ~F_PROTECTED;
        modifiers &= ~F_FINAL;
        return modifiers;
    }

    /**
     * Retrieve the current status, as last set by updateStatus.
     */
    public IStatus getStatus() {
        return fStatus;
    }

    @Override
    protected void updateStatus(IStatus status) {
        super.updateStatus(status);
        fStatus = status;
    }

}
