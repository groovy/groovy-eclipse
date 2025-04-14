/*******************************************************************************
 * Copyright (c) 2024, 2025 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import junit.framework.Test;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class ModuleImportTests extends AbstractModuleCompilationTest {

	static {
//		 TESTS_NAMES = new String[] { "test001" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	public ModuleImportTests(String name) {
		super(name);
	}

	// ========= OPT-IN to run.javac mode: ===========
	@Override
	protected void setUp() throws Exception {
		this.runJavacOptIn = true;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.runJavacOptIn = false; // do it last, so super can still clean up
	}
	// =================================================

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_23);
	}

	public static Class<?> testClass() {
		return ModuleImportTests.class;
	}

	@Override
	protected StringBuilder trimJavacLog(StringBuilder log) {
		// suppress preview warnings from javac:
		if (log.length() > 6 && log.substring(0, 6).equals("Note: ")) {
			StringBuilder filtered = new StringBuilder();
			filtered.append(
				log.toString().lines()
					.filter(line -> !line.equals("Note: Recompile with -Xlint:preview for details."))
					.filter(line -> !(line.startsWith("Note: ") && line.endsWith(" preview features of Java SE 24.")))
					.collect(Collectors.joining("\n")));
			return filtered;
		}
		return log;
	}

	public void test000_previewDisabled() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -24 \"" + getSourceDir() +  File.separator + "module-info.java\" "
	        + "\"" + getSourceDir() +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)
					import module java.sql;
					              ^^^^^^^^
				Module Import Declarations is a preview feature and disabled by default. Use --enable-preview to enable
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 6)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
	        true,
	        "disabled");
	}

	public void test000_previewWrongVersion() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -22 --enable-preview \"" + getSourceDir() +  File.separator + "module-info.java\" "
	        + "\"" + getSourceDir() +  File.separator + "p/X.java\"",
	        "",
	        """
				Preview of features is supported only at the latest source level
				""",
	        true,
	        "only supported for release 24");
	}

	public void test001_simpleOK() throws IOException, ClassFormatException {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import /*ignoreme*/ module java.sql;
					@SuppressWarnings("preview")
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					"""
	        },
			" -24 --enable-preview \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
		verifyClassFile("version 24 : 68.65535", "p/X.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void test002_moduleNotRead() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -24 --enable-preview \"" + getSourceDir() +  File.separator + "module-info.java\" "
	        + "\"" + getSourceDir() +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)
					import module java.sql;
					              ^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)
					import module java.sql;
					              ^^^^^^^^
				Module mod.one does not read module java.sql
				----------
				3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 6)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				3 problems (2 errors, 1 warning)
				""",
	        true,
	        "read");
	}

	public void test003_unresolvableModule() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module missing;
					@SuppressWarnings("preview")
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -24 --enable-preview \"" + getSourceDir() +  File.separator + "module-info.java\" "
	        + "\"" + getSourceDir() +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)
					import module missing;
					              ^^^^^^^
				The import missing cannot be resolved
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 7)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
	        true,
	        "imported module not found");
	}

	public void test004_selfImport_OK() throws IOException, ClassFormatException {
		String modsDir = getSourceDir() +  File.separator + "mods";
		String modOneDir = modsDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, modOneDir + File.separator + "p1", "X1.java",
				"""
					package p1;
					public class X1 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p2", "X2.java",
				"""
					package p2;
					public class X2 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p", "X.java",
				"""
					package p;
					import module mod.one;
					@SuppressWarnings("preview")
					public class X {
						X1 x1;
						X2 x2;
					}
					""");
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
						exports p2 to mod.one;
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview ");
		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		String classFile = String.join(File.separator, "p", "X.class");
		verifyClassFile("version 24 : 68.65535", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test005_selfImport_NOK() {
		String modsDir = getSourceDir() +  File.separator + "mods";
		String modOneDir = modsDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, modOneDir + File.separator + "p1", "X1.java",
				"""
					package p1;
					public class X1 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p", "X.java",
				"""
					package p;
					import module mod.one;
					@SuppressWarnings("preview")
					public class X {
						X1 x1;
					}
					""");
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1 to mod.other;
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview ");

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mods/mod.one/p/X.java (at line 5)
					X1 x1;
					^^
				X1 cannot be resolved to a type
				----------
				1 problem (1 error)
				""",
				"cannot find symbol"); // javac additionally reports warning: [module] module not found: mod.other
	}

	public void test006_selfImportInModule() throws IOException, ClassFormatException {
		String modsDir = getSourceDir()+  File.separator + "mods";
		String modOneDir = modsDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, modOneDir + File.separator + "api", "IService.java",
				"""
					package api;
					public interface IService {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "impl", "ServiceImpl.java",
				"""
					package impl;
					import api.IService;
					public class ServiceImpl implements IService {
						public static IService provider() { return new ServiceImpl(); }
					}
					""");
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					import module mod.one;
					@SuppressWarnings("preview")
					module mod.one {
						exports api;
						exports impl to mod.one;
						provides IService with ServiceImpl;
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview ");
		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		verifyClassFile("version 24 : 68.65535", "module-info.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void test007_shadowing() throws IOException, ClassFormatException {
		String srcDir = getSourceDir();
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, srcDir + File.separator + "p1", "Connection.java",
				"""
					package p1;
					public class Connection {
						public void foo() {}
					}
					""");
		writeFileCollecting(files, srcDir, "module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					""");
		writeFileCollecting(files, srcDir + File.separator + "p2", "Client.java",
				"""
					package p2;
					import module java.sql;
					import p1.Connection;
					@SuppressWarnings("preview")
					class Client {
						void m(Connection c, ConnectionBuilder builder) { // ConnectionBuiler is from java.sql
							c.foo(); // ensure we select p1.Connection
						}
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview ");

		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		String classFile = String.join(File.separator, "p2", "Client.class");
		verifyClassFile("version 24 : 68.65535", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test008_shadowing() throws IOException, ClassFormatException {
		String srcDir = getSourceDir();
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, srcDir + File.separator + "p1", "Connection.java",
				"""
					package p1;
					public class Connection {
						public void foo() {}
					}
					""");
		writeFileCollecting(files, srcDir, "module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					""");
		writeFileCollecting(files, srcDir + File.separator + "p2", "Client.java",
				"""
					package p2;
					import module java.sql;
					import p1.*;
					@SuppressWarnings({ "preview", "unused" }) // module import is not actually used
					class Client {
						void m(Connection c) {
							c.foo(); // ensure we select p1.Connection
						}
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview ");

		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		String classFile = String.join(File.separator, "p2", "Client.class");
		verifyClassFile("version 24 : 68.65535", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test008_shadowing_static_nested() throws IOException, ClassFormatException {
		String srcDir = getSourceDir() + File.separator + "src";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, srcDir + File.separator + "p1", "Outer.java",
				"""
					package p1;
					public class Outer {
						public static class Connection {
							public void foo() {}
						}
					}
					""");
		writeFileCollecting(files, srcDir, "module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					""");
		writeFileCollecting(files, srcDir + File.separator + "p2", "Client.java",
				"""
					package p2;
					import module java.sql;
					import p1.Outer.*;
					@SuppressWarnings({ "preview", "unused" }) // module import is not actually used
					class Client {
						void m(Connection c) {
							c.foo(); // ensure we select p1.Outer.Connection
						}
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview ");

		runConformModuleTest(files, commandLine, "", "");
		String classFile = String.join(File.separator, "p2", "Client.class");
		verifyClassFile("version 24 : 68.65535", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test009_ambiguous_modules() {
		String srcDir = getSourceDir();
		List<String> files = new ArrayList<>();
		String modOneDir = srcDir + File.separator + "mod.one";
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
						exports p2;
						requires transitive java.sql;
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Connection.java",
				"""
					package p1;
					public class Connection {
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Other.java",
				"""
					package p1;
					public class Other {
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p2", "Other.java",
				"""
					package p2;
					public class Other{
					}
					""");

		String modTwoDir = srcDir + File.separator + "mod.two";
		writeFileCollecting(files, modTwoDir, "module-info.java",
				"""
					module mod.two {
						requires mod.one;
					}
					""");
		writeFileCollecting(files, modTwoDir + File.separator + "p3", "Client.java",
				"""
					package p3;
					import module mod.one;
					@SuppressWarnings("preview")
					class Client {
						Connection conn; // module conflict mod.one java.sql (via requires transitive)
						Other other; // package conflict mod.one/p1 mod.one/p2
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview ");
		commandLine.append(" --module-source-path \"").append(srcDir).append("\"");
		commandLine.append(" -d \"").append(OUTPUT_DIR).append(File.separatorChar).append("bin").append("\"");

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p3/Client.java (at line 5)
						Connection conn; // module conflict mod.one java.sql (via requires transitive)
						^^^^^^^^^^
					The type Connection is ambiguous
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p3/Client.java (at line 6)
						Other other; // package conflict mod.one/p1 mod.one/p2
						^^^^^
					The type Other is ambiguous
					----------
					2 problems (2 errors)
					""",
				"reference to Connection is ambiguous");
	}

	public void test009_ambiguous_modules2() {
		// module conflict via separate module imports based on separate requires directly in mod.two
		String srcDir = getSourceDir();
		List<String> files = new ArrayList<>();
		String modOneDir = srcDir + File.separator + "mod.one";
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Connection.java",
				"""
					package p1;
					public class Connection {
					}
					""");

		String modTwoDir = srcDir + File.separator + "mod.two";
		writeFileCollecting(files, modTwoDir, "module-info.java",
				"""
					module mod.two {
						requires mod.one;
						requires java.sql;
					}
					""");
		writeFileCollecting(files, modTwoDir + File.separator + "p3", "Client.java",
				"""
					package p3;
					import module mod.one;
					import module java.sql;
					@SuppressWarnings("preview")
					class Client {
						Connection conn; // module conflict mod.one java.sql
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview ");
		commandLine.append(" --module-source-path \"").append(srcDir).append("\"");
		commandLine.append(" -d \"").append(OUTPUT_DIR).append(File.separatorChar).append("bin").append("\"");

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p3/Client.java (at line 6)
						Connection conn; // module conflict mod.one java.sql
						^^^^^^^^^^
					The type Connection is ambiguous
					----------
					1 problem (1 error)
					""",
				"reference to Connection is ambiguous");
	}

	public void test010_notAccessible() {
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String modOneDir = srcDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "NoAccess.java",
				"""
					package p1;
					class NoAccess {
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Access.java",
				"""
					package p1;
					public class Access {
					}
					""");
		String modTwoDir = srcDir + File.separator + "mod.two";
		writeFileCollecting(files, modTwoDir, "module-info.java",
				"""
					module mod.two {
						requires mod.one;
					}
					""");
		writeFileCollecting(files, modTwoDir + File.separator + "p2", "Client.java",
				"""
					package p2;
					import module mod.one;
					@SuppressWarnings("preview")
					class Client {
						Access good;
						NoAccess bad;
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview");
		commandLine.append(" --module-source-path ").append(srcDir);
		commandLine.append(" -d ").append(OUTPUT_DIR+File.separator+"bin");

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p2/Client.java (at line 6)
						NoAccess bad;
						^^^^^^^^
					NoAccess cannot be resolved to a type
					----------
					1 problem (1 error)
					""",
				"cannot find symbol");
	}

	public void test011_transitive() throws IOException, ClassFormatException {
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String modOneDir = srcDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Access.java",
				"""
					package p1;
					public class Access {
					}
					""");
		String modTwoDir = srcDir + File.separator + "mod.two";
		writeFileCollecting(files, modTwoDir, "module-info.java",
				"""
					module mod.two {
						requires transitive mod.one;
					}
					""");
		String modThreeDir = srcDir + File.separator + "mod.three";
		writeFileCollecting(files, modThreeDir, "module-info.java",
				"""
					module mod.three {
						requires mod.two;
					}
					""");
		writeFileCollecting(files, modThreeDir + File.separator + "p2", "Client.java",
				"""
					package p2;
					import module mod.two;
					@SuppressWarnings("preview")
					class Client {
						Access good;
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview");
		commandLine.append(" --module-source-path ").append(srcDir);

		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		String classFile = String.join(File.separator, "mod.three", "p2", "Client.class");
		verifyClassFile("version 24 : 68.65535", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test012_redundant() {
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "p", "X.java",
				"""
					package p;
					import module java.sql;
					import module java.sql; // redundant
					@SuppressWarnings("preview")
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
							Zork zork;
						}
					}
					""");
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					""");

		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -24 --enable-preview");

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 3)
						import module java.sql; // redundant
						              ^^^^^^^^
					The import java.sql is never used
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
						Zork zork;
						^^^^
					Zork cannot be resolved to a type
					----------
					2 problems (1 error, 1 warning)
					""",
				"cannot find symbol");
	}

	public void test013_inUnnamedModule() throws IOException, ClassFormatException {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					@SuppressWarnings("preview")
					public class X {
						Connection con = null;
					}
					"""
	        },
			" -24 --enable-preview "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
		verifyClassFile("version 24 : 68.65535", "p/X.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void test014_moduleAsPackageName_regular() {
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "module", "Z.java",
				"""
					package module;
					public class Z {}
					""");
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "test", "X.java",
				"""
					package test;
					import module.Z;
					public class X extends Z {}
					""");
		StringBuilder commandLine = new StringBuilder(" -24 --enable-preview");
		runConformModuleTest(files, commandLine, "", "");
	}


	public void test014_moduleAsPackageName_moduleInfo() {
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "module", "Z.java",
				"""
					package module;
					public class Z {}
					""");
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					import module.Z;
					module one {
						uses Z;
					}
					""");
		StringBuilder commandLine = new StringBuilder(" -24 --enable-preview");
		runConformModuleTest(files, commandLine, "", "");
	}

	public void testIllegalModifierRequiresJavaBase_1() {
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires transitive java.base;
					}
					""");
		runNegativeModuleTest(files,
				new StringBuilder(" --release 24"),
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 2)
					requires transitive java.base;
					         ^^^^^^^^^^^^^^^^^^^^
				Modifier 'transitive' is allowed for dependence on module 'java.base' only when preview is enabled
				----------
				1 problem (1 error)
				""",
				"transitive modifier for java.base are a preview feature");
	}

	public void testIllegalModifierRequiresJavaBase_2() {
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires static java.base;
					}
					""");
		runNegativeModuleTest(files,
				new StringBuilder(" --release 24"),
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 2)
					requires static java.base;
					         ^^^^^^^^^^^^^^^^
				Modifiers are not allowed for dependence on module 'java.base'
				----------
				1 problem (1 error)
				""",
				"modifier static not allowed here");
	}

	public void testIllegalModifierRequiresJavaBase_3() throws IOException, ClassFormatException {
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires transitive java.base;
					}
					""");
		runConformModuleTest(files,
				new StringBuilder(" --release 24 --enable-preview"),
				"",
				"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 2)
					requires transitive java.base;
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				1 problem (1 warning)
				""");
		verifyClassFile("version 24 : 68.65535", "module-info.class", ClassFileBytesDisassembler.SYSTEM,
				this.complianceLevel <= ClassFileConstants.JDK24); // Skipped for javac 24 due to https://bugs.openjdk.org/browse/JDK-8347646 - fixed in 25
	}

	public void testIllegalModifierRequiresJavaBase_4() {
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires static java.base;
					}
					""");
		runNegativeModuleTest(files,
				new StringBuilder(" --release 24 --enable-preview"),
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 2)
					requires static java.base;
					         ^^^^^^^^^^^^^^^^
				Modifiers are not allowed for dependence on module 'java.base'
				----------
				1 problem (1 error)
				""",
				"modifier static not allowed here");
	}


	public void testUseRequiresTransitiveJavaBase() throws IOException, ClassFormatException {
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires transitive java.base;
					}
					""");
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "p1", "Client.java",
				"""
				package p1;
				import module one;
				@SuppressWarnings("preview")
				public class Client {
					BigDecimal num;
				}
				""");
		runConformModuleTest(files,
				new StringBuilder(" --release 24 --enable-preview"),
				"",
				"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 2)
					requires transitive java.base;
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				1 problem (1 warning)
				""");
		verifyClassFile("version 24 : 68.65535", "module-info.class", ClassFileBytesDisassembler.SYSTEM, true); // FIXME skip due to https://bugs.openjdk.org/browse/JDK-8347646
		verifyClassFile("version 24 : 68.65535", "p1/Client.class", ClassFileBytesDisassembler.SYSTEM);
	}

}
