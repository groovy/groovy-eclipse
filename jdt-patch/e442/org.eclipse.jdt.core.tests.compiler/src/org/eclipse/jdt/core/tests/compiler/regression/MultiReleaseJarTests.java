package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;

public class MultiReleaseJarTests extends AbstractBatchCompilerTest {

	private boolean isJRE10 = false;
	public MultiReleaseJarTests(String name) {
		super(name);
		try {
			var valueOf = javax.lang.model.SourceVersion.valueOf("RELEASE_10");
			if (valueOf != null) this.isJRE10 = true;
		} catch (Exception ignore) {
		}
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return MultiReleaseJarTests.class;
	}

	private static String createMultiReleaseJar() throws IOException {
		return createMultiReleaseJar(false);
	}

	/**
	 * Creates a JAR containing:
	 * <pre>
	 * base/Root.class
	 * module-info.class                                  (if {@code moduleDescriptorInRoot})
	 * META-INF/versions/9/internal/VersionedOnly.class
	 * META-INF/versions/9/internal/Precedence.class      (returns {@link Object})
	 * META-INF/versions/9/module-info.class              (otherwise)
	 * META-INF/versions/11/internal/Precedence.class     (returns {@link String})
	 * </pre>
	 */
	private static String createMultiReleaseJar(boolean moduleDescriptorInRoot) throws IOException {
		File baseClasses = new File(LIB_DIR, "mrjar-base");
		File moduleClasses = new File(LIB_DIR, "mrjar-module");
		File version9Classes = new File(LIB_DIR, "mrjar-version9");
		File version11Classes = new File(LIB_DIR, "mrjar-version11");
		File jarContent = new File(LIB_DIR, "mrjar-content");
		Util.createClassFolder(
				new String[] {
						"base/Root.java",
						"package base;\n" +
						"public class Root {\n" +
						"}\n"
				},
				baseClasses.getAbsolutePath(),
				JavaCore.VERSION_1_8);
		Util.createClassFolder(
				new String[] {
						"module-info.java",
						"module org.slf4j {\n" +
						"}\n"
				},
				moduleClasses.getAbsolutePath(),
				JavaCore.VERSION_9);
		Util.createClassFolder(
				new String[] {
						"internal/VersionedOnly.java",
						"package internal;\n" +
						"public class VersionedOnly {\n" +
						"}\n",
						"internal/Precedence.java",
						"package internal;\n" +
						"public class Precedence {\n" +
						"  public Object value() { return null; }\n" +
						"}\n"
				},
				version9Classes.getAbsolutePath(),
				JavaCore.VERSION_9);
		Util.createClassFolder(
				new String[] {
						"internal/Precedence.java",
						"package internal;\n" +
						"public class Precedence {\n" +
						"  public String value() { return \"11\"; }\n" +
						"}\n"
				},
				version11Classes.getAbsolutePath(),
				JavaCore.VERSION_11);

		if (jarContent.exists()) {
			if (!Util.flushDirectoryContent(jarContent)) {
				throw new IOException("Could not clear " + jarContent);
			}
		} else if (!jarContent.mkdirs()) {
			throw new IOException("Could not create " + jarContent);
		}
		Util.copy(baseClasses.getAbsolutePath(), jarContent.getAbsolutePath());
		if (moduleDescriptorInRoot) {
			Util.copy(moduleClasses.getAbsolutePath(), jarContent.getAbsolutePath());
		}
		File version9Content = new File(jarContent, "META-INF" + File.separator + "versions" + File.separator + "9");
		if (!version9Content.mkdirs()) {
			throw new IOException("Could not create " + version9Content);
		}
		Util.copy(version9Classes.getAbsolutePath(), version9Content.getAbsolutePath());
		if (!moduleDescriptorInRoot) {
			Util.copy(moduleClasses.getAbsolutePath(), version9Content.getAbsolutePath());
		}
		File version11Content = new File(jarContent, "META-INF" + File.separator + "versions" + File.separator + "11");
		if (!version11Content.mkdirs()) {
			throw new IOException("Could not create " + version11Content);
		}
		Util.copy(version11Classes.getAbsolutePath(), version11Content.getAbsolutePath());
		Util.createFile(
				new File(jarContent, "META-INF" + File.separator + "MANIFEST.MF").getAbsolutePath(),
				"Manifest-Version: 1.0\n" +
				"Multi-Release: true\n");

		String jarPath = LIB_DIR + File.separator + "slf4j-api-2.0.12.jar";
		Util.zip(jarContent, jarPath);
		return jarPath;
	}

	private static String createJarWithoutMultiReleaseHeader() throws IOException {
		File baseClasses = new File(LIB_DIR, "mrjar-base");
		File version9Classes = new File(LIB_DIR, "mrjar-version9");
		File jarContent = new File(LIB_DIR, "mrjar-content");
		Util.createClassFolder(
				new String[] {
						"base/Root.java",
						"package base;\n" +
						"public class Root {\n" +
						"}\n"
				},
				baseClasses.getAbsolutePath(),
				JavaCore.VERSION_1_8);
		Util.createClassFolder(
				new String[] {
						"module-info.java",
						"module org.slf4j {\n" +
						"}\n"
				},
				version9Classes.getAbsolutePath(),
				JavaCore.VERSION_9);

		if (jarContent.exists()) {
			if (!Util.flushDirectoryContent(jarContent)) {
				throw new IOException("Could not clear " + jarContent);
			}
		} else if (!jarContent.mkdirs()) {
			throw new IOException("Could not create " + jarContent);
		}
		Util.copy(baseClasses.getAbsolutePath(), jarContent.getAbsolutePath());
		File version9Content = new File(jarContent, "META-INF" + File.separator + "versions" + File.separator + "9");
		if (!version9Content.mkdirs()) {
			throw new IOException("Could not create " + version9Content);
		}
		Util.copy(version9Classes.getAbsolutePath(), version9Content.getAbsolutePath());
		Util.createFile(
				new File(jarContent, "META-INF" + File.separator + "MANIFEST.MF").getAbsolutePath(),
				"Manifest-Version: 1.0\n" +
				"Automatic-Module-Name: slf4j.api\n");

		String jarPath = LIB_DIR + File.separator + "missing-multi-release-header.jar";
		Util.zip(jarContent, jarPath);
		return jarPath;
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

	public void test007_moduleDescriptorFromLowerRelease() throws IOException {
		String path = createMultiReleaseJar();
		runConformTest(
			new String[] {
				"src/module-info.java",
				"module consumer {\n" +
				"  requires org.slf4j;\n" +
				"}\n",
				"src/test/X.java",
				"package test;\n" +
				"public class X {\n" +
				"}\n"
			},
			" -d \"" + OUTPUT_DIR + File.separator + "out\"" +
			" \"" + OUTPUT_DIR + File.separator + "src" + File.separator + "module-info.java\"" +
			" \"" + OUTPUT_DIR + File.separator + "src" + File.separator + "test" + File.separator + "X.java\"" +
			" --module-path \"" + path + "\" --release 17 ",
			"",
			"",
			false);
	}

	public void test008_classFromLowerRelease() throws IOException {
		String path = createMultiReleaseJar();
		runConformTest(
			new String[] {
				"src/X.java",
				"import internal.VersionedOnly;\n" +
				"public class X {\n" +
				"  VersionedOnly value;\n" +
				"}\n"
			},
			"\"" + OUTPUT_DIR + File.separator + "src" + File.separator + "X.java\"" +
			" -classpath \"" + path + "\" --release 17 ",
			"",
			"",
			false);
	}

	public void test009_highestCompatibleVersionWins() throws IOException {
		String path = createMultiReleaseJar();
		runConformTest(
			new String[] {
				"src/X.java",
				"import internal.Precedence;\n" +
				"public class X {\n" +
				"  String value = new Precedence().value();\n" +
				"}\n"
			},
			"\"" + OUTPUT_DIR + File.separator + "src" + File.separator + "X.java\"" +
			" -classpath \"" + path + "\" --release 17 ",
			"",
			"",
			false);
	}

	public void test010_moduleDescriptorFallsBackToRoot() throws IOException {
		String path = createMultiReleaseJar(true);
		runConformTest(
			new String[] {
				"src/module-info.java",
				"module consumer {\n" +
				"  requires org.slf4j;\n" +
				"}\n",
				"src/test/X.java",
				"package test;\n" +
				"public class X {\n" +
				"}\n"
			},
			" -d \"" + OUTPUT_DIR + File.separator + "out\"" +
			" \"" + OUTPUT_DIR + File.separator + "src" + File.separator + "module-info.java\"" +
			" \"" + OUTPUT_DIR + File.separator + "src" + File.separator + "test" + File.separator + "X.java\"" +
			" --module-path \"" + path + "\" --release 17 ",
			"",
			"",
			false);
	}

	public void test011_missingHeaderIgnoresVersionedModuleDescriptor() throws IOException {
		String path = createJarWithoutMultiReleaseHeader();
		runConformTest(
			new String[] {
				"src/module-info.java",
				"module consumer {\n" +
				"  requires slf4j.api;\n" +
				"}\n",
				"src/test/X.java",
				"package test;\n" +
				"import base.Root;\n" +
				"public class X {\n" +
				"  Root value;\n" +
				"}\n"
			},
			" -d \"" + OUTPUT_DIR + File.separator + "out\"" +
			" \"" + OUTPUT_DIR + File.separator + "src" + File.separator + "module-info.java\"" +
			" \"" + OUTPUT_DIR + File.separator + "src" + File.separator + "test" + File.separator + "X.java\"" +
			" --module-path \"" + path + "\" --release 9 ",
			"",
			"",
			false);
	}
}
