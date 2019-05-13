/*
 * Copyright 2009-2019 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.builder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.builder.EfficiencyCompilerRequestor;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class BuilderTestSuite {

    protected static TestingEnvironment env;
    protected final int moduleNodeMapperCacheSize = ModuleNodeMapper.size();
    protected final EfficiencyCompilerRequestor debugRequestor = new EfficiencyCompilerRequestor();

    @Rule
    public final TestName test = new TestName();

    @Before
    public final void setUpTestCase() {
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + test.getMethodName());

        Compiler.DebugRequestor = debugRequestor;
        if (env == null) {
            env = new TestingEnvironment();
            env.openEmptyWorkspace();
        }
        env.resetWorkspace();
        env.setAutoBuilding(false);
    }

    @After
    public final void tearDownTestCase() {
        env.resetWorkspace();
        try {
            // discard primary working copies and copies with owner left from failed tests
            ICompilationUnit[] wcs = null;
            int i = 0;
            do {
                wcs = JavaModelManager.getJavaModelManager().getWorkingCopies(null, true);
                if (wcs != null) {
                    for (ICompilationUnit workingCopy : wcs) {
                        try {
                            workingCopy.discardWorkingCopy();
                            workingCopy.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                i += 1;
                if (i > 20 && wcs != null) {
                    Assert.fail("Could not delete working copies " + wcs);
                }
            } while (wcs != null && wcs.length > 0);

            Assert.assertTrue("ModuleNodeMapper should be empty when there are no working copies", moduleNodeMapperCacheSize >= ModuleNodeMapper.size());
        } finally {
            JavaCore.setOptions(JavaCore.getDefaultOptions());
        }
    }

    protected final void cleanBuild() {
        debugRequestor.clearResult();
        debugRequestor.activate();
        try {
            env.cleanBuild();
        } finally {
            debugRequestor.deactivate();
        }
    }

    protected final void fullBuild() {
        debugRequestor.clearResult();
        debugRequestor.activate();
        try {
            env.fullBuild();
        } finally {
            debugRequestor.deactivate();
        }
    }

    protected final void fullBuild(IPath projectPath) {
        debugRequestor.clearResult();
        debugRequestor.activate();
        try {
            env.fullBuild(projectPath);
        } finally {
            debugRequestor.deactivate();
        }
    }

    protected final void incrementalBuild() {
        debugRequestor.clearResult();
        debugRequestor.activate();
        try {
            env.incrementalBuild();
        } finally {
            debugRequestor.deactivate();
        }
    }

    protected final void incrementalBuild(IPath projectPath) {
        debugRequestor.clearResult();
        debugRequestor.activate();
        try {
            env.incrementalBuild(projectPath);
        } finally {
            debugRequestor.deactivate();
        }
    }

    protected void executeClass(IPath projectPath, String className, String expectingOutput, String expectedError) {
        List<String> classpath = new ArrayList<>();
        IPath workspacePath = env.getWorkspaceRootPath();
        classpath.add(workspacePath.append(env.getOutputLocation(projectPath)).toOSString());
        IClasspathEntry[] cp = env.getClasspath(projectPath);
        for (IClasspathEntry cpe : cp) {
            IPath c = cpe.getPath();
            if ("jar".equals(c.getFileExtension()) || "zip".equals(c.getFileExtension())) {
                // this will work as long as the jar is contained in the same project
                if (projectPath.isPrefixOf(c)) {
                    classpath.add(workspacePath.append(c).toOSString());
                } else {
                    classpath.add(c.toOSString());
                }
            }
        }

        TestVerifier verifier = new TestVerifier(false);
        verifier.execute(className, classpath.toArray(new String[classpath.size()]));

        String actualError = StringGroovyMethods.normalize((CharSequence) verifier.getExecutionError());
        if (expectedError == null && actualError.length() != 0) {
            if (actualError.trim().endsWith(
                "WARNING: Module [groovy-all] - Unable to load extension class [org.codehaus.groovy.runtime.NioGroovyMethods]")) {
                // allow this it indicates (usually) running the tests with groovy 2.3 on a pre 1.7 vm
            } else {
                Assert.fail("unexpected error : " + actualError);
            }
        }
        if (expectedError != null && actualError.indexOf(expectedError) == -1) {
            System.out.println("ERRORS\n");
            System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualError));
        }
        if (expectedError != null) {
            Assert.assertTrue("unexpected error : " + actualError + " expected : " + expectedError, actualError.indexOf(expectedError) != -1);
        }
        String actualOutput = verifier.getExecutionOutput();
        if (actualOutput.indexOf(expectingOutput) == -1) {
            System.out.println("OUTPUT\n");
            System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput));
        }
        actualOutput = StringGroovyMethods.normalize((CharSequence) actualOutput);
        Assert.assertTrue("unexpected output.\nExpected:\n" + expectingOutput + "\nActual:\n" + actualOutput, actualOutput.indexOf(expectingOutput) != -1);
    }

    //--------------------------------------------------------------------------

    protected final void expectingCompiledClasses(String... expected) {
        String[] actual = ReflectionUtils.executePrivateMethod(debugRequestor.getClass(), "getCompiledClasses", debugRequestor);
        Util.sort(actual);
        Util.sort(expected);
        expectingCompiling(actual, expected, "unexpected recompiled units. lenExpected=" + expected.length + " lenActual=" + actual.length);
    }

    private void expectingCompiling(String[] actual, String[] expected, String message) {
        StringBuilder actualBuffer = new StringBuilder("{");
        for (int i = 0; i < actual.length; i += 1) {
            if (i > 0) actualBuffer.append(",");
            actualBuffer.append(actual[i]);
        }
        actualBuffer.append("}");
        StringBuilder expectedBuffer = new StringBuilder("{");
        for (int i = 0; i < expected.length; i += 1) {
            if (i > 0) expectedBuffer.append(",");
            expectedBuffer.append(expected[i]);
        }
        expectedBuffer.append("}");
        Assert.assertEquals(message, expectedBuffer.toString(), actualBuffer.toString());
    }

    protected final void expectingProblemsFor(IPath root, List<String> expected) {
        expectingProblemsFor(new IPath[] {root}, expected);
    }

    protected final void expectingProblemsFor(IPath[] roots, List<String> expected) {
        Problem[] allProblems = getSortedProblems(roots);
        TestCase.assertStringEquals(toString(expected), toString(Arrays.asList(allProblems)), false);
    }

    protected final void expectingNoProblems() {
        expectingNoProblemsFor(env.getWorkspaceRootPath());
    }

    protected final void expectingNoProblemsFor(IPath... roots) {
        Problem[] allProblems = getSortedProblems(roots);
        TestCase.assertStringEquals("", toString(Arrays.asList(allProblems)), false);
    }

    protected final void expectingSpecificProblemFor(IPath root, Problem problem) {
        expectingSpecificProblemsFor(root, new Problem[] {problem});
    }

    protected void expectingSpecificProblemsFor(IPath root, Problem[] problems) {
        Problem[] rootProblems = env.getProblemsFor(root);
        next: for (int i = 0; i < problems.length; i += 1) {
            Problem problem = problems[i];
            for (int j = 0; j < rootProblems.length; j += 1) {
                Problem rootProblem = rootProblems[j];
                if (rootProblem != null) {
                    if (problem.equals(rootProblem)) {
                        rootProblems[j] = null;
                        continue next;
                    }
                }
            }
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Missing problem while running test " + test.getMethodName() + ":");
            System.out.println("    - expected : " + problem);
            System.out.println("    - current: " + toString(Arrays.asList(rootProblems)));
            Assert.fail("missing expected problem: " + problem);
        }
    }

    protected final void printProblemsFor(IPath... roots) {
        for (IPath root : roots) {
            Problem[] problems = env.getProblemsFor(root);
            System.out.println(toString(Arrays.asList(problems)));
            System.out.println();
        }
    }

    private Problem[] getSortedProblems(IPath[] roots) {
        List<Problem> allProblems = new ArrayList<>();
        for (IPath root : roots) {
            Collections.addAll(allProblems, env.getProblemsFor(root));
        }
        if (allProblems.size() > 1) {
            allProblems.sort(null);
        }
        return allProblems.toArray(new Problem[0]);
    }

    private static String toString(Iterable<?> seq) {
        StringBuilder buf = new StringBuilder();
        for (Object obj : seq) {
            buf.append(obj).append('\n');
        }
        return buf.toString();
    }

    //--------------------------------------------------------------------------

    protected static class TestingEnvironment extends org.eclipse.jdt.core.tests.builder.TestingEnvironment {

        @Override
        public IPath addProject(String projectName) {
            return addProject(projectName, "1.6");
        }

        @Override
        public IPath addProject(String projectName, String compliance) {
            try {
                IPath projectPath = super.addProject(projectName, compliance);

                new ProjectScope(getProject(projectName)).getNode("org.eclipse.jdt.launching")
                    .put("org.eclipse.jdt.launching.PREF_COMPILER_COMPLIANCE_DOES_NOT_MATCH_JRE", JavaCore.IGNORE);

                addEntry(projectPath, JavaRuntime.getDefaultJREContainerEntry());
                addGroovyNature(projectName);
                return projectPath;
            } catch (JavaModelException e) {
                throw new RuntimeException(e);
            }
        }

        public void addGroovyNature(String projectName) {
            try {
                IProject project = getProject(projectName);
                IProjectDescription description = project.getDescription();
                description.setNatureIds(new String[] {JavaCore.NATURE_ID, GroovyNature.GROOVY_NATURE});
                project.setDescription(description, null);
            } catch (CoreException e) {
                handleCoreException(e);
            }
        }

        public void removeGroovyNature(String projectName) {
            try {
                IProject project = getProject(projectName);
                IProjectDescription description = project.getDescription();
                description.setNatureIds(new String[] {JavaCore.NATURE_ID});
                project.setDescription(description, null);
            } catch (CoreException e) {
                handleCoreException(e);
            }
        }

        public void addGroovyJars(IPath projectPath) throws Exception {
            addExternalJar(projectPath, CompilerUtils.getExportedGroovyAllJar().toOSString());
        }

        public void addJar(IPath projectPath, String path) throws Exception {
            URL jar = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.builder").getEntry(path);
            addExternalJar(projectPath, FileLocator.resolve(jar).getFile());
        }

        @Override
        public void addEntry(IPath projectPath, IClasspathEntry entryPath) throws JavaModelException {
            IClasspathEntry[] classpath = getClasspath(projectPath);
            // first look to see if the entry already exists
            for (IClasspathEntry entry : classpath) {
                if (entry.equals(entryPath)) {
                    return;
                }
            }
            super.addEntry(projectPath, entryPath);
        }

        /**
         * Adds a groovy class with the given contents to the given
         * package in the workspace.  The package is created
         * if necessary.  If a class with the same name already
         * exists, it is replaced.  A workspace must be open,
         * and the given class name must not end with ".java".
         * Returns the path of the added class.
         */
        public IPath addGroovyClass(IPath packagePath, String className, String contents) {
            return addGroovyClassExtension(packagePath, className, contents, null);
        }

        /**
         * Adds a groovy class with the given contents to the given
         * package in the workspace.  The package is created
         * if necessary.  If a class with the same name already
         * exists, it is replaced.  A workspace must be open,
         * and the given class name must not end with ".java".
         * Returns the path of the added class.
         */
        public IPath addGroovyClass(IPath packageFragmentRootPath, String packageName, String className, String contents) {
            return addGroovyClassExtension(packageFragmentRootPath, packageName, className, contents, null);
        }

        /**
         * Adds a groovy class with the given contents to the given
         * package in the workspace, the file will use the specified file suffix.
         * The package is created if necessary.  If a class with the same name already
         * exists, it is replaced.
         * Returns the path of the added class.
         */
        public IPath addGroovyClassWithSuffix(IPath packagePath, String className, String suffix, String contents) {
            return addGroovyClassExtension(packagePath, className, suffix, contents, suffix);
        }

        public IPath addGroovyClassWithSuffix(IPath packageFragmentRootPath, String packageName, String className, String suffix, String contents) {
            return addGroovyClassExtension(packageFragmentRootPath, packageName, className, contents, suffix);
        }

        /**
         * Adds a groovy class with the given contents to the given
         * package in the workspace.  The package is created
         * if necessary.  If a class with the same name already
         * exists, it is replaced.  A workspace must be open,
         * and the given class name must not end with ".java".
         * Returns the path of the added class.
         * @param fileExtension file extension of the groovy class to create (without a '.')
         */
        public IPath addGroovyClassExtension(IPath packagePath, String className, String contents, String fileExtension) {
            //checkAssertion("a workspace must be open", fIsOpen);
            if (fileExtension == null) {
                fileExtension = "groovy";
            }
            IPath classPath = packagePath.append(className + "." + fileExtension);
            try {
                createFile(classPath, contents.getBytes("UTF8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Assert.fail("e1");
            }
            return classPath;
        }

        /**
         * Adds a groovy class with the given contents to the given
         * package in the workspace.  The package is created
         * if necessary.  If a class with the same name already
         * exists, it is replaced.  A workspace must be open,
         * and the given class name must not end with ".java".
         * Returns the path of the added class.
         * @param fileExtension file extension of the groovy class to create (without a '.')
         */
        public IPath addGroovyClassExtension(IPath packageFragmentRootPath, String packageName, String className,
            String contents, String fileExtension) {
            // make sure the package exists
            if (packageName != null && packageName.length() > 0) {
                IPath packagePath = addPackage(packageFragmentRootPath, packageName);
                return addGroovyClassExtension(packagePath, className, contents, fileExtension);
            }
            return addGroovyClassExtension(packageFragmentRootPath, className, contents, fileExtension);
        }

        @SuppressWarnings("unchecked")
        public <U extends ICompilationUnit> U getUnit(IPath path) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
            return (U) unit;
        }

        public String readTextFile(IPath path) {
            IFile file = getWorkspace().getRoot().getFile(path);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()))) {
                StringBuilder sb = new StringBuilder(300);
                int read = 0;
                while ((read = br.read()) != -1) {
                    sb.append((char) read);
                }
                return sb.toString();
            } catch (Exception ex) {
                handle(ex);
                return null;
            }
        }
    }
}
