/*
 * Copyright 2009-2023 the original author or authors.
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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.builder.EfficiencyCompilerRequestor;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.groovy.core.tests.GroovyBundle;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.core.JavaModelManager;
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

    protected final void fullBuild(final IPath projectPath) {
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

    protected final void incrementalBuild(final IPath projectPath) {
        debugRequestor.clearResult();
        debugRequestor.activate();
        try {
            env.incrementalBuild(projectPath);
        } finally {
            debugRequestor.deactivate();
        }
    }

    protected void executeClass(final IPath projectPath, final String className, final String expectingOutput, final String expectedError) {
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
            Assert.fail("unexpected error : " + actualError);
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

    protected final void expectingCompiledClasses(final String... expected) {
        String[] actual = ReflectionUtils.executePrivateMethod(debugRequestor.getClass(), "getCompiledClasses", debugRequestor);
        Arrays.sort(actual);
        Arrays.sort(expected);
        Assert.assertArrayEquals(expected, actual);
    }

    protected final void expectingProblemsFor(final IPath root, final List<String> expected) {
        expectingProblemsFor(new IPath[] {root}, expected);
    }

    protected final void expectingProblemsFor(final IPath[] roots, final List<String> expected) {
        Problem[] allProblems = getSortedProblems(roots);
        TestCase.assertStringEquals(toString(expected), toString(Arrays.asList(allProblems)), false);
    }

    protected final void expectingNoProblems() {
        expectingNoProblemsFor(env.getWorkspaceRootPath());
    }

    protected final void expectingNoProblemsFor(final IPath... roots) {
        Problem[] allProblems = getSortedProblems(roots);
        TestCase.assertStringEquals("", toString(Arrays.asList(allProblems)), false);
    }

    protected final void expectingSpecificProblemFor(final IPath root, final Problem problem) {
        expectingSpecificProblemsFor(root, new Problem[] {problem});
    }

    protected void expectingSpecificProblemsFor(final IPath root, final Problem[] problems) {
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

    protected final void printProblemsFor(final IPath... roots) {
        for (IPath root : roots) {
            Problem[] problems = env.getProblemsFor(root);
            System.out.println(toString(Arrays.asList(problems)));
            System.out.println();
        }
    }

    private Problem[] getSortedProblems(final IPath[] roots) {
        List<Problem> allProblems = new ArrayList<>();
        for (IPath root : roots) {
            Collections.addAll(allProblems, env.getProblemsFor(root));
        }
        if (allProblems.size() > 1) {
            allProblems.sort(null);
        }
        return allProblems.toArray(new Problem[0]);
    }

    private static String toString(final Iterable<?> seq) {
        StringBuilder buf = new StringBuilder();
        for (Object obj : seq) {
            buf.append(obj).append('\n');
        }
        return buf.toString();
    }

    //--------------------------------------------------------------------------

    protected static class TestingEnvironment extends org.eclipse.jdt.core.tests.builder.TestingEnvironment {

        @Override
        public IPath addProject(final String projectName) {
            return addProject(projectName, "11");
        }

        @Override
        public IPath addProject(final String projectName, final String compliance) {
            try {
                IPath projectPath = super.addProject(projectName, compliance);
                removePackageFragmentRoot(projectPath, "");
                addPackageFragmentRoot(projectPath, "src");
                addGroovyNature(projectName);

                // add JRE container to classpath
                IClasspathAttribute[] attributes;
                if (JavaCore.compareJavaVersions(compliance, "9") < 0 || !GroovyBundle.isAtLeastGroovy(40)) {
                    attributes = new IClasspathAttribute[0];
                } else {
                    attributes = new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")};
                }
                addEntry(projectPath, JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath(), new IAccessRule[0], attributes, false));

                return projectPath;
            } catch (JavaModelException e) {
                throw new RuntimeException(e);
            }
        }

        public void addGroovyNature(final String projectName) {
            try {
                IProject project = getProject(projectName);
                IProjectDescription description = project.getDescription();
                description.setNatureIds(new String[] {JavaCore.NATURE_ID, GroovyNature.GROOVY_NATURE});
                project.setDescription(description, null);
            } catch (CoreException e) {
                handleCoreException(e);
            }
        }

        public void removeGroovyNature(final String projectName) {
            try {
                IProject project = getProject(projectName);
                IProjectDescription description = project.getDescription();
                description.setNatureIds(new String[] {JavaCore.NATURE_ID});
                project.setDescription(description, null);
            } catch (CoreException e) {
                handleCoreException(e);
            }
        }

        public void addGroovyJars(final IPath projectPath) throws Exception {
            boolean minimal = false, modular = false;
            for (IClasspathEntry cpe : getJavaProject(projectPath).getRawClasspath()) {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER &&
                        cpe.getPath().segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
                    for (IClasspathAttribute cpa : cpe.getExtraAttributes()) {
                        if (IClasspathAttribute.MODULE.equals(cpa.getName()) && "true".equals(cpa.getValue())) {
                            modular = true;
                            break;
                        }
                    }
                }
            }

            addEntry(projectPath, GroovyRuntime.newGroovyClasspathContainerEntry(minimal, modular, null));
        }

        /**
         * @param jarPath resource in builder tests project, e.g. "lib/xyz.jar"
         */
        public void addJar(final IPath projectPath, final String jarPath) throws Exception {
            URL jar = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.builder").getEntry(jarPath);
            addExternalJar(projectPath, FileLocator.resolve(jar).getFile());
        }

        @Override
        public void addEntry(final IPath projectPath, final IClasspathEntry entry) throws JavaModelException {
            if (Arrays.stream(getClasspath(projectPath)).noneMatch(entry::equals)) {
                super.addEntry(projectPath, entry);
            }
        }

        public IPath addGroovyClass(final IPath packagePath, final String className, final String contents) {
            IPath filePath = packagePath.append(className.endsWith(".groovy") ? className : className + ".groovy");
            createFile(filePath, contents.getBytes(StandardCharsets.US_ASCII));

            return filePath;
        }

        public IPath addGroovyClass(final IPath packageFragmentRootPath, final String packageName, final String className, final String contents) {
            if (packageName == null || packageName.length() < 1) {
                return addGroovyClass(packageFragmentRootPath, className, contents);
            }
            return addGroovyClass(addPackage(packageFragmentRootPath, packageName), className, contents);
        }

        public <U extends ICompilationUnit> U getUnit(final IPath path) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            @SuppressWarnings("unchecked")
            U unit = (U) JavaCore.createCompilationUnitFrom(file);
            return unit;
        }
    }
}
