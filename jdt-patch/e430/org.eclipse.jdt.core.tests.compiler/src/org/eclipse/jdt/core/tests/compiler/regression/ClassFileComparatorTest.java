/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import java.io.*;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;

@SuppressWarnings({ "rawtypes" })
public class ClassFileComparatorTest extends AbstractRegressionTest {

	public ClassFileComparatorTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return ClassFileComparatorTest.class;
	}


	private void compileAndDeploy(String source, String className) {
		File directory = new File(SOURCE_DIRECTORY);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + SOURCE_DIRECTORY);
				return;
			}
		}
		String fileName = SOURCE_DIRECTORY + File.separator + className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		StringBuilder buffer = new StringBuilder();
		buffer
			.append("\"")
			.append(fileName)
			.append("\" -d \"")
			.append(EVAL_DIRECTORY)
			.append("\" -nowarn -g -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(SOURCE_DIRECTORY)
			.append("\"");
		BatchCompiler.compile(buffer.toString(), new PrintWriter(System.out), new PrintWriter(System.err), null/*progress*/);
	}

	private boolean areStructurallyDifferent(String classFile1, String classFile2, boolean orderRequired, boolean excludeSynthetic) {
		FileInputStream stream = null;
		try {
			ClassFileReader reader = ClassFileReader.read(EVAL_DIRECTORY + File.separator + classFile1 + ".class");
			int fileLength;
			File file = new File(EVAL_DIRECTORY + File.separator + classFile2 + ".class");
			byte classFileBytes[] = new byte[fileLength = (int) file.length()];
			stream = new java.io.FileInputStream(file);
			int bytesRead = 0;
			int lastReadSize = 0;
			while ((lastReadSize != -1) && (bytesRead != fileLength)) {
				lastReadSize = stream.read(classFileBytes, bytesRead, fileLength - bytesRead);
				bytesRead += lastReadSize;
			}
			return reader.hasStructuralChanges(classFileBytes, orderRequired, excludeSynthetic);
		} catch(IOException e) {
			return true;
		} catch(ClassFormatException e) {
			return true;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					/* ignore */
				}
			}
		}
	}

	public void test001() {
		try {
			String sourceA001 =
				"public class A001 {\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"  public String toString() {\n" +
				"    return \"hello\";\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA001, "A001");
			String sourceA001_2 =
				"public class A001_2 {\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"  public String toString() {\n" +
				"    return \"hello\";\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA001_2, "A001_2");
			assertTrue(!areStructurallyDifferent("A001", "A001_2", false, false));
		} finally {
			removeTempClass("A001");
		}
	}

	public void test002() {
		try {
			String sourceA002 =
				"public class A002 {\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA002, "A002");
			String sourceA002_2 =
				"public class A002_2 {\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"  public String toString() {\n" +
				"    return \"hello\";\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA002_2, "A002_2");
			assertTrue(areStructurallyDifferent("A002", "A002_2", false, false));
		} finally {
			removeTempClass("A002");
		}
	}

	public void test003() {
		try {
			String sourceA003 =
				"public class A003 {\n" +
				"public static final int II = 5;\n" +
				"}";
			compileAndDeploy(sourceA003, "A003");
			String sourceA003_2 =
				"public class A003_2 {\n" +
				"public static final int II = 6;\n" +
				"}";
			compileAndDeploy(sourceA003_2, "A003_2");
			assertTrue(areStructurallyDifferent("A003", "A003_2", false, false));
		} finally {
			removeTempClass("A003");
		}
	}
	public void test004() {
		try {
			String sourceA004 =
				"public class A004 {\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"  public String toString() {\n" +
				"    return \"hello\";\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA004, "A004");
			String sourceA004_2 =
				"public class A004_2 {\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"  public String toString() {\n" +
				"    return \"hello\";\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA004_2, "A004_2");
			assertTrue(!areStructurallyDifferent("A004", "A004_2", true, true));
		} finally {
			removeTempClass("A004");
		}
	}
	public void test005() {
		try {
			String sourceA005 =
				"public class A005 {\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA005, "A005");
			String sourceA005_2 =
				"public class A005_2 {\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"  public String toString() {\n" +
				"    return \"hello\";\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA005_2, "A005_2");
			assertTrue(areStructurallyDifferent("A005", "A005_2", true, true));
		} finally {
			removeTempClass("A005");
		}
	}
	public void test006() {
		try {
			String sourceA006 =
				"public class A006 {\n" +
				"public static final int II = 5;\n" +
				"}";
			compileAndDeploy(sourceA006, "A006");
			String sourceA006_2 =
				"public class A006_2 {\n" +
				"public static final int II = 6;\n" +
				"}";
			compileAndDeploy(sourceA006_2, "A006_2");
			assertTrue(areStructurallyDifferent("A006", "A006_2", true, true));
		} finally {
			removeTempClass("A006");
		}
	}

	public void test007() {
		try {
			String sourceA007 =
				"public class A007 {\n" +
				"public static final int II = 6;\n" +
				"public Runnable foo() {\n" +
				"\treturn null;\n"+
				"}\n" +
				"}";
			compileAndDeploy(sourceA007, "A007");
			String sourceA007_2 =
				"public class A007_2 {\n" +
				"public static final int II = 6;\n" +
				"public Runnable foo() {\n" +
				"\treturn new Runnable() {public void run() {}};\n"+
				"}\n" +
				"}";
			compileAndDeploy(sourceA007_2, "A007_2");
			assertTrue(!areStructurallyDifferent("A007", "A007_2", true, true));
		} finally {
			removeTempClass("A007");
		}
	}
	public void test008() {
		try {
			String sourceA008 =
				"public class A008 {\n" +
				"private int i = 6;\n" +
				"public int foo() {\n" +
				"\treturn i;\n"+
				"}\n" +
				"}";
			compileAndDeploy(sourceA008, "A008");
			String sourceA008_2 =
				"public class A008_2 {\n" +
				"private int i = 6;\n" +
				"public int foo() {\n" +
				"\treturn 2;\n"+
				"}\n" +
				"}";
			compileAndDeploy(sourceA008_2, "A008_2");
			assertTrue(!areStructurallyDifferent("A008", "A008_2", true, false));
		} finally {
			removeTempClass("A008");
		}
	}

	public void test009() {
		try {
			String sourceA009 =
				"public class A009 {\n" +
				"private int i = 6;\n" +
				"public int foo() {\n" +
				"\tclass A {\n" +
				"\t\tint get() {\n" +
				"\t\t\treturn i;\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\treturn new A().get();\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA009, "A009");
			String sourceA009_2 =
				"public class A009_2 {\n" +
				"private int i = 6;\n" +
				"public int foo() {\n" +
				"\treturn 2;\n"+
				"}\n" +
				"}";
			compileAndDeploy(sourceA009_2, "A009_2");
			assertTrue(areStructurallyDifferent("A009", "A009_2", true, false));
		} finally {
			removeTempClass("A009");
		}
	}
	public void test010() {
		try {
			String sourceA010 =
				"public class A010 {\n" +
				"private int i = 6;\n" +
				"public int foo() {\n" +
				"\tclass A {\n" +
				"\t\tint get() {\n" +
				"\t\t\treturn i;\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\treturn new A().get();\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA010, "A010");
			String sourceA010_2 =
				"public class A010_2 {\n" +
				"private int i = 6;\n" +
				"public int foo() {\n" +
				"\treturn 2;\n"+
				"}\n" +
				"}";
			compileAndDeploy(sourceA010_2, "A010_2");
			assertTrue(!areStructurallyDifferent("A010", "A010_2", true, true));
		} finally {
			removeTempClass("A010");
		}
	}

	public void test011() {
		try {
			String sourceA011 =
				"public class A011 {\n" +
				"private int i = 6;\n" +
				"public int foo() {\n" +
				"\tclass A {\n" +
				"\t\tint get() {\n" +
				"\t\t\treturn i;\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\treturn new A().get();\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA011, "A011");
			String sourceA011_2 =
				"public class A011_2 {\n" +
				"private int i = 6;\n" +
				"public int foo() {\n" +
				"\treturn 2;\n"+
				"}\n" +
				"}";
			compileAndDeploy(sourceA011_2, "A011_2");
			assertTrue(!areStructurallyDifferent("A011", "A011_2", false, true));
		} finally {
			removeTempClass("A011");
		}
	}
	public void test012() {
		try {
			String sourceA012 =
				"public class A012 {\n" +
				"public Class foo() {\n" +
				"\treturn null;\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA012, "A012");
			String sourceA012_2 =
				"public class A012_2 {\n" +
				"public Class foo() {\n" +
				"\treturn A012_2.class;\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA012_2, "A012_2");
			assertTrue(areStructurallyDifferent("A012", "A012_2", false, false));
		} finally {
			removeTempClass("A012");
		}
	}
	public void test013() {
		try {
			String sourceA013 =
				"public class A013 {\n" +
				"public Class foo() {\n" +
				"\treturn null;\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA013, "A013");
			String sourceA013_2 =
				"public class A013_2 {\n" +
				"public Class foo() {\n" +
				"\treturn A013_2.class;\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA013_2, "A013_2");
			assertTrue(!areStructurallyDifferent("A013", "A013_2", false, true));
		} finally {
			removeTempClass("A013");
		}
	}
	public void test014() {
		try {
			String sourceA014 =
				"public class A014 {\n" +
				"public Class foo() {\n" +
				"\treturn null;\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA014, "A014");
			String sourceA014_2 =
				"public class A014_2 {\n" +
				"public Class foo() {\n" +
				"\treturn A014_2.class;\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA014_2, "A014_2");
			assertTrue(!areStructurallyDifferent("A014", "A014_2", true, true));
		} finally {
			removeTempClass("A014");
		}
	}

	public void test015() {
		try {
			String sourceA015 =
				"public class A015 {\n" +
				"	public class B {\n" +
				"	}\n" +
				"  public int foo() {\n" +
				"    return 2;\n" +
				"  }\n" +
				"  public String toString() {\n" +
				"    return \"hello\";\n" +
				"  }\n" +
				"}";
			compileAndDeploy(sourceA015, "A015");
			assertTrue(!areStructurallyDifferent("A015$B", "A015$B", false, false));
		} finally {
			removeTempClass("A015");
		}
	}

	public void test016() {
		try {
			String sourceA016 =
				"public class A016 {\n" +
				"public void boo() {\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA016, "A016");
			String sourceA016_2 =
				"public class A016_2 {\n" +
				"public void foo() {\n" +
				"}\n" +
				"}";
			compileAndDeploy(sourceA016_2, "A016_2");
			assertTrue(areStructurallyDifferent("A016", "A016_2", false, false));
		} finally {
			removeTempClass("A016");
		}
	}
}
