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
package org.eclipse.jdt.core.tests.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.tests.compiler.regression.RegressionTestSetup;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AbstractCompilerTest extends TestCase {

	public static final int F_1_3 = 0x01;
	public static final int F_1_4 = 0x02;
	public static final int F_1_5 = 0x04;
	public static final int F_1_6 = 0x08;
	public static final int F_1_7 = 0x10;

	public static final boolean RUN_JAVAC = CompilerOptions.ENABLED.equals(System.getProperty("run.javac"));
	private static final int UNINITIALIZED = -1;
	private static final int NONE = 0;
	private static int possibleComplianceLevels = UNINITIALIZED;

	protected long complianceLevel;

	/**
	 * Build a test suite made of test suites for all possible running VM compliances .
	 *
	 * @see #buildUniqueComplianceTestSuite(Class, long) for test suite children content.
	 *
	 * @param evaluationTestClass The main test suite to build.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildAllCompliancesTestSuite(Class evaluationTestClass) {
		TestSuite suite = new TestSuite(evaluationTestClass.getName());
		buildAllCompliancesTestSuite(suite, evaluationTestClass);
		return suite;
	}
	public static void buildAllCompliancesTestSuite(TestSuite suite, Class evaluationTestClass) {
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_5));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_6));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_7) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_7));
		}
	}

	/**
	 * Build a test suite made of test suites for all possible running VM compliances .
	 *
	 * @see #buildComplianceTestSuite(List, Class, long) for test suite children content.
	 *
	 * @param testSuiteClass The main test suite to build.
	 * @param setupClass The compiler setup to class to use to bundle given tets suites tests.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildAllCompliancesTestSuite(Class testSuiteClass, Class setupClass, List testClasses) {
		TestSuite suite = new TestSuite(testSuiteClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, ClassFileConstants.JDK1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, ClassFileConstants.JDK1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, ClassFileConstants.JDK1_5));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, ClassFileConstants.JDK1_6));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_7) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, ClassFileConstants.JDK1_7));
		}
		return suite;
	}

	/**
	 * Build a test suite for a compliance and a list of test suites.
	 * Returned test suite has only one child: {@link RegressionTestSetup} test suite.
	 * Name of returned suite is the given compliance level.
	 *
	 * @see #buildComplianceTestSuite(List, Class, long) for child test suite content.
	 *
	 * @param complianceLevel The compliance level used for this test suite.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildComplianceTestSuite(long complianceLevel, List testClasses) {
		return buildComplianceTestSuite(testClasses, RegressionTestSetup.class, complianceLevel);
	}

	/**
	 * Build a test suite for a compliance and a list of test suites.
	 * Children of returned test suite are setup test suites (see {@link CompilerTestSetup}).
	 * Name of returned suite is the given compliance level.
	 *
	 * @param complianceLevel The compliance level used for this test suite.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	private static Test buildComplianceTestSuite(List testClasses, Class setupClass, long complianceLevel) {
		// call the setup constructor with the compliance level
		TestSuite complianceSuite = null;
		try {
			Constructor constructor = setupClass.getConstructor(new Class[]{long.class});
			complianceSuite = (TestSuite)constructor.newInstance(new Object[]{new Long(complianceLevel)});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		if (complianceSuite == null)
			return null;

		// add tests
		for (int i=0, m=testClasses.size(); i<m ; i++) {
			Class testClass = (Class)testClasses.get(i);
			TestSuite suite = new TestSuite(testClass.getName());
			List tests = buildTestsList(testClass);
			for (int index=0, size=tests.size(); index<size; index++) {
				suite.addTest((Test)tests.get(index));
			}
			complianceSuite.addTest(suite);
		}
		return complianceSuite;
	}

	/**
	 * Build a regression test setup suite for a minimal compliance and a test suite to run.
	 * Returned test suite has only one child: {@link RegressionTestSetup} test suite.
	 * Name of returned suite is the name of given test suite class.
	 * The test suite will be run iff the compliance is at least the specified one.
	 *
	 * @param minimalCompliance The unqie compliance level used for this test suite.
	 * @param evaluationTestClass The test suite to run.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildMinimalComplianceTestSuite(Class evaluationTestClass, int minimalCompliance) {
		TestSuite suite = new TestSuite(evaluationTestClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		int level13 = complianceLevels & AbstractCompilerTest.F_1_3;
		if (level13 != 0) {
			if (level13 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+CompilerOptions.versionFromJdkLevel(ClassFileConstants.JDK1_3)+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_3));
			}
		}
		int level14 = complianceLevels & AbstractCompilerTest.F_1_4;
		if (level14 != 0) {
			if (level14 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+CompilerOptions.versionFromJdkLevel(ClassFileConstants.JDK1_4)+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_4));
			}
		}
		int level15 = complianceLevels & AbstractCompilerTest.F_1_5;
		if (level15 != 0) {
			if (level15 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+CompilerOptions.versionFromJdkLevel(ClassFileConstants.JDK1_5)+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_5));
			}
		}
		int level16 = complianceLevels & AbstractCompilerTest.F_1_6;
		if (level16 != 0) {
			if (level16 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+CompilerOptions.versionFromJdkLevel(ClassFileConstants.JDK1_6)+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_6));
			}
		}
		int level17 = complianceLevels & AbstractCompilerTest.F_1_7;
		if (level17 != 0) {
			if (level17 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+CompilerOptions.versionFromJdkLevel(ClassFileConstants.JDK1_7)+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.JDK1_7));
			}
		}
		return suite;
	}

	/**
	 * Build a regression test setup suite for a compliance and a test suite to run.
	 * Returned test suite has only one child: {@link RegressionTestSetup} test suite.
	 * Name of returned suite is the name of given test suite class.
	 *
	 * @param uniqueCompliance The unique compliance level used for this test suite.
	 * @param evaluationTestClass The test suite to run.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildUniqueComplianceTestSuite(Class evaluationTestClass, long uniqueCompliance) {
		long highestLevel = highestComplianceLevels();
		if (highestLevel < uniqueCompliance) {
			String complianceString;
			if (highestLevel == ClassFileConstants.JDK1_7)
				complianceString = "1.7";
			else if (highestLevel == ClassFileConstants.JDK1_6)
				complianceString = "1.6";
			else if (highestLevel == ClassFileConstants.JDK1_5)
				complianceString = "1.5";
			else if (highestLevel == ClassFileConstants.JDK1_4)
				complianceString = "1.4";
			else if (highestLevel == ClassFileConstants.JDK1_3)
				complianceString = "1.3";
			else
				complianceString = "unknown";
			System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+complianceString+"!");
			return new TestSuite();
		}
		TestSuite complianceSuite = new RegressionTestSetup(uniqueCompliance);
		List tests = buildTestsList(evaluationTestClass);
		for (int index=0, size=tests.size(); index<size; index++) {
			complianceSuite.addTest((Test)tests.get(index));
		}
		return complianceSuite;
	}

	/*
	 * Returns the highest compliance level this VM instance can run.
	 */
	public static long highestComplianceLevels() {
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_7) != 0) {
			return ClassFileConstants.JDK1_7;
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			return ClassFileConstants.JDK1_6;
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			return ClassFileConstants.JDK1_5;
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			return ClassFileConstants.JDK1_4;
		}
		return ClassFileConstants.JDK1_3;
	}

	/*
	 * Returns the possible compliance levels this VM instance can run.
	 */
	public static int getPossibleComplianceLevels() {
		if (possibleComplianceLevels == UNINITIALIZED) {
			String compliance = System.getProperty("compliance");
			if (compliance != null) {
				if (CompilerOptions.VERSION_1_3.equals(compliance)) {
					possibleComplianceLevels = RUN_JAVAC ? NONE : F_1_3;
				} else if (CompilerOptions.VERSION_1_4.equals(compliance)) {
					possibleComplianceLevels = RUN_JAVAC ? NONE : F_1_4;
				} else if (CompilerOptions.VERSION_1_5.equals(compliance)) {
					possibleComplianceLevels = F_1_5;
				} else if (CompilerOptions.VERSION_1_6.equals(compliance)) {
					possibleComplianceLevels = F_1_6;
				} else if (CompilerOptions.VERSION_1_7.equals(compliance)) {
					possibleComplianceLevels = F_1_7;
				} else {
					System.out.println("Invalid compliance specified (" + compliance + ")");
					System.out.print("Use one of ");
					System.out.print(CompilerOptions.VERSION_1_3 + ", ");
					System.out.print(CompilerOptions.VERSION_1_4 + ", ");
					System.out.print(CompilerOptions.VERSION_1_5 + ", ");
					System.out.print(CompilerOptions.VERSION_1_6 + ", ");
					System.out.println(CompilerOptions.VERSION_1_7);
					System.out.println("Defaulting to all possible compliances");
				}
			}
			if (possibleComplianceLevels == UNINITIALIZED) {
				String specVersion = System.getProperty("java.specification.version");
				if (!RUN_JAVAC) {
					possibleComplianceLevels = F_1_3;
					boolean canRun1_4 = !"1.0".equals(specVersion)
						&& !CompilerOptions.VERSION_1_1.equals(specVersion)
						&& !CompilerOptions.VERSION_1_2.equals(specVersion)
						&& !CompilerOptions.VERSION_1_3.equals(specVersion);
					if (canRun1_4) {
						possibleComplianceLevels |= F_1_4;
					}
					boolean canRun1_5 = canRun1_4 && !CompilerOptions.VERSION_1_4.equals(specVersion);
					if (canRun1_5) {
						possibleComplianceLevels |= F_1_5;
					}
					boolean canRun1_6 = canRun1_5 && !CompilerOptions.VERSION_1_5.equals(specVersion);
					if (canRun1_6) {
						possibleComplianceLevels |= F_1_6;
					}
					boolean canRun1_7 = canRun1_6 && !CompilerOptions.VERSION_1_6.equals(specVersion);
					if (canRun1_7) {
						possibleComplianceLevels |= F_1_7;
					}
				} else if ("1.0".equals(specVersion)
							|| CompilerOptions.VERSION_1_1.equals(specVersion)
							|| CompilerOptions.VERSION_1_2.equals(specVersion)
							|| CompilerOptions.VERSION_1_3.equals(specVersion)
							|| CompilerOptions.VERSION_1_4.equals(specVersion)) {
					possibleComplianceLevels = NONE;
				} else {
					possibleComplianceLevels = F_1_5;
					if (!CompilerOptions.VERSION_1_5.equals(specVersion)) {
						possibleComplianceLevels |= F_1_6;
						if (!CompilerOptions.VERSION_1_6.equals(specVersion)) {
							possibleComplianceLevels |= F_1_7;
						}
					}
				}
			}
		}
		if (possibleComplianceLevels == NONE) {
			System.out.println("Skipping all compliances (found none compatible with run.javac=enabled).");
		}
		return possibleComplianceLevels;
	}

	/*
	 * Returns a test suite including the tests defined by the given classes for all possible complianceLevels
	 * and using the given setup class (CompilerTestSetup or a subclass)
	 */
	public static Test suite(String suiteName, Class setupClass, ArrayList testClasses) {
		TestSuite all = new TestSuite(suiteName);
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			all.addTest(suiteForComplianceLevel(ClassFileConstants.JDK1_3, setupClass, testClasses));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			all.addTest(suiteForComplianceLevel(ClassFileConstants.JDK1_4, setupClass, testClasses));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			all.addTest(suiteForComplianceLevel(ClassFileConstants.JDK1_5, setupClass, testClasses));
		}
		return all;
	}

	/*
	 * Returns a test suite including the tests defined by the given classes for the given complianceLevel
	 * (see AbstractCompilerTest for valid values) and using the given setup class (CompilerTestSetup or a subclass)
	 */
	public static Test suiteForComplianceLevel(long complianceLevel, Class setupClass, ArrayList testClasses) {
		// call the setup constructor with the compliance level
		TestSuite suite = null;
		try {
			Constructor constructor = setupClass.getConstructor(new Class[]{String.class});
			suite = (TestSuite)constructor.newInstance(new Object[]{CompilerOptions.versionFromJdkLevel(complianceLevel)});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		if (suite == null)
			return null;

		// add tests
		Class testClass;
		if (testClasses.size() == 1) {
			suite = new TestSuite(testClass = (Class)testClasses.get(0), CompilerOptions.versionFromJdkLevel(complianceLevel));
			TESTS_COUNTERS.put(testClass.getName(), new Integer(suite.countTestCases()));
		} else {
			suite = new TestSuite(CompilerOptions.versionFromJdkLevel(complianceLevel));
			for (int i = 0, length = testClasses.size(); i < length; i++) {
				TestSuite innerSuite = new TestSuite(testClass = (Class)testClasses.get(i));
				TESTS_COUNTERS.put(testClass.getName(), new Integer(innerSuite.countTestCases()));
				suite.addTest(innerSuite);
			}
		}
		return suite;
	}

	public static Test setupSuite(Class clazz) {
		ArrayList testClasses = new ArrayList();
		testClasses.add(clazz);
		return suite(clazz.getName(), RegressionTestSetup.class, testClasses);
	}

	public static Test buildTestSuite(Class evaluationTestClass) {
		if (TESTS_PREFIX != null || TESTS_NAMES != null || TESTS_NUMBERS!=null || TESTS_RANGE !=null) {
			return buildTestSuite(evaluationTestClass, highestComplianceLevels());
		}
		return setupSuite(evaluationTestClass);
	}

	public static Test buildTestSuite(Class evaluationTestClass, long complianceLevel) {
		TestSuite suite = new RegressionTestSetup(complianceLevel);
		List tests = buildTestsList(evaluationTestClass);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		String className = evaluationTestClass.getName();
		Integer testsNb;
		int newTestsNb = suite.countTestCases();
		if ((testsNb = (Integer) TESTS_COUNTERS.get(className)) != null)
			newTestsNb += testsNb.intValue();
		TESTS_COUNTERS.put(className, new Integer(newTestsNb));
		return suite;
	}


	public static boolean isJRELevel(int compliance) {
		return (AbstractCompilerTest.getPossibleComplianceLevels() & compliance) != 0;
	}

	public AbstractCompilerTest(String name) {
		super(name);
	}

	protected Map getCompilerOptions() {
		Map options = new CompilerOptions().getMap();
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		if (this.complianceLevel == ClassFileConstants.JDK1_3) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
		} else if (this.complianceLevel == ClassFileConstants.JDK1_4) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
		} else if (this.complianceLevel == ClassFileConstants.JDK1_5) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		} else if (this.complianceLevel == ClassFileConstants.JDK1_6) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
		} else if (this.complianceLevel == ClassFileConstants.JDK1_7) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
		}
		return options;
	}

	public String getName() {
		String name = super.getName();
		if (this.complianceLevel != 0) {
			name = name + " - " + CompilerOptions.versionFromJdkLevel(this.complianceLevel);
		}
		return name;
	}

	public void initialize(CompilerTestSetup setUp) {
		this.complianceLevel = setUp.complianceLevel;
	}

	protected String testName() {
		return super.getName();
	}

	// Output files management
	protected IPath outputRootDirectoryPath = new Path(Util.getOutputDirectory());
	protected File outputTestDirectory;

	/**
	 * Create a test specific output directory as a subdirectory of
	 * outputRootDirectory, given a subdirectory path. The whole
	 * subtree is created as needed. outputTestDirectoryPath is
	 * modified according to the latest call to this method.
	 * @param suffixPath a valid relative path for the subdirectory
	 */
	protected void createOutputTestDirectory(String suffixPath) {
		this.outputTestDirectory =  new File(this.outputRootDirectoryPath.toFile(), suffixPath);
		if (!this.outputTestDirectory.exists()) {
			this.outputTestDirectory.mkdirs();
		}
	}
	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected void writeFiles(String[] testFiles) {
		createOutputTestDirectory(testName());

		// Write each given test file
		for (int i = 0, length = testFiles.length; i < length; ) {
			String fileName = testFiles[i++];
			String contents = testFiles[i++];
			File file = new File(this.outputTestDirectory, fileName);
			if (fileName.lastIndexOf('/') >= 0) {
				File dir = file.getParentFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			Util.writeToFile(contents, file.getPath());
		}
	}

	// Summary display
	// Used by AbstractRegressionTest for javac comparison tests
	protected static Map TESTS_COUNTERS = new HashMap();
}
