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
package org.codehaus.groovy.eclipse.refactoring.test;

import java.util.Hashtable;

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.TestOptions;

/**
 * Sets up an 1.5 project with rtstubs15.jar and compiler, code formatting, code generation, and template options.
 */
public final class RefactoringTestSetup extends TestSetup {

    public static final String CONTAINER= "src";
    private static IPackageFragmentRoot fgRoot;
    private static IPackageFragment fgPackageP;
    private static IJavaProject fgJavaTestProject;
    private static IPackageFragmentRoot[] fgJRELibraries;
    private static IPackageFragmentRoot fgGroovyLibrary;

    public static IPackageFragmentRoot getDefaultSourceFolder() throws Exception {
        if (fgRoot != null)
            return fgRoot;
        throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
    }

    public static IPackageFragmentRoot[] getJRELibraries() throws Exception {
        if (fgJRELibraries != null)
            return fgJRELibraries;
        throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
    }

    public static IClasspathEntry[] getJRELibrariesAsRawClasspathEntry() throws Exception {
        if (fgJRELibraries != null) {
            IClasspathEntry[] entries = new IClasspathEntry[fgJRELibraries.length];
            for (int i = 0; i < fgJRELibraries.length; i++) {
                entries[i] = fgJRELibraries[i].getRawClasspathEntry();
            }
            return entries;
        }
        throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
    }

    public static IJavaProject getProject()throws Exception {
        if (fgJavaTestProject != null)
            return fgJavaTestProject;
        throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
    }

    public static IPackageFragment getPackageP()throws Exception {
        if (fgPackageP != null)
            return fgPackageP;
        throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
    }

    //--------------------------------------------------------------------------

    private boolean fWasAutobuild;
    private Hashtable<String, String> fWasOptions;

    public RefactoringTestSetup(Test test) {
        super(test);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fWasOptions = JavaCore.getOptions();
        fWasAutobuild= CoreUtility.setAutoBuilding(false);

        Hashtable<String, String> options= TestOptions.getDefaultOptions();
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
        options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "0");
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
        options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, String.valueOf(9999));

        JavaCore.setOptions(options);
        TestOptions.initializeCodeGenerationOptions();
        JavaPlugin.getDefault().getCodeTemplateStore().load();

        StringBuffer comment= new StringBuffer();
        comment.append("/**\n");
        comment.append(" * ${tags}\n");
        comment.append(" */");
        StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORCOMMENT_ID, comment.toString(), null);

        fgJavaTestProject= JavaProjectHelper.createGroovyProject("TestProject"+System.currentTimeMillis(), "bin");
        fgJRELibraries= addRTJars(fgJavaTestProject);
        fgGroovyLibrary= addGroovyJar(fgJavaTestProject);
        // just in case, remove the source root that is the root of the project (if it exists)
        JavaProjectHelper.removeFromClasspath(fgJavaTestProject, fgJavaTestProject.getProject().getFullPath());
        fgRoot= JavaProjectHelper.addSourceContainer(fgJavaTestProject, CONTAINER);
        fgPackageP= fgRoot.createPackageFragment("p", true, null);
    }

    @Override
    protected void tearDown() throws Exception {
        JavaProjectHelper.delete(fgJavaTestProject);
        if (fWasOptions!=null) {
            //Must restore options or it messes up other test running after us.
            JavaCore.setOptions(fWasOptions);
        }
        CoreUtility.setAutoBuilding(fWasAutobuild);
        /*
         * ensure the workbench state gets saved when running with the Automated Testing Framework
         * TODO: remove when https://bugs.eclipse.org/bugs/show_bug.cgi?id=71362 is fixed
         */
        /* Not needed for JDT/UI tests right now.
        StackTraceElement[] elements=  new Throwable().getStackTrace();
        for (int i= 0; i < elements.length; i++) {
            StackTraceElement element= elements[i];
            if (element.getClassName().equals("org.eclipse.test.EclipseTestRunner")) {
                PlatformUI.getWorkbench().close();
                break;
            }
        }
        */
        super.tearDown();
    }

    public IPackageFragmentRoot getGroovyLibrary() {
        return fgGroovyLibrary;
    }

    protected IPackageFragmentRoot[] addRTJars(IJavaProject project) throws Exception {
        return JavaProjectHelper.addRTJars(project);
    }

    protected IPackageFragmentRoot addGroovyJar(IJavaProject project) throws Exception {
        return JavaProjectHelper.addGroovyJar(project);
    }
}
