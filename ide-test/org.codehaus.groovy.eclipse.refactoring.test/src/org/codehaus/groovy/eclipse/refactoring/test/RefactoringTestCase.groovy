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
package org.codehaus.groovy.eclipse.refactoring.test

import org.codehaus.groovy.eclipse.refactoring.core.rename.ForcePreviewParticipant
import org.codehaus.groovy.eclipse.refactoring.test.internal.JavaProjectHelper
import org.codehaus.groovy.eclipse.refactoring.test.internal.TestOptions
import org.codehaus.groovy.eclipse.refactoring.test.internal.TestRenameParticipantShared
import org.codehaus.groovy.eclipse.refactoring.test.internal.TestRenameParticipantSingle
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.ISourceManipulation
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.JavaModelException
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType
import org.eclipse.jdt.internal.corext.util.Strings
import org.eclipse.jdt.internal.ui.JavaPlugin
import org.eclipse.jdt.internal.ui.util.CoreUtility
import org.eclipse.ltk.core.refactoring.Change
import org.eclipse.ltk.core.refactoring.ChangeDescriptor
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation
import org.eclipse.ltk.core.refactoring.CreateChangeOperation
import org.eclipse.ltk.core.refactoring.IUndoManager
import org.eclipse.ltk.core.refactoring.PerformChangeOperation
import org.eclipse.ltk.core.refactoring.Refactoring
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor
import org.eclipse.ltk.core.refactoring.RefactoringContribution
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName
import org.osgi.framework.FrameworkUtil

abstract class RefactoringTestCase {

    private static boolean fWasAutobuild
    private static Hashtable<String, String> fWasOptions
    private static IJavaProject fgJavaTestProject
    private static IPackageFragmentRoot fgRoot
    private static IPackageFragment fgPackageP
    private static IPackageFragmentRoot[] fgJRELibraries

    @BeforeClass
    static final void setUpTestSuite() {
        fWasOptions = JavaCore.getOptions()
        fWasAutobuild = CoreUtility.setAutoBuilding(false)

        Hashtable<String, String> options = TestOptions.getDefaultOptions()
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, '4')
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB)
        options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, String.valueOf(9999))
        options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, '0')

        JavaCore.setOptions(options)
        TestOptions.initializeCodeGenerationOptions()
        JavaPlugin.getDefault().getCodeTemplateStore().load()

        fgJavaTestProject = JavaProjectHelper.createGroovyProject('TestProject', 'bin')
        fgJRELibraries = JavaProjectHelper.addRTJars(fgJavaTestProject)
        JavaProjectHelper.addGroovyJar(fgJavaTestProject)

        // just in case, remove the source root that is the root of the project (if it exists)
        JavaProjectHelper.removeFromClasspath(fgJavaTestProject, fgJavaTestProject.getProject().getFullPath())
        fgRoot = JavaProjectHelper.addSourceContainer(fgJavaTestProject, 'src')
        fgPackageP = fgRoot.createPackageFragment('p', true, null)

        StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORCOMMENT_ID, '/**\n * ${tags}\n */', null)
    }

    @AfterClass
    static final void tearDownTestSuite() {
        try {
            CoreUtility.setAutoBuilding(fWasAutobuild)
            JavaProjectHelper.delete(fgJavaTestProject)
            JavaCore.setOptions(fWasOptions)
        } finally {
            fgJavaTestProject = null
            fWasOptions = null
        }
    }

    @Rule
    public TestName test = new TestName()

    @Before
    final void setUpTestCase() {
        System.out.println('----------------------------------------')
        System.out.println('Starting: ' + test.getMethodName())

        RefactoringCore.getUndoManager().flush()
        ForcePreviewParticipant.mute()
    }

    /**
     * Removes contents of {@link #getPackageP()}, of {@link #getRoot()} (except for p) and of the
     * Java project (except for src and the JRE library).
     */
    @After
    final void tearDownTestCase() {
        try {
            if (root.exists()) {
                root.resource.refreshLocal(IResource.DEPTH_INFINITE, null)
            } else if (packageP.exists()) {
                // don't refresh package if root already refreshed
                packageP.resource.refreshLocal(IResource.DEPTH_INFINITE, null)
            }

            if (packageP.exists()) {
                tryDeletingAllJavaChildren(packageP)
                tryDeletingAllNonJavaChildResources(packageP)
            }

            if (root.exists()) {
                for (IPackageFragment pack in root.children) {
                    if (!pack.equals(packageP) && pack.exists() && !pack.isReadOnly()) {
                        if (pack.isDefaultPackage()) {
                            pack.delete(true, null)
                        } else {
                            // also delete packages with subpackages
                            JavaProjectHelper.delete(pack.resource)
                        }
                    }
                }
            }

            restoreTestProject()

        } finally {
            TestRenameParticipantShared.reset()
            TestRenameParticipantSingle.reset()
            ForcePreviewParticipant.unmute()
        }
    }

    /**
     * If <code>true</code> a descriptor is created from the change.
     * The new descriptor is then used to create the refactoring again
     * and run the refactoring. As this is very time consuming this should
     * be <code>false</code> by default.
     */
    private static final boolean DESCRIPTOR_TEST = false

    protected static final String TEST_PATH_PREFIX = ''
    protected static final String TEST_INPUT_INFIX = '/in/'
    protected static final String TEST_OUTPUT_INFIX = '/out/'

    private static void tryDeletingAllJavaChildren(IPackageFragment pack) {
        for (kid in pack.children) {
            if (kid instanceof ISourceManipulation && kid.exists() && !kid.isReadOnly()) {
                JavaProjectHelper.delete(kid)
            }
        }
    }

    private static void tryDeletingAllNonJavaChildResources(IPackageFragment pack) {
        for (njr in pack.nonJavaResources) {
            if (njr instanceof IResource) {
                JavaProjectHelper.delete(njr)
            }
        }
    }

    private void restoreTestProject() {
        if (fgJavaTestProject.exists()) {
            IClasspathEntry srcEntry = root.rawClasspathEntry
            try {
                IClasspathEntry[] jreEntries = fgJRELibraries*.rawClasspathEntry as IClasspathEntry[]
                IClasspathEntry[] cpes = fgJavaTestProject.rawClasspath
                List<IClasspathEntry> newCPEs = []
                boolean cpChanged = false
                for (int i = 0; i < cpes.length; i += 1) {
                    IClasspathEntry cpe = cpes[i]
                    boolean isJREEntry = false
                    for (int j = 0; j < jreEntries.length; j += 1) {
                        if (cpe.equals(jreEntries[j])) {
                            isJREEntry = true
                            break
                        }
                    }
                    if (cpe.equals(srcEntry) || isJREEntry) {
                        newCPEs.add(cpe)
                    } else {
                        cpChanged = true
                    }
                }
                if (cpChanged) {
                    fgJavaTestProject.setRawClasspath(newCPEs as IClasspathEntry[], null)
                }
            } catch (JavaModelException e) {
                System.err.println('Exception thrown when trying to restore project to original state.  We can probable ignore this.')
                e.printStackTrace()
            }

            for (njr in fgJavaTestProject.nonJavaResources) {
                if (njr instanceof IResource) {
                    if (!(njr.name in ['.project', '.classpath', '.settings'])) {
                        JavaProjectHelper.delete(njr)
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    /**
     * subclasses override to inform about the location of their test cases
     */
    protected abstract String getRefactoringPath()

    protected final String getTestPath() {
        TEST_PATH_PREFIX + refactoringPath
    }

    protected final IPackageFragmentRoot getRoot() {
        fgRoot
    }

    protected final IPackageFragment getPackageP() {
        fgPackageP
    }

    protected final IJavaProject getProject() {
        fgJavaTestProject
    }

    protected final RefactoringStatus performRefactoring(RefactoringDescriptor descriptor) {
        performRefactoring(descriptor, true)
    }

    protected final RefactoringStatus performRefactoring(RefactoringDescriptor descriptor, boolean providesUndo) {
        Refactoring refactoring = createRefactoring(descriptor)
        performRefactoring(refactoring, providesUndo)
    }

    protected final Refactoring createRefactoring(RefactoringDescriptor descriptor) {
        RefactoringStatus status = new RefactoringStatus()
        Refactoring refactoring = descriptor.createRefactoring(status)
        assert refactoring != null : 'refactoring should not be null'
        assert status.isOK() : 'status should be ok'
        refactoring
    }

    protected final RefactoringStatus performRefactoring(Refactoring ref, boolean performOnFail) {
        performRefactoring(ref, true, performOnFail)
    }

    protected final RefactoringStatus performRefactoring(Refactoring ref, boolean providesUndo, boolean performOnFail) {
        IUndoManager undoManager = getUndoManager()
        if (DESCRIPTOR_TEST) {
            final CreateChangeOperation create = new CreateChangeOperation(
                new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS), RefactoringStatus.FATAL)
            create.run(new NullProgressMonitor())
            RefactoringStatus checkingStatus = create.getConditionCheckingStatus()
            if (checkingStatus.hasError())
                return checkingStatus
            Change change = create.getChange()
            ChangeDescriptor descriptor = change.getDescriptor()
            if (descriptor instanceof RefactoringChangeDescriptor) {
                RefactoringChangeDescriptor rcd = (RefactoringChangeDescriptor) descriptor
                RefactoringDescriptor refactoringDescriptor = rcd.getRefactoringDescriptor()
                if (refactoringDescriptor instanceof JavaRefactoringDescriptor) {
                    JavaRefactoringDescriptor jrd = (JavaRefactoringDescriptor) refactoringDescriptor
                    RefactoringStatus validation = jrd.validateDescriptor()
                    if (validation.hasError() && !performOnFail)
                        return validation
                    RefactoringStatus refactoringStatus = new RefactoringStatus()
                    Class<? extends JavaRefactoringDescriptor> expected = jrd.getClass()
                    RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(jrd.getID())
                    jrd = (JavaRefactoringDescriptor) contribution.createDescriptor(jrd.getID(), jrd.getProject(),
                        jrd.getDescription(), jrd.getComment(), contribution.retrieveArgumentMap(jrd), jrd.getFlags())
                    assert jrd.getClass() == expected
                    ref = jrd.createRefactoring(refactoringStatus)
                    if (refactoringStatus.hasError() && !performOnFail)
                        return refactoringStatus
                    TestRenameParticipantSingle.reset()
                }
            }
        }
        final CreateChangeOperation create = new CreateChangeOperation(
            new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS), RefactoringStatus.FATAL)
        final PerformChangeOperation perform = new PerformChangeOperation(create)
        perform.setUndoManager(undoManager, ref.getName())
        IWorkspace workspace = ResourcesPlugin.getWorkspace()
        executePerformOperation(perform, workspace)
        RefactoringStatus status = create.getConditionCheckingStatus()
        if ((!status.hasError() && !performOnFail) || (status.hasError() && performOnFail))
            return status
        assert perform.changeExecuted() : 'Change was not executed'
        Change undo = perform.getUndoChange()
        if (providesUndo) {
            assert undo != null : 'Undo does not exist'
            assert undoManager.anythingToUndo() : 'Undo manager is empty'
        } else {
            assert undo == null : 'Undo manager contains undo but should not'
        }
        return null
    }

    protected final void executePerformOperation(final PerformChangeOperation perform, IWorkspace workspace) {
        workspace.run(perform, new NullProgressMonitor())
    }

    protected final RefactoringStatus performRefactoringWithStatus(Refactoring ref, boolean performOnFail) {
        RefactoringStatus status = performRefactoring(ref, performOnFail)
        if (status == null)
            return new RefactoringStatus()
        return status
    }

    protected final Change performChange(Refactoring refactoring, boolean storeUndo) {
        CreateChangeOperation create = new CreateChangeOperation(refactoring)
        PerformChangeOperation perform = new PerformChangeOperation(create)
        if (storeUndo) {
            perform.setUndoManager(getUndoManager(), refactoring.getName())
        }
        ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor())
        assert perform.changeExecuted() : 'Change was not executed'
        return perform.getUndoChange()
    }

    protected final Change performChange(final Change change) {
        PerformChangeOperation perform = new PerformChangeOperation(change)
        ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor())
        assert perform.changeExecuted() : 'Change was not executed'
        return perform.getUndoChange()
    }

    protected final IUndoManager getUndoManager() {
        IUndoManager undoManager = RefactoringCore.getUndoManager()
        undoManager.flush()
        return undoManager
    }

    protected final IType getType(ICompilationUnit cu, String name) {
        for (type in cu.allTypes) {
            if (type.getTypeQualifiedName('.' as char) == name || type.elementName == name) {
                return type
            }
        }
    }

    protected final String createTestFileName(String cuName, String infix) {
        return testPath + test.methodName + infix + cuName + '.groovy'
    }

    protected final String getInputTestFileName(String cuName) {
        return createTestFileName(cuName, TEST_INPUT_INFIX)
    }

    /*
     * @param subDirName example 'p/' or 'org/eclipse/jdt/'
     */
    protected final String getInputTestFileName(String cuName, String subDirName) {
        return createTestFileName(cuName, TEST_INPUT_INFIX + subDirName)
    }

    protected final String getOutputTestFileName(String cuName) {
        return createTestFileName(cuName, TEST_OUTPUT_INFIX)
    }

    /*
     * @param subDirName example 'p/' or 'org/eclipse/jdt/'
     */
    protected final String getOutputTestFileName(String cuName, String subDirName) {
        return createTestFileName(cuName, TEST_OUTPUT_INFIX + subDirName)
    }

    protected final ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName) {
        return createCUfromTestFile(pack, cuName, true)
    }

    protected final ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName, String subDirName) {
        return createCUfromTestFile(pack, cuName, subDirName, true)
    }

    protected final ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName, boolean input) {
        String contents = input ? getFileContents(getInputTestFileName(cuName)) : getFileContents(getOutputTestFileName(cuName))
        return createCU(pack, cuName + '.groovy', contents)
    }

    protected final ICompilationUnit createCUfromTestFile(IPackageFragment pack, String cuName, String subDirName, boolean input) {
        String contents = input ? getFileContents(getInputTestFileName(cuName, subDirName))
            : getFileContents(getOutputTestFileName(cuName, subDirName))
        return createCU(pack, cuName + '.groovy', contents)
    }

    //--------------------------------------------------------------------------

    protected static ICompilationUnit createCU(IPackageFragment pack, String name, String contents) {
        assert !pack.getCompilationUnit(name).exists()
        ICompilationUnit cu = pack.createCompilationUnit(name, contents, true, null)
        cu.save(null, true)
        return cu
    }

    protected static String getFileContents(String fileName) {
        return FrameworkUtil.getBundle(RefactoringTestCase).getEntry('/resources/' + fileName).openStream().text
    }

    /*protected static IField[] getFields(IType type, String[] names) {
        if (names == null) return new IField[0]
        Set<IField> fields = new HashSet<IField>()
        for (int i = 0; i < names.length; i++) {
            IField field = type.getField(names[i])
            assert field.exists() : 'field ' + field.getElementName() + ' does not exist'
            fields.add(field)
        }
        return fields.toArray(new IField[fields.size()])
    }

    protected static IType[] getMemberTypes(IType type, String[] names) {
        if (names == null) return new IType[0]
        Set<IType> memberTypes = new HashSet<IType>()
        for (int i = 0; i < names.length; i++) {
            IType memberType
            if (names[i].indexOf('.') != -1) {
                String[] path = names[i].split(/\./)
                memberType = type.getType(path[0])
                for (int j = 1; j < path.length; j++) {
                    memberType = memberType.getType(path[j])
                }
            } else {
                memberType = type.getType(names[i])
            }
            assert memberType.exists() : 'member type ' + memberType.getElementName() + ' does not exist'
            memberTypes.add(memberType)
        }
        return memberTypes.toArray(new IType[memberTypes.size()])
    }*/

    protected static IMethod[] getMethods(IType type, String[] names, String[][] signatures) {
        if (names == null || signatures == null)
            return new IMethod[0]
        List<IMethod> methods = []
        for (int i = 0; i < names.length; i += 1) {
            IMethod method = type.getMethod(names[i], signatures[i])
            assert method.exists() : 'method ' + method.getElementName() + ' does not exist'
            if (!methods.contains(method)) {
                methods.add(method)
            }
        }
        return methods as IMethod[]
    }

    /*protected static IType[] findTypes(IType[] types, String[] namesOfTypesToPullUp) {
        List<IType> found = new ArrayList<IType>(types.length)
        for (int i = 0; i < types.length; i++) {
            IType type = types[i]
            for (int j = 0; j < namesOfTypesToPullUp.length; j++) {
                String name = namesOfTypesToPullUp[j]
                if (type.getElementName().equals(name))
                    found.add(type)
            }
        }
        return found.toArray(new IType[found.size()])
    }

    protected static IField[] findFields(IField[] fields, String[] namesOfFieldsToPullUp) {
        List<IField> found = new ArrayList<IField>(fields.length)
        for (int i = 0; i < fields.length; i++) {
            IField field = fields[i]
            for (int j = 0; j < namesOfFieldsToPullUp.length; j++) {
                String name = namesOfFieldsToPullUp[j]
                if (field.getElementName().equals(name))
                    found.add(field)
            }
        }
        return found.toArray(new IField[found.size()])
    }

    protected static IMethod[] findMethods(IMethod[] selectedMethods, String[] namesOfMethods, String[][] signaturesOfMethods) {
        List<IMethod> found = new ArrayList<IMethod>(selectedMethods.length)
        for (int i = 0; i < selectedMethods.length; i++) {
            IMethod method = selectedMethods[i]
            String[] paramTypes = method.getParameterTypes()
            for (int j = 0; j < namesOfMethods.length; j++) {
                String methodName = namesOfMethods[j]
                if (!methodName.equals(method.getElementName()))
                    continue
                String[] methodSig = signaturesOfMethods[j]
                if (!areSameSignatures(paramTypes, methodSig))
                    continue
                found.add(method)
            }
        }
        return found.toArray(new IMethod[found.size()])
    }

    private static boolean areSameSignatures(String[] s1, String[] s2) {
        if (s1.length != s2.length)
            return false
        for (int i = 0; i < s1.length; i++) {
            if (!s1[i].equals(s2[i]))
                return false
        }
        return true
    }*/

    /**
     * Line-based version of junit.framework.Assert.assertEquals(String, String)
     * without considering line delimiters.
     *
     * @param expected the expected value
     * @param actual the actual value
     */
    protected static void assertEqualLines(String expected, String actual) {
        assertEqualLines('', expected, actual)
    }

    /**
     * Line-based version of junit.framework.Assert.assertEquals(String, String, String)
     * without considering line delimiters.
     *
     * @param message the message
     * @param expected the expected value
     * @param actual the actual value
     */
    protected static void assertEqualLines(String message, String expected, String actual) {
        String[] expectedLines = Strings.convertIntoLines(expected)
        String[] actualLines = Strings.convertIntoLines(actual)
        String expected2 = (expectedLines == null ? null : Strings.concatenate(expectedLines, '\n'))
        String actual2 = (actualLines == null ? null : Strings.concatenate(actualLines, '\n'))
        org.junit.Assert.assertEquals(message, expected2, actual2)
    }
}
