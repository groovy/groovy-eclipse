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

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.test.DebugUtils;
import org.codehaus.groovy.eclipse.refactoring.test.ParticipantTesting;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTest;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaElementMapper;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenamingNameSuggestor;
import org.eclipse.jdt.internal.corext.refactoring.tagging.INameUpdating;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenameTypeTests extends RefactoringTest {

    private static final Class clazz= RenameTypeTests.class;
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

    private IType getClassFromTestFile(IPackageFragment pack, String className) throws Exception{
        return getType(createCUfromTestFile(pack, className), className);
    }

    private RenameJavaElementDescriptor createRefactoringDescriptor(IType type, String newName) {
        RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_TYPE);
        descriptor.setJavaElement(type);
        descriptor.setNewName(newName);
        descriptor.setUpdateReferences(true);
        return descriptor;
    }

    private void helper1_0(String className, String newName) throws Exception {
        IType classA= getClassFromTestFile(getPackageP(), className);
        RefactoringStatus result= performRefactoring(createRefactoringDescriptor(classA, newName));
        assertNotNull("precondition was supposed to fail", result);
        DebugUtils.dump("result: " + result);
    }

    private void helper1() throws Exception{
        helper1_0("A", "B");
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

        INameUpdating nameUpdating= ((INameUpdating)refactoring.getAdapter(INameUpdating.class));
        IType newElement = (IType) nameUpdating.getNewElement();
        assertTrue("new element does not exist:\n" + newElement.toString(), newElement.exists());

        checkMappers(refactoring, classA, newCUName + ".groovy", classAMembers);

        return renameHandles;
    }

    private String[] helper2_0(String oldName, String newName, String newCUName, boolean updateReferences) throws Exception{
        return helperWithTextual(oldName, oldName, newName, newCUName, updateReferences, false);
    }

    private void helper2(String oldName, String newName, boolean updateReferences) throws Exception{
        helper2_0(oldName, newName, newName, updateReferences);
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

    private void helper3(String oldName, String newName, boolean updateRef, boolean updateTextual, boolean updateSimilar) throws JavaModelException, CoreException, IOException, Exception {
        helper3(oldName, newName, updateRef, updateTextual, updateSimilar, null);
    }

    private void helper3(String oldName, String newName, boolean updateRef, boolean updateTextual, boolean updateSimilar, String nonJavaFiles) throws JavaModelException, CoreException, IOException, Exception {
        RefactoringDescriptor descriptor= initWithAllOptions(oldName, oldName, newName, updateRef, updateTextual, updateSimilar, nonJavaFiles, RenamingNameSuggestor.STRATEGY_EMBEDDED);
        Refactoring ref= createRefactoring(descriptor);
        RefactoringStatus status= performRefactoring(ref, false);
        assertNull("was supposed to pass", status);
        checkResultInClass(newName);
        checkMappedSimilarElementsExist(ref);
    }

    private void helper3_inner(String oldName, String oldInnerName, String newName, String innerNewName, boolean updateRef, boolean updateTextual, boolean updateSimilar, String nonJavaFiles) throws JavaModelException, CoreException, IOException, Exception {
        RefactoringDescriptor descriptor= initWithAllOptions(oldName, oldInnerName, innerNewName, updateRef, updateTextual, updateSimilar, nonJavaFiles, RenamingNameSuggestor.STRATEGY_EMBEDDED);
        Refactoring ref= createRefactoring(descriptor);
        assertNull("was supposed to pass", performRefactoring(ref, false));
        checkResultInClass(newName);
        checkMappedSimilarElementsExist(ref);
    }

    private void checkMappedSimilarElementsExist(Refactoring ref) {
        RenameTypeProcessor rtp= (RenameTypeProcessor) ((RenameRefactoring) ref).getProcessor();
        IJavaElementMapper mapper= (IJavaElementMapper) rtp.getAdapter(IJavaElementMapper.class);
        IJavaElement[] similarElements= rtp.getSimilarElements();
        if (similarElements == null)
            return;
        for (int i= 0; i < similarElements.length; i++) {
            IJavaElement element= similarElements[i];
            if (! (element instanceof ILocalVariable)) {
                IJavaElement newElement= mapper.getRefactoredJavaElement(element);
                assertTrue(newElement.exists());
                assertFalse(element.exists());
            }
        }

    }

    private void helper3_fail(String oldName, String newName, boolean updateSimilar, boolean updateTextual, boolean updateRef, int matchStrategy) throws JavaModelException, CoreException, IOException, Exception {
        RefactoringDescriptor descriptor= initWithAllOptions(oldName, oldName, newName, updateRef, updateTextual, updateSimilar, null, matchStrategy);
        assertNotNull("was supposed to fail", performRefactoring(descriptor));
    }

    private void helper3_fail(String oldName, String newName, boolean updateSimilar, boolean updateTextual, boolean updateRef) throws JavaModelException, CoreException, IOException, Exception {
        RefactoringDescriptor descriptor= initWithAllOptions(oldName, oldName, newName, updateRef, updateTextual, updateSimilar, null, RenamingNameSuggestor.STRATEGY_SUFFIX);
        assertNotNull("was supposed to fail", performRefactoring(descriptor));
    }

    private RefactoringDescriptor initWithAllOptions(String oldName, String innerOldName, String innerNewName, boolean updateReferences, boolean updateTextualMatches, boolean updateSimilar, String nonJavaFiles, int matchStrategy) throws Exception, JavaModelException, CoreException {
        ICompilationUnit cu= createCUfromTestFile(getPackageP(), oldName);
        IType classA= getType(cu, innerOldName);
        RenameJavaElementDescriptor descriptor= createRefactoringDescriptor(classA, innerNewName);
        setTheOptions(descriptor, updateReferences, updateTextualMatches, updateSimilar, nonJavaFiles, matchStrategy);
        return descriptor;
    }

    private void setTheOptions(RenameJavaElementDescriptor descriptor, boolean updateReferences, boolean updateTextualMatches, boolean updateSimilar, String nonJavaFiles, int matchStrategy) {
        descriptor.setUpdateReferences(updateReferences);
        descriptor.setUpdateTextualOccurrences(updateTextualMatches);
        if (nonJavaFiles!=null) {
            descriptor.setUpdateQualifiedNames(true);
            descriptor.setFileNamePatterns(nonJavaFiles);
        }
        descriptor.setUpdateSimilarDeclarations(updateSimilar);
        descriptor.setMatchStrategy(matchStrategy);
    }

    private void checkResultInClass(String typeName) throws JavaModelException, IOException {
        ICompilationUnit newcu= getPackageP().getCompilationUnit(typeName + ".groovy");
        assertTrue("cu " + newcu.getElementName()+ " does not exist", newcu.exists());
        assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName(typeName)), newcu.getSource());
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
}