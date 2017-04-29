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
package org.codehaus.groovy.eclipse.test.wizards

import static org.codehaus.jdt.groovy.model.GroovyNature.GROOVY_NATURE
import static org.junit.Assert.assertEquals

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.wizards.NewClassWizardPage
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType
import org.eclipse.jdt.ui.PreferenceConstants
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

// Original source code:
// http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.jdt.ui.tests/ui/org/eclipse/jdt/ui/tests/wizardapi/NewTypeWizardTest.java?revision=1.8
final class NewGroovyTypeWizardTests extends GroovyEclipseTestSuite {

    // FIXKDV: the wizard has some options/controls that probably shouldn't be there
    //  For example a button to make a class public or default
    //  Other such things to clean up?

    @Before
    void setUp() {
        setJavaPreference(PreferenceConstants.CODEGEN_ADD_COMMENTS, 'false')
        setJavaPreference(PreferenceConstants.CODEGEN_USE_OVERRIDE_ANNOTATION, 'true')
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, '4')
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE)

        String newFileTemplate = '${filecomment}\n${package_declaration}\n\n${typecomment}\n${type_declaration}'
        StubUtility.setCodeTemplate(CodeTemplateContextType.NEWTYPE_ID, newFileTemplate, null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, '/**\n * Type\n */', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.FILECOMMENT_ID, '/**\n * File\n */', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORCOMMENT_ID, '/**\n * Constructor\n */', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.METHODCOMMENT_ID, '/**\n * Method\n */', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.OVERRIDECOMMENT_ID, '/**\n * Overridden\n */', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, '${body_statement}', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORSTUB_ID, '${body_statement}', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.CLASSBODY_ID, '/* class body */\n', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.INTERFACEBODY_ID, '/* interface body */\n', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.ENUMBODY_ID, '/* enum body */\n', null)
        StubUtility.setCodeTemplate(CodeTemplateContextType.ANNOTATIONBODY_ID, '/* annotation body */\n', null)
    }

    /**
     * Helper method to compare two strings for equality, while avoiding newline issues on different platforms.
     */
    private void assertEqualLines(String expected, String actual) {
        assertEquals(expected.replace('\n', System.getProperty('line.separator')), actual)
    }

    /** Helper method to check an IStatus */
    protected void assertStatus(int severity, String msgFragment, IStatus status) {
        assert status.getSeverity() == severity
        assert status.getMessage().contains(msgFragment) : 'Unexpected message: ' + status.getMessage()
    }

    @Test
    void testNotGroovyProject() {
        removeNature(GROOVY_NATURE)
        NewClassWizardPage wizardPage = new NewClassWizardPage()
        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
        assertStatus(IStatus.WARNING, 'is not a groovy project.  Groovy Nature will be added to project upon completion.', wizardPage.status)
    }

    @Test
    void testExclusionFilters() {
        IPackageFragmentRoot root = addSourceFolder('other', new Path('**/*.groovy'))
        IPackageFragment frag = root.createPackageFragment('p', true, null)
        NewClassWizardPage wizardPage = new NewClassWizardPage()
        wizardPage.setPackageFragmentRoot(root, true)
        wizardPage.setPackageFragment(frag, true)
        wizardPage.setTypeName('Nuthin', true)
        assertStatus(IStatus.ERROR, 'Cannot create Groovy type because of exclusion patterns on the source folder.', wizardPage.status)
    }

    @Test
    void testDiscouraedDefaultPackage() {
        removeNature(GROOVY_NATURE)
        NewClassWizardPage wizardPage = new NewClassWizardPage()
        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
        assertStatus(IStatus.WARNING, 'The use of the default package is discouraged.', wizardPage.status)
    }

    @Test
    void testCreateGroovyClass1() {
        NewClassWizardPage wizardPage = new NewClassWizardPage()
        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
        wizardPage.setEnclosingTypeSelection(false, true)
        wizardPage.setTypeName('E', true)
        wizardPage.setSuperClass('', true)
        wizardPage.setSuperInterfaces(Collections.EMPTY_LIST, true)
        wizardPage.setMethodStubSelection(false, false, false, true)
        wizardPage.setAddComments(true, true)
        wizardPage.enableCommentControl(true)

        wizardPage.createType(new NullProgressMonitor())

        String expected = '''\
            |/**
            | * File
            | */
            |package test1
            |
            |/**
            | * Type
            | */
            |class E {
            |    /* class body */
            |}
            |'''.stripMargin()

        assertEqualLines(expected, wizardPage.createdType.compilationUnit.source)
    }

    @Test @Ignore('this test fails/crashes in Groovy. cause: problems resolving generic types?')
    void testCreateGroovyClass2GenericSuper() {
        NewClassWizardPage wizardPage = new NewClassWizardPage()
        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
        wizardPage.setEnclosingTypeSelection(false, true)
        wizardPage.setTypeName('E', true)
        wizardPage.setSuperClass('java.util.ArrayList<String>', true)
        wizardPage.setSuperInterfaces(Collections.EMPTY_LIST, true)
        wizardPage.setMethodStubSelection(false, false, false, true)
        wizardPage.setAddComments(true, true)
        wizardPage.enableCommentControl(true)

        wizardPage.createType(null)

        String expected = '''\
            |/**
            | * File
            | */
            |package test1;
            |
            |import java.util.ArrayList;
            |
            |/**
            | * Type
            | */
            |class E extends ArrayList<String> {
            |    /* class body */
            |}
            |'''.stripMargin()

        assertEqualLines(expected, wizardPage.createdType.compilationUnit.source)
    }

    @Test
    void testCreateGroovyClass2() {
        NewClassWizardPage wizardPage = new NewClassWizardPage()
        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
        wizardPage.setEnclosingTypeSelection(false, true)
        wizardPage.setTypeName('E', true)
        wizardPage.setSuperClass('ArrayList', true)
        wizardPage.setSuperInterfaces(Collections.EMPTY_LIST, true)
        wizardPage.setMethodStubSelection(false, false, false, true)
        wizardPage.setAddComments(true, true)
        wizardPage.enableCommentControl(true)

        wizardPage.createType(new NullProgressMonitor())

        String expected = '''\
            |/**
            | * File
            | */
            |package test1
            |
            |import java.util.ArrayList
            |
            |/**
            | * Type
            | */
            |class E extends ArrayList {
            |    /* class body */
            |}
            |'''.stripMargin()

        assertEqualLines(expected, wizardPage.createdType.compilationUnit.source)
    }
}
