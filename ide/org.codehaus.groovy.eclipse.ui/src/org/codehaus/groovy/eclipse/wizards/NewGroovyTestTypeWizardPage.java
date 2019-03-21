/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.wizards;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.formatter.SemicolonRemover;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.junit.util.JUnitStatus;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageOne;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MultiTextEdit;

public class NewGroovyTestTypeWizardPage extends NewTestCaseWizardPageOne {

    public NewGroovyTestTypeWizardPage(NewTestCaseWizardPageTwo page2) {
        super(page2);
    }

    // TODO: override createJUnit4Controls to add Spock test option

    private IType maybeCreatedType;

    @Override
    public IType getCreatedType() {
        return maybeCreatedType != null ? maybeCreatedType : super.getCreatedType();
    }

    @Override
    protected String getCompilationUnitName(String typeName) {
        return typeName + ".groovy";
    }

    @Override
    protected String getJUnit3TestSuperclassName() {
        return "groovy.util.GroovyTestCase";
    }

    /**
     * Checks if the selected project where the test cases will be created
     * contains Groovy nature. True if yes, false otherwise
     *
     * @return true if project has a Groovy nature. False otherwise.
     */
    protected boolean hasGroovyNature() {
        IProject project = getProject();
        if (project != null) {
            return GroovyNature.hasGroovyNature(project);
        }
        return false;
    }

    /**
     * Gets the workspace project where the test case will be added.
     * May be null.
     *
     * @return workspace project if it exists, or null
     */
    protected IProject getProject() {
        IJavaProject javaProject = getJavaProject();
        if (javaProject == null) {
            return null;
        }
        return javaProject.getProject();
    }

    /**
     * Ensure that GroovyTestCase is seen as OK
     * to have in the super class field even if
     * JUnit 3 is not yet on the classpath
     */
    @Override
    protected IStatus superClassChanged() {
        // replaces the super class validation of of the normal type wizard
        if (isJUnit4()) {
            return super.superClassChanged();
        }

        String superClassName= getSuperClass();
        if (getJUnit3TestSuperclassName().equals(superClassName)) {
            return new JUnitStatus();
        }

        return super.superClassChanged();
    }

    @Override
    public int getModifiers() {
        int modifiers = super.getModifiers();
        // Groovy classes do not need public/private/protected
        return modifiers & ~(F_PUBLIC | F_PRIVATE | F_PROTECTED);
    }

    @Override
    protected void updateStatus(IStatus status) {
        // GRECLIPSE-728
        if (!hasGroovyNature()) {
            super.updateStatus(new Status(IStatus.ERROR, GroovyPlugin.PLUGIN_ID, "Project is not a Groovy Project. Please select a Groovy project."));
            return;
        }
        super.updateStatus(status);
    }

    @Override
    public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {
        // GRECLIPSE-728
        if (!hasGroovyNature()) {
            GroovyCore.logWarning("Project is not a Groovy Project. Please select a Groovy project.");
            return;
        }

        SubMonitor submon = SubMonitor.convert(monitor, 5);

        GroovyCompilationUnit unit;
        String name = getTypeName();
        IPackageFragment pack = getPackageFragment();
        // GRECLIPSE-322
        if (pack == null) pack = getPackageFragmentRoot().getPackageFragment("");
        // if JUnit 3 and default package, calling super.creatType will be an error
        if (!isJUnit4() && getPackageFragment().getElementName().equals("")) {
            String newline = StubUtility.getLineDelimiterUsed(pack.getJavaProject());
            StringBuilder source = new StringBuilder();
            String superClass = getSuperClass();

            String[] tokens = superClass.split("\\.");
            if (tokens.length > 1) {
                source.append("import ").append(superClass).append(newline).append(newline);
            }

            source.append("class ").append(name).append(" extends ").append(tokens[tokens.length - 1]);
            source.append(" {").append(newline);
            // TODO: append requested members
            source.append(newline).append("}").append(newline);

            unit = (GroovyCompilationUnit) pack.createCompilationUnit(getCompilationUnitName(name), source.toString(), true, submon.newChild(1));

            maybeCreatedType = unit.getType(name);
        } else {
            super.createType(submon.newChild(1));

            unit = (GroovyCompilationUnit) pack.getCompilationUnit(getCompilationUnitName(name));
        }

        try {
            char[] contents = unit.getContents();
            unit.becomeWorkingCopy(submon.newChild(1));
            MultiTextEdit textEdit = new MultiTextEdit();

            // remove ';' from declarations and statements
            new SemicolonRemover(
                new TextSelection(0, contents.length),
                new Document(String.valueOf(contents)),
                textEdit
            ).format();
            submon.worked(1);

            // TODO: post-process methods: remove public, remove throws

            // TODO: organize imports

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
}
