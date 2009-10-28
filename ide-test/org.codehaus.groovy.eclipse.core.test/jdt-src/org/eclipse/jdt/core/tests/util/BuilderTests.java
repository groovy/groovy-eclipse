/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;

/**
 * Base class for Java image builder tests
 */
public class BuilderTests extends TestCase {
	protected static boolean DEBUG = false;
	protected static TestingEnvironment env = null;
	protected EfficiencyCompilerRequestor debugRequestor = null;

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
		Vector classpath = new Vector(5);

		IPath workspacePath = env.getWorkspaceRootPath();

		classpath.addElement(workspacePath.append(env.getOutputLocation(projectPath)).toOSString());
		IClasspathEntry[] cp = env.getClasspath(projectPath);
		for (int i = 0; i < cp.length; i++) {
			IPath c = cp[i].getPath();
			String ext = c.getFileExtension();
			if (ext != null && (ext.equals("zip") || ext.equals("jar"))) { //$NON-NLS-1$ //$NON-NLS-2$
			    
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

		verifier.execute(className, (String[]) classpath.toArray(new String[0]));

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

		if (expectedError==null && actualError.length()!=0) {
			fail("unexpected error : " + actualError);
		}
		if (expectedError!=null && actualError.indexOf(expectedError) == -1) {
			System.out.println("ERRORS\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(actualError));
		}
		if (expectedError!=null) {
			assertTrue("unexpected error : " + actualError + " expected : " + expectedError, actualError.indexOf(expectedError) != -1); //$NON-NLS-1$ //$NON-NLS-2$
		}

		String actualOutput = verifier.getExecutionOutput();
		if (actualOutput.indexOf(expectingOutput) == -1) {
			System.out.println("OUTPUT\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(actualOutput));
		}
		assertTrue("unexpected output", actualOutput.indexOf(expectingOutput) != -1); //$NON-NLS-1$

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
		String[] actual = debugRequestor.getCompiledClasses();
		org.eclipse.jdt.internal.core.util.Util.sort(actual);
		org.eclipse.jdt.internal.core.util.Util.sort(expected);
		expectingCompiling(actual, expected, "unexpected recompiled units"); //$NON-NLS-1$
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
		expectingCompiling(uniqueActual, expected, "unexpected compiled units"); //$NON-NLS-1$
	}

	/** Verifies that given classes have been compiled in the specified order.
	 */
	protected void expectingCompilingOrder(String[] expected) {
		expectingCompiling(debugRequestor.getCompiledClasses(), expected, "unexpected compiling order"); //$NON-NLS-1$
	}

	private void expectingCompiling(String[] actual, String[] expected, String message) {
		if (DEBUG)
			for (int i = 0; i < actual.length; i++)
				System.out.println(actual[i]);

		StringBuffer actualBuffer = new StringBuffer("{"); //$NON-NLS-1$
		for (int i = 0; i < actual.length; i++) {
			if (i > 0)
				actualBuffer.append(","); //$NON-NLS-1$
			actualBuffer.append(actual[i]);
		}
		actualBuffer.append('}');
		StringBuffer expectedBuffer = new StringBuffer("{"); //$NON-NLS-1$
		for (int i = 0; i < expected.length; i++) {
			if (i > 0)
				expectedBuffer.append(","); //$NON-NLS-1$
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

	/** Verifies that the given element has no problems.
	 */
	protected void expectingNoProblemsFor(IPath root) {
		expectingNoProblemsFor(new IPath[] { root });
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
        System.out.println("------------------------------");
        System.out.println("Starting: " + getName());

		debugRequestor = new EfficiencyCompilerRequestor();
		Compiler.DebugRequestor = debugRequestor;
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
        ICompilationUnit[] wcs = new ICompilationUnit[0];
        int i = 0;
        do {
            wcs = JavaCore.getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY);
            for (ICompilationUnit workingCopy : wcs) {
                try {
                    workingCopy.discardWorkingCopy();
                    workingCopy.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            i++;
            if (i > 20) {
                fail("Could not delete working copies " + wcs);
            }
        } while (wcs.length > 0);
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

}
