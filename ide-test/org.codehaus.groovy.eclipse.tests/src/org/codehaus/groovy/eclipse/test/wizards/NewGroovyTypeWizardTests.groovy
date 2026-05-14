/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.wizards

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.wizards.NewTypeWizard
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants
import org.eclipse.jdt.ui.PreferenceConstants
import org.junit.Before
import org.junit.Test

final class NewGroovyTypeWizardTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        setJavaPreference(PreferenceConstants.CODEGEN_ADD_COMMENTS, false)
        setJavaPreference(PreferenceConstants.CODEGEN_USE_OVERRIDE_ANNOTATION, true)
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB)

        Class stubUtility, codeTemplateContextType
        try {
            stubUtility = Class.forName('org.eclipse.jdt.internal.core.manipulation.StubUtility')
            codeTemplateContextType = Class.forName('org.eclipse.jdt.internal.core.manipulation.CodeTemplateContextType')
        } catch (ClassNotFoundException e) {
            stubUtility = Class.forName('org.eclipse.jdt.internal.corext.codemanipulation.StubUtility')
            codeTemplateContextType = Class.forName('org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType')
        }
        stubUtility.setCodeTemplate(codeTemplateContextType.NEWTYPE_ID, '${filecomment}\n${package_declaration}\n\n${typecomment}\n${type_declaration}', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.FILECOMMENT_ID, '/**\n * File\n */', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.TYPECOMMENT_ID, '/**\n * Type\n */', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.CONSTRUCTORCOMMENT_ID, '/**\n * Constructor\n */', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.METHODCOMMENT_ID, '/**\n * Method\n */', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.OVERRIDECOMMENT_ID, '/**\n * Overridden\n */', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.CLASSBODY_ID, '/* class body */\n', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.INTERFACEBODY_ID, '/* interface body */\n', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.ENUMBODY_ID, '/* enum body */\n', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.ANNOTATIONBODY_ID, '/* annotation body */\n', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.CONSTRUCTORSTUB_ID, '${body_statement}', null)
        stubUtility.setCodeTemplate(codeTemplateContextType.METHODSTUB_ID, '${body_statement}', null)
    }

    @Test
    void testSomething() {
        new NewTypeWizard().with {
            // TODO
        }
    }

//    private NewTypeWizard clearModifiers(NewTypeWizard wizard) {
//        SelectionButtonDialogFieldGroup group = ReflectionUtils.getPrivateField(NewTypeWizard, 'fAccMdfButtons', wizard)
//        for (i in 0..5) {
//            group.setSelection(i, false)
//        }
//        wizard
//    }
//
//    private void assertStatus(int severity, String msgFragment, NewTypeWizard wizard) {
//        IStatus status = ReflectionUtils.getPrivateField(NewTypeWizard, 'fCurrStatus', wizard)
//        assert status.severity == severity
//        assert status.message.contains(msgFragment) : 'Unexpected message: ' + status.message
//    }
//
//    @Test
//    void testNotGroovyProject() {
//        removeNature(GROOVY_NATURE)
//        NewClassWizardPage wizardPage = new NewClassWizardPage()
//        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
//        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
//        assertStatus(IStatus.WARNING, 'is not a groovy project.  Groovy Nature will be added to project upon completion.', wizardPage)
//    }
//
//    @Test
//    void testExclusionFilters() {
//        IPackageFragmentRoot root = addSourceFolder('other', new Path('**/*.groovy'))
//        IPackageFragment frag = root.createPackageFragment('p', true, null)
//        NewClassWizardPage wizardPage = new NewClassWizardPage()
//        wizardPage.setPackageFragmentRoot(root, true)
//        wizardPage.setPackageFragment(frag, true)
//        wizardPage.setTypeName('Nuthin', true)
//        assertStatus(IStatus.ERROR, 'Cannot create Groovy type because of exclusion patterns on the source folder.', wizardPage)
//    }
//
//    @Test
//    void testDiscouraedDefaultPackage() {
//        removeNature(GROOVY_NATURE)
//        NewClassWizardPage wizardPage = new NewClassWizardPage()
//        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
//        assertStatus(IStatus.WARNING, 'The use of the default package is discouraged.', wizardPage)
//    }
//
//    @Test
//    void testCreateGroovyClass1() {
//        NewClassWizardPage wizardPage = new NewClassWizardPage()
//        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
//        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
//        wizardPage.setEnclosingTypeSelection(false, true)
//        wizardPage.setTypeName('E', true)
//        wizardPage.setSuperClass('', true)
//        wizardPage.setSuperInterfaces(Collections.EMPTY_LIST, true)
//        wizardPage.setMethodStubSelection(false, false, false, true)
//        wizardPage.setAddComments(true, true)
//        wizardPage.enableCommentControl(true)
//
//        wizardPage.createType(new NullProgressMonitor())
//
//        String expected = '''\
//            |/**
//            | * File
//            | */
//            |package test1
//            |
//            |/**
//            | * Type
//            | */
//            |class E {
//            |    /* class body */
//            |}
//            |'''.stripMargin()
//
//        assertEquals(expected, wizardPage.createdType.compilationUnit.source)
//    }
//
//    @Test
//    void testCreateGroovyClass2() {
//        NewClassWizardPage wizardPage = new NewClassWizardPage()
//        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
//        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
//        wizardPage.setEnclosingTypeSelection(false, true)
//        wizardPage.setTypeName('E', true)
//        wizardPage.setSuperClass('ArrayList', true)
//        wizardPage.setSuperInterfaces(Collections.EMPTY_LIST, true)
//        wizardPage.setMethodStubSelection(false, false, false, true)
//        wizardPage.setAddComments(true, true)
//        wizardPage.enableCommentControl(true)
//
//        wizardPage.createType(new NullProgressMonitor())
//
//        String expected = '''\
//            |/**
//            | * File
//            | */
//            |package test1
//            |
//            |/**
//            | * Type
//            | */
//            |class E extends ArrayList {
//            |    /* class body */
//            |}
//            |'''.stripMargin()
//
//        assertEquals(expected, wizardPage.createdType.compilationUnit.source)
//    }
//
//    @Test
//    void testCreateGroovyClass3() {
//        NewClassWizardPage wizardPage = new NewClassWizardPage()
//        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
//        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
//        wizardPage.setEnclosingTypeSelection(false, true)
//        wizardPage.setTypeName('E', true)
//        wizardPage.setSuperClass('java.util.ArrayList<String>', true)
//        wizardPage.setSuperInterfaces(Collections.EMPTY_LIST, true)
//        wizardPage.setMethodStubSelection(false, false, false, true)
//        wizardPage.setAddComments(true, true)
//        wizardPage.enableCommentControl(true)
//
//        wizardPage.createType(new NullProgressMonitor())
//
//        String expected = '''\
//            |/**
//            | * File
//            | */
//            |package test1
//            |
//            |/**
//            | * Type
//            | */
//            |class E extends ArrayList<String> {
//            |    /* class body */
//            |}
//            |'''.stripMargin()
//
//        assertEquals(expected, wizardPage.createdType.compilationUnit.source)
//    }
//
//    @Test
//    void testCreateGroovyClass4() {
//        NewClassWizardPage wizardPage = clearModifiers(new NewClassWizardPage())
//        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
//        wizardPage.setPackageFragment(getPackageFragment('test1'), true)
//        wizardPage.setEnclosingTypeSelection(false, true)
//        wizardPage.setTypeName('Foo', true)
//        wizardPage.setSuperClass('', true)
//        wizardPage.setSuperInterfaces(Collections.EMPTY_LIST, true)
//        wizardPage.setMethodStubSelection(false, false, false, true)
//        wizardPage.setModifiers(wizardPage.F_FINAL, true)
//        wizardPage.setAddComments(true, true)
//        wizardPage.enableCommentControl(true)
//
//        wizardPage.createType(new NullProgressMonitor())
//
//        String expected = '''\
//            |/**
//            | * File
//            | */
//            |package test1
//            |
//            |import groovy.transform.PackageScope
//            |
//            |/**
//            | * Type
//            | */
//            |@PackageScope final class Foo {
//            |    /* class body */
//            |}
//            |'''.stripMargin()
//
//        assertEquals(expected, wizardPage.createdType.compilationUnit.source)
//    }
}
