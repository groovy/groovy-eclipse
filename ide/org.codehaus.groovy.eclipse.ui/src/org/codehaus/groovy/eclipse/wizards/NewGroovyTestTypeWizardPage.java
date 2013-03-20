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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.util.JUnitStatus;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageOne;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo;

/**
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 *
 */
public class NewGroovyTestTypeWizardPage extends NewTestCaseWizardPageOne {

    private static final String DOT_GROOVY = ".groovy";
    private static final String GROOVY_TEST_CASE = "groovy.util.GroovyTestCase";

    protected static final String GROOVY_NATURE_ERROR_MSG = "Project is not a Groovy Project. Please select a Groovy project.";

    private IType maybeCreatedType;

    public NewGroovyTestTypeWizardPage(NewTestCaseWizardPageTwo page2) {
        super(page2);
    }

    @Override
    protected String getCompilationUnitName(String typeName) {
        return typeName + DOT_GROOVY;
    }

    @Override
    protected String getJUnit3TestSuperclassName() {
        return GROOVY_TEST_CASE;
    }

    @Override
    public IType getCreatedType() {
        return maybeCreatedType != null ? maybeCreatedType : super.getCreatedType();
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
        if (GROOVY_TEST_CASE.equals(superClassName)) {
            return new JUnitStatus();
        }

        return super.superClassChanged();
    }

    /**
     * Groovy classes do not need public/private/protected modifiers
     */
    @Override
    public int getModifiers() {
        int modifiers = super.getModifiers();
        modifiers &= ~F_PUBLIC;
        modifiers &= ~F_PRIVATE;
        modifiers &= ~F_PROTECTED;
        return modifiers;
    }

    @Override
    protected void updateStatus(IStatus status) {
        // bug GRECLIPSE-728
        if (!hasGroovyNature()) {
            super.updateStatus(new Status(IStatus.ERROR, GroovyPlugin.PLUGIN_ID, GROOVY_NATURE_ERROR_MSG));
            return;
        }
        super.updateStatus(status);
    }

    @Override
    public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {

        // bug GRECLIPSE-728
        if (!hasGroovyNature()) {
            GroovyCore.logWarning(GROOVY_NATURE_ERROR_MSG);
            return;
        }

        // bug GRECLIPSE-322
        // if JUnit 3 and default package, calling super will be an error.
        IPackageFragment pack = getPackageFragment();
        if (pack == null) {
            pack = getPackageFragmentRoot().getPackageFragment("");
        }
        if (!isJUnit4() && getPackageFragment().getElementName().equals("")) {
            createTypeInDefaultPackageJUnit3(pack, monitor);
            //            super.createType(monitor);
        } else {
            super.createType(monitor);
        }
    }

    // this will not handle Enclosing types
    private void createTypeInDefaultPackageJUnit3(
            IPackageFragment pack, IProgressMonitor monitor) throws JavaModelException {

        StringBuffer sb = new StringBuffer();
        String superClass = getSuperClass();
        String typeName = getTypeName();
        String[] splits = superClass.split("\\.");
        if (superClass != null && !superClass.equals(GROOVY_TEST_CASE)) {
            if (splits.length > 1) {
                sb.append("import " + superClass + "\n\n");
            }

            sb.append("class ").append(typeName)
            .append(" extends ")
            .append(splits[splits.length-1]);
        } else {
            sb.append("class ").append(typeName)
            .append(" extends ")
            .append(splits[splits.length-1]);
        }

        sb.append(" {\n\n");
        sb.append("}");

        ICompilationUnit unit = pack.createCompilationUnit(typeName + DOT_GROOVY, sb.toString(), true, monitor);
        maybeCreatedType = unit.getType(typeName);
    }
}