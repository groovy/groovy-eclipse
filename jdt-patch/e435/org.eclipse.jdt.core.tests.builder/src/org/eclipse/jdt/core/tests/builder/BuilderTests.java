/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import static org.eclipse.jdt.core.tests.util.AbstractCompilerTest.F_12;
import static org.eclipse.jdt.core.tests.util.AbstractCompilerTest.F_9;
import static org.eclipse.jdt.core.tests.util.AbstractCompilerTest.getPossibleComplianceLevels;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Base class for Java image builder tests
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BuilderTests extends TestCase {
	protected static boolean DEBUG = false;
	protected static TestingEnvironment env = null;
	protected EfficiencyCompilerRequestor debugRequestor = null;

	public BuilderTests(String name) {
		super(name);
	}

	protected void cleanBuild() {
		this.debugRequestor.clearResult();
		this.debugRequestor.activate();
		env.cleanBuild();
		this.debugRequestor.deactivate();
	}

	protected void cleanBuild(String name) {
		this.debugRequestor.clearResult();
		this.debugRequestor.activate();
		env.cleanBuild(name);
		this.debugRequestor.deactivate();
	}

	/** Execute the given class. Expecting output and error must be specified.
	 */
	protected void executeClass(
		IPath projectPath,
		String className,
		String expectingOutput,
		String expectedError) {
		TestVerifier verifier = new TestVerifier(false);
		ArrayList<String> classpath = new ArrayList<>(5);

		IPath workspacePath = env.getWorkspaceRootPath();

		classpath.add(workspacePath.append(env.getOutputLocation(projectPath)).toOSString());
		IClasspathEntry[] cp = env.getClasspath(projectPath);
		for (int i = 0; i < cp.length; i++) {
			IPath c = cp[i].getPath();
			String ext = c.getFileExtension();
			if (ext != null && (ext.equals("zip") || ext.equals("jar"))) { //$NON-NLS-1$ //$NON-NLS-2$
				if (c.getDevice() == null) {
					classpath.add(workspacePath.append(c).toOSString());
				} else {
					classpath.add(c.toOSString());
				}
			}
		}

		verifier.execute(className, classpath.toArray(new String[0]));

		if (DEBUG) {
			System.out.println("ERRORS\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(verifier.getExecutionError()));

			System.out.println("OUTPUT\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(verifier.getExecutionOutput()));
		}
		String actualError = verifier.getExecutionError();

		// workaround pb on 1.3.1 VM (line delimitor is not the platform line delimitor)
		char[] error = actualError.toCharArray();
		actualError = new String(CharOperation.replace(error, System.getProperty("line.separator").toCharArray(), new char[] { '\n' })); //$NON-NLS-1$

		if (actualError.indexOf(expectedError) == -1) {
			System.out.println("ERRORS\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(actualError));
		}
		assertTrue("unexpected error : " + actualError + " expected : " + expectedError, actualError.indexOf(expectedError) != -1); //$NON-NLS-1$ //$NON-NLS-2$

		String actualOutput = verifier.getExecutionOutput();
		if (actualOutput.indexOf(expectingOutput) == -1) {
			System.out.println("OUTPUT\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(actualOutput));
		}
		assertTrue("unexpected output :" + actualOutput + " expected: " + expectingOutput, actualOutput.indexOf(expectingOutput) != -1); //$NON-NLS-1$

	}

	protected void expectingParticipantProblems(IPath path, String expected) {
		Problem[] problems = env.getProblemsFor(path, "org.eclipse.jdt.core.tests.compile.problem");
		StringBuilder buf = new StringBuilder();
		for (int i = 0, length = problems.length; i < length; i++) {
			Problem problem = problems[i];
			buf.append(problem.getMessage());
			if (i < length - 1) buf.append('\n');
		}
		assertEquals("Unexpected problems", expected, buf.toString());
	}

	/** Verifies that given element is not present.
	 */
	protected void expectingPresenceOf(IPath path) {
		expectingPresenceOf(new IPath[] { path });
	}

	/** Verifies that given elements are not present.
	 */
	protected void expectingPresenceOf(IPath[] paths) {
		IPath wRoot = env.getWorkspaceRootPath();

		for (int i = 0; i < paths.length; i++)
			assertTrue(paths[i] + " is not present", wRoot.append(paths[i]).toFile().exists()); //$NON-NLS-1$
	}

	/** Verifies that given element is not present.
	 */
	protected void expectingNoPresenceOf(IPath path) {
		expectingNoPresenceOf(new IPath[] { path });
	}

	/** Verifies that given elements are not present.
	 */
	protected void expectingNoPresenceOf(IPath[] paths) {
		IPath wRoot = env.getWorkspaceRootPath();

		for (int i = 0; i < paths.length; i++)
			assertTrue(paths[i] + " is present", !wRoot.append(paths[i]).toFile().exists()); //$NON-NLS-1$
	}

	/** Verifies that given classes have been compiled.
	 */
	protected void expectingCompiledClasses(String[] expected) {
		String[] actual = this.debugRequestor.getCompiledClasses();
		org.eclipse.jdt.internal.core.util.Util.sort(actual);
		org.eclipse.jdt.internal.core.util.Util.sort(expected);
		expectingCompiling(actual, expected, "unexpected recompiled units"); //$NON-NLS-1$
	}

	/**
	 * Verifies that the given classes and no others have been compiled,
	 * but permits the classes to have been compiled more than once.
	 */
	protected void expectingUniqueCompiledClasses(String[] expected) {
		String[] actual = this.debugRequestor.getCompiledClasses();
		org.eclipse.jdt.internal.core.util.Util.sort(actual);
		// Eliminate duplicate entries
		int dups = 0;
		for (int i = 0; i < actual.length - 1; ++i) {
			if (actual[i + 1].equals(actual[i])) {
				++dups;
				actual[i] = null;
			}
		}
		String[] uniqueActual = new String[actual.length - dups];
		for (int i = 0, j = 0; i < actual.length; ++i) {
			if (actual[i] != null) {
				uniqueActual[j++] = actual[i];
			}
		}
		org.eclipse.jdt.internal.core.util.Util.sort(expected);
		expectingCompiling(uniqueActual, expected, "unexpected compiled units"); //$NON-NLS-1$
	}

	/** Verifies that given classes have been compiled in the specified order.
	 */
	protected void expectingCompilingOrder(String[] expected) {
		expectingCompiling(this.debugRequestor.getCompiledFiles(), expected, "unexpected compiling order"); //$NON-NLS-1$
	}

	private void expectingCompiling(String[] actual, String[] expected, String message) {
		if (DEBUG)
			for (int i = 0; i < actual.length; i++)
				System.out.println(actual[i]);

		StringBuilder actualBuffer = new StringBuilder("{ "); //$NON-NLS-1$
		for (int i = 0; i < actual.length; i++) {
			if (i > 0)
				actualBuffer.append(", "); //$NON-NLS-1$
			actualBuffer.append("\"");
			actualBuffer.append(actual[i]);
			actualBuffer.append("\"");
		}
		actualBuffer.append(" }");
		StringBuilder expectedBuffer = new StringBuilder("{ "); //$NON-NLS-1$
		for (int i = 0; i < expected.length; i++) {
			if (i > 0)
				expectedBuffer.append(", "); //$NON-NLS-1$
			expectedBuffer.append("\"");
			expectedBuffer.append(expected[i]);
			expectedBuffer.append("\"");
		}
		expectedBuffer.append(" }");
		assertEquals(message, expectedBuffer.toString(), actualBuffer.toString());
	}

	/** Verifies that the workspace has no problems.
	 */
	protected void expectingNoProblems() {
		expectingNoProblemsFor(env.getWorkspaceRootPath());
	}

	/** Verifies that the given element has no problems.
	 */
	protected void expectingNoProblemsFor(IPath root) {
		expectingNoProblemsFor(new IPath[] { root });
	}

	/** Verifies that the given elements have no problems.
	 */
	protected void expectingNoProblemsFor(IPath[] roots) {
		StringBuilder buffer = new StringBuilder();
		Problem[] allProblems = allSortedProblems(roots);
		if (allProblems != null) {
			for (int i=0, length=allProblems.length; i<length; i++) {
				buffer.append(allProblems[i]+"\n");
			}
		}
		String actual = buffer.toString();
		assumeEquals("Unexpected problem(s)!!!", "", actual); //$NON-NLS-1$
	}

	/** Verifies that the given element has problems and
	 * only the given element.
	 */
	protected void expectingOnlyProblemsFor(IPath expected) {
		expectingOnlyProblemsFor(new IPath[] { expected });
	}

	/** Verifies that the given elements have problems and
	 * only the given elements.
	 */
	protected void expectingOnlyProblemsFor(IPath[] expected) {
		if (DEBUG)
			printProblems();

		Problem[] rootProblems = env.getProblems();
		Hashtable actual = new Hashtable(rootProblems.length * 2 + 1);
		for (int i = 0; i < rootProblems.length; i++) {
			IPath culprit = rootProblems[i].getResourcePath();
			actual.put(culprit, culprit);
		}

		for (int i = 0; i < expected.length; i++)
			if (!actual.containsKey(expected[i]))
				assertTrue("missing expected problem with " + expected[i].toString(), false); //$NON-NLS-1$

		if (actual.size() > expected.length) {
			for (Enumeration e = actual.elements(); e.hasMoreElements();) {
				IPath path = (IPath) e.nextElement();
				boolean found = false;
				for (int i = 0; i < expected.length; ++i) {
					if (path.equals(expected[i])) {
						found = true;
						break;
					}
				}
				if (!found)
					assertTrue("unexpected problem(s) with " + path.toString(), false); //$NON-NLS-1$
			}
		}
	}

	/** Verifies that the given element has a specific problem and
	 * only the given problem.
	 */
	protected void expectingOnlySpecificProblemFor(IPath root, Problem problem) {
		expectingOnlySpecificProblemsFor(root, new Problem[] { problem });
	}

	/** Verifies that the given element has specifics problems and
	 * only the given problems.
	 */
	protected void expectingOnlySpecificProblemsFor(IPath root, Problem[] expectedProblems) {
		if (DEBUG)
			printProblemsFor(root);

		Problem[] rootProblems = env.getProblemsFor(root);

		for (int i = 0; i < expectedProblems.length; i++) {
			Problem expectedProblem = expectedProblems[i];
			boolean found = false;
			for (int j = 0; j < rootProblems.length; j++) {
				if(expectedProblem.equals(rootProblems[j])) {
					found = true;
					rootProblems[j] = null;
					break;
				}
			}
			if (!found) {
				printProblemsFor(root);
			}
			assertTrue("problem not found: " + expectedProblem.toString(), found); //$NON-NLS-1$
		}
		for (int i = 0; i < rootProblems.length; i++) {
			if(rootProblems[i] != null) {
				printProblemsFor(root);
				assertTrue("unexpected problem: " + rootProblems[i].toString(), false); //$NON-NLS-1$
			}
		}
	}

	/** Verifies that the given element has problems.
	 */
	protected void expectingProblemsFor(IPath root, String expected) {
		expectingProblemsFor(new IPath[] { root }, expected);
	}

	/** Verifies that the given elements have problems.
	 */
	protected void expectingProblemsFor(IPath[] roots, String expected) {
		Problem[] problems = allSortedProblems(roots);
		assumeEquals("Invalid problem(s)!!!", expected, arrayToString(problems)); //$NON-NLS-1$
	}

	/**
	 * Verifies that the given element has the expected problems.
	 */
	protected void expectingProblemsFor(IPath root, List expected) {
		expectingProblemsFor(new IPath[] { root }, expected);
	}

	/**
	 * Verifies that the given elements have the expected problems.
	 */
	protected void expectingProblemsFor(IPath[] roots, List expected) {
		Problem[] allProblems = allSortedProblems(roots);
		assumeEquals("Invalid problem(s)!!!", arrayToString(expected.toArray()), arrayToString(allProblems));
	}

	/** Verifies that the given element has a specific problem.
	 */
	protected void expectingSpecificProblemFor(IPath root, Problem problem) {
		expectingSpecificProblemsFor(root, new Problem[] { problem });
	}

	/** Verifies that the given element has specific problems.
	 */
	protected void expectingSpecificProblemsFor(IPath root, Problem[] problems) {
		if (DEBUG)
			printProblemsFor(root);

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
			/*
			for (int j = 0; j < rootProblems.length; j++) {
				Problem pb = rootProblems[j];
				if (pb != null) {
					System.out.print("got pb:		new Problem(\"" + pb.getLocation() + "\", \"" + pb.getMessage() + "\", \"" + pb.getResourcePath() + "\"");
					System.out.print(", " + pb.getStart() + ", " + pb.getEnd() +  ", " + pb.getCategoryId()+  ", " + pb.getSeverity());
					System.out.println(")");
				}
			}
			*/
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("Missing problem while running test "+getName()+":");
			System.out.println("	- expected : " + problem);
			System.out.println("	- current: " + arrayToString(rootProblems));
			assumeTrue("missing expected problem: " + problem, false);
		}
	}

	/** Batch builds the workspace.
	 */
	protected void fullBuild() {
		this.debugRequestor.clearResult();
		this.debugRequestor.activate();
		env.fullBuild();
		this.debugRequestor.deactivate();
	}

	/** Batch builds the given project.
	 */
	protected void fullBuild(IPath projectPath) {
		this.debugRequestor.clearResult();
		this.debugRequestor.activate();
		env.fullBuild(projectPath);
		this.debugRequestor.deactivate();
	}

	/** Incrementally builds the given project.
	 */
	protected void incrementalBuild(IPath projectPath) {
		this.debugRequestor.clearResult();
		this.debugRequestor.activate();
		env.incrementalBuild(projectPath);
		this.debugRequestor.deactivate();
	}

	/** Incrementally builds the workspace.
	 */
	protected void incrementalBuild() {
		this.debugRequestor.clearResult();
		this.debugRequestor.activate();
		env.incrementalBuild();
		this.debugRequestor.deactivate();
	}

	protected void printProblems() {
		printProblemsFor(env.getWorkspaceRootPath());
	}

	protected void printProblemsFor(IPath... roots) {
		for (IPath path : roots) {
			/* get the leaf problems for this type */
			Problem[] problems = env.getProblemsFor(path);
			System.out.println(arrayToString(problems));
			System.out.println();
		}
	}

	protected String arrayToString(Object[] array) {
		StringBuilder buffer = new StringBuilder();
		int length = array == null ? 0 : array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] != null) {
				if (i > 0) buffer.append('\n');
				buffer.append(array[i].toString());
			}
		}
		return buffer.toString();
	}

	/** Sets up this test.
	 */
	protected void setUp() throws Exception {
		super.setUp();

		this.debugRequestor = new EfficiencyCompilerRequestor();
		Compiler.DebugRequestor = this.debugRequestor;
		if (env == null) {
			env = new TestingEnvironment();
			env.openEmptyWorkspace();
		}
		env.resetWorkspace();

	}
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		env.resetWorkspace();
		JavaCore.setOptions(JavaCore.getDefaultOptions());
		super.tearDown();
	}

	/**
	 * Concatenate and sort all problems for given root paths.
	 *
	 * @param roots The path to get the problems
	 * @return All sorted problems of all given path
	 */
	Problem[] allSortedProblems(IPath[] roots) {
		Problem[] allProblems = null;
		for (int i = 0, max=roots.length; i<max; i++) {
			Problem[] problems = env.getProblemsFor(roots[i]);
			int length = problems.length;
			if (problems.length != 0) {
				if (allProblems == null) {
					allProblems = problems;
				} else {
					int all = allProblems.length;
					System.arraycopy(allProblems, 0, allProblems = new Problem[all+length], 0, all);
					System.arraycopy(problems, 0, allProblems , all, length);
				}
			}
		}
		if (allProblems != null) {
			Arrays.sort(allProblems);
		}
		return allProblems;
	}

	private static Class[] getAllTestClasses() {
		Class[] classes = new Class[] {
			AbstractMethodTests.class,
			BasicBuildTests.class,
			BuildpathTests.class,
			CopyResourceTests.class,
			DependencyTests.class,
			ErrorsTests.class,
			EfficiencyTests.class,
			ExecutionTests.class,
			IncrementalTests.class,
			IncrementalTests18.class,
			MultiProjectTests.class,
			MultiSourceFolderAndOutputFolderTests.class,
			OutputFolderTests.class,
			PackageTests.class,
			StaticFinalTests.class,
			GetResourcesTests.class,
			FriendDependencyTests.class,
			ReferenceCollectionTest.class,
			StateTest.class,
			CompressedWriterTest.class,
			TestAttributeBuilderTests.class,
			Bug530366Test.class,
			Bug531382Test.class,
			Bug549457Test.class,
			Bug564905Test.class,
			Bug561287Test.class,
			Bug562420Test.class,
			LeakTestsBefore9.class,
			Java50Tests.class,
			PackageInfoTest.class,
			ParticipantBuildTests.class,
			AnnotationDependencyTests.class,
			Bug544921Test.class
		};
		List<Class<?>> list = new ArrayList<>(Arrays.asList(classes));
		if (matchesCompliance(F_9)) {
			list.add(LeakTestsAfter9.class);
			list.add(Bug549646Test.class);
		}
		if (matchesCompliance(F_12)) {
			list.add(Bug571363Test.class);
		}
		return list.toArray(new Class[0]);
	}

	static boolean matchesCompliance(int level) {
		int complianceLevels = getPossibleComplianceLevels();
		return complianceLevels >= level;
	}

	public static Test buildTestSuite(Class evaluationTestClass, long ordering) {
		TestSuite suite = new TestSuite(evaluationTestClass.getName());
		List tests = buildTestsList(evaluationTestClass, 0, ordering);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		return suite;
	}

	public static Test buildTestSuite(Class evaluationTestClas) {
		return buildTestSuite(evaluationTestClas, BYTECODE_DECLARATION_ORDER);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(BuilderTests.class.getName());

		// Hack to load all classes before computing their suite of test cases
		// this allow to reset test cases subsets while running all Builder tests...
		Class[] classes = getAllTestClasses();

		// Reset forgotten subsets of tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;

		/* tests */
		for (int i = 0, length = classes.length; i < length; i++) {
			Class clazz = classes[i];
			Method suiteMethod;
			try {
				suiteMethod = clazz.getDeclaredMethod("suite", new Class[0]);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				continue;
			}
			Object test;
			try {
				test = suiteMethod.invoke(null, new Object[0]);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				continue;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				continue;
			}
			suite.addTest((Test) test);
		}

		return suite;
	}

	static IPath addEmptyInternalJar(IPath projectPath, String jarName) throws IOException, JavaModelException {
		IProject project = env.getProject(projectPath);
		String jarFile = project.getLocation().append(jarName).toOSString();
		Util.createEmptyJar(jarFile, CompilerOptions.getFirstSupportedJavaVersion());
		IPath jarPath = null;
		try (FileInputStream fis = new FileInputStream(jarFile)) {
			int length = fis.available();
			byte[] jarContent = new byte[length];
			fis.read(jarContent);
			jarPath = env.addInternalJar(projectPath, jarName, jarContent);
		}
		return jarPath;
	}

	protected static void expectCompileProblem(IPath project, String expectedProblemMessage) {
		List<String> actualProblemMessages = new ArrayList<>();
		Problem[] problems = env.getProblemsFor(project, "org.eclipse.jdt.core.tests.compile.problem");
		if (problems != null) {
			for (Problem problem : problems) {
				actualProblemMessages.add(problem.getMessage());
			}
		}

		List<String> expectedProblemMessages = Arrays.asList(expectedProblemMessage);
		assertEquals("expected compile problem not observed",
				expectedProblemMessages.toString(), actualProblemMessages.toString());
	}

	protected static void expectNoCompileProblems(IPath project) {
		List<String> actualProblemMessages = new ArrayList<>();
		Problem[] problems = env.getProblemsFor(project, "org.eclipse.jdt.core.tests.compile.problem");
		if (problems != null) {
			for (Problem problem : problems) {
				actualProblemMessages.add(problem.getMessage());
			}
		}

		List<String> expectedProblemMessages = Collections.EMPTY_LIST;
		assertEquals("expected no compile problems",
				expectedProblemMessages.toString(), actualProblemMessages.toString());
	}
}
