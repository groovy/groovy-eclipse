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
package org.codehaus.groovy.eclipse.refactoring.test.rename

import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.codehaus.groovy.eclipse.refactoring.test.internal.ParticipantTesting
import org.eclipse.core.resources.IFile
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.refactoring.IJavaRefactorings
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor
import org.eclipse.jdt.internal.corext.refactoring.tagging.INameUpdating
import org.eclipse.ltk.core.refactoring.Refactoring
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring
import org.junit.Before
import org.junit.Test

final class RenameTypeTests extends RefactoringTestSuite {

    @Override
    protected String getRefactoringPath() {
        'RenameType/'
    }

    @Before
    void setUp() {
        setSomeFieldOptions(project, 'f', 'Suf1', false)
        setSomeFieldOptions(project, 'fs', '_suffix', true)
        setSomeLocalOptions(project, 'lv', '_lv')
        setSomeArgumentOptions(project, 'pm', '_pm')
    }

    private static void setSomeFieldOptions(IJavaProject project, String prefixes, String suffixes, boolean forStatic) {
        if (forStatic) {
            project.setOption(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, prefixes)
            project.setOption(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, suffixes)
        } else {
            project.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, prefixes)
            project.setOption(JavaCore.CODEASSIST_FIELD_SUFFIXES, suffixes)
        }
    }

    private static void setSomeLocalOptions(IJavaProject project, String prefixes, String suffixes) {
        project.setOption(JavaCore.CODEASSIST_LOCAL_PREFIXES, prefixes)
        project.setOption(JavaCore.CODEASSIST_LOCAL_SUFFIXES, suffixes)
    }

    private static void setSomeArgumentOptions(IJavaProject project, String prefixes, String suffixes) {
        project.setOption(JavaCore.CODEASSIST_ARGUMENT_PREFIXES, prefixes)
        project.setOption(JavaCore.CODEASSIST_ARGUMENT_SUFFIXES, suffixes)
    }

    private RenameJavaElementDescriptor createRefactoringDescriptor(IType type, String newName) {
        RenameJavaElementDescriptor descriptor =
            RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_TYPE)
        descriptor.javaElement = type
        descriptor.newName = newName
        descriptor.updateReferences = true
        return descriptor
    }

    private String[] helperWithTextual(String oldCuName, String oldName, String newName, String newCUName, boolean updateReferences, boolean updateTextualMatches) {
        ICompilationUnit cu = createCUfromTestFile(packageP, oldCuName)
        IType classA = getType(cu, oldName)
        IJavaElement[] classAMembers = classA.children
        IPackageFragment pack = (IPackageFragment) cu.parent
        String[] renameHandles = null
        if (classA.declaringType == null && cu.elementName.startsWith(classA.elementName)) {
            renameHandles = ParticipantTesting.createHandles(classA, cu, cu.resource)
        } else {
            renameHandles = ParticipantTesting.createHandles(classA)
        }
        RenameJavaElementDescriptor descriptor = createRefactoringDescriptor(classA, newName)
        descriptor.setUpdateReferences(updateReferences)
        descriptor.setUpdateTextualOccurrences(updateTextualMatches)
        Refactoring refactoring = createRefactoring(descriptor)
        RefactoringStatus result = performRefactoring(refactoring, false)
        assert result == null || result.isOK() : 'was supposed to pass'
        ICompilationUnit newcu = pack.getCompilationUnit(newCUName + '.groovy')
        assert newcu.exists() : "cu $newcu.elementName does not exist"
        assertEqualLines('invalid renaming', getFileContents(getOutputTestFileName(newCUName)), newcu.source)
        INameUpdating nameUpdating = refactoring.getAdapter(INameUpdating)
        IType newElement = (IType) nameUpdating.newElement
        assert newElement.exists() : 'new element does not exist:\n' + newElement.toString()
        checkMappers(refactoring, classA, newCUName + '.groovy', classAMembers)
        return renameHandles
    }

    private String[] helper2_0(String oldName, String newName, String newCUName, boolean updateReferences) {
        return helperWithTextual(oldName, oldName, newName, newCUName, updateReferences, false)
    }

    private String[] helper2(String oldName, String newName) {
        return helper2_0(oldName, newName, newName, true)
    }

    private void checkMappers(Refactoring refactoring, IType type, String newCUName, IJavaElement[] someClassMembers) {
        RenameTypeProcessor rtp = (RenameTypeProcessor) ((RenameRefactoring) refactoring).processor

        ICompilationUnit newUnit = rtp.getRefactoredJavaElement(type.compilationUnit)
        assert newUnit.exists()
        assert newUnit.getElementName() == newCUName

        IFile newFile = rtp.getRefactoredResource(type.resource)
        assert newFile.exists()
        assert newFile.name == newCUName

        if ((type.parent.elementType == IJavaElement.COMPILATION_UNIT) &&
                type.compilationUnit.elementName.equals(type.elementName + '.groovy')) {
            assert !type.getCompilationUnit().exists()
            assert !type.getResource().exists()
        }

        IPackageFragment oldPackage = type.compilationUnit.parent
        IPackageFragment newPackage = rtp.getRefactoredJavaElement(oldPackage)
        assert newPackage == oldPackage

        for (member in someClassMembers) {
            IJavaElement refactoredMember = rtp.getRefactoredJavaElement(member)
            if (member instanceof IMethod && member.elementName == type.elementName)
                continue // constructor
            assert refactoredMember.exists()
            assert refactoredMember.elementName == member.elementName
            assert !refactoredMember.equals(member)
        }
    }

    // NOTE: Test method names are matched to test case data stored externally

    @Test // Rename paramter type
    void test1() {
        helper2('A', 'B')
    }

    @Test // Rename super type
    void test2() {
        helper2('A', 'B')
    }

    @Test // Rename interface type
    void test3() {
        helper2('A', 'B')
    }

    @Test // Rename return type
    void test4() {
        helper2('A', 'B')
    }

    @Test // Rename variable type in method
    void test5() {
        helper2('A', 'B')
    }

    @Test // Rename field type
    void test6() {
        helper2('A', 'B')
    }

    @Test // Rename variable type in closure
    void test7() {
        helper2('A', 'B')
    }

    @Test // Rename parameter type in closure
    void test8() {
        helper2('A', 'B')
    }

    @Test // Rename variable type in closure assigned to field
    void test9() {
        helper2('A', 'B')
    }

    @Test // Rename parameter type in closure assigned to field
    void test10() {
        helper2('A', 'B')
    }

    @Test // Rename type literal static context
    void test11() {
        helper2('A', 'B')
    }

    @Test // Rename type literal non-static context
    void test12() {
        helper2('A', 'B')
    }

    @Test // Rename type and constructors
    void test13() {
        helper2('A', 'B')
    }

    @Test // some funky things with annotations
    void testAnnotation1() {
        helper2('A', 'B')
    }

    @Test
    void testAnnotation2() {
        helper2('A', 'B')
    }

    @Test
    void testAnnotation3() {
        helper2('A', 'B')
    }

    @Test
    void testAlias1() {
        IPackageFragment p2 = root.createPackageFragment('p2', true, null)
        String folder = 'p2/'
        String type = 'A'
        ICompilationUnit cu = createCUfromTestFile(p2, type, folder)

        helper2('A', 'B')

        assertEqualLines('invalid renaming in p2.A', getFileContents(getOutputTestFileName(type, folder)), cu.source)
    }

    @Test
    void testEnum1() {
        IPackageFragment p2 = root.createPackageFragment('p2', true, null)
        String folder = 'p2/'
        String type = 'A'
        ICompilationUnit cu = createCUfromTestFile(p2, type, folder)

        helper2('A', 'B')

        assertEqualLines('invalid renaming in p2.A', getFileContents(getOutputTestFileName(type, folder)), cu.source)
    }

    @Test
    void testEnum2() {
        helper2('A', 'B')
    }

    @Test
    void testGenerics1() {
        helper2('A', 'B')
    }

    @Test
    void testGenerics2() {
        helper2('A', 'B')
    }

    @Test
    void testGenerics3() {
        helper2('A', 'B')
    }

    @Test
    void testGenerics4() {
        helper2('A', 'B')
    }

    @Test
    void testInner1() {
        // rename inner class
        helperWithTextual('Script', 'A', 'B', 'Script', true, false)
    }

    @Test
    void testInner2() {
        // rename outer class
        helperWithTextual('Script', 'Outer', 'Wrapper', 'Script', true, false)
    }

    @Test
    void testInner3() {
        // rename outer class with external references
        ICompilationUnit unit = createCUfromTestFile(packageP, 'Script')
        helperWithTextual('Outer', 'Outer', 'Wrapper', 'Wrapper', true, false)
        assert unit.exists() : "CompilationUnit ${unit.elementName} does not exist"
        assertEqualLines(getFileContents(getOutputTestFileName('Script')), unit.source)
    }

    @Test
    void testJavadoc1() {
        helperWithTextual('A', 'A', 'B', 'B', true, true)
    }
}
