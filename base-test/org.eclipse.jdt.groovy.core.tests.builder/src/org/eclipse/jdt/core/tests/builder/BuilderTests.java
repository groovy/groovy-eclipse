/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.codehaus.jdt.groovy.model.ModuleNodeMapper;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Base class for Java image builder tests
 */
public class BuilderTests extends TestCase {
	protected static boolean DEBUG = false;
	protected static TestingEnvironment env = null;
	protected EfficiencyCompilerRequestor debugRequestor = null;

	private int moduleNodeMapperCacheSize = 0;

	public BuilderTests(String name) {
		super(name);
	}

	protected void cleanBuild() {
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.cleanBuild();
		debugRequestor.deactivate();
	}

	/** Execute the given class. Expecting output and error must be specified - passing null for expectedError means no errors are expected!
	 */
	protected void executeClass(
		IPath projectPath,
		String className,
		String expectingOutput,
		String expectedError) {
		TestVerifier verifier = new TestVerifier(false);
		Vector<String> classpath = new Vector<String>(5);

		IPath workspacePath = env.getWorkspaceRootPath();

		classpath.addElement(workspacePath.append(env.getOutputLocation(projectPath)).toOSString());
		IClasspathEntry[] cp = env.getClasspath(projectPath);
		for (int i = 0; i < cp.length; i++) {
			IPath c = cp[i].getPath();
			String ext = c.getFileExtension();
			if (ext != null && (ext.equals("zip") || ext.equals("jar"))) {

			    // this doesn't work on mac/*nix because device is usually (always?) null
//				if (c.getDevice() == null) {
//					classpath.addElement(workspacePath.append(c).toOSString());
//				} else {
//					classpath.addElement(c.toOSString());
//				}

			    // this will work as long as the jar is contained in the same project
			    if (projectPath.isPrefixOf(c)) {
			        classpath.addElement(workspacePath.append(c).toOSString());
			    } else {
			        classpath.addElement(c.toOSString());
			    }
			}
		}

		verifier.execute(className, classpath.toArray(new String[0]));

		if (DEBUG) {
			System.out.println("ERRORS\n");
			System.out.println(Util.displayString(verifier.getExecutionError()));

			System.out.println("OUTPUT\n");
			System.out.println(Util.displayString(verifier.getExecutionOutput()));
		}
		String actualError = verifier.getExecutionError();

		// workaround pb on 1.3.1 VM (line delimitor is not the platform line delimitor)
		char[] error = actualError.toCharArray();
		actualError = new String(CharOperation.replace(error, System.getProperty("line.separator").toCharArray(), new char[] { '\n' }));

		if (expectedError==null && actualError.length()!=0) {
			if (actualError.trim().endsWith("WARNING: Module [groovy-all] - Unable to load extension class [org.codehaus.groovy.runtime.NioGroovyMethods]")) {
				// Allow this it indicates (usually) running the tests with groovy 2.3 on a pre 1.7 vm
			} else {
				fail("unexpected error : " + actualError);
			}
		}
		if (expectedError!=null && actualError.indexOf(expectedError) == -1) {
			System.out.println("ERRORS\n");
			System.out.println(Util.displayString(actualError));
		}
		if (expectedError!=null) {
			assertTrue("unexpected error : " + actualError + " expected : " + expectedError, actualError.indexOf(expectedError) != -1);
		}

		String actualOutput = verifier.getExecutionOutput();
		if (actualOutput.indexOf(expectingOutput) == -1) {
			System.out.println("OUTPUT\n");
			System.out.println(Util.displayString(actualOutput));
		}
		// strip out carriage return for windoze testing
		int idx=-1;
		while ((idx=actualOutput.indexOf('\r'))!=-1) {
			actualOutput = actualOutput.substring(0,idx)+actualOutput.substring(idx+1);
		}
		assertTrue("unexpected output.\nExpected:\n"+expectingOutput+"\nActual:\n"+actualOutput, actualOutput.indexOf(expectingOutput) != -1);
	}

	protected void expectingParticipantProblems(IPath path, String expected) {
		Problem[] problems = env.getProblemsFor(path, "org.eclipse.jdt.core.tests.compile.problem");
		StringBuffer buf = new StringBuffer();
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
			assertTrue(paths[i] + " is not present", wRoot.append(paths[i]).toFile().exists());
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
			assertTrue(paths[i] + " is present", !wRoot.append(paths[i]).toFile().exists());
	}

	/** Verifies that given classes have been compiled.
	 */
	protected void expectingCompiledClasses(String[] expected) {
		String[] actual = debugRequestor.getCompiledClasses();
		org.eclipse.jdt.internal.core.util.Util.sort(actual);
		org.eclipse.jdt.internal.core.util.Util.sort(expected);
		expectingCompiling(actual, expected, "unexpected recompiled units. lenExpected="+expected.length+" lenActual="+actual.length);
	}

	protected void expectedCompiledClassCount(int expected) {
		int actual = debugRequestor.getCompiledClasses().length;
		assertEquals(expected,actual);
	}

	/**
	 * Verifies that the given classes and no others have been compiled,
	 * but permits the classes to have been compiled more than once.
	 */
	protected void expectingUniqueCompiledClasses(String[] expected) {
		String[] actual = debugRequestor.getCompiledClasses();
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
		expectingCompiling(uniqueActual, expected, "unexpected compiled units");
	}

	/** Verifies that given classes have been compiled in the specified order.
	 */
	protected void expectingCompilingOrder(String[] expected) {
		expectingCompiling(debugRequestor.getCompiledClasses(), expected, "unexpected compiling order");
	}

	private void expectingCompiling(String[] actual, String[] expected, String message) {
		if (DEBUG)
			for (int i = 0; i < actual.length; i++)
				System.out.println(actual[i]);

		StringBuffer actualBuffer = new StringBuffer("{");
		for (int i = 0; i < actual.length; i++) {
			if (i > 0)
				actualBuffer.append(",");
			actualBuffer.append(actual[i]);
		}
		actualBuffer.append('}');
		StringBuffer expectedBuffer = new StringBuffer("{");
		for (int i = 0; i < expected.length; i++) {
			if (i > 0)
				expectedBuffer.append(",");
			expectedBuffer.append(expected[i]);
		}
		expectedBuffer.append('}');
		assertEquals(message, expectedBuffer.toString(), actualBuffer.toString());
	}

	/** Verifies that the workspace has no problems.
	 */
	protected void expectingNoProblems() {
		expectingNoProblemsFor(env.getWorkspaceRootPath());
	}

	protected void expectingNoErrors() {
		expectingNoErrorsFor(env.getWorkspaceRootPath());
	}

	/** Verifies that the given element has no problems.
	 */
	protected void expectingNoProblemsFor(IPath root) {
		expectingNoProblemsFor(new IPath[] { root });
	}

	protected void expectingNoErrorsFor(IPath root) {
		expectingNoErrorsFor(new IPath[] { root });
	}

	/** Verifies that the given elements have no problems.
	 */
	protected void expectingNoProblemsFor(IPath[] roots) {
		StringBuffer buffer = new StringBuffer();
		Problem[] allProblems = allSortedProblems(roots);
		if (allProblems != null) {
			for (int i=0, length=allProblems.length; i<length; i++) {
				buffer.append(allProblems[i]+"\n");
			}
		}
		String actual = buffer.toString();
		assumeEquals("Unexpected problem(s)!!!", "", actual);
	}

	protected void expectingNoErrorsFor(IPath[] roots) {
		StringBuffer buffer = new StringBuffer();
		Problem[] allProblems = allSortedProblems(roots);
		int count = 0;
		if (allProblems != null) {
			for (int i=0, length=allProblems.length; i<length; i++) {
				if (allProblems[i].getSeverity()==IMarker.SEVERITY_ERROR) {
					// TODO could convert task markers into just warnings (or ignore), but this is easier right now...
					if (allProblems[i].toString().indexOf("TODO")==-1) {
					buffer.append(allProblems[i]+"\n");
					count++;
					}
				}
			}
		}
		String actual = buffer.toString();
		assumeEquals("Unexpected problem(s)!!! number="+count, "", actual);
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
		Hashtable<IPath, IPath> actual = new Hashtable<IPath, IPath>(rootProblems.length * 2 + 1);
		for (int i = 0; i < rootProblems.length; i++) {
			IPath culprit = rootProblems[i].getResourcePath();
			actual.put(culprit, culprit);
		}

		for (int i = 0; i < expected.length; i++)
			if (!actual.containsKey(expected[i]))
				assertTrue("missing expected problem with " + expected[i].toString(), false);

		if (actual.size() > expected.length) {
			for (Enumeration<IPath> e = actual.elements(); e.hasMoreElements();) {
				IPath path = e.nextElement();
				boolean found = false;
				for (int i = 0; i < expected.length; ++i) {
					if (path.equals(expected[i])) {
						found = true;
						break;
					}
				}
				if (!found)
					assertTrue("unexpected problem(s) with " + path.toString(), false);
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
			assertTrue("problem not found: " + expectedProblem.toString(), found);
		}
		for (int i = 0; i < rootProblems.length; i++) {
			if(rootProblems[i] != null) {
				printProblemsFor(root);
				assertTrue("unexpected problem: " + rootProblems[i].toString(), false);
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
		assumeEquals("Invalid problem(s)!!!", expected, arrayToString(problems));
	}

	/**
	 * Verifies that the given element has the expected problems.
	 */
	protected void expectingProblemsFor(IPath root, List<String> expected) {
		expectingProblemsFor(new IPath[] { root }, expected);
	}

	/**
	 * Verifies that the given elements have the expected problems.
	 */
	protected void expectingProblemsFor(IPath[] roots, List<String> expected) {
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
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.fullBuild();
		debugRequestor.deactivate();
	}

	/** Batch builds the given project.
	 */
	protected void fullBuild(IPath projectPath) {
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.fullBuild(projectPath);
		debugRequestor.deactivate();
	}

	/** Incrementally builds the given project.
	 */
	protected void incrementalBuild(IPath projectPath) {
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.incrementalBuild(projectPath);
		debugRequestor.deactivate();
	}

	/** Incrementally builds the workspace.
	 */
	protected void incrementalBuild() {
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.incrementalBuild();
		debugRequestor.deactivate();
	}

	protected void printProblems() {
		printProblemsFor(env.getWorkspaceRootPath());
	}

	protected void printProblemsFor(IPath root) {
		printProblemsFor(new IPath[] { root });
	}

	protected void printProblemsFor(IPath[] roots) {
		for (int i = 0; i < roots.length; i++) {
			IPath path = roots[i];

			/* get the leaf problems for this type */
			Problem[] problems = env.getProblemsFor(path);
			System.out.println(arrayToString(problems));
			System.out.println();
		}
	}

	protected String arrayToString(Object[] array) {
		StringBuffer buffer = new StringBuffer();
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
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + getName());

		debugRequestor = new EfficiencyCompilerRequestor();
		Compiler.DebugRequestor = debugRequestor;
		if (env == null) {
			env = new TestingEnvironment();
			env.openEmptyWorkspace();
		}
		env.resetWorkspace();
		env.setAutoBuilding(false);
		this.moduleNodeMapperCacheSize = ModuleNodeMapper.size();
	}

	final protected int getInitialModuleNodeMapperSize() {
		return moduleNodeMapperCacheSize;
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
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
            i++;
            if (i > 20 && wcs != null) {
                fail("Could not delete working copies " + wcs);
            }
        } while (wcs != null && wcs.length > 0);
        assertTrue("ModuleNodeMapper should be empty when there are no working copies", getInitialModuleNodeMapperSize() >= ModuleNodeMapper.size());
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

	public static void assertElements(Set<Object> actualSet, Object... expecteds) {
		HashSet<Object> expectedSet = new HashSet<Object>(Arrays.asList(expecteds));
		StringBuilder msg = new StringBuilder();
		for (Object expected : expectedSet) {
			if (!actualSet.contains(expected)) {
				msg.append("Expected but not found: "+expected+"\n");
			}
		}
		for (Object actual : actualSet) {
			if (!expectedSet.contains(actual)) {
				msg.append("Found but not expected: "+actual+"\n");
			}
		}
		if (!"".equals(msg.toString())) {
			fail(msg.toString());
		}
	}

}
