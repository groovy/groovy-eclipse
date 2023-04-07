/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AbstractCompilerTest extends TestCase {

	public static final int F_1_3 = 0x01;
	public static final int F_1_4 = 0x02;
	public static final int F_1_5 = 0x04;
	public static final int F_1_6 = 0x08;
	public static final int F_1_7 = 0x10;
	public static final int F_1_8 = 0x20;
	public static final int F_9   = 0x40;
	public static final int F_10  = 0x80;
	public static final int F_11  = 0x100;
	public static final int F_12  = 0x200;
	public static final int F_13  = 0x400;
	public static final int F_14  = 0x800;
	public static final int F_15  = 0x1000;
	public static final int F_16  = 0x2000;
	public static final int F_17  = 0x4000;
	public static final int F_18  = 0x8000;
	public static final int F_19  = 0x10000;
	public static final int F_20  = 0x20000;

	public static final boolean RUN_JAVAC = CompilerOptions.ENABLED.equals(System.getProperty("run.javac"));
	public static final boolean PERFORMANCE_ASSERTS = !CompilerOptions.DISABLED.equals(System.getProperty("jdt.performance.asserts"));
	private static final int UNINITIALIZED = -1;
	private static final int NONE = 0;
	private static int possibleComplianceLevels = UNINITIALIZED;

	protected long complianceLevel;
	protected boolean enableAPT = false;
	protected boolean enablePreview = false;
	protected static boolean isJRE9Plus = false; // Stop gap, so tests need not be run at 9, but some tests can be adjusted for JRE 9
	protected static boolean isJRE10Plus = false;
	protected static boolean isJRE11Plus = false;
	protected static boolean isJRE12Plus = false;
	protected static boolean isJRE13Plus = false;
	protected static boolean isJRE14Plus = false;
	protected static boolean isJRE15Plus = false;
	protected static boolean isJRE16Plus = false;
	protected static boolean isJRE17Plus = false;
	protected static boolean isJRE18Plus = false;
	protected static boolean isJRE19Plus = false;
	protected static boolean isJRE20Plus = false;
	protected static boolean reflectNestedClassUseDollar;

	public static int[][] complianceTestLevelMapping = new int[][] {
		new int[] {F_1_3, ClassFileConstants.MAJOR_VERSION_1_3},
		new int[] {F_1_4, ClassFileConstants.MAJOR_VERSION_1_4},
		new int[] {F_1_5, ClassFileConstants.MAJOR_VERSION_1_5},
		new int[] {F_1_6, ClassFileConstants.MAJOR_VERSION_1_6},
		new int[] {F_1_7, ClassFileConstants.MAJOR_VERSION_1_7},
		new int[] {F_1_8, ClassFileConstants.MAJOR_VERSION_1_8},
		new int[] {F_9, ClassFileConstants.MAJOR_VERSION_9},
		new int[] {F_10, ClassFileConstants.MAJOR_VERSION_10},
		new int[] {F_11, ClassFileConstants.MAJOR_VERSION_11},
		new int[] {F_12, ClassFileConstants.MAJOR_VERSION_12},
		new int[] {F_13, ClassFileConstants.MAJOR_VERSION_13},
		new int[] {F_14, ClassFileConstants.MAJOR_VERSION_14},
		new int[] {F_15, ClassFileConstants.MAJOR_VERSION_15},
		new int[] {F_16, ClassFileConstants.MAJOR_VERSION_16},
		new int[] {F_17, ClassFileConstants.MAJOR_VERSION_17},
		new int[] {F_18, ClassFileConstants.MAJOR_VERSION_18},
		new int[] {F_19, ClassFileConstants.MAJOR_VERSION_19},
		new int[] {F_20, ClassFileConstants.MAJOR_VERSION_20},
	};

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
		for (int[] map : complianceTestLevelMapping) {
			if ((complianceLevels & map[0]) != 0) {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.getComplianceLevelForJavaVersion(map[1])));
			}
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

		for (int[] map : complianceTestLevelMapping) {
			if ((complianceLevels & map[0]) != 0) {
				suite.addTest(buildComplianceTestSuite(testClasses, setupClass, ClassFileConstants.getComplianceLevelForJavaVersion(map[1])));
			}
		}
		return suite;
	}

	 public static void setpossibleComplianceLevels(int complianceLevel) {
         possibleComplianceLevels = complianceLevel;
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
			complianceSuite = (TestSuite)constructor.newInstance(new Object[]{Long.valueOf(complianceLevel)});
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
		for (int[] map : complianceTestLevelMapping) {
			if ((complianceLevels & map[0]) != 0) {
				long complianceLevelForJavaVersion = ClassFileConstants.getComplianceLevelForJavaVersion(map[1]);
				checkCompliance(evaluationTestClass, minimalCompliance, suite, complianceLevels, map[0], map[1], getVersionString(complianceLevelForJavaVersion));
			}
		}
		return suite;
	}
	private static void checkCompliance(Class evaluationTestClass, int minimalCompliance, TestSuite suite,
			int complianceLevels, int abstractCompilerTestCompliance, int classFileConstantsVersion, String release) {
		int lev = complianceLevels & abstractCompilerTestCompliance;
		if (lev != 0) {
			if (lev < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance " + release + "!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, ClassFileConstants.getComplianceLevelForJavaVersion(classFileConstantsVersion)));
			}
		}
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
			if (highestLevel == ClassFileConstants.JDK10)
				complianceString = "10";
			else if (highestLevel == ClassFileConstants.JDK9)
				complianceString = "9";
			else if (highestLevel == ClassFileConstants.JDK1_8)
				complianceString = "1.8";
			else if (highestLevel == ClassFileConstants.JDK1_7)
				complianceString = "1.7";
			else if (highestLevel == ClassFileConstants.JDK1_6)
				complianceString = "1.6";
			else if (highestLevel == ClassFileConstants.JDK1_5)
				complianceString = "1.5";
			else if (highestLevel == ClassFileConstants.JDK1_4)
				complianceString = "1.4";
			else if (highestLevel == ClassFileConstants.JDK1_3)
				complianceString = "1.3";
			else {
				highestLevel = ClassFileConstants.getLatestJDKLevel();
				if (highestLevel > 0) {
					complianceString = CompilerOptions.versionFromJdkLevel(highestLevel);
				} else {
					complianceString = "unknown";
				}

			}

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
		int size = complianceTestLevelMapping.length;
		for(int i = size - 1; i >= 0; i-- ) {
			int[] map = complianceTestLevelMapping[i];
			if ((complianceLevels & map[0]) != 0) {
				return ClassFileConstants.getComplianceLevelForJavaVersion(map[1]);
			}
		}
		return ClassFileConstants.JDK1_3;
	}

	static void initReflectionVersion() {
		if (isJRE9Plus) {
			reflectNestedClassUseDollar = true;
			System.out.println("reflectNestedClassUseDollar="+reflectNestedClassUseDollar+" due to isJRE9Plus");
		} else {
			String version = System.getProperty("java.version");
			if (version.startsWith("1.8.0_")) {
				int build = Integer.parseInt(version.substring("1.8.0_".length()));
				reflectNestedClassUseDollar = build >= 171;
			} else if (version.startsWith("1.8.0-")) {
				// Some versions start with 1.8.0- but don't have build qualifier.
				// Just assume they are > 171 build. Nothing much can be done.
				reflectNestedClassUseDollar = true;
			} else {
				throw new IllegalStateException("Unrecognized Java version: "+version);
			}
			System.out.println("reflectNestedClassUseDollar="+reflectNestedClassUseDollar+" based on version="+version);
		}
	}

	/*
	 * Returns the possible compliance levels this VM instance can run.
	 */
	public static int getPossibleComplianceLevels() {
		if (possibleComplianceLevels == UNINITIALIZED) {
			String specVersion = System.getProperty("java.specification.version");
			// During the EA phase of development, the above property is set to the
			// latest version, for e.g. "20", but the java.version that will be tested
			// inside initReflectionVersion() later on will be of the format "20-ea" thus
			// causing an exception. The following code will ensure that we ignore such cases
			// until the latest version has been properly added in CompilerOptions.
			int spec = Integer.parseInt(specVersion);
			if (spec > Integer.parseInt(CompilerOptions.getLatestVersion())) {
				specVersion = CompilerOptions.getLatestVersion();
			}
			isJRE20Plus = CompilerOptions.VERSION_20.equals(specVersion);
			isJRE19Plus = isJRE20Plus || CompilerOptions.VERSION_19.equals(specVersion);
			isJRE18Plus = isJRE19Plus || CompilerOptions.VERSION_18.equals(specVersion);
			isJRE17Plus = isJRE18Plus || CompilerOptions.VERSION_17.equals(specVersion);
			isJRE16Plus = isJRE17Plus || CompilerOptions.VERSION_16.equals(specVersion);
			isJRE15Plus = isJRE16Plus || CompilerOptions.VERSION_15.equals(specVersion);
			isJRE14Plus = isJRE15Plus || CompilerOptions.VERSION_14.equals(specVersion);
			isJRE13Plus = isJRE14Plus || CompilerOptions.VERSION_13.equals(specVersion);
			isJRE12Plus = isJRE13Plus || CompilerOptions.VERSION_12.equals(specVersion);
			isJRE11Plus = isJRE12Plus || CompilerOptions.VERSION_11.equals(specVersion);
			isJRE10Plus = isJRE11Plus || CompilerOptions.VERSION_10.equals(specVersion);
			isJRE9Plus = isJRE10Plus || CompilerOptions.VERSION_9.equals(specVersion);
			initReflectionVersion();
			String key = "compliance.jre." + specVersion;
			String compliances = System.getProperty(key);
			if (compliances == null) {
				compliances = System.getProperty("compliance");
			}
			if (compliances != null) {
				possibleComplianceLevels = 0;
				for (String compliance : compliances.split(",")) {
					if (CompilerOptions.VERSION_1_3.equals(compliance)) {
						possibleComplianceLevels |= RUN_JAVAC ? NONE : F_1_3;
					} else if (CompilerOptions.VERSION_1_4.equals(compliance)) {
						possibleComplianceLevels |= RUN_JAVAC ? NONE : F_1_4;
					}
					boolean versionValid = false;
					for(int i = 0; i < complianceTestLevelMapping.length; i++) {
						int[] versionMap = complianceTestLevelMapping[i];
						if (versionMap[0] < F_1_5) continue;
						long jdkLevel = ClassFileConstants.getComplianceLevelForJavaVersion(versionMap[1]);
						String versionString = CompilerOptions.versionFromJdkLevel(jdkLevel);
						if (versionString.equals(compliance)) {
							possibleComplianceLevels |= versionMap[0];
							versionValid = true;
							break;
						}
					}
					if (!versionValid) {
						System.out.println("Ignoring invalid compliance (" + compliance + ")");
						System.out.print("Use one of ");
						System.out.print(CompilerOptions.VERSION_1_3 + ", ");
						System.out.print(CompilerOptions.VERSION_1_4 + ", ");
						System.out.print(CompilerOptions.VERSION_1_5 + ", ");
						System.out.print(CompilerOptions.VERSION_1_6 + ", ");
						System.out.print(CompilerOptions.VERSION_1_7 + ", ");
						System.out.print(CompilerOptions.VERSION_1_8 + ", ");
						System.out.print(CompilerOptions.VERSION_1_8 + ", ");
						System.out.print(CompilerOptions.VERSION_9 + ", ");
						System.out.print(CompilerOptions.VERSION_10 + ", ");
						System.out.print(CompilerOptions.VERSION_11 + ", ");
						System.out.print(CompilerOptions.VERSION_12 + ", ");
						System.out.print(CompilerOptions.VERSION_13 + ", ");
						System.out.println(CompilerOptions.VERSION_14 + ", ");
						System.out.println(CompilerOptions.VERSION_15 + ", ");
						System.out.println(CompilerOptions.VERSION_16 + ", ");
						System.out.println(CompilerOptions.VERSION_17 + ", ");
						System.out.println(CompilerOptions.VERSION_18 + ", ");
						System.out.println(CompilerOptions.VERSION_19 + ", ");
						System.out.println(CompilerOptions.VERSION_20);
					}
				}
				if (possibleComplianceLevels == 0) {
					System.out.println("Defaulting to all possible compliances");
					possibleComplianceLevels = UNINITIALIZED;
				}
			}
			if (possibleComplianceLevels == UNINITIALIZED) {
				if (!RUN_JAVAC) {
					possibleComplianceLevels = F_1_3;
					boolean canRunPrevious = !"1.0".equals(specVersion)
						&& !CompilerOptions.VERSION_1_1.equals(specVersion)
						&& !CompilerOptions.VERSION_1_2.equals(specVersion)
						&& !CompilerOptions.VERSION_1_3.equals(specVersion);
					if (canRunPrevious) {
						possibleComplianceLevels |= F_1_4;
					}
					String previousVersion = CompilerOptions.VERSION_1_4;
					for(int i = 0; i < complianceTestLevelMapping.length; i++) {
						int[] versionMap = complianceTestLevelMapping[i];
						if (versionMap[0] < F_1_5) continue;
						long jdkLevel = ClassFileConstants.getComplianceLevelForJavaVersion(versionMap[1]);
						String versionString = CompilerOptions.versionFromJdkLevel(jdkLevel);
						boolean canRunNext = canRunPrevious && !previousVersion.equals(specVersion);
						if (canRunNext) {
							possibleComplianceLevels |= versionMap[0];
						} else {
							break;
						}
						previousVersion = versionString;
					}
				} else if ("1.0".equals(specVersion)
							|| CompilerOptions.VERSION_1_1.equals(specVersion)
							|| CompilerOptions.VERSION_1_2.equals(specVersion)
							|| CompilerOptions.VERSION_1_3.equals(specVersion)
							|| CompilerOptions.VERSION_1_4.equals(specVersion)) {
					possibleComplianceLevels = NONE;
				} else {
					for(int i = 0; i < complianceTestLevelMapping.length; i++) {
						int[] versionMap = complianceTestLevelMapping[i];
						if (versionMap[0] < F_1_5) continue;
						long jdkLevel = ClassFileConstants.getComplianceLevelForJavaVersion(versionMap[1]);
						String versionString = CompilerOptions.versionFromJdkLevel(jdkLevel);
						if (versionString.equals(specVersion)) {
							possibleComplianceLevels |= versionMap[0];
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
			TESTS_COUNTERS.put(testClass.getName(), Integer.valueOf(suite.countTestCases()));
		} else {
			suite = new TestSuite(CompilerOptions.versionFromJdkLevel(complianceLevel));
			for (int i = 0, length = testClasses.size(); i < length; i++) {
				TestSuite innerSuite = new TestSuite(testClass = (Class)testClasses.get(i));
				TESTS_COUNTERS.put(testClass.getName(), Integer.valueOf(innerSuite.countTestCases()));
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
		TESTS_COUNTERS.put(className, Integer.valueOf(newTestsNb));
		return suite;
	}


	public static boolean isJRELevel(int compliance) {
		return (AbstractCompilerTest.getPossibleComplianceLevels() & compliance) != 0;
	}

	public String decorateAnnotationValueLiteral(String val) {
		if (!isJRE9Plus) {
			return val;
		}
		StringBuilder builder = new StringBuilder(val);
		builder.insert(0, "\"");
		builder.append("\"");
		return builder.toString();
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
		} else if (this.complianceLevel == ClassFileConstants.JDK1_8) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		} else if (this.complianceLevel == ClassFileConstants.JDK9) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_9);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_9);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_9);
		} else if (this.complianceLevel == ClassFileConstants.JDK10) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_10);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_10);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_10);
		} else {
			// This is already good enough to cover versions from future
			// (as long as versionFromJdkLevel does its job)
			String ver = CompilerOptions.versionFromJdkLevel(this.complianceLevel);
			options.put(CompilerOptions.OPTION_Compliance, ver);
			options.put(CompilerOptions.OPTION_Source, ver);
			options.put(CompilerOptions.OPTION_TargetPlatform, ver);
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

	protected static String getVersionString(long compliance) {
		String version = "version 17 : 61.0";
		if (compliance < ClassFileConstants.JDK9) return "version 1.8 : 52.0";
		if (compliance == ClassFileConstants.JDK9) return "version 9 : 53.0";
		if (compliance == ClassFileConstants.JDK10) return "version 10 : 54.0";
		if (compliance > ClassFileConstants.JDK10) {
			String ver = CompilerOptions.versionFromJdkLevel(compliance);
			int major = Integer.parseInt(ver) + ClassFileConstants.MAJOR_VERSION_0;
			return "version " + ver + " : " + major + ".0";
		}
		if (compliance >= ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_LATEST_VERSION))
			return version;
		return version;
	}

	public void initialize(CompilerTestSetup setUp) {
		this.complianceLevel = setUp.complianceLevel;
		this.enableAPT = System.getProperty("enableAPT") != null;
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
