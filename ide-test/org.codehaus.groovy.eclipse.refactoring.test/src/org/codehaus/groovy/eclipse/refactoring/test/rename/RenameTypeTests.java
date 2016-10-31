/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.test.rename;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.test.ParticipantTesting;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTest;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.jdt.internal.corext.refactoring.tagging.INameUpdating;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenameTypeTests extends RefactoringTest {

    private static final Class<RenameTypeTests> clazz= RenameTypeTests.class;
    private static final String REFACTORING_PATH= "RenameType/";

    public RenameTypeTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new RefactoringTestSetup(new TestSuite(clazz));
    }

    public static Test setUpTest(Test someTest) {
        return new RefactoringTestSetup(someTest);
    }

    protected String getRefactoringPath() {
        return REFACTORING_PATH;
    }

    private RenameJavaElementDescriptor createRefactoringDescriptor(IType type, String newName) {
        RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_TYPE);
        descriptor.setJavaElement(type);
        descriptor.setNewName(newName);
        descriptor.setUpdateReferences(true);
        return descriptor;
    }

    private String[] helperWithTextual(String oldCuName, String oldName, String newName, String newCUName, boolean updateReferences, boolean updateTextualMatches) throws Exception{
        ICompilationUnit cu= createCUfromTestFile(getPackageP(), oldCuName);
        IType classA= getType(cu, oldName);
        IJavaElement[] classAMembers= classA.getChildren();

        IPackageFragment pack= (IPackageFragment)cu.getParent();
        String[] renameHandles= null;
        if (classA.getDeclaringType() == null && cu.getElementName().startsWith(classA.getElementName())) {
            renameHandles= ParticipantTesting.createHandles(classA, cu, cu.getResource());
        } else {
            renameHandles= ParticipantTesting.createHandles(classA);
        }
        RenameJavaElementDescriptor descriptor= createRefactoringDescriptor(classA, newName);
        descriptor.setUpdateReferences(updateReferences);
        descriptor.setUpdateTextualOccurrences(updateTextualMatches);
        Refactoring refactoring= createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, false);
        assertTrue("was supposed to pass", result == null || result.isOK());
        ICompilationUnit newcu= pack.getCompilationUnit(newCUName + ".groovy");
        assertTrue("cu " + newcu.getElementName()+ " does not exist", newcu.exists());
        assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName(newCUName)), newcu.getSource());

        INameUpdating nameUpdating= (refactoring.getAdapter(INameUpdating.class));
        IType newElement = (IType) nameUpdating.getNewElement();
        assertTrue("new element does not exist:\n" + newElement.toString(), newElement.exists());

        checkMappers(refactoring, classA, newCUName + ".groovy", classAMembers);

        return renameHandles;
    }

    private String[] helper2_0(String oldName, String newName, String newCUName, boolean updateReferences) throws Exception{
        return helperWithTextual(oldName, oldName, newName, newCUName, updateReferences, false);
    }

    private String[] helper2(String oldName, String newName) throws Exception{
        return helper2_0(oldName, newName, newName, true);
    }

    // <--------------------- Similarly named elements ---------------------------->

    protected void setUp() throws Exception {
        super.setUp();
        setSomeFieldOptions(getPackageP().getJavaProject(), "f", "Suf1", false);
        setSomeFieldOptions(getPackageP().getJavaProject(), "fs", "_suffix", true);
        setSomeLocalOptions(getPackageP().getJavaProject(), "lv", "_lv");
        setSomeArgumentOptions(getPackageP().getJavaProject(), "pm", "_pm");
        fIsPreDeltaTest= true;
    }

    private void setSomeFieldOptions(IJavaProject project, String prefixes, String suffixes, boolean forStatic) {
        if (forStatic) {
            project.setOption(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, prefixes);
            project.setOption(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, suffixes);
        }
        else {
            project.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, prefixes);
            project.setOption(JavaCore.CODEASSIST_FIELD_SUFFIXES, suffixes);
        }
    }

    private void setSomeLocalOptions(IJavaProject project, String prefixes, String suffixes) {
            project.setOption(JavaCore.CODEASSIST_LOCAL_PREFIXES, prefixes);
            project.setOption(JavaCore.CODEASSIST_LOCAL_SUFFIXES, suffixes);
    }

    private void setSomeArgumentOptions(IJavaProject project, String prefixes, String suffixes) {
        project.setOption(JavaCore.CODEASSIST_ARGUMENT_PREFIXES, prefixes);
        project.setOption(JavaCore.CODEASSIST_ARGUMENT_SUFFIXES, suffixes);
    }

    private void checkMappers(Refactoring refactoring, IType type, String newCUName, IJavaElement[] someClassMembers) {
        RenameTypeProcessor rtp= (RenameTypeProcessor)((RenameRefactoring) refactoring).getProcessor();

        ICompilationUnit newUnit= (ICompilationUnit)rtp.getRefactoredJavaElement(type.getCompilationUnit());
        assertTrue(newUnit.exists());
        assertTrue(newUnit.getElementName().equals(newCUName));

        IFile newFile= (IFile)rtp.getRefactoredResource(type.getResource());
        assertTrue(newFile.exists());
        assertTrue(newFile.getName().equals(newCUName));

        if ((type.getParent().getElementType() == IJavaElement.COMPILATION_UNIT)
                && type.getCompilationUnit().getElementName().equals(type.getElementName() + ".groovy")) {
            assertFalse(type.getCompilationUnit().exists());
            assertFalse(type.getResource().exists());
        }

        IPackageFragment oldPackage= (IPackageFragment)type.getCompilationUnit().getParent();
        IPackageFragment newPackage= (IPackageFragment)rtp.getRefactoredJavaElement(oldPackage);
        assertEquals(oldPackage, newPackage);

        for (int i= 0; i < someClassMembers.length; i++) {
            IMember member= (IMember) someClassMembers[i];
            IJavaElement refactoredMember= rtp.getRefactoredJavaElement(member);
            if (member instanceof IMethod && member.getElementName().equals(type.getElementName()))
                continue; // constructor
            assertTrue(refactoredMember.exists());
            assertEquals(member.getElementName(), refactoredMember.getElementName());
            assertFalse(refactoredMember.equals(member));
        }
    }

    /********************
     * The tests
     */

    // Rename paramter type
    public void test1() throws Exception {
        helper2("A", "B");
    }
    // Rename super type
    public void test2() throws Exception {
        helper2("A", "B");
    }
    // Rename interface type
    public void test3() throws Exception {
        helper2("A", "B");
    }
    // Rename return type
    public void test4() throws Exception {
        helper2("A", "B");
    }
    // Rename variable type in method
    public void test5() throws Exception {
        helper2("A", "B");
    }
    // Rename field type
    public void test6() throws Exception {
        helper2("A", "B");
    }
    // Rename variable type in closure
    public void test7() throws Exception {
        helper2("A", "B");
    }
    // Rename parameter type in closure
    public void test8() throws Exception {
        helper2("A", "B");
    }
    // Rename variable type in closure assigned to field
    public void test9() throws Exception {
        helper2("A", "B");
    }
    // Rename parameter type in closure assigned to field
    public void test10() throws Exception {
        helper2("A", "B");
    }
    // Rename type literal static context
    public void test11() throws Exception {
        helper2("A", "B");
    }
    // Rename type literal non-static context
    public void test12() throws Exception {
        helper2("A", "B");
    }
    // Rename type and constructors
    public void test13() throws Exception {
        helper2("A", "B");
    }

    // some funky things with annotations
    public void testAnnotation1() throws Exception {
        helper2("A", "B");
    }

    public void testAnnotation2() throws Exception {
        helper2("A", "B");
    }

    public void testAnnotation3() throws Exception {
        helper2("A", "B");
    }

    public void testAlias1() throws Exception {
        IPackageFragment p2= getRoot().createPackageFragment("p2", true, null);
        String folder= "p2/";
        String type= "A";
        ICompilationUnit cu= createCUfromTestFile(p2, type, folder);

        helper2("A", "B");

        assertEqualLines("invalid renaming in p2.A", getFileContents(getOutputTestFileName(type, folder)), cu.getSource());
    }

    public void testEnum1() throws Exception {
        IPackageFragment p2= getRoot().createPackageFragment("p2", true, null);
        String folder= "p2/";
        String type= "A";
        ICompilationUnit cu= createCUfromTestFile(p2, type, folder);

        helper2("A", "B");

        assertEqualLines("invalid renaming in p2.A", getFileContents(getOutputTestFileName(type, folder)), cu.getSource());
    }

    public void testEnum2() throws Exception {
        helper2("A", "B");
    }

    public void testGenerics1() throws Exception {
        helper2("A", "B");
    }

    public void testGenerics2() throws Exception {
        helper2("A", "B");
    }

    public void testGenerics3() throws Exception {
        helper2("A", "B");
    }

    public void testGenerics4() throws Exception {
        helper2("A", "B");
    }

    public void testInner1() throws Exception {
        helperWithTextual("Outer", "A", "B", "Outer", true, false);
    }

    public void testJavadoc1() throws Exception {
    	helperWithTextual("A", "A", "B", "B", true, true);
    }
}