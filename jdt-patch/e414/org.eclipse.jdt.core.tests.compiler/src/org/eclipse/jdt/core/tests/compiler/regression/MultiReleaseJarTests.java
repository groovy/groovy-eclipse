package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import javax.lang.model.SourceVersion;

import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class MultiReleaseJarTests extends AbstractBatchCompilerTest {

	static {
//		 TESTS_NAMES = new String[] { "test001" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	private boolean isJRE10 = false;
	public MultiReleaseJarTests(String name) {
		super(name);
		try {
			SourceVersion valueOf = SourceVersion.valueOf("RELEASE_10");
			if (valueOf != null) this.isJRE10 = true;
		} catch(Exception e) {
			
		}
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return MultiReleaseJarTests.class;
	}
	public void test001() {
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		runNegativeTest(
			new String[] {
				"src/X.java",
				  "import a.b.c.MultiVersion1.Inner;\n" + 
				  "import p.q.r.MultiVersion2.Inner;\n" + 
				  "public class X {\n" + 
				  "}\n"},
			"\"" + OUTPUT_DIR +  File.separator + "src/X.java\"" +
			" -classpath " + path + " --release 8 ",
			"",
			"----------\n" + 
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 1)\n" + 
			"	import a.b.c.MultiVersion1.Inner;\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The import a.b.c.MultiVersion1.Inner cannot be resolved\n" + 
			"----------\n" + 
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 2)\n" + 
			"	import p.q.r.MultiVersion2.Inner;\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The import p.q.r.MultiVersion2.Inner cannot be resolved\n" + 
			"----------\n" + 
			"2 problems (2 errors)\n",
			false
		   );
	}
	public void test002() {
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		runNegativeTest(
			new String[] {
				"src/X.java",
				  "import a.b.c.MultiVersion1.Inner;\n" + 
				  "import p.q.r.MultiVersion2.Inner;\n" + 
				  "public class X {\n" + 
				  "}\n"},
			"\"" + OUTPUT_DIR +  File.separator + "src/X.java\"" +
			" -classpath " + path + " --release 9 ",
			"",
			"----------\n" + 
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 1)\n" + 
			"	import a.b.c.MultiVersion1.Inner;\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type a.b.c.MultiVersion1.Inner is not visible\n" + 
			"----------\n" + 
			"1 problem (1 error)\n",
			false
		   );
	}
	public void test003() {
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		runConformTest(
			new String[] {
				"src/X.java",
				  "import p.q.r.MultiVersion3.Inner;\n" + 
				  "public class X {\n" +
				  "  Inner i = null;\n" +
				  "  p.q.r.MultiVersion2.Inner i2 = null;\n" +
				  "}\n"},
			"\"" + OUTPUT_DIR +  File.separator + "src/X.java\"" +
			" -classpath " + path + " --release 9 ",
			"",
			"",
			false
		   );
	}
	public void test004() {
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		runNegativeTest(
			new String[] {
				"src/X.java",
				  "import p.q.r.MultiVersion3.Inner;\n" + 
				  "import p.q.r.MultiVersion2.Inner;\n" +
				  "public class X {\n" +
				  "  Inner i = null;\n" +
				  "}\n"},
			"\"" + OUTPUT_DIR +  File.separator + "src/X.java\"" +
			" -classpath " + path + " --release 9 ",
			"",
			"----------\n" + 
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 2)\n" + 
			"	import p.q.r.MultiVersion2.Inner;\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The import p.q.r.MultiVersion2.Inner collides with another import statement\n" + 
			"----------\n" + 
			"1 problem (1 error)\n",
			false
		   );
	}
	public void test005() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		File directory = new File(OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" );
		File out = new File(OUTPUT_DIR +  File.separator + "out" );
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directory.toString());
				return;
			}
		}
		if (!out.exists()) {
			if (!out.mkdirs()) {
				System.out.println("Could not create " + directory.toString());
				return;
			}
		}
		runNegativeTest(
			new String[] {
				"src/MyModule/module-info.java",
				"module MyModule {\n" +
				"  requires Version9;\n" +
				"}",
				"src/MyModule/p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"  java.sql.Connection con = null;\n" +
				"}\n"},
			"  -d \"" + out.toString() + "\" " +
			" --module-source-path \"" + directory.toString() +  "\" " +
			" \"" + OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" + File.separator + "module-info.java\"" +
			" \"" + OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" + File.separator + "p" + File.separator + "X.java\" "  +
			" --module-path " + path + " --release 9 ",
			"",
			"----------\n" + 
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/MyModule/p/X.java (at line 3)\n" + 
			"	java.sql.Connection con = null;\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"The type java.sql.Connection is not accessible\n" + 
			"----------\n" + 
			"1 problem (1 error)\n",
			false
		   );
	}
	public void test006() {
		if (!this.isJRE10) return;
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "multi.jar";
		String[] libs = new String[1];
		libs[0] = path;
		File directory = new File(OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" );
		File out = new File(OUTPUT_DIR +  File.separator + "out" );
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directory.toString());
				return;
			}
		}
		if (!out.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directory.toString());
				return;
			}
		}
		runConformTest(
			new String[] {
				"src/MyModule/module-info.java",
				"module MyModule {\n" +
				"  requires Version10;\n" +
				"}",
				"src/MyModule/p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"  java.sql.Connection con = null;\n" +
				"}\n"},
			"  -d \"" + out.toString() + "\" " +
			" --module-source-path \"" + directory.toString() +  "\" " +
			" \"" + OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" + File.separator + "module-info.java\"" +
			" \"" + OUTPUT_DIR +  File.separator + "src" + File.separator + "MyModule" + File.separator + "p" + File.separator + "X.java\" "  +
			" --module-path " + path + " --release 10 ",
			"",
			"",
			false
		   );
	}
}
