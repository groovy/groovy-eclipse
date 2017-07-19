/*
 * Copyright 2009-2017 the original author or authors.
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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

public class NewClassWizardPage extends org.eclipse.jdt.ui.wizards.NewClassWizardPage {

    public NewClassWizardPage() {
        super();
        setTitle("Groovy Class");
        setDescription("Create a new Groovy class.");
    }

    @Override
    public int getModifiers() {
        int modifiers = super.getModifiers();
        return modifiers & ~F_PUBLIC; // public is default in Groovy
    }

    protected boolean isPackagePrivate() {
        int modifiers = super.getModifiers();
        return 0 == (modifiers & (F_PUBLIC | F_PRIVATE | F_PROTECTED));
    }

    @Override
    protected void createModifierControls(Composite composite, int nColumns) {
        // TODO: Add radio buttons for class, enum, interface, @interface, script, or trait
        super.createModifierControls(composite, nColumns);
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
                status.setWarning(project.getElementName() + " is not a groovy project.  Groovy Nature will be added to project upon completion.");
            }
        } catch (CoreException e) {
            status.setError("Exception when accessing project natures for " + project.getElementName());
        }

        String typeName = getTypeNameWithoutParameters();
        // must not exist as a .groovy file
        if (!isEnclosingTypeSelected() && (status.getSeverity() < IStatus.ERROR)) {
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

        // lastly, check exclusion filters to see if Groovy files are allowed in the source folder
        if (status.getSeverity() < IStatus.ERROR) {
            try {
                ClasspathEntry entry = (ClasspathEntry) ((IPackageFragmentRoot) pack.getParent()).getRawClasspathEntry();
                if (entry != null) {
                    char[][] inclusionPatterns = entry.fullInclusionPatternChars();
                    char[][] exclusionPatterns = entry.fullExclusionPatternChars();
                    if (Util.isExcluded(pack.getResource().getFullPath().append(getCompilationUnitName(typeName)), inclusionPatterns, exclusionPatterns, false)) {
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
    public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {
        SubMonitor submon = SubMonitor.convert(monitor, 7);

        IPackageFragment pack = getPackageFragment();
        if (pack != null) {
            IProject project = pack.getJavaProject().getProject();
            if (!GroovyNature.hasGroovyNature(project)) {
                GroovyRuntime.addGroovyNature(project);
            }
        }

        super.createType(submon.newChild(1));

        GroovyCompilationUnit unit = (GroovyCompilationUnit) pack.getCompilationUnit(getCompilationUnitName(getTypeName()));
        try {
            char[] contents = unit.getContents();
            unit.becomeWorkingCopy(submon.newChild(1));
            MultiTextEdit textEdit = new MultiTextEdit();

            // remove ';' on package declaration
            IPackageDeclaration[] packs = unit.getPackageDeclarations();
            if (packs.length > 0) {
                ISourceRange range = packs[0].getSourceRange();
                int position = range.getOffset() + range.getLength();
                if (contents[position] == ';') {
                    textEdit.addChild(new ReplaceEdit(position, 1, ""));
                }
            }
            submon.worked(1);

            // remove ';' on import declaration
            IImportDeclaration[] imports = unit.getImports();
            if (imports != null && imports.length > 0) {
                ISourceRange range = imports[0].getSourceRange();
                int position = range.getOffset() + range.getLength() - 1;
                if (contents[position] == ';') {
                    textEdit.addChild(new ReplaceEdit(position, 1, ""));
                }
            }
            submon.worked(1);

            if (isPackagePrivate()) {
                // add package visibility transform
                IType type = unit.getType(getTypeName());
                int offset = type.getSourceRange().getOffset();
                if (type.getJavadocRange() != null) {
                    offset = type.getJavadocRange().getOffset();
                    offset += type.getJavadocRange().getLength();
                    while (contents[offset] == '\n' || contents[offset] == '\r') offset += 1;
                }
                textEdit.addChild(new InsertEdit(offset, "@PackageScope "));

                ImportRewrite importRewrite = CodeStyleConfiguration.createImportRewrite(unit, true);
                importRewrite.addImport("groovy.transform.PackageScope");
                textEdit.addChild(importRewrite.rewriteImports(submon.newChild(1)));
            }

            if (textEdit.hasChildren()) {
                unit.applyTextEdit(textEdit, submon.newChild(1));
                unit.commitWorkingCopy(true, submon.newChild(1));
            }
        } finally {
            if (unit != null) {
                unit.discardWorkingCopy();
            }
            monitor.done();
        }
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports, IProgressMonitor monitor) throws CoreException {
        super.createTypeMembers(type, imports, monitor);
        if (isCreateMain()) {
            // replace main method with a groovier version
            IMethod main = type.getMethod("main", new String[] {"[QString;"});
            if (main != null && main.exists()) {
                main.delete(true, monitor);
                String newline = StubUtility.getLineDelimiterUsed(main);
                type.createMethod("static main(args) {" + newline + "}", null, true, monitor);
            }
        }
    }

    @Override
    protected String getCompilationUnitName(String typeName) {
        return typeName + ".groovy";
    }

    private String getTypeNameWithoutParameters() {
        String typeNameWithParameters = getTypeName();
        int angleBracketOffset = typeNameWithParameters.indexOf('<');
        if (angleBracketOffset == -1) {
            return typeNameWithParameters;
        } else {
            return typeNameWithParameters.substring(0, angleBracketOffset);
        }
    }
}
