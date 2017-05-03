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
package org.eclipse.jdt.core.groovy.tests.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.codehaus.jdt.groovy.model.ModuleNodeMapper;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.builder.EfficiencyCompilerRequestor;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.builder.TestingEnvironment;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class BuilderTestSuite {

    protected static TestingEnvironment env;
    protected final int moduleNodeMapperCacheSize = ModuleNodeMapper.size();
    protected EfficiencyCompilerRequestor debugRequestor = new EfficiencyCompilerRequestor();

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
        // Discard primary working copies and copies with owner left from failed tests
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
        JavaCore.setOptions(JavaCore.getDefaultOptions());
    }

    protected final void cleanBuild() {
        debugRequestor.clearResult();
        debugRequestor.activate();
        env.cleanBuild();
        debugRequestor.deactivate();
    }

    protected final void fullBuild() {
        debugRequestor.clearResult();
        debugRequestor.activate();
        env.fullBuild();
        debugRequestor.deactivate();
    }

    protected final void fullBuild(IPath projectPath) {
        debugRequestor.clearResult();
        debugRequestor.activate();
        env.fullBuild(projectPath);
        debugRequestor.deactivate();
    }

    protected final void incrementalBuild() {
        debugRequestor.clearResult();
        debugRequestor.activate();
        env.incrementalBuild();
        debugRequestor.deactivate();
    }

    protected final void incrementalBuild(IPath projectPath) {
        debugRequestor.clearResult();
        debugRequestor.activate();
        env.incrementalBuild(projectPath);
        debugRequestor.deactivate();
    }

    protected void executeClass(IPath projectPath, String className, String expectingOutput, String expectedError) {
        TestVerifier verifier = new TestVerifier(false);
        Vector<String> classpath = new Vector<String>(5);
        IPath workspacePath = env.getWorkspaceRootPath();
        classpath.addElement(workspacePath.append(env.getOutputLocation(projectPath)).toOSString());
        IClasspathEntry[] cp = env.getClasspath(projectPath);
        for (int i = 0; i < cp.length; i++) {
            IPath c = cp[i].getPath();
            String ext = c.getFileExtension();
            if (ext != null && (ext.equals("zip") || ext.equals("jar"))) {
                // this will work as long as the jar is contained in the same project
                if (projectPath.isPrefixOf(c)) {
                    classpath.addElement(workspacePath.append(c).toOSString());
                } else {
                    classpath.addElement(c.toOSString());
                }
            }
        }

        verifier.execute(className, classpath.toArray(new String[0]));

        String actualError = verifier.getExecutionError();

        // workaround pb on 1.3.1 VM (line delimitor is not the platform line delimitor)
        char[] error = actualError.toCharArray();
        actualError = new String(
            CharOperation.replace(error, System.getProperty("line.separator").toCharArray(), new char[] { '\n' }));

        if (expectedError == null && actualError.length() != 0) {
            if (actualError.trim().endsWith(
                "WARNING: Module [groovy-all] - Unable to load extension class [org.codehaus.groovy.runtime.NioGroovyMethods]")) {
                // Allow this it indicates (usually) running the tests with groovy 2.3 on a pre 1.7 vm
            } else {
                Assert.fail("unexpected error : " + actualError);
            }
        }
        if (expectedError != null && actualError.indexOf(expectedError) == -1) {
            System.out.println("ERRORS\n");
            System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualError));
        }
        if (expectedError != null) {
            Assert.assertTrue("unexpected error : " + actualError + " expected : " + expectedError,
                actualError.indexOf(expectedError) != -1);
        }

        String actualOutput = verifier.getExecutionOutput();
        if (actualOutput.indexOf(expectingOutput) == -1) {
            System.out.println("OUTPUT\n");
            System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput));
        }
        // strip out carriage return for windoze testing
        int idx = -1;
        while ((idx = actualOutput.indexOf('\r')) != -1) {
            actualOutput = actualOutput.substring(0, idx) + actualOutput.substring(idx + 1);
        }
        Assert.assertTrue("unexpected output.\nExpected:\n" + expectingOutput + "\nActual:\n" + actualOutput,
            actualOutput.indexOf(expectingOutput) != -1);
    }

    //--------------------------------------------------------------------------

    protected final void expectingCompiledClasses(String... expected) {
        String[] actual = debugRequestor.getCompiledClasses();
        org.eclipse.jdt.internal.core.util.Util.sort(actual);
        org.eclipse.jdt.internal.core.util.Util.sort(expected);
        expectingCompiling(actual, expected, "unexpected recompiled units. lenExpected=" + expected.length + " lenActual=" + actual.length);
    }

    private void expectingCompiling(String[] actual, String[] expected, String message) {
        StringBuilder actualBuffer = new StringBuilder("{");
        for (int i = 0; i < actual.length; i++) {
            if (i > 0)
                actualBuffer.append(",");
            actualBuffer.append(actual[i]);
        }
        actualBuffer.append('}');
        StringBuilder expectedBuffer = new StringBuilder("{");
        for (int i = 0; i < expected.length; i++) {
            if (i > 0)
                expectedBuffer.append(",");
            expectedBuffer.append(expected[i]);
        }
        expectedBuffer.append('}');
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
        if (allProblems != null) {
            TestCase.assertStringEquals("", toString(Arrays.asList(allProblems)), false);
        }
    }

    protected final void expectingSpecificProblemFor(IPath root, Problem problem) {
        expectingSpecificProblemsFor(root, new Problem[] { problem });
    }

    protected void expectingSpecificProblemsFor(IPath root, Problem[] problems) {
        Problem[] rootProblems = env.getProblemsFor(root);
        next : for (int i = 0; i < problems.length; i++) {
            Problem problem = problems[i];
            for (int j = 0; j < rootProblems.length; j++) {
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
        Problem[] allProblems = null;
        for (IPath root : roots) {
            Problem[] problems = env.getProblemsFor(root);
            int length = problems.length;
            if (problems.length != 0) {
                if (allProblems == null) {
                    allProblems = problems;
                } else {
                    int all = allProblems.length;
                    System.arraycopy(allProblems, 0, allProblems = new Problem[all + length], 0, all);
                    System.arraycopy(problems, 0, allProblems, all, length);
                }
            }
        }
        if (allProblems != null) {
            Arrays.sort(allProblems);
        }
        return allProblems;
    }

    private static String toString(List<?> list) {
        StringBuilder buffer = new StringBuilder();
        for (Object item : list) {
            buffer.append(item).append('\n');
        }
        return buffer.toString();
    }
}
